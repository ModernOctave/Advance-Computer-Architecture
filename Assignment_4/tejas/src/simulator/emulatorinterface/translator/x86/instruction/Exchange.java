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
import generic.InstructionList;
import generic.Instruction;
import generic.Operand;

public class Exchange implements X86StaticInstructionHandler 
{
	public void handle(long instructionPointer, 
			Operand operand1, Operand operand2, Operand operand3,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum)
					throws InvalidInstructionException
	{
		if((operand1.isIntegerRegisterOperand() || operand1.isMemoryOperand())
				&& (operand2 == null || operand2.isIntegerRegisterOperand())
				&& operand3 == null
				&& (!(operand1.isMemoryOperand() && operand2 == null)))
		{
			Operand operand1Value;
			if(operand1.isMemoryOperand())
			{
				operand1Value = OperandTranslator.processSourceMemoryOperand(operand1, instructionArrayList, tempRegisterNum, true);
			}
			else
			{
				operand1Value = operand1;
			}
			
			Operand operand2Reg;
			if(operand2.isIntegerRegisterOperand())
			{
				operand2Reg = operand2;
			}
			else
			{
				operand2Reg = Registers.getAccumulatorRegister();
			}
			
			Operand temp = Registers.getTempIntReg(tempRegisterNum);
			
			//temp = operand2
			instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(operand2Reg, null, temp));
			//operand2 = operand1
			instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(operand1Value, null, operand2Reg));
			//operand1 = temp
			if(operand1.isMemoryOperand())
			{
				OperandTranslator.processDestinationMemoryOperand(temp, operand1, instructionArrayList, tempRegisterNum);
			}
			else
			{
				instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(temp, null, operand1));
			}
		}

		else
		{
			misc.Error.invalidOperation("Exchange", operand1, operand2, operand3);
		}
	}
}