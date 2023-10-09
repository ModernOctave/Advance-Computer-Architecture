package emulatorinterface.translator.visaHandler;

import emulatorinterface.DynamicInstructionBuffer;
import generic.Instruction;
import generic.InstructionTable;

public class Load implements DynamicInstructionHandler 
{
	public int handle(int microOpIndex, 
			Instruction microOp, DynamicInstructionBuffer dynamicInstructionBuffer) 
	{
		long memoryReadAddress;
		memoryReadAddress = dynamicInstructionBuffer.
				getSingleLoadAddress(microOp.getCISCProgramCounter());
		
		if(memoryReadAddress != -1)
		{
			microOp.setSourceOperand1MemValue(memoryReadAddress);
			return ++microOpIndex;
		}
		else
		{
			return -1;
		}
	}
}