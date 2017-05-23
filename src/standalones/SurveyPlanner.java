package standalones;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import bean.Coords;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.PointingException;
import exceptions.TCCException;
import manager.ScheduleManager;
import service.DBService;
import service.EphemService;
import util.Utilities;

public class SurveyPlanner {
	
	public static void main(String[] args) throws EmptyCoordinatesException, CoordinateOverrideException, PointingException, IOException, TCCException {
		
		if(DBService.getAllPointings().isEmpty()) {
		SMIRFGalacticPlaneTiler.SMIRF_tileGalacticPlane();
		System.err.println("tiled..");
		}

		int size = DBService.getAllPointings().size();
		
		ScheduleManager sm = new ScheduleManager();
		BufferedWriter bw = new BufferedWriter(new FileWriter("/home/vivek/SMIRFtests/out.plan2"));
		LocalDateTime utc =Utilities.getUTCLocalDateTime("2017-05-19-06:57:00");
		int n=0;
		for(int i=1;;i++){
			System.err.print("day:" +i +" " + Utilities.getUTCString(utc) );
			List<Coords> coordsList = null; //sm.getPointingsForSession(Utilities.getUTCString(utc), 23*60*60,900, Coords.compareNSMD);
			n+=coordsList.size();
			if(n >= size) break;
			for(Coords c: coordsList)  {
				DBService.incrementPointingObservations(c.getPointingTO().getPointingID());
				bw.write(i + " " +c.getPointingTO().getAngleRA().getDegreeValue() + " " + c.getPointingTO().getAngleDEC().getDegreeValue() + "\n" );
				bw.flush();
			}
			utc = utc.plusDays(1);
		}

	}
//plot "out.plan" using 2:3:1  lc var ps 2 pt 7

}
