package memorysystem.prefetcher;

import java.util.HashMap;

import generic.GlobalClock;
import generic.RequestType;
import memorysystem.Cache;

/* based on the following papers:
 * "Feedback Directed Prefetching: Improving the Performance and Bandwidth-Efficiency of Hardware Prefetchers", Srinath et al., 2006
 * "Power4 System Microarchitecture", Tendler et al., 2001
 */

public class Power4Prefetcher extends Prefetcher {

	int numberOfStreams;
	int trainingWindow;
	int prefetchDistance;
	int prefetchDegree;
	int initialStartupDistance;
	TrackingEntry[] trackingEntries;
	
	public Power4Prefetcher(Cache associatedCache) {
		numberOfStreams = 64; // [Srinath2006]
		trainingWindow = 16; //[Srinath2006]
		prefetchDistance = 16; // middle-of-the-road prefetcher [Srinath2006]
		prefetchDegree = 2; // middle-of-the-road prefetcher [Srinath2006]
		initialStartupDistance = 2;
		
		trackingEntries = new TrackingEntry[numberOfStreams];
		for(int i = 0; i < numberOfStreams; i++)
		{
			trackingEntries[i] = new TrackingEntry(-1, -1);
		}
		this.associatedCache = associatedCache;
	}
	
	@Override
	public void handleMissAtAddress(long addr) {
		long blockAddr = associatedCache.getLineAddress(addr);
		numberOfAccesses++;
		
		//can it be used for training?
		for(int i = 0; i < numberOfStreams; i++)
		{
			TrackingEntry trackingEntry = trackingEntries[i];
			if((trackingEntry.direction >= -1 && trackingEntry.direction <= 1)
					&& (Math.abs(trackingEntry.beginningPointer - blockAddr) <= trainingWindow)
					&& trackingEntry.beginningPointer != -1)
			{
				if(trackingEntry.beginningPointer < blockAddr)
					trackingEntry.direction++;
				else if(trackingEntry.beginningPointer > blockAddr)
					trackingEntry.direction--;
				
				if(trackingEntry.direction == 2 || trackingEntry.direction == -2)
				{//training done
					if(trackingEntry.beginningPointer < blockAddr)
					{
						trackingEntry.endingPointer = blockAddr + initialStartupDistance;
					}
					else
					{
						long temp = trackingEntry.beginningPointer;
						trackingEntry.beginningPointer = trackingEntry.endingPointer - initialStartupDistance;
						trackingEntry.endingPointer = temp;
					}
				}
				trackingEntry.timeLastAccessed = GlobalClock.getCurrentTime();
				return;
			}
		}
		
		//can it be used for prefetching?
		for(int i = 0; i < numberOfStreams; i++)
		{
			TrackingEntry trackingEntry = trackingEntries[i];
			if(trackingEntry.beginningPointer >= 0)
			{
				if((trackingEntry.direction == -2 || trackingEntry.direction == 2)
						&& (trackingEntry.beginningPointer <= blockAddr	&& blockAddr <= trackingEntry.endingPointer))
				{
					if(trackingEntry.direction == 2)
					{
						for(int j = 0; j < prefetchDegree; j++)
						{
							issuePrefetchRequest((trackingEntry.endingPointer+j)<<associatedCache.blockSizeBits);
						}
						if(trackingEntry.endingPointer - trackingEntry.beginningPointer < prefetchDistance - prefetchDegree)
						{
							trackingEntry.endingPointer += prefetchDegree;
						}
						else
						{
							trackingEntry.beginningPointer += prefetchDegree;
							trackingEntry.endingPointer = trackingEntry.beginningPointer + prefetchDistance;
						}
					}
					else
					{
						for(int j = 0; j < prefetchDegree; j++)
						{
							issuePrefetchRequest((trackingEntry.beginningPointer-j)<<associatedCache.blockSizeBits);
						}
						if(trackingEntry.endingPointer - trackingEntry.beginningPointer < prefetchDistance - prefetchDegree)
						{
							trackingEntry.beginningPointer -= prefetchDegree;
						}
						else
						{
							trackingEntry.endingPointer -= prefetchDegree;
							trackingEntry.beginningPointer = trackingEntry.endingPointer - prefetchDistance;
						}
					}
					trackingEntry.timeLastAccessed = GlobalClock.getCurrentTime();
					return;
				}
			}
		}
		
		//need to make a tracking entry for this new stream
		TrackingEntry candidateEntry = null;
		for(int i = 0; i < numberOfStreams; i++)
		{
			if(trackingEntries[i].beginningPointer == -1)
			{
				candidateEntry = trackingEntries[i];
				break;
			}
		}
		if(candidateEntry == null)
		{
			long leastRecentAccessTime = Long.MAX_VALUE;
			for(int i = 0; i < numberOfStreams; i++)
			{
				if(trackingEntries[i].timeLastAccessed < leastRecentAccessTime)
				{
					candidateEntry = trackingEntries[i];
					leastRecentAccessTime = trackingEntries[i].timeLastAccessed;
				}
			}
		}
		candidateEntry.beginningPointer = blockAddr;
		candidateEntry.endingPointer = -1;
		candidateEntry.direction = 0;
		candidateEntry.timeLastAccessed = GlobalClock.getCurrentTime();
	}
	
	private void issuePrefetchRequest(long addr) //NOTE: not blockAddr
	{
		if(associatedCache.accessValid(addr) == null)
		{
			associatedCache.sendRequestToNextLevel(addr, RequestType.Cache_Read);
			numberOfPrefetchesIssued++;
		}
	}
}

class TrackingEntry {
	long beginningPointer;
	long endingPointer;
	int direction; // -2 refers to descending, 2 refers to ascending
	long timeLastAccessed;
	
	TrackingEntry(long beginningPointer, long endingPointer) {
		direction = 0;
		this.beginningPointer = beginningPointer;
		this.endingPointer = endingPointer;
		timeLastAccessed = -1;
	}
}
