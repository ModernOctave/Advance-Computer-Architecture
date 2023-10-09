package pipeline.multi_issue_inorder;

import pipeline.PipelineInterface;
import generic.Core;
import generic.EventQueue;
import generic.GenericCircularQueue;
import generic.GlobalClock;
import generic.Instruction;

public class MultiIssueInorderPipeline implements PipelineInterface{
	
	Core core;
	MultiIssueInorderExecutionEngine containingExecutionEngine;
	EventQueue eventQ;
	long coreStepSize;
	StageLatch_MII ifId,idEx,exMem,memWb,wbDone;
	

	public MultiIssueInorderPipeline(Core _core, EventQueue eventQ){
	
		this.core = _core;
		containingExecutionEngine = (MultiIssueInorderExecutionEngine)core.getExecEngine();
		this.eventQ = eventQ;
		this.coreStepSize = core.getStepSize();	//Not Necessary. Global clock hasn't been initialized yet
												//So, step sizes of the cores hasn't been set.
												//It will be set when the step sizes of the cores will be set.
		this.ifId = containingExecutionEngine.getIfIdLatch();
		this.idEx = containingExecutionEngine.getIdExLatch();
		this.exMem = containingExecutionEngine.getExMemLatch();
		this.memWb = containingExecutionEngine.getMemWbLatch();
		this.wbDone = containingExecutionEngine.getWbDoneLatch();
	}
	
	public void oneCycleOperation(){
		long currentTime = GlobalClock.getCurrentTime();
		if(currentTime % getCoreStepSize()==0
				&& containingExecutionEngine.isExecutionBegun() == true
				&& !containingExecutionEngine.getExecutionComplete()){
			writeback();
		}
		drainEventQueue();		//Process Memory Requests
		if(currentTime % getCoreStepSize()==0
				&& containingExecutionEngine.isExecutionBegun() == true
				&& !containingExecutionEngine.getExecutionComplete()){
			mem();
			exec();
			decode();
			fetch();

//			if(this.containingExecutionEngine.getStallFetch()>0){
//				this.containingExecutionEngine.decrementStallFetch(1); 
//			}
		}
	}

	private void drainEventQueue(){
		eventQ.processEvents();		
	}
	public void writeback(){
		containingExecutionEngine.getWriteBackUnitIn().performWriteBack(this);		
	}
	public void mem(){
		containingExecutionEngine.getMemUnitIn().performMemEvent(this);
	}
	public void exec(){
		containingExecutionEngine.getExecUnitIn().execute(this);
	}
	public void decode(){
		containingExecutionEngine.getDecodeUnitIn().performDecode(this);
	}
	public void fetch(){
		containingExecutionEngine.getFetchUnitIn().performFetch(this);
	}

	@Override
	public boolean isExecutionComplete() {
		return (containingExecutionEngine.getExecutionComplete());
		}

	@Override
	public long getCoreStepSize() {
		return this.core.getStepSize();
	}

	@Override
	public void setcoreStepSize(int stepSize) {
		this.coreStepSize=stepSize;
	}

	@Override
	public void resumePipeline() {
		containingExecutionEngine.getFetchUnitIn().resumePipeline();		
	}

	@Override
	public Core getCore() {
		return core;
	}

	@Override
	public boolean isSleeping() {
		return containingExecutionEngine.getFetchUnitIn().getSleep();
	}

	public StageLatch_MII getIfIdLatch(){
		return this.ifId;
	}
	public StageLatch_MII getIdExLatch(){
		return this.idEx;
	}
	public StageLatch_MII getExMemLatch(){
		return this.exMem;
	}
	public StageLatch_MII getMemWbLatch(){
		return this.memWb;
	}
	public StageLatch_MII getWbDoneLatch(){
		return this.wbDone;
	}

	@Override
	public void setExecutionComplete(boolean status) {
		containingExecutionEngine.setExecutionComplete(status);		
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

	@Override
	public void setTimingStatistics() {
		// Not needed here, set by inorderexecutionengine
		
	}

	@Override
	public void setPerCoreMemorySystemStatistics() {
		// Not needed here, set by inorderexecutionengine
		
	}
}
