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

package emulatorinterface.translator.x86.operand;

import emulatorinterface.translator.InvalidInstructionException;
import emulatorinterface.translator.x86.registers.Registers;
import emulatorinterface.translator.x86.registers.TempRegisterNum;
import generic.Instruction;
import generic.InstructionList;
import generic.Operand;
import generic.OperandType;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import main.CustomObjectPool;
import misc.Numbers;

public class OperandTranslator 
{
	private static Matcher keywordMatcher, memLocationMatcher, memAddressRefMatcher, indexScaleMatcher, wordPtrMatcher;
	
	private static void createMatchers() {
		Pattern p;
		
		p = Pattern.compile("byte|dword|qword|word");
		keywordMatcher = p.matcher("");
		
		p = Pattern.compile(".*\\[.*\\].*");
		memLocationMatcher = p.matcher("");
		
		p = Pattern.compile("[0-9a-f]+ <.*>");
		memAddressRefMatcher = p.matcher("");
		
		p = Pattern.compile("[a-zA-Z ]+:0x[0-9a-f]+");
		wordPtrMatcher = p.matcher("");
		
		p = Pattern.compile("[a-zA-Z0-9]+\\*[0x123456789abcdef]+");
		indexScaleMatcher = p.matcher("");
		
		
	}
	
	public static Operand simplifyOperand(String operandStr,
			InstructionList instructionList, TempRegisterNum tempRegisterNum, String instructionPrefix)
					throws InvalidInstructionException
	{
		if(keywordMatcher==null) {
			createMatchers();
		}
		
		//If there is no operand, then just don't process it. 
		if(operandStr == null) {
			return null;
		}
		
		// Some libraries like udis86 add keyword like byte, dword, qword to indicate the granularity of 
		// the operand. This keyword must be removed before processing as we are not using them.
		operandStr = keywordMatcher.reset(operandStr).replaceAll("");
		
		// Remove spaces from both ends. Helps in making patterns for coming code.
		operandStr = operandStr.trim();
		
		// Replace all the occurrences of registers with the 64-bit register versions
		operandStr = Registers.coarsifyRegisters(operandStr);
		
		//If operand is a valid number, then it is an immediate
		if(Numbers.isValidNumber(operandStr)) 
		{
			//FIXME : We do not care about the actual value of the immediate operand 
			return Operand.getImmediateOperand();
		}
		else if(Registers.isIntegerRegister(operandStr))
		{
			return Operand.getIntegerRegister(Registers.encodeRegister(operandStr));
		}

		else if(Registers.isFloatRegister(operandStr))
		{
			return Operand.getFloatRegister(Registers.encodeRegister(operandStr));
		}
		//Simplify memory locations specified by [...]
		else if(memLocationMatcher.reset(operandStr).matches())
		{
			//contains a memory location specified by the memory address
			//Strip the string enclosed in square brackets
			String memLocation = operandStr = operandStr.substring(operandStr.indexOf("[") + 1, operandStr.indexOf("]"));
			
			//Mark the operand as an operand whose value is stored in the memory
			return simplifyMemoryLocation(memLocation, instructionList, tempRegisterNum, instructionPrefix);
		}
		
		else if(memAddressRefMatcher.reset(operandStr).matches())
		{
			//Above pattern is numbers <random>
			//This operand contains a memory address and a reference address enclosed in <>
			//We just need the first field containing address. This is an immediate
			String memLocation = new StringTokenizer(operandStr).nextToken();
			return Operand.getImmediateOperand();
		}
		
		else if(wordPtrMatcher.reset(operandStr).matches())
		{	
			//This operand contains :. So it must be like DWORD PTR segment-register:memory Address
			StringTokenizer memLocTokenizer = new StringTokenizer(operandStr, ":", false);
			memLocTokenizer.nextToken(); //Skip the segmentDescriptor
			String memLocation = memLocTokenizer.nextToken();

			// FIXME something seems to be wrong
			// If the operand contains the keyword PTR, mark it as stored in memory
			if(operandStr.contains("PTR"))
			{
				return simplifyMemoryLocation(memLocation, instructionList, tempRegisterNum, instructionPrefix);
			}
			else
			{
				//TODO must check if the immediate address is available from PIN tool
				return Operand.getMemoryOperand(Operand.getImmediateOperand(), null);
			}
		}
		
		else
		{
			misc.Error.invalidOperand(operandStr);
			return null;
		}
	}
	

	static Operand simplifyMemoryLocation(String operandStr,
			InstructionList instructionArrayList, TempRegisterNum tempRegisterNum, String instructionPrefix) 
					throws InvalidInstructionException
	{
		String memoryAddressTokens[] = operandStr.split("\\+|-");
		
		if(!areValidMemoryAddressTokens(memoryAddressTokens))
		{
			misc.Error.showErrorAndExit("\n\tIllegal arguments to a memory address : " 
					+ operandStr + " !!");
		}		
		
		Operand base, offset, index,scale;
		String indexStr = null, scaleStr = null;
		base=offset=index=scale=null;
				
		
		//Determine all the parameters of the string 
		for(int i=0; i<memoryAddressTokens.length; i++)
		{
			//base register
			if(Registers.isIntegerRegister(memoryAddressTokens[i]))
			{
				if(base==null) {
					base = Operand.getIntegerRegister(Registers.encodeRegister(memoryAddressTokens[i]));
				} else {
					index = Operand.getIntegerRegister(Registers.encodeRegister(memoryAddressTokens[i]));
				}
			}
			
			//offset
			else if(Numbers.isValidNumber(memoryAddressTokens[i]))
			{
				//if offset is zero, then this won't be considered as an offset in actual address
				if(Numbers.hexToLong(memoryAddressTokens[i])==0) {
					continue;
				}
				
				offset = Operand.getImmediateOperand();
			}
			
			//index*scale
			else if(indexScaleMatcher.reset(memoryAddressTokens[i]).matches())
			{
				indexStr = memoryAddressTokens[i].split("\\*")[0];
				scaleStr = memoryAddressTokens[i].split("\\*")[1];
				
				//if index is eiz then it means that this is a dummy scaled operand
				if(indexStr.contentEquals("eiz")) {
					continue;
				} else if(Registers.isIntegerRegister(indexStr)) {
					index = Operand.getIntegerRegister(Registers.encodeRegister(indexStr));
				} else {
					throw new InvalidInstructionException("illegal operand : operandStr = " + operandStr, true);
				}
				
				if(Numbers.hexToLong(scaleStr)==0) {
					index = null;
				} else if(Numbers.hexToLong(scaleStr)!=1) {
					scale = Operand.getImmediateOperand();
				}
			}
			
			else
			{
				misc.Error.invalidOperand(operandStr);
				return null;
			}
		}

		//Create scaled index
		Operand scaledIndex = null;
		if(scale!=null) {
			scaledIndex = Registers.getTempIntReg(tempRegisterNum);
			instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(index, scale, scaledIndex));
		} else {
			scaledIndex = index;
		}
		
		//providing for the rep, repe, repne, repz, repze prefixes
		//TODO this is an approximation
		if(instructionPrefix != null && instructionPrefix.matches("rep(|e|z|ne|nz)"))
		{
//TODO though this (commented) approach is more accurate, for some instructions
//it ends up using so many temporary registers that it overflows the number
//of architectural registers. Example instruction: repz cmpsb %es:(%rdi),%ds:(%rsi)
//			Operand tempRegister = Registers.getTempIntReg(tempRegisterNum);
//			if(offset == null)
//			{
//				instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(Operand.getImmediateOperand(), Registers.getCounterRegister(), tempRegister));
//			}
//			else
//			{
//				instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(offset, Registers.getCounterRegister(), tempRegister));
//			}
//			offset = tempRegister;
//so having the following simpler alternative
			offset = Registers.getCounterRegister();
		}
						
		//TODO : Once xml file is ready, we have to read this boolean from the configuration parameters
		//Default value is true.
		//current pureRisc code to be verified
		boolean pureRisc;
		pureRisc=false;
		
		//determine the type of addressing used
		Operand memoryLocationFirstOperand = null;
		Operand memoryLocationSecondOperand = null;
		
		if(base==null && scaledIndex==null && offset==null)
		{}
		
		else if(base==null && scaledIndex==null && offset!=null)
		{
			memoryLocationFirstOperand = offset;
		}
		
		else if(base==null && scaledIndex!=null && offset==null)
		{
			memoryLocationFirstOperand = scaledIndex;
		}
		
		else if(base==null && scaledIndex!=null && offset!=null)
		{
			memoryLocationFirstOperand = scaledIndex;
			memoryLocationSecondOperand = offset;
		}
		
		else if(base!=null && scaledIndex==null && offset==null)
		{
			memoryLocationFirstOperand = base;
		}
		
		else if(base!=null && scaledIndex==null && offset!=null)
		{
			memoryLocationFirstOperand = base;
			memoryLocationSecondOperand = offset;
		}
		
		else if(base!=null && scaledIndex!=null && offset==null)
		{
			memoryLocationFirstOperand = base;
			memoryLocationSecondOperand = scaledIndex;
		}
		
		else if(base!=null && scaledIndex!=null && offset!=null)
		{
			if(pureRisc)
			{
				Operand tempRegister = Registers.getTempIntReg(tempRegisterNum);
				instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(scaledIndex, offset, tempRegister));
				memoryLocationFirstOperand = base;
				memoryLocationSecondOperand = tempRegister;
			}
			
			else
			{
				memoryLocationFirstOperand = base;
				memoryLocationSecondOperand = scaledIndex;
			}
		}
		
		else
		{}
		
		//pure risc -> pass a single operand
		if(pureRisc && memoryLocationSecondOperand!=null)
		{
			Operand tempRegister = Registers.getTempIntReg(tempRegisterNum);
			instructionArrayList.appendInstruction(Instruction.getIntALUInstruction(memoryLocationFirstOperand, memoryLocationFirstOperand, tempRegister));//TODO
			memoryLocationFirstOperand = tempRegister;
			memoryLocationSecondOperand = null;
		}
		
		return Operand.getMemoryOperand(memoryLocationFirstOperand, memoryLocationSecondOperand);
	}	


	private static boolean areValidMemoryAddressTokens(String memoryAddressTokens[])
	{
		return ((memoryAddressTokens.length>=1 && memoryAddressTokens.length<=3));
	}
	
	public static Operand getLocationToStoreValue(Operand operand, TempRegisterNum tempRegisterNum)
	{
		if(!operand.isMemoryOperand())
		{
			misc.Error.showErrorAndExit("\n\tTrying to obtain value from a " +	"non-memory operand !!");
		}
		
		Operand tempMemoryRegister;
		
		tempMemoryRegister = Registers.getTemporaryMemoryRegister(operand);
		
		//If we don't have the luxury of an additional temporary register,
		//then we must allocate a new one
		if(tempMemoryRegister == null)
		{
			//If we don't have any disposable register available, then use a new register
			tempMemoryRegister = Registers.getTempIntReg(tempRegisterNum);
		}
		
		return tempMemoryRegister;
	}
	
	public static Operand processSourceMemoryOperand(Operand sourceOperand,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum,
			boolean loadingInteger)
	{
		Operand operandAddressOperand = OperandTranslator.getLocationToStoreValue(sourceOperand, tempRegisterNum);
		instructionArrayList.appendInstruction(Instruction.getLoadAGUInstruction(sourceOperand.getMemoryLocationFirstOperand(), sourceOperand.getMemoryLocationSecondOperand(), operandAddressOperand));
		Operand operandValueOperand;
		if(loadingInteger)
			operandValueOperand = Registers.getTempIntReg(tempRegisterNum);
		else
			operandValueOperand = Registers.getTempFloatReg(tempRegisterNum);
		instructionArrayList.appendInstruction(Instruction.getLoadInstruction(operandAddressOperand, operandValueOperand));
		return operandValueOperand;
	}
	
	public static void processSourceMemoryOperand(Operand sourceOperand,
			Operand destinationOperand,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum)
	{
		Operand operandAddressOperand = OperandTranslator.getLocationToStoreValue(sourceOperand, tempRegisterNum);
		instructionArrayList.appendInstruction(Instruction.getLoadAGUInstruction(sourceOperand.getMemoryLocationFirstOperand(), sourceOperand.getMemoryLocationSecondOperand(), operandAddressOperand));
		instructionArrayList.appendInstruction(Instruction.getLoadInstruction(operandAddressOperand, destinationOperand));
	}
	
	public static void processDestinationMemoryOperand(Operand sourceOperand,
			Operand destinationOperand,
			InstructionList instructionArrayList,
			TempRegisterNum tempRegisterNum)
	{
		Operand operandAddressOperand = OperandTranslator.getLocationToStoreValue(destinationOperand, tempRegisterNum);
		instructionArrayList.appendInstruction(Instruction.getStoreAGUInstruction(destinationOperand.getMemoryLocationFirstOperand(), destinationOperand.getMemoryLocationSecondOperand(), operandAddressOperand));
		instructionArrayList.appendInstruction(Instruction.getStoreInstruction(operandAddressOperand, sourceOperand));
	}
}