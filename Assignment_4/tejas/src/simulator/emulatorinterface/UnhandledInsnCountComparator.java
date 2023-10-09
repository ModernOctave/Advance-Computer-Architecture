package emulatorinterface;

import java.util.Comparator;
import java.util.HashMap;

public class UnhandledInsnCountComparator implements Comparator<Long> {

	public UnhandledInsnCountComparator(HashMap<Long, Long> hashMap) {
		super();
		this.hashMap = hashMap;
	}


	HashMap<Long,Long> hashMap;
	
	
	@Override
	public int compare(Long arg0, Long arg1) {
        if (hashMap.get(arg0) >= hashMap.get(arg1)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
	}

}
