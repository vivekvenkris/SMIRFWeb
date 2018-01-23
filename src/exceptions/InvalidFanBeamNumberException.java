package exceptions;

import java.io.File;
import java.util.List;

import mailer.InlineAttachment;
import util.SMIRFConstants;
import util.Utilities;

public class InvalidFanBeamNumberException extends CustomException {
	public InvalidFanBeamNumberException(String message, String trace) {
		super(message,trace);
		this.level = SMIRFConstants.levelFatal;

	}
	public InvalidFanBeamNumberException(String message){
		super(message);
		this.level = SMIRFConstants.levelFatal;

	}
	public InvalidFanBeamNumberException(String message, String trace, Integer fanbeam) {
		super(message,trace);
		this.statusObject = "FB number: " + fanbeam + " is invalid";
		this.level = SMIRFConstants.levelFatal;

	}
	protected InvalidFanBeamNumberException() {
		super();
		this.level = SMIRFConstants.levelFatal;

	}
	@Override
	public String getEmailSubject() {
		// TODO Auto-generated method stub
		return "Invalid number of fanbeams given."; 
	}
	@Override
	public String getEmailBody() {
		// TODO Auto-generated method stub
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
