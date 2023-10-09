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

import java.util.Hashtable;

public class InstructionClassTable {
	private static Hashtable<String, InstructionClass> instructionClassTable;
	private static Hashtable<InstructionClass, X86StaticInstructionHandler> instructionClassHandlerTable;

	private static void createInstructionClassHandlerTable() {
		// create an empty hash-table for storing object references.
		instructionClassHandlerTable = new Hashtable<InstructionClass, X86StaticInstructionHandler>();
		
		instructionClassHandlerTable.put(
				InstructionClass.AES,
				new AES());
		
		instructionClassHandlerTable.put(
				InstructionClass.VECTOR_STRING_DEST_ECX,
				new VectorStringDestECX());
		
		instructionClassHandlerTable.put(
				InstructionClass.VECTOR_STRING_DEST_XMM0,
				new VectorStringDestXMM0());

		instructionClassHandlerTable.put(
				InstructionClass.STRING_COMPARE,
				new StringCompare());

		instructionClassHandlerTable.put(
				InstructionClass.STRING_COMPARE_ACCUMULATOR,
				new StringCompareAccumulator());

		instructionClassHandlerTable.put(
				InstructionClass.FLAGS_MOD_WITHOUT_SRC,
				new FlagsModWithoutSrc());

		instructionClassHandlerTable.put(
				InstructionClass.FLAGS_MOD_WITH_SRC,
				new FlagsModWithSrc());

		instructionClassHandlerTable.put(
				InstructionClass.FLAGS_STORE,
				new FlagsStore());

		instructionClassHandlerTable.put(
				InstructionClass.FLAGS_LOAD,
				new FlagsLoad());
		
		instructionClassHandlerTable.put(
				InstructionClass.BIT_SCAN,
				new BitScan());
		
		instructionClassHandlerTable.put(
				InstructionClass.VECTOR_SHUFFLE,
				new VectorShuffle());

		instructionClassHandlerTable.put(
				InstructionClass.LOAD_EFFECTIVE_ADDRESS,
				new LoadEffectiveAddress());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_ALU,
				new IntegerALU());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_ALU_NO_FLAG_MOD,
				new IntegerALUNoFlagMod());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_ALU_USES_FLAGS,
				new IntegerALUUsesFlags());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_COMPARE_TEST,
				new IntegerCompareTest());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_COMPARE_TEST_SET,
				new IntegerCompareTestSet());

		instructionClassHandlerTable.put(
				InstructionClass.EXTEND_EAX_TO_EDX,
				new ExtendEAXToEDX());

		instructionClassHandlerTable.put(
				InstructionClass.ACCUMULATOR_ADJUSTMENTS,
				new AccumulatorAdjustments());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_MULTIPLICATION,
				new IntegerMultiplication());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_DIVISION,
				new IntegerDivision());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_MOVE, 
				new IntegerMove());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_MOVE_ACCUMULATOR, 
				new IntegerMoveAccumulator());

		instructionClassHandlerTable.put(
				InstructionClass.FLOAT_MOVE, 
				new FloatMove());

		instructionClassHandlerTable.put(
				InstructionClass.EXCHANGE,
				new Exchange());

		instructionClassHandlerTable.put(
				InstructionClass.CMP_XCHG,
				new CmpExchange());

		instructionClassHandlerTable.put(
				InstructionClass.CMP_XCHG_BYTE,
				new CmpExchangeByte());

		instructionClassHandlerTable.put(
				InstructionClass.EXCHANGE_AND_ADD,
				new ExchangeAndAdd());

		instructionClassHandlerTable.put(
				InstructionClass.STRING_MOVE,
				new StringMove());

		instructionClassHandlerTable.put(
				InstructionClass.STRING_LOAD,
				new StringLoad());

		instructionClassHandlerTable.put(
				InstructionClass.STRING_STORE,
				new StringStore());

		instructionClassHandlerTable.put(
				InstructionClass.PUSH, 
				new Push());

		instructionClassHandlerTable.put(
				InstructionClass.PUSH_ALL, 
				new PushAll());

		instructionClassHandlerTable.put(
				InstructionClass.PUSH_FLAGS, 
				new PushFlags());

		instructionClassHandlerTable.put(
				InstructionClass.POP, 
				new Pop());

		instructionClassHandlerTable.put(
				InstructionClass.POP_ALL, 
				new PopAll());

		instructionClassHandlerTable.put(
				InstructionClass.POP_FLAGS, 
				new PopFlags());

		instructionClassHandlerTable.put(
				InstructionClass.CONDITIONAL_MOVE,
				new ConditionalMove());
		
		instructionClassHandlerTable.put(
				InstructionClass.CONDITIONAL_SET,
				new ConditionalSet());

		instructionClassHandlerTable.put(
				InstructionClass.UNCONDITIONAL_JUMP,
				new UnconditionalJump());

		instructionClassHandlerTable.put(
				InstructionClass.CONDITIONAL_JUMP,
				new ConditionalJump());

		instructionClassHandlerTable.put(
				InstructionClass.UNCONDITIONAL_LOOP, 
				new Loop());

		instructionClassHandlerTable.put(
				InstructionClass.CONDITIONAL_LOOP, 
				new ConditionalLoop());

		instructionClassHandlerTable.put(
				InstructionClass.CALL, 
				new Call());

		instructionClassHandlerTable.put(
				InstructionClass.RETURN,
				new ReturnOp());

		instructionClassHandlerTable.put(
				InstructionClass.NOP, 
				new NOP());

		instructionClassHandlerTable.put(
				InstructionClass.INTERRUPT,
				new Interrupt());

		instructionClassHandlerTable.put(
				InstructionClass.SSE_INTEGER,
				new SSEInteger());

		instructionClassHandlerTable.put(
				InstructionClass.SSE_INTEGER_MUL_ADD,
				new SSEIntegerMul());

		instructionClassHandlerTable.put(
				InstructionClass.SSE_INTEGER_COMPARE,
				new SSEIntegerCmp());

		instructionClassHandlerTable.put(
				InstructionClass.SSE_INTEGER_MUL,
				new SSEIntegerMul());

		instructionClassHandlerTable.put(
				InstructionClass.SSE_STORE,
				new SSEStore());

		instructionClassHandlerTable.put(
				InstructionClass.SSE_FLOAT,
				new SSEFloat());

		instructionClassHandlerTable.put(
				InstructionClass.SSE_FMA,
				new SSEFMA());

		instructionClassHandlerTable.put(
				InstructionClass.SSE_FLOAT_COMPARE,
				new SSEFloatCmp());

		instructionClassHandlerTable.put(
				InstructionClass.SSE_FLOAT_MUL,
				new SSEFloatMul());

		instructionClassHandlerTable.put(
				InstructionClass.SSE_FLOAT_DIV,
				new SSEFloatMul());

		instructionClassHandlerTable.put(
				InstructionClass.FLOAT_ALU,
				new FloatALU());

		instructionClassHandlerTable.put(
				InstructionClass.FMA,
				new FMA());

		instructionClassHandlerTable.put(
				InstructionClass.FLOAT_COMPARE,
				new FloatCmp());

		instructionClassHandlerTable.put(
				InstructionClass.FLOAT_MUL,
				new FloatMul());

		instructionClassHandlerTable.put(
				InstructionClass.FLOAT_DIV,
				new FloatDiv());

		instructionClassHandlerTable.put(
				InstructionClass.LOAD_MXCSR,
				new LoadMXCSR());

		instructionClassHandlerTable.put(
				InstructionClass.STORE_MXCSR,
				new StoreMXCSR());

		instructionClassHandlerTable.put(
				InstructionClass.SQRT,
				new FloatDiv());

		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_LOAD_CONSTANT,
				new X87FloatingPointLoadConstant());

		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_LOAD,
				new X87FloatingPointLoad());

		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_STORE,
				new X87FloatingPointStore());

		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_MULTIPLICATION,
				new X87FloatingPointMultiplication());

		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_DIVISION,
				new X87FloatingPointDivision());

		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_ALU,
				new X87FloatingPointALU());

		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_NO_OPERAND_ALU,
				new X87FloatingPointNoOperandALU());

		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_EXCHANGE,
				new X87FloatingPointExchange());

		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_COMPLEX_OPERATION,
				new X87FloatingPointComplexOperation());

		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_COMPARE,
				new X87FloatingPointCompare());

		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_CONDITIONAL_MOVE,
				new X87FloatingPointConditionalMove());
		
		instructionClassHandlerTable.put(
				InstructionClass.LEAVE,
				new Leave());
		
		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_LOAD_CONTROL_WORD,
				new X87FloatingPointLoadControlWord());
		
		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_STORE_CONTROL_WORD,
				new X87FloatingPointStoreControlWord());
		
		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_EXAMINE,
				new X87FloatingPointExamine());
		
		instructionClassHandlerTable.put(
				InstructionClass.CPUID,
				new CPUID());
		
		instructionClassHandlerTable.put(
				InstructionClass.INTERRUPT_RETURN,
				new InterruptReturn());
		
		instructionClassHandlerTable.put(
				InstructionClass.MFENCE,
				new MFence());
		
		instructionClassHandlerTable.put(
				InstructionClass.SFENCE,
				new SFence());
		
		instructionClassHandlerTable.put(
				InstructionClass.LFENCE,
				new LFence());
		
		instructionClassHandlerTable.put(
				InstructionClass.READ_PREFETCH_L1,
				new ReadPrefetchL1());
		
		instructionClassHandlerTable.put(
				InstructionClass.READ_PREFETCH_L2,
				new ReadPrefetchL2());
		
		instructionClassHandlerTable.put(
				InstructionClass.READ_PREFETCH_L3,
				new ReadPrefetchL3());
		
		instructionClassHandlerTable.put(
				InstructionClass.WRITE_PREFETCH,
				new WritePrefetch());
	}

	private static void createInstructionClassTable() 
	{
		instructionClassTable = new Hashtable<String, InstructionClass>();
		
		String AES[] = "aesenc|vaesenc|aesenclast|vaesenclast|aesdec|vaesdec|aesdeclast|vaesdeclast|aesimc|vaesimc|aeskeygenassist|vaeskeygenassist".split("\\|");
		for(int i=0; i<AES.length; i++)
			instructionClassTable.put(AES[i], 
					InstructionClass.AES);
		
		String VectorString1[] = "pcmpestri|pcmpistri|vpcmpestri|vpcmpistri".split("\\|");
		for(int i=0; i<VectorString1.length; i++)
			instructionClassTable.put(VectorString1[i], 
					InstructionClass.VECTOR_STRING_DEST_ECX);
		
		String VectorString2[] = "pcmpestrm|pcmpistrm|vpcmpestrm|vpcmpistrm".split("\\|");
		for(int i=0; i<VectorString2.length; i++)
			instructionClassTable.put(VectorString2[i], 
					InstructionClass.VECTOR_STRING_DEST_XMM0);

		String stringCompare[] = "cmps".split("\\|");
		for (int i = 0; i < stringCompare.length; i++)
			instructionClassTable.put(stringCompare[i],
					InstructionClass.STRING_COMPARE);

		String stringCompareAccumulator[] = "scas|scasb|scasw|scasd|scasq".split("\\|");
		for (int i = 0; i < stringCompareAccumulator.length; i++)
			instructionClassTable.put(stringCompareAccumulator[i],
					InstructionClass.STRING_COMPARE_ACCUMULATOR);

		String flagsModWithoutSrc[] = "stac|stc|std|sti|clac|clc|cld|cli".split("\\|");
		for (int i = 0; i < flagsModWithoutSrc.length; i++)
			instructionClassTable.put(flagsModWithoutSrc[i],
					InstructionClass.FLAGS_MOD_WITHOUT_SRC);

		String flagsModWithSrc[] = "cmc".split("\\|");
		for (int i = 0; i < flagsModWithSrc.length; i++)
			instructionClassTable.put(flagsModWithSrc[i],
					InstructionClass.FLAGS_MOD_WITH_SRC);

		String flagsStore[] = "lahf".split("\\|");
		for (int i = 0; i < flagsStore.length; i++)
			instructionClassTable.put(flagsStore[i],
					InstructionClass.FLAGS_STORE);

		String flagsLoad[] = "sahf".split("\\|");
		for (int i = 0; i < flagsLoad.length; i++)
			instructionClassTable.put(flagsLoad[i],
					InstructionClass.FLAGS_LOAD);
		
		String BitScan[] = "bsf|bsr".split("\\|");
		for(int i=0; i<BitScan.length; i++)
			instructionClassTable.put(BitScan[i], 
					InstructionClass.BIT_SCAN);
		
		String VectorShuffle[] = "shufps|vshufps|shufpd|vshufpd|pshufb|vpshufb|pshufd|vpshufd|pshufhw|vpshufhw|pshuflw|vpshuflw|pshufw|vpshufw|vshuff32x4|vshufi32x4|vshuff64x2|vshufi64x2".split("\\|");
		for(int i=0; i<VectorShuffle.length; i++)
			instructionClassTable.put(VectorShuffle[i], 
					InstructionClass.VECTOR_SHUFFLE);

		String loadEffectiveAddress[] = "lea".split("\\|");
		for (int i = 0; i < loadEffectiveAddress.length; i++)
			instructionClassTable.put(loadEffectiveAddress[i],
					InstructionClass.LOAD_EFFECTIVE_ADDRESS);

		String integerAlu[] = "and|or|xor|add|sub|shl|sal|shr|sar|rol|rcl|ror|rcr|shld|shrd|tzcnt|andn|bextr|blsi|blsmsk|blsr|bzhi|dec|inc|lzcnt|neg|inc|dec|not"
				.split("\\|");
		for (int i = 0; i < integerAlu.length; i++)
			instructionClassTable.put(integerAlu[i],
					InstructionClass.INTEGER_ALU);

		String integerAluNoFlagMod[] = "sarx|shlx|shrx|rorx|bswap|pdep|pext"
				.split("\\|");
		for (int i = 0; i < integerAluNoFlagMod.length; i++)
			instructionClassTable.put(integerAluNoFlagMod[i],
					InstructionClass.INTEGER_ALU_NO_FLAG_MOD);

		String integerAluUsesFlags[] = "adc|sbb|adox|adcx"
				.split("\\|");
		for (int i = 0; i < integerAluUsesFlags.length; i++)
			instructionClassTable.put(integerAluUsesFlags[i],
					InstructionClass.INTEGER_ALU_USES_FLAGS);

		String integerCompareTest[] = "cmp|test|bt".split("\\|");
		for (int i = 0; i < integerCompareTest.length; i++)
			instructionClassTable.put(integerCompareTest[i],
					InstructionClass.INTEGER_COMPARE_TEST);

		String integerCompareTestSet[] = "bts|btr|btc".split("\\|");
		for (int i = 0; i < integerCompareTestSet.length; i++)
			instructionClassTable.put(integerCompareTestSet[i],
					InstructionClass.INTEGER_COMPARE_TEST_SET);

		String extendEAXToEDX[] = "cwd|cdq|cqo"
				.split("\\|");
		for (int i = 0; i < extendEAXToEDX.length; i++)
			instructionClassTable
					.put(extendEAXToEDX[i],
							InstructionClass.EXTEND_EAX_TO_EDX);

		String accumulatorAdjustments[] = "daa|das|cbw|cwde|cdqe|aaa|aas|aam|aad"
				.split("\\|");
		for (int i = 0; i < accumulatorAdjustments.length; i++)
			instructionClassTable
					.put(accumulatorAdjustments[i],
							InstructionClass.ACCUMULATOR_ADJUSTMENTS);

		String integerMultiplication[] = "mul|imul|mulx".split("\\|");
		for (int i = 0; i < integerMultiplication.length; i++)
			instructionClassTable.put(integerMultiplication[i],
					InstructionClass.INTEGER_MULTIPLICATION);

		String integerDivision[] = "div|idiv".split("\\|");
		for (int i = 0; i < integerDivision.length; i++)
			instructionClassTable.put(integerDivision[i],
					InstructionClass.INTEGER_DIVISION);

		String integerMove[] = "mov|movbe|movdir64b|movdiri|movnti|movsx|movsxd|movzx|lds|les|lfs|lgs|lss".split("\\|");
		for (int i = 0; i < integerMove.length; i++)
			instructionClassTable.put(integerMove[i], InstructionClass.INTEGER_MOVE);
		
		String integerMoveAccumulator[] = "movabs".split("\\|");
		for (int i = 0; i < integerMoveAccumulator.length; i++)
			instructionClassTable.put(integerMoveAccumulator[i],
					InstructionClass.INTEGER_MOVE_ACCUMULATOR);

		String floatMove[] = "movddup|movss|vmovss|movsd|vmovsd".split("\\|");
		for (int i = 0; i < floatMove.length; i++)
			instructionClassTable.put(floatMove[i], InstructionClass.FLOAT_MOVE);

		String exchange[] = "xchg".split("\\|");
		for (int i = 0; i < exchange.length; i++)
			instructionClassTable.put(exchange[i], InstructionClass.EXCHANGE);

		String cmpExchange[] = "cmpxchg".split("\\|");
		for (int i = 0; i < cmpExchange.length; i++)
			instructionClassTable.put(cmpExchange[i], InstructionClass.CMP_XCHG);

		String cmpExchangeByte[] = "cmpxchg16b|cmpxchg8b".split("\\|");
		for (int i = 0; i < cmpExchangeByte.length; i++)
			instructionClassTable.put(cmpExchangeByte[i], InstructionClass.CMP_XCHG_BYTE);

		String stringMove[] = "movs|movsq".split("\\|");
		for (int i = 0; i < stringMove.length; i++)
			instructionClassTable.put(stringMove[i], InstructionClass.STRING_MOVE);

		String stringLoad[] = "lods|lodsb|lodsd|lodsq|lodsw".split("\\|");
		for (int i = 0; i < stringLoad.length; i++)
			instructionClassTable.put(stringLoad[i], InstructionClass.STRING_LOAD);

		String stringStore[] = "stos|stosb|stosd|stosq|stosw".split("\\|");
		for (int i = 0; i < stringStore.length; i++)
			instructionClassTable.put(stringStore[i], InstructionClass.STRING_STORE);

		//FIXME support for atomic operations partially present (changes in coherence protocol required)
		String exchangeAndAdd[] = "xadd".split("\\|");
		for (int i = 0; i < exchangeAndAdd.length; i++)
			instructionClassTable.put(exchangeAndAdd[i],
					InstructionClass.EXCHANGE_AND_ADD);

		String push[] = "push|pushw|pushd".split("\\|");
		for (int i = 0; i < push.length; i++)
			instructionClassTable.put(push[i], InstructionClass.PUSH);

		String pushAll[] = "pusha|pushad".split("\\|");
		for (int i = 0; i < pushAll.length; i++)
			instructionClassTable.put(pushAll[i], InstructionClass.PUSH_ALL);

		String pushFlags[] = "pushf|pushfd|pushfq".split("\\|");
		for (int i = 0; i < pushFlags.length; i++)
			instructionClassTable.put(pushFlags[i], InstructionClass.PUSH_FLAGS);

		String pop[] = "pop|popw|popd".split("\\|");
		for (int i = 0; i < pop.length; i++)
			instructionClassTable.put(pop[i], InstructionClass.POP);

		String popAll[] = "popa|popad".split("\\|");
		for (int i = 0; i < popAll.length; i++)
			instructionClassTable.put(popAll[i], InstructionClass.POP_ALL);

		String popFlags[] = "popf|popfd|popfq".split("\\|");
		for (int i = 0; i < popFlags.length; i++)
			instructionClassTable.put(popFlags[i], InstructionClass.POP_FLAGS);
		
		String conditionalMove[] = "cmov|cmova|cmovae|cmovb|cmovbe|cmovc|cmove|cmovg|cmovge|cmovl|cmovle|cmovna|cmovnae|cmovnb|cmovnbe|cmovnc|cmovne|cmovng|cmovnge|cmovnl|cmovnle|cmovno|cmovnp|cmovns|cmovnz|cmovo|cmovp|cmovpe|cmovpo|cmovs|cmovz"
				.split("\\|");
		for (int i = 0; i < conditionalMove.length; i++)
			instructionClassTable.put(conditionalMove[i],
					InstructionClass.CONDITIONAL_MOVE);
		
		String conditionalSet[] = "seta|setae|setb|setbe|setc|sete|setg|setge|setl|setle|setna|setnae|setnb|setnbe|setnc|setne|setng|setnge|setnl|setnle|setno|setnp|setns|setnz|seto|setp|setpe|setpo|sets|setz"
				.split("\\|");
		for (int i = 0; i < conditionalSet.length; i++)
			instructionClassTable.put(conditionalSet[i],
					InstructionClass.CONDITIONAL_SET);

		String unconditionalJump[] = "jmp".split("\\|");
		for (int i = 0; i < unconditionalJump.length; i++)
			instructionClassTable.put(unconditionalJump[i],
					InstructionClass.UNCONDITIONAL_JUMP);
		
		String conditionalJump[] = "ja|jae|jb|jbe|jc|jcxz|jecxz|jrcxz|je|jg|jge|jl|jle|jna|jnae|jnb|jnbe|jnc|jne|jng|jnge|jnl|jnle|jno|jnp|jns|jnz|jo|jp|jpe|jpo|js|jz"
				.split("\\|");
		for (int i = 0; i < conditionalJump.length; i++)
			instructionClassTable.put(conditionalJump[i],
					InstructionClass.CONDITIONAL_JUMP);

		String loop[] = "loop"
				.split("\\|");
		for (int i = 0; i < loop.length; i++)
			instructionClassTable.put(loop[i], InstructionClass.UNCONDITIONAL_LOOP);

		String conditionalLoop[] = "loop|loopw|loopd|loope|looped|loopew|loopne|loopned|loopnew|loopz|loopzd|loopzw|loopnz|loopnzd|loopnzw"
				.split("\\|");
		for (int i = 0; i < conditionalLoop.length; i++)
			instructionClassTable.put(conditionalLoop[i], InstructionClass.CONDITIONAL_LOOP);

		/*
		 * FIXME: handling syscall and sysenter this way is an approximation
		 */
		String call[] = "call|syscall|sysenter".split("\\|");
		for (int i = 0; i < call.length; i++)
			instructionClassTable.put(call[i], InstructionClass.CALL);

		String returnOp[] = "ret|retn|retf|retw|retnw|retfw|retd|retnd|retfd|retd|sysret|sysexit"
				.split("\\|");
		for (int i = 0; i < returnOp.length; i++)
			instructionClassTable.put(returnOp[i], InstructionClass.RETURN);

		String nop[] = "nop|nopl|fnop|nopw".split("\\|");
		for (int i = 0; i < nop.length; i++)
			instructionClassTable.put(nop[i], InstructionClass.NOP);

		String interrupt[] = "int".split("\\|");
		for (int i = 0; i < interrupt.length; i++)
			instructionClassTable.put(interrupt[i], InstructionClass.INTERRUPT);

		String SSEInteger[] = "pand|vpand|pandn|vpandn|vpandnd|vpandnq|por|vpor|pxor|vpxor|paddb|paddw|paddd|paddq|vpaddb|vpaddw|vpaddd|vpaddq|psubusb|psubusw|vpsubusb|vpsubusw|pmaddubsw|vpmaddubsw|pmaddwd|vpmaddwd|pmaxsb|vpmaxsb|pmaxsw|vpmaxsw|pmaxsd|vpmaxsd|pmaxsq|vpmaxsq|pmaxub|vpmaxub|pmaxuw|vpmaxuw|pmaxud|vpmaxud|pmaxuq|vpmaxuq|pminsb|vpminsb|pminsw|vpminsw|pminsd|vpminsd|pminsq|vpminsq|pminub|vpminub|pminuw|vpminuw|pminud|vpminud|pminuq|vpminuq|pmovmskb|psadbw|vpsadbw|psignb|vpsignb|psignd|vpsignd|psignw|vpsignw|psllw|vpsllw|pslld|vpslld|psllq|vpsllq|pslldq|vpslldq|psraw|vpsraw|psrad|vpsrad|psraq|vpsraq|psrld|vpsrld|psrldq|vpsrldq|psrlq|vpsrlq|psrlw|vpsrlw|psrld|vpsrld|psubb|vpsubb|psubd|vpsubd|psubw|vpsubw|psubq|vpsubq|psubsb|vpsubsb|psubsw|vpsubsw|punpckhbw|vpunpckhbw|punpckhwd|vpunpckhwd|punpckhdq|vpunpckhdq|punpckhqdq|vpunpckhqdq|punpcklbw|vpunpcklbw|punpcklwd|vpunpcklwd|punpckldq|vpunpckldq|punpcklqdq|vpunpcklqdq|valignd|valignq|vdbpsadbw|vextracti128|vextracti32x4|vextracti64x2|vextracti32x8|vextracti64x4|vinserti128|vinserti32x4|vinserti64x2|vinserti32x8|vinserti64x4|movd|movq|movdqa|vmovdqa|vmovdqa32|vmovdqa64|movdqu|vmovdqu|vmovdqu8|vmovdqu16|vmovdqu32|vmovdqu64|movntdq|vmovntdq|movntdqa|vmovntdqa|movntq|vmovd|vmovq|vpmovmskb|pmovsxbw|vpmovsxbw|pmovsxbd|vpmovsxbd|pmovsxbq|vpmovsxbq|pmovsxwd|vpmovsxwd|pmovsxwq|vpmovsxwq|pmovsxdq|vpmovsxdq|pmovzxbw|vpmovzxbw|pmovzxbd|vpmovzxbd|pmovzxbq|vpmovzxbq|pmovzxwd|vpmovzxwd|pmovzxwq|vpmovzxwq|pmovzxdq|vpmovzxdq|vpblendmb|vpblendmw|vpblendmd|vpblendmq|vpbroadcastb|vpbroadcastw|vpbroadcastd|vpbroadcastq|vbroadcasti32x2|vbroadcasti128|vbroadcasti32x4|vbroadcasti64x2|vbroadcasti32x8|vbroadcasti64x4|vpbroadcastmb2q|vpbroadcastmw2d|vcmpb|vcmpub|vcmpw|vcmpuw|vcmpd|vcmpud|vcmpq|vcmpuq|vpcompressd|vpcompressq|vpconflictd|vpconflictq|vperm2i128|vpermb|vpermw|vpermd|vpermi2b|vpermi2d|vpermi2w|vpermi2q|vpermq|vpermt2b|vpermt2w|vpermt2d|vpermt2q|vpexpandd|vpexpandq|vpgatherdd|vpgatherqd|vpgatherdq|vpgatherqq|vlpzcntd|vlpzcntq|vpmadd52huq|vpmadd52luq|vpmaskmovd|vpmaskmovq|vpmovb2m|vpmovw2m|vpmovd2m|vpmovq2m|vpmovdb|vpmovsdb|vpmovusdb|vpmovdw|vpmovsdw|vpmovusdw|vpmovm2b|vpmovm2w|vpmovm2d|vpmovm2q|vpmovqb|vpmovsqb|vpmovusqb|vpmovqd|vpmovsqd|vpmovusqd|vpmosqw|vpmovsqw|vpmovusqw|vpmovwb|vpmovswb|vpmovuswb|vpmultishiftqb|vprold|vprolvd|vprolq|vprolvq|vprord|vprorvd|vprorq|vprorvq|vpscatterdd|vpscatterdq|vpscatterqd|vpscatterqq|vpsllvw|vpsllvd|vpsllvq|vpsravw|vpsravd|vpsravq|vpsrlvw|vpsrlvd|vpsrlvq|vpternlogd|vpternlogq|vptestmb|vptestmw|vptestmd|vptestmq|vptestnmb|vptestnmw|vptestnmd|vptestnmq|vzeroall|vzeroupper|lddqu|vlddqu|pabsb|pabsw|pabsd|pabsq|vpabsb|vpabsw|vpabsd|vpabsq|packsswb|packssdw|vpacksswb|vpackssdw|packuswb|packusdw|vpackuswb|vpackusdw|paddsb|paddsw|vpaddsb|vpaddsw|paddusb|paddusw|vpaddusb|vpaddusw|palignr|vpalignr|pavgb|vpavgb|pavgw|vpavgw|pblendvb|vpblendvb|pblendw|vpblendw|pclmulqdq|vpclmulqdq|pcmpeqb|pcmpeqw|pcmpeqd|pcmpeqq|vpcmpeqb|vpcmpeqw|vpcmpeqd|vpcmpeqq|pcmpgtb|pcmpgtw|pcmpgtd|pcmpgtq|vpcmpgtb|vpcmpgtw|vpcmpgtd|vpcmpgtq|pextrb|pextrw|pextrd|pextrq|vpextrb|vpextrw|vpextrd|vpextrq|phaddd|vphaddd|phaddw|vphaddw|phaddsw|vphaddsw|phminposuw|vphminposuw|phsubd|vphsubd|phsubw|vphsubw|phsubsw|vphsubsw|pinsrb|vpinsrb|pinsrw|vpinsrw|pinsrd|vpinsrd|pinsrq|vpinsrq".split("\\|");
		for (int i = 0; i < SSEInteger.length; i++)
			instructionClassTable.put(SSEInteger[i], InstructionClass.SSE_INTEGER);

		String SSEIntegerMulAdd[] = "pmaddubsw|vpmaddubsw|pmaddwd|vpmaddwd".split("\\|");
		for (int i = 0; i < SSEIntegerMulAdd.length; i++)
			instructionClassTable.put(SSEIntegerMulAdd[i], InstructionClass.SSE_INTEGER_MUL_ADD);

		String SSEIntegerCmp[] = "ptest|vptest".split("\\|");
		for (int i = 0; i < SSEIntegerCmp.length; i++)
			instructionClassTable.put(SSEIntegerCmp[i], InstructionClass.SSE_INTEGER_COMPARE);

		String SSEIntegerMul[] = "pmuldq|vpmuldq|pmulhrsw|vpmulhrsw|pmulhuw|vpmulhuw|pmulhw|vpmulhw|pmulld|vpmulld|pmullq|vpmullq|pmullw|vpmullw|pmuludq|vpmuludq".split("\\|");
		for (int i = 0; i < SSEIntegerMul.length; i++)
			instructionClassTable.put(SSEIntegerMul[i], InstructionClass.SSE_INTEGER_MUL);
		
		String SSEStore[] = "maskmovdqu|maskmovq"
			.split("\\|");
		for (int i = 0; i < SSEStore.length; i++)
			instructionClassTable.put(SSEStore[i], InstructionClass.SSE_STORE);

		String SSEFloat[] = "addps|vaddps|addpd|vaddpd|subps|vsubps|subpd|vsubpd|andps|vandps|andpd|vandpd|andnps|vandnps|andnpd|vandnpd|orps|vorps|orpd|vorpd|xorps|vxorps|xorpd|vxorpd|roundps|roundpd|unpckhpd|vunpckhpd|unpckhps|vunpckhps|unpcklpd|vunpcklpd|unpcklps|vunpcklps|vblendmpd|vblendmps|vbroadcastss|vbroadcastsd|vbroadcastf32x2|vbroadcastf32x4|vbroadcastf32x8|vbroadcastf64x2|vbroadcastf64x4|vcompresspd|vcompressps|vcvtpd2qq|vcvtpd2udq|vcvtpd2uqq|vcvtph2ps|vcvtps2ph|vcvtps2qq|vcvtps2udq|vcvtps2uqq|vcvtqq2pd|vcvtqq2ps|vcvttpd2qq|vcvttpd2udq|vcvttpd2uqq|vcvttps2qq|vcvttps2udq|vcvttps2uqq|vcvtudq2pd|vcvtudq2ps|vcvtuqq2pd|vcvtuqq2ps|vexpandpd|vexpandps|vextractf128|vextractf32x4|vextractf64x2|vextractf32x8|vextractf64x4|vfixupimmpd|vfixupimmps|vfpclasspd|vfpclassps|cvtpd2dq|cvtpd2pi|cvtps2dq|cvttpd2dq|cvttpd2pi|cvttps2dq|cvttps2pi|movmskps|vmovmskps|movmskpd|vmovmskpd|cvtdq2pd|cvtpi2pd|cvtdq2ps|cvttdq2pd|cvttpi2pd|cvttdq2ps|cvttpi2ps|cvtpd2ps|cvtps2pd|vcvtpd2ps|vcvtps2pd|vgetexppd|vgetexpps|vgetmantpd|vgetmantps|vinsertf128|vinsertf32x4|vinsertf64x2|vinsertf32x8|vinsertf64x4|vmaskmovps|vmaskmovpd|movapd|vmovapd|movaps|vmovaps|vmovddup|movhps|movhpd|movlps|movlpd|movhlps|movlhps|movmskpd|vmovmskpd|movmskps|vmovmskps|movntpd|vmovntpd|movntps|vmovntps|movdq2q|movq2dq|movshdup|vmovshdup|movsldup|vmovsldup|movupd|vmovupd|movups|vmovups|vgatherqpd|vgatherdpd|vgatherqps|vgatherdps|vmovhpd|vmovlps|vmovlpd|vmovhps|vmovhlps|vmovlhps|vpblendd|vperm2f128|vpermi2ps|vpermi2pd|vpermilpd|vpermilps|vpermpd|vpermps|vpermt2ps|vpermt2pd|vrangepd|vrangeps|vrcp14pd|vrcp14ps|vreducepd|vreduceps|vrndscalepd|vrndscaleps|vscalefps|vscalefpd|vscatterdps|vscatterdpd|vscatterqps|vscatterqpd|addsubpd|addsubps|blendpd|blendps|blendvpd|blendvps|cmppd|cmpps|vcmppd|vcmpps|dppd|dpps|vdppd|vdpps|extractps|vextractps|haddpd|vhaddpd|haddps|vhaddps|hsubps|vhsubps|hsubpd|vhsubpd|insertps|vinsertps|maxpd|vmaxpd|maxps|vmaxps|minpd|vminpd|minps|vminps".split("\\|");
		for (int i = 0; i < SSEFloat.length; i++)
			instructionClassTable.put(SSEFloat[i], InstructionClass.SSE_FLOAT);
		
		String SSEFloatMulAdd[] = "vfmadd132pd|vfmadd213pd|vfmadd231pd|vfmadd132ps|vfmadd213ps|vfmadd231ps|vfmaddsub132pd|vfmaddsub213pd|vfmaddsub231pd|vfmaddsub132ps|vfmaddsub213ps|vfmaddsub231ps|vfmsub132pd|vfmsub213pd|vfmsub231pd|vfmsub132ps|vfmsub213ps|vfmsub231ps|vfmsubadd132pd|vfmsubadd213pd|vfmsubadd231pd|vfmsubadd132ps|vfmsubadd213ps|vfmsubadd231ps|vfnmadd132pd|vfnmadd213pd|vfnmadd231pd|vfnmadd132ps|vfnmadd213ps|vfnmadd231ps|vfnmsub132pd|vfnmsub213pd|vfnmsub231pd|vfnmsub132ps|vfnmsub213ps|vfnmsub231ps|vfmaddpd|vfmaddps|vfmsubpd|vfmsubps|vfnmaddpd|vfnmaddps|vfnmsubpd|vfnmsubps|vfmsubaddpd|vfmsubaddps|vfmaddsubps|vfmaddsubps".split("\\|");
		                           
		for (int i = 0; i < SSEFloatMulAdd.length; i++)
			instructionClassTable.put(SSEFloatMulAdd[i], InstructionClass.SSE_FMA);

		String SSEFloatCompare[] = "vtestps|vtestpd".split("\\|");
		for (int i = 0; i < SSEFloatCompare.length; i++)
			instructionClassTable.put(SSEFloatCompare[i], InstructionClass.SSE_FLOAT_COMPARE);
		
		String SSEFloatMul[] = "mulps|vmulps|mulpd|vmulpd"
			.split("\\|");
		for (int i = 0; i < SSEFloatMul.length; i++)
			instructionClassTable.put(SSEFloatMul[i], InstructionClass.SSE_FLOAT_MUL);

		String SSEFloatDiv[] = "divps|vdivps|divpd|vdivpd".split("\\|");
		for (int i = 0; i < SSEFloatDiv.length; i++)
			instructionClassTable.put(SSEFloatDiv[i], InstructionClass.SSE_FLOAT_DIV);

		String FloatALU[] = "addss|addsd|subss|subsd|vaddss|vaddsd|vsubss|vsubsd|roundss|roundsd|vfixupimmsd|vfixupimmss|vfpclasssd|vfpclassss|vgetexpsd|vgetexpss|vgetmantss|vgetmantsd|vrangesd|vrangess|vrcp14sd|vrcp14ss|vreducesd|vreducess|vrndscalesd|vrndscaless|vscalefsd|vscalefss|minsd|vminsd|minss|vminss|cvtsi2ss|cvtsi2sd|cvtusi2ss|cvtusi2sd|cvtss2si|cvtsd2si|cvtss2usi|cvtsd2usi|cvttsi2ss|cvttsi2sd|cvttusi2ss|cvttusi2sd|cvttss2si|cvttsd2si|cvttss2usi|cvttsd2usi|vcvtsi2ss|vcvtsi2sd|vcvtusi2ss|vcvtusi2sd|vcvtss2si|vcvtsd2si|vcvtss2usi|vcvtsd2usi|vcvttsi2ss|vcvttsi2sd|vcvttusi2ss|vcvttusi2sd|vcvttss2si|vcvttsd2si|vcvttss2usi|vcvttsd2usi|cmpeqsd|vcmpeqsd|cmpltsd|vcmpltsd|cmplesd|vcmplesd|cmpunordsd|vcmpunordsd|cmpneqsd|vcmpneqsd|cmpnltsd|vcmpnltsd|cmpnlesd|vcmpnlesd|cmpordsd|vcmpordsd|vcmpeq_uqsd|vcmpngesd|vcmpngtsd|vcmpfalsesd|vcmpneq_oqsd|vcmpgesd|vcmpgtsd|vcmptruesd|vcmpeq_ossd|vcmplt_oqsd|vcmple_oqsd|vcmpunord_ssd|vcmpneq_ussd|vcmpnlt_uqsd|vcmpnle_uqsd|vcmpord_ssd|vcmpeq_ussd|vcmpnge_uqsd|vcmpngt_uqsd|vcmpfalse_ossd|vcmpneq_ossd|vcmpge_oqsd|vcmpgt_oqsd|vcmptrue_ussd|cmpeqss|vcmpeqss|cmpltss|vcmpltss|cmpless|vcmpless|cmpunordss|vcmpunordss|cmpneqss|vcmpneqss|cmpnltss|vcmpnltss|cmpnless|vcmpnless|cmpordss|vcmpordss|vcmpeq_uqss|vcmpngess|vcmpngtss|vcmpfalsess|vcmpneq_oqss|vcmpgess|vcmpgtss|vcmptruess|vcmpeq_osss|vcmplt_oqss|vcmple_oqss|vcmpunord_sss|vcmpneq_usss|vcmpnlt_uqss|vcmpnle_uqss|vcmpord_sss|vcmpeq_usss|vcmpnge_uqss|vcmpngt_uqss|vcmpfalse_osss|vcmpneq_osss|vcmpge_oqss|vcmpgt_oqss|vcmptrue_usss|maxsd|vmaxsd|maxss|vmaxss|minsd|vminsd|minss|vminss|cvtsd2ss|vcvtsd2ss|cvtss2sd|vcvtss2sd|cmpsd|vcmpsd".split("\\|");
		for (int i = 0; i < FloatALU.length; i++)
			instructionClassTable.put(FloatALU[i], InstructionClass.FLOAT_ALU);

		String FloatMulAdd[] = "vfmadd132sd|vfmadd213sd|vfmadd231sd|vfmadd132ss|vfmadd213ss|vfmadd231ss|vfmsub132sd|vfmsub213sd|vfmsub231sd|vfmsub132ss|vfmsub213ss|vfmsub231ss|vfnmadd132sd|vfnmadd213sd|vfnmadd231sd|vfnmadd132ss|vfnmadd213ss|vfnmadd231ss|vfnmsub132sd|vfnmsub213sd|vfnmsub231sd|vfnmsub132ss|vfnmsub213ss|vfnmsub231ss|vfmaddsd|vfmaddss|vfmsubsd|vfmsubss|vfnmaddsd|vfnmaddss|vfnmsubsd|vfnmsubss".split("\\|");
		for (int i = 0; i < FloatMulAdd.length; i++)
			instructionClassTable.put(FloatMulAdd[i], InstructionClass.FMA);

		String FloatCompare[] = "ucomiss|ucomisd|comisd|comiss|vucomiss|vucomisd|vcomisd|vcomiss".split("\\|");
		for (int i = 0; i < FloatCompare.length; i++)
			instructionClassTable.put(FloatCompare[i], InstructionClass.FLOAT_COMPARE);
		
		String FloatMul[] = "mulss|mulsd|vmulss|vmulsd".split("\\|");
		for (int i = 0; i < FloatMul.length; i++)
			instructionClassTable.put(FloatMul[i], InstructionClass.FLOAT_MUL);

		String FloatDiv[] = "divss|divsd|vdivsd|vdivss".split("\\|");
		for (int i = 0; i < FloatDiv.length; i++)
			instructionClassTable.put(FloatDiv[i], InstructionClass.FLOAT_DIV);

		String LdMXCSR[] = "ldmxcsr|vldmxcsr|fxrstor".split("\\|");
		for (int i = 0; i < LdMXCSR.length; i++)
			instructionClassTable.put(LdMXCSR[i], InstructionClass.LOAD_MXCSR);

		String StMXCSR[] = "stmxcsr|vstmxcsr|fxsave".split("\\|");
		for (int i = 0; i < StMXCSR.length; i++)
			instructionClassTable.put(StMXCSR[i], InstructionClass.STORE_MXCSR);

		String Sqrt[] = "sqrtss|sqrtsd|sqrtps|sqrtpd|vsqrtss|vsqrtsd|vsqrtps|vsqrtpd|vrsqrt14pd|vrsqrt14ps|vrsqrt14ss|vrsqrt14sd|rsqrtps|rsqrtss".split("\\|");
		for (int i = 0; i < Sqrt.length; i++)
			instructionClassTable.put(Sqrt[i], InstructionClass.SQRT);

		String floatingPointLoadConstant[] = "fld1|fldz|fldl2t|fldl2e|fldpi|fldlg2|fldln2"
				.split("\\|");
		for (int i = 0; i < floatingPointLoadConstant.length; i++)
			instructionClassTable.put(floatingPointLoadConstant[i],
					InstructionClass.FLOATING_POINT_LOAD_CONSTANT);

		String floatingPointLoad[] = "fld|fild".split("\\|");
		for (int i = 0; i < floatingPointLoad.length; i++)
			instructionClassTable.put(floatingPointLoad[i],
					InstructionClass.FLOATING_POINT_LOAD);

		String floatingPointStore[] = "fst|fstp|fist|fistp".split("\\|");
		for (int i = 0; i < floatingPointStore.length; i++)
			instructionClassTable.put(floatingPointStore[i],
					InstructionClass.FLOATING_POINT_STORE);

		String floatingPointMultiplication[] = "fmul|fmulp|fimul|fimulp"
				.split("\\|");
		for (int i = 0; i < floatingPointMultiplication.length; i++)
			instructionClassTable.put(floatingPointMultiplication[i],
					InstructionClass.FLOATING_POINT_MULTIPLICATION);

		String floatingPointDivision[] = "fdiv|fdivp|fidiv|fidivp|fdivr|fdivrp".split("\\|");
		for (int i = 0; i < floatingPointDivision.length; i++)
			instructionClassTable.put(floatingPointDivision[i],
					InstructionClass.FLOATING_POINT_DIVISION);

		String floatingPointALU[] = "fadd|faddp|fiadd|fiaddp|fsub|fsubp|fsubr|fsubrp|fisub|fisubr|fisubrp"
				.split("\\|");
		for (int i = 0; i < floatingPointALU.length; i++)
			instructionClassTable.put(floatingPointALU[i],
					InstructionClass.FLOATING_POINT_ALU);

		// TODO : look out for floating point operations that require a
		// single operand which is source as well as destination
		String floatingPointNoOperandALU[] = "fabs|fchs|frndint".split("\\|");
		for (int i = 0; i < floatingPointNoOperandALU.length; i++)
			instructionClassTable.put(floatingPointNoOperandALU[i],
					InstructionClass.FLOATING_POINT_NO_OPERAND_ALU);

		String floatingPointExchange[] = "fxch".split("\\|");
		for (int i = 0; i < floatingPointExchange.length; i++)
			instructionClassTable.put(floatingPointExchange[i],
					InstructionClass.FLOATING_POINT_EXCHANGE);

		String floatingPointComplexOperation[] = "fsqrt".split("\\|");
		for (int i = 0; i < floatingPointComplexOperation.length; i++)
			instructionClassTable.put(floatingPointComplexOperation[i],
					InstructionClass.FLOATING_POINT_COMPLEX_OPERATION);
		
		// TODO Lock and repeat are not yet handled
		String lock[] = "lock".split("\\|");
		for (int i = 0; i < lock.length; i++)
			instructionClassTable.put(lock[i], InstructionClass.LOCK);

		String repeat[] = "rep|repe|repne|repz|repnz".split("\\|");
		for (int i = 0; i < repeat.length; i++)
			instructionClassTable.put(repeat[i], InstructionClass.REPEAT);

		String FUCompare[] = "fcom|fcomp|fcompp|fucom|fucomp|fucompp|fcomi|fcomip|fucomi|fucomip".split("\\|");
		for(int i=0; i < FUCompare.length; i++)
			instructionClassTable.put(FUCompare[i], 
					InstructionClass.FLOATING_POINT_COMPARE);
		
		String FloatingPointConditionalMove[] = "fcmovb|fcmove|fcmovbe|fcmovu|fcmovnb|fcmovne|fcmovnbe|fcmovnu".split("\\|");
		for(int i=0; i < FloatingPointConditionalMove.length; i++)
			instructionClassTable.put(FloatingPointConditionalMove[i], 
					InstructionClass.FLOATING_POINT_CONDITIONAL_MOVE);
		
		String Leave[]="leave".split("\\|");
		for(int i=0; i<Leave.length; i++)
			instructionClassTable.put(Leave[i],
					InstructionClass.LEAVE);
		
		String FloatingPointLoadControlWord[] = "fldcw".split("\\|");
		for(int i=0; i<FloatingPointLoadControlWord.length; i++)
			instructionClassTable.put(FloatingPointLoadControlWord[i],
					InstructionClass.FLOATING_POINT_LOAD_CONTROL_WORD);
		
		String FloatingPointStoreControlWord[] = "fstcw|fnstcw".split("\\|");
		for(int i=0; i<FloatingPointStoreControlWord.length; i++)
			instructionClassTable.put(FloatingPointStoreControlWord[i],
					InstructionClass.FLOATING_POINT_STORE_CONTROL_WORD);
		
		String FloatingPointExamine[] = "fxam".split("\\|");
		for(int i=0; i<FloatingPointExamine.length; i++)
			instructionClassTable.put(FloatingPointExamine[i],
					InstructionClass.FLOATING_POINT_EXAMINE);
		
		String CPUID[] = "cpuid".split("\\|");
		for(int i=0; i<CPUID.length; i++)
			instructionClassTable.put(CPUID[i],
					InstructionClass.CPUID);
		
		String InterruptReturn[] = "iret|iretd|iretq|iretw".split("\\|");
		for(int i=0; i<InterruptReturn.length; i++)
			instructionClassTable.put(InterruptReturn[i],
					InstructionClass.INTERRUPT_RETURN);
		
		String MFence[] = "mfence".split("\\|");
		for(int i=0; i<MFence.length; i++)
			instructionClassTable.put(MFence[i],
					InstructionClass.MFENCE);
		
		String SFence[] = "sfence".split("\\|");
		for(int i=0; i<SFence.length; i++)
			instructionClassTable.put(SFence[i],
					InstructionClass.SFENCE);
		
		String LFence[] = "lfence".split("\\|");
		for(int i=0; i<LFence.length; i++)
			instructionClassTable.put(LFence[i],
					InstructionClass.LFENCE);
		
		String ReadPrefetchL1[] = "prefetcht0|prefetchnta".split("\\|");
		for(int i=0; i<ReadPrefetchL1.length; i++)
			instructionClassTable.put(ReadPrefetchL1[i],
					InstructionClass.READ_PREFETCH_L1);
		
		String ReadPrefetchL2[] = "prefetcht1".split("\\|");
		for(int i=0; i<ReadPrefetchL2.length; i++)
			instructionClassTable.put(ReadPrefetchL2[i],
					InstructionClass.READ_PREFETCH_L2);
		
		String ReadPrefetchL3[] = "prefetcht2".split("\\|");
		for(int i=0; i<ReadPrefetchL3.length; i++)
			instructionClassTable.put(ReadPrefetchL3[i],
					InstructionClass.READ_PREFETCH_L3);
		
		String WritePrefetch[] = "prefetchw".split("\\|");
		for(int i=0; i<WritePrefetch.length; i++)
			instructionClassTable.put(WritePrefetch[i],
					InstructionClass.WRITE_PREFETCH);
		
		
		
		/*
		 * TODO cache directives like flush, no-allocate
		 * atomic memory operations
		 * input and output from/ to ports
		 * interrupts
		 * mask modifications like kaddw
		 * float flags?
		 */
	}

	public static InstructionClass getInstructionClass(String operation) {
		if (instructionClassTable == null)
			createInstructionClassTable();

		if (operation == null)
			return InstructionClass.INVALID;

		InstructionClass instructionClass;
		instructionClass = instructionClassTable.get(operation);

		//unhandled x86 instruction type
		if(instructionClass == null && operation.compareTo("ud2") != 0)
			System.out.println(operation);
		
		if (instructionClass == null)
			return InstructionClass.INVALID;
		else
			return instructionClass;
	}

	public static X86StaticInstructionHandler getInstructionClassHandler(
			InstructionClass instructionClass) {

		if (instructionClassHandlerTable == null)
			createInstructionClassHandlerTable();

		if (instructionClass == InstructionClass.INVALID)
			return null;

		X86StaticInstructionHandler handler;
		handler = instructionClassHandlerTable.get(instructionClass);

		return handler;
	}
}
