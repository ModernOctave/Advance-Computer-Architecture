/*****************************************************************************
				Tejas Simulator
------------------------------------------------------------------------------------------------------------

   Copyright [2010] [Indian Institute of Technology, Delhi]
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
------------------------------------------------------------------------------------------------------------

				Contributor: Eldhose Peter
*****************************************************************************/
package memorysystem.nuca;

import generic.Event;
import generic.EventQueue;
import generic.RequestType;

import java.util.HashMap;
import java.util.Vector;

import memorysystem.AddressCarryingEvent;
import memorysystem.CacheLine;
import memorysystem.CoreMemorySystem;
import memorysystem.MESI;
import config.CacheConfig;

public class DNucaBank extends NucaCache implements NucaInterface
{	
	public NucaCache parent;
	public int setId;
	public int myId;
	

	public DNucaBank(String cacheName, int id, CacheConfig cacheParameters,
			CoreMemorySystem containingMemSys, NucaCache nuca)
    {
        super(cacheName , id, cacheParameters, containingMemSys);
        this.parent = nuca;
        this.setId = -1;
        this.mshr = nuca.getMshr();
        this.eventsWaitingOnMSHR = nuca.eventsWaitingOnMSHR;
        activeEventsInNuca = new HashMap<Event, Integer>();

    }

	public void broadcastRequest(long addr, RequestType requestType, AddressCarryingEvent event) {
		AddressCarryingEvent newEvent = null;
		for(Integer bankId : parent.bankSets.get(setId))
		{
			if(bankId == myId)
				continue;//Dont send to itself
			parent.hopCount++;
			DNucaBank destination = (DNucaBank) parent.cacheBank.get(bankId);
			newEvent = new AddressCarryingEvent(this.getEventQueue(), 
					0,
					this,
					destination,
					requestType,
					addr);
			newEvent.dn_status = 1;
			newEvent.parentEvent = event;
			sendEvent(newEvent);
		}
		if(requestType != event.getRequestType())
			misc.Error.showErrorAndExit("Something went wrong");
		activeEventsInNuca.put(event, 0);
	}

	@Override
	public void handleAccess(long addr, RequestType requestType,
			AddressCarryingEvent event) {

		if (requestType == RequestType.Cache_Write) {
			noOfWritesReceived++;
		}
		CacheLine cl = this.accessAndMark(addr);

		// IF HIT
		if (cl != null) {
			cacheHit(addr, requestType, cl, event);
			if(mshr.isAddrInMSHR(addr))
				processEventsInMSHR(addr);
		} else { //Miss in nearest bank
			mshr.addToMSHR(event);
			broadcastRequest(addr, requestType, event);//FIXME: If there is only one bank in bankset
	                                           		   //        then dont broadcast. Go to nextlevel.
		}
	}
	private DNucaBank getMigrateDestination(int currentId, int rootBankId, int setId)
	{
		Vector<Integer> set = parent.bankSets.get(setId);
		int migrateIndex = -1;
		if(set.indexOf(rootBankId) > set.indexOf(currentId))
		{
			migrateIndex = set.get(set.indexOf(currentId) + 1);
		}
		else
		{
			migrateIndex = set.get(set.indexOf(currentId) - 1);
		}
		return (DNucaBank) parent.cacheBank.get(migrateIndex);
	}
	public void doMigration(long addr, RequestType requestType,
			AddressCarryingEvent event, CacheLine cl) 
	{
		parent.hopCount++;
		migrations++;
		DNucaBank migrateDestination = getMigrateDestination(myId, 
				((DNucaBank) event.getRequestingElement()).getMyId(),
				((DNucaBank) event.getRequestingElement()).getSetId());
		//Migrate
		cl.setState(MESI.INVALID); //Invalidation of block in current bank
		AddressCarryingEvent migrateEvent = new AddressCarryingEvent(this.getEventQueue(), 
				0,
				this,
				migrateDestination,
				RequestType.Migrate_Block,
				addr);
		sendEvent(migrateEvent);
		
		AddressCarryingEvent newEvent = new AddressCarryingEvent(this.getEventQueue(), 
				0,
				this,
				event.getRequestingElement(),
				requestType,
				addr);
		newEvent.dn_status = 2;
		newEvent.parentEvent = event.parentEvent;
		sendEvent(newEvent);
	}
	public void handleBroadcastAccess(long addr, RequestType requestType,
			AddressCarryingEvent event) 
	{
		if(!((DNucaBank)(event.parentEvent.getProcessingElement())).activeEventsInNuca.containsKey(
				event.parentEvent) || mshr.getWaitingEventsInMSHR(addr) == null)
		{
			return;//Somebody else has reported the hit
		}
					
		if (requestType == RequestType.Cache_Write) {
			noOfWritesReceived++;
		}
		CacheLine cl = this.accessAndMark(addr);

		if (cl != null) {
			doMigration(addr, requestType, event, cl);			
		}
		else {
			AddressCarryingEvent newEvent = new AddressCarryingEvent(this.getEventQueue(), 
					0,
					this,
					event.getRequestingElement(),
					requestType,
					addr);
			newEvent.dn_status = 3;
			newEvent.parentEvent = event.parentEvent;
			sendEvent(newEvent);
		}
	}
	public void handleAccessDNuca(long addr, RequestType requestType, AddressCarryingEvent event)
	{
		if(event.dn_status == 2) //Hit reply came
		{
			if(!activeEventsInNuca.containsKey(event.parentEvent))
			{
				misc.Error.showErrorAndExit("Hit more than once!!! Multiple copies!!!");
			}
			else{
				activeEventsInNuca.remove(event.parentEvent);
			}
		}
		else if(event.dn_status == 3)//Miss reply came
		{
			if(activeEventsInNuca.containsKey(event.parentEvent))
			{//Till now no bank in the same set replied a hit
				activeEventsInNuca.put(event.parentEvent, activeEventsInNuca.get(event.parentEvent)+1);
				if(activeEventsInNuca.get(event.parentEvent) == parent.bankSets.get(setId).size()-1)
				{//Actual Miss -- Got miss from all the other banks in this bank set
					activeEventsInNuca.remove(event.parentEvent);
					sendRequestToNextLevel(addr, RequestType.Cache_Read);
				}
			}//else means somebody has reported a hit. Leave this reply.
		}
		else
		{//broadcast request received
			handleBroadcastAccess(addr, requestType, event);
		}
	}
	@Override
	public void handleEvent(EventQueue eventQ, Event e) {
		AddressCarryingEvent event = (AddressCarryingEvent) e;
		printCacheDebugMessage(event);
		
		long addr = ((AddressCarryingEvent) event).getAddress();
		RequestType requestType = event.getRequestType();

		if(event.dn_status > 0){//within set
			handleAccessDNuca(addr, requestType, event);
			return;
		}
		
		if((requestType==RequestType.Cache_Read || requestType==RequestType.Cache_Write || requestType==RequestType.EvictCacheLine))
		{
			if(mshr.isAddrInMSHR(addr)) {
				mshr.addToMSHR(event);
				return;
			}
		}
		switch (event.getRequestType()) {
			case Cache_Read:
			case Cache_Write: {
				handleAccess(addr, requestType, event);
				break;
			}
			
			case Mem_Response: {
				handleMemResponse(event);
				break;
			}
	
			case EvictCacheLine: {
				updateStateOfCacheLine(addr, MESI.INVALID);
				break;
			}
			
			case AckEvictCacheLine: {
				processEventsInMSHR(addr);
				break;
			}
			case Migrate_Block: {
				handleMigration(event, requestType);
				break;
			}
			default : 
			{
				misc.Error.showErrorAndExit("Unknown request type " + requestType);
				break;
			}
		}
	}
	public int getSetId() {
		return setId;
	}
	public void setSetId(int setId) {
		this.setId = setId;
	}
	public int getMyId() {
		return myId;
	}
	public void setMyId(int myId) {
		this.myId = myId;
	}
	public long getEvictions() {
		return evictions;
	}

	public void setEvictions(long evictions) {
		this.evictions = evictions;
	}
	public void incrementEvictions(long evictions) {
		this.evictions += evictions;
	}
}