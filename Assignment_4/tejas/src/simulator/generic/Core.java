package generic;

import java.io.FileWriter;
import java.io.IOException;

import main.ArchitecturalComponent;
import memorysystem.Cache;
import memorysystem.CoreMemorySystem;
import memorysystem.nuca.NucaCache;
import net.BusInterface;
import net.NocInterface;
import pipeline.ExecutionEngine;
import pipeline.FunctionalUnitType;
import pipeline.multi_issue_inorder.InorderCoreMemorySystem_MII;
import pipeline.multi_issue_inorder.MultiIssueInorderExecutionEngine;
import pipeline.multi_issue_inorder.MultiIssueInorderPipeline;
import pipeline.outoforder.OutOfOrderPipeline;
import pipeline.outoforder.OutOrderCoreMemorySystem;
import pipeline.outoforder.OutOrderExecutionEngine;
import config.CoreConfig;
import config.EnergyConfig;
import config.PipelineType;
import config.SystemConfig;

/**
 * represents a single core
 * has it's own clock, and comprises of an execution engine and an event queue
 * all core parameters are defined here
 */

public class Core extends SimulationElement{
	
	//long clock;
	CoreConfig coreConfig;
	Port port;
	long stepSize;
	long frequency;
	ExecutionEngine execEngine;
	public EventQueue eventQueue;
	public int currentThreads;	
	private int core_number;
	private int no_of_input_pipes;
	private int no_of_threads;
	private long coreCyclesTaken;
	
	private int[] threadIDs;
	
	private long noOfInstructionsExecuted;
	
	private pipeline.PipelineInterface pipelineInterface;
	public int numReturns;
	private int numInorderPipelines;
	
	public int barrier_latency;
	public boolean TreeBarrier;
	public int barrierUnit; //0=>central 1=>distributed

//	private InorderPipeline inorderPipeline;

	
	public Core(int core_number,
			int no_of_input_pipes,
			int no_of_threads,
			InstructionLinkedList[] incomingInstructionLists,
			int[] threadIDs)
	{
		super(PortType.Unlimited, -1, -1, -1, SystemConfig.core[core_number].frequency);	

		coreConfig = SystemConfig.core[core_number];
		
		this.eventQueue = new EventQueue();
		this.frequency = coreConfig.frequency;
		this.stepSize = coreConfig.stepSize;
				
		this.core_number = core_number;
		this.no_of_input_pipes = no_of_input_pipes;
		this.no_of_threads = no_of_threads;
		this.threadIDs = threadIDs;
		this.currentThreads =0;

		this.noOfInstructionsExecuted = 0;
		this.numReturns=0;

		// Create execution engine
		if(this.isPipelineInOrder()) {
			this.execEngine = new MultiIssueInorderExecutionEngine(this, coreConfig.IssueWidth);
		} else if (isPipelineOutOfOrder()){
			this.execEngine = new OutOrderExecutionEngine(this);
		} else {
			misc.Error.showErrorAndExit("pipeline type not identified : " + 
				SystemConfig.core[core_number].pipelineType);
		}
		
		// Create pipeline interface
		if(isPipelineInOrder()) {
			this.pipelineInterface = new MultiIssueInorderPipeline(this, eventQueue);
		} else if (isPipelineOutOfOrder()) {
			this.pipelineInterface = new OutOfOrderPipeline(this, eventQueue);
		} else {
			misc.Error.showErrorAndExit("pipeline type not identified : " + 
				SystemConfig.core[core_number].pipelineType);
		}
		
		// Create core memory interface
		CoreMemorySystem coreMemSys = null;
		if(isPipelineInOrder()) {
			coreMemSys = new InorderCoreMemorySystem_MII(this);
		} else if (isPipelineOutOfOrder()) {
			coreMemSys = new  OutOrderCoreMemorySystem(this);
		} else {
			misc.Error.showErrorAndExit("pipeline type not identified : " + 
				SystemConfig.core[core_number].pipelineType);
		}
		
		this.execEngine.setCoreMemorySystem(coreMemSys);
		ArchitecturalComponent.coreMemSysArray.add(coreMemSys);
	}
	
	/*public void boot()
	{
		//set up initial events in the queue
		eventQueue.addEvent(new PerformDecodeEvent(GlobalClock.getCurrentTime(), this, 0));
//TODO commented only for perfect pipeline		
		if (perfectPipeline == false)
			eventQueue.addEvent(new PerformCommitsEvent(GlobalClock.getCurrentTime(), this));
	}*/
	
	/*public void work()
	{
		execEngine.work();
	}*/

	/*public long getClock() {
		return clock;
	}

	public void setClock(long clock) {
		this.clock = clock;
	}
	
	public void incrementClock()
	{
		this.clock++;
	}*/
	
	public boolean isPipelineInOrder() {
		return (SystemConfig.core[this.core_number].pipelineType==PipelineType.inOrder);
	}
	
	public boolean isPipelineOutOfOrder() {
		return (SystemConfig.core[this.core_number].pipelineType==PipelineType.outOfOrder);
	}
	
	private void setBarrierLatency(int barrierLatency) {
		this.barrier_latency = barrierLatency;
		
	}
	private void setBarrierUnit(int barrierUnit){
		this.barrierUnit = barrierUnit;
	}
	public void activatePipeline(){
		this.pipelineInterface.resumePipeline();
	}
	public void sleepPipeline(){
		
		((MultiIssueInorderExecutionEngine)this.getExecEngine()).getFetchUnitIn().inputToPipeline.enqueue(Instruction.getSyncInstruction());
	}

	public void setTreeBarrier(boolean bar)
	{
		TreeBarrier = bar;
	}
	
	public int getIssueWidth() {
		return coreConfig.IssueWidth;
	}

	public int getNumInorderPipelines() {
		return numInorderPipelines;
	}

	public void setNumInorderPipelines(int numInorderPipelines) {
		this.numInorderPipelines = numInorderPipelines;
	}

	public int getRetireWidth() {
		return coreConfig.RetireWidth;
	}

	public EventQueue getEventQueue() {
		return eventQueue;
	}
	
	public void setEventQueue(EventQueue _eventQueue) {
		eventQueue = _eventQueue;
	}

	public ExecutionEngine getExecEngine() {
		return execEngine;
	}

	public int getBranchMispredictionPenalty() {
		return coreConfig.BranchMispredPenalty;
	}

	public int getDecodeWidth() {
		return coreConfig.DecodeWidth;
	}
	
	public int getRenameWidth() {
		return coreConfig.RenameWidth;
	}

	public int getVectorRegisterFileSize() {
		return coreConfig.VectorRegFileSize;
	}

	public int getIntegerRegisterFileSize() {
		return coreConfig.IntRegFileSize;
	}

	public int getNVectorArchitecturalRegisters() {
		return coreConfig.VectorArchRegNum;
	}

	public int getNIntegerArchitecturalRegisters() {
		return coreConfig.IntArchRegNum;
	}

	public int getReorderBufferSize() {
		return coreConfig.ROBSize;
	}

	public int getIWSize() {
		return coreConfig.IWSize;
	}
	
	public int[] getThreadIDs() {
		return threadIDs;
	}

	public int getNo_of_input_pipes() {
		return no_of_input_pipes;
	}
	
	public int getNo_of_threads() {
		return no_of_threads;
	}
	
	public int getCore_number() {
		return core_number;
	}
	
	public long getNoOfInstructionsExecuted() {
		return noOfInstructionsExecuted;
	}

	public void setNoOfInstructionsExecuted(long noOfInstructionsExecuted) {
		this.noOfInstructionsExecuted = noOfInstructionsExecuted;
	}
	
	public void incrementNoOfInstructionsExecuted()
	{
		this.noOfInstructionsExecuted++;
	}
	
	public pipeline.PipelineInterface getPipelineInterface() {
		return pipelineInterface;
	}
	
	public void setPipelineInterface(OutOfOrderPipeline pipelineInterface) {
		this.pipelineInterface = pipelineInterface;
	}
	
	public void setInputToPipeline(GenericCircularQueue<Instruction>[] inputsToPipeline)
	{
		this.getExecEngine().setInputToPipeline(inputsToPipeline);
	}
	
	public void setStepSize(int stepSize)
	{
		this.stepSize = stepSize;
		this.pipelineInterface.setcoreStepSize(stepSize);
	}

	public long getCoreCyclesTaken() {
		return coreCyclesTaken;
	}

	public void setCoreCyclesTaken(long coreCyclesTaken) {
		this.coreCyclesTaken = coreCyclesTaken;
	}
	
	public long getFrequency()
	{
		return this.frequency;
	}
	
	public void setFrequency(long frequency)
	{
		this.frequency = frequency;
	}
	
	public long getStepSize()
	{
		return stepSize;
	}
	
	@Override
	public void handleEvent(EventQueue eventQ, Event event) 
	{
	}
	
	public EnergyConfig getbPredPower() {
		return coreConfig.bPredPower;
	}

	public EnergyConfig getDecodePower() {
		return coreConfig.decodePower;
	}

	public EnergyConfig getIntRATPower() {
		return coreConfig.intRATPower;
	}

	public EnergyConfig getFpRATPower() {
		return coreConfig.floatRATPower;
	}

	public EnergyConfig getIntFreeListPower() {
		return coreConfig.intFreeListPower;
	}

	public EnergyConfig getVectorFreeListPower() {
		return coreConfig.vectorFreeListPower;
	}

	public EnergyConfig getLsqPower() {
		return coreConfig.lsqPower;
	}

	public EnergyConfig getIntRegFilePower() {
		return coreConfig.intRegFilePower;
	}

	public EnergyConfig getVectorRegFilePower() {
		return coreConfig.vectorRegFilePower;
	}

	public EnergyConfig getIwPower() {
		return coreConfig.iwPower;
	}

	public EnergyConfig getRobPower() {
		return coreConfig.robPower;
	}

	public EnergyConfig getResultsBroadcastBusPower() {
		return coreConfig.resultsBroadcastBusPower;
	}

	public EnergyConfig getiTLBPower() {
		return coreConfig.iTLBPower;
	}

	public EnergyConfig getdTLBPower() {
		return coreConfig.dTLBPower;
	}
	
	public CoreConfig getCoreConfig() {
		return coreConfig;
	}

	public void setComInterface(CommunicationInterface comInterface) {
		this.comInterface = comInterface;
		this.getExecEngine().getCoreMemorySystem().setComInterface(comInterface);
		for(Cache cache : getExecEngine().getCoreMemorySystem().getCoreCacheList()) {
			cache.setComInterface(comInterface);
		}
	}

	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		EnergyConfig totalPower = new EnergyConfig(0, 0);
		
		if(coreCyclesTaken == 0)
		{
			return totalPower;
		}
		
		outputFileWriter.write("\n\n");
		
		// --------- Core Memory System -------------------------
		EnergyConfig iCachePower =  this.execEngine.getCoreMemorySystem().getiCache().calculateAndPrintEnergy(outputFileWriter, componentName + ".iCache");
		totalPower.add(totalPower, iCachePower);
		EnergyConfig iTLBPower =  this.execEngine.getCoreMemorySystem().getiTLB().calculateAndPrintEnergy(outputFileWriter, componentName + ".iTLB");
		totalPower.add(totalPower, iTLBPower);
		
		EnergyConfig dCachePower =  this.execEngine.getCoreMemorySystem().getL1Cache().calculateAndPrintEnergy(outputFileWriter, componentName + ".dCache");
		totalPower.add(totalPower, dCachePower);
		
		EnergyConfig dTLBPower =  this.execEngine.getCoreMemorySystem().getdTLB().calculateAndPrintEnergy(outputFileWriter, componentName + ".dTLB");
		totalPower.add(totalPower, dTLBPower);
		
		// -------- Pipeline -----------------------------------
		EnergyConfig pipelinePower =  this.execEngine.calculateAndPrintEnergy(outputFileWriter, componentName + ".pipeline");
		totalPower.add(totalPower, pipelinePower);
		
		totalPower.printEnergyStats(outputFileWriter, componentName + ".total");
		
		return totalPower;
	}
}
