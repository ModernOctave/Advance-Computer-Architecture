package pipeline;


public class FunctionalUnit {
	
	FunctionalUnitType FUType;
	int numFunctionalities;
	/*
	 * FMA can do float ALU, float Mul, float vector ALU, float vector Mul, FMA, vector FMA
	 */
	int[] latency;
	int[] reciprocalOfThroughput;
	long timeWhenFUAvailable;
	int portNumber;
	
	long noOfCyclesBusy;
	
	public FunctionalUnit(FunctionalUnitType FUType, int numFunctionalities, int[] latency,
			int[] reciprocalOfThroughput, int portNumber)
	{
		this.FUType = FUType;
		this.numFunctionalities = numFunctionalities;
		this.latency = new int[numFunctionalities];
		this.reciprocalOfThroughput = new int[numFunctionalities];
		for(int i = 0; i < numFunctionalities; i++)
		{
			this.latency[i] = latency[i];
			this.reciprocalOfThroughput[i] = reciprocalOfThroughput[i];
		}
		this.timeWhenFUAvailable = 0;
		this.portNumber = portNumber;
	}

	public FunctionalUnitType getFUType() {
		return FUType;
	}

	public int getLatency(int functionality) {
		return latency[functionality];
	}

	public int getReciprocalOfThroughput(int functionality) {
		return reciprocalOfThroughput[functionality];
	}

	public long getTimeWhenFUAvailable() {
		return timeWhenFUAvailable;
	}

	public void setTimeWhenFUAvailable(long timeWhenFUAvailable) {
		this.timeWhenFUAvailable = timeWhenFUAvailable;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public long getNoOfCyclesBusy() {
		return noOfCyclesBusy;
	}

	public void incrementNoOfCyclesBusy(long noOfCyclesBusy) {
		this.noOfCyclesBusy += noOfCyclesBusy;
	}

}
