package generic;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import main.ArchitecturalComponent;
import main.Main;
import memorysystem.AddressCarryingEvent;


public class EventQueue 
{
	public static int queueSize = 1024;
	LinkedList<Event> myPriorityQueue[];
	int head = 0;
	
	public EventQueue() 
	{
		myPriorityQueue = (LinkedList<Event>[]) Array.newInstance(LinkedList.class, queueSize);
		for(int i=0; i<queueSize; i++) {
			myPriorityQueue[i] = new LinkedList<Event>(); 
		}
	}
	
	int getIndex(long index) {
		return (int) ((head+index)%queueSize);
	}
	
	public void addEvent(Event event)
	{
		long currentClockTime = GlobalClock.currentTime;
		long eventTime = event.getEventTime();
		if(eventTime<currentClockTime) {
			myPriorityQueue[getIndex(0)].add(event);
		} else {
			long diffTime = (eventTime - currentClockTime);
			myPriorityQueue[getIndex(diffTime)].add(event);
		}
	}
	
	public void processEvents()
	{
		LinkedList<Event> eventList = myPriorityQueue[head];
		
		//Main.debugPrinter.print("Processing events at time " + GlobalClock.getCurrentTime() + "\n");
		//System.out.println("Processing events at time " + GlobalClock.getCurrentTime() + "\n");

		while(eventList.isEmpty()==false) {
			Event e = eventList.pollFirst();
			
			e.getProcessingElement().handleEvent(this, e);
			
		}
	
		head = (head + 1) % queueSize;
		
	}
}
