package pipeline.branchpredictor;

public class BTB {
	
	int type;
	int PCBits;
	int GHRBits;
	
	int GHR;
    long BTB[];

	int PCMask;
	int takenMask;
    
    public BTB(int PCBits, int GHRBits)
    {
    	/*
    	 * Intel Sandybridge is supposed to have a single level 4096 entry BTB
    	 * Not much is known about subsequent microarchitectures
    	 * Since Sandybridge is at 32 nm, and Skylake is at 14 nm, we can guess a 16k entry BTB
    	 */
    	
    	type = 2;
    	
    	if(type == 1)
    	{
    		this.PCBits = 16;
    		PCMask = (1 << this.PCBits) - 1;
        	BTB = new long[(1 << this.PCBits)];
    	}
    	else if(type == 2)
    	{
    		this.PCBits = 16;
    		PCMask = (1 << this.PCBits) - 1;
    		BTB = new long[(1 << this.PCBits)];
    		takenMask = (1 << (this.PCBits-1));
    	}
    	else if(type == 3)
    	{
    		this.PCBits = 10;
    		this.GHRBits = 6;
    		PCMask = (1 << this.PCBits) - 1;
    		BTB = new long[(1 << (this.PCBits+this.GHRBits))];
    		takenMask = (1 << (this.GHRBits-1));
    	}
    }

    public void GHRTrain(boolean taken)
    {
    	GHR = GHR >> 1;
        if(taken == true)
        	GHR = GHR | takenMask;
    }
    
    public void BTBTrain(long programCounter, long target)
    {
    	BTB[getIndex(programCounter)] = target;
    }
    
    public long BTBPredict(long programCounter)
    {
    	return BTB[getIndex(programCounter)];
    }
    
    private int getIndex(long programCounter)
    {
    	int index = -1;
    	if(type == 1)
    	{
    		index = (int)((programCounter & (long)PCMask));
    	}
    	else if(type == 2)
    	{
	    	index = GHR ^ (int)((programCounter & (long)PCMask));
    	}
    	else if(type == 3)
    	{
    		index = (GHR << PCBits) | (int)((programCounter & (long)PCMask));
    	}
    	return index;
    }
}
