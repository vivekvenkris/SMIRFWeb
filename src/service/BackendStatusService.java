package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import bean.BackendStatus;
import bean.DataBlock;
import exceptions.BackendException;
import mailer.Mailer;
import util.BackendConstants;
import util.ConfigManager;
import util.Utilities;

public class BackendStatusService implements BackendConstants{

	private boolean isON() throws BackendException{
		try {
			this.sendCommand(state);
			return true;
		} catch (BackendException e) {
			return false;
		}
	}

	public BackendStatus getBackendStatus() throws  BackendException{
		return new BackendStatus(isON(), sendCommand(state), sendCommand(startUTC));
	}


	public synchronized String sendCommand(String message) throws BackendException{
		try{
			String response = Utilities.talkToServer(message, backendIP, backendStatusPort);
			return response;		
		}
		catch (Exception e) {
			throw new BackendException(" Backend failed: Cause: "+ e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}

	public List<DataBlock> getDataBlocks(String nodeShortlist, Integer dbID ) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{

		String XMLString = getBuffersXML();

		dbf.setValidating(false);
		DocumentBuilder builder = dbf.newDocumentBuilder();

		Document doc = builder.parse( new InputSource( new StringReader( XMLString ) ) ); 
		XPathFactory factory = XPathFactory.newInstance();

		List<DataBlock> dataBlocksToCheck = new ArrayList<>();

		for(String computingNode: ConfigManager.getNodes().stream().filter(f -> f.contains(nodeShortlist)).collect(Collectors.toList())) {

			XPath xpath = factory.newXPath();
			String expression = "//*[@host='"+computingNode+"']/*[@db_id='"+dbID+"']";
			NodeList nodeList = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);

			for(int i=0; i<  nodeList.getLength(); i++ ) {

				DataBlock db = new DataBlock(computingNode, nodeList.item(i));
				dataBlocksToCheck.add(db);
			}

		}
		
		return dataBlocksToCheck;


	}


	public String getBuffersXML() {
		URL url;
		String pageText = "";
		try {
			url = new URL("http://localhost/mopsr/machine_summary.lib.php?update=true");
			URLConnection conn = url.openConnection();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
				pageText = reader.lines().collect(Collectors.joining("\n"));
			}
		} catch (IOException e) {
			e.printStackTrace();
			Mailer.sendEmail(e);
		}

		return pageText;

	}

	public static void main(String[] args) throws BackendException, InterruptedException {

		BackendStatusService service = new BackendStatusService();
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		while(true){
			System.err.println(stopWatch.getTime() + " " + service.getBackendStatus() );
			Thread.sleep(2000);
		}

	}


}
