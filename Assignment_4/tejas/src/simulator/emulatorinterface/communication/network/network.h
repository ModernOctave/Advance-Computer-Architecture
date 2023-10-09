#ifndef H_include_network
#define H_include_network

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include "../IPCBase.h"

namespace IPC
{

class network:public IPCBase
{
protected:

public:
	network(int maxNumApplicationThreads, void (*lock)(int), void (*unlock)(int));
	int analysisFn (int tid,uint64_t ip, int value, uint64_t tgt);
	void onThread_start (int tid);
	int onThread_finish (int tid);
	void sendPackets (int tid, int last);
	void get_lock(packet *map);
	void release_lock(packet *map);
	bool unload ();    
	~network();
};
}

#endif
