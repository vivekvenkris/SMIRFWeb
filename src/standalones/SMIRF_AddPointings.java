package standalones;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jastronomy.jsofa.JSOFA;
import org.jastronomy.jsofa.JSOFA.SphericalCoordinate;

import bean.Angle;
import bean.Pointing;
import service.DBService;

public class SMIRF_AddPointings {
	
	public static void main(String[] args) throws IOException {
		// file of the form NAME RA DEC TYPE
		List<String> list = Files.readAllLines(Paths.get("/home/vivek/frb.pointings"));
		List<Pointing> pointings = new ArrayList<>();
		list.forEach( f -> {
			System.err.println("Considering " + f);
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
			
			SphericalCoordinate sc = JSOFA.jauIcrs2g(pointing.getAngleRA().getRadianValue(),  pointing.getAngleDEC().getRadianValue());
			pointing.setAngleLON(new Angle(sc.alpha, Angle.DDMMSS));
			pointing.setAngleLAT(new Angle(sc.delta, Angle.DDMMSS));
			
			pointing.setNumObs(0);
			pointing.setPriority(10);
			pointing.setType(chunks[3]);
		
			pointings.add(pointing);
			
			
		});
		
		DBService.addPointingsToDB(pointings);
		
		System.err.println("Done.");
	}

}
