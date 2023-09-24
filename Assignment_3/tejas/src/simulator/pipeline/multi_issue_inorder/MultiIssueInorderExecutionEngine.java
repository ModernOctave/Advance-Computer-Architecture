package pipeline.multi_issue_inorder;

import java.io.FileWriter;
import java.io.IOException;
import config.EnergyConfig;
import config.SimulationConfig;
import memorysystem.CoreMemorySystem;
import pipeline.ExecutionEngine;
import generic.Core;
import generic.GenericCircularQueue;
import generic.GlobalClock;
import generic.Instruction;
import generic.Statistics;

public class MultiIssueInorderExecutionEngine extends ExecutionEngine{
	
	Core core;
	
	//private int numCycles;
	int issueWidth;
	private FetchUnitIn_MII fetchUnitIn;
	private DecodeUnit_MII decodeUnitIn;
	private ExecUnitIn_MII execUnitIn;
	private MemUnitIn_MII memUnitIn;
	private WriteBackUnitIn_MII writeBackUnitIn;
	private boolean executionComplete;
	private boolean fetchComplete;
	public InorderCoreMemorySystem_MII multiIssueInorderCoreMemorySystem;
	public long noOfMemRequests;
	public long noOfLd;
	public long noOfSt;
	private long memStall;
	private long dataHazardStall;
	public long l2memres;
	public long oldl2req;
	public long freshl2req;
	public long icachehit;
	public long l2memoutstanding;
	public long l2hits;
	public long l2accesses;
	private int numPipelines;

	
	long valueReadyInteger[];
	long valueReadyFloat[];
	
	private int mispredStall;	//to simulate pipeline flush during branch misprediction
	StageLatch_MII ifIdLatch,idExLatch,exMemLatch,memWbLatch,wbDoneLatch;
	
	public int noOfOutstandingLoads = 0;


	public MultiIssueInorderExecutionEngine(Core _core, int issueWidth){
		
		super(_core);
		
		this.core = _core;

		this.issueWidth = issueWidth;
		
		ifIdLatch = new StageLatch_MII(issueWidth);
		idExLatch = new StageLatch_MII(issueWidth);
		exMemLatch = new StageLatch_MII(issueWidth);
		memWbLatch = new StageLatch_MII(issueWidth);
		wbDoneLatch = new StageLatch_MII(issueWidth);
		
		this.setFetchUnitIn(new FetchUnitIn_MII(core,core.getEventQueue(),this));
		this.setDecodeUnitIn(new DecodeUnit_MII(core,this));
		this.setExecUnitIn(new ExecUnitIn_MII(core,this));
		this.setMemUnitIn(new MemUnitIn_MII(core,this));
		this.setWriteBackUnitIn(new WriteBackUnitIn_MII(core,this));
		this.executionComplete=false;
		memStall=0;
		dataHazardStall=0;
		
		l2memres=0;
		freshl2req=0;
		oldl2req=0;
		icachehit=0;
		l2memoutstanding=0;
		l2hits=0;
		l2accesses=0;
		
		valueReadyInteger = new long[core.getNIntegerArchitecturalRegisters()];
		valueReadyFloat = new long[core.getNVectorArchitecturalRegisters()];
	}

	public int getNumPipelines() {
		return numPipelines;
	}

	public void setNumPipelines(int numPipelines) {
		this.numPipelines = numPipelines;
	}

	public FetchUnitIn_MII getFetchUnitIn(){
		return this.fetchUnitIn;
	}
	public DecodeUnit_MII getDecodeUnitIn(){
		return this.decodeUnitIn;
	}
	public ExecUnitIn_MII getExecUnitIn(){
		return this.execUnitIn;
	}
	public MemUnitIn_MII getMemUnitIn(){
		return this.memUnitIn;
	}
	public WriteBackUnitIn_MII getWriteBackUnitIn(){
		return this.writeBackUnitIn;
	}
	public void setFetchUnitIn(FetchUnitIn_MII _fetchUnitIn){
		this.fetchUnitIn = _fetchUnitIn;
	}
	public void setDecodeUnitIn(DecodeUnit_MII _decodeUnitIn){
		this.decodeUnitIn = _decodeUnitIn;
	}
	public void setExecUnitIn(ExecUnitIn_MII _execUnitIn){
		this.execUnitIn = _execUnitIn;
	}
	public void setMemUnitIn(MemUnitIn_MII _memUnitIn){
		this.memUnitIn = _memUnitIn;
	}
	public void setWriteBackUnitIn(WriteBackUnitIn_MII _wbUnitIn){
		this.writeBackUnitIn = _wbUnitIn;
	}
	public void setExecutionComplete(boolean execComplete){
		this.executionComplete=execComplete;
		System.out.println("Core "+core.getCore_number()+" numCycles = " + GlobalClock.getCurrentTime());
		
		if (execComplete == true)
		{
			core.setCoreCyclesTaken(GlobalClock.getCurrentTime());
		}
	}
	public boolean getExecutionComplete(){
		return this.executionComplete;
	}
	public boolean getFetchComplete(){
		return this.fetchComplete;
	}
	
	public void setTimingStatistics()
	{
		System.out.println("Mem Stalls = "+getMemStall());
		System.out.println("Data Hazard Stalls = "+getDataHazardStall());
		System.out.println("Instruction Mem Stalls = "+getInstructionMemStall());

	}
	
	public void setPerCoreMemorySystemStatistics()
	{
//		Statistics.setNoOfTLBRequests(multiIssueInorderCoreMemorySystem.getTLBuffer().getTlbRequests(), core.getCore_number());
//		Statistics.setNoOfTLBHits(multiIssueInorderCoreMemorySystem.getTLBuffer().getTlbHits(), core.getCore_number());
//		Statistics.setNoOfTLBMisses(multiIssueInorderCoreMemorySystem.getTLBuffer().getTlbMisses(), core.getCore_number());
		
//		Statistics.setNoOfIRequests(multiIssueInorderCoreMemorySystem.getiCache().noOfRequests, core.getCore_number());
//		Statistics.setNoOfIHits(multiIssueInorderCoreMemorySystem.getiCache().hits, core.getCore_number());
//		Statistics.setNoOfIMisses(multiIssueInorderCoreMemorySystem.getiCache().misses, core.getCore_number());
		
		if(SimulationConfig.collectInsnWorkingSetInfo==true) {
			setInsWorkingSetStats();
		}
		
		if(SimulationConfig.collectDataWorkingSetInfo==true) {
			setDataWorkingSetStats();
		}
	}
	
	private void setInsWorkingSetStats() {
		Statistics.setMinInsWorkingSetSize(multiIssueInorderCoreMemorySystem.getiCache().minWorkingSetSize, 
			core.getCore_number());
		Statistics.setMaxInsWorkingSetSize(multiIssueInorderCoreMemorySystem.getiCache().maxWorkingSetSize, 
			core.getCore_number());
		Statistics.setTotalInsWorkingSetSize(multiIssueInorderCoreMemorySystem.getiCache().totalWorkingSetSize, 
			core.getCore_number());
		Statistics.setNumInsWorkingSetNoted(multiIssueInorderCoreMemorySystem.getiCache().numFlushesInWorkingSet, 
			core.getCore_number());
		Statistics.setNumInsWorkingSetHits(multiIssueInorderCoreMemorySystem.getiCache().numWorkingSetHits, 
			core.getCore_number());
		Statistics.setNumInsWorkingSetMisses(multiIssueInorderCoreMemorySystem.getiCache().numWorkingSetMisses, 
			core.getCore_number());
	}
	
	private void setDataWorkingSetStats() {
		Statistics.setMinDataWorkingSetSize(multiIssueInorderCoreMemorySystem.getL1Cache().minWorkingSetSize, 
			core.getCore_number());
		Statistics.setMaxDataWorkingSetSize(multiIssueInorderCoreMemorySystem.getL1Cache().maxWorkingSetSize, 
			core.getCore_number());
		Statistics.setTotalDataWorkingSetSize(multiIssueInorderCoreMemorySystem.getL1Cache().totalWorkingSetSize, 
			core.getCore_number());
		Statistics.setNumDataWorkingSetNoted(multiIssueInorderCoreMemorySystem.getL1Cache().numFlushesInWorkingSet, 
			core.getCore_number());
		Statistics.setNumDataWorkingSetHits(multiIssueInorderCoreMemorySystem.getL1Cache().numWorkingSetHits, 
			core.getCore_number());
		Statistics.setNumDataWorkingSetMisses(multiIssueInorderCoreMemorySystem.getL1Cache().numWorkingSetMisses, 
			core.getCore_number());
	}

	public void updateNoOfLd(int i) {
		this.noOfLd += i;
	}

	public void updateNoOfMemRequests(int i) {
		this.noOfMemRequests += i;
	}

	public void updateNoOfSt(int i) {
		this.noOfSt += i;
	}

	public long getMemStall() {
		return memStall;
	}

	public long getDataHazardStall() {
		return dataHazardStall;
	}

	public void incrementDataHazardStall(int i) {
		this.dataHazardStall += i;
		
	}

	public void incrementMemStall(int i) {
		this.memStall += i;
		
	}

	public long[] getValueReadyInteger() {
		return valueReadyInteger;
	}

	public long[] getValueReadyFloat() {
		return valueReadyFloat;
	}

	public int getMispredStall() {
		return mispredStall;
	}

	public void setMispredStall(int stallFetch) {
		if(this.mispredStall > stallFetch)
			return;
		else
			this.mispredStall = stallFetch;
	}

	public void decrementMispredStall(int stallFetch) {
		this.mispredStall -= stallFetch;
	}

	public int getIssueWidth() {
		return issueWidth;
	}

	public StageLatch_MII getIfIdLatch(){
		return this.ifIdLatch;
	}
	public StageLatch_MII getIdExLatch(){
		return this.idExLatch;
	}
	public StageLatch_MII getExMemLatch(){
		return this.exMemLatch;
	}
	public StageLatch_MII getMemWbLatch(){
		return this.memWbLatch;
	}
	public StageLatch_MII getWbDoneLatch(){
		return this.wbDoneLatch;
	}

	@Override
	public void setInputToPipeline(GenericCircularQueue<Instruction>[] inpList) {
		
		fetchUnitIn.setInputToPipeline(inpList[0]);
		
	}

	public void setCoreMemorySystem(CoreMemorySystem coreMemorySystem) {
		this.coreMemorySystem = coreMemorySystem;
		this.multiIssueInorderCoreMemorySystem = (InorderCoreMemorySystem_MII)coreMemorySystem;
	}
	
	/*
	 * debug helper functions
	 */
//	public void dumpAllLatches()
//	{
//		System.out.println("ifid stall = " + ifIdLatch[0].getStallCount());
//		System.out.println(ifIdLatch[0].getInstruction());
//		System.out.println("idex stall = " + idExLatch[0].getStallCount());
//		System.out.println(idExLatch[0].getInstruction());		
//		System.out.println("exMem stall = " + exMemLatch[0].getStallCount());
//		System.out.println("exmem memdone = " + exMemLatch[0].getMemDone());
//		System.out.println(exMemLatch[0].getInstruction());
//		System.out.println("memWb stall = " + memWbLatch[0].getStallCount());
//		System.out.println(memWbLatch[0].getInstruction());
//	}	
	
	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		EnergyConfig totalPower = new EnergyConfig(0, 0);
		
		EnergyConfig bPredPower =  getBranchPredictor().calculateAndPrintEnergy(outputFileWriter, componentName + ".bPred");
		totalPower.add(totalPower, bPredPower);
		
		EnergyConfig decodePower =  getDecodeUnitIn().calculateAndPrintEnergy(outputFileWriter, componentName + ".decode");
		totalPower.add(totalPower, decodePower);
		
		EnergyConfig regFilePower =  getWriteBackUnitIn().calculateAndPrintEnergy(outputFileWriter, componentName + ".regFile");
		totalPower.add(totalPower, regFilePower);
		
		EnergyConfig fuPower =  getExecutionCore().calculateAndPrintEnergy(outputFileWriter, componentName + ".FuncUnit");
		totalPower.add(totalPower, fuPower);
		
		EnergyConfig resultsBroadcastBusPower =  getExecUnitIn().calculateAndPrintEnergy(outputFileWriter, componentName + ".resultsBroadcastBus");
		totalPower.add(totalPower, resultsBroadcastBusPower);
		
		totalPower.printEnergyStats(outputFileWriter, componentName + ".total");
		
		return totalPower;
	}

	@Override
	public long getNumberOfBranches() {
		return decodeUnitIn.numBranches;
	}

	@Override
	public long getNumberOfMispredictedBranches() {
		return decodeUnitIn.numMispredictedBranches;
	}

	@Override
	public void setNumberOfBranches(long numBranches) {
		decodeUnitIn.numBranches = numBranches;
	}

	@Override
	public void setNumberOfMispredictedBranches(long numMispredictedBranches) {
		decodeUnitIn.numMispredictedBranches = numMispredictedBranches;
	}

	@Override
	public long getNumberOfJumps() {
		return decodeUnitIn.jumpCount;
	}

	@Override
	public long getNumberOfMispredictedTargets() {
		return decodeUnitIn.targetMispredCount;
	}
}
