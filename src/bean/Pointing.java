package bean;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.jastronomy.jsofa.JSOFA;
import org.jastronomy.jsofa.JSOFA.SphericalCoordinate;

import converters.Angle2DDMMSS;
import converters.Angle2HHMMSS;
@Entity
@Table(name="pointings")
public class Pointing{
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	@Column(name = "pointing_id")
	private Integer pointingID;
	
	@Column(name = "pointing_name")
	private String pointingName;
	
	@Convert(converter = Angle2DDMMSS.class)
	@Column(name = "gal_lat_dms")
	private Angle angleLAT;
	
	@Convert(converter = Angle2DDMMSS.class)
	@Column(name = "gal_lon_dms")
	private Angle angleLON;
	
	@Convert(converter =Angle2HHMMSS.class)
	@Column(name = "ra_hms")
	private Angle angleRA;	
	
	@Convert(converter = Angle2DDMMSS.class)
	@Column(name = "dec_dms")
	private Angle angleDEC;
	@Column
	private Integer priority= 10;
	@Column
	private String type = "G";
	
	@Column(name = "num_obs")
	private Integer numObs = 0;
	
	@Column(name = "least_cadance_days")
	private Integer leastCadanceInDays;
	
	@Column(name = "assoc_psrs")
	private String associatedPulsars;
	
	private Integer tobs;
	
	@Column(name = "start_md_percent")
	private Integer startMDInPercent;
	
	@Column(name = "end_md_percent")
	private Integer endMDInPercent;
	
	
	public Pointing(){
				
	}
	
	public Pointing(Angle lat, Angle lon) {
		SphericalCoordinate sc = JSOFA.jauG2icrs(lon.getRadianValue(), lat.getRadianValue());
		this.angleLAT = lat; 
		this.angleLON  = lon ;
		this.angleRA = new Angle(sc.alpha,Angle.HHMMSS); // (value, print format)
		this.angleDEC = new Angle(sc.delta,Angle.DDMMSS);
		this.associatedPulsars = "";
		this.endMDInPercent = 25;
		this.startMDInPercent = -25;
	}
	
	public Pointing(PointingTO pointingTO){
		if(pointingTO.getPointingID() != null ) this.pointingID = pointingTO.getPointingID();
		this.pointingName = pointingTO.getPointingName();
		this.angleLAT = pointingTO.getAngleLAT();
		this.angleLON = pointingTO.getAngleLON();
		this.angleRA = pointingTO.getAngleRA();
		this.angleDEC = pointingTO.getAngleDEC();
		
		this.type = pointingTO.getType();
		this.priority = pointingTO.getPriority();
		this.numObs = pointingTO.getNumObs();
		this.tobs = pointingTO.getTobs();
		this.leastCadanceInDays = pointingTO.getLeastCadanceInDays();
		
		this.startMDInPercent = pointingTO.getStartMDInPercent();
		this.endMDInPercent = pointingTO.getEndMDInPercent();
		
		this.associatedPulsars = "";
		if(pointingTO.getAssociatedPulsars() != null)
		for(int i=0; i< pointingTO.getAssociatedPulsars().size(); i++) {
			
			TBSourceTO to = pointingTO.getAssociatedPulsars().get(i);
			this.associatedPulsars += to.getPsrName();
			
			if(i+1 < pointingTO.getAssociatedPulsars().size()) this.associatedPulsars += ";";
			
		}
		
	}
	
	
	
	
	
	 
	public Integer getLeastCadanceInDays() {
		return leastCadanceInDays;
	}
	public void setLeastCadanceInDays(Integer leastCadanceInDays) {
		this.leastCadanceInDays = leastCadanceInDays;
	}
	
	@Override
	public String toString() {
		//return this.angleLAT.getDegreeValue() + " "+ this.angleLON.getDegreeValue() + " " + this.angleRA + " "+ this.angleDEC + "\n";
		return this.angleRA.getDegreeValue() + " "+ this.angleDEC.getDegreeValue() + " " + this.angleRA + " "+ this.angleDEC + "\n";
	}

	
	

	public Integer getTobs() {
		return tobs;
	}
	public void setTobs(Integer tobs) {
		this.tobs = tobs;
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


	public int getPriority() {
		return priority;
	}


	public void setPriority(int priority) {
		this.priority = priority;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
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
	
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	public Integer getNumObs() {
		return numObs;
	}
	public void setNumObs(Integer numObs) {
		this.numObs = numObs;
	}
	public String getAssociatedPulsars() {
		return associatedPulsars;
	}
	public void setAssociatedPulsars(String associatedPulsars) {
		this.associatedPulsars = associatedPulsars;
	}

	public Integer getStartMDInPercent() {
		return startMDInPercent;
	}

	public void setStartMDInPercent(Integer startMDInPercent) {
		this.startMDInPercent = startMDInPercent;
	}

	public Integer getEndMDInPercent() {
		return endMDInPercent;
	}

	public void setEndMDInPercent(Integer endMDInPercent) {
		this.endMDInPercent = endMDInPercent;
	}
	
	
	


}

