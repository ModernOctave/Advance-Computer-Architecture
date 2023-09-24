/*
 *This file is included in the PIN tool and the C file of the javareader
 *It defines some common parameters/structures which are common to simulator and emulator.
 */
#ifndef H_include_common
#define H_include_common

#include <stdint.h>
#define ftokpath	("/tmp")
#define ftok_id	(6)

//NOTE We have not included the parameters size and maxnumthreads here as they are
//needed by the java file too. So, to change these values update IPCBase.h

//for size of the shared memory segment we specify COUNT of packets.
//the shared memory segment size allocated is (COUNT+5)*sizeof(packet)*MaxNumThread
//COUNT- queue_size
//COUNT+1 - flag[0] of peterson lock
//COUNT+2 - flag[1] of peterson lock
//COUNT+3 - turn of peterson lock
//COUNT+4 - per thread number of packets produced

// the structure of the packet to be transferred via IPC
// its important to use uint64_t for ip so that different sizes on different
// platforms do not affect.
typedef struct{
	uint64_t ip;					/* address of instruction */
	uint64_t volatile value;					/* defines the encoding scheme,details in DynamicInstructionBuffer.java */
	uint64_t tgt;					/* value according to encoding scheme */
}packet;

union semun_pin {
        int val;                    /* value for SETVAL */
        struct semid_ds *buf;       /* buffer for IPC_STAT, IPC_SET */
        unsigned short int *array;  /* array for GETALL, SETALL */
        struct seminfo *__buf;      /* buffer for IPC_INFO */
};

#endif
