package emulatorinterface.communication;

public class CustomAsmCharPool {
	byte pool[][][];
	int head[];
	int tail[];
	final int bufferSize = 2*1024;
	
	public CustomAsmCharPool(int maxApplicationThreads)
	{
		pool = new byte[maxApplicationThreads][bufferSize][64];
		head = new int[maxApplicationThreads];
		tail = new int[maxApplicationThreads];
		for(int tidApp=0; tidApp<maxApplicationThreads; tidApp++) {
			head[tidApp] = tail[tidApp] = -1;
		}
	}
	
	public void enqueue(int tidApp, byte inputBytes[], int offset)
	{
		// System.out.println("enqueue : " + new String(inputBytes, offset, 64));
		
		if(isFull(tidApp)) {
			misc.Error.showErrorAndExit("unable to handle new asm bytes");
		}
		
		tail[tidApp] = (tail[tidApp]+1)%bufferSize;
		//pool[tidApp][tail[tidApp]] = newBytes
		for(int i=0; i<64; i++) {
			if( (offset+i) < inputBytes.length) {
				pool[tidApp][tail[tidApp]][i] = inputBytes[offset+i];	
			} else {//asm packets for filePacket interface may have less than 64 characters
				pool[tidApp][tail[tidApp]][i] = (byte)0;
				break;
			}
		}
		
		if(head[tidApp]==-1) {
			head[tidApp] = 0;
		}
	}
	
	public byte[] dequeue(int tidApp)
	{
		if(isEmpty(tidApp)) {
			misc.Error.showErrorAndExit("pool underflow !!");
		}
		
		byte[] toBeReturned = pool[tidApp][head[tidApp]]; 
		
		if(head[tidApp] == tail[tidApp]) {
			head[tidApp] = -1;
			tail[tidApp] = -1;
		} else {
			head[tidApp] = (head[tidApp] + 1)%bufferSize;
		}
		
		// System.out.println("dequeue : " + new String(toBeReturned, 0, 64));
		return toBeReturned;
	}
	
	private boolean isEmpty(int tidApp) {
		if(head[tidApp]==-1) {
			return true;
		} else {
			return false;
		}
	}
	
	public int size(int tidApp)
	{
		if(head[tidApp] == -1)
		{
			return 0;
		}
		if(head[tidApp] <= tail[tidApp])
		{
			return (tail[tidApp] - head[tidApp] + 1);
		}
		else
		{
			return (bufferSize - head[tidApp] + tail[tidApp] + 1);
		}
	}
	
	public void clear(int tidApp) {
		head[tidApp]=tail[tidApp]=-1;
	}

	//position refers to logical position in queue - NOT array index
	public byte[] peek(int tidApp, int position)
	{
		if(size(tidApp) <= position)
		{
			misc.Error.showErrorAndExit("pool underflow");
		}
		
		int peekIndex = (head[tidApp] + position)%bufferSize;
		return pool[tidApp][peekIndex];
	}
		
	public byte[] front(int tidApp)
	{
		return peek(tidApp, 0);
	}
	
	public boolean isFull(int tidApp)
	{
		if((tail[tidApp] + 1)%bufferSize == head[tidApp])
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public int currentPosition(int tidApp) {
		if(isEmpty(tidApp)) {
			return 0;
		} else if (head[tidApp]<=tail[tidApp]) {
			return (tail[tidApp]-head[tidApp]+1);
		} else {
			return (bufferSize-(head[tidApp]-tail[tidApp]-1));
		}
	}
}
