package generic;

public abstract class SimulationElement implements Cloneable
{
	//a simulation element encapsulates a port.
	//all the request for the port are ported through simulationElement
	Port port;
	protected long latency;
	protected long stepSize = 1;
	CommunicationInterface comInterface;

   public Object clone()
    {
        try
        {
            // call clone in Object.
            return super.clone();
        } catch(CloneNotSupportedException e)
        {
            System.out.println("Cloning not allowed.");
            return this;
        }
    }

	
	public SimulationElement(PortType portType, 
		int noOfPorts, long occupancy, long latency, long frequency	)
	{
		this.port = new Port(this, portType, noOfPorts, occupancy, latency);
		this.latency = latency;
	}

	
	public SimulationElement(PortType portType, 
		int noOfReadPorts, int noOfWritePorts, int noOfReadWritePorts, long readOccupancy, long writeOccupancy, long readLatency, long writeLatency, long frequency	)
	{
		this.port = new Port(this, portType, noOfReadPorts, noOfWritePorts, noOfReadWritePorts, readOccupancy, writeOccupancy, readLatency, writeLatency);
		this.latency = readLatency;
	}
	
	//To get the time delay(due to latency) to schedule the event 
	public long getLatencyDelay()
	{
		return (this.latency /** this.stepSize*/);
	}
	
	public long getLatency() 
	{
		return this.latency;
	}
	
	protected void setLatency(long latency) {
		this.latency = latency;
	}

	public Port getPort()
	{
		return this.port;
	}	
	
	public void setPort(Port port){
		this.port = port;
	}
	
	/*public long getFrequency() {
		return frequency;
	}

	

	public int getStepSize() {
		return stepSize;
	}

	public void setStepSize(int stepSize) {
		this.stepSize = stepSize;
	}*/
	
//	public abstract void handleEvent(EventQueue eventQueue);
	public abstract void handleEvent(EventQueue eventQ, Event event);

	public void setComInterface(CommunicationInterface comInterface) {
		this.comInterface = comInterface;
	}
	
	public CommunicationInterface getComInterface() {
		return comInterface;
	}
	
	public void sendEvent(Event event) {
		if (event.getEventTime() != 0) {
			misc.Error.showErrorAndExit("Send event with zero latency !!");
		}

		if (event.getProcessingElement().getComInterface() != this
				.getComInterface()) {
			getComInterface().sendMessage(event);
		} else {
			event.getProcessingElement().getPort().put(event);
		}
	}
}