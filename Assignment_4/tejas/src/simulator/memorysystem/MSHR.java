package memorysystem;

import generic.RequestType;

import java.util.LinkedList;

public class MSHR {
        private int mshrMaxSize;
        private int mshrSize;
        private LinkedList<LinkedList<AddressCarryingEvent>> missRegister;
        private int blockSizeBits;
        Cache containingCache;
        
        public MSHR(int mshrMaxSize, int blockSizeBits, Cache containingCache) {
                this.mshrMaxSize = mshrMaxSize;
                missRegister = new LinkedList<LinkedList<AddressCarryingEvent>>();
                this.blockSizeBits = blockSizeBits;
                this.containingCache = containingCache;
        }

        public void addToMSHR(AddressCarryingEvent event) {
                long addr = event.getAddress();

                // First try to add it to the linked list of addr
                // If the linked list is not there, create a new MSHR entry
                LinkedList<AddressCarryingEvent> missList = getWaitingEventsInMSHR(addr);
                if (missList != null) {
                        if(event.getRequestType() == RequestType.Cache_Write
                                        && missList.getLast().getRequestType() == RequestType.Cache_Write)
                        {
                        		containingCache.combinedWrites++;
                                return;
                        }
                        missList.add(event);
                } else {
                        createMSHREntry(event);
                }

                mshrSize++;
        }

        public LinkedList<AddressCarryingEvent> removeEventsFromMSHR(long addr) {
                LinkedList<AddressCarryingEvent> missList = getWaitingEventsInMSHR(addr);
                if (missList == null) {
                		return new LinkedList<AddressCarryingEvent>();
                        //misc.Error.showErrorAndExit("No pending event in MSHR for addr : " + addr + ". Cache : " + this);
                }

                missRegister.remove(missList);
                mshrSize-=missList.size();
                return missList;
        }

        public boolean isAddrInMSHR(long addr) {
                if(isMSHREmpty()) {
                        return false;
                } else {
                        return (getWaitingEventsInMSHR(addr) != null);
                }
        }

        private boolean isMSHREmpty() {
                return mshrSize==0;
        }

        public boolean isMSHRFull(long addr) {
        		if(mshrSize >= mshrMaxSize)
        			return true;
        		
    			if(containingCache.numberOfLinesOfSetInMSHR(addr) >= containingCache.assoc - 1)
    				return true;
    			
    			return false;
        }

        public void printMSHR() {
                System.out.println("\nMSHR of " + this + "\n");
                for (LinkedList<AddressCarryingEvent> missList : missRegister) {
                        System.out.println("\nLineAddr : " + getLineAddr(missList.peek().getAddress()));
                        for (AddressCarryingEvent event : missList) {
                                System.out.println(event);
                        }
                }
        }

        public void createMSHREntry(AddressCarryingEvent event) {
                LinkedList<AddressCarryingEvent> missList = new LinkedList<AddressCarryingEvent>();
                missList.add(event);
                missRegister.add(missList);
        }
        
        private long getLineAddr(long addr) {
                return (addr>>blockSizeBits);
        }

        public LinkedList<AddressCarryingEvent> getWaitingEventsInMSHR(long addr) {
                long lineAddr = getLineAddr(addr);
                for (LinkedList<AddressCarryingEvent> missList : missRegister) {
                        if (getLineAddr(missList.peek().getAddress()) == lineAddr) {
                                return missList;
                        }
                }

                return null;
        }
        
        public AddressCarryingEvent getFirstWaitingEventInMSHR(long addr) {
                long lineAddr = getLineAddr(addr);
                for (LinkedList<AddressCarryingEvent> missList : missRegister) {
                        if (getLineAddr(missList.peek().getAddress()) == lineAddr) {
                                return missList.getFirst();
                        }
                }

                misc.Error.showErrorAndExit("No event for addr : " + addr);
                return null;
        }

        public void removeFirstEventFromMSHR(long addr) {
                long lineAddr = getLineAddr(addr);
                for (LinkedList<AddressCarryingEvent> missList : missRegister) {
                        if (getLineAddr(missList.peek().getAddress()) == lineAddr) {
                                missList.remove(0);
                                return;
                        }
                }
                
                misc.Error.showErrorAndExit("No event for addr : " + addr);
        }
        
        
        public long sumEventsPendingInMSHR = 0,  sumNumMSHREntries = 0;
        public long freqEventsPendingInMSHR = 0;
        
        public void noteMSHRStats() {
                sumEventsPendingInMSHR += mshrSize;
                freqEventsPendingInMSHR++;
                sumNumMSHREntries += missRegister.size();
        }
        
        public double getAvgNumEventsPendingInMSHR() {
                return (double)sumEventsPendingInMSHR/(double)freqEventsPendingInMSHR;
        }
        
        public double getAvgNumEventsPendingInMSHREntry() {
                return (double)sumEventsPendingInMSHR/(double)(sumNumMSHREntries);
        }

        public int getNumPendingEventsForAddr(long addr) {
                
                long lineAddr = getLineAddr(addr);
                int numRet = 0;
                for (LinkedList<AddressCarryingEvent> missList : missRegister) {
                        if (getLineAddr(missList.peek().getAddress()) == lineAddr) {
                                for(AddressCarryingEvent e : missList) {
                                        if(e.getRequestType()==RequestType.EvictCacheLine ||
e.getRequestType()==RequestType.DirectoryEvictedFromCoherentCache) {
                                                return numRet;
                                        } else {
                                                numRet++;
                                        }
                                }
                        }
                }
                
                return numRet;
        }
}

