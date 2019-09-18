package service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.JulianFields;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jastronomy.jsofa.JSOFA;
import org.jastronomy.jsofa.JSOFA.JulianDate;
import org.jastronomy.jsofa.JSOFA.SphericalCoordinateEO;
import org.jastronomy.jsofa.JSOFAIllegalParameter;
import org.jastronomy.jsofa.JSOFAInternalError;

import bean.Angle;
import bean.CoordinateTO;
import bean.PointingTO;
import bean.TBSourceTO;
import control.Control;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.EphemException;
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
		if(!utcStr.contains(".")) utcStr +=  ".000";
		LocalDateTime utc = LocalDateTime.parse(utcStr, DateTimeFormatter.ofPattern(BackendConstants.backendUTCFormatOfPattern));
		double mjd = utc.getLong(JulianFields.MODIFIED_JULIAN_DAY) +(utc.getHour()*3600+utc.getMinute()*60+utc.getSecond())/86400.0;
		return mjd;
	}

	public static double getMJDForUTC(String utcStr, int offsetSecondsFromUTC){
		if(!utcStr.contains(".")) utcStr +=  ".000";
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
		
		if(Control.getEmulateForUTC() != null) {
			
			System.err.println("emulating for UTC" +Control.getEmulateForUTC() + " " + new Angle(getRadLMSTforMolonglo(Control.getEmulateForUTC()),Angle.HHMMSS) );
			
			return new Angle(getRadLMSTforMolonglo(Control.getEmulateForUTC()),Angle.HHMMSS);
		}
		
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
		
		double ha = lst.getRadianValue() - ra.getRadianValue();
		double turn = 24 * Constants.hrs2Deg * Constants.deg2Rad;
		
		if (Math.abs(ha) > 12 * Constants.hrs2Deg * Constants.deg2Rad) {
			ha = (ha > 0)? ha -turn : ha + turn;	
		}
		
//		if (ha < -12 * Constants.hrs2Deg * Constants.deg2Rad) {
//			ha += turn;	
//		}
		return new Angle(ha, Angle.HHMMSS);
	}
	
	public static Angle getHA(Angle ra){
		return getHA(getAngleLMSTForMolongloNow(), ra);
	}

	

	public static Angle getRA(Angle lst, Angle ha){
		return new Angle(lst.getRadianValue() - ha.getRadianValue(),Angle.HHMMSS);
	}
	
	
	public static SphericalCoordinateEO precessToUTC( Angle ra, Angle dec, LocalDateTime utc) throws EphemException{
		
		double pr = 0;  // atan2 ( -354.45e-3 * DAS2R, cos(dc) );
		double pd = 0;  // 595.35e-3 * DAS2R;

		// Parallax (arcsec) and recession speed (km/s).
		double px = 0;  // 164.99e-3;
		double rv = 0;  // 0.0;
		
		try {
			
			JulianDate utcDate = JSOFA.jauDtf2d("UTC", utc.getYear(), utc.getMonthValue(), utc.getDayOfMonth(), 
					utc.getHour(),utc.getMinute(), utc.getSecond() + utc.getNano()/1e9);
			
			JulianDate taiDate = JSOFA.jauUtctai(utcDate.djm0, utcDate.djm1);
			JulianDate ttDate = JSOFA.jauTaitt(taiDate.djm0, taiDate.djm1);
						
			SphericalCoordinateEO coord = JSOFA.jauAtci13(ra.getRadianValue(), dec.getRadianValue(),
					pr, pd, px, rv, ttDate.djm0, ttDate.djm1);
			
			return coord;
		
		} catch (JSOFAIllegalParameter | JSOFAInternalError e) {
			throw new EphemException(e.getMessage(),ExceptionUtils.getStackTrace(e));
		}

		
	}

	public static PointingTO precessPointingToNow(PointingTO pointingTO) throws EphemException{
		SphericalCoordinateEO coord =  precessToUTC(pointingTO.getAngleRA(), pointingTO.getAngleDEC(), getUTCTimestamp());
		
		pointingTO.setAngleRA( new Angle(coord.pos.alpha, Angle.HHMMSS));
		pointingTO.setAngleDEC( new Angle(coord.pos.delta, Angle.HHMMSS));
		
		pointingTO.setPrecessed(true);
		
		return pointingTO;
		
	}
	
	public static TBSourceTO precessTBSourceToNow(TBSourceTO tbSourceTO) throws EphemException{
		SphericalCoordinateEO coord =  precessToUTC(tbSourceTO.getAngleRA(), tbSourceTO.getAngleDEC(), getUTCTimestamp());
		
		TBSourceTO outTO = new TBSourceTO(tbSourceTO);
		
		outTO.setAngleRA( new Angle(coord.pos.alpha, Angle.HHMMSS));
		outTO.setAngleDEC( new Angle(coord.pos.delta, Angle.DDMMSS));
		
		outTO.setPrecessed(true);
		
//		System.err.println("Precessed " + tbSourceTO.getAngleRA() + " -> " + outTO.getAngleRA() + " and " +
//				tbSourceTO.getAngleDEC() +  " -> " + outTO.getAngleDEC());
		
		return outTO;
		
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
