package manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import bean.Angle;
import bean.CoordinateTO;
import bean.Coords;
import bean.ObservationSessionTO;
import bean.ObservationTO;
import bean.PointingTO;
import bean.TCCStatus;
import control.Control;
import exceptions.BackendException;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.EphemException;
import exceptions.NoSourceVisibleException;
import exceptions.ObservationException;
import exceptions.PointingException;
import exceptions.TCCException;
import service.EphemService;
import service.TCCService;
import service.TCCStatusService;
import standalones.SMIRFTransitSimulator;
import util.BackendConstants;
import util.Constants;
import util.SMIRFConstants;
import util.TCCConstants;
@Deprecated
public class TransitScheduleManager extends ScheduleManager{
	
	static String schedulerMessages = "";
	Future<Boolean> scheduler = null;
	Future<Boolean> sendStitches = null;
	ExecutorService executorService = Executors.newFixedThreadPool(16);

	
	public void start(int obsDuration, 
			int tobs, String observer, Boolean tccEnabled, Boolean backendEnabled,
			 Boolean doPulsarSearch, Boolean  doPostObservationStuff, 
			 ObservationSessionTO observationSessionTO, Double nsSpeed, Double nsOffsetDeg){
		
		System.err.println("Starting Transit scheduler.");
		
		Future<Boolean> start = null;
		
		Callable<Boolean> startObservations = new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				
				try{
				
				
				
				startDynamicTransit(obsDuration, tobs, observer, tccEnabled, backendEnabled, doPulsarSearch, doPostObservationStuff, observationSessionTO, nsSpeed, nsOffsetDeg);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}
		};
		
		executorService.submit(startObservations);
		
		
		
	}
	
	/**
	 * If the East and West arm are pointing in different directions, make it come together before it tries to observe anything.
	 * To the future Vivek: This is a bit crude, I know. This is because it might become unnecessarily complicated to precisely
	 * observe a transit if I assume the mean of both the arms and calculate transit times.
	 
	 * @throws TCCException
	 * @throws CoordinateOverrideException
	 * @throws EmptyCoordinatesException
	 */
	public void syncArmsToMedian() throws TCCException, CoordinateOverrideException, EmptyCoordinatesException{
		
		TCCStatus status = Control.getTccStatus();
		
		
		System.err.println("Checking NS positions of drives East = " + status.getNs().getEast().getTilt().getDegreeValue() + " "
				 + status.getNs().getWest().getTilt().getDegreeValue());

		
		if( (status.getNs().getEast().getTilt().getDegreeValue() - status.getNs().getWest().getTilt().getDegreeValue() ) > 
		TCCConstants.OnSourceThresholdRadNS ){
			
			
			TCCService service = TCCService.createTccInstance();
			
			Coords coords = new Coords(status);
			
			System.err.println("Driving telescope to mean position: " + coords.getAngleNS());
			
			service.pointNS(coords.getAngleNS(), TCCConstants.BOTH_ARMS);
			
			
		}
	}
	
	public void startDynamicTransit(int obsDuration, 
			int tobs, String observer, Boolean tccEnabled, Boolean backendEnabled,
			 Boolean doPulsarSearch, Boolean  doPostObservationStuff, 
			 ObservationSessionTO observationSessionTO, Double nsSpeed, Double nsOffsetDeg) 
					throws EmptyCoordinatesException, CoordinateOverrideException, 
					PointingException, TCCException, BackendException, InterruptedException, 
					NoSourceVisibleException, ExecutionException, ObservationException, IOException, EphemException{
		
		System.err.println("Starting Dynamic Transit");
		
		syncArmsToMedian();
		
		ObservationManager manager = new ObservationManager();		
		List<PointingTO> today = new ArrayList<PointingTO>();
		
//		Callable<Boolean> schedulerCore = new Callable<Boolean>() {
//			@Override
//			public Boolean call() throws Exception {
				
				Future<Boolean> previousObservation = null;

				long timeElapsed = 0;
				long startTime = new Date().getTime();
				
				while(!Control.isFinishCall() && (timeElapsed = (new Date().getTime() - startTime)/1000 ) < obsDuration ){
					
					PointingTO pointingTO = getNextPointing(tobs,nsSpeed, today, previousObservation != null ? tobs : 0);
					
					
					/**
					 * Wait for previous observation to complete, if there is one.
					 */
					
					if(previousObservation != null) {
						System.err.println("Waiting for previous observation to complete");
						previousObservation.get();
					}
					System.err.println("Observing: " + pointingTO.getPointingName());
					Control.setCurrentObservation(null);
					

					if(Control.isFinishCall()) break;
					
					/**
					 * 1. Get local coordinates of the next pointing for LST = now.
					 * 2. Get the corresponding HA for MD = 2 degrees. 
					 * 3. if HA_Now - HA_2deg > a minute, then do FRB transit for (HA_Now - HA_2Deg)
					 */
					
					Coords coords = new Coords(pointingTO, EphemService.getAngleLMSTForMolongloNow());
					CoordinateTO coordinateTO = new CoordinateTO(null, null, coords.getAngleNS(), new Angle(-Constants.RadMolongloMDBeamWidthForFB, Angle.DEG));
					MolongloCoordinateTransforms.telToSky(coordinateTO);
					
					double halfPowerHA = coordinateTO.getAngleHA().getDecimalHourValue();
					double haNow = coords.getAngleHA().getDecimalHourValue();
					double oneMinute = 1/60.0;
					
					System.err.println( "half power HA: " +  halfPowerHA  + " HA now: " + haNow + " diff: " +  ( haNow - halfPowerHA)*3600 + "seconds");
					/**
					 * if the pointing has passed the meridian, immediately start the observation
					 * if it hasn't, and hasn't crossed HPBW, start FRB transit if there is atleast 1 minute of observation
					 * else just sleep 
					 */
					if(haNow < 0 && Math.abs(halfPowerHA) < Math.abs(haNow)){
						
						double differenceHours =  Math.abs(Math.abs(haNow) - Math.abs(halfPowerHA));
						
						if( differenceHours > oneMinute ){
							
							doInterimFRBTransit((int)(differenceHours* Constants.hrs2Sec), coordinateTO.getAngleDEC(), manager);

						}
						else{
							System.err.println("Sleeping for " +differenceHours*Constants.hrs2Sec + " secs as time less than one minute for obs_start");
							Thread.sleep((long) (differenceHours*Constants.hrs2Sec*1000));
						}
					}
					

					ObservationTO observation = new ObservationTO(coords,observationSessionTO,tobs, observer,
							BackendConstants.smirfBackend,BackendConstants.tiedArrayFanBeam, SMIRFConstants.PID,nsOffsetDeg,
							true, true, doPulsarSearch, true, true);
					Control.setCurrentObservation(observation);
					 

					try{
						 if(doPulsarSearch) waitForPreviousSMIRFSoups();
						manager.startObserving(observation);
						DBManager.addObservationToDB(observation);


					}catch (ObservationException | EphemException e) {
						// add log that the pointing was not observable.
						e.printStackTrace();
						throw e;
					}
					
					/**
					 *  If pulsar search is enabled, do it.
					 */
					sendStitches = null;

					if( doPulsarSearch ){
						
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
					
					
					Callable<Boolean> manageObservation = new Callable<Boolean>() {

						@Override
						public Boolean call() throws Exception {
							
							System.err.println("Observation started.");
							manager.waitForObservationCompletion(observation);
							manager.stopObserving();
							if(Control.isTerminateCall()) {
								return true;
							}

							if( doPulsarSearch ) sendStitches.get();
							if(doPostObservationStuff ) PostObservationManager.doPostObservationStuff(observation);

							DBManager.makeObservationComplete(observation);
							DBManager.incrementCompletedObservation(observation.getObservingSession());

							return true;
						}
					};
					
					previousObservation = executorService.submit(manageObservation);
					
					
				}
				
			//	return true;
				
//			}
//		};
				Control.reset();
				System.err.println(" Dynamic transit ended.");
	}

	public void doInterimFRBTransit(int tobs, Angle dec, ObservationManager manager) throws EmptyCoordinatesException, CoordinateOverrideException, TCCException, BackendException, ObservationException, InterruptedException, EphemException{
		
		PointingTO pointingTO = new PointingTO(EphemService.getAngleLMSTForMolongloNow(), dec,"FRB Transit",transitPointingSymbol);
		
		ObservationTO observation = new ObservationTO(new Coords(pointingTO, EphemService.getAngleLMSTForMolongloNow()),null,
											tobs, "IFTM", BackendConstants.smirfBackend,
											BackendConstants.tiedArrayFanBeam, SMIRFConstants.interimFRBTransitPID, 0.0, 
											true, true, false, true, true);
		
		
		manager.startObserving(observation);
		
		if(Control.isTerminateCall()) {
			System.err.println("Terminate call initated. Exiting IFTM");
			return;
		}
		
		if(observation.getUtc() != null) DBManager.addObservationToDB(observation);

		
		System.err.println("FRB Transit Observation started. Running for tobs = " + tobs);
		Control.setCurrentObservation(observation);

		manager.waitForObservationCompletion(observation);
		
		manager.stopObserving();
		Control.setCurrentObservation(null);

		
		if(Control.isTerminateCall()) {
			System.err.println("Terminate call initated. Exiting IFTM");
			return;
		}

		DBManager.makeObservationComplete(observation);
		
	}
	
	
	
	
	
	public PointingTO getNextPointing(int tobs, Double nsSpeed, List<PointingTO> today, int offsetSeconds) throws CoordinateOverrideException, EmptyCoordinatesException, TCCException, NoSourceVisibleException{
		
		/**
		 * Get all pointings from database
		 */
		List<PointingTO> gridPoints = DBManager.getAllPointings();
		
		System.err.println("All pointings: " + gridPoints.size());
		
		/**
		 *  Get the present telescope position
		 */
		Coords telescopePosition = new Coords(Control.getTccStatus());
		CoordinateTO initTelPosition = new CoordinateTO(null, null, telescopePosition.getAngleNS().getRadianValue(), 0.0);
		
		System.err.println("Telescope is here: " + telescopePosition.getAngleNS());

		/**
		 * Get positions of pointings (coords) in local coordinates for LST = now, sorted by increasing distance from Meridian.
		 */
		
		Angle initLST = EphemService.getAngleLMSTForMolongloNow();
		initLST.addSolarSeconds(offsetSeconds);
		
		List<Coords> coords = getCoordsListForLST(gridPoints, initLST, initTelPosition, nsSpeed );
		
		if(coords.isEmpty()) throw new NoSourceVisibleException("No pointings visible to observe");
		
		System.err.println("coords: " + coords.size());
		
//		for(Coords c: coords) System.err.println(c.getPointingTO().getPointingName() + " " + c.getAngleHA().getDecimalHourValue());
		
		/**
		 * Get the number of times the pointing that was least observed, was observed for.
		 */
		
		int minimumObs = Collections.min(coords.stream().map(f -> f.getPointingTO().getNumObs()).collect(Collectors.toSet()));
		int index = 1;
		
		coords = coords.stream().filter( 
				f -> {
					if(Control.getCurrentObservation() == null ) return true;
					if(f.getPointingTO().equals(Control.getCurrentObservation().getCoords().getPointingTO())) return false;
					return true;
					
				}).collect(Collectors.toList());
		/**
		 * Get the nearest pointing that is observed the least number of times.
		 */
		for(Coords c: coords){
			
			if(c.getPointingTO().getNumObs().equals(minimumObs)) break;
			index++;
		}
		
		/**
		 * Shortlist all the pointings that are enroute to the least observed pointing, to check if they can be observed in the mean time. 
		 * Sort it by number of times it has been observed
		 */
		
		List<Coords> shortlisted = coords.subList(0, index).stream().sorted(
				Comparator.comparing(f -> ((Coords)f).getPointingTO().getNumObs())).collect(Collectors.toList());
		
		if(shortlisted.isEmpty()) throw new NoSourceVisibleException("No pointings visible to observe");
		
		
		/**
		 * First of the shortlist is the nearest, least observed pointing. Get its HA. Call it leastObservedHA
		 * Compute the slew time to it (for NS)
		 */
		
		Coords leastObserved = shortlisted.get(0);
		
		System.err.println("Least observed: " + leastObserved.getPointingTO().getPointingName());
		
		double leastObservedHA = Math.abs(initLST.getDecimalHourValue() - leastObserved.getPointingTO().getAngleRA().getDecimalHourValue());

		double slewTime = SMIRFTransitSimulator.computeNSSlewTime(leastObserved.getAngleNS(), initTelPosition.getAngleNS())* Constants.sec2Hrs;
		
		Coords interim = null;
		
		/**
		 * If wait time is longer than tobs, then try if you can go do another observation and come back to the least observed.
		 */
		
		System.err.println(leastObserved.getPointingTO().getPointingName() + "  takes " + slewTime + " to go to.");
		System.err.println("leastObservedHA - slewTime = " + (leastObservedHA - slewTime)*60 + "minutes");
		
		if ( (leastObservedHA - slewTime) > tobs* Constants.sec2Hrs ) {
			
			for( int i=1; i< shortlisted.size(); i++){
				
				Coords iCoords = shortlisted.get(i);
				System.err.println("Considering " + iCoords.getPointingTO().getPointingName());
				/**
				 * Don't re observe the pointings on the same day.
				 * Don't observe the current pointing again. It makes you look like a fool.
				 */
				if(today.contains(iCoords.getPointingTO())) continue;
				if(Control.getCurrentObservation() != null && iCoords.getPointingTO().equals(Control.getCurrentObservation().getCoords().getPointingTO())) continue;
				
				
				double slewTimeToInterimInHours = SMIRFTransitSimulator.computeNSSlewTime(iCoords.getAngleNS(), initTelPosition.getAngleNS()) * Constants.sec2Hrs;
				double slewTimeFromInterimInHours = SMIRFTransitSimulator.computeNSSlewTime(leastObserved.getAngleNS(), iCoords.getAngleNS())* Constants.sec2Hrs;
				double timeToDetourInHours = slewTimeFromInterimInHours + slewTimeToInterimInHours + tobs * Constants.sec2Hrs;
				
				/**
				 * First pointing in the loop that can do this is the pointing that has been the second-least observed.
				 */
				System.err.println(iCoords.getPointingTO().getPointingName() + " takes " + slewTimeToInterimInHours + "to go"
						+ " and " + slewTimeFromInterimInHours + "to come back. By then least observed pointing will be in"
								+ " HA = " + (leastObservedHA - timeToDetourInHours) );

				if( (leastObservedHA - (timeToDetourInHours) ) > tobs * Constants.sec2Hrs){
						interim = iCoords;
						break;
						
					}
					
				}
				
			}
		
		/**
		 * If there is an interim pointing, ignore the least observed and observe the interim. Least observed will be picked up again in the next iteration.
		 */
		
		Coords next = (interim != null)? interim : leastObserved;
		today.add(next.getPointingTO());
		
		System.err.println("Next: " + next.getPointingTO().getPointingName() + " " + next.getAngleHA().getDecimalHourValue());
			
		return next.getPointingTO();
		
		
	}
	
	
public static List<Coords> getCoordsListForLST(List<PointingTO> gridPoints, Angle initLST, CoordinateTO initTelPosition, Double nsSpeed){
		
		return gridPoints
				
		.stream()
		/**
		 * Map each pointingTO to a Coords object for the input LST
		 */
		
		.map(p -> {
				try {
					return new Coords(p, initLST);
				} catch (EmptyCoordinatesException | CoordinateOverrideException e) {
					return null;
				} 
			})
		
		/**
		 *  Remove non null 
		 */
		
		.filter(x -> x!=null)
		
		/**
		 *  Sort to descending angle of hour angle
		 */
		
		.sorted(Comparator.comparing(c -> ((Coords)c).getAngleHA().getDecimalHourValue()).reversed())
		
		/**
		 * Filter only the pointings that have not transited yet ( HA > 0 ) and will not have transited when we go there ( adding slew time ) 
		 */
				
		.filter(coord -> {
			
			Coords c = ((Coords)coord);
			
			
			Angle lst = new Angle(initLST.getRadianValue(), Angle.HHMMSS);
			
			lst.addSolarSeconds( TCCManager.computeNSSlewTime(c.getAngleNS(), initTelPosition.getAngleNS(), nsSpeed ));
			
			double ha = lst.getDecimalHourValue() - c.getPointingTO().getAngleRA().getDecimalHourValue();
									
			return ha < 0;
		})
		
		.collect(Collectors.toList());
		
	}



}
