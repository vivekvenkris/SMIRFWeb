package exceptions;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import bean.TCCStatus;
import control.Control;
import mailer.InlineAttachment;
import util.SMIRFConstants;
import util.TCCConstants;
import util.Utilities;

public class TCCException extends CustomException{
	
	protected TCCException() {
		this.level = SMIRFConstants.levelFatal;

	}
	public TCCException(String message, String trace) {
		super(message,trace);
		this.level = SMIRFConstants.levelFatal;

	}
	public TCCException(String message){
		super(message);
		this.level = SMIRFConstants.levelFatal;

	}
	
	public TCCException(String message, String trace, TCCStatus status) {
		super(message,trace);
		this.statusObject = status;
		this.level = SMIRFConstants.levelFatal;

	}
	@Override
	public String getEmailSubject() {
	 	return "Problem with TCC (or) telescope motors";
	}
	@Override
	public String getEmailBody() {
	
		// To do: get TCC pings
		
		
		String body = Utilities.buildEmailBodyTextFromException(this);
		
		body += "<span style='font-size:medium'>";
		
		body += "I am trying some basic diagnostic information which may or may not be relevant to this error. <br/>";
		
		
		
		try {
			
			InetAddress tccControllerIP = InetAddress.getByName(TCCConstants.tccControllerIP);
			InetAddress tccStatusIP = InetAddress.getByName(TCCConstants.tccStatusIP);
			InetAddress eZ80NSIP = InetAddress.getByName(TCCConstants.eZ80NSIP);
			InetAddress eZ80MDIP = InetAddress.getByName(TCCConstants.eZ80MDIP);
			/**
			 * To do: this is stupid. What I should check for Anansi is if the ports are listening.
			 */
			
			body += " Trying to see if TCC Anansi controller is up and running: ";
			body += tccControllerIP.isReachable(2000) ? "Yes" : "<b>No</b>";
			body += "<br/>";
			
			body += "Trying to see if  TCC Anansi status controller is up and running: ";
			body += tccStatusIP.isReachable(2000) ? "Yes" : "<b>No</b>";
			body += "<br/>";
			
			body += "Trying to see if the NS eZ80 is up and running: ";
			body += eZ80NSIP.isReachable(2000) ? "Yes" : "<b>No</b>";
			body += "<br/>";
			
			body += "Trying to see if the MD eZ80 is up and running: ";
			body += eZ80MDIP.isReachable(2000) ? "Yes" : "<b>No</b>";
			body += "<br/>";
			
			body += "Here is a TCC status for your reference: <br/> </span>";
					
			body += "<textarea style='font-size:medium' rows=\"75\" cols=\"50\">" + Utilities.getPrettyPrintXML(Control.getTccStatus().getXml()) + "</textarea>";		
					
			body += "<br/>";
			
			body +=  "<span style='font-size:medium'>" + this.extra + "</span>";
			
		} catch (IOException e) {
			e.printStackTrace();
			body = "Problem generating this email body. Cause: " + e.getMessage();
		}

		
		return body;
	}
	@Override
	public List<InlineAttachment> getEmailInline() {
		
		return null;
	}
	@Override
	public List<File> getEmailAttachments() {
		return null;
	}	
}
