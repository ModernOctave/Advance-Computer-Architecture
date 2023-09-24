package emulatorinterface.translator.visaHandler;

import emulatorinterface.DynamicInstructionBuffer;
import generic.Instruction;
import generic.InstructionTable;

public class AcceleratedOp implements DynamicInstructionHandler 
{
	public int handle(int microOpIndex,
			Instruction microOp, DynamicInstructionBuffer dynamicInstructionBuffer) 
	{
		return ++microOpIndex;
	}
}
