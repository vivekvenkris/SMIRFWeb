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
	//Map<Integer, List<Traversal>> traversalMap = new HashMap<Integer, List<Traversal>>();
	List<Traversal> traversalList = new ArrayList<>();
	boolean uniq;
	Integer beamSearcher;
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