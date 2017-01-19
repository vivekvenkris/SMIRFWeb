package standalones;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bean.Angle;
import bean.CoordinateTO;
import bean.Observation;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import manager.MolongloCoordinateTransforms;
import service.EphemService;
import util.Constants;
import util.SMIRFConstants;
import util.Utilities;

public class SMIRF_GetUniqStitches implements SMIRFConstants, Constants {

	public Integer getServer(double startFB, double endFB){
		Integer startServer = (int)Math.floor((startFB-1)/numBeamsPerServer);
		Integer endServer = (int)Math.floor((endFB-1)/numBeamsPerServer);

		if(startServer != endServer ) return BF08;

		return startServer;

	}
	
	public List<Point> generateUniqStitches(Observation observation) throws EmptyCoordinatesException, CoordinateOverrideException{
		return generateUniqStitches(observation.getUtc(), observation.getCoords().getPointingTO().getAngleRA(), observation.getCoords().getPointingTO().getAngleDEC(), SMIRFConstants.thresholdPercent, Math.round(observation.getTobs()/Constants.tsamp));
	}

	public List<Point> generateUniqStitches(String utcStr, Angle ra, Angle dec, double thresholdPercent, Long totalSamples) throws EmptyCoordinatesException, CoordinateOverrideException {

		Angle lst = new Angle(EphemService.getRadLMSTforMolonglo(utcStr),Angle.HHMMSS);
		Angle ha = EphemService.getHA(lst, ra);

		double boresightHA = ha.getRadianValue(); 
		double boresightDEC = dec.getRadianValue();


		List<Point> points = new LinkedList<Point>();
		int sampleSteps = 20480;
		Integer maxTraversals = 0;

		double beamWidthNS = 2.0 * Constants.toRadians;
		double beamWidthMD = 4.0 * Constants.toRadians;

		int numFB = 352;

		CoordinateTO boresight = new CoordinateTO(boresightHA,boresightDEC,null,null);
		MolongloCoordinateTransforms.skyToTel(boresight);


		for(double nfb = 1; nfb <= numFB; nfb = nfb + 1){

			double nfbNow = nfb;
			double mdDistance = Utilities.getMDDistance(nfbNow, numFB, boresight.getRadMD());
			double mdNow = boresight.getRadMD() + mdDistance;
			double nsDistance = -beamWidthNS/2.0;
			Map<Integer, Long> lastPointMap = null;
			while(nsDistance <= beamWidthNS/2.0){

				double nsNow = boresight.getRadNS() + nsDistance;

				CoordinateTO now = new CoordinateTO(null,null,nsNow,mdNow);
				MolongloCoordinateTransforms.telToSky(now);

				boolean unwantedPoint = false;
				Map<Integer, Long> samplesInFB = new LinkedHashMap<Integer, Long>();

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
				p.startFanBeam = p.endFanBeam = nfbNow;
				p.startNS = p.endNS = nsNow;
				p.uniq = false;
				p.dec = new Angle(now.getRadDec(),Angle.DDMMSS).toString();
				p.ra = EphemService.getRA(lst, new Angle(now.getRadHA(),Angle.HHMMSS)).toString();
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
								lastPoint.endFanBeam = nfbNow;
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
		String utcStr = EphemService.getUtcStringNow();
		Angle ra = new Angle("22:41:00", Angle.HHMMSS);
		Angle dec = new Angle("-52:36:00",Angle.DDMMSS);

		List<Point> points = gus.generateUniqStitches(utcStr,ra,dec ,10,Math.round(900/Constants.tsamp)); 
		for(Point p: points){
			//List<Traversal> traversals = p.traversalMap.get(Integer.parseInt(args[3]));
			for(Entry<Integer, List<Traversal>> entry :p.traversalMap.entrySet()){
				List<Traversal> traversals = entry.getValue();
				if(!entry.getKey().equals(1)) continue;
				if(traversals!=null) {
					System.err.print(p.ra + " " + p.dec + " " + p.startFanBeam + " "+ p.endFanBeam + " "+ String.format("%7.5f", p.startNS*Constants.rad2Deg) + " "+ String.format("%7.5f", p.endNS*Constants.rad2Deg) + " ");
					for(Traversal t: traversals){
						System.err.print(t.fanbeam+ " " + String.format("%7.5f", t.ns) + " "+ t.startSample + " "+ t.numSamples + " "+ t.percent + " ");
					}
					System.err.println();
				}
			}
		}

	}
}

