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

	public DriveBrokenException(String message, String trace) {
		super(message, trace);
	}
	public DriveBrokenException(String message) {
		super(message);
	}
	public DriveBrokenException(String message, String trace,TCCStatus status) {
		super(message, trace,status);
		
		Utilities.prettyPrintXML(status.getXml());
		
	}
}
