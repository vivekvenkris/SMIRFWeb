package manager;

import java.util.ArrayList;
import java.util.List;

import bean.FluxCalibrator;
import bean.FluxCalibratorTO;
import bean.PhaseCalibrator;
import bean.PhaseCalibratorTO;
import service.DBService;

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
	
	public static PhaseCalibratorTO getPhaseCalibratorByName(String name){
		return new PhaseCalibratorTO(DBService.getPhaseCalibratorByName(name));
	}
	
	public static FluxCalibratorTO getFluxCalibratorByName(String name){
		return new FluxCalibratorTO(DBService.getFluxCalibratorByName(name));
	}


}
