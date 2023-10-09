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
public class GApPredictor extends BranchPredictor {

	int[] PHT;
	int BHR;
	int initialBHR;
	int num_states;
	int maskbits;
	int PCBits;

	public GApPredictor(ExecutionEngine containingExecEngine, int BHRsize,int PCBits, int saturating_bits){
		super(containingExecEngine);

		int PHTsize=1<<(PCBits+BHRsize);
		PHT=new int[PHTsize];
		for(int i=0;i<PHTsize;i++)
			PHT[i]=(1<<saturating_bits)-1;

		BHR=(1<<BHRsize)-1;
		initialBHR=BHR;

		maskbits=(1<<PCBits)-1;

		this.PCBits = PCBits;

		num_states = (1<<saturating_bits)-1;
	}

	public void Train(long address, boolean outcome,boolean predict) {
		int index=(BHR<<PCBits)+(int)(address&maskbits);

		if(outcome && PHT[index]!=num_states)
			PHT[index]++;
		else if(!outcome && PHT[index]!=0)
			PHT[index]--;

		/*to record the new 0/1 value (at the LSB) according to the actual implemetation, in the BHR register*/
		BHR=BHR<<1;
		if(outcome==true)
			BHR=BHR+1;
		BHR=BHR&((initialBHR)-1);
	}

	public boolean predict(long address, boolean outcome) {
		int index=(BHR<<PCBits)+(int)(address&maskbits);
		if(PHT[index] <= num_states/2)
			return false;
		else
			return true;
	}
}
