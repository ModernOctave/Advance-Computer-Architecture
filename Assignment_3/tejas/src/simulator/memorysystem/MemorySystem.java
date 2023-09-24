/*****************************************************************************
				Tejas Simulator
------------------------------------------------------------------------------------------------------------

   Copyright [2010] [Indian Institute of Technology, Delhi]
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
------------------------------------------------------------------------------------------------------------

	Contributors:  Prathmesh, Moksh Upadhyay, Mayur Harne
*****************************************************************************/
package memorysystem;

import generic.CommunicationInterface;

import java.util.Hashtable;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import main.ArchitecturalComponent;
import memorysystem.coherence.Coherence;
import memorysystem.coherence.Directory;
import memorysystem.nuca.NucaCache;
import memorysystem.nuca.NucaCache.NucaType;
import net.ID;
import net.NocInterface;
import net.BusInterface;
import config.CacheConfig;
import config.SystemConfig;

import dram.MainMemoryDRAMController;

public class MemorySystem
{
	public static final int PAGE_OFFSET_BITS = 12;
	
	public static Hashtable<String, Cache> cacheNameMappings = new Hashtable<String, Cache>();
	public static Hashtable<String, Coherence> coherenceNameMappings = new Hashtable<String, Coherence>();
	
	public static void setCoherenceOfCaches() {
		for(Map.Entry<String, Cache> cacheListEntry :  cacheNameMappings.entrySet()) {
			Cache c = cacheListEntry.getValue();
			if(c.cacheConfig.coherenceName.equals("None")) {
				continue;
			}
			
			Coherence coherence = coherenceNameMappings.get(c.cacheConfig.coherenceName);
			c.setCoherence(coherence);
		}
	}
	static int sharedCacheCount = 0;
	public static Cache createSharedCache(String token, CommunicationInterface comInterface) {
		for(CacheConfig config : SystemConfig.sharedCacheConfigs) {
			if(token.equals(config.cacheName)) {
				Cache c = null;
				if(config.isDirectory==true) {
					c = new Directory(token, 0, config, null);
				}
				else if(config.nucaType != NucaType.NONE)
				{
					NucaCache nuca;
					if(!ArchitecturalComponent.nucaList.containsKey(token))
					{
						nuca = new NucaCache(token, 0, config, null);
						ArchitecturalComponent.nucaList.put(token, nuca);
					}
					else{
						nuca = ArchitecturalComponent.nucaList.get(token);
					}
					c = nuca.createBanks(token, config, comInterface);
				}
				else {
					c = new Cache(token+"[" + sharedCacheCount++ + "]", 0, config, null);
				}
				c.setComInterface(comInterface);
				return c;
			}
		}
		
		misc.Error.showErrorAndExit("Unable to find a cache config for " + token);
		return null;
	}

	public static void createLinkBetweenCaches() {
		for(Map.Entry<String, Cache> cacheListEntry :  cacheNameMappings.entrySet()) {
			Cache c = cacheListEntry.getValue();
			
			// If the next level field has been set in the coreMemSys, do not set it again
			if(c.nextLevel==null) {
				createLinkToNextLevelCache(c);
			}
		}
	}
	
	private static void createLinkToNextLevelCache(Cache c) {
		String cacheName = c.cacheName;
		int cacheId = c.id;
		String nextLevelName = c.cacheConfig.nextLevel;
		
		if(nextLevelName=="" || nextLevelName==null) {
			return;
		}
		
		Cache nextLevelCache;
		if(ArchitecturalComponent.nucaList.get(nextLevelName) != null)
		{
			nextLevelCache = ArchitecturalComponent.nucaList.get(nextLevelName);
		}
		else
		{
			String nextLevelIdStrOrig = c.cacheConfig.nextLevelId;
			
			if(nextLevelIdStrOrig!=null && nextLevelIdStrOrig!="") {
				int nextLevelId = getNextLevelId(cacheName, cacheId, nextLevelIdStrOrig);
				nextLevelName += "[" + nextLevelId + "]";
			} else {
				nextLevelName += "[0]";
			}
			
			nextLevelCache = cacheNameMappings.get(nextLevelName);
		}
		
		if(nextLevelCache==null) {
			misc.Error.showErrorAndExit("Inside " + cacheName + ".\n" +
				"Could not find the next level cache. Name : " + nextLevelName);
		}
		
		c.createLinkToNextLevelCache(nextLevelCache);		
	}
	
	private static int getNextLevelId(String cacheName, int cacheId,
			String nextLevelIdStrOrig) {
		
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");

		//Replace $i with the component id of cache
		String cacheIdStr = (new Integer(cacheId)).toString();
		String nextLevelIdStr = nextLevelIdStrOrig.replaceAll("\\$i", cacheIdStr);
		
		try {
			Double ret =  (Double)engine.eval(nextLevelIdStr);
			return ret.intValue();
		} catch (Exception e) {
			misc.Error.showErrorAndExit("Error in evaluating the formula " +
				"for the next level cache.\n" +
				"\nname : " + cacheName + "\tid : " + cacheId + 
				"\nnextLevelIdStrOrig : " + nextLevelIdStrOrig + 
				"\nnextLevelIdStrAfterTransformation : " + nextLevelIdStr + "\n" + e);
			return -1;
		}
	}

	public static void addToCacheList(String cacheName, Cache cache) {
		if(cacheNameMappings.contains(cacheName)) {
			misc.Error.showErrorAndExit("A cache with same name already exists !!\nCachename : " + cacheName);
		} else {
			cacheNameMappings.put(cacheName, cache);
		}
	}
	
	public MainMemoryDRAMController getMemoryControllerId(CommunicationInterface comInterface) {
		
		MainMemoryDRAMController memControllerRet = null;
		
		if(comInterface.getClass()==NocInterface.class) {
			ID currBankId = ((NocInterface)comInterface).getId();
	    	double distance = Double.MAX_VALUE;
	    	ID memControllerId = ((NocInterface) (ArchitecturalComponent.memoryControllers.get(0).getComInterface())).getId();
	    	int x1 = currBankId.getx();//bankid/cacheColumns;
	    	int y1 = currBankId.gety();//bankid%cacheColumns;
	   
	    	for(MainMemoryDRAMController memController:ArchitecturalComponent.memoryControllers) {
	    		int x2 = ((NocInterface)memController.getComInterface()).getId().getx();
	    		int y2 = ((NocInterface)memController.getComInterface()).getId().gety();
	    		double localdistance = Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
	    		if(localdistance < distance) {
	    			distance = localdistance;
	    			memControllerRet = memController;
	    			memControllerId = ((NocInterface)memController.getComInterface()).getId();
	    		}
	    	}
		} else if(comInterface.getClass()==BusInterface.class) {
			memControllerRet = ArchitecturalComponent.memoryControllers.get(0);
		}
    	
    	return memControllerRet;
    }

//	static Core[] cores;
//	
//	public static MainMemoryDRAMController mainMemoryController;
//		
//	private static CoreMemorySystem coreMemSysArray[];
//	public static CoreMemorySystem[] getCoreMemorySystems() {
//		return coreMemSysArray;
//	}
//	
//	public static Vector<Cache> getSharedCacheList() {
//		return (new Vector<Cache>(cacheList.values()));
//	}
//	
//	public static Hashtable<String, Cache> getCacheList() {
//		return cacheList;
//	}
//
//	public static CoreMemorySystem[] initializeMemSys(Core[] cores)
//	{
//		createDirectories();
//		MemorySystem.cores = cores;
//		coreMemSysArray = new CoreMemorySystem[cores.length];
//		
//		System.out.println("initializing memory system...");
//		// initialising the memory system
//		
//		//Set up the main memory properties
//		
//		
//		/*-- Initialise the memory system --*/
//		NucaType nucaType = NucaType.NONE;
//				
//		boolean flag = false;
//		NucaCache nucaCache = null;
//		for (CacheConfig cacheParameterObj : SystemConfig.declaredCacheConfigs)
//		{
//			String cacheName = cacheParameterObj.cacheName;
//			
//			if (!(cacheList.containsKey(cacheName))) //If not already present
//			{
//				//cacheParameterObj = SystemConfig.declaredCaches.get(cacheName);
//				
//				//Declare the new cache
//				Cache newCache = null;
//				if (cacheParameterObj.getNucaType() == NucaType.NONE) {
//					// XXX : We are already creating such caches in the createSharedCaches function
//					//newCache = new Cache(cacheParameterObj, null);
//					continue;
//				} else if (cacheParameterObj.getNucaType() == NucaType.S_NUCA)
//				{	
//					nucaType = NucaType.S_NUCA;
//					flag = true;
//					newCache = new SNuca(cacheParameterObj,null,nucaType);
//					nucaCache = (NucaCache) newCache;
//					Core.nucaCache = nucaCache;
//				}
//				else if (cacheParameterObj.getNucaType() == NucaType.D_NUCA)
//				{	
//					nucaType = NucaType.D_NUCA;
//					flag = true;
//					newCache = new DNuca(cacheParameterObj,null,nucaType);
//					nucaCache = (NucaCache) newCache;
//					Core.nucaCache = nucaCache;
//				}
//				
//				
//				//Put the newly formed cache into the new list of caches
//				cacheList.put(cacheName, newCache);
//			}
//		}
//		
//		//Initialize centralized directory
//		int numCacheLines=262144;//FIXME 256KB in size. Needs to be fixed.
//		if(SystemConfig.interconnect == SystemConfig.Interconnect.Bus)
//		{
//			centralizedDirectory = new CentralizedDirectoryCache("Directory", 0, SystemConfig.directoryConfig, null, cores.length, 
//				SystemConfig.dirNetworkDelay);
//			mainMemoryController = new MainMemoryDRAMController(nucaType);
//		}
//		else if(SystemConfig.interconnect == SystemConfig.Interconnect.Noc)
//		{
//			//mainMemoryController = new MainMemoryDRAMController(SystemConfig.memoryControllersLocations,nucaType);
//			ArchitecturalComponent.noc = new NOC();
//			
//		}
//		//Link all the initialised caches to their next levels
//
//		createPrivateCaches();
//		createSharedCaches();
//		createLinksBetweenSharedCaches();
//		createLinkFromPrivateCacheToSharedCache();
//		
//		return coreMemSysArray;
///*		
//		//Initialising the BUS for cache coherence
//		if (!cacheList.containsKey(SystemConfig.coherenceEnforcingCache))
//		{
//			System.err.println("Memory system configuration error : A cache specified as coherence enforcing cache does not exist");
//			System.exit(1);
//		}
//		else
//		{
//			Bus.lowerLevel = cacheList.get(SystemConfig.coherenceEnforcingCache);
//			Bus.upperLevels = Bus.lowerLevel.prevLevel;
//		}
//		Bus.lowerLevel.enforcesCoherence = true;
//		
//		propagateCoherencyUpwards(Bus.upperLevels);*/
//		/*for (Enumeration<String> cacheNameSet = cacheList.keys(); cacheNameSet.hasMoreElements(); )
//		{
//			String cacheName = cacheNameSet.nextElement();
//			Cache cacheToSetNextLevel = cacheList.get(cacheName);
//			
//			System.out.println(cacheName + " : " + cacheToSetNextLevel.connectedMSHR.size());
//		}*/
//	}
//	
//	
//
//	private static void createSharedCaches() {
//		// Creating a list of shared caches
//		for (int i=0; i<SystemConfig.declaredCacheConfigs.size(); i++) {
//			CacheConfig config = SystemConfig.declaredCacheConfigs.get(i);
//			int numComponents = SystemConfig.declaredCacheConfigs.get(i).numComponents;
//			
//			for(int component=0; component<numComponents; component++) {
//				String cacheName = null;
//				cacheName = config.cacheName + "[" + component + "]";
//				Cache c = new Cache(cacheName, component, config, null);
//				cacheList.put(cacheName, c);
//			}
//		}
//	}	
//	
//	private static void createLinksBetweenSharedCaches() {
//		ArrayList<Cache> sharedCacheList = new ArrayList<Cache>(cacheList.values());
//		
//		for(Cache c : sharedCacheList) {
//			if(c.isLastLevel==false) {
//				if(c.nextLevel!=null) {
//					misc.Error.showErrorAndExit("Next level cache must not be set for this cache");
//				}
//				
//				createLinkToNextLevelCache(c);
//			}
//		}
//	}
//	
//	private static void createLinkToNextLevelCache(Cache c) {
//		String cacheName = c.cacheName;
//		int cacheId = c.id;
//		String nextLevelName = c.cacheConfig.nextLevel;
//		String nextLevelIdStrOrig = c.cacheConfig.nextLevelId;
//		
//		if(nextLevelIdStrOrig!=null && nextLevelIdStrOrig!="") {
//			int nextLevelId = getNextLevelId(cacheName, cacheId, nextLevelIdStrOrig);
//			nextLevelName += "[" + nextLevelId + "]";
//		} else {
//			nextLevelName += "[0]";
//		}
//		
//		Cache nextLevelCache = cacheList.get(nextLevelName);
//		if(nextLevelCache==null) {
//			misc.Error.showErrorAndExit("Inside " + cacheName + ".\n" +
//				"Could not find the next level cache. Name : " + nextLevelName);
//		}
//		
//		c.createLinkToNextLevelCache(nextLevelCache);		
//	}
//
//	private static void createLinkFromPrivateCacheToSharedCache() {
//			
//		for(CoreMemorySystem  coreMemSys : coreMemSysArray) {
//			ArrayList<Cache> coreCacheList = new ArrayList<Cache>(coreMemSys.getCacheList().values());
//			for(Cache c : coreCacheList) {
//				if(c.nextLevel==null && c.isLastLevel==false) {
//					createLinkToNextLevelCache(c);
//				}
//			}
//		}
//	}
//	
//	private static int getNextLevelId(String cacheName, int cacheId,
//			String nextLevelIdStrOrig) {
//		
//		ScriptEngineManager mgr = new ScriptEngineManager();
//		ScriptEngine engine = mgr.getEngineByName("JavaScript");
//
//		//Replace $i with the component id of cache
//		String cacheIdStr = (new Integer(cacheId)).toString();
//		String nextLevelIdStr = nextLevelIdStrOrig.replaceAll("\\$i", cacheIdStr);
//		
//		try {
//			Double ret =  (Double)engine.eval(nextLevelIdStr);
//			return ret.intValue();
//		} catch (Exception e) {
//			misc.Error.showErrorAndExit("Error in evaluating the formula " +
//				"for the next level cache.\n" +
//				"\nname : " + cacheName + "\tid : " + cacheId + 
//				"\nnextLevelIdStrOrig : " + nextLevelIdStrOrig + 
//				"\nnextLevelIdStrAfterTransformation : " + nextLevelIdStr + "\n" + e);
//			return -1;
//		}
//	}
//
//	private static CoreMemorySystem getCoreMemorySystem(int core) {
//		return coreMemSysArray[core];
//	}
//
//	
//
//	/**
//	 * Recursive method to mark all the caches above the bus as COHERENT
//	 * @param list : Initial input is an Arraylist of Caches juat above the Bus and then works recursively upwards
//	 */
//	public static void propagateCoherencyUpwards(ArrayList<Cache> list)
//	{
//		if (list.isEmpty())
//			return;
//		for (int i = 0; i < list.size(); i++)
//		{
//			list.get(i).isCoherent = true;
//			propagateCoherencyUpwards(list.get(i).prevLevel);
//		}
//	}
//	
//	public static Directory getDirectory(long addr)
//	{
//		return centralizedDirectory;
//	}
//	
//	public static void printMemSysResults()
//	{
//		System.out.println("\n Memory System results\n");
//		for (int i = 0; i < SystemConfig.NoOfCores; i++)
//		{
//			System.out.println(
//					"LSQ[" + i + "] Loads : "
//					+ cores[i].getExecEngine().getCoreMemorySystem().lsqueue.NoOfLd 
//					+ "\t ; LSQ[" + i + "] Stores : " 
//					+ cores[i].getExecEngine().getCoreMemorySystem().lsqueue.NoOfSt
//					+ "\t ; LSQ[" + i + "] Value Forwards : " 
//					+ cores[i].getExecEngine().getCoreMemorySystem().lsqueue.NoOfForwards);
//		}
//		
//		System.out.println(" ");
//		
//		for (int i = 0; i < SystemConfig.NoOfCores; i++)
//		{
//			System.out.println(
//					"ITLB[" + i + "] Hits : " 
//					+ cores[i].getExecEngine().getCoreMemorySystem().getiTLB().tlbHits 
//					+ "\t ; ITLB[" + i + "] misses : " 
//					+ cores[i].getExecEngine().getCoreMemorySystem().getiTLB().tlbMisses);
//		}
//		
//		System.out.println(" ");
//		
//		for (int i = 0; i < SystemConfig.NoOfCores; i++)
//		{
//			System.out.println(
//					"DTLB[" + i + "] Hits : " 
//					+ cores[i].getExecEngine().getCoreMemorySystem().getdTLB().tlbHits 
//					+ "\t ; DTLB[" + i + "] misses : " 
//					+ cores[i].getExecEngine().getCoreMemorySystem().getdTLB().tlbMisses);
//		}
//		
//		System.out.println(" ");
//		
//		for (int i = 0; i < SystemConfig.NoOfCores; i++)
//		{
//			System.out.println(
//					"L1[" + i + "] Hits : " 
//					+ cores[i].getExecEngine().getCoreMemorySystem().l1Cache.hits 
//					+ "\t ; L1[" + i + "] misses : " 
//					+ cores[i].getExecEngine().getCoreMemorySystem().l1Cache.misses);
//		}
//		
//		System.out.println(" ");
//		System.out.println(" Results of other caches");
//		
//		for (Enumeration<String> cacheNameSet = cacheList.keys(); cacheNameSet.hasMoreElements(); /*Nothing*/)
//		{
//			String cacheName = cacheNameSet.nextElement();
//			Cache cache = cacheList.get(cacheName);
//			
//			System.out.println(
//					cacheName + " Hits : " 
//					+ cache.hits 
//					+ "\t ; " + cacheName + " misses : " 
//					+ cache.misses);
//		}
//	}
}
