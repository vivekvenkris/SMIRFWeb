package exceptions;

public class SchedulerException extends CustomException {
	
	public SchedulerException(String message, String trace) {
		super(message,trace);
	}
	
	public SchedulerException(String message) {
		super(message);
	}
	
	protected SchedulerException(){
		super();
	}
	
	
}
