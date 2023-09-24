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
import generic.Operand;
import generic.Instruction;
import generic.InstructionList;

public class ExchangeAndAdd implements X86StaticInstructionHandler 
{
	public void handle(long instructionPointer, 
			Operand operand1, Operand operand2, Operand operand3,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum)
					throws InvalidInstructionException
	{
		if(operand1.isMemoryOperand() && operand2.isIntegerRegisterOperand() && operand3 == null)
		{
			Operand memoryOpnd = operand1;
			Operand registerOpnd = operand2;
			
			// memValueTemp = [memoryOpnd]
			Operand memValueTemp = OperandTranslator.processSourceMemoryOperand(memoryOpnd, instructionArrayList, tempRegisterNum, true);
			
			// sumTemp = registerOpnd + memValueTemp
			Operand sumTemp = Registers.getTempIntReg(tempRegisterNum);
			instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(registerOpnd, memValueTemp, sumTemp));
			
			// [memoryOpnd] = sumTemp
			OperandTranslator.processDestinationMemoryOperand(sumTemp, memoryOpnd, instructionArrayList, tempRegisterNum);
			
			// registerOpnd = memValueTemp
			instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(memValueTemp, null, registerOpnd));			
		}
		else if(operand1.isIntegerRegisterOperand() && operand2.isIntegerRegisterOperand() && operand3 == null) /*both operands are registers*/
		{
			// sumTemp = sourceReg + destReg
			Operand sumTemp = Registers.getTempIntReg(tempRegisterNum);
			instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(operand1, operand2, sumTemp));
			
			// sourceReg = destReg
			instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(operand1, null, operand2));
			
			// destReg = sumTemp
			instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(sumTemp, null, operand1));
		}
		else
		{
			misc.Error.invalidOperation("Exchange and Add ", operand1, operand2, operand3);
		}
	}
}
