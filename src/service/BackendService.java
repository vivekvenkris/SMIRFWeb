package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import bean.Furby;
import bean.ObservationTO;
import bean.TBSourceTO;
import exceptions.BackendException;
import exceptions.BackendInInvalidStateException;
import exceptions.UnexpectedBackendReplyException;
import mailer.Mailer;
import manager.FurbyManager;
import util.BackendConstants;
import util.ConfigManager;
import util.SMIRFConstants;
import util.Utilities;

class Lights {
	int red, green, yellow, grey;

	public Lights() {
		red = green = yellow = grey = 0;
	}
}

public class BackendService implements BackendConstants {
	private static BackendService backendService = null;
	Boolean statusService = true;

	public static BackendService createBackendInstance() {
		if (backendService == null)
			backendService = new BackendService(false);
		return backendService;
	}

	public static BackendService createBackendStatusInstance() {
		return new BackendService(true);
	}

	private BackendService(Boolean statusService) {
		this.statusService = statusService;
	}

	private void setUpBackendForObservation(ObservationTO observation) throws BackendException {

		String template = "";
		template += pfbParams;
		template += signalParams;
		template += obsParams;
		template += westArmParams;
		template += eastArmParams;
		template += boresightParams;
		
		populateDefaultParameters();
		defaultParams.put("tobs", observation.getTobs().toString());

		defaultParams.put("observer", "SMIRFWeb-" + observation.getObserver());

		defaultParams.put("boresight_project_id", observation.getProjectID());

		defaultParams.put("boresight_source_name", observation.getName());
		defaultParams.put("boresight_ra", observation.getCoords().getPointingTO().getAngleRA().toHHMMSS());
		defaultParams.put("boresight_dec", observation.getCoords().getPointingTO().getAngleDEC().toDDMMSS());
		defaultParams.put("boresight_proc_file", "mopsr.aqdsp.hires.gpu");
		
		Integer tObs = observation.getTobs();

		
		if( Boolean.parseBoolean(ConfigManager.getSmirfMap().get("ADD_FURBIES"))) {
						
			List<Furby>  allFurbies =  FurbyManager.getFurbies();
			

			
			Integer maxFurbies = Integer.parseInt(ConfigManager.getSmirfMap().get("MAX_FURBIES"));
			
			
			maxFurbies = maxFurbies < allFurbies.size() ? maxFurbies : allFurbies.size();
			Double furbySpacing = Double.parseDouble(ConfigManager.getSmirfMap().get("FURBY_SPACING"));

			maxFurbies = (int) (maxFurbies < tObs/furbySpacing ? maxFurbies: Math.floor(tObs/furbySpacing));

			//Integer numFurbies = (int) Math.abs(new Random().nextGaussian() * Double.parseDouble(ConfigManager.getSmirfMap().get("FURBY_NUM_SD"))
					//+ Double.parseDouble(ConfigManager.getSmirfMap().get("FURBY_NUM_MEAN")));
			//numFurbies = numFurbies < maxFurbies ? numFurbies : maxFurbies;

			
			Double timePerFurby =  Double.parseDouble(ConfigManager.getSmirfMap().get("NUM_SECS_PER_FURBY"));
			
			
			if(timePerFurby < 2*furbySpacing) {
				
				timePerFurby = 2*furbySpacing;
				System.err.println("Changing NUM_SECS_PER_FURBY to " + timePerFurby + " after checking for future vegemite foolishness");

			}
			
			
			Double furbyTimeStart = furbySpacing;
			

			
			Integer numFurbies = (int)Math.floor(tObs/timePerFurby);
			numFurbies = numFurbies < maxFurbies ? numFurbies : maxFurbies;
		
			System.err.println("Adding a total of " + numFurbies + " furbies");
			
			List<Furby> selectedFurbies = new ArrayList<>();
			
			
			for(int  i=0; i< numFurbies; i++) {
				
				Furby randomFurby = allFurbies.get(new Random().nextInt(allFurbies.size())).clone();
				
				randomFurby.setBeam(Furby.getRandomBeamNumber());
				
				double timeStamp = i*timePerFurby + Math.random()*(timePerFurby - furbySpacing) ;
				
				if(timeStamp<=furbyTimeStart) timeStamp += furbyTimeStart;
								
				Furby.addTimeStamp(randomFurby,timeStamp);
				
				System.err.println("adding " + randomFurby );
				
				selectedFurbies.add(randomFurby);
				
			}
			System.err.println(selectedFurbies);
			
			String furbyIDs = String.join(",",selectedFurbies.stream().map(f -> f.getFurbyID().toString()).collect(Collectors.toList()));
			String furbyBeams = String.join(",",selectedFurbies.stream().map(f -> f.getBeam().toString()).collect(Collectors.toList()));
			String furbyTimestamps=String.join(",",selectedFurbies.stream().map(f -> String.format("%.1f",f.getTimeStamp())).collect(Collectors.toList()));
			
			template += furbyParams;

			defaultParams.put("num_furbies",numFurbies.toString());
			defaultParams.put("furby_ids", furbyIDs);
			defaultParams.put("furby_beams",furbyBeams);
			defaultParams.put("furby_tstamps", furbyTimestamps);
			
		}
		
		switch (observation.getObsType()) {
		case correlation:
			defaultParams.put("corr_project_id", SMIRFConstants.PID);
			defaultParams.put("corr_type", "FX");
			defaultParams.put("antenna_weights", "false");
			defaultParams.put("corr_proc_file", "mopsr.calib.hires.pref16.gpu");
			template += corrParams;
			break;
		case tiedArrayFanBeam:
			int index = 0;
			for (TBSourceTO tbs : observation.getTiedBeamSources()) {
				String tbParamStr = tbParams[index];
				String tbStr = "tb" + index;
				defaultParams.put(tbStr + "_project_id", observation.getProjectID());
				defaultParams.put(tbStr + "_mode", "PSR");
				defaultParams.put("antenna_weights", "true");
				// This is a temporary hack by SO/CF, need a better solution, like custom proc files in the db or some file.
				if (tbs.getPsrName().equals("J2251-3711")) {
					defaultParams.put(tbStr + "_proc_file", "mopsr_2kbins_25s.dspsr.cpu");
				} else {
					defaultParams.put(tbStr + "_proc_file", "mopsr.dspsr.cpu");
				}
				defaultParams.put(tbStr + "_source_name", tbs.getPsrName());
				defaultParams.put(tbStr + "_ra", tbs.getAngleRA().toHHMMSS());
				defaultParams.put(tbStr + "_dec", tbs.getAngleDEC().toDDMMSS());
				TBSourceTO.DSPSRParameters dspsrParameters = tbs.getDspsrParams();
				Map<String, String> map2 = new HashMap<String, String>();
				StrSubstitutor dspsrSubstitutor = new StrSubstitutor(map2);

				map2.put(tbStr + "DspsrParams", "");
				if (dspsrParameters != null) {
					String dspsrParamStr = dspsrParams[index];
					Map<String, String> map = new HashMap<String, String>();
					map.put(tbStr + "_dm", dspsrParameters.getDM().toString());
					map.put(tbStr + "_period", dspsrParameters.getPeriodSecs().toString());
					map.put(tbStr + "_acc", dspsrParameters.getAcceleration().toString());
					StrSubstitutor paramSubstitutor = new StrSubstitutor(map);
					String dspsr = paramSubstitutor.replace(dspsrParamStr);
					map2.put(tbStr + "DspsrParams", dspsr);

				}
				tbParamStr = dspsrSubstitutor.replace(tbParamStr);

				template += tbParamStr;
				if (index >= BackendConstants.maximumNumberOfTB - 1)
					break;
				index++;
			}
			/**
			 * Do not break here as tied array fanbeam requires fanbeam case too.
			 */
		case fanBeam:
			defaultParams.put("fb_project_id", observation.getProjectID());
			defaultParams.put("fb_mode", "PSR");
			defaultParams.put("fb_nbeams", observation.getNfb().toString());
			defaultParams.put("fb_spacing", observation.getFanbeamSpacing().getDegreeValue().toString());
			defaultParams.put("delay_tracking", observation.getDelayTracking().toString());
			template += fabBeamParams;
			break;
		}

		prepareBackend(template);

	}
	@Deprecated
	private Double getFurbyTimestamp(Integer tobs, List<Furby> selectedFurbies) {
		Double furbyTimeStart = Double.parseDouble(ConfigManager.getSmirfMap().get("FURBY_TIME_START"));
		Double furbySpacing = Double.parseDouble(ConfigManager.getSmirfMap().get("FURBY_SPACING"));
		
		Double randomTimeStamp = null;
		while(true) {
			randomTimeStamp = Math.random() * (tobs -furbyTimeStart) + furbyTimeStart;
			
			boolean flag = true;
			for(Furby selectedFurby: selectedFurbies) {
				
				if(Math.abs(selectedFurby.getTimeStamp() - randomTimeStamp) < furbySpacing ) {
					flag = false;
				}
				
			}
			if(flag)  break;
		}
		
		return randomTimeStamp;
		
	}

	private void prepareBackend(String template) throws BackendException {
		if (!isON())
			throw new BackendException("Backend failed: Cause: Backend not ON ");
		
		if(!isIdle()) stopBackend();
		
		try {
			String response = this.sendCommand(prepare, template);
			if (!response.equals(backendPrepared))
				throw new UnexpectedBackendReplyException(prepare, backendPrepared, response);
		} catch (ConnectException e) {
			throw new BackendException("Backend failed: Cause: ", ExceptionUtils.getStackTrace(e));
		}

	}

	
	
	
	public void startBackend(ObservationTO observation) throws BackendException {
		if (this.statusService)
			throw new BackendException(invalidInstance);
		if (!isON())
			throw new BackendException("Backend failed: Cause: Backend not ON ");

		this.setUpBackendForObservation(observation);
		String response = "";
		try {
			response = this.sendCommand(BackendService.start, "");
			if (response.equals("fail"))
				throw new UnexpectedBackendReplyException(start, response);
			observation.setUTCDateAndString(response);
		} catch (ConnectException e) {
			throw new BackendException("Backend failed: Cause: ", ExceptionUtils.getStackTrace(e));
		} catch (ParseException e) {
			throw new BackendException("Backend failed with Error: " + response, ExceptionUtils.getStackTrace(e));
		}

	}

	public void stopBackend() throws BackendException {
		if (this.statusService)
			throw new BackendException(invalidInstance);
		if (!isON())
			throw new BackendException("Backend failed: Cause: Backend not ON ");
		if (isIdle())
			return;
		try {
			String response = this.sendCommand(BackendService.stop, "");
			if (!response.equals(backendResponseSuccess))
				throw new UnexpectedBackendReplyException(stop, response);
		} catch (ConnectException e) {
			throw new BackendException("Backend failed: Cause: ", ExceptionUtils.getStackTrace(e));
		}

	}

	public String getBackendStatus() throws BackendException {
		try {
			String response = this.sendCommand(BackendService.query, "");
			return response;
		} catch (ConnectException e) {
			throw new BackendException("Backend failed: Cause: ", ExceptionUtils.getStackTrace(e));
		}

	}

	@Deprecated
	public String getBackendStatusForHomePage() throws BackendException {
		try {
			String response = this.sendCommand(BackendService.query, "");
			return response;
		} catch (ConnectException e) {
			return "cannot connect";
		}

	}

	public Boolean isIdle() throws BackendException {
		try {
			String response = this.sendCommand(BackendService.query, "");
			return (response.equals(backendIdle) || response.contains(backendPrepared));
		} catch (ConnectException e) {
			throw new BackendException("Backend failed: Cause: ", ExceptionUtils.getStackTrace(e));
		}

	}

	public boolean isON() throws BackendException {
		try {
			String response = this.sendCommand(BackendService.query, "");
			return true;
		} catch (ConnectException e) {
			return false;
		} catch (BackendException e) {
			throw e;
		}
	}

	public synchronized String talkToBackend(String xmlMessage) throws BackendException, ConnectException {
		try {
			String xmlResponse = Utilities.talkToServer(xmlMessage, backendIP, backendPort);
			return xmlResponse;
		} catch (ConnectException e) {
			throw e;
		} catch (Exception e) {
			throw new BackendException("Backend failed: Cause:" + e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}

	public String sendCommand(String command, String parameterTemplate) throws BackendException, ConnectException {
		Map<String, String> messageMap = new HashMap<String, String>();
		messageMap.put("command", command);
		StrSubstitutor messageSubstitutor = new StrSubstitutor(messageMap);

		StrSubstitutor paramSubstitutor = new StrSubstitutor(defaultParams);
		String params = paramSubstitutor.replace(parameterTemplate);
		messageMap.put("parameters", params);
		
		String message = messageSubstitutor.replace(messageWrapper);



		try {
			String xmlResponseStr = talkToBackend(message);
			String response = "";

			switch (command) {

			case query:
				response = Utilities.getTextFromXpath(xmlResponseStr, "//response/mpsr_status");
				break;
			case stop:
				response = Utilities.getTextFromXpath(xmlResponseStr, "//reply");
				break;
			case prepare:
			case start:
				Utilities.prettyPrintXML(message);
				response = Utilities.getTextFromXpath(xmlResponseStr, "//response");
				break;
			}
			return response;
		} catch (ConnectException e) {
			throw e;
		} catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException e) {
			if (!command.equals(query))
				e.printStackTrace();
			throw new BackendException("Backend failed: Cause: ", ExceptionUtils.getStackTrace(e));

		}
	}

	public Lights getLights(HtmlPage controlsPage) {
		int green = 0, red = 0, grey = 0, yellow = 0;
		DomNodeList<DomElement> images = controlsPage.getElementsByTagName("img");
		for (DomElement img : images) {
			String file = img.getAttribute("src");
			if (file.contains("grey_light.png"))
				grey++;
			if (file.contains("green_light.png"))
				green++;
			if (file.contains("red_light.png"))
				red++;
			if (file.contains("yellow_light.png"))
				yellow++;
		}
		System.err.println("red:" + red + " yellow: " + yellow + " green: " + green + " grey: " + grey);
		Lights l = new Lights();
		l.red = red;
		l.green = green;
		l.yellow = yellow;
		l.grey = grey;
		return l;
	}

	private void talkToBackendServer(String command) throws BackendException {
		try {
			// run backend order here.
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// private void talkToBackendServer(String command) throws BackendException{
	// try {
	// long javascriptRuntime = 60000;
	// WebClient webClient = new WebClient(BrowserVersion.FIREFOX_45);
	// webClient.getCookieManager().setCookiesEnabled(false);
	// webClient.waitForBackgroundJavaScript(60000);
	// webClient.getCache().clear();
	// HtmlPage page =
	// webClient.getPage("http://localhost:80/mopsr/control.lib.php?single=true");
	// ScriptResult result = page.executeJavaScript(command);
	//
	// Page newPage = result.getNewPage();
	// webClient.waitForBackgroundJavaScript(javascriptRuntime);
	// if(newPage.isHtmlPage()){
	// HtmlPage newHtmlPage = (HtmlPage) newPage;
	// int iter=0;
	// boolean isDone = false;
	// while(!isDone){
	//
	// if(iter++ > maxIter){
	// throw new BackendException("maximum iteration to backend controls page
	// reached", ExceptionUtils.getStackTrace(new Exception()));
	// }
	// Lights l = getLights(newHtmlPage);
	// if(command.equals(bootUp) && l.red ==0 && l.yellow == 0) isDone = true;
	// else if(command.equals(shutDown) && l.green ==0 && l.yellow ==0) isDone =
	// true;
	// else {
	// //newHtmlPage = (HtmlPage) newHtmlPage.refresh();
	// webClient.waitForBackgroundJavaScript(javascriptRuntime);
	// }
	// }
	//
	// }
	// webClient.close();
	//
	//
	// } catch (FailingHttpStatusCodeException | IOException e) {
	// e.printStackTrace();
	// } catch (BackendException e) {
	// throw new BackendException("maximum iteration to backend controls page
	// reached", ExceptionUtils.getStackTrace(e));
	// }
	// }
	//

	public void bootUpBackend() throws BackendException {
		if (this.statusService)
			throw new BackendException(invalidInstance);
		try {
			talkToBackendServer(bootUp);
		} catch (BackendException e) {
			throw new BackendException("maximum iteration to backend controls page reached",
					ExceptionUtils.getStackTrace(e));
		}
	}

	public void shutDownBackend() throws BackendException {
		if (!isIdle())
			throw new BackendInInvalidStateException("shutdown", "idle", getBackendStatus());
		if (this.statusService)
			throw new BackendException(invalidInstance);
		try {
			talkToBackendServer(shutDown);
		} catch (BackendException e) {
			throw new BackendException("maximum iteration to backend controls page reached",
					ExceptionUtils.getStackTrace(e));
		}
	}

	public void changeBackendConfig(String backend) throws BackendException {
		if (this.statusService)
			throw new BackendException(invalidInstance);
		System.err.println("trying to install this backend" + backend);
		try {
			String[] cmd = new String[] { "ssh", "-t", "dada@localhost",
					" " + loadBackendScript + " " + backend + " " };
			System.err.println("sending this:" + Arrays.asList(cmd).toString());
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			while ((line = in.readLine()) != null) {

				System.out.println(line);

				if (Thread.currentThread().isInterrupted())
					return;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getCurrentBackend() throws BackendException {
		String[] cmd = new String[] { "/bin/bash", "-c",
				"grep CONFIG_NAME /home/dada/linux_64/share/mopsr_aq.cfg | awk '{print $2}'" };
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String str = "";
			String line = null;
			while ((line = in.readLine()) != null) {
				str += line;
			}
			if (str.contains(smirfBackend))
				return smirfBackend;
			if (str.contains(globalBackend))
				return globalBackend;
			return otherBackends;
		} catch (IOException e) {
			throw new BackendException(e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}

	public void populateDefaultParameters() {
		defaultParams.clear();
		defaultParams.put("boresight_source_epoch", "J2000");
		defaultParams.put("tb0_source_epoch", "J2000");
		defaultParams.put("tb1_source_epoch", "J2000");
		defaultParams.put("tb2_source_epoch", "J2000");
		defaultParams.put("tb3_source_epoch", "J2000");

		defaultParams.put("position_epoch", "J2000");
		defaultParams.put("position_error", "0.000001");
		defaultParams.put("position_error_units", "degrees");

		defaultParams.put("boresight_ra_units", "hh:mm:ss.s");
		defaultParams.put("tb0_ra_units", "hh:mm:ss.s");
		defaultParams.put("tb1_ra_units", "hh:mm:ss.s");
		defaultParams.put("tb2_ra_units", "hh:mm:ss.s");
		defaultParams.put("tb3_ra_units", "hh:mm:ss.s");

		defaultParams.put("boresight_dec_units", "dd:mm:ss.s");
		defaultParams.put("tb0_dec_units", "dd:mm:ss.s");
		defaultParams.put("tb1_dec_units", "dd:mm:ss.s");
		defaultParams.put("tb2_dec_units", "dd:mm:ss.s");
		defaultParams.put("tb3_dec_units", "dd:mm:ss.s");

		defaultParams.put("nbits", "8");
		defaultParams.put("ndim", "2");
		defaultParams.put("npol", "1");
		defaultParams.put("nant", "8");
		defaultParams.put("nchan", "320");
		defaultParams.put("bw", "31.25");
		defaultParams.put("bw_units", "MHz");
		defaultParams.put("cfreq", "835.5957031");
		defaultParams.put("cfreq_units", "MHz");
		defaultParams.put("foff", "0.09765625");
		defaultParams.put("foff_units", "MHz");
		defaultParams.put("tsamp", "10.24");
		defaultParams.put("tsamp_units", "microseconds");
		defaultParams.put("oversampling_ratio", "1");
		defaultParams.put("dual_sideband", "1");
		defaultParams.put("resolution", "5120");
		defaultParams.put("resolution_units", "bytes");

		defaultParams.put("east_md_angle", "0.0");
		defaultParams.put("west_md_angle", "0.0");

		defaultParams.put("east_ns_tilt", "0.0");
		defaultParams.put("west_ns_tilt", "0.0");

		defaultParams.put("east_ns_tilt_units", "degrees");
		defaultParams.put("west_ns_tilt_units", "degrees");

		defaultParams.put("east_md_angle_units", "degrees");
		defaultParams.put("west_md_angle_units", "degrees");

		defaultParams.put("west_tracking", "false");
		defaultParams.put("east_tracking", "false");

		defaultParams.put("corr_dump_time_units", "seconds");
		defaultParams.put("corr_dump_time", "60");

		defaultParams.put("fb_spacing_units", "degrees");

		defaultParams.put("tb0_dm_units", "parsecs per cc");
		defaultParams.put("tb1_dm_units", "parsecs per cc");
		defaultParams.put("tb2_dm_units", "parsecs per cc");
		defaultParams.put("tb3_dm_units", "parsecs per cc");

		defaultParams.put("tb0_period_units", "seconds");
		defaultParams.put("tb1_period_units", "seconds");
		defaultParams.put("tb2_period_units", "seconds");
		defaultParams.put("tb3_period_units", "seconds");

		defaultParams.put("tb0_acc_units", "metres per second");
		defaultParams.put("tb1_acc_units", "metres per second");
		defaultParams.put("tb2_acc_units", "metres per second");
		defaultParams.put("tb3_acc_units", "metres per second");

		defaultParams.put("rfi_mitigation", "true");
		defaultParams.put("antenna_weights", "true");
		defaultParams.put("delay_tracking", "true");
//		defaultParams.put("num_furbies", "");
//		defaultParams.put("furby_ids", "");
//		defaultParams.put("furby_beams", "");
//		defaultParams.put("furby_tstamps", "");
		defaultParams.put("", "");
		defaultParams.put("", "");

		// defaultParams.put("mode","PSR");
		// defaultParams.put("tobs","120");
		// defaultParams.put("obs_type","TRACKING");
		// defaultParams.put("aq_proc_file","mopsr.aqdsp.gpu");
		// defaultParams.put("bf_proc_file","mopsr.dspsr.cpu.cdd");
		// defaultParams.put("bp_proc_file","mopsr.null");
		//
		// defaultParams.put("observer","VVK");
		// defaultParams.put("project_id","P001");
		//
		// defaultParams.put("config","FAN_BEAM");
		// defaultParams.put("source_name","junk1");
		// defaultParams.put("ra","11:41:07");
		// defaultParams.put("dec","-65:45:19");
		//
		// defaultParams.put("config","TIED_ARRAY_FAN_BEAM");
		// defaultParams.put("source_name","J1141-6545");
		// defaultParams.put("ra","11:41:07");
		// defaultParams.put("dec","-65:45:19");

	}


}
