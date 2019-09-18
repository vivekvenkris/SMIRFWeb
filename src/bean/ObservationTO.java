package bean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.ObservationException;
import manager.DBManager;
import manager.PSRCATManager;
import service.EphemService;
import util.BackendConstants;
import util.Constants;
import util.SMIRFConstants;
import util.Utilities;

public class ObservationTO {
	Integer observationID;
	String name;
	Angle angleRA;
	Angle angleDEC;
	Integer tobs;
	String backendType;
	String obsType;
	String observer;
	String utc;
	Date utcDate;
	List<TBSourceTO> tiedBeamSources;
	Integer nfb;
	Angle fanbeamSpacing;
	Coords coords;
	ObservationSessionTO observingSession;
	Boolean delayTracking;
	String projectID;
	Double degNSOffset;
	
	Boolean mdTransit;
	Boolean doTiming;
	Boolean doPulsarSearch;
	Boolean backendEnabled;
	Boolean tccEnabled;
	
	Boolean isFRBTransit;
	
	

	private Integer managementStatus;
	private Boolean complete;

	
	@Override
	public String toString() {
		return "name: " + name + "\n" 
				+ " RA:" + this.coords.pointingTO.getAngleRA() + "\n"
				+ " DEC:" +  this.coords.pointingTO.getAngleDEC() + "\n" 
				+ " UTC: " + utc + "\n" 
				+ " TBs: " + this.getTiedBeamSources();
	}
	
	public ObservationTO() {
		nfb = 352; 
		//fanbeamSpacing = new Angle( "0.0113960114", Angle.DEG);
		//nfb = 512;
		double spacing  = Constants.RadMolongloMDBeamWidthForFB * Constants.rad2Deg  / (nfb-1);
		fanbeamSpacing = new Angle( spacing + "", Angle.DEG); //Added by WF/CF... To Vivek, maybe we could put this in a .cfg file
		tiedBeamSources = new ArrayList<TBSourceTO>();
		name = "";
		observer = "";
		delayTracking = true;
		
		mdTransit = doTiming = doPulsarSearch = true;
		
		complete = false;
		managementStatus = 0;
		
	}
	
	public ObservationTO(Coords coords, Integer tobs){
		this();
		this.coords = coords;
		this.tobs = tobs;
		this.angleRA = coords.getPointingTO().getAngleRA();
		this.angleDEC = coords.getPointingTO().getAngleDEC();
		
		complete = false;
		managementStatus = 0;
	}
	
	public ObservationTO(Coords coords, UserInputs ui, String backendType, String obsType,String projectID) throws ObservationException{
		this(coords,null, ui.getTobsInSecs(), ui.getObserver(), backendType, obsType, projectID, ui.getNsOffsetInDeg(),
				ui.getMdTransit(), ui.getDoPulsarTiming(), ui.getDoPulsarSearching(), ui.getEnableBackend(), ui.getEnableTCC());
	}
	
	public ObservationTO(Coords coords, ObservationSessionTO observationSessionTO, Integer tobs, String observer, 
			String backendType, String obsType, String projectID, Double degNSOffset, Boolean mdTransit,
			Boolean doTiming, Boolean doPulsarSearch, Boolean backendEnabled, Boolean tccEnabled) throws ObservationException{
		this();
		PointingTO pointingTO = coords.getPointingTO();
		
		this.observingSession = observationSessionTO;
		this.tobs = tobs == null ? pointingTO.getTobs(): tobs;
		this.observer = observer;
		this.backendType = backendType;
		this.obsType = obsType;
		this.name = pointingTO.getPointingName();
		this.angleDEC = pointingTO.getAngleDEC();
		this.angleRA = pointingTO.getAngleRA();
		this.coords = coords;
		this.projectID = projectID;
		
		if(!coords.getPointingTO().isSMIRFPointing() && !coords.getPointingTO().isFRBFollowUpPointing()){
			delayTracking = false;
		}
		
		this.degNSOffset = degNSOffset;
		this.mdTransit = mdTransit;
		this.doTiming = doTiming;
		this.doPulsarSearch = doPulsarSearch;
		this.backendEnabled  = backendEnabled;
		this.tccEnabled = tccEnabled;
		this.isFRBTransit = isFRBTransit;
		
		complete = false;
		managementStatus = 0;
		
	}
	
	public ObservationTO(Observation observation) {
		this();
		this.name = observation.getSourceName();
		try {
			String utc = observation.getUtc().contains(".") ? observation.getUtc() : observation.getUtc() + ".000";
			this.utc =  utc.charAt(10) == '-' ? utc : new SimpleDateFormat(BackendConstants.backendUTCFormat)
																			.format(new SimpleDateFormat(BackendConstants.backendUTCMySQLFormat)
																			.parse(utc));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.tobs = observation.getTobs();
		this.tiedBeamSources = Arrays.asList(observation.getTiedBeamSources().substring(1, observation.getTiedBeamSources().length() - 1).split(", "))
								.stream().map(t -> PSRCATManager.getTBSouceByName(t)).filter(t -> t!=null).collect(Collectors.toList());
		this.obsType = observation.getObservationType();
		
		if(observation.getObservingSession()!=null){
			this.observingSession = new ObservationSessionTO(observation.getObservingSession());
		}
		
		if(this.name !=null){
			this.coords = new Coords(DBManager.getPointingByUniqueName(this.name));
			
			if(this.utc != null ){
				try {
					
					this.coords = new Coords( DBManager.getPointingByUniqueName(this.name), Utilities.getUTCLocalDateTime(this.utc));
					
				} catch (EmptyCoordinatesException e) {
					e.printStackTrace();
					
				} catch (CoordinateOverrideException e) {
					e.printStackTrace();
					
				}

			}
		}
		
		
		this.complete = observation.getComplete();
		this.managementStatus = observation.getManagementStatus();
	}
	
	
	
	private double computeHAForMJD(double mjd){ 
		double radLMST = EphemService.getRadLMSTForMolonglo(mjd);
		double radRA = this.coords.getPointingTO().getAngleRA().getRadianValue();
		double radHA = radLMST - radRA;
		int sign = (radHA > 0)?1:-1;
		if( Math.abs(radHA) > 12*Constants.hrs2Rad)
			radHA = sign* (Math.abs(radHA) - 24*Constants.hrs2Rad);
		return radHA;
	}
	
	public Angle getHANow(){
		double mjd = EphemService.getMJDNow();
		double radHA =  computeHAForMJD(mjd);
		Angle HA = new Angle(radHA,Angle.RAD);
		return HA;
	}
	
	public Angle getHAForUTC(String utcStr){
		double mjd = EphemService.getMJDForUTC(utcStr);
		double radHA =  computeHAForMJD(mjd);
		Angle HA = new Angle(radHA,Angle.RAD);
		return HA;
	}
	public Angle getHAForUTCPlusOffset(String utcStr, int offsetSecondsFromUTC){
		double mjd = EphemService.getMJDForUTC(utcStr, offsetSecondsFromUTC);
		double radHA =  computeHAForMJD(mjd);
		Angle HA = new Angle(radHA,Angle.RAD);
		return HA;
	}
	
	
	
	public Angle getHAAfter(int offsetSecsFromNow){
		double mjd = EphemService.getMJDAfterOffset(offsetSecsFromNow);
		double radHA =  computeHAForMJD(mjd);
		Angle HA = new Angle(radHA,Angle.RAD);
		return HA;
	}
	
	public void setUTCDateAndString(String utc) throws ParseException{
		this.utc = utc;
		SimpleDateFormat sdf = new SimpleDateFormat(BackendConstants.backendUTCFormat);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		utcDate = sdf.parse(utc+".000");
	}
	
	
	
	
	public String getProjectID() {
		return projectID;
	}

	public void setProjectID(String projectID) {
		this.projectID = projectID;
	}

	public Boolean getDelayTracking() {
		return delayTracking;
	}

	public void setDelayTracking(Boolean delayTracking) {
		this.delayTracking = delayTracking;
	}

	public String getUtc() {
		return utc;
	}

	public Integer getTobs() {
		return tobs;
	}

	public void setTobs(Integer tobs) {
		this.tobs = tobs;
	}

	public String getBackendType() {
		return backendType;
	}

	public void setBackendType(String backendType) {
		this.backendType = backendType;
	}

	public String getObsType() {
		return obsType;
	}

	public void setObsType(String obsType) {
		this.obsType = obsType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getObserver() {
		return observer;
	}

	public void setObserver(String observer) {
		this.observer = observer;
	}

	

	
	public ObservationSessionTO getObservingSession() {
		return observingSession;
	}

	public void setObservingSession(ObservationSessionTO observingSession) {
		this.observingSession = observingSession;
	}

	public List<TBSourceTO> getTiedBeamSources() {
		return tiedBeamSources;
	}

	public void setTiedBeamSources(List<TBSourceTO> tiedBeamSources) {
		this.tiedBeamSources = tiedBeamSources;
	}

	
	public Integer getNfb() {
		return nfb;
	}

	public void setNfb(Integer nfb) {
		this.nfb = nfb;
	}

	public Angle getFanbeamSpacing() {
		return fanbeamSpacing;
	}

	public void setFanbeamSpacing(Angle fanbeamSpacing) {
		this.fanbeamSpacing = fanbeamSpacing;
	}

	public Coords getCoords() {
		return coords;
	}

	public void setCoords(Coords coords) {
		this.coords = coords;
	}
	
	
	public Angle getAngleRA() {
		return angleRA;
	}

	public void setAngleRA(Angle angleRA) {
		this.angleRA = angleRA;
	}

	public Angle getAngleDEC() {
		return angleDEC;
	}

	public void setAngleDEC(Angle angleDEC) {
		this.angleDEC = angleDEC;
	}

	public Date getUtcDate() {
		return utcDate;
	}

	public void setUtcDate(Date utcDate) {
		this.utcDate = utcDate;
	}
	

	


	public void setUtc(String utc) {
		this.utc = utc;
	}

	public Integer getObservationID() {
		return observationID;
	}

	public void setObservationID(Integer observationID) {
		this.observationID = observationID;
	}

	public boolean isGalacticPointing() throws ObservationException{
		try{
			return this.coords.getPointingTO().getType().equals(SMIRFConstants.galacticPointingSymbol);
		}catch(NullPointerException e){
			throw new ObservationException("Incomplete information on observation type.");
		}
	}
	
	public boolean isSMCPointing() throws ObservationException{
		try{
			return this.coords.getPointingTO().getType().equals(SMIRFConstants.smcPointingSymbol);
		}catch(NullPointerException e){
			throw new ObservationException("Incomplete information on observation type.");
		}
	}
	
	public boolean isLMCPointing() throws ObservationException{
		try{
			return this.coords.getPointingTO().getType().equals(SMIRFConstants.lmcPointingSymbol);
		}catch(NullPointerException e){
			throw new ObservationException("Incomplete information on observation type.");
		}
	}
	public boolean isPhaseCalPointing() throws ObservationException{
		try{
			return this.coords.getPointingTO().getType().equals(SMIRFConstants.phaseCalibratorSymbol);
		}catch(NullPointerException e){
			throw new ObservationException("Incomplete information on observation type.");
		}
	}
	public boolean isFluxCalPointing() throws ObservationException{
		try{
			return this.coords.getPointingTO().getType().equals(SMIRFConstants.fluxCalibratorSymbol);
		}catch(NullPointerException e){
			throw new ObservationException("Incomplete information on observation type.");
		}
	}
	public boolean isCandidatePointing() throws ObservationException{
		try{
			return this.coords.getPointingTO().getType().equals(SMIRFConstants.candidatePointingSymbol);
		}catch(NullPointerException e){
			throw new ObservationException("Incomplete information on observation type.");
		}
	}
	public boolean isSurveyPointing() throws ObservationException{
		try{
			String type = this.coords.getPointingTO().getType();
			return (SMIRFConstants.smcPointingSymbol + SMIRFConstants.lmcPointingSymbol + SMIRFConstants.galacticPointingSymbol).contains(type);
		}catch(NullPointerException e){
			throw new ObservationException("Incomplete information on observation type.");
		}
	}
	
	public boolean isTransitPointing() throws ObservationException{
		try{
			return this.coords.getPointingTO().getType().equals(SMIRFConstants.transitPointingSymbol);
		}catch(NullPointerException e){
			throw new ObservationException("Incomplete information on observation type.");
		}
	}

	public Double getDegNSOffset() {
		return degNSOffset;
	}

	public void setDegNSOffset(Double degNSOffset) {
		this.degNSOffset = degNSOffset;
	}

	public Boolean getMdTransit() {
		return mdTransit;
	}

	public void setMdTransit(Boolean mdTransit) {
		this.mdTransit = mdTransit;
	}

	public Boolean getDoTiming() {
		return doTiming;
	}

	public void setDoTiming(Boolean doTiming) {
		this.doTiming = doTiming;
	}

	public Boolean getDoPulsarSearch() {
		return doPulsarSearch;
	}

	public void setDoPulsarSearch(Boolean doPulsarSearch) {
		this.doPulsarSearch = doPulsarSearch;
	}

	public Boolean getBackendEnabled() {
		return backendEnabled;
	}

	public void setBackendEnabled(Boolean backendEnabled) {
		this.backendEnabled = backendEnabled;
	}

	public Boolean getTccEnabled() {
		return tccEnabled;
	}

	public void setTccEnabled(Boolean tccEnabled) {
		this.tccEnabled = tccEnabled;
	}

	public Integer getManagementStatus() {
		return managementStatus;
	}

	public void setManagementStatus(Integer managementStatus) {
		this.managementStatus = managementStatus;
	}

	public Boolean getComplete() {
		return complete;
	}

	public void setComplete(Boolean complete) {
		this.complete = complete;
	}

	
	public Boolean getIsFRBTransit() {
		return isFRBTransit;
	}

	public void setIsFRBTransit(Boolean isFRBTransit) {
		this.isFRBTransit = isFRBTransit;
	}

	
	
}
