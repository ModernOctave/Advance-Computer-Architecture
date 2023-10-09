/*****************************************************************************
				Tejas Simulator
------------------------------------------------------------------------------------------------------------

   Copyright [2010] [Indian Institute of Technology, Delhi]
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
------------------------------------------------------------------------------------------------------------

	Contributors:  Prathmesh Kallurkar
*****************************************************************************/

package emulatorinterface.translator.x86.instruction;


import emulatorinterface.translator.InvalidInstructionException;
import emulatorinterface.translator.x86.operand.OperandTranslator;
import emulatorinterface.translator.x86.registers.Registers;
import emulatorinterface.translator.x86.registers.TempRegisterNum;
import generic.Instruction;
import generic.Operand;
import generic.InstructionList;

public class PopAll implements X86StaticInstructionHandler 
{
	public void handle(long instructionPointer, 
			Operand operand1, Operand operand2, Operand operand3,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum)
					throws InvalidInstructionException
	{
		// Create stack-pointer and [stack-pointer]
		Operand stackPointer = Registers.getStackPointer();
		Operand stackPointerLocation = Operand.getMemoryOperand(stackPointer, null);
		
		OperandTranslator.processSourceMemoryOperand(stackPointerLocation, Registers.getDestinationIndexRegister(), instructionArrayList, tempRegisterNum);
		OperandTranslator.processSourceMemoryOperand(stackPointerLocation, Registers.getSourceIndexRegister(), instructionArrayList, tempRegisterNum);
		OperandTranslator.processSourceMemoryOperand(stackPointerLocation, Registers.getBasePointer(), instructionArrayList, tempRegisterNum);
		OperandTranslator.processSourceMemoryOperand(stackPointerLocation, Registers.getStackPointer(), instructionArrayList, tempRegisterNum);
		OperandTranslator.processSourceMemoryOperand(stackPointerLocation, Registers.getBaseIndexRegister(), instructionArrayList, tempRegisterNum);
		OperandTranslator.processSourceMemoryOperand(stackPointerLocation, Registers.getDataRegister(), instructionArrayList, tempRegisterNum);
		OperandTranslator.processSourceMemoryOperand(stackPointerLocation, Registers.getCounterRegister(), instructionArrayList, tempRegisterNum);
		OperandTranslator.processSourceMemoryOperand(stackPointerLocation, Registers.getAccumulatorRegister(), instructionArrayList, tempRegisterNum);
		
		//stack-pointer = stack-pointer - immediate
		instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(stackPointer, Operand.getImmediateOperand(), stackPointer));
	}
}