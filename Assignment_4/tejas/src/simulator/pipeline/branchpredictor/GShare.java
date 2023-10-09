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
public class GShare extends BranchPredictor{

	int[] PHT;
	int BHR;
	int num_states;
	int maskbits;

	public GShare(ExecutionEngine containingExecEngine, int BHRsize, int saturating_bits)
	{
		super(containingExecEngine);

		BHR=(1<<BHRsize) - 1;

		num_states=(1<<saturating_bits)-1;

		int PHTsize=BHR+1;
		PHT=new int[PHTsize];
		for(int i=0;i<PHTsize;i++)
			PHT[i]=num_states;

		maskbits=BHR;
	}

	public void Train(long address, boolean outcome, boolean predict) {
		int index = ((int)address&maskbits) ^ BHR;

		if(outcome && PHT[index]!=num_states)
			PHT[index]++;
		else if(!outcome && PHT[index]!=0)
			PHT[index]--;

		BHR=BHR<<1;
		if(outcome==true)
			BHR=BHR+1;
		BHR=maskbits&BHR;
	}

	public boolean predict(long address, boolean outcome) {
		int index = ((int)address&maskbits) ^ BHR;
		if(PHT[index] <= num_states/2)
            return false;
		else
            return true;
	}
}
