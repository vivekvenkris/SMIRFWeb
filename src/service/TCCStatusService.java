package service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.javatuples.Pair;

import bean.Angle;
import bean.TCCStatus;
import exceptions.TCCException;
import util.TCCConstants;
import util.Utilities;

public class TCCStatusService implements TCCConstants {
	
	TCCStatus status;
	
	public String talkToAnansiStatusServer(String xmlMessage) throws TCCException{
		try{
			String xmlResponse ="";
			Socket client = new Socket(tccStatusIP, tccStatusPort);
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(client.getInputStream()));
			xmlResponse = inFromServer.readLine(); // read xml header
			xmlResponse += inFromServer.readLine();
			//System.out.println("FROM SERVER: " + xmlResponse);
			
			client.close();
			return xmlResponse;
		}
		catch (Exception e) {
			throw new TCCException("TCC status service failed: Cause:"+ e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}
	public TCCStatus getTelescopeStatus() throws TCCException{
		
		try {
			String xmlResponse = talkToAnansiStatusServer(TCCConstants.pingCommand);
			return getStatusObj(xmlResponse);
		} catch (TCCException e) {
			throw new TCCException("TCC status service failed: Cause:"+ e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}
	
	public TCCStatus getStatusObj(String xmlMessage) throws TCCException{
		try {
		TCCStatus status = new TCCStatus();
		status.setOverview(Utilities.getTextFromXpath(xmlMessage, "//overview/error_string"));
		
		TCCStatus.SourceCoordinates coords = status.getCoordinates();
		coords.setRa(new Angle(Utilities.getTextFromXpath(xmlMessage, "//coordinates/RA"),Angle.HHMMSS));
		coords.setDec(new Angle(Utilities.getTextFromXpath(xmlMessage, "//coordinates/Dec"),Angle.DDMMSS));
		coords.setHa(new Angle(Utilities.getTextFromXpath(xmlMessage, "//coordinates/HA"),Angle.HHMMSS));
		coords.setgLat(new Angle(Utilities.getTextFromXpath(xmlMessage, "//coordinates/Glat"),Angle.RAD,Angle.DEG));
		coords.setgLon(new Angle(Utilities.getTextFromXpath(xmlMessage, "//coordinates/Glon"),Angle.RAD,Angle.DEG));
		coords.setNs(new Angle(Utilities.getTextFromXpath(xmlMessage, "//coordinates/NS"),Angle.RAD,Angle.DEG));
		coords.setEw(new Angle(Utilities.getTextFromXpath(xmlMessage, "//coordinates/EW"),Angle.RAD,Angle.DEG));
		coords.setLmst(new Angle(Utilities.getTextFromXpath(xmlMessage, "//coordinates/LMST"),Angle.HHMMSS));
		
		TCCStatus.Drive ns = status.getNs();		
		TCCStatus.Arm nsEast = ns.getEast();
		TCCStatus.Arm nsWest = ns.getWest();
		
		ns.setError(Utilities.getTextFromXpath(xmlMessage, "//ns/error"));
		nsEast.setTilt(new Angle(Utilities.getTextFromXpath(xmlMessage, "//ns/east/tilt"),Angle.RAD));
		nsEast.setCount(Integer.parseInt(Utilities.getTextFromXpath(xmlMessage, "//ns/east/count")));
		nsEast.setDriving(Boolean.parseBoolean(Utilities.getTextFromXpath(xmlMessage, "//ns/east/driving")));
		nsEast.setOnTarget(Boolean.parseBoolean(Utilities.getTextFromXpath(xmlMessage, "//ns/east/on_target")));
		nsEast.setStatus(Integer.parseInt(Utilities.getTextFromXpath(xmlMessage, "//ns/east/system_status")));
		nsEast.setDisabled(Utilities.getTextFromXpath(xmlMessage, "//ns/east/state") == "disabled");
		if(!nsEast.getDisabled()) nsEast.setFast(Utilities.getTextFromXpath(xmlMessage, "//ns/east/state") == "fast");
		
		nsWest.setTilt(new Angle(Utilities.getTextFromXpath(xmlMessage, "//ns/west/tilt"),Angle.RAD));
		nsWest.setCount(Integer.parseInt(Utilities.getTextFromXpath(xmlMessage, "//ns/west/count")));
		nsWest.setDriving(Boolean.parseBoolean(Utilities.getTextFromXpath(xmlMessage, "//ns/west/driving")));
		nsWest.setOnTarget(Boolean.parseBoolean(Utilities.getTextFromXpath(xmlMessage, "//ns/west/on_target")));
		nsWest.setStatus(Integer.parseInt(Utilities.getTextFromXpath(xmlMessage, "//ns/west/system_status")));
		nsWest.setDisabled(Utilities.getTextFromXpath(xmlMessage, "//ns/west/state") == "disabled");
		if(!nsWest.getDisabled()) nsWest.setFast(Utilities.getTextFromXpath(xmlMessage, "//ns/west/state") == "fast");

		TCCStatus.Drive md = status.getMd();
		TCCStatus.Arm mdEast = md.getEast();
		TCCStatus.Arm mdWest = md.getWest();
		
		md.setError(Utilities.getTextFromXpath(xmlMessage, "//md/error"));
		mdEast.setTilt(new Angle(Utilities.getTextFromXpath(xmlMessage, "//md/east/tilt"),Angle.RAD));
		mdEast.setCount(Integer.parseInt(Utilities.getTextFromXpath(xmlMessage, "//md/east/count")));
		mdEast.setDriving(Boolean.parseBoolean(Utilities.getTextFromXpath(xmlMessage, "//md/east/driving")));
		mdEast.setOnTarget(Boolean.parseBoolean(Utilities.getTextFromXpath(xmlMessage, "//md/east/on_target")));
		mdEast.setStatus(Integer.parseInt(Utilities.getTextFromXpath(xmlMessage, "//md/east/system_status")));
		mdEast.setDisabled(Utilities.getTextFromXpath(xmlMessage, "//md/east/state") == "disabled");
		if(!mdEast.getDisabled()) mdEast.setFast(Utilities.getTextFromXpath(xmlMessage, "//md/east/state") == "fast");
		
		mdWest.setTilt(new Angle(Utilities.getTextFromXpath(xmlMessage, "//md/west/tilt"),Angle.RAD));
		mdWest.setCount(Integer.parseInt(Utilities.getTextFromXpath(xmlMessage, "//md/west/count")));
		mdWest.setDriving(Boolean.parseBoolean(Utilities.getTextFromXpath(xmlMessage, "//md/west/driving")));
		mdWest.setOnTarget(Boolean.parseBoolean(Utilities.getTextFromXpath(xmlMessage, "//md/west/on_target")));
		mdWest.setStatus(Integer.parseInt(Utilities.getTextFromXpath(xmlMessage, "//md/west/system_status")));
		mdWest.setDisabled(Utilities.getTextFromXpath(xmlMessage, "//md/west/state") == "disabled");
		if(!mdWest.getDisabled()) mdWest.setFast(Utilities.getTextFromXpath(xmlMessage, "//md/west/state") == "fast");
		
		status.setTiltMD(new Pair<Double, Double>(mdEast.getTilt().getRadianValue(),mdWest.getTilt().getRadianValue()));
		status.setTiltNS(new Pair<Double, Double>(nsEast.getTilt().getRadianValue(),nsWest.getTilt().getRadianValue()));
		status.setSpeedNS(new Pair<Boolean,Boolean>(nsEast.getFast(),nsWest.getFast()));
		
		return status;
		} catch(Exception e){
			e.printStackTrace();
			throw new TCCException("TCC status service failed: Cause:"+ e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}
	
	public static void main(String[] args) throws TCCException {
		TCCStatusService service = new TCCStatusService();
		TCCStatus status = service.getTelescopeStatus();
		System.exit(0);
		 final String s = "<?xml version='1.0' encoding='ISO-8859-1'?>"
	        		+" <tcc_status> "
	        		+"   <overview> "
	        		+"     <error_string>blah</error_string> "
	        		+"   </overview> "
	        		+"   <coordinates> "
	        		+"     <RA>12:45:31.35</RA> "
	        		+"     <Dec>-88:56:33.4</Dec> "
	        		+"     <HA>6:19:10.2</HA> "
	        		+"     <Glat>-0.453464155975</Glat> "
	        		+"     <Glon>5.28620911537</Glon> "
	        		+"     <Alt>0.636015534401</Alt> "
	        		+"     <Az>3.14418578148</Az> "
	        		+"     <NS>-0.935129959926</NS> "
	        		+"     <EW>0.0</EW> "
	        		+"     <LMST>13:11:29.55</LMST> "
	        		+"   </coordinates> "
	        		+"   <ns> "
	        		+"     <error>None</error> "
	        		+"     <east> "
	        		+"       <tilt>-0.935093133362</tilt> "
	        		+"       <count>10345</count> "
	        		+"       <driving>False</driving> "
	        		+"       <state>disabled</state> "
	        		+"       <on_target>True</on_target> "
	        		+"       <system_status>112</system_status> "
	        		+"     </east> "
	        		+"     <west> "
	        		+"       <tilt>-0.933763344331</tilt> "
	        		+"       <count>10562</count> "
	        		+"       <driving>False</driving> "
	        		+"       <state>slow</state> "
	        		+"       <on_target>True</on_target> "
	        		+"       <system_status>112</system_status> "
	        		+"     </west> "
	        		+"   </ns> "
	        		+"   <md> "
	        		+"     <error>None</error> "
	        		+"     <east> "
	        		+"       <tilt>0.0</tilt> "
	        		+"       <count>8388608</count> "
	        		+"       <driving>False</driving> "
	        		+"       <state>auto</state> "
	        		+"       <on_target>True</on_target> "
	        		+"       <system_status>0</system_status> "
	        		+"     </east> "
	        		+"     <west> "
	        		+"       <tilt>0.0</tilt> "
	        		+"       <count>8388608</count> "
	        		+"       <driving>False</driving> "
	        		+"       <state>auto</state> "
	        		+"       <on_target>True</on_target> "
	        		+"       <system_status>0</system_status> "
	        		+"     </west> "
	        		+"   </md> "
	        		+" </tcc_status> ";
		 
		 try {
			service.getStatusObj(s);
		} catch (TCCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
