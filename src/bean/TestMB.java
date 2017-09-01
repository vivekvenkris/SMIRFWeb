package bean;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import control.Control;
import manager.DBManager;
import manager.ScheduleManager;
import service.EphemService;
import util.SMIRFConstants;
@ManagedBean
@ApplicationScoped
public class TestMB {
	String utc;
	String enteredUTC;
	Boolean utcRendered = false;
	
	Integer tobs;
	Integer tobsUnits;

	Boolean tccEnabled = true;
	Boolean backendEnabled = true;
	
	Boolean doPostObservationStuff = true;

	String observer = "VVK";

	String raString;
	String decString;
		
	ScheduleManager manager = new ScheduleManager();


	public TestMB(){
		utc = "now";
		utcRendered = false;
		tobs = 720;
		tobsUnits = 1;

	}

	public void toggleUTCInput(){
		if(utc.equals("now")){
			utcRendered = false;
		}
		else {
			enteredUTC = EphemService.getUtcStringNow();
			utcRendered = true;

		}
	}
	

	public void startObservation(ActionEvent event){
		try{
			System.err.println(raString);
			System.err.println(decString);
			System.err.println(tobs);
			System.err.println(tobsUnits);
			System.err.println(observer);
			System.err.println(tccEnabled);
			System.err.println(backendEnabled);
			
			
			//manager.startObserving(new PointingTO(0, "SMIRFTest", raString, decString) ,tobs*tobsUnits,observer,tccEnabled,backendEnabled,doPostObservationStuff);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void terminateObservation(ActionEvent event){
		Control.setTerminateCall(true);
		while(manager.getScheduler()!= null && manager.getScheduler().isDone());
		addMessage("Terminated.");
	}


	public void addMessage(String summary) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  null);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public void addMessage(String summary, String detail) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  detail);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public String getUtc() {
		return utc;
	}

	public void setUtc(String utc) {
		this.utc = utc;
	}

	public String getEnteredUTC() {
		return enteredUTC;
	}

	public void setEnteredUTC(String enteredUTC) {
		this.enteredUTC = enteredUTC;
	}

	public Boolean getUtcRendered() {
		return utcRendered;
	}

	public void setUtcRendered(Boolean utcRendered) {
		this.utcRendered = utcRendered;
	}

	public Integer getTobs() {
		return tobs;
	}

	public void setTobs(Integer tobs) {
		this.tobs = tobs;
	}

	public Integer getTobsUnits() {
		return tobsUnits;
	}

	public void setTobsUnits(Integer tobsUnits) {
		this.tobsUnits = tobsUnits;
	}

	public Boolean getTccEnabled() {
		return tccEnabled;
	}

	public void setTccEnabled(Boolean tccEnabled) {
		this.tccEnabled = tccEnabled;
	}

	public Boolean getBackendEnabled() {
		return backendEnabled;
	}

	public void setBackendEnabled(Boolean backendEnabled) {
		this.backendEnabled = backendEnabled;
	}

	public Boolean getDoPostObservationStuff() {
		return doPostObservationStuff;
	}

	public void setDoPostObservationStuff(Boolean doPostObservationStuff) {
		this.doPostObservationStuff = doPostObservationStuff;
	}

	public String getObserver() {
		return observer;
	}

	public void setObserver(String observer) {
		this.observer = observer;
	}

	public String getRaString() {
		return raString;
	}

	public void setRaString(String raString) {
		this.raString = raString;
	}

	public String getDecString() {
		return decString;
	}

	public void setDecString(String decString) {
		this.decString = decString;
	}

	public ScheduleManager getManager() {
		return manager;
	}

	public void setManager(ScheduleManager manager) {
		this.manager = manager;
	}

	

}
