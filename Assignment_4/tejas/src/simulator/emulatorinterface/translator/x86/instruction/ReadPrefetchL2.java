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
import generic.Instruction;
import generic.Operand;
import generic.OperandType;
import generic.InstructionList;


public class ReadPrefetchL2 implements X86StaticInstructionHandler 
{
	public void handle(long instructionPointer, 
			Operand operand1, Operand operand2, Operand operand3,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum) 
					throws InvalidInstructionException
	{
		//if operand1 is a register and operand2 is a register/immediate, we will use normal move operation
		if(operand1.isMemoryOperand() && operand2 == null && operand3==null)
		{
			Operand operandAddressOperand = OperandTranslator.getLocationToStoreValue(operand1, tempRegisterNum);
			instructionArrayList.appendInstruction(Instruction.getLoadAGUInstruction(operand1.getMemoryLocationFirstOperand(), operand1.getMemoryLocationSecondOperand(), operandAddressOperand));
			Operand cacheLevelToPrefetchTo = new Operand(OperandType.immediate, 2);
			instructionArrayList.appendInstruction(Instruction.getReadPrefetch(operandAddressOperand, cacheLevelToPrefetchTo));
		}
		
		else
		{
			misc.Error.invalidOperation("Read Prefetch L2", operand1, operand2, operand3);
		}
	}
}
