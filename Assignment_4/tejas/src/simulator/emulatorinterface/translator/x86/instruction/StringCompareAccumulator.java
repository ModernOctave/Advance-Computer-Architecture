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

public class StringCompareAccumulator implements X86StaticInstructionHandler 
{
	public void handle(long instructionPointer, 
			Operand operand1, Operand operand2, Operand operand3,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum) 
					throws InvalidInstructionException
	{
		Operand sourceLocation;
		Operand destinationLocation;
		
		
		if(operand1==null && operand2==null && operand3==null)
		{
			sourceLocation = Registers.getAccumulatorRegister();
			
			destinationLocation = OperandTranslator.processSourceMemoryOperand(Registers.getDestinationIndexRegister(), instructionArrayList, tempRegisterNum, true);
			
			instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(sourceLocation, destinationLocation, Registers.getEFlagsRegister()));
			
			instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(Registers.getDestinationIndexRegister(), Operand.getImmediateOperand(), Registers.getDestinationIndexRegister()));
		}
		
		
//		else if(operand1.isMemoryOperand() && operand2.isMemoryOperand() &&
//				operand3==null)
//		{
//			sourceLocation = operand2;
//			destinationLocation = operand1;
//			
//			//Load the value at the sourceLocation in a temporary register
//			Operand sourceIndex = Registers.getTempIntReg(tempRegisterNum);
//			instructionArrayList.appendInstruction(Instruction.getLoadInstruction(sourceLocation, sourceIndex));
//			
//			//Load the value at the destination Location in a temporary register
//			Operand destinationIndex = Registers.getTempIntReg(tempRegisterNum);
//			instructionArrayList.appendInstruction(Instruction.getLoadInstruction(destinationLocation, destinationIndex));
//
//			//Perform compare operation
////			IntegerALUExplicitDestination integerALUExplicitDestination = 
////						new IntegerALUExplicitDestination();
////			
////			integerALUExplicitDestination.handle(instructionPointer, sourceIndex, 
////					destinationIndex, null, instructionArrayList, tempRegisterNum);
//			instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(sourceIndex, destinationIndex, destinationIndex));
//		}
		
		else
		{
			misc.Error.invalidOperation("String Compare Accumulator ", operand1, operand2, operand3);
		}
	}
}