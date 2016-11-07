package test;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import bean.Angle;
import bean.CoordinateTO;
import bean.Observation;
import exceptions.BackendException;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.PointingException;
import exceptions.TCCException;
import manager.MolongloCoordinateTransforms;
import manager.ObservationManager;
import manager.ScheduleManager;
import util.BackendConstants;
import util.SMIRFConstants;

public class Main {
	public static void main(String[] args) throws InterruptedException, TCCException, BackendException, IOException, EmptyCoordinatesException, CoordinateOverrideException, PointingException {
		
		ScheduleManager scheduleManager = new ScheduleManager();
		scheduleManager.Calibrate("CJXXXX_XXXX");
		scheduleManager.observeTestPSR();
		Instant instant = Instant.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
		scheduleManager.startScheduler(formatter.format(instant).replaceAll("T", "-"), 900, SMIRFConstants.tobs, "VVK");
		
		
		
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
		observation.setName("J2144-3933");
		observation.setAngleRA(new Angle("21:44:12.060404", Angle.HHMMSS));
		observation.setAngleDEC(new Angle("-39:33:56.88504",Angle.DDMMSS));
		observation.setTobs(900);
		observation.setBackendType(BackendConstants.psrBackend);
		observation.setObserver("VVK");
		observation.setObsType(BackendConstants.tiedArrayFanBeam);
		
	
		CoordinateTO coordinateTO = new CoordinateTO(observation);
		MolongloCoordinateTransforms.skyToTel(coordinateTO);
		
//		BackendService bs = BackendService.createBackendInstance();
//		bs.startBackend(observation);
//		
		manager.observe(observation);
		
//		System.err.println("After observation");
//		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
//		
//		for(Thread t: threadSet) System.err.println(t);
	}
}
