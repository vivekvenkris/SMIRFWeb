package bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="birdies")
public class Birdie {


	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "birdie_id")
	private Integer birdieID;
	
	private Double period;

	@Column(name = "num_obs_seen")
	private Integer numObsSeen;
	
	@Column(name = "avg_num_times_seen")
	private Integer avgNumFBsSeenIn;
	
	@Column(name = "in_birdies_list")
	private Boolean inBirdiesList;
	
	public Birdie() {	}
	public Birdie(BirdieTO b) {
		this.birdieID = b.getBirdieID();
		this.period = b.getPeriod();
		this.numObsSeen = b.getNumObsSeen();
		this.avgNumFBsSeenIn = b.getAvgNumFBsSeenIn();
		this.inBirdiesList = b.getInBirdiesList();
	}
	
	
	public Integer getBirdieID() {
		return birdieID;
	}

	public void setBirdieID(Integer birdieID) {
		this.birdieID = birdieID;
	}

	public Double getPeriod() {
		return period;
	}

	public void setPeriod(Double period) {
		this.period = period;
	}

	public Integer getNumObsSeen() {
		return numObsSeen;
	}

	public void setNumObsSeen(Integer numObsSeen) {
		this.numObsSeen = numObsSeen;
	}

	public Integer getAvgNumFBsSeenIn() {
		return avgNumFBsSeenIn;
	}

	public void setAvgNumFBsSeenIn(Integer avgNumFBsSeenIn) {
		this.avgNumFBsSeenIn = avgNumFBsSeenIn;
	}

	public Boolean getInBirdiesList() {
		return inBirdiesList;
	}

	public void setInBirdiesList(Boolean inBirdiesList) {
		this.inBirdiesList = inBirdiesList;
	}
	
	

}
