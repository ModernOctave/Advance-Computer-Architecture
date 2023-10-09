package memorysystem;

import generic.Event;
import generic.EventQueue;
import generic.RequestType;
import generic.SimulationElement;

import java.io.FileWriter;
import java.io.IOException;

import config.EnergyConfig;
import config.SystemConfig;

public class MainMemoryController extends SimulationElement
{
	long numAccesses;
	
	public MainMemoryController() {
		super(SystemConfig.mainMemPortType,
				SystemConfig.mainMemoryAccessPorts,
				SystemConfig.mainMemoryPortOccupancy * SystemConfig.mainMemoryStepSize,
				SystemConfig.mainMemoryLatency * SystemConfig.mainMemoryStepSize,
				SystemConfig.mainMemoryFrequency
				);
	}
	
	public void handleEvent(EventQueue eventQ, Event event)
	{
		if (event.getRequestType() == RequestType.Cache_Read)
		{
			AddressCarryingEvent e = new AddressCarryingEvent(eventQ, 0,
					this, event.getRequestingElement(),	RequestType.Mem_Response,
					((AddressCarryingEvent)event).getAddress());
			
			getComInterface().sendMessage(e);
		}
		else if (event.getRequestType() == RequestType.Cache_Write)
		{
			//Just to tell the requesting things that the write is completed
		}
		
		incrementNumAccesses();
	}
	
	void incrementNumAccesses()
	{
		numAccesses += 1;
	}
	
	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		EnergyConfig power = new EnergyConfig(SystemConfig.mainMemoryControllerPower, numAccesses);
		power.printEnergyStats(outputFileWriter, componentName);
		return power;
	}
	
	public EnergyConfig calculateEnergy(FileWriter outputFileWriter) throws IOException
	{
		EnergyConfig power = new EnergyConfig(SystemConfig.mainMemoryControllerPower, numAccesses);
		return power;
	}
}
