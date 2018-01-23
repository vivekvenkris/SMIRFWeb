package exceptions;

import java.io.File;
import java.util.List;

import mailer.InlineAttachment;
import util.SMIRFConstants;
import util.Utilities;

public class NoSourceVisibleException extends CustomException{
	public NoSourceVisibleException(String message, String trace) {
		super(message, trace);
		this.level = SMIRFConstants.levelFatal;

	}
	public NoSourceVisibleException(String message) {
		super(message);
		this.level = SMIRFConstants.levelFatal;

	}
	protected NoSourceVisibleException() {
		super();
		this.level = SMIRFConstants.levelFatal;

	}
	@Override
	public String getEmailSubject() {
		return "No Source is visible to observe.";
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
