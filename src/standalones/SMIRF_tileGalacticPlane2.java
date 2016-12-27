package standalones;
//package util;
//
//import java.awt.Color;
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.swing.JFrame;
//
//import org.jastronomy.jsofa.JSOFA;
//import org.jastronomy.jsofa.JSOFA.SphericalCoordinate;
//
//import de.erichseifert.gral.data.DataTable;
//import de.erichseifert.gral.plots.XYPlot;
//import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
//import de.erichseifert.gral.plots.lines.LineRenderer;
//import de.erichseifert.gral.ui.InteractivePanel;
//import javafx.application.Application;
//import javafx.scene.Scene;
//import javafx.scene.chart.NumberAxis;
//import javafx.scene.chart.ScatterChart;
//import javafx.scene.chart.XYChart;
//import javafx.scene.chart.XYChart.Data;
//import javafx.stage.Stage;
//
//
//
//
//public class SMIRF_tileGalacticPlane2  extends Application implements SMIRFConstants{
//
//	public SMIRF_tileGalacticPlane2() {   
//
//	}
//	public static void main(String[] args) {
//		launch(args);
//	}
//	public void start(Stage primaryStage) throws Exception {
//
//		
//		List<Grid> gridPoints = new ArrayList<>();
//
//		double radius = tilingDiameter/2.0; 
//		double side = radius;
//		double height = radius*0.5*Math.sqrt(3);
//		int x=0;
//		XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
//		for(double lat=minGalacticLatitude;lat<= maxGalacticLatitude; lat += height){
//			double offset = (x++ %2 ==0)? radius+ 0.5*side : 0;
//			for(double lon = minGalacticLongitude; lon <= maxGalacticLongitude; lon += 2*radius + side){
//				Grid grid = new Grid(lat, lon+offset);
//				series1.getData().add(new Data<Number,Number>(grid.ra.doubleValue(),grid.dec.doubleValue()));
//				gridPoints.add(grid);
//				
//				System.out.print(grid);
//
//			}
//		}
//		int longitudeBins = (int)(((maxGalacticLongitude - minGalacticLongitude)/tilingDiameter +1) * 1.05);
//		int latitudeBins = (int)((maxGalacticLatitude - minGalacticLatitude)/tilingDiameter +3);
//
//		System.err.println(latitudeBins + " "+ longitudeBins);
//
//		double longitudeBinWidth = (maxGalacticLongitude - minGalacticLongitude) / longitudeBins;
//		double latitudeBinWidth = (maxGalacticLatitude - minGalacticLatitude)/ latitudeBins;
//		
//		double lon[] = new double[longitudeBins];
//		double lat[] = new double[latitudeBins];
//		XYChart.Series<Number, Number> series2 = new XYChart.Series<>();
//		for(int b=0;b<latitudeBins;b++) lat[b] = minGalacticLatitude + b*latitudeBinWidth;
//		for(int l=0;l<longitudeBins;l++) lon[l] = minGalacticLongitude + l*longitudeBinWidth;
//		x=0;
//		for(double b : lat){
//			double offset = 0;
//			if(x++ %2==0) offset = tilingDiameter/2.0;
//			for(double l: lon){
//				Grid grid = new Grid(b,l+offset);
//				series2.getData().add(new Data<Number,Number>(grid.ra.doubleValue(),grid.dec.doubleValue()));
//			}
//		}
//		System.err.println(gridPoints.size() + " days: " + gridPoints.size() * 900/3600.0 * 24/18.0 *1/24.0 );
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
//		chart.getData().add(series1);
//		chart.getData().add(series2);
//		
//		System.err.println(series1.getData().size() + " " + series2.getData().size());
//		
//		Scene scene = new Scene(chart,1400,900);
//		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
//		primaryStage.setScene(scene);
//		//primaryStage.show();
//		System.exit(0);
////		xAxis.autosize();
////		yAxis.autosize();
//		
//		
//		
//	}
//
//
//}
