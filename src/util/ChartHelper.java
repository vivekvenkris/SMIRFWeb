package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

public class ChartHelper {
	static Map<LineChartSeries, List<LineChartSeries>> seriesChain = new HashMap<>();
	static int color=0;
	static String[] colorArray = {"7FC97F","FFFF99","386BC0","FDC086","#BEAED4"};
	public static void addToSeries(LineChartSeries series,Object key, Number value ){
		if(series.getData().containsKey(key)){
			System.err.println("contains key" + key);
			List<LineChartSeries> seriesList = seriesChain.getOrDefault(series, new ArrayList<>());
			for(LineChartSeries iSeries: seriesList){
				System.err.println("inside list....");
				if(!iSeries.getData().containsKey(key)){
					iSeries.set(key, value);
					seriesChain.put(series, seriesList);
					return;
				}
			}
			System.err.println("Adding new list for " + key);
			LineChartSeries newSeries = new LineChartSeries();
			newSeries.set(key, value);
			newSeries.setShowLine(false);
			seriesList.add(newSeries);
			seriesChain.put(series, seriesList);
			return;
		}
		System.err.println("does not contain key:" + key);
		series.set(key, value);
	}

	public static void addSeriesToChart(LineChartModel chart, LineChartSeries series){
		String color = getColor();
		String colorForSeries = "";
		
		chart.addSeries(series);
		colorForSeries +=(color+",");
		
		List<LineChartSeries> seriesList = seriesChain.getOrDefault(series, new ArrayList<>());
		for(LineChartSeries iSeries: seriesList){
			chart.addSeries(iSeries);
			colorForSeries +=(color+",");
		}
		
		String existingColors = chart.getSeriesColors();
		String newColors= (existingColors!=null)?existingColors+ ",":""  + colorForSeries.substring(0, colorForSeries.length()-2);
		System.err.println(newColors);
		chart.setSeriesColors(color+","+newColors);

	}
	
	public static String getColor(){
		return colorArray[color++];
	}


}
