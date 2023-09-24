
package emulatorinterface.translator.qemuTranslationCache;

import java.util.LinkedHashMap;
import java.util.Map;

import main.CustomObjectPool;
import generic.InstructionList;

public class LRUCache extends LinkedHashMap<String, InstructionList> {
	private static final float loadFactor = 1f;
	private final int cacheSize;
	
	public LRUCache(final int cacheSize) {
		super((int)Math.ceil(cacheSize/loadFactor) + 1, loadFactor, true);
		this.cacheSize = cacheSize;
	}
	
	@Override protected boolean removeEldestEntry(final Map.Entry<String, InstructionList> eldest) {
		if(super.size() > cacheSize) {
			InstructionList eldestList = eldest.getValue();
			for(int i=0; i<eldestList.length(); i++) {
				CustomObjectPool.getInstructionPool().returnObject(eldestList.get(i));	
			}
			return true;
		} else {
			return false;
		}
	}
}