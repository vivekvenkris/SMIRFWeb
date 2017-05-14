package standalones;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import bean.Coords;
import bean.ObservationSessionTO;
import bean.Pointing;
import bean.PointingTO;
import bean.TBSourceTO;
import exceptions.BackendException;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.PointingException;
import exceptions.TCCException;
import manager.DBManager;
import manager.PSRCATManager;
import manager.ScheduleManager;
import service.DBService;
import service.EphemService;
import util.SMIRFConstants;
import util.Utilities;
import util.Constants;

public class SMIRF_GetScheduleWithPulsars {


	public static List<Pointing> getListofPointingsContainingPulsars(List<String> pulsars){
		List<TBSourceTO> tbSourceTOs = new ArrayList<>();

		for(String pulsar: pulsars)tbSourceTOs.add(PSRCATManager.getTBSouceByName(pulsar));

		List<Pointing> pointingList = DBService.getAllPointings();

		List<Pointing> shortlisted = new ArrayList<>();

		for(Pointing pointing: pointingList){

			Iterator<TBSourceTO> tbSourceTOIterator  =  tbSourceTOs.iterator();
			while(tbSourceTOIterator.hasNext()){
				TBSourceTO tbSourceTO = tbSourceTOIterator.next();
				
				//System.err.println("Considering " + tbSourceTO.getPsrName());
				
				if(Utilities.isWithinCircle(pointing.getAngleRA().getRadianValue(), pointing.getAngleDEC().getRadianValue(),
						tbSourceTO.getAngleRA().getRadianValue(), tbSourceTO.getAngleDEC().getRadianValue(), 1.5*Constants.deg2Rad)){

					System.err.println(pointing.getPointingID() + " " +pointing.getAngleRA() + " " + pointing.getAngleDEC() + " " + 
							Constants.rad2Deg * Utilities.getEuclideanDistance(pointing.getAngleRA().getRadianValue(), pointing.getAngleDEC().getRadianValue(),
									tbSourceTO.getAngleRA().getRadianValue(), tbSourceTO.getAngleDEC().getRadianValue()));

					shortlisted.add(pointing);

				}
			}

		}
		return shortlisted;
	}


	public static void main(String[] args) {

		//List<String> pulsars = Arrays.asList(new String[]{"J1644-4559","J0835-4510", "J0742-2822", "J0738-4042", "J0837-4135", "J1456-6843", "J2241-5236", "J1752-2806"});
		List<String> pulsars = Arrays.asList(new String[]{"J1848-0123"});

		List<Pointing> pointings = getListofPointingsContainingPulsars(pulsars);
		
		pointings = pointings.stream().sorted(Comparator.comparing(a -> ((Pointing)a).getAngleRA().getRadValue())).collect(Collectors.toList());

		
		for(Pointing pointing: pointings)
		System.err.println(pointing.getPointingID());
		
		
		List<Coords> finalCoordsList = pointings.stream().map(p -> new Coords(new PointingTO(p))).collect(Collectors.toList());
		ScheduleManager manager = new ScheduleManager();
		
		ObservationSessionTO sessionTO = new ObservationSessionTO(Utilities.getUTCString(EphemService.getUTCTimestamp()), "VVK", 720, 2, true, false, false, false, finalCoordsList.size());
		DBManager.addSessionToDB(sessionTO);
		try {
			manager.startSMIRFScheduler(finalCoordsList, 7200, 720, "VVK",true,true,true,sessionTO);
		} catch (EmptyCoordinatesException | CoordinateOverrideException | PointingException | TCCException
				| BackendException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
