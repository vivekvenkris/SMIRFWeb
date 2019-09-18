package standalones;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import bean.Pointing;
import service.DBService;

public class PointingDumper {

	 public static void main(String[] args) {
		List<Pointing> pointings = DBService.getAllPointings().stream()
				.filter(f-> f.getPointingName().startsWith("SMIRF_"))
				.sorted(Comparator.comparing(f-> ((Pointing)f).getAngleRA().getDecimalHourValue()))
				.collect(Collectors.toList());
		
		for(Pointing p: pointings){
			System.err.println(p.getPointingName() + " " + p.getAngleRA().getDecimalHourValue() + " " + p.getAngleDEC().getDegreeValue());
		}
		
	}
	
}
