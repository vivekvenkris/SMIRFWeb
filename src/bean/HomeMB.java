package bean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import exceptions.BackendException;
import exceptions.TCCException;
import manager.ScheduleManager;
import service.BackendService;
import service.TCCService;
import service.TCCStatusService;

@ManagedBean
@ApplicationScoped
public class HomeMB {

	private Observation observation;
	private String backendStatus;
	private String TCCStatus;
	private String SMIRFStatus;
	private Integer timeElapsedPercent;

	private TCCStatusService tccService = new TCCStatusService();
	private BackendService backendStatusService = BackendService.createBackendStatusInstance();

	public HomeMB() {
		backendStatus = TCCStatus = SMIRFStatus = null;
	}
	public Observation getObservation() {
		return observation;
	}
	public void setObservation(Observation observation) {
		this.observation = observation;
	}
	public String getBackendStatus() {
		return backendStatus;
	}
	public void setBackendStatus(String backendStatus) {
		this.backendStatus = backendStatus;
	}
	public String getTCCStatus() {
		return TCCStatus;
	}
	public void setTCCStatus(String tCCStatus) {
		TCCStatus = tCCStatus;
	}
	public String getSMIRFStatus() {
		return SMIRFStatus;
	}
	public void setSMIRFStatus(String sMIRFStatus) {
		SMIRFStatus = sMIRFStatus;
	}
	public Integer getTimeElapsedPercent() {
		return timeElapsedPercent;
	}
	public void setTimeElapsedPercent(Integer timeElapsedPercent) {
		this.timeElapsedPercent = timeElapsedPercent;
	}
	
	public void updateObs(){
		this.observation = ScheduleManager.getCurrentObservation();
		try {
			this.backendStatus = backendStatusService.getBackendStatus();
			if(tccService.getTelescopeStatus().isTelescopeDriving())this.TCCStatus =  "driving";
			if(tccService.getTelescopeStatus().isTelescopeTracking())this.TCCStatus =  "tracking";
			else this.TCCStatus = "idle";
		} catch (BackendException | TCCException e) {
			e.printStackTrace();

		}
	}
	
	public void updateTimeElapsed(){
		try {
			if(this.observation == null || this.observation.getUtc() == null) return;
			this.timeElapsedPercent = (int) ((new Date().getTime() - new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSS").parse(observation.getUtc()).getTime())*100/(observation.getTobs()*1000));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}

}
