package manager;

import java.io.IOException;

import bean.Observation;
import exceptions.ObservationException;
import service.CalibrationService;
import service.DBService;
import util.SMIRFConstants;

public class PostObservationManager {

	
	public static boolean CalibrateAndUpdateDelays(Observation observation) throws IOException, InterruptedException{
		return SMIRFConstants.simulate ?  true: new CalibrationService().Calibrate( observation.getUtc(), observation.getCoords().getPointingTO().getPointingName());
	}
	
	public static boolean startPeasoup(Observation observation){
		
		return true;
	}
	
	public static void doPostObservationStuff(Observation observation) throws ObservationException, IOException, InterruptedException{
		if(observation.isSurveyPointing()){
			DBService.incrementPointingObservations(observation.getCoords().getPointingTO().getPointingID());
			// call peasoup controller.
			}
		if(observation.isPhaseCalPointing()){
			CalibrateAndUpdateDelays(observation);
		}
	}
}
