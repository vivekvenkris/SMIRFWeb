package standalones;

public class Traversal{
	Double fanbeam;
	Double ns;
	Long startSample;
	Long numSamples;
	Integer percent;
	public Traversal(Double fanbeam, Double ns,Long startSample,Long numSamples, Integer percent){
		this.fanbeam = fanbeam;
		this.ns = ns;
		this.percent = percent;
		this.startSample = startSample;
		this.numSamples = numSamples;
	}
	public Double getFanbeam() {
		return fanbeam;
	}
	public void setFanbeam(Double fanbeam) {
		this.fanbeam = fanbeam;
	}
	public Double getNs() {
		return ns;
	}
	public void setNs(Double ns) {
		this.ns = ns;
	}
	public Long getStartSample() {
		return startSample;
	}
	public void setStartSample(Long startSample) {
		this.startSample = startSample;
	}
	public Long getNumSamples() {
		return numSamples;
	}
	public void setNumSamples(Long numSamples) {
		this.numSamples = numSamples;
	}
	public Integer getPercent() {
		return percent;
	}
	public void setPercent(Integer percent) {
		this.percent = percent;
	}
	
	
}