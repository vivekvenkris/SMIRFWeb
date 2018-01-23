package exceptions;

import java.io.File;
import java.util.List;

import mailer.InlineAttachment;
import util.SMIRFConstants;
import util.Utilities;

public class ObservationException extends CustomException{
	public ObservationException(String message, String trace) {
		super(message, trace);
		this.level = SMIRFConstants.levelWarn;

	}
	public ObservationException(String message) {
		super(message);
		this.level = SMIRFConstants.levelWarn;

	}
	
	protected ObservationException() {
		super();
		this.level = SMIRFConstants.levelWarn;

	}
	@Override
	public String getEmailSubject() {
		return "Problem with pointings database or observation configureation.";
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
