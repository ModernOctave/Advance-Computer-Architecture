package pipeline.outoforder;

import java.io.FileWriter;
import java.io.IOException;

import config.EnergyConfig;
import config.SimulationConfig;
import generic.Core;
import generic.Event;
import generic.EventQueue;
import generic.ExecCompleteEvent;
import generic.GlobalClock;
import generic.OperationType;
import generic.PortType;
import generic.RequestType;
import generic.SimulationElement;

public class ExecutionLogic extends SimulationElement {
	
	Core core;
	OutOrderExecutionEngine execEngine;
	ReorderBuffer ROB;
	
	long numResultsBroadCastBusAccess;
	
	public ExecutionLogic(Core core, OutOrderExecutionEngine execEngine)
	{
		super(PortType.Unlimited, -1, -1, -1, -1);
		
		this.core = core;
		this.execEngine = execEngine;
	}

	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
				
		ROB = execEngine.getReorderBuffer();
		ReorderBufferEntry reorderBufferEntry = null;
		
		if(event.getRequestType() == RequestType.EXEC_COMPLETE)
		{
			reorderBufferEntry = ((ExecCompleteEvent)event).getROBEntry();
		}
		else if(event.getRequestType() == RequestType.BROADCAST)
		{
			reorderBufferEntry = ((BroadCastEvent)event).getROBEntry();
		}
		else
		{
			misc.Error.showErrorAndExit("execution logic received unknown event " + event);
		}
				
		if(event.getRequestType() == RequestType.EXEC_COMPLETE)
		{
			handleExecutionCompletion(reorderBufferEntry);
		}
		else if(event.getRequestType() == RequestType.BROADCAST)
		{
			performBroadCast(reorderBufferEntry);
		}
	}
	
	public void handleExecutionCompletion(ReorderBufferEntry reorderBufferEntry)
	{
		//assertions
		if(reorderBufferEntry.getExecuted() == true ||
				reorderBufferEntry.isRenameDone() == false ||
				reorderBufferEntry.getIssued() == false)
		{
			misc.Error.showErrorAndExit("cannot complete execution of this instruction");
		}		
		if(reorderBufferEntry.getIssued() == false)
		{
			misc.Error.showErrorAndExit("not yet issued, but execution complete");
		}
		if(reorderBufferEntry.getInstruction().getOperationType() == OperationType.load)
		{
			if(reorderBufferEntry.getLsqEntry().isValid() == false)
			{
				misc.Error.showErrorAndExit("invalid load has completed execution");
			}
			if(reorderBufferEntry.getLsqEntry().isForwarded() == false)
			{
				misc.Error.showErrorAndExit("unforwarded load has completed execution");
			}
		}
		
		//set execution complete
		reorderBufferEntry.setExecuted(true);
		
		//wake up dependent instructions
		if(reorderBufferEntry.getInstruction().getDestinationOperand() != null)
		{
			performBroadCast(reorderBufferEntry);
			incrementResultsBroadcastBusAccesses(1);
		}
		else
		{
			//this is an instruction that doesn't write to the register file,
			//	such as store, branch, jump, nop
			//no need for the wake-up procedure
			
			reorderBufferEntry.setWriteBackDone1(true);
			reorderBufferEntry.setWriteBackDone2(true);
		}
		
		ROB.incrementNumAccesses(1);
		
		if(SimulationConfig.debugMode)
		{
			System.out.println("executed : " + GlobalClock.getCurrentTime()/core.getStepSize() + " : "  + reorderBufferEntry.getInstruction());
		}
		
	}
	
	//wake up dependent instructions in the IW
	void performBroadCast(ReorderBufferEntry reorderBufferEntry)
	{
		if(reorderBufferEntry.getInstruction().getDestinationOperand() != null)
		{
			WakeUpLogic.wakeUpLogic(core,
									reorderBufferEntry.getInstruction().getDestinationOperand().getOperandType(),
									reorderBufferEntry.getPhysicalDestinationRegister(),
									reorderBufferEntry.getThreadID(),
									(reorderBufferEntry.pos + 1)%ROB.MaxROBSize);
		}
	}
	
	void incrementResultsBroadcastBusAccesses(int incrementBy)
	{
		numResultsBroadCastBusAccess += incrementBy;
	}

	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		EnergyConfig power = new EnergyConfig(core.getResultsBroadcastBusPower(), numResultsBroadCastBusAccess);
		power.printEnergyStats(outputFileWriter, componentName);
		return power;
	}
	
}
