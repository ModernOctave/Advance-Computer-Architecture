package pipeline.branchpredictor;

import pipeline.ExecutionEngine;

public class NoPredictor extends BranchPredictor {

	public NoPredictor(ExecutionEngine containingExecutionEngine) {
		super(containingExecutionEngine);
	}

	@Override
	public void Train(long address, boolean outcome, boolean predict) {
		

	}

	@Override
	public boolean predict(long address, boolean outcome) {
		
		return (!outcome);
	}

}
