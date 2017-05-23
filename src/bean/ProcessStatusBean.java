package bean;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessStatusBean {
	
	public static Map<String, Integer> statusCodes ;
	
	static{
		 Map<String, Integer> status = new HashMap<>();
		 status.put("observing",1);
		 status.put("queued",2);
		 status.put("smirfing",3);
		 status.put("smirfed",4);
		 status.put("folding",5);
		 status.put("folded",6);
		 status.put("transferring",7);
		 status.put("transferred",8);

		statusCodes = Collections.unmodifiableMap(status);
	}
	
	
	public static Map<Integer, Map<String, Integer> > status = new ConcurrentHashMap<>();
	
	

}
