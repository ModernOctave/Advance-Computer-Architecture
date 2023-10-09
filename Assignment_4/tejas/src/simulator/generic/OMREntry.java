package generic;

import java.util.ArrayList;
import memorysystem.AddressCarryingEvent;

public class OMREntry {
	public ArrayList<AddressCarryingEvent> outStandingEvents;
	
	public OMREntry(ArrayList<AddressCarryingEvent> outStandingEvent)
	{
		this.outStandingEvents = outStandingEvent;
	}
}
