package emulatorinterface.translator.x86.instruction;

import emulatorinterface.translator.InvalidInstructionException;
import emulatorinterface.translator.x86.registers.Registers;
import emulatorinterface.translator.x86.registers.TempRegisterNum;
import generic.Instruction;
import generic.InstructionList;
import generic.Operand;

public class FlagsLoad implements X86StaticInstructionHandler 
{
	public void handle(long instructionPointer, 
			Operand operand1, Operand operand2, Operand operand3,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum) 
					throws InvalidInstructionException
	{
		if(operand1 == null && operand2 == null && operand3 == null)
		{
			instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(Registers.getAccumulatorRegister(), null, Registers.getEFlagsRegister()));
		}
		
		else
		{
			misc.Error.invalidOperation("Flags Load ", operand1, operand2, operand3);
		}
	}

}
