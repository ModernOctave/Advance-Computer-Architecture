package pipeline.outoforder;

import java.io.FileWriter;
import java.io.IOException;

import config.EnergyConfig;
import generic.Core;
import generic.Event;
import generic.EventQueue;
import generic.PortType;
import generic.SimulationElement;

public class RegisterFile extends SimulationElement{
	
	private Core core;
	private int registerFileSize;
	private Object[] value;
	private boolean[] valueValid;					//currently used only for
	private ReorderBufferEntry[] producerROBEntry;	//machine specific registers
	long numAccesses;
	
	public RegisterFile(Core core, int _registerFileSize)
	{
		super(PortType.Unlimited, -1, -1, -1, -1);
		
		this.core = core;
		registerFileSize = _registerFileSize;
		value = new Object[registerFileSize];
		valueValid = new boolean[registerFileSize];
		producerROBEntry = new ReorderBufferEntry[registerFileSize];
		for(int i = 0; i < registerFileSize; i++)
		{
			valueValid[i] = true;
			producerROBEntry[i] = null;
		}
	}

	public Object getValue(int index) {
		return value[index];
	}

	public void setValue(Object value, int index) {
		this.value[index] = value;
	}

	public int getRegisterFileSize() {
		return registerFileSize;
	}

	public boolean getValueValid(int index) {
		incrementNumAccesses(1);
		return valueValid[index];
	}

	public void setValueValid(boolean valueValid, int index) {
		this.valueValid[index] = valueValid;
		if(valueValid == true)
		{
			incrementNumAccesses(1);
		}
	}

	public ReorderBufferEntry getProducerROBEntry(int index) {
		return producerROBEntry[index];
	}

	public void setProducerROBEntry(ReorderBufferEntry producerROBEntry, int index) {
		this.producerROBEntry[index] = producerROBEntry;
	}

	public Core getCore() {
		return core;
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
		EnergyConfig totalPower = null;
		
		if(((OutOrderExecutionEngine)core.getExecEngine()).getIntegerRegisterFile() == this)
		{
			totalPower = new EnergyConfig(core.getIntRegFilePower(), numAccesses);
		}
		else
		{
			totalPower = new EnergyConfig(core.getVectorRegFilePower(), numAccesses);
		}
		
		totalPower.printEnergyStats(outputFileWriter, componentName);		
		return totalPower;
	}
}