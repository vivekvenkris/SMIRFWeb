package util;

public class Angle {
	public static final String HHMMSS = "hhmmss";
	public static final String DDMMSS = "ddmmss";
	public static final String RAD = "rad";
	public static final String DEG = "deg";
	Double radValue; // always in radians
	String toStringUnits;
	
	
	public Angle(Double radValue, String toStringUnits){
		this.radValue = radValue;
		this.toStringUnits = toStringUnits;
	}
	public Angle( String toStringUnits){
		this.toStringUnits = toStringUnits;
	}
	
	public Angle(String value, String format){
		this.toStringUnits = format;
		switch(format){
		case HHMMSS:
			fromHHMMSS(value);
			break;
		case DDMMSS:
			fromDDMMSS(value);
			break;
		case DEG:
			radValue = Double.parseDouble(value)*Constants.deg2Rad;
			break;
		case RAD:
			radValue = Double.parseDouble(value);
			break;
		}
		
	}
	
	public Angle(String value, String format, String toStringUnits){
		this.toStringUnits = toStringUnits;
		switch(format){
		case HHMMSS:
			fromHHMMSS(value);
			break;
		case DDMMSS:
			fromDDMMSS(value);
			break;
		case DEG:
			radValue = Double.parseDouble(value)*Constants.deg2Rad;
			break;
		case RAD:
			radValue = Double.parseDouble(value);
			break;
		}
		
	}
	
	public Double getDegreeValue(){
		return radValue*Constants.rad2Deg;
	}
	
	public Double getRadianValue(){
		return radValue;
	}
	
	public Double getDecimalHourValue(){
		return getDegreeValue()/15.0;
	}
	
	public void fromHHMMSS(String hhmmss){
		String[] hms = hhmmss.split(":");
		radValue = (Integer.parseInt(hms[0]) + Integer.parseInt(hms[1])/60.0 + Double.parseDouble(hms[2])/3600.0)*15.0 *Constants.deg2Rad;
	}
	
	public void fromDDMMSS(String ddmmss){
		String[] dms = ddmmss.split(":");
		int sign = (Integer.parseInt(dms[0])>0)? 1:-1;
		radValue = (Integer.parseInt(dms[0]) + sign*Integer.parseInt(dms[1])/60.0 + sign*Double.parseDouble(dms[2])/3600.0)*Constants.deg2Rad;
	}
	public String toHHMMSS(){
		double DegVal = Math.abs(getDegreeValue());
		int hours = (int)(DegVal/15.0);
		int minutes = (int)((DegVal - hours*15.0)*60/15.0);
		double seconds = (DegVal - (hours + minutes/60.0)*15.0)*3600/15.0;
		String hhmmss = String.format("%02d:%02d:%4.2f", hours,minutes,seconds);
		return hhmmss;
	}
	
	public static String toHHMMSS(double radValue){
		double DegVal = Math.abs(radValue*Constants.rad2Deg);
		int hours = (int)(DegVal/15.0);
		int minutes = (int)((DegVal - hours*15.0)*60/15.0);
		double seconds = (DegVal - (hours + minutes/60.0)*15.0)*3600/15.0;
		String hhmmss = String.format("%02d:%02d:%4.2f", hours,minutes,seconds);
		return hhmmss;
	}
	
	
	public String toDDMMSS(){
		double DegVal = Math.abs(getDegreeValue());
		char sign = '-';
		if(radValue > 0) sign = '+'; 
		int degrees = (int)(DegVal);
		int minutes = (int)((DegVal - degrees)*60);
		double seconds = ((DegVal - (degrees + minutes/60.0))*3600.0);
		String ddmmss = String.format("%c%02d:%02d:%4.2f",sign,degrees,minutes,seconds);
		return ddmmss;
	}
	
	public static String toDDMMSS(double radValue){
		double DegVal = Math.abs(radValue*Constants.rad2Deg);
		char sign = '-';
		if(radValue > 0) sign = '+'; 
		int degrees = (int)(DegVal);
		int minutes = (int)((DegVal - degrees)*60);
		double seconds = ((DegVal - (degrees + minutes/60.0))*3600.0);
		String ddmmss = String.format("%c%02d:%02d:%4.2f",sign,degrees,minutes,seconds);
		return ddmmss;
	}
	
	@Override
	public String toString() {
		
		switch(toStringUnits){
		case HHMMSS:
			return toHHMMSS();
		case DDMMSS:
			return toDDMMSS();
		case DEG:
			return getDegreeValue().toString();
		case RAD:
			return radValue.toString();
		}
		return null;
				
	}
	

}
