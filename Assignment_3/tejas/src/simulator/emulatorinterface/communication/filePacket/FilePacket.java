package emulatorinterface.communication.filePacket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import main.CustomObjectPool;
import misc.Util;

import config.EmulatorConfig;
import config.EmulatorType;
import config.SimulationConfig;
import config.SystemConfig;
import emulatorinterface.communication.Encoding;
import emulatorinterface.communication.IpcBase;
import emulatorinterface.communication.Packet;
import generic.CircularPacketQueue;

//This communication type reads from a file containing instructions in the format "ip value tgt"
//where value is the type of the instruction and tgt is either assembly string(for assembly instruction) 
//or a value for other instructions 
public class FilePacket extends IpcBase implements Encoding {

	BufferedReader inputBufferedReader[];
	int maxApplicationThreads = -1;
	long totalFetchedAssemblyPackets = 0;
	ArrayList<Integer> pinpointWeights;
	int cur_slice = 0;
	String []basenameForBenchmarks;
	
	public FilePacket(String []basenameForBenchmarks) {
		/*
		this.maxApplicationThreads = SystemConfig.maxNumJavaThreads*SystemConfig.numEmuThreadsPerJavaThread;
		this.basenameForBenchmarks = basenameForBenchmarks;
		
		inputBufferedReader = new BufferedReader[maxApplicationThreads];
		
		pinpointWeights = new ArrayList<Integer>();
		if(SimulationConfig.pinpointsSimulation == true)
		{
			try
			{
				BufferedReader pinpointsFile = new BufferedReader(new FileReader(SimulationConfig.pinpointsFile));
				String line;
				while((line = pinpointsFile.readLine()) != null)
				{
					pinpointWeights.add(Math.round(Float.parseFloat(line.split(" ")[1]) * 100));
				}
				pinpointsFile.close();
			}
			catch(Exception e)
			{
				misc.Error.showErrorAndExit(e.getMessage());
			}
		}
		
		int numTotalThreads = 0;
		for (int benchmark=0; benchmark<basenameForBenchmarks.length; benchmark++) {
			for (int tid=0; ;tid++) {
				String inputFileName;
				if(SimulationConfig.pinpointsSimulation == false)
				{
					inputFileName = basenameForBenchmarks[benchmark] + "_" + tid + ".gz";
				}
				else
				{
					inputFileName = basenameForBenchmarks[benchmark] + "_0_0.gz";
				}
				try {
					inputBufferedReader[numTotalThreads] = new BufferedReader(
							new InputStreamReader(
							new GZIPInputStream(
							new FileInputStream(inputFileName))));
					numTotalThreads++;
					
					if(SimulationConfig.pinpointsSimulation == true)
					{
						//pinpoints is for single threaded applications only
						break;
					}
				} catch (Exception e) {
					if(tid==0) {
						// not able to find first file is surely an error.
						misc.Error.showErrorAndExit("Error in reading input packet file " + inputFileName);
					} else {
						System.out.println("Number of threads for benchmark " + basenameForBenchmarks[benchmark] + " : " + tid);
						break;
					}
				}
			}
		}
		*/
	}

	public void initIpc() {
		// this does nothing
	}

	public int fetchManyPackets(int tidApp, CircularPacketQueue fromEmulator) {
		/*
		if(tidApp>=maxApplicationThreads) {
			misc.Error.showErrorAndExit("FilePacket cannot handle tid = " + tidApp);
		}
		
		if(inputBufferedReader[tidApp]==null) {
			return 0;
		}
		
		int maxSize = fromEmulator.spaceLeft();
		
		for(int i=0; i<maxSize; i++) {
			
			try {
				//Subset Simulation
				if(SimulationConfig.subsetSimulation && totalFetchedAssemblyPackets >= (SimulationConfig.subsetSimSize + SimulationConfig.NumInsToIgnore)) {
					fromEmulator.enqueue(totalFetchedAssemblyPackets-SimulationConfig.NumInsToIgnore, -2, -1);
					return (i+1);
				}
				
				String inputLine = inputBufferedReader[tidApp].readLine();
				
				if(inputLine != null) {
					
					long ip = -1, value = -1, tgt = -1;
					StringTokenizer stringTokenizer = new StringTokenizer(inputLine);
					
					ip = Util.parseLong(stringTokenizer.nextToken());
					value = Long.parseLong(stringTokenizer.nextToken());
					
					if(value!=ASSEMBLY) {
						tgt = Util.parseLong(stringTokenizer.nextToken());
					} else {
						totalFetchedAssemblyPackets += 1;
						
						if(totalFetchedAssemblyPackets%1000000==0) {
							System.out.println("Number of assembly instructions till now : " + totalFetchedAssemblyPackets);
						}
						
						tgt = -1;
						CustomObjectPool.getCustomAsmCharPool().enqueue(tidApp, stringTokenizer.nextToken("\n").getBytes(), 1);
					}
						
					//ignore these many instructions: NumInsToIgnore 
					if(totalFetchedAssemblyPackets < SimulationConfig.NumInsToIgnore) {
						if(value == ASSEMBLY) {
							CustomObjectPool.getCustomAsmCharPool().dequeue(tidApp);
						}
						return 0;
					// totalFetchedAssemblyPackets just became equal to NumInsToIgnore, so 
					// we start setting fromEmulator packets
					} else if(totalFetchedAssemblyPackets == SimulationConfig.NumInsToIgnore && value==ASSEMBLY) {
						i=0;						
					}	
					
					if(SimulationConfig.pinpointsSimulation == false)
					{
						fromEmulator.enqueue(ip, value, tgt);
					}
					else
					{
						if(value != Encoding.SUBSETSIMCOMPLETE)
						{
							fromEmulator.enqueue(ip, value, tgt);
						}
						else
						{
							inputBufferedReader[tidApp].close();
							pinpointWeights.set(cur_slice, pinpointWeights.get(cur_slice)-1);
							while(cur_slice < pinpointWeights.size() && pinpointWeights.get(cur_slice) <= 0)
							{
								cur_slice++;
							}
							
							if(cur_slice >= pinpointWeights.size())
							{
								fromEmulator.enqueue(ip, value, tgt);
							}
							else
							{	
								inputBufferedReader[tidApp] = new BufferedReader(
										new InputStreamReader(
										new GZIPInputStream(
										new FileInputStream(basenameForBenchmarks[tidApp] + "_" + cur_slice + "_0.gz"))));
								System.out.println("opening file: " + basenameForBenchmarks[tidApp] + "_" + cur_slice + "_0.gz");
							}
						}
					}
				} else {
					return (i);
				}
			} catch (IOException e) {
				// We are expecting an end of file exception at the end of the trace.
				// Lets return the number of elements read till now.
				// Hopefully some thread will contain a subset simulation complete packet
				//System.out.println("Thread " + tidApp + " 's trace file has completed");
				return i;
				// misc.Error.showErrorAndExit("error in reading from file for tid = " + tidApp + "\n" + e);
			}
		}
		
		return maxSize;
		*/
		return 0;
	}

	public void errorCheck(int tidApp, long totalReads) {
		// we do not do any error checking for filePacket interface
	}
	
	public void finish() {
		/*
		for(int i=0; i<maxApplicationThreads; i++) {
			try {
				if(inputBufferedReader[i] != null) {
					inputBufferedReader[i].close();	
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		*/
	}
}
