package standalones;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import bean.Angle;
import bean.Pointing;
import bean.PointingTO;
import service.DBService;
import util.SMIRFConstants;

public class SMIRF_AddFRBFields {
	
	public static void main(String[] args) throws IOException {
		List<String> psrNames = Files.readAllLines(Paths.get("/home/vivek/SMIRF/db/FRB_fields.txt"));

		List<PointingTO> tos = psrNames.stream().map( f ->  {
		String[] chunks = f.split(" ");
		String name = chunks[0];
		Angle RA = new Angle(chunks[1], Angle.HHMMSS);
		Angle DEC = new Angle(chunks[2], Angle.DDMMSS);
		PointingTO pto = new PointingTO(RA, DEC);
		pto.setPointingID(null);
		pto.setPointingName("FRB_"+name);
		pto.setType(SMIRFConstants.frbFieldPointingSymbol);
		pto.setPriority(SMIRFConstants.highestPriority);
		
		return pto;
	}).filter(f->f !=null).collect(Collectors.toList());
	
		tos.forEach( f -> System.out.println( f.getAngleRA()));
		
	DBService.addPointingsToDB(tos.stream().map(f -> new Pointing(f)).collect(Collectors.toList()));
	}

}
