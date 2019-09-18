package bean;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

public class BirdieSightingsTO {
	
	private Integer birdieSightingsID;
	
	private ObservationTO observationTO;
	
	private BirdieTO birdieTO;
	
	private Integer numFBs;
	
	
	public BirdieSightingsTO(BirdieSightings bs) {
		this.birdieSightingsID = bs.getBirdieSightingsID();
		
		this.observationTO = new ObservationTO(bs.getObservation());
		
		this.birdieTO = new BirdieTO(bs.getBirdie());
		
		this.numFBs = bs.getNumFBs();
	}

	public Integer getBirdieSightingsID() {
		return birdieSightingsID;
	}

	public void setBirdieSightingsID(Integer birdieSightingsID) {
		this.birdieSightingsID = birdieSightingsID;
	}

	

	public ObservationTO getObservationTO() {
		return observationTO;
	}

	public void setObservationTO(ObservationTO observationTO) {
		this.observationTO = observationTO;
	}

	public BirdieTO getBirdieTO() {
		return birdieTO;
	}

	public void setBirdieTO(BirdieTO birdieTO) {
		this.birdieTO = birdieTO;
	}

	public Integer getNumFBs() {
		return numFBs;
	}

	public void setNumFBs(Integer numFBs) {
		this.numFBs = numFBs;
	}
	
	
	


}
