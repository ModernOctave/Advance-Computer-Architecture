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

	Contributors:  Moksh Upadhyay
*****************************************************************************/
package config;

import java.util.Hashtable;
import java.util.Vector;

import generic.PortType;

public class SystemConfig 
{
	public static enum Interconnect {
		Bus, Noc
	}

	public static int NoOfCores;
	
	public static int maxNumJavaThreads;
	public static int numEmuThreadsPerJavaThread;	
	
	public static CoreConfig[] core; 
	public static Vector<CacheConfig> sharedCacheConfigs=new Vector<CacheConfig>();	

	//added later kush
	public static boolean memControllerToUse;

	public static int mainMemoryLatency;
	public static long mainMemoryFrequency;
	public static long mainMemoryStepSize;
	public static PortType mainMemPortType;
	public static int mainMemoryAccessPorts;
	public static int mainMemoryPortOccupancy;
	public static int cacheBusLatency;
	public static String coherenceEnforcingCache;
	public static BusConfig busConfig;
	public static NocConfig nocConfig;
	public static EnergyConfig busEnergy;
	
	public static Interconnect interconnect;
	public static EnergyConfig  mainMemoryControllerPower;
	public static EnergyConfig  globalClockPower;
	
	//FIXME
	//TODO
	//have to do it here since the object is not being created in xml parser yet
	public static MainMemoryConfig mainMemoryConfig; 
}
