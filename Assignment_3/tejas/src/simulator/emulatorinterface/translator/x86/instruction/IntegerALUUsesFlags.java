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


public class IntegerALUUsesFlags implements X86StaticInstructionHandler 
{
	public void handle(long instructionPointer, 
			Operand operand1, Operand operand2, Operand operand3,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum) 
					throws InvalidInstructionException
	{
		if(
		(operand1.isIntegerRegisterOperand() || operand1.isMemoryOperand()) &&
		(operand2 == null || operand2.isImmediateOperand() || operand2.isIntegerRegisterOperand() || operand2.isMemoryOperand()) &&
		(operand3 == null || operand3.isImmediateOperand() || operand2.isIntegerRegisterOperand() || operand2.isMemoryOperand()))
		{
			Operand operand1ValueOperand;
			Operand operand2ValueOperand;
			Operand operand3ValueOperand;
			
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
			if(operand2 != null)
			{
				if(operand2.isMemoryOperand())
				{
					operand2ValueOperand = OperandTranslator.processSourceMemoryOperand(operand2, instructionArrayList, tempRegisterNum, true);
				}
				else
				{
					operand2ValueOperand = operand2;
				}				
			}
			else
			{
				operand2ValueOperand = null;
			}			
			
			//get value-operand for operand2
			if(operand3 != null)
			{
				if(operand3.isMemoryOperand())
				{
					operand3ValueOperand = OperandTranslator.processSourceMemoryOperand(operand3, instructionArrayList, tempRegisterNum, true);
				}
				else
				{
					operand3ValueOperand = operand3;
				}				
			}
			else
			{
				operand3ValueOperand = null;
			}

			//Perform integer alu operation
			if(operand3ValueOperand == null)
			{
				instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(operand2ValueOperand, 
						operand1ValueOperand, operand1ValueOperand));
				/* TODO using integer ALU to use flags is not entirely right */
				instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(Registers.getEFlagsRegister(), 
						operand1ValueOperand, operand1ValueOperand));
			}
			else
			{
				instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(operand2ValueOperand, 
						operand3ValueOperand, operand1ValueOperand));
				/* TODO using integer ALU to use flags is not entirely right */
				instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(Registers.getEFlagsRegister(), 
						operand1ValueOperand, operand1ValueOperand));
			}

			
			//If operand1 is a memory operand, then perform a store operation too
			if(operand1.isMemoryOperand())
			{
				OperandTranslator.processDestinationMemoryOperand(operand1ValueOperand, operand1, instructionArrayList, tempRegisterNum);
			}
		}
		
		else
		{
			misc.Error.invalidOperation("Integer operation ", operand1, operand2, operand3);
		}
	}
}