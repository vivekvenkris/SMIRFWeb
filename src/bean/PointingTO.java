package bean;

import java.util.ArrayList;
import java.util.List;

import org.jastronomy.jsofa.JSOFA;
import org.jastronomy.jsofa.JSOFA.SphericalCoordinate;

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

	@Override
	public String toString() {
		if(!this.type.equals(SMIRFConstants.phaseCalibratorSymbol) && ! this.type.equals(SMIRFConstants.fluxCalibratorSymbol)) return "("+this.pointingID+") "+this.pointingName;
		 return "("+this.type+") "+this.pointingName;
	}
	
	public PointingTO(Integer pointingID, String PointingName, String raStr, String decStr){
		this.pointingID = pointingID;
		this.pointingName = PointingName;
		this.angleRA = new Angle(raStr,Angle.HHMMSS);
		this.angleDEC = new Angle(decStr, Angle.DDMMSS);
		
		this.priority =10;
		this.type = SMIRFConstants.randomPointingSymbol;
		this.numObs = 0;
		
		SphericalCoordinate sc = JSOFA.jauIcrs2g(angleRA.getRadianValue(), angleDEC.getRadianValue());
		this.angleLON = new Angle(sc.alpha, Angle.DDMMSS);
		this.angleLAT = new Angle(sc.delta, Angle.DDMMSS);
		
		
	}
	
	public PointingTO(Angle RA, Angle DEC){
		
		this.pointingID = null;
		this.pointingName = null;
		this.angleRA = RA;
		this.angleDEC = DEC;
		
		this.type = SMIRFConstants.randomPointingSymbol;
		this.priority =null;
		this.numObs = null;

		SphericalCoordinate sc = JSOFA.jauIcrs2g(angleRA.getRadianValue(), angleDEC.getRadianValue());
		this.angleLON = new Angle(sc.alpha, Angle.DDMMSS);
		if(this.angleLON.getRadianValue() > Math.PI ) this.angleLON.setRadValue(this.angleLON.getRadianValue() - 2*Math.PI);
		this.angleLAT = new Angle(sc.delta, Angle.DDMMSS);
		
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
	

}
