package emulatorinterface.translator.visaHandler;

import emulatorinterface.DynamicInstructionBuffer;
import generic.Instruction;
import generic.InstructionTable;

public interface DynamicInstructionHandler 
{
	// This function will return the next instruction.
	// It takes an Instruction and a DynamicInstructionBuffer, changes microOp appropriately.
	// It will raise an error and terminate if it is not able to get an expected value, from
	// dynamicInstructionBuffer.
	public int handle(int microOpIndex, Instruction microOp, 
			DynamicInstructionBuffer dynamicInstructionBuffer);
}
