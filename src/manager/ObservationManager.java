package manager;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import bean.Angle;
import bean.CoordinateTO;
import bean.Coords;
import bean.ObservationTO;
import bean.TBSourceTO;
import exceptions.BackendException;
import exceptions.CoordinateOverrideException;
import exceptions.DriveBrokenException;
import exceptions.EmptyCoordinatesException;
import exceptions.ObservationException;
import exceptions.TCCException;
import service.BackendService;
import service.TCCService;
import service.TCCStatusService;
import util.BackendConstants;
import util.Constants;
import util.SMIRFConstants;
import util.Utilities;

public class ObservationManager {
	ExecutorService executorService = null;

	public synchronized void startObserving(ObservationTO observation) throws TCCException, BackendException, EmptyCoordinatesException, CoordinateOverrideException, ObservationException, InterruptedException{
		startObserving(observation, true, true);
	}

	public synchronized void  startObserving(ObservationTO observation, boolean tccEnabled, boolean backendEnabled) throws TCCException, BackendException, InterruptedException, EmptyCoordinatesException, CoordinateOverrideException, ObservationException{

		executorService = Executors.newFixedThreadPool(4);
		Future<Boolean> backendReady = null;
		Future<Boolean> tccReady = null;


		if(tccEnabled) {
			if( !observable(observation)) {
				System.err.println("not observable.");
				throw new ObservationException("cant observe this source. Source not visible." + observation.getCoords().getAngleHA() + " " 
						+ observation.getCoords().getAngleNS() + " "+ observation.getCoords().getAngleMD());
			}

			TCCService       tccService =     TCCService.createTccInstance();
			TCCStatusService tccStatusService = new TCCStatusService();

			CoordinateTO coordinates = new CoordinateTO(observation);
			MolongloCoordinateTransforms.skyToTel(coordinates);

			Callable<Boolean> getTCCReady = new Callable<Boolean>() {

				@Override
				public Boolean call() throws TCCException, InterruptedException{
					System.err.println("starting TCC..");
					tccService.pointAndTrackSource(observation.getCoords().getPointingTO().getAngleRA().toHHMMSS(), observation.getCoords().getPointingTO().getAngleDEC().toDDMMSS());
					System.err.println("started TCC..");
					long maxSlewTime = TCCManager.computeSlewTime(coordinates.getRadNS(), coordinates.getRadMD())*1000; // to milliseconds
					System.err.println(" max slew time.." + maxSlewTime);
					long startTime = System.currentTimeMillis();
					long elapsedTime = 0L;
					Thread.sleep(5000);
					while(true){
						if(!tccStatusService.getTelescopeStatus().isTelescopeDriving()) break;
						elapsedTime = (new Date()).getTime() - startTime;
						System.err.print("\rslewing for the past "+ elapsedTime/1000.0 + " seconds");
						if(elapsedTime > maxSlewTime) {
							throw new DriveBrokenException("Drive taking longer than expected to go to source.");
						}
						Thread.sleep(2000);
					}
					System.err.println("TCC reached..");
					return true;
				}


			};

			tccReady =  executorService.submit(getTCCReady);


		}

		if(backendEnabled) {
			BackendService   backendService = BackendService.createBackendInstance();
			BackendService   backendStatus =  BackendService.createBackendStatusInstance();

			Callable<Boolean> getBackendReady = new Callable<Boolean>() {

				@Override
				public Boolean call() throws BackendException {

					if(SMIRFConstants.simulate) return true;

					boolean isON = backendStatus.isON();
					if(isON){
						System.err.println("backend is ON");
						boolean isidle = backendStatus.isIdle();
						if(!isidle) backendService.stopBackend();
					}
					//				String currentBackend;
					//				currentBackend = backendService.getCurrentBackend();
					//				System.err.println("Current Backend:" + currentBackend);
					//				System.err.println("Required backend:" + observation.getBackendType());
					//				if(!currentBackend.equals(observation.getBackendType())){
					//					System.err.println("backend change needed..");
					//					if(isON) {
					//						backendService.shutDownBackend();
					//					}
					//					backendService.changeBackendConfig(observation.getBackendType());
					//				}
					//				System.err.println("Booting backend..");
					//				backendService.bootUpBackend();
					//				System.err.println("backend booted..");
					return true;
				}
			};
			backendReady = executorService.submit(getBackendReady);
		}

		if(backendEnabled) {
			try {
				Boolean backendResult = backendReady.get();
			} catch (InterruptedException | ExecutionException e) {
				Throwable t = e.getCause();
				if(t instanceof BackendException){
					throw (BackendException)t;
				}
				e.printStackTrace();
			}
		}

		if(tccEnabled)  { 
			try {
				Boolean tccResult = tccReady.get();
			} catch (InterruptedException | ExecutionException e) {
				Throwable t = e.getCause();
				if(t instanceof TCCException){
					throw (TCCException)t;
				}
				e.printStackTrace();
			}
		}
		
		executorService.shutdown();
		
		if(!observation.getCoords().getPointingTO().getType().equals(SMIRFConstants.phaseCalibratorSymbol)) {
			observation.setTiedBeamSources(getTBSourcesForObservation(observation));
		}
		System.err.println(observation.getTiedBeamSources());
		if(backendEnabled) {
			System.err.println("starting backend..");
		BackendService   backendService = BackendService.createBackendInstance();
		backendService.startBackend(observation);
		}
		System.err.println("starting observing..");
	}

	public boolean stopObserving() throws TCCException, BackendException, InterruptedException{
		return stopObserving(true, true);
	}

	public boolean stopObserving(boolean tccEnabled, boolean backendEnabled) throws TCCException, BackendException, InterruptedException{

		TCCService       tccService =     TCCService.createTccInstance();
		TCCStatusService tccStatusService = new TCCStatusService();
		BackendService   backendService = BackendService.createBackendInstance();

		if(tccEnabled) {
			try{
				tccService.stopTelescope();
				System.err.println("all success!");
				int i=0;
				while(tccStatusService.getTelescopeStatus().isTelescopeDriving()){
					Thread.sleep(1000);
					if(i++ >5) throw new TCCException("TCC doesn't stop after a stop command");
				};
	
			}catch (TCCException  e) {
				backendService.stopBackend();
				throw e;
			}catch (InterruptedException e) {
				backendService.stopBackend();
				tccService.stopTelescope();
				throw e;
			}
		}
		if(backendEnabled) {
		try{
			backendService.stopBackend();
		}catch (BackendException e) {
			tccService.stopTelescope();
			throw e;
		}
		}
		return true;
		
	}

	@Deprecated
	public boolean stopObserving2(ObservationTO observation) throws TCCException, BackendException, InterruptedException{

		TCCService       tccService =     TCCService.createTccInstance();
		TCCStatusService tccStatusService = new TCCStatusService();
		BackendService   backendService = BackendService.createBackendInstance();

		long startTime = System.currentTimeMillis(); //LocalDateTime.parse(observation.getUtc(), DateTimeFormatter.ofPattern("yyyy-MM-dd-kk:mm:ss")).atZone(ZoneId.of("Australia/Melbourne")).toInstant().toEpochMilli();;

		long obsTime = observation.getTobs()*1000; // in milliseconds
		long elapsedTime = 0L;

		try{
			while(true){
				elapsedTime = (new Date()).getTime() - startTime;
				if(elapsedTime > obsTime) {
					backendService.stopBackend();
					tccService.stopTelescope();
					System.err.println("all success!");
					System.err.println(Thread.currentThread().getName());
					executorService.shutdown();
					return true;

				}
				if(!tccStatusService.getTelescopeStatus().isTelescopeTracking()) break;
				Thread.sleep(2000);

			}

			return false;
		}catch (BackendException e) {
			tccService.stopTelescope();
			throw e;
		}catch (TCCException  e) {
			backendService.stopBackend();
			throw e;
		}catch (InterruptedException e) {
			backendService.stopBackend();
			tccService.stopTelescope();
			throw e;
		}
	}


	//* to do : add TB sources for this pointing */
	public List<TBSourceTO> getTBSourcesForObservation(ObservationTO observation) throws EmptyCoordinatesException, CoordinateOverrideException{
		System.err.println("Getting TB sources for observation");
		List<TBSourceTO> tbSources = PSRCATManager.getTbSources();
		List<TBSourceTO> shortListed = new ArrayList<>();
		Coords coords = observation.getCoords();
		double radPTRA = coords.getPointingTO().getAngleRA().getRadianValue();
		double radPTDEC = coords.getPointingTO().getAngleDEC().getRadianValue();
		if(observation.getCoords().getPointingTO().getType().equals(SMIRFConstants.fluxCalibratorSymbol)){
			TBSourceTO tbSourceTO = PSRCATManager.getTBSouceByName(observation.getCoords().getPointingTO().getPointingName());
			shortListed.add(tbSourceTO);
		}
		coords.recomputeForNow();
		/* first check if the source is within the 4 degree circle of the pointing center in RA/DEC, if so, coordinate transform it and check if it is 
		 	within the primary beam in NS/MD coordinates*/
		for(TBSourceTO tbSourceTO: tbSources){
			double radTBRA = tbSourceTO.getAngleRA().getRadianValue();
			double radTBDEC = tbSourceTO.getAngleDEC().getRadianValue();
			if( Utilities.isWithinCircle(radPTRA, radPTDEC, radTBRA, radTBDEC, Constants.RadMolongloNSBeamWidth/2.0)){
				CoordinateTO tbCoordinateTO = new CoordinateTO(coords.getAngleHA(), coords.getPointingTO().getAngleDEC(), null, null);
				MolongloCoordinateTransforms.skyToTel(tbCoordinateTO);
				if (Utilities.isWithinEllipse(coords.getAngleNS().getRadianValue(), coords.getAngleMD().getRadianValue(), 
						tbCoordinateTO.getAngleNS().getRadianValue(), tbCoordinateTO.getAngleMD().getRadianValue(), 
						Constants.RadMolongloNSBeamWidth/2.0, Constants.RadMolongloMDBeamWidth/2.0)){

					if(!shortListed.contains(tbSourceTO)) shortListed.add(tbSourceTO);
				}
			}
		}


		return shortListed.subList(0, Math.min(BackendConstants.maximumNumberOfTB, shortListed.size()));
	}


	@Deprecated
	public static boolean observable(ObservationTO observation) throws TCCException, EmptyCoordinatesException, CoordinateOverrideException{

		Angle HANow = observation.getHANow();
		if(Math.abs(HANow.getDecimalHourValue())>6){ 
			System.err.println("HA >6: "+ HANow.getDecimalHourValue());
			return false;
		}

		CoordinateTO coordsNow  = new CoordinateTO(HANow.getRadianValue(), observation.getCoords().getPointingTO().getAngleDEC().getRadianValue(),null,null);
		MolongloCoordinateTransforms.skyToTel(coordsNow);
		//System.err.println( "*************" +observation.getCoords().getPointingTO().getPointingName() +" " +coordsNow.getRadMD()*Constants.rad2Deg  + " " +coordsNow.getRadNS()*Constants.rad2Deg );
		if(coordsNow.getRadMD() < SMIRFConstants.minRadMD || coordsNow.getRadMD() > SMIRFConstants.maxRadMD) return false;

		int slewTime = TCCManager.computeSlewTime(coordsNow.getRadNS(), coordsNow.getRadMD());
		int obsTime = observation.getTobs();

		int totalSecs = obsTime + slewTime + 60;
		Angle HAend = observation.getHAAfter(totalSecs);
		if(Math.abs(HAend.getDecimalHourValue())>6) return false;

		CoordinateTO coordsEnd  = new CoordinateTO(HAend.getRadianValue(), observation.getCoords().getPointingTO().getAngleDEC().getRadianValue(),null,null);
		MolongloCoordinateTransforms.skyToTel(coordsEnd);
		if(coordsEnd.getRadMD() < SMIRFConstants.minRadMD || coordsEnd.getRadMD() > SMIRFConstants.maxRadMD) return false;

		return true;
	}

	public static boolean observable(ObservationTO observation, String utc) throws TCCException, EmptyCoordinatesException, CoordinateOverrideException{
		Angle HANow = observation.getHAForUTC(utc);
		if(Math.abs(HANow.getDecimalHourValue())>6){ 
			System.err.println("HA >6: "+ HANow.getDecimalHourValue());
			return false;
		}

		CoordinateTO coordsNow  = new CoordinateTO(HANow.getRadianValue(), observation.getCoords().getPointingTO().getAngleDEC().getRadianValue(),null,null);
		MolongloCoordinateTransforms.skyToTel(coordsNow);
		//System.err.println( "*************" +observation.getCoords().getPointingTO().getPointingName() +" " +coordsNow.getRadMD()*Constants.rad2Deg  + " " +coordsNow.getRadNS()*Constants.rad2Deg );
		if(coordsNow.getRadMD() < SMIRFConstants.minRadMD || coordsNow.getRadMD() > SMIRFConstants.maxRadMD) return false;

		int slewTime = TCCManager.computeSlewTime(coordsNow.getRadNS(), coordsNow.getRadMD());
		int obsTime = observation.getTobs();

		int totalSecs = obsTime + slewTime + 60;
		Angle HAend = observation.getHAForUTCPlusOffset(utc,totalSecs);
		if(Math.abs(HAend.getDecimalHourValue())>6) return false;

		CoordinateTO coordsEnd  = new CoordinateTO(HAend.getRadianValue(), observation.getCoords().getPointingTO().getAngleDEC().getRadianValue(),null,null);
		MolongloCoordinateTransforms.skyToTel(coordsEnd);
		if(coordsEnd.getRadMD() < SMIRFConstants.minRadMD || coordsEnd.getRadMD() > SMIRFConstants.maxRadMD) return false;

		return true;
	}



}
