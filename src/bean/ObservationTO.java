package bean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import exceptions.ObservationException;
import service.EphemService;
import util.BackendConstants;
import util.Constants;
import util.SMIRFConstants;

public class ObservationTO {
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
	
	public ObservationTO() {
		nfb = 352;
		fanbeamSpacing = new Angle("0.01139601", Angle.DEG);
		tiedBeamSources = new ArrayList<TBSourceTO>();
		name = "";
		observer = "";
		
	}
	
	public ObservationTO(Coords coords, Integer tobs){
		this();
		this.coords = coords;
		this.tobs = tobs;
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
}
