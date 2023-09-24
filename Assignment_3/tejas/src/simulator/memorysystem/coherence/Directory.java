package memorysystem.coherence;

import generic.Event;
import generic.EventQueue;
import generic.GlobalClock;
import generic.RequestType;
import generic.Statistics;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import main.ArchitecturalComponent;
import memorysystem.AddressCarryingEvent;
import memorysystem.Cache;
import memorysystem.CacheLine;
import memorysystem.CoreMemorySystem;
import memorysystem.MESI;
import memorysystem.MemorySystem;
import misc.Util;
import config.CacheConfig;
import config.EnergyConfig;
import config.SystemConfig;

// Unlock function should call the state change function. This is called using the current event field inside the directory entry.
// For write hit event, there is some mismatch

public class Directory extends Cache implements Coherence {
	
	long readMissAccesses = 0;
	long writeHitAccesses = 0;
	long writeMissAccesses = 0;
	long evictedFromCoherentCacheAccesses = 0;
	long evictedFromSharedCacheAccesses = 0;

	public Directory(String cacheName, int id, CacheConfig cacheParameters,
			CoreMemorySystem containingMemSys) {		
		super(cacheName, id, cacheParameters, containingMemSys);		
		MemorySystem.coherenceNameMappings.put(cacheName, this);
	}

	public void writeHit(long addr, Cache c) {
//		XXX if(ArchitecturalComponent.getCore(0).getNoOfInstructionsExecuted()>3000000l) {
//			System.out.println("Directory WriteHit t : " + GlobalClock.getCurrentTime() + " addr : " + addr + " cache : " + c);
//		}
		writeHitAccesses++;
		sendAnEventFromCacheToDirectory(addr, c, RequestType.DirectoryWriteHit);
	}

	public void readMiss(long addr, Cache c) {
//		XXX if(ArchitecturalComponent.getCore(0).getNoOfInstructionsExecuted()>3000000l) {
//			System.out.println("Directory ReadMiss t : " + GlobalClock.getCurrentTime() + " addr : " + addr + " cache : " + c);
//		}
		readMissAccesses++;
		sendAnEventFromCacheToDirectory(addr, c, RequestType.DirectoryReadMiss);
	}

	public void writeMiss(long addr, Cache c) {
//		XXX if(ArchitecturalComponent.getCore(0).getNoOfInstructionsExecuted()>3000000l) {
//			System.out.println("Directory WriteMiss t : " + GlobalClock.getCurrentTime() + " addr : " + addr + " cache : " + c);
//		}
		writeMissAccesses++;
		sendAnEventFromCacheToDirectory(addr, c, RequestType.DirectoryWriteMiss);
	}

	private AddressCarryingEvent sendAnEventFromCacheToDirectory(long addr, Cache c, RequestType request) {
		
		incrementHitMissInformation(addr);
		
		// Create an event
		Directory directory = this;
		AddressCarryingEvent event = new AddressCarryingEvent(
				c.getEventQueue(), 0, c, directory, request, addr);

		// 2. Send event to directory
		c.sendEvent(event);
		
		return event;
	}
	
	private void incrementHitMissInformation(long addr) {
		CacheLine dirEntry = access(addr);
		
		if(dirEntry==null || dirEntry.getState()==MESI.INVALID) {
			misses++;
		} else {
			hits++;
		}		
	}

	public void handleWriteHit(long addr, Cache c, AddressCarryingEvent event) {
		CacheLine dirEntry = access(addr);

		switch (dirEntry.getState()) {
			case MODIFIED:
			case EXCLUSIVE:
			case SHARED: {
				
				if(dirEntry.isSharer(c)==false) {
					// Valid case : c1 and c2 are sharers address x
					// Both encountered a write at the same time
					noteInvalidState("WriteHit expects cache to be a sharer. Cache : " + c + ". Addr : " + addr);
				}
				
				for(Cache sharerCache : dirEntry.getSharers()) {
					if(sharerCache!=c) {
						sendAnEventFromMeToCache(addr, sharerCache, RequestType.EvictCacheLine);
					}
				}
				
				dirEntry.clearAllSharers();
				dirEntry.addSharer(c);
				dirEntry.setState(MESI.MODIFIED);
				
				break;
			}
	
			case INVALID: {
				noteInvalidState("WriteHit expects entry to be in a valid state. Cache : " + c + ". Addr : " + addr);
				dirEntry.clearAllSharers();
				dirEntry.setState(MESI.MODIFIED);
				dirEntry.addSharer(c);				
				break;
			}
		}
		
		sendAnEventFromMeToCache(addr, c, RequestType.AckDirectoryWriteHit);
	}
	
	private void forceInvalidate(CacheLine dirEntry) {
		misc.Error.showErrorAndExit("Force Invalidate !!");
		// The directory is in an inconsistent state. 
		// Force a consistent change by evicting the dirEntry.
		for (Cache sharerCache : dirEntry.getSharers()) {
			sharerCache.updateStateOfCacheLine(dirEntry.getAddress(), MESI.INVALID);
		}
		
		dirEntry.clearAllSharers();
		dirEntry.setState(MESI.INVALID);		
	}

	public AddressCarryingEvent evictedFromSharedCache(long addr, Cache c) {
//		XXX if(ArchitecturalComponent.getCore(0).getNoOfInstructionsExecuted()>3000000l) {
//			System.out.println("Directory EvictShared t : " + GlobalClock.getCurrentTime() + " addr : " + addr + " cache : " + c);
//		}
		evictedFromSharedCacheAccesses++;
		return sendAnEventFromCacheToDirectory(addr, c, RequestType.DirectoryEvictedFromSharedCache);
	}
	
	public AddressCarryingEvent evictedFromCoherentCache(long addr, Cache c) {
//		XXX if(ArchitecturalComponent.getCore(0).getNoOfInstructionsExecuted()>3000000l) {
//			System.out.println("Directory EvictCoherent t : " + GlobalClock.getCurrentTime() + " addr : " + addr + " cache : " + c);
//		}
		evictedFromCoherentCacheAccesses++;
		return sendAnEventFromCacheToDirectory(addr, c, RequestType.DirectoryEvictedFromCoherentCache);
	}
	
	public void handleEvent(EventQueue eventQ, Event e) {
		AddressCarryingEvent event = (AddressCarryingEvent) e;
		long addr = event.getAddress();
		long lineAddr = event.getAddress()>>blockSizeBits;
		RequestType reqType = e.getRequestType();

//		if(ArchitecturalComponent.getCores()[0].getNoOfInstructionsExecuted() > 4000000) {
//			System.out.println("\n\nDirectory handleEvent currEvent : " + event);
//			toStringPendingEvents();
//		}
		
		if(access(addr)==null && (reqType==RequestType.DirectoryWriteHit || reqType==RequestType.DirectoryWriteMiss ||
			reqType==RequestType.DirectoryReadMiss || reqType==RequestType.DirectoryEvictedFromCoherentCache)) {
			
			// This events expect a directory entry to be present.
			// Create a directory entry.
			CacheLine evictedEntry = fill(addr, MESI.INVALID);
			
			if(evictedEntry!=null && evictedEntry.isValid()) {
//				System.out.println("Evicted line : " + (evictedEntry.getAddress()>>blockSizeBits) + "\n" + evictedEntry);
				invalidateDirectoryEntry(evictedEntry);
			}
		}
		
		Cache senderCache = (Cache) event.getRequestingElement();

		switch (event.getRequestType()) {
			case DirectoryWriteHit: {
				handleWriteHit(addr, senderCache, event);
				break;
			}
			
			case DirectoryReadMiss: {
				handleReadMiss(addr, senderCache);
				break;
			}
			
			case DirectoryWriteMiss: {
				handleWriteMiss(addr, senderCache);
				break;
			}
			
			case DirectoryEvictedFromSharedCache: {
				handleEvictFromSharedCache(addr);
				break;
			}
			
			case DirectoryEvictedFromCoherentCache: {
				handleEvictedFromCoherentCache(addr, senderCache);
				break;
			}
		}
	}
	
	private void handleEvictedFromCoherentCache(long addr, Cache c) {
		CacheLine dirEntry = access(addr);
		
		if(dirEntry.isSharer(c)) {
			dirEntry.removeSharer(c);
			if(dirEntry.getSharers().size()==0) {
				dirEntry.setState(MESI.INVALID);
			} else if(dirEntry.getSharers().size()==1) {
				dirEntry.setState(MESI.EXCLUSIVE);
				sendAnEventFromMeToCache(addr, dirEntry.getOwner(), RequestType.DirectorySharedToExclusive);
			}
		} else {
			// Cache c1 holds an address x
			// directory and c1 evict line for x in the same cycle
			// When c1's invalidate message reaches directory, it is not a sharer
			noteInvalidState("Eviction from a non-sharer. Cache : " + c + ". Addr : " + addr);
		}
		
		sendAnEventFromMeToCache(addr, c, RequestType.AckEvictCacheLine);
	}

	private void handleWriteMiss(long addr, Cache c) {
		CacheLine dirEntry = access(addr);
		
		handleReadMiss(addr, c);
		for(Cache sharerCache : dirEntry.getSharers()) {
			if(sharerCache!=c) {
				sendAnEventFromMeToCache(addr, sharerCache, RequestType.EvictCacheLine);
			}
		}
		
		dirEntry.clearAllSharers();
		dirEntry.addSharer(c);
		dirEntry.setState(MESI.MODIFIED);
	}

	private void handleEvictFromSharedCache(long addr) {
		CacheLine cl = access(addr);
		
		if(cl==null || cl.isValid()==false) {
			return;
		} else {
			invalidateDirectoryEntry(cl);			
		}
	}

	private void invalidateDirectoryEntry(CacheLine cl) {
		long addr = cl.getAddress();
		for(Cache c : cl.getSharers()) {
			sendAnEventFromMeToCache(addr, c, RequestType.EvictCacheLine);
		}
		
		cl.clearAllSharers();
		cl.setState(MESI.INVALID);		
	}

	private void handleReadMiss(long addr, Cache c) {
		CacheLine dirEntry = access(addr);
		
		switch(dirEntry.getState()) {
			case MODIFIED: 
			case EXCLUSIVE: 
			case SHARED : {
				
				if(dirEntry.isSharer(c)==true) {
					// Cache c1 and c2 are sharers of address x
					// Both perform a write at the same time. Hence, both send a writeHit to the directory at the same time
					// Assume c2's writeHit reaches directory first. It sends invalidate to c1. This invalidate is queued behind the write entry in c1's mshr entry for addr
					// c1's writeHit reaches directory. The directory re-configured the owner to c1
					// c1 processes invalidate from c2
					// now, there is a read at c1. The c1 sends readMiss to directory. However, it is a sharer.
					noteInvalidState("Miss from a sharer. Cache : " + c + ". Addr : " + addr);
					sendAnEventFromMeToCache(addr, c, RequestType.Mem_Response);
				} else {
					Cache sharerCache = dirEntry.getFirstSharer();
					sendCachelineForwardRequest(sharerCache, c, addr);
				}
				
				dirEntry.setState(MESI.SHARED);
				dirEntry.addSharer(c);
				
				break;
			}
			
			
			case INVALID: {
				dirEntry.setState(MESI.EXCLUSIVE);
				dirEntry.clearAllSharers();
				dirEntry.addSharer(c);
				// If the line is supposed to be fetched from the next level cache, 
				// we will just send a cacheRead request to this cache
				// Note that the directory is not coming into the picture. This is just a minor hack to maintain readability of the code
				c.sendRequestToNextLevel(addr, RequestType.Cache_Read);
				break;
			}
		}
	}
	
	private void sendCachelineForwardRequest(Cache ownerCache, Cache destinationCache, long addr) {
		EventQueue eventQueue = ownerCache.getEventQueue();
		
		AddressCarryingEvent event = new AddressCarryingEvent(eventQueue, 0, 
			this, ownerCache, 
			RequestType.DirectoryCachelineForwardRequest, addr);
		
		event.payloadElement = destinationCache;
		
		this.sendEvent(event);
	}

	public void printStatistics(FileWriter outputFileWriter) throws IOException {
		outputFileWriter.write("\n");
		outputFileWriter.write("Directory Access due to ReadMiss\t=\t" + readMissAccesses + "\n");
		outputFileWriter.write("Directory Access due to WriteMiss\t=\t" + writeMissAccesses + "\n");
		outputFileWriter.write("Directory Access due to WriteHit\t=\t" + writeHitAccesses + "\n");
		outputFileWriter.write("Directory Access due to EvictionFromCoherentCache\t=\t" + evictedFromCoherentCacheAccesses + "\n");
		outputFileWriter.write("Directory Access due to EvictionFromSharedCache\t=\t" + evictedFromSharedCacheAccesses + "\n");
		
		outputFileWriter.write("Directory Hits\t=\t" + hits + "\n");
		outputFileWriter.write("Directory Misses\t=\t" + misses + "\n");
		if ((hits+misses) != 0) {
			outputFileWriter.write("Directory Hit-Rate\t=\t" + Statistics.formatDouble((double)(hits)/(hits+misses)) + "\n");
			outputFileWriter.write("Directory Miss-Rate\t=\t" + Statistics.formatDouble((double)(misses)/(hits+misses)) + "\n");
		}
	}
	
	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		long numAccesses = readMissAccesses + writeHitAccesses + writeMissAccesses 
				+ evictedFromCoherentCacheAccesses + evictedFromSharedCacheAccesses;
		EnergyConfig newPower = new EnergyConfig(cacheConfig.power.leakageEnergy,
				cacheConfig.power.readDynamicEnergy);
		EnergyConfig power = new EnergyConfig(newPower, numAccesses);
		power.printEnergyStats(outputFileWriter, componentName);
		return power;
	}	
}
