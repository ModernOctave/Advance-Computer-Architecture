#ifndef H_include_mmap
#define H_include_mmap

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>

#include "../IPCBase.h"

// Must ensure that this is same as in Memmap.java
#define COUNT	(1000)
#define locQ	(50)
#define FILEPATH	"pfile"

namespace IPC
{

class Mmap:public IPCBase
{
protected:
	int fd;
	// For keeping a record of various thread related variables
#define PADSIZE 28 //64-36, assuming it is on 32bit machine(as addresses are 32bit)
	struct THREAD_DATA
	{
		uint32_t tlqsize;							/* address of instruction */
		uint32_t in;								/* in pointer in the local queue */
		uint32_t out;								/* out pointer in the local queue */
		packet *map;								/* thread's mmap index pointer */
		packet *tlq;								/* XXX local queue, write in shmem when this fils */
		uint32_t prod_ptr;							/* producer pointer in the shared mem */
		uint32_t tot_prod;							/* total packets produced */
		uint64_t sum;								/* checksum */
		uint8_t _pad[PADSIZE];						/* to handle false sharing */
	};

public:
	THREAD_DATA tldata[MaxThreads];
	Mmap(int maxNumActiveThreads, void (*lock)(int), void (*unlock)(int));
	int analysisFn (int tid,uint64_t ip, int value, uint64_t tgt);
	void onThread_start (int tid);
	int onThread_finish (int tid);
	int filewrite(int tid, int last);
	void get_lock(packet *map);
	void release_lock(packet *map);
	bool unload ();    
	~Mmap();
};

}

#endif
