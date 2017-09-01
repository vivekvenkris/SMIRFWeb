package manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import bean.Angle;
import bean.Coords;
import bean.FluxCalibratorTO;
import bean.ObservationSessionTO;
import bean.ObservationTO;
import bean.PhaseCalibratorTO;
import bean.Pointing;
import bean.PointingTO;
import control.Control;
import exceptions.BackendException;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.EphemException;
import exceptions.InvalidFanBeamNumberException;
import exceptions.ObservationException;
import exceptions.PointingException;
import exceptions.TCCException;
import service.CalibrationService;
import service.DBService;
import service.EphemService;
import standalones.Point;
import standalones.SMIRFGalacticPlaneTiler;
import standalones.SMIRF_GetUniqStitches;
import standalones.Traversal;
import util.BackendConstants;
import util.ConfigManager;
import util.Constants;
import util.SMIRFConstants;
import util.Switches;
import util.TableBuilder;
import util.Utilities;

public class ScheduleManager implements SMIRFConstants {

//	static ObservationTO currentObservation = null;
	static String schedulerMessages = "";
//	private boolean finishCall = false;
//	private boolean terminateCall = false;
	Future<Boolean> scheduler = null;

	public void startObserving(PointingTO selectedPointing, int tobs,String observer, Boolean tccEnabled, Boolean backendEnabled, 
			Boolean  doPostObservationStuff, Boolean mdTransit, Boolean doTiming, Boolean doPulsarSearch) throws TCCException, BackendException, EmptyCoordinatesException, CoordinateOverrideException, ObservationException, InterruptedException, IOException{
		ObservationManager manager = new ObservationManager();
		ExecutorService executorService = Executors.newFixedThreadPool(4);

		Callable<Boolean> observe = new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				ObservationTO observation = new ObservationTO();
				observation.setName(selectedPointing.getPointingName());
				if(selectedPointing.getType().equals(SMIRFConstants.phaseCalibratorSymbol)) {
					observation.setBackendType(BackendConstants.globalBackend);
					observation.setObsType(BackendConstants.correlation);
				}
				else {
					observation.setBackendType(BackendConstants.smirfBackend);
					observation.setObsType(BackendConstants.tiedArrayFanBeam);
				}

				observation.setCoords(new Coords(selectedPointing));
				observation.setObserver(observer);
				observation.setTobs(tobs);
				observation.setMdTransit(mdTransit);
				observation.setDoPulsarSearch(doPulsarSearch);
				observation.setDoTiming(doTiming);
				observation.setBackendEnabled(backendEnabled);
				observation.setTccEnabled(tccEnabled);
				
				if(observation.getObsType().equals(SMIRFConstants.phaseCalibratorSymbol)) observation.setTobs(SMIRFConstants.phaseCalibrationTobs);
				if(observation.getObsType().equals(SMIRFConstants.fluxCalibratorSymbol)) observation.setTobs(SMIRFConstants.fluxCalibrationTobs);
				
				Control.setCurrentObservation(observation);
				
				waitForPreviousSMIRFSoups();
				manager.startObserving(observation);
				DBManager.addObservationToDB(observation);

				if(!observation.isPhaseCalPointing() && doPulsarSearch){
					Callable<Boolean> getStictchesReady = new Callable<Boolean>() {

						@Override
						public Boolean call() throws Exception {
							try{
								System.err.println("Sending stitches for observation" + observation.getUtc());
								return sendUniqStitchesForObservation(observation);
							} catch (Exception e) {
								e.printStackTrace();
								throw e;
							}
						}
					};
					executorService.submit(getStictchesReady);
				}
				manager.waitForObservationCompletion(observation);
				
				if(Control.isTerminateCall()) {
					Control.setTerminateCall(false);
					Control.setFinishCall(false);
					Control.setCurrentObservation(null);
					return false; 
				}

				System.err.println("observation over.");
				manager.stopObserving();


				if(doPostObservationStuff ) PostObservationManager.doPostObservationStuff(observation);

				DBManager.makeObservationComplete(observation);

				System.err.println("stopped.");	
				Control.setCurrentObservation(null);
				Control.setTerminateCall(false);
				Control.setFinishCall(false);
				return true;
			}
		};

		scheduler = executorService.submit(observe);



	}

	public void startSMIRFScheduler(List<Coords> coordsList, int obsDuration, int tobs, String observer, Boolean tccEnabled, Boolean backendEnabled, Boolean  doPostObservationStuff , ObservationSessionTO session) throws EmptyCoordinatesException, CoordinateOverrideException, PointingException, TCCException, BackendException, InterruptedException{
		ObservationManager manager = new ObservationManager();		
		ExecutorService executorService = Executors.newFixedThreadPool(8);



		Callable<Boolean> schedulerCore = new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				try{
					for(Coords coords: coordsList){

						PointingTO pointing = coords.getPointingTO();
						System.err.println("Observing: " + pointing.getPointingName());


						ObservationTO observation = new ObservationTO();
						observation.setObservingSession(session);
						observation.setName(pointing.getPointingName());
						observation.setTobs(tobs);
						

						if(coords.getPointingTO().getType().equals(SMIRFConstants.phaseCalibratorSymbol)) {

							observation.setBackendType(BackendConstants.globalBackend);
							observation.setObsType(BackendConstants.correlation);
							observation.setTobs(SMIRFConstants.phaseCalibrationTobs);
						}
						else {

							observation.setBackendType(BackendConstants.smirfBackend);
							observation.setObsType(BackendConstants.tiedArrayFanBeam);
						}

						observation.setCoords(coords);
						observation.setObserver(observer);
						observation.setMdTransit(false);
						observation.setDoPulsarSearch(true);
						observation.setDoTiming(true);
						observation.setBackendEnabled(backendEnabled);
						observation.setTccEnabled(tccEnabled);

						if(coords.getPointingTO().getType().equals(SMIRFConstants.phaseCalibratorSymbol)) observation.setTobs(SMIRFConstants.phaseCalibrationTobs);
						if(coords.getPointingTO().getType().equals(SMIRFConstants.fluxCalibratorSymbol)) observation.setTobs(SMIRFConstants.fluxCalibrationTobs);
						Control.setCurrentObservation(observation);

						try{
							waitForPreviousSMIRFSoups();
							manager.startObserving(observation);
							DBManager.addObservationToDB(observation);


						}catch (ObservationException e) {
							// add log that the pointing was not observable.
							e.printStackTrace();
							break;
						}

						Future<Boolean> sendStitches = null;

						if( ( !observation.isPhaseCalPointing() ) && doPostObservationStuff){
							Callable<Boolean> sendStitchesThread = new Callable<Boolean>() {

								@Override
								public Boolean call() throws Exception {
									try { 
										System.err.println("Sending stitches for observation - " + observation);
										return sendUniqStitchesForObservation(observation);
									}catch (Exception e) {
										e.printStackTrace();
										throw e;
									}
								}

							};
							sendStitches = executorService.submit(sendStitchesThread);

						}
						System.err.println("Observation started. Tobs = " + observation.getTobs());
						long obsTime = observation.getTobs()*1000;
						long startTime = observation.getUtcDate().getTime();
						while(true) {
							if( (new Date().getTime() - startTime) > obsTime || Control.isTerminateCall()) break; 
							Thread.sleep(200);
						}
						if(Control.isTerminateCall()) break;

						System.err.println("observation over.");

						manager.stopObserving();
						//DBManager.makeObservationComplete(observation);


						if(sendStitches !=null) sendStitches.get();

						if(doPostObservationStuff ) PostObservationManager.doPostObservationStuff(observation);

						DBManager.makeObservationComplete(observation);
						DBManager.incrementCompletedObservation(observation.getObservingSession());


						System.err.println("stopped.");

						if(Control.isFinishCall() || Control.isTerminateCall()) {
							break;
						}
					}
				}catch( Exception e){
					e.printStackTrace();
				}
				Control.setCurrentObservation(null);
				Control.setFinishCall(false);
				Control.setTerminateCall(false);
				return true;
			}
		};


		scheduler = executorService.submit(schedulerCore);



	}
	
	
	public void cancelSmirfingObservation(ObservationTO observation) throws IOException {
		
		if(observation == null || observation.getUtc() == null ) return;
		
		for(Entry<String,Map<Integer,Integer> > nepenthesServer: BackendConstants.bfNodeNepenthesServers.entrySet()){
			
			String hostname = nepenthesServer.getKey();
			
			for(Entry<Integer, Integer> bsEntry : nepenthesServer.getValue().entrySet()){
				
				System.err.println("Attempting to connect to " + nepenthesServer.getValue());
				Socket socket = new Socket();
				
				socket.connect(new InetSocketAddress(nepenthesServer.getKey(), bsEntry.getValue()),10000);
				System.err.println("Connected to " + nepenthesServer.getValue());
				
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader( new InputStreamReader(socket.getInputStream()));	
				
				out.println(ConfigManager.getSmirfMap().get("NEPENTHES_REMOVE_UTC_PREFIX") + observation.getUtc());
				
				System.err.println(in.readLine());
				
				out.flush();
				out.close();
				in.close();
				socket.close();
				
			}
		}
	}
	

	public void waitForPreviousSMIRFSoups() throws IOException, InterruptedException{
		
		 Integer maxUtcOnQueue = Integer.parseInt(ConfigManager.getSmirfMap().get("NEPENTHES_MAX_UTC_QUEUE"));

		for(Entry<String,Map<Integer,Integer> > nepenthesServer: BackendConstants.bfNodeNepenthesServers.entrySet()){

			String hostname = nepenthesServer.getKey();
			
			for(Entry<Integer, Integer> bsEntry : nepenthesServer.getValue().entrySet()){
				
				int size = 0;
				do { 
				System.err.println("Attempting to connect to " + nepenthesServer.getValue());
				Socket socket = new Socket();
				
				socket.connect(new InetSocketAddress(nepenthesServer.getKey(), bsEntry.getValue()),10000);
				System.err.println("Connected to " + nepenthesServer.getValue());
				
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader( new InputStreamReader(socket.getInputStream()));	
				
				out.println(ConfigManager.getSmirfMap().get("NEPENTHES_STATUS_PREFIX"));
				
				size = Integer.parseInt(in.readLine());
				
				System.err.println(nepenthesServer.getValue() + "has " + size + " UTCs on queue");
				
				out.flush();
				out.close();
				in.close();
				socket.close();
				
				if(size > maxUtcOnQueue) Thread.sleep(2000);
				
				}while( size > maxUtcOnQueue );
				
				
				
				
			}
		}
	}


	public boolean sendUniqStitchesForObservation(ObservationTO observation) throws EmptyCoordinatesException, CoordinateOverrideException, UnknownHostException, IOException, InvalidFanBeamNumberException{

		SMIRF_GetUniqStitches getUniqStitches = new SMIRF_GetUniqStitches();
		List<Point> points = getUniqStitches.generateUniqStitches(observation);

		System.err.println("Unique points for observation: " + points.size());

		Map<Integer, Set<Integer>> fanbeamsToTransfer = new HashMap<>();
		Set<Integer> edgeBeams = new HashSet<>();


		for(Point p: points){

			if(!p.getBeamSearcher().equals(ConfigManager.getEdgeBS())) continue;

			Integer fanbeam = p.getStartFanBeam().intValue();

			Integer bs =  ConfigManager.getBeamSearcherForFB(fanbeam);

			Set<Integer> fanbeams = fanbeamsToTransfer.getOrDefault(bs, new LinkedHashSet<>());

			fanbeams.add(fanbeam);

			fanbeamsToTransfer.put(bs, fanbeams);

			Set<Integer> traversedFBs = p.getTraversalList().stream().map(t -> t.getFanbeam().intValue()).collect(Collectors.toSet());

			for(Integer fb: traversedFBs){

				fanbeams  = fanbeamsToTransfer.getOrDefault(ConfigManager.getBeamSearcherForFB(fb), new LinkedHashSet<>());
				fanbeams.add(fb);
				fanbeamsToTransfer.put(ConfigManager.getBeamSearcherForFB(fb), fanbeams);
				edgeBeams.addAll(fanbeams);

			}

			edgeBeams.addAll(fanbeams);


		}

		for(Point p: points){

			Set<Integer> traversedFBs = p.getTraversalList().stream().map(t -> t.getFanbeam().intValue()).collect(Collectors.toSet());
			if(edgeBeams.containsAll(traversedFBs)) p.setBeamSearcher(ConfigManager.getEdgeBS());

		}

		for(Entry<String,Map<Integer,Integer> > nepenthesServer: BackendConstants.bfNodeNepenthesServers.entrySet()){

			String hostname = nepenthesServer.getKey();
			for(Entry<Integer, Integer> bsEntry : nepenthesServer.getValue().entrySet()){
				System.err.println("Attempting to connect to " + nepenthesServer.getValue());
				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(nepenthesServer.getKey(), bsEntry.getValue()),10000);
				System.err.println("Connected to " + nepenthesServer.getValue());

				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader( new InputStreamReader(socket.getInputStream()));	

				/***
				 * Format is the following:
				 * utc: <UTC_START>
				 * values
				 */
				String utcStr = ( observation.getUtc().contains(".") ) ? observation.getUtc().split("\\.")[0] : observation.getUtc();
				out.println(ConfigManager.getSmirfMap().get("NEPENTHES_UTC_PREFIX") + utcStr);

				int numPoints = 0;
				for(Point p: points) {

					if(! p.getBeamSearcher().equals(bsEntry.getKey())) continue;

					out.println(p);
					out.flush();

					numPoints++;

				}

				System.err.println(numPoints + " points for " + hostname + ":" + nepenthesServer.getValue());

				//out.println(ConfigManager.getSmirfMap().get("NEPENTHES_END"));
				out.println(ConfigManager.getSmirfMap().get("NEPENTHES_RSYNC_PREFIX"));

				Set<Integer> fanbeams = fanbeamsToTransfer.get(bsEntry.getKey());
				if(fanbeams != null && !fanbeams.isEmpty()) { 
					for(Integer fanbeam: fanbeams){

						out.println(ConfigManager.getBeamSubDir(utcStr,fanbeam));

						System.err.println(ConfigManager.getBeamSubDir(utcStr,fanbeam) );
					}
				}
				
				out.println(ConfigManager.getSmirfMap().get("NEPENTHES_SRCNAME_PREFIX"));
				out.println("SOURCE  "+ observation.getCoords().getPointingTO().getPointingName());


				out.println(ConfigManager.getSmirfMap().get("NEPENTHES_END"));

				out.flush();
				out.close();

				in.close();
				socket.close();
			}




		}
		return true;
	}


	public List<Coords> getPointingsForSession(String utc, int totalSeconds, int tobsSeconds, Comparator<Coords> comparator, Coords lastCoords, boolean fluxCalWhenever) throws EmptyCoordinatesException, CoordinateOverrideException, PointingException, TCCException{

		//List<Pointing> pointings = DBService.getAllUnobservedPointingsOrderByPriority();
		//pointingTOs = pointings.stream().filter(p -> EphemService.getHA(lst, p.getAngleRA()).getRadianValue() > SMIRFConstants.minRadHA 
//		&& EphemService.getHA(lst, p.getAngleRA()).getRadianValue() < SMIRFConstants.maxRadHA).map(p-> new PointingTO(p)).collect(Collectors.toList());

		LocalDateTime utcTime = Utilities.getUTCLocalDateTime(utc);
		Angle lst = new Angle(EphemService.getRadLMSTforMolonglo(utc),Angle.HHMMSS);


		List<PointingTO> fluxCals = DBService.getAllFluxCalibrators().stream().map(f -> new PointingTO(new FluxCalibratorTO(f))).collect(Collectors.toList());

		List<PointingTO> pointingTOs = DBManager.getAllUnobservedPointingsOrderByPriority();

		
		LinkedList<Coords> pointingList = new LinkedList<>();
		
		int obsSinceFluxcal=0;

		for(int s = 0; s<=totalSeconds; s+=tobsSeconds) {
			
			boolean doFluxCal = (obsSinceFluxcal == 10 ) ? true: false;
			
			doFluxCal = doFluxCal && fluxCalWhenever;

			List<Coords> coordsList = new ArrayList<>();

			for(PointingTO pointingTO: doFluxCal ? fluxCals : pointingTOs) {

				Coords coords = new Coords(pointingTO, lst);

				double radMD = coords.getAngleMD().getRadianValue();
				double radNS = coords.getAngleNS().getRadianValue();
				double radHA = coords.getAngleHA().getRadianValue();

				if(radMD > minRadMD && radMD < maxRadMD &&
						radNS > minRadNS && radNS < maxRadNS && 
						radHA > minRadHA && radHA < maxRadHA   ){
					coordsList.add(coords);
				}

			}

			Collections.sort(coordsList,comparator);

			coordsList.removeAll(pointingList);

			lst.addSolarSeconds(tobsSeconds);
			utcTime = utcTime.plusSeconds(tobsSeconds);

			/* remove pointings that did not last in the FOV for tobs + tslew*/
			for(Iterator<Coords> coordsIterator = coordsList.iterator(); coordsIterator.hasNext();){

				Coords coords = coordsIterator.next();
				coords.recompute(lst);

				double radMD = coords.getAngleMD().getRadianValue();
				double radNS = coords.getAngleNS().getRadianValue();
				double radHA = coords.getAngleHA().getRadianValue();
				if(!(radMD > minRadMD && radMD < maxRadMD &&
						radNS > minRadNS && radNS < maxRadNS && 
						radHA > minRadHA && radHA < maxRadHA  )){
					coordsIterator.remove();
				}

			}
			if(pointingList.isEmpty()) {

				/* This means there is no pointing to go to at UTC start */ 
				if(coordsList.isEmpty())  continue;

				coordsList = Coords.sortCoordsByNearestDistanceTo(coordsList, lastCoords);
				coordsList.get(0).setUtc(Utilities.getUTCString(utcTime));

				pointingList.add(coordsList.get(0));
				int slewtime = TCCManager.computeSlewTime(coordsList.get(0).getAngleNS().getRadianValue(), coordsList.get(0).getAngleMD().getRadianValue());
				s+=slewtime;
				lst.addSolarSeconds(slewtime);
				utcTime = utcTime.plusSeconds(slewtime);
			}
			else{
				Coords closest = null;
				Coords previous = pointingList.getLast();
				
//				Integer minSlewTime = null;
//				for(Coords now: coordsList){
//					int slewtime = TCCManager.computeSlewTime(previous.getAngleNS().getRadianValue(), previous.getAngleMD().getRadianValue(), now.getAngleNS().getRadianValue(), now.getAngleMD().getRadianValue());
//					if(closest==null || slewtime < minSlewTime){
//						minSlewTime = slewtime;
//						closest = now;
//					}
//				}
//				if(closest == null){
//					System.err.println("Nothing to observe at s = " + s/3600.0 + " hours");
//					continue;
//				}
//				if(closest!=null) {
//					closest.setUtc(Utilities.getUTCString(utcTime));
//					pointingList.add(closest);
//				}
				
				if(coordsList.isEmpty()) {
					System.err.println("Nothing to observe at s = " + s/3600.0 + " hours");
					continue;
				}

				coordsList = Coords.sortCoordsByNearestDistanceTo(coordsList, previous);
				closest = coordsList.get(0);
				closest.setUtc(Utilities.getUTCString(utcTime));
				pointingList.add(closest);
				int slewtime = TCCManager.computeSlewTime(closest.getAngleNS().getRadianValue(),closest.getAngleMD().getRadianValue(),previous.getAngleNS().getRadianValue(),previous.getAngleMD().getRadianValue());
				s+=slewtime;
				lst.addSolarSeconds(slewtime);
				utcTime = utcTime.plusSeconds(slewtime);
			}
			
			obsSinceFluxcal = doFluxCal ? 0: obsSinceFluxcal+1;

			
		}
		//for(Coords c: pointingList) System.err.print(c);
		System.err.println( pointingList.size()+ " pointings " + pointingList.size()*tobsSeconds/3600.0 + " hours " +pointingList.size()*tobsSeconds/60.0 + " min " + pointingList.size()*tobsSeconds + " seconds");
		return pointingList;


	}
	
	
	
	

	public static void main(String[] args) throws EmptyCoordinatesException, CoordinateOverrideException, PointingException, TCCException {
		ScheduleManager manager = new ScheduleManager();
		List<Coords> coords = manager.getPointingsForSession("2017-05-16-08:00:00.000", 10 * 60 * 60 , 720, Coords.compareMDNS, new Coords(new PointingTO( DBService.getPointingByID(1)), 
				new Angle(EphemService.getRadLMSTforMolonglo("2017-05-16-08:00:00.000"), Angle.HHMMSS)), true);
		
		TableBuilder tb = new TableBuilder();
		for(Coords c: coords) tb.addRow(c.toString());
		System.err.println(tb.toString());
		
		System.err.println( coords.size()+ " pointings " + coords.size()*720/3600.0 + " hours " +coords.size()*720/60.0 + " min " + coords.size()*720 + " seconds");

	}




	

	public Future<Boolean> getScheduler() {
		return scheduler;
	}

	public void setScheduler(Future<Boolean> scheduler) {
		this.scheduler = scheduler;
	}



	@Deprecated
	public boolean Calibrate(Integer calibratorID, Integer tobs, String observer) throws TCCException, BackendException, InterruptedException, IOException, EmptyCoordinatesException, CoordinateOverrideException, ObservationException, EphemException{

		PhaseCalibratorTO calibratorTO = new PhaseCalibratorTO(DBService.getCalibratorByID(calibratorID));
		ObservationTO observation = new ObservationTO();
		observation.setName(calibratorTO.getSourceName());
		PointingTO pointingTO = new PointingTO(calibratorTO);

		observation.setCoords(new Coords(pointingTO));
		observation.setTobs(tobs);
		observation.setBackendType(BackendConstants.globalBackend);
		observation.setObserver(observer);
		observation.setObsType(BackendConstants.correlation);
		observation.setMdTransit(false);

		System.err.println("starting observation...");


		ObservationManager manager = new ObservationManager();
		Control.setCurrentObservation(observation);
		manager.startObserving(observation);


		String utc = observation.getUtc();
		System.err.println("utc:" + utc);
		CalibrationService service = new CalibrationService();
		return service.Calibrate(utc, calibratorTO.getSourceName());



	}

	/*public List<Coords> getPointingsForSession2(String utc, int totalSeconds, int tobsSeconds) throws EmptyCoordinatesException, CoordinateOverrideException, PointingException{
	List<Pointing> pointings = DBService.getAllUnobservedPointingsOrderByPriority();
	List<PointingTO> pointingTOs = new LinkedList<PointingTO>();
	Angle lst = new Angle(EphemService.getRadLMSTforMolonglo(utc),Angle.HHMMSS);
	for(Pointing p : pointings) {
		if(EphemService.getHA(lst, p.getAngleRA()).getRadianValue() > SMIRFConstants.minRadHA 
				&& EphemService.getHA(lst, p.getAngleRA()).getRadianValue() < SMIRFConstants.maxRadHA){
			pointingTOs.add(new PointingTO(p));

		}
	}
	LinkedList<Coords> pointingList = new LinkedList<>();
	Integer minSlewTime = 0;

	for(int s = tobsSeconds; s<=totalSeconds; s+=tobsSeconds) {
		List<Coords> coordsList = new ArrayList<>();
		for(PointingTO pointingTO: pointingTOs) {
			Coords coords = new Coords(pointingTO, lst);
			double radMD = coords.getAngleMD().getRadianValue();
			double radNS = coords.getAngleNS().getRadianValue();
			double radHA = coords.getAngleHA().getRadianValue();
			if(radMD >= minRadMD && radMD <= maxRadMD &&
					radNS >= minRadNS && radNS <= maxRadNS && 
					radHA >= minRadHA && radHA <= maxRadHA   ){
				coordsList.add(coords);
			}
		}
		Collections.sort(coordsList,Coords.compareMDNS);
		lst.addSolarSeconds(minSlewTime + tobsSeconds);
		for(Iterator<Coords> coordsIterator = coordsList.iterator(); coordsIterator.hasNext();){
			Coords coords = coordsIterator.next();
			coords.recompute(lst);
			double radMD = coords.getAngleMD().getRadianValue();
			double radNS = coords.getAngleNS().getRadianValue();
			double radHA = coords.getAngleHA().getRadianValue();
			if(!(radMD >= minRadMD && radMD <= maxRadMD &&
					radNS >= minRadNS && radNS <= maxRadNS && 
					radHA >= minRadHA && radHA <= maxRadHA  )){
				coordsIterator.remove();
			}
		}
		coordsList.removeAll(pointingList);
		if(pointingList.isEmpty()) {
			if(coordsList.isEmpty())  {
				continue;//throw new PointingException("No pointing visible now. Please try later");
			}
			pointingList.add(coordsList.get(0));
		}
		else{
			Coords previous = pointingList.getLast();
			Coords closest = null;

			for(Coords now: coordsList){
				int slewtime = TCCManager.computeSlewTime(previous.getAngleNS().getRadianValue(), previous.getAngleMD().getRadianValue(), now.getAngleNS().getRadianValue(), now.getAngleMD().getRadianValue());
				if(closest==null || slewtime < minSlewTime){
					minSlewTime = slewtime;
					closest = now;
				}
			}
			if(closest!=null) pointingList.add(closest);
			s+=minSlewTime;
		}
	}
	//for(Coords c: pointingList) System.err.print(c);
	System.err.println( pointingList.size()+ " pointings " + pointingList.size()*tobsSeconds/3600.0 + " hours " +pointingList.size()*tobsSeconds/60.0 + " min " + pointingList.size()*tobsSeconds + " seconds");
	return pointingList;


}*/

	//	public static void main(String[] args) throws EmptyCoordinatesException, CoordinateOverrideException, PointingException, IOException, TCCException {
	//		SMIRFGalacticPlaneTiler.SMIRF_tileGalacticPlane();
	//		System.err.println("tiled..");
	//		System.exit(0);
	//		System.in.read();
	//		ScheduleManager sm = new ScheduleManager();
	//		DecimalFormat df = new DecimalFormat("00");
	//		BufferedWriter bw = new BufferedWriter(new FileWriter("/Users/vkrishnan/Desktop/dustbin/blah2"));
	//		LocalDateTime utc = LocalDateTime.parse("2016-11-01-13:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd-kk:mm:ss"));
	//		for(int i=1;;i++){
	//			System.err.print("day:" +i +" " + utc.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-kk:mm:ss"))+"\t" );
	//			List<Coords> coordsList = sm.getPointingsForSession(utc.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-kk:mm:ss")), 24*60*60,900, Coords.compareMDNS);
	//			for(Coords c: coordsList)  {
	//				DBService.incrementPointingObservations(c.getPointingTO().getPointingID());
	//				bw.write(i + " " +c.getPointingTO().getAngleRA().getDegreeValue() + " " + c.getPointingTO().getAngleDEC().getDegreeValue() + "\n" );
	//				bw.flush();
	//			}
	//			System.in.read();
	//			utc = utc.plusDays(1);
	//		}
	//
	//	}
	//

}

