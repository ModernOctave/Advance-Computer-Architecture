package pipeline.branchpredictor.TAGESCL;

import pipeline.ExecutionEngine;
import pipeline.branchpredictor.BranchPredictor;

public class TAGESCL extends BranchPredictor {

	TAGESCLInvoker ti;	
	public TAGESCL(ExecutionEngine containingExecEngine)
	{
		super(containingExecEngine);
		ti = new TAGESCLInvoker(containingExecEngine.getContainingCore().getCoreConfig().branchPredictor.TAGESCLLibDirectory);
	}
	
	public boolean predict(long address, boolean outcome)
	{
		return ti.invokerPredict(address);
	}
	
	public void Train(long address, boolean outcome, boolean predict)
	{
		misc.Error.showErrorAndExit("use the other Train() function");
		//don't use this!!
		//use Train(long PC, int opType, boolean resolveDir, boolean predDir, long branchTarget)
	}
	
	public void Train(long PC, int opType, boolean resolveDir, boolean predDir, long branchTarget)
	{
		ti.invokerTrain (PC, opType, resolveDir, predDir, branchTarget);
	}
	
}
