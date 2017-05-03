package bean;

public class ObservationSessionTO {
	private Integer sessionID;
	
	private String startUTC;
	
	private Integer pointingTobs;
	
	private Integer sessionDuration;
	
	private Boolean mdMajor;
	
	private Boolean phaseCalibrateAtStart;
	
	private Boolean fluxCalibrateAtStart;
	
	private Boolean fluxCalibrateWhenever;
	
	private Integer numPlannedPointings;
	
	private Integer numPointingsDone;
	
	private String observer;
	
	public ObservationSessionTO(String startUTC,String observer, Integer pointingTobs,Integer sessionDuration,Boolean mdMajor,Boolean phaseCalibrateAtStart,Boolean fluxCalibrateAtStart,Boolean fluxCalibrateWhenever,Integer numPlannedPointings ) {
		this.startUTC =startUTC;
		this.observer = observer;
		this.pointingTobs = pointingTobs;
		this.sessionDuration = sessionDuration;
		this.mdMajor = mdMajor;
		this.phaseCalibrateAtStart = phaseCalibrateAtStart;
		this.fluxCalibrateAtStart = fluxCalibrateAtStart;
		this.fluxCalibrateWhenever = fluxCalibrateWhenever;
	}

	public Integer getSessionID() {
		return sessionID;
	}

	public void setSessionID(Integer sessionID) {
		this.sessionID = sessionID;
	}

	public String getStartUTC() {
		return startUTC;
	}

	public void setStartUTC(String startUTC) {
		this.startUTC = startUTC;
	}

	public Integer getPointingTobs() {
		return pointingTobs;
	}

	public void setPointingTobs(Integer pointingTobs) {
		this.pointingTobs = pointingTobs;
	}

	public Integer getSessionDuration() {
		return sessionDuration;
	}

	public void setSessionDuration(Integer sessionDuration) {
		this.sessionDuration = sessionDuration;
	}

	public Boolean getMdMajor() {
		return mdMajor;
	}

	public void setMdMajor(Boolean mdMajor) {
		this.mdMajor = mdMajor;
	}

	public Boolean getPhaseCalibrateAtStart() {
		return phaseCalibrateAtStart;
	}

	public void setPhaseCalibrateAtStart(Boolean phaseCalibrateAtStart) {
		this.phaseCalibrateAtStart = phaseCalibrateAtStart;
	}

	public Boolean getFluxCalibrateAtStart() {
		return fluxCalibrateAtStart;
	}

	public void setFluxCalibrateAtStart(Boolean fluxCalibrateAtStart) {
		this.fluxCalibrateAtStart = fluxCalibrateAtStart;
	}

	public Boolean getFluxCalibrateWhenever() {
		return fluxCalibrateWhenever;
	}

	public void setFluxCalibrateWhenever(Boolean fluxCalibrateWhenever) {
		this.fluxCalibrateWhenever = fluxCalibrateWhenever;
	}

	public Integer getNumPlannedPointings() {
		return numPlannedPointings;
	}

	public void setNumPlannedPointings(Integer numPlannedPointings) {
		this.numPlannedPointings = numPlannedPointings;
	}

	public Integer getNumPointingsDone() {
		return numPointingsDone;
	}

	public void setNumPointingsDone(Integer numPointingsDone) {
		this.numPointingsDone = numPointingsDone;
	}

	public String getObserver() {
		return observer;
	}

	public void setObserver(String observer) {
		this.observer = observer;
	}
	
	
}
