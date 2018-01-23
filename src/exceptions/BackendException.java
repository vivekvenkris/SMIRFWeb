package exceptions;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.DownloadedContent.OnFile;

import bean.BackendStatus;
import control.Control;
import mailer.InlineAttachment;
import service.EphemService;
import util.BackendConstants;
import util.ConfigManager;
import util.SMIRFConstants;
import util.TCCConstants;
import util.Utilities;

public class BackendException extends CustomException{
	
	protected BackendException() {
		super();
		this.level = SMIRFConstants.levelFatal;

	}
	
	public BackendException(String message, String trace) {
		super(message,trace);
		this.level = SMIRFConstants.levelFatal;

	}
	
	public BackendException(String message) {
		super(message);
		this.level = SMIRFConstants.levelFatal;

	}
	
	
	
	public String getTrace() {
		return trace;
	}



	public void setTrace(String trace) {
		this.trace = trace;
	}



	public void setMessage(String message) {
		this.message = message;
	}



	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getEmailSubject() {
		// TODO Auto-generated method stub
		return "Problem with backend. ";
	}

	@Override
	public String getEmailBody() {
		// TODO Auto-generated method stub
		
		String body = Utilities.buildEmailBodyTextFromException(this);
		
		body += "<p> I am trying some basic diagnostic information which may or may not be relevant to this error.</p> <br/> <p>";

		
		try {

			InetAddress backendIP = InetAddress.getByName(BackendConstants.backendIP);


			body += " Trying to see if Backend server (mpsr-srv0) is up and running: ";
			body += backendIP.isReachable(2000) ? "Yes" : "No";
			body += "<br/>";

			
			body += "Here is a backend status: </p> <br/> ";
			body +=  Control.getBackendStatus();
			body += "<br/>";
			
			
			body += "You can also see a snapshot of the backend controls page below.";
										
			body += "<br/> </p> ";
			
		} catch (IOException e) {
			e.printStackTrace();
			body = "Problem generating this email body. Cause: " + e.getMessage();

		}

		
		
		
		return body;
	}

	@Override
	public List<InlineAttachment> getEmailInline() {
		
		List<InlineAttachment> inlineAttachments = new ArrayList<>();
		
		String utc = EphemService.getUtcStringNow();

		
		try {
			
			
			String command = "cd /home/vivek/SMIRF/screenshots/; "
					+ "/home/vivek/.npm-global/bin/pageres -d 5 --filename='"+ utc + "' 'http://mpsr-srv0/mopsr/control.lib.php?single=true'";
			
			Utilities.runShellProcess(command, true);
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return null;

		} 
		
		File dir = new File(ConfigManager.getSmirfMap().get("BACKEND_SCREENSHOTS_DIR"));
		
		File picture = new File(dir, utc+".png");
		
		inlineAttachments.add(new InlineAttachment("Backend controls page screenshot", "screenshot stored at: "+ picture.getAbsolutePath(), picture) );
		
		return inlineAttachments;
	}

	@Override
	public List<File> getEmailAttachments() {
		// TODO Auto-generated method stub
		return null;
	}

}
