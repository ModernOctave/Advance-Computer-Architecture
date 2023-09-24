package generic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Enumeration;

import main.ArchitecturalComponent;
import memorysystem.Cache;
import memorysystem.MemorySystem;
import config.SimulationConfig;
import config.SystemConfig;

/*
 * the config file is used to specify if pinpoints simulation is to be used
 * a sample pinpoints file is of the form :
 * 2 0.17 0.166667
 * 5 0.33 0.333333
 * 12 0.5 0.5
 * 
 * this file is sent to PIN when the simulation begins. the first column refers
 * to slice number, and the second and third columns give the weight in different
 * precisions. note that the slice numbers must be in ascending order.
 * 
 * each slice is 3000000 CISC (x86) instructions long. so PIN would send to the java
 * process the packets corresponding to instructions 6000001 - 9000000,
 * 15000001 - 18000000 and 36000001 - 3900000.
 * 
 *  the java process will simulate these slices and apply the appropriate weights
 *  to all statistics
 */

public class PinPointsProcessing {
	
	static float[] weightsArray;
	static int currentSlice;
	
	static long coreCyclesTaken[];
	static long coreFrequencies[];				//in MHz
	static long numCoreInstructions[];
	static long branchCount[];
	static long mispredictedBranchCount[];	
	static long noOfMemRequests[];
	static long noOfLoads[];
	static long noOfStores[];
	static long noOfValueForwards[];
	static long noOfiTLBRequests[];
	static long noOfiTLBHits[];
	static long noOfiTLBMisses[];
	static long noOfdTLBRequests[];
	static long noOfdTLBHits[];
	static long noOfdTLBMisses[];
	static long noOfCacheRequests[];
	static long noOfCacheHits[];
	static long noOfCacheMisses[];
	static long noOfL2Requests;
	static long noOfL2Hits;
	static long noOfL2Misses;
	static long totalNucaBankAccesses;
	static long noOfDirHits;
	static long noOfDirMisses;
	static long noOfDirDataForwards;
	static long noOfDirInvalidations;
	static long noOfDirWritebacks;
	
	static long tempcoreCyclesTaken[];
	static long tempnumCoreInstructions[];
	static long tempbranchCount[];
	static long tempmispredictedBranchCount[];
	static long tempnoOfMemRequests[];
	static long tempnoOfLoads[];
	static long tempnoOfStores[];
	static long tempnoOfValueForwards[];
	static long tempnoOfiTLBRequests[];
	static long tempnoOfiTLBHits[];
	static long tempnoOfiTLBMisses[];
	static long tempnoOfdTLBRequests[];
	static long tempnoOfdTLBHits[];
	static long tempnoOfdTLBMisses[];
	static long tempnoOfCacheRequests[];
	static long tempnoOfCacheHits[];
	static long tempnoOfCacheMisses[];
	static long tempnoOfL2Requests;
	static long tempnoOfL2Hits;
	static long tempnoOfL2Misses;
	static long tempnoOfDirHits;
	static long tempnoOfDirMisses;
	static long tempnoOfDirDataForwards;
	static long tempnoOfDirInvalidations;
	static long tempnoOfDirWritebacks;
	
	public static void initialize()
	{
		if(SimulationConfig.pinpointsSimulation == false)
		{
			return;
		}
		
		FileReader pinpointsFileReader = null;
		BufferedReader pinpointsBufferedReader = null;
		int numberOfSlices = 0;
		String lineRead;
		
		try {
			pinpointsFileReader = new FileReader(SimulationConfig.pinpointsFile);
			pinpointsBufferedReader = new BufferedReader(pinpointsFileReader);
			
			while((lineRead = pinpointsBufferedReader.readLine()) != null)
			{
				numberOfSlices++;
			}
			
			weightsArray = new float[numberOfSlices];

			pinpointsBufferedReader.close();
			pinpointsFileReader.close();
			pinpointsFileReader = new FileReader(SimulationConfig.pinpointsFile);
			pinpointsBufferedReader = new BufferedReader(pinpointsFileReader);
			int index = 0;
			
			while((lineRead = pinpointsBufferedReader.readLine()) != null)
			{
				String subs[] = lineRead.split("[ \t]");
				weightsArray[index++] = Float.parseFloat(subs[2]);
			}			
			
		} catch (Exception e) {
			misc.Error.showErrorAndExit("pinpoints file not found");
			e.printStackTrace();
		}
		
		currentSlice = 0;
		
		coreCyclesTaken = new long[SystemConfig.NoOfCores];
		coreFrequencies = new long[SystemConfig.NoOfCores];
		numCoreInstructions = new long[SystemConfig.NoOfCores];
		branchCount = new long[SystemConfig.NoOfCores];
		mispredictedBranchCount = new long[SystemConfig.NoOfCores];
		
		noOfMemRequests = new long[SystemConfig.NoOfCores];
		noOfLoads = new long[SystemConfig.NoOfCores];
		noOfStores = new long[SystemConfig.NoOfCores];
		noOfValueForwards = new long[SystemConfig.NoOfCores];
		noOfiTLBRequests = new long[SystemConfig.NoOfCores];
		noOfiTLBHits = new long[SystemConfig.NoOfCores];
		noOfiTLBMisses = new long[SystemConfig.NoOfCores];
		noOfdTLBRequests = new long[SystemConfig.NoOfCores];
		noOfdTLBHits = new long[SystemConfig.NoOfCores];
		noOfdTLBMisses = new long[SystemConfig.NoOfCores];
		noOfCacheRequests = new long[SystemConfig.NoOfCores];
		noOfCacheHits = new long[SystemConfig.NoOfCores];
		noOfCacheMisses = new long[SystemConfig.NoOfCores];

		tempcoreCyclesTaken = new long[SystemConfig.NoOfCores];
		tempnumCoreInstructions = new long[SystemConfig.NoOfCores];
		tempbranchCount = new long[SystemConfig.NoOfCores];
		tempmispredictedBranchCount = new long[SystemConfig.NoOfCores];

		tempnoOfMemRequests = new long[SystemConfig.NoOfCores];
		tempnoOfLoads = new long[SystemConfig.NoOfCores];
		tempnoOfStores = new long[SystemConfig.NoOfCores];
		tempnoOfValueForwards = new long[SystemConfig.NoOfCores];
		tempnoOfiTLBRequests = new long[SystemConfig.NoOfCores];
		tempnoOfiTLBHits = new long[SystemConfig.NoOfCores];
		tempnoOfiTLBMisses = new long[SystemConfig.NoOfCores];
		tempnoOfdTLBRequests = new long[SystemConfig.NoOfCores];
		tempnoOfdTLBHits = new long[SystemConfig.NoOfCores];
		tempnoOfdTLBMisses = new long[SystemConfig.NoOfCores];
		tempnoOfCacheRequests = new long[SystemConfig.NoOfCores];
		tempnoOfCacheHits = new long[SystemConfig.NoOfCores];
		tempnoOfCacheMisses = new long[SystemConfig.NoOfCores];
	}
	
	public static void processEndOfSlice()
	{
		if(SimulationConfig.pinpointsSimulation == false)
		{
			return;
		}
		
		Core core;
		
		if(currentSlice < weightsArray.length)
		{
		
			for(int i = 0; i < ArchitecturalComponent.getCores().length; i++)
			{
				core = ArchitecturalComponent.getCores()[i];
				
				if(core.getNoOfInstructionsExecuted() == 0)
				{
					continue;
				}
				
				System.out.println("\n\n!!!!!!process end of slice!!!!!!!!\n\n");
				System.out.print(coreCyclesTaken[i] + " : " + GlobalClock.getCurrentTime()/core.getStepSize());
				System.out.println(" : " + tempcoreCyclesTaken[i]);
				
				coreCyclesTaken[i] += (long) (GlobalClock.getCurrentTime()/core.getStepSize() - tempcoreCyclesTaken[i]) * weightsArray[currentSlice];
				tempcoreCyclesTaken[i] = GlobalClock.getCurrentTime()/core.getStepSize();
				System.out.println(coreCyclesTaken[i] + " : " + tempcoreCyclesTaken[i]);
				System.out.println();
				coreFrequencies[i] = core.getFrequency();
				numCoreInstructions[i] += (long) (core.getNoOfInstructionsExecuted() - tempnumCoreInstructions[i]) * weightsArray[currentSlice];
				tempnumCoreInstructions[i] = core.getNoOfInstructionsExecuted();
				branchCount[i] += (long) (core.getExecEngine().getNumberOfBranches() - tempbranchCount[i]) * weightsArray[currentSlice];
				tempbranchCount[i] = core.getExecEngine().getNumberOfBranches();
				mispredictedBranchCount[i] += (long) (core.getExecEngine().getNumberOfMispredictedBranches() - tempmispredictedBranchCount[i]) * weightsArray[currentSlice];
				tempmispredictedBranchCount[i] = core.getExecEngine().getNumberOfMispredictedBranches();
				
				noOfMemRequests[i] += (long) (core.getExecEngine().getCoreMemorySystem().getNumberOfMemoryRequests() - tempnoOfMemRequests[i]) * weightsArray[currentSlice];
				tempnoOfMemRequests[i] = core.getExecEngine().getCoreMemorySystem().getNumberOfMemoryRequests();
				noOfLoads[i] += (long) (core.getExecEngine().getCoreMemorySystem().getNumberOfLoads() - tempnoOfLoads[i]) * weightsArray[currentSlice];
				tempnoOfLoads[i] = core.getExecEngine().getCoreMemorySystem().getNumberOfLoads();
				noOfStores[i] += (long) (core.getExecEngine().getCoreMemorySystem().getNumberOfStores() - tempnoOfStores[i]) * weightsArray[currentSlice];
				tempnoOfStores[i] = core.getExecEngine().getCoreMemorySystem().getNumberOfStores();
				noOfValueForwards[i] += (long) (core.getExecEngine().getCoreMemorySystem().getNumberOfValueForwardings() - tempnoOfValueForwards[i]) * weightsArray[currentSlice];
				tempnoOfValueForwards[i] = core.getExecEngine().getCoreMemorySystem().getNumberOfValueForwardings();
				
				noOfiTLBRequests[i] += (long) (core.getExecEngine().getCoreMemorySystem().getiTLB().getTlbRequests() - tempnoOfiTLBRequests[i]) * weightsArray[currentSlice];
				tempnoOfiTLBRequests[i] = core.getExecEngine().getCoreMemorySystem().getiTLB().getTlbRequests();
				noOfiTLBHits[i] += (long) (core.getExecEngine().getCoreMemorySystem().getiTLB().getTlbHits() - tempnoOfiTLBHits[i]) * weightsArray[currentSlice];
				tempnoOfiTLBHits[i] = core.getExecEngine().getCoreMemorySystem().getiTLB().getTlbHits();
				noOfiTLBMisses[i] += (long) (core.getExecEngine().getCoreMemorySystem().getiTLB().getTlbMisses() - tempnoOfiTLBMisses[i]) * weightsArray[currentSlice];
				tempnoOfiTLBMisses[i] = core.getExecEngine().getCoreMemorySystem().getiTLB().getTlbMisses();
				noOfdTLBRequests[i] += (long) (core.getExecEngine().getCoreMemorySystem().getdTLB().getTlbRequests() - tempnoOfdTLBRequests[i]) * weightsArray[currentSlice];
				tempnoOfdTLBRequests[i] = core.getExecEngine().getCoreMemorySystem().getdTLB().getTlbRequests();
				noOfdTLBHits[i] += (long) (core.getExecEngine().getCoreMemorySystem().getdTLB().getTlbHits() - tempnoOfdTLBHits[i]) * weightsArray[currentSlice];
				tempnoOfdTLBHits[i] = core.getExecEngine().getCoreMemorySystem().getdTLB().getTlbHits();
				noOfdTLBMisses[i] += (long) (core.getExecEngine().getCoreMemorySystem().getdTLB().getTlbMisses() - tempnoOfdTLBMisses[i]) * weightsArray[currentSlice];
				tempnoOfdTLBMisses[i] = core.getExecEngine().getCoreMemorySystem().getdTLB().getTlbMisses();
			}
			
			int i = 0;
			for (Cache cache : ArchitecturalComponent.getCacheList())
			{
				noOfCacheRequests[i] += (long) (cache.hits + cache.misses - tempnoOfCacheRequests[i]) * weightsArray[currentSlice];
				tempnoOfCacheRequests[i] = cache.hits + cache.misses;
				noOfCacheHits[i] += (long) (cache.hits - tempnoOfCacheHits[i]) * weightsArray[currentSlice];
				tempnoOfCacheHits[i] = cache.hits;
				noOfCacheMisses[i] += (long) (cache.misses - tempnoOfCacheMisses[i]) * weightsArray[currentSlice];
				tempnoOfCacheMisses[i] = cache.misses;
				
				i++;
			}
		}
		
		currentSlice++;
	}
	
	public static void windup()
	{
		if(SimulationConfig.pinpointsSimulation == false)
		{
			return;
		}
		
		Core core;
		
		for(int i = 0; i < ArchitecturalComponent.getCores().length; i++)
		{
			core = ArchitecturalComponent.getCores()[i];
			
			if(core.getNoOfInstructionsExecuted() == 0)
			{
				continue;
			}
			
			core.setCoreCyclesTaken(coreCyclesTaken[i]);
			core.setNoOfInstructionsExecuted(numCoreInstructions[i]);
			core.getExecEngine().setNumberOfBranches(branchCount[i]);
			core.getExecEngine().setNumberOfMispredictedBranches(mispredictedBranchCount[i]);
			
			core.getExecEngine().getCoreMemorySystem().setNumberOfMemoryRequests(noOfMemRequests[i]);
			core.getExecEngine().getCoreMemorySystem().setNumberOfLoads(noOfLoads[i]);
			core.getExecEngine().getCoreMemorySystem().setNumberOfStores(noOfStores[i]);
			core.getExecEngine().getCoreMemorySystem().setNumberOfValueForwardings(noOfValueForwards[i]);
			
			core.getExecEngine().getCoreMemorySystem().getiTLB().setTlbRequests(noOfiTLBRequests[i]);
			core.getExecEngine().getCoreMemorySystem().getiTLB().setTlbHits(noOfiTLBHits[i]);
			core.getExecEngine().getCoreMemorySystem().getiTLB().setTlbMisses(noOfiTLBMisses[i]);
			core.getExecEngine().getCoreMemorySystem().getdTLB().setTlbRequests(noOfdTLBRequests[i]);
			core.getExecEngine().getCoreMemorySystem().getdTLB().setTlbHits(noOfdTLBHits[i]);
			core.getExecEngine().getCoreMemorySystem().getdTLB().setTlbMisses(noOfdTLBMisses[i]);
		}
		
		int i = 0;
		for (Cache cache : ArchitecturalComponent.getCacheList())
		{
			cache.hits = noOfCacheHits[i];
			cache.misses = noOfCacheMisses[i];
			
			i++;
		}
	}
	
	public static void toProcessEndOfSlice(long numHandledCISCInsn) 
	{
		if(SimulationConfig.pinpointsSimulation == true)
		{
			if(numHandledCISCInsn > 3000000 * (currentSlice + 1))
			{
				processEndOfSlice();
			}
		}
	}

}
