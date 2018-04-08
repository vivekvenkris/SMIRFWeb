package manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import bean.Angle;
import bean.CoordinateTO;
import bean.Coords;
import bean.ObservationTO;
import bean.PointingTO;
import bean.UserInputs;
import control.Control;
import exceptions.BackendException;
import exceptions.CoordinateOverrideException;
import exceptions.DriveBrokenException;
import exceptions.EmptyCoordinatesException;
import exceptions.EphemException;
import exceptions.NoSourceVisibleException;
import exceptions.ObservationException;
import exceptions.ScheduleEndedException;
import exceptions.SchedulerException;
import exceptions.TCCException;
import mailer.Mailer;
import service.EphemService;
import util.BackendConstants;
import util.Constants;
import util.SMIRFConstants;

/**
 * A base level implementation of the scheduling algorithm for transit observations. Any new scheduler should override the next() and init() method
 * from the "Schedulable" interface  and provide its implementation.
 * 
 * the init() method must set this.userInputs = userInputs;
 * 
 * @author Vivek Venkatraman Krishnan
 *
 */
public abstract class TransitScheduler extends AbstractScheduler {
	

	
public static Schedulable createInstance(String schedulerType) throws SchedulerException{
	
		Schedulable	scheduler = Control.getScheduler();
		
		if(scheduler != null) {
		
			if(schedulerType.equals(scheduler.getType())) return scheduler;
		
			else throw new SchedulerException("Scheduler already running. Type: " + scheduler.getType());
		}
		
		/**
		 * Added this in case we will get the type of scheduler to use from the user, in the future.
		 */
		
		switch (schedulerType) {
		
		case dynamicTransitScheduler:
			
			scheduler = new DynamicTransitScheduler();
			break;
			
		case staticTransitScheduler:
			
			scheduler = new StaticTransitScheduler();
			break;
			
		case candidateConfirmationTransitScheduler:
			
			scheduler = new CandidateConfirmationTransitScheduler();
			break;

		case pulsarDynamicTransitScheduler:
			
			scheduler = new PulsarDynamicTransitScheduler();
			break;
			
		case interfacedDynamicScheduler:
			
			scheduler = new InterfacedDynamicScheduler();
			break;	
			
		default:
			throw new SchedulerException("No such scheduler type.");
			
		}
		
		//Control.setScheduler(scheduler);
		
		return scheduler;
	}
	
	@Override
	public void start() throws TCCException, CoordinateOverrideException, EmptyCoordinatesException, InterruptedException, ExecutionException, ObservationException, IOException, NoSourceVisibleException, SchedulerException, BackendException, EphemException {
		try {
			
			Control.setScheduler(this);
			
			TCCManager.syncArmsToMedian();
						
			long startTime = new Date().getTime();
			
			boolean end = false;
			PointingTO next =null;
			
			while(!Control.isFinishCall() ){
				
				if(previousObservation != null){
					
					System.err.println("Waiting for previous observation to complete");
					previousObservation.get();
					if(end) break;
					Control.setCurrentObservation(null);
					previousObservation = null;

				}
				

				/**
				 * quit if obs will take more than session time.
				 */
				// To do: Add t_slew to this calculation 
				long timeElapsedAtObsEnd = (new Date().getTime() - startTime)/1000 + userInputs.getTobsInSecs();
				if(userInputs.getSessionTimeInSecs() != null && timeElapsedAtObsEnd > userInputs.getSessionTimeInSecs() ) break;
				
				try {
				 next = next();
				}catch (ScheduleEndedException e) {
					System.err.println(e.getMessage());
					Mailer.sendEmail(e);
					end = true;
					break;
				}
				
				
				System.err.println("Now observing: " + next.getPointingName());

				if(Control.isFinishCall()) break;
				
				/**
				 * Point to the NS of the next pointing, but do not track
				 * The point NS code is still not working, I think I have a captialisation issue in the XML I send to the TCC.
				 * Should check at some point, but for now, lets just track a LST, DEC instead. The FRB transit will have no difference as
				 * delaytracking = false.
				 */
				//TCCManager.pointNS(next);
				
				double waitTimeInHours = getWaitTimeInHours(next);
				

				/**
				 * if the pointing has passed the meridian, immediately start the observation
				 * if it hasn't, and hasn't crossed HPBW, start FRB transit if there is atleast 1 minute of observation
				 * else just sleep 
				 */
				double minObsGapinHours = BackendConstants.minTimeBetweenObsInSecs * Constants.sec2Hrs;
				
				System.err.println("Wait time in hours:" + waitTimeInHours);
				System.err.println("Should wait " + waitTimeInHours * Constants.hrs2Sec + "seconds for this to start");
				
				if(waitTimeInHours > 1) {
					Mailer.sendEmail("Warning: Performing Interim FRB transits for  " + waitTimeInHours + " while waiting for the pointing" + next.getPointingName());
					System.err.println("Sent warning email");
				}
				
				if(waitTimeInHours > minObsGapinHours) doInterimFRBTransit( (int) (waitTimeInHours * Constants.hrs2Sec), next);
				else if(waitTimeInHours > 0) 	{
					
					System.err.println("Waiting for pointing to enter the beam. Sleeping for " + waitTimeInHours*Constants.hrs2Sec*1000 );
					
					Thread.sleep((long) (waitTimeInHours*Constants.hrs2Sec*1000));
				}
				
				if(Control.isTerminateCall()) break;
				
				Coords coords = new Coords(next, EphemService.getAngleLMSTForMolongloNow());
				
				if(coords.getAngleMD().getRadianValue() > Constants.radMDToEndObs){
					
					System.err.println("Skipping pointing " + coords.getPointingTO().getPointingName() + " as it is already gone out of beam.");
					
					String mailText = "Skipping pointing " + coords.getPointingTO().getPointingName() + 
							"as md_start " + coords.getAngleMD().getRadianValue() + ">" + Constants.radMDToEndObs + "\n"
							+ "This happened when lst = " + EphemService.getAngleLMSTForMolongloNow() + "\n";
					Mailer.sendEmail(new SchedulerException(mailText));
					
					continue;
				}
				
				/**
				 * Set PID and pulsar search flag from the source type and name.
				 */
				String pid = SMIRFConstants.PID;
				userInputs.setDoPulsarSearching(true);
				if(!coords.getPointingTO().isSMIRFPointing()) {
					/**
					 * Altering user input is bad. change this at some point, future Vivek.
					 */
					userInputs.setDoPulsarSearching(false);
					
					if(coords.getPointingTO().isPulsarPointing()) pid = SMIRFConstants.nonSMIRFPointingPID;
					else if(coords.getPointingTO().isTransitPointing()) pid = SMIRFConstants.interimFRBTransitPID;
					else if(coords.getPointingTO().isFRBFollowUpPointing()) pid = SMIRFConstants.frbFollowUpPID;
					
				}
				
				ObservationTO observation = new ObservationTO(coords, userInputs,BackendConstants.smirfBackend,BackendConstants.tiedArrayFanBeam, pid);
				Control.setCurrentObservation(observation);
		
				if(userInputs.getDoPulsarSearching()) observationManager.waitForPreviousSMIRFSoups();
				observationManager.startObserving(observation);
				
				if(Control.isTerminateCall()) {
					System.err.println("Terminate call initated. Exiting Transit Scheduler");

					break;
				}

				DBManager.addObservationToDB(observation);
				addToSession(observation.getCoords().getPointingTO());


				
				/**
				 *  If pulsar search is enabled, do it.
				 */
				sendStitches = null;

				if( userInputs.getDoPulsarSearching() )sendStitches = Control.getExecutorService().submit(sendStitchesThread);
				previousObservation = Control.getExecutorService().submit(manageObservation);

			}
			
			System.err.println(" Dynamic transit ended.");
			
		} finally {
			Control.setCurrentObservation(null);
			System.err.println("current observation set to null");
		}
		System.err.println("Exiting transit scheduler");
	}
	
	private void doInterimFRBTransit(Integer tobs, PointingTO next) throws ObservationException, EmptyCoordinatesException, CoordinateOverrideException, 
	InterruptedException, TCCException, BackendException, EphemException{
		
		Integer numObs = (int) (tobs*1.0 / SMIRFConstants.maxFRBTrtansitTOBS);
		Integer tobsDifference = tobs - numObs*SMIRFConstants.maxFRBTrtansitTOBS;
		
		System.err.println("Doing FRB transits in the mean time.");
		
		int obs = 1; 
		for(;obs <= numObs; obs++ ){
			
			System.err.println("Attempting FRB transit obs:" + obs);
			
			Integer thisTobs = SMIRFConstants.maxFRBTrtansitTOBS - BackendConstants.minTimeBetweenObsInSecs;
			doInterimFRBTransit(thisTobs,obs,next);
			
			if(Control.isFinishCall() || Control.isTerminateCall()) break;

			
			System.err.println("Backend rest for 60 seconds.");
			Thread.sleep(BackendConstants.minTimeBetweenObsInSecs * 1000);
			
			
		}
		
		if(tobsDifference > BackendConstants.minTobs ) doInterimFRBTransit(tobsDifference,obs,next);
		else Thread.sleep(tobsDifference * 1000);

		
	}
	
	private void doInterimFRBTransit(Integer tobs,Integer obsNo, PointingTO next) 
			throws ObservationException, EmptyCoordinatesException, CoordinateOverrideException, 
					InterruptedException, TCCException, BackendException, EphemException{
		
		PointingTO pointingTO = new PointingTO(EphemService.getAngleLMSTForMolongloNow(), next.getAngleDEC(),"IFTB_"+ next.getPointingName() +"_" +obsNo,transitPointingSymbol);
		Coords coordsNow = new Coords(pointingTO, EphemService.getAngleLMSTForMolongloNow());
		int slewTime = TCCManager.computeNSSlewTime(Control.getTccStatus().getCoordinates().getNs(),coordsNow.getAngleNS());
		System.err.println("Slew time to FRB transit: " + slewTime);
		ObservationTO observation = 
				new ObservationTO(
						new Coords(pointingTO, 
								EphemService.getAngleLMSTForMolongloNow().addSolarSeconds(slewTime)),null,
				tobs, "IFTM", BackendConstants.smirfBackend,
				BackendConstants.tiedArrayFanBeam, SMIRFConstants.interimFRBTransitPID, 0.0, 
				true, true, false, true, true);
		
		if(Control.isTerminateCall()) return;
		
		Control.setCurrentObservation(observation);

		
		observationManager.startObserving(observation);
		DBManager.addObservationToDB(observation);

		
		System.err.println("FRB Transit Observation started. Running for tobs = " + tobs);

		observationManager.waitForObservationCompletion(observation);
		
		observationManager.stopObserving();
		Control.setCurrentObservation(null);

		
		if(Control.isTerminateCall()) {
			System.err.println("Terminate call initated. Exiting IFTM");

			return;
		}

		DBManager.makeObservationComplete(observation);
	}
	
	public double getWaitTimeInHours(PointingTO pointingTO) throws EmptyCoordinatesException, CoordinateOverrideException{
		
		System.err.println("Calculating wait time for " + pointingTO.getPointingName());
		
		/**
		 * 1. Get local coordinates of the next pointing for LST = now + slew time. Call it HA_reach.
		 * 2. Get the corresponding HA for MD = x degrees. 
		 * 3. if HA_Reach - HA_2deg > a minute, then do FRB transit for (HA_Now - HA_2Deg)
		 * 
		 */
				
		Coords coords = new Coords(pointingTO, EphemService.getAngleLMSTForMolongloNow());
		
		int slewTime = TCCManager.computeNSSlewTime(Control.getTccStatus().getCoordinates().getNs(), coords.getAngleNS());
		
		coords = new Coords(pointingTO, EphemService.getAngleLMSTForMolongloNow().addSolarSeconds(slewTime));
		
		/**
		 * Get HA coordinates for MD ~ -1 deg ( so that the fan beams are all within the primary beam of 2 by 2 degrees ) .
		 */
		CoordinateTO coordinateTO = new CoordinateTO(null, null, coords.getAngleNS(), new Angle(getRadStartMDPosition(), Angle.DEG));
		MolongloCoordinateTransforms.telToSky(coordinateTO);
		
		double halfPowerHA = coordinateTO.getAngleHA().getDecimalHourValue();
		double haReach = coords.getAngleHA().getDecimalHourValue();
				
		if(haReach < 0 && Math.abs(halfPowerHA) < Math.abs(haReach)){
			double differenceHours =  Math.abs(Math.abs(haReach) - Math.abs(halfPowerHA));
			return differenceHours;
		}
		
		return 0.0;
	}
	
	
	protected Angle getHAForCoordTransitAtMD(Coords coords, Angle angleMD) throws EmptyCoordinatesException, CoordinateOverrideException{
				
		/**
		 * Get HA coordinates for MD ~ -1 deg ( so that the fan beams are all within the primary beam of 2 by 2 degrees ) .
		 */
		CoordinateTO coordinateTO = new CoordinateTO(null, null, coords.getAngleNS(), angleMD);
		MolongloCoordinateTransforms.telToSky(coordinateTO);
		
		return coordinateTO.getAngleHA();

	}
	protected Angle getHAForCoordTransitAtMD(Coords coords, double radMD) throws EmptyCoordinatesException, CoordinateOverrideException{
		return getHAForCoordTransitAtMD(coords, new Angle(radMD, Angle.DEG));
	}	
	protected Angle getHAForPointingTransitAtMDforLST(PointingTO pointingTO,Angle angleMD, Angle angleLST) throws EmptyCoordinatesException, CoordinateOverrideException{
		return getHAForCoordTransitAtMD(new Coords(pointingTO,angleLST), angleMD);
	}

	
	/**
	 * Get a HA sorted coordslist for a pointing list for the given LST
	 * @param gridPoints
	 * @param initLST
	 * @param initTelPosition
	 * @param nsSpeed
	 * @return
	 */
	protected static List<Coords> getCoordsListForLST(List<PointingTO> gridPoints, Angle initLST, CoordinateTO initTelPosition, Double nsSpeed){
		
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
			
			
			double ha = EphemService.getHA(lst, c.getPointingTO().getAngleRA()).getDecimalHourValue();
									
			return ha < 0;
		})
		
		.collect(Collectors.toList());
		
	}


	@Override
	public String getType() {
		return this.userInputs.getSchedulerType();
	}


	
	public static void main(String[] args) throws Exception {
		TransitScheduler scheduler = new TransitScheduler() {
			
			@Override
			public PointingTO next() throws CoordinateOverrideException, EmptyCoordinatesException, TCCException,
					NoSourceVisibleException, SchedulerException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void init(UserInputs userInputs) throws SchedulerException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public double getRadStartMDPosition() {
				
				return Constants.radMDEastHPBW;
			}


			@Override
			public List<PointingTO> getDefaultPointings() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		double waitTimeInHours = scheduler.getWaitTimeInHours(DBManager.getPointingByUniqueName("SMIRF_0025-7249"));
		
		System.err.println("Wait time in hours:" + waitTimeInHours);
		System.err.println("Should wait " + waitTimeInHours * Constants.hrs2Sec + "seconds for this to start");
	}

}
