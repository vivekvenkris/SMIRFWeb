package exceptions;

public class BackendException extends CustomException{
	public BackendException(String message, String trace) {
		super(message,trace);
	}
	
	public BackendException(String message) {
		super(message);
	}
	
	protected BackendException(){
		super();
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
		return message;
	}

}
