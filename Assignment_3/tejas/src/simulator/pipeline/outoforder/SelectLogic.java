package pipeline.outoforder;

import generic.Core;
import generic.Event;
import generic.EventQueue;
import generic.PortType;
import generic.SimulationElement;

public class SelectLogic extends SimulationElement {
	
	Core core;
	OutOrderExecutionEngine execEngine;
	InstructionWindow IW;	
	int issueWidth;
	
	public SelectLogic(Core core, OutOrderExecutionEngine execEngine)
	{
		super(PortType.Unlimited, -1, -1, -1, -1);
		this.core = core;
		this.execEngine = execEngine;
		IW = execEngine.getInstructionWindow();
		issueWidth = core.getIssueWidth();
	}
	
	/*
	 * ready instructions' issue are attempted (maximum of 'issueWidth' number of issues)
	 * important - all issues must be attempted first; only then must awakening be done
	 * 		this is because an awakened instruction is a
	 * 		candidate for issue ONLY in the next cycle 
	 */
	public void performSelect()
	{
		ReorderBuffer ROB = execEngine.getReorderBuffer();		
		if(execEngine.isToStall5() == true /*pipeline stalled due to branch mis-prediction*/
				|| ROB.head == -1 /*ROB empty*/)
		{
			return;
		}
		
		execEngine.getExecutionCore().clearPortUsage();
		
		int noIssued = 0;
		int i;
		ReorderBufferEntry ROBEntry;
		
		i = ROB.head;
		do
		{
			ROBEntry = ROB.ROB[i];
			
			if(ROBEntry.getIssued() == false &&
					ROBEntry.getAssociatedIWEntry() != null)
			{
				if(ROBEntry.getAssociatedIWEntry().issueInstruction())
				{
					//if issued
					noIssued++;						
				}
			}
			
			if(noIssued >= issueWidth)
			{
				break;
			}
			
			i = (i+1)%ROB.MaxROBSize;
			
		}while(i != (ROB.tail+1)%ROB.MaxROBSize);
	}

	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
				
	}

}
