package bean;

import java.util.ArrayList;
import java.util.List;

public class UserInputs {

	private String schedulerType;
	
	private String utcStart;
	private Integer sessionTimeInSecs;
	private Integer tobsInSecs;
	
	private PhaseCalibratorTO phaseCalibratorTO;
	private FluxCalibratorTO fluxCalibratorTO;
	private Boolean doInterimFluxCals;
	
	private Boolean doPulsarTiming;
	private Boolean doPulsarSearching;
	private Boolean enableBackend;
	private Boolean enableTCC;
	private Boolean mdTransit;
	
	private String observer;
	
	private Double nsOffsetInDeg;
	private Double nsSpeed;
	
	List<PointingTO> pointingTOs;

	public UserInputs() {
		pointingTOs = new ArrayList<>();
		
	}
	
	public void addToList(PointingTO pointingTO){
		pointingTOs.add(pointingTO);
	}

	public String getSchedulerType() {
		return schedulerType;
	}

	public void setSchedulerType(String schedulerType) {
		this.schedulerType = schedulerType;
	}

	public String getUtcStart() {
		return utcStart;
	}

	public void setUtcStart(String utcStart) {
		this.utcStart = utcStart;
	}

	public Integer getSessionTimeInSecs() {
		return sessionTimeInSecs;
	}

	public void setSessionTimeInSecs(Integer sessionTimeInSecs) {
		this.sessionTimeInSecs = sessionTimeInSecs;
	}

	public Integer getTobsInSecs() {
		return tobsInSecs;
	}

	public void setTobsInSecs(Integer tobsInSecs) {
		this.tobsInSecs = tobsInSecs;
	}

	public PhaseCalibratorTO getPhaseCalibratorTO() {
		return phaseCalibratorTO;
	}

	public void setPhaseCalibratorTO(PhaseCalibratorTO phaseCalibratorTO) {
		this.phaseCalibratorTO = phaseCalibratorTO;
	}

	public FluxCalibratorTO getFluxCalibratorTO() {
		return fluxCalibratorTO;
	}

	public void setFluxCalibratorTO(FluxCalibratorTO fluxCalibratorTO) {
		this.fluxCalibratorTO = fluxCalibratorTO;
	}

	public Boolean getDoPulsarTiming() {
		return doPulsarTiming;
	}

	public void setDoPulsarTiming(Boolean doPulsarTiming) {
		this.doPulsarTiming = doPulsarTiming;
	}

	public Boolean getDoPulsarSearching() {
		return doPulsarSearching;
	}

	public void setDoPulsarSearching(Boolean doPulsarSearching) {
		this.doPulsarSearching = doPulsarSearching;
	}

	public Boolean getEnableBackend() {
		return enableBackend;
	}

	public void setEnableBackend(Boolean enableBackend) {
		this.enableBackend = enableBackend;
	}

	public Boolean getEnableTCC() {
		return enableTCC;
	}

	public void setEnableTCC(Boolean enableTCC) {
		this.enableTCC = enableTCC;
	}

	public Double getNsOffsetInDeg() {
		return nsOffsetInDeg;
	}

	public void setNsOffsetInDeg(Double nsOffsetInDeg) {
		this.nsOffsetInDeg = nsOffsetInDeg;
	}

	public List<PointingTO> getPointingTOs() {
		return pointingTOs;
	}

	public void setPointingTOs(List<PointingTO> pointingTOs) {
		this.pointingTOs = pointingTOs;
	}

	public Boolean getDoInterimFluxCals() {
		return doInterimFluxCals;
	}

	public void setDoInterimFluxCals(Boolean doInterimFluxCals) {
		this.doInterimFluxCals = doInterimFluxCals;
	}

	public Boolean getMdTransit() {
		return mdTransit;
	}

	public void setMdTransit(Boolean mdTransit) {
		this.mdTransit = mdTransit;
	}

	public String getObserver() {
		return observer;
	}

	public void setObserver(String observer) {
		this.observer = observer;
	}

	public Double getNsSpeed() {
		return nsSpeed;
	}

	public void setNsSpeed(Double nsSpeed) {
		this.nsSpeed = nsSpeed;
	}
	
	
	
}
