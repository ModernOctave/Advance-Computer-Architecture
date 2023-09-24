/*****************************************************************************
				Tejas Simulator
------------------------------------------------------------------------------------------------------------

   Copyright 2010 Indian Institute of Technology, Delhi
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

	Contributors:  Moksh Upadhyay
*****************************************************************************/
package memorysystem;

import java.util.Hashtable;
import java.util.Vector;
import generic.PortType;
import generic.SimulationElement;
import generic.Core;
import generic.RequestType;
import config.CacheDataType;
import config.SystemConfig;
import config.CacheConfig;

public abstract class CoreMemorySystem extends SimulationElement
{
	protected int coreID;
	protected Core core;
	protected Cache iCache;
	protected Cache l1Cache;
	protected TLB iTLB;
	protected TLB dTLB;
	protected TLB sTLB;
	protected LSQ lsqueue;
	
	protected long numInstructionSetChunksNoted = 0;
	protected long numDataSetChunksNoted = 0;
	
	// All the private caches of the core are maintained in a list
	// coreCacheList contains a vector of caches
	// cacheNameList contains a vector of cache names
	// Elements in both the lists have a one to one mapping
	private Hashtable<String,Cache> cacheNameToCacheMapping = new Hashtable<String,Cache>();
	private Vector<Cache> coreCacheList = new Vector<Cache>();
	
	public Vector<Cache> getCoreCacheList() {
		return coreCacheList;
	}
	
	public void addToCoreCacheList(Cache c) {
		coreCacheList.add(c);
	}
	
	public Hashtable<String, Cache> getCacheList() {
		return cacheNameToCacheMapping;
	}
	
	private String tagNameWithCoreId(String name) {
		return (name + "[" + this.coreID + "]");
	}
	
	public Cache getCache(String cacheName) {
		return cacheNameToCacheMapping.get(cacheName);
	}
	
	protected CoreMemorySystem(Core core)
	{
		super(PortType.Unlimited, -1, -1, 0, -1);
		
		this.setCore(core);
		this.coreID = core.getCore_number();
		
		createCoreCaches();
		maintainDataAndInstructionCacheAsFirstLevelCache();
		createLinksBetweenCoreCaches();
		
		//Initialise the TLB
		int numPageLevels = 1;
		iTLB = new TLB(SystemConfig.core[coreID].ITLBPortType,
				SystemConfig.core[coreID].ITLBAccessPorts, 
				SystemConfig.core[coreID].ITLBPortOccupancy, 
				SystemConfig.core[coreID].ITLBLatency,
				this,
				SystemConfig.core[coreID].ITLBSize,
				SystemConfig.core[coreID].ITLBMissPenalty,
				SystemConfig.core[coreID].iTLBPower);
		
		dTLB = new TLB(SystemConfig.core[coreID].DTLBPortType,
				SystemConfig.core[coreID].DTLBAccessPorts, 
				SystemConfig.core[coreID].DTLBPortOccupancy, 
				SystemConfig.core[coreID].DTLBLatency,
				this,
				SystemConfig.core[coreID].DTLBSize,
				SystemConfig.core[coreID].DTLBMissPenalty,
				SystemConfig.core[coreID].dTLBPower);
		
		sTLB = new TLB(SystemConfig.core[coreID].STLBPortType,
				SystemConfig.core[coreID].STLBAccessPorts, 
				SystemConfig.core[coreID].STLBPortOccupancy, 
				SystemConfig.core[coreID].STLBLatency,
				this,
				SystemConfig.core[coreID].STLBSize,
				SystemConfig.core[coreID].STLBMissPenalty,
				SystemConfig.core[coreID].sTLBPower);
		
		//Initialise the LSQ
		lsqueue = new LSQ(SystemConfig.core[coreID].LSQPortType,
		                    SystemConfig.core[coreID].LSQAccessPorts, 
							SystemConfig.core[coreID].LSQPortOccupancy, 
							SystemConfig.core[coreID].LSQLatency,
							this, 
							SystemConfig.core[coreID].LSQNumLoadEntries, 
							SystemConfig.core[coreID].LSQNumStoreEntries);
	//	lsqueue.setMultiPortType(SystemConfig.core[coreID].LSQMultiportType);
	}
	
	private void createLinksBetweenCoreCaches() {
		for(int i=0; i<SystemConfig.core[coreID].coreCacheList.size(); i++) {
			CacheConfig cacheConfig = SystemConfig.core[coreID].coreCacheList.elementAt(i);
			String nextLevelName = tagNameWithCoreId(cacheConfig.nextLevel);
			Cache nextLevelCache = this.getCache(nextLevelName); 
			if(nextLevelCache!=null) {
				this.coreCacheList.get(i).createLinkToNextLevelCache(nextLevelCache);
			}
		}
	}

	private void maintainDataAndInstructionCacheAsFirstLevelCache() {
		if(iCache!=null && l1Cache==null) {
			misc.Error.showErrorAndExit("Instruction cache set but data cache has not been set !!");
		}
		
		if(iCache==null && l1Cache!=null) {
			misc.Error.showErrorAndExit("Data cache set but instruction cache has not been set !!");
		}
		
		// If both instruction and data cache are set to null, 
		// then use a unified first level cache
		if(iCache==null && l1Cache==null) {
			for(int i=0; i<SystemConfig.core[coreID].coreCacheList.size(); i++) {
				CacheConfig cacheConfig = SystemConfig.core[coreID].coreCacheList.elementAt(i);
				
				if(cacheConfig.firstLevel==true) {
					if(iCache==null && l1Cache==null) {
						iCache = coreCacheList.get(i);
						l1Cache = coreCacheList.get(i);
					} else {
						misc.Error.showErrorAndExit("Core cannot have more than one first level " +
							"unified cache !!");
					}
				}
			}
		}
		
		if(iCache==null && l1Cache==null) {
			if(SystemConfig.core[coreID].coreCacheList.size()==0) {
				misc.Error.showErrorAndExit("No private cache for this core !!");
			} else {
				misc.Error.showErrorAndExit("There are " + coreCacheList.size() + " private caches " +
					"but none of them is a first level cache. " +
					"Set the firstLevel attribute to true in the " +
					"cache tag of the configuration file");
			}
		}		
	}

	private void createCoreCaches() {
		for(int i=0; i<SystemConfig.core[coreID].coreCacheList.size(); i++) {
			CacheConfig cacheConfig = SystemConfig.core[coreID].coreCacheList.elementAt(i);
			Cache cache = new Cache(tagNameWithCoreId(cacheConfig.cacheName), 
					core.getCore_number(), cacheConfig, this);
			
			cacheNameToCacheMapping.put(tagNameWithCoreId(cacheConfig.cacheName), cache);
			addToCoreCacheList(cache);
						
			if(cacheConfig.cacheDataType==CacheDataType.Instruction) {
				if(iCache==null && cacheConfig.firstLevel==true) {
					iCache = cache;
				} else if (iCache!=null) {
					misc.Error.showErrorAndExit("Core cannot have two instruction caches !!");
				} else if(cacheConfig.firstLevel==false) {
					misc.Error.showErrorAndExit("Instruction cache must be a first level cache\n" + 
						"Set the firstLevel attribute field of the cache to true");
				}
			}
			
			if(cacheConfig.cacheDataType==CacheDataType.Data) {
				if(l1Cache==null && cacheConfig.firstLevel==true) {
					l1Cache = cache;
				} else if (l1Cache!=null) {
					misc.Error.showErrorAndExit("Core cannot have two data caches !!");
				} else if(cacheConfig.firstLevel==false) {
					misc.Error.showErrorAndExit("Data cache must be a first level cache\n" + 
						"Set the firstLevel attribute field of the cache to true");
				}
			}
		}
	}

	public abstract void issueRequestToInstrCache(long address);
	
	public abstract boolean issueRequestToL1Cache(RequestType requestType, long address);
	
	public LSQ getLsqueue() {
		return lsqueue;
	}
	
	public Cache getL1Cache() {
		return l1Cache;
	}

	public TLB getiTLB() {
		return iTLB;
	}
	
	public TLB getdTLB() {
		return dTLB;
	}

	public Cache getiCache() {
		return iCache;
	}

	public void setCore(Core core) {
		this.core = core;
	}

	public Core getCore() {
		return core;
	}
	
	public abstract long getNumberOfMemoryRequests();	
	public abstract long getNumberOfLoads();	
	public abstract long getNumberOfStores();		
	public abstract long getNumberOfSoftwareReadPrefetches();	
	public abstract long getNumberOfSoftwareWritePrefetches();	
	public abstract long getNumberOfValueForwardings();
	public abstract void setNumberOfMemoryRequests(long numMemoryRequests);	
	public abstract void setNumberOfLoads(long numLoads);	
	public abstract void setNumberOfStores(long numStores);	
	public abstract void setNumberOfValueForwardings(long numValueForwardings);
}
