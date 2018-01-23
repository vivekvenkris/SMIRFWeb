package exceptions;

import java.io.File;
import java.util.List;

import mailer.InlineAttachment;
import util.SMIRFConstants;

public class PointingException extends CustomException {

	public PointingException(String message) {
		message="Pointing exception. Cause:" +message;
		this.level = SMIRFConstants.levelWarn;

	}

	@Override
	public String getEmailSubject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEmailBody() {
		// TODO Auto-generated method stub
		return null;
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
