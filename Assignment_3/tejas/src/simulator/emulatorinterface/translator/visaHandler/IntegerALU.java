package emulatorinterface.translator.visaHandler;

import emulatorinterface.DynamicInstructionBuffer;
import generic.Instruction;
import generic.InstructionTable;

public class IntegerALU implements DynamicInstructionHandler 
{
	public int handle(int microOpIndex, 
			Instruction microOp, DynamicInstructionBuffer dynamicInstructionBuffer) 
	{
		microOp.setPredicateAndNotExecuted(dynamicInstructionBuffer.isPredicateInsnNotExecuted());
		return ++microOpIndex;
	}
}
