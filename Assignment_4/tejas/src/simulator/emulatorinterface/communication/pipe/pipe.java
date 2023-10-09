package emulatorinterface.communication.pipe;

import emulatorinterface.DynamicInstructionBuffer;
import emulatorinterface.communication.*;

public class pipe// extends IpcBase
{
	public pipe(){
		System.out.println("dummy constructor");
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

	public void createRunnables(DynamicInstructionBuffer passPackets) {
		System.out.println("creat readers");
	}

	public long doExpectedWaitForSelf() {
		System.out.println("wait for self");
		return 0;
	}
	
	public void doWaitForPIN(Process p){
		System.out.println("wait for pin");
	}
	
	public void finish(){
		System.out.println("finish");
	}

	// returns the numberOfPackets which are currently there in the stream for tidApp
	public int numPackets(int tidApp){return -1;}
	
	// fetch one packetList for tidApp from index
	public Packet fetchOnePacket(int tidApp, int index ){ return null;}
	
	public int update(int tidApp, int numReads){ return -1;}
	// The main thread waits for the finish of reader threads and returns total number of 
	// instructions read
	
	// return the total packets produced by PIN till now
	public int totalProduced(int tidApp){ return -1;}
}
