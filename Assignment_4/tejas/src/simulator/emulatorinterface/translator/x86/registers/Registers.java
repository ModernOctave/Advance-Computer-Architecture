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

package emulatorinterface.translator.x86.registers;


import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import generic.Operand;


public class Registers 
{
	private static Hashtable<String, Long> integerRegistersHashTable = null;
	private static Hashtable<String, Long> floatRegistersHashTable = null;

	// --- Number of temporary registers must be maintained by the translator ---
//	public static int noOfIntTempRegs = 0;
//	public static int noOfFloatTempRegs = 0;

	//Allocate a new temporary register	
	public static Operand getTempIntReg(TempRegisterNum tempRegister)
	{
		return Operand.getIntegerRegister(encodeRegister("temp" + tempRegister.numTempIntRegister++));
	}

	//Allocate a new temporary float register	
	public static Operand getTempFloatReg(TempRegisterNum tempRegister)
	{
		return Operand.getFloatRegister(encodeRegister("tempFloat" + tempRegister.numTempFloatRegister++));
	}
	
	public static void createRegisterHashTable()
	{
		//Create required hash-tables
		integerRegistersHashTable = new Hashtable<String, Long>();
		floatRegistersHashTable = new Hashtable<String, Long>();
		
		
		//--------------------------Machine specific registers---------------------------------
		//Segment Registers
		
		// Load register and store register
//		machineSpecificRegistersHashTable.put("load_reg", new Long(8));
//		machineSpecificRegistersHashTable.put("store_reg", new Long(9));
		
		//-------------------------Integer register-----------------------------------------------
		//Registers available to the programmer
		integerRegistersHashTable.put("rax", new Long(0));
		integerRegistersHashTable.put("rbx", new Long(1));
		integerRegistersHashTable.put("rcx", new Long(2));
		integerRegistersHashTable.put("rdx", new Long(3));
		integerRegistersHashTable.put("r8", new Long(4));
		integerRegistersHashTable.put("r9", new Long(5));
		integerRegistersHashTable.put("r10", new Long(6));
		integerRegistersHashTable.put("r11", new Long(7));
		integerRegistersHashTable.put("r12", new Long(8));
		integerRegistersHashTable.put("r13", new Long(9));
		integerRegistersHashTable.put("r14", new Long(10));
		integerRegistersHashTable.put("r15", new Long(11));
		
		//Index registers
		integerRegistersHashTable.put("rsi", new Long(12));
		integerRegistersHashTable.put("rdi", new Long(13));
		integerRegistersHashTable.put("rbp", new Long(14));
		integerRegistersHashTable.put("rsp", new Long(15));
		
		//Weird Register
		integerRegistersHashTable.put("riz", new Long(17));
		
		// Load register and store register
		integerRegistersHashTable.put("load_reg", new Long(18));
		integerRegistersHashTable.put("store_reg", new Long(19));

		// Machine Specific Registers
		integerRegistersHashTable.put("es", new Long(20));
		integerRegistersHashTable.put("cs", new Long(21));
		integerRegistersHashTable.put("ss", new Long(22));
		integerRegistersHashTable.put("ds", new Long(23));
		integerRegistersHashTable.put("fs", new Long(24));
		integerRegistersHashTable.put("gs", new Long(25));
		
		integerRegistersHashTable.put("eflags", new Long(26));
		integerRegistersHashTable.put("rip", new Long(27));
		
		//FIXME: Not sure if this goes here
		integerRegistersHashTable.put("FP_CWORD", new Long(28));
		
		//Temporary registers
		integerRegistersHashTable.put("temp0", new Long(29));
		integerRegistersHashTable.put("temp1", new Long(30));
		integerRegistersHashTable.put("temp2", new Long(31));
		integerRegistersHashTable.put("temp3", new Long(32));
		integerRegistersHashTable.put("temp4", new Long(33));
		integerRegistersHashTable.put("temp5", new Long(34));
		integerRegistersHashTable.put("temp6", new Long(35));
		integerRegistersHashTable.put("temp7", new Long(36));
		
		
		//-------------------------Floating-point register-----------------------------------------
		//Stack registers
		floatRegistersHashTable.put("st",    new Long(0));
		floatRegistersHashTable.put("st(0)", new Long(0));
		floatRegistersHashTable.put("st0", new Long(0));
		
		floatRegistersHashTable.put("st(1)", new Long(1));
		floatRegistersHashTable.put("st1", new Long(1));
		
		floatRegistersHashTable.put("st(2)", new Long(2));
		floatRegistersHashTable.put("st2", new Long(2));
		
		floatRegistersHashTable.put("st(3)", new Long(3));
		floatRegistersHashTable.put("st3", new Long(3));
		
		floatRegistersHashTable.put("st(4)", new Long(4));
		floatRegistersHashTable.put("st4", new Long(4));
		
		floatRegistersHashTable.put("st(5)", new Long(5));
		floatRegistersHashTable.put("st5", new Long(5));
		
		floatRegistersHashTable.put("st(6)", new Long(6));
		floatRegistersHashTable.put("st6", new Long(6));
		
		floatRegistersHashTable.put("st(7)", new Long(7));
		floatRegistersHashTable.put("st7", new Long(7));
		
		//FIXME : This register-set can be used to perform integer operations too.
		//So its exact type - integer or floating point is ambiguous
		floatRegistersHashTable.put("xmm0",  new Long(9));
		floatRegistersHashTable.put("xmm1",  new Long(10));
		floatRegistersHashTable.put("xmm2",  new Long(11));
		floatRegistersHashTable.put("xmm3",  new Long(12));
		floatRegistersHashTable.put("xmm4",  new Long(13));
		floatRegistersHashTable.put("xmm5",  new Long(14));
		floatRegistersHashTable.put("xmm6",  new Long(15));
		floatRegistersHashTable.put("xmm7",  new Long(16));
		floatRegistersHashTable.put("xmm8",  new Long(17));
		floatRegistersHashTable.put("xmm9",  new Long(18));
		floatRegistersHashTable.put("xmm10", new Long(19));
		floatRegistersHashTable.put("xmm11", new Long(20));
		floatRegistersHashTable.put("xmm12", new Long(21));
		floatRegistersHashTable.put("xmm13", new Long(22));
		floatRegistersHashTable.put("xmm14", new Long(23));
		floatRegistersHashTable.put("xmm15", new Long(24));		
		
		//temporary floating-point registers
		floatRegistersHashTable.put("tempFloat0", new Long(25));
		floatRegistersHashTable.put("tempFloat1", new Long(26));
		floatRegistersHashTable.put("tempFloat2", new Long(27));
		floatRegistersHashTable.put("tempFloat3", new Long(28));
		floatRegistersHashTable.put("MXCSR", new Long(29));
	}

	
	//assign an index to each coarse-register
	public static long encodeRegister(String regStr)
	{
		checkAndCreateRegisterHashTable();
		
		Long codeRegister = null;
		
		if((codeRegister = integerRegistersHashTable.get(regStr)) != null)
		{
			return codeRegister.longValue();
		}
		else if((codeRegister = floatRegistersHashTable.get(regStr)) != null)
		{
			return codeRegister.longValue();
		}
		else
		{
			misc.Error.showErrorAndExit("\n\tNot a valid register : " + regStr + " !!");
			return -1;
		}
	}
	
	public static boolean isFloatRegister(String regStr)
	{
		checkAndCreateRegisterHashTable();
		
		return (floatRegistersHashTable.get(regStr)!=null);
	}
	
	public static boolean isIntegerRegister(String regStr)
	{
		checkAndCreateRegisterHashTable();
		
		return (integerRegistersHashTable.get(regStr)!=null);
	}
			
	//public static void releaseTempRegister(Operand tempRegister)
	//{
	//	//Must be called only from the simplify location method only
	//	noOfIntTempRegs--;
	//}
	
	
	private static Matcher rax,rbx,rcx,rdx,rsi,rdi,rbp,rsp,r8,r9,r10,r11,r12,r13,r14,r15,eiz;
	private static void createMatchers()
	{
		Pattern p;
		
		p = Pattern.compile("rax|eax|ax|ah|al");
		rax = p.matcher("");
		
		p = Pattern.compile("rbx|ebx|bx|bh|bl");
		rbx = p.matcher("");
		
		p = Pattern.compile("rcx|ecx|cx|ch|cl");
		rcx = p.matcher("");
		
		p = Pattern.compile("rdx|edx|dx|dh|dl");
		rdx = p.matcher("");
		
		p = Pattern.compile("rsi|esi|sil|si");
		rsi = p.matcher("");
		
		p = Pattern.compile("rdi|edi|dil|di");
		rdi = p.matcher("");
		
		p = Pattern.compile("rbp|ebp|bpl|bp");
		rbp = p.matcher("");
		
		p = Pattern.compile("rsp|esp|spl|sp");
		rsp = p.matcher("");
		
		p = Pattern.compile("r8b|r8d|r8w|r8l");
		r8 = p.matcher("");
		
		p = Pattern.compile("r9b|r9d|r9w|r9l");
		r9 = p.matcher("");
		
		p = Pattern.compile("r10b|r10d|r10w|r10l");
		r10 = p.matcher("");
		
		p = Pattern.compile("r11b|r11d|r11w|r11l");
		r11 = p.matcher("");
		
		p = Pattern.compile("r12b|r12d|r12w|r12l");
		r12 = p.matcher("");
		
		p = Pattern.compile("r13b|r13d|r13w|r13l");
		r13 = p.matcher("");
		
		p = Pattern.compile("r14b|r14d|r14w|r14l");
		r14 = p.matcher("");
		
		p = Pattern.compile("r15b|r15d|r15w|r15l");
		r15 = p.matcher("");
		
		p = Pattern.compile("eiz");
		eiz = p.matcher("");
	}

	/**
	 * This method converts the smaller parts of register to the complete register
	 * @param operandStr Operand string 
	 */
 	public static String coarsifyRegisters(String operandStr)
	{
 		if(rax==null) {
 			createMatchers();
 		}
 		
 		operandStr = rax.reset(operandStr).replaceAll("rax");
 		operandStr = rbx.reset(operandStr).replaceAll("rbx");
 		operandStr = rcx.reset(operandStr).replaceAll("rcx");
 		operandStr = rdx.reset(operandStr).replaceAll("rdx");
 		
 		operandStr = rsi.reset(operandStr).replaceAll("rsi");
 		operandStr = rdi.reset(operandStr).replaceAll("rdi");
 		operandStr = rbp.reset(operandStr).replaceAll("rbp");
 		operandStr = rsp.reset(operandStr).replaceAll("rsp");
 		
 		operandStr = r8.reset(operandStr).replaceAll("r8");
 		operandStr = r9.reset(operandStr).replaceAll("r9");
 		operandStr = r10.reset(operandStr).replaceAll("r10");
 		operandStr = r11.reset(operandStr).replaceAll("r11");
 		operandStr = r12.reset(operandStr).replaceAll("r12");
 		operandStr = r13.reset(operandStr).replaceAll("r13");
 		operandStr = r14.reset(operandStr).replaceAll("r14");
 		operandStr = r15.reset(operandStr).replaceAll("r15");
 		 		
 		operandStr = eiz.reset(operandStr).replaceAll("eiz"); 		
 		
		return operandStr;
	}

 	
 	public static Operand getStackPointer()
 	{
 		return Operand.getIntegerRegister(encodeRegister("rsp"));
 	}
 	
 	public static Operand getAccumulatorRegister()
 	{
 		return Operand.getIntegerRegister(encodeRegister("rax"));
 	}
 	
 	public static Operand getDataRegister()
 	{
 		return Operand.getIntegerRegister(encodeRegister("rdx"));
 	}

 	public static Operand getTopFPRegister()
 	{
 		return Operand.getFloatRegister(encodeRegister("st(0)"));
 	}
 	
 	public static Operand getSecondTopFPRegister()
 	{
 		return Operand.getFloatRegister(encodeRegister("st(1)"));
 	}
 	
 	public static Operand getInstructionPointer()
 	{
 		return Operand.getIntegerRegister(encodeRegister("rip"));
 	}
 	

 	public static Operand getTemporaryMemoryRegister(Operand operand)
	{
 		Operand firstOperand;
 		Operand secondOperand;
 		
 		firstOperand = operand.getMemoryLocationFirstOperand();
 		secondOperand = operand.getMemoryLocationSecondOperand();
 		
		if(	operand.isMemoryOperand() && isTempIntRegister(firstOperand))
		{
			return firstOperand;
		}
		
		else if(operand.isMemoryOperand() && isTempIntRegister(secondOperand))
		{
			return secondOperand;
		}
		
		else
		{
			return null;
		}
	}

 	private static boolean isTempIntRegister(Operand operand)
 	{
 		return( 
 		operand!=null && operand.isIntegerRegisterOperand()
 		&& operand.getValue() >=encodeRegister("temp0") && operand.getValue() <=encodeRegister("temp7"));
 	}
 	
 	private static void checkAndCreateRegisterHashTable()
 	{
 		if(integerRegistersHashTable==null || floatRegistersHashTable==null)
 			createRegisterHashTable();
 	}
 	
 	public static Operand getCounterRegister()
 	{
 		return Operand.getIntegerRegister(encodeRegister("rcx"));
 	}

 	public static Operand getSourceIndexRegister()
 	{
 		return Operand.getIntegerRegister(encodeRegister("rsi"));
 	}

 	public static Operand getDestinationIndexRegister()
 	{
 		return Operand.getIntegerRegister(encodeRegister("rdi"));
 	}

	public static Operand getBasePointer() 
	{
		return Operand.getIntegerRegister(encodeRegister("rbp"));
	}

	public static Operand getBaseIndexRegister() 
	{
		return Operand.getIntegerRegister(encodeRegister("rbx"));
	}

	public static Operand getEFlagsRegister() 
	{
		return Operand.getIntegerRegister(encodeRegister("eflags"));
	}

	public static Operand getFloatingPointControlWord() 
	{
		return Operand.getIntegerRegister(encodeRegister("FP_CWORD"));
	}

	public static Operand getMXCSR() 
	{
		return Operand.getFloatRegister(encodeRegister("MXCSR"));
	}

	public static int getMaxIntegerRegisters() {
		checkAndCreateRegisterHashTable();
		return integerRegistersHashTable.size();
	}

	public static int getMaxFloatRegisters() {
		checkAndCreateRegisterHashTable();
		return floatRegistersHashTable.size();
	}
 }