package test;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import bean.Angle;
import bean.CoordinateTO;
import bean.Observation;
import bean.TBSource;
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
		TBSource tbs1 = new TBSource();
		tbs1.setAngleRA(new Angle("21:44:12.060404", Angle.HHMMSS));
		tbs1.setAngleDEC(new Angle("-39:33:56.88504",Angle.DDMMSS));
		tbs1.setPsrName("J2144-3933");
		
		TBSource tbs2 = new TBSource();
		tbs2.setAngleRA(new Angle("22:41:12.060404", Angle.HHMMSS));
		tbs2.setAngleDEC(new Angle("-52:36:56.88504",Angle.DDMMSS));
		tbs2.setPsrName("J2241-5236");
		
		TBSource tbs3 = new TBSource();
		tbs3.setAngleRA(new Angle("16:44:00", Angle.HHMMSS));
		tbs3.setAngleDEC(new Angle("-45:59:00",Angle.DDMMSS));
		tbs3.setPsrName("J1644-4559");
		
		TBSource tbs4 = new TBSource();
		tbs4.setAngleRA(new Angle("21:24:43.849372", Angle.HHMMSS));
		tbs4.setAngleDEC(new Angle("-33:58:44.8500",Angle.DDMMSS));
		tbs4.setPsrName("J2124-3358");
		
		
		
		observation.getTiedBeamSources().add(tbs1);
		observation.getTiedBeamSources().add(tbs2);
		observation.getTiedBeamSources().add(tbs3);
		observation.getTiedBeamSources().add(tbs4);
	
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
