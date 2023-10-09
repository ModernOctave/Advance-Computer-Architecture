package config;

public class EmulatorConfig {
	
	public static CommunicationType communicationType;
	public static EmulatorType emulatorType;

	public static int maxThreadsForTraceCollection = 1024;
	public static boolean storeExecutionTraceInAFile;
	public static String basenameForTraceFiles;
	
	public static String PinTool = null;
	public static String PinInstrumentor = null;
	public static String QemuTool = null;
	public static String ShmLibDirectory;
	public static String GetBenchmarkPIDScript;
	public static String KillEmulatorScript;
}
