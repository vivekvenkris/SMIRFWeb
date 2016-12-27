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
@Table(name="tied_beam_sources")
public class TiedBeamSource {
@Id
@GeneratedValue(strategy= GenerationType.AUTO)
@Column(name = "source_id")
private Integer sourceID;

@Column(name = "source_name")
private String sourceName;

@Column(name = "known_pulsar")
private Boolean knownPSR;

@Column(name = "ra_hms")
@Convert(converter = Angle2HHMMSS.class)
private Angle angleRA;
	
@Column(name = "dec_hms")
@Convert(converter = Angle2DDMMSS.class)
private Angle angleDEC;

@Convert(converter = Angle2DDMMSS.class)
@Column(name = "gal_lat_dms")
private Angle angleLAT;

@Convert(converter = Angle2DDMMSS.class)
@Column(name = "gal_lon_dms")
private Angle angleLON;
@Column
private Double period;

@Column(name = "dm")
private Double dispersionMeasure;

@Column(name = "acc")
private Double acceleration;

@Column(name = "snr_5")
private Double SNR5;

@Column(name = "snr_15")
private Double SNR15;



}
