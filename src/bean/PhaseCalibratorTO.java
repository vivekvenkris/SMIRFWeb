package bean;

public class PhaseCalibratorTO  {
	
	private Integer sourceID;
	private String sourceName;
	private Angle angleRA;
	private Angle angleDEC;
	private Double fluxJY;
	
	public PhaseCalibratorTO(){}
	
	public PhaseCalibratorTO(PhaseCalibrator calibrator){
		this.sourceID = calibrator.getSourceID();
		this.sourceName = calibrator.getSourceName();
		this.angleRA = calibrator.getAngleRA();
		this.angleDEC = calibrator.getAngleDEC();
		this.fluxJY = calibrator.getFluxJY();
	}
	
	@Override
	public String toString() {
		return this.sourceName;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PhaseCalibratorTO && ((PhaseCalibratorTO) obj).sourceName.equals(sourceName) ) return true;
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

	public Double getFluxJY() {
		return fluxJY;
	}

	public void setFluxJY(Double fluxJY) {
		this.fluxJY = fluxJY;
	}
	
	
}
