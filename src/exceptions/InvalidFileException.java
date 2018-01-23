package exceptions;

import java.io.File;
import java.util.List;

import mailer.InlineAttachment;
import util.SMIRFConstants;
import util.Utilities;

public class InvalidFileException extends CustomException{
	public InvalidFileException(String message, String trace) {
		super(message, trace);
		this.level = SMIRFConstants.levelFatal;

	}
	public InvalidFileException(String message) {
		super(message);
		this.level = SMIRFConstants.levelFatal;

	}
	
	
	protected InvalidFileException() {
		super();
		this.level = SMIRFConstants.levelFatal;

	}
	@Override
	public String getEmailSubject() {
		// TODO Auto-generated method stub
		return "Invalid file given for PDMP";
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
