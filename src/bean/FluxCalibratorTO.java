package bean;

public class FluxCalibratorTO {

	private Integer sourceID;
	private String sourceName;
	private Angle angleRA;
	private Angle angleDEC;
	private Integer dm;
	private Integer fiveMinSNR;
	
	public FluxCalibratorTO() {	}
	public FluxCalibratorTO(FluxCalibrator calibrator){
		this.sourceID = calibrator.getSourceID();
		this.sourceName = calibrator.getSourceName();
		this.angleRA = calibrator.getAngleRA();
		this.angleDEC = calibrator.getAngleDEC();
		this.dm = calibrator.getDm();
		this.fiveMinSNR = calibrator.getFiveMinSNR();
	}
	@Override
	public String toString() {
		return this.sourceName;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof FluxCalibratorTO && ((FluxCalibratorTO) obj).sourceName.equals(sourceName) ) return true;
		return false;
	}
	
	public Integer getSourceID() {
		return sourceID;
	}
	public void setSourceID(Integer sourceID) {
		this.sourceID = sourceID;
	}
	public String getSourceName() {
		return sourceName;
	}
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	public Angle getAngleRA() {
		return angleRA;
	}
	public void setAngleRA(Angle angleRA) {
		this.angleRA = angleRA;
	}
	public Angle getAngleDEC() {
		return angleDEC;
	}
	public void setAngleDEC(Angle angleDEC) {
		this.angleDEC = angleDEC;
	}
	public Integer getDm() {
		return dm;
	}
	public void setDm(Integer dm) {
		this.dm = dm;
	}
	public Integer getFiveMinSNR() {
		return fiveMinSNR;
	}
	public void setFiveMinSNR(Integer fiveMinSNR) {
		this.fiveMinSNR = fiveMinSNR;
	}
	
	
}
