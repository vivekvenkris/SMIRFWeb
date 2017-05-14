package bean;

import manager.PSRCATManager;
import util.SMIRFConstants;

public class TBSourceTO {
	
	public static class DSPSRParameters{
		Double periodSecs;
		Double DM;
		Double acceleration;
		public DSPSRParameters(){
			
		}
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
	String ephemerides;
	Integer priority;
	
	@Override
	public boolean equals(Object obj) {
		if( obj instanceof TBSourceTO) return ((TBSourceTO)obj).psrName.equals(psrName);
		return false;
	}
	
	
	@Override
	public String toString() {
		return psrName;
	}
	
	
	public TBSourceTO() {
		projectID = SMIRFConstants.PID;
		priority = 1;

	}
	public TBSourceTO(String jname){
		this();
		psrName = jname;
		priority = 1;
	}
	
	public void addToEphemerides( String str){
		this.ephemerides+=(str+"\n");
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

	public String getEphemerides() {
		return ephemerides;
	}

	public void setEphemerides(String ephemerides) {
		this.ephemerides = ephemerides;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	
	
	
}
