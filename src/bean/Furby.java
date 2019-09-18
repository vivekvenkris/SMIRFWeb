package bean;

import util.Constants;
import util.SMIRFConstants;

public class Furby {
	
	private String furbyID;
	private Integer	 beam;
	private Double timeStamp;
	
	public static Integer getRandomBeamNumber() {
		Integer nfb = 352;
		return  (int) Math.floor(Math.random() * 351 +2);
	}
	
	public Furby clone() {
		return new Furby(this.furbyID,this.beam, this.timeStamp);
	}
	
	
	public Integer getBeam() {
		return beam;
	}


	public void setBeam(Integer beam) {
		this.beam = beam;
	}


	public String getFurbyID() {
		return furbyID;
	}
	public void setFurbyID(String furbyID) {
		this.furbyID = furbyID;
	}

	public Double getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Double timeStamp) {
		this.timeStamp = timeStamp;
	}
	public Furby(String furbyID) {
		super();
		this.furbyID = furbyID;
		
	}
	
	
	
	public Furby(String furbyID, Integer beam, Double timeStamp) {
		super();
		this.furbyID = furbyID;
		this.beam = beam;
		this.timeStamp = timeStamp;
	}

	public static Furby addTimeStamp(Furby f, double timeStamp) {
		f.timeStamp = timeStamp;
		return f;
	}
	
	@Override
	public String toString() {
		return this.furbyID + " "+ this.timeStamp;
	}

	

}
