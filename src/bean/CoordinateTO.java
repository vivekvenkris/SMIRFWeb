package bean;

public class CoordinateTO {

	private Double radHA;
	private Double radDec;
	private Double radNS;
	private Double radMD;
	
	public CoordinateTO(){}
	public CoordinateTO(Double radHA, Double radDec, Double radNS, Double radMD) {
		this.radHA = radHA;
		this.radDec = radDec;
		this.radNS = radNS;
		this.radMD = radMD;
	}
	
	public CoordinateTO(Angle HA, Angle Dec, Angle NS, Angle MD) {
		if(HA != null ) this.radHA = HA.getRadianValue();
		if(Dec != null) this.radDec = Dec.getRadianValue();
		if(NS != null ) this.radNS = NS.getRadianValue();
		if(MD != null ) this.radMD = MD.getRadianValue();
	}
	public CoordinateTO(ObservationTO observation){
		this.radHA = observation.getHANow().getRadianValue();
		this.radDec = observation.getCoords().getPointingTO().getAngleDEC().getRadianValue();
		this.radMD = this.radNS = null;
	}
	
	public Angle getAngleNS(){
		return new Angle(this.radNS,Angle.DEG);
	}
	public Angle getAngleMD(){
		return new Angle(this.radMD,Angle.DEG);
	}
	public Angle getAngleHA(){
		return new Angle(this.radHA,Angle.HHMMSS);
	}
	public Angle getAngleDEC(){
		return new Angle(this.radDec,Angle.DDMMSS);
	}
	
	public boolean hasNullSky(){
		return ((this.radHA == null) && (this.radDec == null));
	}
	
	public boolean hasNullTel(){
		return ((this.radNS == null) && (this.radMD == null));
	}
	
	public void removeSkyCoords(){
		this.radHA= null;
		this.radDec = null;
	}
	
	public void removeTelCoords(){
		this.radNS = null;
		this.radMD = null;
	}
	
	public Double getRadHA() {
		return radHA;
	}
	public void setRadHA(Double radHA) {
		this.radHA = radHA;
	}
	public Double getRadDec() {
		return radDec;
	}
	public void setRadDec(Double radDec) {
		this.radDec = radDec;
	}
	public Double getRadNS() {
		return radNS;
	}
	public void setRadNS(Double radNS) {
		this.radNS = radNS;
	}
	public Double getRadMD() {
		return radMD;
	}
	public void setRadMD(Double radMD) {
		this.radMD = radMD;
	}
	
	@Override
	public String toString() {
		
		return "HA:" + radHA + " DEC: " + radDec + " NS: " + radNS + " MD: " + radMD;
	}
	
	
	
}
