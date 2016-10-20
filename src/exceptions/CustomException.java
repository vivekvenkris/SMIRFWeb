package exceptions;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class CustomException extends Exception{
	String message;
	String trace;
	Object statusObject;
	
	protected CustomException(String message, String trace, Object statusObject) {
		this.message = message;
		this.trace = trace;
		this.statusObject = statusObject;
	}
	
	protected CustomException(String message, String trace) {
		this.message = message;
		this.trace = trace;
	}
	
	protected CustomException(String message) {
		this.message = message;
		this.trace = ExceptionUtils.getStackTrace(this);
	}
	
	protected CustomException(String message, Object statusObject) {
		this.message = message;
		this.trace = ExceptionUtils.getStackTrace(this);
		this.statusObject = statusObject;

	}
	
	protected CustomException(){
		this.trace = ExceptionUtils.getStackTrace(this);
	}
	
	
	
	public String getTrace() {
		return trace;
	}



	public void setTrace(String trace) {
		this.trace = trace;
	}



	public void setMessage(String message) {
		this.message = message;
	}



	@Override
	public String getMessage() {
		if(statusObject!=null)
		return message + statusObject.toString();
		return message;
	}

	public Object getStatusObject() {
		return statusObject;
	}

	public void setStatusObject(Object statusObject) {
		this.statusObject = statusObject;
	}
	
	

}
