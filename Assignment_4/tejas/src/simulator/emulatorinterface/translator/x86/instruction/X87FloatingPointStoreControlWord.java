package emulatorinterface.translator.x86.instruction;

import emulatorinterface.translator.InvalidInstructionException;
import emulatorinterface.translator.x86.operand.OperandTranslator;
import emulatorinterface.translator.x86.registers.Registers;
import emulatorinterface.translator.x86.registers.TempRegisterNum;
import generic.Instruction;
import generic.Operand;
import generic.InstructionList;

public class X87FloatingPointStoreControlWord implements X86StaticInstructionHandler 
{
	public void handle(long instructionPointer, 
			Operand operand1, Operand operand2, Operand operand3,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum) 
					throws InvalidInstructionException
	{
		if(operand1.isMemoryOperand() && 
				operand2==null && operand3==null)
		{
			OperandTranslator.processDestinationMemoryOperand(Registers.getFloatingPointControlWord(), operand1, instructionArrayList, tempRegisterNum);
		}
		
		else
		{
			misc.Error.invalidOperation("Floating Point Store Control Word", operand1, operand2, operand3);
		}
	}
}