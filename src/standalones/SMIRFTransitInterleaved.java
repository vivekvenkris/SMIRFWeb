package standalones;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
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
import util.SMIRFConstants;
import util.TableBuilder;

public class SMIRFTransitInterleaved implements SMIRFConstants{
	

	public static void SMIRF_simulateInterleavedTransit(List<PointingTO> gridPoints, List<PointingTO> completed, int day,	TableBuilder tb
			, TableBuilder tb2) throws CoordinateOverrideException, EmptyCoordinatesException {		
				
		double totalHours = 0.0;
		
		double tobs = 6.0/60;
		
		CoordinateTO initTelPosition = new CoordinateTO(null, null, 0.0, 0.0);

		Angle initLST = new Angle("07:00:00", Angle.HHMMSS, Angle.HHMMSS);
		
		while(gridPoints.size() >0){
			
			List<Coords> coords = SMIRFTransitSimulator.getCoordsListForLST(gridPoints, initLST, initTelPosition);
			if(coords.isEmpty()) break;
			
			int index = 1;
			
			int minimumObs = Collections.min(coords.stream().map(f -> f.getPointingTO().getNumObs()).collect(Collectors.toSet()));
			
			
			for(Coords c: coords){
				
				if(c.getPointingTO().getNumObs().equals(minimumObs)) break;
				index++;
			}
			
			List<Coords> shortlisted = coords.subList(0, index).stream().sorted(
					Comparator.comparing(f -> ((Coords)f).getPointingTO().getNumObs())).collect(Collectors.toList());
			
			if(shortlisted.isEmpty()) break;
			
			Coords leastObserved = shortlisted.get(0);
			
			
			double leastObservedHA = Math.abs(initLST.getDecimalHourValue() - leastObserved.getPointingTO().getAngleRA().getDecimalHourValue());

			double slewTime = SMIRFTransitSimulator.computeNSSlewTime(leastObserved.getAngleNS(), initTelPosition.getAngleNS())/3600.0;
			
			Coords interim = null;
						
			if ( (leastObservedHA - slewTime) > tobs ) {
				
				for( int i=1; i< shortlisted.size(); i++){
					
					Coords iCoords = shortlisted.get(i);
					
					if(completed.contains(iCoords.getPointingTO())) continue;
					
					
					double slewTimeToInterim = SMIRFTransitSimulator.computeNSSlewTime(iCoords.getAngleNS(), initTelPosition.getAngleNS())/3600.0;
					double slewTimeFromInterim = SMIRFTransitSimulator.computeNSSlewTime(leastObserved.getAngleNS(), iCoords.getAngleNS())/3600.0;
					double timeToDetour = slewTimeFromInterim + slewTimeToInterim + tobs;
					if( (leastObservedHA - (timeToDetour) ) > tobs ){
							System.err.println(day + "Interim adding " + (leastObservedHA - (timeToDetour +tobs))*60  
									+ " " + iCoords.getPointingTO().getPointingName() + " " + leastObserved.getPointingTO().getPointingName());

							interim = iCoords;
							break;
							
						}
						
					}
					
				}
			
			Coords next = (interim != null)? interim : leastObserved;
				
			if(interim != null){
				
				double addHours = tobs + SMIRFTransitSimulator.computeNSSlewTime(interim.getAngleNS(), initTelPosition.getAngleNS())/3600.0 ;
				initLST.addSolarSeconds( (int) (addHours * 3600) );
				
				initTelPosition.setRadNS(interim.getAngleNS().getRadianValue());
				initTelPosition.setRadMD(0.0);
				
				//gridPoints.remove( interim.getPointingTO());
				gridPoints.get(gridPoints.indexOf(interim.getPointingTO())).incrementNumObs();
				completed.add(interim.getPointingTO());
				
			}
			else{
				
				double addHours = SMIRFTransitSimulator.computeNSSlewTime(leastObserved.getAngleNS(), initTelPosition.getAngleNS())/3600.0;
				addHours += leastObservedHA;
				initLST.addSolarSeconds( (int) (addHours * 3600) );
				
				initTelPosition.setRadNS(leastObserved.getAngleNS().getRadianValue());
				initTelPosition.setRadMD(0.0);
				gridPoints.get(gridPoints.indexOf(leastObserved.getPointingTO())).incrementNumObs();

				//gridPoints.remove( leastObserved.getPointingTO());
				completed.add(leastObserved.getPointingTO());
				
			}
			//System.err.println("Next observed:" + next.getPointingTO().getPointingName() + " " + next.getPointingTO().getNumObs());
			

			String row  = next.getPointingTO().getPointingName() + " ";
			   row += initLST + " " ; 
			   row += next.getPointingTO().getAngleRA() + " ";
			   row += SMIRFTransitSimulator.computeNSSlewTime(next.getAngleNS(), initTelPosition.getAngleNS())/60.0 + " ";
			   row +=  (-35.3 + next.getAngleNS().getDegreeValue()) + " " ;
			   row += next.getAngleMD() + " ";
			   row += next.getPointingTO().getAngleRA().getDecimalHourValue() + " ";
			   row += next.getPointingTO().getAngleDEC().getDegreeValue() + " ";
			   row += next.getPointingTO().getAngleLON().getDegreeValue() + " ";
			   row += next.getPointingTO().getAngleLAT().getDegreeValue() + " ";
			   row += day;
			   
		tb.addRow(row);
			
			tb2.addRow( day + " " + String.format("%.1f", totalHours) + " " + gridPoints.size() + " "+
					 completed.size()  + " " + String.format("%.5f", completed.size() * tobs/totalHours));
			
		}
		

	}
	
	public static void main(String[] args) throws CoordinateOverrideException, EmptyCoordinatesException, IOException {
		List<PointingTO> gridPoints = SMIRFTransitSimulator.getPointingGrid();
		
		List<PointingTO> completed = new ArrayList<>();
		int day=1;
		TableBuilder tb = new TableBuilder();
		TableBuilder tb2 = new TableBuilder();
		
		tb2.addRow("Day total_hours remaining_pointings pointings_done efficiency ");
		for(int i=0; i< 100; i++){
		SMIRF_simulateInterleavedTransit(gridPoints,new ArrayList<>(), day++,tb,tb2);
		}
		int k=1;
		String s = ""; 
		for(PointingTO p : gridPoints){
			
			s += (p.getPointingName() + " " + p.getAngleRA().getDegreeValue() + " " + p.getAngleDEC().getDegreeValue() + " " +  p.getNumObs() + " " + k++) + "\n";
			
		}
		Files.write(Paths.get("/home/vivek/junk"), s.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);


//		for(int i=0; i< 10; i++){
//			
//			//System.err.print("day:" + day + " ");
//			SMIRF_simulateInterleavedTransit(gridPoints,new ArrayList<>(), day++,tb,tb2);
//
//		}
		
	//	System.err.println(tb2);
		
		//Files.write(Paths.get("/home/vivek/test.txt"), tb.toString().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

	}
	
}
