package exceptions;

import util.SMIRFConstants;

public class ScheduleEndedException extends SchedulerException {
	public ScheduleEndedException(String message, String trace) {
		super(message,trace);
		this.level = SMIRFConstants.levelInfo;

	}
	
	public ScheduleEndedException(String message) {
		super(message);
		this.level = SMIRFConstants.levelInfo;

	}
	
	protected ScheduleEndedException(){
		super();
		this.level = SMIRFConstants.levelInfo;

	}
	
	
}
