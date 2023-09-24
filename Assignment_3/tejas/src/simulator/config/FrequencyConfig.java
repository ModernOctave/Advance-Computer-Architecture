package config;

import generic.EventQueue;
import generic.GlobalClock;

public class FrequencyConfig {
	
	static long HCF;
	
	public static void setupStepSizes()
	{
		findLCM();
		
		long lowestFrequency = Long.MAX_VALUE;
		for(int i = 0; i < SystemConfig.core.length; i++)
		{
			if((LCM / SystemConfig.core[i].frequency) < lowestFrequency)
			{
				lowestFrequency = (LCM / SystemConfig.core[i].frequency);
			}
		}
		for(int i = 0; i < SystemConfig.sharedCacheConfigs.size(); i++)
		{
			if((LCM / SystemConfig.sharedCacheConfigs.get(i).operatingFreq) < lowestFrequency)
			{
				lowestFrequency = (LCM / SystemConfig.sharedCacheConfigs.get(i).operatingFreq);
			}
		}
		if((LCM / SystemConfig.nocConfig.operatingFreq) < lowestFrequency)
		{
			lowestFrequency = (LCM / SystemConfig.nocConfig.operatingFreq);
		}
		if((LCM / SystemConfig.mainMemoryFrequency) < lowestFrequency)
		{
			lowestFrequency = (LCM / SystemConfig.mainMemoryFrequency);
		}
		
		int div = 1;
		while(lowestFrequency/div >= 1)
		{
			if(lowestFrequency % div != 0)
			{
				div++;
				continue;
			}
			
			boolean factor = true;
			for(int i = 0; i < SystemConfig.core.length; i++)
			{
				if((LCM / SystemConfig.core[i].frequency) % (lowestFrequency/div) != 0)
				{
					factor = false;
					break;
				}
			}
			for(int i = 0; i < SystemConfig.sharedCacheConfigs.size(); i++)
			{
				if((LCM / SystemConfig.sharedCacheConfigs.get(i).operatingFreq) % (lowestFrequency/div) != 0)
				{
					factor = false;
					break;
				}
			}
			if((LCM / SystemConfig.nocConfig.operatingFreq) % (lowestFrequency/div) != 0)
			{
				factor = false;
			}
			if((LCM / SystemConfig.mainMemoryFrequency) % (lowestFrequency/div) != 0)
			{
				factor = false;
			}
			
			if(factor == true)
			{
				HCF = lowestFrequency / div;
				break;
			}
			
			div++;
		}
		
		for(int i = 0; i < SystemConfig.core.length; i++)
		{
			SystemConfig.core[i].stepSize = (LCM / SystemConfig.core[i].frequency) / HCF;
			for(int j = 0; j < SystemConfig.core[i].coreCacheList.size(); j++)
			{
				SystemConfig.core[i].coreCacheList.get(j).operatingFreq = SystemConfig.core[i].frequency;
				SystemConfig.core[i].coreCacheList.get(j).stepSize = SystemConfig.core[i].stepSize;
			}
		}
		for(int i = 0; i < SystemConfig.sharedCacheConfigs.size(); i++)
		{
			SystemConfig.sharedCacheConfigs.get(i).stepSize = (LCM / SystemConfig.sharedCacheConfigs.get(i).operatingFreq) / HCF;
		}
		SystemConfig.nocConfig.stepSize = (LCM / SystemConfig.nocConfig.operatingFreq) / HCF;
		SystemConfig.mainMemoryStepSize = (LCM / SystemConfig.mainMemoryFrequency) / HCF;
		
		GlobalClock.effectiveGlobalClockFrequency = SystemConfig.core[0].frequency * SystemConfig.core[0].stepSize;
		setEventQueueSize();
	}

	static long LCM = 1;
	static void findLCM()
	{
		long maxFrequency = Long.MIN_VALUE;
		for(int i = 0; i < SystemConfig.core.length; i++)
		{
			if(SystemConfig.core[i].frequency > maxFrequency)
			{
				maxFrequency = SystemConfig.core[i].frequency;
			}
		}
		for(int i = 0; i < SystemConfig.sharedCacheConfigs.size(); i++)
		{
			if(SystemConfig.sharedCacheConfigs.get(i).operatingFreq > maxFrequency)
			{
				maxFrequency = SystemConfig.sharedCacheConfigs.get(i).operatingFreq;
			}
		}
		if(SystemConfig.nocConfig.operatingFreq > maxFrequency)
		{
			maxFrequency = SystemConfig.nocConfig.operatingFreq;
		}
		if(SystemConfig.mainMemoryFrequency > maxFrequency)
		{
			maxFrequency = SystemConfig.mainMemoryFrequency;
		}
		
		int m = 1;
		while(true)
		{
			boolean factor = true;
			for(int i = 0; i < SystemConfig.core.length; i++)
			{
				if((maxFrequency * m) % SystemConfig.core[i].frequency != 0)
				{
					factor = false;
					break;
				}
			}
			for(int i = 0; i < SystemConfig.sharedCacheConfigs.size(); i++)
			{
				if((maxFrequency * m) % SystemConfig.sharedCacheConfigs.get(i).operatingFreq != 0)
				{
					factor = false;
					break;
				}
			}
			if((maxFrequency * m) % SystemConfig.nocConfig.operatingFreq != 0)
			{
				factor = false;
			}
			if((maxFrequency * m) % SystemConfig.mainMemoryFrequency != 0)
			{
				factor = false;
			}
			
			if(factor == true)
			{
				LCM = (maxFrequency * m);
				break;
			}
			
			m++;
		}
	}
	
	static void setEventQueueSize()
	{
		long highestLatencyOfAnyElement = Long.MIN_VALUE;
		for(int i = 0; i < SystemConfig.sharedCacheConfigs.size(); i++)
		{
			if(((SystemConfig.sharedCacheConfigs.get(i).writeLatency + SystemConfig.sharedCacheConfigs.get(i).portWriteOccupancy) * SystemConfig.sharedCacheConfigs.get(i).stepSize) > highestLatencyOfAnyElement)
			{
				highestLatencyOfAnyElement = ((SystemConfig.sharedCacheConfigs.get(i).writeLatency + SystemConfig.sharedCacheConfigs.get(i).portWriteOccupancy) * SystemConfig.sharedCacheConfigs.get(i).stepSize);
			}
		}
		if(((SystemConfig.nocConfig.latency + SystemConfig.nocConfig.latencyBetweenNOCElements + SystemConfig.nocConfig.portOccupancy) * SystemConfig.nocConfig.stepSize) > highestLatencyOfAnyElement)
		{
			highestLatencyOfAnyElement = ((SystemConfig.nocConfig.latency + SystemConfig.nocConfig.latencyBetweenNOCElements + SystemConfig.nocConfig.portOccupancy) * SystemConfig.nocConfig.stepSize);
		}
		if(((SystemConfig.mainMemoryLatency + SystemConfig.mainMemoryPortOccupancy) * SystemConfig.mainMemoryStepSize) > highestLatencyOfAnyElement)
		{
			highestLatencyOfAnyElement = ((SystemConfig.mainMemoryLatency + SystemConfig.mainMemoryPortOccupancy) * SystemConfig.mainMemoryStepSize);
		}
		
		EventQueue.queueSize = (int)(highestLatencyOfAnyElement * 2);
	}

}
