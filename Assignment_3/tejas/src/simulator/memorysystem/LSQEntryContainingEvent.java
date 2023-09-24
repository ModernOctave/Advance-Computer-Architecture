package memorysystem;

import generic.EventQueue;
import generic.RequestType;
import generic.SimulationElement;

public class LSQEntryContainingEvent extends AddressCarryingEvent
{
	private LSQEntry lsqEntry;
	
	public LSQEntryContainingEvent(EventQueue eventQ, long eventTime, SimulationElement requestingElement,
			SimulationElement processingElement,
			RequestType requestType, LSQEntry lsqEntry,int coreId) {
		super(eventQ, eventTime, requestingElement, processingElement,
				requestType,lsqEntry.getAddr());
		this.lsqEntry = lsqEntry;
	}
	
	public void updateEvent(EventQueue eventQ, long eventTime, SimulationElement requestingElement,
			SimulationElement processingElement,
			RequestType requestType, LSQEntry lsqEntry) {
		this.lsqEntry = lsqEntry;
		this.update(eventQ, eventTime, requestingElement, processingElement, requestType);
	}

	public LSQEntry getLsqEntry() {
		return lsqEntry;
	}

	public void setLsqEntry(LSQEntry lsqEntry) {
		this.lsqEntry = lsqEntry;
	}
}
