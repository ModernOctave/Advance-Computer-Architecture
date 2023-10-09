package generic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import config.CommunicationType;
import config.EmulatorConfig;
import config.EmulatorType;
import config.SystemConfig;

public class BenchmarkThreadMapping {
	ArrayList<Integer> threadToBenchmarkMapping;
	int numBenchmarks;
	
	public BenchmarkThreadMapping(String basenameForBenchmarks[]) {

		threadToBenchmarkMapping = new ArrayList<Integer>();
		
		if(EmulatorConfig.emulatorType==EmulatorType.none 
			&& EmulatorConfig.communicationType==CommunicationType.file) 
		{
			numBenchmarks = basenameForBenchmarks.length;
			
			int numTotalThreads = 0;
			for (int benchmark=0; benchmark<basenameForBenchmarks.length; benchmark++) {
				for (int tid=0; ;tid++) {
					String inputFileName = basenameForBenchmarks[benchmark] + "_" + tid + ".gz";
					try {
						File f = new File(inputFileName);
						if(f.exists() && !f.isDirectory()) {
							threadToBenchmarkMapping.add(benchmark);
							numTotalThreads++;
						} else {
							break;
						}			
					} catch (Exception e) {
						break;
					}
				}
			}
			
			// Assign the last benchmark ID to remaining threads
			for(int i = numTotalThreads; i<SystemConfig.NoOfCores; i++) {
				threadToBenchmarkMapping.add(numBenchmarks-1);
			}
			
		} else {
			// We support multiple benchmarks only for file interface.
			// For any other configuration, lets assume one benchmark only
			// For each benchmark, we set the benchmark ID to zero.
			numBenchmarks = 1;
			for(int i = 0; i<SystemConfig.NoOfCores; i++) {
				threadToBenchmarkMapping.add(0);
			}
		}
	}
	
	public int getNumBenchmarks() {
		return numBenchmarks;
	}
	
	public int getBenchmarkIDForThread(int tid) {
		int bm = threadToBenchmarkMapping.get(tid); 
		return bm;
	}
}