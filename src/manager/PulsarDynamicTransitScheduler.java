package manager;

import java.util.List;

import bean.PointingTO;
import util.SMIRFConstants;

public class PulsarDynamicTransitScheduler extends DynamicTransitScheduler{
		
	@Override
	protected double futureLookUpTimeInHours() {
		
		/* negative because this is in the future */
		return -11.59;
	}
	
	@Override
	public List<PointingTO> getDefaultPointings() {
		return DBManager.getAllPointingsForPointingType(SMIRFConstants.psrPointingSymbol);
	}
	
	

	
	


}
