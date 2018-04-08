package manager;

import java.util.Arrays;
import java.util.Collections;

import org.javatuples.Pair;

import bean.Angle;
import bean.CoordinateTO;
import bean.Coords;
import bean.ObservationTO;
import bean.PointingTO;
import bean.TCCStatus;
import control.Control;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.TCCException;
import service.EphemService;
import service.TCCService;
import service.TCCStatusService;
import util.Constants;
import util.TCCConstants;

public class TCCManager {
	
	/**
	 * If the East and West arm are pointing in different directions, make it come together before it tries to observe anything.
	 * To the future Vivek: This is a bit crude, I know. This is because it might become unnecessarily complicated to precisely
	 * observe a transit if I assume the mean of both the arms and calculate transit times.
	 
	 * @throws TCCException
	 * @throws CoordinateOverrideException
	 * @throws EmptyCoordinatesException
	 */
	public static void syncArmsToMedian() throws TCCException, CoordinateOverrideException, EmptyCoordinatesException{
		
		TCCStatus status = Control.getTccStatus();
		
		
		System.err.println("Checking NS positions of drives East = " + status.getNs().getEast().getTilt().getDegreeValue() + " "
				 + status.getNs().getWest().getTilt().getDegreeValue());

		/**
		 * If the difference in position is greater than the threshold.
		 */
		if( (status.getNs().getEast().getTilt().getDegreeValue() - status.getNs().getWest().getTilt().getDegreeValue() ) > 
		TCCConstants.OnSourceThresholdRadNS ){
			
			
			TCCService service = TCCService.createTccInstance();
			
			Coords coords = new Coords(status);
			
			System.err.println("Driving telescope to mean position: " + coords.getAngleNS());
			
			service.pointNS(coords.getAngleNS(), TCCConstants.BOTH_ARMS); 
			
			
		}
	}

	public static Integer computeSlewTime(double srcRadNS, double srcRadMD) throws TCCException{
		
		TCCStatus tccStatus = Control.getTccStatus();
		Pair<Double, Double> tiltMDNow = tccStatus.getTiltMD();
		Pair<Double,Double> tiltNSNow = tccStatus.getTiltNS();
		
		int rampTime = 10;
		
		double nsDiff = Math.min((Double)tiltNSNow.getValue(0) - srcRadNS, (Double)tiltNSNow.getValue(1) - srcRadNS);
		
		
		double speed = TCCConstants.slewRateNSSlow;
		if(nsDiff*Constants.rad2Deg > 1.5) speed = TCCConstants.slewRateNSFast;
	
		Pair<Double,Double> slewTimeNS = new Pair<Double, Double>(Math.abs((Double)tiltNSNow.getValue(0) - srcRadNS)*Constants.rad2Deg/speed
				, Math.abs((Double)tiltNSNow.getValue(1) - srcRadNS)*Constants.rad2Deg/speed);
		
		Pair<Double,Double> slewTimeMD = new Pair<Double, Double>(Math.abs((Double)tiltMDNow.getValue(0) - srcRadMD)*Constants.rad2Deg/TCCConstants.slewRateMD
				, Math.abs((Double)tiltMDNow.getValue(1) - srcRadMD)*Constants.rad2Deg/TCCConstants.slewRateMD);
		
		
		Double maxSlewTime = Collections.max(Arrays.asList(slewTimeNS.getValue0(),slewTimeNS.getValue1(),slewTimeMD.getValue0(),slewTimeMD.getValue1()));
		return (int)(1.2*(maxSlewTime + 2*rampTime));
				
	}
	
	public static Integer computeSlewTime(double radNS1, double radMD1, double radNS2, double radMD2){
		
		double slewTimeNS = Math.abs(radNS1 - radNS2)*Constants.rad2Deg/TCCConstants.slewRateNSSlow;		
		double slewTimeMD = Math.abs(radMD1 - radMD2)*Constants.rad2Deg/TCCConstants.slewRateMD;
		
		
		Double maxSlewTime = slewTimeNS > slewTimeMD ?slewTimeNS:slewTimeMD;
		return (int)(maxSlewTime + 2*TCCConstants.rampTime);
				
	}
	
	public static int computeNSSlewTime(Angle ns1, Angle ns2){
		
		double nsDiff = Math.abs(ns1.getDegreeValue() - ns2.getDegreeValue());
		
		double speed = TCCConstants.slewRateNSSlow;
		
		if(nsDiff*Constants.rad2Deg > 1) speed = TCCConstants.slewRateNSFast;
		
		return (int)Math.round(nsDiff/speed);
		
	}
		
	public static int computeNSSlewTime(Angle ns1, Angle ns2, Double slewDegPerSecond){
		
		return (int)Math.round(Math.abs((ns1.getDegreeValue() - ns2.getDegreeValue())/slewDegPerSecond));
		
	}
	
	public static void pointNS(PointingTO pointingTO) throws EmptyCoordinatesException, CoordinateOverrideException, TCCException{
		
		CoordinateTO to = new CoordinateTO(0.0, pointingTO.getAngleDEC().getRadianValue(), null, null);
		MolongloCoordinateTransforms.skyToTel(to);
		
		TCCService.createTccInstance().pointNS(to.getAngleNS(), TCCConstants.BOTH_ARMS);
	}
		
		
		
}
