package bean;



public class PointingTO {
	
	
	private Integer pointingID;
	private String pointingName;
	private Angle angleLAT;
	private Angle angleLON;
	private Angle angleRA;	
	private Angle angleDEC;
	private Integer priority;
	private String type;
	private Integer numObs = 0;

	
	
	public PointingTO(Pointing pointing) {
		this.pointingID = pointing.getPointingID();
		this.pointingName = pointing.getPointingName();
		this.angleLAT = pointing.getAngleLAT();
		this.angleLON = pointing.getAngleLON();
		this.angleRA = pointing.getAngleRA();
		this.angleDEC = pointing.getAngleDEC();
		this.priority = pointing.getPriority();
		this.type = pointing.getType();

	}
	
	
	public Integer getPointingID() {
		return pointingID;
	}
	public void setPointingID(Integer pointingID) {
		this.pointingID = pointingID;
	}
	public String getPointingName() {
		return pointingName;
	}
	public void setPointingName(String pointingName) {
		this.pointingName = pointingName;
	}
	public Angle getAngleLAT() {
		return angleLAT;
	}
	public void setAngleLAT(Angle angleLAT) {
		this.angleLAT = angleLAT;
	}
	public Angle getAngleLON() {
		return angleLON;
	}
	public void setAngleLON(Angle angleLON) {
		this.angleLON = angleLON;
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
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}


	public Integer getNumObs() {
		return numObs;
	}


	public void setNumObs(Integer numObs) {
		this.numObs = numObs;
	}
	

}
