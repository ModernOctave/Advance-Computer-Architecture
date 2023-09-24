package generic;

public class CustomInstructionPool {
	
	/*Instruction[] pool;
	public int head;
	public int tail;
	public int poolSize;
	
	public CustomInstructionPool(int poolSize)
	{
		this.poolSize = poolSize;
		pool = new Instruction[poolSize];
		for(int i = 0; i < poolSize; i++)
		{
			pool[i] = new Instruction();
		}
		head = 0;
		tail = poolSize - 1;
	}
	
	public Instruction borrowObject()
	{
//		System.out.println("borrow"+head+" "+tail);
		if(head == -1)
		{
			System.out.println("instruction pool empty!!");
			String cmd[] = {"/bin/sh",
				      "-c",
				      "killall -9 " + Newmain.executableFile};
			try
			{
				Process process = Runtime.getRuntime().exec(cmd);
				int ret = process.waitFor();
				System.out.println("ret :" + ret);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.exit(1);
			return null;
		}
		
		Instruction toBeReturned = pool[head];
		if(head == tail)
		{
			head = tail = -1;
		}
		else
		{
			head = (head + 1)%poolSize;
		}
		return toBeReturned;		
	}
	
	public void returnObject(Instruction arg0)
	{
//		System.out.println("return"+head+" "+tail);
		if(arg0.getSourceOperand1() != null)
		{
			Newmain.operandPool.returnObject(arg0.getSourceOperand1());
		}
		if(arg0.getSourceOperand2() != null)
		{
			Newmain.operandPool.returnObject(arg0.getSourceOperand2());
		}
		if(arg0.getDestinationOperand() != null)
		{
			Newmain.operandPool.returnObject(arg0.getDestinationOperand());
		}
		
		if(tail == -1)
		{
			head = tail = 0;
		}
		else
		{
			tail = (tail + 1)%poolSize;
		}
		pool[tail] = arg0;
	}
	
	public int getNumIdle()
	{
		if(head == -1)
		{
			return 0;
		}
		if(tail >= head)
		{
			return (tail - head + 1);
		}
		return (poolSize - head + tail + 1);
	}
	*/
	
	GenericCircularBuffer<Instruction> pool;
	
	public CustomInstructionPool(int minPoolSize, int maxPoolSize)
	{
		pool = new GenericCircularBuffer<Instruction>(Instruction.class, minPoolSize, 
				maxPoolSize, true);
	}
	
	public Instruction borrowObject()
	{
//		if(pool.isEmpty()) {
//			misc.Error.showErrorAndExit("instruction pool empty!!");
//			return null;
//		}
		
		return pool.removeObjectAtHead();		
	}
	
	public void returnObject(Instruction arg0)
	{
//		System.out.println("ip = " + arg0.getCISCProgramCounter());
		
//		System.out.println("return"+head+" "+tail);
		
		arg0.clear();
		pool.append(arg0);
	}
	
	public int getNumIdle()
	{
		return pool.size();
	}
}


