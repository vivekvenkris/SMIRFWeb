package bean;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import control.Control;
import exceptions.BackendException;
import exceptions.TCCException;
import manager.DBManager;
import manager.ObservationManager;
import manager.Schedulable;
import manager.ScheduleManager;
import manager.StaticTransitScheduler;
import manager.TransitScheduler;
import service.BackendService;
import service.EphemService;
import service.TCCService;
import util.BackendConstants;
import util.SMIRFConstants;
import util.TCCConstants;
@ManagedBean
@ApplicationScoped
public class SingleMB {
	String utc;
	String enteredUTC;
	Boolean utcRendered;
	Integer tobs;
	Integer tobsUnits;

	Boolean tccEnabled;
	Boolean backendEnabled;

	String observer = "The SMIRF Lord";

	List<String> pointingTypes;
	String selectedPointingType;

	List<PointingTO> pointings;
	PointingTO selectedPointing;
	String selectedPointingName;

	Schedulable scheduler = null;

	Boolean doPulsarSearch;
	Boolean doTiming;
	Boolean mdTransit;
	
	Double nsOffsetInDeg;
	String nsSpeed;
	List<String> nsSpeeds;

	public SingleMB(){
		utc = "now";
		utcRendered = false;
		tobs = SMIRFConstants.tobs;
		tobsUnits = 1;
		
		mdTransit = doPulsarSearch = doTiming = tccEnabled = backendEnabled = true;

		pointingTypes = DBManager.getAllPointingTypes();
		pointingTypes.add(SMIRFConstants.phaseCalibratorSymbol);
		pointingTypes.add(SMIRFConstants.fluxCalibratorSymbol);
		
		pointings = DBManager.getAllPointings();
		pointings = pointings.stream()
				.sorted(Comparator.comparing(f -> ((PointingTO)f).getAngleRA().getDecimalHourValue()))
				.collect(Collectors.toList());
		
		pointings.addAll(PointingTO.getFluxCalPointingList(DBManager.getAllFluxCalibrators()));
		pointings.addAll(PointingTO.getPhaseCalPointingList(DBManager.getAllPhaseCalibrators()));
		
		nsSpeeds = new ArrayList<>();
		nsSpeeds.add("Fast");
		nsSpeeds.add("Slow");
		nsSpeed = nsSpeeds.get(0);
		
		nsOffsetInDeg = 0.0;
		

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

		pointings.clear();

		if(selectedPointingType.equals("All")){

			pointings = DBManager.getAllPointings();
			
			pointings = pointings.stream()
			.sorted(Comparator.comparing(f -> ((PointingTO)f).getAngleRA().getDecimalHourValue()))
			.collect(Collectors.toList());
			
			pointings.addAll(PointingTO.getFluxCalPointingList(DBManager.getAllFluxCalibrators()));
			pointings.addAll(PointingTO.getPhaseCalPointingList(DBManager.getAllPhaseCalibrators()));
			
			

		}
		else if(selectedPointingType.equals(SMIRFConstants.phaseCalibratorSymbol)){
			
			pointings.addAll(PointingTO.getPhaseCalPointingList(DBManager.getAllPhaseCalibrators()));
			
		}
		else if(selectedPointingType.equals(SMIRFConstants.fluxCalibratorSymbol)) {
			
			pointings.addAll(PointingTO.getFluxCalPointingList(DBManager.getAllFluxCalibrators()));
			
		}
		else{
			
			pointings = DBManager.getAllPointingsForPointingType(selectedPointingType);
			
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
		else{
			selectedPointing =  DBManager.getPointingByUniqueName(selectedPointingName);
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
			
			UserInputs inputs = new UserInputs();
			
			if(mdTransit != true) {
				System.err.println("Tracking scheduler not supported yet.");
				addMessage("Tracking scheduler not supported yet. set md transit = true.");
				return;
			}
			
			inputs.setSchedulerType(SMIRFConstants.staticTransitScheduler);
			
			inputs.setUtcStart(EphemService.getUtcStringNow());
			inputs.setTobsInSecs(tobs * tobsUnits);
			inputs.setSessionTimeInSecs(null);
			
			inputs.setDoPulsarSearching(doPulsarSearch);
			inputs.setDoPulsarTiming(doTiming);
			inputs.setEnableTCC(tccEnabled);
			inputs.setEnableBackend(backendEnabled);
			inputs.setMdTransit(mdTransit);
			inputs.setObserver(observer);
			inputs.setNsOffsetInDeg(nsOffsetInDeg);
			inputs.setNsSpeed(nsSpeed.equals("Fast") ? TCCConstants.slewRateNSFast : TCCConstants.slewRateNSSlow );
			inputs.addToList(selectedPointing);
			
			scheduler = TransitScheduler.createInstance(SMIRFConstants.staticTransitScheduler);
			
			scheduler.init(inputs);
			scheduler.start();
			
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void terminateObservation(ActionEvent event){
		
		if(Control.getScheduler() !=null){
			
			Control.getScheduler().terminate();
			addMessage("Terminated.");
			return;
		}
		
		addMessage("No scheduler running to terminate.");
	
	}

	public void addMessage(String summary, Severity severity) {
		FacesMessage message = new FacesMessage(severity, summary,  null);
		FacesContext.getCurrentInstance().addMessage(null, message);
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



	public String getObserver() {
		return observer;
	}

	public void setObserver(String observer) {
		this.observer = observer;
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

	public Boolean getMdTransit() {
		return mdTransit;
	}

	public void setMdTransit(Boolean mdTransit) {
		this.mdTransit = mdTransit;
	}

	public Schedulable getScheduler() {
		return scheduler;
	}

	public void setScheduler(Schedulable scheduler) {
		this.scheduler = scheduler;
	}

	public Double getNsOffsetInDeg() {
		return nsOffsetInDeg;
	}

	public void setNsOffsetInDeg(Double nsOffsetInDeg) {
		this.nsOffsetInDeg = nsOffsetInDeg;
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


}
