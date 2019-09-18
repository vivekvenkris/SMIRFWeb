package bean;


import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.javatuples.Pair;

import control.Control;
import exceptions.DriveBrokenException;
import exceptions.TCCException;
import service.TCCStatusService;
import util.Constants;
import util.TCCConstants;
import util.Utilities;
@XmlRootElement(name="tcc_status")
public class TCCStatus {

	String status;
	String overview;
	SourceCoordinates coordinates;
	Drive ns;
	Drive md;
	Pair<Double, Double> tiltMD;
	Pair<Double, Double> tiltNS;
	Pair<Boolean,Boolean> speedNS;
	String xml;
	
	public String getNSMDPositionString() {
		
		String status = "NSMD position (all in degrees): \n";
		
		status += "NS_East: " + this.getNs().getEast().getTilt().getDegreeValue() + "\n";
		status += "NS_West: " + this.getNs().getWest().getTilt().getDegreeValue() + "\n";
		status += "MD_East: " + this.getMd().getEast().getTilt().getDegreeValue() + "\n";
		status += "MD_West: " + this.getMd().getWest().getTilt().getDegreeValue() + "\n";
		
		return status;
	}
	
	public Angle getNSPosition(String arm) {
		if(arm.equals(TCCConstants.EAST)) return this.getNs().getEast().getTilt();
		else if(arm.equals(TCCConstants.WEST)) return this.getNs().getWest().getTilt();
		else if(arm.equals(TCCConstants.BOTH_ARMS)) {
			
			return (new Angle(0.5 * (this.getNs().getEast().getTilt().getRadianValue() 
					+ this.getNs().getWest().getTilt().getRadianValue()),Angle.DEG));
		
		}
		return null;
	}

	@Override
	public String toString() {
		
		return this.getOverview();
	}	

	public TCCStatus() {
		coordinates = new SourceCoordinates();
		ns = new Drive(TCCConstants.NS);
		md = new Drive(TCCConstants.MD);
	}

	public TCCStatus(String xmlMessage) throws TCCException{
		this();
		try{
			this.setOverview(Utilities.getTextFromXpath(xmlMessage, "//overview/error_string"));
			this.setXml(xmlMessage);
			TCCStatus.SourceCoordinates coords = this.getCoordinates();
			coords.setRa(new Angle(Utilities.getTextFromXpath(xmlMessage, "//coordinates/RA"),Angle.HHMMSS));
			coords.setDec(new Angle(Utilities.getTextFromXpath(xmlMessage, "//coordinates/Dec"),Angle.DDMMSS));
			coords.setHa(new Angle(Utilities.getTextFromXpath(xmlMessage, "//coordinates/HA"),Angle.HHMMSS));
			coords.setgLat(new Angle(Utilities.getTextFromXpath(xmlMessage, "//coordinates/Glat"),Angle.RAD,Angle.DEG));
			coords.setgLon(new Angle(Utilities.getTextFromXpath(xmlMessage, "//coordinates/Glon"),Angle.RAD,Angle.DEG));
			coords.setNs(new Angle(Utilities.getTextFromXpath(xmlMessage, "//coordinates/NS"),Angle.RAD,Angle.DEG));
			coords.setEw(new Angle(Utilities.getTextFromXpath(xmlMessage, "//coordinates/EW"),Angle.RAD,Angle.DEG));
			coords.setLmst(new Angle(Utilities.getTextFromXpath(xmlMessage, "//coordinates/LMST"),Angle.HHMMSS));

			TCCStatus.Drive ns = this.getNs();		
			TCCStatus.Arm nsEast = ns.getEast();
			TCCStatus.Arm nsWest = ns.getWest();

			ns.setError(Utilities.getTextFromXpath(xmlMessage, "//ns/error"));
			nsEast.setTilt(new Angle(Utilities.getTextFromXpath(xmlMessage, "//ns/east/tilt"),Angle.RAD));
			nsEast.setCount(Integer.parseInt(Utilities.getTextFromXpath(xmlMessage, "//ns/east/count")));
			nsEast.setDriving(Boolean.parseBoolean(Utilities.getTextFromXpath(xmlMessage, "//ns/east/driving")));
			nsEast.setOnTarget(Boolean.parseBoolean(Utilities.getTextFromXpath(xmlMessage, "//ns/east/on_target")));
			nsEast.setStatus(Integer.parseInt(Utilities.getTextFromXpath(xmlMessage, "//ns/east/system_status")));
			nsEast.setDisabled(Utilities.getTextFromXpath(xmlMessage, "//ns/east/state").equalsIgnoreCase("disabled"));
			nsEast.setOffset(new Angle(Utilities.getTextFromXpath(xmlMessage, "//ns/east/offset"),Angle.RAD));
			if(!nsEast.getDisabled()) nsEast.setFast(Utilities.getTextFromXpath(xmlMessage, "//ns/east/state").equals("fast"));

			nsWest.setTilt(new Angle(Utilities.getTextFromXpath(xmlMessage, "//ns/west/tilt"),Angle.RAD));
			nsWest.setCount(Integer.parseInt(Utilities.getTextFromXpath(xmlMessage, "//ns/west/count")));
			nsWest.setDriving(Boolean.parseBoolean(Utilities.getTextFromXpath(xmlMessage, "//ns/west/driving")));
			nsWest.setOnTarget(Boolean.parseBoolean(Utilities.getTextFromXpath(xmlMessage, "//ns/west/on_target")));
			nsWest.setStatus(Integer.parseInt(Utilities.getTextFromXpath(xmlMessage, "//ns/west/system_status")));
			nsWest.setDisabled(Utilities.getTextFromXpath(xmlMessage, "//ns/west/state").equalsIgnoreCase("disabled"));
			nsWest.setOffset(new Angle(Utilities.getTextFromXpath(xmlMessage, "//ns/west/offset"),Angle.RAD));
			if(!nsWest.getDisabled()) nsWest.setFast(Utilities.getTextFromXpath(xmlMessage, "//ns/west/state").equals("fast"));

			TCCStatus.Drive md = this.getMd();
			TCCStatus.Arm mdEast = md.getEast();
			TCCStatus.Arm mdWest = md.getWest();

			md.setError(Utilities.getTextFromXpath(xmlMessage, "//md/error"));
			mdEast.setTilt(new Angle(Utilities.getTextFromXpath(xmlMessage, "//md/east/tilt"),Angle.RAD));
			mdEast.setCount(Integer.parseInt(Utilities.getTextFromXpath(xmlMessage, "//md/east/count")));
			mdEast.setDriving(Boolean.parseBoolean(Utilities.getTextFromXpath(xmlMessage, "//md/east/driving")));
			mdEast.setOnTarget(Boolean.parseBoolean(Utilities.getTextFromXpath(xmlMessage, "//md/east/on_target")));
			mdEast.setStatus(Integer.parseInt(Utilities.getTextFromXpath(xmlMessage, "//md/east/system_status")));
			mdEast.setDisabled(Utilities.getTextFromXpath(xmlMessage, "//md/east/state").equalsIgnoreCase("disabled"));
			mdEast.setOffset(new Angle(Utilities.getTextFromXpath(xmlMessage, "//md/east/offset"),Angle.RAD));
			if(!mdEast.getDisabled()) mdEast.setFast(Utilities.getTextFromXpath(xmlMessage, "//md/east/state").equals("fast"));

			mdWest.setTilt(new Angle(Utilities.getTextFromXpath(xmlMessage, "//md/west/tilt"),Angle.RAD));
			mdWest.setCount(Integer.parseInt(Utilities.getTextFromXpath(xmlMessage, "//md/west/count")));
			mdWest.setDriving(Boolean.parseBoolean(Utilities.getTextFromXpath(xmlMessage, "//md/west/driving")));
			mdWest.setOnTarget(Boolean.parseBoolean(Utilities.getTextFromXpath(xmlMessage, "//md/west/on_target")));
			mdWest.setStatus(Integer.parseInt(Utilities.getTextFromXpath(xmlMessage, "//md/west/system_status")));
			mdWest.setDisabled(Utilities.getTextFromXpath(xmlMessage, "//md/west/state").equalsIgnoreCase("disabled"));
			mdWest.setOffset(new Angle(Utilities.getTextFromXpath(xmlMessage, "//md/west/offset"),Angle.RAD));
			if(!mdWest.getDisabled()) mdWest.setFast(Utilities.getTextFromXpath(xmlMessage, "//md/west/state").equals("fast"));

			this.setTiltMD(new Pair<Double, Double>(mdEast.getTilt().getRadianValue(),mdWest.getTilt().getRadianValue()));
			this.setTiltNS(new Pair<Double, Double>(nsEast.getTilt().getRadianValue(),nsWest.getTilt().getRadianValue()));
			this.setSpeedNS(new Pair<Boolean,Boolean>(nsEast.getFast(),nsWest.getFast()));
			
			
			
			
			if(this.isTelescopeIdle())   this.status = "Idle";

			else if(this.isTelescopeDriving()) this.status =  "Driving";
			
			else if(this.isTelescopeTracking()) this.status =  "Tracking";

			
			else this.status = "Tracking";

		} catch(Exception e){
			e.printStackTrace();
			throw new TCCException("TCC status service failed: Cause:"+ e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}

	public Boolean isTelescopeIdle(){
		
		Boolean eastNSIdle = this.ns.getEast().getDriving() && this.ns.getEast().getDisabled();
		Boolean westNSIdle = this.ns.getWest().getDriving() && this.ns.getWest().getDisabled();
		Boolean eastMDIdle = this.md.getEast().getDriving() && this.md.getEast().getDisabled();
		Boolean westMDIdle = this.md.getWest().getDriving() && this.md.getWest().getDisabled();
		
		return !(eastNSIdle || westNSIdle || eastMDIdle || westMDIdle);
		
		//return !(this.ns.getEast().getDriving() || this.ns.getWest().getDriving() || this.md.getEast().getDriving() || this.md.getWest().getDriving());
	}

	public Boolean isTelescopeDriving() throws DriveBrokenException, InterruptedException{
		
		DriveBrokenException nsEast = null, nsWest = null, mdEast = null, mdWest = null; 

		boolean isEastNSDriving = this.ns.getEast().getDriving();
		
		

		if(!isEastNSDriving){
			if(!this.ns.getEast().getDisabled() && !isEastNSTracking()) {

				if(!this.ns.getEast().getStatus().equals(240)) nsEast =  new DriveBrokenException(
						"East arm NS was not driving, disabled or on target ", 
						ExceptionUtils.getStackTrace(new Exception()),this, TCCConstants.EAST, TCCConstants.NS);
			}
		}

		boolean isWestNSDriving =  this.ns.getWest().getDriving();

		if(!isWestNSDriving){
			if(!this.ns.getWest().getDisabled() && !isWestNSTracking())
				if(!this.ns.getWest().getStatus().equals(240)) nsWest = new DriveBrokenException("West arm NS was not driving, disabled or on target ", 
						ExceptionUtils.getStackTrace(new Exception()),this,TCCConstants.WEST, TCCConstants.NS);
		}



		boolean isEastMDDriving = this.md.getEast().getDriving();

		if(!isEastMDDriving){
			if(!this.md.getEast().getDisabled() && !isEastMDTracking())
				mdEast = new DriveBrokenException("East arm MD was not driving, disabled or on target ",
						ExceptionUtils.getStackTrace(new Exception()),this,TCCConstants.EAST, TCCConstants.MD);
		}

		boolean isWestMDDriving =  this.md.getWest().getDriving();

		if(!isWestMDDriving ){
			if(!this.md.getWest().getDisabled() && !isWestMDTracking())
				mdWest = new DriveBrokenException("West arm MD was not driving, disabled or on target ", 
						ExceptionUtils.getStackTrace(new Exception()),this,TCCConstants.WEST, TCCConstants.MD);
		}

		if(nsEast != null && nsWest != null ) {
			
			String error  = this.getNs().getError();
			if(StringUtils.containsIgnoreCase(error, "east")) throw nsEast;
			
			else if(StringUtils.containsIgnoreCase(error, "west")) throw nsWest;
			
			else throw new DriveBrokenException("East and West NS drives were not driving, disabled or on target ", 
					ExceptionUtils.getStackTrace(new Exception()),this,TCCConstants.BOTH_ARMS, TCCConstants.NS);
			
		}
		
		else if ( nsEast != null ) throw nsEast;
		
		else if ( nsWest != null ) throw nsWest;
		
		
		if(mdEast != null && mdWest != null ) {
			
			String error  = this.getMd().getError();
			if(StringUtils.containsIgnoreCase(error, "east")) throw mdEast;
			
			else if(StringUtils.containsIgnoreCase(error, "west")) throw mdWest;
			
			else throw new DriveBrokenException("East and West MD drives were not driving, disabled or on target ", 
					ExceptionUtils.getStackTrace(new Exception()),this,TCCConstants.BOTH_ARMS, TCCConstants.MD);
			
		}
		
		else if ( mdEast != null ) throw mdEast;
		
		else if ( mdWest != null ) throw mdWest;
		

		return isEastMDDriving || isWestMDDriving || isEastNSDriving || isWestNSDriving;
	}



	public boolean isEastNSTracking(){
		double sourceRadNS = this.coordinates.getNs().getRadianValue();		
		double eastRadNS = this.ns.getEast().getTilt().getRadianValue() + this.ns.getEast().getOffset().getRadianValue();

		return (Math.abs(eastRadNS - sourceRadNS) < TCCConstants.OnSourceThresholdRadNS);

	}

	public boolean isWestNSTracking(){
		double sourceRadNS = this.coordinates.getNs().getRadianValue();
		double westRadNS = this.ns.getWest().getTilt().getRadianValue() + this.ns.getWest().getOffset().getRadianValue();

		return (Math.abs(westRadNS - sourceRadNS) < TCCConstants.OnSourceThresholdRadNS);

	}


	public boolean isEastMDTracking(){
		double sourceRadMD = this.coordinates.getEw().getRadianValue();		
		double eastRadMD = this.md.getEast().getTilt().getRadianValue() + this.md.getEast().getOffset().getRadianValue();

		return (Math.abs(eastRadMD - sourceRadMD) < TCCConstants.OnSourceThresholdRadMD);

	}

	public boolean isWestMDTracking(){
		double sourceRadMD = this.coordinates.getEw().getRadianValue();
		double westRadMD = this.md.getWest().getTilt().getRadianValue() + this.md.getWest().getOffset().getRadianValue();

		return (Math.abs(westRadMD - sourceRadMD) < TCCConstants.OnSourceThresholdRadMD);

	}




	public Boolean isTelescopeTracking() throws DriveBrokenException{


		double sourceRadNS = this.coordinates.getNs().getRadianValue();
		double sourceRadMD = this.coordinates.getEw().getRadianValue();
		

		double eastRadNS = this.ns.getEast().getTilt().getRadianValue();
		if(Math.abs(eastRadNS - sourceRadNS) > TCCConstants.OnSourceThresholdRadNS 
				&& !this.ns.getEast().getDisabled()) {
			System.err.println(eastRadNS * Constants.rad2Deg + "-" + sourceRadNS * Constants.rad2Deg + ">" +  TCCConstants.OnSourceThresholdRadNS * Constants.rad2Deg);
			throw new DriveBrokenException("East NS seems to have been broken while tracking",TCCConstants.EAST, TCCConstants.NS);
		}

		double westRadNS = this.ns.getWest().getTilt().getRadianValue();
		if(Math.abs(westRadNS - sourceRadNS) > TCCConstants.OnSourceThresholdRadNS
				&& !this.ns.getWest().getDisabled()) {
			System.err.println(westRadNS * Constants.rad2Deg + "-" + sourceRadNS * Constants.rad2Deg + ">" +  TCCConstants.OnSourceThresholdRadNS * Constants.rad2Deg);
			throw new DriveBrokenException("West NS seems to have been broken while tracking",TCCConstants.WEST, TCCConstants.NS);
		}

		double eastRadMD = this.md.getEast().getTilt().getRadianValue();
		if(Math.abs(eastRadMD - sourceRadMD) > TCCConstants.OnSourceThresholdRadMD
				&& !this.md.getEast().getDisabled()) {
			throw new DriveBrokenException("East MD seems to have been broken while tracking",TCCConstants.EAST, TCCConstants.MD);
		}

		double westRadMD = this.md.getWest().getTilt().getRadianValue();
		if(Math.abs(westRadMD - sourceRadMD) > TCCConstants.OnSourceThresholdRadMD
				&& !this.md.getWest().getDisabled()) {
			throw new DriveBrokenException("West MD seems to have been broken while tracking",TCCConstants.WEST, TCCConstants.MD);
		}
		 
		return true;

	}




	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
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
		Angle offset;
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

		public Angle getOffset() {
			return offset;
		}

		public void setOffset(Angle offset) {
			this.offset = offset;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}




}





