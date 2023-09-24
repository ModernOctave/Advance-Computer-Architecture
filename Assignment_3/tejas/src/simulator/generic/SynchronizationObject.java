package generic;

public class SynchronizationObject {
	
	int whoIsSleeping;		//0 - no one is sleeping
							//1 - consumer is sleeping
							//2 - producer is sleeping

	public SynchronizationObject()
	{
		whoIsSleeping = 0;
	}

	public int getWhoIsSleeping() {
		return whoIsSleeping;
	}

	public void setWhoIsSleeping(int whoIsSleeping) {
		this.whoIsSleeping = whoIsSleeping;
	}

}
