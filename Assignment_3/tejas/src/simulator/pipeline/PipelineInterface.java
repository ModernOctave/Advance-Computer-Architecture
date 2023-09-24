package pipeline;

import generic.Core;
import generic.GenericCircularQueue;
import generic.Instruction;

public interface PipelineInterface {
	
	public void oneCycleOperation();	
	public boolean isExecutionComplete();
	public void setcoreStepSize(int stepSize);
	public long getCoreStepSize();
	public void resumePipeline();
	public Core getCore();
	public boolean isSleeping();
	public void setTimingStatistics();
	public void setPerCoreMemorySystemStatistics();
	public void setExecutionComplete(boolean status);
	public void adjustRunningThreads(int adjval);
	public void setInputToPipeline(GenericCircularQueue<Instruction>[] inputToPipeline);
}
