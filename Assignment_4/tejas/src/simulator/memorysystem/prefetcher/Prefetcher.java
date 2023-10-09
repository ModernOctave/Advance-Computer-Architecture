package memorysystem.prefetcher;

import memorysystem.Cache;

public abstract class Prefetcher {
	
	Cache associatedCache;
	int numberOfAccesses;
	int numberOfPrefetchesIssued;
	int numberOfUsefulPrefetches;
	
	public abstract void handleMissAtAddress(long addr);

}
