package main;

import misc.Error;
import misc.ShutDownHook;
import java.io.IOException;
import config.CommunicationType;
import config.EmulatorConfig;
import config.EmulatorType;
import config.SimulationConfig;
import config.SystemConfig;
import config.XMLParser;
import dram.DebugPrinter;
import emulatorinterface.RunnableThread;
import emulatorinterface.communication.IpcBase;
import emulatorinterface.communication.filePacket.FilePacket;
import emulatorinterface.communication.network.Network;
import emulatorinterface.communication.shm.SharedMem;
import emulatorinterface.translator.x86.objparser.ObjParser;
import generic.BenchmarkThreadMapping;
import generic.Operand;
import generic.PinPointsProcessing;
import generic.Statistics;


public class Main {
	
	private static Emulator emulator;
	
	// the reader threads. Each thread reads from EMUTHREADS
	public static RunnableThread [] runners;
	public static volatile boolean statFileWritten = false;
	
	private static  String emulatorFile = " ";
	private static  String[] traceFileBasename;
	
	public static int pid;
	public static IpcBase ipcBase;

	public static String executableAndArguments[];
	public static String benchmarkArguments = " ";
	public static long startTime, endTime;
	public static boolean printStatisticsOnAsynchronousTermination = false;
	
	public static BenchmarkThreadMapping benchmarkThreadMapping;
	
	//public static DebugPrinter debugPrinter;
	public static DebugPrinter timingLog;
	public static DebugPrinter traceFile;
	public static DebugPrinter outputLog;

	public static void main(String[] arguments)
	{
		//register shut down hook
		Runtime.getRuntime().addShutdownHook(new ShutDownHook());
		
		checkCommandLineArguments(arguments);

		String configFileName = arguments[0];
		SimulationConfig.outputFileName = arguments[1];
		
		// Parse the command line arguments
		XMLParser.parse(configFileName);
		printStatisticsOnAsynchronousTermination = true;

		if(EmulatorConfig.communicationType==CommunicationType.file){
			traceFileBasename = new String[arguments.length - 2];
			for(int i = 2; i < arguments.length; i++)
				setTraceFileBasename(arguments[i], i-2);
		} 
		if(EmulatorConfig.emulatorType==EmulatorType.pin){
			//pin mode
			setEmulatorFile(arguments[2]);
			// Read the command line arguments
			executableAndArguments = new String[arguments.length-2];
			// read the command line arguments for the benchmark (not emulator) here.
			for(int i=2; i < arguments.length; i++) {
				executableAndArguments[i-2] = arguments[i]; 
				benchmarkArguments = benchmarkArguments + " " + arguments[i];
			}
		}
		
		// Initialize the statistics
		Statistics.initStatistics();
		
		// PinPointsProcessing.initialize();
		
		initializeObjectPools();
		
		// Create a hash-table for the static representation of the executable
		if(EmulatorConfig.emulatorType==EmulatorType.pin) {
			ObjParser.buildStaticInstructionTable(getEmulatorFile());
		} /*else if(EmulatorConfig.emulatorType==EmulatorType.none) {
			ObjParser.initializeThreadMicroOpsList(SystemConfig.numEmuThreadsPerJavaThread);
		}*/
		
		ObjParser.initializeDynamicInstructionBuffer(SystemConfig.numEmuThreadsPerJavaThread*SystemConfig.numEmuThreadsPerJavaThread);
		ObjParser.initializeControlMicroOps();
		
		// initialize cores, memory
		ArchitecturalComponent.createChip();

		// Start communication channel before starting emulator
		// PS : communication channel must be started before starting the emulator
		ipcBase = startCommunicationChannel();
		benchmarkThreadMapping = new BenchmarkThreadMapping(getTraceFileBasename());
		
		runners = new RunnableThread[SystemConfig.maxNumJavaThreads];
		
		//added by harveenk
		//uncomment for printing debug info
		//debugPrinter = new DebugPrinter("kush_event_log");
		//timingLog = new DebugPrinter("./logfiles/timing/test.timing");
		//traceFile = new DebugPrinter("./logfiles/traces/k6_test.trc");
		//outputLog = new DebugPrinter("output_log_CMDQ");
		
		// start emulator
		if(EmulatorConfig.emulatorType==EmulatorType.pin){
			String emulatorArguments = constructEmulatorArguments(benchmarkArguments);
			startEmulator(emulatorArguments, ipcBase);
		}

		for (int i=0; i<SystemConfig.maxNumJavaThreads; i++) {
			runners[i] = new RunnableThread("thread"+Integer.toString(i),i, ipcBase, ArchitecturalComponent.getCores());
		}
		
		startTime = System.currentTimeMillis();

		for (int i=0; i<SystemConfig.maxNumJavaThreads; i++) {
			if(runners[i].ipcBase != null) {
				(new Thread(runners[i], "thread"+Integer.toString(i))).start();
			}
		}
		
		getBenchmarkPID();
		
		ipcBase.waitForJavaThreads();

		endTime = System.currentTimeMillis();
		
		if(emulator!=null) {
			emulator.forceKill();
		}
		
		ipcBase.finish();
		
		// PinPointsProcessing.windup();
		
		//added by harveenk
		//debugPrinter.close();  				//close the debug file
		//timingLog.close();
		//traceFile.close();
		//outputLog.close();

		Statistics.printAllStatistics(getEmulatorFile(), startTime, endTime);
		statFileWritten = true;
		
		System.out.println("\n\nSimulation completed !!");
		System.exit(0);
	}

	public static void initializeObjectPools() {
		
		int numStaticInstructions = 0;
		
		if(EmulatorConfig.emulatorType == EmulatorType.pin) {
			// approximately 3 micro-operations are required per cisc instruction
			numStaticInstructions = ObjParser.noOfLines(getEmulatorFile()) * 3;
		} else {
			
		}
		
		// Initialise pool of instructions
		CustomObjectPool.initCustomPools(SystemConfig.maxNumJavaThreads*SystemConfig.numEmuThreadsPerJavaThread, numStaticInstructions);
		
		// Pre-allocate all the possible operands
		Operand.preAllocateOperands();
	}

	private static IpcBase startCommunicationChannel() {
		IpcBase ipcBase = null;
		if(EmulatorConfig.communicationType==CommunicationType.sharedMemory) {
			getMyPID();
			System.out.println("Newmain : pid = " + pid);
			ipcBase = new SharedMem(pid);
 		} else if(EmulatorConfig.communicationType==CommunicationType.network) {
 			//ipcBase = new Network(IpcBase.MaxNumJavaThreads*IpcBase.EmuThreadsPerJavaThread);
 			ipcBase = new Network();
 		} else if(EmulatorConfig.communicationType==CommunicationType.file) {
 			ipcBase = new FilePacket(getExecutableAndArguments());
 		} else {
 			ipcBase = null;
 			misc.Error.showErrorAndExit("Incorrect communication type : " + EmulatorConfig.communicationType);
 		}
		
		return ipcBase;
	}

	private static void startEmulator(String emulatorArguments, IpcBase ipcBase) {
		if(EmulatorConfig.communicationType==CommunicationType.file) {
			// The emulator is not needed when we are reading from a file
			emulator = null;
		} else {
			
			if (EmulatorConfig.emulatorType==EmulatorType.pin) {
				emulator = new Emulator(EmulatorConfig.PinTool, EmulatorConfig.PinInstrumentor, 
						emulatorArguments, ((SharedMem)ipcBase).idToShmGet);
			} else if (EmulatorConfig.emulatorType==EmulatorType.qemu) {
				emulator = new Emulator(EmulatorConfig.QemuTool + " " + emulatorArguments);
			} else if (EmulatorConfig.emulatorType==EmulatorType.none) {
				emulator = null;
			} else {
				emulator = null;
				misc.Error.showErrorAndExit("Invalid emulator type : " + EmulatorConfig.emulatorType);
			}
		}
	}

	private static String constructEmulatorArguments(String benchmarkArguments) {
		String emulatorArguments = " ";
		
		if(EmulatorConfig.communicationType == CommunicationType.network) {
			System.out.println("Emulator argument passed! portStart is: "+Network.portStart);
			// Passing the start Port No through command line to the emulator
			emulatorArguments += "-P " + Network.portStart;	
		}
		
		if(EmulatorConfig.emulatorType == EmulatorType.qemu) {
			// send num instructions to skip and simulate to Qemu.
			// semantics : this fields apply locally to all the threads in Qemu.
			emulatorArguments += " -SO " + SimulationConfig.NumInsToIgnore 
					+ " -ST " + SimulationConfig.subsetSimSize;
		}
		
		// convention : benchmark specific arguments come at the end only.
		emulatorArguments += benchmarkArguments;
		return emulatorArguments;
	}

	// checks if the command line arguments are in required format and number
	private static void checkCommandLineArguments(String arguments[]) {
		if (arguments.length < 2) {
			Error.showErrorAndExit("\n\tIllegal number of arguments !!\n" +
					"Usage java main <config-file> <output-file> <benchmark-program and arguments>");
		}
	}
	
	/*
	 * debug helper functions
	 */

	/**
	 * @author Moksh
	 * For real-time printing of the running time, when program exited on request
	 */
	public static void printSimulationTime(long time)
	{
		//print time taken by simulator
		long seconds = time/1000;
		long minutes = seconds/60;
		seconds = seconds%60;
			System.out.println("\n");
			System.out.println("[Simulator Time]\n");
			
			System.out.println("Time Taken\t=\t" + minutes + " : " + seconds + " minutes");
			System.out.println("\n");
	}
	
	public static Emulator getEmulator() {
		return emulator;
	}
	
	private static void getMyPID() {
		pid = getPidThroughSystems();
	}

	private static int getPidThroughSystems() {
		// TODO: Not checked for Windows yet
		String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
		//System.out.println("ProcessName: " + processName);
		int x = (int) Long.parseLong(processName.split("@")[0]);
		return x;
	}

	public static String getEmulatorFile() {
		return emulatorFile;
	}

	public static void setEmulatorFile(String emulatorFile) {
		Main.emulatorFile = emulatorFile;
	}

	public static String[] getTraceFileBasename() {
		return traceFileBasename;
	}

	public static void setTraceFileBasename(String inputFile, int index) {
		traceFileBasename[index] = inputFile;
	}

	public static String[] getExecutableAndArguments() {
		return executableAndArguments;
	}

	public static String getBenchmarkArguments() {
		return benchmarkArguments;
	}
	
	public static long getStartTime() {
		return startTime;
	}
	
	private static void getBenchmarkPID()
	{
		try {//hopefully the benchmark should have started by this time!
			Runtime.getRuntime().exec("bash " + EmulatorConfig.GetBenchmarkPIDScript + " " + Main.pid);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
