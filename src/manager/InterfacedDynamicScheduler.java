package manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import bean.Angle;
import bean.CoordinateTO;
import bean.Coords;
import bean.ObservationTO;
import bean.PointingTO;
import bean.UserInputs;
import control.Control;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.NoSourceVisibleException;
import exceptions.SchedulerException;
import exceptions.TCCException;
import service.EphemService;
import util.SMIRFConstants;

public class InterfacedDynamicScheduler extends TransitScheduler{
	
	private Double startMD;
	private Integer tobs;
	
	List<PointingTO> today = new ArrayList<PointingTO>();

	@Override
	public void init(UserInputs userInputs) throws SchedulerException {
		this.userInputs = userInputs;
		
		this.userInputs.setPointingTOs(getDefaultPointings());


//		if(!userInputs.getSchedulerType().equals(SMIRFConstants.dynamicTransitScheduler)){
//			throw new SchedulerException("Mismatch between scheduler object and type");
//		}
		
	}

	/**
	 * 1. Send prev_UTC_end NS MD string to Stefan
	 * He uses this to compute which one to observe next.
	 * Get the following string from Stefan for what to observe next
	 * pointing_id tobs md_start auto/manual tb0 tb1 tb2 tb3. If not available ( not in the beam and not for the next 30 minutes) will send "NA" 
	 * 
	 * What Stefan sends could be a long way away Eg: 1644 when we are next to LST=8. So, if HA of the source > 30 minutes, just ignore and proceed
	 * with the usual SMIRF logic and find the "least observed pointing".
	 * 
	 * if not, his pointing will be the "least observed pointing". Continue with the rest of the logic.
	 * The algorithm:
	 * 		1. Get the current observation's NS or the telescope's status if this is the first 
	 * 		2. Get all possible "future" observations after the current observation 
	 * 		3. Order by least observed 
	 * 		4. recursively check if I could go to LO; SLO and LO; TLO, SLO and LO and so on.
	 * 		5. Stop if at any time you think it is not possible to make it to LO.
	 *  
	 * 
	 * 
	 * 
	 * 
	 * 
	 *  
	 * 
	 */
	
	@Override
	public PointingTO next() throws CoordinateOverrideException, EmptyCoordinatesException, TCCException,
			NoSourceVisibleException, SchedulerException {
		// TODO Auto-generated method stub
		
		/**
		 * If the external interface provides a nearby valid pointing, just go there.
		 */
		PointingTO external = getPointingFromExternalInterface();
		
		PointingTO leastObserved = external;
		if(leastObserved == null ) {
		
		
			/**
			 * Get all pointings from database
			 */
			List<PointingTO> gridPoints = userInputs.getPointingTOs(); 
			
			/**
			 * Sort the pointings based on num observations already done
			 */
			gridPoints = gridPoints
							.stream()
							.sorted(Comparator.comparing(PointingTO::getNumObs))
							.collect(Collectors.toList());
			
			
			/**
			 * Get telescope position now or current obs position now.
			 */
			//System.err.println("Sleeping for 5 seconds, because I am lazy.");

			//try{Thread.sleep(5000);}catch (Exception e) {} 

			ObservationTO currentObservation = Control.getCurrentObservation();

			Coords telescopePosition = null;

			if(currentObservation != null) telescopePosition = currentObservation.getCoords();
			else telescopePosition = new Coords(Control.getTccStatus());

			System.err.println("Telescope is here: " + telescopePosition.getAngleNS());

			CoordinateTO initTelPosition = new CoordinateTO(null, null, telescopePosition.getAngleNS().getRadianValue(), 0.0);
			

			/**
			 * Get positions of pointings (coords) in local coordinates for LST = now (+tobs) , sorted by increasing distance from Meridian.
			 * Add tobs if the current observation is not null.
			 */

			Angle initLST = EphemService.getAngleLMSTForMolongloNow();
			if(Control.isThereAnActiveObservation()) initLST.addSolarSeconds(userInputs.getTobsInSecs());

			List<Coords> coords = getCoordsListForLST(gridPoints, initLST, initTelPosition, userInputs.getNsSpeed());
			
			System.err.println("No. of Pointings in the future that can be observed: " + coords.size());
			if(coords.isEmpty()) throw new NoSourceVisibleException("No pointings visible to observe");


			/**
			 * Get the number of times the pointing that was least observed, was observed for.
			 */
			
			coords = coords.stream().filter( 
					f -> {
						/**
						 * Don't do the current observation again.
						 */
						if( Control.isThereAnActiveObservation() 
								&& f.getPointingTO().equals(Control.getCurrentObservation().getCoords().getPointingTO())) return false;
						
						/**
						 * Don't reobserve pointings that were done today already
						 */
						if(today.contains(f.getPointingTO())) return false;
						
						/**
						 * Dont look too much in the future. Limit yourself to the next x minutes of pointings. ( default = 90)
						 */
						
						if (f.getAngleHA().getDecimalHourValue() < futureLookUpTimeInHours()) return false; 
						return true;

					}).collect(Collectors.toList());
			
			/**
			 * Integer set containing the unique numobs values of each pointing. The values are arranged in ascending order.
			 * 
			 * Eg: 
			 * PPOINTING_ID NUM_TIMES_OBSERVED 
			 * 1			2
			 * 2			0
			 * 3			2
			 * 4			5
			 * 5			0
			 * then minObsSet = {0,2,5}
			 * 
			 */
			Set<Integer> minObsSet = new TreeSet<>(coords.stream().map(f -> f.getPointingTO().getNumObs()).collect(Collectors.toSet()));
			System.err.println("minObsSet: " + minObsSet);
			
			/**
			 * minimum num obs in the existing pointing set. For the above example, this is the value "0"
			 */
			int minimumObs = Collections.min(minObsSet);

			
			/**
			 * Get the nearest pointing that is observed the least number of times.
			 * In the above example, this is the pointing 2. (5 is further than 2 in HA as it is ordered already in HA increasing order)
			 */
			int index = 1;
			for(Coords c: coords){

				if(c.getPointingTO().getNumObs().equals(minimumObs)) break;
				index++;
			}
			

			/**
			 * Shortlist all the pointings that are enroute to the least observed pointing, to check if they can be observed in the mean time. 
			 * Sort it by number of times it has been observed
			 * This list, after sorting, will have the least observed pointing as its first value and then in ascending order of  num obs
			 */
			
			List<Coords> shortlisted = coords.subList(0, index).stream().sorted(
					Comparator.comparing(f -> ((Coords)f).getPointingTO().getNumObs())).collect(Collectors.toList());
			
			
			Coords next = null;
			Coords interim = null;
		
		}
		
		
		return null;
	}
	
	protected double futureLookUpTimeInHours() {
		
		/* negative because this is in the future */
		return -1.5;
	}

	@Override
	public double getRadStartMDPosition() {
		return startMD;
	}

	@Override
	public List<PointingTO> getDefaultPointings() {
		System.err.println("Setting default pointings for Interfaced SMIRF scheduler");
		return DBManager.getAllPointings().stream().filter(f -> f.getPointingName().contains(SMIRFConstants.SMIRFPointingPrefix)).collect(Collectors.toList());		return null;
	}
	
	public PointingTO getPointingFromExternalInterface() throws EmptyCoordinatesException, CoordinateOverrideException {
		PointingTO external = null;
		/**
		 * Call Stefan's code here.
		 * 
		 */	
		Angle HA = new Coords(external, EphemService.getAngleLMSTForMolongloNow()).getAngleHA();
		
		/**
		 * if the HA is positive meaning it has already transited ( hopefully not, but incase Stefan's code has a bug, let's check) or if the 
		 * pointing is more than half an hour away, just ignore it.
		 */
		if( HA.getDecimalHourValue() > 0 || Math.abs(HA.getDecimalHourValue()) > 0.5 ) return null;
		
		
		/**
		 * Set the required startMD. Should this just be on the database too?  For now No.
		 * Set the required Tobs from Stefan;
		 */
		startMD = 0.0;
		tobs = SMIRFConstants.tobs;
		return external;
	}

}
