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

import java.util.Vector;
import generic.PortType;

public class CoreConfig 
{
	public long frequency;
	public long stepSize;
	
	public int LSQNumLoadEntries;
	public int LSQNumStoreEntries;
	public int LSQLatency;
	public PortType LSQPortType;
	public int LSQAccessPorts;
	public int LSQPortOccupancy;
		
	public PipelineType pipelineType;
		
	public int ITLBSize;
	public int ITLBLatency;
	public int ITLBMissPenalty;
	public PortType ITLBPortType;
	public int ITLBAccessPorts;
	public int ITLBPortOccupancy;
	
	public int DTLBSize;
	public int DTLBLatency;
	public int DTLBMissPenalty;
	public PortType DTLBPortType;
	public int DTLBAccessPorts;
	public int DTLBPortOccupancy;
	
	public int STLBSize;
	public int STLBLatency;
	public int STLBMissPenalty;
	public PortType STLBPortType;
	public int STLBAccessPorts;
	public int STLBPortOccupancy;

	public int NoOfMicroOpCacheEntries;
	public int DecodeWidth;
	public int RenameWidth;
	public int IssueWidth;
	public int RetireWidth;
	public int ROBSize;
	public int IWSize;
	public int IntRegFileSize;
	public int VectorRegFileSize;
	public int IntArchRegNum;
	public int VectorArchRegNum;
	
	public int BranchMispredPenalty;
	
	public int ExecutionCoreNumPorts;
	
	public int IntALUNum;
	public int IntMulNum;
	public int IntDivNum;
	public int FloatALUNum;
	public int FloatMulNum;
	public int FloatDivNum;
	public int IntVectorALUNum;
	public int IntVectorMulNum;
	public int FloatVectorALUNum;
	public int FloatVectorMulNum;
	public int FMANum;
	public int BranchNum;
	public int LoadNum;
	public int LoadAGUNum;
	public int StoreNum;
	public int StoreAGUNum;
	public int AESNum;
	public int VectorStringNum;
	public int BitScanNum;
	public int VectorShuffleNum;
	public int LEANum;
	
	public int IntALULatency;
	public int IntMulLatency;
	public int IntDivLatency;
	public int FloatALULatency;
	public int FloatMulLatency;
	public int FloatDivLatency;
	public int IntVectorALULatency;
	public int IntVectorMulLatency;
	public int FloatVectorALULatency;
	public int FloatVectorMulLatency;
	public int FMALatency;
	public int BranchLatency;
	public int LoadLatency;
	public int LoadAGULatency;
	public int StoreLatency;
	public int StoreAGULatency;
	public int AESLatency;
	public int VectorStringLatency;
	public int BitScanLatency;
	public int VectorShuffleLatency;
	public int LEALatency;
	
	public int IntALUReciprocalOfThroughput;
	public int IntMulReciprocalOfThroughput;
	public int IntDivReciprocalOfThroughput;
	public int FloatALUReciprocalOfThroughput;
	public int FloatMulReciprocalOfThroughput;
	public int FloatDivReciprocalOfThroughput;
	public int IntVectorALUReciprocalOfThroughput;
	public int IntVectorMulReciprocalOfThroughput;
	public int FloatVectorALUReciprocalOfThroughput;
	public int FloatVectorMulReciprocalOfThroughput;
	public int FMAReciprocalOfThroughput;
	public int BranchReciprocalOfThroughput;
	public int LoadReciprocalOfThroughput;
	public int LoadAGUReciprocalOfThroughput;
	public int StoreReciprocalOfThroughput;
	public int StoreAGUReciprocalOfThroughput;
	public int AESReciprocalOfThroughput;
	public int VectorStringReciprocalOfThroughput;
	public int BitScanReciprocalOfThroughput;
	public int VectorShuffleReciprocalOfThroughput;
	public int LEAReciprocalOfThroughput;
	
	public int[] IntALUPortNumbers;
	public int[] IntMulPortNumbers;
	public int[] IntDivPortNumbers;
	public int[] FloatALUPortNumbers;
	public int[] FloatMulPortNumbers;
	public int[] FloatDivPortNumbers;
	public int[] IntVectorALUPortNumbers;
	public int[] IntVectorMulPortNumbers;
	public int[] FloatVectorALUPortNumbers;
	public int[] FloatVectorMulPortNumbers;
	public int[] FMAPortNumbers;
	public int[] BranchPortNumbers;
	public int[] LoadPortNumbers;
	public int[] LoadAGUPortNumbers;
	public int[] StorePortNumbers;
	public int[] StoreAGUPortNumbers;
	public int[] AESPortNumbers;
	public int[] VectorStringPortNumbers;
	public int[] BitScanPortNumbers;
	public int[] VectorShufflePortNumbers;
	public int[] LEAPortNumbers;
	
	public Vector<CacheConfig> coreCacheList = new Vector<CacheConfig>();

	public BranchPredictorConfig branchPredictor;
	
	public boolean TreeBarrier;

	public int barrierLatency;
	public int barrierUnit;
	
	public EnergyConfig bPredPower;
	public EnergyConfig decodePower;
	public EnergyConfig intRATPower;
	public EnergyConfig floatRATPower;
	public EnergyConfig intFreeListPower;
	public EnergyConfig vectorFreeListPower;
	public EnergyConfig lsqPower;
	public EnergyConfig intRegFilePower;
	public EnergyConfig vectorRegFilePower;
	public EnergyConfig iwPower;
	public EnergyConfig robPower;
	public EnergyConfig resultsBroadcastBusPower;
	public EnergyConfig iTLBPower;
	public EnergyConfig dTLBPower;
	public EnergyConfig sTLBPower;
	
	public EnergyConfig IntALUPower;
	public EnergyConfig IntMulPower;
	public EnergyConfig IntDivPower;
	public EnergyConfig FloatALUPower;
	public EnergyConfig FloatMulPower;
	public EnergyConfig FloatDivPower;
	public EnergyConfig IntVectorALUPower;
	public EnergyConfig IntVectorMulPower;
	public EnergyConfig FloatVectorALUPower;
	public EnergyConfig FloatVectorMulPower;
	public EnergyConfig FMAPower;
	public EnergyConfig BranchPower;
	public EnergyConfig LoadPower;
	public EnergyConfig LoadAGUPower;
	public EnergyConfig StorePower;
	public EnergyConfig StoreAGUPower;
	public EnergyConfig AESPower;
	public EnergyConfig VectorStringPower;
	public EnergyConfig BitScanPower;
	public EnergyConfig VectorShufflePower;
	public EnergyConfig LEAPower;
	
	public int getICacheLatency() {
		int latency = 0;
		
		for(CacheConfig config : coreCacheList) {
			if(config.firstLevel) {
				if(config.cacheDataType==CacheDataType.Instruction ||
					config.cacheDataType==CacheDataType.Unified) {
					return config.readLatency;
				}
			}
		}
		
		misc.Error.showErrorAndExit("Could not locate instruction cache config !!");
		return latency;
	}
}