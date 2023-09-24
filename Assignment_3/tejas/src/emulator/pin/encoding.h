// Do not change these encodings, change Encoding.java too, if needed.

#define THREADCOMPLETE -1
#define SUBSETSIMCOMPLETE -2

#define MEMREAD	2
#define MEMWRITE	3
#define	TAKEN	4
#define	NOTTAKEN	5
#define	REGREAD	6
#define	REGWRITE	7
#define TIMER	8

// these are for function entry, For function exit x+1 will be used
#define	BCAST	10
#define	SIGNAL	12
#define	LOCK	14
#define	UNLOCK	16
#define	JOIN	18
#define	CONDWAIT	20
#define	BARRIERWAIT	22
#define BARRIERINIT 26
#define ASSEMBLY 27
#define INSTRUCTION 28
#define FUNC_START 29
#define INTERRUPT 30
#define PROCESS_SWITCH 31
#define DOM_SWITCH 32
#define CPL_SWITCH 34

#define PARENT_SPAWN 35
#define CHILD_START 36

#define PREDICATE_INSN_NOT_EXECUTED 37


const char* findType(int type);
