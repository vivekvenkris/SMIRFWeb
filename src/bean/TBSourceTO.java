package bean;

import util.SMIRFConstants;

public class TBSourceTO {
	
	public static class DSPSRParameters{
		Double periodSecs;
		Double DM;
		Double acceleration;
		public DSPSRParameters(Double dm, Double periodSecs, Double acceleration) {
			this.periodSecs = periodSecs;
			this.DM = dm;
			this.acceleration = acceleration;
		}
		public Double getPeriodSecs() {
			return periodSecs;
		}
		public void setPeriodSecs(Double periodSecs) {
			this.periodSecs = periodSecs;
		}
		public Double getDM() {
			return DM;
		}
		public void setDM(Double dM) {
			DM = dM;
		}
		public Double getAcceleration() {
			return acceleration;
		}
		public void setAcceleration(Double acceleration) {
			this.acceleration = acceleration;
		}
		
		
	}
	
	Angle angleRA;
	Angle angleDEC;
	DSPSRParameters dspsrParams;
	String psrName;
	String projectID;
	
	public TBSourceTO() {
		projectID = SMIRFConstants.PID;
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
	public DSPSRParameters getDspsrParams() {
		return dspsrParams;
	}
	public void setDspsrParams(DSPSRParameters dspsrParams) {
		this.dspsrParams = dspsrParams;
	}
	public String getProjectID() {
		return projectID;
	}
	public void setProjectID(String projectID) {
		this.projectID = projectID;
	}
	public String getPsrName() {
		return psrName;
	}
	public void setPsrName(String psrName) {
		this.psrName = psrName;
	}
	
	
	
}
