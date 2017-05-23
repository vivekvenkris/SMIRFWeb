package bean;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LegendPlacement;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import standalones.Point;
import standalones.SMIRF_GetUniqStitches;
import standalones.Traversal;

@ManagedBean
@SessionScoped
public class PointsPlotMB {
	String utc;
	String raStr;	
	String decStr;
	Long   numSamples; 
	
	private LineChartModel pointsPlotter;	
	
	public void plotPoints(ActionEvent event){
		
		System.err.println("GENERATING POINTS...");
		try {
		List<Point> pointsList = new SMIRF_GetUniqStitches().generateUniqStitches(utc, 
				new Angle(raStr, Angle.HHMMSS), new Angle(decStr, Angle.DDMMSS), 10, numSamples, 327.68e-6);
		
		for(Point p: pointsList){
			LineChartSeries pointSeries = new LineChartSeries();
			for(Traversal t: p.getTraversalList()){
				pointSeries.set(t.getFanbeam(), t.getNs());
			}
			pointsPlotter.addSeries(pointSeries);
		} 
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println("Doneee :-D ");
	}
	
	public PointsPlotMB() throws EmptyCoordinatesException, CoordinateOverrideException {
		
		utc= "2017-03-11-00:21:21";
		raStr = "17:30:00";
		decStr = "-30:00:00";
		numSamples = 131072L;
		
		pointsPlotter = new LineChartModel();
		pointsPlotter.setTitle("Traversal plot");
		
		pointsPlotter.getAxis(AxisType.X).setLabel("FB");
		pointsPlotter.getAxis(AxisType.Y).setLabel("NS");
		
		pointsPlotter.setZoom(true);
		pointsPlotter.setResetAxesOnResize(true);
//		pointsPlotter.setLegendCols(150);
//		pointsPlotter.setLegendPosition("se");
//		pointsPlotter.setLegendPlacement(LegendPlacement.OUTSIDEGRID);
//		
		try {
			List<Point> pointsList = new SMIRF_GetUniqStitches().generateUniqStitches(utc, 
					new Angle(raStr, Angle.HHMMSS), new Angle(decStr, Angle.DDMMSS), 10, numSamples, 327.68e-6);
			
			for(Point p: pointsList){
				LineChartSeries pointSeries = new LineChartSeries();
				for(Traversal t: p.getTraversalList()){
					pointSeries.set(t.getFanbeam(), t.getNs());
				}
				pointsPlotter.addSeries(pointSeries);
			} 
			}
			catch (Exception e) {
				e.printStackTrace();
			}

	}
	
	
	
	public void addMessage(String summary) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  null);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public void addMessage(String summary, String detail) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  detail);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public String getUtc() {
		return utc;
	}

	public void setUtc(String utc) {
		this.utc = utc;
	}

	public String getRaStr() {
		return raStr;
	}

	public void setRaStr(String raStr) {
		this.raStr = raStr;
	}

	public String getDecStr() {
		return decStr;
	}

	public void setDecStr(String decStr) {
		this.decStr = decStr;
	}

	public Long getNumSamples() {
		return numSamples;
	}

	public void setNumSamples(Long numSamples) {
		this.numSamples = numSamples;
	}

	public LineChartModel getPointsPlotter() {
		return pointsPlotter;
	}

	public void setPointsPlotter(LineChartModel pointsPlotter) {
		this.pointsPlotter = pointsPlotter;
	}
	
	
}
