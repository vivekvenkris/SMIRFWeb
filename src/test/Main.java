package test;

import java.io.IOException;
import java.util.Set;

import bean.CoordinateTO;
import bean.Observation;
import exceptions.BackendException;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.TCCException;
import manager.MolongloCoordinateTransforms;
import manager.ObservationManager;
import service.CalibrationService;
import util.Angle;
import util.BackendConstants;

public class Main {
	public static void main(String[] args) throws InterruptedException, TCCException, BackendException, IOException, EmptyCoordinatesException, CoordinateOverrideException {
		
		
		ObservationManager manager = new ObservationManager();		
//		Observation observation = new Observation();
//		observation.setName("CJ1819-6345");
//		observation.setAngleRA(new Angle("18:19:35.0", Angle.HHMMSS));
//		observation.setAngleDec(new Angle("-63:45:48.6",Angle.DDMMSS));
//		observation.setTobs(900);
//		observation.setBackendType(BackendConstants.corrBackend);
//		observation.setObserver("VVK");
//		observation.setObsType(BackendConstants.correlation);
//		System.err.println("starting observation...");
//		
//		CoordinateTO coordinateTO = new CoordinateTO(observation);
//		MolongloCoordinateTransforms.Sky2Tel(coordinateTO);
//		
//		manager.observe(observation);
//		CalibrationService service = new CalibrationService();
//		service.Calibrate(observation.getUtc());
		
		
		Observation observation = new Observation();
		observation.setName("J1644-4559");
		observation.setAngleRA(new Angle("16:44:49.281", Angle.HHMMSS));
		observation.setAngleDec(new Angle("-45:59:09.5",Angle.DDMMSS));
		observation.setTobs(900);
		observation.setBackendType(BackendConstants.psrBackend);
		observation.setObserver("VVK");
		observation.setObsType(BackendConstants.tiedArrayFanBeam);
		System.err.println("starting observation...");
		
		CoordinateTO coordinateTO = new CoordinateTO(observation);
		MolongloCoordinateTransforms.skyToTel(coordinateTO);
		
		manager.observe(observation);
		
//		System.err.println("After observation");
//		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
//		
//		for(Thread t: threadSet) System.err.println(t);
	}
}
