package emulatorinterface;

public class ThreadBlockState {
	public enum blockState{LIVE, BLOCK, INACTIVE};
	blockState BlockState;
	int encode;
	public ThreadBlockState() {
		// TODO Auto-generated constructor stub
		this.BlockState=blockState.INACTIVE;
		encode=-1;
	}
	blockState getState()
	{
		return BlockState;
	}
	/**
	 * 
	 * @param encode
	 * LOCK	14,15
	 * JOIN	18,19
	 * CONDWAIT	20,21
	 * BARRIERWAIT	22,23
	 */
	public void gotBlockingPacket(int encode)
	{
		switch(BlockState)
		{
			case LIVE: this.encode=encode; BlockState=blockState.BLOCK;break;
			case BLOCK: this.encode=encode;break;
			case INACTIVE: this.encode=encode; BlockState=blockState.BLOCK;break;
		}
	}
	/**
	 * Thread started receiving packets after blockage
	 */
	public void gotUnBlockingPacket()
	{
		switch(BlockState)
		{
			case LIVE: break;
			case BLOCK: this.encode=-1;BlockState=blockState.LIVE;break;
			case INACTIVE: this.encode=-1;BlockState=blockState.LIVE;break;
		}
	}
	
	public void gotLive()
	{
		if(BlockState==blockState.INACTIVE)
		{
			BlockState=blockState.LIVE;
		}
	}

}
