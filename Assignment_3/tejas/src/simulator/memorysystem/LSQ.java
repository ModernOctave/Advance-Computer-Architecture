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

	Contributors:  Moksh Upadhyay, Smruti R. Sarangi 
 *****************************************************************************/
package memorysystem;

import java.io.FileWriter;
import java.io.IOException;

import config.EnergyConfig;

import memorysystem.LSQEntry.LSQEntryType;
import pipeline.outoforder.OutOrderCoreMemorySystem;
import pipeline.outoforder.ReorderBufferEntry;

import generic.*;

public class LSQ extends SimulationElement
{
	CoreMemorySystem containingMemSys;
	protected LSQEntry[] lsqueue;
	protected int tail; // points to last valid entry
	protected int head; // points to first valid entry
	public int lsqNumLoadEntries;
	public int lsqNumStoreEntries;
	protected int curNumLoadsInQ;
	protected int curNumStoresInQ;
	protected int lsqSize;

	public long noOfMemRequests = 0;
	public long NoOfLd = 0;
	public long NoOfSt = 0;
	public long NoOfSoftwareReadPrefetch = 0;
	public long NoOfSoftwareWritePrefetch = 0;
	public long NoOfForwards = 0; // Total number of forwards made by the LSQ

	long numAccesses;

	public LSQ(PortType portType, int noOfPorts, long occupancy, long latency, CoreMemorySystem containingMemSys, int lsqNumLoadEntries, int lsqNumStoreEntries) 
	{
		super(portType, noOfPorts, occupancy, latency, containingMemSys.getCore().getFrequency());
		this.containingMemSys = containingMemSys;
		this.lsqNumLoadEntries = lsqNumLoadEntries;
		this.lsqNumStoreEntries = lsqNumStoreEntries;
		this.lsqSize = lsqNumLoadEntries + lsqNumStoreEntries;
		head = -1;
		tail = -1;
		curNumLoadsInQ = 0;
		curNumStoresInQ = 0;
		lsqueue = new LSQEntry[lsqSize];
		for(int i = 0; i < lsqSize; i++)
		{
			LSQEntry entry = new LSQEntry(LSQEntryType.LOAD, null);
			entry.setAddr(-1);
			entry.setIndexInQ(i);
			lsqueue[i] = entry;
		}
	}

	public LSQEntry addEntry(boolean isLoad, long address, ReorderBufferEntry robEntry) //To be accessed at the time of allocating the entry
	{
		noOfMemRequests++;
		LSQEntry.LSQEntryType type = (isLoad) ? LSQEntry.LSQEntryType.LOAD 
				: LSQEntry.LSQEntryType.STORE;

		if(head == -1)
		{
			head = tail = 0;
		}
		else
		{
			tail = (tail + 1)%lsqSize;
		}

		LSQEntry entry = lsqueue[tail];
		if(!entry.isRemoved())
			misc.Error.showErrorAndExit("entry currently in use being re-allocated");
		entry.recycle();
		entry.setType(type);
		entry.setRobEntry(robEntry);
		entry.setAddr(address);

		if (isLoad)
		{
			NoOfLd++;
			curNumLoadsInQ++;
		}
		else
		{
			NoOfSt++;
			curNumStoresInQ++;
		}

		incrementNumAccesses(1);

		return entry;
	}

	/*
	 * the load-store predictor is a perfect one
	 * once a load address is known,
	 * * * if there are earlier resolved stores to the same address, the load gets the value through LSQ forwarding
	 * * * if there are earlier unresolved stores to the same address, the load waits
	 * * * if there are no stores, resolved or unresolved, to the same address, the load goes to L1
	 */
	private enum LSQSearchStatus {Forwarded, CanBeForwarded, ShouldGoToL1};

	public boolean loadValidate(LSQEntry entry)
	{
		//Test check
		if (lsqueue[entry.getIndexInQ()] != entry)
			misc.Error.showErrorAndExit(" Entry index and actual entry dont match : LOAD" + entry.getIndexInQ());

		entry.setValid(true);
		LSQSearchStatus couldForward = loadResolve(entry.getIndexInQ(), entry);
		if(couldForward == LSQSearchStatus.Forwarded) 
		{
			return true;
		}
		else if(couldForward == LSQSearchStatus.CanBeForwarded)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	protected LSQSearchStatus loadResolve(int index, LSQEntry entry)
	{
		if (!entry.isValid())
			misc.Error.showErrorAndExit(" 01 Invalid entry forwarded");

		int tmpIndex;

		if (entry.getIndexInQ() == head)
			return LSQSearchStatus.ShouldGoToL1;
		else
			tmpIndex = decrementQ(index);

		while(true)
		{
			LSQEntry tmpEntry = lsqueue[tmpIndex];
			if (tmpEntry.getType() == LSQEntry.LSQEntryType.STORE)
			{
				if (tmpEntry.getAddr() == entry.getAddr())
				{
					if (tmpEntry.isValid())
					{
						// Successfully forwarded the value
						entry.setForwarded(true);
						NoOfForwards++;
						if (entry.getRobEntry() != null && !entry.getRobEntry().getExecuted())
							((OutOrderCoreMemorySystem)containingMemSys).sendExecComplete(entry.getRobEntry());
						return LSQSearchStatus.Forwarded;
					}
					else
					{
						//found a store to the same address, but it is not valid yet
						return LSQSearchStatus.CanBeForwarded;
					}
				}
			}
			if(tmpIndex == head)
				break;
			tmpIndex = decrementQ(tmpIndex);
		}
		return LSQSearchStatus.ShouldGoToL1;
	}

	public void storeValidate(LSQEntry entry)
	{
		//Test check
		if (lsqueue[entry.getIndexInQ()] != entry)
			misc.Error.showErrorAndExit(" Entry index and actual entry dont match : STORE" + entry.getIndexInQ());

		entry.setValid(true);
		storeResolve(entry.getIndexInQ(), entry);
	}

	protected void storeResolve(int index, LSQEntry entry)
	{
		if (!entry.isValid())
			misc.Error.showErrorAndExit(" 02 Invalid entry forwarded");

		int sindex = incrementQ(index);

		while (true)
		{
			LSQEntry tmpEntry = lsqueue[sindex];

			if (tmpEntry.getType() == LSQEntry.LSQEntryType.LOAD)
			{
				if(tmpEntry.getAddr() == entry.getAddr()) 
				{
					if (tmpEntry.isValid() && !tmpEntry.isForwarded())
					{
						tmpEntry.setForwarded(true);
						if (tmpEntry.getRobEntry() != null && !tmpEntry.getRobEntry().getExecuted())
							((OutOrderCoreMemorySystem)containingMemSys).sendExecComplete(tmpEntry.getRobEntry());

						NoOfForwards++;
					}
				}
			}
			else
			{
				if(tmpEntry.getAddr() == entry.getAddr())
				{
					//found a store to the same address
					break;
				}
			}
			if(sindex == tail)
				break;
			// increment
			sindex = incrementQ(sindex);
		}
	}


	//Only used by the statistical pipeline
	public void processROBCommitForStatisticalPipeline(EventQueue eventQueue)
	{
		while (curNumLoadsInQ + curNumStoresInQ > 0 && ((lsqueue[head].getType() == LSQEntryType.STORE && lsqueue[head].isValid())||
				(lsqueue[head].getType() == LSQEntryType.LOAD && lsqueue[head].isForwarded() == true)))
		{
			LSQEntry entry = lsqueue[head];

			// if it is a store, send the request to the cache
			if(entry.getType() == LSQEntry.LSQEntryType.STORE) 
			{
				this.containingMemSys.l1Cache.getPort().put(
						new LSQEntryContainingEvent(
								eventQueue,
								this.containingMemSys.l1Cache.getLatencyDelay(),
								this,
								this.containingMemSys.l1Cache,
								RequestType.Cache_Write,
								entry,
								this.containingMemSys.coreID));
			}

			if(head == tail)
			{
				head = tail = -1;
			}
			else
			{
				this.head = this.incrementQ(this.head);
			}
			if(entry.getType() == LSQEntry.LSQEntryType.LOAD)
			{
				this.curNumLoadsInQ--;
			}
			else
			{
				this.curNumStoresInQ--;
			}
		}
	}


	protected int incrementQ(int value)
	{
		value = (value+1)%lsqSize;
		return value;
	}
	protected int decrementQ(int value)
	{
		if (value > 0)
			value--;
		else if (value == 0)
			value = lsqSize - 1;
		return value;
	}

	public boolean isEmpty()
	{
		if (curNumLoadsInQ + curNumStoresInQ == 0)
			return true;
		else 
			return false;
	}

	public boolean isFull(boolean isLoad)
	{
		if(privIsFull(isLoad))
			freeOneEntry(isLoad);
		return privIsFull(isLoad);
	}
	
	private boolean privIsFull(boolean isLoad)
	{
		if(isLoad)
		{
			if(curNumLoadsInQ >= lsqNumLoadEntries)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			if(curNumStoresInQ >= lsqNumStoreEntries)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}

	public int getLsqSize() {
		return lsqSize;
	}

	public void setRemoved(int index)
	{
		lsqueue[index].setRemoved(true);
	}

	public void handleEvent(EventQueue eventQ, Event event)
	{
		if (event.getRequestType() == RequestType.Tell_LSQ_Addr_Ready)
		{
			handleAddressReady(eventQ, event);
		}
		else if (event.getRequestType() == RequestType.Validate_LSQ_Addr)
		{
			handleAddrValidate(eventQ, event);
		}
		else if (event.getRequestType() == RequestType.LSQ_Commit)
		{
			handleCommitsFromROB(eventQ, event);
		}
		else if (event.getRequestType() == RequestType.Attempt_L1_Issue)
		{
			handleAttemptL1Issue(event);
		}
	}

	public void handleAddressReady(EventQueue eventQ, Event event)
	{
		LSQEntry lsqEntry = ((LSQEntryContainingEvent)(event)).getLsqEntry();
		long virtualAddr = lsqEntry.getAddr();

		if (this.containingMemSys.dTLB.searchTLBForPhyAddr(virtualAddr))
		{
			this.handleAddrValidate(eventQ, event);
		}
		else
		{
			int missPenalty = 0;
			if(containingMemSys.sTLB.doesTLBContainEntry(virtualAddr) == true)
			{
				missPenalty = (int) (containingMemSys.sTLB.getLatency() * containingMemSys.getCore().getStepSize());
			}
			else
			{
				missPenalty = (int) ((containingMemSys.sTLB.getLatency() + containingMemSys.sTLB.getMissPenalty()) * containingMemSys.getCore().getStepSize());
			}

			this.getPort().put(
					event.update(
							eventQ,
							missPenalty,
							null,
							this,
							RequestType.Validate_LSQ_Addr));
		}

		//incrementNumAccesses(1);
	}

	public void handleAddrValidate(EventQueue eventQ, Event event)
	{
		LSQEntry lsqEntry = ((LSQEntryContainingEvent)(event)).getLsqEntry();

		//If the LSQ entry is a load
		if (lsqEntry.getType() == LSQEntryType.LOAD)
		{

			if (!(this.loadValidate(lsqEntry)))
			{
				handleAttemptL1Issue(event);
			}
		}
		else //If the LSQ entry is a store
		{
			this.storeValidate(lsqEntry);
		}
	}

	public void handleAttemptL1Issue(Event event)
	{
		LSQEntry lsqEntry = ((LSQEntryContainingEvent)(event)).getLsqEntry();
		
		if(lsqEntry.isForwarded())
			return;

		boolean requestIssued = this.containingMemSys.issueRequestToL1Cache(RequestType.Cache_Read, lsqEntry.getAddr());

		if(requestIssued == false)
		{
			event.addEventTime(1);
			event.setRequestType(RequestType.Attempt_L1_Issue);
			event.getEventQ().addEvent(event);
		}
		else
		{
			lsqEntry.setIssued(true);
		}
	}

	public void handleMemResponse(long address)
	{
		LSQEntry lsqEntry = null;

		int index = head;
		for(int i = 0; i < curNumLoadsInQ + curNumStoresInQ; i++)
		{
			lsqEntry = lsqueue[index];

			if(lsqEntry.getType() == LSQEntryType.STORE
					&& (lsqEntry.getAddr() == address))
			{
				break;
			}

			if ((lsqEntry.getType() == LSQEntryType.LOAD) &&
					!lsqEntry.isRemoved() &&
					!lsqEntry.isForwarded() &&
					lsqEntry.getAddr() == address)
			{
				if (!lsqEntry.isValid())
				{
					index = (index+1)%lsqSize;
					continue;
				}

				lsqEntry.setForwarded(true);

				//inform pipeline that the load has completed
				if (lsqEntry.getRobEntry() != null && !lsqEntry.getRobEntry().getExecuted())
				{
					((OutOrderCoreMemorySystem)containingMemSys).sendExecComplete(lsqEntry.getRobEntry());
				}
			}

			index = (index+1)%lsqSize;
		}	

		//incrementNumAccesses(1);
	}

	public void handleCommitsFromROB(EventQueue eventQ, Event event)
	{
		LSQEntry lsqEntry = ((LSQEntryContainingEvent)(event)).getLsqEntry();

		/*
		 * a wide OOO pipeline may send multiple commits to the LSQ in one cycle;
		 * these commits need not come in the same order as they were inserted;
		 * (this is due to the priority queue implementation in the event queue)
		 * to handle this, all memory operations from 'head' to the one in the event are
committed
		 */

		int commitUpto = lsqEntry.getIndexInQ();

		if(lsqueue[commitUpto].isRemoved() == true)
		{
			return;
		}

		int i = head;

		for(; ; i = (i+1)%lsqSize)
		{
			LSQEntry tmpEntry = lsqueue[i];

			// if it is a store, send the request to the cache
			if(tmpEntry.getType() == LSQEntry.LSQEntryType.STORE
					|| tmpEntry.getType() == LSQEntryType.LOAD && tmpEntry.isForwarded()) 
			{
				tmpEntry.setCanBeRemoved(true);
			}

			//If it is a LOAD which has not yet received its value
			else
			{
				System.err.println("Error in LSQ " +this.containingMemSys.coreID+ " :  ROB sent commit for a load which has not received its value");
				misc.Error.showErrorAndExit(tmpEntry.getIndexInQ() + " : load : " + tmpEntry.getAddr());
			}

			if(i == commitUpto)
			{
				break;
			}
		}

		//incrementNumAccesses(1);
	}

	public void freeOneEntry(boolean isLoadToBeRemoved)
	{
		boolean removedEnough = false;
		
		while(removedEnough == false)
		{
			LSQEntry tmpEntry = lsqueue[head];
			
			if(tmpEntry.isCanBeRemoved() == false)
			{
				return;
			}
	
			// if it is a store, send the request to the cache
			if(tmpEntry.getType() == LSQEntry.LSQEntryType.STORE) 
			{
				if(tmpEntry.isValid() == false)
				{
					misc.Error.showErrorAndExit("store not ready to be committed");
				}
	
				boolean requestIssued =
						containingMemSys.issueRequestToL1Cache(RequestType.Cache_Write,
								tmpEntry.getAddr());
	
				if(requestIssued == false)
				{
					return; //removals must be in-order : if u can't commit the operation at the head, u can't commit the ones that follow it
				}
	
				else
				{
					if(head == tail)
					{
						head = tail = -1;
					}
					else
					{
						this.head = this.incrementQ(this.head);
					}
					this.curNumStoresInQ--;
					tmpEntry.setRemoved(true);
					
					if(isLoadToBeRemoved == false)
						removedEnough = true;
				}
			}
	
			//If it is a LOAD which has received its value
			else if (tmpEntry.isForwarded())
			{
				if(head == tail)
				{
					head = tail = -1;
				}
				else
				{
					this.head = this.incrementQ(this.head);
				}
				this.curNumLoadsInQ--;
				tmpEntry.setRemoved(true);
				
				if(isLoadToBeRemoved == true)
					removedEnough = true;
			}
	
			//If it is a LOAD which has not yet received its value
			else
			{
				System.err.println("Error in LSQ " +this.containingMemSys.coreID+ " :  ROB sent commit for a load which has not received its value");
				misc.Error.showErrorAndExit(tmpEntry.getIndexInQ() + " : load : " + tmpEntry.getAddr());
			}
			//incrementNumAccesses(1);
		}
	}

	void incrementNumAccesses(int incrementBy)
	{
		numAccesses += incrementBy;
	}

	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		EnergyConfig power = new EnergyConfig(containingMemSys.core.getLsqPower(), numAccesses);
		power.printEnergyStats(outputFileWriter, componentName);
		return power;
	}
}
