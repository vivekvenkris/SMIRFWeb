package exceptions;

import bean.TCCStatus;

public class DriveBrokenException extends TCCException {

	public DriveBrokenException(String message, String trace) {
		super(message, trace);
	}
	public DriveBrokenException(String message) {
		super(message);
	}
	public DriveBrokenException(String message, String trace,TCCStatus status) {
		super(message, trace,status);
	}
}
