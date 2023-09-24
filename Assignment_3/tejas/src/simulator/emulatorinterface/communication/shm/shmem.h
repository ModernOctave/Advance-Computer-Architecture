#ifndef H_include_shmem
#define H_include_shmem

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
//#include "../IPCBase.h"
#include "../common.h"
#include "../../../../emulator/pin/encoding.h"

#ifndef _WIN32
	//#include "unistd.h"
//#else
	#include <unistd.h>
#endif

#ifdef _WIN32
namespace tejas_win{
#include <Windows.h>
}
#endif

// Must ensure that this is same as in SharedMem.java
#define COUNT	(1000)
#define locQ	(50)
#define shm_debug_queue (false)
#define MaxThreads (10000)
namespace IPC
{



class Shm// : public IPCBase
{
protected:
	#ifdef _WIN32
		tejas_win::HANDLE hMapFile;
	#else
		int shmid;										/* shared memory segment id */
	#endif										

	// Once subset sim complete boolean variable is set, we should not write to shared memory any further.
	volatile bool isSubsetsimComplete;
	int ap_bh_counter;
	

	// For keeping a record of various thread related variables
#define PADSIZE 28 //64-36, assuming it is on 32bit machine(as addresses are 32bit)
	struct THREAD_DATA
	{
		uint32_t tlqsize;							/* address of instruction */
		uint32_t in;								/* in pointer in the local queue */
		uint32_t out;								/* out pointer in the local queue */
		packet *shm;								/* thread's shared mem index pointer */
		packet *tlq;								/* local queue, write in shmem when this fils */
		uint32_t prod_ptr;							/* producer pointer in the shared mem */
		uint64_t tot_prod;							/* total packets produced */
		uint64_t sum;								/* checksum */
		uint8_t _pad[PADSIZE];						/* to handle false sharing */
//		uint32_t tid;								/* current tid running on it (implemented for shmem reuse)*/
	};

public:
	THREAD_DATA *tldata;
//	uint32_t *memMapping;
	Shm(int maxNumThreads /*, void (*lock)(int), void (*unlock)(int)*/);
	Shm(uint64_t, int maxNumThreads /*, void (*lock)(int), void (*unlock)(int)*/);
	Shm (uint32_t count,uint32_t localQueue, void (*lock)(int), void (*unlock)(int));
	bool isSubsetsimCompleted(void);
	bool setSubsetsimComplete(bool val);

	int analysisFn (int tid,uint64_t ip, uint64_t value, uint64_t tgt);
	int analysisFnAssembly (int tid,uint64_t ip, uint64_t value, char *asmString);

	void onThread_start (int tid);
	int onThread_finish (int tid, long numCISC);
	int onSubset_finish (int tid, long numCISC);
	int shmwrite (int tid, int last, long numCISC);
	void get_lock(packet *map);
	void release_lock(packet *map);
	bool unload ();
	~Shm ();

	int MaxNumActiveThreads;
	void print_thread_data(THREAD_DATA *);

};

}

#endif