package generic;

public class Port 
{
	private SimulationElement connectedElement;
	private PortType portType;
	private int noOfReadPorts;
	private int noOfWritePorts;
	private int noOfReadWritePorts;

	//occupancy defines the number of clockCycles needed for  
	//a single transfer on the port.
	private long readLatencyOfConnectedElement;
	private long writeLatencyOfConnectedElement;
	private long readOccupancy;
	private long writeOccupancy;
	private long[] portBusyUntil;
	
	//NOTE : Time is in terms of GlobalClock cycles
	
	public Port(SimulationElement connectedElement, PortType portType, int noOfPorts, long occupancy, long latencyOfConnectedElement)
	{
		this.connectedElement = connectedElement;
		this.portType = portType;
		this.readLatencyOfConnectedElement = latencyOfConnectedElement;
		this.writeLatencyOfConnectedElement = latencyOfConnectedElement;
		
		//initialise no. of ports and the occupancy.
		if(portType==PortType.Unlimited)
		{
			return;
		}
		
		else if(portType!=PortType.Unlimited && 
				noOfPorts>0 && occupancy>0)
		{
			// For a FCFS or a priority based port, noOfPorts and
			// occupancy must be non-zero.
			this.noOfReadPorts = 0;
			this.noOfWritePorts = 0;
			this.noOfReadWritePorts = noOfPorts;
			this.readOccupancy = occupancy;
			this.writeOccupancy = occupancy;
			
			//set busy field of all ports to 0
			portBusyUntil = new long[noOfReadPorts+noOfWritePorts+noOfReadWritePorts];
					
			for(int i=0; i < noOfReadPorts+noOfWritePorts+noOfReadWritePorts; i++)
			{
				this.portBusyUntil[i] = 0;
			}
		}
		
		else
		{
			// Display an error for invalid initialization.
			misc.Error.showErrorAndExit("Invalid initialization of port !!\n");
		}
	}
	
	public Port(SimulationElement connectedElement, PortType portType, int noOfReadPorts, int noOfWritePorts, int noOfReadWritePorts, long readOccupancy, long writeOccupancy, long readLatencyOfConnectedElement, long writeLatencyOfConnectedElement)
	{
		this.connectedElement = connectedElement;
		this.portType = portType;
		this.readLatencyOfConnectedElement = readLatencyOfConnectedElement;
		this.writeLatencyOfConnectedElement = writeLatencyOfConnectedElement;
		
		//initialise no. of ports and the occupancy.
		if(portType==PortType.Unlimited)
		{
			return;
		}
		
		else if(portType!=PortType.Unlimited && 
				(noOfReadPorts+noOfReadWritePorts)>0 && readOccupancy>0 && 
				(noOfWritePorts+noOfReadWritePorts)>0 && writeOccupancy>0)
		{
			// For a FCFS or a priority based port, noOfPorts and
			// occupancy must be non-zero.
			this.noOfReadPorts = noOfReadPorts;
			this.noOfWritePorts = noOfWritePorts;
			this.noOfReadWritePorts = noOfReadWritePorts;
			this.readOccupancy = readOccupancy;
			this.writeOccupancy = writeOccupancy;
			
			//a single portBusyUntil array for all ports
			////first 'noOfReadPorts' number of ports correspond to read-only ports
			////next 'noOfWritePorts' number of ports correspond to write-only ports
			////next 'noOfReadWritePorts' number of ports correspond to readwrite ports
			//set busy field of all ports to 0
			portBusyUntil = new long[noOfReadPorts+noOfWritePorts+noOfReadWritePorts];
					
			for(int i=0; i < noOfReadPorts+noOfWritePorts+noOfReadWritePorts; i++)
			{
				this.portBusyUntil[i] = 0;
			}
		}
		
		else
		{
			// Display an error for invalid initialization.
			misc.Error.showErrorAndExit("Invalid initialization of port !!\n");
		}
	}
	
	public void put(Event event)
	{
		//overloaded method.
		this.put(event, 1);
	}
	
	public void put(Event event, int noOfSlots)
	{
		PortRequestType portRequestType;
		if((event.getRequestType() == RequestType.Cache_Write
				//if reqE != procE, this means this write request is from upper level
				//and that we only need to spend access latency, and not write latency, at this point
				//if reqE == procE, this means the line is in the cache, and the write needs to be performed; hence, spend write latency / occupancy
				&& event.getProcessingElement() == connectedElement)
				//line received from below needs to be written to the cache; hence, write port
				|| event.getRequestType() == RequestType.Mem_Response)
		{
			portRequestType = PortRequestType.Write;
		}
		else
		{
			portRequestType = PortRequestType.Read;
		}
		
		if(this.portType == PortType.Unlimited)
		{
			if(portRequestType == PortRequestType.Read)
			{
				event.addEventTime(GlobalClock.getCurrentTime() + readLatencyOfConnectedElement);
			}
			else
			{
				event.addEventTime(GlobalClock.getCurrentTime() + writeLatencyOfConnectedElement);
			}
			event.getEventQ().addEvent(event);
			return;
		}
		
		else if(this.portType==PortType.FirstComeFirstServe)
		{
			//else return the slot that will be available earliest.
			int availablePortID = -1;
			long occupancy;
			long latencyOfConnectedElement;
			
			if(portRequestType == PortRequestType.Read)
			{
				availablePortID = 0;
				occupancy = readOccupancy;
				latencyOfConnectedElement = readLatencyOfConnectedElement;
			}
			else
			{
				availablePortID = noOfReadPorts;
				occupancy = writeOccupancy;
				//for writes, the time taken to search for the line/set has already been spent
				//for mem response, the time taken to search for the line/set must also be spent at this point
				if(event.getRequestType() == RequestType.Mem_Response)
				{
					latencyOfConnectedElement = writeLatencyOfConnectedElement;
				}
				else
				{
					latencyOfConnectedElement = writeLatencyOfConnectedElement-readLatencyOfConnectedElement;
				}
			}
			
			// find the first available port
			if(portRequestType == PortRequestType.Read)
			{
				for(int i=0; i<noOfReadPorts; i++)
				{
					if(portBusyUntil[i]< 
						portBusyUntil[availablePortID])
					{
						availablePortID = i;
					}
				}
				for(int i=noOfReadPorts+noOfWritePorts; i<noOfReadPorts+noOfWritePorts+noOfReadWritePorts; i++)
				{
					if(portBusyUntil[i]< 
						portBusyUntil[availablePortID])
					{
						availablePortID = i;
					}
				}
			}
			else
			{
				for(int i=noOfReadPorts; i<noOfReadPorts+noOfWritePorts+noOfReadWritePorts; i++)
				{
					if(portBusyUntil[i]< 
						portBusyUntil[availablePortID])
					{
						availablePortID = i;
					}
				}
			}
			
			// If a port is available, set its portBusyUntil field to
			// current time
			if(portBusyUntil[availablePortID]<
				GlobalClock.getCurrentTime())
			{
				// this port will be busy for next occupancy cycles
				portBusyUntil[availablePortID] = 
					GlobalClock.getCurrentTime() + occupancy;
			}else{
				// set the port as busy for occupancy cycles
				portBusyUntil[availablePortID] += occupancy;	
			}
						
			// set the time of the event
			event.addEventTime(portBusyUntil[availablePortID] - occupancy + latencyOfConnectedElement);
			
			// add event in the eventQueue
			event.getEventQ().addEvent(event);
		}
	}

	public PortType getPortType() {
		return portType;
	}

	public long getReadLatencyOfConnectedElement() {
		return readLatencyOfConnectedElement;
	}

	public long getWriteLatencyOfConnectedElement() {
		return writeLatencyOfConnectedElement;
	}
}