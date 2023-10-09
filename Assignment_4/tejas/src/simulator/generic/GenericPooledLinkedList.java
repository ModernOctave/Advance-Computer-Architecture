package generic;

import java.util.Comparator;

import main.ArchitecturalComponent;
import memorysystem.GenericPooledLinkedListInterface;

public class GenericPooledLinkedList<E> {
	
	Class<E> type;
	GenericPooledLinkedListInterface<E> helperMethodContainer;
	LinkedListNode<E> head;
	LinkedListNode<E> tail;
	int bufferSize;
	
	@SuppressWarnings("unchecked")
	public GenericPooledLinkedList(Class<E> classType, int bufferSize, GenericPooledLinkedListInterface<E> helperMethodContainer)
	{
		this.type = classType;
		this.helperMethodContainer = helperMethodContainer;		
		head = tail = null;
		this.bufferSize = bufferSize;
		
		for(int i = 0; i < bufferSize; i++)
		{
			LinkedListNode<E> temp = new LinkedListNode<E>(classType);
			
			if(head == null)
			{
				tail = head = temp;
			}
			else
			{
				tail.next = temp;
				tail = temp;
			}
		}
	}
	
	public E search(E searchNode)
	{
		LinkedListNode<E> temp;
		temp = head;
		while(temp != null)
		{
			if(temp.valid == false)
			{
				break;
			}
			if(helperMethodContainer.compare(searchNode, temp.element) == 0)
			{
				return temp.element;
			}
			temp = temp.next;
		}
		return null;
	}
	
	public E remove(E removeNode)
	{
		LinkedListNode<E> temp, prev;
		temp = head;
		prev = null;
		while(temp != null)
		{
			if(temp.valid == false)
			{
				break;
			}
			if(helperMethodContainer.compare(removeNode, temp.element) == 0)
			{
				if(prev != null)
				{
					prev.next = temp.next;
				}
				else
				{
					head = temp.next;
				}
				tail.next = temp;
				tail = temp;
				temp.next = null;
				temp.valid = false;
				return temp.element;
			}
			prev = temp;
			temp = temp.next;
		}
		return null;
	}
	
	/*public E getFirstInvalidElement()
	{
		LinkedListNode<E> temp;
		temp = head;
		while(temp != null && temp.valid == true)
		{
			temp = temp.next;
		}
		
		if(temp == null)
		{
			ArchitecturalComponent.exitOnAssertionFail("mshr overflow!!");
		}
		
		temp.valid = true;
		return temp.element;
	}*/
	
	public void add(E newObject)
	{
		LinkedListNode<E> temp;
		temp = head;
		while(temp != null && temp.valid == true)
		{
			temp = temp.next;
		}
		
		if(temp == null)
		{
			misc.Error.showErrorAndExit("mshr overflow!!");
		}
		
		temp.valid = true;
		helperMethodContainer.copy(newObject, temp.element);
	}

}

class LinkedListNode<E> {
	
	E element;
	LinkedListNode<E> next;
	boolean valid;
	
	LinkedListNode(Class<E> classType)
	{
		try {
			element = classType.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		next = null;
		valid = false;
	}
	
}
	

