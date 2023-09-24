package pipeline.outoforder;

import generic.Core;
import generic.Event;
import generic.EventQueue;
import generic.ExecCompleteEvent;
import generic.GlobalClock;
import generic.RequestType;
import generic.SimulationElement;
import main.ArchitecturalComponent;
import memorysystem.AddressCarryingEvent;
import memorysystem.Cache;
import memorysystem.CoreMemorySystem;
import memorysystem.LSQEntryContainingEvent;
import memorysystem.coherence.Directory;

public class OutOrderCoreMemorySystem extends CoreMemorySystem {
	
	OutOrderExecutionEngine containingExecEngine;

	public OutOrderCoreMemorySystem(Core core) {
		super(core);
		core.getExecEngine().setCoreMemorySystem(this);
		containingExecEngine = (OutOrderExecutionEngine)core.getExecEngine();
	}
	

	
	//To issue the request to instruction cache
	public void issueRequestToInstrCache(long address)
	{
		if(iCache.AMAT != -1)
		{
			AddressCarryingEvent addressEvent = new AddressCarryingEvent(getCore().getEventQueue(),
					iCache.AMAT*getCore().getStepSize(), iCache, this, RequestType.Mem_Response, address);
			this.getPort().put(addressEvent);
			return;
		}
		
		int tlbMissPenalty = performITLBLookup(address);
		
		AddressCarryingEvent addressEvent = new AddressCarryingEvent(getCore().getEventQueue(),
			tlbMissPenalty, this, iCache, RequestType.Cache_Read, address);

		//attempt issue to lower level cache
		this.iCache.getPort().put(addressEvent);
	}
	
	public void allocateLSQEntry(boolean isLoad, long address, ReorderBufferEntry robEntry)
	{
		robEntry.setLsqEntry(lsqueue.addEntry(isLoad, address, robEntry));
	}
	
	//To issue the request to LSQ
	public void issueRequestToLSQ(SimulationElement requestingElement, 
											ReorderBufferEntry robEntry)
	{
		if(robEntry.isOperand1Available() == false ||
						robEntry.isOperand2Available() == false ||
						robEntry.getAssociatedIWEntry() == null ||
						robEntry.getIssued() == false)
		{
			misc.Error.showErrorAndExit("attempting to validate the address of a load/store that hasn't been issued");
		}
		
		if(l1Cache.AMAT == -1)
		{		
			lsqueue.getPort().put(
				new LSQEntryContainingEvent(
						getCore().getEventQueue(),
						lsqueue.getLatencyDelay(), 
						requestingElement,
						lsqueue, 
						RequestType.Tell_LSQ_Addr_Ready,
						robEntry.getLsqEntry(),
						this.coreID));
		}
		else
		{
			lsqueue.getPort().put(
					new LSQEntryContainingEvent(
							getCore().getEventQueue(),
							lsqueue.getLatencyDelay(), 
							requestingElement,
							lsqueue, 
							RequestType.Validate_LSQ_Addr,
							robEntry.getLsqEntry(),
							this.coreID));
		}
	}
	
	//To issue the request directly to L1 cache
	public boolean issueRequestToL1Cache(RequestType requestType, 
											long address)
	{
		if(l1Cache.AMAT != -1)
		{
			if(requestType == RequestType.Cache_Read)
			{
				AddressCarryingEvent addressEvent = new AddressCarryingEvent(getCore().getEventQueue(),
						l1Cache.AMAT*getCore().getStepSize(), l1Cache, this, RequestType.Mem_Response, address);
				this.getPort().put(addressEvent);
			}
			return true;
		}
		
		AddressCarryingEvent addressEvent = new AddressCarryingEvent(getCore().getEventQueue(),
			0, this, l1Cache, requestType, address);		
		
		if(l1Cache.isBusy(address)) {
			return false;
		}
		if(requestType == RequestType.Cache_Write)
		{
			Cache c = l1Cache;
			while(c != null)
			{
				if(c.canCacheAcceptWriteRequest() == false)
					return false;
				
				c = c.nextLevel;
			}
			this.l1Cache.incrementNumberOfOutstandingWrites();
		}
		
		this.l1Cache.getPort().put(addressEvent);		
				
		return true;
	}
	
	//To commit Store in LSQ
	public void issueLSQCommit(ReorderBufferEntry robEntry)
	{
		lsqueue.getPort().put(
				 new LSQEntryContainingEvent(
						getCore().getEventQueue(),
						lsqueue.getLatencyDelay(),
						null,
						lsqueue, 
						RequestType.LSQ_Commit, 
						robEntry.getLsqEntry(),
						this.coreID));
	}
	
	private int performITLBLookup(long address)
	{
		boolean tLBHit = iTLB.searchTLBForPhyAddr(address);
		int missPenalty = 0;
		if(!tLBHit){
			if(sTLB.doesTLBContainEntry(address) == true)
			{
				missPenalty = (int) (sTLB.getLatency() * getCore().getStepSize());
			}
			else
			{
				missPenalty = (int) ((sTLB.getLatency() + sTLB.getMissPenalty()) * getCore().getStepSize());
			}
		}
		
		return missPenalty;
	}

	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
		
		//handle memory response
		
		AddressCarryingEvent memResponse = (AddressCarryingEvent) event;
		long address = memResponse.getAddress();
		
		// Unified cache scenario
		if(iCache==l1Cache)
		{
			containingExecEngine.getFetcher().processCompletionOfMemRequest(address);
			lsqueue.handleMemResponse(address);
		}

		//if response comes from iCache, inform fetchunit
		else if(memResponse.getRequestingElement() == iCache)
		{
			containingExecEngine.getFetcher().processCompletionOfMemRequest(address);
		}
		
		//if response comes from l1Cache, inform memunit
		else if(memResponse.getRequestingElement() == l1Cache)
		{
			lsqueue.handleMemResponse(address);
		}
		
		else
		{
			misc.Error.showErrorAndExit("mem response received by outordercoreMemSys from unkown object : " + memResponse.getRequestingElement());
		}
	}
	
	public void sendExecComplete(ReorderBufferEntry robEntry)
	{
		getCore().getEventQueue().addEvent(
				new ExecCompleteEvent(
						getCore().getEventQueue(),
						GlobalClock.getCurrentTime() + getCore().getStepSize(),
						null,
						containingExecEngine.getExecuter(),
						RequestType.EXEC_COMPLETE,
						robEntry));
	}

	@Override
	public long getNumberOfMemoryRequests() {
		return getLsqueue().noOfMemRequests;
	}

	@Override
	public long getNumberOfLoads() {
		return getLsqueue().NoOfLd;
	}

	@Override
	public long getNumberOfStores() {
		return getLsqueue().NoOfSt;
	}

	@Override
	public long getNumberOfValueForwardings() {
		return getLsqueue().NoOfForwards;
	}

	@Override
	public void setNumberOfMemoryRequests(long numMemoryRequests) {
		getLsqueue().noOfMemRequests = numMemoryRequests;
	}

	@Override
	public void setNumberOfLoads(long numLoads) {
		getLsqueue().NoOfLd = numLoads;
	}

	@Override
	public void setNumberOfStores(long numStores) {
		getLsqueue().NoOfSt = numStores;
	}

	@Override
	public void setNumberOfValueForwardings(long numValueForwardings) {
		getLsqueue().NoOfForwards = numValueForwardings;
	}



	@Override
	public long getNumberOfSoftwareReadPrefetches() {
		return getLsqueue().NoOfSoftwareReadPrefetch;
	}



	@Override
	public long getNumberOfSoftwareWritePrefetches() {
		return getLsqueue().NoOfSoftwareWritePrefetch;
	}

}
