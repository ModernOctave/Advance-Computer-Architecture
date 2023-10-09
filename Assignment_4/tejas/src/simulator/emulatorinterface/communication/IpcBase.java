/*
 * This file declares some parameters which is common to all IPC mechanisms. Every IPC mechanism
 * inherits this class and implements the functions declared. Since Java has runtime binding
 * so the corresponding methods will be called.
 * 
 * MAXNUMTHREADS - The maximum number of java threads running
 * EMUTHREADS - The number of emulator threads 1 java thread is reading from
 * COUNT - this many number of packets is allocated in the shared memory for each 
 * 		   application/emulator thread 
 * */

package emulatorinterface.communication;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import config.SystemConfig;

import emulatorinterface.GlobalTable;
import emulatorinterface.RunnableThread;
import generic.CircularPacketQueue;
import generic.GenericCircularQueue;

public abstract class IpcBase {

	// Must ensure that MAXNUMTHREADS*EMUTHREADS == MaxNumThreads on the PIN side
	// Do not move it to config file unless you can satisfy the first constraint
	//public static final int MaxNumJavaThreads = 1;
	//public static final int EmuThreadsPerJavaThread = 64; 
//	public static int memMapping[] = new int[EmuThreadsPerJavaThread];

	// state management for reader threads
	public boolean[] javaThreadTermination;
	public boolean[] javaThreadStarted;

	// number of instructions read by each of the threads
	// public long[] numInstructions = new long[MaxNumJavaThreads];

	// to maintain synchronization between main thread and the reader threads
	public static final Semaphore free = new Semaphore(0, true);

	// public static InstructionTable insTable;
	public static GlobalTable glTable;

	// Initialise structures and objects
	public IpcBase () {
		
		javaThreadTermination = new boolean[SystemConfig.maxNumJavaThreads];
		javaThreadStarted = new boolean[SystemConfig.maxNumJavaThreads];
		
		for (int i=0; i<SystemConfig.maxNumJavaThreads; i++) {
			javaThreadTermination[i]=false;
			javaThreadStarted[i]=false;
			//TODO not all cores are assigned to each thread
			//when the mechanism to tie threads to cores is in place
			//this has to be changed
		}

		glTable = new GlobalTable(this);
	}

	public abstract void initIpc();

	/*** start, finish, isEmpty, fetchPacket, isTerminated ****/
	public RunnableThread[] getRunnableThreads(){
		System.out.println("Implement getRunnableThreads() in the IPC mechanism");
		return null;
	}

	// returns the numberOfPackets which are currently there in the stream for tidApp
	//the runnable thread does not require the numPackets in stream
	//public abstract int numPackets(int tidApp);

	// fetch one packet for tidApp from index.
	// fetchPacket creates a Packet structure which will strain the garbage collector.
	// Hence, this method is no longer supported.
	//public abstract Packet fetchOnePacket(int tidApp, int index);
	
	//public abstract int fetchManyPackets(int tidApp, int readerLocation, int numReads,ArrayList<Packet> fromPIN);
	public abstract int fetchManyPackets(int tidApp, CircularPacketQueue fromEmulator);
	
	//public abstract long update(int tidApp, int numReads);
	// The main thread waits for the finish of reader threads and returns total number of 
	// instructions read

	// return the total packets produced by PIN till now
	//public abstract long totalProduced(int tidApp);
	
	public abstract void errorCheck(int tidApp, long totalReads);

	public void waitForJavaThreads() {
		
		try {		
			// this takes care if no thread started yet.
			free.acquire();	
			
			int j=0;
			// if any thread has started and not finished then wait.
			for (int i=0; i<SystemConfig.maxNumJavaThreads; i++) {
				if (javaThreadStarted[i] && !javaThreadTermination[i]) {
					free.acquire();
					j++;
				}
			}
			
			//inform threads which have not started about finish
			for (int i=0; i<SystemConfig.maxNumJavaThreads; i++) {
				if (javaThreadStarted[i]==false) {
					javaThreadTermination[i]=true;
				}
			}
			
			for (; j<SystemConfig.maxNumJavaThreads-1; j++) {
				free.acquire();
			}
		} catch (InterruptedException ioe) {
			misc.Error.showErrorAndExit("Wait for java threads interrupted !!");
		}
	}

	// Free buffers, free memory , deallocate any stuff.
	public void finish() {
		System.out.println("Implement finish in the IPC mechanism");
	}

	public static int getEmuThreadsPerJavaThread() {
		return SystemConfig.numEmuThreadsPerJavaThread;
	}
}