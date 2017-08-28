package exceptions;

import bean.TCCStatus;
import util.Utilities;

public class EphemException extends CustomException {


	public EphemException(String message, String trace) {
		super(message, trace);
	}
	public EphemException(String message) {
		super(message);
	}
	public EphemException(String message, String trace,TCCStatus status) {
		super(message, trace,status);
				
	}
}
