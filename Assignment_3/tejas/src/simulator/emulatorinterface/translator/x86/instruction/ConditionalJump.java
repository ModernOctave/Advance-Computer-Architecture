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
import generic.InstructionList;
import generic.Operand;

public class ConditionalJump implements X86StaticInstructionHandler 
{
	public void handle(long instructionPointer, Operand operand1,
			Operand operand2, Operand operand3, InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum)
					throws InvalidInstructionException
	{
		Operand jumpLocation = null;
		
		if((operand1.isImmediateOperand() || operand1.isIntegerRegisterOperand()) 
		   &&  operand2==null  &&  operand3==null)
		{
			jumpLocation = operand1;
			instructionArrayList.appendInstruction(Instruction.getBranchInstruction(jumpLocation, Registers.getEFlagsRegister()));
		}
		
		else if(operand1.isMemoryOperand() && operand2==null && operand3==null)
		{
			jumpLocation = OperandTranslator.processSourceMemoryOperand(operand1, instructionArrayList, tempRegisterNum, true);
			instructionArrayList.appendInstruction(Instruction.getBranchInstruction(jumpLocation, Registers.getEFlagsRegister()));
		}
		
		else
		{
			misc.Error.invalidOperation("Conditional Jump", operand1, operand2, operand3);
		}
	}
}