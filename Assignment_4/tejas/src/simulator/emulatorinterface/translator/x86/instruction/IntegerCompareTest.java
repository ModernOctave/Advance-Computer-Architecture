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


public class IntegerCompareTest implements X86StaticInstructionHandler 
{
	public void handle(long instructionPointer, 
			Operand operand1, Operand operand2, Operand operand3,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum) 
					throws InvalidInstructionException
	{
		if(
		(operand1.isImmediateOperand() || operand1.isIntegerRegisterOperand() || operand1.isMemoryOperand()) &&
		(operand2.isImmediateOperand() || operand2.isIntegerRegisterOperand() || operand2.isMemoryOperand()) &&
		(operand3==null))
		{
			Operand operand1ValueOperand;
			Operand operand2ValueOperand;
			
			//get value-operand for operand1
			if(operand1.isMemoryOperand())
			{
				operand1ValueOperand = OperandTranslator.processSourceMemoryOperand(operand1, instructionArrayList, tempRegisterNum, true);
			}
			else
			{
				operand1ValueOperand = operand1;
			}
						
			//get value-operand for operand2
			if(operand2.isMemoryOperand())
			{
				operand2ValueOperand = OperandTranslator.processSourceMemoryOperand(operand2, instructionArrayList, tempRegisterNum, true);
			}
			else
			{
				operand2ValueOperand = operand2;
			}

			//Perform integer alu operation
			instructionArrayList.appendInstruction(Instruction.getIntALUInstruction( operand2ValueOperand, operand1ValueOperand, Registers.getEFlagsRegister()));
		}
		else
		{
			misc.Error.invalidOperation("Integer compare or test ", operand1, operand2, operand3);
		}
	}
}