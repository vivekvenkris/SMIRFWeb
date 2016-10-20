package bean;

import util.Angle;

public class TBSource {

	Angle angleRA;
	Angle angleDEC;
	double periodSecs;
	double DM;
	double acceleration;
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
	public double getPeriodSecs() {
		return periodSecs;
	}
	public void setPeriodSecs(double periodSecs) {
		this.periodSecs = periodSecs;
	}
	public double getDM() {
		return DM;
	}
	public void setDM(double dM) {
		DM = dM;
	}
	public double getAcceleration() {
		return acceleration;
	}
	public void setAcceleration(double acceleration) {
		this.acceleration = acceleration;
	}
	
	
	
}
