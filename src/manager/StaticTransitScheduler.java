package manager;

import java.util.ArrayList;
import java.util.List;

import bean.PointingTO;
import bean.UserInputs;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.NoSourceVisibleException;
import exceptions.ScheduleEndedException;
import exceptions.SchedulerException;
import exceptions.TCCException;
import util.Constants;
import util.SMIRFConstants;

public class StaticTransitScheduler extends TransitScheduler {

	List<PointingTO> pointingTOs = null;
	Integer index = 0;
	
	@Override
	public PointingTO next()
			throws CoordinateOverrideException, EmptyCoordinatesException, TCCException, NoSourceVisibleException, SchedulerException {
		
		System.err.println("Getting index: " + index + " out of " + pointingTOs.size() + ".");
		
		if(index < pointingTOs.size()) return pointingTOs.get(index++);
		
		throw new ScheduleEndedException("All pointings observed. Schedule ended.");
		
	}

	@Override
	public void init(UserInputs userInputs) throws SchedulerException {
		this.userInputs = userInputs;
		

		if(!userInputs.getSchedulerType().equals(SMIRFConstants.staticTransitScheduler)){
			throw new SchedulerException("Mismatch between scheduler object and type");
		}

		List<PointingTO> pointingTOs = this.userInputs.getPointingTOs();
		
		if(pointingTOs == null || pointingTOs.isEmpty()){
			throw new SchedulerException("No pointings given to scheduler to observe. Pointing list size = 0 ");
		}
		
		this.pointingTOs = pointingTOs;
		this.index = 0;

	}

	@Override
	public double getRadStartMDPosition() {
		
		return Constants.radMDEastHPBW;
		
	}

	@Override
	public List<PointingTO> getDefaultPointings() {
		return new ArrayList<PointingTO>();
	}

	

}
