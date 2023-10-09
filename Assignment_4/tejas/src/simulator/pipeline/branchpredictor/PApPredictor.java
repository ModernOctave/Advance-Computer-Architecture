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

	Contributors:  Rikita Ahuja
 *****************************************************************************/



package pipeline.branchpredictor;

import pipeline.ExecutionEngine;

/**
 *
 * @author Rikita
 */
public class PApPredictor extends BranchPredictor{

	int[] PBHT;
	int[][] PPHT;
	int initialBHR;
	int num_states;
	int maskbits;

	public PApPredictor(ExecutionEngine containingExecEngine, int PCBits,int BHRsize,int saturating_bits)
	{
		super(containingExecEngine);

		maskbits=(1<<PCBits)-1;
		PBHT=new int[maskbits+1];

		num_states=(1<<saturating_bits)-1;

		initialBHR = (1<<BHRsize)-1;
		PPHT = new int[maskbits+1][];
		for(int i = 0; i <= maskbits; i++)
		{
			PPHT[i] = new int[initialBHR+1];
			for(int j = 0; j <= initialBHR; j++)
				PPHT[i][j] = num_states;
		}
	}

	public void Train(long address, boolean outcome, boolean predict) {
		int index=(int)(maskbits&address);
		int BHR=PBHT[index];

		if(outcome && PPHT[index][BHR]!=num_states)
			PPHT[index][BHR]++;
		else if(!outcome && PPHT[index][BHR]!=0)
			PPHT[index][BHR]--;

		BHR=BHR<<1;
		if(outcome==true)
			BHR++;
		BHR=BHR&initialBHR;
		PBHT[index]=BHR;
	}

	public boolean predict(long address, boolean outcome) {
		int index=(int)(maskbits&address);
		int BHR=PBHT[index];
		if(PPHT[index][BHR] <= num_states/2)
			return false;
		else
			return true;
	}
}
