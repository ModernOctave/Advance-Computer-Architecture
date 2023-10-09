package generic;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import memorysystem.CoreMemorySystem;

import memorysystem.Cache;
import memorysystem.MemorySystem;
import config.EnergyConfig;
import config.SystemConfig;

public class GlobalClock {
	
	static long currentTime = 0;
	static int stepSize = 1;
	public static long effectiveGlobalClockFrequency;

	public static long getCurrentTime() {
		return GlobalClock.currentTime;
	}

	public static void setCurrentTime(long currentTime) {
		GlobalClock.currentTime = currentTime;
	}
	
	public static void incrementClock()
	{
		GlobalClock.currentTime += GlobalClock.stepSize;
	}

	public static int getStepSize() {
		return GlobalClock.stepSize;
	}

	public static void setStepSize(int stepSize) {
		GlobalClock.stepSize = stepSize;
	}
	
	public static EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		double leakagePower = SystemConfig.globalClockPower.leakageEnergy;
		double dynamicPower = SystemConfig.globalClockPower.dynamicEnergy;
		
		EnergyConfig power = new EnergyConfig(leakagePower, dynamicPower);
		
		power.printEnergyStats(outputFileWriter, componentName);
		
		return power;
	}
}
