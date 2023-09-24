package dram;

import java.util.ArrayList;
import config.MainMemoryConfig;
import config.MainMemoryConfig.QueuingStructure;
import config.MainMemoryConfig.RowBufferPolicy;
import config.MainMemoryConfig.SchedulingPolicy;
import config.SystemConfig;
import dram.BankState.CurrentBankState;
import dram.MainMemoryBusPacket.BusPacketType;
import generic.Core;
import generic.Event;
import generic.EventQueue;
import generic.GlobalClock;
import generic.RequestType;
import main.ArchitecturalComponent;
import main.Main;
import memorysystem.AddressCarryingEvent;
import memorysystem.Cache;
import memorysystem.MainMemoryController;
import misc.Error;

public class MainMemoryDRAMController extends MainMemoryController{
	
	private int numTransactions;
	
	//how many CPU clock cycles to next RAM cycle
	//int nextTick = 0;

	int channel;        //channel number for this mem controller
	long busFreeTime = 0L;    //for keeping track of when the bus is free

	MainMemoryConfig mainMemoryConfig;
	
	//TODO: need a way to store the actual requesting element
	//dirty workaround for now
	
	Cache parentCache;
	int refreshRank;
	
	//for statistics
	long totalTime = 0;
	long totalTransactions = 0;
	long totalReadTransactions[][];
	long totalWriteTransactions[][];
	
	ArrayList<MainMemoryBusPacket> pendingTransQueue;
	//MainMemoryBusPacket pendingTransQueue[]; 	//to keep track of packets that could not be added to command queue 
	BankState bankStates[][];						
	CommandQueue commandQueue;
	Rank ranks[];
	int refreshCount[];
	
	public MainMemoryDRAMController(MainMemoryConfig mainMemoryConfig) {
		super();

		//added later by kush
		if(SystemConfig.memControllerToUse==false){
			return;
		}
	
		this.mainMemoryConfig = mainMemoryConfig;
	
		numTransactions = 0;
		refreshRank=0;
		ranks = new Rank[mainMemoryConfig.numRanks];
		bankStates = new BankState[mainMemoryConfig.numRanks][mainMemoryConfig.numBanks];
		totalReadTransactions = new long[mainMemoryConfig.numRanks][mainMemoryConfig.numBanks];
		totalWriteTransactions = new long[mainMemoryConfig.numRanks][mainMemoryConfig.numBanks];
		
		for(int i=0; i < mainMemoryConfig.numRanks;i++)
		{
			for(int j=0; j < mainMemoryConfig.numBanks; j++)
			{
				bankStates[i][j] = new BankState();
			}
		ranks[i] = new Rank(mainMemoryConfig,i,this);
		}
		
		pendingTransQueue=new ArrayList<MainMemoryBusPacket>();
		
		commandQueue = new CommandQueue(mainMemoryConfig,bankStates);
		
		refreshCount=new int[mainMemoryConfig.numRanks];
		
		for(int i=0;i<mainMemoryConfig.numRanks;i++){
			refreshCount[i]=(int)((mainMemoryConfig.RefreshPeriod/mainMemoryConfig.tCK)/mainMemoryConfig.numRanks)*(i+1);
		}
		
	}
	

	@Override
	public void handleEvent(EventQueue eventQ, Event e)
	{
	
		//added later by kush
		if(SystemConfig.memControllerToUse==false){
			super.handleEvent(eventQ,e);
			return;
		}

		long currentTime = GlobalClock.getCurrentTime();
		
		//System.out.println("Hi!! handling a dram event of type " + e.getRequestType());
		//Main.debugPrinter.print("\nHi!! handling a dram event of type " + e.getRequestType()+ "\n");
		
		
		//check if state update event
		
		if(e.getRequestType() == RequestType.Mem_Cntrlr_State_Update) {
			
			StateUpdateEvent event = (StateUpdateEvent) e;
			
			int rank = event.getRank();
			int bank = event.getBank();
			long eventTime = event.getEventTime();		//IMP: the reference for timing should be the time previous event was generated
														//and not the current clock cycle as these 2 may differ sometimes!
			BankState bankState = bankStates[rank][bank];
			
			//FSM for commands with implicit state change
			switch(bankState.lastCommand) {
				
				case WRITE_P:
				case READ_P:	
					bankState.currentBankState = CurrentBankState.PRECHARGING;
					bankState.lastCommand = BusPacketType.PRECHARGE;
					//create new FSM event and add to original queue
					StateUpdateEvent FSMevent = new StateUpdateEvent(eventQ, (eventTime+mainMemoryConfig.tRP-1), e.getRequestingElement(),
							e.getProcessingElement(), RequestType.Mem_Cntrlr_State_Update, rank, bank);
					eventQ.addEvent(FSMevent);
				break;
				
				case REFRESH:
					//if last command was refresh, all banks were refreshed in that rank. set all as idle
					for(int i=0; i < mainMemoryConfig.numBanks; i++)
					{
						bankStates[rank][i].currentBankState = CurrentBankState.IDLE;
					}
				break;
				case PRECHARGE:
					bankState.currentBankState = CurrentBankState.IDLE;
				break;
				default:
					break;
			}
			
		}
		
		else if(e.getRequestType() == RequestType.Cache_Read || e.getRequestType() == RequestType.Cache_Write) {
			
			//got a read or write event -> perform address mapping and add it to command queue
			
			//TODO: workaround
			this.parentCache = (Cache) e.getRequestingElement();
			
			
			AddressCarryingEvent event = (AddressCarryingEvent) e;
			
			//maintain number of transactions waiting to be serviced
			numTransactions++;
			
			
			MainMemoryBusPacket b = AddressMapping(event.getAddress()); 
			b.setBusPacketType(requestTypeToBusPacketType(event.getRequestType()));
			
			//for TIMING
			//create k6 style trace file
			b.timeCreated = GlobalClock.getCurrentTime();
			//if(event.getRequestType() == RequestType.Cache_Read)
     		//		Main.traceFile.print( String.format("0x%08X", b.physicalAddress) + " P_MEM_RD " + b.timeCreated + "\n");
			//else
			//	Main.traceFile.print( String.format("0x%08X", b.physicalAddress) + " P_MEM_WR " + b.timeCreated + "\n");
			
			
			
			//System.out.println("Of bus packet type:");
			//b.printPacket();
		
			//add it to the list of pending transactions, we will enqueue to command queue from this list
			pendingTransQueue.add(b);	
			
		}
		
		//finally send the data to cpu
		
		else if (e.getRequestType() == RequestType.Rank_Response)
		{
			
			//System.out.println("Received rank response! Sending event");	
			//System.out.println("Time : " + GlobalClock.getCurrentTime() );
			

			//For TIMING
			//Main.timingLog.print(Long.toString(((RamBusAddressCarryingEvent) e).getBusPacket().timeCreated));
			//Main.timingLog.print(" " + GlobalClock.getCurrentTime() + "\n");
			
			MainMemoryBusPacket b = ((RamBusAddressCarryingEvent) e).getBusPacket();
			totalTransactions++;
			totalReadTransactions[b.rank][b.bank]++;
			totalTime += ( GlobalClock.getCurrentTime() - b.timeCreated);

			AddressCarryingEvent event = new AddressCarryingEvent(eventQ, 0,
					this, this.parentCache,	RequestType.Mem_Response,
					((AddressCarryingEvent)e).getAddress());
			//TODO: how to make processing element as cache????
			//dirty workaround right now
			
			getComInterface().sendMessage(event);
			
		}
		
		
	}
	
	
	public void enqueueToCommandQ()
	{

		MainMemoryBusPacket b;	
		for(int i=0; i < pendingTransQueue.size(); i++)
		{
			b = pendingTransQueue.get(i);
			if(commandQueue.hasRoomFor(2,b.rank, b.bank))
			{
				numTransactions--;							//the transaction is no longer waiting in the controller
				//pendingTransQueue.remove(0);
				//create new ACTIVATE bus packet with the address we just decoded 
				MainMemoryBusPacket ACTcommand = b.Clone();							//check cloning is ok
				ACTcommand.setBusPacketType(BusPacketType.ACTIVATE);
				
				//create read or write command and enqueue it
				MainMemoryBusPacket RWcommand = b.Clone();
				//RWcommand.setBusPacketType(requestTypeToBusPacketType(event.getRequestType()));
				
				//System.out.println("Enqueuing commands for address " + event.getAddress());
				//System.out.println("ACTcommand busPacketType "+ACTcommand.busPacketType);
				//System.out.println("RWcommand busPacketType "+RWcommand.busPacketType);
				
				commandQueue.enqueue(ACTcommand);
				commandQueue.enqueue(RWcommand);
				
				//Main.debugPrinter.print("Enqueued ACT command bus packet to queue as follows:");
				ACTcommand.printPacketToFile();
				//Main.debugPrinter.print("Enqueued RW command bus packet to queue as follows:");
				RWcommand.printPacketToFile();
				
				//if enqueued, remove the pending packet
				pendingTransQueue.remove(0);

				break;               //just enqueue the first one !! not all pending, break when first is enqueued

			}
			
		}	
				
	}
			
			

	
	public void oneCycleOperation(){
		long currentTime = GlobalClock.getCurrentTime();
		Core core0 = ArchitecturalComponent.getCores()[0];				//using core 0 queue similar to as in cache
		
		if (refreshCount[refreshRank]==0)
		{
			commandQueue.needRefresh(refreshRank);
			ranks[refreshRank].refreshWaiting = true;
			refreshCount[refreshRank] =	(int)(mainMemoryConfig.RefreshPeriod/mainMemoryConfig.tCK);
			refreshRank++;
			if (refreshRank == mainMemoryConfig.numRanks)
			{
				refreshRank = 0;
			}
		}
		
		MainMemoryBusPacket b = null;
		b = commandQueue.pop(currentTime);
		//if(commandQueue.canPop()) 
		
		
		if(b!=null)
		{
			
			int rank = b.rank;
			int bank = b.bank;
			
			if (b.busPacketType == BusPacketType.WRITE || b.busPacketType == BusPacketType.WRITE_P)
			{
				//if write, schedule the data packet
				
				MainMemoryBusPacket dataPacketToSend = b.Clone();
				dataPacketToSend.setBusPacketType(BusPacketType.DATA);
				
				//Main.debugPrinter.print("\n\n Received a write, scheduling event for data packet for address " + dataPacketToSend.physicalAddress + "\n\n");
				
				RamBusAddressCarryingEvent event = new RamBusAddressCarryingEvent( core0.getEventQueue() , (currentTime + mainMemoryConfig.tWL), this,
						ranks[rank], RequestType.Main_Mem_Access, dataPacketToSend.physicalAddress, dataPacketToSend);
				event.getEventQ().addEvent(event);
				totalWriteTransactions[rank][bank]++;
			}
			
			//update state according to the popped bus packet
			
			switch(b.busPacketType)
			{
			case READ_P:
			case READ:
				if(b.busPacketType == BusPacketType.READ_P)
				{
					bankStates[rank][bank].nextActivate = Math.max(currentTime + mainMemoryConfig.ReadAutopreDelay,
																	bankStates[rank][bank].nextActivate);
					bankStates[rank][bank].lastCommand = BusPacketType.READ_P;
					
					//create and send event state update event
					//sending to core 0 event queue currently
					//keeping requesting and processing element same
					StateUpdateEvent StUpdtEvent = new StateUpdateEvent(core0.getEventQueue(), (currentTime+mainMemoryConfig.ReadToPreDelay), this,
							this, RequestType.Mem_Cntrlr_State_Update, rank, bank);
					StUpdtEvent.getEventQ().addEvent(StUpdtEvent);
				
				}
				else if (b.busPacketType == BusPacketType.READ)
				{
					bankStates[rank][bank].nextPrecharge = Math.max(currentTime + mainMemoryConfig.ReadToPreDelay,
																		bankStates[rank][bank].nextPrecharge);
					bankStates[rank][bank].lastCommand = BusPacketType.READ;

				}

				for (int i=0;i< mainMemoryConfig.numRanks;i++)
				{
					for (int j=0;j<mainMemoryConfig.numBanks;j++)
					{
						if (i!= rank)
						{
							
							if (bankStates[i][j].currentBankState == CurrentBankState.ROW_ACTIVE)
							{
								bankStates[i][j].nextRead = Math.max(currentTime + mainMemoryConfig.tBL/2 + mainMemoryConfig.tRTRS, bankStates[i][j].nextRead);
								bankStates[i][j].nextWrite = Math.max(currentTime + mainMemoryConfig.ReadToWriteDelay,
										bankStates[i][j].nextWrite);
							}
						}
						else
						{
							bankStates[i][j].nextRead = Math.max(currentTime + Math.max(mainMemoryConfig.tCCD, mainMemoryConfig.tBL/2), 
																	bankStates[i][j].nextRead);
							bankStates[i][j].nextWrite = Math.max(currentTime + mainMemoryConfig.ReadToWriteDelay,
																	bankStates[i][j].nextWrite);
						}
					}
				}

				if (b.busPacketType == BusPacketType.READ_P)
				{
					//set read and write to nextActivate so the state table will prevent a read or write
					//  being issued (in cq.isIssuable())before the bank state has been changed because of the
					//  auto-precharge associated with this command
					bankStates[rank][bank].nextRead = bankStates[rank][bank].nextActivate;
					bankStates[rank][bank].nextWrite = bankStates[rank][bank].nextActivate;
				}

				break;
			case WRITE_P:
			case WRITE:
				if (b.busPacketType == BusPacketType.WRITE_P) 
				{
					bankStates[rank][bank].nextActivate = Math.max(currentTime + mainMemoryConfig.WriteAutopreDelay,
																	bankStates[rank][bank].nextActivate);
					bankStates[rank][bank].lastCommand = BusPacketType.WRITE_P;
					
					//create and send event state update event
					//sending to core 0 event queue currently
					//keeping requesting and processing element same
					StateUpdateEvent StUpdtEvent = new StateUpdateEvent(core0.getEventQueue(), (currentTime+mainMemoryConfig.WriteToPreDelay), this,
																		this, RequestType.Mem_Cntrlr_State_Update, rank, bank);
					StUpdtEvent.getEventQ().addEvent(StUpdtEvent);
					
					
				}
				else if (b.busPacketType == BusPacketType.WRITE)
				{
					bankStates[rank][bank].nextPrecharge = Math.max(currentTime + mainMemoryConfig.WriteToPreDelay,
																		bankStates[rank][bank].nextPrecharge);
					bankStates[rank][bank].lastCommand = BusPacketType.WRITE;
				}



				for (int i=0;i< mainMemoryConfig.numRanks;i++)
				{
					for (int j=0;j<mainMemoryConfig.numBanks;j++)
					{
						if (i!=rank)
						{
							if (bankStates[i][j].currentBankState == CurrentBankState.ROW_ACTIVE)
							{
								bankStates[i][j].nextWrite = Math.max(currentTime + mainMemoryConfig.tBL/2 + mainMemoryConfig.tRTRS, bankStates[i][j].nextWrite);
								bankStates[i][j].nextRead = Math.max(currentTime + mainMemoryConfig.WriteToReadDelayR,
																bankStates[i][j].nextRead);
							}
						}
						else
						{
							bankStates[i][j].nextWrite = Math.max(currentTime + Math.max(mainMemoryConfig.tBL/2, mainMemoryConfig.tCCD), bankStates[i][j].nextWrite);
							bankStates[i][j].nextRead = Math.max(currentTime + mainMemoryConfig.WriteToReadDelayB,
									bankStates[i][j].nextRead);
						}
					}
				}

				//set read and write to nextActivate so the state table will prevent a read or write
				//  being issued (in cq.isIssuable())before the bank state has been changed because of the
				//  auto-precharge associated with this command
				if (b.busPacketType == BusPacketType.WRITE_P)
				{
					bankStates[rank][bank].nextRead = bankStates[rank][bank].nextActivate;
					bankStates[rank][bank].nextWrite = bankStates[rank][bank].nextActivate;
				}

				break;
				
			case ACTIVATE:

				bankStates[rank][bank].currentBankState = CurrentBankState.ROW_ACTIVE;
				bankStates[rank][bank].lastCommand = BusPacketType.ACTIVATE;
				bankStates[rank][bank].openRowAddress = b.row;
				bankStates[rank][bank].nextActivate = Math.max(currentTime + mainMemoryConfig.tRC, bankStates[rank][bank].nextActivate);
				bankStates[rank][bank].nextPrecharge = Math.max(currentTime + mainMemoryConfig.tRAS, bankStates[rank][bank].nextPrecharge);

				//if we are using posted-CAS, the next column access can be sooner than normal operation

				bankStates[rank][bank].nextRead = Math.max(currentTime + (mainMemoryConfig.tRCD-mainMemoryConfig.tAL), bankStates[rank][bank].nextRead);
				bankStates[rank][bank].nextWrite = Math.max(currentTime + (mainMemoryConfig.tRCD-mainMemoryConfig.tAL), bankStates[rank][bank].nextWrite);

				for (int i=0;i<mainMemoryConfig.numBanks;i++)
				{
					if (i!=bank)
					{
						bankStates[rank][i].nextActivate = Math.max(currentTime + mainMemoryConfig.tRRD, bankStates[rank][i].nextActivate);
					}
				}

				break;
			case PRECHARGE:
			{
				bankStates[rank][bank].currentBankState = CurrentBankState.PRECHARGING;
				bankStates[rank][bank].lastCommand = BusPacketType.PRECHARGE;
				bankStates[rank][bank].nextActivate = Math.max(currentTime + mainMemoryConfig.tRP, bankStates[rank][bank].nextActivate);
				
				//create and send event state update event
				//sending to core 0 event queue currently
				//keeping requesting and processing element same
				StateUpdateEvent StUpdtEvent = new StateUpdateEvent(core0.getEventQueue(), (currentTime+mainMemoryConfig.tRP - 1), this,
																	this, RequestType.Mem_Cntrlr_State_Update, rank, bank);
				StUpdtEvent.getEventQ().addEvent(StUpdtEvent);
			}
				break;
			case REFRESH:
			{
				for (int i=0; i< mainMemoryConfig.numBanks ;i++)
				{
					bankStates[rank][i].nextActivate = currentTime + mainMemoryConfig.tRFC;
					bankStates[rank][i].currentBankState = CurrentBankState.REFRESHING;
					bankStates[rank][i].lastCommand = BusPacketType.REFRESH;
				}
				
				//create and send event state update event
				//sending to core 0 event queue currently
				//keeping requesting and processing element same
				
				//Sending only 1 event, need to refresh all banks in the rank for this - do this in handle event
				StateUpdateEvent StUpdtEvent = new StateUpdateEvent(core0.getEventQueue(), (currentTime+mainMemoryConfig.tRFC - 1), this,
																	this, RequestType.Mem_Cntrlr_State_Update, rank, bank);
				StUpdtEvent.getEventQ().addEvent(StUpdtEvent);
			}
				break;
			default:
				Error.showErrorAndExit("== Error - Popped a command we shouldn't have of type : " + b.busPacketType);
			}
			
		//after state update
		//schedule command packet as event to rank
		RamBusAddressCarryingEvent event = new RamBusAddressCarryingEvent( core0.getEventQueue() , (currentTime + mainMemoryConfig.tCMD), this,
					ranks[rank], RequestType.Main_Mem_Access, b.physicalAddress, b);
		
		event.getEventQ().addEvent(event);
		
		}

		else{
			//Main.debugPrinter.print("Nothing to pop at this time\n");
									//nothing to do this cycle as nothing popped
			}	
		
		
		
		for (int i=0;i<mainMemoryConfig.numRanks;i++)
		{
			refreshCount[i]--;
		}
		
		return;	
	
	}
	
	//getter and setter for number of CPU cycles to next RAM clock posedge
	/*public int getNextTick()
	{
		return nextTick;
	}*/

	
	/*public void setNextTick(int nextTick)
	{
		this.nextTick = nextTick;
	}*/


	public double getAverageLatency()
	{
		if(totalTransactions > 0)
			return totalTime/totalTransactions;
		else
			return -1.0;
	}

	public long[][] getTotalReadTransactions()
	{
		return totalReadTransactions;
	}

	public long[][] getTotalWriteTransactions()
	{
		return totalWriteTransactions;
	}

	public boolean WillAcceptTransaction()
	{
		//return (this.numTransactions < mainMemoryConfig.TRANSQUEUE_DEPTH);
		return true;     	
	}
	
	public MainMemoryBusPacket AddressMapping(long physicalAddress)
	{	
		long address = physicalAddress;			//this will be returned
		
		//always remember - physical Address is the Byte address!
		
		long tempA, tempB;
		int decodedRank, decodedBank, decodedRow, decodedCol, decodedChan;
		
		int transactionMask = mainMemoryConfig.TRANSACTION_SIZE - 1; 		//this is the mask in binary. for eg: 0x3f for 64 bytes
		
		int channelBits = log2(mainMemoryConfig.numChans);
		int rankBits = log2(mainMemoryConfig.numRanks);
		int bankBits = log2(mainMemoryConfig.numBanks);
		int rowBits = log2(mainMemoryConfig.numRows);
		int colBits = log2(mainMemoryConfig.numCols);
		int colEffectiveBits;
		
		int DataBusBytesOffest = log2(mainMemoryConfig.DATA_BUS_BYTES);		//for 64 bit bus -> 8 bytes -> lower 3 bits of address irrelevant
		
		int ColBytesOffset = log2(mainMemoryConfig.BL);		
		//these are the bits we need to throw away because of "bursts". The column address is incremented internally on bursts
		//So for a burst length of 4, 8 bytes of data are transferred on each burst
		//Each consecutive 8 byte chunk comes for the "next" column
		//So we traverse 4 columns in 1 request. Thus the lower log2(4) bits become irrelevant for us. Throw them away
		//Finally we get 8 bytes * 4 = 32 bytes of data for a 64 bit data bus and BL = 4.
		//This is equal to a cache line
		
		//For clarity
		//Throw away bits to account for data bus size in bytes
		//and for burst length
		physicalAddress >>>= (DataBusBytesOffest + ColBytesOffset); 		//using >>> for unsigned right shift
		//System.out.println("Shifted address by " + (DataBusBytesOffest + ColBytesOffset) + " bits");
				
				
		//By the same logic, need to remove the burst-related column bits from the column bit width to be decoded
		colEffectiveBits = colBits - ColBytesOffset;
				
		
		if(mainMemoryConfig.rowBufferPolicy == RowBufferPolicy.OpenPage)
		{
		//baseline open page scheme
		//row:rank:bank:col:chan

		tempA = physicalAddress;
		physicalAddress = physicalAddress >>> channelBits;			//always unsigned shifting
		tempB = physicalAddress << channelBits;
		//System.out.println("Shifted address by " + rankBits + " bits");
		decodedChan = (int) (tempA ^ tempB);
		//System.out.println("decoded rank: " + Integer.toBinaryString(decodedRank));
		
		tempA = physicalAddress;
		physicalAddress = physicalAddress >>> colEffectiveBits;
		tempB = physicalAddress << colEffectiveBits;
		//System.out.println("Shifted address by " + bankBits + " bits");
		decodedCol = (int) (tempA ^ tempB);
		//System.out.println("decoded bank: " + Integer.toBinaryString(decodedBank));
			
		tempA = physicalAddress;
		physicalAddress = physicalAddress >>> bankBits;
		tempB = physicalAddress << bankBits;
		//System.out.println("Shifted address by " + colEffectiveBits + " bits");
		decodedBank = (int) (tempA ^ tempB);
		//System.out.println("decoded col: " + Integer.toBinaryString(decodedCol));
		
		tempA = physicalAddress;
		physicalAddress = physicalAddress >>> rankBits;
		tempB = physicalAddress << rankBits;
		decodedRank = (int) (tempA ^ tempB);

		tempA = physicalAddress;
		physicalAddress = physicalAddress >>> rowBits;
		tempB = physicalAddress << rowBits;
		decodedRow = (int) (tempA ^ tempB);
		}
		
		else if(mainMemoryConfig.rowBufferPolicy == RowBufferPolicy.ClosePage)
		{
		//baseline close page scheme
		//row:col:rank:bank:chan

		tempA = physicalAddress;
		physicalAddress = physicalAddress >>> channelBits;			//always unsigned shifting
		tempB = physicalAddress << channelBits;
		//System.out.println("Shifted address by " + rankBits + " bits");
		decodedChan = (int) (tempA ^ tempB);
		//System.out.println("decoded rank: " + Integer.toBinaryString(decodedRank));
		
		tempA = physicalAddress;
		physicalAddress = physicalAddress >>> bankBits;
		tempB = physicalAddress << bankBits;
		//System.out.println("Shifted address by " + bankBits + " bits");
		decodedBank = (int) (tempA ^ tempB);
		//System.out.println("decoded bank: " + Integer.toBinaryString(decodedBank));
			
		tempA = physicalAddress;
		physicalAddress = physicalAddress >>> rankBits;
		tempB = physicalAddress << rankBits;
		//System.out.println("Shifted address by " + colEffectiveBits + " bits");
		decodedRank = (int) (tempA ^ tempB);
		//System.out.println("decoded col: " + Integer.toBinaryString(decodedCol));
		
		tempA = physicalAddress;
		physicalAddress = physicalAddress >>> colEffectiveBits;
		tempB = physicalAddress << colEffectiveBits;
		decodedCol = (int) (tempA ^ tempB);

		tempA = physicalAddress;
		physicalAddress = physicalAddress >>> rowBits;
		tempB = physicalAddress << rowBits;
		decodedRow = (int) (tempA ^ tempB);
		}

		else //invalid case
		{
		decodedRow = -1;
		decodedCol = -1;
		decodedBank = -1;
		decodedRank = -1;
		decodedChan = -1;
		Error.showErrorAndExit("Invalid Row Buffer Policy!");
		}

		
		//if num ranks = 1, decoded rank will always be "0"


		MainMemoryBusPacket b = new MainMemoryBusPacket(decodedRow, decodedCol, decodedBank, decodedRank, address, null);
		return b;
	}
	
	public static int log2(int a)
	{
		return (int) (Math.log(a)/Math.log(2));
	}
	
	
	public BusPacketType requestTypeToBusPacketType(RequestType requestType)
	{
		switch(requestType)
		{
		case Cache_Read:
			if(mainMemoryConfig.getRowBufferPolicy()==RowBufferPolicy.ClosePage)
			{
				return BusPacketType.READ_P;
			}
			else if(mainMemoryConfig.getRowBufferPolicy()==RowBufferPolicy.OpenPage)
			{
				return BusPacketType.READ;
			}
			else
			{
				Error.showErrorAndExit("Unkown row buffer policy");
				return null; 										//needed to avoid compile error
			}
			//break;												//not required because "unreachable" code
		case Cache_Write:
			if(mainMemoryConfig.getRowBufferPolicy()==RowBufferPolicy.ClosePage)
			{
				return BusPacketType.WRITE_P;
			}
			else if(mainMemoryConfig.getRowBufferPolicy()==RowBufferPolicy.OpenPage)
			{
				return BusPacketType.WRITE;
			}
			else
			{
				Error.showErrorAndExit("Unkown row buffer policy");
				return null; 										//needed to avoid compile error
			}
			//break;
		default:
			Error.showErrorAndExit("Request type "+ requestType + "does not have a corresponding bus packet type");
			return null;
		}
	}
	
	public void setChannelNumber(int n)
	{
		this.channel = n;
	}
	

	public int getChannelNumber()
	{
		return this.channel;
	}

	public void setBusFreeTime(long t)
	{
		this.busFreeTime = t;
	}
	
	public long getBusFreeTime()
	{
		return this.busFreeTime;
	}

	/*public void printBankStateTest()
	{
		Main.outputLog.print("== Printing bank states (According to MC) at Clock Cycle " + GlobalClock.getCurrentTime() + "\n");
		for (int i=0; i < mainMemoryConfig.numRanks; i++)
		{
			for (int j=0; j < mainMemoryConfig.numBanks; j++)
			{
				if (bankStates[i][j].currentBankState == CurrentBankState.ROW_ACTIVE)
				{
					Main.outputLog.print("[" + bankStates[i][j].openRowAddress + "] ");
				}
				else if (bankStates[i][j].currentBankState == CurrentBankState.IDLE)
				{
					Main.outputLog.print("[idle] ");
				}
				else if (bankStates[i][j].currentBankState == CurrentBankState.PRECHARGING)
				{
					Main.outputLog.print("[pre] ");
				}
				else if (bankStates[i][j].currentBankState == CurrentBankState.REFRESHING)
				{
					Main.outputLog.print("[ref] ");
				}
				else if (bankStates[i][j].currentBankState == CurrentBankState.POWER_DOWN)
				{
					Main.outputLog.print("[lowp] ");
				}
			}
			Main.outputLog.print("\n");
		}
	}*/
	
}
