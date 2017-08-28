package manager;

import java.io.IOException;
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
import control.Control;
import exceptions.BackendException;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.EphemException;
import exceptions.NoSourceVisibleException;
import exceptions.ObservationException;
import exceptions.ScheduleEndedException;
import exceptions.SchedulerException;
import exceptions.TCCException;
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
		
		if(scheduler != null) {
		
			if(schedulerType.equals(scheduler.getType())) return scheduler;
		
			else throw new SchedulerException("Scheduler already running. Type: " + scheduler.getType());
		}
		
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

		default:
			throw new SchedulerException("No such scheduler type.");
			
		}
		
		return scheduler;
	}
	
	@Override
	public void start() {
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
					end = true;
					break;
				}
				
				
				System.err.println("Now observing: " + next.getPointingName());

				if(Control.isFinishCall()) break;
				
				/**
				 * Point to the NS of the next pointing, but do not track
				 */
				TCCManager.pointNS(next);
				
				double waitTimeInHours = getWaitTimeInHours(next);
				

				/**
				 * if the pointing has passed the meridian, immediately start the observation
				 * if it hasn't, and hasn't crossed HPBW, start FRB transit if there is atleast 1 minute of observation
				 * else just sleep 
				 */
				double minObsGapinHours = BackendConstants.minTimeBetweenObsInSecs * Constants.sec2Hrs;
				
				if(waitTimeInHours > minObsGapinHours) doInterimFRBTransit( (int) (waitTimeInHours * Constants.hrs2Sec), next.getAngleDEC());
				else 	Thread.sleep((long) (waitTimeInHours*Constants.hrs2Sec*1000));
				
				if(Control.isTerminateCall()) break;
				
				Coords coords = new Coords(next, EphemService.getAngleLMSTForMolongloNow());
				
				ObservationTO observation = new ObservationTO(coords, userInputs,BackendConstants.smirfBackend,BackendConstants.tiedArrayFanBeam, SMIRFConstants.PID);
				Control.setCurrentObservation(observation);
		
				if(userInputs.getDoPulsarSearching()) observationManager.waitForPreviousSMIRFSoups();
				observationManager.startObserving(observation);
				DBManager.addObservationToDB(observation);
				
				/**
				 *  If pulsar search is enabled, do it.
				 */
				sendStitches = null;

				if( userInputs.getDoPulsarSearching() )sendStitches = Control.getExecutorService().submit(sendStitchesThread);
				previousObservation = Control.getExecutorService().submit(manageObservation);

			}
			/**
			 * Reset control flags -- finish and terminate to false.
			 */
			System.err.println(" Dynamic transit ended.");
			
		} catch (TCCException | CoordinateOverrideException | EmptyCoordinatesException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (ObservationException e) {
			e.printStackTrace();
		} catch (BackendException e) {
			e.printStackTrace();
		} catch (EphemException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSourceVisibleException e) {
			e.printStackTrace();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}finally {
			Control.reset();
			Control.setScheduler(null);
			Control.setCurrentObservation(null);
		}
		
	}
	
	private void doInterimFRBTransit(Integer tobs, Angle angleDEC) 
			throws ObservationException, EmptyCoordinatesException, CoordinateOverrideException, 
					InterruptedException, TCCException, BackendException, EphemException{
		
		PointingTO pointingTO = new PointingTO(EphemService.getAngleLMSTForMolongloNow(), angleDEC,"FRB Transit",transitPointingSymbol);
		ObservationTO observation = new ObservationTO(new Coords(pointingTO, EphemService.getAngleLMSTForMolongloNow()),null,
				tobs, "IFTM", BackendConstants.smirfBackend,
				BackendConstants.tiedArrayFanBeam, SMIRFConstants.interimFRBTransitPID, 0.0, 
				true, true, false, true, true);
		
		Control.setCurrentObservation(observation);

		
		observationManager.startObserving(observation);
		DBManager.addObservationToDB(observation);

		
		System.err.println("FRB Transit Observation started. Running for tobs = " + tobs);

		observationManager.waitForObservationCompletion(observation);
		
		observationManager.stopObserving();
		Control.setCurrentObservation(null);

		
		if(Control.isTerminateCall()) return;

		DBManager.makeObservationComplete(observation);
	}
	
	private double getWaitTimeInHours(PointingTO pointingTO) throws EmptyCoordinatesException, CoordinateOverrideException{
		
		/**
		 * 1. Get local coordinates of the next pointing for LST = now.
		 * 2. Get the corresponding HA for MD = 2 degrees. 
		 * 3. if HA_Now - HA_2deg > a minute, then do FRB transit for (HA_Now - HA_2Deg)
		 */
		Coords coords = new Coords(pointingTO, EphemService.getAngleLMSTForMolongloNow());
		/**
		 * Get HA coordinates for MD ~ -1 deg ( so that the fan beams are all within the primary beam of 2 by 2 degrees ) .
		 */
		CoordinateTO coordinateTO = new CoordinateTO(null, null, coords.getAngleNS(), new Angle(Constants.radMDToStartObs, Angle.DEG));
		MolongloCoordinateTransforms.telToSky(coordinateTO);
		
		double halfPowerHA = coordinateTO.getAngleHA().getDecimalHourValue();
		double haNow = coords.getAngleHA().getDecimalHourValue();
		
		
		if(haNow < 0 && Math.abs(halfPowerHA) < Math.abs(haNow)){
			double differenceHours =  Math.abs(Math.abs(haNow) - Math.abs(halfPowerHA));
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
			
			double ha = lst.getDecimalHourValue() - c.getPointingTO().getAngleRA().getDecimalHourValue();
									
			return ha < 0;
		})
		
		.collect(Collectors.toList());
		
	}


	@Override
	public String getType() {
		return this.userInputs.getSchedulerType();
	}



}
