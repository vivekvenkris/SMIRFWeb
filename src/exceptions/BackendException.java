package exceptions;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import bean.BackendStatus;
import bean.DataBlock;
import control.Control;
import mailer.InlineAttachment;
import service.BackendStatusService;
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
		
		body +="<h2>Preliminary diagnostic information</h2>";
		
		body +="<span style='font-size:medium'>";
		
		body += " I am trying some basic diagnostic information which may or may not be relevant to this error.<br/> <br/>";

		
		try {

			InetAddress backendIP = InetAddress.getByName(BackendConstants.backendIP);


			body += " Trying to see if Backend server (" + backendIP.getHostName() + ") is up and running: ";
			body += "<b>" + (backendIP.isReachable(2000) ? "Yes" : "No") + "</b>";
			body += "<br/>";
			
			body += "Here is a backend status: ";
			body +=  "<b>" + Control.getBackendStatus() + "</b>";
			body += "<br/><br/>";
			
			
			body += "Trying to see if all the nodes in use are pingable: <br/><br/> </span>";
			
			body += "<table style='font-size: medium; border: 1px solid black;'>";
			
			body += "<tr style='border: 1px solid black; padding: 10px'>"
					+ "<th style='border: 1px solid black; padding: 10px' colspan='2'>NODE</th>"
					+ "<th style='border: 1px solid black; padding: 10px'>IP</th>"
					+ "<th style='border: 1px solid black; padding: 10px'>PING</th>"
					+ "</tr>";
			
			for(String hostname: ConfigManager.getNodes()) {
				
				InetAddress ip = InetAddress.getByName(hostname);
				
				body += "<tr style='border: 1px solid black; padding: 10px'>"
						+ "<td style='border: 1px solid black; padding: 10px; ' colspan='2'>"+hostname+"</td>"
						+ "<td style='border: 1px solid black; padding: 10px'>"+ip.getHostAddress()+"</td>"
						+ "<td style='border: 1px solid black; padding: 10px'>"+ (ip.isReachable(2000)? "Yes" : "No") +"</td>"
						+ "</tr>";
			
			}
			
			body +="</table> <br/>";
			
			body += "<span style='font-size:medium'> Checking if any of the AQ datablocks with db_id=0 has overflown. "
					+ "This is an indicative of the backend failing. </span> <br/> <br/> ";
			
			
			List<DataBlock> dblocks = new BackendStatusService().getDataBlocks("aq", 0);
			
			body += "<table style='font-size: medium; border: 1px solid black;'>";
			
			body += "<tr style='border: 1px solid black; padding: 10px'>"
					+ "<th style='border: 1px solid black; padding: 10px'>NODE</th>"
					+ "<th style='border: 1px solid black; padding: 10px'>KEY</th>"
					+ "<th style='border: 1px solid black; padding: 10px'>OVERFLOWN?</th>"
					+ "<th style='border: 1px solid black; padding: 10px'>KEY</th>"
					+ "<th style='border: 1px solid black; padding: 10px'>OVERFLOWN?</th>"
					+ "<th style='border: 1px solid black; padding: 10px'>KEY</th>"
					+ "<th style='border: 1px solid black; padding: 10px'>OVERFLOWN?</th>"
					+ "<th style='border: 1px solid black; padding: 10px'>KEY</th>"
					+ "<th style='border: 1px solid black; padding: 10px'>OVERFLOWN?</th>"
					+ "</tr>";
			
			Map<String,List<String>> overflowStringMap = new TreeMap<>();
			
			for(DataBlock block: dblocks) {
				
				List<String> strings = overflowStringMap.getOrDefault(block.getNode(),new ArrayList<>() );
				strings.add("<td style='border: 1px solid black; padding: 10px'>"+block.getKey()+"</td>"
						+ "<td style='border: 1px solid black; padding: 10px'>"+ (block.hasOverflown()? "<b>Yes</b>" : "No") +"</td>");
				
				overflowStringMap.put(block.getNode(), strings);
				
//				body += "<tr style='border: 1px solid black; padding: 10px'>"
//						+ "<td style='border: 1px solid black; padding: 10px; >"+block.getNode()+"</td>"
//						+ "<td style='border: 1px solid black; padding: 10px'>"+block.getKey()+"</td>"
//						+ "<td style='border: 1px solid black; padding: 10px'>"+ (block.hasOverflown()? "<b>Yes</b>" : "No") +"</td>"
//						+ "</tr>";
//				
				
			}
			
			Set<Entry<String, List<String>>> entrySet = overflowStringMap.entrySet();
			
			for(Entry<String, List<String>> e : entrySet) {
				body += "<tr style='border: 1px solid black; padding: 10px'>"
						+ "<td style='border: 1px solid black; padding: 10px;' >"+e.getKey()+"</td>";
				
				for(String s: e.getValue()) {
					body +=s;
				}
				
				body +="</tr>";
				
			}
			
			body +="</table> <br/>";
 
			System.err.println(body);
			
			body += "<span style='font-size:medium'>Here is a screenshot of the controls page and the buffers page: <br/>";
										
			body += "<br/> </span> ";
			
		} catch (IOException | XPathExpressionException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
			body += "Problem generating the rest of this email body. Cause: " + e.getMessage();

		} 

			
		
		return body;
	}

	@Override
	public List<InlineAttachment> getEmailInline() {
		
		List<InlineAttachment> inlineAttachments = new ArrayList<>();
		

		Map<String, String> pageMap = new LinkedHashMap<>();
		
		pageMap.put("Backend_Controls", "http://mpsr-srv0/mopsr/control.lib.php?single=true");
		pageMap.put("AQ_Buffers", "http://mpsr-srv0/mopsr/area_summary.lib.php?single=true&area=aq");
		pageMap.put("BF_Buffers", "http://mpsr-srv0/mopsr/area_summary.lib.php?single=true&area=bf");
		pageMap.put("BP_Buffers", "http://mpsr-srv0/mopsr/area_summary.lib.php?single=true&area=bp");
		pageMap.put("SMIRF_Buffers", "http://mpsr-srv0/mopsr/area_summary.lib.php?single=true&area=bs");

		String utc = EphemService.getUtcStringNow();
		
		pageMap.entrySet().stream().forEach( f ->
		{ 
			String name = utc+"_"+f.getKey();

			try {
				
				
				String command = "cd /home/vivek/SMIRF/screenshots/; "
						+ "/home/vivek/.npm-global/bin/pageres -d 15 --filename='"+ name + "' '"+ f.getValue() +"'";
				
				Utilities.runShellProcess(command, true);
				
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
	
			} 
			
			File dir = new File(ConfigManager.getSmirfMap().get("BACKEND_SCREENSHOTS_DIR"));
			
			File picture = new File(dir, name+".png");
			
			inlineAttachments.add(new InlineAttachment(f.getKey(), "<span style='font-size:medium'> screenshot stored at: "+ picture.getAbsolutePath() + "</span>", picture) );
		});	
		return inlineAttachments;
	}

	@Override
	public List<File> getEmailAttachments() {
		// TODO Auto-generated method stub
		return null;
	}

}
