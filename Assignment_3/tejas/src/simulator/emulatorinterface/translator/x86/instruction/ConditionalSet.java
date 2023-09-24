package emulatorinterface.translator.x86.instruction;

import emulatorinterface.translator.InvalidInstructionException;
import emulatorinterface.translator.x86.registers.Registers;
import emulatorinterface.translator.x86.registers.TempRegisterNum;
import generic.Instruction;
import generic.Operand;
import generic.InstructionList;

public class ConditionalSet implements X86StaticInstructionHandler 
{
	public void handle(long instructionPointer, 
			Operand operand1, Operand operand2, Operand operand3,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum)
					throws InvalidInstructionException
	{
		if((operand1.isIntegerRegisterOperand()) && 
				operand2==null && operand3==null)
		{
			Instruction newInstruction = Instruction.getIntALUInstruction(Registers.getEFlagsRegister(), 
					operand1, operand1);
			newInstruction.setPredicate(true);
			instructionArrayList.appendInstruction(newInstruction);
		}
		
		else if(operand1.isMemoryOperand())
		{
			Instruction newInstruction = Instruction.getStoreInstruction(operand1, 
					Registers.getEFlagsRegister());
			newInstruction.setPredicate(true);
			instructionArrayList.appendInstruction(newInstruction);
		}
		
		else
		{
			misc.Error.invalidOperation("Conditional Set", operand1, operand2, operand3);
		}
	}
}