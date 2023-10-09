package pipeline.multi_issue_inorder;

import generic.GlobalClock;
import generic.Instruction;

public class StageLatch_MII {
	
	private Instruction[] instructions;
	long instructionCompletesAt[];	//used to indicate when the corresponding instruction is ready for
									//consumption by the next stage;
									//a long is used instead of a boolean because a boolean would require
									//modeling the completion of execution at FUs through events,
									//which would slow down simulation
	
	int size;
	int head;
	int tail;
	int curSize;
	
	public StageLatch_MII(int size)
	{
		this.size = size;
		instructions = new Instruction[size];
		instructionCompletesAt = new long[size];
		head = -1;
		tail = -1;
		curSize = 0;
	}

	public boolean isFull()
	{
		if(curSize >= size)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean isEmpty()
	{
		if(curSize <= 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void add(Instruction newInstruction, long instCompletesAt)
	{
		if(tail == -1)
		{
			head = 0;
			tail = 0;
		}
		else
		{
			tail = (tail + 1)%size;
		}
		
		instructions[tail] = newInstruction;
		instructionCompletesAt[tail] = instCompletesAt;
		curSize++;
	}
	
	public Instruction peek(int pos)
	{
		if(curSize <= pos)
		{
			return null;
		}
		
		int retPos = (head + pos) % size;
		
		if(instructionCompletesAt[retPos] > GlobalClock.getCurrentTime())
		{
			return null;
		}
		
		return instructions[retPos];
	}
	
	public Instruction poll()
	{
		if(curSize <= 0)
		{
			return null;
		}
		
		Instruction toBeReturned = instructions[head];
		if(instructionCompletesAt[head] > GlobalClock.getCurrentTime())
		{
			return null;
		}
		instructions[head] = null;
		
		if(head == tail)
		{
			head = -1;
			tail = -1;
		}
		else
		{
			head = (head + 1) % size;
		}		
		curSize--;
		
		return toBeReturned;
	}

	public Instruction[] getInstructions() {
		return instructions;
	}

	public long[] getInstructionCompletesAt() {
		return instructionCompletesAt;
	}
	
	public long getInstructionCompletesAt(Instruction ins)
	{
		for(int i = 0; i < size; i++)
		{
			if(instructions[i] == ins)
			{
				return instructionCompletesAt[i];
			}
		}
		return -1;
	}
	
}
