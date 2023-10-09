package emulatorinterface.translator.visaHandler;

import config.EmulatorConfig;
import emulatorinterface.DynamicInstructionBuffer;
import generic.Instruction;
import generic.InstructionTable;

public class Branch implements DynamicInstructionHandler 
{
	public int handle(int microOpIndex, 
			Instruction microOp, DynamicInstructionBuffer dynamicInstructionBuffer)
	{
		boolean branchTaken = dynamicInstructionBuffer.getBranchTaken(microOp.getCISCProgramCounter());
		long branchAddress = dynamicInstructionBuffer.getBranchAddress(microOp.getCISCProgramCounter());
		//BranchInstr branchInstruction;
		//branchInstruction = dynamicInstructionBuffer.getBranchPacket(microOp.getCISCProgramCounter()); 

		//if(branchInstruction != null)
		if(branchAddress!=-1) // branchAddress = -1 indicates there was no branch packet
		{
			microOp.setBranchTaken(branchTaken);
			microOp.setBranchTargetAddress(branchAddress);
			
			return ++microOpIndex; // Actually the next micro-op will be from a different cisc instruction
		}
		else
		{
			return -1;
		}
	}
}
