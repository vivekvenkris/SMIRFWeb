package service;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import bean.Observation;
import exceptions.BackendException;
import exceptions.BackendInInvalidStateException;
import exceptions.UnexpectedBackendReplyException;
import util.BackendConstants;
import util.SMIRFConstants;
import util.Utilities;


class Lights{
	int red,green,yellow,grey;

	public Lights() {
		red = green = yellow = grey = 0;
	}
}

public class BackendService implements BackendConstants {
	static BackendService backendService = null;
	Boolean statusService = true;
	public static BackendService createBackendInstance(){
		if(backendService == null) backendService = new BackendService(false);
		return backendService;
	}

	public static BackendService createBackendStatusInstance(){
		return new BackendService(true);
	}

	private BackendService(Boolean statusService){
		this.statusService = statusService;
	}

	private void setUpBackendForObservation(Observation observation) throws BackendException{

		defaultParams.put("tobs",observation.getTobs().toString());


		defaultParams.put("observer","SMIRFWeb/"+ observation.getObserver());
		defaultParams.put("project_id",SMIRFConstants.PID);

		defaultParams.put("source_name",observation.getName());
		defaultParams.put("ra",observation.getAngleRA().toHHMMSS());
		defaultParams.put("dec",observation.getAngleDec().toDDMMSS());
		defaultParams.put("config",observation.getObsType());


		switch(observation.getBackendType()){
		case corrBackend:
			defaultParams.put("mode","CORR");
			defaultParams.put("aq_proc_file","mopsr.aqdsp.unscaled.gpu");
			defaultParams.put("bf_proc_file","mopsr.calib.pref16.gpu");
			defaultParams.put("bp_proc_file","mopsr.null");
			break;
		case psrBackend:
			defaultParams.put("mode","PSR");
			defaultParams.put("aq_proc_file","mopsr.aqdsp.gpu");
			defaultParams.put("bf_proc_file","mopsr.dspsr.cpu.cdd");
			defaultParams.put("bp_proc_file","mopsr.null");

			switch(observation.getObsType()){
			case tiedArrayFanBeam:
				break;
			case fanBeam:
				break;
			}
			break;
		}

		prepareBackend();

	}

	private void prepareBackend() throws BackendException{
		if(!isON()) throw new BackendException("Backend failed: Cause: Backend not ON "); 
		try {
			String response = this.sendCommand(prepare);
			if(!response.equals(backendResponseSuccess)) throw new UnexpectedBackendReplyException(prepare, response);
		}catch (ConnectException e) {
			throw new BackendException("Backend failed: Cause: " , ExceptionUtils.getStackTrace(e));  
		}

	}

	public void startBackend(Observation observation) throws BackendException{
		if(this.statusService) throw new BackendException(invalidInstance);
		if(!isON()) throw new BackendException("Backend failed: Cause: Backend not ON "); 

		this.setUpBackendForObservation(observation);
		try {
			String response = this.sendCommand(BackendService.start);
			if(response.equals("fail")) throw new UnexpectedBackendReplyException(start, response);
			observation.setUtc(response);
		}catch (ConnectException e) {
			throw new BackendException("Backend failed: Cause: " , ExceptionUtils.getStackTrace(e));  
		}
	}

	public void stopBackend() throws BackendException{
		if(this.statusService) throw new BackendException(invalidInstance);
		if(!isON()) throw new BackendException("Backend failed: Cause: Backend not ON "); 
		if(isIdle()) return;
		try{
			String response = this.sendCommand(BackendService.stop);
			if(!response.equals(backendResponseSuccess)) throw new UnexpectedBackendReplyException(stop, response);
		}catch (ConnectException e) {
			throw new BackendException("Backend failed: Cause: " , ExceptionUtils.getStackTrace(e));  
		}

	}

	public String getBackendStatus() throws BackendException {
		try{
			String response = this.sendCommand(BackendService.query);
			return response;
		}catch (ConnectException e) {
			throw new BackendException("Backend failed: Cause: " , ExceptionUtils.getStackTrace(e));  
		}

	}

	public Boolean isIdle() throws BackendException {
		try{
			String response = this.sendCommand(BackendService.query);
			return (response.equals(backendIdle) || response.contains(backendPrepared));
		}catch (ConnectException e) {
			throw new BackendException("Backend failed: Cause: " , ExceptionUtils.getStackTrace(e));  
		}

	}

	public boolean isON() throws BackendException{
		try {
			String response = this.sendCommand(BackendService.query);
			return true;
		} catch (ConnectException e) {
			return false;
		} catch (BackendException e) {
			throw e;
		}
	}

	public String talkToBackend(String xmlMessage) throws BackendException, ConnectException{
		try{
			String xmlResponse = Utilities.talkToServer(xmlMessage, backendIP, backendPort);
			return xmlResponse;		
		}catch (ConnectException e) {  
			throw e;
		}
		catch (Exception e) {
			throw new BackendException("Backend failed: Cause:"+ e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}



	public String sendCommand(String command) throws BackendException, ConnectException{
		Map<String, String> messageMap = new HashMap<String, String>();
		messageMap.put("command", command);
		StrSubstitutor messageSubstitutor  = new StrSubstitutor(messageMap);
		String message = "";
		switch (command) {

		case prepare:
			StrSubstitutor paramSubstitutor  = new StrSubstitutor(defaultParams);
			String params = paramSubstitutor.replace(paramTemplate);
			messageMap.put("parameters", params);
			break;
		case query:
		case start:
		case stop:
			messageMap.put("parameters", "");
			break;
		default:
			throw new BackendException("Backend failed: Cause: invalid command ["+command+"] given." , ExceptionUtils.getStackTrace(new Exception()));
		}
		message = messageSubstitutor.replace(messageWrapper);
		System.err.println(message);
		String xmlResponseStr = talkToBackend(message);
		String response = "";

		try  
		{  
			switch (command) {

			case query:
				response = Utilities.getTextFromXpath(xmlResponseStr, "//response/mpsr_status");
				break;
			case prepare:
			case stop:
				response = Utilities.getTextFromXpath(xmlResponseStr, "//reply");
				break;
			case start:
				response  = Utilities.getTextFromXpath(xmlResponseStr, "//response");
				break;
			}
			return response;
		} catch (ConnectException e) {  
			throw e;
		} catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			throw new BackendException("Backend failed: Cause: " , ExceptionUtils.getStackTrace(e));  

		} 
	}


	public Lights getLights(HtmlPage controlsPage){
		int green = 0,red = 0,grey = 0,yellow=0;
		DomNodeList<DomElement> images =  controlsPage.getElementsByTagName("img");
		for(DomElement img: images){
			String file = img.getAttribute("src");
			if(file.contains("grey_light.png")) grey++;
			if(file.contains("green_light.png")) green++;
			if(file.contains("red_light.png")) red++;
			if(file.contains("yellow_light.png")) yellow++;
		}
		System.err.println("red:" + red + " yellow: " + yellow + " green: "+ green + " grey: " + grey);
		Lights l = new Lights();
		l.red = red;
		l.green = green;
		l.yellow = yellow;
		l.grey = grey;
		return l;
	}

	private void talkToBackendServer(String command) throws BackendException{
		try {
			long javascriptRuntime = 60000;
			WebClient webClient = new WebClient(BrowserVersion.FIREFOX_45);
			webClient.getCookieManager().setCookiesEnabled(false);
			webClient.waitForBackgroundJavaScript(60000);
			webClient.getCache().clear();
			HtmlPage page = webClient.getPage("http://localhost/mopsr/control.lib.php?single=true");
			ScriptResult result = page.executeJavaScript(command);

			Page newPage = result.getNewPage();
			webClient.waitForBackgroundJavaScript(javascriptRuntime);
			if(newPage.isHtmlPage()){
				HtmlPage newHtmlPage = (HtmlPage) newPage;
				int iter=0;
				boolean isDone = false;
				while(!isDone){

					if(iter++ > maxIter){
						throw new BackendException("maximum iteration to backend controls page reached", ExceptionUtils.getStackTrace(new Exception()));
					}
					Lights l = getLights(newHtmlPage);
					if(command.equals(bootUp) && l.red ==0 && l.yellow == 0) isDone = true;
					else if(command.equals(shutDown) && l.green ==0 && l.yellow ==0) isDone = true;
					else {
						//newHtmlPage = (HtmlPage) newHtmlPage.refresh();
						webClient.waitForBackgroundJavaScript(javascriptRuntime);
					}
				}

			}
			webClient.close();


		} catch (FailingHttpStatusCodeException | IOException e) {
			e.printStackTrace();
		} catch (BackendException e) {
			throw new BackendException("maximum iteration to backend controls page reached", ExceptionUtils.getStackTrace(e));
		}
	}



	public void bootUpBackend() throws BackendException{
		if(this.statusService) throw new BackendException(invalidInstance);
		try {
			talkToBackendServer(bootUp);
		} catch (BackendException e) {
			throw new BackendException("maximum iteration to backend controls page reached", ExceptionUtils.getStackTrace(e));
		}
	}

	public void shutDownBackend() throws BackendException{
		if(!isIdle()) throw new BackendInInvalidStateException("shutdown","idle",getBackendStatus());
		if(this.statusService) throw new BackendException(invalidInstance);
		try {
			talkToBackendServer(shutDown);
		} catch (BackendException e) {
			throw new BackendException("maximum iteration to backend controls page reached", ExceptionUtils.getStackTrace(e));
		}
	}

	public void changeBackendConfig(String backend) throws BackendException{
		if(this.statusService) throw new BackendException(invalidInstance);
		System.err.println("trying to install this backend" + backend);
		try {
			String[] cmd = new String[]{"ssh", "-t", "dada@localhost", " " + loadBackendScript + " " + backend + " "};
			System.err.println("sending this:" + Arrays.asList(cmd).toString());
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(p.getErrorStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getCurrentBackend() throws BackendException{
		String[] cmd = new String[]{"/bin/bash","-c","grep CONFIG_NAME /home/dada/linux_64/share/mopsr_aq.cfg | awk '{print $2}'"};
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			String str = "";
			String line = null;
			while ((line = in.readLine()) != null) {
				str+=line;
			}
			if(str.contains(psrBackend)) return psrBackend;
			if(str.contains(corrBackend)) return corrBackend;
			return otherBackends;
		} catch (IOException e) {
			throw new BackendException(e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}

	static{
		defaultParams.put("epoch","J2000");
		defaultParams.put("position_epoch","J2000");
		defaultParams.put("position_error","0.000001");

		defaultParams.put("position_error_units","degrees");
		defaultParams.put("ra_units","hhmmss");
		defaultParams.put("dec_units","ddmmss");

		defaultParams.put("nbits","8");
		defaultParams.put("ndim","2");
		defaultParams.put("npol","1");
		defaultParams.put("nant","16");
		defaultParams.put("nchan","40");
		defaultParams.put("bw","31.25");
		defaultParams.put("bw_units","MHz");
		defaultParams.put("cfreq","834.765625");
		defaultParams.put("cfreq_units","MHz");
		defaultParams.put("foff","0.78125");
		defaultParams.put("foff_units","MHz");
		defaultParams.put("tsamp","1.28");
		defaultParams.put("tsamp_units","microseconds");
		defaultParams.put("oversampling_ratio","1");
		defaultParams.put("dual_sideband","1");
		defaultParams.put("resolution","1280");
		defaultParams.put("resolution_units","bytes");

		defaultParams.put("md_angle","0.0");
		defaultParams.put("ns_tilt","0.0");
		defaultParams.put("ns_tilt_units","degrees");
		defaultParams.put("md_angle_units","degrees");

		defaultParams.put("obs_type","TRACKING");


		//		defaultParams.put("mode","PSR");
		//		defaultParams.put("tobs","120");
		//		defaultParams.put("obs_type","TRACKING");
		//		defaultParams.put("aq_proc_file","mopsr.aqdsp.gpu");
		//		defaultParams.put("bf_proc_file","mopsr.dspsr.cpu.cdd");
		//		defaultParams.put("bp_proc_file","mopsr.null");
		//
		//		defaultParams.put("observer","VVK");
		//		defaultParams.put("project_id","P001");
		//
		//		defaultParams.put("config","FAN_BEAM");
		//		defaultParams.put("source_name","junk1");
		//		defaultParams.put("ra","11:41:07");
		//		defaultParams.put("dec","-65:45:19");
		//
		//		defaultParams.put("config","TIED_ARRAY_FAN_BEAM");
		//		defaultParams.put("source_name","J1141-6545");
		//		defaultParams.put("ra","11:41:07");
		//		defaultParams.put("dec","-65:45:19");

	}

}
