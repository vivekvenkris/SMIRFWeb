package manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jfree.data.time.Millisecond;

import bean.Angle;
import bean.CoordinateTO;
import bean.Coords;
import bean.ObservationTO;
import bean.PointingTO;
import bean.TBSourceTO;
import bean.TCCStatus;
import control.Control;
import exceptions.BackendException;
import exceptions.CoordinateOverrideException;
import exceptions.DriveBrokenException;
import exceptions.DriveIsSlowException;
import exceptions.EmptyCoordinatesException;
import exceptions.EphemException;
import exceptions.InvalidFanBeamNumberException;
import exceptions.ObservationException;
import exceptions.TCCException;
import mailer.Mailer;
import service.BackendService;
import service.EphemService;
import service.TCCService;
import service.TCCStatusService;
import standalones.Point;
import standalones.SMIRF_GetUniqStitches;
import util.BackendConstants;
import util.ConfigManager;
import util.Constants;
import util.SMIRFConstants;
import util.Switches;
import util.Utilities;

public class ObservationManager {

	public synchronized void startObserving(ObservationTO observation) throws TCCException, BackendException, EmptyCoordinatesException, CoordinateOverrideException, ObservationException, InterruptedException, EphemException{
		startObserving(observation, observation.getTccEnabled(), observation.getBackendEnabled());
	}

	private synchronized void  startObserving(ObservationTO observation, boolean tccEnabled, boolean backendEnabled) throws TCCException, BackendException, InterruptedException, EmptyCoordinatesException, CoordinateOverrideException, ObservationException, EphemException{

		Future<Boolean> backendReady = null;
		Future<Boolean> tccReady = null;

		if(tccEnabled) {
			if( !observable(observation,EphemService.getUtcStringNow())) {
				System.err.println("not observable.");
				throw new ObservationException("cant observe this source. Source not visible. Reqd Coords= HA:" + observation.getCoords().getAngleHA() + " NS:" 
						+ observation.getCoords().getAngleNS() + " MD:"+ observation.getCoords().getAngleMD());
			}

			TCCService       tccService =     TCCService.createTccInstance();
			TCCStatusService tccStatusService = new TCCStatusService();

			CoordinateTO coordinates = new CoordinateTO(observation);
			MolongloCoordinateTransforms.skyToTel(coordinates);

			Callable<Boolean> getTCCReady = new Callable<Boolean>() {

				@Override
				public Boolean call() throws TCCException, InterruptedException, ObservationException{

					System.err.println("Starting TCC.");
					tccService.pointAndTrackSource(observation);

					long startTime = System.currentTimeMillis();

					System.err.println("Computing slew time.");
					long slewTime = TCCManager.computeSlewTime(coordinates.getRadNS(), coordinates.getRadMD())*1000;
					long maxSlewTime = (long)4* slewTime; // to milliseconds
					
					
					System.err.println(" max slew time = " + maxSlewTime/1000.0 + "seconds = " + maxSlewTime/60000.0 + "minutes");

					long elapsedTime = 0L;
					Thread.sleep(5000);
					int weeCount = 0;

					while(true){
						
						if(Thread.currentThread().isInterrupted()) {
							tccService.stopTelescope();
							return false;
						}
						
						if(!tccStatusService.isTelescopeDriving() ) break;

						elapsedTime = (new Date()).getTime() - startTime;

						System.err.print("\r slewing for the past "+ elapsedTime/1000.0 + " seconds...");

						if(elapsedTime > maxSlewTime) {
							if(!tccStatusService.isTelescopeDriving()) break;
							throw new DriveIsSlowException("Drive taking longer than expected to go to source. nominal slew Time = " + (int)(slewTime/1000.0) + " seconds, but it has already "
									+ "taken " + (int)(elapsedTime/1000) + " seconds.");
						}
						
						try{
						Thread.sleep(2000);
						}catch (InterruptedException e) {
							tccService.stopTelescope();
							return false;
						}
						
						if(Control.isTerminateCall()){
							System.err.println("Terminate call initated. Exiting TCC wait");
							tccService.stopTelescope();
							return false;
						}
					}
					System.err.println("TCC reached.");
					if(!observation.isSurveyPointing()) {
						System.err.println("Stopping the telescope as the observation is not a SMIRF obs.");
						tccService.stopTelescope();
					}
					return true;
				}


			};

			tccReady =  Control.getExecutorService().submit(getTCCReady);


		}

		if(backendEnabled) {
			BackendService   backendService = BackendService.createBackendInstance();
			BackendService   backendStatus =  BackendService.createBackendStatusInstance();

			Callable<Boolean> getBackendReady = new Callable<Boolean>() {

				@Override
				public Boolean call() throws BackendException {
					
					if(Switches.simulate || true) return true;

					boolean isON = backendStatus.isON();
					if(isON){
						System.err.println("backend is ON");
						boolean isidle = backendStatus.isIdle();
						if(!isidle) backendService.stopBackend();
					}
					String currentBackend;
					currentBackend = backendService.getCurrentBackend();
					System.err.println("Current Backend:" + currentBackend);
					System.err.println("Required backend:" + observation.getBackendType());
					if(!currentBackend.equals(observation.getBackendType())){
						System.err.println("backend change needed..");
						if(isON) {
							backendService.shutDownBackend();
						}
						backendService.changeBackendConfig(observation.getBackendType());
					}
					System.err.println("Booting backend..");
					backendService.bootUpBackend();
					System.err.println("backend booted..");
					return true;
				}
			};
			backendReady = Control.getExecutorService().submit(getBackendReady);
		}

		if(backendEnabled) {
			Boolean backendResult  = null;
			try {
				backendResult = backendReady.get();
			} catch (InterruptedException | ExecutionException e) {
				Throwable t = e.getCause();
				if(t instanceof BackendException){
					throw (BackendException)t;
				}
				e.printStackTrace();
				Mailer.sendEmail(e);

			}
			//if(backendResult == null || backendResult.equals(false)) return;

		}
		System.err.println("Backend Ready");

		if(tccEnabled)  { 
			Boolean tccResult = null;
			try {
				 tccResult = tccReady.get();
			} catch (InterruptedException | ExecutionException e) {
				Throwable t = e.getCause();
				if(t instanceof DriveBrokenException) {
					System.err.println("Caught Drive Broken Exception on observation manager");
					throw (DriveBrokenException)t;
				}
				else if(t instanceof TCCException){
					throw (TCCException)t;
				}
				e.printStackTrace();
				Mailer.sendEmail(e);
			}
			//if(tccResult == null || tccResult.equals(false)) return;

		}
		
		System.err.println("TCC Ready");

		if(Control.isTerminateCall()){
			System.err.println("Terminate call initated. Exiting Observation Manager");
			return;
		}
		
		Double dec = observation.getAngleDEC().getRadValue();
		
		Double transitTime = Math.abs(Constants.RadMolongloMDBeamWidth * Constants.rad2Hrs /Math.cos(dec) * Constants.hrs2Sec);
		System.err.println("Transit time at this dec=" + dec * Constants.rad2Deg + " is ~" + transitTime/60.0 + " minutes. tobs requested: " + observation.getTobs() );

		
		if(!observation.getCoords().getPointingTO().getType().equals(SMIRFConstants.phaseCalibratorSymbol) && transitTime > observation.getTobs()/3.0) {
			observation.setTiedBeamSources(getTBSourcesForObservation(observation,BackendConstants.maximumNumberOfTB));
		}
		System.err.println(observation.getTiedBeamSources());
		if(backendEnabled) {
			System.err.println("starting backend..");
			BackendService   backendService = BackendService.createBackendInstance(); 
			backendService.startBackend(observation);
		}
		System.err.println("starting observing..");
	}


	
	public synchronized void waitForObservationCompletion(ObservationTO observation) throws InterruptedException, TCCException, BackendException, ObservationException{
		
		long startTime = observation.getUtcDate().getTime();

		if(observation != null) {
			
			System.err.println("Checking for backend safety = 60 seconds into observation");
			
			String utc = observation.getUtc();
			LocalDateTime now = EphemService.getUTCTimestamp();
			LocalDateTime utcStart = Utilities.getUTCLocalDateTime(utc);
			
			
			
			long milliSeconds = 60 * 1000 - (long)(Utilities.getTimeDifferenceInDays(now, utcStart) * Constants.day2Hrs * Constants.hrs2Sec * 1000.0);
			

			if (milliSeconds > 0) {
				Thread.sleep(milliSeconds);
			}
			
			
		}
		

		
		long obsTime = observation.getTobs()*1000;
		while(true) {
			
			if( (new Date().getTime() - startTime) > obsTime || Control.isTerminateCall()) break; 
			Thread.sleep(2000);
			
			if(Thread.currentThread().isInterrupted()) break;
			
			if(!observation.isSurveyPointing()) continue;
			
			/**
			 * This will throw a Drive Broken Exception if not tracking.
			 */
			
			DriveBrokenException exception = null;
			for(int i=0; i<20; i++ ){
				
				try{
					
					Control.getTccStatus().isTelescopeTracking();
					exception = null;
					break;
				}catch (DriveBrokenException e) {
					System.err.println("Telescope not tracking target.Wee count = " + i);
					System.err.println(e.getMessage());
					exception = e;
				}
				Thread.sleep(2000);
				
			}
			if(exception != null) {
				System.err.println("I think the telescope is not tracking. Stopping observation to check.");
				this.stopObserving();
				System.err.println("Backend cool down.");
				Thread.sleep(BackendConstants.minTimeBetweenObsInSecs * 1000);
				throw exception;
			}
		}
		
	} 
	
	
	public boolean stopObserving() throws TCCException, BackendException, InterruptedException{
		return stopObserving(true, true);

	}

	public boolean stopObserving(boolean tccEnabled, boolean backendEnabled) throws TCCException, BackendException, InterruptedException{

		TCCService       tccService =     TCCService.createTccInstance();
		TCCStatusService tccStatusService = new TCCStatusService();
		BackendService   backendService = BackendService.createBackendInstance();

		if(tccEnabled) {
			try{
				tccService.stopTelescope();
				System.err.println("all success!");
				int i=0;
				while(!Control.getTccStatus().isTelescopeIdle()){
					Thread.sleep(1000);
					if(i++ > 10) throw new TCCException("TCC doesn't stop after a stop command");
				};

			}catch (TCCException  e) {
				backendService.stopBackend();
				throw e;
			}catch (InterruptedException e) {
				backendService.stopBackend();
				tccService.stopTelescope();
				throw e;
			}
		}
		if(backendEnabled) {
			try{
				backendService.stopBackend();
			}catch (BackendException e) {
				tccService.stopTelescope();
				throw e;
			}
		}
		return true;

	}
/**
 * Outdated. do not use
 * @param observation
 * @return
 * @throws TCCException
 * @throws BackendException
 * @throws InterruptedException
 */
	@Deprecated
	public boolean stopObserving2(ObservationTO observation) throws TCCException, BackendException, InterruptedException{

		TCCService       tccService =     TCCService.createTccInstance();
		TCCStatusService tccStatusService = new TCCStatusService();
		BackendService   backendService = BackendService.createBackendInstance();

		long startTime = System.currentTimeMillis(); //LocalDateTime.parse(observation.getUtc(), DateTimeFormatter.ofPattern("yyyy-MM-dd-kk:mm:ss")).atZone(ZoneId.of("Australia/Melbourne")).toInstant().toEpochMilli();;

		long obsTime = observation.getTobs()*1000; // in milliseconds
		long elapsedTime = 0L;

		try{
			while(true){
				elapsedTime = (new Date()).getTime() - startTime;
				if(elapsedTime > obsTime) {
					backendService.stopBackend();
					tccService.stopTelescope();
					System.err.println("all success!");
					System.err.println(Thread.currentThread().getName());
					return true;

				}
				if(!tccStatusService.getTelescopeStatus().isTelescopeTracking()) break;
				Thread.sleep(2000);

			}

			return false;
		}catch (BackendException e) {
			tccService.stopTelescope();
			throw e;
		}catch (TCCException  e) {
			backendService.stopBackend();
			throw e;
		}catch (InterruptedException e) {
			backendService.stopBackend();
			tccService.stopTelescope();
			throw e;
		}
	}

	public static void main(String[] args) throws EmptyCoordinatesException, CoordinateOverrideException, EphemException, DriveBrokenException, InterruptedException {
	
		ObservationTO to = new ObservationTO();
		to.setUtc(Utilities.getUTCString(EphemService.getUTCTimestamp().plusSeconds(120)));
	}

	public List<TBSourceTO> getTBSourcesForObservation(ObservationTO observation, Integer max) throws EmptyCoordinatesException, CoordinateOverrideException, EphemException{
	
		System.err.println("Getting TB sources for observation");
		
		//List<TBSourceTO> tbSources = PSRCATManager.getTbSources();
	/**
	 *  As if 14 Jan 2018, we will use only the Timing programme list of pulsars.
	 */
		List<TBSourceTO> tbSources = PSRCATManager.getTimingProgrammeSources();
		List<TBSourceTO> shortListed = new ArrayList<>();
		
		Coords pointingCoords = observation.getCoords();
		
		double radPTRA = pointingCoords.getPointingTO().getAngleRA().getRadianValue();
		double radPTDEC = pointingCoords.getPointingTO().getAngleDEC().getRadianValue();
		
		/**
		 * If it is a flux cal observation, make sure we add the flux cal as a TB source. Or else what is the point?
		 * But what is the point of anything? what is the length of a piece of string?
		 * In the end, everything will be engulfed by the sun anyway, including this brilliantly written code.
		 * 
		 */
		if(observation.getCoords().getPointingTO().getType().equals(SMIRFConstants.fluxCalibratorSymbol)){
			
			TBSourceTO tbSourceTO = PSRCATManager.getTBSouceByName(observation.getCoords().getPointingTO().getPointingName());
			shortListed.add(tbSourceTO);
			
		}
		
		if(observation.getCoords().getPointingTO().getType().equals(SMIRFConstants.pulsarPointingSymbol)) {
			TBSourceTO tbSourceTO = PSRCATManager.getTBSouceByName(observation.getCoords().getPointingTO().getPointingName().replaceAll("PSR_", ""));
			
			if(tbSourceTO == null) {
				System.err.println("Cannot find the pulsar for pointing " + observation.getCoords().getPointingTO().getPointingName());
				Mailer.sendEmail("INFO: Tried to put TB for " + observation.getCoords().getPointingTO().getPointingName() + " and failed");
			}
			if(!shortListed.contains(tbSourceTO))shortListed.add(tbSourceTO);
		}
		
		
		List<TBSourceTO> associatedPulsars = observation.getCoords().getPointingTO().getAssociatedPulsars();
		
		System.err.println("Associated pulsars of this pointing"+ observation.getCoords().getPointingTO().getPointingName() +":"  + associatedPulsars);
		
		if(associatedPulsars != null ) {
			for(TBSourceTO f: associatedPulsars) {
				if(!shortListed.contains(f)) shortListed.add(f);
			}
		}
		
		
		/**
		 * Refresh coordinates for LST = now
		 */
		if(observation.getMdTransit()) {
			System.err.println("Changing coords to MD = 0 as this is a transit observation.");
			//EphemService.getAngleLMSTForMolongloNow()
			pointingCoords = new Coords(pointingCoords, pointingCoords.getAngleLST(), true);
		}
		else pointingCoords.recomputeForNow();
		
		Coords coords = pointingCoords;
		
		/**
		 * first check if the source is within the 4 degree circle of the pointing center in RA/DEC
		 * if so, coordinate transform it and check if it is  within the primary beam in NS/MD coordinates
		 */

		
		for(TBSourceTO tbSourceTO: tbSources){
						
			double radTBRA = tbSourceTO.getAngleRA().getRadianValue();
			double radTBDEC = tbSourceTO.getAngleDEC().getRadianValue();
			
			boolean withinCircle = Utilities.isWithinCircle(radPTRA, radPTDEC, radTBRA, radTBDEC, Constants.RadMolongloMDBeamWidth/2.0);
			
			
			if(withinCircle){

				TBSourceTO tempTBSourceTO = EphemService.precessTBSourceToNow(tbSourceTO);

				CoordinateTO tbCoordinateTO = new CoordinateTO( EphemService.getHA(coords.getAngleLST(), tempTBSourceTO.getAngleRA()), tempTBSourceTO.getAngleDEC(), null, null);			
				MolongloCoordinateTransforms.skyToTel(tbCoordinateTO);
				
				/**
				 * Check if NS_diff < 2, MD_diff < 4, within ellipse 
				 * To future Vivek: Sorry for being so crude. 
				 */
				
		
				
				boolean withinNS = Math.abs(coords.getAngleNS().getRadianValue() - tbCoordinateTO.getAngleNS().getRadianValue() ) <= Constants.RadMolongloNSBeamWidth/2.0;
				
				boolean withinMD = Math.abs(coords.getAngleMD().getRadianValue() - tbCoordinateTO.getAngleMD().getRadianValue() ) <= Constants.RadMolongloMDBeamWidth/2.0;
				
				boolean withinEllipse = Utilities.isWithinEllipse(coords.getAngleNS().getRadianValue(), coords.getAngleMD().getRadianValue(), 
						tbCoordinateTO.getAngleNS().getRadianValue(), tbCoordinateTO.getAngleMD().getRadianValue(), 
						Constants.RadMolongloNSBeamWidth/2.0, Constants.RadMolongloMDBeamWidth/2.0);
				
				
				if( withinNS && withinMD && withinEllipse){
					
					if(!shortListed.contains(tbSourceTO)) {
						shortListed.add(tbSourceTO);
						System.err.println("Adding " + tbSourceTO.getPsrName() + "at " + 
								Constants.rad2Deg * Utilities.distance(coords.getAngleNS(), coords.getAngleMD(), 
										tbCoordinateTO.getAngleNS(), tbCoordinateTO.getAngleMD()) + " Degrees");
						
					}
					
				}

				
			}
			
		}

		System.err.println("TB sources in the beam: " + shortListed);
		
		shortListed.sort(Comparator.comparing(TBSourceTO::getPriority).reversed()
						//.thenComparing(Comparator.comparing(TBSourceTO::getFluxAt843MHz).reversed())
						.thenComparing(Comparator.comparing(f -> {
							TBSourceTO t = ((TBSourceTO)f);
							Coords c = null;
							try {
								c = new Coords(new PointingTO(t),observation.getCoords().getAngleLST());
							} catch (EmptyCoordinatesException | CoordinateOverrideException e) {
								e.printStackTrace();
							}
							return Math.abs(c.getAngleHA().getDecimalHourValue());
						}))
						.thenComparing(a -> {
							try {
								return ((TBSourceTO)a).getAbsoluteDistanceFromBoresight(coords);
							} catch (EmptyCoordinatesException | CoordinateOverrideException e) {
								e.printStackTrace();
								return 1e10;
							}
						})
						);
		
		
		

		System.err.println("Sorted TB sources in the beam: " + shortListed);
		if(max !=null) {
			System.err.println("Chosen TB sources:  " + shortListed.subList(0, Math.min(BackendConstants.maximumNumberOfTB, shortListed.size())));
			shortListed = shortListed.subList(0, Math.min(BackendConstants.maximumNumberOfTB, shortListed.size()));
		}


		return shortListed;
	}
	
	public void waitForPreviousSMIRFSoups() throws IOException, InterruptedException{
		
		 Integer maxUtcOnQueue = Integer.parseInt(ConfigManager.getSmirfMap().get("NEPENTHES_MAX_UTC_QUEUE"));

		for(Entry<String,Map<Integer,Integer> > nepenthesServer: BackendConstants.bfNodeNepenthesServers.entrySet()){

			String hostname = nepenthesServer.getKey();
			
			boolean mailed = false;
			
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
				if(size > maxUtcOnQueue && !mailed){
					
					String mail = "Problem with Nepenthes server: BS" + nepenthesServer.getValue()
							+ "\n";
					
					socket = new Socket();
					socket.connect(new InetSocketAddress(nepenthesServer.getKey(), bsEntry.getValue()),10000);	
					out = new PrintWriter(socket.getOutputStream(), true);
					in = new BufferedReader( new InputStreamReader(socket.getInputStream()));	
					out.println(ConfigManager.getSmirfMap().get("NEPENTHES_UTC_LIST_PREFIX"));
					mail += in.readLine() + "\n";
					
					System.err.println("Sending email about problem.");
					
					Mailer.sendEmail(mail);
					
					out.flush();
					out.close();
					in.close();
					socket.close();
					
					mailed = true;
				}
				
				if(size > maxUtcOnQueue) Thread.sleep(30000);
				
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
		
		if(Control.isTerminateCall()) {
			System.err.println("Terminate call initated. Exiting Observation Manager");
			return false;
		}


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





	@Deprecated
	public static boolean observable(ObservationTO observation) throws TCCException, EmptyCoordinatesException, CoordinateOverrideException{

		Angle HANow = observation.getHANow();
		if(Math.abs(HANow.getDecimalHourValue())>6){ 
			System.err.println("HA >6: "+ HANow.getDecimalHourValue());
			return false;
		}

		CoordinateTO coordsNow  = new CoordinateTO(HANow.getRadianValue(), observation.getCoords().getPointingTO().getAngleDEC().getRadianValue(),null,null);
		MolongloCoordinateTransforms.skyToTel(coordsNow);
		//System.err.println( "*************" +observation.getCoords().getPointingTO().getPointingName() +" " +coordsNow.getRadMD()*Constants.rad2Deg  + " " +coordsNow.getRadNS()*Constants.rad2Deg );
		if(coordsNow.getRadMD() < SMIRFConstants.minRadMD || coordsNow.getRadMD() > SMIRFConstants.maxRadMD) return false;

		int slewTime = TCCManager.computeSlewTime(coordsNow.getRadNS(), coordsNow.getRadMD());
		int obsTime = observation.getTobs();

		int totalSecs = obsTime + slewTime + 60;
		Angle HAend = observation.getHAAfter(totalSecs);
		if(Math.abs(HAend.getDecimalHourValue())>6) return false;

		CoordinateTO coordsEnd  = new CoordinateTO(HAend.getRadianValue(), observation.getCoords().getPointingTO().getAngleDEC().getRadianValue(),null,null);
		MolongloCoordinateTransforms.skyToTel(coordsEnd);
		if(coordsEnd.getRadMD() < SMIRFConstants.minRadMD || coordsEnd.getRadMD() > SMIRFConstants.maxRadMD) return false;

		return true;
	}

	
	
	
	
	
	public static boolean observable(ObservationTO observation, String utc) throws TCCException, EmptyCoordinatesException, CoordinateOverrideException{
		Angle HANow = observation.getHAForUTC(utc);
		if(Math.abs(HANow.getDecimalHourValue())>6){ 
			System.err.println("cant observe -----------   HA >6: "+ HANow.getDecimalHourValue());
			return false;
		}

		CoordinateTO coordsNow  = new CoordinateTO(HANow.getRadianValue(), observation.getCoords().getPointingTO().getAngleDEC().getRadianValue(),null,null);
		MolongloCoordinateTransforms.skyToTel(coordsNow);
		//System.err.println( "*************" +observation.getCoords().getPointingTO().getPointingName() +" " +coordsNow.getRadMD()*Constants.rad2Deg  + " " +coordsNow.getRadNS()*Constants.rad2Deg );
		if(coordsNow.getRadMD() < SMIRFConstants.minRadMD || coordsNow.getRadMD() > SMIRFConstants.maxRadMD) {
			System.err.println("cant observe ---------- coords begin > limit md = " + coordsNow.getRadMD());
			return false;
		}

		int slewTime = TCCManager.computeSlewTime(coordsNow.getRadNS(), coordsNow.getRadMD());
		int obsTime = observation.getTobs();

		int totalSecs = obsTime + slewTime + 60;
		Angle HAend = observation.getHAForUTCPlusOffset(utc,totalSecs);
		if(Math.abs(HAend.getDecimalHourValue())>6) {
			System.err.println(" cant observe ---------- HA end  > 6" + + HAend.getDecimalHourValue());
			return false;
		}

		CoordinateTO coordsEnd  = new CoordinateTO(HAend.getRadianValue(), observation.getCoords().getPointingTO().getAngleDEC().getRadianValue(),null,null);
		MolongloCoordinateTransforms.skyToTel(coordsEnd);
		if(coordsEnd.getRadMD() < SMIRFConstants.minRadMD || coordsEnd.getRadMD() > SMIRFConstants.maxRadMD) {
			System.err.println(" cant observe ---------- coords end > limit md = "  + coordsEnd.getRadMD());
			return false;
		}

		return true;
	}



}
