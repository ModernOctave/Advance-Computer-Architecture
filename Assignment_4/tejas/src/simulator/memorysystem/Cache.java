/*****************************************************************************
				Tejas Simulator
------------------------------------------------------------------------------------------------------------

   Copyright 2010 Indian Institute of Technology, Delhi
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

	Contributors:  Moksh Upadhyay
 *****************************************************************************/
package memorysystem;

import generic.Core;
import generic.Event;
import generic.EventQueue;
import generic.GlobalClock;
import generic.RequestType;
import generic.SimulationElement;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeSet;

import main.ArchitecturalComponent;
import memorysystem.coherence.Coherence;
import memorysystem.nuca.NucaCache;
import memorysystem.nuca.NucaCache.NucaType;
import memorysystem.prefetcher.Power4Prefetcher;
import memorysystem.prefetcher.Prefetcher;
import misc.Util;
import net.NocInterface;
import config.CacheConfig;
import config.CacheConfig.PrefetcherType;
import config.CacheConfig.WritePolicy;
import config.CacheDataType;
import config.CacheEnergyConfig;
import config.EnergyConfig;
import config.SystemConfig;

import dram.MainMemoryDRAMController;

public class Cache extends SimulationElement {
	/* cache parameters */
	public CoreMemorySystem containingMemSys;
	protected int blockSize; // in bytes
	public int blockSizeBits; // in bits
	public int assoc;
	protected int assocBits; // in bits
	protected int size; // MegaBytes
	protected int numLines;
	protected int numLinesBits;
	public int numSetsBits;
	protected long timestamp;
	protected int numLinesMask;

	public Coherence mycoherence;
	public Prefetcher myprefetcher;
	public int AMAT;

	// public CacheType levelFromTop;
	public boolean isLastLevel; // Tells whether there are any more levels of
								// cache
	public CacheConfig.WritePolicy writePolicy; // WRITE_BACK or WRITE_THROUGH
	public String nextLevelName; // Name of the next level cache according to
									// the configuration file
	public ArrayList<Cache> prevLevel = new ArrayList<Cache>(); // Points
																// towards the
																// previous
																// level in the
																// cache
																// hierarchy
	public Cache nextLevel; // Points towards the next level in the cache
							// hierarchy
	protected CacheLine lines[];
	
	public static boolean toCountCompulsoryMisses = false;
	public TreeSet<Long> addressesAccessedSoFar;
	public long numberOfCompulsoryMisses;

	public long noOfRequests;
	public long noOfAccesses;
	public long noOfResponsesReceived;
	public long noOfResponsesSent;
	public long noOfWritesForwarded;
	public long noOfWritesReceived;
	public long hits;
	public long misses;
	public long halfmisses;
	public long combinedWrites;
	public long evictions;
	public boolean debug = false;
	public NucaType nucaType;
	
	public long invalidAccesses = 0;

	CacheEnergyConfig energy;

	public String cacheName;

	public void createLinkToNextLevelCache(Cache nextLevelCache) {
		this.nextLevel = nextLevelCache;
		this.nextLevel.prevLevel.add(this);
	}

	public CacheConfig cacheConfig;
	public int id;

	protected MSHR mshr;
	public boolean isBusy(long addr) {
		return mshr.isMSHRFull(addr);
	}
	
	private int maximumNumberOfOutstandingWrites;
	protected int numberOfOutstandingWrites;
	public void incrementNumberOfOutstandingWrites()
	{
		numberOfOutstandingWrites++;
	}
	public boolean canCacheAcceptWriteRequest()
	{
		return (numberOfOutstandingWrites < maximumNumberOfOutstandingWrites);
	}
	
	public Cache(String cacheName, int id, CacheConfig cacheParameters,
			CoreMemorySystem containingMemSys) {
		
		super(cacheParameters.portType,
				cacheParameters.getReadPorts(),
				cacheParameters.getWritePorts(),
				cacheParameters.getReadWritePorts(),
				cacheParameters.getPortReadOccupancy() * cacheParameters.stepSize,
				cacheParameters.getPortWriteOccupancy() * cacheParameters.stepSize,
				cacheParameters.getReadLatency() * cacheParameters.stepSize,
				cacheParameters.getWriteLatency() * cacheParameters.stepSize,
				cacheParameters.operatingFreq);
		// add myself to the global cache list
		if(cacheParameters.isDirectory==true) {
			ArchitecturalComponent.coherences.add((Coherence) this);
		} else {
			MemorySystem.addToCacheList(cacheName, this);
			
			if(containingMemSys==null) {
				ArchitecturalComponent.sharedCaches.add(this);
			}
			
			ArchitecturalComponent.caches.add(this);
		}
		
		if(cacheParameters.prefetcherType == PrefetcherType.Power4)
		{
			this.myprefetcher = new Power4Prefetcher(this);
		}		
		
		this.AMAT = cacheParameters.AMAT;

		if (cacheParameters.collectWorkingSetData == true) {
			workingSet = new TreeSet<Long>();
			workingSetChunkSize = cacheParameters.workingSetChunkSize;
		}

		this.containingMemSys = containingMemSys;

		// set the parameters
		this.blockSize = cacheParameters.getBlockSize();
		this.assoc = cacheParameters.getAssoc();
		this.size = cacheParameters.getSize();
		this.blockSizeBits = Util.logbase2(blockSize);
		this.assocBits = Util.logbase2(assoc);
		this.numLines = getNumLines();
		this.numLinesBits = Util.logbase2(numLines);
		this.numSetsBits = numLinesBits - assocBits;

		this.writePolicy = cacheParameters.getWritePolicy();

		this.cacheConfig = cacheParameters;
		if (this.containingMemSys == null) {
			// Use the core memory system of core 0 for all the shared caches.
			this.isSharedCache = true;
			// this.containingMemSys =
			// ArchitecturalComponent.getCore(0).getExecEngine().getCoreMemorySystem();
		}

		if (cacheParameters.nextLevel == "") {
			this.isLastLevel = true;
		} else {
			this.isLastLevel = false;
		}

		this.cacheName = cacheName;
		this.id = id;

		this.nextLevelName = cacheParameters.getNextLevel();
		// this.enforcesCoherence = cacheParameters.isEnforcesCoherence();

		this.timestamp = 0;
		this.numLinesMask = numLines - 1;
		this.noOfRequests = 0;
		noOfAccesses = 0;
		noOfResponsesReceived = 0;
		noOfResponsesSent = 0;
		noOfWritesForwarded = 0;
		noOfWritesReceived = 0;
		this.hits = 0;
		this.misses = 0;
		this.halfmisses = 0;
		this.combinedWrites = 0;
		this.evictions = 0;
		// make the cache
		makeCache(cacheParameters.isDirectory);

		this.mshr = new MSHR(cacheConfig.mshrSize, blockSizeBits, this);
		maximumNumberOfOutstandingWrites = cacheConfig.mshrSize;

		this.nucaType = cacheParameters.nucaType;

		energy = cacheParameters.power;

		eventsWaitingOnMSHR = new LinkedList<AddressCarryingEvent>();
		
		if(toCountCompulsoryMisses)
		{
			addressesAccessedSoFar = new TreeSet<Long>();
			numberOfCompulsoryMisses = 0;
		}
		
		halfmissList = new ArrayList<Event>();
	}

	
	public void setCoherence(Coherence c) {
		this.mycoherence = c;
	}

	private boolean printCacheDebugMessages = false;

	//request (read or write or line-forward from another cache)
	//	- read (*reads++)
	//		- line found in cache (hits++)
	//		- line not in cache
	//			- entry exists in MSHR for same addr (halfmisses++)
	//			- no MSHR entry (misses++)
	//	- write (*writes++)
	//		- line found in cache (hits++)
	//		- line not in cache
	//			- entry exists in MSHR for same addr (halfmisses++)
	//				- latest MSHR entry for that addr is a write (combinedWrites++)
	//			- no MSHR entry (misses++)
	ArrayList<Event> halfmissList;
	public void handleEvent(EventQueue eventQ, Event e) {
		

		AddressCarryingEvent event = (AddressCarryingEvent) e;
		printCacheDebugMessage(event);

		long addr = ((AddressCarryingEvent) event).getAddress();
		RequestType requestType = event.getRequestType();
		
		noOfAccesses++;
		
		if (requestType == RequestType.Cache_Write) {
			numberOfOutstandingWrites--;
		}
		
		if(mshr.isAddrInMSHR(addr) && 
			(requestType==RequestType.Cache_Read || requestType==RequestType.Cache_Write || requestType==RequestType.EvictCacheLine)) {
			if(requestType==RequestType.Cache_Read || requestType==RequestType.Cache_Write)
			{
				halfmisses++;
				noOfRequests++;
//				if(this.cacheName.contains("L1"))
//				{
//					System.out.println("half miss event added to mshr : " + e.serializationID);
//					halfmissList.add(event);
//					if(event.serializationID == 4385)
//					{
//						System.out.println(event.serializationID);
//					}
//				}
			}
			mshr.addToMSHR(event);
			return;
		}

		switch (event.getRequestType()) {
			case Cache_Read:
			case Cache_Write: {
				noOfRequests++;
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
	
			case DirectoryCachelineForwardRequest: {
				noOfRequests++;
				handleDirectoryCachelineForwardRequest(addr, (Cache) (((AddressCarryingEvent) event).getPayloadElement()));
				break;
			}
	
			case DirectorySharedToExclusive: {
				handleDirectorySharedToExclusive(addr);
				break;
			}
			
			case AckDirectoryWriteHit: {
				handleAckDirectoryWriteHit(event);
				break;
			}
		}
	}

	protected void handleAckDirectoryWriteHit(AddressCarryingEvent event) {
		//This function just ensures that the writeHit event gets a line
		long addr = event.getAddress();
		CacheLine cl = accessValid(addr);
		
		if(cl==null) {
			misc.Error.showErrorAndExit("Ack write hit expects cache line");
			// writehit expects a line to be present
			if(isThereAnUnlockedOrInvalidEntryInCacheSet(addr)) {
				fillAndSatisfyRequests(addr);
				return;
			} else {
				event.setEventTime(event.getEventTime() + 1);
				return;
			}
		} else {
			processEventsInMSHR(addr);
		}
	}

	protected void handleDirectorySharedToExclusive(long addr) {
		if(accessValid(addr)==null) {
			// c1 and c2 both have address x
			// both decide to evict at the same time
			// c2's evict reaches directory first. directory asks c1 to change state from shared to exclusive
			// c1 however does not have the line
			noteInvalidState("shared to exclusive for a line that does not exist. addr : " + addr + ". cache : " + this);
		}
		updateStateOfCacheLine(addr, MESI.EXCLUSIVE);
	}

	protected void handleDirectoryCachelineForwardRequest(long addr, Cache cache) {
		AddressCarryingEvent event = new AddressCarryingEvent(
				cache.getEventQueue(), 0, this, cache,
				RequestType.Mem_Response, addr);
		
		updateStateOfCacheLine(addr, MESI.SHARED);

		this.sendEvent(event);
	}

	protected void printCacheDebugMessage(Event event) {
		if (printCacheDebugMessages == true) {
			if (event.getClass() == AddressCarryingEvent.class) {
				System.out.println("CACHE : globalTime = "
						+ GlobalClock.getCurrentTime() + "\teventTime = "
						+ event.getEventTime() + "\t" + event.getRequestType()
						+ "\trequestingElelement = "
						+ event.getRequestingElement() + "\taddress = "
						+ ((AddressCarryingEvent) event).getAddress() + "\t"
						+ this + "\t" + ((AddressCarryingEvent) event).dn_status);
			}
		}
	}

	public void handleAccess(long addr, RequestType requestType,
			AddressCarryingEvent event) {

		if (requestType == RequestType.Cache_Write) {
			noOfWritesReceived++;
		}
		
		if(toCountCompulsoryMisses
				&& addressesAccessedSoFar.contains(getLineAddress(addr)) == false)
		{
			numberOfCompulsoryMisses++;
			addressesAccessedSoFar.add(getLineAddress(addr));
		}

		CacheLine cl = this.accessAndMark(addr);

		// IF HIT
		if (cl != null) {
			hits++;
			cacheHit(addr, requestType, cl, event);
		} else {
			misses++;
			if (this.mycoherence != null) {
				if (requestType == RequestType.Cache_Write) {
					mycoherence.writeMiss(addr, this);
				} else if (requestType == RequestType.Cache_Read) {
					mycoherence.readMiss(addr, this);
				}
			} else {
				sendRequestToNextLevel(addr, RequestType.Cache_Read);
			}
			
			mshr.addToMSHR(event);
			
			if(this.myprefetcher != null)
			{
				this.myprefetcher.handleMissAtAddress(addr);
			}
		}
	}

	protected void cacheHit(long addr, RequestType requestType, CacheLine cl,
			AddressCarryingEvent event) {
		
		if (requestType == RequestType.Cache_Read) {
			sendAcknowledgement(event);
		} else if (requestType == RequestType.Cache_Write) {
			if(event.getRequestingElement() != this
					&& getPort().getWriteLatencyOfConnectedElement() - getPort().getReadLatencyOfConnectedElement() > 0)
			{
					//this case refers to when the line to be written to is found in the cache
					//time for line searching has already been spent
					//now spend time for writing
					event.update(0);
					event.update(this, this);
					this.getPort().put(event);
					//to correct the double counting of stats
					noOfAccesses--;
					noOfRequests--;
					noOfWritesReceived--;
					hits--;
			}
			else
			{
				if(this.writePolicy == WritePolicy.WRITE_THROUGH) {
					sendRequestToNextLevel(addr, RequestType.Cache_Write);
				}
				
				if( (cl.getState() == MESI.SHARED || cl.getState() == MESI.EXCLUSIVE)  && 
						(mycoherence!=null)) {
					handleCleanToModified(addr, event);
				}
			}
		} else {
			misc.Error.showErrorAndExit("cache hit unknown event type\n" + event + "\ncache : " + this);
		}
	}

	protected void handleMemResponse(AddressCarryingEvent memResponseEvent) {
		long addr = memResponseEvent.getAddress();
		
		if(isThereAnUnlockedOrInvalidEntryInCacheSet(addr)) {
			noOfResponsesReceived++;
			this.fillAndSatisfyRequests(addr);
		} else {
			memResponseEvent.setEventTime(GlobalClock.getCurrentTime()+1);
			this.getEventQueue().addEvent(memResponseEvent);
		}
	}

	public void sendRequestToNextLevel(long addr, RequestType requestType) {
		Cache c = this.nextLevel;
		AddressCarryingEvent event = null;
		if (c != null) {
			if(c.AMAT != -1 && requestType == RequestType.Cache_Read)
			{
				event = new AddressCarryingEvent(c.getEventQueue(), c.AMAT*c.stepSize, c, this,
						RequestType.Mem_Response, addr);
				this.getPort().put(event);
			}
			else
			{
				if(c.nucaType != NucaType.NONE)
				{
	//				 String nextLevelName = null;
	//                 if(nextLevel.cacheName.contains("["))
	//                 {
	//                         nextLevelName = nextLevel.cacheName.substring(0, nextLevel.cacheName.indexOf('['));
	//                 }
	//                 else
	//                 {
	//                         nextLevelName = nextLevel.cacheName;
	//                 }
	//
	//				NucaCache nuca = ArchitecturalComponent.nucaList.get(nextLevelName);
					c = ((NucaCache)c).getBank(((NocInterface)this.getComInterface()).getId(),addr);
				}
				event = new AddressCarryingEvent(c.getEventQueue(), 0, this, c,
						requestType, addr);
				addEventAtLowerCache(event, c);
				if(requestType == RequestType.Cache_Write)
				{
					c.incrementNumberOfOutstandingWrites();
				}
			}
		} else {
			Core core0 = ArchitecturalComponent.getCores()[0];
			//added later by kush
			MainMemoryDRAMController memController;
			if(SystemConfig.memControllerToUse==true){
				int chan=findChannelNumber(addr);
				memController = getComInterface()
						.getNearestMemoryController(chan);
			}
			else{
				memController = getComInterface()
						.getNearestMemoryController(0);	
			}

			event = new AddressCarryingEvent(core0.getEventQueue(), 0, this,
					memController, requestType, addr);
			sendEvent(event);
		}		
	}

	//added by kush
	public static int findChannelNumber(long physicalAddress){
		long tempA,tempB;
		int channelBits = log2(SystemConfig.mainMemoryConfig.numChans);
		
		int DataBusBytesOffest = log2(SystemConfig.mainMemoryConfig.DATA_BUS_BYTES);		//for 64 bit bus -> 8 bytes -> lower 3 bits of address irrelevant
		
		int ColBytesOffset = log2(SystemConfig.mainMemoryConfig.BL);		
		//these are the bits we need to throw away because of "bursts". The column address is incremented internally on bursts
		//So for a burst length of 4, 8 bytes of data are transferred on each burst
		//Each consecutive 8 byte chunk comes for the "next" column
		//So we traverse 4 columns in 1 request. Thus the lower log2(4) bits become irrelevant for us. Throw them away
		//Finally we get 8 bytes * 4 = 32 bytes of data for a 64 bit data bus and BL = 4.
		//This is equal to a cache line
		
		//For clarity
		//Throw away bits to account for data bus size in bytes
		//and for burst length
		physicalAddress >>>= (DataBusBytesOffest + ColBytesOffset); 		//using >>> for unsigned right shift
		//System.out.println("Shifted address by " + (DataBusBytesOffest + ColBytesOffset) + " bits");
				
				
		//By the same logic, need to remove the burst-related column bits from the column bit width to be decoded
		//colEffectiveBits = colBits - ColBytesOffset;
	
		//row:rank:bank:col:chan
		
		tempA = physicalAddress;
		physicalAddress = physicalAddress >>> channelBits;			//always unsigned shifting
		tempB = physicalAddress << channelBits;
		//System.out.println("Shifted address by " + rankBits + " bits");
		int decodedChan = (int) (tempA ^ tempB);
		return decodedChan;
	}
	
	public static int log2(int a)
	{
		return (int) (Math.log(a)/Math.log(2));
	}

	
	private boolean isTopLevelCache() {
		return this.cacheConfig.firstLevel;
	}

	public boolean isL2cache() {
		// I am not a first level cache.
		// But a cache connected on top of me is a first level cache
		return (this.cacheConfig.firstLevel == false && this.prevLevel.get(0).cacheConfig.firstLevel == true);
	}

	public boolean isIcache() {
		return (this.cacheConfig.firstLevel == true && (this.cacheConfig.cacheDataType == CacheDataType.Instruction || this.cacheConfig.cacheDataType == CacheDataType.Unified));
	}

	public boolean isL1cache() {
		return (this.cacheConfig.firstLevel == true && (this.cacheConfig.cacheDataType == CacheDataType.Data || this.cacheConfig.cacheDataType == CacheDataType.Unified));
	}

	private boolean isSharedCache = false;

	public boolean isSharedCache() {
		return isSharedCache;
	}

	public boolean isPrivateCache() {
		return !isSharedCache();
	}

	public boolean addEventAtLowerCache(AddressCarryingEvent event, Cache c) {
		if (c.isBusy(event.getAddress()) == false) {
			sendEvent(event);
			c.workingSetUpdate();
			return true;
		} else {
			// Slight approximation used. MSHR full is a rare event.
			// On occurrence of such events, we just add this event to the pending events list of the lower level cache.
			// The network congestion and the port occupancy of the next level is not modelled in such cases.
			// It must be noted that the MSHR full event of the first level caches is being modelled correctly.
			// This approximation applies only to non-firstlevel caches.
			c.eventsWaitingOnMSHR.add(event);
//			System.out.println();
			return false;
		}
	}

	public void fillAndSatisfyRequests(long addr) {
		CacheLine evictedLine = this.fill(addr, MESI.SHARED);
		handleEvictedLine(evictedLine);
		processEventsInMSHR(addr);		
	}

	protected void processEventsInMSHR(long addr) {
		LinkedList<AddressCarryingEvent> missList = mshr.removeEventsFromMSHR(addr);
		AddressCarryingEvent writeEvent = null;
				
		for(AddressCarryingEvent event : missList) {
			switch(event.getRequestType()) {
				case Cache_Read: {
					sendAcknowledgement(event);
//					if(this.cacheName.contains("L1"))
//					{
//						System.out.println("process read miss : " + event.serializationID);
//						halfmissList.remove(event);
//					}
					break;
				}
				
				case Cache_Write: {
//					if(this.cacheName.contains("L1"))
//					{
//						System.out.println("process write miss : " + event.serializationID);
//						halfmissList.remove(event);
//					}
					CacheLine cl = accessValid(addr);
					
					if(cl!=null) {
						updateStateOfCacheLine(addr, MESI.MODIFIED);
						writeEvent = event;
					} else {
						misc.Error.showErrorAndExit("Cache write expects a line here : " + event);
					}
					
					break;
				}
				
				case DirectoryEvictedFromCoherentCache:
				case EvictCacheLine: {
					updateStateOfCacheLine(addr, MESI.INVALID);
					addUnprocessedEventsToEventQueue(missList);
					
					processEventsInPendingList();
					return;
				}
			}
		}
		
		if(writeEvent!=null && writePolicy==WritePolicy.WRITE_THROUGH) {
			sendRequestToNextLevel(addr, RequestType.Cache_Write);
		}
		
		processEventsInPendingList();
	}

	private void processEventsInPendingList() {
		int size = eventsWaitingOnMSHR.size();
		for(int i = 0; i < size; i++)
		{
			if(eventsWaitingOnMSHR.isEmpty())
			{
				break;
			}
			
			AddressCarryingEvent event = eventsWaitingOnMSHR.get(i);
			if(mshr.isMSHRFull(event.getAddress()))
			{
				continue;
			}

			event.getProcessingElement().handleEvent(event.getEventQ(), event);
			
			eventsWaitingOnMSHR.remove(event);
			i--;
			size--;
		}
	}

	protected void handleEvictedLine(CacheLine evictedLine) {
		if (evictedLine != null && evictedLine.getState() != MESI.INVALID) {
			if(mshr.isAddrInMSHR(evictedLine.getAddress())) {
				misc.Error.showErrorAndExit("evicting locked line : " + evictedLine + ". cache : " + this);
			}
			if (mycoherence != null) {
				 AddressCarryingEvent evictEvent = mycoherence.evictedFromCoherentCache(evictedLine.getAddress(), this);
				 mshr.addToMSHR(evictEvent);
			} else if (evictedLine.isModified() && writePolicy == WritePolicy.WRITE_BACK) {
				sendRequestToNextLevel(evictedLine.getAddress(), RequestType.Cache_Write);
			}
		}
	}

	private void handleCleanToModified(long addr, AddressCarryingEvent event) {
		if(mycoherence!=null) {
			mycoherence.writeHit(addr, this);
			mshr.addToMSHR(event);
		} else {
			sendRequestToNextLevel(addr, RequestType.Cache_Write);
		}		
	}

	private void addUnprocessedEventsToEventQueue(LinkedList<AddressCarryingEvent> missList) {
		int timeToSet = missList.size() * -1;
		boolean startAddition = false;
		for(AddressCarryingEvent event : missList) {
			if(startAddition == true) {
				event.setEventTime(timeToSet);
				getEventQueue().addEvent(event);
				timeToSet++;
			}
			if(event.getRequestType()==RequestType.EvictCacheLine
					|| event.getRequestType()==RequestType.DirectoryEvictedFromCoherentCache) {
				startAddition = true;
			}
		}		
	}

	public void sendAcknowledgement(AddressCarryingEvent event) {
		RequestType returnType = null;
		if(event.getRequestType()==RequestType.Cache_Read) {
			returnType = RequestType.Mem_Response;
		} else {
			misc.Error.showErrorAndExit("sendAcknowledgement is meant for cache read operation only : " + event);
		}
		
		AddressCarryingEvent memResponseEvent = new AddressCarryingEvent(
				event.getEventQ(), 0, event.getProcessingElement(),
				event.getRequestingElement(), returnType,
				event.getAddress());
		
		sendEvent(memResponseEvent);
		noOfResponsesSent++;

		// if(ArchitecturalComponent.getCores()[1].getNoOfInstructionsExecuted()
		// > 6000000l)
		// System.out.println("sending mem response from " +
		// event.getProcessingElement() + " to " + event.getRequestingElement()
		// + " for addr : " + event.getAddress());
	}

	public long computeTag(long addr) {
		long tag = addr >>> (numSetsBits + blockSizeBits);
		return tag;
	}

	public int getSetIdx(long addr) {
		int startIdx = getStartIdx(addr);
		return startIdx / assoc;
	}

	public int getStartIdx(long addr) {
		long SetMask = (1 << (numSetsBits)) - 1;
		int startIdx = (int) ((addr >>> blockSizeBits) & (SetMask));
		return startIdx;
	}

	public int getNextIdx(int startIdx, int idx) {
		int index = startIdx + (idx << numSetsBits);
		return index;
	}

	public CacheLine accessValid(long addr) {
		CacheLine cl = access(addr);
		if (cl != null && cl.getState() != MESI.INVALID) {
			return cl;
		} else {
			return null;
		}
	}

	public CacheLine access(long addr) {
		/* compute startIdx and the tag */
		int startIdx = getStartIdx(addr);
		long tag = computeTag(addr);

		/* search in a set */
		for (int idx = 0; idx < assoc; idx++) {
			// calculate the index
			int index = getNextIdx(startIdx, idx);
			// fetch the cache line
			CacheLine ll = this.lines[index];
			// If the tag is matching, we have a hit
			if (ll.hasTagMatch(tag)) {
				return ll;
			}
		}
		return null;
	}

	protected void mark(CacheLine ll, long tag) {
		ll.setTag(tag);
		mark(ll);
	}

	private void mark(CacheLine ll) {
		ll.setTimestamp(timestamp);
		timestamp += 1.0;
	}

	private void makeCache(boolean isDirectory) {
		lines = new CacheLine[numLines];
		for (int i = 0; i < numLines; i++) {
			lines[i] = new CacheLine(isDirectory);
		}
	}

	private int getNumLines() {
		long totSize = size;
		return (int) (totSize / (long) (blockSize));
	}

	protected CacheLine accessAndMark(long addr) {
		CacheLine cl = accessValid(addr);
		if (cl != null) {
			mark(cl);
		}
		return cl;
	}

	public CacheLine fill(long addr, MESI stateToSet) // Returns a copy of the
														// evicted line
	{
		CacheLine evictedLine = null;
		/* compute startIdx and the tag */
		int startIdx = getStartIdx(addr);
		long tag = computeTag(addr);
		boolean addressAlreadyPresent = false;
		/* find any invalid lines -- no eviction */
		CacheLine fillLine = null;
		boolean evicted = false;

		// ------- Check if address is in cache ---------
		for (int idx = 0; idx < assoc; idx++) {
			int nextIdx = getNextIdx(startIdx, idx);
			CacheLine ll = this.lines[nextIdx];
			if (ll.getTag() == tag) {
				addressAlreadyPresent = true;
				fillLine = ll;
				break;
			}
		}

		// ------- Check if there's an invalid line ---------
		for (int idx = 0; !addressAlreadyPresent && idx < assoc; idx++) {
			int nextIdx = getNextIdx(startIdx, idx);
			CacheLine ll = this.lines[nextIdx];
			if (ll.isValid() == false && mshr.isAddrInMSHR(ll.getAddress())==false 
					|| (this.nucaType!= NucaType.NONE && ll.isValid() == false)) {
				fillLine = ll;
				break;
			}
		}

		// ------- Check if there's an unlocked valid line ---------
		if (fillLine == null) {
			evicted = true; // We need eviction in this case
			double minTimeStamp = Double.MAX_VALUE;
			for (int idx = 0; idx < assoc; idx++) {
				int index = getNextIdx(startIdx, idx);
				CacheLine ll = this.lines[index];
				
				if(mshr.isAddrInMSHR(ll.getAddress())==true) {
					continue;
				}

				if (minTimeStamp > ll.getTimestamp()) {
					minTimeStamp = ll.getTimestamp();
					fillLine = ll;
				}
			}
		}

		if (fillLine == null) {
			misc.Error.showErrorAndExit("Unholy mess !!");
		}

		/* if there has been an eviction */
		if (evicted) {
			evictedLine = (CacheLine) fillLine.clone();
			long evictedLinetag = evictedLine.getTag();
			evictedLinetag = (evictedLinetag << numSetsBits)
					+ (startIdx / assoc);
			evictedLine.setTag(evictedLinetag);
			this.evictions++;
		}

		/* This is the new fill line */
		fillLine.setState(stateToSet);
		fillLine.setAddress(addr);
		mark(fillLine, tag);
		return evictedLine;
	}

	public LinkedList<AddressCarryingEvent> eventsWaitingOnMSHR = new LinkedList<AddressCarryingEvent>(); 

	public String toString() {
		return cacheName;
	}

	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter,
			String componentName) throws IOException {
		EnergyConfig newPower = new EnergyConfig(energy.leakageEnergy,
				energy.readDynamicEnergy);
		EnergyConfig cachePower = new EnergyConfig(newPower, noOfAccesses);
		cachePower.printEnergyStats(outputFileWriter, componentName);
		return cachePower;
	}

	public void updateStateOfCacheLine(long addr, MESI newState) {
		CacheLine cl = this.access(addr);
		
		if (cl != null) {

			cl.setState(newState);
			
			if(newState==MESI.INVALID && mshr.isAddrInMSHR(addr)) {
				misc.Error.showErrorAndExit("Cannot invalidate a locked line. Addr : " + addr + ". Cache : " + this);
			}

			
			if (newState == MESI.INVALID) {
				if (isBelowCoherenceLevel()) {
					getPrevLevelCoherence().evictedFromSharedCache(addr, this);
				} else {
					for (Cache c : prevLevel) {
						sendAnEventFromMeToCache(addr, c, RequestType.EvictCacheLine);
					}
				}
			} else {
				// If you are not below coherence, then keep the same state in the previous level caches
				// This ensures that the caches in the same core have the same MESI state
				if(isBelowCoherenceLevel()==false && this.isTopLevelCache()==false) {
					for(Cache c : prevLevel) {
						c.updateStateOfCacheLine(addr, newState);
					}
				}
			}			
		}
	}

	public EventQueue getEventQueue() {
		if (containingMemSys != null) {
			return containingMemSys.getCore().eventQueue;
		} else {
			return (ArchitecturalComponent.getCores()[0]).eventQueue;
		}
	}

	public Cache getNextLevelCache(long addr) {
		return nextLevel;
	}

	public void workingSetUpdate() {
		// Clear the working set data after every x instructions
		if (this.containingMemSys != null && this.workingSet != null) {

			if (isIcache()) {
				long numInsn = containingMemSys.getiCache().hits
						+ containingMemSys.getiCache().misses;
				long numWorkingSets = numInsn / workingSetChunkSize;
				if (numWorkingSets > containingMemSys.numInstructionSetChunksNoted) {
					this.clearWorkingSet();
					containingMemSys.numInstructionSetChunksNoted++;
				}
			} else if (isL1cache()) {
				long numInsn = containingMemSys.getiCache().hits
						+ containingMemSys.getiCache().misses;
				long numWorkingSets = numInsn / workingSetChunkSize;
				if (numWorkingSets > containingMemSys.numDataSetChunksNoted) {
					this.clearWorkingSet();
					containingMemSys.numDataSetChunksNoted++;
				}
			}
		}
	}

	TreeSet<Long> workingSet = null;
	long workingSetChunkSize = 0;
	public long numWorkingSetHits = 0;
	public long numWorkingSetMisses = 0;
	public long numFlushesInWorkingSet = 0;
	public long totalWorkingSetSize = 0;
	public long maxWorkingSetSize = Long.MIN_VALUE;
	public long minWorkingSetSize = Long.MAX_VALUE;

	void addToWorkingSet(long addr) {
		long lineAddr = addr >>> blockSizeBits;
		if (workingSet != null) {
			if (workingSet.contains(lineAddr) == true) {
				numWorkingSetHits++;
				return;
			} else {
				numWorkingSetMisses++;
				workingSet.add(lineAddr);
			}
		}
	}

	float getWorkingSetHitrate() {
		if (numWorkingSetHits == 0 && numWorkingSetMisses == 0) {
			return 0.0f;
		} else {
			return (float) numWorkingSetHits
					/ (float) (numWorkingSetHits + numWorkingSetMisses);
		}
	}

	void clearWorkingSet() {
		numFlushesInWorkingSet++;
		totalWorkingSetSize += workingSet.size();
		if (workingSet.size() > maxWorkingSetSize) {
			maxWorkingSetSize = workingSet.size();
		}

		if (workingSet.size() < minWorkingSetSize) {
			minWorkingSetSize = workingSet.size();
		}

		// System.out.println(this + " : For chunk " +
		// (numFlushesInWorkingSet-1) +
		// "\tworkSet = " + workingSet.size() +
		// "\tminSet = " + minWorkingSetSize +
		// "\tavgSet = " +
		// (float)totalWorkingSetSize/(float)numFlushesInWorkingSet +
		// "\tmaxSet = " + maxWorkingSetSize +
		// "\tworkSetHitrate = " + getWorkingSetHitrate());

		if (workingSet != null) {
			workingSet.clear();
		}
	}

	public long getLineAddress(long addr) {
		return addr >>> blockSizeBits;
	}

	protected AddressCarryingEvent sendAnEventFromMeToCache(long addr, Cache c,
			RequestType request) {
		// Create an event

		AddressCarryingEvent event = new AddressCarryingEvent(
				c.getEventQueue(), 0, this, c, request, addr);

		// 2. Send event to cache
		this.sendEvent(event);

		return event;
	}

	private Coherence getPrevLevelCoherence() {
		return prevLevel.get(0).mycoherence;
	}

	private boolean isBelowCoherenceLevel() {
		if (prevLevel != null && prevLevel.size() > 0
				&& prevLevel.get(0).mycoherence != null) {
			return true;
		} else {
			return false;
		}
	}

	protected boolean isThereAnUnlockedOrInvalidEntryInCacheSet(long addr) {
		/* compute startIdx and the tag */
		int startIdx = getStartIdx(addr);

		/* search in a set */
		for (int idx = 0; idx < assoc; idx++) {
			// calculate the index
			int index = getNextIdx(startIdx, idx);
			// fetch the cache line
			CacheLine ll = lines[index];
			// If the tag is matching, we have a hit
			
			if(ll.getAddress()==addr) {
				return true;
			}
			
			if ((ll.isValid() == false && mshr.isAddrInMSHR(ll.getAddress())==false 
					|| (this.nucaType!= NucaType.NONE && ll.isValid() == false))) {
				return true;
			}
			
			if(mshr.isAddrInMSHR(ll.getAddress())==false) {
				return true;
			}
//			else if (ll.getState() == MESI.INVALID) {
//				return true;
//			} 
		}
		
		return false;
	}
	
	public int numberOfLinesOfSetInMSHR(long addr)
	{
		int count = 0;
		int startIdx = getStartIdx(addr);

		for (int idx = 0; idx < assoc; idx++)
		{
			int index = getNextIdx(startIdx, idx);
			CacheLine ll = lines[index];
			if(mshr.isAddrInMSHR(ll.getAddress()))
			{
				count++;
			} 
		}
		return count;
	}

	public void printMSHR() {
		mshr.printMSHR();
	}
	
	protected void noteInvalidState(String msg) {
		invalidAccesses++;
		
		//System.err.println(msg);
	}

	public void noteMSHRStats() {
		mshr.noteMSHRStats();
	}
	
	public double getAvgNumEventsPendingInMSHR() {
		return mshr.getAvgNumEventsPendingInMSHR();
	}
	
	public double getAvgNumEventsPendingInMSHREntry() {
		return mshr.getAvgNumEventsPendingInMSHREntry();
	}

	public Prefetcher getMyprefetcher() {
		return myprefetcher;
	}

	public void setMyprefetcher(Prefetcher myprefetcher) {
		this.myprefetcher = myprefetcher;
	}
}
