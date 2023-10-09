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

	Contributors:  Moksh Upadhyay, Abhishek Sagar
*****************************************************************************/
package config;

import memorysystem.nuca.NucaCache.NucaType;

public class SimulationConfig 
{
	public static int NumTempIntReg; //Number of temporary Integer registers
	public static boolean IndexAddrModeEnable; //Indexed addressing mode Enabled or disabled
	public static long MapEmuCores;  //Emulator cores to run on
	public static long MapJavaCores; //Java simulator cores to run on
	public static long NumInsToIgnore; // Number of "Profilable" instructions to ignore from start
	public static String outputFileName;
	public static boolean debugMode;
	
	public static boolean detachMemSysInsn;
	public static boolean detachMemSysData;
	
	public static boolean subsetSimulation;
	public static long subsetSimSize;
	public static boolean pinpointsSimulation;
	public static String pinpointsFile;
	public static boolean markerFunctionsSimulation;
	public static String startMarker;
	public static String endMarker;
	public static long numInsForTrace;
	public static long numCyclesForTrace;
	public static NucaType nucaType;
	public static boolean powerStats;
	public static boolean broadcast;
	
	public static boolean collectInsnWorkingSetInfo;
	public static long insnWorkingSetChunkSize;
	
	public static boolean collectDataWorkingSetInfo;
	public static long dataWorkingSetChunkSize;
}
