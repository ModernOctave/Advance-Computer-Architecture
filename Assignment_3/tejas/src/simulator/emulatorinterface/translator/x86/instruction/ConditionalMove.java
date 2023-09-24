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

public class ConditionalMove implements X86StaticInstructionHandler 
{
	//TODO Must figure out a way to introduce condition in a 
	//conditional move. Currently, we are doing a simple move operation.  
	public void handle(long instructionPointer, 
			Operand operand1, Operand operand2, Operand operand3,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum)
			throws InvalidInstructionException
	{
		//if operand1 = register and operand2 = register/immediate - move
		if( (operand1.isIntegerRegisterOperand()) &&
			(operand2.isIntegerRegisterOperand() || operand2.isImmediateOperand()) &&
		    (operand3==null))
		{
			Operand temp = Registers.getTempIntReg(tempRegisterNum);
			Instruction newInstruction1 = Instruction.getIntALUInstruction(Registers.getEFlagsRegister(), operand1, temp);
			newInstruction1.setPredicate(true);
			instructionArrayList.appendInstruction(newInstruction1);
			Instruction newInstruction2 = Instruction.getIntALUInstruction(operand2, temp, operand1);
			newInstruction2.setPredicate(true);
			instructionArrayList.appendInstruction(newInstruction2);
		}
		
		//if operand1 = register and operand2 = memory - load
		else if((operand1.isIntegerRegisterOperand()) &&
				 operand2.isMemoryOperand() && 
				 operand3==null)
		{
			Operand temp = Registers.getTempIntReg(tempRegisterNum);
			Instruction newInstruction1 = Instruction.getIntALUInstruction(Registers.getEFlagsRegister(), operand1, temp);
			newInstruction1.setPredicate(true);
			instructionArrayList.appendInstruction(newInstruction1);
			Operand sourceOperand = OperandTranslator.processSourceMemoryOperand(operand2, instructionArrayList, tempRegisterNum, true);
			Instruction newInstruction2 = Instruction.getIntALUInstruction(sourceOperand, temp, operand1);
			newInstruction2.setPredicate(true);
			instructionArrayList.appendInstruction(newInstruction2);
		}
		
//		//if operand1 = memory and operand2 = memory - store
//		else if((operand1.isMemoryOperand()) &&
//				(operand2.isImmediateOperand() || operand2.isIntegerRegisterOperand()) &&
//				(operand3==null))
//		{
//			instructionArrayList.appendInstruction(Instruction.getStoreInstruction(operand2, operand1));
//		}
//		
//		//TODO I doubt if this is a valid instruction !! Anyways found this in an object-code
//		//operand1 can be a data-stored in memory and operand2 can be immediate/register.
//		//first, we load the value at location into temporary register
//		//Then we will store op2 to [temporary-register]
//		else if(operand1.isMemoryOperand() && operand2.isMemoryOperand() && operand3==null)
//		{
//			misc.Error.invalidOperation("Conditional Move", operand1, operand2, operand3);
//		}
		
		else
		{
			misc.Error.invalidOperation("Conditional Move", operand1, operand2, operand3);
		}
	}
}