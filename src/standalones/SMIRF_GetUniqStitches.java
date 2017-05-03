package standalones;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import bean.Angle;
import bean.CoordinateTO;
import bean.ObservationTO;
import bean.TBSourceTO;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import manager.MolongloCoordinateTransforms;
import service.EphemService;
import util.ConfigManager;
import util.Constants;
import util.SMIRFConstants;
import util.Utilities;

public class SMIRF_GetUniqStitches implements SMIRFConstants, Constants {
	
	public Integer getServer(double startFB, double endFB){
		Integer startServer = ConfigManager.getServerNumberForFB((int) startFB);
		Integer endServer =  ConfigManager.getServerNumberForFB((int) endFB);
		if(startServer != endServer ) return ConfigManager.getServerNumberForServerName(ConfigManager.getEdgeNode());

		return startServer;

	}

//	public Integer getServer(double startFB, double endFB){
//		Integer startServer = (int)Math.floor((startFB-1)/numBeamsPerServer);
//		Integer endServer = (int)Math.floor((endFB-1)/numBeamsPerServer);
//		if(startServer != endServer ) return BF08;
//
//		return startServer;
//
//	}

	public List<Point> generateUniqStitches(ObservationTO observation) throws EmptyCoordinatesException, CoordinateOverrideException{
		List<Point> points =  generateUniqStitches(observation.getUtc(), observation.getCoords().getPointingTO().getAngleRA(), observation.getCoords().getPointingTO().getAngleDEC(), 
				SMIRFConstants.thresholdPercent, fft_size, Constants.tsamp);
		
		for(TBSourceTO to : observation.getTiedBeamSources()) {
			Point point = getPointForSkyPosition(observation.getUtc(), observation.getAngleRA(), observation.getAngleDEC(), to.getAngleRA(), to.getAngleDEC(), fft_size, Constants.tsamp);
			points.add(point);
		}
		return points;
	}

	public Point getPointForSkyPosition(String utcStr, Angle boresightRA, Angle boresightDeclination, Angle pointRA, Angle pointDEC, Long totalSamples, double tsamp)throws EmptyCoordinatesException, CoordinateOverrideException{

		double samples2secs = tsamp;
		Point p = new Point();
		Angle lst = new Angle(EphemService.getRadLMSTforMolonglo(utcStr),Angle.HHMMSS);
		Angle ha = EphemService.getHA(lst,boresightRA);

		double boresightHA = ha.getRadianValue(); 
		double boresightDEC = boresightDeclination.getRadianValue();


		int numFB = ConfigManager.getNumFanBeams();

		CoordinateTO boresight = new CoordinateTO(boresightHA,boresightDEC,null,null);
		MolongloCoordinateTransforms.skyToTel(boresight);

		CoordinateTO now = new CoordinateTO(EphemService.getHA(lst,pointRA),pointDEC,null,null);
		MolongloCoordinateTransforms.skyToTel(now);

		double nfbNow = (int)Utilities.getFB(numFB, boresight.getRadMD(), now.getRadMD());
		Double nsNow = now.getRadNS();

		Map<Integer, Long> samplesInFB = new LinkedHashMap<Integer, Long>();

		int sampleSteps = 1024;

		for(int n=0;n<totalSamples; n+=sampleSteps){

			double haBoresightLater = boresightHA + n * samples2secs * Constants.sec2Hrs * Constants.hrs2Deg * Constants.deg2Rad;
			double decBoresightLater = boresightDEC;

			CoordinateTO boresightLater = new CoordinateTO(haBoresightLater,decBoresightLater,null,null);
			MolongloCoordinateTransforms.skyToTel(boresightLater);

			double haLater = now.getRadHA() +  n * samples2secs * Constants.sec2Hrs * Constants.hrs2Deg * Constants.deg2Rad;
			double decLater = now.getRadDec();

			CoordinateTO later = new CoordinateTO(haLater,decLater,null,null);
			MolongloCoordinateTransforms.skyToTel(later);

			Integer nfbLater = (int) Math.round(Utilities.getFB(numFB, boresightLater.getRadMD(), later.getRadMD()));
			if(nfbLater <= 0 || nfbLater > numFB) return null;

			Long numSamples = samplesInFB.getOrDefault(nfbLater, 0L);
			numSamples += sampleSteps;
			samplesInFB.put(nfbLater, numSamples);

		}

		p.startFanBeam = p.endFanBeam = nfbNow;
		p.startNS = p.endNS = nsNow;
		p.uniq = false;
		p.dec = new Angle(now.getRadDec(),Angle.DDMMSS).toString();
		p.ra = EphemService.getRA(lst, new Angle(now.getRadHA(),Angle.HHMMSS)).toString();
		Long startSample = 0L;
		List<Traversal> traversals = p.traversalList;
		for(Map.Entry<Integer, Long> entry: samplesInFB.entrySet()){
			Long numSamples = entry.getValue();
			Long numSamps = (startSample + numSamples) > totalSamples ? (totalSamples-startSample): numSamples;
			Double pc =  100*(numSamples+0.0)/totalSamples;
			traversals.add(new Traversal(entry.getKey()+0.0,0.0,startSample,numSamps,pc.intValue()));
			startSample += numSamples;
		}
		return p;

	}


	public List<Point> generateUniqStitches(String utcStr, Angle ra, Angle dec, double thresholdPercent, Long totalSamples, double tsamp) throws EmptyCoordinatesException, CoordinateOverrideException {
		
		double samples2secs = tsamp;
		Angle lst = new Angle(EphemService.getRadLMSTforMolonglo(utcStr),Angle.HHMMSS);
		Angle ha = EphemService.getHA(lst, ra);

		double boresightHA = ha.getRadianValue(); 
		double boresightDEC = dec.getRadianValue();


		List<Point> points = new LinkedList<Point>();


		int sampleSteps = 20480;
		Integer maxTraversals = 0;

		double beamWidthNS = 2.0 * Constants.toRadians;
		double beamWidthMD = 4.0 * Constants.toRadians;

		int numFB = ConfigManager.getNumFanBeams();
		int startFB = 2;

		CoordinateTO boresight = new CoordinateTO(boresightHA,boresightDEC,null,null);
		MolongloCoordinateTransforms.skyToTel(boresight);


		for(double nfb = startFB; nfb <= numFB; nfb = nfb + 1){

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

					double haBoresightLater = boresightHA + n * samples2secs * Constants.sec2Hrs * Constants.hrs2Deg * Constants.deg2Rad;
					double decBoresightLater = boresightDEC;

					CoordinateTO boresightLater = new CoordinateTO(haBoresightLater,decBoresightLater,null,null);
					MolongloCoordinateTransforms.skyToTel(boresightLater);

					double haLater = now.getRadHA() +  n * samples2secs * Constants.sec2Hrs * Constants.hrs2Deg * Constants.deg2Rad;
					double decLater = now.getRadDec();

					CoordinateTO later = new CoordinateTO(haLater,decLater,null,null);
					MolongloCoordinateTransforms.skyToTel(later);

					Integer nfbLater = (int) Math.round(Utilities.getFB(numFB, boresightLater.getRadMD(), later.getRadMD()));
					if(nfbLater < startFB || nfbLater > numFB) unwantedPoint = true;

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

				p.server = getServer(Collections.min(samplesInFB.keySet()),Collections.max(samplesInFB.keySet()));

				if(!unwantedPoint){
					if(lastPointMap ==null){
						Long startSample = 0L;
						List<Traversal> traversals =  p.traversalList;
						for(Map.Entry<Integer, Long> entry: samplesInFB.entrySet()){
							Long numSamples = entry.getValue();
							Long numSamps = (startSample + numSamples) > totalSamples ? (totalSamples-startSample): numSamples;
							Double percent =  100*(numSamples+0.0)/totalSamples;
							traversals.add(new Traversal(entry.getKey()+0.0,nsDistance,startSample,numSamps,percent.intValue()));
							startSample += numSamples;
						}
						//p.traversalMap.put(server, traversals);
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
								List<Traversal> traversals = p.traversalList;
								for(Map.Entry<Integer, Long> entry: samplesInFB.entrySet()){
									Long numSamples = entry.getValue();
									Long numSamps = (startSample + numSamples) > totalSamples ? (totalSamples-startSample): numSamples;
									Double pc =  100*(numSamples+0.0)/totalSamples;
									traversals.add(new Traversal(entry.getKey()+0.0,nsDistance,startSample,numSamps,pc.intValue()));
									startSample += numSamples;
								}
								//p.traversalMap.put(server, traversals);
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


	public static void main(String[] args) throws EmptyCoordinatesException, CoordinateOverrideException, IOException{
		
		//args = new String("-u 2017-03-17-00:00:21 -r 17:45:00 -d -30:00:00 -n 13732910 -t 327.68e-6 -o stderr ").split(" ");

		CommandLine line;
		CommandLineParser parser = new DefaultParser();

		Options options = new Options();
		Option utcOption = new Option("u", "utc", true, " UTC start of observation [required]");
		Option raBoresightOption = new Option("r", "ra", true, " boresight RA in hh:mm:ss [required]");
		Option decBoresightOption = new Option("d", "dec", true, " boresight DEC in dd:mm:ss [required]");
		Option nsampOption = new Option("n", "nsamp", true, " nsamples [required]");
		Option tsampOption = new Option("t", "tsamp", true, " tsamp in seconds [required]");
		Option outPrefixOption = new Option("o", "out_prefix", true, " if out suffix is stderr, out on sys.err else, out file = <out_prefix>.<node>");
		Option pointRA =  new Option("R", "pt_ra", true, " UTC start of observation");
		Option pointDEC =  new Option("D", "pt_dec", true, " UTC start of observation");

		options.addOption(utcOption);
		options.addOption(raBoresightOption);
		options.addOption(decBoresightOption);
		options.addOption(nsampOption);
		options.addOption(tsampOption);
		options.addOption(outPrefixOption);
		options.addOption(pointRA);
		options.addOption(pointDEC);


		try {

			if(args.length==0) {
				help(options);
				return;
			}
			line = parser.parse(options, args);

			if( !hasOption(line, utcOption) || !hasOption(line, raBoresightOption) ||  !hasOption(line, decBoresightOption) || 
					!hasOption(line, nsampOption) ||  !hasOption(line, tsampOption) || !hasOption(line, outPrefixOption) ){
				help(options);
				return;
			}

			if( ( !hasOption(line, pointRA) && hasOption(line, pointDEC) ) || ( hasOption(line, pointRA) && !hasOption(line, pointDEC) ) ) {
				help(options);
				return;
			}

			boolean point = hasOption(line, pointRA) && hasOption(line, pointDEC);

			String outPrefix = getValue(line, outPrefixOption);
			String utcStr  =  ( getValue(line, utcOption).split(".").length > 1) ? getValue(line, utcOption)  : getValue(line, utcOption) + ".000";
			String raStr   =  getValue(line,raBoresightOption);
			String decStr  =  getValue(line,decBoresightOption);
			long nsamp     =  Long.parseLong(getValue(line,nsampOption));
			double tsamp   =  Double.parseDouble(getValue(line, tsampOption));
			
			Angle raBoresight = new Angle(raStr, Angle.HHMMSS);
			Angle decBoresight = new Angle(decStr,Angle.DDMMSS);
			
			if(point){
				Point pt = new SMIRF_GetUniqStitches().getPointForSkyPosition(utcStr,raBoresight, decBoresight, 
						new Angle( getValue(line, pointRA),Angle.HHMMSS), new Angle(getValue(line, pointDEC), Angle.DDMMSS), nsamp, tsamp);
				
				PrintStream ps = (outPrefix.equalsIgnoreCase("stderr")) ? System.err : new PrintStream(outPrefix+ ".point");
				
				ps.println(pt);
				ps.flush();
				ps.close();
				return;
			}
			
			List<PrintStream> printStreams = new ArrayList<>();
			List<Integer> servers = Arrays.asList(new Integer[]{0,1,2,3,4,5,6,7,8});

			for(Integer server: servers) {
				
				if(outPrefix.equalsIgnoreCase("stderr")) printStreams.add(System.err);
				
				else printStreams.add(new PrintStream(outPrefix+ ".mpsr-bf0" +server));
				
			}

			List<Point> pointsList = new SMIRF_GetUniqStitches().generateUniqStitches( utcStr, raBoresight, decBoresight, thresholdPercent, nsamp,tsamp);
			for(Point pt: pointsList) {
				
				if(!servers.contains(pt.server)) continue;
				
				PrintStream ps = printStreams.get(servers.indexOf(pt.server));
				ps.println(pt);
				ps.flush();
				
			}
			

		} catch (ParseException e) {
			e.printStackTrace();
		}


	}

	public static void help(Options options){
		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("Main", options);
	}

	public static String getValue(CommandLine line, Option option){
		return line.getOptionValue(option.getOpt());
	}

	public static boolean hasOption(CommandLine line, Option option){
		return line.hasOption(option.getOpt());
	}
}


























//	public static void main2(String[] args) throws EmptyCoordinatesException, CoordinateOverrideException, IOException  {
//		String utcStr =  "2017-03-11-00:21:21.000";
//		Angle ra = new Angle("17:41:60.0", Angle.HHMMSS);
//		Angle dec = new Angle("-30:24:60.0",Angle.DDMMSS);
//
//		List<Point> points = new SMIRF_GetUniqStitches().generateUniqStitches(utcStr,ra,dec ,10,1376256L); 
//
//		for( int i=0;i<=8;i++){
//			String out = "2psrs.uniq.points."+i;
//			BufferedWriter bw  = new BufferedWriter( new FileWriter(out));
//			for(Point p: points){
//				List<Traversal> traversals = p.traversalList;
//				if(traversals!=null) {
//					bw.write(p.ra + " " + p.dec + " " + p.startFanBeam + " "+ p.endFanBeam + " "+ String.format("%7.5f", p.startNS*Constants.rad2Deg) + " "+ String.format("%7.5f", p.endNS*Constants.rad2Deg) + " ");
//					for(Traversal t: traversals){
//						bw.write(t.fanbeam+ " " + String.format("%7.5f", t.ns) + " "+ t.startSample + " "+ t.numSamples + " "+ t.percent + " ");
//					}
//					bw.newLine();
//				}
//			}
//			bw.flush();
//			bw.close();
//		}
//	}
//
//	public static void main3(String[] args) throws EmptyCoordinatesException, CoordinateOverrideException, IOException {
////		String utcStr =  "2017-02-22-07:07:53.000";
////		Angle ra = new Angle("17:41:60.0", Angle.HHMMSS);
////		Angle dec = new Angle("-30:24:60.0",Angle.DDMMSS);
//		
//		String utcStr =  "2017-03-29-06:00:23.000";
//		Angle ra = new Angle("04:37:15.9", Angle.HHMMSS);
//		Angle dec = new Angle("-47:15:09.1",Angle.DDMMSS);
//		String out = "0437.uniq";
//		BufferedWriter bw  = new BufferedWriter( new FileWriter(out));
//		List<Point> points = new SMIRF_GetUniqStitches().generateUniqStitches(utcStr,ra,dec ,10,1376256L); 
//		for(Point p: points){
//			List<Traversal> traversals = p.traversalList;
//			//for(Entry<Integer, List<Traversal>> entry :p.traversalMap.entrySet()){
//			//List<Traversal> traversals = entry.getValue(); //.get(SERVER);
//			if(traversals!=null) {
//				bw.write(p.ra + " " + p.dec + " " + p.startFanBeam + " "+ p.endFanBeam + " "+ String.format("%7.5f", p.startNS*Constants.rad2Deg) + " "+ String.format("%7.5f", p.endNS*Constants.rad2Deg) + " ");
//				for(Traversal t: traversals){
//					bw.write(t.fanbeam+ " " + String.format("%7.5f", t.ns) + " "+ t.startSample + " "+ t.numSamples + " "+ t.percent + " ");
//				}
//				bw.newLine();
//			}
//			//}
//		}
//
//		bw.flush();
//		bw.close();
//
//	}
//}
//
