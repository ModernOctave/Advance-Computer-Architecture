package emulatorinterface.translator.x86.instruction;


import emulatorinterface.translator.InvalidInstructionException;
import emulatorinterface.translator.x86.operand.OperandTranslator;
import emulatorinterface.translator.x86.registers.Registers;
import emulatorinterface.translator.x86.registers.TempRegisterNum;
import generic.Instruction;
import generic.Operand;
import generic.InstructionList;

public class X87FloatingPointCompare implements X86StaticInstructionHandler 
{
	public void handle(long instructionPointer, 
			Operand operand1, Operand operand2, Operand operand3,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum)
					throws InvalidInstructionException
	{
	
		if(operand1==null && operand2==null && operand3==null)
		{
			//Both the operands are implicit i.e st0 and st1
			Operand st0 = Registers.getTopFPRegister();
			Operand st1 = Registers.getSecondTopFPRegister();
			
			instructionArrayList.appendInstruction(Instruction.getFloatingPointALU(st0, st1, Registers.getFloatingPointControlWord()));
		}
		
		else if(operand1.isFloatRegisterOperand() && operand2==null && operand3==null)
		{
			//First implicit operand is implicitly st0 and second operand is passed as argument.
			Operand st0 = Registers.getTopFPRegister();

			instructionArrayList.appendInstruction(Instruction.getFloatingPointALU(st0, operand1, Registers.getFloatingPointControlWord()));
		}
		
		else if(operand1.isFloatRegisterOperand() && operand2.isFloatRegisterOperand() && operand3==null)
		{
			//Both the operands are passed as operands to the command.
			instructionArrayList.appendInstruction(Instruction.getFloatingPointALU(operand1, operand2, Registers.getFloatingPointControlWord()));
		}
		
		else if(operand1.isMemoryOperand() && operand2==null && operand3==null)
		{
			//First implicit operand is implicitly st0 and second operand is passed as argument.
			Operand st0 = Registers.getTopFPRegister();
			Operand tempFloatRegister = OperandTranslator.processSourceMemoryOperand(operand1, instructionArrayList, tempRegisterNum, false);
			
			//tempFloatRegister = [operand1]
			instructionArrayList.appendInstruction(Instruction.getFloatingPointALU(st0, tempFloatRegister, Registers.getFloatingPointControlWord()));
		}
		
 		else
		{
			misc.Error.invalidOperation("Floating Point Compare for ip=" 
					+ instructionPointer
					, operand1, operand2, operand3);
		}
	}
}
