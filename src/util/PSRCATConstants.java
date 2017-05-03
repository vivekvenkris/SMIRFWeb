package util;

import java.util.Arrays;
import java.util.List;

public interface PSRCATConstants {
	List<String> psrcatDBs = Arrays.asList( ConfigManager.getSmirfMap().get("PSRCAT"));
	
	String PSRJ = "PSRJ";
	String RAJ = "RAJ";
	String DECJ = "DECJ";
	String DM = "DM";
	Integer endOfName=8;
	Integer endofValue = 34;

}
