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

import java.util.Hashtable;

//Hashtable record : instructionPointer - index

public class InstructionTable 
{
	private Hashtable<Long, Integer> instructionHashTable;

	public InstructionTable(int n)
	{
		this.instructionHashTable = new Hashtable<Long, Integer>(n); 
	}
	
	public void addInstruction(Long instructionPointer, int index)
	{
		instructionHashTable.put(instructionPointer, index);
	}
	
	public int getMicroOpIndex(Long instructionPointer)
	{
		try
		{
			Integer returnValue = instructionHashTable.get(instructionPointer);
			if(returnValue == null)
			{
				//to handle lock prefix
				//TODO atomicity of a locked instruction needs to be implemented
				return (-1 * instructionHashTable.get(instructionPointer-1));
			}
			return returnValue;	
		}
		catch(Exception exception)
		{
			return -1;
		}
		
	}
}