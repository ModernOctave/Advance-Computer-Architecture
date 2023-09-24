package pipeline;

import generic.OperationType;

/**
 * given an operation type, what is the functional unit required?
 */
public class OpTypeToFUTypeMapping {
	
	public static FunctionalUnitType[] intALUFUs = {FunctionalUnitType.integerALU};
	public static FunctionalUnitType[] intMulFUs = {FunctionalUnitType.integerMul};
	public static FunctionalUnitType[] intDivFUs = {FunctionalUnitType.integerDiv};
	public static FunctionalUnitType[] floatALUFUs = {FunctionalUnitType.FMA, FunctionalUnitType.floatALU};
	public static FunctionalUnitType[] floatMulFUs = {FunctionalUnitType.FMA, FunctionalUnitType.floatMul};
	public static FunctionalUnitType[] floatDivFUs = {FunctionalUnitType.floatDiv};
	public static FunctionalUnitType[] intVectorALUFUs = {FunctionalUnitType.integerVectorALU};
	public static FunctionalUnitType[] intVectorMulFUs = {FunctionalUnitType.integerVectorMul};
	public static FunctionalUnitType[] floatVectorALUFUs = {FunctionalUnitType.FMA, FunctionalUnitType.floatVectorALU};
	public static FunctionalUnitType[] floatVectorMulFUs = {FunctionalUnitType.FMA, FunctionalUnitType.floatVectorMul};
	public static FunctionalUnitType[] FMAFUs = {FunctionalUnitType.FMA};
	public static FunctionalUnitType[] VectorFMAFUs = {FunctionalUnitType.FMA};
	public static FunctionalUnitType[] loadFUs = {FunctionalUnitType.load};
	public static FunctionalUnitType[] loadAGUFUs = {FunctionalUnitType.loadAGU};
	public static FunctionalUnitType[] storeFUs = {FunctionalUnitType.store};
	public static FunctionalUnitType[] storeAGUFUs = {FunctionalUnitType.storeAGU};
	public static FunctionalUnitType[] branchFUs = {FunctionalUnitType.branch};
	public static FunctionalUnitType[] AESFUs = {FunctionalUnitType.AES};
	public static FunctionalUnitType[] vectorStringFUs = {FunctionalUnitType.vectorString};
	public static FunctionalUnitType[] bitScanFUs = {FunctionalUnitType.bitScan};
	public static FunctionalUnitType[] vectorShuffleFUs = {FunctionalUnitType.vectorShuffle};
	public static FunctionalUnitType[] LEAFUs = {FunctionalUnitType.LEA};
	public static FunctionalUnitType[] invalidFUs = {FunctionalUnitType.inValid};
	
	
	public static FunctionalUnitType[] getFUType(OperationType opType)
	{
		switch(opType)
		{
			case integerALU	:	{
									return intALUFUs;
								}
			case integerMul	:	{
									return intMulFUs;
								}
			case integerDiv	:	{
									return intDivFUs;
								}
			case floatALU	:	{
									return floatALUFUs;
								}
			case floatMul	:	{
									return floatMulFUs;
								}
			case floatDiv	:	{
									return floatDivFUs;
								}
			case integerVectorALU	:	{
									return intVectorALUFUs;
								}
			case integerVectorMul	:	{
									return intVectorMulFUs;
								}
			case floatVectorALU	:	{
									return floatVectorALUFUs;
								}
			case floatVectorMul	:	{
									return floatVectorMulFUs;
								}
			case FMA :			{
									return FMAFUs;
								}
			case vectorFMA :	{
									return VectorFMAFUs;
								}
			case read_prefetch:
			case load		:	{
									return loadFUs;
								}
			case loadAGU		:	{
									return loadAGUFUs;
								}
			case write_prefetch:
			case store		:	{
									return storeFUs;
								}
			case storeAGU	:	{
									return storeAGUFUs;
								}
			case jump		:	{
									return branchFUs;
								}
			case branch		:	{
									return branchFUs;
								}
			case AES		:	{
									return AESFUs;
								}
			case vectorString		:	{
									return vectorStringFUs;
								}
			case bitScan		:	{
									return bitScanFUs;
								}
			case vectorShuffle		:	{
									return vectorShuffleFUs;
								}
			case LEA		:	{
									return LEAFUs;
								}
			default			:	{
									return invalidFUs;
								}
		}
	}

}