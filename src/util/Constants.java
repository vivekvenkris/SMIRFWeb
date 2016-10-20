package util;

public interface Constants {

	Double deg2Rad = Math.PI/180.0;
	Double rad2Deg = 180.0/Math.PI;
	Double toRadians = deg2Rad;
	Double toDegrees = rad2Deg;
	Double arcSec2Deg = 1/(60.0*60.0);
	Double sec2Hrs = 1/(60.0*60.0);
	Double hrs2Deg = 15.0;
	Double hrs2Rad = hrs2Deg*deg2Rad;
	
	Double rad2Hrs = rad2Deg/hrs2Deg;
	
	Double tsamp = 655.36E-6;
	Double samples2secs  = tsamp;
	
	/* Andrew's numbers*/
//	Double MolongloLatitude = -( 35 + 22/60.0 + 14.5452 / 3600.0)*Deg2Rad;
//	Double MolongloSlope = (1.0 / 289.9);
//	Double MolongloSkew = (4.9 * arcSec2Deg * Deg2Rad);
//	
	/*Pablo's numbers*/
	Double MolongloSkew     = -2.3755870374367263e-5;
	Double MolongloSlope    = -3.448285788259685e-3;
	Double MolongloLatitude = -0.6173567480991081;
	
	Double cosSkew = Math.cos(MolongloSkew);
	Double sinSkew = Math.sin(MolongloSkew);
	
	Double cosSlope = Math.cos(MolongloSlope);
	Double sinSlope = Math.sin(MolongloSlope);
	
	Double cosLat = Math.cos(MolongloLatitude);
	Double sinLat = Math.sin(MolongloLatitude);
	
	Double MolongloLongitude = 149.424658;
	Double MolongloLongitudeRAD = MolongloLongitude*deg2Rad;
	
	
	
	
			
	

}
