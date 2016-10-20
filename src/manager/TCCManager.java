package manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.javatuples.Pair;

import bean.Observation;
import bean.TCCStatus;
import exceptions.TCCException;
import service.TCCService;
import service.TCCStatusService;
import util.Constants;
import util.TCCConstants;

public class TCCManager {

	public static Integer computeSlewTime(double srcRadNS, double srcRadMD) throws TCCException{
		
		TCCStatus tccStatus = new TCCStatusService().getTelescopeStatus();
		Pair<Double, Double> tiltMDNow = tccStatus.getTiltMD();
		Pair<Double,Double> tiltNSNow = tccStatus.getTiltNS();
		
		int rampTime = 10;
		System.err.println("source:"+srcRadNS*Constants.rad2Deg + " "+srcRadMD*Constants.rad2Deg);
		System.err.println("status:" + " " + tiltNSNow.getValue0()*Constants.rad2Deg + " " + tiltNSNow.getValue1()*Constants.rad2Deg + " "
		+ tiltMDNow.getValue0()*Constants.rad2Deg + " "+tiltMDNow.getValue1()*Constants.rad2Deg );
		Pair<Double,Double> slewTimeNS = new Pair<Double, Double>(Math.abs((Double)tiltNSNow.getValue(0) - srcRadNS)*Constants.rad2Deg/TCCConstants.slewRateNSFast
				, Math.abs((Double)tiltNSNow.getValue(1) - srcRadNS)*Constants.rad2Deg/TCCConstants.slewRateNSFast);
		
		Pair<Double,Double> slewTimeMD = new Pair<Double, Double>(Math.abs((Double)tiltMDNow.getValue(0) - srcRadMD)*Constants.rad2Deg/TCCConstants.slewRateMD
				, Math.abs((Double)tiltMDNow.getValue(1) - srcRadMD)*Constants.rad2Deg/TCCConstants.slewRateMD);
		
		System.err.println("slew time:" + slewTimeNS.getValue0() + " "+slewTimeNS.getValue1()+ " "+slewTimeMD.getValue0()+ " "+slewTimeMD.getValue1());
		
		Double maxSlewTime = Collections.max(Arrays.asList(slewTimeNS.getValue0(),slewTimeNS.getValue1(),slewTimeMD.getValue0(),slewTimeMD.getValue1()));
		return (int)(maxSlewTime + 2*rampTime);
				
	}
}
