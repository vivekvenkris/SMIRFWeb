package util;

import java.util.Arrays;
import java.util.List;

public interface PSRCATConstants {
	List<String> psrcatDBs = Arrays.asList( SMIRFConstants.simulate ? "/Users/vkrishnan/softwares/psrcat_tar_new_jan10/self.db": "/home/vivek/SMIRFneeds/self.db");
	
	String PSRJ = "PSRJ";
	String RAJ = "RAJ";
	String DECJ = "DECJ";
	String DM = "DM";
	Integer endOfName=8;
	Integer endofValue = 34;

}
