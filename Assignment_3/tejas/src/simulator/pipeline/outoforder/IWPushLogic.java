package pipeline.outoforder;

import config.SimulationConfig;
import generic.Core;
import generic.Event;
import generic.EventQueue;
import generic.GenericCircularQueue;
import generic.GlobalClock;
import generic.OperationType;
import generic.PortType;
import generic.SimulationElement;

public class IWPushLogic extends SimulationElement {
	
	Core core;
	OutOrderExecutionEngine execEngine;
	GenericCircularQueue<ReorderBufferEntry> renameBuffer;
	InstructionWindow IW;
	int renameWidth;	
	
	public IWPushLogic(Core core, OutOrderExecutionEngine execEngine)
	{
		super(PortType.Unlimited, -1, -1 , -1, -1);
		this.core = core;
		this.execEngine = execEngine;
		renameBuffer = execEngine.getRenameBuffer();		
		IW = execEngine.getInstructionWindow();
		renameWidth = core.getRenameWidth();
	}
	
	/*
	 * for each instruction in the renameBuffer, if there is place in the IW, make an entry
	 * else, indicate that all preceding stages must stall from the next cycle
	 */
	public void performIWPush()
	{
		if(execEngine.isToStall5() == true /*pipeline stalled due to branch mis-prediction*/)
		{
			return;
		}
		
		for(int i = 0; i < renameWidth; i++)
		{
			ReorderBufferEntry headROBEntry = renameBuffer.peek(0);
			if(headROBEntry != null)
			{
				if(headROBEntry.getInstruction().getOperationType() == OperationType.inValid ||
						headROBEntry.getInstruction().getOperationType() == OperationType.nop ||
						headROBEntry.getInstruction().getOperationType() == OperationType.cpuid ||
						headROBEntry.getInstruction().getOperationType() == OperationType.mfence ||
						headROBEntry.getInstruction().getOperationType() == OperationType.sfence ||
						headROBEntry.getInstruction().getOperationType() == OperationType.lfence)
				{
					//need not be added to instruction window
					headROBEntry.setIssued(true);
					headROBEntry.setExecuted(true);
					headROBEntry.setWriteBackDone1(true);
					headROBEntry.setWriteBackDone2(true);
				}
				else
				{
					if(IW.isFull())
					{
						execEngine.setToStall1(true);
						break;
					}
					else
					{
						if(headROBEntry.isRenameDone() == false)
						{
							misc.Error.showErrorAndExit("cannot push an instruction that hasn't been renamed");
						}
						
						if(SimulationConfig.debugMode)
						{
							System.out.println("IW push : " + GlobalClock.getCurrentTime()/core.getStepSize() + " : "  + headROBEntry.getInstruction());
						}
						
						//add to IW
						IW.addToWindow(headROBEntry);
					}
				}
					
				//remove from rename buffer
				renameBuffer.dequeue();
				
				execEngine.setToStall1(false);
			}
			else
			{
				break;
			}
		}
	}

	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
		
	}

}
