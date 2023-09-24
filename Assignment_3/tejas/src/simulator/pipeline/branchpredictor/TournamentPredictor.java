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
import config.CoreConfig;
import config.SystemConfig;

/**
 *
 * @author Rikita
 */
public class TournamentPredictor extends BranchPredictor{

	PAgPredictor pred1;
	PApPredictor pred2;
	int counter; //2-bit saturating counter that chooses between the two predictors

	public TournamentPredictor(ExecutionEngine containingExecEngine, int PCBits,int BHRsize,int saturating_bits)
	{
		super(containingExecEngine);

		pred1=new PAgPredictor(containingExecutionEngine, PCBits, BHRsize, saturating_bits);
		pred2=new PApPredictor(containingExecutionEngine, PCBits, BHRsize, saturating_bits);
		counter=0;
	}

	public void Train(long address, boolean outcome, boolean predict) {
		boolean pred1_pred = pred1.predict(address, outcome);
		boolean pred2_pred = pred2.predict(address, outcome);
		pred1.Train(address, outcome, pred1_pred);
		pred2.Train(address, outcome, pred2_pred);
		
		if(pred1_pred!=pred2_pred)
		{
			if (pred1_pred == outcome && counter != 0)
				counter--;
			else if(pred2_pred==outcome && counter!=3)
				counter++;
		}
	}

	public boolean predict(long address, boolean outcome) {
		if(counter==0 ||counter==1)
			return pred1.predict(address, outcome);
		else
			return pred2.predict(address, outcome);
	}
}
