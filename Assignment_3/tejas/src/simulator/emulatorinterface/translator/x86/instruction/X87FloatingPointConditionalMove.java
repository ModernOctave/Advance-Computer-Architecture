package emulatorinterface.translator.x86.instruction;

import emulatorinterface.translator.InvalidInstructionException;
import emulatorinterface.translator.x86.registers.Registers;
import emulatorinterface.translator.x86.registers.TempRegisterNum;
import generic.Operand;
import generic.InstructionList;
import generic.Instruction;

public class X87FloatingPointConditionalMove implements X86StaticInstructionHandler 
{
	public void handle(long instructionPointer, 
			Operand operand1, Operand operand2, Operand operand3,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum)
					throws InvalidInstructionException
	{
		if(operand1.isFloatRegisterOperand() && operand2==null
				&& operand3==null)
		{
			instructionArrayList.appendInstruction(Instruction.getFloatingPointALU(operand1, Registers.getFloatingPointControlWord(), Registers.getTopFPRegister()));
		}
		
		else if(operand1.isFloatRegisterOperand() && operand2.isFloatRegisterOperand()
				&& operand3==null)
		{
			instructionArrayList.appendInstruction(Instruction.getFloatingPointALU(operand2, Registers.getFloatingPointControlWord(), operand1));
		}
		
		else
		{
			misc.Error.invalidOperation("Floating Point Conditional Move", operand1, operand2, operand3);
		}
	}
}
