package pipeline.multi_issue_inorder;

import config.SimulationConfig;
import memorysystem.AddressCarryingEvent;
import memorysystem.Cache;
import pipeline.outoforder.OutOrderCoreMemorySystem;
import generic.Core;
import generic.Event;
import generic.EventQueue;
import generic.GlobalClock;
import generic.Instruction;
import generic.Operand;
import generic.OperationType;
import generic.PortType;
import generic.RequestType;
import generic.SimulationElement;

public class MemUnitIn_MII extends SimulationElement{
	
	Core core;
	MultiIssueInorderExecutionEngine containingExecutionEngine;
	EventQueue eventQueue;
	StageLatch_MII exMemLatch;
	StageLatch_MII memWbLatch;
	
	long instCtr; //for debug

	public MemUnitIn_MII(Core core, MultiIssueInorderExecutionEngine execEngine) {
		super(PortType.Unlimited, -1, -1 , -1, -1);
		this.core = core;
		this.eventQueue = core.getEventQueue();
		containingExecutionEngine = execEngine;
		exMemLatch = execEngine.getExMemLatch();
		memWbLatch = execEngine.getMemWbLatch();
		
		instCtr = 0;
	}
	
	public void performMemEvent(MultiIssueInorderPipeline inorderPipeline)
	{
		if(containingExecutionEngine.getMispredStall() > 0)
		{
			return;
		}
		
		Instruction ins = null;
		
		while(exMemLatch.isEmpty() == false
				&& memWbLatch.isFull() == false)
		{
			ins = exMemLatch.peek(0);
			
			if(ins != null)
			{
				long lat = 1;
				if(ins.getOperationType() == OperationType.load)
				{
					//issue load request
					boolean memReqIssued = containingExecutionEngine.multiIssueInorderCoreMemorySystem.issueRequestToL1Cache(
							RequestType.Cache_Read,
							ins.getSourceOperand1MemValue());

					if(memReqIssued == false)
					{
						break;
					}
					
					//set instruction's MEM stage completion time to Long.MAX_VALUE
					lat = Long.MAX_VALUE - GlobalClock.getCurrentTime();
				}
				if(ins.getOperationType() == OperationType.store)
				{
					boolean memReqIssued = containingExecutionEngine.multiIssueInorderCoreMemorySystem.issueRequestToL1Cache(
							RequestType.Cache_Write,
							ins.getSourceOperand1MemValue());

					if(memReqIssued == false)
					{
						break;
					}
				}
				if(ins.getOperationType() == OperationType.read_prefetch)
				{
					Cache cacheToPrefetchTo = containingExecutionEngine.getCoreMemorySystem().getL1Cache();
					for(int i = 2; i <= ins.getSourceOperand2().getValue(); i++)
					{
						cacheToPrefetchTo = cacheToPrefetchTo.nextLevel;
					}
					
					long addr = ins.getSourceOperand1MemValue();
					
					if(cacheToPrefetchTo.isBusy(addr))
						break;
					
					AddressCarryingEvent addressEvent = new AddressCarryingEvent(core.getEventQueue(),
							0, containingExecutionEngine.getCoreMemorySystem(), cacheToPrefetchTo, RequestType.Cache_Read, addr);
					
					cacheToPrefetchTo.getPort().put(addressEvent);
					
					((InorderCoreMemorySystem_MII)containingExecutionEngine.getCoreMemorySystem()).NoOfSoftwareReadPrefetch++;
				}
				if(ins.getOperationType() == OperationType.write_prefetch)
				{
					Cache cacheToPrefetchTo = containingExecutionEngine.getCoreMemorySystem().getL1Cache();
					
					long addr = ins.getSourceOperand1MemValue();
					
					if(cacheToPrefetchTo.isBusy(addr))
						break;
					
					AddressCarryingEvent addressEvent = new AddressCarryingEvent(core.getEventQueue(),
							0, containingExecutionEngine.getCoreMemorySystem(), cacheToPrefetchTo, RequestType.Cache_Write, addr);
					/*
					 * setting as cache write is an approximation
					 * this would dirty the cache line, and that should actually not happen
					 */
					
					cacheToPrefetchTo.getPort().put(addressEvent);
					
					((InorderCoreMemorySystem_MII)containingExecutionEngine.getCoreMemorySystem()).NoOfSoftwareWritePrefetch++;
				}
				
				if(ins.getSerialNo() != instCtr && ins.getOperationType() != OperationType.inValid)
				{
					misc.Error.showErrorAndExit("mem out of order!!");
				}
				instCtr++;
				
				//move ins to next stage
				memWbLatch.add(ins, GlobalClock.getCurrentTime() + lat);
				exMemLatch.poll();
				
				if(SimulationConfig.debugMode)
				{
					System.out.println("MEM : " + GlobalClock.getCurrentTime()/core.getStepSize() + "\n"  + ins + "\n");
				}
			}
			else
			{
				break;
			}
		}
	}


	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
		updateInstructionCompletions(((AddressCarryingEvent)event).getAddress());
	}
	
	public void processCompletionOfMemRequest(long requestedAddress)
	{
		updateInstructionCompletions(requestedAddress);
		containingExecutionEngine.noOfOutstandingLoads--;
	}
	
	void updateInstructionCompletions(long address)
	{
		Instruction instructions[] = memWbLatch.getInstructions();
		long instructionCompletesAt[] = memWbLatch.getInstructionCompletesAt();
		
		for(int i = 0; i < containingExecutionEngine.getIssueWidth(); i++)
		{
			if(instructions[i] != null
					&& instructions[i].getOperationType() == OperationType.load
					&& instructions[i].getSourceOperand1MemValue() == address
					&& instructionCompletesAt[i] > GlobalClock.getCurrentTime())
			{
				instructionCompletesAt[i] = GlobalClock.getCurrentTime();
				
				Operand destOpnd = instructions[i].getDestinationOperand();
				if(destOpnd.isIntegerRegisterOperand())
				{
					containingExecutionEngine.getValueReadyInteger()[(int)destOpnd.getValue()]
							 												= GlobalClock.getCurrentTime();
				}
				else if(destOpnd.isFloatRegisterOperand())
				{
					containingExecutionEngine.getValueReadyFloat()[(int)destOpnd.getValue()]
							 												= GlobalClock.getCurrentTime();
				}
			}
		}
	}
}
