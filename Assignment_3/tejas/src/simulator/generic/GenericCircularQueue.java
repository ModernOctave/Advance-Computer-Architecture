package generic;

import java.lang.reflect.Array;

public class GenericCircularQueue<E> {
	
	Class<E> type;
	int head;
	int tail;
	int bufferSize;
	E buffer[];
	
	@SuppressWarnings("unchecked")
	public GenericCircularQueue(Class<E> classType, int bufferSize)
	{
		this.type = classType;
		this.bufferSize = bufferSize;	
		head = tail = -1;
		buffer = (E[])Array.newInstance(this.type, bufferSize);
	}
	
	//returns true if enqueue succeeds
	public boolean enqueue(E newObject)
	{
		if(isFull())
		{
			misc.Error.showErrorAndExit("can't enqueue - queue full");
		}
		
		tail = (tail+1)%bufferSize;
		buffer[tail] = newObject;
		
		if(head == -1)
		{
			head = 0;
		}
		
		//System.out.println("enqueue : " + head + " - " + tail);
		
		return true;
	}
	
	public E dequeue()
	{
		if(isEmpty())
		{
			//System.out.println("can't dequeue - queue empty");
			return null;
		}
		
		E toBeReturned = buffer[head];
		buffer[head] = null;
		if(head == tail)
		{
			head = -1;
			tail = -1;
		}
		else
		{
			head = (head + 1)%bufferSize;
		}
		
		//System.out.println("dequeue : " + head + " - " + tail);
		
		return toBeReturned;
	}
	
	//position refers to logical position in queue - NOT array index
	public E peek(int position)
	{
		if(size() <= position)
		{
			return null;
		}
		
		int peekIndex = (head + position)%bufferSize;
		return buffer[peekIndex];
	}
	
	public boolean isFull()
	{
		if((tail + 1)%bufferSize == head)
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
		if(head == -1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public int size()
	{
		if(head == -1)
		{
			return 0;
		}
		if(head <= tail)
		{
			return (tail - head + 1);
		}
		else
		{
			return (bufferSize - head + tail + 1);
		}
	}
	
	public E pollFirst()
	{
		return dequeue();
	}
	
	public void clear()
	{
		head = -1;
		tail = -1;
	}
	
	public E pop()
	{
		if(size() <= 0)
		{
			return null;
		}
		
		E toBeReturned = buffer[tail];
		buffer[tail] = null;
		if(head == tail)
		{
			head = -1;
			tail = -1;
		}
		else if(tail == 0)
		{
			tail = bufferSize - 1;
		}
		else
		{
			tail = tail - 1;
		}
		
		//System.out.println("pop : " + head + " - " + tail);
		
		return toBeReturned;
	}

	@Override
	public String toString() {
		String str = "";
		if(head != -1)
		{
			for(int i = head; i <= tail; i = (i+1)%bufferSize)
			{
				str = str + buffer[i] + "\n";
			}
		}
		return str;
	}

	public int spaceLeft() {
		return this.bufferSize - this.size();
	}
	
	
}
