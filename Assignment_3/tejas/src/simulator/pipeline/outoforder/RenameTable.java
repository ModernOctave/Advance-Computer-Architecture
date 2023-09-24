package pipeline.outoforder;

import java.io.FileWriter;
import java.io.IOException;

import config.EnergyConfig;

import generic.Event;
import generic.EventQueue;
import generic.PortType;
import generic.SimulationElement;

public class RenameTable extends SimulationElement{
	
	OutOrderExecutionEngine execEngine;
	int nArchRegisters;
	int nPhyRegisters;
	int noOfThreads;
	
	int[] archReg;
	int[] threadID;							//thread that is going to write to the register
	boolean[] mappingValid;
	boolean[] valueValid;
	private ReorderBufferEntry[] producerROBEntry;
	
	int[][] archToPhyMapping;
	
	RegisterFile associatedRegisterFile;
	
	int availableList[];
	int availableListHead;
	int availableListTail;
	
	long numRATAccesses;
	long numFreeListAccesses;
	
	public RenameTable(OutOrderExecutionEngine execEngine,
						int nArchRegisters, int nPhyRegisters, RegisterFile associatedRegisterFile,
						int noOfThreads)
	{
		super(PortType.Unlimited, -1, -1, -1, -1);
		this.execEngine = execEngine;
		this.nArchRegisters = nArchRegisters;
		this.nPhyRegisters = nPhyRegisters;
		this.noOfThreads = noOfThreads;
		this.associatedRegisterFile = associatedRegisterFile;
		
		archReg = new int[this.nPhyRegisters];
		threadID = new int[this.nPhyRegisters];
		mappingValid = new boolean[this.nPhyRegisters];
		valueValid = new boolean[this.nPhyRegisters];
		producerROBEntry = new ReorderBufferEntry[this.nPhyRegisters];
		
		if(noOfThreads * this.nArchRegisters > this.nPhyRegisters)
		{
			System.out.println("too many threads, not enough registers!");
			System.exit(1);
		}
		
		int temp;
		for(int j = 0; j < noOfThreads; j++)
		{
			temp = j * this.nArchRegisters;
			for(int i = 0; i < this.nArchRegisters; i++)
			{
				archReg[temp + i] = i;
				threadID[temp + i] = j;
				mappingValid[temp + i] = true;
				valueValid[temp + i] = true;
				producerROBEntry[temp + i] = null;
			}
		}
		
		for(int i = this.nArchRegisters * this.noOfThreads; i < this.nPhyRegisters; i++)
		{
			archReg[i] = -1;
			threadID[i] = -1;
			mappingValid[i] = false;
			valueValid[i] = false;
			producerROBEntry[i] = null;
		}
		
		availableList = new int[this.nPhyRegisters - this.nArchRegisters * this.noOfThreads];
		int ctr = 0;
		for(int i = this.nArchRegisters * this.noOfThreads; i < this.nPhyRegisters; i++)
		{
			availableList[ctr++] = i;
		}
		availableListHead = 0;
		availableListTail = this.nPhyRegisters - this.nArchRegisters * this.noOfThreads - 1;
		
		archToPhyMapping = new int[this.noOfThreads][this.nArchRegisters];
		for(int j = 0; j < this.noOfThreads; j++)
		{
			temp = j * this.nArchRegisters;
			for(int i = 0; i < this.nArchRegisters; i++)
			{
				archToPhyMapping[j][i] = temp + i;
			}
		}
		
	}
	
	public int allocatePhysicalRegister(int threadID, int archReg)
	{
		if(availableListHead == -1)
		{
			//no free physical registers
			return -1;
		}
		
		int newPhyReg = removeFromAvailableList();
		if(archReg > 63)
		{
			System.out.println();
		}
		int oldPhyReg = this.archToPhyMapping[threadID][archReg];
		this.archReg[newPhyReg] = archReg;
		this.threadID[newPhyReg] = threadID;
		this.valueValid[newPhyReg] = false;
		this.associatedRegisterFile.setValueValid(false, newPhyReg);		
		this.archToPhyMapping[threadID][archReg] = newPhyReg;
		
		if(this.associatedRegisterFile.getValueValid(oldPhyReg) == true)
		{
			addToAvailableList(oldPhyReg);
		}
		else
		{
			this.mappingValid[oldPhyReg] = false;
		}
		
		this.mappingValid[newPhyReg] = true;
		
		incrementRatAccesses(1);
		
		
		return newPhyReg;
	}

	public int getArchReg(int index) {
		return archReg[index];
	}
	
	public int getThreadID(int index)
	{
		return threadID[index];
	}

	public void setArchReg(int threadID, int archReg, int index) {
		this.archReg[index] = archReg;
		this.archToPhyMapping[threadID][archReg] = index;
	}

	public boolean getMappingValid(int index) {
		return mappingValid[index];
	}

	public void setMappingValid(boolean mappingValid, int index) {
		this.mappingValid[index] = mappingValid;
	}

	public boolean getValueValid(int index) {
		return valueValid[index];
	}

	public void setValueValid(boolean valueValid, int index) {
		this.valueValid[index] = valueValid;
	}
	
	public int getPhysicalRegister(int threadID, int archReg)
	{
		incrementRatAccesses(1);
		return archToPhyMapping[threadID][archReg];
	}

	public ReorderBufferEntry getProducerROBEntry(int index) {
		return producerROBEntry[index];
	}

	public void setProducerROBEntry(ReorderBufferEntry producerROBEntry, int index) {
		this.producerROBEntry[index] = producerROBEntry;
	}	

	public RegisterFile getAssociatedRegisterFile() {
		return associatedRegisterFile;
	}

	public void setAssociatedRegisterFile(RegisterFile associatedRegisterFile) {
		this.associatedRegisterFile = associatedRegisterFile;
	}
	
	public void addToAvailableList(int phyRegNum)
	{
		if(getAvailableListSize() >= this.nPhyRegisters - this.nArchRegisters * this.noOfThreads)
		{
			System.out.println("available register list overflow!!");
			System.exit(1);
		}
		
		availableListTail = (availableListTail + 1)%(this.nPhyRegisters - this.nArchRegisters * this.noOfThreads);
		availableList[availableListTail] = phyRegNum;
		if(availableListHead == -1)
		{
			availableListHead = 0;
		}
		
		incrementFreeListAccesses(1);
	}
	
	public int removeFromAvailableList()
	{
		//NOTE - list empty check to be done before this function is called
		int toBeReturned = availableList[availableListHead];
		
		if(availableListHead == availableListTail)
		{
			availableListHead = availableListTail = -1;
		}
		else
		{
			availableListHead = (availableListHead + 1)%(this.nPhyRegisters - this.nArchRegisters * this.noOfThreads);
		}
		
		incrementFreeListAccesses(1);
		
		return toBeReturned;
	}
	
	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
				
	}
	
	public int getAvailableListSize()
	{
		if(availableListHead == -1)
		{
			return 0;
		}
		
		if(availableListTail >= availableListHead)
		{
			return (availableListTail - availableListHead + 1);
		}
		
		return (this.nPhyRegisters - this.nArchRegisters * this.noOfThreads - availableListHead + availableListTail + 1);
	}
	
	public void incrementRatAccesses(int incrementBy)
	{
		numRATAccesses += incrementBy;
	}
	
	public void incrementFreeListAccesses(int incrementBy)
	{
		numFreeListAccesses += incrementBy;
	}
	
	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		EnergyConfig RATpower = null;
		EnergyConfig freeListPower = null;
		
		if(execEngine.getIntegerRenameTable() == this)
		{
			RATpower = new EnergyConfig(execEngine.getContainingCore().getIntRATPower(), numRATAccesses);
			freeListPower = new EnergyConfig(execEngine.getContainingCore().getIntFreeListPower(), numFreeListAccesses);
		}
		else
		{
			RATpower = new EnergyConfig(execEngine.getContainingCore().getFpRATPower(), numRATAccesses);
			freeListPower = new EnergyConfig(execEngine.getContainingCore().getVectorFreeListPower(), numFreeListAccesses);
		}
		
		EnergyConfig totalPower = new EnergyConfig(0, 0);
		totalPower.add(RATpower);
		totalPower.add(freeListPower);
		
		RATpower.printEnergyStats(outputFileWriter, componentName + ".RAT");
		freeListPower.printEnergyStats(outputFileWriter, componentName + ".FreeList");
		totalPower.printEnergyStats(outputFileWriter, componentName);
		
		return totalPower;
	}

}