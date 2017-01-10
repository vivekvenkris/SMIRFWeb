package util;

public interface SMIRFConstants {
	
	boolean simulate = true;
	
	double minRadMD = -50.0 * Constants.deg2Rad;
	double maxRadMD = +50.0 * Constants.deg2Rad;
	
	double minRadNS = -54.0 * Constants.deg2Rad;
	double maxRadNS = +54.0 * Constants.deg2Rad;
	
	double minRadHA = -06.0 * Constants.hrs2Rad;
	double maxRadHA = +06.0 * Constants.hrs2Rad;
	
	double minGalacticLongitude = -115 * Constants.deg2Rad;
	double maxGalacticLongitude = +35 * Constants.deg2Rad;
	
	double minGalacticLatitude = -4 * Constants.deg2Rad;
	double maxGalacticLatitude = +4 * Constants.deg2Rad;
	
	double tilingDiameter = 2.0 * Constants.deg2Rad;
	
	Integer numBeamsPerServer = 44;
	
	Integer BF08 = 8;
	
	String PID = "P001";
	Integer tobs = 30;
	Integer phaseCalibrationTobs = 20* 60;
	Integer fluxCalibrationTobs = 900;
	
	String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	String DB_URL = "jdbc:mysql://localhost/mpsr_ksp_survey";
	
	String USER = "root";
	String PASS = "tcsmysqlpwd";
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
	


}
