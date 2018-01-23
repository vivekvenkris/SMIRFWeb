package exceptions;

import java.io.File;
import java.util.List;

import mailer.InlineAttachment;
import util.SMIRFConstants;
import util.Utilities;

public class SchedulerException extends CustomException {
	
	public SchedulerException(String message, String trace) {
		super(message,trace);
		this.level = SMIRFConstants.levelWarn;
	}
	
	public SchedulerException(String message) {
		super(message);
		this.level = SMIRFConstants.levelWarn;

	}
	
	protected SchedulerException(){
		super();
		this.level = SMIRFConstants.levelWarn;

	}

	@Override
	public String getEmailSubject() {
		return "Scheduler exception";
	}

	@Override
	public String getEmailBody() {

		
		return Utilities.buildEmailBodyTextFromException(this);
	}

	@Override
	public List<InlineAttachment> getEmailInline() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<File> getEmailAttachments() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
