package pipeline;

public enum FunctionalUnitType {

	inValid,
	integerALU,
	integerMul,
	integerDiv/*approximated as a single instruction exercising a single FU, though data sheets speak of multiple micro-ops*/,
	floatALU,
	floatMul,
	floatDiv,
	FMA,
	integerVectorALU,
	integerVectorMul,
	floatVectorALU,
	floatVectorMul,
	load,
	loadAGU,
	store,
	storeAGU,
	branch,
	AES,
	vectorString,
	bitScan,
	vectorShuffle,
	LEA,
	no_of_types
	
}
