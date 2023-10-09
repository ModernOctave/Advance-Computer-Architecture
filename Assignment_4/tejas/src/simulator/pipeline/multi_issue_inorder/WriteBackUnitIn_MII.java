package pipeline.multi_issue_inorder;

import java.io.FileWriter;
import java.io.IOException;

import pipeline.outoforder.OutOrderExecutionEngine;
import generic.Instruction;

import config.EnergyConfig;
import config.SimulationConfig;
import main.CustomObjectPool;
import generic.Core;
import generic.Event;
import generic.EventQueue;
import generic.GlobalClock;
import generic.Operand;
import generic.OperandType;
import generic.OperationType;
import generic.PinPointsProcessing;
import generic.PortType;
import generic.RequestType;
import generic.SimulationElement;
import generic.Statistics;

public class WriteBackUnitIn_MII extends SimulationElement{
	
	Core core;
	MultiIssueInorderExecutionEngine containingExecutionEngine;
	StageLatch_MII memWbLatch;
	
	long instCtr; //for debug
	
	long numIntRegFileAccesses;
	long numFloatRegFileAccesses;
	
	public WriteBackUnitIn_MII(Core core, MultiIssueInorderExecutionEngine execEngine)
	{
		super(PortType.Unlimited, -1, -1 , -1, -1);
		this.core = core;
		containingExecutionEngine = execEngine;
		memWbLatch = execEngine.getMemWbLatch();
		
		instCtr = 0;
	}
	
	public void performWriteBack(MultiIssueInorderPipeline inorderPipeline)
	{
		if(containingExecutionEngine.getMispredStall() > 0)
		{
			return;
		}
		
		Instruction ins = null;
		
		while(memWbLatch.isEmpty() == false)
		{
			ins = memWbLatch.peek(0);
			if(ins != null)
			{
				//check if simulation complete
				if(ins.getOperationType()==OperationType.inValid)
				{
					this.core.currentThreads--;
					
					if(this.core.currentThreads == 0){   //set exec complete only if there are n other thread already 
														  //assigned to this pipeline	
						containingExecutionEngine.setExecutionComplete(true);
						// if(SimulationConfig.pinpointsSimulation == false)
						//{					
							containingExecutionEngine.setTimingStatistics();			
							containingExecutionEngine.setPerCoreMemorySystemStatistics();
						//}
						//else
						//{
						//	PinPointsProcessing.processEndOfSlice();
						//}
					}
				}
				else
				{
					if(core.getNoOfInstructionsExecuted()%1000000==0)
					{
						System.out.println(core.getNoOfInstructionsExecuted()/1000000 + " million done" + " by core "+core.getCore_number() 
								+ " global clock cycle " + GlobalClock.getCurrentTime());
					}
					core.incrementNoOfInstructionsExecuted();
				}
				
				//increment register file accesses for power statistics
				
				//operand fetch
				incrementNumRegFileAccesses(ins.getSourceOperand1(), 1);
				incrementNumRegFileAccesses(ins.getSourceOperand2(), 1);
				
				//write-back
				incrementNumRegFileAccesses(ins.getDestinationOperand(), 1);
								
				if(ins.getSerialNo() != instCtr && ins.getOperationType() != OperationType.inValid)
				{
					misc.Error.showErrorAndExit("wb out of order!!");
				}
				instCtr++;	
				
				if(SimulationConfig.debugMode)
				{
					System.out.println("write back : " + GlobalClock.getCurrentTime()/core.getStepSize() + "\n"  + ins + "\n");
				}
				
				memWbLatch.poll();				
				try
				{
					CustomObjectPool.getInstructionPool().returnObject(ins);
					core.numReturns++;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			else
			{
				break;
			}
		}
	}

	@Override
	public void handleEvent(EventQueue eventQ, Event event) {

	}
	
	void incrementNumRegFileAccesses(Operand operand, int incrementBy)
	{
		if(operand == null)
		{
			return;
		}
		
		if(operand.isIntegerRegisterOperand())
		{
			incrementNumIntRegFileAccesses(incrementBy);
		}
		else if(operand.isFloatRegisterOperand())
		{
			incrementNumFloatRegFileAccesses(incrementBy);
		}
	}
	
	void incrementNumIntRegFileAccesses(int incrementBy)
	{
		numIntRegFileAccesses += incrementBy;
	}
	
	void incrementNumFloatRegFileAccesses(int incrementBy)
	{
		numFloatRegFileAccesses += incrementBy;
	}
	
	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		EnergyConfig totalPower = new EnergyConfig(0, 0);
		EnergyConfig intRegFilePower = new EnergyConfig(core.getIntRegFilePower(), numIntRegFileAccesses);
		totalPower.add(totalPower, intRegFilePower);
		EnergyConfig floatRegFilePower = new EnergyConfig(core.getVectorRegFilePower(), numFloatRegFileAccesses);
		totalPower.add(totalPower, floatRegFilePower);
		
		intRegFilePower.printEnergyStats(outputFileWriter, componentName + ".int");
		floatRegFilePower.printEnergyStats(outputFileWriter, componentName + ".float");
		
		return totalPower;
	}
}
