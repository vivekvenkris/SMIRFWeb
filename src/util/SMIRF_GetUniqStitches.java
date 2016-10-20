package util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import bean.CoordinateTO;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import manager.MolongloCoordinateTransforms;

public class SMIRF_GetUniqStitches implements SMIRFConstants, Constants {

	public Integer getServer(double startFB, double endFB){
		Integer startServer = (int)Math.floor((startFB-1)/numBeamsPerServer);
		Integer endServer = (int)Math.floor((endFB-1)/numBeamsPerServer);

		if(startServer != endServer ) return BF08;

		return startServer;

	}

	public List<Point> generatePoints(double boresightHA, double boresightDEC, double thresholdPercent, Long totalSamples) throws EmptyCoordinatesException, CoordinateOverrideException {

		List<Point> points = new LinkedList<Point>();
		int sampleSteps = 20480;
		int maxFBTraversal = 10;
		Integer maxTraversals = 0;

		double beamWidthNS = 2.0 * Constants.toRadians;
		double beamWidthMD = 4.0 * Constants.toRadians;

		int numFB = 352;

		CoordinateTO boresight = new CoordinateTO(boresightHA,boresightDEC,null,null);
		MolongloCoordinateTransforms.skyToTel(boresight);


		for(double nfb = 1; nfb <= numFB; nfb = nfb + 1){

			double nfbNow = nfb;
			double mdDistance = Utilities.getMDDistance(nfb, numFB, boresight.getRadMD());
			double mdNow = boresight.getRadMD() + mdDistance;
			double nsDistance = -beamWidthNS/2.0;
			Map<Integer, Long> lastPointMap = null;
			while(nsDistance <= beamWidthNS/2.0){

				double nsNow = boresight.getRadNS() + nsDistance;

				CoordinateTO now = new CoordinateTO(null,null,nsNow,mdNow);
				MolongloCoordinateTransforms.telToSky(now);

				boolean unwantedPoint = false;
				Map<Integer, Long> samplesInFB = new LinkedHashMap<Integer, Long>(maxFBTraversal);

				for(int n=0;n<totalSamples; n+=sampleSteps){

					double haBoresightLater = boresightHA + n * Constants.samples2secs * Constants.sec2Hrs * Constants.hrs2Deg * Constants.deg2Rad;
					double decBoresightLater = boresightDEC;

					CoordinateTO boresightLater = new CoordinateTO(haBoresightLater,decBoresightLater,null,null);
					MolongloCoordinateTransforms.skyToTel(boresightLater);

					double haLater = now.getRadHA() +  n * Constants.samples2secs * Constants.sec2Hrs * Constants.hrs2Deg * Constants.deg2Rad;
					double decLater = now.getRadDec();

					CoordinateTO later = new CoordinateTO(haLater,decLater,null,null);
					MolongloCoordinateTransforms.skyToTel(later);

					Integer nfbLater = (int) Math.round(Utilities.getFB(numFB, boresightLater.getRadMD(), later.getRadMD()));
					if(nfbLater <= 0 || nfbLater > numFB) unwantedPoint = true;

					Long numSamples = samplesInFB.getOrDefault(nfbLater, 0L);
					numSamples += sampleSteps;
					samplesInFB.put(nfbLater, numSamples);

				}

				Point p = new Point();
				p.startFanBeam = p.endFanBeam = nfb;
				p.startNS = p.endNS = nsNow;
				p.uniq = false;

				if(!unwantedPoint){
					if(lastPointMap ==null){
						Long startSample = 0L;
						Integer server = getServer(Collections.min(samplesInFB.keySet()),Collections.max(samplesInFB.keySet()));
						List<Traversal> traversals = p.traversalMap.getOrDefault( server , new ArrayList<>());
						for(Map.Entry<Integer, Long> entry: samplesInFB.entrySet()){
							Long numSamples = entry.getValue();
							Long numSamps = (startSample + numSamples) > totalSamples ? (totalSamples-startSample): numSamples;
							Double percent =  100*(numSamples+0.0)/totalSamples;
							traversals.add(new Traversal(entry.getKey()+0.0,nsDistance,startSample,numSamps,percent.intValue()));
							startSample += numSamples;
						}
						p.traversalMap.put(server, traversals);
						if(maxTraversals < samplesInFB.size()) maxTraversals = samplesInFB.size();
						p.uniq = true;
					}
					else{
						for(Map.Entry<Integer, Long> previous : lastPointMap.entrySet()){
							Integer fb = previous.getKey();
							Long numSamplesPrevious = previous.getValue();
							Double  percent =  100*(numSamplesPrevious+0.0)/totalSamples;
							Long numSamplesNow = samplesInFB.getOrDefault(fb, 0L);
							Double percentNow = 100*(numSamplesNow + 0.0)/totalSamples;

							if(Math.abs(percent - percentNow) > thresholdPercent){
								Point lastPoint = points.get(points.size()-1);
								lastPoint.endFanBeam = nfb;
								lastPoint.endNS = nsNow;
								Long startSample = 0L;
								Integer server = getServer(Collections.min(samplesInFB.keySet()),Collections.max(samplesInFB.keySet()));
								List<Traversal> traversals = p.traversalMap.getOrDefault( server , new ArrayList<>());
								for(Map.Entry<Integer, Long> entry: samplesInFB.entrySet()){
									Long numSamples = entry.getValue();
									Long numSamps = (startSample + numSamples) > totalSamples ? (totalSamples-startSample): numSamples;
									Double pc =  100*(numSamples+0.0)/totalSamples;
									traversals.add(new Traversal(entry.getKey()+0.0,nsDistance,startSample,numSamps,pc.intValue()));
									startSample += numSamples;
								}
								p.traversalMap.put(server, traversals);
								if(maxTraversals < samplesInFB.size()) maxTraversals = samplesInFB.size();
								p.uniq = true;
								break;
							}
						}
					}
					if(p.uniq) {
						lastPointMap = samplesInFB;
						points.add(p);
					}
				}
				nsDistance += 23*Constants.arcSec2Deg*Constants.deg2Rad;
			}
		}
		return points;
	}
	public static void main(String[] args) throws EmptyCoordinatesException, CoordinateOverrideException {
		SMIRF_GetUniqStitches gus = new SMIRF_GetUniqStitches();
		List<Point> points = gus.generatePoints(Double.parseDouble(args[0])*Constants.deg2Rad,Double.parseDouble(args[1])*Constants.deg2Rad,10,Math.round(900/Constants.tsamp)); 
		for(Point p: points){
			List<Traversal> traversals = p.traversalMap.get(Integer.parseInt(args[2]));
			if(traversals!=null) {
				System.err.print( p.startFanBeam + " "+ p.endFanBeam + " "+ String.format("%7.5f", p.startNS*Constants.rad2Deg) + " "+ String.format("%7.5f", p.endNS*Constants.rad2Deg) + " ");
				for(Traversal t: traversals){
					System.err.print(t.fanbeam+ " " + String.format("%7.5f", t.ns) + " "+ t.startSample + " "+ t.numSamples + " "+ t.percent + " ");
				}
				System.err.println();
			}
		}
		//	for( double ha = -45; ha <= 45; ha = ha ++){
		//	for(double dec = -66; dec <=20; dec++){
		//	gus.generatePoints(ha*Constants.deg2Rad,dec*Constants.deg2Rad);
		//	}
		//	}
		//launch(args);
	}
}

