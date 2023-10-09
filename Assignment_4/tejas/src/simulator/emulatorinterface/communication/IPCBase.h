/*
 *  Here is the base class for the IPC mechanisms. Any new IPC mechanism should implement the
 * declared virtual functions. Also any variable common or independent of IPC mechanism should
 * be declared here
 */

#ifndef	H_include_IPC
#define	H_include_IPC

#include <stdint.h>
#include <iostream>
#include "common.h"

// This must be equal to the MAXNUMTHREADS*EMUTHREADS in IPCBase.java file. This is
// important so that we attach to the same sized memory segment
//#define MaxNumThreads	(64)
#define MaxThreads (10000)

namespace IPC
{

class IPCBase
{
public:

	int MaxNumActiveThreads;
  void (*lock)(int);
  void (*unlock)(int);

	// Initialise buffers or other stuffs related to IPC mechanisms
	IPCBase(int maxNumActiveThreads, void (*lock)(int), void (*unlock)(int)){MaxNumActiveThreads = maxNumActiveThreads; this->lock=lock; this->unlock=unlock;}

	// Fill the packet struct when doing analysis and send to Java process. This is the
	// most important function
	virtual int analysisFn (int tid,uint64_t ip, uint64_t value, uint64_t tgt)=0;

	// Fill the packet struct when doing analysis and send to Java process. This is the
	// most important function
	virtual int analysisFnAssembly (int tid,uint64_t ip, uint64_t value, char *asmString)=0;

	// Things to be done when a thread is started in PIN/ application
	virtual void onThread_start (int tid)=0;

	// Things to be done when a thread is finished in PIN/ application
	virtual int onThread_finish (int tid, long numCISC)=0;

	// Things to be done when subset simulation is finished in PIN/ application
	virtual int onSubset_finish (int tid, long numCISC)=0;

	virtual bool isSubsetsimCompleted(void)=0;
	virtual bool setSubsetsimComplete(bool val)=0;

	// Deallocate any memory, delete any buffers, shared memory, semaphores
	virtual bool unload ()=0;

	virtual ~IPCBase() {}
};

}

#endif
