package emulatorinterface.translator.visaHandler;

import generic.OperationType;


public class VisaHandlerSelector
{
	private static DynamicInstructionHandler inValid;
	private static DynamicInstructionHandler integerALU;
	private static DynamicInstructionHandler integerMul;
	private static DynamicInstructionHandler integerDiv;
	private static DynamicInstructionHandler floatALU;
	private static DynamicInstructionHandler floatMul;
	private static DynamicInstructionHandler floatDiv;
	private static DynamicInstructionHandler integerVectorALU;
	private static DynamicInstructionHandler integerVectorMul;
	private static DynamicInstructionHandler floatVectorALU;
	private static DynamicInstructionHandler floatVectorMul;
	private static DynamicInstructionHandler FMA;
	private static DynamicInstructionHandler vectorFMA;
	private static DynamicInstructionHandler loadAGU;
	private static DynamicInstructionHandler load;
	private static DynamicInstructionHandler storeAGU;
	private static DynamicInstructionHandler store;
	private static DynamicInstructionHandler jump;
	private static DynamicInstructionHandler branch;
	private static DynamicInstructionHandler AES;
	private static DynamicInstructionHandler vectorString;
	private static DynamicInstructionHandler bitScan;
	private static DynamicInstructionHandler vectorShuffle;
	private static DynamicInstructionHandler LEA;
	private static DynamicInstructionHandler acceleratedOp;
	private static DynamicInstructionHandler nop;
	private static DynamicInstructionHandler cpuid;
	private static DynamicInstructionHandler mfence;
	private static DynamicInstructionHandler sfence;
	private static DynamicInstructionHandler lfence;
	private static DynamicInstructionHandler readprefetch;
	private static DynamicInstructionHandler writeprefetch;
	private static DynamicInstructionHandler interrupt;

	public static DynamicInstructionHandler selectHandler(OperationType operationType)
	{
		// if the handlers are not defined in the beginning, we
		// must initialise them.
		if(inValid==null)
		{
			createVisaHandlers();
		}
		
		switch(operationType)
		{
			case inValid:
				return inValid;

			case integerALU:
				return integerALU;
				
			case integerMul:
				return integerMul;
				
			case integerDiv:
				return integerDiv;

			case floatALU:
				return floatALU;
				
			case floatMul:
				return floatMul;
				
			case floatDiv:
				return floatDiv;
				
			case integerVectorALU:
				return integerVectorALU;
				
			case integerVectorMul:
				return integerVectorMul;
				
			case floatVectorALU:
				return floatVectorALU;
				
			case floatVectorMul:
				return floatVectorMul;
				
			case FMA:
				return FMA;
				
			case vectorFMA:
				return vectorFMA;
				
			case loadAGU:
				return loadAGU;
				
			case load:
				return load;
				
			case storeAGU:
				return storeAGU;
				
			case store:
				return store;
				
			case jump:
				return jump;
				
			case branch:
				return branch;
				
			case AES:
				return AES;
				
			case vectorString:
				return vectorString;
				
			case bitScan:
				return bitScan;
				
			case vectorShuffle:
				return vectorShuffle;
				
			case LEA:
				return LEA;
				
			case acceleratedOp:
				return acceleratedOp;
				
			case cpuid:
				return cpuid;
				
			case mfence:
				return mfence;
				
			case sfence:
				return sfence;
				
			case lfence:
				return lfence;
				
			case read_prefetch:
				return readprefetch;
				
			case write_prefetch:
				return writeprefetch;
				
			case nop:
				return nop;
				
			case interrupt:
				return interrupt;
				
			default:
				System.out.print("Invalid operation");
				System.exit(0);
				return null;
		}
	}

	private static void createVisaHandlers() 
	{
		inValid = new Invalid();
		integerALU = new IntegerALU();
		integerMul = new IntegerMul();
		integerDiv = new IntegerDiv();
		floatALU = new FloatALU();
		floatMul = new FloatMul();
		floatDiv = new FloatDiv();
		integerVectorALU = new IntegerVectorALU();
		integerVectorMul = new IntegerVectorMul();
		floatVectorALU = new FloatVectorALU();
		floatVectorMul = new FloatVectorMul();
		FMA = new FMA();
		vectorFMA = new VectorFMA();
		loadAGU = new LoadAGU();
		load = new Load();
		storeAGU = new StoreAGU();
		store = new Store();
		jump = new Jump();
		branch = new Branch();
		AES = new AES();
		vectorString = new VectorString();
		bitScan = new BitScan();
		vectorShuffle = new VectorShuffle();
		LEA = new LEA();
		acceleratedOp = new AcceleratedOp();
		nop = new NOP();
		cpuid = new CPUID();
		mfence = new MFence();
		sfence = new SFence();
		lfence = new LFence();
		readprefetch = new ReadPrefetch();
		writeprefetch = new WritePrefetch();
		interrupt = new Interrupt();
	}
}
