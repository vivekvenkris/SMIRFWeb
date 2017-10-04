package bean;

import java.text.SimpleDateFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="observations")
public class Observation {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "obs_id")
	private Integer observationID;
	
	@Column(name = "source_name")
	private String sourceName;
	
	@Column(name = "utc")
	private String utc;
	
	@Column(name = "tobs_seconds")
	private Integer tobs;
	
	@ManyToOne
	@JoinColumn(name = "session_id")
	private ObservingSession observingSession;
	
	@Column(name = "tied_beam_sources")
	private String tiedBeamSources;
	
	@Column(name = "observation_type")
	private String observationType;
	
	@Column(name = "complete")
	private Boolean complete;
	
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSS");

	
	public Observation() {	}
	public Observation(ObservationTO observationTO) {
		this.sourceName = observationTO.getName();
		this.utc = observationTO.getUtc();
		this.tobs = observationTO.getTobs();
		this.tiedBeamSources = observationTO.getTiedBeamSources().toString();
		this.observationType = observationTO.getObsType();
		if(observationTO.getObservingSession()!=null)
			this.observingSession = new ObservingSession(observationTO.getObservingSession());
		
	}

	public Integer getObservationID() {
		return observationID;
	}

	public void setObservationID(Integer observationID) {
		this.observationID = observationID;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getUtc() {
		return utc;
	}

	public void setUtc(String utc) {
		this.utc = utc;
	}

	public Integer getTobs() {
		return tobs;
	}

	public void setTobs(Integer tobs) {
		this.tobs = tobs;
	}


	public ObservingSession getObservingSession() {
		return observingSession;
	}
	public void setObservingSession(ObservingSession observingSession) {
		this.observingSession = observingSession;
	}
	public String getTiedBeamSources() {
		return tiedBeamSources;
	}

	public void setTiedBeamSources(String tiedBeamSources) {
		this.tiedBeamSources = tiedBeamSources;
	}

	public String getObservationType() {
		return observationType;
	}

	public void setObservationType(String observationType) {
		this.observationType = observationType;
	}
	public Boolean getComplete() {
		return complete;
	}
	public void setComplete(Boolean complete) {
		this.complete = complete;
	}

	
	
	
}
