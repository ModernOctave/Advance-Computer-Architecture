package main;

import java.io.File;

import config.EmulatorConfig;
import config.EmulatorType;
import config.SimulationConfig;
import config.SystemConfig;

public class Emulator {
	
	private Process emulatorProcess;
	private String OS;	
	private boolean isStreamGobblerNeeded = true;
	
	TejasStreamGobbler s1;
	TejasStreamGobbler s2;
	
	public Emulator(String pinTool, String pinInstrumentor, 
			String executableArguments, int pid) 
	{
		OS = System.getProperty("os.name").toLowerCase();
		System.out.println("subset sim size = "  + 
				SimulationConfig.subsetSimSize + "\t" + 
				SimulationConfig.subsetSimulation);
		
		System.out.println("marker functions = "  + SimulationConfig.markerFunctionsSimulation 
				+ "\t start marker = " + SimulationConfig.startMarker
				+ "\t end marker = " + SimulationConfig.endMarker);

		// Creating command for PIN tool.
		StringBuilder pin = null;
		
		if(new File(pinTool + "/pin.sh").exists())
		{
			pin = new StringBuilder(pinTool + "/pin.sh");
		}
		else
		{
			pin = new StringBuilder(pinTool + "/pin");
		}
		StringBuilder cmd;
		if(OS.indexOf("win") >= 0)	{
			 cmd = new StringBuilder(pin +  //" -injection child "+
					" -t " + pinInstrumentor +
					" -maxNumActiveThreads " + (SystemConfig.maxNumJavaThreads*SystemConfig.numEmuThreadsPerJavaThread) +
					" -map " + SimulationConfig.MapEmuCores +
					" -numIgn " + SimulationConfig.NumInsToIgnore +
					" -numSim " + SimulationConfig.subsetSimSize +
					" -id " + pid + 
					" -traceMethod " + EmulatorConfig.communicationType.toString());
		}
		else{
			 cmd = new StringBuilder(pin + " -injection child "+
					" -t " + pinInstrumentor +
					" -maxNumActiveThreads " + (SystemConfig.maxNumJavaThreads*SystemConfig.numEmuThreadsPerJavaThread) +
					" -map " + SimulationConfig.MapEmuCores +
					" -numIgn " + SimulationConfig.NumInsToIgnore +
					" -numSim " + SimulationConfig.subsetSimSize +
					" -id " + pid + 
					" -traceMethod " + EmulatorConfig.communicationType.toString());
		}		
		if(SimulationConfig.pinpointsSimulation == true)
		{
			cmd.append(" -pinpointsFile " + SimulationConfig.pinpointsFile);
		}
		if(SimulationConfig.startMarker != "")
		{
			cmd.append(" -startMarker " + SimulationConfig.startMarker);
		}
		if(SimulationConfig.endMarker != "")
		{
			cmd.append(" -endMarker " + SimulationConfig.endMarker);
		}
		
		cmd.append(" -- " + executableArguments);
		System.out.println("command is : " + cmd.toString());
		
		startEmulator(cmd.toString());
	}
	
	public Emulator(String pinTool, String pinInstrumentor, 
			String executableArguments, String basenameForTraceFile) 
	{
	
		// This constructor is used for trace collection inside a file
		OS = System.getProperty("os.name").toLowerCase();	
		System.out.println("subset sim size = "  + 
				SimulationConfig.subsetSimSize + "\t" + 
				SimulationConfig.subsetSimulation);
		
		System.out.println("marker functions = "  + SimulationConfig.markerFunctionsSimulation 
				+ "\t start marker = " + SimulationConfig.startMarker
				+ "\t end marker = " + SimulationConfig.endMarker);

		// Creating command for PIN tool.
		StringBuilder pin = null;
		
		if(new File(pinTool + "/pin.sh").exists())
		{
			pin = new StringBuilder(pinTool + "/pin.sh");
		}
		else
		{
			pin = new StringBuilder(pinTool + "/pin");
		}

		StringBuilder cmd = new StringBuilder(pin +//  " -injection child "+
				" -t " + pinInstrumentor +
				" -maxNumActiveThreads  " + EmulatorConfig.maxThreadsForTraceCollection +
				" -map " + SimulationConfig.MapEmuCores +
				" -numIgn " + SimulationConfig.NumInsToIgnore +
				" -numSim " + SimulationConfig.subsetSimSize +
				" -traceMethod file -traceFileName " + basenameForTraceFile);
		
		if(SimulationConfig.pinpointsSimulation == true)
		{
			misc.Error.showErrorAndExit("Cannot create a trace file, and a pinpoints file at the same time !!");
		}
		if(SimulationConfig.startMarker != "")
		{
			cmd.append(" -startMarker " + SimulationConfig.startMarker);
		}
		if(SimulationConfig.endMarker != "")
		{
			cmd.append(" -endMarker " + SimulationConfig.endMarker);
		}
		
		cmd.append(" -- " + executableArguments);
		System.out.println("command is : " + cmd.toString());
		
		startEmulator(cmd.toString());
	}

	
	public Emulator(String qemuTool)
	{
		OS = System.getProperty("os.name").toLowerCase();
		startEmulator(qemuTool);
	}


	// Start the PIN process. Parse the cmd accordingly
	private void startEmulator(String cmd) {
		emulatorCommand = cmd;
		Runtime rt = Runtime.getRuntime();
		try {
			emulatorProcess = rt.exec(cmd);
			if(isStreamGobblerNeeded==true) {
				s1 = new TejasStreamGobbler ("stdin", emulatorProcess.getInputStream ());
				s2 = new TejasStreamGobbler ("stderr", emulatorProcess.getErrorStream ());
				
				s1.start ();
				s2.start ();
			}
		} catch (Exception e) {
			e.printStackTrace();
			misc.Error.showErrorAndExit("Error in starting the emulator.\n" +
					"Emulator Command : " + cmd);
		}
	}
	
	// Should wait for PIN too before calling the finish function to deallocate stuff related to
	// the corresponding mechanism
	public void waitForEmulator() {
		try {
			emulatorProcess.waitFor();
			if(isStreamGobblerNeeded==true) {
				s1.terminate();
				s2.terminate();
			}
		} catch (Exception e) { }
	}
	
	public void forceKill() {
		s1.terminate();
		s2.terminate();
		emulatorProcess.destroy();
		
		Main.ipcBase.finish();
		
		if(EmulatorConfig.emulatorType==EmulatorType.pin) {
			//System.err.println(errorMessage);
			Process process;
			if(OS.indexOf("win") >= 0)	{
		
				String cmd = "cmd.exe /c " +
		        EmulatorConfig.KillEmulatorScript+" "+String.valueOf(Main.pid);
				try 
				{
					process = Runtime.getRuntime().exec(cmd);
					TejasStreamGobbler s1 = new TejasStreamGobbler ("stdin", process.getInputStream ());
					TejasStreamGobbler s2 = new TejasStreamGobbler ("stderr", process.getErrorStream ());
					s1.start ();
					s2.start ();
					System.out.println("killing emulator process");
					process.waitFor();
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
	
			}
			else{
				  String cmd[] = {"/bin/bash",
				  EmulatorConfig.KillEmulatorScript,
				  String.valueOf(Main.pid)
			 	  };
    			  try 
				  {
						process = Runtime.getRuntime().exec(cmd);
						TejasStreamGobbler s1 = new TejasStreamGobbler ("stdin", process.getInputStream ());
						TejasStreamGobbler s2 = new TejasStreamGobbler ("stderr", process.getErrorStream ());
						s1.start ();
						s2.start ();
						System.out.println("killing emulator process");
						process.waitFor();
				  } 
				  catch (Exception e) 
				  {
						e.printStackTrace();
				  }
			}
		


		}
	}
	
	private static String emulatorCommand = null;

	public static String getEmulatorCommand() {
		return emulatorCommand;
	}
	
	
}
