package manager;

import java.io.IOException;

import bean.CoordinateTO;
import bean.Observation;
import exceptions.BackendException;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.TCCException;
import service.CalibrationService;
import util.Angle;
import util.BackendConstants;

public class ScheduleManager {


	public boolean Calibrate(String name) throws TCCException, BackendException, InterruptedException, IOException, EmptyCoordinatesException, CoordinateOverrideException{

		Observation observation = new Observation();
		observation.setName("");
		observation.setAngleRA(new Angle("", Angle.HHMMSS));
		observation.setAngleDEC(new Angle("",Angle.DDMMSS));
		observation.setTobs(30);
		observation.setBackendType(BackendConstants.corrBackend);
		observation.setObserver("VVK");
		observation.setObsType(BackendConstants.correlation);
		System.err.println("starting observation...");

		CoordinateTO coordinateTO = new CoordinateTO(observation);
		MolongloCoordinateTransforms.skyToTel(coordinateTO);
		ObservationManager manager = new ObservationManager();
		manager.observe(observation);

		String utc = observation.getUtc();
		System.err.println("utc:" + utc);
		CalibrationService service = new CalibrationService();
		return service.Calibrate(utc);
		

		
	}
}

