package util;

public interface SMIRFConstants {
	
	double minRadMD = -60.0 * Constants.deg2Rad;
	double maxRadMD = 60.0 * Constants.deg2Rad;
	
	double minGalacticLongitude = -120 * Constants.deg2Rad;
	double maxGalacticLongitude = +35 * Constants.deg2Rad;
	
	double minGalacticLatitude = -3.5 * Constants.deg2Rad;
	double maxGalacticLatitude = +3.5 * Constants.deg2Rad;
	
	double tilingDiameter = 2.0 * Constants.deg2Rad;
	
	Integer numBeamsPerServer = 44;
	
	Integer BF08 = 8;
	
	String PID = "P001";
	Integer tobs = 900;

}
