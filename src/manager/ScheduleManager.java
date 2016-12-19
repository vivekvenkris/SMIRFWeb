package manager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import bean.Angle;
import bean.CoordinateTO;
import bean.Coords;
import bean.Observation;
import bean.Pointing;
import bean.PointingTO;
import bean.TBSourceTO;
import exceptions.BackendException;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.PointingException;
import exceptions.TCCException;
import service.CalibrationService;
import service.DBService;
import service.EphemService;
import util.BackendConstants;
import util.SMIRFConstants;
import util.SMIRF_tileGalacticPlane;

public class ScheduleManager implements SMIRFConstants {


	public boolean Calibrate(String name) throws TCCException, BackendException, InterruptedException, IOException, EmptyCoordinatesException, CoordinateOverrideException{

		Observation observation = new Observation();
		observation.setName("");
		/***
		 * Should do a table for calibrators and add it to pointing list.
		 */
		observation.setCoords(new Coords(null, null));
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
	
	public void observeTestPSR(){
		
	}
	
	public void startScheduler(String utc, int obsDuration, int tobs, String observer) throws EmptyCoordinatesException, CoordinateOverrideException, PointingException, TCCException, BackendException, InterruptedException{
		List<Coords> coordsList = this.getPointingsForSession(utc, obsDuration,tobs);
		ObservationManager manager = new ObservationManager();		

		for(Coords coords: coordsList){
			
			PointingTO pointing = coords.getPointingTO();
			DBService.incrementPointingObservations(coords.getPointingTO().getPointingID());
			
			Observation observation = new Observation();
			observation.setName(pointing.getPointingName());
			observation.setBackendType(BackendConstants.psrBackend);
			observation.setObsType(BackendConstants.tiedArrayFanBeam);
			observation.setCoords(coords);
			//* to do : add TB sources for this pointing */
			observation.setObserver(observer);
			manager.observe(observation);
		}
		

	}
	
	

	public List<Coords> getPointingsForSession(String utc, int totalSeconds, int tobsSeconds) throws EmptyCoordinatesException, CoordinateOverrideException, PointingException{
		List<Pointing> pointings = DBService.getAllPointingsOrderByNumObs();
		List<PointingTO> pointingTOs = new ArrayList<PointingTO>();
		for(Pointing p : pointings) pointingTOs.add(new PointingTO(p));

		Angle lst = new Angle(EphemService.getRadLMSTforMolonglo(utc),Angle.HHMMSS);
		LinkedList<Coords> pointingList = new LinkedList<>();
		Integer minSlewTime = 0;

		for(int s = tobsSeconds; s<totalSeconds; s+=tobsSeconds) {
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
			lst.addSolarSeconds(minSlewTime);
			//System.err.println(" at obs start: " +lst + " "+coordsList.size());
			lst.addSolarSeconds(tobsSeconds );
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


	}

	public static void main(String[] args) throws EmptyCoordinatesException, CoordinateOverrideException, PointingException, IOException {
		SMIRF_tileGalacticPlane.SMIRF_tileGalacticPlane();
		System.err.println("tiled..");
		System.in.read();
		ScheduleManager sm = new ScheduleManager();
		DecimalFormat df = new DecimalFormat("00");
		BufferedWriter bw = new BufferedWriter(new FileWriter("/Users/vkrishnan/Desktop/dustbin/blah2"));
		LocalDateTime utc = LocalDateTime.parse("2016-11-01-13:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd-kk:mm:ss"));
		for(int i=1;;i++){
		System.err.print("day:" +i +" " + utc.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-kk:mm:ss"))+"\t" );
		List<Coords> coordsList = sm.getPointingsForSession(utc.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-kk:mm:ss")), 24*60*60,900);
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

