package pipeline.outoforder;

import generic.Event;
import generic.RequestType;
import generic.SimulationElement;

public class BroadCastEvent extends Event {
	
	ReorderBufferEntry ROBEntry;

	public BroadCastEvent(long eventTime,
			SimulationElement requestingElement,
			SimulationElement processingElement,
			RequestType requestType,
			ReorderBufferEntry ROBEntry)
	{
		super(null, eventTime, requestingElement, processingElement, requestType, -1);
		
		this.ROBEntry = ROBEntry;
	}

	public ReorderBufferEntry getROBEntry() {
		return ROBEntry;
	}

}
