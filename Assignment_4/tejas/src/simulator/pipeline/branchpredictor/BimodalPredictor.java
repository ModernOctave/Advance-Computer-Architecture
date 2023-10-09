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
public class BimodalPredictor extends BranchPredictor {

	int[] PHT;
	int num_states, maskbits;

	public BimodalPredictor(ExecutionEngine containingExecEngine,int PCBits,int saturating_bits)
	{
		super(containingExecEngine);

		num_states=(1<<saturating_bits)-1;
		maskbits=(1<<PCBits)-1;
		PHT=new int[maskbits+1];
		for(int i=0;i<=maskbits;i++)
			PHT[i]=(1<<saturating_bits)-1;
	}

	public void Train(long address, boolean outcome, boolean predict) {
		int index=(int)(address&maskbits);
		if(outcome && PHT[index]!=num_states)
			PHT[index]++;
		else if(!outcome && PHT[index]!=0)
			PHT[index]--;
	}

	public boolean predict(long address, boolean outcome) {
		int index=(int)(address&maskbits);
		if(PHT[index] <= num_states/2)
			return false;
		else
			return true;
	}
}
