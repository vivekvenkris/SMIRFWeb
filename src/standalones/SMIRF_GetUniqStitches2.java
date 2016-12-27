package standalones;

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
class Point2{
	Double fanbeam;
	Double ns;
	Map<Integer, Double> timeInFB;
	boolean uniq;
}

public class SMIRF_GetUniqStitches2  {



	public List<Point2> generatePoints(double boresightHA, double boresightDEC) throws EmptyCoordinatesException, CoordinateOverrideException {
		List<Point2> points = new LinkedList<Point2>();
		int tobs = 900;
		double thresholdPercent = 5;
		int timeSteps = 10;
		Double PercentTime = timeSteps*100.0/tobs;
		int maxFBTraversal = 10;

		double beamWidthNS = 2.0 * Constants.toRadians;
		double beamWidthMD = 4.0 * Constants.toRadians;
		int numFB = 352;
		double deltaMD = beamWidthMD/numFB;


		CoordinateTO boresight = new CoordinateTO(boresightHA,boresightDEC,null,null);
		MolongloCoordinateTransforms.skyToTel(boresight);

		for(double nfb = 1; nfb <= numFB; nfb = nfb + 1){

			double nfbNow = nfb;

			double mdDistance = (nfb-(numFB/2 +1)) * deltaMD;
			double mdNow = boresight.getRadMD() + mdDistance;
			double nsDistance = -beamWidthNS/2.0;
			Map<Integer, Double> lastPointMap = null;
			List<Point2> subPoints = new LinkedList<Point2>();
			while(nsDistance <= beamWidthNS/2.0){

				double nsNow = boresight.getRadNS() + nsDistance;

				CoordinateTO now = new CoordinateTO(null,null,nsNow,mdNow);
				MolongloCoordinateTransforms.telToSky(now);

				Map<Integer, Double> timeInFB = new LinkedHashMap<Integer, Double>(maxFBTraversal);

				boolean unwantedPoint = false;

				for(int t=0;t<tobs; t+=timeSteps){

					double haBoresightLater = boresightHA + t * Constants.sec2Hrs * Constants.hrs2Deg * Constants.deg2Rad;
					double decBoresightLater = boresightDEC;

					CoordinateTO boresightLater = new CoordinateTO(haBoresightLater,decBoresightLater,null,null);
					MolongloCoordinateTransforms.skyToTel(boresightLater);

					double haLater = now.getRadHA() +  t * Constants.sec2Hrs * Constants.hrs2Deg * Constants.deg2Rad;
					double decLater = now.getRadDec();

					CoordinateTO later = new CoordinateTO(haLater,decLater,null,null);
					MolongloCoordinateTransforms.skyToTel(later);

					Integer nfbLater = (int) Math.round(Utilities.getFB(numFB, boresightLater.getRadMD(), later.getRadMD()));

					if(nfbLater < 0 || nfbLater > numFB) unwantedPoint = true;

					Double percent = timeInFB.getOrDefault(nfbLater, 0.0);
					percent += PercentTime;
					timeInFB.put(nfbLater, percent);

				}
				Point2 p = new Point2();
				p.fanbeam = nfb;
				p.ns = nsDistance;
				p.timeInFB = timeInFB; 
				p.uniq = false;
				if(!unwantedPoint){
					if(lastPointMap ==null){
						lastPointMap = timeInFB;
						p.uniq = true;
					}
					else{
						for(Map.Entry<Integer, Double> previous : lastPointMap.entrySet()){
							Integer fb = previous.getKey();
							Double  percent = previous.getValue();

							Double percent_now = timeInFB.getOrDefault(fb, 0.0);

							if(Math.abs(percent - percent_now) > thresholdPercent){
								lastPointMap = timeInFB;
								p.uniq = true;
								break;
							}



						}


					}
					if(p.uniq) {
						if(!subPoints.isEmpty())
							points.add(subPoints.get(subPoints.size()/2));
						else
							points.add(p);
						subPoints.clear();

					}
					subPoints.add(p);

				}


				nsDistance += 23*Constants.arcSec2Deg*Constants.deg2Rad;



			}
			if(points.size() ==1){
				points.get(0).ns = 0.0;
			}
			


		}


		return points;
	}

	public static void main(String[] args) throws EmptyCoordinatesException, CoordinateOverrideException {
		SMIRF_GetUniqStitches2 gus = new SMIRF_GetUniqStitches2();
		Double RA = new Angle("19:07:51.0", Angle.HHMMSS).getRadianValue();
		Double LST = new Angle("21:20:43.3656", Angle.HHMMSS).getRadianValue();
		Double HA = LST -RA;
		Double DEC = new Angle("-60:00:00", Angle.DDMMSS).getRadianValue();
		List<Point2> points = gus.generatePoints(HA, DEC); 

		for(Point2 p: points){
			for(Map.Entry<Integer, Double> entry : p.timeInFB.entrySet()){
				System.err.println( p.fanbeam + " "+  String.format("%7.5f", p.ns*Constants.rad2Deg)  + " "+ entry.getKey() + " " 
						+ String.format("%7.5f", entry.getValue()) + " ");
			}
		}



		//launch(args);
	}


}
