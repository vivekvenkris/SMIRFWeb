package standalones;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import bean.Pointing;
import bean.PointingTO;
import bean.TBSourceTO;
import manager.DBManager;
import manager.PSRCATManager;
import service.DBService;
import util.SMIRFConstants;

public class SMIRF_AddPulsarPointings {
	
	
	public static void main(String[] args) throws IOException {
		
		List<String> psrNames = Files.readAllLines(Paths.get("/home/vivek/SMIRF/temp.psrs"));
	
		List<PointingTO> pointings = DBManager.getAllPointings();
		
		List<PointingTO> tos = psrNames.stream().map( f ->  {
			
			for( PointingTO p: pointings) {
				if(p.getPointingName().contains(f)) {
					System.err.println("Skipping" + f);
					return null;
				}
				
			}
			
			System.err.println("Adding...." + f);
			
			TBSourceTO to = PSRCATManager.getTBSouceByName(f);
			if(to==null) return null;
			PointingTO pto = new PointingTO(to);
			pto.setPointingID(null);
			pto.setPointingName("PSR_"+pto.getPointingName());;
			pto.setType(SMIRFConstants.pulsarPointingSymbol);
			pto.setPriority(SMIRFConstants.highestPriority);
			pto.setNumObs(0);
			pto.setTobs(SMIRFConstants.tobs);
			return pto;
		}).filter(f->f !=null).collect(Collectors.toList());
		
		DBService.addPointingsToDB(tos.stream().map(f -> new Pointing(f)).collect(Collectors.toList()));
		
		/***
		 * 		List<String> psrNames = Files.readAllLines(Paths.get("/home/vivek/SMIRF/db/528psrs.txt"));

			List<PointingTO> tos = psrNames.stream().map( f ->  {
			String[] chunks = f.split(" ");
			String name = chunks[0];
			Angle RA = new Angle(chunks[1], Angle.HHMMSS);
			Angle DEC = new Angle(chunks[2], Angle.DDMMSS);
			PointingTO pto = new PointingTO(RA, DEC);
			pto.setPointingID(null);
			pto.setPointingName("PSR_"+pto.getPointingName());;
			pto.setType(SMIRFConstants.psrPointingSymbol);
			pto.setPriority(SMIRFConstants.highestPriority);
			
			return pto;
		}).filter(f->f !=null).collect(Collectors.toList());
		
		DBService.addPointingsToDB(tos.stream().map(f -> new Pointing(f)).collect(Collectors.toList()));
		 */
		
	}

}
