package service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.JulianFields;

import org.jastronomy.jsofa.JSOFA;

import bean.Angle;
import bean.CoordinateTO;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import manager.MolongloCoordinateTransforms;
import util.BackendConstants;
import util.Constants;

public class EphemService {

	public static  LocalDateTime getUTCTimestamp(){
		return  LocalDateTime.now(Clock.systemUTC());
	}
	
	public static String getUtcStringNow(){
		Instant instant = Instant.now();
		return instant.toString().replaceAll("T", "-").replaceAll("Z", "");
	}
	
	public static  double getMJDNow(){
		return getMJDAfterOffset(0);
	}
	public static double getMJDAfterOffset(int offsetSecsFromNow){
		LocalDateTime dateTime =   LocalDateTime.now(Clock.systemUTC());
		dateTime = dateTime.plusSeconds(offsetSecsFromNow);
		return dateTime.getLong(JulianFields.MODIFIED_JULIAN_DAY) +(dateTime.getHour()*3600+dateTime.getMinute()*60+dateTime.getSecond())/86400.0;

	}
	
	public static double getMJDForUTC(String utcStr){
		LocalDateTime utc = LocalDateTime.parse(utcStr, DateTimeFormatter.ofPattern(BackendConstants.backendUTCFormatOfPattern));
		double mjd = utc.getLong(JulianFields.MODIFIED_JULIAN_DAY) +(utc.getHour()*3600+utc.getMinute()*60+utc.getSecond())/86400.0;
		return mjd;
	}
	
	public static double getMJDForUTC(String utcStr, int offsetSecondsFromUTC){
		LocalDateTime utc = LocalDateTime.parse(utcStr, DateTimeFormatter.ofPattern(BackendConstants.backendUTCFormatOfPattern));
		utc = utc.plusSeconds(offsetSecondsFromUTC);
		double mjd = utc.getLong(JulianFields.MODIFIED_JULIAN_DAY) +(utc.getHour()*3600+utc.getMinute()*60+utc.getSecond())/86400.0;
		return mjd;
	}
	
	
	private static double getGAST (double mjd){	// convert MJD in UT1 to a LAST

		final int num_leap_seconds = 26;
		final double   tt_offset = 32.184;

		double dt = (double) num_leap_seconds + tt_offset;
		double jd_base = 2400000.5;

		JSOFA.JulianDate terrestrialTime = JSOFA.jauUt1tt (jd_base, mjd, dt);
		if (terrestrialTime == null)
		{
			System.err.println("ERROR in UT1 to TT conversion\n");
			return 0;
		}

		double gast = JSOFA.jauGst06a (jd_base, mjd, terrestrialTime.djm0, terrestrialTime.djm1);
		return gast;
	}


	public static double getRadLMSTForMolongloNow(){
		return getRadLMSTForMolonglo (getMJDNow());
	}

	
	public static Angle getAngleLMSTForMolongloNow(){
		return new Angle(getRadLMSTForMolongloNow(),Angle.HHMMSS);
	}
	
	public static double getRadLMSTForMolonglo (double mjd)
	{
		double gast, last;

		gast = getGAST (mjd);
		last  = gast + Constants.MolongloLongitudeRAD;

		final double two_pi = 2.0 * Math.PI;
		double w = last % two_pi;
		return ( w >= 0.0 ) ? w : w + two_pi;

	}
	
	public static double getRadLMSTforMolonglo(String utcStr){
		if(!utcStr.contains(".")) utcStr += ".000";
		LocalDateTime utc = LocalDateTime.parse(utcStr, DateTimeFormatter.ofPattern(BackendConstants.backendUTCFormatOfPattern));
		double mjd = utc.getLong(JulianFields.MODIFIED_JULIAN_DAY) +(utc.getHour()*3600+utc.getMinute()*60+utc.getSecond())/86400.0;
		double last = EphemService.getRadLMSTForMolonglo(mjd);
		return last;
	}
	
	public static double getRadLMSTforMolonglo(LocalDateTime utc){
		double mjd = utc.getLong(JulianFields.MODIFIED_JULIAN_DAY) +(utc.getHour()*3600+utc.getMinute()*60+utc.getSecond())/86400.0;
		double last = EphemService.getRadLMSTForMolonglo(mjd);
		return last;
	}
	
	public static Angle getHA(Angle lst, Angle ra){
		return new Angle(lst.getRadianValue() - ra.getRadianValue(), Angle.HHMMSS);
	}
	
	public static Angle getRA(Angle lst, Angle ha){
		return new Angle(lst.getRadianValue() - ha.getRadianValue(),Angle.HHMMSS);
	}
	
	

	public static void main(String[] args) throws EmptyCoordinatesException, CoordinateOverrideException {
		LocalDateTime dateTime = LocalDateTime.now(Clock.systemUTC());
		double mjd = dateTime.getLong(JulianFields.MODIFIED_JULIAN_DAY) +(dateTime.getHour()*3600+dateTime.getMinute()*60+dateTime.getSecond())/86400.0;
		System.err.println("mopsr_getlst "+ dateTime.toString().replace("T", "-"));
		System.err.println(mjd);
		double last = EphemService.getRadLMSTForMolonglo(mjd);

		int HMSF[] = new int[4];
		int NDP = 4;    // number of decimal places

		JSOFA.jauA2tf (NDP, last, HMSF);
		System.err.format("LAST: %02d:%02d:%02d.%d [radians=%f]\n", HMSF[0],HMSF[1],HMSF[2],HMSF[3], last);

		Angle RA = new Angle("04:37:00", "hhmmss");
		Angle DEC = new Angle("-47:15:00","ddmmss");

		CoordinateTO coords = new CoordinateTO();
		coords.setRadHA(RA.getRadianValue() - last);
		coords.setRadDec(DEC.getRadianValue());
		MolongloCoordinateTransforms.skyToTel(coords);		
		System.err.println(coords.getRadHA()*Constants.rad2Deg + " " + coords.getRadDec()*Constants.rad2Deg + " "+ coords.getRadNS()*Constants.rad2Deg + " " + coords.getRadMD()*Constants.rad2Deg);


	}

}
