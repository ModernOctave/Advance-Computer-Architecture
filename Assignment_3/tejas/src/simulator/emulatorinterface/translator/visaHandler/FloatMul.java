package emulatorinterface.translator.visaHandler;

import emulatorinterface.DynamicInstructionBuffer;
import generic.Instruction;
import generic.InstructionTable;

public class FloatMul implements DynamicInstructionHandler 
{
	public int handle(int microOpIndex, 
			Instruction microOp, DynamicInstructionBuffer dynamicInstructionBuffer) 
	{
		//nothing to be done in such cases
		return ++microOpIndex;
	}
}
