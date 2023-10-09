#include "network.h"
#include "stdio.h"

namespace IPC
{

// bookkeeping variables.
network::network (int maxNumApplicationThreads, void (*lock)(int), void (*unlock)(int)) : IPCBase(maxNumApplicationThreads, lock, unlock)
{

}

int
network::analysisFn (int tid,uint64_t ip, int val, uint64_t addr)
{
	printf("analysis %d\n",val);
	return 0;
}

void
network::onThread_start (int threadid)
{
	printf("in onThread_start %d\n",threadid);
}

int
network::onThread_finish (int tid)
{
	printf("thread fini \n");
	return 0;
}


void
network::sendPackets (int tid, int last)
{
	printf("written \n");
}

bool
network::unload()
{
	printf("unlaod");
}

network::~network ()
{

}

} // namespace IPC
