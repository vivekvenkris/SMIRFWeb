package standalones;

import java.util.ArrayList;
import java.util.List;

import bean.Angle;
import bean.Pointing;
import service.DBService;
import util.SMIRFConstants;



public class SMIRFGalacticPlaneTiler implements SMIRFConstants{
	
	public static void SMIRF_tileGalacticPlane() {
		List<Pointing> gridPoints = new ArrayList<>();
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
				gridPoints.add(grid);


			}
		}
		
		DBService.addPointingsToDB(gridPoints);
	}

}



















//class PlanePlotter extends Application implements SMIRFConstants {
//	public void start(Stage primaryStage) throws Exception {
//
//
//		List<Grid> gridPoints = new ArrayList<>();
//
//		double radius = tilingDiameter/2.0; 
//		double side = radius;
//		double height = radius*0.5*Math.sqrt(3);
//		int x=0;
//		XYChart.Series<Number, Number> gridSeries = new XYChart.Series<>();
//		for(double lat=minGalacticLatitude;lat<= maxGalacticLatitude; lat += height){
//			double offset = (x++ %2 ==0)? radius+ 0.5*side : 0;
//			for(double lon = minGalacticLongitude; lon <= maxGalacticLongitude; lon += 2*radius + side){
//				Grid grid = new Grid(new Angle(lat, Angle.DDMMSS), new Angle(lon+offset,Angle.DDMMSS));
//				gridSeries.getData().add(new Data<Number,Number>(grid.ra.getDegreeValue(),grid.dec.getDegreeValue()));
//				gridPoints.add(grid);
//
//				System.out.print(grid);
//
//			}
//		}
//		System.err.println(gridPoints.size() + " days: " + gridPoints.size() * 900/3600.0 * 24/18.0 *1/24.0 );
//
//		XYChart.Series<Number, Number> lowerLim = new XYChart.Series<>();
//		XYChart.Series<Number, Number> upperLim = new XYChart.Series<>();
//		for(double lon = minGalacticLongitude; lon <= maxGalacticLongitude; lon +=0.1*Constants.deg2Rad){
//			Grid lowerLimit = new Grid(new Angle(minGalacticLatitude, Angle.DDMMSS), new Angle(lon,Angle.DDMMSS));
//			lowerLim.getData().add(new Data<Number,Number>(lowerLimit.ra.getDegreeValue(),lowerLimit.dec.getDegreeValue()));
//
//			Grid upperLimit = new Grid(new Angle(maxGalacticLatitude, Angle.DDMMSS), new Angle(lon,Angle.DDMMSS));
//			upperLim.getData().add(new Data<Number,Number>(upperLimit.ra.getDegreeValue(),upperLimit.dec.getDegreeValue()));
//
//		}
//
//		final NumberAxis xAxis = new NumberAxis();
//		final NumberAxis yAxis = new NumberAxis();
//		xAxis.setAutoRanging(false);
//		yAxis.setAutoRanging(false);
//		xAxis.setLowerBound(100);
//		xAxis.setUpperBound(290);
//		yAxis.setLowerBound(-70);
//		yAxis.setUpperBound(20);
//
//		final ScatterChart<Number, Number> chart = new ScatterChart<>(xAxis, yAxis);
//		chart.getData().add(upperLim);
//		chart.getData().add(lowerLim);
//
//		chart.getData().add(gridSeries);
//		Scene scene = new Scene(chart,1400,900);
//		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
//		primaryStage.setScene(scene);
//		primaryStage.show();
//
//
//		//System.exit(0);
//		//		xAxis.autosize();
//		//		yAxis.autosize();
//
//
//
//	}
//
//
//}
