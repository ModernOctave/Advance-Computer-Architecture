package pipeline.multi_issue_inorder;

import java.io.FileWriter;
import java.io.IOException;

import pipeline.FunctionalUnitType;
import pipeline.OpTypeToFUTypeMapping;
import pipeline.multi_issue_inorder.MultiIssueInorderExecutionEngine;
import pipeline.multi_issue_inorder.StageLatch_MII;
import config.EnergyConfig;
import config.SimulationConfig;
import generic.Core;
import generic.Event;
import generic.EventQueue;
import generic.GlobalClock;
import generic.Instruction;
import generic.OperationType;
import generic.PortType;
import generic.SimulationElement;

public class ExecUnitIn_MII extends SimulationElement{
	Core core;
	EventQueue eventQueue;
	MultiIssueInorderExecutionEngine containingExecutionEngine;
	StageLatch_MII idExLatch;
	StageLatch_MII exMemLatch;
	long[] instructionCompletesAt;
	
	long instCtr; //for debug
	
	long numResultsBroadCastBusAccess;
	
	public ExecUnitIn_MII(Core core, MultiIssueInorderExecutionEngine execEngine)
	{
		super(PortType.Unlimited, -1, -1 , -1, -1);
		this.core = core;
		this.eventQueue = core.getEventQueue();
		containingExecutionEngine = execEngine;
		idExLatch = execEngine.getIdExLatch();
		exMemLatch = execEngine.getExMemLatch();
		
		instructionCompletesAt = new long[containingExecutionEngine.getIssueWidth()];
		for(int i = 0; i < containingExecutionEngine.getIssueWidth(); i++)
		{
			instructionCompletesAt[i] = -1;
		}
		
		instCtr = 0;
	}
	
	public void execute(MultiIssueInorderPipeline inorderPipeline)
	{
		if(containingExecutionEngine.getMispredStall() > 0)
		{
			return;
		}
		
		Instruction ins = null;
		
		while(idExLatch.isEmpty() == false
				&& exMemLatch.isFull() == false)
		{
			ins = idExLatch.peek(0);
			if(ins!=null)
			{
				FunctionalUnitType FUType = containingExecutionEngine.getDecodeUnitIn().assignedFUType.get(ins.getSerialNo());
				long lat = 1;
				
				if(FUType != FunctionalUnitType.load
						&& FUType != FunctionalUnitType.loadAGU
						&& FUType != FunctionalUnitType.store
						&& FUType != FunctionalUnitType.storeAGU
						&& FUType != FunctionalUnitType.inValid)
				{
					lat = containingExecutionEngine.getExecutionCore().getFULatency(FUType, 0);
				}
				/*
				 * memory address computation for loads/stores happens in this cycle
				 * assumed as one cycle operation
				 */
				
				if(ins.getSerialNo() != instCtr && ins.getOperationType() != OperationType.inValid)
				{
					misc.Error.showErrorAndExit("exec out of order!!");
				}
				instCtr++;
				
				//move ins to next stage
				exMemLatch.add(ins, idExLatch.getInstructionCompletesAt(ins) + lat);
				idExLatch.poll();
				
				containingExecutionEngine.getDecodeUnitIn().assignedFUType.remove(ins.getSerialNo());
				
				if(ins.getDestinationOperand() != null)
				{
					incrementResultsBroadcastBusAccesses(1);
				}
				
				if(SimulationConfig.debugMode)
				{
					System.out.println("executed : " + GlobalClock.getCurrentTime()/core.getStepSize() + "\n"  + ins + "\n");
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
	
	void incrementResultsBroadcastBusAccesses(int incrementBy)
	{
		numResultsBroadCastBusAccess += incrementBy;
	}

	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		EnergyConfig power = new EnergyConfig(core.getResultsBroadcastBusPower(), numResultsBroadCastBusAccess);
		power.printEnergyStats(outputFileWriter, componentName);
		return power;
	}
}
