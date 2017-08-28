package bean;

import java.util.Date;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import control.Control;

@ManagedBean
@ApplicationScoped
public class HomeMB {

	private ObservationTO observation;
	private String backendStatus;
	private String TCCStatus;
	private String SMIRFStatus;
	private Integer timeElapsedPercent;

	public HomeMB() {
		backendStatus = TCCStatus = SMIRFStatus = null;
	}
	public ObservationTO getObservation() {
		return observation;
	}
	public void setObservation(ObservationTO observation) {
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
		
		if(!Control.isPolling()) return;
		
		System.err.println(Control.isPolling() + " " + backendStatus);
		
		this.observation = Control.getCurrentObservation();
		BackendStatus backendStatus = Control.getBackendStatus();

		this.backendStatus = backendStatus.toString();
		
		this.TCCStatus = Control.getTccStatus().getStatus();



	}

	public void updateTimeElapsed(){
		if(this.observation == null || this.observation.getUtc() == null || observation.getUtcDate() == null) {
			this.timeElapsedPercent = 0;
			return;
		}
		this.timeElapsedPercent = (int) ((new Date().getTime() - observation.getUtcDate().getTime())*100/(observation.getTobs()*1000));
	}

}
