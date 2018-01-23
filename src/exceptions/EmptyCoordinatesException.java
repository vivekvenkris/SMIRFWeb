package exceptions;

import java.io.File;
import java.util.List;

import bean.CoordinateTO;
import mailer.InlineAttachment;
import util.SMIRFConstants;
import util.Utilities;

public class EmptyCoordinatesException extends CustomException{
	
	public EmptyCoordinatesException(String message, String trace, CoordinateTO coords) {
		super(message,trace,coords);
		this.level = SMIRFConstants.levelDebug;

	}
	public EmptyCoordinatesException(String message, CoordinateTO coords) {
		super(message,coords);
		this.level = SMIRFConstants.levelDebug;

	}
	public EmptyCoordinatesException(String message, String trace) {
		super(message,trace);
		this.level = SMIRFConstants.levelDebug;

	}
	
	public EmptyCoordinatesException(String message) {
		super(message);
		this.level = SMIRFConstants.levelDebug;

	}
	
	protected EmptyCoordinatesException(){
		super();
		this.level = SMIRFConstants.levelDebug;
	}
	@Override
	public String getEmailSubject() {
		// TODO Auto-generated method stub
		return "Empty Coordinates Exception. Possible expert level bug - contact Vivek V K.";
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
