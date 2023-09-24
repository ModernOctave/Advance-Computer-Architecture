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

	Contributors:  Prathmesh Kallurkar, Rajshekar Kalyappam
*****************************************************************************/

package generic;

import java.io.Serializable;

import emulatorinterface.translator.x86.registers.Registers;

import main.CustomObjectPool;
import main.Main;



public class Operand implements Serializable
{
	// pre-allocated operands
	private static Operand floatRegisterOperands[];
	private static Operand integerRegisterOperands[];
	
	// different types of memory operands : integer-msr-immediate
	private static Operand memoryIntegerOperands[];
	private static Operand memoryIntegerIntegerOperands[][];
	private static Operand[] memoryIntegerImmediateOperands;
	
	private static Operand memoryImmediateOperand;
	private static Operand memoryImmediateImmediateOperand;
	
	
	
	private static Operand immediateOperand;
	
	public static void preAllocateOperands() {
		
		// Create immediate operand
		immediateOperand = new Operand();
		immediateOperand.type = OperandType.immediate;
		immediateOperand.value = -1;
		
		// Create integer registers
		integerRegisterOperands = new Operand[Registers.getMaxIntegerRegisters()];
		for(int i=0; i<Registers.getMaxIntegerRegisters(); i++) {
			integerRegisterOperands[i] = new Operand();
			integerRegisterOperands[i].type = OperandType.integerRegister;
			integerRegisterOperands[i].value = i;
		}
		
		// Create float registers
		floatRegisterOperands = new Operand[Registers.getMaxFloatRegisters()];
		for(int i=0; i<Registers.getMaxFloatRegisters(); i++) {
			floatRegisterOperands[i] = new Operand();
			floatRegisterOperands[i].type = OperandType.vectorRegister;
			floatRegisterOperands[i].value = i;
		}
		
		// Create memory operands
		memoryIntegerOperands = new Operand[integerRegisterOperands.length];
		for(int i=0; i<integerRegisterOperands.length; i++) {
			memoryIntegerOperands[i] = new Operand();
			memoryIntegerOperands[i].type = OperandType.memory;
			memoryIntegerOperands[i].memoryLocationFirstOperand = integerRegisterOperands[i];
			memoryIntegerOperands[i].memoryLocationSecondOperand = null;
		}
		
		memoryIntegerIntegerOperands = new Operand[integerRegisterOperands.length][integerRegisterOperands.length];
		for(int i=0; i<integerRegisterOperands.length; i++) {
			for(int j=0; j<integerRegisterOperands.length; j++) {
				memoryIntegerIntegerOperands[i][j] = new Operand();
				memoryIntegerIntegerOperands[i][j].type = OperandType.memory;
				memoryIntegerIntegerOperands[i][j].memoryLocationFirstOperand = integerRegisterOperands[i];
				memoryIntegerIntegerOperands[i][j].memoryLocationSecondOperand = integerRegisterOperands[j];
			}
		}
		
		memoryIntegerImmediateOperands = new Operand[integerRegisterOperands.length];
		for(int i=0; i<integerRegisterOperands.length; i++) {
			memoryIntegerImmediateOperands[i] = new Operand();
			memoryIntegerImmediateOperands[i].type = OperandType.memory;
			memoryIntegerImmediateOperands[i].memoryLocationFirstOperand = integerRegisterOperands[i];
			memoryIntegerImmediateOperands[i].memoryLocationSecondOperand = immediateOperand;
		}
		
		memoryImmediateOperand = new Operand();
		memoryImmediateOperand.type = OperandType.memory;
		memoryImmediateOperand.memoryLocationFirstOperand = immediateOperand;
		memoryImmediateOperand.memoryLocationSecondOperand = null;
		
		memoryImmediateImmediateOperand = new Operand();
		memoryImmediateImmediateOperand.type = OperandType.memory;
		memoryImmediateImmediateOperand.memoryLocationFirstOperand = immediateOperand;
		memoryImmediateImmediateOperand.memoryLocationSecondOperand = immediateOperand;
	}
	
	
	private OperandType type;
	private long value;			//if operand type is register, value indicates which register
								//if operand type is immediate, value indicates the operand value
	Operand memoryLocationFirstOperand;
	Operand memoryLocationSecondOperand;
	
	public void setMemoryLocationFirstOperand(Operand memoryLocationFirstOperand) {
		this.memoryLocationFirstOperand = memoryLocationFirstOperand;
	}

	public void setMemoryLocationSecondOperand(Operand memoryLocationSecondOperand) {
		this.memoryLocationSecondOperand = memoryLocationSecondOperand;
	}

	public Operand()
	{
		this.value = -1;
		this.memoryLocationFirstOperand = null;
		this.memoryLocationSecondOperand = null;
	}
	 
	public void clear()
	{
		this.value = 0;
		this.memoryLocationFirstOperand = null;
		this.memoryLocationSecondOperand = null;
	}
	
	public Operand(OperandType operandType, long  operandValue)
	{
		this.type = operandType;
		this.value = operandValue;
		
		this.memoryLocationFirstOperand = null;
		this.memoryLocationSecondOperand = null;
	}

	public Operand(OperandType operandType, long  operandValue,
			Operand memoryLocationFirstOperand, Operand memoryOperandSecondOperand)
	{
		this.type = operandType;
		this.value = operandValue;

		this.memoryLocationFirstOperand = memoryLocationFirstOperand;
		this.memoryLocationSecondOperand = memoryOperandSecondOperand;
	}
	
//	/* our copy constructor */
//	public Operand(Operand operand)
//	{
//		this.type=operand.type;
//		this.value=operand.value;
//		
//		if(operand.memoryLocationFirstOperand==null) {
//			this.memoryLocationFirstOperand=null;
//		} else {
//			this.memoryLocationFirstOperand=new Operand(operand.memoryLocationFirstOperand);
//		}
//		
//		if(operand.memoryLocationSecondOperand==null) {
//			this.memoryLocationSecondOperand=null;
//		} else {
//			this.memoryLocationSecondOperand=new Operand(operand.memoryLocationSecondOperand);
//		}
//	}
	
	//all properties of sourceOperand is copied to the current operand
	public void copy(Operand sourceOperand)
	{
		this.type=sourceOperand.type;
		this.value=sourceOperand.value;
		
		this.memoryLocationFirstOperand = sourceOperand.memoryLocationFirstOperand;
		this.memoryLocationSecondOperand = sourceOperand.memoryLocationSecondOperand;
	}
	
	public String toString()
	{
			return ("(" + type + ") " + value);
	}

	public OperandType getOperandType()
	{
		return type;
	}

	public void setValue(long value) 
	{
		if(this.type==OperandType.memory) {
			misc.Error.showErrorAndExit("please do not use value field for memory operand");
		}
		
		this.value = value; 
	}
		
	public long getValue()
	{
		if(this.type==OperandType.memory) {
			misc.Error.showErrorAndExit("please do not use value field for memory operand");
		}
		
		return value;
	}
	
	public Operand getMemoryLocationFirstOperand()
	{
		return this.memoryLocationFirstOperand;
	}
	
	public Operand getMemoryLocationSecondOperand()
	{
		return this.memoryLocationSecondOperand;
	}
	
	public boolean isIntegerRegisterOperand()
	{
		return (this.type == OperandType.integerRegister);
	}
	
	public boolean isImmediateOperand()
	{
		return (this.type == OperandType.immediate);
	}
	
	public boolean isMemoryOperand()
	{
		return (this.type == OperandType.memory);
	}
	
	public boolean isFloatRegisterOperand()
	{
		return (this.type == OperandType.vectorRegister);
	}
	
	private void set(OperandType operandType, long  operandValue)
	{
		this.type = operandType;
		this.value = operandValue;
		
		this.memoryLocationFirstOperand = null;
		this.memoryLocationSecondOperand = null;
	}

	private void set(OperandType operandType, long  operandValue,
			Operand memoryLocationFirstOperand, Operand memoryOperandSecondOperand)
	{
		this.type = operandType;
		this.value = operandValue;

		this.memoryLocationFirstOperand = memoryLocationFirstOperand;
		this.memoryLocationSecondOperand = memoryOperandSecondOperand;
	}
	
	public static Operand getIntegerRegister(long value)
	{
		return integerRegisterOperands[(int) value];
	}
	
	public static Operand getFloatRegister(long value)
	{
		return floatRegisterOperands[(int) value];
	}
	
	public static Operand getImmediateOperand()
	{
		return immediateOperand;
	}
	
	public static Operand getMemoryOperand(Operand op1, Operand op2)
	{
		switch(op1.type) {
			case integerRegister:
				if (op2==null) {
					return memoryIntegerOperands[(int) op1.value];
				} else if (op2.type==OperandType.integerRegister) {
					return memoryIntegerIntegerOperands[(int) op1.value][(int) op2.value];
				} else if (op2.type==OperandType.immediate) {
					return memoryIntegerImmediateOperands[(int) op1.value];
				}
				break;
				
			case immediate:
				if(op2==null) {
					return memoryImmediateOperand;
				} else if (op2.type==OperandType.immediate) {
					return memoryImmediateImmediateOperand;
				}
				break;
				
			default:
				misc.Error.showErrorAndExit("invalid operand type for memory : " + op1);
		}
		
		return null;
	}
	
}