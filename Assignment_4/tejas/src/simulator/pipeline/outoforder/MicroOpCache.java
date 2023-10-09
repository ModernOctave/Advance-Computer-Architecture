package pipeline.outoforder;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import config.SimulationConfig;
import generic.Event;
import generic.EventQueue;
import generic.GlobalClock;
import generic.PortType;
import generic.SimulationElement;

public class MicroOpCache extends SimulationElement {
	
	int maxSize; //in terms of number of micro-ops
	int curSize;
	HashMap<Long, MicroOpCacheEntry> uopCache;
	
	public long numAdditions;
	public long numSearches;
	public long numHits;

	public MicroOpCache(int maxSize) {
		super(PortType.Unlimited, -1, -1, -1, -1);
		this.maxSize = maxSize;
		uopCache = new HashMap<Long, MicroOpCacheEntry>();
	}

	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
		// TODO Auto-generated method stub

	}
	
	public boolean isPresentInCache(long searchPC) //will be called for each micro-op (and not each CISC instruction)
	{
		numSearches++;
		
		MicroOpCacheEntry entry = uopCache.get(searchPC);
		if(entry != null)
		{
			entry.timeLastUsed = GlobalClock.getCurrentTime();
			numHits++;
			if(SimulationConfig.debugMode)
			{
				System.out.println("hit in microp-cache : " + GlobalClock.getCurrentTime()/24 + " : "  + Long.toHexString(searchPC));
			}
			return true;
		}
		
		return false;
	}
	
	public void addToCache(long newPC, int numberOfMicroOps)
	{
		if(uopCache.containsKey(newPC) == false)
		{
			//remove old entries to make place for the new one
			while(curSize + numberOfMicroOps > maxSize)
			{
				//find LRU PC
				long LRU_PC = -1;
				MicroOpCacheEntry LRUEntry = null;
				for(Map.Entry<Long, MicroOpCacheEntry> entry : uopCache.entrySet())
				{
					if(LRUEntry == null)
					{
						LRUEntry = entry.getValue();
						LRU_PC = entry.getKey();
					}
					else
					{
						if(entry.getValue().timeLastUsed < LRUEntry.timeLastUsed)
						{
							LRUEntry = entry.getValue();
							LRU_PC = entry.getKey();
						}
					}
				}
				
				//remove all micro-ops corresponding to LRU PC
				uopCache.remove(LRU_PC);
				curSize -= LRUEntry.numberOfMicroOps;
			}
			
			//add new micro-ops
			MicroOpCacheEntry newEntry = new MicroOpCacheEntry();
			newEntry.numberOfMicroOps = numberOfMicroOps;
			newEntry.timeLastUsed = GlobalClock.getCurrentTime();
			uopCache.put(newPC, newEntry);
			curSize += numberOfMicroOps;
			
			numAdditions += numberOfMicroOps;
			if(SimulationConfig.debugMode)
			{
				System.out.println("add to microp-cache : " + GlobalClock.getCurrentTime()/24 + " : "  + Long.toHexString(newPC));
			}
		}
	}
}

class MicroOpCacheEntry
{
	int numberOfMicroOps;
	long timeLastUsed;
}