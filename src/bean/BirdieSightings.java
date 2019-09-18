package bean;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

public class BirdieSightings {
	
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "birdie_sightings_id")
	private Integer birdieSightingsID;
	
	@OneToOne
	@JoinColumn(name = "obs_id")
	private Observation observation;
	
	@ManyToOne
	@JoinColumn(name = "birdie_id")
	private Birdie birdie;
	
	@Column(name = "nfbs")
	private Integer numFBs;

	
	public BirdieSightings(BirdieSightingsTO bs) {
		this.birdieSightingsID = bs.getBirdieSightingsID();
		
		this.observation = new Observation(bs.getObservationTO());
		
		this.birdie = new Birdie(bs.getBirdieTO());
		
		this.numFBs = bs.getNumFBs();
	}
	
	
	
	public Integer getBirdieSightingsID() {
		return birdieSightingsID;
	}

	public void setBirdieSightingsID(Integer birdieSightingsID) {
		this.birdieSightingsID = birdieSightingsID;
	}

	public Observation getObservation() {
		return observation;
	}

	public void setObservation(Observation observation) {
		this.observation = observation;
	}

	

	public Birdie getBirdie() {
		return birdie;
	}

	public void setBirdie(Birdie birdie) {
		this.birdie = birdie;
	}

	public Integer getNumFBs() {
		return numFBs;
	}

	public void setNumFBs(Integer numFBs) {
		this.numFBs = numFBs;
	}
	
	
	


}
