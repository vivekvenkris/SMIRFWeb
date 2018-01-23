package exceptions;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import bean.CoordinateTO;
import mailer.InlineAttachment;
import util.SMIRFConstants;
import util.Utilities;

public class CoordinateOverrideException  extends CustomException{
	
	public CoordinateOverrideException(String message, CoordinateTO coordinateTO) {
		super(message,coordinateTO);
	}
	
	public CoordinateOverrideException(String message, String trace, CoordinateTO coords) {
		super(message,trace,coords);
	}
	
	public CoordinateOverrideException(String message) {
		super(message);
	}
	
	protected CoordinateOverrideException(){
		super();
		this.level = SMIRFConstants.levelDebug;

	}

	@Override
	public String getEmailSubject() {
		// TODO Auto-generated method stub
		return "Coordinate Override Exception. Possible expert level bug - contact Vivek V K.";
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
