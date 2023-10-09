package pipeline;

import java.io.FileWriter;
import java.io.IOException;

import config.CoreConfig;
import config.EnergyConfig;
import config.SystemConfig;
import config.BranchPredictorConfig.BP;
import pipeline.branchpredictor.AlwaysNotTaken;
import pipeline.branchpredictor.AlwaysTaken;
import pipeline.branchpredictor.BimodalPredictor;
import pipeline.branchpredictor.BranchPredictor;
import pipeline.branchpredictor.GAgpredictor;
import pipeline.branchpredictor.GApPredictor;
import pipeline.branchpredictor.GShare;
import pipeline.branchpredictor.NoPredictor;
import pipeline.branchpredictor.PAgPredictor;
import pipeline.branchpredictor.PApPredictor;
import pipeline.branchpredictor.PerfectPredictor;
import pipeline.branchpredictor.TournamentPredictor;
import pipeline.branchpredictor.TAGE;
import pipeline.branchpredictor.TAGESCL.TAGESCL;
import pipeline.branchpredictor.BTB;
import generic.Core;
import generic.GenericCircularQueue;
import generic.Instruction;
import memorysystem.CoreMemorySystem;

public abstract class ExecutionEngine {
	
	protected Core containingCore;
	protected boolean executionComplete;
	protected boolean executionBegun;
	protected CoreMemorySystem coreMemorySystem;
	protected ExecutionCore executionCore;

	private long instructionMemStall;
	
	private BranchPredictor branchPredictor;
	private BTB BTB;
	
	public ExecutionEngine(Core containingCore)
	{
		this.containingCore = containingCore;
		executionComplete = false;
		executionBegun = false;
		coreMemorySystem = null;
		instructionMemStall=0;
		
		CoreConfig coreConfig = SystemConfig.core[containingCore.getCore_number()];
		
		executionCore = new ExecutionCore(containingCore);
		
		if(coreConfig.branchPredictor.predictorMode == BP.NoPredictor)
			this.branchPredictor = new NoPredictor(this);
		else if(coreConfig.branchPredictor.predictorMode == BP.PerfectPredictor)
			this.branchPredictor = new PerfectPredictor(this);
		else if(coreConfig.branchPredictor.predictorMode == BP.AlwaysTaken)
			this.branchPredictor = new AlwaysTaken(this);
		else if(coreConfig.branchPredictor.predictorMode == BP.AlwaysNotTaken)
			this.branchPredictor = new AlwaysNotTaken(this);
		else if(coreConfig.branchPredictor.predictorMode == BP.Tournament)
			this.branchPredictor = new TournamentPredictor(this, coreConfig.branchPredictor.PCBits, 
					coreConfig.branchPredictor.BHRsize, 
					coreConfig.branchPredictor.saturating_bits);
		else if(coreConfig.branchPredictor.predictorMode == BP.Bimodal)
			this.branchPredictor = new BimodalPredictor(this, coreConfig.branchPredictor.PCBits,
					coreConfig.branchPredictor.saturating_bits);
		else if(coreConfig.branchPredictor.predictorMode == BP.GShare)
			this.branchPredictor = new GShare(this, coreConfig.branchPredictor.BHRsize, 
					coreConfig.branchPredictor.saturating_bits);
		else if(coreConfig.branchPredictor.predictorMode == BP.GAg)
			this.branchPredictor = new GAgpredictor(this, coreConfig.branchPredictor.BHRsize, 
					coreConfig.branchPredictor.saturating_bits);
		else if(coreConfig.branchPredictor.predictorMode == BP.GAp)
			this.branchPredictor = new GApPredictor(this, coreConfig.branchPredictor.BHRsize, 
					coreConfig.branchPredictor.PCBits, 
					coreConfig.branchPredictor.saturating_bits);
		else if(coreConfig.branchPredictor.predictorMode == BP.PAg)
			this.branchPredictor = new PAgPredictor(this, coreConfig.branchPredictor.PCBits, 
					coreConfig.branchPredictor.BHRsize, 
					coreConfig.branchPredictor.saturating_bits);
		else if(coreConfig.branchPredictor.predictorMode == BP.PAp)
			this.branchPredictor = new PApPredictor(this, coreConfig.branchPredictor.PCBits, 
					coreConfig.branchPredictor.BHRsize, 
					coreConfig.branchPredictor.saturating_bits);
		else if(coreConfig.branchPredictor.predictorMode == BP.TAGE)
                        this.branchPredictor = new TAGE(this,
                                        coreConfig.branchPredictor.PCBits,
                                        coreConfig.branchPredictor.saturating_bits);
		else if(coreConfig.branchPredictor.predictorMode == BP.TAGE_SC_L)
                                        	this.branchPredictor = new TAGESCL(this);

		BTB = new BTB(coreConfig.branchPredictor.PCBits, coreConfig.branchPredictor.BHRsize);
	}
	
	public abstract void setInputToPipeline(GenericCircularQueue<Instruction>[] inpList);

	public void setExecutionComplete(boolean executionComplete) {
		this.executionComplete = executionComplete;
	}

	public ExecutionCore getExecutionCore() {
		return executionCore;
	}

	public boolean isExecutionComplete() {
		return executionComplete;
	}

	public boolean isExecutionBegun() {
		return executionBegun;
	}

	public void setExecutionBegun(boolean executionBegun) {
		this.executionBegun = executionBegun;
	}

	public void setCoreMemorySystem(CoreMemorySystem coreMemorySystem) {
		this.coreMemorySystem = coreMemorySystem;
	}

	public CoreMemorySystem getCoreMemorySystem() {
		return coreMemorySystem;
	}

	public void incrementInstructionMemStall(int i) {
		this.instructionMemStall += i;
		
	}

	public long getInstructionMemStall() {
		return instructionMemStall;
	}

	public Core getContainingCore() {
		return containingCore;
	}

	public BranchPredictor getBranchPredictor() {
		return branchPredictor;
	}
	
	public BTB getBTB() {
		return BTB;
	}

	public abstract long getNumberOfBranches();	
	public abstract long getNumberOfMispredictedBranches();
	public abstract void setNumberOfBranches(long numBranches);	
	public abstract void setNumberOfMispredictedBranches(long numMispredictedBranches);
	public abstract long getNumberOfJumps();	
	public abstract long getNumberOfMispredictedTargets();
	
	public abstract EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException;
	
}
