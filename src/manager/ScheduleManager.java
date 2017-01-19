package manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import bean.Angle;
import bean.Coords;
import bean.ObservationTO;
import bean.PhaseCalibratorTO;
import bean.Pointing;
import bean.PointingTO;
import exceptions.BackendException;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.ObservationException;
import exceptions.PointingException;
import exceptions.TCCException;
import service.CalibrationService;
import service.DBService;
import service.EphemService;
import standalones.Point;
import standalones.SMIRFGalacticPlaneTiler;
import standalones.SMIRF_GetUniqStitches;
import standalones.Traversal;
import util.BackendConstants;
import util.Constants;
import util.SMIRFConstants;
import util.Utilities;

public class ScheduleManager implements SMIRFConstants {
	static ObservationTO currentObservation = null;
	static String schedulerMessages = "";
	private boolean finishCall = false;
	private boolean terminateCall = false;
	Future<Boolean> scheduler = null;

	public boolean Calibrate(Integer calibratorID, Integer tobs, String observer) throws TCCException, BackendException, InterruptedException, IOException, EmptyCoordinatesException, CoordinateOverrideException, ObservationException{

		PhaseCalibratorTO calibratorTO = new PhaseCalibratorTO(DBService.getCalibratorByID(calibratorID));
		ObservationTO observation = new ObservationTO();
		observation.setName(calibratorTO.getSourceName());
		PointingTO pointingTO = new PointingTO(calibratorTO);

		observation.setCoords(new Coords(pointingTO));
		observation.setTobs(tobs);
		observation.setBackendType(BackendConstants.corrBackend);
		observation.setObserver(observer);
		observation.setObsType(BackendConstants.correlation);
		System.err.println("starting observation...");


		ObservationManager manager = new ObservationManager();
		currentObservation = observation;
		manager.startObserving(observation);


		String utc = observation.getUtc();
		System.err.println("utc:" + utc);
		CalibrationService service = new CalibrationService();
		return service.Calibrate(utc, calibratorTO.getSourceName());



	}



	public void observeTestPSR(){

		/***
		 *  Get where you are.
		 *  Get nearest pulsar
		 *  observe for x minutes
		 *  save data somewhere with session ID
		 *  
		 */

	}

	public void startObserving(PointingTO selectedPointing, int tobs,String observer, Boolean tccEnabled, Boolean backendEnabled, Boolean  doPostObservationStuff) throws TCCException, BackendException, EmptyCoordinatesException, CoordinateOverrideException, ObservationException, InterruptedException, IOException{
		ObservationManager manager = new ObservationManager();
		ExecutorService executorService = Executors.newFixedThreadPool(4);

		Callable<Boolean> observe = new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				ObservationTO observation = new ObservationTO();
				observation.setName(selectedPointing.getPointingName());
				if(selectedPointing.getType().equals(SMIRFConstants.phaseCalibratorSymbol)) {
					observation.setBackendType(BackendConstants.corrBackend);
					observation.setObsType(BackendConstants.correlation);
				}
				else {
					observation.setBackendType(BackendConstants.psrBackend);
					observation.setObsType(BackendConstants.tiedArrayFanBeam);
				}

				observation.setCoords(new Coords(selectedPointing));
				observation.setObserver(observer);
				observation.setTobs(tobs);
				currentObservation = observation;

				manager.startObserving(observation, tccEnabled, backendEnabled);
				
				if(observation.isSurveyPointing() && doPostObservationStuff){
					Callable<Boolean> getStictchesReady = new Callable<Boolean>() {

						@Override
						public Boolean call() throws Exception {
							try{
							System.err.println("Sending stitches for observation" + observation.getUtc());
							return sendUniqStitchesForObservation(observation);
							} catch (Exception e) {
								e.printStackTrace();
								throw e;
							}
						}
					};
					executorService.submit(getStictchesReady);
				}

				long obsTime = observation.getTobs()*1000;
				long startTime = backendEnabled ?  observation.getUtcDate().getTime() : new Date().getTime();
				while(!((new Date().getTime() - startTime) > obsTime)) {
					if(terminateCall) {
						terminateCall = finishCall = false;
						currentObservation = null;
						return false; 
					}
					Thread.sleep(100);
				}

				System.err.println("observation over.");
				manager.stopObserving();


				if(doPostObservationStuff ) PostObservationManager.doPostObservationStuff(observation);

				System.err.println("stopped.");	
				currentObservation = null;
				finishCall = terminateCall = false;
				return true;
			}
		};

		scheduler = executorService.submit(observe);



	}

	public void startSMIRFScheduler(List<Coords> coordsList, int obsDuration, int tobs, String observer, Boolean tccEnabled, Boolean backendEnabled, Boolean  doPostObservationStuff) throws EmptyCoordinatesException, CoordinateOverrideException, PointingException, TCCException, BackendException, InterruptedException{
		ObservationManager manager = new ObservationManager();		
		ExecutorService executorService = Executors.newFixedThreadPool(4);


		Callable<Boolean> schedulerCore = new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				try{
					for(Coords coords: coordsList){
						PointingTO pointing = coords.getPointingTO();
						System.err.println("Observing: " + pointing.getPointingName());


						ObservationTO observation = new ObservationTO();
						observation.setName(pointing.getPointingName());
						if(coords.getPointingTO().getType().equals("P")) {
							observation.setBackendType(BackendConstants.corrBackend);
							observation.setObsType(BackendConstants.correlation);
						}
						else {
							observation.setBackendType(BackendConstants.psrBackend);
							observation.setObsType(BackendConstants.tiedArrayFanBeam);
						}

						observation.setCoords(coords);
						observation.setObserver(observer);
						observation.setTobs(tobs);
						currentObservation = observation;
						try{
							manager.startObserving(observation, tccEnabled, backendEnabled);
						}catch (ObservationException e) {
							// add log that the pointing was not observable.
							e.printStackTrace();
							continue;
						}

						if(observation.isSurveyPointing() && doPostObservationStuff){
							Callable<Boolean> getStictchesReady = new Callable<Boolean>() {

								@Override
								public Boolean call() throws Exception {
									System.err.println("Sending stitches for observation" + observation.getUtc());
									return sendUniqStitchesForObservation(observation);
								}
							};
							executorService.submit(getStictchesReady);
						}

						long obsTime = observation.getTobs()*1000;
						long startTime = observation.getUtcDate().getTime();
						while(true) {
							if( (new Date().getTime() - startTime) > obsTime || terminateCall) break; 
							Thread.sleep(100);
						}
						if(terminateCall) break;

						System.err.println("observation over.");

						manager.stopObserving();

						if(doPostObservationStuff ) PostObservationManager.doPostObservationStuff(observation);

						System.err.println("stopped.");

						if(finishCall || terminateCall) {
							break;
						}
					}
				}catch( Exception e){
					e.printStackTrace();
				}
				currentObservation = null;
				finishCall = terminateCall = false;
				return true;
			}
		};


		scheduler = executorService.submit(schedulerCore);




	}

	
	public boolean sendUniqStitchesForObservation(ObservationTO observation) throws EmptyCoordinatesException, CoordinateOverrideException, UnknownHostException, IOException{
		SMIRF_GetUniqStitches getUniqStitches = new SMIRF_GetUniqStitches();
		List<Point> points = getUniqStitches.generateUniqStitches(observation);
		Set<Entry<String, Integer>> nepenthesServerEntrySet = BackendConstants.bfNodeNepenthesServers.entrySet();
		for(Entry<String,Integer> nepenthesServer: nepenthesServerEntrySet){
			System.err.println("Attempting to connect to " + nepenthesServer.getValue());
			Socket socket = new Socket( simulate? "localhost" :nepenthesServer.getKey(), nepenthesServer.getValue());
			System.err.println("Connected to " + nepenthesServer.getValue());
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader( new InputStreamReader(socket.getInputStream()));	

			out.println("utc: " + observation.getUtc());
			out.println("0");

			for(Point p: points){
				List<Traversal> traversals = p.getTraversalMap().get(nepenthesServer.getValue() - 38030);
				if(traversals!=null) {
					out.print(p.getRa() + " " + p.getDec() + " " + p.getStartFanBeam() + " "+ p.getEndFanBeam() + " "+ 
									String.format("%7.5f", p.getStartNS()*Constants.rad2Deg) + " "+ String.format("%7.5f", p.getEndNS()*Constants.rad2Deg) + " ");
					for(Traversal t: traversals){
						out.print(t.getFanbeam()+ " " + String.format("%7.5f", t.getNs()) + " "+ t.getStartSample() + " "+ t.getNumSamples() + " "+ t.getPercent() + " ");
					}
					out.println();
					out.flush();

				}
			}
			out.println("#end");
			out.flush();
			out.close();
			in.close();
			socket.close();
			
		}
		return true;
	}


	public List<Coords> getPointingsForSession(String utc, int totalSeconds, int tobsSeconds, Comparator<Coords> comparator) throws EmptyCoordinatesException, CoordinateOverrideException, PointingException, TCCException{
		List<Pointing> pointings = DBService.getAllUnobservedPointingsOrderByPriority();
		List<PointingTO> pointingTOs = new LinkedList<PointingTO>();
		Angle lst = new Angle(EphemService.getRadLMSTforMolonglo(utc),Angle.HHMMSS);
		for(Pointing p : pointings) {
			if(EphemService.getHA(lst, p.getAngleRA()).getRadianValue() > SMIRFConstants.minRadHA 
					&& EphemService.getHA(lst, p.getAngleRA()).getRadianValue() < SMIRFConstants.maxRadHA){
				pointingTOs.add(new PointingTO(p));
			}
		}
		LinkedList<Coords> pointingList = new LinkedList<>();
		Integer minSlewTime = 0;
		LocalDateTime utcTime = Utilities.getUTCLocalDateTime(utc);

		for(int s = tobsSeconds; s<=totalSeconds; s+=tobsSeconds) {
			List<Coords> coordsList = new ArrayList<>();
			for(PointingTO pointingTO: pointingTOs) {
				Coords coords = new Coords(pointingTO, lst);
				double radMD = coords.getAngleMD().getRadianValue();
				double radNS = coords.getAngleNS().getRadianValue();
				double radHA = coords.getAngleHA().getRadianValue();
				if(radMD > minRadMD && radMD < maxRadMD &&
						radNS > minRadNS && radNS < maxRadNS && 
						radHA > minRadHA && radHA < maxRadHA   ){
					coordsList.add(coords);
				}
			}
			Collections.sort(coordsList,comparator);
			coordsList.removeAll(pointingList);
			lst.addSolarSeconds(tobsSeconds);
			utcTime = utcTime.plusSeconds(tobsSeconds);
			/* remove pointings that did not last in the FOV for tobs + tslew*/
			for(Iterator<Coords> coordsIterator = coordsList.iterator(); coordsIterator.hasNext();){
				Coords coords = coordsIterator.next();
				coords.recompute(lst);
				double radMD = coords.getAngleMD().getRadianValue();
				double radNS = coords.getAngleNS().getRadianValue();
				double radHA = coords.getAngleHA().getRadianValue();
				if(!(radMD > minRadMD && radMD < maxRadMD &&
						radNS > minRadNS && radNS < maxRadNS && 
						radHA > minRadHA && radHA < maxRadHA  )){
					coordsIterator.remove();
				}
			}
			if(pointingList.isEmpty()) {
				if(coordsList.isEmpty())  {
					continue;//throw new PointingException("No pointing visible now. Please try later");
				}
				coordsList.get(0).setUtc(Utilities.getUTCString(utcTime));
				pointingList.add(coordsList.get(0));
				int slewtime = TCCManager.computeSlewTime(coordsList.get(0).getAngleNS().getRadianValue(), coordsList.get(0).getAngleMD().getRadianValue());
				s+=slewtime;
				lst.addSolarSeconds(slewtime);
				utcTime = utcTime.plusSeconds(slewtime);
			}
			else{
				Coords previous = pointingList.getLast();
				Coords closest = null;

				for(Coords now: coordsList){
					int slewtime = TCCManager.computeSlewTime(previous.getAngleNS().getRadianValue(), previous.getAngleMD().getRadianValue(), now.getAngleNS().getRadianValue(), now.getAngleMD().getRadianValue());
					if(closest==null || slewtime < minSlewTime){
						minSlewTime = slewtime;
						closest = now;
					}
				}
				if(closest!=null) {
					closest.setUtc(Utilities.getUTCString(utcTime));
					pointingList.add(closest);
				}
				s+=minSlewTime;
				lst.addSolarSeconds(minSlewTime);
				utcTime = utcTime.plusSeconds(minSlewTime);
			}

		}
		//for(Coords c: pointingList) System.err.print(c);
		System.err.println( pointingList.size()+ " pointings " + pointingList.size()*tobsSeconds/3600.0 + " hours " +pointingList.size()*tobsSeconds/60.0 + " min " + pointingList.size()*tobsSeconds + " seconds");
		return pointingList;


	}

	/*public List<Coords> getPointingsForSession2(String utc, int totalSeconds, int tobsSeconds) throws EmptyCoordinatesException, CoordinateOverrideException, PointingException{
		List<Pointing> pointings = DBService.getAllUnobservedPointingsOrderByPriority();
		List<PointingTO> pointingTOs = new LinkedList<PointingTO>();
		Angle lst = new Angle(EphemService.getRadLMSTforMolonglo(utc),Angle.HHMMSS);
		for(Pointing p : pointings) {
			if(EphemService.getHA(lst, p.getAngleRA()).getRadianValue() > SMIRFConstants.minRadHA 
					&& EphemService.getHA(lst, p.getAngleRA()).getRadianValue() < SMIRFConstants.maxRadHA){
				pointingTOs.add(new PointingTO(p));

			}
		}
		LinkedList<Coords> pointingList = new LinkedList<>();
		Integer minSlewTime = 0;

		for(int s = tobsSeconds; s<=totalSeconds; s+=tobsSeconds) {
			List<Coords> coordsList = new ArrayList<>();
			for(PointingTO pointingTO: pointingTOs) {
				Coords coords = new Coords(pointingTO, lst);
				double radMD = coords.getAngleMD().getRadianValue();
				double radNS = coords.getAngleNS().getRadianValue();
				double radHA = coords.getAngleHA().getRadianValue();
				if(radMD >= minRadMD && radMD <= maxRadMD &&
						radNS >= minRadNS && radNS <= maxRadNS && 
						radHA >= minRadHA && radHA <= maxRadHA   ){
					coordsList.add(coords);
				}
			}
			Collections.sort(coordsList,Coords.compareMDNS);
			lst.addSolarSeconds(minSlewTime + tobsSeconds);
			for(Iterator<Coords> coordsIterator = coordsList.iterator(); coordsIterator.hasNext();){
				Coords coords = coordsIterator.next();
				coords.recompute(lst);
				double radMD = coords.getAngleMD().getRadianValue();
				double radNS = coords.getAngleNS().getRadianValue();
				double radHA = coords.getAngleHA().getRadianValue();
				if(!(radMD >= minRadMD && radMD <= maxRadMD &&
						radNS >= minRadNS && radNS <= maxRadNS && 
						radHA >= minRadHA && radHA <= maxRadHA  )){
					coordsIterator.remove();
				}
			}
			coordsList.removeAll(pointingList);
			if(pointingList.isEmpty()) {
				if(coordsList.isEmpty())  {
					continue;//throw new PointingException("No pointing visible now. Please try later");
				}
				pointingList.add(coordsList.get(0));
			}
			else{
				Coords previous = pointingList.getLast();
				Coords closest = null;

				for(Coords now: coordsList){
					int slewtime = TCCManager.computeSlewTime(previous.getAngleNS().getRadianValue(), previous.getAngleMD().getRadianValue(), now.getAngleNS().getRadianValue(), now.getAngleMD().getRadianValue());
					if(closest==null || slewtime < minSlewTime){
						minSlewTime = slewtime;
						closest = now;
					}
				}
				if(closest!=null) pointingList.add(closest);
				s+=minSlewTime;
			}
		}
		//for(Coords c: pointingList) System.err.print(c);
		System.err.println( pointingList.size()+ " pointings " + pointingList.size()*tobsSeconds/3600.0 + " hours " +pointingList.size()*tobsSeconds/60.0 + " min " + pointingList.size()*tobsSeconds + " seconds");
		return pointingList;


	}*/



	public static ObservationTO getCurrentObservation() {
		return currentObservation;
	}

	public static void setCurrentObservation(ObservationTO currentObservation) {
		ScheduleManager.currentObservation = currentObservation;
	}

	public boolean isFinishCall() {
		return finishCall;
	}

	public void setFinishCall(boolean finishCall) {
		this.finishCall = finishCall;
	}

	public boolean isTerminateCall() {
		return terminateCall;
	}

	public void setTerminateCall(boolean terminateCall) {
		this.terminateCall = terminateCall;
	}

	public void terminate(){
		terminateCall = true;
	}

	public void finish(){
		finishCall = true;
	}


	public Future<Boolean> getScheduler() {
		return scheduler;
	}

	public void setScheduler(Future<Boolean> scheduler) {
		this.scheduler = scheduler;
	}

	public static void main(String[] args) throws EmptyCoordinatesException, CoordinateOverrideException, PointingException, IOException, TCCException {
		SMIRFGalacticPlaneTiler.SMIRF_tileGalacticPlane();
		System.err.println("tiled..");
		System.exit(0);
		System.in.read();
		ScheduleManager sm = new ScheduleManager();
		DecimalFormat df = new DecimalFormat("00");
		BufferedWriter bw = new BufferedWriter(new FileWriter("/Users/vkrishnan/Desktop/dustbin/blah2"));
		LocalDateTime utc = LocalDateTime.parse("2016-11-01-13:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd-kk:mm:ss"));
		for(int i=1;;i++){
			System.err.print("day:" +i +" " + utc.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-kk:mm:ss"))+"\t" );
			List<Coords> coordsList = sm.getPointingsForSession(utc.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-kk:mm:ss")), 24*60*60,900, Coords.compareMDNS);
			for(Coords c: coordsList)  {
				DBService.incrementPointingObservations(c.getPointingTO().getPointingID());
				bw.write(i + " " +c.getPointingTO().getAngleRA().getDegreeValue() + " " + c.getPointingTO().getAngleDEC().getDegreeValue() + "\n" );
				bw.flush();
			}
			System.in.read();
			utc = utc.plusDays(1);
		}

	}


}

