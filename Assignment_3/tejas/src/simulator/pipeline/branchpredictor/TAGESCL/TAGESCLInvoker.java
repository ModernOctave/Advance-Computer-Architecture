package pipeline.branchpredictor.TAGESCL;

public class TAGESCLInvoker {
	
	private native void initialize();
	private native boolean predict (long PC);
	private native void train (long PC, int opType, boolean resolveDir, boolean predDir, long branchTarget);
	
	public TAGESCLInvoker(String TAGESCLLibDirectory)
	{
		System.load(TAGESCLLibDirectory + "/libnative.so");
		initialize();
	}
	
	public boolean invokerPredict(long address)
	{
		return predict(address);
	}
	
	public void invokerTrain(long PC, int opType, boolean resolveDir, boolean predDir, long branchTarget)
	{
		train (PC, opType, resolveDir, predDir, branchTarget);
	}
	
}
