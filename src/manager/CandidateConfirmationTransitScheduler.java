package manager;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import bean.Angle;
import bean.CoordinateTO;
import bean.Coords;
import bean.PointingTO;
import bean.UserInputs;
import control.Control;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.NoSourceVisibleException;
import exceptions.ScheduleEndedException;
import exceptions.SchedulerException;
import exceptions.TCCException;
import service.EphemService;
import service.TCCStatusService;
import util.Constants;
import util.SMIRFConstants;

public class CandidateConfirmationTransitScheduler extends TransitScheduler {

	List<PointingTO> pointingTOs = null;
	
	@Override
	public PointingTO next()
			throws CoordinateOverrideException, EmptyCoordinatesException, TCCException, NoSourceVisibleException, SchedulerException {
		
		if(pointingTOs.isEmpty()) 	throw new ScheduleEndedException("All pointings observed. Schedule ended.");
		
		/**
		 *  Get the present telescope position
		 */
		Coords telescopePosition = new Coords(Control.getTccStatus());
		CoordinateTO initTelPosition = new CoordinateTO(null, null, telescopePosition.getAngleNS().getRadianValue(), 0.0);
		
		
		/**
		 * Get positions of pointings (coords) in local coordinates for LST = now, sorted by increasing distance from Meridian.
		 */
		
		Angle initLST = EphemService.getAngleLMSTForMolongloNow();
		if(Control.getCurrentObservation() !=null) initLST.addSolarSeconds(userInputs.getTobsInSecs());
		
		List<Coords> coords = getCoordsListForLST(pointingTOs, initLST, initTelPosition, userInputs.getNsSpeed());
		
		if(coords.isEmpty()) throw new NoSourceVisibleException("No pointings visible to observe");
		
		return coords.stream()
				.filter(a -> Math.abs(a.getAngleMD().getDegreeValue()) < Constants.RadMolongloMDBeamWidth)
				.sorted(Comparator.comparing(a -> ((Coords)a).getAngleMD().getDegreeValue()))
				.collect(Collectors.toList())
				.get(0)
				.getPointingTO();
		
		
		
	}

	@Override
	public void init(UserInputs userInputs) throws SchedulerException {
		this.userInputs = userInputs;
		

		if(!userInputs.getSchedulerType().equals(SMIRFConstants.candidateConfirmationTransitScheduler)){
			throw new SchedulerException("Mismatch between scheduler object and type");
		}

		List<PointingTO> pointingTOs = this.userInputs.getPointingTOs();
		
		if(pointingTOs == null || pointingTOs.isEmpty()){
			throw new SchedulerException("No pointings given to scheduler to observe. Pointing list size = 0 ");
		}
		
		this.pointingTOs = pointingTOs;

	}
	
	@Override
	public double getRadStartMDPosition() {
		
		return Constants.RadMolongloMDBeamWidth;
		
	}



}
