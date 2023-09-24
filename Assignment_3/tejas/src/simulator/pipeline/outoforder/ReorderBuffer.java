package pipeline.outoforder;

import generic.Core;
import generic.Event;
import generic.EventQueue;
import generic.GlobalClock;
import generic.Instruction;
import generic.OperationType;
import generic.PinPointsProcessing;
import generic.PortType;
import generic.RequestType;
import generic.SimulationElement;
import generic.Statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import main.CustomObjectPool;
import pipeline.branchpredictor.TAGESCL.TAGESCL;
import config.EmulatorConfig;
import config.EnergyConfig;
import config.SimulationConfig;
import config.BranchPredictorConfig.BP;

public class ReorderBuffer extends SimulationElement{
	
	private Core core;	
	OutOrderExecutionEngine execEngine;
	int retireWidth;
	
	ReorderBufferEntry[] ROB;
	int MaxROBSize;	
	int head;
	int tail;
	
	int numCPUIDsInBuffer;
	int numMFencesInBuffer;
	int numSFencesInBuffer;
	int numLFencesInBuffer;
	
	int stall1Count;
	int stall2Count;
	int stall3Count;
	int stall4Count;
	int stall5Count;
	int stall6Count;
	long branchCount;
	long mispredCount;
	public long predicateCount;
	public long predicateMispredCount;
	long jumpCount;
	long targetMispredCount;
	long lastValidIPSeen;
	
	long numAccesses;
	
	public BufferedWriter bw;
	
	public ReorderBuffer(Core _core, OutOrderExecutionEngine execEngine)
	{
		super(PortType.Unlimited, -1, -1, -1, -1);
		
		core = _core;
		this.execEngine = execEngine;
		retireWidth = core.getRetireWidth();
		
		MaxROBSize = core.getReorderBufferSize();
		head = -1;
		tail = -1;
		ROB = new ReorderBufferEntry[MaxROBSize];		
		for(int i = 0; i < MaxROBSize; i++)
		{
			ROB[i] = new ReorderBufferEntry(i, execEngine);
		}
		
		numCPUIDsInBuffer = 0;
		numMFencesInBuffer = 0;
		numSFencesInBuffer = 0;
		numLFencesInBuffer = 0;
		
		stall1Count = 0;
		stall2Count = 0;
		stall3Count = 0;
		stall4Count = 0;
		stall5Count = 0;
		stall6Count = 0;
		mispredCount = 0;
		branchCount = 0;
		lastValidIPSeen = -1;
		
		if(EmulatorConfig.storeExecutionTraceInAFile == true)
		{
	        try
			{
	        	File f = new File(EmulatorConfig.basenameForTraceFiles + "_" + core.getCore_number() + ".gz");
				if(f.exists() && !f.isDirectory())
				{
					misc.Error.showErrorAndExit("Trace file already present : " + EmulatorConfig.basenameForTraceFiles + " !!" + 
							"\nKindly rename the trace file and start collecting trace again.");
				}
				bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(EmulatorConfig.basenameForTraceFiles + "_" + core.getCore_number() + ".gz"))));
			}
			catch (IOException e) {
	            e.printStackTrace();
            	misc.Error.showErrorAndExit(e.getMessage());
	        }
		}
	}
	
	//creates a  new ROB entry, initialises it, and returns it
	//check if there is space in ROB before calling this function
	public ReorderBufferEntry addInstructionToROB(Instruction newInstruction, int threadID)
	{
		if(!isFull())
		{
			tail = (tail + 1)%MaxROBSize;
			if(head == -1)
			{
				head = 0;
			}
			ReorderBufferEntry newReorderBufferEntry = ROB[tail];
			
			if(newReorderBufferEntry.isValid() == true)
			{
				System.out.println("new rob entry is alread valid");
			}
			
			newReorderBufferEntry.setInstruction(newInstruction);
			newReorderBufferEntry.setThreadID(threadID);
			newReorderBufferEntry.setOperand1PhyReg1(-1);
			newReorderBufferEntry.setOperand1PhyReg2(-1);
			newReorderBufferEntry.setOperand2PhyReg1(-1);
			newReorderBufferEntry.setOperand2PhyReg2(-1);
			newReorderBufferEntry.setPhysicalDestinationRegister(-1);
			newReorderBufferEntry.setRenameDone(false);
			newReorderBufferEntry.setOperand11Available(false);
			newReorderBufferEntry.setOperand12Available(false);
			newReorderBufferEntry.setOperand1Available(false);
			newReorderBufferEntry.setOperand21Available(false);
			newReorderBufferEntry.setOperand22Available(false);
			newReorderBufferEntry.setOperand2Available(false);
			newReorderBufferEntry.setIssued(false);
			newReorderBufferEntry.setFUInstance(-1);
			newReorderBufferEntry.setExecuted(false);
			newReorderBufferEntry.setWriteBackDone1(false);
			newReorderBufferEntry.setWriteBackDone2(false);
			newReorderBufferEntry.setAssociatedIWEntry(null);
			
			newReorderBufferEntry.setValid(true);
			
			incrementNumAccesses(1);
			
			return newReorderBufferEntry;
		}
		
		return null;
	}
	
	public void performCommits()
	{	
		if(execEngine.isToStall1())
		{
			stall1Count++;
		}
		if(execEngine.isToStall2())
		{
			stall2Count++;
		}
		if(execEngine.isToStall3())
		{
			stall3Count++;
		}
		if(execEngine.isToStall4())
		{
			stall4Count++;
		}
		if(execEngine.isToStall5())
		{
			stall5Count++;
		}
		if(execEngine.isToStall6())
		{
			stall6Count++;
		}
		
		if(execEngine.isToStall5() == true /*pipeline stalled due to branch mis-prediction*/)
		{
			return;
		}
		
		boolean anyMispredictedBranch = false;
		
		for(int no_insts = 0; no_insts < retireWidth; no_insts++)
		{
			if(head == -1)
			{
				//ROB empty .. does not mean execution has completed
				return;
			}
			
			ReorderBufferEntry first = ROB[head];
			Instruction firstInstruction = first.getInstruction();
			OperationType firstOpType = firstInstruction.getOperationType();								
			
			if(first.isWriteBackDone() == true)
			{
				//has a thread finished?
				if(firstOpType==OperationType.inValid)
				{
					this.core.currentThreads--;
					
					if(this.core.currentThreads < 0)
					{
						this.core.currentThreads=0;
						System.out.println("num threads < 0");
					}
					
					if(this.core.currentThreads == 0)
					{   //set exec complete only if there are no other thread already 
														  //assigned to this pipeline	
						execEngine.setExecutionComplete(true);
					}
					
					// if(SimulationConfig.pinpointsSimulation == false)
					//{
						setTimingStatistics();
						setPerCoreMemorySystemStatistics();
					//}
					//else
					//{
					//	PinPointsProcessing.processEndOfSlice();
					//}
				}
				
				//if store, and if store not yet validated
				if(firstOpType == OperationType.store && !first.getLsqEntry().isValid())
				{
					break;
				}
				
				//update last valid IP seen
				if(firstInstruction.getCISCProgramCounter() != -1)
				{
					lastValidIPSeen = firstInstruction.getCISCProgramCounter();
				}
				
				//branch prediction
				if(firstOpType == OperationType.branch)
				{
					//perform prediction
					boolean prediction;
					prediction = execEngine.getBranchPredictor().predict(
							lastValidIPSeen,
							first.getInstruction().isBranchTaken());
					
					if(prediction != first.getInstruction().isBranchTaken())
					{
						if(SimulationConfig.debugMode)
						{
							System.out.println("branch mispredicted : " + firstInstruction.getSerialNo());
						}
						
						anyMispredictedBranch = true;
						mispredCount++;
					}
					
					//train predictor
					if(core.getCoreConfig().branchPredictor.predictorMode != BP.TAGE_SC_L)
					{
						execEngine.getBranchPredictor().Train(
								lastValidIPSeen,
								firstInstruction.isBranchTaken(),
								prediction
								);
					}
					else
					{
						((TAGESCL)execEngine.getBranchPredictor()).Train(
								lastValidIPSeen,
								9, //OPTYPE_JMP_DIRECT_COND,
								firstInstruction.isBranchTaken(),
								prediction,
								firstInstruction.getBranchTargetAddress()
								);
					}
					
					this.execEngine.getBTB().GHRTrain(firstInstruction.isBranchTaken());
					this.execEngine.getBranchPredictor().incrementNumAccesses(2);
					
					branchCount++;
				}
				
				//jump operation
				if(firstOpType == OperationType.jump)
				{
					long actualTarget = first.getInstruction().getBranchTargetAddress();					
					long predictedTarget = this.execEngine.getBTB().BTBPredict(lastValidIPSeen);
					
					if(actualTarget != predictedTarget)
					{
						if(SimulationConfig.debugMode)
						{
							System.out.println("jump target mispredicted : " + firstInstruction.getSerialNo());
						}
						
						anyMispredictedBranch = true;
						targetMispredCount++;
					}
				
					this.execEngine.getBTB().BTBTrain(lastValidIPSeen, actualTarget);
					
					if(core.getCoreConfig().branchPredictor.predictorMode == BP.TAGE_SC_L)
					{
						((TAGESCL)execEngine.getBranchPredictor()).Train(
								lastValidIPSeen,
								4, //OPTYPE_JMP_DIRECT_UNCOND,
								true,
								true,
								firstInstruction.getBranchTargetAddress()
								);
					}
					jumpCount++;
				}
				
				//predicate prediction
				if(firstInstruction.isPredicate())
				{
					//perform prediction
					boolean prediction = execEngine.getBranchPredictor().predict(
																		lastValidIPSeen,
																		!first.getInstruction().isPredicateAndNotExecuted());
					if(prediction != !first.getInstruction().isPredicateAndNotExecuted())
					{	
						if(SimulationConfig.debugMode)
						{
							System.out.println("predicate mispredicted : " + firstInstruction.getSerialNo());
						}
						
						anyMispredictedBranch = true;
						predicateMispredCount++;
					}
				
					//train predictor
					if(core.getCoreConfig().branchPredictor.predictorMode != BP.TAGE_SC_L)
					{
						execEngine.getBranchPredictor().Train(
								lastValidIPSeen,
								!firstInstruction.isPredicateAndNotExecuted(),
								prediction
								);
					}
					else
					{
						((TAGESCL)execEngine.getBranchPredictor()).Train(
								lastValidIPSeen,
								9, //OPTYPE_JMP_DIRECT_COND,
								!firstInstruction.isPredicateAndNotExecuted(),
								prediction,
								firstInstruction.getBranchTargetAddress()
								);
					}
					predicateCount++;
				}
				
				//Signal LSQ for committing the Instruction at the queue head
				if(firstOpType == OperationType.load || firstOpType == OperationType.store)
				{
					if (!first.getLsqEntry().isValid())
					{
						misc.Error.showErrorAndExit("The committed entry is not valid");
					}
					
					execEngine.getCoreMemorySystem().issueLSQCommit(first);
				}
				
				if(firstInstruction.getOperationType() == OperationType.cpuid)
				{
					decrementNumCPUIDsInBuffer();
				}
				
				if(firstInstruction.getOperationType() == OperationType.mfence)
				{
					decrementNumMFencesInBuffer();
				}
				
				if(firstInstruction.getOperationType() == OperationType.sfence)
				{
					decrementNumSFencesInBuffer();
				}
				
				if(firstInstruction.getOperationType() == OperationType.lfence)
				{
					decrementNumLFencesInBuffer();
				}
				
				if(EmulatorConfig.storeExecutionTraceInAFile == true)
				{
					writeInstructionToFile(firstInstruction);
				}
				
				//free ROB entry
				retireInstructionAtHead();
				
				//increment number of instructions executed
				core.incrementNoOfInstructionsExecuted();
				if(core.getNoOfInstructionsExecuted()%1000000==0)
				{
					System.out.println(core.getNoOfInstructionsExecuted()/1000000 + " million done on " + core.getCore_number());
				}

				//debug print
				if(SimulationConfig.debugMode)
				{
					System.out.println("committed : " + GlobalClock.getCurrentTime()/core.getStepSize() + " : " + firstInstruction);
//						System.out.println(first.getOperand1PhyReg1()
//								+ " : " + first.getOperand2PhyReg1()
//								+ " : " + first.getPhysicalDestinationRegister());
				}
				
				//return instruction to pool
				returnInstructionToPool(firstInstruction);
			}
			else
			{
				//commits must be in order
				break;
			}
		}
		
		if(anyMispredictedBranch)
		{
			handleBranchMisprediction();
		}
	}
	
	void retireInstructionAtHead()
	{
		ROB[head].setValid(false);
		ROB[head].setInstruction(null);
		if(head == tail)
		{
			head = -1;
			tail = -1;
		}
		else
		{
			head = (head+1)%MaxROBSize;
		}
		incrementNumAccesses(1);
	}
	
	void handleBranchMisprediction()
	{
		if(SimulationConfig.debugMode)
		{
			System.out.println("branch mispredicted");
		}
		
		if(core.getBranchMispredictionPenalty() <= 0)
		{
			//if branch mispredictions have no penalty
			return;
		}
		
		//impose branch mis-prediction penalty
		execEngine.setToStall5(true);
		
		//set-up event that signals end of misprediction penalty period
		core.getEventQueue().addEvent(
				new MispredictionPenaltyCompleteEvent(
						GlobalClock.getCurrentTime() + core.getBranchMispredictionPenalty() * core.getStepSize(),
						null,
						this,
						RequestType.MISPRED_PENALTY_COMPLETE));
		
	}
	
	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
		
		if(event.getRequestType() == RequestType.MISPRED_PENALTY_COMPLETE)
		{
			completeMispredictionPenalty();
		}
		
	}
	
	void completeMispredictionPenalty()
	{
		execEngine.setToStall5(false);
	}
	
	void returnInstructionToPool(Instruction instruction)
	{
		try {
			CustomObjectPool.getInstructionPool().returnObject(instruction);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void writeInstructionToFile(Instruction instruction) {
        
		try {
		 
            Instruction tmp = instruction;
            bw.write(tmp.getCISCProgramCounter()+" "+tmp.getOperationType());
            if(tmp.getSourceOperand1()!=null) {
                bw.write(" "+tmp.getSourceOperand1()+" "+tmp.getSourceOperand1().getMemoryLocationFirstOperand()+" "+
                tmp.getSourceOperand1().getMemoryLocationSecondOperand());
            }
            else bw.write(" null null null");
            if(tmp.getSourceOperand2()!=null) {
                bw.write(" "+tmp.getSourceOperand2()+" "+tmp.getSourceOperand2().getMemoryLocationFirstOperand()+" "+
                tmp.getSourceOperand2().getMemoryLocationSecondOperand());
            }
            else bw.write(" null null null");
            if(tmp.getDestinationOperand()!=null) {
                bw.write(" "+tmp.getDestinationOperand()+" "+tmp.getDestinationOperand().getMemoryLocationFirstOperand()+" "+
                tmp.getDestinationOperand().getMemoryLocationSecondOperand());
            }
            else bw.write(" null null null");
            bw.write(" "+tmp.getSourceOperand1MemValue()+" "+
            tmp.getSourceOperand2MemValue()+" "+tmp.getDestinationOperandMemValue()+" "+
            tmp.getBranchTargetAddress()+" "+tmp.isBranchTaken()+" "+tmp.getThreadID()+" "+tmp.getSerialNo()+" "+tmp.isPredicate()+" "+tmp.isPredicateAndNotExecuted()+"\n");
        }
		catch(Exception e)
		{
			e.printStackTrace();
        	misc.Error.showErrorAndExit(e.getMessage());
		}        
    }


	
	//debug helper - print contents of ROB
	public void dump()
	{
		ReorderBufferEntry e;
		
		System.out.println();
		System.out.println();
		System.out.println("----------ROB dump---------");
		
		if(head == -1)
		{
			return;
		}
		
		int i = head;
		while(true)
		{
			e = ROB[i];
			System.out.println(e.getOperand1PhyReg1() + " ; " + e.getOperand1PhyReg2() + " ; "
					+ e.getOperand2PhyReg1() + " ; "+ e.getOperand2PhyReg2() + " ; " + 
					e.getPhysicalDestinationRegister() + " ; " + 
					e.getIssued() + " ; " + 
					e.getFUInstance() + " ; " + e.getExecuted());
			if(e.getAssociatedIWEntry() != null)
			{
				System.out.println(e.isOperand1Available()
						 + " ; " + e.isOperand2Available());
			}
			System.out.println(e.getInstruction().toString());
			
			if(i == tail)
			{
				break;
			}
			i = (i+1)%MaxROBSize;
		}
		System.out.println();
	}
	
	public void setTimingStatistics()
	{
		core.setCoreCyclesTaken(GlobalClock.getCurrentTime());
	}
	
	public void setPerCoreMemorySystemStatistics()
	{
		if(SimulationConfig.collectInsnWorkingSetInfo==true) {
			setInsWorkingSetStats();
		}
		
		if(SimulationConfig.collectDataWorkingSetInfo==true) {
			setDataWorkingSetStats();
		}
		
	}
	
	private void setInsWorkingSetStats() {
		Statistics.setMinInsWorkingSetSize(execEngine.getCoreMemorySystem().getiCache().minWorkingSetSize, 
			core.getCore_number());
		Statistics.setMaxInsWorkingSetSize(execEngine.getCoreMemorySystem().getiCache().maxWorkingSetSize, 
			core.getCore_number());
		Statistics.setTotalInsWorkingSetSize(execEngine.getCoreMemorySystem().getiCache().totalWorkingSetSize, 
			core.getCore_number());
		Statistics.setNumInsWorkingSetNoted(execEngine.getCoreMemorySystem().getiCache().numFlushesInWorkingSet, 
			core.getCore_number());
		Statistics.setNumInsWorkingSetHits(execEngine.getCoreMemorySystem().getiCache().numWorkingSetHits, 
			core.getCore_number());
		Statistics.setNumInsWorkingSetMisses(execEngine.getCoreMemorySystem().getiCache().numWorkingSetMisses, 
			core.getCore_number());
	}
	
	private void setDataWorkingSetStats() {
		Statistics.setMinDataWorkingSetSize(execEngine.getCoreMemorySystem().getL1Cache().minWorkingSetSize, 
			core.getCore_number());
		Statistics.setMaxDataWorkingSetSize(execEngine.getCoreMemorySystem().getL1Cache().maxWorkingSetSize, 
			core.getCore_number());
		Statistics.setTotalDataWorkingSetSize(execEngine.getCoreMemorySystem().getL1Cache().totalWorkingSetSize, 
			core.getCore_number());
		Statistics.setNumDataWorkingSetNoted(execEngine.getCoreMemorySystem().getL1Cache().numFlushesInWorkingSet, 
			core.getCore_number());
		Statistics.setNumDataWorkingSetHits(execEngine.getCoreMemorySystem().getL1Cache().numWorkingSetHits, 
			core.getCore_number());
		Statistics.setNumDataWorkingSetMisses(execEngine.getCoreMemorySystem().getL1Cache().numWorkingSetMisses, 
			core.getCore_number());
	}

	public boolean isFull()
	{
		if((tail - head) == MaxROBSize - 1)
		{
			return true;
		}
		if((tail - head) == -1)
		{
			return true;
		}
		return false;
	}
	
	public ReorderBufferEntry[] getROB()
	{
		return ROB;
	}
	
	public int indexOf(ReorderBufferEntry reorderBufferEntry)
	{
		if(reorderBufferEntry.pos - head >= 0)
		{
			return (reorderBufferEntry.pos - head);
		}
		else
		{
			return (reorderBufferEntry.pos - head + MaxROBSize);
		}
	}
	
	public int getNumCPUIDsInBuffer() {
		return numCPUIDsInBuffer;
	}

	public void incrementNumCPUIDsInBuffer() {
		this.numCPUIDsInBuffer++;
	}

	public void decrementNumCPUIDsInBuffer() {
		this.numCPUIDsInBuffer--;
	}

	public int getNumMFencesInBuffer() {
		return numMFencesInBuffer;
	}

	public void incrementNumMFencesInBuffer() {
		this.numMFencesInBuffer++;
	}

	public void decrementNumMFencesInBuffer() {
		this.numMFencesInBuffer--;
	}

	public int getNumSFencesInBuffer() {
		return numSFencesInBuffer;
	}

	public void incrementNumSFencesInBuffer() {
		this.numSFencesInBuffer++;
	}

	public void decrementNumSFencesInBuffer() {
		this.numSFencesInBuffer--;
	}

	public int getNumLFencesInBuffer() {
		return numLFencesInBuffer;
	}

	public void incrementNumLFencesInBuffer() {
		this.numLFencesInBuffer++;
	}

	public void decrementNumLFencesInBuffer() {
		this.numLFencesInBuffer--;
	}

	public int getMaxROBSize()
	{
		return MaxROBSize;
	}

	public int getStall1Count() {
		return stall1Count;
	}

	public int getStall2Count() {
		return stall2Count;
	}

	public int getStall3Count() {
		return stall3Count;
	}

	public int getStall4Count() {
		return stall4Count;
	}

	public int getStall5Count() {
		return stall5Count;
	}

	public int getStall6Count() {
		return stall6Count;
	}

	public long getBranchCount() {
		return branchCount;
	}

	public long getMispredCount() {
		return mispredCount;
	}
	
	void incrementNumAccesses(int incrementBy)
	{
		numAccesses += incrementBy;
	}
	
	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		EnergyConfig power = new EnergyConfig(core.getRobPower(), numAccesses);
		power.printEnergyStats(outputFileWriter, componentName);
		return power;
	}

}