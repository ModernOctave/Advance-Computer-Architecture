package emulatorinterface;

import java.util.ArrayList;

import emulatorinterface.communication.Packet;

public class EmulatorPacketList {
	
	ArrayList<Packet> packetList;
	int size = 0;
	final int listSize = 5; // load + store + branch + assembly + control-flow(thread)

	public EmulatorPacketList() {
		super();
		this.packetList = new ArrayList<Packet>();
		
		for(int i=0; i<listSize; i++) {
			packetList.add(new Packet());
		}
	}
	
	public void add(Packet p) {
		if(size==packetList.size()) {
		//	System.out.println("IP = " + p.ip + " Type = " +p.value);
//			misc.Error.showErrorAndExit("packetList : trying to push more packets for fuse function" +
//				"current size = " + size);
			//System.out.println("packetList : trying to push more packets for fuse function" +
		//		"current size = " + size);
			return;
		}
		this.packetList.get(size).set(p);
		size++;
	}
	
	public void clear() {
		size = 0;
	}
	
	public Packet get(int index) {
		if(index>size) {
			misc.Error.showErrorAndExit("trying to access element outside packetList size" + 
				"size = " + size + "\tindex = " + index);
		}

		return packetList.get(index);
	}
	
	public int size() {
		return size;
	}
}
