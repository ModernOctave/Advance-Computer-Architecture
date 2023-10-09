package emulatorinterface.translator.visaHandler;

import config.EmulatorConfig;
import emulatorinterface.DynamicInstructionBuffer;
import generic.Instruction;
import generic.InstructionTable;

public class Jump implements DynamicInstructionHandler 
{
	public int handle(int microOpIndex, 
			Instruction microOp, DynamicInstructionBuffer dynamicInstructionBuffer) 
	{
		//BranchInstr branchInstruction;
		//branchInstruction = dynamicInstructionBuffer.getBranchPacket(microOp.getCISCProgramCounter());
		boolean branchTaken = dynamicInstructionBuffer.getBranchTaken(microOp.getCISCProgramCounter());
		long branchAddress = dynamicInstructionBuffer.getBranchAddress(microOp.getCISCProgramCounter());
		
		//if(branchInstruction != null)
		if(branchAddress != -1)
		{
			microOp.setBranchTaken(true);
			microOp.setBranchTargetAddress(branchAddress);
			
			return ++microOpIndex;
		}
		else
		{
			return -1;
		}
		
	}
}