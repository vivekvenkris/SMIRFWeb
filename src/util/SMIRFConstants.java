package util;

public interface SMIRFConstants {
	
	boolean simulate =  Utilities.simulate();
	
	double minRadMD = (simulate? -100.0: -100) * Constants.deg2Rad;
	double maxRadMD = (simulate? +100.0: 100) * Constants.deg2Rad;
	
	double minRadHA = (simulate? -10.0: -10 ) * Constants.hrs2Rad;
	double maxRadHA = (simulate? +10.0: +10 ) * Constants.hrs2Rad;
	
	double minRadNS = -54.0 * Constants.deg2Rad;
	double maxRadNS = +54.0 * Constants.deg2Rad;
	
	double minGalacticLongitude = -115 * Constants.deg2Rad;
	double maxGalacticLongitude = +35 * Constants.deg2Rad;
	
	double minGalacticLatitude = -4 * Constants.deg2Rad;
	double maxGalacticLatitude = +4 * Constants.deg2Rad;
	
	double tilingDiameter = 2.0 * Constants.deg2Rad;
	
	Integer numBeamsPerServer = 44;
	
	Integer BF08 = 8;
	Integer thresholdPercent=10;
	
	String PID = "P999";
	Integer tobs = simulate? 30: 900;
	Integer phaseCalibrationTobs = simulate? 30 : 20* 60;
	Integer fluxCalibrationTobs = simulate? 30 : 900;
	
	String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	String DB_URL = "jdbc:mysql://localhost/mpsr_ksp_survey";
	
	String USER = simulate? "root":"vivek";
	String PASS = simulate? "tcsmysqlpwd": "4&.S1kz5";
//	String USER = "vivek";
//	String PASS = "4&.S1kz5";
	
	Integer highestPriority=1;
	
	String phaseCalibratorSymbol = "P";
	String fluxCalibratorSymbol = "F";
	String smcPointingSymbol = "S";
	String lmcPointingSymbol = "L";
	String galacticPointingSymbol = "G";
	String candidatePointingSymbol="C";
	String randomPointingSymbol = "R";
	
	String SMIRFPointingPrefix = "SMIRF_";
	String fluxCalPointingPrefix = "J";
	String phaseCalPointingPrefix = "CJ";
	
	

}
