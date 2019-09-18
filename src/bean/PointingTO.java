package bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;

import org.jastronomy.jsofa.JSOFA;
import org.jastronomy.jsofa.JSOFA.SphericalCoordinate;

import exceptions.ObservationException;
import manager.DBManager;
import manager.PSRCATManager;
import service.DBService;
import service.EphemService;
import util.SMIRFConstants;

public class PointingTO {
	
	
	private Integer pointingID;
	private String pointingName;
	private Angle angleLAT;
	private Angle angleLON;
	private Angle angleRA;	
	private Angle angleDEC;
	private Integer priority;
	private String type;
	private Integer numObs = 0;
	private boolean precessed;
	private Integer tobs;
	private Integer leastCadanceInDays;
	private List<TBSourceTO> associatedPulsars;
	
	private Integer startMDInPercent;
	
	private Integer endMDInPercent;
	
	public Double getMaxUnObservedDays() {
		try {
			return (associatedPulsars != null && !associatedPulsars.isEmpty())? 
					associatedPulsars.stream().filter(f->f.getDaysSinceLastObserved() != null ).mapToDouble(f -> f.getDaysSinceLastObserved()).max().orElse(0.0):
						(this.isSMIRFPointing()? DBManager.getDaysSinceObserved(pointingName):0.0);
		} catch (ObservationException e) {
			e.printStackTrace();
			return 0.0;
		}
		
	}
	
	
	public static void updatePointing(Pointing pointing, PointingTO pointingTO){
		pointing.setPointingName(pointingTO.getPointingName());
		pointing.setAngleLAT(pointingTO.getAngleLAT());
		pointing.setAngleLON(pointingTO.getAngleLON());
		pointing.setAngleRA(pointingTO.getAngleRA());
		pointing.setAngleDEC(pointingTO.getAngleDEC());
		
		pointing.setType(pointingTO.getType());
		pointing.setPriority(pointingTO.getPriority());
		pointing.setNumObs( pointingTO.getNumObs());
		pointing.setTobs(pointingTO.getTobs());
		pointing.setLeastCadanceInDays(pointingTO.getLeastCadanceInDays());
		pointing.setAssociatedPulsars(String.join(";",pointingTO.getAssociatedPulsars().stream().map(f -> f.getPsrName()).collect(Collectors.toList())));
		pointing.setStartMDInPercent(pointingTO.getStartMDInPercent());
		pointing.setEndMDInPercent(pointingTO.getEndMDInPercent());
	
	}
	
	
	public Integer getTobs() {
		return tobs;
	}
	public void setTobs(Integer tobs) {
		this.tobs = tobs;
	}
	
	public void addToAssociatedPulsars(TBSourceTO to) {
		if(this.associatedPulsars == null) this.associatedPulsars = new ArrayList<>();
			this.associatedPulsars.add(to);
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(obj instanceof PointingTO && ((PointingTO)obj).getPointingName().equals(this.pointingName)) return true;
		return false;
	}

	@Override
	public String toString() {
		if(!this.type.equals(SMIRFConstants.phaseCalibratorSymbol) && ! this.type.equals(SMIRFConstants.fluxCalibratorSymbol)) return "("+this.pointingID+") "+this.pointingName;
		 return "("+this.type+") "+this.pointingName;
	}
	
//	public PointingTO(Integer pointingID, String PointingName, String raStr, String decStr){
//		this.pointingID = pointingID;
//		this.pointingName = PointingName;
//		this.angleRA = new Angle(raStr,Angle.HHMMSS);
//		this.angleDEC = new Angle(decStr, Angle.DDMMSS);
//		
//		this.priority =10;
//		this.type = SMIRFConstants.randomPointingSymbol;
//		this.numObs = 0;
//		this.tobs = -1;
//		
//		SphericalCoordinate sc = JSOFA.jauIcrs2g(angleRA.getRadianValue(), angleDEC.getRadianValue());
//		this.angleLON = new Angle(sc.alpha, Angle.DDMMSS);
//		this.angleLAT = new Angle(sc.delta, Angle.DDMMSS);
//		this.associatedPulsars = new ArrayList<>();
//	
//		
//	}
	
	public PointingTO(Angle RA, Angle DEC){
		
		this.pointingID = null;
		this.pointingName = null;
		this.angleRA = RA;
		this.angleDEC = DEC;
		
		this.type = SMIRFConstants.randomPointingSymbol;
		this.priority = SMIRFConstants.lowestPriority;
		this.numObs = 0;
		this.tobs = -1;

		SphericalCoordinate sc = JSOFA.jauIcrs2g(angleRA.getRadianValue(), angleDEC.getRadianValue());
		this.angleLON = new Angle(sc.alpha, Angle.DDMMSS);
		if(this.angleLON.getRadianValue() > Math.PI ) this.angleLON.setRadValue(this.angleLON.getRadianValue() - 2*Math.PI);
		this.angleLAT = new Angle(sc.delta, Angle.DDMMSS);
		
		this.associatedPulsars = new ArrayList<>();
		
		this.startMDInPercent = -25;
		this.endMDInPercent = 25;

		
	}
	
	public PointingTO(Angle RA, Angle DEC, String pointingName,String type){
		this(RA, DEC);
		this.pointingName = pointingName;
		this.pointingID = -1;
		this.type = type;
	}

	
	
	public PointingTO(Pointing pointing) {
		this.pointingID = pointing.getPointingID();
		this.pointingName = pointing.getPointingName();
		this.angleLAT = pointing.getAngleLAT();
		this.angleLON = pointing.getAngleLON();
		this.angleRA = pointing.getAngleRA();
		this.angleDEC = pointing.getAngleDEC();
		this.priority = pointing.getPriority();
		this.type = pointing.getType();
		this.numObs = pointing.getNumObs();
		this.tobs = pointing.getTobs();
		this.leastCadanceInDays = pointing.getLeastCadanceInDays();
		if(pointing.getAssociatedPulsars() != null)
			this.associatedPulsars = Arrays.asList(pointing.getAssociatedPulsars().split(",")).stream().map(f -> PSRCATManager.getTimingProgrammeSouceByName(f)).filter(f -> f !=null).collect(Collectors.toList());
		else this.associatedPulsars = new ArrayList<>();
		
		this.startMDInPercent = pointing.getStartMDInPercent();
		this.endMDInPercent = pointing.getEndMDInPercent();
		if(this.associatedPulsars != null && !this.associatedPulsars.isEmpty())
		this.leastCadanceInDays = this.associatedPulsars.stream().map(f-> DBService.getDesiredCadence(f.getPsrName())).min(Integer::compare).get();
		
	}
	
	public PointingTO(PhaseCalibratorTO calibratorTO){
		this.pointingID = calibratorTO.getSourceID();
		this.pointingName = calibratorTO.getSourceName();
		this.angleRA = calibratorTO.getAngleRA();
		this.angleDEC = calibratorTO.getAngleDEC();
		this.type = SMIRFConstants.phaseCalibratorSymbol;
		this.priority = SMIRFConstants.highestPriority;
		/**
		 * Check this coordinate conversion. - 1950 and J2000 .
		 */
		SphericalCoordinate sc = JSOFA.jauIcrs2g(angleRA.getRadianValue(), angleDEC.getRadianValue());
		this.angleLON = new Angle(sc.alpha, Angle.DDMMSS);
		if(this.angleLON.getRadianValue() > Math.PI ) this.angleLON.setRadValue(this.angleLON.getRadianValue() - 2*Math.PI);
		this.angleLAT = new Angle(sc.delta, Angle.DDMMSS);
		
		
	}
	
	public PointingTO(FluxCalibratorTO calibratorTO){
		this.pointingID = calibratorTO.getSourceID();
		this.pointingName = calibratorTO.getSourceName();
		this.angleRA = calibratorTO.getAngleRA();
		this.angleDEC = calibratorTO.getAngleDEC();
		this.type = SMIRFConstants.fluxCalibratorSymbol;
		this.priority = SMIRFConstants.highestPriority;
		/**
		 * Check this coordinate conversion. - 1950 and J2000 shit.
		 */
		SphericalCoordinate sc = JSOFA.jauIcrs2g(angleRA.getRadianValue(), angleDEC.getRadianValue());
		this.angleLON = new Angle(sc.alpha, Angle.DDMMSS);
		if(this.angleLON.getRadianValue() > Math.PI ) this.angleLON.setRadValue(this.angleLON.getRadianValue() - 2*Math.PI);
		this.angleLAT = new Angle(sc.delta, Angle.DDMMSS);
		
		
	}
	
	public PointingTO(TBSourceTO sourceTO){
		
		this.pointingID = -1;
		this.pointingName = sourceTO.getPsrName();
		this.angleDEC = sourceTO.getAngleDEC();
		this.angleRA = sourceTO.getAngleRA();
		this.type = SMIRFConstants.randomPointingSymbol;
		this.priority = SMIRFConstants.lowestPriority;
		
		/**
		 * Check this coordinate conversion. - 1950 and J2000 shit.
		 */
		SphericalCoordinate sc = JSOFA.jauIcrs2g(angleRA.getRadianValue(), angleDEC.getRadianValue());
		this.angleLON = new Angle(sc.alpha, Angle.DDMMSS);
		if(this.angleLON.getRadianValue() > Math.PI ) this.angleLON.setRadValue(this.angleLON.getRadianValue() - 2*Math.PI);
		this.angleLAT = new Angle(sc.delta, Angle.DDMMSS);
		
	}
	
	public static List<PointingTO> getFluxCalPointingList( List<FluxCalibratorTO> fluxCalibratorTOs){
		List<PointingTO> pointingTOs = new ArrayList<>();
		for(FluxCalibratorTO fto: fluxCalibratorTOs) pointingTOs.add(new PointingTO(fto));
		return pointingTOs;
	}
	
	public static List<PointingTO> getPhaseCalPointingList( List<PhaseCalibratorTO> phaseCalibratorTOs){
		List<PointingTO> pointingTOs = new ArrayList<>();
		for(PhaseCalibratorTO pto: phaseCalibratorTOs) pointingTOs.add(new PointingTO(pto));
		return pointingTOs;
	}
	
	public void incrementNumObs(){
		numObs++;
	}
	
	public Integer getPointingID() {
		return pointingID;
	}
	public void setPointingID(Integer pointingID) {
		this.pointingID = pointingID;
	}
	public String getPointingName() {
		return pointingName;
	}
	public void setPointingName(String pointingName) {
		this.pointingName = pointingName;
	}
	public Angle getAngleLAT() {
		return angleLAT;
	}
	public void setAngleLAT(Angle angleLAT) {
		this.angleLAT = angleLAT;
	}
	public Angle getAngleLON() {
		return angleLON;
	}
	public void setAngleLON(Angle angleLON) {
		this.angleLON = angleLON;
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
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}


	public Integer getNumObs() {
		return numObs;
	}


	public void setNumObs(Integer numObs) {
		this.numObs = numObs;
	}
	
	

	public boolean isPrecessed() {
		return precessed;
	}

	public void setPrecessed(boolean precessed) {
		this.precessed = precessed;
	}
	
	
	
	public Integer getLeastCadanceInDays() {
		return leastCadanceInDays;
	}
	public void setLeastCadanceInDays(Integer leastCadanceInDays) {
		this.leastCadanceInDays = leastCadanceInDays;
	}
	public boolean isGalacticPointing() throws ObservationException{
		try{
			return this.getType().equals(SMIRFConstants.galacticPointingSymbol);
		}catch(NullPointerException e){
			throw new ObservationException("Incomplete information on observation type.");
		}
	}
	
	public boolean isSMCPointing() throws ObservationException{
		try{
			return this.getType().equals(SMIRFConstants.smcPointingSymbol);
		}catch(NullPointerException e){
			throw new ObservationException("Incomplete information on observation type.");
		}
	}
	
	public boolean isLMCPointing() throws ObservationException{
		try{
			return this.getType().equals(SMIRFConstants.lmcPointingSymbol);
		}catch(NullPointerException e){
			throw new ObservationException("Incomplete information on observation type.");
		}
	}
	public boolean isPhaseCalPointing() throws ObservationException{
		try{
			return this.getType().equals(SMIRFConstants.phaseCalibratorSymbol);
		}catch(NullPointerException e){
			throw new ObservationException("Incomplete information on observation type.");
		}
	}
	public boolean isFluxCalPointing() throws ObservationException{
		try{
			return this.getType().equals(SMIRFConstants.fluxCalibratorSymbol);
		}catch(NullPointerException e){
			throw new ObservationException("Incomplete information on observation type.");
		}
	}
	public boolean isCandidatePointing() throws ObservationException{
		try{
			return this.getType().equals(SMIRFConstants.candidatePointingSymbol);
		}catch(NullPointerException e){
			throw new ObservationException("Incomplete information on observation type.");
		}
	}
	public boolean isSMIRFPointing() throws ObservationException{
		try{
			String type = this.getType();
			return (SMIRFConstants.smcPointingSymbol + SMIRFConstants.lmcPointingSymbol + SMIRFConstants.galacticPointingSymbol + SMIRFConstants.candidatePointingSymbol).contains(type);
		}catch(NullPointerException e){
			throw new ObservationException("Incomplete information on observation type.");
		}
	}
	
	public boolean isTransitPointing() throws ObservationException{
		try{
			return this.getType().equals(SMIRFConstants.transitPointingSymbol);
		}catch(NullPointerException e){
			throw new ObservationException("Incomplete information on observation type.");
		}
	}

	
	public boolean isFRBFollowUpPointing() throws ObservationException{
		try{
			return this.getType().equals(SMIRFConstants.frbFieldPointingSymbol);
		}catch(NullPointerException e){
			throw new ObservationException("Incomplete information on observation type.");
		}
	}
	
	public boolean isPulsarPointing() throws ObservationException{
		try{
			return this.getType().equals(SMIRFConstants.pulsarPointingSymbol);
		}catch(NullPointerException e){
			throw new ObservationException("Incomplete information on observation type.");
		}
	}
	public List<TBSourceTO> getAssociatedPulsars() {
		return associatedPulsars;
	}
	public void setAssociatedPulsars(List<TBSourceTO> associatedPulsars) {
		this.associatedPulsars = associatedPulsars;
	}


	public Integer getStartMDInPercent() {
		return startMDInPercent;
	}


	public void setStartMDInPercent(Integer startMDInPercent) {
		this.startMDInPercent = startMDInPercent;
	}


	public Integer getEndMDInPercent() {
		return endMDInPercent;
	}


	public void setEndMDInPercent(Integer endMDInPercent) {
		this.endMDInPercent = endMDInPercent;
	}
	
	
	
}
