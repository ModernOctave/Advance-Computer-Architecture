package generic;

import java.util.Comparator;

/**
 *events firstly sorted in increasing order of event time
 *secondly, by event type
 *		- denoted by priority : higher the priority, earlier it is scheduled
 *thirdly, by tie-breaker
 *      - in some cases, a relative ordering, between events of the same type,
 *        that are scheduled at the same time, is desired.
 *      - this is enforced by having a third parameter for sorting, i.e, tie-breaker.
 *      - smaller the tie-breaker, earlier it is scheduled
 */

public class EventComparator implements Comparator<Event> 
{
	public int compare(Event newEvent0, Event newEvent1)
	{
		if(newEvent0.getEventTime() < newEvent1.getEventTime())
		{
			return -1;
		}

		else if(newEvent0.getEventTime() > newEvent1.getEventTime())
		{
			return 1;
		}
		
		else
		{
			if(newEvent0.getPriority() > newEvent1.getPriority())
			{
				return -1;
			}
			else if(newEvent0.getPriority() < newEvent1.getPriority())
			{
				return 1;
			}
			else
			{
//				if(newEvent0.getTieBreaker() < newEvent1.getTieBreaker())
//				{
//					return -1;
//				}
//				else if(newEvent0.getTieBreaker() > newEvent1.getTieBreaker())
//				{
//					return 1;
//				}
//				else
//				{
					return 0;
//				}
			}
		}
	}
}