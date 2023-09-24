package emulatorinterface.communication.mmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import config.SimulationConfig;
import config.SystemConfig;

import emulatorinterface.communication.*;
import generic.CircularPacketQueue;


/*XXX
 * Caution, this code has not been tested.
 * */

public class MemMap extends IpcBase
{
	// Must ensure that this is same as in mmap.h
	public static final int COUNT = 1000;
	static final String FILEPATH = "pfile";

	File aFile;
	RandomAccessFile ioFile;
	FileChannel ioChannel;

	private IntBuffer ibuf;

	private IntBuffer lockBuf;
	MappedByteBuffer buf;
	MappedByteBuffer lBuf;

	public MemMap(){
		super();
		aFile = new File (FILEPATH);
		try {
			ioFile = new RandomAccessFile (aFile, "rw");
			ioChannel = ioFile.getChannel ();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Process startPIN(String cmd) throws Exception{
		Runtime rt = Runtime.getRuntime();
		try {
			Process p = rt.exec(cmd);
			StreamGobbler s1 = new StreamGobbler ("stdin", p.getInputStream ());
			StreamGobbler s2 = new StreamGobbler ("stderr", p.getErrorStream ());
			s1.start ();
			s2.start ();
			return p;
		} catch (Exception e) {
			return null;
		}
	}



	public void waitForJavaThreads() {
		try {
			// this takes care if no thread started yet.
			free.acquire();	
	
			// if any thread has started and not finished then wait.
			for (int i=0; i<SystemConfig.maxNumJavaThreads; i++) {
				if (javaThreadStarted[i] && !javaThreadTermination[i]) {
					free.acquire();
				}
			}
	
			//inform threads which have not started about finish
			for (int i=0; i<SystemConfig.maxNumJavaThreads; i++) {
				if (javaThreadStarted[i]==false) javaThreadTermination[i]=true;
				//totalInstructions += numInstructions[i];
			}
		} catch (InterruptedException ioe) {
			misc.Error.showErrorAndExit("Wait for java threads interrupted !!");	
		}
	}

	public void finish(){
		try {
			ioFile.close ();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Packet fetchOnePacket(int tidApp, int index) {
		 //TODO
		//this should return a packet	(ibuf.get( (index) %COUNT ) );
		return null;
	}

	@Override
	public void initIpc() {
		if (SimulationConfig.debugMode) 
			System.out.println("-- Mmap initialising");
		try {
			buf = ioChannel.map (FileChannel.MapMode.READ_WRITE, 0L,
					(long) ((COUNT) * 4)).load ();

			lBuf = ioChannel.map (FileChannel.MapMode.READ_WRITE, (long) ((COUNT) * 4),
					(long) (20) ).load ();
			ioChannel.close ();

			//FIXME TODO
			// these should be as packet buffer not int buffers
			ibuf = buf.asIntBuffer ();
			lockBuf = lBuf.asIntBuffer ();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		String name;
		for (int i=0; i<SystemConfig.maxNumJavaThreads; i++){
			name = "thread"+Integer.toString(i);
			javaThreadTermination[i]=false;
			javaThreadStarted[i]=false;
			//TODO not all cores are assigned to each thread
			//when the mechanism to tie threads to cores is in place
			//this has to be changed
		}
	}

	public int numPackets(int tidApp) {
		get_lock(lockBuf, 0, lBuf);
		int queue_size = lockBuf.get(0);
		release_lock(lockBuf, 0, lBuf);
		return queue_size;
	}

	private void release_lock(IntBuffer lockBuf2, int i, MappedByteBuffer lBuf2) {
		ibuf.put(COUNT + 2, 0);
		buf.force();

	}

	private void get_lock(IntBuffer lockBuf2, int i, MappedByteBuffer lBuf2) {

		ibuf.put(COUNT + 2, 1); // flag[1] = 1
		buf.force();
		ibuf.put(COUNT + 3, 0); // turn = 0
		buf.force();
		while( (ibuf.get(COUNT+1) == 1) && (ibuf.get(COUNT + 3) == 0 )) {}

	}

	public long totalProduced(int tidApp) {
		return lockBuf.get(0 + 4);
	}

	public long update(int tidApp, int numReads) {
		int queue_size;
		get_lock(lockBuf, 0, lBuf);
		  queue_size = lockBuf.get(0);
	      queue_size -= numReads;
		  lockBuf.put(0, queue_size);
		  release_lock(lockBuf, 0,lBuf);
		  return queue_size;
	}

	public int fetchManyPackets(int tidApp, int index, int numPackets,
			ArrayList<Packet> fromPIN) {
				return 0;
		// TODO Auto-generated method stub
		
	}

	public ArrayList<Packet> fetchManyPackets(int tidApp, int readerLocation,
			int numReads) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void errorCheck(int tidApp, long totalReads) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int fetchManyPackets(int tidApp, CircularPacketQueue fromEmulator) {
		// TODO Auto-generated method stub
		return 0;
	}
}
