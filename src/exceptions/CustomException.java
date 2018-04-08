package exceptions;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import mailer.InlineAttachment;

public abstract class CustomException extends Exception{
	String message;
	String trace;
	Object statusObject;
	String level;
	String extra;
	
	
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

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}
	
	
	
	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	abstract public String getEmailSubject();
	abstract public String getEmailBody();
	abstract public List<InlineAttachment> getEmailInline();
	abstract public List<File> getEmailAttachments();
	
	

}
