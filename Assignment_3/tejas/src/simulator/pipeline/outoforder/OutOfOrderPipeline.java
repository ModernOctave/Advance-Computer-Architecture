package pipeline.outoforder;

import generic.Core;
import generic.EventQueue;
import generic.GenericCircularQueue;
import generic.GlobalClock;
import generic.Instruction;

public class OutOfOrderPipeline implements pipeline.PipelineInterface {
	
	Core core;
	EventQueue eventQ;
	long coreStepSize;
	
	public OutOfOrderPipeline(Core core, EventQueue eventQ)
	{
		this.core = core;
		this.eventQ = eventQ;
		this.coreStepSize = core.getStepSize();
	}

	@Override
	public void oneCycleOperation() {
		
		OutOrderExecutionEngine execEngine;
		
		execEngine = (OutOrderExecutionEngine) core.getExecEngine();
		
		long currentTime = GlobalClock.getCurrentTime();
		if(currentTime % coreStepSize == 0
				&& execEngine.isExecutionBegun() == true
				&& execEngine.isExecutionComplete() == false)
		{
			execEngine.getReorderBuffer().performCommits();
			execEngine.getWriteBackLogic().performWriteBack();
			execEngine.getSelector().performSelect();
		}
		
		//handle events
		eventQ.processEvents();
		
		if(currentTime % coreStepSize == 0
				&& execEngine.isExecutionBegun() == true
				&& execEngine.isExecutionComplete() == false)
		{
			execEngine.getIWPusher().performIWPush();
			execEngine.getRenamer().performRename();
			execEngine.getDecoder().performDecode();
			execEngine.getFetcher().performFetch();
		}
		
	}
	public Core getCore() {
		return core;
	}

	public void setCore(Core core) {
		this.core = core;
	}

	public EventQueue getEventQ() {
		return eventQ;
	}

	public void setEventQ(EventQueue eventQ) {
		this.eventQ = eventQ;
	}

	@Override
	public boolean isExecutionComplete() {		
		return core.getExecEngine().isExecutionComplete();		
	}
	
	public void setcoreStepSize(int stepSize)
	{
		this.coreStepSize = stepSize;
	}
	
	public long getCoreStepSize()
	{
		return coreStepSize;
	}

	@Override
	public void resumePipeline() {
		((OutOrderExecutionEngine)core.getExecEngine()).getFetcher().setSleep(false);
	}

	@Override
	public boolean isSleeping() {
		return ((OutOrderExecutionEngine)core.getExecEngine()).getFetcher().isSleep();
	}

	@Override
	public void setTimingStatistics() {
		
		OutOrderExecutionEngine execEngine = (OutOrderExecutionEngine) core.getExecEngine();
		execEngine.getReorderBuffer().setTimingStatistics();
		
	}

	@Override
	public void setPerCoreMemorySystemStatistics() {
		
		OutOrderExecutionEngine execEngine = (OutOrderExecutionEngine) core.getExecEngine();
		execEngine.getReorderBuffer().setPerCoreMemorySystemStatistics();
		
	}


	@Override
	public void setExecutionComplete(boolean status) {
				
	}


	@Override
	public void adjustRunningThreads(int adjval) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInputToPipeline(
			GenericCircularQueue<Instruction>[] inputToPipeline) {
		
		this.core.getExecEngine().setInputToPipeline(inputToPipeline);
		
	}	

}
