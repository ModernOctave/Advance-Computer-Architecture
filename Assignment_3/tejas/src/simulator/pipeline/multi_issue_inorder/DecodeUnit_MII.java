package pipeline.multi_issue_inorder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import config.EnergyConfig;
import config.SimulationConfig;
import pipeline.FunctionalUnitType;
import pipeline.OpTypeToFUTypeMapping;
import generic.Core;
import generic.Event;
import generic.EventQueue;
import generic.GlobalClock;
import generic.Instruction;
import generic.Operand;
import generic.OperationType;
import generic.PortType;
import generic.SimulationElement;

public class DecodeUnit_MII extends SimulationElement{
	
	Core core;
	MultiIssueInorderExecutionEngine containingExecutionEngine;
	StageLatch_MII ifIdLatch;
	StageLatch_MII idExLatch; 
	
	long numBranches;
	long numMispredictedBranches;
	long jumpCount;
	long targetMispredCount;
	long lastValidIPSeen;
	
	long numAccesses;
	
	long instCtr; //for debug
	
	public HashMap<Long, FunctionalUnitType> assignedFUType;
	
	public DecodeUnit_MII(Core core, MultiIssueInorderExecutionEngine execEngine)
	{
		/*
		 * numPorts and occupancy = -1 => infinite ports 
		 * Latency = 1 . 
		 * 
		*/
		super(PortType.Unlimited, -1, -1 , -1, -1);
		this.core = core;
		containingExecutionEngine = execEngine;
		ifIdLatch = execEngine.getIfIdLatch();
		idExLatch = execEngine.getIdExLatch();
		
		numBranches = 0;
		numMispredictedBranches = 0;
		lastValidIPSeen = -1;
		
		instCtr = 0;
		
		assignedFUType = new HashMap<Long, FunctionalUnitType>();
	}

	
	public void performDecode(MultiIssueInorderPipeline inorderPipeline){
		
		if(containingExecutionEngine.getMispredStall() > 0)
		{
			return;
		}
		
		containingExecutionEngine.getExecutionCore().clearPortUsage();
		
		Instruction ins = null;
				
		while(ifIdLatch.isEmpty() == false
				&& idExLatch.isFull() == false)
		{
			ins = ifIdLatch.peek(0);
			OperationType opType;
				
			if(ins!=null)
			{
				opType = ins.getOperationType();
				
				if(checkDataHazard(ins))	//Data Hazard Detected,Stall Pipeline
				{
					containingExecutionEngine.incrementDataHazardStall(1);
					break;
				}
				
				//check for structural hazards
				FunctionalUnitType[] FUType = OpTypeToFUTypeMapping.getFUType(ins.getOperationType());
				long FURequest = 0;
				
				//int functionality = 6;
				FunctionalUnitType assignedFUType = FunctionalUnitType.inValid;
				
				if(FUType[0] != FunctionalUnitType.inValid)
				{
					for(int i = 0; i < FUType.length; i++)
					{
						/*
						if(FUType[i].compareTo(FunctionalUnitType.FMA) != 0)
						{
							functionality = 0;		
						}
						else
						{
							switch(opType)
							{
								case floatALU:	{
													functionality = 0;
													break;
												}
								case floatMul:	{
													functionality = 1;
													break;
												}
								case floatVectorALU:	{
													functionality = 2;
													break;
												}
								case floatVectorMul:	{
													functionality = 3;
													break;
												}
								case FMA:	{
													functionality = 4;
													break;
												}
								case vectorFMA:	{
													functionality = 5;
													break;
												}
								default : 		{
													misc.Error.showErrorAndExit("requesting unsupported operation " + opType + " from an FMA");
												}
							}
						}
						*/
						
						FURequest = containingExecutionEngine.getExecutionCore().requestFU(FUType[i], 0);
						if(FURequest <= 0)
						{
							assignedFUType = FUType[i];
							break;
						}
					}
				
					if(FURequest > 0)
					{
						break;
					}
				}
				
				incrementNumDecodes(1);
				
				this.assignedFUType.put(ins.getSerialNo(), assignedFUType);
				
				//add destination register of ins to list of outstanding registers
				if(ins.getOperationType() == OperationType.load)
				{
					addToValueReadyArray(ins.getDestinationOperand(), Long.MAX_VALUE);
				}
				else
				{
					if(ins.getDestinationOperand() != null)
					{
						addToValueReadyArray(ins.getDestinationOperand(),
											GlobalClock.getCurrentTime()
												+ containingExecutionEngine.getExecutionCore().getFULatency(
														assignedFUType, 0));
					}
				}
				
				//update last valid IP seen
				if(ins.getCISCProgramCounter() != -1)
				{
					lastValidIPSeen = ins.getCISCProgramCounter();
				}
				
				//perform branch prediction
				if(ins.getOperationType()==OperationType.branch)
				{
					boolean prediction = containingExecutionEngine.getBranchPredictor().predict(
																		lastValidIPSeen,
																		ins.isBranchTaken());
					if(prediction != ins.isBranchTaken())
					{
						//Branch mispredicted
						//stall pipeline for appropriate cycles
						containingExecutionEngine.setMispredStall(core.getBranchMispredictionPenalty());
						numMispredictedBranches++;
					}
					this.containingExecutionEngine.getBranchPredictor().incrementNumAccesses(1);
	
					//Train Branch Predictor
					containingExecutionEngine.getBranchPredictor().Train(
							ins.getCISCProgramCounter(),
							ins.isBranchTaken(),
							prediction
							);
					this.containingExecutionEngine.getBranchPredictor().incrementNumAccesses(1);
					
					this.containingExecutionEngine.getBTB().GHRTrain(ins.isBranchTaken());
					
					numBranches++;
				}
				
				//jump operation
				if(ins.getOperationType() == OperationType.jump)
				{
					long actualTarget = ins.getBranchTargetAddress();					
					long predictedTarget = this.containingExecutionEngine.getBTB().BTBPredict(lastValidIPSeen);
					
					if(actualTarget != predictedTarget)
					{
						containingExecutionEngine.setMispredStall(core.getBranchMispredictionPenalty());
						targetMispredCount++;
					}
					
					this.containingExecutionEngine.getBTB().BTBTrain(lastValidIPSeen, actualTarget);
					jumpCount++;
				}
				
				if(ins.getSerialNo() != instCtr && ins.getOperationType() != OperationType.inValid)
				{
					misc.Error.showErrorAndExit("decode out of order!!");
				}
				instCtr++;
				
				//move ins to next stage
				idExLatch.add(ins, GlobalClock.getCurrentTime() + 1);
				ifIdLatch.poll();
				
				if(SimulationConfig.debugMode)
				{
					System.out.println("decoded : " + GlobalClock.getCurrentTime()/core.getStepSize() + "\n"  + ins + "\n");
				}
				
				//if a branch/jump instruction is issued, no more instructions to be issued this cycle
				if(opType == OperationType.branch
						|| opType == OperationType.jump)
				{
					break;
				}
			}
			else
			{
				break;
			}
		}
	}

	private boolean checkDataHazard(Instruction ins)
	{
		return !(isOperandReady(ins.getSourceOperand1()) &&
isOperandReady(ins.getSourceOperand2()));		
	}
	
	private boolean isOperandReady(Operand opnd)
	{
		if(opnd != null)
		{
			if(opnd.isIntegerRegisterOperand())
			{
				if(containingExecutionEngine.getValueReadyInteger()[(int)(opnd.getValue())]
																			> GlobalClock.getCurrentTime())
				{
					return false;
				}
			}

			else if(opnd.isFloatRegisterOperand())
			{
				if(containingExecutionEngine.getValueReadyFloat()[(int)(opnd.getValue())]
																			> GlobalClock.getCurrentTime())
				{
					return false;
				}
			}
			
			else if(opnd.isMemoryOperand())
			{
				return (isOperandReady(opnd.getMemoryLocationFirstOperand())
						&& isOperandReady(opnd.getMemoryLocationSecondOperand())); 
			}
		}
		
		return true;
	}
	
	private void addToValueReadyArray(Operand destOpnd, long timeWhenValueReady)
	{
		if(destOpnd.isIntegerRegisterOperand())
		{
			containingExecutionEngine.getValueReadyInteger()[(int)(destOpnd.getValue())]
																		 = timeWhenValueReady;
		}

		else if(destOpnd.isFloatRegisterOperand())
		{
			containingExecutionEngine.getValueReadyFloat()[(int)(destOpnd.getValue())]
																		 = timeWhenValueReady;
		}
	}


	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
		
	}

	public long getNumBranches() {
		return numBranches;
	}

	public long getNumMispredictedBranches() {
		return numMispredictedBranches;
	}
	
	void incrementNumDecodes(int incrementBy)
	{
		numAccesses += incrementBy;
	}

	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		EnergyConfig power = new EnergyConfig(containingExecutionEngine.getContainingCore().getDecodePower(), numAccesses);
		power.printEnergyStats(outputFileWriter, componentName);
		return power;
	}
}
