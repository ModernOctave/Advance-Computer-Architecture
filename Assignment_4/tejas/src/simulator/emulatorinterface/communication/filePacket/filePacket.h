#ifndef H_include_filePacket
#define H_include_filePacket

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../IPCBase.h"
#include "../../../../emulator/pin/encoding.h"

#ifdef _WIN32
	#include <conio.h>
#else
	#include <unistd.h>
	#include <zlib.h>
#endif

namespace IPC
{
	

	class FilePacket : public IPCBase
	{
		
		protected:
			
			int maxNumActiveThreads;
			
			// Once subset sim complete boolean variable is set, we should not write to shared memory any further.
			volatile bool isSubsetsimComplete;
			#ifndef _WIN32
				gzFile *files;
			#else
				FILE **files;
			#endif
			void createPacketFileForThread(int tid);
			void closeAllFiles();
			void getFiles();
			void printToFileIpValAsmString(int tid,uint64_t ip, uint64_t val, char *asmString);
			void printToFileIpValAddr(int tid,uint64_t ip, uint64_t val, uint64_t addr);
		public:
			
			FilePacket(int maxNumThreads, const char *baseFileName, void (*lock)(int), void (*unlock)(int));
			bool isSubsetsimCompleted(void);
			bool setSubsetsimComplete(bool val);

			int analysisFn (int tid,uint64_t ip, uint64_t value, uint64_t tgt);
			int analysisFnAssembly (int tid,uint64_t ip, uint64_t value, char *asmString);

			void onThread_start (int tid);
			int onThread_finish (int tid, long numCISC);
			int onSubset_finish (int tid, long numCISC);

			bool unload ();
			~FilePacket ();
	};
}
#endif
