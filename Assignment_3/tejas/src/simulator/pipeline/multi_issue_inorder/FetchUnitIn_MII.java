package pipeline.multi_issue_inorder;

import main.ArchitecturalComponent;
import main.CustomObjectPool;
import memorysystem.AddressCarryingEvent;
import memorysystem.MemorySystem;
import config.CoreConfig;
import config.SimulationConfig;
import config.SystemConfig;
import generic.Barrier;
import generic.BarrierTable;
import generic.Core;
import generic.Event;
import generic.EventQueue;
import generic.GenericCircularQueue;
import generic.GlobalClock;
import generic.Instruction;
import generic.OperationType;
import generic.PortType;
import generic.RequestType;
import generic.SimulationElement;

public class FetchUnitIn_MII extends SimulationElement
{
	Core core;
	MultiIssueInorderExecutionEngine containingExecutionEngine;
	
	public GenericCircularQueue<Instruction> inputToPipeline;
	StageLatch_MII ifId_latch;
	
	Instruction fetchBuffer[];
	public int fetchBufferCapacity;
	private int fetchFillCount;	//Number of instructions in the fetch buffer
	private int fetchBufferIndex;	//Index to first instruction to be popped out of fetch buffer
	private boolean fetchBufferStatus[];  // To check whether request to ICache is complete or not
	
	private boolean sleep;		//The boolean to stall the pipeline when a sync request is received
	int syncCount;
	long numRequestsSent;
	int numRequestsAcknowledged;
	
	long instCtr; //for debug

	
	public FetchUnitIn_MII(Core core, EventQueue eventQueue, MultiIssueInorderExecutionEngine execEngine)
	{
		super(PortType.Unlimited, -1, -1, -1, -1);
		
		this.core = core;
		this.containingExecutionEngine = execEngine;
		
		this.ifId_latch = execEngine.getIfIdLatch();
		
		this.fetchBufferCapacity = (int)(core.getIssueWidth()
				* SystemConfig.core[core.getCore_number()].getICacheLatency());
		this.fetchBuffer = new Instruction[this.fetchBufferCapacity];
		this.fetchFillCount=0;
		this.fetchBufferIndex=0;
		this.fetchBufferStatus = new boolean[this.fetchBufferCapacity];
		for(int i=0;i<this.fetchBufferCapacity;i++)
		{
			this.fetchBufferStatus[i]=false;
		}
		
		this.sleep=false;
		this.syncCount=0;
		this.numRequestsSent=0;
		this.numRequestsAcknowledged=0;
		instCtr = 0;
	}
	
	public void fillFetchBuffer(MultiIssueInorderPipeline inorderPipeline)
	{
		Instruction newInstruction=null;
		for(int i=(this.fetchBufferIndex+this.fetchFillCount)%this.fetchBufferCapacity;this.fetchFillCount<this.fetchBufferCapacity
				;i = (i+1)%this.fetchBufferCapacity){
			
			if(inputToPipeline.isEmpty()
					|| containingExecutionEngine.multiIssueInorderCoreMemorySystem.getiCache().isBusy(inputToPipeline.peek(0).getCISCProgramCounter())){
				break;
			}
			
			newInstruction = inputToPipeline.pollFirst();
			
			if(newInstruction.getOperationType() == OperationType.load ||
					newInstruction.getOperationType() == OperationType.store)
			{
				if(SimulationConfig.detachMemSysData == true)
				{
					CustomObjectPool.getInstructionPool().returnObject(newInstruction);
					i--;
					continue;
				}
			}
			
			numRequestsSent++;
			if(newInstruction.getOperationType() == OperationType.inValid) {
				this.fetchBuffer[i] = newInstruction;
				this.fetchBufferStatus[i]=true;
				this.fetchFillCount++;
				return;
			}
			
			else
			{
				this.fetchBuffer[i]= newInstruction;
				this.fetchFillCount++;
				if(newInstruction.getOperationType() != OperationType.sync)
				{
					newInstruction.setSerialNo(instCtr++);
				}

				// The first micro-operation of an instruction has a valid CISC IP. All the subsequent 
				// micro-ops will have IP = -1(meaning invalid). We must not forward this requests to iCache.
				if(SimulationConfig.detachMemSysInsn || newInstruction.getCISCProgramCounter()==-1)
				{
					this.fetchBufferStatus[i]=true;
				}
				else
				{
					this.fetchBufferStatus[i]=false;
					containingExecutionEngine.multiIssueInorderCoreMemorySystem.issueRequestToInstrCache(
							newInstruction.getCISCProgramCounter());
				}
			}
		}
	}
	
	public void performFetch(MultiIssueInorderPipeline inorderPipeline)
	{
		if(containingExecutionEngine.getMispredStall() > 0)
		{
			containingExecutionEngine.decrementMispredStall(1);
			return;
		}
		
		Instruction ins;
			
		if(!this.fetchBufferStatus[this.fetchBufferIndex])
			containingExecutionEngine.incrementInstructionMemStall(1); 

		//move to the IF-ID latch those instructions that have completed
		//fetch from the i-cache
		while(!this.sleep && this.fetchFillCount > 0
				&& this.fetchBufferStatus[this.fetchBufferIndex]
				&& this.ifId_latch.isFull() == false)
	        	{
					ins = this.fetchBuffer[this.fetchBufferIndex];

					if(ins.getOperationType()==OperationType.sync)
					{
						this.fetchFillCount--;
						this.fetchBufferIndex = (this.fetchBufferIndex+1)%this.fetchBufferCapacity;
						long barrierAddress = ins.getCISCProgramCounter();
						Barrier bar = BarrierTable.barrierList.get(barrierAddress);
						bar.incrementThreads();
						if(this.core.TreeBarrier == true){
							setSleep(true);
							int coreId = this.core.getCore_number();
							ArchitecturalComponent.coreBroadcastBus.getPort().put(new AddressCarryingEvent(
									0,this.core.eventQueue,
									 this.core.barrier_latency,
									 ArchitecturalComponent.coreBroadcastBus, 
									 ArchitecturalComponent.coreBroadcastBus, 
									 RequestType.TREE_BARRIER, 
									 barrierAddress,
									 coreId));
						}
						else{
							if(bar.timeToCross())
							{
								sleepThePipeline();
								int bar_lat;
								
								if(this.core.barrierUnit == 0){
									if(GlobalClock.getCurrentTime() < bar.time + 35)
									{
										bar_lat = (int)(this.core.barrier_latency + GlobalClock.getCurrentTime() - bar.time);
									}
									else
										bar_lat = this.core.barrier_latency;
								}
								else{
									if(GlobalClock.getCurrentTime() < bar.time + 4)
									{
										bar_lat = (int)(this.core.barrier_latency + GlobalClock.getCurrentTime() - bar.time);
									}
									else
										bar_lat = this.core.barrier_latency;
								}
								for(int i=0; i<bar.getNumThreads(); i++ ){
									ArchitecturalComponent.coreBroadcastBus.addToResumeCore(bar.getBlockedThreads().elementAt(i));
								}
								ArchitecturalComponent.coreBroadcastBus.getPort().put(new AddressCarryingEvent(
										 this.core.eventQueue,
										 bar_lat,
										 ArchitecturalComponent.coreBroadcastBus, 
										 ArchitecturalComponent.coreBroadcastBus, 
										 RequestType.PIPELINE_RESUME, 
										 0));
	
							}
							else
							{
								sleepThePipeline();
								return;
							}
						}
					}
					else{
						this.ifId_latch.add(ins, GlobalClock.getCurrentTime() + 1);
						this.fetchFillCount--;
						this.fetchBuffer[this.fetchBufferIndex] = null;
						this.fetchBufferStatus[this.fetchBufferIndex] = false;
						this.fetchBufferIndex = (this.fetchBufferIndex+1)%this.fetchBufferCapacity;
						
						if(SimulationConfig.debugMode)
						{
							System.out.println("fetched : " + GlobalClock.getCurrentTime()/core.getStepSize() + "\n"  + ins + "\n");
						}
					}
			}
		
		//remove micro-ops from inputToPipeline and issue request to the i-cache
		fillFetchBuffer(inorderPipeline);
	}
	
	public void setSleep(boolean _sleep){
		this.sleep=_sleep;
	}
	public boolean getSleep(){
		return this.sleep;
	}
	public void sleepThePipeline(){
		System.out.println("sleeping pipeline" + this.core.getCore_number()+ "...!!");
		this.syncCount--;
		this.sleep=true;
	}	
	public GenericCircularQueue<Instruction> getInputToPipeline(){
		return this.inputToPipeline;
	}
	public void setInputToPipeline(GenericCircularQueue<Instruction> inpList){
		this.inputToPipeline = inpList;
	}
	public void resumePipeline(){
		System.out.println("Resuming the pipeline "+this.core.getCore_number() + "...!!");
		this.syncCount++;
		this.sleep=false;
	}

	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
				
	}
	
	public void processCompletionOfMemRequest(long requestedAddress)
	{
		for(int i=0;i<this.fetchBufferCapacity;i++){
			if(this.fetchBuffer[i] != null && 
					this.fetchBuffer[i].getCISCProgramCounter() == requestedAddress && 
					this.fetchBufferStatus[i]==false){
				this.fetchBufferStatus[i]=true;
			}
		}
	}

}
