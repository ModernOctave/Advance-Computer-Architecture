/*****************************************************************************
				Tejas Simulator
------------------------------------------------------------------------------------------------------------

   Copyright [2010] [Indian Institute of Technology, Delhi]
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
------------------------------------------------------------------------------------------------------------

	Contributors:  Prathmesh Kallurkar
*****************************************************************************/

package generic;

import main.CustomObjectPool;

public class InstructionList 
{
	//private ArrayList<Instruction> instructionArrayList;
	private GenericCircularQueue<Instruction> instructionQueue = null;
	//SynchronizationObject syncObject;
	//SynchronizationObject syncObject2;
	
	public InstructionList(int initSize)
	{
		//instructionArrayList = new ArrayList<Instruction>(initSize);
		instructionQueue = new GenericCircularQueue<Instruction>(Instruction.class, initSize);
		
		//syncObject = new SynchronizationObject();
		//syncObject2 = new SynchronizationObject();
	}

	//appends a single instruction to the instruction list
	public void appendInstruction(Instruction newInstruction)
	{
		//instructionArrayList.add(newInstruction);
		instructionQueue.enqueue(newInstruction);
	}
	
	public boolean isEmpty()
	{
		//return instructionArrayList.isEmpty();
		return instructionQueue.isEmpty();
	}
	
	public Instruction get(int index)
	{
		// For the last instruction of the file, we will have to return null,
		// otherwise, we will encounter an Exception.
		//if(index >= instructionArrayList.size())
		if(index >= instructionQueue.size())
		{
			return null;
		}
		else
		{
			//return instructionArrayList.get(index);
			return instructionQueue.peek(index);
		}
	}
	
	public void printList() 
	{
		//for(int i = 0; i< instructionArrayList.size(); i++)
		for(int i = 0; i< instructionQueue.size(); i++)
		{
			//System.out.print(instructionArrayList.get(i).toString() + "\n");
			System.out.print(instructionQueue.peek(i).toString() + "\n");
		}
	}

//	public Instruction getNextInstruction()
//	{
//		if(listIterator.hasNext())
//		{
//			return listIterator.next(); 
//		}
//		else 
//		{
//			//If the list iterator is well past the last element we return a null
//			return null;
//		}
//	}
	
//	public Instruction pollFirst()
//	{
//		// FIXME : Need to decide an laternative for this
//		return instructionLinkedList.pollFirst();
//	}

	public void setCISCProgramCounter(int index, long instructionPointer) 
	{
		//instructionArrayList.get(index).setCISCProgramCounter(instructionPointer);
		instructionQueue.peek(index).setCISCProgramCounter(instructionPointer);
	}
		
	public int getListSize()
	{
		//return instructionArrayList.size();
		return instructionQueue.size();
	}
	
	public Instruction peekInstructionAt(int position)
	{
		//return instructionArrayList.get(position);
		return instructionQueue.peek(position);
	}
	
	// remove last instruction but leave out operand1,operand2,operand3 or their components
	public void removeLastInstr(Operand operand1, Operand operand2, Operand operand3)
	{
		//this.instructionArrayList.remove(instructionArrayList.size()-1);
		CustomObjectPool.getInstructionPool().returnObject(this.instructionQueue.pop());
	}
	
	public void removeLastInstr() {
		CustomObjectPool.getInstructionPool().returnObject(this.instructionQueue.pop());
	}
	
//	public SynchronizationObject getSyncObject() {
//		return syncObject;
//	}
	
	public int length()
	{
		//return instructionArrayList.size();
		return instructionQueue.size();
	}

	public void clear() {
		instructionQueue.clear();
	}
	
	/*public SynchronizationObject getSyncObject2() {
		return syncObject2;
	}*/
}