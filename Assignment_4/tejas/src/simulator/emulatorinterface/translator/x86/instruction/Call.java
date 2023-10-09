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
import emulatorinterface.translator.x86.registers.Registers;
import emulatorinterface.translator.x86.registers.TempRegisterNum;
import generic.InstructionList;
import generic.Operand;
import misc.Error;;

/**
 * Call
 * 1) pushes the program-counter on the stack.
 * 2) Perform an unconditional jump to location stored in operand1.
 * @author prathmesh
 */
public class Call implements X86StaticInstructionHandler 
{
	public void handle(long instructionPointer, Operand operand1,
			Operand operand2, Operand operand3, InstructionList instructionArrayList, 
			TempRegisterNum tempRegisterNum)
					throws InvalidInstructionException	
	{
		if((operand1.isImmediateOperand() || operand1.isIntegerRegisterOperand() || operand1.isMemoryOperand()) 
		   &&  operand2==null  &&   operand3==null)
		{
			//push the next instruction pointer on to the stack
			//TODO Check if the NEXT_INSTRUCTION can be computed
			Operand nextInstruction = Registers.getInstructionPointer();
			new Push().handle(instructionPointer, nextInstruction, null, null, instructionArrayList, tempRegisterNum);
			
			//Unconditional jump to a new location
			(new UnconditionalJump()).handle(instructionPointer, operand1, null, null, instructionArrayList, tempRegisterNum);
		}
		
		else
		{
			Error.invalidOperation("Call", operand1, operand2, operand3);
		}
	}
}