package standalones;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Point{
	String ra;
	String dec;
	Double startFanBeam;
	Double startNS;
	Double endFanBeam;
	Double endNS;
	Map<Integer, List<Traversal>> traversalMap = new HashMap<Integer, List<Traversal>>();
	boolean uniq;
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
	public Map<Integer, List<Traversal>> getTraversalMap() {
		return traversalMap;
	}
	public void setTraversalMap(Map<Integer, List<Traversal>> traversalMap) {
		this.traversalMap = traversalMap;
	}
	public boolean isUniq() {
		return uniq;
	}
	public void setUniq(boolean uniq) {
		this.uniq = uniq;
	}
	
	
}