#include <iostream>
#include <fstream>
#include "pin.H"
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/shm.h>
#include <cstdlib>
#include <cstring>
#include <sched.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <time.h>
#include <sys/timeb.h>

#include "IPCBase.h"
#include "shmem.h"

#include "encoding.h"


#ifdef _LP64
#define MASK 0xffffffffffffffff
#else
#define MASK 0x00000000ffffffff
#endif

// Defining  command line arguments
KNOB<UINT64>   KnobLong(KNOB_MODE_WRITEONCE,       "pintool",
		"map", "1", "Maps");

PIN_LOCK lock;
INT32 numThreads = 0;
UINT64 checkSum = 0;
IPC::IPCBase *tst;


// needs -lrt (real-time lib)
// 1970-01-01 epoch UTC time, 1 nanosecond resolution
uint64_t ClockGetTime()
{
    timespec ts;
    clock_gettime(CLOCK_REALTIME, &ts);
    return (uint64_t)ts.tv_sec * 1000000000LL + (uint64_t)ts.tv_nsec;
}

#define cmp(a)	(rtn_name->find(a) != string::npos)


VOID ThreadStart(THREADID threadid, CONTEXT *ctxt, INT32 flags, VOID *v)
{
	GetLock(&lock, threadid+1);
	numThreads++;
	printf("threads till now %d\n",numThreads);
	fflush(stdout);
	ReleaseLock(&lock);
	ASSERT(numThreads <= MaxNumThreads, "Maximum number of threads exceeded\n");

	/*tst->onThread_start(threadid);*/
}

VOID ThreadFini(THREADID tid,const CONTEXT *ctxt, INT32 flags, VOID *v)
{
/*	while (tst->onThread_finish(tid)==-1) {
		PIN_Yield();
	}*/
}

//Pass a memory read record
VOID RecordMemRead(THREADID tid,VOID * ip, VOID * addr)
{
	checkSum+=MEMREAD;
/*	uint64_t nip = MASK & (uint64_t)ip;
	uint64_t naddr = MASK & (uint64_t)addr;
	while (tst->analysisFn(tid,nip,MEMREAD,naddr)== -1) {
		PIN_Yield();
	}*/
}

// Pass a memory write record
VOID RecordMemWrite(THREADID tid,VOID * ip, VOID * addr)
{
	checkSum+=MEMWRITE;
/*	uint64_t nip = MASK & (uint64_t)ip;
	uint64_t naddr = MASK & (uint64_t)addr;
	while(tst->analysisFn(tid,nip,MEMWRITE,naddr)== -1) {
		PIN_Yield();
	}*/
}

VOID BrnFun(THREADID tid,ADDRINT tadr,BOOL taken,VOID *ip)
{
	checkSum = taken ? TAKEN : NOTTAKEN;
/*	uint64_t nip = MASK & (uint64_t)ip;
	uint64_t ntadr = MASK & (uint64_t)tadr;
	if (taken) {
		while (tst->analysisFn(tid,nip,TAKEN,ntadr)==-1) {
			PIN_Yield();
		}
	}
	else {
		while (tst->analysisFn(tid,nip,NOTTAKEN,ntadr)==-1) {
			PIN_Yield();
		}
	}*/
}

//VOID FunEntry(ADDRINT first_arg, const string * name, THREADID threadid)
VOID FunEntry(ADDRINT first_arg, UINT32 encode, THREADID threadid)
{


    GetLock(&lock, threadid+1);
    printf("%d enters in %d with first arg %p    --%llu \n",threadid,encode,(void *)first_arg,ClockGetTime());
    //TraceFile << threadid <<" enter "<< *name << "(" << first_arg << ")" << endl;
    ReleaseLock(&lock);


	checkSum+=encode;

	/*uint64_t uarg = MASK & (uint64_t)first_arg;
	while (tst->analysisFn(threadid,ClockGetTime(),encode,uarg)== -1) {
		PIN_Yield();
	}*/
}

VOID FunExit(ADDRINT *ret, UINT32 encode, THREADID threadid)
{


    GetLock(&lock, threadid+1);
    printf("%d exits from %d with return value %p   --%llu\n",threadid,encode,(void *)ret,ClockGetTime());
    //TraceFile << threadid <<" exit "<< *name << " returns " << ret << endl;
    ReleaseLock(&lock);


	checkSum+=encode;
/*	uint64_t uret = MASK & (uint64_t)ret;
	while (tst->analysisFn(threadid,ClockGetTime(),encode,uret)== -1) {
		PIN_Yield();
	}*/

}


// Pin calls this function every time a new instruction is encountered
VOID Instruction(INS ins, VOID *v)
{
	UINT32 memOperands = INS_MemoryOperandCount(ins);

	if (INS_IsBranchOrCall(ins))//INS_IsIndirectBranchOrCall(ins))
	{
		INS_InsertCall(ins, IPOINT_BEFORE, (AFUNPTR)BrnFun, IARG_THREAD_ID, IARG_BRANCH_TARGET_ADDR, IARG_BRANCH_TAKEN, IARG_INST_PTR, IARG_END);
	}

	// Iterate over each memory operand of the instruction.
	for (UINT32 memOp = 0; memOp < memOperands; memOp++)
	{
		if (INS_MemoryOperandIsRead(ins, memOp))
		{
			INS_InsertPredicatedCall(
					ins, IPOINT_BEFORE, (AFUNPTR)RecordMemRead,
					IARG_THREAD_ID,
					IARG_INST_PTR,
					IARG_MEMORYOP_EA, memOp,
					IARG_END);
		}
		// Note that in some architectures a single memory operand can be
		// both read and written (for instance incl (%eax) on IA-32)
		// In that case we instrument it once for read and once for write.
		if (INS_MemoryOperandIsWritten(ins, memOp))
		{
			INS_InsertPredicatedCall(
					ins, IPOINT_BEFORE, (AFUNPTR)RecordMemWrite,
					IARG_THREAD_ID,
					IARG_INST_PTR,
					IARG_MEMORYOP_EA, memOp,
					IARG_END);
		}
	}
}

//if (RTN_Valid(rtn) && RtnMatchesName(RTN_Name(rtn), name))

// This is a routine level instrumentation
VOID FlagRtn(RTN rtn, VOID* v)
{
	RTN_Open(rtn);
	const string* rtn_name = new string(RTN_Name(rtn));
	INT32 encode;

	if (cmp("pthread_cond_broadcast")) encode = BCAST;
	else if (cmp("pthread_cond_signal")) encode = SIGNAL;
	else if (cmp("pthread_mutex_lock")) encode = LOCK;
	else if (cmp("pthread_mutex_unlock_")) encode = UNLOCK; //pthread_mutex_unlock is just a wrapper
	else if (cmp("pthread_join")) encode = JOIN;
	else if (cmp("pthread_cond_wait")) encode = CONDWAIT;
	else if (cmp("pthread_barrier_wait")) encode = BARRIERWAIT;
	else encode = -1;

	if (encode != -1 && RTN_Valid(rtn)) {
		RTN_InsertCall(rtn, IPOINT_BEFORE, (AFUNPTR)FunEntry,
				IARG_FUNCARG_ENTRYPOINT_VALUE,0,
				IARG_UINT32,encode,
				IARG_THREAD_ID,
				IARG_END);

		RTN_InsertCall(rtn, IPOINT_AFTER, (AFUNPTR)FunExit,
				IARG_FUNCRET_EXITPOINT_VALUE,
				IARG_UINT32,encode+1,
				IARG_THREAD_ID,
				IARG_END);

	}
	RTN_Close(rtn);
}


// This function is called when the application exits
VOID Fini(INT32 code, VOID *v)
{
	printf("checkSum is %lld\n",checkSum);
	tst->unload();
}

/* ===================================================================== */
/* Print Help Message                                                    */
/* ===================================================================== */

INT32 Usage()
{
	cerr << "This tool instruments the benchmarks" << endl;
	cerr << endl << KNOB_BASE::StringKnobSummary() << endl;
	return -1;
}

/* ===================================================================== */
/* Main                                                                  */
/* ===================================================================== */

// argc, argv are the entire command line, including pin -t <toolname> -- ...
int main(int argc, char * argv[])
{

	UINT64 mask = KnobLong;
	printf("mask for pin %lld\n", mask);
	fflush(stdout);
	if (sched_setaffinity(0, sizeof(mask), (cpu_set_t *)&mask) <0) {
		perror("sched_setaffinity");
	}

	PIN_InitSymbols();
	// Initialize pin
	if (PIN_Init(argc, argv)) return Usage();

	tst = new IPC::Shm ();

	PIN_AddThreadStartFunction(ThreadStart, 0);

	// Register Instruction to be called to instrument instructions
	INS_AddInstrumentFunction(Instruction, 0);

	// Register ThreadFini to be called when a thread exits
	PIN_AddThreadFiniFunction(ThreadFini, 0);

	// Register FlagRtn whenever you get a routine
	RTN_AddInstrumentFunction(FlagRtn, 0);

	// Register Fini to be called when the application exits
	PIN_AddFiniFunction(Fini, 0);

	// Start the program, never returns
	PIN_StartProgram();

	return 0;
}
