package manager;

import java.io.IOException;

import bean.ObservationTO;
import exceptions.ObservationException;
import service.CalibrationService;
import service.DBService;
import util.SMIRFConstants;
import util.Switches;

public class PostObservationManager {

	
	public static boolean CalibrateAndUpdateDelays(ObservationTO observation) throws IOException, InterruptedException{
		return Switches.simulate ?  true: new CalibrationService().Calibrate( observation.getUtc(), observation.getCoords().getPointingTO().getPointingName());
	}
	
	public static void doPostObservationStuff(ObservationTO observation) throws ObservationException, IOException, InterruptedException{
		if(observation.isSurveyPointing()){
			DBService.incrementPointingObservations(observation.getCoords().getPointingTO().getPointingID());
			// call peasoup controller.
			}
		if(observation.isPhaseCalPointing()){
			CalibrateAndUpdateDelays(observation);
		}
	}
}
