package manager;

import java.util.List;

import bean.PointingTO;
import bean.UserInputs;
import exceptions.SchedulerException;
import util.SMIRFConstants;

public class PulsarDynamicTransitScheduler extends DynamicTransitScheduler{
		
	@Override
	protected double futureLookUpTimeInHours() {
		
		/* negative because this is in the future */
		return -11.9;
	}
	
	@Override
	public List<PointingTO> getDefaultPointings() {
		List<PointingTO> list= DBManager.getAllPointingsForPointingType(SMIRFConstants.psrPointingSymbol);
		list.addAll(DBManager.getAllPointingsForPointingType(SMIRFConstants.frbFieldPointingSymbol));
		return list;
	}
	
	
	@Override
	public void init(UserInputs userInputs) throws SchedulerException {
		this.userInputs = userInputs;

		this.userInputs.setPointingTOs(getDefaultPointings());
		
		System.err.println("Assed default pointings: "+ this.userInputs.getPointingTOs().size());
		
		this.userInputs.getPointingTOs().stream().forEach( f -> System.err.println(f.getPointingName()));
		
		if(!userInputs.getSchedulerType().equals(SMIRFConstants.pulsarDynamicTransitScheduler)){
			throw new SchedulerException("Mismatch between scheduler object and type");
		}

	}

	
	


}
