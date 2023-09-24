package main;

import config.EmulatorConfig;
import config.EmulatorType;
import emulatorinterface.RunnableThread;
import emulatorinterface.communication.CustomAsmCharPool;
import generic.CustomInstructionPool;

public class CustomObjectPool {
	
	private static CustomInstructionPool instructionPool;
	private static CustomAsmCharPool customAsmCharPool;
	
	public static void initCustomPools(int maxApplicationThreads, int staticInstructionPoolSize) {
		
		// Create Pools of Instructions, Operands and AddressCarryingEvents
		int runTimePoolPerAppThread =  RunnableThread.INSTRUCTION_THRESHOLD;
		int staticTimePool = staticInstructionPoolSize;
		
		// best case -> single threaded application
		int minInstructionPoolSize = staticTimePool + runTimePoolPerAppThread;
		int maxInstructionPoolSize = staticTimePool + runTimePoolPerAppThread * maxApplicationThreads * 2;
				
		/* custom pool */
		System.out.println("creating instruction pool..");
		setInstructionPool(new CustomInstructionPool(minInstructionPoolSize, maxInstructionPoolSize));
		
		if(EmulatorConfig.emulatorType==EmulatorType.none) {
			System.out.println("creating custom asm-char pool. max threads = " + maxApplicationThreads);
			customAsmCharPool = new CustomAsmCharPool(maxApplicationThreads);
		}
	}

	public static CustomInstructionPool getInstructionPool() {
		return instructionPool;
	}

	public static void setInstructionPool(CustomInstructionPool instructionPool) {
		CustomObjectPool.instructionPool = instructionPool;
	}

	public static CustomAsmCharPool getCustomAsmCharPool() {
		return customAsmCharPool;
	}

	public static void setCustomAsmCharPool(CustomAsmCharPool customAsmCharPool) {
		CustomObjectPool.customAsmCharPool = customAsmCharPool;
	}
}
