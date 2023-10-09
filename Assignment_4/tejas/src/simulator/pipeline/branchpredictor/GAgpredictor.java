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
public class GAgpredictor extends BranchPredictor {
	int[] PHT;
	int BHR;
	int initialBHR;
	int num_states;

	public GAgpredictor(ExecutionEngine containingExecEngine, int BHRsize, int saturating_bits){
		super(containingExecEngine);

		initialBHR=(int) (Math.pow(2, BHRsize));
		BHR=(initialBHR)-1;

		PHT=new int[initialBHR];
		for(int i=0;i<(initialBHR);i++)
			PHT[i]=(1<<saturating_bits)-1;

		num_states = (1<<saturating_bits)-1;
	}

	public void Train(long address, boolean outcome,boolean predict) {
		if(outcome && PHT[BHR]!=num_states)
			PHT[BHR]++;
		else if(!outcome && PHT[BHR]!=0)
			PHT[BHR]--;

		/*to record the new 0/1 value (at the LSB) according to the actual implemetation, in the BHR register*/
		BHR=BHR<<1;
		if(outcome==true)
			BHR=BHR+1;
		BHR=BHR&((initialBHR)-1);

	}

	public boolean predict(long address, boolean outcome) {
		if(PHT[BHR] <= num_states/2)
			return false;
		else
			return true;
	}
}
