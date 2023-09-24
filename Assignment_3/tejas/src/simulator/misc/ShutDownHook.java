package misc;

import generic.Statistics;
import main.Main;

public class ShutDownHook extends Thread {

	public void run()
	{
		if(Main.printStatisticsOnAsynchronousTermination == false) {
			// There is no need to write the statistics when tejas is used as a front end for collecting traces
			Runtime.getRuntime().halt(0);
		}

		try {
			Main.getEmulator().forceKill();
		} finally {
			System.out.println("shut down");

			if(Main.statFileWritten == false)
			{
				Statistics.printAllStatistics(Main.getEmulatorFile(), -1, -1);
			}
			//Runtime.getRuntime().runFinalization();
			Runtime.getRuntime().halt(0);
		}
	}

}
