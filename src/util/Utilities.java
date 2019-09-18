package util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import bean.Angle;
import exceptions.CoordinateOverrideException;
import exceptions.CustomException;
import exceptions.EmptyCoordinatesException;

public class Utilities {

	public static String asq(String str){ return "'"+str +"'";}

	public static double getFB(int totalFB, double boresightMD, double sourceMD){
		return totalFB*( ( (sourceMD-boresightMD)*Constants.rad2Deg/ (2.0/Math.cos((boresightMD+sourceMD)/2.0) ) )+1)/2.0 + 1;
	}

	public static void main(String[] args) throws EmptyCoordinatesException, CoordinateOverrideException {
		
		
		System.err.println(putSpacesBetweenWords("HrlloMisterEthirKatchi"));
		
		System.exit(0);

		for(int i=-60;i<60; i++){
			double beam = 4.0*Constants.deg2Rad/ Math.cos(i*Constants.deg2Rad);
			double deltaMD = beam/ 352;
			for(int fb=1;fb<=352;fb++){
				double solved = getMDDistance(fb, 352, i*Constants.deg2Rad);
				System.err.println(i + " " + fb + " " + solved*Constants.rad2Deg
						+ " "+  (fb-(352/2 +1)) * deltaMD*Constants.rad2Deg + " "+ (solved -(fb-(352/2 +1)) * deltaMD)*Constants.rad2Deg );
			}
		}
		System.exit(0);
		int nfb = 352;
		for(int i=-60;i<60; i++){
			double md = i* Constants.deg2Rad;
			double beam = 4.0*Constants.deg2Rad/ Math.cos(md);
			double deltaMD = beam/ nfb;
			for(double j=-beam/2.0;j<beam/2.0; j=j+23* Constants.arcSec2Deg * Constants.deg2Rad){
				double offset = j*Constants.deg2Rad;
				double expectedFB = getFB(nfb, md, md + offset);
				double gotFB = ((offset)/deltaMD) + nfb/2.0 + 1;
				System.err.println( i +  " " + j +" " + expectedFB + " "+ gotFB + " " + (expectedFB - gotFB));
			}
		}
	}

	public static double getMDDistance(double fb, int totalFB, double boresightMD){
		UnivariateFunction uf = new UnivariateFunction() {

			@Override
			public double value(double sourceMD) {
				return getFB(totalFB,boresightMD,sourceMD)-fb;
			}
		};
		BrentSolver solver = new BrentSolver();
		double sourceMD = solver.solve(10000, uf, boresightMD -10*Constants.deg2Rad, boresightMD+10*Constants.deg2Rad);
		return sourceMD - boresightMD;
	}

	public static String getTextFromXpath(String xmlString, String xpathExpr) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
		
		return getNodeFromXpath(xmlString,xpathExpr).getTextContent();
	}
	
	public static Node getNodeFromXpath(String xmlString, String xpathExpr) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
		DocumentBuilder builder;  

		builder = factory.newDocumentBuilder();  
		Document document = builder.parse( new InputSource( new StringReader( xmlString ) ) ); 
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile(xpathExpr);
		Object result = expr.evaluate(document, XPathConstants.NODE);
		Node node = (Node)result;
		return node;
	}
	
	

	public static String talkToServer(String xmlMessage, String ip, int port) throws IOException{
		try {
			String xmlResponse;
			//System.out.println("TO SERVER: " + xmlMessage + " "+ ip + " " + port);
			Socket client = new Socket(ip, port);
			client.setSoTimeout(20000);
			DataOutputStream outToServer = new DataOutputStream(client.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(client.getInputStream()));
			outToServer.writeBytes(xmlMessage.replace("\n", " ") +"\r\n");
			xmlResponse = inFromServer.readLine();
			//System.out.println("FROM SERVER: " + xmlResponse);

			client.close();
			return xmlResponse ;
		}catch(UnknownHostException  e){
			e.printStackTrace();
			throw e;
		}
		catch(ConnectException  e){
			throw e;
		} catch (IOException e) {
			throw e;
		}
	}

	public static String talkToUDPServer(String xmlMessage, String ip, int port) throws IOException{
		BufferedReader inFromUser =
				new BufferedReader(new InputStreamReader(System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName(ip);
		byte[] sendData = xmlMessage.getBytes();
		byte[] receiveData = new byte[64000];
		String sentence = inFromUser.readLine();
		sendData = sentence.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
		clientSocket.send(sendPacket);
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		String xmlResponse = new String(receivePacket.getData());
		System.out.println("FROM SERVER:" + xmlResponse);
		clientSocket.close();
		return xmlResponse;
	}

	public static String runShellProcess(String process, boolean waitFor) throws IOException, InterruptedException{
		String[] cmd = new String[]{"/bin/csh","-c",process};
		Process p = Runtime.getRuntime().exec(cmd);

		// cause this process to stop until process p is terminated
		BufferedReader in = new BufferedReader(
				new InputStreamReader(p.getInputStream()));
		String str = "";
		String line = null;
		while ((line = in.readLine()) != null) {
			str+=line;
		}
		if(waitFor)p.waitFor();
		return str;
	}

	public static String runSSHProcess(String userAtHost, String process, String input, boolean waitFor) throws IOException, InterruptedException{
		String[] cmd = new String[]{"ssh","-t",userAtHost, " " + process+" "};
		System.err.println(Arrays.asList(cmd));
		Process p = Runtime.getRuntime().exec(cmd);

		// cause this process to stop until process p is terminated
		BufferedReader err = new BufferedReader(
				new InputStreamReader(p.getErrorStream()));
		BufferedReader in = new BufferedReader(
				new InputStreamReader(p.getInputStream()));
		String str = "";
		String line = null;
		while ((line = in.readLine()) != null) {
			str+=line + "\n";
		}

		while ((line = err.readLine()) != null) {
			str+=line + "\n";
		}
		p.waitFor();
		return str;
	}

	public static double getEuclideanDistance(double x1, double y1, double x2, double y2){

		double xdist = Math.abs(x2 - x1);
		double ydist = Math.abs(y2 - y1);

		return ( Math.sqrt(xdist*xdist + ydist*ydist));
	}

	public static boolean isWithinCircle(double x1, double y1, double x2, double y2, double radius){
		
		double ydist = Math.abs(y2 - y1);
		double xdist = Math.abs(x2 - x1)*Math.cos(0.5*Math.abs(y1+y2));

		return ( (xdist*xdist + ydist*ydist) <= radius*radius);

	}
	
	public static double equatorialDistance(double x1, double y1, double x2, double y2){
		
		double ydist = Math.abs(y2 - y1);
		double xdist = Math.abs(x2 - x1) * Math.cos(0.5*Math.abs(y1+y2));

		return ( (xdist*xdist + ydist*ydist));

	}

	public static boolean isWithinEllipse(double x1, double y1, double x2, double y2, double a, double b){
		double xdist = Math.abs(x2 - x1)* Math.cos(0.5*Math.abs(y1+y2));
		double ydist = Math.abs(y2 - y1);

		return ( ( xdist*xdist/(a*a) + ydist*ydist/(b*b)) <= 1.0);
	}

	public static double distance(double x1, double y1, double x2, double y2){
		double xdist = Math.abs(x2 - x1)/Math.cos(0.5*(y1+y2));
		double ydist = Math.abs(y2 - y1);

		return Math.sqrt(xdist*xdist + ydist*ydist);
	}

	public static double distance( Angle RA1, Angle DEC1, Angle RA2, Angle DEC2){
		return distance(RA1.getRadianValue(), DEC1.getRadianValue(), RA2.getRadianValue(), DEC2.getRadianValue());
	}


	public static String getUTCString(LocalDateTime utcTime){
		return utcTime.format(DateTimeFormatter.ofPattern(BackendConstants.backendUTCFormatOfPattern));
	}

	public static LocalDateTime getUTCLocalDateTime(String utc){
		if(!utc.contains(".")) utc += ".000";
		return LocalDateTime.parse(utc, DateTimeFormatter.ofPattern(BackendConstants.backendUTCFormatOfPattern));
	}
	
	public static double getTimeDifferenceInDays(LocalDateTime init, LocalDateTime fin) {
		
		Duration duration = Duration.between(init, fin);
		
		return duration.getSeconds()  * Constants.sec2Hrs * Constants.hrs2Day;
		
	}



	public static String createDirectoryStructure(String...names){
		String result = "";
		for( int i=0; i< names.length; i++){
			result += names[i];
			if(i != names.length -1 ) result += "/";
		}
		return result;
	}
	
	public static void prettyPrintXML(String xml){
		System.err.println(getPrettyPrintXML(xml));

	}


	public static String getPrettyPrintXML(String xml){
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			//initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(Utilities.parseXml(xml));
			transformer.transform(source, result);
			String xmlString = result.getWriter().toString();
			return xmlString;
			
		} catch (TransformerConfigurationException | TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "Problem with XML pretty print";


	}

	public static Document parseXml(String in)
	{
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(in));
			return db.parse(is);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static String buildEmailBodyTextFromException(CustomException e) {
		String body = "<h2>SMIRF threw a " + putSpacesBetweenWords(e.getClass().getSimpleName()) + " during operation.<h2>";
		
		body += "<h2>Message:" + e.getMessage() + "</h2> <br/>";
		
		body += "<span style='font-size:medium'>Here is a stacktrace: <br/>" + e.getTrace().replaceAll("\n", "<br/>") + "</span><br/><br/>";
		return body;
	}
	
	public static String putSpacesBetweenWords(String text) {
		
		return text.replaceAll("\\d+", "").replaceAll("(.)([A-Z])", "$1 $2");
		
	}
	
	
	public static long largestPowerOf2(long n) {
	    return (long)Math.pow(2, Math.floor(Math.log(n) / Math.log(2)));
	}

}
