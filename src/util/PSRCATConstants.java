package util;

import java.util.Arrays;
import java.util.List;

public interface PSRCATConstants {
	List<String> psrcatDBs = Arrays.asList( ConfigManager.getSmirfMap().get("PSRCAT").split(","));
	
	String PSRJ = "PSRJ";
	String RAJ = "RAJ";
	String DECJ = "DECJ";
	String F0 = "F0";
	String P0 = "P0";
	String DM = "DM";
	Integer endOfName=8;
	Integer endofValue = 34;

}
