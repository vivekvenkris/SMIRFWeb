package bean;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import manager.DBManager;
import manager.ObservationManager;
import manager.ScheduleManager;
import service.EphemService;
import util.BackendConstants;
import util.SMIRFConstants;
@ManagedBean
@ApplicationScoped
public class SingleMB {
	String utc;
	String enteredUTC;
	Boolean utcRendered = false;
	Integer tobs;
	Integer tobsUnits;
	
	Boolean tccEnabled = true;
	Boolean backendEnabled = true;
	
	String observer = "VVK";

	
	List<String> pointingTypes;
	String selectedPointingType;
	
	List<PointingTO> pointings;
	PointingTO selectedPointing;
	String selectedPointingName;
	ScheduleManager manager = new ScheduleManager();

	
	public SingleMB(){
		utc = "now";
		utcRendered = false;
		tobs = 300;
		tobsUnits = 1;

		pointingTypes = DBManager.getAllPointingTypes();
		pointingTypes.add(SMIRFConstants.phaseCalibratorSymbol);
		pointingTypes.add(SMIRFConstants.fluxCalibratorSymbol);
		pointings = DBManager.getAllPointings();
		pointings.addAll(PointingTO.getFluxCalPointingList(DBManager.getAllFluxCalibrators()));
		pointings.addAll(PointingTO.getPhaseCalPointingList(DBManager.getAllPhaseCalibrators()));
		
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
	public void pointingTypeSelected(){
		System.err.println("called...");
		pointings.clear();
		switch (selectedPointingType) {
		case "All":
			pointings = DBManager.getAllPointings();
			pointings.addAll(PointingTO.getFluxCalPointingList(DBManager.getAllFluxCalibrators()));
			pointings.addAll(PointingTO.getPhaseCalPointingList(DBManager.getAllPhaseCalibrators()));
			break;
		case SMIRFConstants.phaseCalibratorSymbol:
			pointings.addAll(PointingTO.getPhaseCalPointingList(DBManager.getAllPhaseCalibrators()));
			break;
		case SMIRFConstants.fluxCalibratorSymbol:
			pointings.addAll(PointingTO.getFluxCalPointingList(DBManager.getAllFluxCalibrators()));
			break;

		default:
			pointings = DBManager.getAllPointingsForPointingType(selectedPointingType);
			break;
			
		}
	}
	
	public void pointingSelected(){
		
		if(selectedPointingName.startsWith(SMIRFConstants.SMIRFPointingPrefix)){
			
			selectedPointing = DBManager.getPointingByUniqueName(selectedPointingName);
			
		}
		else if(selectedPointingName.startsWith(SMIRFConstants.fluxCalPointingPrefix)) {
			
			selectedPointing = DBManager.getFluxCalByUniqueName(selectedPointingName);
			
		}
		else if(selectedPointingName.startsWith(SMIRFConstants.phaseCalPointingPrefix)) {
			
			selectedPointing = DBManager.getPhaseCalByUniqueName(selectedPointingName);
			
		}
	}
	
	public void startObservation(ActionEvent event){
		try{
			System.err.println(selectedPointing);
			System.err.println(tobs);
			System.err.println(tobsUnits);
			System.err.println(observer);
			System.err.println(tccEnabled);
			System.err.println(backendEnabled);
			manager.startObserving(selectedPointing,tobs*tobsUnits,observer,tccEnabled,backendEnabled);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void terminateObservation(ActionEvent event){
		manager.terminate();
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

	public List<String> getPointingTypes() {
		return pointingTypes;
	}

	public void setPointingTypes(List<String> pointingTypes) {
		this.pointingTypes = pointingTypes;
	}

	public String getSelectedPointingType() {
		return selectedPointingType;
	}

	public void setSelectedPointingType(String selectedPointingType) {
		this.selectedPointingType = selectedPointingType;
	}

	public List<PointingTO> getPointings() {
		return pointings;
	}

	public void setPointings(List<PointingTO> pointings) {
		this.pointings = pointings;
	}

	public PointingTO getSelectedPointing() {
		return selectedPointing;
	}

	public void setSelectedPointing(PointingTO selectedPointing) {
		this.selectedPointing = selectedPointing;
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

	public String getSelectedPointingName() {
		return selectedPointingName;
	}

	public void setSelectedPointingName(String selectedPointingName) {
		this.selectedPointingName = selectedPointingName;
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
	
	
}
