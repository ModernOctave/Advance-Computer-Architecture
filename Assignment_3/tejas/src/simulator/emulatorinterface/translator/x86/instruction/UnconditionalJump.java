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
import emulatorinterface.translator.x86.registers.TempRegisterNum;
import generic.Operand;
import generic.Instruction;
import generic.InstructionList;

public class UnconditionalJump implements X86StaticInstructionHandler 
{
	public void handle(long instructionPointer, 
			Operand operand1, Operand operand2, Operand operand3,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum) 
					throws InvalidInstructionException
	{
		if((operand1.isImmediateOperand() || operand1.isIntegerRegisterOperand() || operand1.isMemoryOperand()) 
		   &&  operand2==null && operand3==null)
		{
			Operand jumpLocation;
			
			if(operand1.isMemoryOperand())
			{
				//far jump : jumpLocation = [operand1]
				jumpLocation = OperandTranslator.processSourceMemoryOperand(operand1, instructionArrayList, tempRegisterNum, true);
			}
			else
			{
				//near jump : jumpLocation = instruction-pointer + operand1
				jumpLocation = operand1;
				//instructionLinkedList.appendInstruction(Instruction.getIntALUInstruction(Registers.getInstructionPointer(), operand1, jumpLocation));
			}

			//jump to this location
			Instruction jumpInstruction = Instruction.getUnconditionalJumpInstruction(jumpLocation);
			jumpInstruction.setBranchTaken(true);
			instructionArrayList.appendInstruction(jumpInstruction);
		}
		else
		{
			misc.Error.invalidOperation("Unconditional Jump", operand1, operand2, operand3);
		}
	}
}