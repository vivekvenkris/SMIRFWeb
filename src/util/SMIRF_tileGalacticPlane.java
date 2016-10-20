package util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.jastronomy.jsofa.JSOFA;
import org.jastronomy.jsofa.JSOFA.SphericalCoordinate;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.ui.InteractivePanel;

class Grid{
	Double lat;
	Double lon;
	Double ra;
	Double dec;
	
	public Grid(Double lat, Double lon) {
		SphericalCoordinate sc = JSOFA.jauG2icrs(lon, lat);
		this.lat = lat * Constants.rad2Deg;
		this.lon  = lon * Constants.rad2Deg;
		this.ra = sc.alpha * Constants.rad2Deg;
		this.dec = sc.delta * Constants.rad2Deg;
	}
	@Override
	public String toString() {
		return this.lat + " "+ this.lon + " " + this.ra + " "+ this.dec + "\n";
	}
}



public class SMIRF_tileGalacticPlane  extends JFrame implements SMIRFConstants{
	
	public SMIRF_tileGalacticPlane(List<Grid> gridPoints) {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        DataTable data = new DataTable(Double.class, Double.class);
        for(Grid g: gridPoints){
        	data.add(g.ra.doubleValue(),g.dec.doubleValue());
        }
        System.err.println(data);
        XYPlot plot = new XYPlot(data);
        getContentPane().add(new InteractivePanel(plot));
        LineRenderer lines = new DefaultLineRenderer2D();
        plot.setLineRenderers(data, lines);
        plot.getPointRenderers(data).get(0).setColor(Color.BLACK);
        plot.getLineRenderers(data).get(0).setColor(Color.white);
        System.err.println(plot.getBounds());
        
        
	}

	public static void main(String[] args) {
		
		int longitudeBins = (int)(((maxGalacticLongitude - minGalacticLongitude)/tilingDiameter +1) * 1.05);
		int latitudeBins = (int)((maxGalacticLatitude - minGalacticLatitude)/tilingDiameter +3);
		
		System.err.println(latitudeBins + " "+ longitudeBins);
		
		double longitudeBinWidth = (maxGalacticLongitude - minGalacticLongitude) / longitudeBins;
		double latitudeBinWidth = (maxGalacticLatitude - minGalacticLatitude)/ latitudeBins;
		
		List<Grid> gridPoints = new ArrayList<>();
		double lon[] = new double[longitudeBins];
		double lat[] = new double[latitudeBins];
		
		for(int b=0;b<latitudeBins;b++) lat[b] = minGalacticLatitude + b*latitudeBinWidth;
		for(int l=0;l<longitudeBins;l++) lon[l] = minGalacticLongitude + l*longitudeBinWidth;
		int x=0;
		for(double b : lat){
			double offset = 0;
			if(x++ %2==0) offset = tilingDiameter/2.0;
			 for(double l: lon){
				 gridPoints.add(new Grid(b,l+offset));
			 }
		}
		System.err.println(gridPoints);
		System.err.println(gridPoints.size());
		new SMIRF_tileGalacticPlane(gridPoints).setVisible(true);
	}
}
