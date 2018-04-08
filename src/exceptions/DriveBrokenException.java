package exceptions;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import bean.TCCStatus;
import util.Utilities;

public class DriveBrokenException extends TCCException {
	
	String arm;
	String drive;

	public DriveBrokenException(String message, String trace, String arm, String drive) {
		
		super(message, trace);
		this.arm = arm;
		this.drive = drive;
	}
	public DriveBrokenException(String message, String arm, String drive) {
		super(message);
		this.arm = arm;
		this.drive = drive;
	}
	public DriveBrokenException(String message, String trace,TCCStatus status, String arm, String drive) {
		super(message, trace,status);
		this.arm = arm;
		this.drive = drive;
		
		Utilities.prettyPrintXML(status.getXml());
		
	}
	public String getArm() {
		return arm;
	}
	public void setArm(String arm) {
		this.arm = arm;
	}
	public String getDrive() {
		return drive;
	}
	public void setDrive(String drive) {
		this.drive = drive;
	}
	
	
}
