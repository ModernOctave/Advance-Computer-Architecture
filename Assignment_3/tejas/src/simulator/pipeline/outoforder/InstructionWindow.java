package pipeline.outoforder;

import java.io.FileWriter;
import java.io.IOException;

import pipeline.ExecutionEngine;

import config.EnergyConfig;
import generic.Core;
import generic.Event;
import generic.EventQueue;
import generic.PortType;
import generic.SimulationElement;

public class InstructionWindow extends SimulationElement {
	
	/*
	 * IW is implemented as an unordered buffer
	 * the precedence required when issuing instructions, is achieved by the ordering of the ROB
	 */
	
	Core core;
	IWEntry[] IW;
	int maxIWSize;
	
	int[] availList;
	int availListHead;
	int availListTail;
	
	long numAccesses;
	
	public InstructionWindow(Core core, OutOrderExecutionEngine executionEngine)
	{
		super(PortType.Unlimited, -1, -1, -1, -1);
		this.core = core;
		
		maxIWSize = core.getIWSize();
		IW = new IWEntry[maxIWSize];
		availList = new int[maxIWSize];
		for(int i = 0; i < maxIWSize; i++)
		{
			IW[i] = new IWEntry(core, i, executionEngine, this);
			availList[i] = i;
		}
		availListHead = 0;
		availListTail = maxIWSize - 1;
		
	}
	
	public IWEntry addToWindow(ReorderBufferEntry ROBEntry)
	{
		int index = findInvalidEntry();
		if(index == -1)
		{
			//Instruction window full
			return null;
		}
		
		IWEntry newEntry = IW[index];
		
		newEntry.setInstruction(ROBEntry.getInstruction());
		newEntry.setAssociatedROBEntry(ROBEntry);
		newEntry.setValid(true);
		
		ROBEntry.setAssociatedIWEntry(newEntry);
		
		incrementNumAccesses(1);
		
		return newEntry;
	}
		
	int findInvalidEntry()
	{
		if(availListHead == -1)
		{
			return -1;
		}
		
		int temp = availListHead;
		if(availListHead == availListTail)
		{
			availListHead = -1;
			availListTail = -1;
		}
		else
		{
			availListHead = (availListHead + 1)%maxIWSize;
		}
		
		return availList[temp];
	}
	
	public void removeFromWindow(IWEntry entryToBeRemoved)
	{
		entryToBeRemoved.setValid(false);
		availListTail = (availListTail + 1)%maxIWSize;
		availList[availListTail] = entryToBeRemoved.pos;
		if(availListHead == -1)
		{
			availListHead = availListTail;
		}
		
		incrementNumAccesses(1);
	}
	
	public void flush()
	{
		for(int i = 0; i < maxIWSize; i++)
		{
			IW[i].setValid(false);
		}
	}

	public IWEntry[] getIW() {
		return IW;
	}
	
	public boolean isFull()
	{
		if(availListHead == -1)
		{
			return true;
		}
		return false;
	}
	
	public int getMaxIWSize() {
		return maxIWSize;
	}

	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
				
	}
	
	void incrementNumAccesses(int incrementBy)
	{
		numAccesses += incrementBy;
	}

	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		EnergyConfig power = new EnergyConfig(core.getIwPower(), numAccesses);
		power.printEnergyStats(outputFileWriter, componentName);
		return power;
	}

}