package bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang3.exception.ExceptionUtils;

import control.Control;
import exceptions.BackendException;
import exceptions.SchedulerException;
import exceptions.TCCException;
import manager.DBManager;
import manager.Schedulable;
import manager.TransitScheduleManager;
import manager.TransitScheduler;
import service.BackendService;
import service.EphemService;
import service.TCCService;
import util.SMIRFConstants;
import util.TCCConstants;

@ManagedBean
@ApplicationScoped
public class TransitMB {
	
	String utc;
	String enteredUTC;
	Integer duration;
	Integer tobs;
	Integer durationUnits;
	Integer tobsUnits;
	Boolean phaseCal;
	Boolean doPulsarSearch;
	Boolean doTiming;
	String observer;
	Boolean enableTCC;
	Boolean enableBackend;
	String nsSpeed;
	List<String> nsSpeeds;
	PhaseCalibratorTO phaseCalibrator;
	List<PhaseCalibratorTO> phaseCalibrators;
	Boolean utcRendered = false;
	Double nsOffsetDeg;
	
	Schedulable scheduler = null;


	//TransitScheduleManager manager = new TransitScheduleManager();

	
	public TransitMB(){
		utc = "now";
		utcRendered = false;
		phaseCal = false;
		doPulsarSearch = true;
		doTiming = true;
		
		enableBackend = true;
		enableTCC = true;
		
		phaseCalibrators = DBManager.getAllPhaseCalibrators();
		
		tobs = SMIRFConstants.tobs;
		duration = 10;
		tobsUnits =1;
		durationUnits = 3600;
		nsOffsetDeg = 0.0;
		
		nsSpeeds = new ArrayList<>();
		nsSpeeds.add("Fast");
		nsSpeeds.add("Slow");
		nsSpeed = nsSpeeds.get(0);
		
	}
	
	public void addMessage(String summary) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  null);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public void addMessage(String summary, String detail) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  detail);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}
	
	public void startSMIRFTransit(ActionEvent event){
		
		if(Control.getScheduler() != null) {
			addMessage("Stop observation before starting another.");
		}
		
		if(this.utc.equals("now")) utc = EphemService.getUtcStringNow();
		else utc = enteredUTC + ".000";

		ObservationSessionTO observationSessionTO = new ObservationSessionTO(utc, observer, tobs * tobsUnits , duration * durationUnits, null, phaseCal, false, false, 0);
		DBManager.addSessionToDB(observationSessionTO);
		
		UserInputs inputs = new UserInputs();
		
		inputs.setSchedulerType(SMIRFConstants.dynamicTransitScheduler);
		
		inputs.setUtcStart(utc);
		inputs.setTobsInSecs(tobs * tobsUnits);
		inputs.setSessionTimeInSecs( duration * durationUnits );
		
		inputs.setDoPulsarSearching(doPulsarSearch);
		inputs.setDoPulsarTiming(doTiming);
		inputs.setEnableTCC(true);
		inputs.setEnableBackend(true);
		inputs.setMdTransit(true);
		inputs.setObserver(observer);
		inputs.setNsOffsetInDeg(nsOffsetDeg);
		inputs.setNsSpeed(nsSpeed.equals("Fast") ? TCCConstants.slewRateNSFast : TCCConstants.slewRateNSSlow );
		
		try {
		scheduler = TransitScheduler.createInstance(SMIRFConstants.dynamicTransitScheduler);
		
		scheduler.init(inputs);
		scheduler.start();
		
		}catch (SchedulerException e) {
			System.err.println("Exception starting schedluer");
			addMessage("Scheduler could not be started. Reason: " + e.getMessage(), ExceptionUtils.getStackTrace(e));
		}


//		manager.start(duration * durationUnits, tobs, observer, enableTCC, enableBackend, doPulsarSearch, true, observationSessionTO, 
//				nsSpeed.equals("Fast") ? TCCConstants.slewRateNSFast : TCCConstants.slewRateNSSlow, nsOffsetDeg );
//		
		
	}
	
	
public void terminateSchedule(ActionEvent event){
	

		if(Control.getScheduler() !=null){
			
			Control.getScheduler().terminate();
			addMessage("Terminated.");
			return;
		}
		
		addMessage("No scheduler running to terminate.");
//	
//		if(Control.getCurrentObservation() ==null){
//			addMessage("No observation running at the moment.");
//			return;
//		}
//		
//		Control.setTerminateCall(true);
//		
//		while(manager.getScheduler()!= null && manager.getScheduler().isDone());
//		
//		try {
//			
//			BackendService.createBackendInstance().stopBackend();
//			TCCService.createTccInstance().stopTelescope();
//
//		} catch (BackendException e) {
//			
//			e.printStackTrace();
//			addMessage(e.getMessage());
//
//			
//		} catch (TCCException e) {
//			
//			e.printStackTrace();
//			addMessage(e.getMessage());
//
//		}
//		
//		try {
//			manager.cancelSmirfingObservation(Control.getCurrentObservation());
//		} catch (IOException e) {
//			e.printStackTrace();
//			addMessage(e.getMessage());
//
//		}
//		Control.setTerminateCall(false);
//
//		addMessage("Terminated.");
	}

	public void finishSchedule(ActionEvent event){
		

		if(Control.getScheduler() !=null){
			
			Control.getScheduler().terminate();
			addMessage("Schedule will finish at the end of this observation.");
			return;
		}
		
		addMessage("No scheduler running to finish.");
		
//		if(Control.getCurrentObservation() ==null){
//			addMessage("No observation running at the moment.");
//			return;
//		}
//		
//		Control.setFinishCall(true);
//		addMessage("Schedule will finish at the end of this observation.");

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

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public Integer getTobs() {
		return tobs;
	}

	public void setTobs(Integer tobs) {
		this.tobs = tobs;
	}

	public Integer getDurationUnits() {
		return durationUnits;
	}

	public void setDurationUnits(Integer durationUnits) {
		this.durationUnits = durationUnits;
	}

	public Integer getTobsUnits() {
		return tobsUnits;
	}

	public void setTobsUnits(Integer tobsUnits) {
		this.tobsUnits = tobsUnits;
	}

	public Boolean getPhaseCal() {
		return phaseCal;
	}

	public void setPhaseCal(Boolean phaseCal) {
		this.phaseCal = phaseCal;
	}

	public Boolean getDoPulsarSearch() {
		return doPulsarSearch;
	}

	public void setDoPulsarSearch(Boolean doPulsarSearch) {
		this.doPulsarSearch = doPulsarSearch;
	}

	public Boolean getDoTiming() {
		return doTiming;
	}

	public void setDoTiming(Boolean doTiming) {
		this.doTiming = doTiming;
	}

	public String getObserver() {
		return observer;
	}

	public void setObserver(String observer) {
		this.observer = observer;
	}

	public Boolean getEnableTCC() {
		return enableTCC;
	}

	public void setEnableTCC(Boolean enableTCC) {
		this.enableTCC = enableTCC;
	}

	public Boolean getEnableBackend() {
		return enableBackend;
	}

	public void setEnableBackend(Boolean enableBackend) {
		this.enableBackend = enableBackend;
	}


	public String getNsSpeed() {
		return nsSpeed;
	}

	public void setNsSpeed(String nsSpeed) {
		this.nsSpeed = nsSpeed;
	}

	public List<String> getNsSpeeds() {
		return nsSpeeds;
	}

	public void setNsSpeeds(List<String> nsSpeeds) {
		this.nsSpeeds = nsSpeeds;
	}

	public PhaseCalibratorTO getPhaseCalibrator() {
		return phaseCalibrator;
	}

	public void setPhaseCalibrator(PhaseCalibratorTO phaseCalibrator) {
		this.phaseCalibrator = phaseCalibrator;
	}

	public List<PhaseCalibratorTO> getPhaseCalibrators() {
		return phaseCalibrators;
	}

	public void setPhaseCalibrators(List<PhaseCalibratorTO> phaseCalibrators) {
		this.phaseCalibrators = phaseCalibrators;
	}

	public Boolean getUtcRendered() {
		return utcRendered;
	}

	public void setUtcRendered(Boolean utcRendered) {
		this.utcRendered = utcRendered;
	}



}


