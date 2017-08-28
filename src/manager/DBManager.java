package manager;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import bean.FluxCalibrator;
import bean.FluxCalibratorTO;
import bean.Observation;
import bean.ObservationSessionTO;
import bean.ObservationTO;
import bean.PhaseCalibrator;
import bean.PhaseCalibratorTO;
import bean.Pointing;
import bean.PointingTO;
import service.DBService;
import util.BackendConstants;

public class DBManager {
	public static List<PhaseCalibratorTO> getAllPhaseCalibrators(){
		List<PhaseCalibratorTO> toList = new ArrayList<>();		
		for(PhaseCalibrator entity: DBService.getAllPhaseCalibrators()) toList.add(new PhaseCalibratorTO(entity));
		return toList;
	}

	public static List<PhaseCalibratorTO> getAllPhaseCalibratorsOrderByFluxDesc(){
		List<PhaseCalibratorTO> toList = new ArrayList<>();		
		for(PhaseCalibrator entity: DBService.getAllPhaseCalibratorsOrderByFluxDesc()) toList.add(new PhaseCalibratorTO(entity));
		return toList;
	}

	public static List<FluxCalibratorTO> getAllFluxCalibrators(){
		List<FluxCalibratorTO> toList = new ArrayList<>();		
		for(FluxCalibrator entity: DBService.getAllFluxCalibrators()) toList.add(new FluxCalibratorTO(entity));
		return toList;
	}

	public static List<FluxCalibratorTO> getAllFluxCalibratorsOrderByDMDesc(){
		List<FluxCalibratorTO> toList = new ArrayList<>();		
		for(FluxCalibrator entity: DBService.getAllFluxCalibratorsOrderByDMDesc()) toList.add(new FluxCalibratorTO(entity));
		return toList;
	}

	public static List<PointingTO> getAllPointings(){
		List<PointingTO> toList = new ArrayList<>();		
		for(Pointing entity: DBService.getAllPointings()) toList.add(new PointingTO(entity));
		return toList;
	}
	
	
	public static List<PointingTO> getAllUnobservedPointingsOrderByPriority(){
		List<PointingTO> toList = new ArrayList<>();		
		for(Pointing entity: DBService.getAllUnobservedPointingsOrderByPriority()) toList.add(new PointingTO(entity));
		return toList;
	} 
	

	public static List<String> getAllPointingTypes(){
		return DBService.getAllPointingTypes();
	}


	public static PointingTO getPointingByUniqueName(String pointingName){
		
		if(pointingName.startsWith("J")) return new PointingTO(DBManager.getFluxCalibratorByName(pointingName));
		else if(pointingName.startsWith("CJ")) return DBManager.getPhaseCalByUniqueName(pointingName);
		else return new PointingTO(DBService.getPointingByUniqueName(pointingName));
		
	}

	public static PointingTO getFluxCalByUniqueName(String name){
		FluxCalibrator calibrator = DBService.getFluxCalByUniqueName(name);
		return calibrator == null? null : new PointingTO(new FluxCalibratorTO(calibrator));
	}
	public static PointingTO getPhaseCalByUniqueName(String name){
		return new PointingTO(new PhaseCalibratorTO(DBService.getPhaseCalByUniqueName(name)));
	}

	public static List<PointingTO> getAllPointingsForPointingType(String pointingType){
		List<PointingTO> toList = new ArrayList<>();		
		for(Pointing entity: DBService.getAllPointingsForPointingType(pointingType)) toList.add(new PointingTO(entity));
		return toList;
	}



	public static PhaseCalibratorTO getPhaseCalibratorByName(String name){
		return new PhaseCalibratorTO(DBService.getPhaseCalibratorByName(name));
	}

	public static FluxCalibratorTO getFluxCalibratorByName(String name){
		return new FluxCalibratorTO(DBService.getFluxCalibratorByName(name));
	}


	public static void addObservationToDB(ObservationTO observationTO){
		DBService.addObservationToDB(observationTO);
	}
	
	public static void addSessionToDB(ObservationSessionTO observationSessionTO){
		DBService.addSessionToDB(observationSessionTO);
	}

	public static void makeObservationComplete(ObservationTO observationTO){
		DBService.makeObservationComplete(observationTO);
	}
	
	public static void incrementCompletedObservation(ObservationSessionTO observationSessionTO){
		if(observationSessionTO == null ) return;
		DBService.incrementCompletedObservation(observationSessionTO);
	}
	
	public static ObservationTO getObservationByUTC(String utc){
		
		return new ObservationTO(DBService.getObservationByUTC(utc));
		
	}


}
