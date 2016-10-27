package bean;

import java.util.ArrayList;
import java.util.List;

import service.EphemService;
import util.Constants;

public class Observation {
	String name;
	Angle angleRA;
	Angle angleDEC;
	Integer tobs;
	String backendType;
	String obsType;
	String observer;
	String utc;
	List<TBSource> tiedBeamSources;
	Integer nfb;
	Angle fanbeamSpacing;
	
	public Observation() {
		nfb = 352;
		fanbeamSpacing = new Angle("0.01139601", Angle.DEG);
		tiedBeamSources = new ArrayList<TBSource>();
	}
	
	private double computeHAForMJD(double mjd){ 
		double radLMST = EphemService.getRadLMSTforMolonglo(mjd);
		double radRA = this.angleRA.getRadianValue();
		double radHA = radLMST - radRA;
		int sign = (radHA > 0)?1:-1;
		if( Math.abs(radHA) > 12*Constants.hrs2Rad)
			radHA = sign* (Math.abs(radHA) - 24*Constants.hrs2Rad);
		return radHA;
	}
	
	public Angle getHANow(){
		double mjd = EphemService.getMJDNow();
		double radHA =  computeHAForMJD(mjd);
		Angle HA = new Angle(radHA,Angle.RAD);
		return HA;
	}
	
	public Angle getHAAfter(int offsetSecsFromNow){
		double mjd = EphemService.getMJDAfterOffset(offsetSecsFromNow);
		double radHA =  computeHAForMJD(mjd);
		Angle HA = new Angle(radHA,Angle.RAD);
		return HA;
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

	public Integer getTobs() {
		return tobs;
	}

	public void setTobs(Integer tobs) {
		this.tobs = tobs;
	}

	public String getBackendType() {
		return backendType;
	}

	public void setBackendType(String backendType) {
		this.backendType = backendType;
	}

	public String getObsType() {
		return obsType;
	}

	public void setObsType(String obsType) {
		this.obsType = obsType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getObserver() {
		return observer;
	}

	public void setObserver(String observer) {
		this.observer = observer;
	}

	public List<TBSource> getTiedBeamSources() {
		return tiedBeamSources;
	}

	public void setTiedBeamSources(List<TBSource> tiedBeamSources) {
		this.tiedBeamSources = tiedBeamSources;
	}

	public String getUtc() {
		return utc;
	}

	public void setUtc(String utc) {
		this.utc = utc;
	}

	

	public Integer getNfb() {
		return nfb;
	}

	public void setNfb(Integer nfb) {
		this.nfb = nfb;
	}

	public Angle getFanbeamSpacing() {
		return fanbeamSpacing;
	}

	public void setFanbeamSpacing(Angle fanbeamSpacing) {
		this.fanbeamSpacing = fanbeamSpacing;
	}
	

	
}
