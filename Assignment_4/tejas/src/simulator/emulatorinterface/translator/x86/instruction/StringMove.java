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

public class StringMove implements X86StaticInstructionHandler 
{
	public void handle(long instructionPointer, 
			Operand operand1, Operand operand2, Operand operand3,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum) 
					throws InvalidInstructionException
	{
		Operand sourceLocation;
		
		if(operand1==null && operand2==null && operand3==null)
		{
			sourceLocation = OperandTranslator.processSourceMemoryOperand(Operand.getMemoryOperand(Registers.getSourceIndexRegister(), null), instructionArrayList, tempRegisterNum, true);
			
			OperandTranslator.processDestinationMemoryOperand(sourceLocation, Operand.getMemoryOperand(Registers.getDestinationIndexRegister(), null), instructionArrayList, tempRegisterNum);
			
			instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(Registers.getSourceIndexRegister(), Operand.getImmediateOperand(), Registers.getSourceIndexRegister()));
			instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(Registers.getDestinationIndexRegister(), Operand.getImmediateOperand(), Registers.getDestinationIndexRegister()));
		}
		
		else if(operand1.isMemoryOperand() && operand2.isMemoryOperand() &&
				operand3==null)
		{
			sourceLocation = OperandTranslator.processSourceMemoryOperand(operand2, instructionArrayList, tempRegisterNum, true);
			
			OperandTranslator.processDestinationMemoryOperand(sourceLocation, operand1, instructionArrayList, tempRegisterNum);
		}
		
		else
		{
			misc.Error.invalidOperation("String Move", operand1, operand2, operand3);
		}
	}
}