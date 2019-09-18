package bean;

public class BirdieTO {


	private Integer birdieID;

	private Double period;

	private Integer numObsSeen;

	private Integer avgNumFBsSeenIn;

	private Boolean inBirdiesList;
	
	
	public BirdieTO(Birdie b) {
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



