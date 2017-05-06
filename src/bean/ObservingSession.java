package bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="observing_sessions")
public class ObservingSession {
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	@Column(name = "session_id")
	private Integer sessionID;
	
	@Column(name = "start_utc")
	private String startUTC;
	
	@Column(name = "tobs_per_pointing_seconds")
	private Integer pointingTobs;
	
	@Column(name = "session_duration_seconds")
	private Integer sessionDuration;
	
	@Column(name = "md_major_order")
	private Boolean mdMajor;
	
	@Column(name = "phasecal_start")
	private Boolean phaseCalibrateAtStart;
	
	@Column(name = "fluxcal_start")
	private Boolean fluxCalibrateAtStart;
	
	@Column(name = "fluxcal_whenever")
	private Boolean fluxCalibrateWhenever;
	
	@Column(name = "npointings_planned")
	private Integer numPlannedPointings;
	
	@Column(name = "npointings_done")
	private Integer numPointingsDone;
	
	private String observer;

	public ObservingSession() {	}
	public ObservingSession(ObservationSessionTO observationSessionTO) {	
		this.startUTC = observationSessionTO.getStartUTC();
		this.pointingTobs = observationSessionTO.getPointingTobs();
		this.sessionDuration = observationSessionTO.getSessionDuration();
		this.mdMajor = observationSessionTO.getMdMajor();
		this.phaseCalibrateAtStart = observationSessionTO.getPhaseCalibrateAtStart();
		this.fluxCalibrateAtStart = observationSessionTO.getFluxCalibrateAtStart();
		this.fluxCalibrateWhenever = observationSessionTO.getFluxCalibrateWhenever();
		this.observer = observationSessionTO.getObserver();
		this.numPlannedPointings = observationSessionTO.getNumPlannedPointings();
		this.numPointingsDone = 0;
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
