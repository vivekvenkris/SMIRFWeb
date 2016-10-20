package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import bean.CoordinateTO;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import manager.MolongloCoordinateTransforms;




public class SMIRF_GetUniqStitches4 implements SMIRFConstants, Constants {

	public Integer getServer(double startFB, double endFB){
		Integer startServer = (int)Math.floor((startFB-1)/numBeamsPerServer);
		Integer endServer = (int)Math.floor((endFB-1)/numBeamsPerServer);
		
		if(startServer != endServer ) return BF08;
		
		return startServer;
		
	}


	public List<Point> generatePoints(double boresightHA, double boresightDEC) throws EmptyCoordinatesException, CoordinateOverrideException {
		List<Point> points = new LinkedList<Point>();
		int tobs = 900;
		double tsamp = 655.36E-6;
		long nsamples = (long)(tobs/tsamp); 
		double thresholdPercent = 5;
		int sampleSteps = 20480;
		Double PercentTime = sampleSteps*100.0/nsamples;
		int maxFBTraversal = 10;
		Integer maxTraversals = 0;

		double beamWidthNS = 2.0 * Constants.toRadians;
		double beamWidthMD = 4.0 * Constants.toRadians;
		int numFB = 352;
		CoordinateTO boresight = new CoordinateTO(boresightHA,boresightDEC,null,null);
		MolongloCoordinateTransforms.skyToTel(boresight);

		double deltaMD = beamWidthMD/(Math.cos(boresight.getRadMD())*numFB);


		
		for(double nfb = 1; nfb <= numFB; nfb = nfb + 1){

			double nfbNow = nfb;

			double mdDistance = (nfb-(numFB/2 +1)) * deltaMD;
			double mdNow = boresight.getRadMD() + mdDistance;
			double nsDistance = -beamWidthNS/2.0;
			Map<Integer, Double> lastPointMap = null;
			while(nsDistance <= beamWidthNS/2.0){

				double nsNow = boresight.getRadNS() + nsDistance;

				CoordinateTO now = new CoordinateTO(null,null,nsNow,mdNow);
				MolongloCoordinateTransforms.telToSky(now);

				boolean unwantedPoint = false;
				Map<Integer, Double> samplesInFB = new LinkedHashMap<Integer, Double>(maxFBTraversal);

				for(int n=0;n<nsamples; n+=sampleSteps){
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

					Double percent = samplesInFB.getOrDefault(nfbLater, 0.0);
					percent += PercentTime;
					samplesInFB.put(nfbLater, percent);

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
						for(Map.Entry<Integer, Double> entry: samplesInFB.entrySet()){
							Double percent = entry.getValue();
							traversals.add(new Traversal(entry.getKey()+0.0,nsDistance,startSample,(long)(percent*nsamples),percent.intValue()));
							startSample +=(int)(percent*nsamples);
						}
						p.traversalMap.put(server, traversals);
						if(maxTraversals < samplesInFB.size()) maxTraversals = samplesInFB.size();
						p.uniq = true;
					}
					else{
						for(Map.Entry<Integer, Double> previous : lastPointMap.entrySet()){
							Integer fb = previous.getKey();
							Double  percent = previous.getValue();

							Double percent_now = samplesInFB.getOrDefault(fb, 0.0);

							if(Math.abs(percent - percent_now) > thresholdPercent){
								Point lastPoint = points.get(points.size()-1);
								lastPoint.endFanBeam = nfb;
								lastPoint.endNS = nsNow;
								Long startSample = 0L;
								Integer server = getServer(Collections.min(samplesInFB.keySet()),Collections.max(samplesInFB.keySet()));
								List<Traversal> traversals = p.traversalMap.getOrDefault( server , new ArrayList<>());
								for(Map.Entry<Integer, Double> entry: samplesInFB.entrySet()){
									 Double pc = entry.getValue();
									 traversals.add(new Traversal(entry.getKey()+0.0,nsNow,startSample, startSample+ (Long)Math.round(percent*nsamples/100.0),pc.intValue()));
									startSample += (int)Math.round(percent*nsamples);

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

		//System.err.println(boresightHA*Constants.rad2Deg + " " + boresightDEC*Constants.rad2Deg + " " + maxTraversals);
		return points;
	}

	public static void main(String[] args) throws EmptyCoordinatesException, CoordinateOverrideException {
		SMIRF_GetUniqStitches4 gus = new SMIRF_GetUniqStitches4();
		List<Point> points = gus.generatePoints(Double.parseDouble(args[0])*Constants.deg2Rad,Double.parseDouble(args[1])*Constants.deg2Rad); 
		for(Point p: points){
			for(Map.Entry<Integer, List<Traversal>> traversalEntry : p.traversalMap.entrySet()) {
				for(Traversal t: traversalEntry.getValue()){
					System.err.println(   p.startFanBeam + " "+ p.endFanBeam + " "+ String.format("%7.5f", p.startNS*Constants.rad2Deg)
					+ " "+ String.format("%7.5f", p.endNS*Constants.rad2Deg) + " "+ t.fanbeam+ " " + String.format("%7.5f", t.ns) + " "+ t.startSample 
					+ " "+ t.numSamples + " "+ t.percent);
				}
			}
			
		}
		
//		for( double ha = -45; ha <= 45; ha = ha ++){
//			for(double dec = -66; dec <=20; dec++){
//				gus.generatePoints(ha*Constants.deg2Rad,dec*Constants.deg2Rad);
//			}
//		}



		//launch(args);
	}


}
