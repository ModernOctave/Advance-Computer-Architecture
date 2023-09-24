package generic;

import main.Main;
import memorysystem.MemorySystem;

/*
 * Event class contains the bare-minimum that every event must contain.
 * This class must be extended for every RequestType.
 * 
 * The extendedClass would define the requestType and would contain the payLoad 
 * of the request too.This simplifies the code as we now don't have to create a 
 * separate pay-load class for each type of request. 
 */
public abstract class Event implements Cloneable
{
	protected long eventTime;
	protected EventQueue eventQ;
	protected RequestType requestType;
	private long priority;
	public int coreId;
	public long serializationID = 0;
	public static long globalSerializationID = 0;
	public RequestType payloadRequestType;	
	public SimulationElement payloadElement;
	
	public void incrementSerializationID() {
		globalSerializationID++;
		serializationID = globalSerializationID;
	}

	public Event clone()
	{
		incrementSerializationID();
		
		try {
			return (Event) (super.clone());
		} catch (CloneNotSupportedException e) {
			misc.Error.showErrorAndExit("Error in cloning event object");
			return null;
		}
	}
	
	//Element which processes the event.
	protected SimulationElement requestingElement;
	protected SimulationElement processingElement;
	protected SimulationElement actualRequestingElement;
	protected SimulationElement actualProcessingElement;
	
	public Event(EventQueue eventQ, SimulationElement requestingElement,
			SimulationElement processingElement, RequestType requestType) 
	{
		incrementSerializationID();
		eventTime = -1; // this should be set by the port
		this.eventQ = eventQ;
		this.requestingElement = requestingElement;
		this.processingElement = processingElement;
		this.requestType = requestType;
		this.coreId = 0;	//FIXME!!
		
		this.priority = requestType.ordinal();
	}

	public Event(EventQueue eventQ, long eventTime, SimulationElement requestingElement,
			SimulationElement processingElement, RequestType requestType, int coreId) 
	{
		incrementSerializationID();
		this.eventTime = eventTime; // this should be set by the port
		this.eventQ = eventQ;
		this.requestingElement = requestingElement;
		this.processingElement = processingElement;
		this.requestType = requestType;
		this.coreId = coreId;
		this.priority = requestType.ordinal();
	}

	public Event update(EventQueue eventQ, long eventTime, SimulationElement requestingElement,
			SimulationElement processingElement, RequestType requestType)
	{
		incrementSerializationID();
		this.eventTime =  eventTime;
		this.eventQ = eventQ;
		this.requestingElement = requestingElement;
		this.processingElement = processingElement;
		this.requestType = requestType;
		
		//this.priority = calculatePriority(requestType);
		this.priority = requestType.ordinal();
		return this;
	}
	public Event update(long eventTime)
	{
		incrementSerializationID();
		this.eventTime =  eventTime;
		return this;
	}
	
	public Event update(SimulationElement requestingElement,
			SimulationElement processingElement, SimulationElement actualRequestingElement,
			SimulationElement actualProcessingElement)
	{
		incrementSerializationID();
		this.requestingElement = requestingElement;
		this.processingElement = processingElement;
		this.actualProcessingElement = actualProcessingElement;
		this.actualRequestingElement = actualRequestingElement;
		return this;
	}
	public Event update(SimulationElement requestingElement,
			SimulationElement processingElement)
	{
		incrementSerializationID();
		this.requestingElement = requestingElement;
		this.processingElement = processingElement;
		return this;
	}
	//Converts request-type to priority.
	private long calculatePriority(RequestType requestType) 
	{
		//TODO: switch case for different request types would come here.
		return 0;
	}

	public long getEventTime() {
		return eventTime;
	}

	public long getPriority() {
		return priority;
	}

	public SimulationElement getRequestingElement() {
		return requestingElement;
	}
	
	public SimulationElement getActualRequestingElement() {
		return actualRequestingElement;
	}
	
	public void setRequestingElement(SimulationElement requestingElement) {
		this.requestingElement = requestingElement;
	}

	public SimulationElement getProcessingElement() {
		return processingElement;
	}
	
	public SimulationElement getActualProcessingElement() {
		return actualProcessingElement;
	}
	public void setProcessingElement(SimulationElement processingElement) {
		this.processingElement = processingElement;
	}

	
	public void setEventTime(long eventTime) {
		this.eventTime = eventTime;
	}
	
	public void addEventTime(long additionTime) {
		this.setEventTime(this.eventTime + additionTime);
	}

	public void setPriority(long priority) {
		this.priority = priority;
	}
	
	public EventQueue getEventQ() {
		return eventQ;
	}

	public void resetPriority(RequestType requestType)
	{
		this.priority = requestType.ordinal();
	}
	
	public RequestType getRequestType()
	{
		return requestType;
	}
	
	public void setRequestType(RequestType requestType)
	{
		this.requestType = requestType;
	}

	//If the event cannot be handled in the current clock-cycle,
	//then the eventPriority and eventTime will be changed and then 
	//the modified event will be added to the eventQueue which is 
	//now passed as a paramter to this function.
	//TODO handleEvent(event)
	public void handleEvent(EventQueue eventQ)
	{
			processingElement.handleEvent(eventQ, this);
	}
	
	public void dump()
	{
		System.out.println(coreId + " : " + requestType + " : " + requestingElement + " : " + processingElement + " : " + eventTime);
		//added by harveenk
		//Main.debugPrinter.print("CoreId: " + coreId + " RequestType : " + requestType + " RequestingElement : " + requestingElement + " ProcessingElement : " + processingElement + " EventTime : " + eventTime + "\n" );
	}
	
	//added by harveenk
	public void print()
	{
		System.out.println(coreId + " : " + requestType + " : " + requestingElement + " : " + processingElement + " : " + eventTime);
	}

	public void setPayloadElement(SimulationElement element) {
		this.payloadElement = element;
	}
	
	public void setPayloadRequestType(RequestType reqType) {
		this.payloadRequestType = reqType;
	}
	
	public SimulationElement getPayloadElement() {
		return payloadElement;
	}
	
	public RequestType getPayloadRequestType() {
		return payloadRequestType;
	}
}
