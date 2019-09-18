package standalones;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jastronomy.jsofa.JSOFA;
import org.jastronomy.jsofa.JSOFA.SphericalCoordinate;

import bean.Angle;
import bean.Pointing;
import bean.PointingTO;
import manager.DBManager;
import service.DBService;
import util.SMIRFConstants;

public class SMIRF_AddPointings {
	
	public static void main(String[] args) throws IOException {
		// file of the form NAME RA DEC TYPE"/home/observer/schedule/FRBs_list.txt"
		// List<String> list = Files.readAllLines(Paths.get("/home/vivek/SMIRF/new.txt"));
		List<String> list = Files.readAllLines(Paths.get("/home/observer/schedule/FRBs_list.txt"));
		
		List<PointingTO> existing = DBManager.getAllPointings();
		
		List<Pointing> newPointings = new ArrayList<>();
		List<Pointing> updatedPointings = new ArrayList<>();
		list.forEach( f -> {
			System.err.println("Considering " + f);
			
			if(f.contains("#")) {
				System.err.println("Ignoring comment: " + f);
				return;
			}
			
			String[] chunks = f.trim().split(" ");
			
			Pointing pointing = new Pointing();
			pointing.setAngleRA(new Angle(chunks[1], Angle.HHMMSS));
			pointing.setAngleDEC(new Angle(chunks[2], Angle.DDMMSS));
			pointing.setPointingName(chunks[0]);
			
			if(chunks[0].contains("auto")){
				String ra[] = pointing.getAngleRA().toHHMMSS().split(":");
				String raStr = ra[0]+ra[1];

				String dec[] = pointing.getAngleDEC().toDDMMSS().split(":");
				String decStr = dec[0] + dec[1];
				String name = "SMIRF_"+raStr + decStr;
				pointing.setPointingName(name);

			}
			
			if(existing.contains(new PointingTO(pointing))) {
				
				
				PointingTO existingPointingTO = existing.get( existing.indexOf(new PointingTO(pointing)));
				
				System.err.println(existingPointingTO.getPointingID() + " " 
						+ (existingPointingTO.getAngleRA().getRadianValue() + " " + pointing.getAngleRA().getRadianValue()) + " " +
								(existingPointingTO.getAngleDEC().getRadianValue() + " " + pointing.getAngleDEC().getRadianValue()));
				
				if( !(existingPointingTO.getAngleRA().getRadianValue().equals(pointing.getAngleRA().getRadianValue())) ||
					!(existingPointingTO.getAngleDEC().getRadianValue().equals(pointing.getAngleDEC().getRadianValue()))) {
				
					System.err.println("updating existing pointing: " + existingPointingTO.getPointingID());;
					
					existingPointingTO.setAngleRA(pointing.getAngleRA());
					existingPointingTO.setAngleDEC(pointing.getAngleDEC());
					
					SphericalCoordinate sc = JSOFA.jauIcrs2g(pointing.getAngleRA().getRadianValue(),  pointing.getAngleDEC().getRadianValue());
					existingPointingTO.setAngleLON(new Angle(sc.alpha, Angle.DDMMSS));
					existingPointingTO.setAngleLAT(new Angle(sc.delta, Angle.DDMMSS));
					
					updatedPointings.add(new Pointing(existingPointingTO));
					
				}
				
				else {
					System.err.println("Skipping existing pointing: " + f);
				}
				
				return;

				
			}
			
			SphericalCoordinate sc = JSOFA.jauIcrs2g(pointing.getAngleRA().getRadianValue(),  pointing.getAngleDEC().getRadianValue());
			pointing.setAngleLON(new Angle(sc.alpha, Angle.DDMMSS));
			pointing.setAngleLAT(new Angle(sc.delta, Angle.DDMMSS));
			
			pointing.setNumObs(10);
			pointing.setPriority(10);
			pointing.setType(chunks[3]);
			pointing.setTobs(900);
			pointing.setLeastCadanceInDays(4);
			pointing.setEndMDInPercent(-5);
			pointing.setStartMDInPercent(-30);
		
			newPointings.add(pointing);
			
			
		});
		
		System.err.println("will add the following pointings: " + newPointings.stream().map(f -> f.getPointingName()).collect(Collectors.toList()) );
		System.err.println("will update the following pointings: " + updatedPointings.stream().map(f -> f.getPointingName()).collect(Collectors.toList()) );
		//System.exit(0);
		if(!newPointings.isEmpty()) DBService.addPointingsToDB(newPointings);
		if(!updatedPointings.isEmpty()) DBService.updatePointingsToDB(
				updatedPointings.stream().map(f-> new PointingTO(f)).collect(Collectors.toList()));
		System.err.println("Done.");
	}

}
