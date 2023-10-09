package generic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import main.ArchitecturalComponent;
import main.Main;
import memorysystem.Cache;
import memorysystem.CoreMemorySystem;
import memorysystem.MainMemoryController;
import memorysystem.MemorySystem;
import memorysystem.coherence.Coherence;
import memorysystem.nuca.NucaCache;
import memorysystem.nuca.NucaCache.NucaType;
import net.NocInterface;
import net.Router;
import pipeline.outoforder.OutOrderExecutionEngine;
import config.CoreConfig;
import config.EmulatorConfig;
import config.EnergyConfig;
import config.SimulationConfig;
import config.SystemConfig;
import config.SystemConfig.Interconnect;
import emulatorinterface.communication.IpcBase;
import emulatorinterface.translator.qemuTranslationCache.TranslatedInstructionCache;

import dram.MainMemoryDRAMController;
import config.MainMemoryConfig;
import config.PipelineType;

public class Statistics {
	
	
	static FileWriter outputFileWriter;
	
	static String benchmark;
	public static void printSystemConfig()
	{
		//read config.xml and write to output file
		try
		{
			outputFileWriter.write("[Configuration]\n");
			outputFileWriter.write("\n");
			
			outputFileWriter.write("EmulatorType: " + EmulatorConfig.emulatorType + "\n");
			outputFileWriter.write("Benchmark: "+benchmark+"\n");
			outputFileWriter.write("Schedule: " + (new Date()).toString() + "\n");


			//added by harveenk
			if(SystemConfig.memControllerToUse==true){
				outputFileWriter.write("\n[Main Memory Configuration]\n");
				outputFileWriter.write("RAM frequency:" + (1/(SystemConfig.mainMemoryConfig.tCK)*1000) + " MHz\n");
				outputFileWriter.write("Num Channels: " + SystemConfig.mainMemoryConfig.numChans + "\n");
				outputFileWriter.write("Num Ranks: " + SystemConfig.mainMemoryConfig.numRanks + "\n");
				outputFileWriter.write("Num Banks: " + SystemConfig.mainMemoryConfig.numBanks + "\n");
				outputFileWriter.write("Row Buffer Policy: " + SystemConfig.mainMemoryConfig.rowBufferPolicy + "\n");
				outputFileWriter.write("Scheduling Policy: " + SystemConfig.mainMemoryConfig.schedulingPolicy + "\n");
				outputFileWriter.write("Queuing Structure: " + SystemConfig.mainMemoryConfig.queuingStructure + "\n");
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	private static CoreMemorySystem coreMemSys[];
	private static Core cores[];
	
	//Translator Statistics
	
	static long dataRead[];
	static long numHandledCISCInsn[][];
	static long numCISCInsn[][];
	static long noOfMicroOps[][];
	static double staticCoverage;
	static double dynamicCoverage;
	
	public static void printTranslatorStatistics()
	{
		for(int i = 0; i < SystemConfig.maxNumJavaThreads; i++)
		{
			for (int j=0; j<IpcBase.getEmuThreadsPerJavaThread(); j++) {
				// if(SimulationConfig.pinpointsSimulation == false)
				//{
					totalNumMicroOps += noOfMicroOps[i][j];
				//}
//				totalNumMicroOps += numCoreInstructions[i];
				totalHandledCISCInsn += numHandledCISCInsn[i][j];
				totalPINCISCInsn += numCISCInsn[i][j];
			}
		}
		
		dynamicCoverage = ((double)totalHandledCISCInsn/(double)totalPINCISCInsn)*(double)100.0;
		
		// if(SimulationConfig.pinpointsSimulation == true)
		//{
		//	for(int i = 0; i < SystemConfig.NoOfCores; i++)
		//	{
		//		totalNumMicroOps += cores[i].getNoOfInstructionsExecuted();
		//	}
		//	totalHandledCISCInsn = 3000000;
		//}
		
		//for each java thread, print number of instructions provided by PIN and number of instructions forwarded to the pipeline
		try
		{
			outputFileWriter.write("\n");
			outputFileWriter.write("[Translator Statistics]\n");
			outputFileWriter.write("\n");
			
			for(int i = 0; i < SystemConfig.maxNumJavaThreads; i++)
			{
				outputFileWriter.write("Java thread\t=\t" + i + "\n");
				outputFileWriter.write("Data Read\t=\t" + dataRead[i] + " bytes\n");
//				outputFileWriter.write("Number of instructions provided by emulator\t=\t" + numHandledCISCInsn[i] + "\n");
//				outputFileWriter.write("Number of Micro-Ops\t=\t" + noOfMicroOps[i] + " \n");
//				outputFileWriter.write("MicroOps/CISC = " + 
//						((double)(numInstructions[i]))/((double)(noOfMicroOps[i])) + "\n");
//				outputFileWriter.write("\n");
			}
			outputFileWriter.write("Number of micro-ops\t\t=\t" + totalNumMicroOps + "\n");
			outputFileWriter.write("Number of handled CISC instructions\t=\t" + totalHandledCISCInsn + "\n");
			outputFileWriter.write("Number of PIN CISC instructions\t=\t" + totalPINCISCInsn + "\n");
			
			outputFileWriter.write("Static coverage\t\t=\t" + formatDouble(staticCoverage) + " %\n");
			outputFileWriter.write("Dynamic Coverage\t=\t" + formatDouble(dynamicCoverage) + " %\n");
			outputFileWriter.write("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	//Timing Statistics	
	static long totalNumMicroOps = 0;
	static long totalHandledCISCInsn = 0;
	static long totalPINCISCInsn = 0;
	public static long maxCoreCycles = 0;
	
		
	public static void printTimingStatistics()
	{
		long coreCyclesTaken[] = new long[SystemConfig.NoOfCores];
		for (int i =0; i < SystemConfig.NoOfCores; i++)
		{
			coreCyclesTaken[i] = cores[i].getCoreCyclesTaken();
			if (maxCoreCycles < coreCyclesTaken[i])
				maxCoreCycles = coreCyclesTaken[i];
		}
	
		
		//for each core, print number of cycles taken by pipeline to reach completion
		
		try
		{
			outputFileWriter.write("\n");
			outputFileWriter.write("[Timing Statistics]\n");
			outputFileWriter.write("\n");
			outputFileWriter.write("Total Execution Time\t\t=\t" + GlobalClock.getCurrentTime() / GlobalClock.effectiveGlobalClockFrequency + " microseconds\n\n");
			outputFileWriter.write("Total Instructions per Second\t\t=\t" + formatDouble((double)totalNumMicroOps/((double)GlobalClock.getCurrentTime() / (GlobalClock.effectiveGlobalClockFrequency * 1000000))) + "\t\tin terms of micro-ops\n");
			outputFileWriter.write("Total Instructions per Second\t\t=\t" + formatDouble((double)totalHandledCISCInsn/((double)GlobalClock.getCurrentTime() / (GlobalClock.effectiveGlobalClockFrequency * 1000000))) + "\t\tin terms of CISC instructions\n\n");
			
			for(int i = 0; i < SystemConfig.NoOfCores; i++)
			{
				if(cores[i].getNoOfInstructionsExecuted()==0){
					outputFileWriter.write("Nothing executed on core "+i+"\n");
					continue;
				}
				outputFileWriter.write("core\t\t=\t" + i + "\n");
				
				CoreConfig coreConfig = SystemConfig.core[i];
				
				outputFileWriter.write("Pipeline: " + coreConfig.pipelineType + "\n");
								
				outputFileWriter.write("instructions executed\t=\t" + cores[i].getNoOfInstructionsExecuted() + "\n");
				outputFileWriter.write("cycles taken\t=\t" + coreCyclesTaken[i] / SystemConfig.core[i].stepSize + " cycles\n");
				//FIXME will work only if java thread is 1
				// if(SimulationConfig.pinpointsSimulation == false)
				//{
					outputFileWriter.write("IPC\t\t=\t" + formatDouble((double)cores[i].getNoOfInstructionsExecuted()/(coreCyclesTaken[i] / SystemConfig.core[i].stepSize)) + "\t\tin terms of micro-ops\n");
					outputFileWriter.write("IPC\t\t=\t" + formatDouble((double)numHandledCISCInsn[0][i]/(coreCyclesTaken[i] / SystemConfig.core[i].stepSize)) + "\t\tin terms of CISC instructions\n");
				//}
				//else
				//{
				//	outputFileWriter.write("IPC\t\t=\t" + formatDouble((double)cores[i].getNoOfInstructionsExecuted()/coreCyclesTaken[i]) + "\t\tin terms of micro-ops\n");
				//	outputFileWriter.write("IPC\t\t=\t" + formatDouble((double)3000000/coreCyclesTaken[i]) + "\t\tin terms of CISC instructions\n");
				//}
				
				outputFileWriter.write("core frequency\t=\t" + cores[i].getFrequency() + " MHz\n");
				outputFileWriter.write("time taken\t=\t" + formatDouble((double)coreCyclesTaken[i]/GlobalClock.effectiveGlobalClockFrequency) + " microseconds\n");
				outputFileWriter.write("\n");
				
				if(cores[i].getCoreConfig().pipelineType == PipelineType.outOfOrder)
				{
					outputFileWriter.write("number of micro-op cache accesses = " + ((OutOrderExecutionEngine)cores[i].getExecEngine()).getMicroOpCache().numSearches + "\n");
					outputFileWriter.write("micro-op cache hit rate = " + formatDouble((double)((OutOrderExecutionEngine)cores[i].getExecEngine()).getMicroOpCache().numHits/(double)((OutOrderExecutionEngine)cores[i].getExecEngine()).getMicroOpCache().numSearches) + "\n");
					outputFileWriter.write("\n");
				}
				
				outputFileWriter.write("number of branches\t=\t" + cores[i].getExecEngine().getNumberOfBranches() + "\n");
				outputFileWriter.write("number of mispredicted branches\t=\t" + cores[i].getExecEngine().getNumberOfMispredictedBranches() + "\n");
				outputFileWriter.write("branch predictor accuracy\t=\t" + formatDouble((double)((double)(1.0 - (double)cores[i].getExecEngine().getNumberOfMispredictedBranches()/(double)cores[i].getExecEngine().getNumberOfBranches())*100.0)) + " %\n");
				outputFileWriter.write("number of jumps\t=\t" + cores[i].getExecEngine().getNumberOfJumps() + "\n");
				outputFileWriter.write("number of mispredicted jump targets\t=\t" + cores[i].getExecEngine().getNumberOfMispredictedTargets() + "\n");
				outputFileWriter.write("target predictor accuracy\t=\t" + formatDouble((double)((double)(1.0 - (double)cores[i].getExecEngine().getNumberOfMispredictedTargets()/(double)cores[i].getExecEngine().getNumberOfJumps())*100.0)) + " %\n");
				outputFileWriter.write("number of predicate instructions\t=\t" + ((OutOrderExecutionEngine)cores[i].getExecEngine()).getReorderBuffer().predicateCount + "\n");
				outputFileWriter.write("number of mispredicted predicate instructions\t=\t" + ((OutOrderExecutionEngine)cores[i].getExecEngine()).getReorderBuffer().predicateMispredCount + "\n");
				outputFileWriter.write("predicate predictor accuracy\t=\t" + formatDouble((double)((double)(1.0 - (double)((OutOrderExecutionEngine)cores[i].getExecEngine()).getReorderBuffer().predicateMispredCount/(double)((OutOrderExecutionEngine)cores[i].getExecEngine()).getReorderBuffer().predicateCount)*100.0)) + " %\n");
				outputFileWriter.write("\n");
				
				outputFileWriter.write("predictor type = " + coreConfig.branchPredictor.predictorMode + "\n");
				outputFileWriter.write("PC bits = " + coreConfig.branchPredictor.PCBits + "\n");
				outputFileWriter.write("BHR size = " + coreConfig.branchPredictor.BHRsize + "\n");
				outputFileWriter.write("Saturating bits = " + coreConfig.branchPredictor.saturating_bits + "\n");
				outputFileWriter.write("\n");
				
				outputFileWriter.write("\nIW Full stall = " + ((OutOrderExecutionEngine)cores[i].getExecEngine()).getReorderBuffer().getStall1Count());
				outputFileWriter.write("\nrename stall = " + ((OutOrderExecutionEngine)cores[i].getExecEngine()).getReorderBuffer().getStall2Count());
				outputFileWriter.write("\nLSQ Full stall = " + ((OutOrderExecutionEngine)cores[i].getExecEngine()).getReorderBuffer().getStall3Count());
				outputFileWriter.write("\nROB Full stall = " + ((OutOrderExecutionEngine)cores[i].getExecEngine()).getReorderBuffer().getStall4Count());
				outputFileWriter.write("\nMispred stall = " + ((OutOrderExecutionEngine)cores[i].getExecEngine()).getReorderBuffer().getStall5Count());
				outputFileWriter.write("\nSerialization instruction stall = " + ((OutOrderExecutionEngine)cores[i].getExecEngine()).getReorderBuffer().getStall6Count());
				outputFileWriter.write("\n");
				
			}
			outputFileWriter.write("\n");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	static void printEnergyStatistics()
	{
		EnergyConfig totalEnergy = new EnergyConfig(0, 0);
		
		try {
			// Cores
			int i = 0;
			
			outputFileWriter.write("\n\n[ComponentName LeakageEnergy DynamicEnergy TotalEnergy NumDynamicAccesses] : \n");

			EnergyConfig coreEnergy = new EnergyConfig(0, 0);
			i = 0;
			for(Core core : cores) {
				coreEnergy.add(core.calculateAndPrintEnergy(outputFileWriter, "core[" + (i++) + "]"));
			}
			
			outputFileWriter.write("\n\n");
			coreEnergy.printEnergyStats(outputFileWriter, "coreEnergy.total");
			totalEnergy.add(coreEnergy);
			
			outputFileWriter.write("\n\n");
			
			// Shared Cache
			EnergyConfig sharedCacheEnergy = new EnergyConfig(0, 0);
			for (Cache cache : ArchitecturalComponent.getSharedCacheList()) {
				sharedCacheEnergy.add(cache.calculateAndPrintEnergy(outputFileWriter, cache.toString()));
			}
			
			outputFileWriter.write("\n\n");
			sharedCacheEnergy.printEnergyStats(outputFileWriter, "sharedCacheEnergy.total");
			totalEnergy.add(sharedCacheEnergy);
						
			// Main Memory
			EnergyConfig mainMemoryEnergy = new EnergyConfig(0, 0);						
			int memControllerId = 0;
			outputFileWriter.write("\n\n");
			for(MainMemoryDRAMController memController : ArchitecturalComponent.memoryControllers) {
				String name = "MainMemoryDRAMController[" + memControllerId + "]";
				memControllerId++;
				mainMemoryEnergy.add(memController.calculateAndPrintEnergy(outputFileWriter, name));
			}
			
			outputFileWriter.write("\n");
			mainMemoryEnergy.printEnergyStats(outputFileWriter, "mainMemoryControllerEnergy.total");
			totalEnergy.add(mainMemoryEnergy);
			
			// Coherence
			EnergyConfig coherenceEnergy = new EnergyConfig(0, 0);						
			int coherenceId = 0;
			for(Coherence coherence : ArchitecturalComponent.coherences) {
				String name = "Coherence[" + coherenceId + "]";
				coherenceId++;
				coherenceEnergy.add(coherence.calculateAndPrintEnergy(outputFileWriter, name));
			}
			
			outputFileWriter.write("\n\n");
			coherenceEnergy.printEnergyStats(outputFileWriter, "coherenceEnergy.total");
			totalEnergy.add(coherenceEnergy);
			
			// Interconnect
			EnergyConfig interconnectEnergy = new EnergyConfig(0, 0);
			interconnectEnergy.add(ArchitecturalComponent.getInterConnect().calculateAndPrintEnergy(outputFileWriter, "Interconnect"));
			totalEnergy.add(interconnectEnergy);
			
			outputFileWriter.write("\n\n");
			totalEnergy.printEnergyStats(outputFileWriter, "TotalEnergy");
			
		} catch (Exception e) {
			System.err.println("error in printing stats + \nexception = " + e);
			e.printStackTrace();
		}
	}
	
	
	//Memory System Statistics
	static long totalNucaBankAccesses;
	
	public static String nocTopology;
	public static String nocRoutingAlgo;
	public static int hopcount=0;
		
	static float averageHopLength;
	static int maxHopLength;
	static int minHopLength;
	
	static long numInsWorkingSetHits[];
	static long numInsWorkingSetMisses[];
	static long maxInsWorkingSetSize[];
	static long minInsWorkingSetSize[];
	static long totalInsWorkingSetSize[];
	static long numInsWorkingSetsNoted[];
	
	static long numDataWorkingSetHits[];
	static long numDataWorkingSetMisses[];
	static long maxDataWorkingSetSize[];
	static long minDataWorkingSetSize[];
	static long totalDataWorkingSetSize[];
	static long numDataWorkingSetsNoted[];
	
	
	public static void printMemorySystemStatistics()
	{
		//for each core, print memory system statistics
		Main.printStatisticsOnAsynchronousTermination = false;
		try
		{
			outputFileWriter.write("\n");
			outputFileWriter.write("[Memory System Statistics]\n");
			outputFileWriter.write("\n");
			
			outputFileWriter.write("[Per core statistics]\n");
			outputFileWriter.write("\n");
			
			for(int i = 0; i < SystemConfig.NoOfCores; i++)
			{
				if(cores[i].getCoreCyclesTaken()==0){
					outputFileWriter.write("Nothing executed on core "+i+"\n");
					continue;
				}
				outputFileWriter.write("core\t\t=\t" + i + "\n");
				outputFileWriter.write("Memory Requests\t=\t" + coreMemSys[i].getNumberOfMemoryRequests() + "\n");
				outputFileWriter.write("Loads\t\t=\t" + coreMemSys[i].getNumberOfLoads() + "\n");
				outputFileWriter.write("Stores\t\t=\t" + coreMemSys[i].getNumberOfStores() + "\n");
				outputFileWriter.write("Software read prefetches\t\t=\t" + coreMemSys[i].getNumberOfSoftwareReadPrefetches() + "\n");
				outputFileWriter.write("Software write prefetches\t\t=\t" + coreMemSys[i].getNumberOfSoftwareWritePrefetches() + "\n");
				outputFileWriter.write("LSQ forwardings\t=\t" + coreMemSys[i].getNumberOfValueForwardings() + "\n");
				
				printCacheStatistics("iTLB[" + i + "]", coreMemSys[i].getiTLB().getTlbHits(), coreMemSys[i].getiTLB().getTlbMisses());
				printCacheStatistics("dTLB[" + i + "]", coreMemSys[i].getdTLB().getTlbHits(), coreMemSys[i].getdTLB().getTlbMisses());
				
				for(Cache c : coreMemSys[i].getCoreCacheList()) {
					printCacheStats(c);
					addToConsolidatedCacheList(c);
				}
				
				outputFileWriter.write("\n");
			}
			
			outputFileWriter.write("\n\n[Shared Caches]\n\n");
			for (Cache c : ArchitecturalComponent.getSharedCacheList()) {
				printCacheStats(c);
				addToConsolidatedCacheList(c);
			}
			
			outputFileWriter.write("\n\n[Consolidated Stats For Caches]\n\n");
			for (Map.Entry<String, Vector<Cache>> entry : consolidatedCacheList.entrySet()) {
				printConsolidatedCacheStats(entry.getKey(), entry.getValue());
			}
			
			for(String name : ArchitecturalComponent.nucaList.keySet())
			{
				NucaCache nuca = ArchitecturalComponent.nucaList.get(name);
				long access = 0;
				for(Cache bank : nuca.cacheBank)
				{
					access += bank.hits;
					access += bank.misses;
				}
				outputFileWriter.write("\n\nNUCA \t\t=\t" + name + "\n");
				outputFileWriter.write("NUCA Type\t=\t" + ArchitecturalComponent.nucaList.get(name).nucaType + "\n");
				outputFileWriter.write("Total Nuca Bank Accesses\t=\t" + access +"\n");
				outputFileWriter.write("Total Nuca Bank Migrations\t=\t" + nuca.migrations +"\n");
				outputFileWriter.write("Average number of NUCA Events\t=\t" + (float)nuca.hopCount/access +"\n");				
				
			}
			
			if (SystemConfig.interconnect == Interconnect.Noc)
			{
				outputFileWriter.write("\n\nNOC Topology\t\t=\t" + SystemConfig.nocConfig.topology + "\n");
				outputFileWriter.write("NOC Routing Algorithm\t=\t" + SystemConfig.nocConfig.rAlgo + "\n");
			}
			if(SimulationConfig.nucaType!=NucaType.NONE)
			{
				
				
				/* TODO anuj
				double totalNucaBankPower = (totalNucaBankAccesses*PowerConfig.dcache2Power)/executionTime;
				outputFileWriter.write("Total Nuca Bank Accesses Power\t=\t" + totalNucaBankPower + "\n");
				double totalRouterPower = ((hopcount*(PowerConfig.linkEnergy+PowerConfig.totalRouterEnergy))/executionTime);
				
				
				if (hopcount != 0)
				{
					outputFileWriter.write("Router Hops\t=\t" + hopcount + "\n");
					outputFileWriter.write("Total Router Power\t=\t" + totalRouterPower + "\n");
				}*/
								
				/* TODO anuj
				double totalBufferPower = (Switch.totalBufferAccesses*PowerConfig.bufferEnergy)/executionTime;
				outputFileWriter.write("Total Buffer Accesses\t=\t" + Switch.totalBufferAccesses + "\n");
				if(totalBufferPower!=0)
					outputFileWriter.write("Total Buffer Power\t=\t" + totalBufferPower + "\n");
				outputFileWriter.write("Total NUCA Dynamic Power\t=\t" 
								+ (totalNucaBankPower
								+  totalRouterPower
								+ totalBufferPower) + "\n");
				*/
			}
			
			if(SystemConfig.memControllerToUse==true){							
			outputFileWriter.write("\n[RAM statistics]\n\n");
			long totalReadAndWrite= 0L;
			long totalReadRank= 0L;
			long totalWriteRank= 0L;
			long totalReadTransactions[][];
			long totalWriteTransactions[][];
			double avgLatency;

			for(int k=0; k < SystemConfig.mainMemoryConfig.numChans; k++)
			{
				outputFileWriter.write("For channel " + k + ":\n");
				avgLatency = ArchitecturalComponent.getMainMemoryDRAMController(null,k).getAverageLatency() / SystemConfig.mainMemoryStepSize;
				outputFileWriter.write("Average Read Latency: " + avgLatency + " cycles = " + (avgLatency/cores[0].getFrequency() * 1000) + " ns\n");
				totalReadTransactions = ArchitecturalComponent.getMainMemoryDRAMController(null,k).getTotalReadTransactions();
				totalWriteTransactions = ArchitecturalComponent.getMainMemoryDRAMController(null,k).getTotalWriteTransactions();
				totalReadAndWrite=0L;			
		
				for(int i=0;i<SystemConfig.mainMemoryConfig.numRanks;i++){
					
					outputFileWriter.write("\t Rank "+(i+1)+"\n");
					
					for(int j=0;j<SystemConfig.mainMemoryConfig.numBanks;j++){
						outputFileWriter.write("\t\t Bank "+(j+1)+" :: ");
						outputFileWriter.write(" Reads : " +totalReadTransactions[i][j] + " | Writes: "+totalWriteTransactions[i][j] +"\n\n");
						totalReadAndWrite += totalReadTransactions[i][j] + totalWriteTransactions[i][j];
						totalReadRank += totalReadTransactions[i][j];
						totalWriteRank += totalWriteTransactions[i][j];
						}

					outputFileWriter.write("\t Total Reads: " + totalReadRank);
					outputFileWriter.write("\t Total Writes: " + totalWriteRank + "\n");
					totalReadRank = 0L;
					totalWriteRank = 0L;

				}
				outputFileWriter.write("\nTotal Reads and Writes: " + (totalReadAndWrite*64) + " Bytes\n");
				outputFileWriter.write("Total Bandwidth: "+ (totalReadAndWrite*64)/(double)((maxCoreCycles/SystemConfig.mainMemoryStepSize)/SystemConfig.mainMemoryConfig.cpu_ram_ratio * SystemConfig.mainMemoryConfig.tCK)/(1024*1024*1024) * 1000000000 + " GB/s\n");
				outputFileWriter.write("\n");
			}
			}
			outputFileWriter.write("\n\n");
			
			for(Coherence coherence : ArchitecturalComponent.coherences) {
				coherence.printStatistics(outputFileWriter);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	static Hashtable<String, Vector<Cache>> consolidatedCacheList = 
			new Hashtable<String, Vector<Cache>>();
	
	private static void addToConsolidatedCacheList(Cache c) {
		Vector<Cache> cacheList = consolidatedCacheList.get(c.cacheConfig.cacheName);
		if(cacheList==null) {
			cacheList = new Vector<Cache>();
			cacheList.add(c);
			consolidatedCacheList.put(c.cacheConfig.cacheName, cacheList);
		}  else {
			if(cacheList.contains(c)==true) {
				misc.Error.showErrorAndExit("This cache has already been added to cache list " + c + " " + cacheList.toString());
			} else {
				cacheList.add(c);
			}
		}
	}

	static void printStatisticsForACache(String name, long requests, 
		long hits, long misses, long halfmisses, long combinedWrites, long numberOfCompulsoryMisses, long evictions, long accesses, double avgNumEventsInMSHR, double avgNumEventsInMSHREntry) throws IOException 
	{
		outputFileWriter.write("\n");
		outputFileWriter.write("\n" + name + " Requests\t=\t" + requests);
		outputFileWriter.write("\n" + name + " Hits\t=\t" + hits);
		outputFileWriter.write("\n" + name + " Full-Misses\t=\t" + misses);
		outputFileWriter.write("\n" + name + " Half-Misses\t=\t" + halfmisses);
		outputFileWriter.write("\n" + name + " Combined-Writes\t=\t" + combinedWrites);
		if(Cache.toCountCompulsoryMisses)
			outputFileWriter.write("\n" + name + " Compulsory Misses\t=\t" + numberOfCompulsoryMisses);
		outputFileWriter.write("\n" + name + " Accesses\t=\t" + accesses);
		
		float hitrate = (float)hits/(float)(requests);
		outputFileWriter.write("\n" + name + " Hit-Rate\t=\t" + hitrate);
		
		float missrate = (float)(misses+halfmisses)/(float)(requests);
		outputFileWriter.write("\n" + name + " Miss-Rate\t=\t" + missrate);
		
		outputFileWriter.write("\n" + name + " AvgNumEventsInMSHR\t=\t" + formatDouble(avgNumEventsInMSHR));
		outputFileWriter.write("\n" + name + " AvgNumEventsInMSHREntry\t=\t" + formatDouble(avgNumEventsInMSHREntry));
		
		//outputFileWriter.write("\n" + name + " Evictions\t=\t" + evictions);
		outputFileWriter.write("\n");
	}
	
	static void printCacheStats(Cache c) throws IOException
	{
		printStatisticsForACache(c.toString(), c.noOfRequests, c.hits, c.misses, c.halfmisses, c.combinedWrites, c.numberOfCompulsoryMisses, c.evictions, c.noOfAccesses, c.getAvgNumEventsPendingInMSHR(), c.getAvgNumEventsPendingInMSHREntry());
	}
	
	static void printConsolidatedCacheStats(String cacheName, 
			Vector<Cache> cacheArray) throws IOException
	{
		long requests = 0, hits = 0, misses = 0, halfmisses = 0, combinedWrites = 0, compulsoryMisses = 0, evictions = 0, accesses = 0;
		
		for(Cache c : cacheArray) {
			requests += c.noOfRequests;
			hits += c.hits;
			misses += c.misses;
			halfmisses += c.halfmisses;
			combinedWrites += c.combinedWrites;
			compulsoryMisses += c.numberOfCompulsoryMisses;
			evictions += c.evictions;
			accesses = c.noOfAccesses;
		}
		
		int numCaches = 0;
		double avgNumEventsInMSHR = 0, avgNumEventsInMSHREntry = 0;
		for(Cache c : cacheArray) {
			if((c.hits+c.misses)==0) {
				continue;
			}
			
			numCaches++;
			avgNumEventsInMSHR += c.getAvgNumEventsPendingInMSHR(); 
			avgNumEventsInMSHREntry += c.getAvgNumEventsPendingInMSHREntry();
		}
		
		avgNumEventsInMSHR = avgNumEventsInMSHR / (double)numCaches;
		avgNumEventsInMSHREntry = avgNumEventsInMSHREntry / (double)numCaches;
		
		printStatisticsForACache(cacheName, requests, hits, misses, halfmisses, combinedWrites, compulsoryMisses, evictions, accesses, avgNumEventsInMSHR, avgNumEventsInMSHREntry);
	}
	
	static void printInsWorkingSetStats() throws IOException {
		long insMaxWorkingSet = Long.MIN_VALUE; 
		long insMinWorkingSet = Long.MAX_VALUE;
		long insTotalWorkingSet = 0, insNumWorkingSetNoted = 0;
		long insWorkingSetHits = 0, insWorkingSetMisses = 0;
		
		outputFileWriter.write("\n\nPer Core Ins Working Set Stats : \n");
		for(int i=0; i<SystemConfig.NoOfCores; i++) {
			if(numInsWorkingSetHits[i]==0 && numInsWorkingSetMisses[i]==0) {
				continue;
			} else {
				// Min, Avg, Max working set of the core
				// Hitrate for the working set
				outputFileWriter.write("\nMinInsWorkingSet[" + i + "]\t=\t" + 
						minInsWorkingSetSize[i]);
				
				if(minInsWorkingSetSize[i]<insMinWorkingSet) {
					insMinWorkingSet = minInsWorkingSetSize[i];
				}
				
				outputFileWriter.write("\nAvgInsWorkingSet[" + i + "]\t=\t" + 
						(float)totalInsWorkingSetSize[i]/(float)numInsWorkingSetsNoted[i]);
				
				insTotalWorkingSet += totalInsWorkingSetSize[i];
				insNumWorkingSetNoted += numInsWorkingSetsNoted[i];
				
				outputFileWriter.write("\nMaxInsWorkingSet[" + i + "]\t=\t" + 
						maxInsWorkingSetSize[i]);
				
				if(maxInsWorkingSetSize[i]>insMaxWorkingSet) {
					insMaxWorkingSet = maxInsWorkingSetSize[i];
				}
				
				outputFileWriter.write("\nInsWorkingSetHitrate[" + i + "]\t=\t" + 
						(float)numInsWorkingSetHits[i]/(float)(numInsWorkingSetHits[i] + numInsWorkingSetMisses[i]));
				
				insWorkingSetHits += numInsWorkingSetHits[i];
				insWorkingSetMisses += numInsWorkingSetMisses[i];
			}
			outputFileWriter.write("\n");
		}
		
		outputFileWriter.write("\n\nTotal Ins Working Set Stats : \n");
		outputFileWriter.write("\nMinInsWorkingSet\t=\t" + insMinWorkingSet);
		outputFileWriter.write("\nAvgInsWorkingSet\t=\t" + formatDouble((float)insTotalWorkingSet/(float)insNumWorkingSetNoted));
		outputFileWriter.write("\nMaxInsWorkingSet\t=\t" + insMaxWorkingSet);
		
		float hitrate = (float)insWorkingSetHits/(float)(insWorkingSetHits+insWorkingSetMisses);
		outputFileWriter.write("\nInsWorkingSetHitrate\t=\t" + formatDouble(hitrate));
		
		outputFileWriter.write("\n\n");
	}
	
	
	static void printDataWorkingSetStats() throws IOException {
		long dataMaxWorkingSet = Long.MIN_VALUE; 
		long dataMinWorkingSet = Long.MAX_VALUE;
		long dataTotalWorkingSet = 0, dataNumWorkingSetNoted = 0;
		long dataWorkingSetHits = 0, dataWorkingSetMisses = 0;
		
		outputFileWriter.write("\n\nPer Core Data Working Set Stats : \n");
		for(int i=0; i<SystemConfig.NoOfCores; i++) {
			if(numDataWorkingSetHits[i]==0 && numDataWorkingSetMisses[i]==0) {
				continue;
			} else {
				// Min, Avg, Max working set of the core
				// Hitrate for the working set
				outputFileWriter.write("\nMinDataWorkingSet[" + i + "]\t=\t" + 
						minDataWorkingSetSize[i]);
				
				if(minDataWorkingSetSize[i]<dataMinWorkingSet) {
					dataMinWorkingSet = minDataWorkingSetSize[i];
				}
				
				outputFileWriter.write("\nAvgDataWorkingSet[" + i + "]\t=\t" + 
						formatDouble((float)totalDataWorkingSetSize[i]/(float)numDataWorkingSetsNoted[i]));
				
				dataTotalWorkingSet += totalDataWorkingSetSize[i];
				dataNumWorkingSetNoted += numDataWorkingSetsNoted[i];
				
				outputFileWriter.write("\nMaxDataWorkingSet[" + i + "]\t=\t" + 
						maxDataWorkingSetSize[i]);
				
				if(maxDataWorkingSetSize[i]>dataMaxWorkingSet) {
					dataMaxWorkingSet = maxDataWorkingSetSize[i];
				}
				
				outputFileWriter.write("\nDataWorkingSetHitrate[" + i + "]\t=\t" + 
						formatDouble((float)numDataWorkingSetHits[i]/(float)(numDataWorkingSetHits[i] + numDataWorkingSetMisses[i])));
				
				dataWorkingSetHits += numDataWorkingSetHits[i];
				dataWorkingSetMisses += numDataWorkingSetMisses[i];
			}
			outputFileWriter.write("\n");
		}
		
		outputFileWriter.write("\n\nTotal Data Working Set Stats : \n");
		outputFileWriter.write("\nMinDataWorkingSet\t=\t" + dataMinWorkingSet);
		outputFileWriter.write("\nAvgDataWorkingSet\t=\t" + (float)dataTotalWorkingSet/(float)dataNumWorkingSetNoted);
		outputFileWriter.write("\nMaxDataWorkingSet\t=\t" + dataMaxWorkingSet);
		
		float hitrate = (float)dataWorkingSetHits/(float)(dataWorkingSetHits+dataWorkingSetMisses);
		outputFileWriter.write("\nDataWorkingSetHitrate\t=\t" + formatDouble(hitrate));
		
		outputFileWriter.write("\n\n");
	}
	
	static void printCacheStatistics(String cacheStr,
			long hits, long misses) throws IOException
	{
		outputFileWriter.write("\n\n" + cacheStr + " Hits\t=\t" + hits);
		outputFileWriter.write("\n" + cacheStr + " Misses\t=\t" + misses);
		outputFileWriter.write("\n" + cacheStr + " Accesses\t=\t" + (hits+misses));
		outputFileWriter.write("\n" + cacheStr + " Hit-Rate\t=\t" + formatDouble((double)hits/(double)(hits+misses)));
		outputFileWriter.write("\n" + cacheStr + " Miss-Rate\t=\t" + formatDouble((double)misses/(double)(hits+misses)));
	}
	
	static void printCacheStatistics(Cache cache) throws IOException
	{
		outputFileWriter.write("\n\nCache : ");
		/*if(cache.getName() != null)
		{
			outputFileWriter.write(cache.getName());
		}
		if(cache.getContainingMemSys().length == 1)
		{
			outputFileWriter("\nPrivate to core " + cache.getContainingMemSys()[0].getCore().getCore_number());
		}
		else
		{
			outputFileWriter("\nShared between cores ");
			for(int i = 0; i < cache.getContainingMemSys().length; i++)
			{
				outputFileWriter.write(cache.getContainingMemSys()[i].getCore().getCore_number() + " ");
			}
		}*/
		outputFileWriter.write("\nRequests\t=\t" + cache.hits+cache.misses);
		outputFileWriter.write("\nHits\t=\t" + cache.hits);
		outputFileWriter.write("\nMisses\t=\t" + cache.misses);
		outputFileWriter.write("\nHit-Rate\t=\t" + formatDouble((double)cache.hits/(double)(cache.hits+cache.misses)));
		outputFileWriter.write("\nMiss-Rate\t=\t" + formatDouble((double)cache.misses/(double)(cache.hits+cache.misses)));
	}
	
	//Simulation time
	//static long time;
	//static long subsetTime;
	private static long simulationTime;

	public static void printSimulationTime()
	{
		//print time taken by simulator
		long seconds = simulationTime/1000;
		long minutes = seconds/60;
		seconds = seconds%60;
		try
		{
			outputFileWriter.write("\n");
			outputFileWriter.write("[Simulator Time]\n");
			
			outputFileWriter.write("Time Taken\t\t=\t" + minutes + " : " + seconds + " minutes\n");
			
			outputFileWriter.write("Instructions per Second\t=\t" + 
					formatDouble((double)totalNumMicroOps/simulationTime) + " KIPS\t\tin terms of micro-ops\n");
			outputFileWriter.write("Instructions per Second\t=\t" + 
					formatDouble((double)totalHandledCISCInsn/simulationTime) + " KIPS\t\tin terms of CISC instructions\n");
			
			outputFileWriter.write("\n");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}




	public static void initStatistics()
	{		
		dataRead = new long[SystemConfig.maxNumJavaThreads];
		numHandledCISCInsn = new long[SystemConfig.maxNumJavaThreads][SystemConfig.numEmuThreadsPerJavaThread];
		numCISCInsn = new long[SystemConfig.maxNumJavaThreads][SystemConfig.numEmuThreadsPerJavaThread];
		noOfMicroOps = new long[SystemConfig.maxNumJavaThreads][SystemConfig.numEmuThreadsPerJavaThread];
		
		if(SimulationConfig.collectInsnWorkingSetInfo==true) {
			numInsWorkingSetHits = new long[SystemConfig.NoOfCores];
			numInsWorkingSetMisses = new long[SystemConfig.NoOfCores];
			maxInsWorkingSetSize = new long[SystemConfig.NoOfCores];
			minInsWorkingSetSize = new long[SystemConfig.NoOfCores];
			totalInsWorkingSetSize = new long[SystemConfig.NoOfCores];
			numInsWorkingSetsNoted = new long[SystemConfig.NoOfCores];
		}
		
		if(SimulationConfig.collectDataWorkingSetInfo==true) {
			numDataWorkingSetHits = new long[SystemConfig.NoOfCores];
			numDataWorkingSetMisses = new long[SystemConfig.NoOfCores];
			maxDataWorkingSetSize = new long[SystemConfig.NoOfCores];
			minDataWorkingSetSize = new long[SystemConfig.NoOfCores];
			totalDataWorkingSetSize = new long[SystemConfig.NoOfCores];
			numDataWorkingSetsNoted = new long[SystemConfig.NoOfCores];
		}
		
		if(SimulationConfig.pinpointsSimulation == true)
		{		
			
		}
		
	}	

	public static void openStream()
	{
		if(SimulationConfig.outputFileName == null)
		{
			SimulationConfig.outputFileName = "default";
		}
		
		try {
			File outputFile = new File(SimulationConfig.outputFileName);
			
			if(outputFile.exists()) {
				
				// rename the previous output file
				Date lastModifiedDate = new Date(outputFile.lastModified());
				File backupFile = new File(SimulationConfig.outputFileName + "_" + lastModifiedDate.toString());
				if(!outputFile.renameTo(backupFile)) {
					System.err.println("error in creating a backup of your previous output file !!\n");
				}
				
				// again point to the new file
				outputFile = new File(SimulationConfig.outputFileName);
			}
			
			outputFileWriter = new FileWriter(outputFile);
			
			
		} catch (IOException e) {
			
			StringBuilder sb = new StringBuilder();
			sb.append("DEFAULT_");
		    Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
			sb.append(sdf.format(cal.getTime()));
			try
			{
				outputFileWriter = new FileWriter(sb.toString());
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
			System.out.println("unable to create specified output file");
			System.out.println("statistics written to " + sb.toString());
		}
	}
	
	public static void closeStream()
	{
		try {
			outputFileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}




	public static void setDataRead(long dataRead, int thread) {
		Statistics.dataRead[thread] = dataRead;
	}

	public static long getNumHandledCISCInsn(int javaThread, int emuThread) {
		return Statistics.numHandledCISCInsn[javaThread][emuThread];
	}

	public static void setNumHandledCISCInsn(long numInstructions, int javaThread, int emuThread) {
		Statistics.numHandledCISCInsn[javaThread][emuThread] = numInstructions;
		// PinPointsProcessing.toProcessEndOfSlice(numHandledCISCInsn[javaThread][emuThread]);
	}
	
	public static void setNumCISCInsn(long numInstructions, int javaThread, int emuThread) {
		Statistics.numCISCInsn[javaThread][emuThread] = numInstructions;
	}

	public static void setNoOfMicroOps(long noOfMicroOps[], int thread) {
		Statistics.noOfMicroOps[thread] = noOfMicroOps;
	}
	
	public static void setStaticCoverage(double staticCoverage) {
		Statistics.staticCoverage = staticCoverage;
	}
	
	// Ins working set
	public static void setMaxInsWorkingSetSize(long workingSetSize, int core) {
		Statistics.maxInsWorkingSetSize[core] = workingSetSize;
	}
	
	public static void setMinInsWorkingSetSize(long workingSetSize, int core) {
		Statistics.minInsWorkingSetSize[core] = workingSetSize;
	}
	
	public static void setTotalInsWorkingSetSize(long workingSetSize, int core) {
		Statistics.totalInsWorkingSetSize[core] = workingSetSize;
	}
	
	public static void setNumInsWorkingSetNoted(long workingSetNoted, int core) {
		Statistics.numInsWorkingSetsNoted[core] = workingSetNoted;
	}
	
	public static void setNumInsWorkingSetHits(long workingSetHits, int core) {
		Statistics.numInsWorkingSetHits[core] = workingSetHits;
	}
	
	public static void setNumInsWorkingSetMisses(long workingSetMisses, int core) {
		Statistics.numInsWorkingSetMisses[core] = workingSetMisses;
	}
	
	
	// Data working set
	public static void setMaxDataWorkingSetSize(long workingSetSize, int core) {
		Statistics.maxDataWorkingSetSize[core] = workingSetSize;
	}
	
	public static void setMinDataWorkingSetSize(long workingSetSize, int core) {
		Statistics.minDataWorkingSetSize[core] = workingSetSize;
	}
	
	public static void setTotalDataWorkingSetSize(long workingSetSize, int core) {
		Statistics.totalDataWorkingSetSize[core] = workingSetSize;
	}
	
	public static void setNumDataWorkingSetNoted(long workingSetNoted, int core) {
		Statistics.numDataWorkingSetsNoted[core] = workingSetNoted;
	}
	
	public static void setNumDataWorkingSetHits(long workingSetHits, int core) {
		Statistics.numDataWorkingSetHits[core] = workingSetHits;
	}
	
	public static void setNumDataWorkingSetMisses(long workingSetMisses, int core) {
		Statistics.numDataWorkingSetMisses[core] = workingSetMisses;
	}
	
	public static void setSimulationTime(long simulationTime) {
		Statistics.simulationTime = simulationTime;
	}

	public static void setExecutable(String executableFile) {
		Statistics.benchmark = executableFile;
	}
		
	public static void printAllStatistics(String benchmarkName, 
			long startTime, long endTime) {
		//set up statistics module
		//Statistics.initStatistics();
		// Statistics.initStatistics();
		cores = ArchitecturalComponent.getCores();
		coreMemSys = ArchitecturalComponent.getCoreMemSysArray();

		Statistics.setExecutable(benchmarkName);
		
		//TODO : NUCA stats not being printed !!
		//printNucaStats();
			
		Statistics.setSimulationTime(endTime - startTime);
		
		//print statistics
		Statistics.openStream();
		Statistics.printSystemConfig();
		Statistics.printTranslatorStatistics();
		Statistics.printTimingStatistics();
		Statistics.printMemorySystemStatistics();
		
		try {
			if(SimulationConfig.collectInsnWorkingSetInfo) {
				Statistics.printInsWorkingSetStats();
			}
			
			if(SimulationConfig.collectDataWorkingSetInfo) {
				Statistics.printDataWorkingSetStats();
			}
		} catch (IOException e) {
			
		}
		
		Statistics.printSimulationTime();
		Statistics.printEnergyStatistics();
		
		// Qemu translation cache stats
		if(TranslatedInstructionCache.getHitRate()!=-1) {
			try {
				outputFileWriter.write("[Qemu translation cache]\n");
				outputFileWriter.write("Hit-rate = " + (TranslatedInstructionCache.getHitRate() * 100 ) + " %");
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
		Statistics.closeStream();
	}
	
	private static void printNucaStats() {
		for(Cache cache : ArchitecturalComponent.getCacheList())
		{
			if(cache.getClass()!=Cache.class)
			{
//				if (((NucaCache)cache).nucaType != NucaType.NONE )
//				{
//					averageHopLength = ((NucaCache)cache).getAverageHoplength(); 
//					maxHopLength = ((NucaCache)cache).getMaxHopLength();
//					minHopLength = ((NucaCache)cache).getMinHopLength();
//					Statistics.nocTopology = ((NocInterface)((NucaCache)cache).cacheBank.get(0).comInterface).getRouter().topology.name();
//					Statistics.nocRoutingAlgo = ((NocInterface)((NucaCache)cache).cacheBank.get(0).comInterface).getRouter().rAlgo.name();
//					for(int i=0;i< ((NucaCache)cache).cacheRows;i++)
//					{
//						Statistics.hopcount += ((NocInterface)((NucaCache)cache).cacheBank.get(i).comInterface).getRouter().hopCounters; 
//					}
//					if(Statistics.nocTopology.equals("FATTREE") ||
//							Statistics.nocTopology.equals("OMEGA") ||
//							Statistics.nocTopology.equals("BUTTERFLY")) {
//						for(int k = 0 ; k<((NucaCache)cache).noc.intermediateSwitch.size();k++){
//							Statistics.hopcount += ((NucaCache)cache).noc.intermediateSwitch.get(k).hopCounters;
//						}
//					}
//				}
//				Statistics.totalNucaBankAccesses = ((NucaCache)cache).getTotalNucaBankAcesses();
			}
			
		}
		
	}

	public static long getNumCISCInsn(int javaTid, int tidEmu) {
		return numCISCInsn[javaTid][tidEmu];
	}
	
	public static String formatFloat(float f)
	{
		return String.format("%.4f", f);
	}
	
	public static String formatDouble(double d)
	{
		return String.format("%.4f", d);
	}
	
}
