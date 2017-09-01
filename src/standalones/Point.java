package standalones;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.Constants;

public class Point{
	String ra;
	String dec;
	Double startFanBeam;
	Double startNS;
	Double endFanBeam;
	Double endNS;
	String line;
	//Map<Integer, List<Traversal>> traversalMap = new HashMap<Integer, List<Traversal>>();
	List<Traversal> traversalList = new ArrayList<>();
	boolean uniq;
	Integer beamSearcher;
	
	public Point(){}
	public Point(String pointString) {
		
		String[] chunks = pointString.split(" ");
		line = pointString;
		ra = chunks[0];
		dec = chunks[1];
		
		startFanBeam = Double.parseDouble(chunks[2]);
		endFanBeam = Double.parseDouble(chunks[3]);
		
		startNS = Double.parseDouble(chunks[4]);
		endNS = Double.parseDouble(chunks[5]);
		
		for(int i=6; i< chunks.length; i+=5){
			
			Traversal traversal = new Traversal(Double.parseDouble(chunks[i]), Double.parseDouble(chunks[i+1]),
					Long.parseLong(chunks[i+2]), Long.parseLong(chunks[i+3]), Integer.parseInt(chunks[i+4]));
			
			this.traversalList.add(traversal);
			
		}
		
	}
	
	public String getFBPercents(){
		
		String pointStr = "";
		
		for(Traversal t : this.getTraversalList()){
			pointStr += ( t.getFanbeam() + " " + t.getPercent() + "\n");
		}
		return pointStr;
	}
	
	public String getRa() {
		return ra;
	}
	public void setRa(String ra) {
		this.ra = ra;
	}
	public String getDec() {
		return dec;
	}
	public void setDec(String dec) {
		this.dec = dec;
	}
	public Integer getBeamSearcher() {
		return beamSearcher;
	}
	public void setBeamSearcher(Integer beamSearcher) {
		this.beamSearcher = beamSearcher;
	}
	public Double getStartFanBeam() {
		return startFanBeam;
	}
	public void setStartFanBeam(Double startFanBeam) {
		this.startFanBeam = startFanBeam;
	}
	public Double getStartNS() {
		return startNS;
	}
	public void setStartNS(Double startNS) {
		this.startNS = startNS;
	}
	public Double getEndFanBeam() {
		return endFanBeam;
	}
	public void setEndFanBeam(Double endFanBeam) {
		this.endFanBeam = endFanBeam;
	}
	public Double getEndNS() {
		return endNS;
	}
	public void setEndNS(Double endNS) {
		this.endNS = endNS;
	}
	
	public boolean isUniq() {
		return uniq;
	}
	public void setUniq(boolean uniq) {
		this.uniq = uniq;
	}
	public List<Traversal> getTraversalList() {
		return traversalList;
	}
	public void setTraversalList(List<Traversal> traversalList) {
		this.traversalList = traversalList;
	}
	
	
	public String getLine() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
	@Override
	public String toString() {
		Point pt = this;
		String s = 	pt.ra + " " + pt.dec + " " + pt.startFanBeam + " "+
		pt.endFanBeam + " "+ String.format("%7.5f", pt.startNS*Constants.rad2Deg) + " "+ 
				String.format("%7.5f", pt.endNS*Constants.rad2Deg) + " " ;
		
		for(Traversal t: pt.traversalList)  s += t;

		return s;
	}
	
	
}