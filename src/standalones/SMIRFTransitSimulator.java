package standalones;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import bean.Angle;
import bean.CoordinateTO;
import bean.Coords;
import bean.Pointing;
import bean.PointingTO;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import util.Constants;
import util.SMIRFConstants;
import util.TCCConstants;
import util.TableBuilder;

public class SMIRFTransitSimulator implements SMIRFConstants{
	
	public static List<PointingTO> getPointingGrid() {
		List<PointingTO> gridPoints = new ArrayList<>();
		double radius = tilingDiameter/2.0; 
		double side = radius;
		double height = radius*0.5*Math.sqrt(3);
		int x=0;
		for(double lat=minGalacticLatitude;lat<= maxGalacticLatitude; lat += height){
			double offset = (x++ %2 ==0)? radius+ 0.5*side : 0;
			for(double lon = minGalacticLongitude; lon <= maxGalacticLongitude; lon += 2*radius + side){
				Pointing grid = new Pointing(new Angle(lat, Angle.DDMMSS), new Angle(lon+offset,Angle.DDMMSS));
				String ra[] = grid.getAngleRA().toHHMMSS().split(":");
				String raStr = ra[0]+ra[1];

				String dec[] = grid.getAngleDEC().toDDMMSS().split(":");
				String decStr = dec[0] + dec[1];
				String name = "SMIRF_"+raStr + decStr;
				grid.setPointingName(name);
				gridPoints.add(new PointingTO(grid));

			}
		}
		
		return gridPoints;
		
	}
	
	public static List<PointingTO> getPointingGridForLatitude(double lat){
		List<PointingTO> gridPoints = new ArrayList<>();
		double radius = tilingDiameter/2.0; 
		double side = radius;
		int x=0;
		double offset = (x++ %2 ==0)? radius+ 0.5*side : 0;
		for(double lon = minGalacticLongitude; lon <= maxGalacticLongitude; lon += 2*radius + side){
			Pointing grid = new Pointing(new Angle(lat, Angle.DDMMSS), new Angle(lon+offset,Angle.DDMMSS));
			String ra[] = grid.getAngleRA().toHHMMSS().split(":");
			String raStr = ra[0]+ra[1];

			String dec[] = grid.getAngleDEC().toDDMMSS().split(":");
			String decStr = dec[0] + dec[1];
			String name = "SMIRF_"+raStr + decStr;
			grid.setPointingName(name);
			gridPoints.add(new PointingTO(grid));
			//System.err.print(grid);

		}
		return gridPoints;

	}
	
	public static List<Coords> getCoordsListForLST(List<PointingTO> gridPoints, Angle initLST, CoordinateTO initTelPosition){
		
		return gridPoints
				
		.stream()
		
		.map(p -> {
				try {
					return new Coords(p, initLST);
				} catch (EmptyCoordinatesException | CoordinateOverrideException e) {
					return null;
				} 
			})
		.filter(x -> x!=null)
		
		.sorted(Comparator.comparing(c -> ((Coords)c).getAngleHA().getDecimalHourValue()).reversed())
		
//		.sorted(Comparator
//				.comparing(c -> Math.abs(((Coords)c).getPointingTO().getAngleLAT().getDegreeValue())).reversed()
//				.thenComparing(c -> ((Coords)c).getAngleHA().getDecimalHourValue()).reversed()
//				)
//		
		
		.filter(coord -> {
			
			Coords c = ((Coords)coord);
			
			Angle lst = new Angle(initLST.getRadianValue(), Angle.HHMMSS);
			
			lst.addSolarSeconds( computeNSSlewTime(c.getAngleNS(), initTelPosition.getAngleNS()) );
			
			double ha = lst.getDecimalHourValue() - c.getPointingTO().getAngleRA().getDecimalHourValue();
			
//			System.err.println(c.getPointingTO().getPointingName() + " " +lst+ " " + c.getPointingTO().getAngleRA() + " " 
//					+ ha*60 + " "+ (ha < 0) );
			
			return ha < 0;
		})
		
		.collect(Collectors.toList());
	}
	
	public static int computeNSSlewTime(Angle ns1, Angle ns2){
		
		Double slewDegPerSecond = TCCConstants.slewRateNSFast;
		
		return (int)Math.round(Math.abs((ns1.getDegreeValue() - ns2.getDegreeValue())/slewDegPerSecond));
		
	}
	

	public static void SMIRF_simulateTransit(List<PointingTO> gridPoints,List<PointingTO> completed, int day,	TableBuilder tb
			, TableBuilder tb2) throws CoordinateOverrideException, EmptyCoordinatesException {
		
		double totalHours = 0.0;
		
		double tobs = 6.0/60;

		//List<PointingTO> gridPoints = getPointingGridForLatitude(-4.0 * Constants.deg2Rad);
		
		
		//System.err.println("Total pointings:" + gridPoints.size());
		
		CoordinateTO initTelPosition = new CoordinateTO(null, null, 0.0, 0.0);

		Angle initLST = new Angle("07:00:00", Angle.HHMMSS, Angle.HHMMSS);
		
		//initLST.addSolarSeconds(-6 * 60 * 60);
		
		
		while(gridPoints.size() >0){
		
		 
		List<Coords> coords = getCoordsListForLST(gridPoints, initLST, initTelPosition);
		
		
		if(coords.isEmpty()) break;

		Coords nearestCoord = coords.get(0);
		
		double absHA = Math.abs(initLST.getDecimalHourValue() - nearestCoord.getPointingTO().getAngleRA().getDecimalHourValue());


		String row  = nearestCoord.getPointingTO().getPointingName() + " ";
			   row += initLST + " " ; 
			   row += nearestCoord.getPointingTO().getAngleRA() + " ";
			   row += ((absHA*60 - 6) > 0? absHA*60 - 6: 0 )+ " ";
			   row += computeNSSlewTime(nearestCoord.getAngleNS(), initTelPosition.getAngleNS())/60.0 + " ";
			   row +=  (-35.3 + nearestCoord.getAngleNS().getDegreeValue()) + " " ;
			   row += nearestCoord.getAngleMD() + " ";
			   row += nearestCoord.getPointingTO().getAngleRA().getDecimalHourValue() + " ";
			   row += nearestCoord.getPointingTO().getAngleDEC().getDegreeValue() + " ";
			   row += nearestCoord.getPointingTO().getAngleLON().getDegreeValue() + " ";
			   row += nearestCoord.getPointingTO().getAngleLAT().getDegreeValue() + " ";
			   row += day;
			   
		tb.addRow(row);
				
  
		double addHours = absHA > tobs ? absHA : tobs;
		
		totalHours +=(addHours+computeNSSlewTime(nearestCoord.getAngleNS(), initTelPosition.getAngleNS())/3600.0);
		
		initLST.addSolarSeconds( (int) (addHours * 3600) );
		
		initTelPosition.setRadNS(nearestCoord.getAngleNS().getRadianValue());
		initTelPosition.setRadMD(0.0);
		
		gridPoints.remove( nearestCoord.getPointingTO());
		completed.add(nearestCoord.getPointingTO());

		
		}

		//System.err.print(tb);
		
		//System.err.print(" hours:" + String.format("%.1f", totalHours) + " ");
		
		//System.err.println("Remaining: " + gridPoints.size() + " Done today: " + completed.size() + " Eff:" + completed.size() * tobs / totalHours);
		
		tb2.addRow( day + " " + String.format("%.1f", totalHours) + " " + gridPoints.size() + " "+
				 completed.size()  + " " + String.format("%.5f", completed.size() * tobs/totalHours));
		
		//gridPoints.forEach(p -> System.err.println(p.getPointingName()));
//		load 'accent.pal
//		set multiplot layout 2,1;plot "~/test.txt" using 8:9 ps 2 pt 7  lc  rgb "#272727", "~/test.txt" using 8:9:($12<=days?$12:1/0) ps 2  pt 7 palette; plot "~/test.txt" using 10:11 ps 2 pt 7  lc  rgb "#272727", "~/test.txt" using 10:11:($12<=days?$12:1/0) ps 2  pt 7 palette; unset multiplot

 
	}

	public static void main(String[] args) throws CoordinateOverrideException, EmptyCoordinatesException, IOException {
		
		List<PointingTO> gridPoints = getPointingGrid();
		//System.err.println("Total pointings: " + gridPoints.size());
		List<PointingTO> completed = new ArrayList<>();
		int day=1;
		TableBuilder tb = new TableBuilder();
		TableBuilder tb2 = new TableBuilder();
		
		tb2.addRow("Day total_hours remaining_pointings pointings_done efficiency ");

		while(!gridPoints.isEmpty()){
			
//			System.err.print("day:" + day + " ");
			SMIRF_simulateTransit(gridPoints,new ArrayList<>(), day++,tb,tb2);

		}
		
		System.err.println(tb2);
		
		Files.write(Paths.get("/home/vivek/test.txt"), tb.toString().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);



	}

}
