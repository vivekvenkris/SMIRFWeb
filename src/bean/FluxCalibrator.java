package bean;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import converters.Angle2DDMMSS;
import converters.Angle2HHMMSS;

@Entity
@Table(name="flux_calibrators")
public class FluxCalibrator {
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	@Column(name = "source_id")
	private Integer sourceID;
	
	@Column(name = "source_name")
	private String sourceName;
	
	@Column(name = "ra_hms")
	@Convert(converter = Angle2HHMMSS.class)
	private Angle angleRA;
		
	@Column(name = "dec_dms")
	@Convert(converter = Angle2DDMMSS.class)
	private Angle angleDEC;
	
	@Column(name="dm")
	private Integer dm;
	
	@Column(name = "snr_5min")
	private Integer fiveMinSNR;
	
	
	
	
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




	public static FluxCalibrator findNearestFluxCal(){
		
		return null;
	}
}
