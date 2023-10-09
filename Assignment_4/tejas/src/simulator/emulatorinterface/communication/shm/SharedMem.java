/*
 * Implementation for the shared memory IPC between the simulator and emulator. Implements the
 * functions declared in IPCBase.java. It declares the native functions which are implemented
 * in JNIShm.c
 * 
 * */

package emulatorinterface.communication.shm;

import java.lang.System;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import config.EmulatorConfig;
import config.SimulationConfig;
import config.SystemConfig;
import emulatorinterface.communication.*;
import emulatorinterface.*;
import generic.CircularPacketQueue;
import generic.Core;
import generic.CoreBcastBus;
import generic.GenericCircularQueue;
import generic.InstructionTable;


public class SharedMem extends  IpcBase
{
	// Must ensure that this is same as COUNT in shmem.h
	public static final int COUNT = 1000;
	private static int readerLocation[];
	public int idToShmGet;
	
	private int lastCounterValue = 0;
	private boolean shm_debug_queue = false;
	
	public SharedMem(int pid) 
	{
		super();
		
		// loads the library which contains the implementation of these native functions. The name of
		// the library should match in the makefile.
		String OS = System.getProperty("os.name").toLowerCase();
                                  
		if(OS.indexOf("win") >= 0) {
			System.load(EmulatorConfig.ShmLibDirectory + "\\libshmlib.dll");
		} else {
			System.load(EmulatorConfig.ShmLibDirectory + "/libshmlib.so");
		}
		

		
		// MAXNUMTHREADS is the max number of java threads while EMUTHREADS is the number of 
		// emulator(PIN) threads it is reading from. For each emulator threads 5 packets are
		// needed for lock management, queue size etc. For details look common.h
		System.out.println("coremap "+SimulationConfig.MapJavaCores);
		idToShmGet = pid;
		
		do
		{
			shmid = shmget(COUNT,SystemConfig.maxNumJavaThreads, SystemConfig.numEmuThreadsPerJavaThread, SimulationConfig.MapJavaCores, idToShmGet);
			if(shmid < 0)
			{
				idToShmGet = (idToShmGet + 1)%Integer.MAX_VALUE;
			}
			else
			{
				shmAddress = shmat(shmid, COUNT,SystemConfig.maxNumJavaThreads, SystemConfig.numEmuThreadsPerJavaThread, SimulationConfig.MapJavaCores, idToShmGet);
				if(shmAddress < 0)
				{
					shmdel(shmid);
					idToShmGet = (idToShmGet + 1)%Integer.MAX_VALUE;
				}
			}
		}
		while(shmid < 0 || shmAddress < 0);
		
		// initialise the reader location of all application threads
		readerLocation = new int[SystemConfig.maxNumJavaThreads * SystemConfig.numEmuThreadsPerJavaThread];
		for(int tidApp = 0; tidApp<SystemConfig.maxNumJavaThreads * SystemConfig.numEmuThreadsPerJavaThread; tidApp++) {
			readerLocation[tidApp] = 0;
		}
	}
	static int bar_wait = 0;
	static int numSharedMemPackets = 0;
	public int fetchManyPackets(int tidApp, CircularPacketQueue fromEmulator) {
		int numPackets;
		numPackets = numPacketsAlternate(tidApp);
		
		// negative value must be inferred by the runnable.
		if(numPackets <= 0) {
			return numPackets;
		}
		
		// System.out.println("numPackets = " + numPackets + "\nfromEmulator = " + fromEmulator.spaceLeft());
		
		// do not add packets to fromEmulator if there is not enough space to hold them
		if(numPackets>fromEmulator.spaceLeft()) {
			numPackets = fromEmulator.spaceLeft();
			if(numPackets<=0) {
				return numPackets;
			}
		}
		 
		long[] ret  = new long[3*numPackets]; 
		SharedMem.shmreadMult(tidApp, shmAddress, readerLocation[tidApp], numPackets,ret);
			for (int i=0; i<numPackets; i++) {
				// System.out.println("$sharedMem " + (++numSharedMemPackets) + " : " + ret[3*i]);
				fromEmulator.enqueue(ret[3*i], ret[3*i+1], ret[3*i+2]);
				if(shm_debug_queue) {
					lastCounterValue++;
					long val = ret[(3*i)+2];
					if(lastCounterValue!=val) {
						System.out.println("CONSUMER FOUND A BUG. Did not read the expected value: " + lastCounterValue + ", shared mem gave: " + val);
						System.exit(0);
					}
					
					int block=10000;
					if(lastCounterValue%block==0) {
						System.out.println("CONSUMER read " + ((float)val/(float)1000000) + " million packets till now");
					}
				}
				//System.out.println("CONSUMER read packet #" + ret[3*i]);
				//System.out.println(fromPIN.get(i).toString());
			}
		
		readerLocation[tidApp] = (readerLocation[tidApp] + numPackets) % SharedMem.COUNT;
		
		// update the queue-size of the shared segment
		tejasUpdateQueueSize(tidApp, numPackets);
		return numPackets;
	}
	
	
	public long totalProduced (int tidApp) {
		return tejasTotalProduced(tidApp);
	}
	
	public void finish(){
		shmd(shmAddress);
		shmdel(shmid);
	}

	public void cleanup() {
		shmd(shmAddress);
		shmdel(shmid);
	}
	
	// calls shmget function and returns the shmid. Only 1 big segment is created and is indexed
	// by the threads id. Also pass the core mapping read from config.xml
	native int shmget(int COUNT,int MaxNumJavaThreads,int EmuThreadsPerJavaThread , long coremap, int pid);
	
	// attaches to the shared memory segment identified by shmid and returns the pointer to 
	// the memory attached. 
	native  long shmat(int shmid, int COUNT,int MaxNumJavaThreads,int EmuThreadsPerJavaThread , long coremap, int pid);
	
	// returns the class corresponding to the packet struct in common.h. Takes as argument the
	// emulator thread id, the pointer corresponding to that thread, the index where we want to
	// read and COUNT
	native static Packet shmread(int tid,long pointer, int index);
	
	// reads multiple packets into the arrays passed.
	native static void shmreadMult(int tid,long pointer, int index, int numToRead, long[] ret);
	
	native static void tejasUpdateQueueSize(int tid, int numPacketsReadFromTheQueue);
	
	// reads only the "value" from the packet struct. could be done using shmread() as well,
	// but if we only need to read value this saves from the heavy JNI callback and thus saves
	// on time.
	native static long tejasTotalProduced(int tid);
	
	// write in the shared memory. needed in peterson locks.
	//native static int shmwrite(int tid,long pointer, int index, int val);
	
	// deatches the shared memory segment
	native static int shmd(long pointer);
	
	// deletes the shared memory segment
	native static int shmdel(int shmid);
	
	// inserts compiler barriers to avoid reordering. Needed for correct implementation of 
	// Petersons lock.
	native static void asmmfence();
	
	native static int numPacketsAlternate(int tidApp);

	// get a lock to access a resource shared between PIN and java. For an explanation of the 
	// shared memory segment structure which explains the parameters passed to the shmwrite 
	// and shmreadvalue functions here take a look in common.h
	
	// cores associated with this java thread
	Core[] cores;

	// address of shared memory segment attached. should be of type 'long' to ensure for 64bit
	static long shmAddress;
	static int shmid;

	public void initIpc() {
	}

	public void errorCheck(int tidApp, long totalReads) {
		long totalProduced = totalProduced(tidApp); 
		if(totalReads > totalProduced) {
			misc.Error.showErrorAndExit("For application thread" + tidApp
					+"totalRead="+totalReads+" > totalProduced="+totalProduced);
		}
	}
}
