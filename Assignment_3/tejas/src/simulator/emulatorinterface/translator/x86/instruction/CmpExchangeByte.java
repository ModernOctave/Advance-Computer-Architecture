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


public class CmpExchangeByte implements X86StaticInstructionHandler 
{
	public void handle(long instructionPointer, 
			Operand operand1, Operand operand2, Operand operand3,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum) 
					throws InvalidInstructionException
	{
		if(
		(operand1.isMemoryOperand()) &&
		(operand2 == null) &&
		(operand3==null))
		{
			Operand operand1ValueOperand = OperandTranslator.processSourceMemoryOperand(operand1, instructionArrayList, tempRegisterNum, true);
			
			//compare
			Operand tempComparisonResult = Registers.getTempIntReg(tempRegisterNum);
			instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(Registers.getAccumulatorRegister(), operand1ValueOperand, tempComparisonResult));
			instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(Registers.getDestinationIndexRegister(), tempComparisonResult, Registers.getEFlagsRegister()));
			
			/*
			 * TODO
			 * currently implementing both the success and failure outcomes of the comparison
			 * in reality, only one of the outcomes can happen dynamically
			 */
			
			//if equal
			OperandTranslator.processDestinationMemoryOperand(Registers.getCounterRegister(), operand1, instructionArrayList, tempRegisterNum);
			OperandTranslator.processDestinationMemoryOperand(Registers.getBaseIndexRegister(), operand1, instructionArrayList, tempRegisterNum);
			
			//if not equal
			OperandTranslator.processSourceMemoryOperand(operand1, Registers.getAccumulatorRegister(), instructionArrayList, tempRegisterNum);
			OperandTranslator.processSourceMemoryOperand(operand1, Registers.getDestinationIndexRegister(), instructionArrayList, tempRegisterNum);			
		}
		
		else
		{
			misc.Error.invalidOperation("Integer operation ", operand1, operand2, operand3);
		}
	}
}