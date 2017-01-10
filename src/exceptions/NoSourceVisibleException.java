package exceptions;

import bean.TCCStatus;

public class NoSourceVisibleException extends CustomException{
	public NoSourceVisibleException(String message, String trace) {
		super(message, trace);
	}
	public NoSourceVisibleException(String message) {
		super(message);
	}
	
}
