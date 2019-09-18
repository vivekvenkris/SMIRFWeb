package standalones;

import java.util.List;
import java.util.stream.Collectors;

import bean.Pointing;
import bean.PointingTO;
import bean.TBSourceTO;
import exceptions.ObservationException;
import manager.DBManager;
import manager.PSRCATManager;
import service.DBService;
import util.Utilities;

public class SMIRF_revampPulsars {
	public static void main(String[] args) throws ObservationException {
		List<TBSourceTO> timingProgrammePulsars = PSRCATManager.getTimingProgrammeSources();
		List<Pointing> pointings = DBService.getAllPointings();

		//	for(TBSourceTO sourceTO: timingProgrammePulsars) {
		//boolean flag = false;
		//		for(Pointing p: pointings) {
		//			if(Utilities.distance(sourceTO.getAngleRA().getDegreeValue(), sourceTO.getAngleDEC().getDegreeValue(),
		//					p.getAngleRA().getDegreeValue(), p.getAngleDEC().getDegreeValue())< 1) {
		//				flag=true;
		//				break;
		//			}
		//		}
		//		if(!flag) {
		//			System.err.println(sourceTO.getPsrName());
		//		}
		//	}

//		for(Pointing p: pointings) {
//			boolean flag = false;
//			for(TBSourceTO sourceTO: timingProgrammePulsars) {
//				if(Utilities.distance(sourceTO.getAngleRA().getDegreeValue(), sourceTO.getAngleDEC().getDegreeValue(),
//						p.getAngleRA().getDegreeValue(), p.getAngleDEC().getDegreeValue())< 1) {
//					flag = true;
//					break;
//				}
//			}
//			if(!flag && p.getPointingName().startsWith("PSR_")) {
//				System.err.println(p.getPointingName());
//				//DBService.deletePointingFromDB(p);
//			}
//		}
		
		List<PointingTO> pointingTOs = DBManager.getAllPointings().stream().filter(f->f.getPointingName().startsWith("PSR_")).collect(Collectors.toList());
		List<TBSourceTO> ap = pointingTOs.stream().map(f->f.getAssociatedPulsars()).flatMap(List::stream).collect(Collectors.toList());
		for(TBSourceTO sourceTO: timingProgrammePulsars) {
			boolean flag = false;
//					for(Pointing p: pointings) {
//						if(new PointingTO(p).getAssociatedPulsars().contains(sourceTO)) {
//							flag=true;
//							break;
//						}
//					}
//					if(!flag) {
			if(!ap.contains(sourceTO)) {
						System.err.println(sourceTO.getPsrName());
					}
				}
	}
}
