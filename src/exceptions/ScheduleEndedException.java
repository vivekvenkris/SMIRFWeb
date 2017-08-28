package exceptions;

public class ScheduleEndedException extends SchedulerException {
	public ScheduleEndedException(String message, String trace) {
		super(message,trace);
	}
	
	public ScheduleEndedException(String message) {
		super(message);
	}
	
	protected ScheduleEndedException(){
		super();
	}
}
