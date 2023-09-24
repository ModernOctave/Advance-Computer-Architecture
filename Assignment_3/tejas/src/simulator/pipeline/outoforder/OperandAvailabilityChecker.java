package pipeline.outoforder;

import generic.Core;
import generic.Operand;
import generic.OperandType;

public class OperandAvailabilityChecker {
	
	public static boolean[] isAvailable(ReorderBufferEntry reorderBufferEntry,
										Operand opnd,
										int phyReg1,
										int phyReg2,
										Core core)
	//phyReg2 required because if OperandType is memory, 2 physical registers may have to be specified
	{
		if(opnd == null)
		{
			return new boolean[]{true};
		}
		
		OutOrderExecutionEngine execEngine = (OutOrderExecutionEngine) core.getExecEngine();
		OperandType tempOpndType = opnd.getOperandType();
		
		if(tempOpndType == OperandType.immediate ||
				tempOpndType == OperandType.inValid)
		{
			return new boolean[]{true};
		}
		
		if(tempOpndType == OperandType.integerRegister ||
				tempOpndType == OperandType.vectorRegister)
		{
			RenameTable tempRN;
			if(tempOpndType	== OperandType.integerRegister)
			{
				tempRN = execEngine.getIntegerRenameTable();
			}
			else
			{
				tempRN = execEngine.getVectorRenameTable();
			}
			
			if(tempRN.getAssociatedRegisterFile().getValueValid(phyReg1) == true
					|| tempRN.getValueValid(phyReg1) == true
					/*|| tempRN.getProducerROBEntry(phyReg1) == reorderBufferEntry*/)
			{
				return new boolean[]{true};
			}
			else
			{
				return new boolean[]{false};
			}
		}
		
		if(tempOpndType == OperandType.memory)
		{
			return new boolean[]
			 {OperandAvailabilityChecker.isAvailable(reorderBufferEntry, opnd.getMemoryLocationFirstOperand(), phyReg1, phyReg2, core)[0],
			  OperandAvailabilityChecker.isAvailable(reorderBufferEntry, opnd.getMemoryLocationSecondOperand(), phyReg2, phyReg1, core)[0]};
		}
		
		return new boolean[]{true};
	}

}