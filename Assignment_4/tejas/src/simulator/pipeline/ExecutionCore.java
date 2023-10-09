package pipeline;

import java.io.FileWriter;
import java.io.IOException;

import config.CoreConfig;
import config.EnergyConfig;
import config.SystemConfig;
import generic.Core;
import generic.GlobalClock;

public class ExecutionCore {
	
	Core core;
	FunctionalUnit[][] FUs;
	boolean[] portUsedThisCycle;
	int numPorts;
	
	public ExecutionCore(Core core)
	{
		this.core = core;
		
		CoreConfig coreConfig = SystemConfig.core[core.getCore_number()];
		
		FUs = new FunctionalUnit[FunctionalUnitType.values().length][];
		
		//int ALUs
		if(coreConfig.IntALUNum > 0)
		{
			FUs[FunctionalUnitType.integerALU.ordinal()] = new FunctionalUnit[coreConfig.IntALUNum];
			for(int i = 0; i < coreConfig.IntALUNum; i++)
			{
				int[] latency = {coreConfig.IntALULatency};
				int[] reciprocalOfThroughput = {coreConfig.IntALUReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.integerALU, 1, latency,
						reciprocalOfThroughput, coreConfig.IntALUPortNumbers[i]);
				FUs[FunctionalUnitType.integerALU.ordinal()][i] = FU;
			}
		}
		
		//int Muls
		if(coreConfig.IntMulNum > 0)
		{
			FUs[FunctionalUnitType.integerMul.ordinal()] = new FunctionalUnit[coreConfig.IntMulNum];
			for(int i = 0; i < coreConfig.IntMulNum; i++)
			{
				int[] latency = {coreConfig.IntMulLatency};
				int[] reciprocalOfThroughput = {coreConfig.IntMulReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.integerMul, 1, latency,
						reciprocalOfThroughput, coreConfig.IntMulPortNumbers[i]);
				FUs[FunctionalUnitType.integerMul.ordinal()][i] = FU;
			}
		}
		
		//int Divs
		if(coreConfig.IntDivNum > 0)
		{
			FUs[FunctionalUnitType.integerDiv.ordinal()] = new FunctionalUnit[coreConfig.IntDivNum];
			for(int i = 0; i < coreConfig.IntDivNum; i++)
			{
				int[] latency = {coreConfig.IntDivLatency};
				int[] reciprocalOfThroughput = {coreConfig.IntDivReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.integerDiv, 1, latency,
						reciprocalOfThroughput, coreConfig.IntDivPortNumbers[i]);
				FUs[FunctionalUnitType.integerDiv.ordinal()][i] = FU;
			}
		}
		
		//float ALUs
		if(coreConfig.FloatALUNum > 0)
		{
			FUs[FunctionalUnitType.floatALU.ordinal()] = new FunctionalUnit[coreConfig.FloatALUNum];
			for(int i = 0; i < coreConfig.FloatALUNum; i++)
			{
				int[] latency = {coreConfig.FloatALULatency};
				int[] reciprocalOfThroughput = {coreConfig.FloatALUReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.floatALU, 1, latency,
						reciprocalOfThroughput, coreConfig.FloatALUPortNumbers[i]);
				FUs[FunctionalUnitType.floatALU.ordinal()][i] = FU;
			}
		}
		
		//float Muls
		if(coreConfig.FloatMulNum > 0)
		{
			FUs[FunctionalUnitType.floatMul.ordinal()] = new FunctionalUnit[coreConfig.FloatMulNum];
			for(int i = 0; i < coreConfig.FloatMulNum; i++)
			{
				int[] latency = {coreConfig.FloatMulLatency};
				int[] reciprocalOfThroughput = {coreConfig.FloatMulReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.floatMul, 1, latency,
						reciprocalOfThroughput, coreConfig.FloatMulPortNumbers[i]);
				FUs[FunctionalUnitType.floatMul.ordinal()][i] = FU;
			}
		}
		
		//float Divs
		if(coreConfig.FloatDivNum > 0)
		{
			FUs[FunctionalUnitType.floatDiv.ordinal()] = new FunctionalUnit[coreConfig.FloatDivNum];
			for(int i = 0; i < coreConfig.FloatDivNum; i++)
			{
				int[] latency = {coreConfig.FloatDivLatency};
				int[] reciprocalOfThroughput = {coreConfig.FloatDivReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.floatDiv, 1, latency,
						reciprocalOfThroughput, coreConfig.FloatDivPortNumbers[i]);
				FUs[FunctionalUnitType.floatDiv.ordinal()][i] = FU;
			}
		}
		
		//FMA
		if(coreConfig.FMANum > 0)
		{
			FUs[FunctionalUnitType.FMA.ordinal()] = new FunctionalUnit[coreConfig.FMANum];
			for(int i = 0; i < coreConfig.FMANum; i++)
			{
				int[] latency = {coreConfig.FloatALULatency, coreConfig.FloatMulLatency, coreConfig.FloatVectorALULatency, coreConfig.FloatVectorMulLatency};
				int[] reciprocalOfThroughput = {coreConfig.FloatALUReciprocalOfThroughput, coreConfig.FloatMulReciprocalOfThroughput, coreConfig.FloatVectorALUReciprocalOfThroughput, coreConfig.FloatVectorMulReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.FMA, 4, latency,
						reciprocalOfThroughput, coreConfig.FMAPortNumbers[i]);
				FUs[FunctionalUnitType.FMA.ordinal()][i] = FU;
			}
		}
		
		//int vector ALUs
		if(coreConfig.IntVectorALUNum > 0)
		{
			FUs[FunctionalUnitType.integerVectorALU.ordinal()] = new FunctionalUnit[coreConfig.IntVectorALUNum];
			for(int i = 0; i < coreConfig.IntVectorALUNum; i++)
			{
				int[] latency = {coreConfig.IntVectorALULatency};
				int[] reciprocalOfThroughput = {coreConfig.IntVectorALUReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.integerVectorALU, 1, latency,
						reciprocalOfThroughput, coreConfig.IntVectorALUPortNumbers[i]);
				FUs[FunctionalUnitType.integerVectorALU.ordinal()][i] = FU;
			}
		}
		
		//int vector Muls
		if(coreConfig.IntVectorMulNum > 0)
		{
			FUs[FunctionalUnitType.integerVectorMul.ordinal()] = new FunctionalUnit[coreConfig.IntVectorMulNum];
			for(int i = 0; i < coreConfig.IntVectorMulNum; i++)
			{
				int[] latency = {coreConfig.IntVectorMulLatency};
				int[] reciprocalOfThroughput = {coreConfig.IntVectorMulReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.integerVectorMul, 1, latency,
						reciprocalOfThroughput, coreConfig.IntVectorMulPortNumbers[i]);
				FUs[FunctionalUnitType.integerVectorMul.ordinal()][i] = FU;
			}
		}
		
		//float vector ALUs
		if(coreConfig.FloatVectorALUNum > 0)
		{
			FUs[FunctionalUnitType.floatVectorALU.ordinal()] = new FunctionalUnit[coreConfig.FloatVectorALUNum];
			for(int i = 0; i < coreConfig.FloatVectorALUNum; i++)
			{
				int[] latency = {coreConfig.FloatVectorALULatency};
				int[] reciprocalOfThroughput = {coreConfig.FloatVectorALUReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.floatVectorALU, 1, latency,
						reciprocalOfThroughput, coreConfig.FloatVectorALUPortNumbers[i]);
				FUs[FunctionalUnitType.floatVectorALU.ordinal()][i] = FU;
			}
		}
		
		//float vector Muls
		if(coreConfig.FloatVectorMulNum > 0)
		{
			FUs[FunctionalUnitType.floatVectorMul.ordinal()] = new FunctionalUnit[coreConfig.FloatVectorMulNum];
			for(int i = 0; i < coreConfig.FloatVectorMulNum; i++)
			{
				int[] latency = {coreConfig.FloatVectorMulLatency};
				int[] reciprocalOfThroughput = {coreConfig.FloatVectorMulReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.floatVectorMul, 1, latency,
						reciprocalOfThroughput, coreConfig.FloatVectorMulPortNumbers[i]);
				FUs[FunctionalUnitType.floatVectorMul.ordinal()][i] = FU;
			}
		}
		
		//branch
		if(coreConfig.BranchNum > 0)
		{
			FUs[FunctionalUnitType.branch.ordinal()] = new FunctionalUnit[coreConfig.BranchNum];
			for(int i = 0; i < coreConfig.BranchNum; i++)
			{
				int[] latency = {coreConfig.BranchLatency};
				int[] reciprocalOfThroughput = {coreConfig.BranchReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.branch, 1, latency,
						reciprocalOfThroughput, coreConfig.BranchPortNumbers[i]);
				FUs[FunctionalUnitType.branch.ordinal()][i] = FU;
			}
		}
		
		//load
		if(coreConfig.LoadNum > 0)
		{
			FUs[FunctionalUnitType.load.ordinal()] = new FunctionalUnit[coreConfig.LoadNum];
			for(int i = 0; i < coreConfig.LoadNum; i++)
			{
				int[] latency = {coreConfig.LoadLatency};
				int[] reciprocalOfThroughput = {coreConfig.LoadReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.load, 1, latency,
						reciprocalOfThroughput, coreConfig.LoadPortNumbers[i]);
				FUs[FunctionalUnitType.load.ordinal()][i] = FU;
			}
		}
		
		//load AGU
		if(coreConfig.LoadAGUNum > 0)
		{
			FUs[FunctionalUnitType.loadAGU.ordinal()] = new FunctionalUnit[coreConfig.LoadAGUNum];
			for(int i = 0; i < coreConfig.LoadAGUNum; i++)
			{
				int[] latency = {coreConfig.LoadAGULatency};
				int[] reciprocalOfThroughput = {coreConfig.LoadAGUReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.loadAGU, 1, latency,
						reciprocalOfThroughput, coreConfig.LoadAGUPortNumbers[i]);
				FUs[FunctionalUnitType.loadAGU.ordinal()][i] = FU;
			}
		}
		
		//load
		if(coreConfig.StoreNum > 0)
		{
			FUs[FunctionalUnitType.store.ordinal()] = new FunctionalUnit[coreConfig.StoreNum];
			for(int i = 0; i < coreConfig.StoreNum; i++)
			{
				int[] latency = {coreConfig.StoreLatency};
				int[] reciprocalOfThroughput = {coreConfig.StoreReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.store, 1, latency,
						reciprocalOfThroughput, coreConfig.StorePortNumbers[i]);
				FUs[FunctionalUnitType.store.ordinal()][i] = FU;
			}
		}
		
		//load AGU
		if(coreConfig.StoreAGUNum > 0)
		{
			FUs[FunctionalUnitType.storeAGU.ordinal()] = new FunctionalUnit[coreConfig.StoreAGUNum];
			for(int i = 0; i < coreConfig.StoreAGUNum; i++)
			{
				int[] latency = {coreConfig.StoreAGULatency};
				int[] reciprocalOfThroughput = {coreConfig.StoreAGUReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.storeAGU, 1, latency,
						reciprocalOfThroughput, coreConfig.StoreAGUPortNumbers[i]);
				FUs[FunctionalUnitType.storeAGU.ordinal()][i] = FU;
			}
		}
		
		//AES
		if(coreConfig.AESNum > 0)
		{
			FUs[FunctionalUnitType.AES.ordinal()] = new FunctionalUnit[coreConfig.AESNum];
			for(int i = 0; i < coreConfig.AESNum; i++)
			{
				int[] latency = {coreConfig.AESLatency};
				int[] reciprocalOfThroughput = {coreConfig.AESReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.AES, 1, latency,
						reciprocalOfThroughput, coreConfig.AESPortNumbers[i]);
				FUs[FunctionalUnitType.AES.ordinal()][i] = FU;
			}
		}
		
		//Vector String
		if(coreConfig.VectorStringNum > 0)
		{
			FUs[FunctionalUnitType.vectorString.ordinal()] = new FunctionalUnit[coreConfig.VectorStringNum];
			for(int i = 0; i < coreConfig.VectorStringNum; i++)
			{
				int[] latency = {coreConfig.VectorStringLatency};
				int[] reciprocalOfThroughput = {coreConfig.VectorStringReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.vectorString, 1, latency,
						reciprocalOfThroughput, coreConfig.VectorStringPortNumbers[i]);
				FUs[FunctionalUnitType.vectorString.ordinal()][i] = FU;
			}
		}
		
		//bit scan
		if(coreConfig.BitScanNum > 0)
		{
			FUs[FunctionalUnitType.bitScan.ordinal()] = new FunctionalUnit[coreConfig.BitScanNum];
			for(int i = 0; i < coreConfig.BitScanNum; i++)
			{
				int[] latency = {coreConfig.BitScanLatency};
				int[] reciprocalOfThroughput = {coreConfig.BitScanReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.bitScan, 1, latency,
						reciprocalOfThroughput, coreConfig.BitScanPortNumbers[i]);
				FUs[FunctionalUnitType.bitScan.ordinal()][i] = FU;
			}
		}
		
		//vector shuffle
		if(coreConfig.VectorShuffleNum > 0)
		{
			FUs[FunctionalUnitType.vectorShuffle.ordinal()] = new FunctionalUnit[coreConfig.VectorShuffleNum];
			for(int i = 0; i < coreConfig.VectorShuffleNum; i++)
			{
				int[] latency = {coreConfig.VectorShuffleLatency};
				int[] reciprocalOfThroughput = {coreConfig.VectorShuffleReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.vectorShuffle, 1, latency,
						reciprocalOfThroughput, coreConfig.VectorShufflePortNumbers[i]);
				FUs[FunctionalUnitType.vectorShuffle.ordinal()][i] = FU;
			}
		}
		
		//LEA
		if(coreConfig.LEANum > 0)
		{
			FUs[FunctionalUnitType.LEA.ordinal()] = new FunctionalUnit[coreConfig.LEANum];
			for(int i = 0; i < coreConfig.LEANum; i++)
			{
				int[] latency = {coreConfig.LEALatency};
				int[] reciprocalOfThroughput = {coreConfig.LEAReciprocalOfThroughput};
				FunctionalUnit FU = new FunctionalUnit(FunctionalUnitType.LEA, 1, latency,
						reciprocalOfThroughput, coreConfig.LEAPortNumbers[i]);
				FUs[FunctionalUnitType.LEA.ordinal()][i] = FU;
			}
		}
		
		this.numPorts = coreConfig.ExecutionCoreNumPorts;
		portUsedThisCycle = new boolean[numPorts];
	}
	
	//if an FU is available, it is assigned (timeTillFUAvailable is updated);
	//						negative of the FU instance is returned
	//else, the earliest time, at which an FU of the type becomes available, is returned
	
	public long requestFU(FunctionalUnitType FUType, int functionality)
	{
		if(FUs[FUType.ordinal()] == null || FUs[FUType.ordinal()].length == 0)
		{
			return Long.MAX_VALUE;
		}		
		
		long currentTime = GlobalClock.getCurrentTime();
		long stepSize = core.getStepSize();
		
		long timeTillAvailable = Long.MAX_VALUE;
		
		for(int i = 0; i < FUs[FUType.ordinal()].length; i++)
		{
			boolean canUse = true;
			if(FUs[FUType.ordinal()][i].getTimeWhenFUAvailable() > currentTime)
			{
				canUse = false;
			}
			if(portUsedThisCycle[FUs[FUType.ordinal()][i].getPortNumber()] == true)
			{
				canUse = false;
			}
			
			if(canUse == true)
			{
				FUs[FUType.ordinal()][i].setTimeWhenFUAvailable(currentTime
						+ FUs[FUType.ordinal()][i].getReciprocalOfThroughput(functionality)*stepSize);
				
				FUs[FUType.ordinal()][i].incrementNoOfCyclesBusy(FUs[FUType.ordinal()][i].getLatency(functionality));
				
				portUsedThisCycle[FUs[FUType.ordinal()][i].getPortNumber()] = true;
				
				return i * (-1);
			}
			if(FUs[FUType.ordinal()][i].getTimeWhenFUAvailable() < timeTillAvailable)
			{
				timeTillAvailable = FUs[FUType.ordinal()][i].getTimeWhenFUAvailable();
			}
		}
		
		return timeTillAvailable;
	}
	
	public int getFULatency(FunctionalUnitType FUType, int functionality)
	{
		return FUs[FUType.ordinal()][0].getLatency(functionality);
	}
	
	public int getNumberOfUnits(FunctionalUnitType FUType)
	{
		return FUs[FUType.ordinal()].length;
	}
	
	public long getTimeWhenFUAvailable(FunctionalUnitType _FUType, int _FUInstance)
	{
		return FUs[_FUType.ordinal()][_FUInstance].getTimeWhenFUAvailable();
	}
	
	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		EnergyConfig totalPower = new EnergyConfig(0, 0);
		
		for(int i = 0; i < FUs.length; i++)
		{
			if(FUs[i] != null && FUs[i].length > 0)
			{
				FunctionalUnitType FUType = FUs[i][0].getFUType();
				for(int j = 0; j < FUs[i].length; j++)
				{
					EnergyConfig FUPower = new EnergyConfig(0, 0);
					
					switch(FUType)
					{
					case integerALU : {
						FUPower = new EnergyConfig(core.getCoreConfig().IntALUPower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case integerMul : {
						FUPower = new EnergyConfig(core.getCoreConfig().IntMulPower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case integerDiv : {
						FUPower = new EnergyConfig(core.getCoreConfig().IntDivPower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case floatALU : {
						FUPower = new EnergyConfig(core.getCoreConfig().FloatALUPower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case floatMul : {
						FUPower = new EnergyConfig(core.getCoreConfig().FloatMulPower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case floatDiv : {
						FUPower = new EnergyConfig(core.getCoreConfig().FloatDivPower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case integerVectorALU : {
						FUPower = new EnergyConfig(core.getCoreConfig().IntVectorALUPower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case integerVectorMul : {
						FUPower = new EnergyConfig(core.getCoreConfig().IntVectorMulPower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case floatVectorALU : {
						FUPower = new EnergyConfig(core.getCoreConfig().FloatVectorALUPower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case floatVectorMul : {
						FUPower = new EnergyConfig(core.getCoreConfig().FloatVectorMulPower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case FMA : {
						FUPower = new EnergyConfig(core.getCoreConfig().FMAPower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case branch : {
						FUPower = new EnergyConfig(core.getCoreConfig().BranchPower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case load : {
						FUPower = new EnergyConfig(core.getCoreConfig().LoadPower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case loadAGU : {
						FUPower = new EnergyConfig(core.getCoreConfig().LoadAGUPower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case store : {
						FUPower = new EnergyConfig(core.getCoreConfig().StorePower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case storeAGU : {
						FUPower = new EnergyConfig(core.getCoreConfig().StoreAGUPower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case AES : {
						FUPower = new EnergyConfig(core.getCoreConfig().AESPower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case vectorString : {
						FUPower = new EnergyConfig(core.getCoreConfig().VectorStringPower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case bitScan : {
						FUPower = new EnergyConfig(core.getCoreConfig().BitScanPower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case vectorShuffle : {
						FUPower = new EnergyConfig(core.getCoreConfig().VectorShufflePower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					case LEA : {
						FUPower = new EnergyConfig(core.getCoreConfig().LEAPower, FUs[i][j].getNoOfCyclesBusy());
						break;
					}
					default : {
						misc.Error.showErrorAndExit("trying to compute power consumed of unknown FU Type " + FUType);
					}
					}				
					
					totalPower.add(FUPower);
				}
			}
		}
		
		return totalPower;
	}
	
	public void clearPortUsage()
	{
		for(int i = 0; i < numPorts; i++)
		{
			portUsedThisCycle[i] = false;
		}
	}

}
