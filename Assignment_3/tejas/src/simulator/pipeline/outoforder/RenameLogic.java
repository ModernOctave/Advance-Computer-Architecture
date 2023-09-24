package pipeline.outoforder;

import java.io.FileWriter;
import java.io.IOException;

import config.EnergyConfig;
import config.SimulationConfig;
import generic.Core;
import generic.Event;
import generic.EventQueue;
import generic.GenericCircularQueue;
import generic.GlobalClock;
import generic.Instruction;
import generic.Operand;
import generic.OperandType;
import generic.OperationType;
import generic.PortType;
import generic.SimulationElement;

public class RenameLogic extends SimulationElement {
	
	Core core;
	OutOrderExecutionEngine execEngine;
	GenericCircularQueue<ReorderBufferEntry> decodeBuffer;
	GenericCircularQueue<ReorderBufferEntry> renameBuffer;
	int renameWidth;
	
	int threadID;
	Instruction instruction;
	ReorderBufferEntry reorderBufferEntry;
	OperationType opType;
	
	public RenameLogic(Core core, OutOrderExecutionEngine execEngine)
	{
		super(PortType.Unlimited, -1, -1 , -1, -1);
		this.core = core;
		this.execEngine = execEngine;
		decodeBuffer = execEngine.getDecodeBuffer();
		renameBuffer = execEngine.getRenameBuffer();
		renameWidth = core.getRenameWidth();
	}
	
	public void performRename()
	{
		if(execEngine.isToStall5() == true /*pipeline stalled due to branch mis-prediction*/)
		{
			return;
		}
		
		for(int i = 0; i < renameWidth; i++)
		{
			if(renameBuffer.isFull() == true)
			{
				break;
			}
			
			if(decodeBuffer.peek(0) != null)
			{
				reorderBufferEntry = decodeBuffer.peek(0);
				instruction = reorderBufferEntry.getInstruction();
				threadID = reorderBufferEntry.getThreadID();
				
				//check if the instruction can be assigned a destination register
				if(canDestOperandBeProcessed(reorderBufferEntry))
				{
					//find out which physical registers correspond
					//to the source operands
					processOperand1(reorderBufferEntry);
					processOperand2(reorderBufferEntry);
					
					//check for availability of source operands
					checkOperand1Availability();
					if(reorderBufferEntry.isOperand2Available() == false)
					{
						checkOperand2Availability();
					}
					
					//assign register for destination operand(s)
					processDestOperand(reorderBufferEntry);
					
					renameBuffer.enqueue(decodeBuffer.dequeue());
					reorderBufferEntry.setRenameDone(true);
					
					execEngine.setToStall2(false);
					
					if(SimulationConfig.debugMode)
					{
						System.out.println("renamed : " + GlobalClock.getCurrentTime()/core.getStepSize() + " : "  + reorderBufferEntry.getInstruction());
					}
				}
				else
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

	/*
	 * find the physical register(s) corresponding to operand 1
	 */
	private void processOperand1(ReorderBufferEntry reorderBufferEntry)
	{
		Operand tempOpnd = reorderBufferEntry.getInstruction().getSourceOperand1();
		if(tempOpnd == null ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.inValid ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.nop ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.cpuid ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.mfence ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.sfence ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.lfence)
		{
			reorderBufferEntry.setOperand1PhyReg1(-1);
			reorderBufferEntry.setOperand1PhyReg2(-1);
			return;
		}

		OperandType tempOpndType = tempOpnd.getOperandType();
		int archReg;
		if(tempOpndType == OperandType.integerRegister)
		{
			archReg = (int) tempOpnd.getValue();
			reorderBufferEntry.setOperand1PhyReg1(execEngine.getIntegerRenameTable().getPhysicalRegister(threadID, archReg));
			reorderBufferEntry.setOperand1PhyReg2(-1);
		}
		else if(tempOpndType == OperandType.vectorRegister)
		{
			archReg = (int) tempOpnd.getValue();
			reorderBufferEntry.setOperand1PhyReg1(execEngine.getVectorRenameTable().getPhysicalRegister(threadID, archReg));
			reorderBufferEntry.setOperand1PhyReg2(-1);
		}
		else if(tempOpndType == OperandType.memory)
		{
			Operand memLocOpnd1 = tempOpnd.getMemoryLocationFirstOperand();
			Operand memLocOpnd2 = tempOpnd.getMemoryLocationSecondOperand();
			
			//processing memoryLocationFirstOperand
			if(memLocOpnd1 == null)
			{
				reorderBufferEntry.setOperand1PhyReg1(-1);
			}
			else
			{
				archReg = (int)memLocOpnd1.getValue();
				tempOpndType = memLocOpnd1.getOperandType();
				
				if(tempOpndType == OperandType.integerRegister)
				{
					reorderBufferEntry.setOperand1PhyReg1(execEngine.getIntegerRenameTable().getPhysicalRegister(threadID, archReg));
				}
				else if(tempOpndType == OperandType.vectorRegister)
				{
					reorderBufferEntry.setOperand1PhyReg1(execEngine.getVectorRenameTable().getPhysicalRegister(threadID, archReg));
				}
				else
				{
					reorderBufferEntry.setOperand1PhyReg1(-1);
				}
			}
			
			//processing memoryLocationSecondOperand
			if(memLocOpnd2 == null)
			{
				reorderBufferEntry.setOperand1PhyReg2(-1);
			}
			else
			{
				archReg = (int)memLocOpnd2.getValue();
				tempOpndType = memLocOpnd2.getOperandType();
				
				if(tempOpndType == OperandType.integerRegister)
				{
					reorderBufferEntry.setOperand1PhyReg2(execEngine.getIntegerRenameTable().getPhysicalRegister(threadID, archReg));
				}
				else if(tempOpndType == OperandType.vectorRegister)
				{
					reorderBufferEntry.setOperand1PhyReg2(execEngine.getVectorRenameTable().getPhysicalRegister(threadID, archReg));
				}
				else
				{
					reorderBufferEntry.setOperand1PhyReg2(-1);
				}
			}
		}
		else
		{
			reorderBufferEntry.setOperand1PhyReg1(-1);
			reorderBufferEntry.setOperand1PhyReg2(-1);
		}
	}
	
	/*
	 * find the physical register(s) corresponding to operand 2
	 */
	private void processOperand2(ReorderBufferEntry reorderBufferEntry)
	{
		Operand tempOpnd = reorderBufferEntry.getInstruction().getSourceOperand2();
		
		if(tempOpnd == null ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.inValid ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.nop ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.cpuid ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.mfence ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.sfence ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.lfence)
		{
			reorderBufferEntry.setOperand2PhyReg1(-1);
			reorderBufferEntry.setOperand2PhyReg2(-1);
			return;
		}

		OperandType tempOpndType = tempOpnd.getOperandType();
		int archReg;
		if(tempOpndType == OperandType.integerRegister)
		{
			archReg = (int) tempOpnd.getValue();
			reorderBufferEntry.setOperand2PhyReg1(execEngine.getIntegerRenameTable().getPhysicalRegister(threadID, archReg));
			reorderBufferEntry.setOperand2PhyReg2(-1);
		}
		else if(tempOpndType == OperandType.vectorRegister)
		{
			archReg = (int) tempOpnd.getValue();
			reorderBufferEntry.setOperand2PhyReg1(execEngine.getVectorRenameTable().getPhysicalRegister(threadID, archReg));
			reorderBufferEntry.setOperand2PhyReg2(-1);
		}
		else if(tempOpndType == OperandType.memory)
		{
			Operand memLocOpnd1 = tempOpnd.getMemoryLocationFirstOperand();
			Operand memLocOpnd2 = tempOpnd.getMemoryLocationSecondOperand();
			
			//processing memoryLocationFirstOperand
			if(memLocOpnd1 == null)
			{
				reorderBufferEntry.setOperand2PhyReg1(-1);
			}
			else
			{
				archReg = (int)memLocOpnd1.getValue();
				tempOpndType = memLocOpnd1.getOperandType();
				
				if(tempOpndType == OperandType.integerRegister)
				{
					reorderBufferEntry.setOperand2PhyReg1(execEngine.getIntegerRenameTable().getPhysicalRegister(threadID, archReg));
				}
				else if(tempOpndType == OperandType.vectorRegister)
				{
					reorderBufferEntry.setOperand2PhyReg1(execEngine.getVectorRenameTable().getPhysicalRegister(threadID, archReg));
				}
				else
				{
					reorderBufferEntry.setOperand2PhyReg1(-1);
				}
			}
			
			//processing memoryLocationSecondOperand
			if(memLocOpnd2 == null)
			{
				reorderBufferEntry.setOperand2PhyReg2(-1);
			}
			else
			{
				archReg = (int)memLocOpnd2.getValue();
				tempOpndType = memLocOpnd2.getOperandType();
				
				if(tempOpndType == OperandType.integerRegister)
				{
					reorderBufferEntry.setOperand2PhyReg2(execEngine.getIntegerRenameTable().getPhysicalRegister(threadID, archReg));
				}
				else if(tempOpndType == OperandType.vectorRegister)
				{
					reorderBufferEntry.setOperand2PhyReg2(execEngine.getVectorRenameTable().getPhysicalRegister(threadID, archReg));
				}
				else
				{
					reorderBufferEntry.setOperand2PhyReg2(-1);
				}
			}
		}
		else
		{
			reorderBufferEntry.setOperand2PhyReg1(-1);
			reorderBufferEntry.setOperand2PhyReg2(-1);
		}
	}
	
	/*
	 * find if registers are available for the destination operand(s)
	 * note : actual allocation isn't done at this point
	 */
	private boolean canDestOperandBeProcessed(ReorderBufferEntry reorderBufferEntry)
	{
		if(reorderBufferEntry.getInstruction().getOperationType() == OperationType.inValid ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.nop ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.cpuid ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.mfence ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.sfence ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.lfence)
		{
			return true;
		}
		
		int numIntRegsRequired = 0;
		int numFloatRegsRequired = 0;
		
		
		Operand tempOpnd = reorderBufferEntry.getInstruction().getDestinationOperand();
		if(tempOpnd != null)
		{
			if(tempOpnd.getOperandType() == OperandType.integerRegister)
			{
				numIntRegsRequired++;
			}
			else if(tempOpnd.getOperandType() == OperandType.vectorRegister)
			{
				numFloatRegsRequired++;
			}
		}
			
		if(numIntRegsRequired <= execEngine.getIntegerRenameTable().getAvailableListSize()
				&& numFloatRegsRequired <= execEngine.getVectorRenameTable().getAvailableListSize())
		{
			return true;
		}
		else
		{
			return false;
		}		
	}
	
	/*
	 * perform renaming to obtain physical register(s) for the destination operand(s)
	 */
	private boolean processDestOperand(ReorderBufferEntry reorderBufferEntry)
	{
		Operand tempOpnd;
		OperandType tempOpndType;
		
		tempOpnd = reorderBufferEntry.getInstruction().getDestinationOperand();
		if(tempOpnd == null ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.inValid ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.nop ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.cpuid ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.mfence ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.sfence ||
				reorderBufferEntry.getInstruction().getOperationType() == OperationType.lfence)
		{
			return true;
		}

		tempOpndType = tempOpnd.getOperandType();
		if(tempOpndType != OperandType.integerRegister &&
				tempOpndType != OperandType.vectorRegister)
		{
			return true;
		}		
		else
		{
			return handleIntFloat(reorderBufferEntry, 1);
		}
	}
	
	boolean handleIntFloat(ReorderBufferEntry reorderBufferEntry, int whichOperand)
	{
		RenameTable tempRN;
		OperandType tempOpndType;
		int registerNumber;
		
		if(whichOperand == 1)
		{
			tempOpndType = reorderBufferEntry.getInstruction().
									getDestinationOperand().getOperandType();
			registerNumber = (int) reorderBufferEntry.getInstruction().getDestinationOperand().getValue();
		}
		else if(whichOperand == 2)
		{
			tempOpndType = reorderBufferEntry.getInstruction().getSourceOperand1().getOperandType();
			registerNumber = (int) reorderBufferEntry.getInstruction().getSourceOperand1().getValue();
		}
		else if(whichOperand == 3)
		{
			tempOpndType = reorderBufferEntry.getInstruction().getSourceOperand2().getOperandType();
			registerNumber = (int) reorderBufferEntry.getInstruction().getSourceOperand2().getValue();
		}
		else
		{
			System.err.println("invalid whichOperand!");
			return true;
		}
		
		if(tempOpndType == OperandType.integerRegister)
		{
			tempRN = execEngine.getIntegerRenameTable();
		}
		else
		{
			tempRN = execEngine.getVectorRenameTable();
		}
		
		int r = tempRN.allocatePhysicalRegister(threadID, registerNumber);
		if(r >= 0)
		{
			//physical register found
			
			if(whichOperand == 1)
			{
				reorderBufferEntry.setPhysicalDestinationRegister(r);
			}
			else if(whichOperand == 2)
			{
				reorderBufferEntry.setOperand1PhyReg1(r);
			}
			else if(whichOperand == 3)
			{
				reorderBufferEntry.setOperand2PhyReg1(r);
			}
			else
			{
				System.err.println("invalid whichOperand!");
				return true;
			}
			tempRN.setValueValid(false, r);
			tempRN.setProducerROBEntry(reorderBufferEntry, r);
			execEngine.setToStall2(false);
			
			return true;
		}
		else
		{
			//look for a physical register in the next clock cycle
			//stall decode because physical register for destination was not allocated
			execEngine.setToStall2(true);
			return false;
		}
	}
	
	void checkOperand1Availability()
	{
		Operand tempOpnd = instruction.getSourceOperand1();
		
		if(tempOpnd == null
				|| reorderBufferEntry.getInstruction().getOperationType() == OperationType.inValid
				|| reorderBufferEntry.getInstruction().getOperationType() == OperationType.nop
				|| reorderBufferEntry.getInstruction().getOperationType() == OperationType.cpuid
				|| reorderBufferEntry.getInstruction().getOperationType() == OperationType.mfence
				|| reorderBufferEntry.getInstruction().getOperationType() == OperationType.sfence
				|| reorderBufferEntry.getInstruction().getOperationType() == OperationType.lfence
				|| tempOpnd.getOperandType() == OperandType.immediate)
		{
			reorderBufferEntry.setOperand1Available(true);
			return;
		}
		
		int tempOpndPhyReg1 = reorderBufferEntry.getOperand1PhyReg1();
		int tempOpndPhyReg2 = reorderBufferEntry.getOperand1PhyReg2();
		boolean[] opndAvailable = OperandAvailabilityChecker.isAvailable(reorderBufferEntry, tempOpnd, tempOpndPhyReg1, tempOpndPhyReg2, core);
		
		OperandType tempOpndType = tempOpnd.getOperandType();
		if(tempOpndType == OperandType.integerRegister ||
				tempOpndType == OperandType.vectorRegister)
		{		
			if(opndAvailable[0] == true)
			{
				reorderBufferEntry.setOperand1Available(true);
			}
		}		
		else if(tempOpndType == OperandType.memory)
		{
			reorderBufferEntry.setOperand11Available(opndAvailable[0]);
			reorderBufferEntry.setOperand12Available(opndAvailable[1]);
			reorderBufferEntry.setOperand1Available(opndAvailable[0] && opndAvailable[1]);
		}
	}
	
	void checkOperand2Availability()
	{
		Operand tempOpnd = reorderBufferEntry.getInstruction().getSourceOperand2();
		
		if(tempOpnd == null
				|| reorderBufferEntry.getInstruction().getOperationType() == OperationType.inValid
				|| reorderBufferEntry.getInstruction().getOperationType() == OperationType.nop
				|| reorderBufferEntry.getInstruction().getOperationType() == OperationType.cpuid
				|| reorderBufferEntry.getInstruction().getOperationType() == OperationType.mfence
				|| reorderBufferEntry.getInstruction().getOperationType() == OperationType.sfence
				|| reorderBufferEntry.getInstruction().getOperationType() == OperationType.lfence
				|| tempOpnd.getOperandType() == OperandType.immediate)
		{
			reorderBufferEntry.setOperand2Available(true);
			return;
		}
		
		int tempOpndPhyReg1 = reorderBufferEntry.getOperand2PhyReg1();
		int tempOpndPhyReg2 = reorderBufferEntry.getOperand2PhyReg2();
		boolean[] opndAvailable = OperandAvailabilityChecker.isAvailable(reorderBufferEntry, tempOpnd, tempOpndPhyReg1, tempOpndPhyReg2, core);
		
		OperandType tempOpndType = tempOpnd.getOperandType();
		if(tempOpndType == OperandType.integerRegister ||
				tempOpndType == OperandType.vectorRegister)
		{
			if(opndAvailable[0] == true)
			{
				reorderBufferEntry.setOperand2Available(true);
			}
		}		
		else if(tempOpndType == OperandType.memory)
		{
			reorderBufferEntry.setOperand21Available(opndAvailable[0]);
			reorderBufferEntry.setOperand22Available(opndAvailable[1]);
			reorderBufferEntry.setOperand2Available(opndAvailable[0] && opndAvailable[1]);
		}
	}

	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
		
	}
	
	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		EnergyConfig intRenamePower = execEngine.getIntegerRenameTable().calculateAndPrintEnergy(outputFileWriter, (componentName + ".Int"));
		EnergyConfig floatRenamePower = execEngine.getVectorRenameTable().calculateAndPrintEnergy(outputFileWriter, (componentName + ".Float"));
		
		EnergyConfig totalPower = new EnergyConfig(0, 0);
		totalPower.add(totalPower,  intRenamePower);
		totalPower.add(totalPower,  floatRenamePower);
		
		totalPower.printEnergyStats(outputFileWriter, componentName);
		
		return totalPower;
	}
	


}
