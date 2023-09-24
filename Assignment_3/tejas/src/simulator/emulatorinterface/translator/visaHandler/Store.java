package emulatorinterface.translator.visaHandler;

import emulatorinterface.DynamicInstructionBuffer;
import generic.Instruction;
import generic.InstructionTable;

import java.util.LinkedList;

public class Store implements DynamicInstructionHandler 
{
	public int handle(int microOpIndex, 
			Instruction microOp, DynamicInstructionBuffer dynamicInstructionBuffer) 
	{
		long memoryWriteAddress;
		memoryWriteAddress = dynamicInstructionBuffer.
				getSingleStoreAddress(microOp.getCISCProgramCounter());
		
		if(memoryWriteAddress!=-1)
		{
			microOp.setSourceOperand1MemValue(memoryWriteAddress);
			return ++microOpIndex;
		}
		else
		{
			return -1;
		}
	}
}