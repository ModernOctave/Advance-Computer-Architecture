/*
 * This is the JNI file which has the implementation of the native functions declared in
 * SharedMem.java. The functions name must be according to the full package names. We also use
 * a callback in the shmread function for Packet's constructor.
 */
#define _GNU_SOURCE
#include <jni.h>
#include "SharedMem.h"
#include <sys/types.h>

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include "../commonJNI.h"
#include <string.h>

#ifdef _WIN32
#include <windows.h>
#include <time.h>
#else
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/resource.h>
#include <sys/time.h>
#include <sched.h>
#endif



#ifdef _WIN32
//typedef void * HANDLE;
typedef unsigned long DWORD;
//#define HANDLE void* 
#endif
// TODO these could be saved to avoid calling again and again,or do multiple reads to avoid
// multiple JNI calls.
jclass cls;
jmethodID constr;
jlong shmAddress;
jint gCOUNT;
jint gMaxNumJavaThreads;
jint gEmuThreadsPerJavaThread;
#ifdef _WIN32
HANDLE hMapFile;
LPINT hMapView,aux;
#endif


uint64_t shmreadvalue(int tid, long pointer, int index){
	packet *addr;
	addr=(packet *)(intptr_t)pointer;
	return  (addr [  (tid*(gCOUNT+5)) + index].value);
}

void shmwrite(int tid, long pointer, int index, int val){
	packet *addr;
	addr=(packet *)(intptr_t)pointer;
	addr[tid*(gCOUNT+5)+index].value=val;
}

void tejas_fence(){
#ifdef _WIN32
	//_ReadWriteBarrier();    // compiler barriers
	MemoryBarrier();		  
	//_mm_mfence();
#else
	__sync_synchronize();
#endif
}

void tejas_get_lock(int tidApp) {
	tejas_fence();
	shmwrite(tidApp,shmAddress,gCOUNT+2,1);
	tejas_fence();
	shmwrite(tidApp,shmAddress,gCOUNT+3,0);
	tejas_fence();
	while( (shmreadvalue(tidApp,shmAddress,gCOUNT+1) == 1) && (shmreadvalue(tidApp,shmAddress,gCOUNT+3) == 0)) {
		//tejas_fence();
	}
	tejas_fence();
}

void tejas_release_lock(int tidApp) {
	tejas_fence();
	shmwrite(tidApp,shmAddress, gCOUNT+2,0);
	tejas_fence();
}


/*
 * shmget a shared memory area using the keys from common.h. Creates
 * a key using ftok.Creates a dummy shared memory segment and then
 * deletes it to ensure a fresh memory segment. Now create a fresh
 * segment with the parameter size. return the shmid for this segment
 */
JNIEXPORT jint JNICALL Java_emulatorinterface_communication_shm_SharedMem_shmget
(JNIEnv * env, jobject jobj, jint COUNT,jint MaxNumJavaThreads,jint EmuThreadsPerJavaThread,
		jlong coremap, jint pid) {
	uint64_t mask = coremap;
		int size;//=sizeof(packet)*(COUNT+5)*MaxNumJavaThreads*EmuThreadsPerJavaThread;
	#ifdef _WIN32
		int *a_ptr;
		char str[50];
		int ptr;
		int a=1000;
	//	HANDLE hMapFile;
		int* b;
		int d;
		DWORD dwError;
	#endif
	
	size=sizeof(packet)*(COUNT+5)*MaxNumJavaThreads*EmuThreadsPerJavaThread;

		//set the global variables
	gCOUNT = COUNT;
	gMaxNumJavaThreads = MaxNumJavaThreads;
	gEmuThreadsPerJavaThread = EmuThreadsPerJavaThread;
	
	//size1 is the number of packets needed in the segment.
	
#ifdef _WIN32
	
	_itoa(pid,str,10);
	
	
	hMapFile=(CreateFileMapping(INVALID_HANDLE_VALUE,
		NULL, PAGE_READWRITE,
		0,size,str));
	if (hMapFile == NULL)
	{
		// printf("Unable to create a shared mem file.");
		 exit(1);
	}
	if(GetLastError() ==  ERROR_ALREADY_EXISTS)
     { 
		 printf("File mapping object already exists");
		 CloseHandle(hMapFile);
		 	return (-1);
	 }
	else
	{
		


		CloseHandle(hMapFile);
	}
	hMapFile = CreateFileMapping(INVALID_HANDLE_VALUE,
		NULL, PAGE_READWRITE,
		0,size, str);
	 dwError = GetLastError();
	 
	

	if (hMapFile == NULL)
	{
		printf("Unable to create a shared mem file.");
		exit(1);
	}
	if(GetLastError() ==  ERROR_ALREADY_EXISTS)
     { 
		 printf("File mapping object already exists");
		 CloseHandle(hMapFile);
		 	return (-1);
	 }
	else
	{
		

		
		
		ptr=((int)(hMapFile));
		
		return ptr;          ///////////////error in return type *************************************
				
	
	
	}
	#else


	int shmid;
	//key_t key=ftok(ftokpath,ftok_id);
	//printf("jnishm : id = %d\n", pid);
	key_t key=ftok(ftokpath,pid);
	if ( key == (key_t)-1 )
	{
		//perror("ftok in jni ");
		//printf("error in shmget : ftok failed\n");
		return (-1);
	}


	// first create a dummy and delete
	shmid = shmget(key,32, IPC_CREAT | 0666);
	struct shmid_ds sds;
	if(shmid > 0)
		shmctl(shmid,IPC_RMID,&sds);

	if ((shmid = shmget(key, size, IPC_CREAT | IPC_EXCL | 0666)) < 0) {
		//perror("shmget in jni -:");
		//printf("error in shmget : shmget failed\n");
		return (-1);
	}

	return (shmid);
	#endif
}


#ifdef _WIN32
JNIEXPORT jint JNICALL Java_emulatorinterface_communication_shm_SharedMem_add 
(JNIEnv * env, jobject jobj, jint n1, jint n2) {
	int a = 0;
	int *a_ptr;
	HANDLE myhandle;
	a_ptr=&a;
	myhandle = &a;
	*((int *)myhandle)=1000;
	a_ptr=(int*)myhandle;
//	printf("-----------------------------------reached here : %d ----------------------------------------", *(a_ptr));
	
	return *a_ptr;
	
}
#endif
// Attach a memory segment using the shmid generated by the shmget
// returns the pointer of the segment.
JNIEXPORT jlong JNICALL Java_emulatorinterface_communication_shm_SharedMem_shmat 
(JNIEnv * env, jobject jobj, jint shmid, jint COUNT,jint MaxNumJavaThreads,jint EmuThreadsPerJavaThread,
		jlong coremap, jint pid) {
			
	intptr_t ret;
	packet *shm;
	int size;
	
	
	
#ifdef _WIN32
	LPINT hMapView,aux;
	size = sizeof(packet)*(COUNT+5)*MaxNumJavaThreads*EmuThreadsPerJavaThread;
	if((shm = (packet*)MapViewOfFile((HANDLE)hMapFile, FILE_MAP_ALL_ACCESS,0,0,size)) == (packet*)-1){
	
	
		return (-1);
	}
#else
	if ((shm = (packet *)shmat(shmid, NULL, 0)) == (packet *) -1) {
	
		return (-1);
	}
#endif
	ret=(intptr_t)shm;
	
	shmAddress = ret;
	
	
	return (ret);
}

// Detach a segment using the shm pointer        
JNIEXPORT jint JNICALL Java_emulatorinterface_communication_shm_SharedMem_shmd 
(JNIEnv * env, jobject jobj, jlong pointer) {

	register int rtrn;
	packet *addr;
	addr=(packet *)(intptr_t)pointer;
	
    #ifdef _WIN32
	if (rtrn = UnmapViewOfFile(addr)==-1) 
	{ 
       //printf("Could not unmap view of file."); 
	} 
	#else
	if ((rtrn=shmdt(addr))==-1) {

		//perror("shmdt in jni ");
		//exit(1);
		//printf("error in shmdt\n");
	}
	#endif

	return (rtrn);
}

// Delete a segment using shmid
JNIEXPORT jint JNICALL Java_emulatorinterface_communication_shm_SharedMem_shmdel
(JNIEnv * env, jobject jobj, jint shmid) {
register int rtrn;
struct shmid_ds  *shmid_ds;
#ifdef _WIN32
if ((rtrn = UnmapViewOfFile(&shmid_ds))==-1) 
	{ 
       printf("Could not unmap view of file."); 
	} 

#else
       //////////shmid_ds error shmctl error ***********************************
	
	if ((rtrn = shmctl(shmid, IPC_RMID, &shmid_ds)) == -1) {
		//perror("shmdel in jni ");
		//exit(1);
		//printf("error in shmdel\n");
	}

#endif 

	

	return (rtrn);
}

// Return a packet object
JNIEXPORT jobject JNICALL Java_emulatorinterface_communication_shm_SharedMem_shmread
(JNIEnv * env, jobject jobj,jint tid,jlong pointer,jint index) {
	packet *addr;
	jvalue args[3];
	jobject object;
	addr=(packet *)(intptr_t)pointer;

	cls = (*env)->FindClass(env,"emulatorinterface/communication/Packet");
	constr = (*env)->GetMethodID(env,cls,"<init>","(JJJ)V");

	addr = &(addr[tid*(gCOUNT+5)+index]);
	args[0].j = (*addr).ip;
	args[1].j = (*addr).value;
	args[2].j = (*addr).tgt;
	
	object = (*env)->NewObjectA(env,cls,constr,args);
	return object;
}

// Return a packet object
JNIEXPORT void JNICALL Java_emulatorinterface_communication_shm_SharedMem_tejasUpdateQueueSize
(JNIEnv * env, jclass jcls,jint tid, jint numPacketsReadFromTheQueue) {
	int tejasQueueSize;
	tejas_get_lock(tid);
	tejasQueueSize = shmreadvalue(tid, shmAddress, gCOUNT);
	tejasQueueSize -= numPacketsReadFromTheQueue;
	shmwrite(tid, shmAddress, gCOUNT, (int)tejasQueueSize);
	tejas_release_lock(tid);
}

// Return a packet object
JNIEXPORT void JNICALL Java_emulatorinterface_communication_shm_SharedMem_shmreadMult
(JNIEnv * env, jclass jcls,jint tid,jlong pointer,jint index,jint num,jlongArray ret) {

	jlongArray result;
	uint64_t *orig;
	uint64_t *transfer;
	uint64_t *int1;
	packet *addr;
	 tejas_get_lock(tid);
	 
	 
	 addr=(packet *)(intptr_t)pointer;
	 orig = (uint64_t *)&(addr[tid*(gCOUNT+5)]);
	 addr = &(addr[tid*(gCOUNT+5)+index]);

	 transfer = (uint64_t*)addr;
	 
	 int1 = (uint64_t*)malloc(sizeof(uint64_t)*num*3);
	 //Do copying here
	 if ((index+num)<gCOUNT) {
	 		 //(*env)->SetLongArrayRegion(env, result, 0, num*3, transfer);
	 		 memcpy(int1,transfer,sizeof(uint64_t)*num*3);
	 	 }
	 	 else {
	 		 int part1 = gCOUNT-index;
	 		 int part2 = num - part1;
	 		 //(*env)->SetLongArrayRegion(env, result, 0, part1*3, transfer);
	 		 //(*env)->SetLongArrayRegion(env, result, part1*3, part2*3, orig);
	 		 memcpy(int1,transfer,sizeof(uint64_t)*part1*3);
	 		 memcpy(int1+part1*3,orig,sizeof(uint64_t)*part2*3);
	 	 }

	 (*env)->SetLongArrayRegion(env,ret,0,num*3,(jlong*)int1);
	 free(int1);
	 
	 tejas_release_lock(tid);
/*
	 // move from the temp structure to the java structure
	 if (index+num<gCOUNT) {
		 (*env)->SetLongArrayRegion(env, result, 0, num*3, transfer);
	 }
	 else {
		 int part1 = gCOUNT-index;
		 int part2 = num - part1;
		 (*env)->SetLongArrayRegion(env, result, 0, part1*3, transfer);
		 (*env)->SetLongArrayRegion(env, result, part1*3, part2*3, orig);
	 }
	 return result;
*/


	}



// Returns just the value, needed when we want to read just the "value" for lock managment
JNIEXPORT jlong JNICALL Java_emulatorinterface_communication_shm_SharedMem_tejasTotalProduced
(JNIEnv * env, jobject jobj,jint tid) {
	return shmreadvalue(tid, shmAddress, gCOUNT + 4);
}



/*
// Write at 'index' the value 'val'. One big segment is created for all
// threads and being indexed by the thread ids.
JNIEXPORT jint JNICALL Java_emulatorinterface_communication_shm_SharedMem_shmwrite 
(JNIEnv * env, jobject jobj,jint tid,jlong pointer,jint index,jint val) {

	shmwrite( tid, pointer, index,val);
	return 1;
}

// Returns just the value, needed when we want to read just the "value" for lock managment
JNIEXPORT jlong JNICALL Java_emulatorinterface_communication_shm_SharedMem_shmreadvalue
(JNIEnv * env, jobject jobj,jint tid,jlong pointer,jint index) {
	return shmreadvalue(tid,pointer, index);
}
*/

// Return number of packets
JNIEXPORT jint JNICALL Java_emulatorinterface_communication_shm_SharedMem_numPacketsAlternate
(JNIEnv * env, jobject jobj,jint tid) {
	int size;
	tejas_get_lock(tid);
	size = shmreadvalue(tid, shmAddress, gCOUNT);
	tejas_release_lock(tid);
	return size;
}


// hardware barriers dont seem to work.So using compiler barriers.
JNIEXPORT void JNICALL Java_emulatorinterface_communication_shm_SharedMem_asmmfence 
(JNIEnv * env, jobject jobj) {
#ifdef _WIN32
	 MemoryBarrier();			// compiler barriers
#else
	__sync_synchronize();
#endif
}
