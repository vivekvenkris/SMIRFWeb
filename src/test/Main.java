package test;

import java.io.IOException;

import bean.CoordinateTO;
import bean.Observation;
import bean.TBSource;
import exceptions.BackendException;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.TCCException;
import manager.MolongloCoordinateTransforms;
import manager.ObservationManager;
import service.BackendService;
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
		observation.setAngleDEC(new Angle("-45:59:09.5",Angle.DDMMSS));
		observation.setTobs(900);
		observation.setBackendType(BackendConstants.correlation);
		observation.setObserver("VVK");
		observation.setObsType(BackendConstants.correlation);
		TBSource tbs1 = new TBSource();
		tbs1.setAngleRA(new Angle("16:44:49.281", Angle.HHMMSS));
		tbs1.setAngleDEC(new Angle("-45:59:09.5",Angle.DDMMSS));
		tbs1.setPsrName("J1644-4559");
		
		TBSource tbs2 = new TBSource();
		tbs2.setAngleRA(new Angle("16:44:49.281", Angle.HHMMSS));
		tbs2.setAngleDEC(new Angle("-45:59:09.5",Angle.DDMMSS));
		tbs2.setPsrName("J1644-4559");
		tbs2.setDspsrParams(new TBSource.DSPSRParameters(10.0,0.2,-20.0));
		System.err.println("starting observation...");
		
		
		
		observation.getTiedBeamSources().add(tbs1);
		observation.getTiedBeamSources().add(tbs2);		
		observation.getTiedBeamSources().add(tbs1);
		observation.getTiedBeamSources().add(tbs2);
		
		CoordinateTO coordinateTO = new CoordinateTO(observation);
		MolongloCoordinateTransforms.skyToTel(coordinateTO);
		
		BackendService bs = BackendService.createBackendInstance();
		bs.startBackend(observation);
		
		//manager.observe(observation);
		
//		System.err.println("After observation");
//		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
//		
//		for(Thread t: threadSet) System.err.println(t);
	}
}
