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
		
	@Column(name = "dec_hms")
	@Convert(converter = Angle2DDMMSS.class)
	private Angle angleDEC;
	
	@Column(name = "snr_5min")
	private Integer fiveMinSNR;
	
	
	public static FluxCalibrator findNearestFluxCal(){
		
		return null;
	}
}
