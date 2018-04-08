package manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import bean.Angle;
import bean.CoordinateTO;
import bean.Coords;
import bean.ObservationTO;
import bean.PointingTO;
import bean.TBSourceTO;
import bean.UserInputs;
import control.Control;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.NoSourceVisibleException;
import exceptions.ObservationException;
import exceptions.SchedulerException;
import exceptions.TCCException;
import listeners.StatusPooler;
import mailer.Mailer;
import service.EphemService;
import standalones.SMIRFTransitSimulator;
import util.ConfigManager;
import util.Constants;
import util.SMIRFConstants;
import util.TCCConstants;

public class InterfacedDynamicScheduler extends TransitScheduler{

	private Double startMD;
	private Integer tobs;


	@Override
	public void init(UserInputs userInputs) throws SchedulerException {
		this.userInputs = userInputs;

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

		Coords leastObserved = null;

		/**
		 * Get all pointings from database or the shortlisted ones from the user.
		 */
		List<PointingTO> gridPoints = (!userInputs.getPointingTOs().isEmpty())? userInputs.getPointingTOs() : getDefaultPointings(); 

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

		System.err.println("Getting coords for LST = " + initLST);

		List<Coords> coords = getCoordsListForLST(gridPoints, initLST, initTelPosition, userInputs.getNsSpeed());

		System.err.println("No. of Pointings in the future that can be observed: " + coords.size());
		if(coords.isEmpty()) throw new NoSourceVisibleException("No pointings visible to observe. "
				+ "performing FRB transit for " + futureLookUpTimeInHours() + "hours.");


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
					if(thisSession.contains(f.getPointingTO())) return false;

					/**
					 * Dont look too much in the future. Limit yourself to the next x minutes of pointings. ( default = 90)
					 */
					
					if (f.getAngleHA().getDecimalHourValue() < futureLookUpTimeInHours()) return false; 
					return true;

				}).collect(Collectors.toList());
		
		System.err.println("Number of coords after basic house keeping: " + coords.size());

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
		Set<Integer> minObsSet = 
				new TreeSet<>(coords.stream().map(f -> f.getPointingTO().getNumObs()).collect(Collectors.toSet()));

		System.err.println("minObsSet is: " + minObsSet);


		/**
		 * 
		 * Create a grouped map for every pointing with the same number of times observed
		 * for the above example, form a grouped set 
		 * nobs  pointing
		 * 0		2,5
		 * 2		1,3
		 * 4		 4
		 * 
		 * Make sure the individual sets are sorted in increasing HA order, so that we can add just the closest pointing.
		 * 
		 */

		Map<Integer, Coords> groupedCoordsMap = new TreeMap<>();
		System.err.println("Num_times_observed \t List_of_pointings");
		for(int nobs: minObsSet) {

			List<Coords> temp = coords
					.stream()
					.filter(f -> f.getPointingTO().getNumObs().equals(nobs))
					.sorted(Comparator.comparing(f -> Math.abs(((Coords)f).getAngleHA().getDecimalHourValue())))
					.collect(Collectors.toList());
			System.err.println(nobs + " " + temp);
			if(!temp.isEmpty()) groupedCoordsMap.put(nobs, temp.get(0));
		}


		/**
		 * If the external interface provides a nearby valid pointing, just go there.
		 */
		PointingTO external = getPointingFromExternalInterface();

		if(external != null) leastObserved = new Coords(external, EphemService.getAngleLMSTForMolongloNow());

		else {




			/**
			 * Take every key value pair, get the first value in the list ( the closest one of the group to Zenith ).
			 * 
			 * If the pointing is still "observable" after slew to there, then choose that. 
			 * 
			 */

			Set<Integer> alreadyChecked = new TreeSet<>();
			boolean cadenceCheck =true;

			while (leastObserved == null) {
				/**
				 * If there is nothing to observe with cadence check, remove cadence check so that you will have something to do.
				 * else raise exception
				 */
				if(alreadyChecked.size() == minObsSet.size()) {
					System.err.println("Nothing to observe. releasing cadence check.");
					if(cadenceCheck) {
						cadenceCheck = false;
						alreadyChecked.clear();
					}
					else{
						throw new NoSourceVisibleException("Checked all sources. nothing to go to. ");
					}
				}

				Coords coord = null;

				for(int nobs: minObsSet) {

					if(alreadyChecked.contains(nobs)) continue;

					alreadyChecked.add(nobs);
					coord = groupedCoordsMap.get(nobs);

					/* in principle, this should never happen but hey future Vivek, it is your code. Anything can happen :P */
					if(coord == null ) {
						System.err.println("Coord is null. This should not have happened.");
						continue; 
					}

					System.err.println("Considering " + coord.getPointingTO().getPointingName() + " num obs: " + coord.getPointingTO().getNumObs() + " for least observed.");

					
					Angle coordHA = coord.getAngleHA();

					int slewTime = TCCManager.computeNSSlewTime(coord.getAngleNS(), initTelPosition.getAngleNS());

					//slewTime = coordHA.getDecimalHourValue() > 0 ? slewTime : slewTime * -1.0;

					coordHA.addSolarSeconds(slewTime);

					/**
					 * Check minimum cadence if cadence check is true.
					 */
					if(cadenceCheck) {
						
						Integer leastCadance = coord.getPointingTO().getLeastCadanceInDays();
						Double daysSinceLastObserved = null;
	
						try {
							if(coord.getPointingTO().isPulsarPointing()) {
	
								TBSourceTO to = PSRCATManager.getTimingProgrammeSouceByName(coord.getPointingTO().getPointingName().replaceAll("PSR_", ""));
	
								if(to != null ) daysSinceLastObserved = to.getDaysSinceLastObserved();
	
	
							}
							else {
								
								daysSinceLastObserved = DBManager.getDaysSinceObserved(coord.getPointingTO().getPointingName());
								
							}
						} catch (ObservationException e) {
							e.printStackTrace();
						}
						
						
						if( leastCadance != null 
								&& daysSinceLastObserved != null 
//								&& slewTime > Integer.parseInt(ConfigManager.getSmirfMap().get("TOTAL_SLEW_TIME_TO_CHECK_CADENCE"))
								&& (leastCadance > daysSinceLastObserved) ) {
	
								System.err.println("Skipping least observed = " + coord.getPointingTO().getPointingName() + " as it was observed only " + daysSinceLastObserved +" ago. min cadence = " + leastCadance);
								
								continue;
	
						}

					}
					
					
					
					
					
 
					/**
					 * While we are computing all this, if this source has actually transited and gone beyond MD = MD_FoV/4, choose the next
					 */
					if(coordHA.getDecimalHourValue() > 0 && coordHA.getDecimalHourValue() > getHAForCoordTransitAtMD(coord, Constants.radMDToEndObs).getDecimalHourValue()) {
						System.err.println(coord.getPointingTO().getPointingName() + " is out of beam. HA=" + coordHA.getDecimalHourValue() );
						break;
					}

					/**
					 * If the code has reached this point, it means the following:
					 * 1. coord is the "least observed" pointing. This should have the highest priority to go to.
					 * 2. coord is definitely not transited MD = -1 (or in general FoV/4 after transit)
					 * 
					 */
					System.err.println("Setting " + coord.getPointingTO().getPointingName() + " as least observed" );
					leastObserved = coord;

					/**
					 * If the least observed pointing is already in the beam, then just choose that.
					 * Do FRB transit until it comes inside the MD = MD_FoV/4 point.
					 */

					if(coordHA.getDecimalHourValue() > -1 && coordHA.getDecimalHourValue() > 
					getHAForCoordTransitAtMD(leastObserved, Constants.radMDEastHPBW).getDecimalHourValue()) { 
						
						
						System.err.println("Least observed is already in the beam. HA=" + coordHA.getDecimalHourValue() + " ");
						return leastObserved.getPointingTO();

					}

					if (leastObserved != null ) break;


				}
				System.err.println("Least observed is " + leastObserved);


			}

		}

		/**
		 * At this point, we have chosen the "least observed" pointing, be it we compute it ourselves, or
		 * masquarade an external pointing as the least observed.
		 * 
		 * And this pointing is not in the beam yet. So we check if there is enough time to do something else and come back.
		 * 
		 */

		int minNobs = (external == null) ? leastObserved.getPointingTO().getNumObs() :  0;

		Coords finalLeastObserved = leastObserved;

		System.err.println(
				" max_HA that least observed can be observed = " +   
						getHAForCoordTransitAtMD((Coords)leastObserved,  
								Constants.radMDToEndObs).getDecimalHourValue());


		System.err.println(coords.size() + " interims to coose from");


		List<Coords> neighbouringCoords = groupedCoordsMap.entrySet()
				.stream()
				.map( f -> {
					if(f.getValue() != null ) return f.getValue();
					return null;
				})
				.filter(f -> f != null)
				.collect(Collectors.toList());

		System.err.println("neighbouring coords irrespective of num_obs:" + neighbouringCoords);


		List<Coords> shortlistedInterims = neighbouringCoords
				.stream()
				.filter(f -> {
					try {
						if ( f.getPointingTO().getNumObs() <= minNobs ) return false;

						int slewTimeToInterimInSeconds = TCCManager.computeNSSlewTime(f.getAngleNS(), initTelPosition.getAngleNS());

						double interimHAOnReach = f.getAngleHA().clone().addSolarSeconds(slewTimeToInterimInSeconds).getDecimalHourValue();

						return 	interimHAOnReach < 
								getHAForCoordTransitAtMD((Coords)f, Constants.radMDToEndObs).getDecimalHourValue();

					} catch (EmptyCoordinatesException | CoordinateOverrideException e) {
						e.printStackTrace();
						Mailer.sendEmail(e);
						return false;
					}
				})
				.sorted(
						Comparator.comparing(f -> {
							Coords c = ((Coords)f);
							return c.getPointingTO().getNumObs();

						})

						.thenComparing(Comparator.comparing( f -> { 
							Coords c = ((Coords)f);
							return c.getAngleHA().getDecimalHourValue(); 
						}).reversed()))
				.filter(interim -> {

					/**
					 * See if going to this source, we will have enough time to make it to the least observed pointing.
					 * 
					 * The time taken for us to do an interim pointing include
					 * 
					 * 			1. time taken for slew from present location to interim
					 * 
					 * 			2. dead time waiting for interim doing FRB transits
					 * 
					 * 			3. tobs of interim
					 * 
					 * 			4. time taken for slew from interim to least observed
					 * 
					 * So we should make sure that the MD of least_observed after HA_To_MD( HA_now + 1 + 2 + 3 + 4 ) is < MD_end

					 * 
					 */


					int totalDeadTime = 0, totalSlewTime = 0;

					int slewTimeToInterim = TCCManager.computeNSSlewTime(interim.getAngleNS(), initTelPosition.getAngleNS());
					int slewTimeFromInterim = TCCManager.computeNSSlewTime(finalLeastObserved.getAngleNS(), interim.getAngleNS());
					totalSlewTime = slewTimeFromInterim + slewTimeToInterim;



					try {
						
						Integer leastCadance = interim.getPointingTO().getLeastCadanceInDays();
						Double daysSinceLastObserved = null;

						if(interim.getPointingTO().isPulsarPointing()) {

							TBSourceTO to = PSRCATManager.getTimingProgrammeSouceByName(interim.getPointingTO().getPointingName().replaceAll("PSR_", ""));

							if(to != null ) daysSinceLastObserved = to.getDaysSinceLastObserved();


						}
						else {
							
							daysSinceLastObserved = DBManager.getDaysSinceObserved(interim.getPointingTO().getPointingName());
							
						}
						
						
						if( leastCadance != null 
								&& daysSinceLastObserved != null 
								&& totalSlewTime > Integer.parseInt(ConfigManager.getSmirfMap().get("TOTAL_SLEW_TIME_TO_CHECK_CADENCE"))
								&& (leastCadance > daysSinceLastObserved) ) {

								System.err.println("Skipping " + interim.getPointingTO().getPointingName() + " as it was observed only " + daysSinceLastObserved +" ago. min cadence = " + leastCadance);
								
								return false;

						}



						/**
						 * This will be the LST after we go to the pointing.
						 */
						Angle iLST = EphemService.getAngleLMSTForMolongloNow();
						iLST.addSolarSeconds((int) (slewTimeToInterim));

						Coords tempCoord = new Coords(interim.getPointingTO(), iLST);

						/**
						 * This will be the HA of MD = MD_FoV/4.0 degrees 
						 */

						double startHAInHours = getHAForCoordTransitAtMD((Coords)interim,  Constants.radMDToStartObs).getDecimalHourValue();

						/**
						 * This will be the DEAD TIME for which we need to do FRB transits before the obs is complete.
						 * Add this to the detour time
						 * 
						 * if start HA is positive, then it has already transited. No point in calculating dead time as there is none.
						 * 
						 * if HA is negative, it may be within the "optimal" position in the beam or not.
						 * 
						 * if it is not, then the HA of source will be much more negative than the HA of start position.
						 * 
						 * Hence if the source is outside the beam,  HA_optimum - HA_source will always be positive.
						 * 
						 * 
						 */
						if(tempCoord.getAngleHA().getDecimalHourValue() < 0 && startHAInHours > tempCoord.getAngleHA().getDecimalHourValue() )
							totalDeadTime = (int) ((startHAInHours - tempCoord.getAngleHA().getDecimalHourValue()) * Constants.hrs2Sec);


						int timeToDetour =  totalSlewTime + totalDeadTime + SMIRFConstants.tobs;


						/**
						 * This is the coords of the least observed pointing after observing interim
						 */

						double finalHAInHours = finalLeastObserved.getAngleHA().clone().addSolarSeconds(timeToDetour).getDecimalHourValue(); 

						System.err.println(tempCoord.getPointingTO().getPointingName() 
								+ " DT=" +  totalDeadTime + " ST=" + totalSlewTime + " TOBS=" + SMIRFConstants.tobs + "Final HA after LOP = " + finalHAInHours );


						return finalHAInHours 
								< getHAForCoordTransitAtMD((Coords)finalLeastObserved,  Constants.radMDToEndObs).getDecimalHourValue();


					} catch (EmptyCoordinatesException | CoordinateOverrideException | ObservationException e) {
						e.printStackTrace();
						Mailer.sendEmail(e);
					}


					return false;
				})
				.collect(Collectors.toList());


		/**
		 * If there are several interims to go to, choose the nearest, most-least observed pointing so that you don't just sit there and do FRB transits all the time.
		 */

		List<Coords> nearestInterims = new ArrayList<>();

		System.err.println("There are " + shortlistedInterims.size() + " shortlisted interims: " + shortlistedInterims);



		if(shortlistedInterims.size() > 0 ) {

			nearestInterims = shortlistedInterims
					.stream()

					.sorted(
							(Comparator.comparing( f -> { 
								Coords c = ((Coords)f);
								return c.getAngleHA().getDecimalHourValue(); 
							}).reversed())
							)
					.filter(f ->  f.getAngleHA().getDecimalHourValue() > -1)
					.collect(Collectors.toList());
			
			
			
			
		}
		
		Coords immediateAttention = null;
		
//		if(nearestInterims.size() > 0 ) {
//			
//			immediateAttention = nearestInterims
//					.stream()
//					.filter( f-> f.getPointingTO().getMaxUnObservedDays() > 10)
//					.sorted(Comparator.comparing(f -> {
//
//						Coords c = (Coords)f;
//						return c.getPointingTO().getMaxUnObservedDays();
//					}
//							).reversed())
//					.findFirst().orElse(null);
//		}

	//	if(nearestInterims.size() > 0 ) {
			
		immediateAttention = coords
				.stream()
				.filter(f ->  f.getAngleHA().getDecimalHourValue() > -0.5 
						&& f.getAngleHA().getDecimalHourValue() < 0 
						&& f.getPointingTO().getMaxUnObservedDays() > 10 )
				.sorted(Comparator.comparing(f -> {

					Coords c = (Coords)f;
					return c.getPointingTO().getMaxUnObservedDays();
				}
						).reversed())
				.findFirst().orElse(null);
	//	}

		
		System.err.println( "Out of that " + nearestInterims.size() + " are nearby: " + nearestInterims);
		System.err.println("Pointing that requires immediate attention:" + immediateAttention + ((immediateAttention != null)? (" assoc pulsars:" 
				+ immediateAttention.getPointingTO().getAssociatedPulsars() + " days:" + immediateAttention.getPointingTO().getMaxUnObservedDays()):""));

		Coords next = null;
		
		if(immediateAttention != null) {
			System.err.println("Choosing the interim that requires immediate attention.");
			next = immediateAttention; 
		}
		
		else if(!shortlistedInterims.isEmpty()) { 
			if(shortlistedInterims.get(0).getAngleHA().getDecimalHourValue() < -0.5 && !nearestInterims.isEmpty()) {
				System.err.println("Choosing the nearest interim");
				next = nearestInterims.get(0);
			}
			else {
				System.err.println("Choosing the most appropriate interim");
				next = shortlistedInterims.get(0);
			}

		}
		else next = leastObserved;

		System.err.println("Chosen next: " + next.getPointingTO().getPointingName() + "at HA:" + next.getAngleHA().getDecimalHourValue());

		return next.getPointingTO();
	}


	public  Coords checkInterim(List<Coords> coords,final Coords finalLeastObserved, Coords interim , Coords initTelPosition) {



		/**
		 * See if going to this source, we will have enough time to make it to the least observed pointing.
		 * 
		 * The time taken for us to do an interim pointing include
		 * 
		 * 			1. time taken for slew from present location to interim
		 * 
		 * 			2. dead time waiting for interim doing FRB transits
		 * 
		 * 			3. tobs of interim
		 * 
		 * 			4. time taken for slew from interim to least observed
		 * 
		 * So we should make sure that the MD of least_observed after HA_To_MD( HA_now + 1 + 2 + 3 + 4 ) is < MD_end

		 * 
		 */


		int totalDeadTime = 0, totalSlewTime = 0;

		int slewTimeToInterim = TCCManager.computeNSSlewTime(interim.getAngleNS(), initTelPosition.getAngleNS());
		int slewTimeFromInterim = TCCManager.computeNSSlewTime(finalLeastObserved.getAngleNS(), interim.getAngleNS());
		totalSlewTime = slewTimeFromInterim + slewTimeToInterim;



		try {

			Integer leastCadance = interim.getPointingTO().getLeastCadanceInDays();
			Double daysSinceLastObserved = null;

			if(interim.getPointingTO().isPulsarPointing()) {

				TBSourceTO to = PSRCATManager.getTimingProgrammeSouceByName(interim.getPointingTO().getPointingName().replaceAll("PSR_", ""));

				if(to != null ) daysSinceLastObserved = to.getDaysSinceLastObserved();


			}
			else {
				
				daysSinceLastObserved = DBManager.getDaysSinceObserved(interim.getPointingTO().getPointingName());
				
			}
			
			
			if( leastCadance != null 
					&& daysSinceLastObserved != null 
					&& totalSlewTime > Integer.parseInt(ConfigManager.getSmirfMap().get("TOTAL_SLEW_TIME_TO_CHECK_CADENCE"))
					&& (leastCadance > daysSinceLastObserved) ) {

					System.err.println("Skipping " + interim.getPointingTO().getPointingName() + " as it was observed only " + daysSinceLastObserved +" ago. min cadence = " + leastCadance);
					
					return null;

			}

			/**
			 * This will be the LST after we go to the pointing.
			 */
			Angle iLST = EphemService.getAngleLMSTForMolongloNow();
			iLST.addSolarSeconds((int) (slewTimeToInterim));

			Coords tempCoord = new Coords(interim.getPointingTO(), iLST);

			/**
			 * This will be the HA of MD = MD_FoV/4.0 degrees 
			 */

			double startHAInHours = getHAForCoordTransitAtMD((Coords)interim,  Constants.radMDToStartObs).getDecimalHourValue();

			/**
			 * This will be the DEAD TIME for which we need to do FRB transits before the obs is complete.
			 * Add this to the detour time
			 * 
			 * if start HA is positive, then it has already transited. No point in calculating dead time as there is none.
			 * 
			 * if HA is negative, it may be within the "optimal" position in the beam or not.
			 * 
			 * if it is not, then the HA of source will be much more negative than the HA of start position.
			 * 
			 * Hence if the source is outside the beam,  HA_optimum - HA_source will always be positive.
			 * 
			 * 
			 */
			if(tempCoord.getAngleHA().getDecimalHourValue() < 0 && startHAInHours > tempCoord.getAngleHA().getDecimalHourValue() )
				totalDeadTime = (int) ((startHAInHours - tempCoord.getAngleHA().getDecimalHourValue()) * Constants.hrs2Sec);


			int timeToDetour =  totalSlewTime + totalDeadTime + SMIRFConstants.tobs;

			System.err.print(tempCoord.getPointingTO().getPointingName() 
					+ " DT=" +  totalDeadTime + " ST=" + totalSlewTime + " TOBS=" + SMIRFConstants.tobs);

			/**
			 * This is the coords of the least observed pointing after observing interim
			 */
			Coords finalCoord = new Coords(finalLeastObserved.getPointingTO(), 
					EphemService.getAngleLMSTForMolongloNow().addSolarSeconds(timeToDetour));
			double finalHAInHours = finalCoord.getAngleHA().getDecimalHourValue(); 
			//tempLeastObserved.getAngleHA().addSolarSeconds(timeToDetour).getDecimalHourValue();

			System.err.println(
					"HA of LOP after interim=" +  finalHAInHours + 
					" max_HA=" +   
					getHAForCoordTransitAtMD((Coords)finalLeastObserved,  
							Constants.radMDToEndObs).getDecimalHourValue());

			if( finalHAInHours 
					< getHAForCoordTransitAtMD((Coords)finalLeastObserved,  Constants.radMDToEndObs).getDecimalHourValue()){
				return finalCoord;
			}


		} catch (EmptyCoordinatesException | CoordinateOverrideException | ObservationException e) {
			e.printStackTrace();
			Mailer.sendEmail(e);
		}


		return null;
	}






	protected double futureLookUpTimeInHours() {

		/* negative because this is in the future */
		return -4;
	}

	@Override
	public double getRadStartMDPosition() {
		return Constants.radMDToStartObs;
	}

	@Override
	public List<PointingTO> getDefaultPointings() {
		System.err.println("Setting " + DBManager.getAllPointings().size() + " default pointings for Interfaced SMIRF scheduler");
		
//		if(EphemService.getAngleLMSTForMolongloNow().getDecimalHourValue() > 7.5 && EphemService.getAngleLMSTForMolongloNow().getDecimalHourValue() < 19){
//			
//			System.err.println("Providing just SMIRF pointings");
//			return DBManager.getAllPointings().stream().filter(f -> f.getPointingName().contains(SMIRFConstants.SMIRFPointingPrefix)).collect(Collectors.toList());		
//		}
//		
		System.err.println("Providing all pointings as default.");

		
		return DBManager.getAllPointings();//.stream().filter(f -> !f.getPointingName().contains(SMIRFConstants.SMIRFPointingPrefix)).collect(Collectors.toList());		
	} 

	public PointingTO getPointingFromExternalInterface() throws EmptyCoordinatesException, CoordinateOverrideException {
		PointingTO external = null;
		/**
		 * To do: Call Stefan's code here. As of now the code does nothing.
		 *  
		 */	

		if(external == null) return null;

		Angle HA = new Coords(external, EphemService.getAngleLMSTForMolongloNow()).getAngleHA();

		/**
		 * if the HA is positive meaning it has already transited ( hopefully not, but in case Stefan's code has a bug, let's check) or if the 
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






	public static void main(String[] args) throws CoordinateOverrideException, EmptyCoordinatesException, TCCException, NoSourceVisibleException, SchedulerException, InterruptedException {

		StatusPooler poller = new StatusPooler();
		poller.startPollingThreads();
		Thread.sleep(2000);
		UserInputs inputs = new UserInputs();

		inputs.setSchedulerType(SMIRFConstants.interfacedDynamicScheduler);

		inputs.setUtcStart("2018-02-14-05:06:39");
		
		Control.setEmulateForUTC(inputs.getUtcStart());
		
		inputs.setTobsInSecs(360);
		inputs.setSessionTimeInSecs( 3600 );

		inputs.setDoPulsarSearching(true);
		inputs.setDoPulsarTiming(true);
		inputs.setEnableTCC(true);
		inputs.setEnableBackend(true);
		inputs.setMdTransit(true);
		inputs.setObserver("VVK");
		inputs.setNsOffsetInDeg(0.0);
		inputs.setNsSpeed(TCCConstants.slewRateNSFast  );
		
		List<PointingTO> tos =DBManager.getAllPointingsForPointingType("G");
		
		InterfacedDynamicScheduler ids1 = new InterfacedDynamicScheduler();
		ids1.init(inputs);
		
		for(PointingTO to: tos) {
			ids1.addToSession(to);
			System.err.println(ids1.thisSession.size() + " " + ids1.thisSession);
		}
		
		System.exit(0);
	
		
		System.err.println(tos);
		
		for(int i=0; i< 1; i++) {
		
		inputs.setPointingTOs(tos);
				
		InterfacedDynamicScheduler ids = new InterfacedDynamicScheduler();
		ids.init(inputs);
		PointingTO to = ids.next();
		System.err.println(to);		
		to.setNumObs(to.getNumObs() + 1);

		}

		poller.contextDestroyed(null);
		Control.terminate();
		Control.getExecutorService().shutdown();
		
	}

}
