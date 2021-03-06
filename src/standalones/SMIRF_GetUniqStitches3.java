package standalones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import bean.Angle;
import bean.CoordinateTO;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import manager.MolongloCoordinateTransforms;
import util.Constants;
import util.Utilities;
class Traversal2{
	Double fanbeam;
	Double ns;
	Long startSample;
	Long numSamples;
	Integer percent;
	public Traversal2(Double fanbeam, Double ns,Long startSample,Long numSamples, Integer percent){
		this.fanbeam = fanbeam;
		this.ns = ns;
		this.percent = percent;
		this.startSample = startSample;
		this.numSamples = numSamples;
	}
}
class Point3{
	Double startFanBeam;
	Double startNS;
	Double endFanBeam;
	Double endNS;
	List<Traversal2> traversals = new ArrayList<>();
	Map<Integer, List<Traversal2>> traversalMap = new HashMap<Integer, List<Traversal2>>();
	boolean uniq;
}


public class SMIRF_GetUniqStitches3  {



	public List<Point3> generatePoints(double boresightHA, double boresightDEC) throws EmptyCoordinatesException, CoordinateOverrideException {
		List<Point3> points = new LinkedList<Point3>();
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


		
		for(double nfb = 0; nfb <= numFB; nfb = nfb + 1){

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

				for(int n=0;n<=nsamples; n+=sampleSteps){
					double haBoresightLater = boresightHA + n * Constants.samples2secs * Constants.sec2Hrs * Constants.hrs2Deg * Constants.deg2Rad;
					double decBoresightLater = boresightDEC;

					CoordinateTO boresightLater = new CoordinateTO(haBoresightLater,decBoresightLater,null,null);
					MolongloCoordinateTransforms.skyToTel(boresightLater);

					double haLater = now.getRadHA() +  n * Constants.samples2secs * Constants.sec2Hrs * Constants.hrs2Deg * Constants.deg2Rad;
					double decLater = now.getRadDec();

					CoordinateTO later = new CoordinateTO(haLater,decLater,null,null);
					MolongloCoordinateTransforms.skyToTel(later);

					Integer nfbLater = (int) Math.round(Utilities.getFB(numFB, boresightLater.getRadMD(), later.getRadMD()));
//					System.err.println(boresightHA*Constants.rad2Hrs + " " + haBoresightLater *Constants.rad2Hrs 
//							+ " " + haLater*Constants.rad2Hrs + " " + boresight.getRadMD()*Constants.rad2Deg + " "+ mdNow*Constants.rad2Deg + " "+later.getRadMD()*Constants.rad2Deg + " " +nfbLater);
					if(nfbLater < 0 || nfbLater > numFB) unwantedPoint = true;

					Double percent = samplesInFB.getOrDefault(nfbLater, 0.0);
					percent += PercentTime;
					samplesInFB.put(nfbLater, percent);

				}
				Point3 p = new Point3();
				p.startFanBeam = p.endFanBeam = nfb;
				p.startNS = p.endNS = nsNow;
				p.uniq = false;
				if(!unwantedPoint){
					if(lastPointMap ==null){
						Long startSample = 0L;
						for(Map.Entry<Integer, Double> entry: samplesInFB.entrySet()){
							Double percent = entry.getValue();
							p.traversals.add(new Traversal2(entry.getKey()+0.0,nsDistance,startSample,(long)(percent*nsamples),percent.intValue()));
							startSample +=(int)(percent*nsamples);
						}
						if(maxTraversals < samplesInFB.size()) maxTraversals = samplesInFB.size();
						p.uniq = true;
					}
					else{
						for(Map.Entry<Integer, Double> previous : lastPointMap.entrySet()){
							Integer fb = previous.getKey();
							Double  percent = previous.getValue();

							Double percent_now = samplesInFB.getOrDefault(fb, 0.0);

							if(Math.abs(percent - percent_now) > thresholdPercent){
								Point3 lastPoint = points.get(points.size()-1);
								lastPoint.endFanBeam = nfb;
								lastPoint.endNS = nsNow;
								Long startSample = 0L;
								for(Map.Entry<Integer, Double> entry: samplesInFB.entrySet()){
									 Double pc = entry.getValue();
									p.traversals.add(new Traversal2(entry.getKey()+0.0,nsNow,startSample, startSample+ (Long)Math.round(percent*nsamples/100.0),pc.intValue()));
									startSample += (int)Math.round(percent*nsamples);

								}
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

		System.err.println(boresightHA*Constants.rad2Deg + " " + boresightDEC*Constants.rad2Deg + " " + maxTraversals);
		return points;
	}

	public static void main(String[] args) throws EmptyCoordinatesException, CoordinateOverrideException {
		SMIRF_GetUniqStitches3 gus = new SMIRF_GetUniqStitches3();
		
		Double RA = new Angle("19:07:51.0", Angle.HHMMSS).getRadianValue();
		Double LST = new Angle("21:20:43.3656", Angle.HHMMSS).getRadianValue();
		Double HA = LST -RA;
		Double DEC = new Angle("-66:00:00", Angle.DDMMSS).getRadianValue();
		List<Point3> points2 = gus.generatePoints(45*Constants.deg2Rad,DEC); 
//	
		for(Point3 p: points2){
			for(Traversal2 t: p.traversals){
//				System.err.println( " SFB " + p.startFanBeam + " EFB "+ p.endFanBeam + " SNS "+ p.startNS
//				+ " ENS "+ p.endNS + " FB "+ t.fanbeam+ " NS " + t.ns + " SS "+ t.startSample 
//				+ " ES "+ t.numSamples + " P "+  t.percent);
				System.err.println( p.startFanBeam + " "+ p.endFanBeam + " "+ String.format("%7.5f", p.startNS*Constants.rad2Deg)
				+ " "+ String.format("%7.5f", p.endNS*Constants.rad2Deg) + " "+ t.fanbeam+ " " + String.format("%7.5f", t.ns) + " "+ t.startSample 
				+ " "+ t.numSamples + " "+ t.percent);
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
