package emulatorinterface.communication;

// This is very hardbound to the jni C file. in the shmread function.Update JNIShm.c if any changes
// are made here
public class Packet 
{
	// If info packetList then ip represents instruction pointer and tgt represents the target addr/mem 
	// address. Else if synchronization packetList then ip represents time and tgt represents lock 
	// address. Else if timer packetList then ip represents time and tgt represents nothing.
	
	// For a qemu packetList containing assembly of instruction, tgt indicates the size of the assembly string
	public long ip;
	public long value;
	public long tgt;
	
	public Packet () 
	{
		ip = -1;
	}

	public Packet(long ip, long value, long tgt) 
	{
		this.ip = ip;
		this.value = value;
		this.tgt = tgt;
	}

	@Override
	public String toString() {
		return "Packet [ip=" + Long.toHexString(ip).toLowerCase() + ", tgt=" + tgt + ", value=" + value + "]";
	}

	public void set(long ip, long value, long tgt) {
		this.ip = ip;
		this.value = value;
		this.tgt = tgt;
	}
	
	public void set(Packet p) {
		this.ip = p.ip;
		this.tgt = p.tgt;
		this.value = p.value;
	}
	
	/*
	public Long getTgt()
	{
		return tgt;
	}
	public Long getIp()
	{
		return ip;
	}*/
	
}