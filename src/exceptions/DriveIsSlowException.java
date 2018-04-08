package exceptions;

import bean.TCCStatus;
import util.Utilities;

public class DriveIsSlowException extends TCCException {


	public DriveIsSlowException(String message, String trace) {
		
		super(message, trace);
	}
	public DriveIsSlowException(String message) {
		super(message);
	
	}
	public DriveIsSlowException(String message, String trace,TCCStatus status) {
		super(message, trace,status);

		Utilities.prettyPrintXML(status.getXml());
		
	}
	
	
	
}
