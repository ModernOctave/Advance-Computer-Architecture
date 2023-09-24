package memorysystem;

import main.Main;
import generic.Event;
import generic.EventQueue;
import generic.RequestType;
import generic.SimulationElement;


public class AddressCarryingEvent extends Event implements Cloneable
{
	//changed by harveenk
	//change from private to protected in order to make it inheritable
	protected long address;
	public long event_id;
	public int hopLength;
	public int dn_status=-1; //-1=initial, 1=broadcast, 2=hit, 3=miss
	public Event parentEvent=null;
	public AddressCarryingEvent(EventQueue eventQ, long eventTime,
			SimulationElement requestingElement,
			SimulationElement processingElement,
			RequestType requestType, long address) {
		super(eventQ, eventTime, requestingElement, processingElement,
				requestType, -1);
		this.address = address;
	}
	
	public AddressCarryingEvent()
	{
		super(null, -1, null, null, RequestType.Cache_Read, -1);
		this.address = -1;
	}
	
	public AddressCarryingEvent(long eventId, EventQueue eventQ, long eventTime,
			SimulationElement requestingElement,
			SimulationElement processingElement,
			RequestType requestType, long address,int coreId) {
		super(eventQ, eventTime, requestingElement, processingElement,
				requestType, coreId);
		this.event_id = eventId;
		this.address = address;
	}
	
	public AddressCarryingEvent updateEvent(EventQueue eventQ, long eventTime, 
			SimulationElement requestingElement,
			SimulationElement processingElement,
			RequestType requestType, long address,int coreId) {
		this.address = address;
		this.coreId = coreId;
		return (AddressCarryingEvent)this.update(eventQ, eventTime, requestingElement, processingElement, requestType);
	}
	
	public AddressCarryingEvent updateEvent(EventQueue eventQ, long eventTime, 
			SimulationElement requestingElement,
			SimulationElement processingElement,
			RequestType requestType) {
		return (AddressCarryingEvent) this.update(eventQ, eventTime, requestingElement, processingElement, requestType);
	}
	
	public long getAddress() {
		return address;
	}

	public void setAddress(long address) {
		this.address = address;
	}
	
	
	public void dump()
	{
		System.out.println("CoreId: " + coreId + " RequestType : " + requestType + " RequestingElement : " + requestingElement + " ProcessingElement : " + processingElement + " EventTime : " + eventTime + " Address : " + address + "\n");
	
		//modified by harveenk
		//write to debug file instead
		//Main.debugPrinter.print("CoreId: " + coreId + " RequestType : " + requestType + " RequestingElement : " + requestingElement + " ProcessingElement : " + processingElement + " EventTime : " + eventTime + " Address : " + address + "\n" );
	}
	
	//added by harveenk
	public void print()
	{
		System.out.println("CoreId: " + coreId + " RequestType : " + requestType + " RequestingElement : " + requestingElement + " ProcessingElement : " + processingElement + " EventTime : " + eventTime + " Address : " + address + "\n");
	}
	
	
	public String toString(){
		String s = (coreId + " req : " + requestType + " reqE : " + requestingElement + " proE : " + processingElement + " evT : " + eventTime + " addr : " + address + " # " + serializationID); 
		return s;
	}

}
