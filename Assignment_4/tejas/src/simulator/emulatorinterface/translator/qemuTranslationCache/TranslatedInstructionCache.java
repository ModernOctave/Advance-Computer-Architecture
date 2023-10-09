package emulatorinterface.translator.qemuTranslationCache;

import main.CustomObjectPool;

import generic.Instruction;
import generic.InstructionList;

//Translation Cache for caching translations of qemu assembly instructions to micro-ops
public class TranslatedInstructionCache {
	private static final int cacheSize = 100000;
	private static long cacheHit =0;
	private static long cacheMiss =0;
	public static  LRUCache translatedInstructionTable;
	
	private static void createTranslatedInstructionTable(){
		translatedInstructionTable = new LRUCache(cacheSize);
	}
	
	public static void add(String asmText, InstructionList instructionList){
		if(translatedInstructionTable == null) {
			createTranslatedInstructionTable();
		}
		
		InstructionList instList = new InstructionList(instructionList.length());
		for(int i=0; i<instructionList.length(); i++) {
			Instruction newInsn = CustomObjectPool.getInstructionPool().borrowObject();
			newInsn.copy(instructionList.get(i));
			instList.appendInstruction(newInsn);
		}
		
		translatedInstructionTable.put(asmText, instList);
	}
	
	public static InstructionList getInstructionList(String instText) {
		if(translatedInstructionTable == null) {
			return null;
		}
		else {
			InstructionList instructionList = translatedInstructionTable.get(instText);
			InstructionList instructionListToReturn = new InstructionList(instructionList.getListSize());
		
			// increment references for each argument for each instruction
			for(int i=0; i<instructionList.length(); i++) {
				Instruction newInstruction = new Instruction();
				newInstruction.copy(instructionList.get(i));
				instructionListToReturn.appendInstruction(newInstruction);
			}
			return instructionListToReturn;
		}
	}
	
	public static boolean isPresent(String instText) {
		if(translatedInstructionTable == null) {
			return false;
		} else {
			boolean ret = translatedInstructionTable.containsKey(instText);
			
			if (ret) {
				cacheHit++;
			} else {
				cacheMiss++;
			}
			
			return ret;
		}
	}
	
	public static float getHitRate() {
		if((cacheHit+cacheMiss)==0) {
			return -1f;
		} else {
			return ((float)(cacheHit)/(float)(cacheHit+cacheMiss));
		}
	}
}

