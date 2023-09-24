package generic;

/*
 * represents a circular buffer of type E
 * circular buffer is implemented as a linked list
 * starts of with size bufferSize, specified in constructor
 * if isGrowable is set, then upon exhaustion of the buffer, new objects are created and added; bufferSize is duly incremented
 * (idea is to support a higher level pool class)
 */

public class GenericCircularBuffer<E> {
	
	Class type;
	Element<E> head;
	Element<E> tail;
	int minBufferSize;
	int maxBufferSize;
	int currentMaxBufferSize; // ensures that we do not return more objects than we gave out
	boolean isGrowable;
	int currentSize;
		
	@SuppressWarnings("unchecked")
	public GenericCircularBuffer(Class E, int minBufferSize, int maxBufferSize,
			boolean isGrowable)
	{
		this.type = E;
		this.minBufferSize = minBufferSize;
		this.maxBufferSize = maxBufferSize;
		this.currentMaxBufferSize = minBufferSize;
		this.currentSize = minBufferSize;
		
		tail = new Element<E>(E, null);
		
		Element<E> temp = tail;
		for(int i = 0; i < minBufferSize - 1; i++)
		{
			temp = new Element<E>(E, temp);
		}
		
		head = temp;
		tail.next = head;
		
		this.isGrowable = isGrowable;
		//this.isGrowable = false;
	}
	
	public boolean append(E newObject)
	{
		if(isFull())
		{
			return false;
		}
		
		tail = tail.next;
		tail.object = newObject;
		
		currentSize++;
		
		return true;
	}
	
	public E removeObjectAtHead()
	{
		if(isEmpty() && !isGrowable)
		{
			return null;
		}
		
		else if(!isEmpty())
		{
			E toBeReturned = head.object;
			head = head.next;
			
			currentSize--;
			
			return toBeReturned;
		}
		
		else
		{
//			When we have to increment by just one element
//			Element<E> newElement = new Element<E>(type, tail);
//			head.next = newElement;
//			
//			bufferSize++;
//			currentSize++;
//			
//			return newElement.object;

			// When we have to increment by dynamic number of elements
			Element<E> temp = head.next;
			int numElementsAdded = (int)(0.2*minBufferSize); // 0.2 -> 20% 
			
			if((currentMaxBufferSize+numElementsAdded) > maxBufferSize) {
				misc.Error.showErrorAndExit("pool overflow !!");
			}
			
			for(int i = 0; i < numElementsAdded ; i++) 
			{
				temp = new Element<E>(this.type, temp);
			}
			
			head.next=temp;
//			head = head.next;
			
			//System.out.println("pool size increased from " + currentMaxBufferSize + " to "
			//		+ (currentMaxBufferSize + numElementsAdded));
			
			currentMaxBufferSize += numElementsAdded;
			currentSize += numElementsAdded;
			
			return removeObjectAtHead();
		}
	}
	
	public boolean isFull()
	{
		if(currentSize == currentMaxBufferSize)
		{
			return true;
		}
		return false;
	}
	
	public boolean isEmpty()
	{
		if(currentSize == 2)
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
		return currentSize;
	}

	public int getPoolCapacity() {
		return currentMaxBufferSize;
	}
}

@SuppressWarnings("unchecked")
class Element<E> {
	
	E object;
	Element<E> next;
	
	Element(Class E, Element<E> next)
	{
		try {
			object = (E) E.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		this.next = next;
	}
}
/*
package generic;

public class GenericCircularBuffer<E> {
	
	E[] buffer;
	int bufferSize;
	int head;
	int tail;
	
	@SuppressWarnings("unchecked")
	public GenericCircularBuffer(int bufferSize, Class E)
	{
		this.bufferSize = bufferSize;
		
		buffer = (E[]) new Object[bufferSize];
		for(int i = 0; i < bufferSize; i++)
		{
			try {
				buffer[i] = (E) E.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		head = 0;
		tail = bufferSize - 1;
	}
	
	public boolean append(E newObject)
	{
		if(isFull())
		{
			return false;
		}
		
		if(tail == -1)
		{
			head = tail = 0;
		}
		else
		{
			tail = (tail + 1)%bufferSize;
		}
		buffer[tail] = newObject;
		
		return true;
	}
	
	public E removeObjectAtHead()
	{
		if(isEmpty())
		{
			return null;
		}
		
		E toBeReturned = buffer[head];
		if(head == tail)
		{
			head = tail = -1;
		}
		else
		{
			head = (head + 1)%bufferSize;
		}
		return toBeReturned;
	}
	
	public E removeObjectAtTail()
	{
		if(isEmpty())
		{
			return null;
		}
		
		E toBeReturned = buffer[tail];
		if(head == tail)
		{
			head = tail = -1;
		}
		else
		{
			tail = (tail - 1)%bufferSize;
		}
		return toBeReturned;
	}
	
	public boolean isFull()
	{
		if((head == 0 && tail == bufferSize - 1) ||
				head == tail + 1)
		{
			return true;
		}
		return false;
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
		if(tail >= head)
		{
			return (tail - head + 1);
		}
		return (bufferSize - head + tail + 1);
	}
}

@SuppressWarnings("unchecked")
class Element<E> {
	
	E object;
	Element<E> next;
	
	Element(Class E)
	{
		try {
			object = (E) E.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		next = null;
	}
}

*/