package test;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.gargoylesoftware.htmlunit.javascript.host.file.File;

import bean.PointingTO;
import bean.TBSourceTO;
import manager.DBManager;
import manager.PSRCATManager;
import service.DBService;
import util.Constants;
import util.Utilities;

public class PulsarDistances {
public static void main(String[] args) throws Exception{
	
	List<PointingTO> smirfPointings = DBManager.getAllPointings();// .stream().filter(f -> f.getPointingName().contains("SMIRF_")).collect(Collectors.toList());
	List<TBSourceTO> timingProgramme = PSRCATManager.getTimingProgrammeSources();
	List<String> values = new ArrayList<>();
	for(TBSourceTO tb : timingProgramme) { 
		
		PointingTO minTO = null;
		double minDist = 0.0;
		
		for(PointingTO smirf : smirfPointings ) {
			if(!Utilities
					.isWithinCircle(tb.getAngleRA().getRadianValue(), tb.getAngleDEC().getRadianValue(),
							smirf.getAngleRA().getRadianValue(), smirf.getAngleDEC().getRadianValue(), 1 * Constants.deg2Rad)) continue;
			double dist = distance(tb, smirf);
			if(minTO == null || minDist > dist ) {
				minTO = smirf;
				minDist = dist;
			}
		}
		
		values.add(tb.getPsrName() + " " + minTO + " " + minDist);		
		if(minTO != null)
		minTO.addToAssociatedPulsars(tb);
		else
		System.err.println(tb);
	}
	
	for(PointingTO smirf : smirfPointings) {
		System.err.println(smirf.getPointingName()  + " " + smirf.getAssociatedPulsars());
	}
	
	DBService.updatePointingsToDB(smirfPointings);
	
	//Files.write(Paths.get("/home/vivek/SMIRF/pulsar_distances.txt"), values, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

	System.err.println(String.join("\n", values));
	
}

static double distance(TBSourceTO tb, PointingTO smirf) {
	return Math.abs(tb.getAngleDEC().getDegreeValue() - smirf.getAngleDEC().getDegreeValue());
}
}
