package exceptions;

import java.io.File;
import java.util.List;

import bean.TCCStatus;
import mailer.InlineAttachment;
import util.SMIRFConstants;
import util.Utilities;

public class EphemException extends CustomException {


	public EphemException(String message, String trace) {
		super(message, trace);
		this.level = SMIRFConstants.levelFatal;

	}
	public EphemException(String message) {
		super(message);
		this.level = SMIRFConstants.levelFatal;

	}
	public EphemException(String message, String trace,TCCStatus status) {
		super(message, trace,status);
		this.level = SMIRFConstants.levelFatal;

				
	}
	
	protected EphemException() {
		super();
		this.level = SMIRFConstants.levelFatal;

	}
	@Override
	public String getEmailSubject() {
		// TODO Auto-generated method stub
		return "Problem with Precession (or) input coordinates";
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
