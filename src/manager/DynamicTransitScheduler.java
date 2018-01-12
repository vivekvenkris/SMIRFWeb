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
import service.TCCStatusService;
import standalones.SMIRFTransitSimulator;
import util.Constants;
import util.SMIRFConstants;

public class DynamicTransitScheduler extends TransitScheduler{

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
	 * Implementation of the next() method for DYNAMIC transit scheduling.
	 * The HA convention is negative for the future and positive for the past ( HA = LST - RA)
	 */
	public PointingTO next2() throws CoordinateOverrideException, EmptyCoordinatesException, TCCException, NoSourceVisibleException{

		/**
		 * Get all pointings from database
		 */
		List<PointingTO> gridPoints = userInputs.getPointingTOs();

		System.err.println("All pointings: " + gridPoints.size());

		/**
		 *  Get the current telescope position
		 */
		Coords telescopePosition = new Coords(Control.getTccStatus());
		CoordinateTO initTelPosition = new CoordinateTO(null, null, telescopePosition.getAngleNS().getRadianValue(), 0.0);

		System.err.println("Telescope is here: " + telescopePosition.getAngleNS());

		/**
		 * Get positions of pointings (coords) in local coordinates for LST = now, sorted by increasing distance from Meridian.
		 */

		Angle initLST = EphemService.getAngleLMSTForMolongloNow();
		if(Control.getCurrentObservation() !=null) initLST.addSolarSeconds(userInputs.getTobsInSecs());

		List<Coords> coords = getCoordsListForLST(gridPoints, initLST, initTelPosition, userInputs.getNsSpeed());

		if(coords.isEmpty()) throw new NoSourceVisibleException("No pointings visible to observe");

		System.err.println("No. Pointings in the future: " + coords.size());

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
		 * First element of the shortlist list is the nearest, least observed pointing. Get its HA. Call it leastObservedHA
		 * Compute the slew time to it (for NS)
		 */

		Coords leastObserved = shortlisted.get(0);

		System.err.println("Least observed: " + leastObserved.getPointingTO().getPointingName());

		double leastObservedHA = Math.abs(initLST.getDecimalHourValue() - leastObserved.getPointingTO().getAngleRA().getDecimalHourValue());

		double slewTime = SMIRFTransitSimulator.computeNSSlewTime(leastObserved.getAngleNS(), initTelPosition.getAngleNS()) * Constants.sec2Hrs;

		Coords interim = null;

		Coords nearest = null;

		double shortestDetourInHours = 0;

		Coords secondLeastObserved = null;

		/**
		 * If wait time is longer than tobs, then try if you can go do another observation and come back to the least observed.
		 */

		if ( (leastObservedHA - slewTime) > tobs * Constants.sec2Hrs ) {

			for( int i=1; i< shortlisted.size(); i++){

				Coords iCoords = shortlisted.get(i);

				/**
				 * Don't re observe the pointings on the same day.
				 * Dont observe the current pointing again. It makes you look like a fool.
				 */
				if(today.contains(iCoords.getPointingTO())) continue;
				if(Control.getCurrentObservation() != null && iCoords.getPointingTO().equals(Control.getCurrentObservation().getCoords().getPointingTO())) continue;


				double slewTimeToInterimInHours = SMIRFTransitSimulator.computeNSSlewTime(iCoords.getAngleNS(), initTelPosition.getAngleNS())*Constants.sec2Hrs;
				double slewTimeFromInterimInHours = SMIRFTransitSimulator.computeNSSlewTime(leastObserved.getAngleNS(), iCoords.getAngleNS())*Constants.sec2Hrs;
				double totalSlewTimeInHours = slewTimeFromInterimInHours + slewTimeToInterimInHours;

				/**
				 * Since this calculations take only NS into consideration, it is important to check the HA too.
				 * Else, all we will be doing is to go and wait at say -60 degrees for a pointing 1 hour away
				 * because we were at -59 and it was shorter to go to -60 than to say -54 even if that -54 pointing 
				 * in the beam in 5 minutes.
				 * 
				 * Add a "dead time" where we will do FRB transit since the source is not "up"
				 */

				/**
				 * This will be the LST after we go to the pointing.
				 */
				Angle iLST = EphemService.getAngleLMSTForMolongloNow();
				iLST.addSolarSeconds((int) (slewTimeToInterimInHours* Constants.hrs2Sec));
				/**
				 * This will be the coordinate of iCoords at iLST
				 */
				Coords tempCoord = new Coords(iCoords.getPointingTO(), iLST);

				double deadTimeInHours =0.0;

				/**
				 * Do all the following crap only if the pointing is in the future.
				 */

				if(tempCoord.getAngleHA().getDecimalHourValue() <= 0) {

					/**
					 * This will be the HA of MD = MD_FoV/4.0 degrees 
					 */
					CoordinateTO coordinateTO = new CoordinateTO(null, null, tempCoord.getAngleNS(), new Angle(Constants.radMDToStartObs, Angle.DEG));
					MolongloCoordinateTransforms.telToSky(coordinateTO);
					double absObsHA = Math.abs(coordinateTO.getAngleHA().getDecimalHourValue());

					/**
					 * This will be the DEAD TIME for which we need to do FRB transits before the obs is complete.
					 * Add this to the detour time
					 */
					deadTimeInHours = absObsHA - Math.abs(tempCoord.getAngleHA().getDecimalHourValue());

				}

				double timeToDetourInHours =  totalSlewTimeInHours + deadTimeInHours + tobs*Constants.sec2Hrs;


				/**
				 * First pointing in the loop that can do this check is the pointing that has been the second-least observed.
				 * It may not necessarily be the pointing that is closest. The closest, second least observed, can still be quite far away in abs HA.
				 * So also calculate which pointing we can go to, ignoring the "prev num obs" parameter,
				 * as otherwise will be doing FRB transit all the time.
				 * 
				 * Try to see if atleast half of tobs is before TRANSIT 
				 * 
				 */

				if( (leastObservedHA - timeToDetourInHours ) >= (tobs*Constants.sec2Hrs)/2.0){

					if(secondLeastObserved == null) secondLeastObserved = iCoords;
					if(nearest == null || shortestDetourInHours > timeToDetourInHours){

						nearest = iCoords;
						shortestDetourInHours = timeToDetourInHours;

					}

				}

			}

			if( Math.abs(secondLeastObserved.getAngleHA().getDecimalHourValue()) 
					- shortestDetourInHours > (tobs*Constants.sec2Hrs)/2.0){

				interim = nearest;
			}

			else interim = secondLeastObserved;

			System.err.print("LO: " + leastObserved.getPointingTO().getPointingName());
			System.err.print(" SLO: " + secondLeastObserved.getPointingTO().getPointingName());
			System.err.print("Nearest: " + nearest.getPointingTO().getPointingName());

			System.err.println("Choosing " + interim.getPointingTO().getPointingName());

		}

		/**
		 * If there is an interim pointing, ignore the least observed and observe the interim. Least observed will be picked up again in the next iteration.
		 */

		Coords next = (interim != null)? interim : leastObserved;
		today.add(next.getPointingTO());

		System.err.println("Next: " + next.getPointingTO().getPointingName() + " " + next.getAngleHA().getDecimalHourValue());


		return next.getPointingTO();


	}
	/**
	 * Get the next() pointing for dynamic transit observations.
	 * next gen next()
	 * 
	 * The algorithm:
	 * 		1. Get the current observation's NS or the telescope's status if this is the first *
	 * 		2. Get all possible "future" observations after the current observation *
	 * 		3. Order by least observed *
	 * 		4. recursively check if I could go to LO; SLO and LO; TLO, SLO and LO and so on.
	 * 		5. Stop if at any time you think it is not possible to make it to LO.
	 * 
	 * @return PointingTO
	 * @throws CoordinateOverrideException
	 * @throws EmptyCoordinatesException
	 * @throws TCCException
	 * @throws NoSourceVisibleException
	 */
	@Override
	public PointingTO next() throws CoordinateOverrideException, EmptyCoordinatesException, TCCException, NoSourceVisibleException{

		
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

		System.err.println("All pointings: " + gridPoints.size());
 
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

		if(coords.isEmpty()) throw new NoSourceVisibleException("No pointings visible to observe");

		System.err.println("No. of Pointings in the future: " + coords.size());

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
		
		Set<Integer> minObsSet = new TreeSet<>(coords.stream().map(f -> f.getPointingTO().getNumObs()).collect(Collectors.toSet()));
		
		System.err.println("min obs set: " + minObsSet);
		
		int minimumObs = Collections.min(minObsSet);
		int index = 1;
				
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
		 * First element of the shortlist list is the nearest, least observed pointing. Get its HA. Call it leastObservedHA
		 * Compute the slew time to it (for NS)
		 */
		Coords leastObserved = null;
		Coords next = null;
		Coords interim = null;
		
		/**
		 * Find the nearest least observed,
		 * that is not too near that it has already transited and exited the beam.
		 */
		while (leastObserved == null) {
		
			if(shortlisted.isEmpty()) throw new NoSourceVisibleException("No pointings visible to observe");
			
			leastObserved = shortlisted.remove(0);

			System.err.println(" Considering Least observed: " + leastObserved.getPointingTO().getPointingName() + " num obs: " + leastObserved.getPointingTO().getNumObs());

			double leastObservedHA = leastObserved.getAngleHA().getDecimalHourValue();
		
			double slewTime = SMIRFTransitSimulator.computeNSSlewTime(leastObserved.getAngleNS(), initTelPosition.getAngleNS()) * Constants.sec2Hrs;

			slewTime = leastObservedHA > 0 ? slewTime : slewTime * -1.0;
		
			leastObservedHA  = leastObservedHA + slewTime;
		
			/**
			 * While we are computing all this, if the source has actually transited and gone beyond MD = MD_FoV/4, choose the next
			 */
			if(leastObservedHA > getHAForCoordTransitAtMD(leastObserved, Constants.radMDToEndObs).getDecimalHourValue()) continue;
			
			/**
			 * If the least observed pointing is already in the beam, then just choose that.
			 * Do FRB transit until it comes inside the MD = MD_FoV/4 point.
			 */
			if(leastObservedHA > 
					getHAForCoordTransitAtMD(leastObserved, Constants.radMDEastHPBW).getDecimalHourValue()) return leastObserved.getPointingTO();
			
			
			} /* end of while */
		
		System.err.println("Least observed is" + leastObserved.getPointingTO().getPointingName());
		System.err.println("least observed is at HA = " + leastObserved.getAngleHA().getDecimalHourValue());
		
		System.err.println("Other shortlisted pointings inbetween are: ");
		
		shortlisted
		.stream()
		.forEach(f -> System.err.println(f.getPointingTO().getPointingName() 
				+ " " + f.getAngleHA().getDecimalHourValue()
				+ " " + f.getPointingTO().getNumObs()));
		
		/**
		 *  If there was any least observed pointing in the beam, it would not have reached this point. Since there is none,
		 *  just choose the closest and give that as the interim
		 */
		
		 shortlisted = shortlisted
		 	.stream()
		 	.sorted(
				Comparator.comparing(f -> {
					Coords c = ((Coords)f);
					return c.getPointingTO().getNumObs();

				})
				
		 		.thenComparing(Comparator.comparing( f -> { 
		 			Coords c = ((Coords)f);
		 			return c.getAngleHA().getDecimalHourValue(); 
		 		}).reversed()))
		 	.filter(f -> {
				try {
					return f.getAngleHA().getDecimalHourValue() < getHAForCoordTransitAtMD((Coords)f, Constants.radMDToEndObs).getDecimalHourValue();
				} catch (EmptyCoordinatesException | CoordinateOverrideException e) {
					e.printStackTrace();
					return false;
				}
			})
		 	.collect(Collectors.toList());
		
		 System.err.println("Sorted shortlists:");
		 
		 shortlisted
			.stream()
			.forEach(f -> System.err.println(f.getPointingTO().getPointingName() 
					+ " " + f.getAngleHA().getDecimalHourValue()
					+ " " + f.getPointingTO().getNumObs()));		 
		 /**
		  * If there are no shortlisted, send the least observed pointing and do FRB transit till it comes inside the beam
		  */
		 if (shortlisted.isEmpty()) {
			 return leastObserved.getPointingTO();
		 }
		
		 next = shortlisted.get(0);
		 
		 System.err.println("Chosen next: " + next.getPointingTO().getPointingName() + "at HA:" + next.getAngleHA().getDecimalHourValue());
		 
		 return next.getPointingTO();
		 
		/**
		 * Now that we have got the "least observed" pointing, the goal, is to go there eventually.
		 * But the pointing could be several hours away and we should probably do something in the mean time.
		 * 
		 * We could see, if the second least observed (SLO) is nearer and we can do SLO and come back to LO in time.
		 * May be SLO itself is very far. So check TLO and FLO and so on.
		 * 
		 * As long as we can go to Nth LO  and make it to (N-1)th LO, let's choose that.
		 * 
		 * But what if, It is better to go from SLO to TLO to LO? This algorithm can't do that if we maintain states.
		 * So, let's be state less and recalculate this every time we get next()
		 * Then after observing SLO, it will check if it can go to any SLO before going to LO and if not,
		 * it will naturally choose TLO. I am a F'in genius! :D
		 * 
		 * So lets take a recursive approach. Get one path ( may be not the optimal path but doesn't matter)
		 * to go to the least observed pointing.
		 * 
		 * get the nth LO that has the shortest distance to the LO that can be done in time before the LO transits.
		 * add this to a list. 
		 * 
		 * get the mth LO that has the shortest distance to nth LO that can be done in time before the nth LO transits.
		 * 
		 * Now this is a problem. THe slew time to Interim will now not be from the present position to interim but from 
		 * interim 1 to interim 2. so for every additional pointing added to this "snake", I should unravel the effect from present
		 * to LO. What if the unraveling breaks in the middle. Should I just brute force?
		 * 
		 * Let me rethink this again. at time t=0, all I need to choose is the best of the available pointings to go to. Don't I?
		 * What I do not want to do is to miss the chance of doing LO because I was busy doing something else.
		 *  
		 * I think it is just best if you take everything in the future, and do a double sort on num observed and HA
		 * and choose the best trade off between the two. So check LO, it is out of the beam and it takes more than tobs,
		 * so check SLO, it is the same, check TLo, it is the same etc.. 
		 * 
		 * THe problem earlier was that I was choosing the least observed over how distant it was.
		 * because, lets say I take the closest, by the time I do 6 minutes on the closest, the SLO might go beyond where I want
		 * 
		 * 
		 * HOLD THIS TRAIN OF THOUGHT FOR THE NEXT UPGRADE......
		 * 
		 */

	}

	
	
	@Override
	public double getRadStartMDPosition() {
		
		return Constants.radMDToStartObs;
		
	}

	
	protected double futureLookUpTimeInHours() {
		
		/* negative because this is in the future */
		return -1.5;
	}

	@Override
	public List<PointingTO> getDefaultPointings() {
		System.err.println("Setting default pointings for SMIRF only scheduler");
		return DBManager.getAllPointings().stream().filter(f -> f.getPointingName().contains(SMIRFConstants.SMIRFPointingPrefix)).collect(Collectors.toList());
	}

public static void main(String[] args) {
	System.err.println( DBManager.getAllPointings().stream().
			filter(f -> f.getPointingName().contains(SMIRFConstants.SMIRFPointingPrefix)).
			collect(Collectors.toList()));

}


}
