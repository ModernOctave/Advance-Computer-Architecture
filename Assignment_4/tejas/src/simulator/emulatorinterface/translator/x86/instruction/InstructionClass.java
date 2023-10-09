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

public enum InstructionClass 
{
	INTEGER_ALU,
	INTEGER_ALU_NO_FLAG_MOD,
	INTEGER_ALU_USES_FLAGS,
	INTEGER_COMPARE_TEST,
	INTEGER_COMPARE_TEST_SET,
	EXTEND_EAX_TO_EDX,
	ACCUMULATOR_ADJUSTMENTS,
	SHIFT_OPERATION_THREE_OPERANDS,
	INTEGER_MOVE,
	INTEGER_MOVE_ACCUMULATOR,
	FLOAT_MOVE,
	CONDITIONAL_MOVE,
	EXCHANGE,
	CMP_XCHG,
	CMP_XCHG_BYTE,
	EXCHANGE_AND_ADD,
	CONDITIONAL_JUMP,
	UNCONDITIONAL_JUMP,
	CONDITIONAL_LOOP,
	UNCONDITIONAL_LOOP,
	NOP,
	INTEGER_MULTIPLICATION,
	INTEGER_DIVISION,
	INTERRUPT,
	LOAD_EFFECTIVE_ADDRESS,
	CONDITIONAL_SET,

	
	//Stack Operations
	PUSH,
	PUSH_ALL,
	PUSH_FLAGS,
	POP,
	POP_ALL,
	POP_FLAGS,
	CALL,
	RETURN,
	LEAVE,

	
	//String Operations
	STRING_MOVE,
	STRING_LOAD,
	STRING_STORE,
	STRING_COMPARE,
	STRING_COMPARE_ACCUMULATOR,
	
	//flag mods
	FLAGS_MOD_WITHOUT_SRC,
	FLAGS_MOD_WITH_SRC,
	FLAGS_STORE,
	FLAGS_LOAD,	
	
	//Floating Point operations
	FLOATING_POINT_LOAD_CONSTANT,
	FLOATING_POINT_LOAD,
	FLOATING_POINT_STORE,
	FLOATING_POINT_MULTIPLICATION,
	FLOATING_POINT_DIVISION,
	FLOATING_POINT_ALU,
	FLOATING_POINT_NO_OPERAND_ALU,
	FLOATING_POINT_EXCHANGE,
	FLOATING_POINT_COMPLEX_OPERATION,
	FLOATING_POINT_COMPARE,
	FLOATING_POINT_CONDITIONAL_MOVE,
	FLOATING_POINT_LOAD_CONTROL_WORD,
	FLOATING_POINT_STORE_CONTROL_WORD,
	FLOATING_POINT_EXAMINE,
	
	//Convert operations
	CONVERT_FLOAT_TO_PACKED_INTEGER,
	CONVERT_FLOAT_TO_INTEGER,
	CONVERT_PACKED_INTEGER_TO_FLOAT,
	CONVERT_INTEGER_TO_FLOAT,
	CONVERT_FLOAT_TO_FLOAT,
	
	//Not Handled
	REPEAT,
	LOCK,

	//SSE Instructions
	SSE_INTEGER,
	SSE_INTEGER_MUL_ADD,
	SSE_INTEGER_COMPARE,
	SSE_INTEGER_MUL,
	SSE_STORE,
	SSE_FLOAT,
	SSE_FMA,
	SSE_FLOAT_COMPARE,
	SSE_FLOAT_MUL,
	SSE_FLOAT_DIV,
	
	//Float
	FLOAT_ALU,
	FMA,
	FLOAT_COMPARE,
	FLOAT_COMPARE_EXPLICIT_DEST,
	FLOAT_MUL,
	FLOAT_DIV,
	LOAD_MXCSR,
	STORE_MXCSR,
	
	AES,
	VECTOR_STRING_DEST_ECX,
	VECTOR_STRING_DEST_XMM0,
	BIT_SCAN,
	VECTOR_SHUFFLE,
	SQRT,
	
	CPUID,
	INTERRUPT_RETURN,
	MFENCE,
	SFENCE,
	LFENCE,
	
	READ_PREFETCH_L1,
	READ_PREFETCH_L2,
	READ_PREFETCH_L3,
	WRITE_PREFETCH,
	
	INVALID,   
}