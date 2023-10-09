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

public class Push implements X86StaticInstructionHandler 
{
	public void handle(long instructionPointer, 
			Operand operand1, Operand operand2, Operand operand3,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum)
					throws InvalidInstructionException
	{
		//get stack-pointer and [stack-pointer]
		Operand stackPointer = Registers.getStackPointer();
		Operand stackPointerLocation = Operand.getMemoryOperand(stackPointer, null);
		
		
		if((operand1.isMemoryOperand() || operand1.isImmediateOperand() ||	operand1.isIntegerRegisterOperand()) && 
		   operand2==null &&  operand3==null)
		{
			Operand valueToPush;

			if(operand1.isMemoryOperand())
			{
				valueToPush = OperandTranslator.processSourceMemoryOperand(stackPointerLocation, instructionArrayList, tempRegisterNum, true);
			}
			else
			{
				valueToPush = operand1;
			}
			
			//Increment the stack-pointer
			instructionArrayList.appendInstruction(Instruction.getIntALUInstruction( stackPointer, Operand.getImmediateOperand(), stackPointer));
			
			//perform a store to the top of the stack
			OperandTranslator.processDestinationMemoryOperand(valueToPush, stackPointerLocation, instructionArrayList, tempRegisterNum);
		}
		
		else
		{
			misc.Error.invalidOperation("Push", operand1, operand2, operand3);
		}
	}
}