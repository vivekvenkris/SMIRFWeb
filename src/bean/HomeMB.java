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
			TCCStatus tccStatus = tccService.getTelescopeStatus();
			if(tccStatus.isTelescopeIdle()) this.TCCStatus = "Idle";
			else if(tccStatus.isTelescopeDriving())this.TCCStatus =  "driving";
			else if(tccStatus.isTelescopeTracking())this.TCCStatus =  "tracking";
			else this.TCCStatus = "Idle";
		} catch (TCCException e) {
			this.TCCStatus =  "slacking";
			e.printStackTrace();
		}catch(BackendException e){
			this.backendStatus="problem";
			e.printStackTrace();
			
		}
	}
	
	public void updateTimeElapsed(){
			if(this.observation == null || this.observation.getUtc() == null || observation.getUtcDate() == null) return;
			this.timeElapsedPercent = (int) ((new Date().getTime() - observation.getUtcDate().getTime())*100/(observation.getTobs()*1000));
	}

}
