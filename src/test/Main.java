package test;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import bean.Angle;
import bean.Coords;
import bean.ObservationTO;
import bean.Pointing;
import bean.PointingTO;
import bean.TBSourceTO;
import bean.UserInputs;
import exceptions.BackendException;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.EphemException;
import exceptions.NoSourceVisibleException;
import exceptions.ObservationException;
import exceptions.PointingException;
import exceptions.SchedulerException;
import exceptions.TCCException;
import listeners.StatusPooler;
import mailer.Mailer;
import manager.DBManager;
import manager.DynamicTransitScheduler;
import manager.ObservationManager;
import manager.PSRCATManager;
import manager.Schedulable;
import manager.TransitScheduler;
import service.DBService;
import service.EphemService;
import service.TCCService;
import service.TCCStatusService;
import util.BackendConstants;
import util.ConfigManager;
import util.Constants;
import util.SMIRFConstants;
import util.TCCConstants;
import util.Utilities;

public class Main {
	
	


	static double distance(TBSourceTO tb, PointingTO smirf) {
		return Math.abs(tb.getAngleDEC().getDegreeValue() - smirf.getAngleDEC().getDegreeValue());
	}
	public static void main(String[] args) throws InterruptedException, TCCException,
	BackendException, IOException, EmptyCoordinatesException, CoordinateOverrideException, 
	PointingException, NoSourceVisibleException, SchedulerException, ObservationException, 
	EphemException, ParserConfigurationException, SAXException, ParseException {
		String utc = "";

		
		utc = "2018-04-04-13:56:54";
		
		ObservationTO tot = new ObservationTO(new Coords(DBManager.getPointingByUniqueName("SMIRF_1257-6651"),
				Utilities.getUTCLocalDateTime(utc)),
				null, 360, "VVK", BackendConstants.tiedArrayFanBeam,SMIRFConstants.pulsarPointingPrefix, "P000", 0.0, true, false, false, true, true);

		new ObservationManager().getTBSourcesForObservation(tot, 4);

		System.err.println(tot.getTiedBeamSources());

		
		System.exit(0);
	
		PointingTO to12 = new PointingTO(new Angle("17:05:37.7", Angle.HHMMSS), new Angle("-53:50:16.00", Angle.DDMMSS), "CAN_1705-5350", SMIRFConstants.candidatePointingSymbol);
		Pointing pointing = new Pointing(to12);
		DBService.addPointingsToDB(Arrays.asList(new Pointing[] {pointing}));
		System.exit(0);
		
		BackendException e = new BackendException("testing screenshots of buffers. please ignore this email.");

		Mailer.sendEmail(e);

		System.exit(0);
		
		List<PointingTO> pointingTOs = DBManager.getAllPointings().stream().filter(f -> f.getAngleRA().getDecimalHourValue() > 8 
				&& f.getAngleRA().getDecimalHourValue()<19 && f.getPointingName().startsWith("PSR") ).collect(Collectors.toList());

		System.err.println(pointingTOs.size());
		
		pointingTOs.forEach(f -> f.setNumObs(f.getNumObs() + 2));
		
		DBService.updatePointingsToDB(pointingTOs);
		
		System.err.println(pointingTOs.stream().mapToDouble(f -> f.getNumObs()).sum()/pointingTOs.size());
		
		
		
		System.exit(0);
		
		Pointing pto = new Pointing(new PointingTO(PSRCATManager.getTBSouceByName("J1038+0032")));
		pto.setPointingID(null);
		pto.setPointingName("PSR_"+pto.getPointingName());;
		pto.setType(SMIRFConstants.pulsarPointingSymbol);
		pto.setPriority(SMIRFConstants.highestPriority);
		pto.setNumObs(0);
		pto.setTobs(SMIRFConstants.tobs);
		DBService.addPointingsToDB(new ArrayList<>(Arrays.asList(new Pointing[] {pto})));
		System.err.println(pto);
		System.exit(0);
		String a = "";
		System.err.println("-"+String.join(",",new ArrayList<TBSourceTO>().stream().map(f -> f.getPsrName()).collect(Collectors.toList())));
		System.exit(0);

		Angle dec2 = PSRCATManager.getTimingProgrammeSouceByName("J2033+0042").getAngleDEC();
		System.err.println(dec2.toDDMMSS());
		System.exit(0);

		List<PointingTO> smirfPointings = DBManager.getAllPointings().stream().filter(f -> f.getPointingName().contains("SMIRF_")).collect(Collectors.toList());
		List<TBSourceTO> timingProgramme = PSRCATManager.getTimingProgrammeSources();
		
		DBService.updatePulsarLastObservedTimes();

		
		List<String> values = new ArrayList<>();
		for(TBSourceTO tb : timingProgramme) {
			
			if(tb.getDaysSinceLastObserved() < 10 ) continue;

			PointingTO minTO = null;
			double minDist = 0.0;

			for(PointingTO smirf : smirfPointings ) {
				if(!Utilities
						.isWithinCircle(tb.getAngleRA().getRadianValue(), tb.getAngleDEC().getRadianValue(),
								smirf.getAngleRA().getRadianValue(), smirf.getAngleDEC().getRadianValue(), 1 * Constants.deg2Rad)) continue;
				double dist = distance(tb, smirf);
				if(minTO == null || minDist > dist ) {
					minTO = smirf;
					minDist = dist;
				}
			}

			values.add(tb.getPsrName() + " " +  tb.getDaysSinceLastObserved() + " " + minTO + " " + minDist);		
		}

		
		values.stream().forEach(f -> System.err.println(f));
		
		System.exit(0);

		List<String> timingProgrammePulsars = Files.readAllLines(Paths.get(ConfigManager.getSmirfMap().get("TIMING_PROGRAMME")));

		DBService.updatePulsarLastObservedTimes();

		PSRCATManager.getTimingProgrammeSources().stream().filter(f -> (((TBSourceTO)f).getDaysSinceLastObserved() > 10 
				&& Math.abs(new PointingTO(f).getAngleLAT().getDegreeValue()) > 4 )).forEach( f -> {
					System.err.println(f.getPsrName() + " " + new PointingTO(f).getAngleLAT().getDegreeValue() + " "  );
					//System.err.println(DBManager.getPointingByUniqueName("PSR_"+f.getPsrName()));
				});

		System.err.println(timingProgrammePulsars.size() + " " + PSRCATManager.getTimingProgrammeSources().size());

		timingProgrammePulsars.stream().forEach(f -> {

			if (!PSRCATManager.getTimingProgrammeSources().contains(new TBSourceTO(f))) {
				System.err.println(f);
			}
		});

		TCCService tccService = TCCService.createTccInstance();

		System.exit(0);

		tccService.pointNS(new Angle("25.0", Angle.DEG, Angle.DEG),TCCConstants.BOTH_ARMS);

		TCCStatusService tccStatusService = new TCCStatusService();
		while(tccStatusService.isTelescopeDriving()) {
			System.err.println("still driving...");
			Thread.sleep(10000);
		}


		System.exit(0);

		


	
		System.exit(0);

		StatusPooler poller = new StatusPooler();
		poller.startPollingThreads();

		Thread.sleep(3000);

		TCCException tccE = new TCCException("The telescope is on FIRE!");

		Mailer.sendEmail(tccE);

		System.exit(0);




		utc = EphemService.getUtcStringNow();

		String command = "cd /home/vivek/SMIRF/screenshots/; "
				+ "/home/vivek/.npm-global/bin/pageres -d 5 --filename='"+ utc + "' 'http://mpsr-srv0/mopsr/control.lib.php?single=true'";

		Utilities.runShellProcess(command, true);

		System.exit(0);

		InetAddress inet;

		inet = InetAddress.getByName(TCCConstants.tccControllerIP);
		System.out.println("Sending Ping Request to " + inet);
		System.out.println(inet.isReachable(5000) ? "Host is reachable" : "Host is NOT reachable");

		inet = InetAddress.getByAddress(new byte[] { (byte) 173, (byte) 194, 32, 38 });
		System.out.println("Sending Ping Request to " + inet);
		System.out.println(inet.isReachable(5000) ? "Host is reachable" : "Host is NOT reachable");

		System.exit(0);

		System.err.println(PSRCATManager.getTimingProgrammeSources().get(
				PSRCATManager.getTimingProgrammeSources().indexOf(new TBSourceTO("J2129-5721"))).getEphemerides());

		Angle dec = new Angle("-57:21:14.21183",Angle.DDMMSS);
		String ddmmss = "-57:21:14.21183";
		String[] dms = ddmmss.split(":");
		int sign = (Integer.parseInt(dms[0])>0)? 1:-1;
		if(Integer.parseInt(dms[0]) == 0) sign =  (dms[0].contains("-"))? -1:1; 
		System.err.println("sign:" + sign);
		double radValue = 0.0;
		if(dms.length >=1) radValue+= Integer.parseInt(dms[0])*Constants.deg2Rad; 
		if(dms.length >=2) radValue+= sign*Integer.parseInt(dms[1])*Constants.deg2Rad/60.0;		
		if(dms.length >=3) radValue+= sign*Double.parseDouble(dms[2])*Constants.deg2Rad/3600.0;
		System.err.println(radValue + " " + Angle.toDDMMSS(radValue));

		System.exit(0);

		//StatusPooler poller = new StatusPooler();
		poller.startPollingThreads();

		TransitScheduler s2 = new DynamicTransitScheduler();

		UserInputs inputs = new UserInputs();

		inputs.setSchedulerType(SMIRFConstants.dynamicTransitScheduler);

		inputs.setTobsInSecs(360);
		inputs.setSessionTimeInSecs(3600 );

		inputs.setDoPulsarSearching(true);
		inputs.setDoPulsarTiming(true);
		inputs.setEnableTCC(true);
		inputs.setEnableBackend(true);
		inputs.setMdTransit(true);
		inputs.setObserver("VVK");
		inputs.setNsOffsetInDeg(0.0);
		inputs.setNsSpeed("FAST".equals("Fast") ? TCCConstants.slewRateNSFast : TCCConstants.slewRateNSSlow );
		inputs.setPointingTOs(new ArrayList<>());
		s2.init(inputs);
		PointingTO to = s2.next();
		System.err.println(s2.getWaitTimeInHours(to));

		System.exit(0);


		System.err.println(Constants.RadMolongloMDBeamWidthForFB * Constants.rad2Deg / 511);

		System.exit(0);

		List<String> points = Files.readAllLines(Paths.get("/home/vivek/frb.points"));

		List<PointingTO> tos = points.stream().map(f -> {
			String s = f;
			String[] chunks = s.trim().split(" ");
			return new PointingTO(new Angle(chunks[0], Angle.HHMMSS), new Angle(chunks[1], Angle.DDMMSS));
		}).collect(Collectors.toList());

		System.err.println("Initial size: " + tos.size());

		List<PointingTO> shortlisted = new ArrayList<>();

		tos.forEach(f -> {

			boolean done = false;

			for ( PointingTO p: shortlisted){
				double distance = Math.sqrt(Utilities.equatorialDistance(p.getAngleRA().getRadianValue(),p.getAngleDEC().getRadianValue(),
						f.getAngleRA().getRadianValue(), f.getAngleDEC().getRadianValue()));

				if(distance < (1.0 * Constants.deg2Rad)){
					done  = true;
					System.err.println(p.getAngleRA() + " " + p.getAngleDEC() + " covers " + f.getAngleRA() + " " + f.getAngleDEC());
					break;
				}
			}
			if(!done) shortlisted.add(f);

		});

		shortlisted.forEach(f -> System.out.println(f.getAngleRA() + " " + f.getAngleDEC()));

		System.err.println("Final size: " + shortlisted.size());




	}

}	

//CoordinateTO to = new CoordinateTO(0.0, new Angle("0.0",Angle.DEG, Angle.DDMMSS).getRadianValue(), null, null);
//
//MolongloCoordinateTransforms.skyToTel(to);
//
//System.err.println(to.getAngleNS().getDegreeValue() + " " + to.getAngleMD().getDegreeValue());

//		TransitScheduleManager manager = new TransitScheduleManager();
//		String utc = EphemService.getUtcStringNow();
//		ObservationSessionTO observationSessionTO = new ObservationSessionTO(utc, "VVK", 360 , 36000, null, false, false, false, 0);
//		DBManager.addSessionToDB(observationSessionTO);
//
//		manager.start(36000, 360, "VVK", true, true, true, true, observationSessionTO, 
//				 TCCConstants.slewRateNSFast ,0.0);
//		

//		List<Integer> a = new ArrayList<>();
//		for ( int i=0; i< 100; i++ ) a.add(i);
//		
//		System.err.println(a.stream().filter(f -> f.intValue() < 0).collect(Collectors.toList()));


//		ScheduleManager scheduleManager = new ScheduleManager();
//		//scheduleManager.Calibrate("CJXXXX_XXXX");
//		//scheduleManager.observeTestPSR();
//		Instant instant = Instant.now();
//		instant = instant.plusSeconds(86400);
//		System.err.println(instant.toString().replaceAll("T", "-").replaceAll("Z", ""));
//		System.err.println(instant.toString().replaceAll("T", "-").replaceAll("Z", "").charAt(19));
//		scheduleManager.startSMIRFScheduler(instant.toString().replaceAll("T", "-").replaceAll("Z", ""), 900, SMIRFConstants.tobs, "VVK");
//		System.err.println(InetAddress.getLocalHost().getHostName());
//		System.err.println(BackendConstants.maximumNumberOfTB);
//		System.err.println("Test");
//		
//	System.err.println(EphemService.getAngleLMSTForMolongloNow());


//		ObservationManager manager = new ObservationManager();	

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

// this will no  longer work as observation has coords which in turn has pointingTO

//		Observation observation = new Observation();
//		observation.setName("J2144-3933");
//		observation.setAngleRA(new Angle("21:44:12.060404", Angle.HHMMSS));
//		observation.setAngleDEC(new Angle("-39:33:56.88504",Angle.DDMMSS));
//		observation.setTobs(900);
//		observation.setBackendType(BackendConstants.psrBackend);
//		observation.setObserver("VVK");
//		observation.setObsType(BackendConstants.tiedArrayFanBeam);
//		
//	
//		CoordinateTO coordinateTO = new CoordinateTO(observation);
//		MolongloCoordinateTransforms.skyToTel(coordinateTO);
//		manager.observe(observation);

//		BackendService bs = BackendService.createBackendInstance();
//		bs.startBackend(observation);
//		

//		System.err.println("After observation");
//		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
//		
//		for(Thread t: threadSet) System.err.println(t);

