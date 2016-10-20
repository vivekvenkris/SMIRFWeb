package bean;


import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.javatuples.Pair;

import exceptions.DriveBrokenException;
import util.Angle;
import util.TCCConstants;
@XmlRootElement(name="tcc_status")
public class TCCStatus {
	
	String overview;
	SourceCoordinates coordinates;
	Drive ns;
	Drive md;
	Pair<Double, Double> tiltMD;
	Pair<Double, Double> tiltNS;
	Pair<Boolean,Boolean> speedNS;
	
	
	public TCCStatus() {
		coordinates = new SourceCoordinates();
		ns = new Drive(TCCConstants.NS);
		md = new Drive(TCCConstants.MD);
	}
	
	public Boolean isTelescopeDriving() throws DriveBrokenException{
		boolean isEastNSDriving = this.ns.getEast().getDriving();
		
		if(!isEastNSDriving){
			if(!this.ns.getEast().getDisabled() && !isEastNSTracking())
				throw new DriveBrokenException("East arm NS was not driving, disabled or on target", ExceptionUtils.getStackTrace(new Exception()),this);
		}
		
		boolean isWestNSDriving =  this.ns.getWest().getDriving();
		
		if(!isWestNSDriving){
			if(!this.ns.getWest().getDisabled() && !isWestNSTracking())
				throw new DriveBrokenException("West arm NS was not driving, disabled or on target", ExceptionUtils.getStackTrace(new Exception()),this);
		}
		
		
		
		boolean isEastMDDriving = this.md.getEast().getDriving();
		
		if(!isEastMDDriving){
			if(!this.md.getEast().getDisabled() && !isEastMDTracking())
				throw new DriveBrokenException("East arm MD was not driving, disabled or on target", ExceptionUtils.getStackTrace(new Exception()),this);
		}
		
		boolean isWestMDDriving =  this.md.getWest().getDriving();
		
		if(!isWestMDDriving){
			if(!this.md.getWest().getDisabled() && !isWestMDTracking())
				throw new DriveBrokenException("West arm MD was not driving, disabled or on target", ExceptionUtils.getStackTrace(new Exception()),this);
		}
				return isEastMDDriving || isWestMDDriving || isEastMDDriving || isWestMDDriving;
	}
	
	public boolean isEastNSTracking(){
		double sourceRadNS = this.coordinates.getNs().getRadianValue();		
		double eastRadNS = this.ns.getEast().getTilt().getRadianValue();
		
		return (Math.abs(eastRadNS - sourceRadNS) < TCCConstants.OnSourceThresholdRadNS);
		
	}
	
	public boolean isWestNSTracking(){
		double sourceRadNS = this.coordinates.getNs().getRadianValue();
		
		double westRadNS = this.ns.getWest().getTilt().getRadianValue();
		return (Math.abs(westRadNS - sourceRadNS) < TCCConstants.OnSourceThresholdRadNS);
		
	}
	

	public boolean isEastMDTracking(){
		double sourceRadMD = this.coordinates.getEw().getRadianValue();		
		double eastRadMD = this.md.getEast().getTilt().getRadianValue();
		
		return (Math.abs(eastRadMD - sourceRadMD) < TCCConstants.OnSourceThresholdRadMD);
		
	}
	
	public boolean isWestMDTracking(){
		double sourceRadMD = this.coordinates.getEw().getRadianValue();
		
		double westRadMD = this.md.getWest().getTilt().getRadianValue();
		return (Math.abs(westRadMD - sourceRadMD) < TCCConstants.OnSourceThresholdRadMD);
		
	}
	
	
	
	
	public Boolean isTelescopeTracking() throws DriveBrokenException{
		
		
		double sourceRadNS = this.coordinates.getNs().getRadianValue();
		double sourceRadMD = this.coordinates.getEw().getRadianValue();
		
		double eastRadNS = this.ns.getEast().getTilt().getRadianValue();
		if(Math.abs(eastRadNS - sourceRadNS) > TCCConstants.OnSourceThresholdRadNS)
			new DriveBrokenException("East NS seems to have been broken while tracking");
		
		double westRadNS = this.ns.getWest().getTilt().getRadianValue();
		if(Math.abs(westRadNS - sourceRadNS) > TCCConstants.OnSourceThresholdRadNS)
			new DriveBrokenException("West NS seems to have been broken while tracking");
		
		double eastRadMD = this.md.getEast().getTilt().getRadianValue();
		if(Math.abs(eastRadMD - sourceRadMD) > TCCConstants.OnSourceThresholdRadMD)
			new DriveBrokenException("East MD seems to have been broken while tracking");
		
		double westRadMD = this.md.getWest().getTilt().getRadianValue();
		if(Math.abs(westRadMD - sourceRadMD) > TCCConstants.OnSourceThresholdRadMD)
			new DriveBrokenException("West MD seems to have been broken while tracking");
		return true;

	}

	
	
	
	public static class Arm{
		String name;
		Angle tilt;
		Integer count;
		Boolean driving;
		Boolean disabled;
		Boolean onTarget;
		Boolean fast;
		Integer status;
		public Arm(String name) {
			this.name = name;
			disabled = onTarget = driving = false;
			fast = true;
		}
		
		public Boolean getOnTarget() {
			return onTarget;
		}

		public void setOnTarget(Boolean onTarget) {
			this.onTarget = onTarget;
		}

		public Integer getCount() {
			return count;
		}
		public void setCount(Integer count) {
			this.count = count;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Angle getTilt() {
			return tilt;
		}
		public void setTilt(Angle tilt) {
			this.tilt = tilt;
		}
		public Boolean getDriving() {
			return driving;
		}
		public void setDriving(Boolean driving) {
			this.driving = driving;
		}
		public Boolean getDisabled() {
			return disabled;
		}
		public void setDisabled(Boolean disabled) {
			this.disabled = disabled;
		}
		
		public Boolean getFast() {
			return fast;
		}
		public void setFast(Boolean fast) {
			this.fast = fast;
		}
		public Integer getStatus() {
			return status;
		}
		public void setStatus(Integer status) {
			this.status = status;
		}
		
		
		
	}

	public static class Drive {
		String error;
		String name;
		Arm east;
		Arm west;
		public Drive(String name) {
			this.name = name;
			east = new Arm(TCCConstants.EAST);
			west = new Arm(TCCConstants.WEST);
		}
		public String getError() {
			return error;
		}
		public void setError(String error) {
			this.error = error;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Arm getEast() {
			return east;
		}
		public void setEast(Arm east) {
			this.east = east;
		}
		public Arm getWest() {
			return west;
		}
		public void setWest(Arm west) {
			this.west = west;
		}
		
		
	}
	
	public static class SourceCoordinates{
		Angle ra;
		Angle dec;
		Angle ha;
		Angle gLat;
		Angle gLon;
		Angle ns;
		Angle ew;
		Angle lmst;
		
		public SourceCoordinates() {
			ra = new Angle(Angle.HHMMSS);
			ha = new Angle(Angle.HHMMSS);
			lmst = new Angle(Angle.HHMMSS);
			
			dec = new Angle(Angle.DDMMSS);
			
			gLat = new Angle(Angle.DEG);
			gLon = new Angle(Angle.DEG);
			
			ns = new Angle(Angle.DEG);
			ew = new Angle(Angle.DEG);
			
		}

		public Angle getRa() {
			return ra;
		}

		public void setRa(Angle ra) {
			this.ra = ra;
		}

		public Angle getDec() {
			return dec;
		}

		public void setDec(Angle dec) {
			this.dec = dec;
		}

		public Angle getHa() {
			return ha;
		}

		public void setHa(Angle ha) {
			this.ha = ha;
		}

		public Angle getgLat() {
			return gLat;
		}

		public void setgLat(Angle gLat) {
			this.gLat = gLat;
		}

		public Angle getgLon() {
			return gLon;
		}

		public void setgLon(Angle gLon) {
			this.gLon = gLon;
		}

		public Angle getNs() {
			return ns;
		}

		public void setNs(Angle ns) {
			this.ns = ns;
		}

		public Angle getEw() {
			return ew;
		}

		public void setEw(Angle ew) {
			this.ew = ew;
		}

		public Angle getLmst() {
			return lmst;
		}

		public void setLmst(Angle lmst) {
			this.lmst = lmst;
		}
		
	}

	public String getOverview() {
		return overview;
	}

	public void setOverview(String overview) {
		this.overview = overview;
	}

	public SourceCoordinates getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(SourceCoordinates coordinates) {
		this.coordinates = coordinates;
	}

	public Drive getNs() {
		return ns;
	}

	public void setNs(Drive ns) {
		this.ns = ns;
	}

	public Drive getMd() {
		return md;
	}

	public void setMd(Drive md) {
		this.md = md;
	}

	public Pair<Double, Double> getTiltMD() {
		return tiltMD;
	}

	public void setTiltMD(Pair<Double, Double> tiltMD) {
		this.tiltMD = tiltMD;
	}

	public Pair<Double, Double> getTiltNS() {
		return tiltNS;
	}

	public void setTiltNS(Pair<Double, Double> tiltNS) {
		this.tiltNS = tiltNS;
	}

	public Pair<Boolean, Boolean> getSpeedNS() {
		return speedNS;
	}

	public void setSpeedNS(Pair<Boolean, Boolean> speedNS) {
		this.speedNS = speedNS;
	}
	
	

	
}





