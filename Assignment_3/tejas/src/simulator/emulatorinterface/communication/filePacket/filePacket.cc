#include "filePacket.h"
#include <sys/stat.h>
#ifdef _WIN32
	#include <Windows.h>
	#include <stdlib.h>
#else
	#include <sys/ipc.h>
	#include <sys/sem.h>
	#include <sys/shm.h>
	#include <sys/msg.h>
	#include <sys/syscall.h>
	#include <pthread.h>
	#include <unistd.h>
#endif
#include <sys/types.h>
#include <errno.h>

namespace IPC
{

char *basefileNamePublic;

FilePacket::FilePacket(int maxNumActiveThreads, const char *baseFileName, void (*lock)(int), void (*unlock)(int)) : IPCBase(maxNumActiveThreads, lock, unlock)
{
	basefileNamePublic = new char[1000];
	strcpy(basefileNamePublic, baseFileName);

	this->maxNumActiveThreads = maxNumActiveThreads;
	isSubsetsimComplete = false;

	// If there is an existing file which can be overlapped due to the execution of pintool,
	// flag an error and exit
	for(int tid=0; tid<maxNumActiveThreads; tid++) {
		char fileName[1000];
		sprintf(fileName, "%s_%d.gz", baseFileName, tid);
		FILE *f = fopen(fileName, "r");
		if(f!=NULL) {
			printf("Cannot overwrite an existing trace file : %s\n", fileName);
			exit(1);
		}
	}
	
	getFiles();
	
	// Initialize the files array
	for(int tid=0; tid<maxNumActiveThreads; tid++) {
		files[tid] = NULL;
	}
}

void
FilePacket::getFiles(){

#ifndef _WIN32
	files = new gzFile[maxNumActiveThreads];
#else
	files = new FILE*[maxNumActiveThreads];
#endif
}

void
FilePacket::printToFileIpValAddr(int tid,uint64_t ip, uint64_t val, uint64_t addr){
	#ifndef _WIN32
		gzprintf(files[tid], "%ld %ld %ld\n", ip, val, addr);
	#else
		fprintf(files[tid],"%ld %ld %ld\n", ip, val, addr);
		fflush(files[tid]);
	#endif
}

void
FilePacket::printToFileIpValAsmString(int tid,uint64_t ip, uint64_t val, char *asmString){
	#ifndef _WIN32
		gzprintf(files[tid], "%ld %ld %s\n", ip, val, asmString);
	#else
		fprintf(files[tid], "%ld %ld %s\n", ip, val, asmString);
		fflush(files[tid]);
	#endif
}

void 
FilePacket::createPacketFileForThread(int tid)
{
	#ifndef _WIN32
		if(files[tid]==NULL) {
			char fileName[1000];
			sprintf(fileName, "%s_%d.gz", basefileNamePublic, tid);
			
			FILE *fd = fopen(fileName, "w");
			if(fd==NULL) {
				perror("error in creating file !! ");
				exit(1);
			}		
			files[tid] = gzdopen(fileno(fd), "w6");
		}	
	#else
		if(files[tid]==NULL) {
			char fileName[1000];
			sprintf(fileName, "%s_%d", basefileNamePublic, tid);

			files[tid] = fopen(fileName, "w");
		}
	#endif
}


/* If local queue is full, write to the shared memory and then write to localQueue.
 * else just write at localQueue at the appropriate index i.e. at 'in'
 */
int
FilePacket::analysisFn (int tid,uint64_t ip, uint64_t val, uint64_t addr)
{
	(*lock)(tid);
	createPacketFileForThread(tid);	
	if(val==INSTRUCTION) {
		printf("Error in writing INSTRUCTION packet to trace file !!");
		exit(1);
	}
	printToFileIpValAddr(tid, ip, val, addr);	
	(*unlock)(tid);
	return 0;
}


int
FilePacket::analysisFnAssembly (int tid,uint64_t ip, uint64_t val, char *asmString)
{
	(*lock)(tid);
	createPacketFileForThread(tid);	

	printToFileIpValAsmString(tid, ip, val, asmString);
	(*unlock)(tid);
	return 0;
}

void
FilePacket::onThread_start (int tid)
{
	fflush(stdout);
}

int
FilePacket::onThread_finish (int tid, long numCISC)
{
	return 0;
}

int FilePacket::onSubset_finish (int tid, long numCISC)
{
	while(analysisFn(tid, 0, SUBSETSIMCOMPLETE, numCISC)==-1) {
		continue;
	}
	return 0;
}

void
FilePacket::closeAllFiles() {
	printf("close all files \n"); fflush(stdout);
	printf("Number of active threads...%d\n",MaxNumActiveThreads);
	fflush(stdout);
	for(int i=0; i<MaxNumActiveThreads; i++) {
		#ifndef _WIN32
			gzclose(files[i]);
		#else
			fclose(files[i]);
		#endif
	}
}

bool
FilePacket::unload()
{
	if(files==NULL) {
		return true;
	}

	closeAllFiles();
	delete files;
}

FilePacket::~FilePacket ()
{
	if(files==NULL) {
		return;
	}

	unload();
}

bool
FilePacket::setSubsetsimComplete(bool val)
{
	isSubsetsimComplete = val;
	return isSubsetsimComplete; 
}

bool
FilePacket::isSubsetsimCompleted()
{
	return isSubsetsimComplete;
}


} // namespace IPC
