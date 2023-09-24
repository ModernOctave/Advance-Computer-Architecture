package pipeline;

import java.lang.reflect.Array;
import main.ArchitecturalComponent;
import main.Main;
import config.XMLParser;
import generic.GenericCircularQueue;
import generic.GlobalClock;
import generic.Instruction;
import generic.Operand;
import generic.Statistics;

public class PipelineTests {
	
	static String configFileName;
	static GenericCircularQueue<Instruction> inputToPipeline;
	static final int INSTRUCTION_THRESHOLD = 2000;

	public static void setUpBeforeClass(String configFile) {
		
		// Parse the command line arguments
		XMLParser.parse(configFile);
		
		//initialize object pools
		Main.initializeObjectPools();
		
		// initialize cores, memory, tokenBus
		ArchitecturalComponent.createChip();
		inputToPipeline = new GenericCircularQueue<Instruction>(
											Instruction.class, INSTRUCTION_THRESHOLD);
		GenericCircularQueue<Instruction>[] toBeSet = (GenericCircularQueue<Instruction>[])
														Array.newInstance(GenericCircularQueue.class, 1);
		toBeSet[0] = inputToPipeline;
		ArchitecturalComponent.getCores()[0].getPipelineInterface().setInputToPipeline(toBeSet);
		ArchitecturalComponent.getCores()[0].currentThreads = 1;
		ArchitecturalComponent.getCore(0).getExecEngine().setExecutionBegun(true);
		
		// Initialize the statistics
		Statistics.initStatistics();
	}

	/*
	 * simulates a sequence of intALU instructions that have no data dependencies
	 */
	public static void minimumDataDependencies() {
		
		//generate instruction sequence
		Instruction newInst;
		int temp = 1;
		for(int i = 0; i < 100; i++)
		{
			temp++;
			if(temp%16 == 0)
			{
				temp++;
			}
			
			newInst = Instruction.getIntALUInstruction(
										Operand.getIntegerRegister(0),
										Operand.getIntegerRegister(0),
										Operand.getIntegerRegister(temp%16));
			
			inputToPipeline.enqueue(newInst);
		}		
		inputToPipeline.enqueue(Instruction.getInvalidInstruction());
		
		//simulate pipeline
		while(ArchitecturalComponent.getCores()[0].getPipelineInterface().isExecutionComplete() == false)
		{
			ArchitecturalComponent.getCores()[0].getPipelineInterface().oneCycleOperation();
			GlobalClock.incrementClock();
		}
	}

	/*
	 * simulates a sequence of intALU instructions, with (i+1)th instruction dependent on ith
	 */
	public static void maximumDataDependencies() {
		
		//generate instruction sequence
		Instruction newInst;
		for(int i = 0; i < 100; i++)
		{
			newInst = Instruction.getIntALUInstruction(
										Operand.getIntegerRegister(i%16),
										Operand.getIntegerRegister(i%16),
										Operand.getIntegerRegister((i+1)%16));
			
			inputToPipeline.enqueue(newInst);
		}		
		inputToPipeline.enqueue(Instruction.getInvalidInstruction());
		
		//simulate pipeline
		while(ArchitecturalComponent.getCores()[0].getPipelineInterface().isExecutionComplete() == false)
		{
			ArchitecturalComponent.getCores()[0].getPipelineInterface().oneCycleOperation();
			GlobalClock.incrementClock();
		}
	}

	/*
	 * simulates a sequence of floatDiv instructions, with no data dependencies
	 */
	public static void structuralHazards() {
		
		//generate instruction sequence
		Instruction newInst;
		int temp = 1;
		for(int i = 0; i < 100; i++)
		{
			temp++;
			if(temp%16 == 0)
			{
				temp++;
			}
			
			newInst = Instruction.getFloatingPointDivision(
										Operand.getIntegerRegister(0),
										Operand.getIntegerRegister(0),
										Operand.getIntegerRegister(temp%16));
			
			inputToPipeline.enqueue(newInst);
		}		
		inputToPipeline.enqueue(Instruction.getInvalidInstruction());
		
		//simulate pipeline
		while(ArchitecturalComponent.getCores()[0].getPipelineInterface().isExecutionComplete() == false)
		{
			ArchitecturalComponent.getCores()[0].getPipelineInterface().oneCycleOperation();
			GlobalClock.incrementClock();
		}
	}

	/*
	 * simulates a sequence of floatDiv instructions, all operating on R0, and writing to R0
	 */
	public static void renameTest() {
		
		//generate instruction sequence
		Instruction newInst;
		for(int i = 0; i < 100; i++)
		{
			newInst = Instruction.getFloatingPointDivision(
										Operand.getFloatRegister(0),
										Operand.getFloatRegister(0),
										Operand.getFloatRegister(0));
			
			inputToPipeline.enqueue(newInst);
		}		
		inputToPipeline.enqueue(Instruction.getInvalidInstruction());
		
		//simulate pipeline
		while(ArchitecturalComponent.getCores()[0].getPipelineInterface().isExecutionComplete() == false)
		{
			ArchitecturalComponent.getCores()[0].getPipelineInterface().oneCycleOperation();
			GlobalClock.incrementClock();
		}
	}
	
	public static void main(String[] arguments)
	{
		String configFile = arguments[0]; 
		int testType = Integer.parseInt(arguments[1]);
		
		setUpBeforeClass(configFile);
		
		switch(testType)
		{
			case 0 :	minimumDataDependencies();
						break;
						
			case 1 :	maximumDataDependencies();
						break;
						
			case 2 :	structuralHazards();
						break;
						
			case 3 :	renameTest();
						break;
			
			default :	misc.Error.showErrorAndExit("unknown test type");
		}
	}

}
