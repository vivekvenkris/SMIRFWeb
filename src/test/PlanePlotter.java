package test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontFormatException;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.sun.javafx.webkit.theme.Renderer;

import bean.FluxCalibratorTO;
import bean.PhaseCalibratorTO;
import bean.PointingTO;
import manager.DBManager;
import util.SMIRFConstants;

/**
 * @see http://stackoverflow.com/a/13794076/230513
 * @see http://stackoverflow.com/questions/8430747
 * @see http://stackoverflow.com/questions/8048652
 * @see http://stackoverflow.com/questions/7231824
 * @see http://stackoverflow.com/questions/7205742 O
 * @see http://stackoverflow.com/questions/7208657
 * @see http://stackoverflow.com/questions/7071057
 * @see http://stackoverflow.com/questions/8736553
 */
public class PlanePlotter extends JFrame {

	private static final String title = "SMIRF pointings";
	private static final Random rand = new Random();


	public PlanePlotter(String s) throws FontFormatException, IOException {
		super(s);
		final ChartPanel equatorialPanel = createPanel(true);
		final ChartPanel galacticPanel = createPanel(false);
		
		equatorialPanel.setPreferredSize(new Dimension(1920, 1080));
		galacticPanel.setPreferredSize(new Dimension(1920, 1080));
		
		JPanel controlPanel = new JPanel();
		
		JButton galactic = new JButton("Galactic coordinates");
		
		JFrame frame = this;
		
		galactic.setAction(new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.remove(equatorialPanel);
				frame.add(galacticPanel, BorderLayout.CENTER);
				frame.repaint();

			}
		});
		
		JButton equatorial = new JButton("Equatorial coordinates");
		
		equatorial.setAction(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.remove(galacticPanel);
				frame.add(equatorialPanel, BorderLayout.CENTER);
				frame.repaint();
			}
		});
		
		equatorial.setText("EQ");
		galactic.setText("GAL");
		
		JButton refresh = new JButton("refesh");
		
		this.add(equatorialPanel, BorderLayout.CENTER);
		
		controlPanel.add(galactic);
		controlPanel.add(equatorial);
		
		this.add(controlPanel, BorderLayout.SOUTH);
	}

	private ChartPanel createPanel(boolean equatorial) throws FontFormatException, IOException {

		JFreeChart jfreechart = null ;
		
		if(equatorial) {
		jfreechart = ChartFactory.createScatterPlot(
				title, "RA (hours)", "DEC (degrees)", CreateEquatorialGridded(),

				PlotOrientation.VERTICAL, true, true, false);
		}
		else {
			jfreechart = ChartFactory.createScatterPlot(
					title, "GL (degrees)", "GB (degrees)", CreateGalactic(),

					PlotOrientation.VERTICAL, true, true, false);
			}
		
		InputStream is = new FileInputStream("/home/vivek/monaco.ttf");
		java.awt.Font customFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, is);
		customFont = customFont.deriveFont(24f);
		jfreechart.getTitle().setFont(customFont);


		XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
		xyPlot.setDomainCrosshairVisible(true);
		xyPlot.setRangeCrosshairVisible(true);
		

		XYItemRenderer renderer = xyPlot.getRenderer();
		
        SpectrumPaintScale ps = new SpectrumPaintScale(0, xyPlot.getSeriesCount() * 2);
        
        for(int i=0; i < xyPlot.getSeriesCount(); i++) {
        	
        	renderer.setSeriesPaint(i, ps.getPaint(i* 2));
    		renderer.setSeriesShape(i,  DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[1]);
        	
        }

		
		
//		renderer.setSeriesPaint(1, Color.BLUE);
//		renderer.setSeriesShape(1,  DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[1]);
//		
//		renderer.setSeriesPaint(2, Color.RED);
//		renderer.setSeriesShape(2,  DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[1]);
//		
//		renderer.setSeriesPaint(3, Color.GREEN);
//		renderer.setSeriesShape(3,  DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[1]);


		XYToolTipGenerator xyToolTipGenerator = new XYToolTipGenerator() {

			@Override
			public String generateToolTip(XYDataset dataset, int series, int item) {

				double x = dataset.getXValue(series, item);
				double y = dataset.getYValue(series, item);
				return "(" + x + "," + y + ")";
			}
		};
		renderer.setToolTipGenerator(xyToolTipGenerator);
	
		adjustAxis((NumberAxis) xyPlot.getDomainAxis(), true);
		adjustAxis((NumberAxis) xyPlot.getRangeAxis(), false);
		xyPlot.setBackgroundPaint(Color.white);
		return new ChartPanel(jfreechart);
	}

	private void adjustAxis(NumberAxis axis, boolean vertical) {
		axis.setAutoRange(true);
		axis.setTickUnit(new NumberTickUnit(5));
		axis.setVerticalTickLabels(vertical);
	}

	private XYDataset CreateGalactic() {
		XYSeries galacticPointings = new XYSeries("Galactic pointings");
		XYSeries galacticPointingsDone = new XYSeries("Galactic pointings Done");
		XYSeries fluxCals = new XYSeries("Flux Calibrators");
		XYSeries phaseCals = new XYSeries("Phase Calibrators");
		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		List<PointingTO> tos = DBManager.getAllPointings();

		for(PointingTO to : tos){
			if(to.getNumObs() == 0) galacticPointings.add(to.getAngleLON().getDegreeValue(), to.getAngleLAT().getDegreeValue());
			else galacticPointingsDone.add(to.getAngleLON().getDegreeValue(), to.getAngleLAT().getDegreeValue());
		}

		List<PointingTO> tbs = DBManager.getAllFluxCalibrators().stream().map(f -> new PointingTO(f)).collect(Collectors.toList());

		for(PointingTO to : tbs){
			fluxCals.add(to.getAngleLON().getDegreeValue(), to.getAngleLAT().getDegreeValue());
		}

		List<PointingTO> pcs = DBManager.getAllPhaseCalibrators().stream().map(f -> new PointingTO(f)).collect(Collectors.toList());

		for(PointingTO to : pcs){
			phaseCals.add(to.getAngleLON().getDegreeValue(), to.getAngleLAT().getDegreeValue());
		}


		xySeriesCollection.addSeries(galacticPointings);
		xySeriesCollection.addSeries(galacticPointingsDone);
		xySeriesCollection.addSeries(fluxCals);
		xySeriesCollection.addSeries(phaseCals);

		return xySeriesCollection;
	}
	
	private XYDataset CreateEquatorialGridded() {
		
		

		

		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		List<PointingTO> tos = DBManager.getAllPointings().stream().filter(f -> SMIRFConstants.SMIRFPointingSymbols.contains(f.getType())).collect(Collectors.toList());
		Map<Integer, XYSeries> seriesMap = new HashMap<>();
		

		for(PointingTO to : tos){
			
			XYSeries series = seriesMap.getOrDefault(to.getNumObs(), new XYSeries(to.getNumObs()));
			
			series.add(to.getAngleRA().getDecimalHourValue(), to.getAngleDEC().getDegreeValue());
			seriesMap.put(to.getNumObs(), series);
		}

		seriesMap.entrySet().stream().forEach(f -> xySeriesCollection.addSeries(f.getValue()));
		

		return xySeriesCollection;
		
	}


	private XYDataset CreateEquatorial() {

		XYSeries galacticPointings = new XYSeries("Galactic pointings");
		XYSeries galacticPointingsDone = new XYSeries("Galactic pointings Done");

		XYSeries fluxCals = new XYSeries("Flux Calibrators");
		XYSeries phaseCals = new XYSeries("Phase Calibrators");

		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		List<PointingTO> tos = DBManager.getAllPointings();

		for(PointingTO to : tos){
			
			if(to.getNumObs() == 0) galacticPointings.add(to.getAngleRA().getDecimalHourValue(), to.getAngleDEC().getDegreeValue());
			else galacticPointingsDone.add(to.getAngleRA().getDecimalHourValue(), to.getAngleDEC().getDegreeValue());
		}

		List<FluxCalibratorTO> tbs = DBManager.getAllFluxCalibrators();

		for(FluxCalibratorTO to : tbs){
			fluxCals.add(to.getAngleRA().getDecimalHourValue(), to.getAngleDEC().getDegreeValue());
		}

		List<PhaseCalibratorTO> pcs = DBManager.getAllPhaseCalibrators();

		for(PhaseCalibratorTO to : pcs){
			phaseCals.add(to.getAngleRA().getDecimalHourValue(), to.getAngleDEC().getDegreeValue());
		}


		xySeriesCollection.addSeries(galacticPointings);
		xySeriesCollection.addSeries(galacticPointingsDone);
		xySeriesCollection.addSeries(fluxCals);
		xySeriesCollection.addSeries(phaseCals);

		return xySeriesCollection;
	}
	
	 private static class SpectrumPaintScale implements PaintScale {

	        private static final float H1 = 0f;
	        private static final float H2 = 1f;
	        private final double lowerBound;
	        private final double upperBound;

	        public SpectrumPaintScale(double lowerBound, double upperBound) {
	            this.lowerBound = lowerBound;
	            this.upperBound = upperBound;
	        }

	        @Override
	        public double getLowerBound() {
	            return lowerBound;
	        }

	        @Override
	        public double getUpperBound() {
	            return upperBound;
	        }

	        @Override
	        public Paint getPaint(double value) {
	            float scaledValue = (float) (value / (getUpperBound() - getLowerBound()));
	            float scaledH = H1 + scaledValue * (H2 - H1);
	            return Color.getHSBColor(scaledH, 1f, 1f);
	        }
	    }


	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				PlanePlotter demo = null;
				try {
					demo = new PlanePlotter(title);
				} catch (FontFormatException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				demo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				demo.pack();
				demo.setLocationRelativeTo(null);
				demo.setVisible(true);
			}
		});
	}
}