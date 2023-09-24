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

*****************************************************************************/

package emulatorinterface.translator.x86.instruction;

import emulatorinterface.translator.InvalidInstructionException;
import emulatorinterface.translator.x86.operand.OperandTranslator;
import emulatorinterface.translator.x86.registers.TempRegisterNum;
import generic.Instruction;
import generic.Operand;
import generic.InstructionList;

public class BitScan implements X86StaticInstructionHandler{
	public void handle(long instructionPointer, 
			Operand operand1, Operand operand2, Operand operand3,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum) 
				throws InvalidInstructionException
	{
		if(operand1 != null && operand1.isFloatRegisterOperand()
				&& operand2 != null && (operand2.isFloatRegisterOperand() || operand2.isMemoryOperand())
				&& operand3 == null)
		{
			Operand srcOpnd1, destOpnd;
				
			destOpnd = operand1;
			if(operand2.isFloatRegisterOperand())
			{
				srcOpnd1 = operand2;
			}
			else
			{
				srcOpnd1 = OperandTranslator.processSourceMemoryOperand(operand2, instructionArrayList, tempRegisterNum, false);
			}
			
			instructionArrayList.appendInstruction(Instruction.getBitScan(srcOpnd1, destOpnd));
		}
		else
		{
			misc.Error.invalidOperation("bit scan ", operand1, operand2, operand3);
		}
	}
}