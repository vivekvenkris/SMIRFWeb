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
}