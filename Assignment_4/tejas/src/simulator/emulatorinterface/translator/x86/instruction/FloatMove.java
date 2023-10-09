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
import generic.InstructionList;


public class FloatMove implements X86StaticInstructionHandler 
{
	public void handle(long instructionPointer, 
			Operand operand1, Operand operand2, Operand operand3,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum) 
					throws InvalidInstructionException
	{
		//if operand1 is a register and operand2 is a register/immediate, we will use normal move operation
		if( (operand1.isIntegerRegisterOperand() || operand1.isFloatRegisterOperand()) &&
			(operand2.isIntegerRegisterOperand() || operand2.isFloatRegisterOperand() || operand2.isImmediateOperand()) &&
		    (operand3==null))
		{
			instructionArrayList.appendInstruction(Instruction.getFloatingPointALU(operand2, null, operand1));
		}
		
		
		//if operand1 is register and operand2 is a memory-operand, we will use load operation
		else if((operand1.isIntegerRegisterOperand() || operand1.isFloatRegisterOperand()) &&
				 operand2.isMemoryOperand() && 
				 operand3==null)
		{
			//Obtain value at the memory location [operand2]
			OperandTranslator.processSourceMemoryOperand(operand2, operand1, instructionArrayList, tempRegisterNum);
		}
		
		//if the operand1 is a memory location and operand2 is a register/immediate then
		//it is a store operation
		else if((operand1.isMemoryOperand()) &&
				(operand2.isImmediateOperand() || operand2.isIntegerRegisterOperand() || operand2.isFloatRegisterOperand()) &&
				(operand3==null))
		{
			OperandTranslator.processDestinationMemoryOperand(operand2, operand1, instructionArrayList, tempRegisterNum);
		}
		
		else if(operand1.isFloatRegisterOperand() && operand2.isFloatRegisterOperand() && operand3.isFloatRegisterOperand())
		{
			instructionArrayList.appendInstruction(Instruction.getFloatingPointALU(operand2, operand3, operand1));
		}
		
		//TODO I doubt if this is a valid instruction !! Anyways found this in an object-code
		//operand1 can be a data-stored in memory and operand2 can be immediate/register.
		//first, we load the value at location into temporary register
		//Then we will store op2 to [temporary-register]
		else if(operand1.isMemoryOperand() &&
			    operand2.isMemoryOperand() && 
			    operand3==null)
		{
			misc.Error.invalidOperation("Float Move", operand1, operand2, operand3);
		}
		
		else
		{
			misc.Error.invalidOperation("Float Move", operand1, operand2, operand3);
		}
	}
}

