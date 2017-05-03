package bean;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BubbleChartModel;
import org.primefaces.model.chart.LegendPlacement;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

import exceptions.BackendException;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.NoSourceVisibleException;
import exceptions.PointingException;
import exceptions.TCCException;
import javafx.scene.chart.NumberAxis;
import manager.DBManager;
import manager.ObservationManager;
import manager.ScheduleManager;
import service.BackendService;
import service.DBService;
import service.EphemService;
import util.BackendConstants;
import util.ChartHelper;
import util.SMIRFConstants;
import util.Utilities;

@ManagedBean
@ApplicationScoped
public class SchedulerMB implements Serializable {

	String utc;
	String enteredUTC;
	Integer duration;
	Integer tobs;
	Integer durationUnits;
	Integer tobsUnits;
	Boolean phaseCal;
	Boolean fluxCalStart;
	Boolean fluxCalWhenever;

	PhaseCalibratorTO phaseCalibrator;
	List<PhaseCalibratorTO> phaseCalibrators;

	FluxCalibratorTO fluxCalibrator;
	List<FluxCalibratorTO> fluxCalibrators;

	List<Coords> coordsList;

	Coords selectedCoord;
	ScheduleManager manager = new ScheduleManager();

	Boolean utcRendered = false;
	static Integer counter = 0;
	private Integer num = 0;
	
	Boolean orderMDNS = true;


	private LineChartModel glanceGal;	
	private LineChartModel glanceEq;	
	private LineChartModel glanceTel;	

	public SchedulerMB(){
		utc = "now";
		utcRendered = false;
		phaseCal = fluxCalStart = fluxCalWhenever = true;
		tobs = SMIRFConstants.tobs;
		duration = 10;
		tobsUnits =1;
		durationUnits = 3600;
		phaseCalibrators = DBManager.getAllPhaseCalibrators();
		fluxCalibrators = DBManager.getAllFluxCalibrators();
		coordsList = new ArrayList<>();

		glanceEq = new LineChartModel();
		glanceGal = new LineChartModel();
		glanceTel = new LineChartModel();

		glanceEq.setTitle("This session in RA/DEC");
		glanceGal.setTitle("This session in GL/GB");
		glanceTel.setTitle("This session in NS/MD");
		

		glanceEq.getAxis(AxisType.X).setLabel("RA");
		glanceGal.getAxis(AxisType.X).setLabel("GL");
		glanceTel.getAxis(AxisType.X).setLabel("MD");

		glanceEq.getAxis(AxisType.Y).setLabel("DEC");
		glanceGal.getAxis(AxisType.Y).setLabel("GB");
		glanceTel.getAxis(AxisType.Y).setLabel("NS");

		glanceEq.setZoom(true);
		glanceGal.setZoom(true);
		glanceTel.setZoom(true);

		glanceEq.setLegendPlacement(LegendPlacement.OUTSIDEGRID);
		glanceGal.setLegendPlacement(LegendPlacement.OUTSIDEGRID);
		glanceTel.setLegendPlacement(LegendPlacement.OUTSIDEGRID);

		glanceEq.setLegendPosition("s");
		glanceGal.setLegendPosition("s");
		glanceTel.setLegendPosition("s");
		
		glanceEq.setLegendCols(3);
		glanceGal.setLegendCols(3);
		glanceTel.setLegendCols(3);
		
		LineChartSeries allPointingsEq = new LineChartSeries();
		LineChartSeries allPointingsGal = new LineChartSeries();
		for(PointingTO pointingTO: DBManager.getAllPointings()){
			allPointingsEq.set(pointingTO.getAngleRA().getDecimalHourValue(), pointingTO.getAngleDEC().getDegreeValue());
			//ChartHelper.addToSeries(allPointingsGal,pointingTO.getAngleLON().getDegreeValue(),pointingTO.getAngleLAT().getDegreeValue());
			allPointingsGal.set(pointingTO.getAngleLON().getDegreeValue(),pointingTO.getAngleLAT().getDegreeValue());
		}
		allPointingsEq.setLabel("all pointings");
		allPointingsGal.setLabel("all pointings");
		allPointingsEq.setShowLine(false);
		allPointingsGal.setShowLine(false);
		glanceEq.addSeries(allPointingsEq);
		glanceGal.addSeries(allPointingsGal);
		//ChartHelper.addSeriesToChart(glanceGal, allPointingsGal);
		System.err.println(DBManager.getAllPointings().size() + " " +allPointingsGal.getData().size() + " " + glanceGal.getSeries().get(0).getData().size() + " " + glanceEq.getSeries().get(0).getData().size() );
		
		
		
		//		glance.getAxes().put(AxisType.X, new NumberAxis("RA"));

	}

	public void addMessage(String summary) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  null);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public void addMessage(String summary, String detail) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  detail);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}


	public void showSchedule(ActionEvent event){
		ScheduleManager manager = new ScheduleManager();
		coordsList.clear();

		try {

			if(this.utc.equals("now")) utc = EphemService.getUtcStringNow();
			else utc = enteredUTC + ".000";

			LocalDateTime utcTime =  Utilities.getUTCLocalDateTime(utc);
			utcTime = utcTime.plusSeconds(SMIRFConstants.fluxCalibrationTobs + SMIRFConstants.phaseCalibrationTobs);

			List<Coords> surveyCoordsList = manager.getPointingsForSession(Utilities.getUTCString(utcTime), duration* durationUnits ,tobs * tobsUnits, orderMDNS? Coords.compareMDNS: Coords.compareNSMD);
			if(surveyCoordsList.isEmpty()) throw new NoSourceVisibleException("No pointing is visible to observe at UTC = " + utc);
			
			Coords phaseCalCoords = null;
			Coords fluxCalCoords = null;
			utcTime =  Utilities.getUTCLocalDateTime(utc);
			if(this.phaseCal) {
				if(this.phaseCalibrator==null) phaseCalibrator = getNearestAndBrightestPhaseCalibrator(utc);		
				phaseCalCoords = new Coords(new PointingTO(phaseCalibrator),utcTime);
			}
			
			utcTime = utcTime.plusSeconds(SMIRFConstants.phaseCalibrationTobs);
			if(fluxCalStart){
				if(this.fluxCalibrator == null)  fluxCalibrator = getNearestAndHighDMFluxCalibrator(surveyCoordsList.get(0).getPointingTO(),utc);
				fluxCalCoords =  new Coords(new PointingTO(fluxCalibrator),utcTime);
			}

			if(phaseCalCoords!=null) coordsList.add(phaseCalCoords);
			if(fluxCalCoords!=null) coordsList.add(fluxCalCoords);
			coordsList.addAll(surveyCoordsList);
			System.err.println(coordsList.size());
			drawGlancePlot();
		} catch (EmptyCoordinatesException | CoordinateOverrideException | PointingException | NoSourceVisibleException | TCCException e) {
			e.printStackTrace();
			addMessage(e.getMessage());
		}

	}

	public void drawGlancePlot(){

		if(glanceEq.getSeries().size()>1) glanceEq.getSeries().remove(1);
		if(glanceGal.getSeries().size()>1) glanceGal.getSeries().remove(1);
		glanceTel.clear();
		LineChartSeries scheduleEq = new LineChartSeries();
		LineChartSeries scheduleGal = new LineChartSeries();
		LineChartSeries scheduleTel = new LineChartSeries();
		scheduleEq.setLabel("this session");
		scheduleGal.setLabel("this session");
		scheduleTel.setLabel("this session");
		for(Coords coords: coordsList){
			scheduleEq.set(coords.getPointingTO().getAngleRA().getDecimalHourValue(), coords.getPointingTO().getAngleDEC().getDegreeValue());
			scheduleGal.set(coords.getPointingTO().getAngleLON().getDegreeValue(),coords.getPointingTO().getAngleLAT().getDegreeValue());
			//ChartHelper.addToSeries(scheduleGal,coords.getPointingTO().getAngleLON().getDegreeValue(),coords.getPointingTO().getAngleLAT().getDegreeValue());
			scheduleTel.set(-coords.getAngleMD().getDegreeValue(), coords.getAngleNS().getDegreeValue());
			
		}


		glanceEq.addSeries(scheduleEq);
		glanceGal.addSeries(scheduleGal);
		//ChartHelper.addSeriesToChart(glanceGal, scheduleGal);

		glanceTel.addSeries(scheduleTel);

	}

	public void toggleUTCInput(){
		if(utc.equals("now")){
			utcRendered = false;
		}
		else {
			enteredUTC = EphemService.getUtcStringNow();
			utcRendered = true;

		}
	}

	public void deleteRow(){
		coordsList.remove(selectedCoord);
		drawGlancePlot();
	}

	public void startSchedule(ActionEvent event){
		List<Coords> finalCoordsList = coordsList;

		try {
			ObservationSessionTO sessionTO = new ObservationSessionTO(Utilities.getUTCString(EphemService.getUTCTimestamp()), "VVK", tobs, duration, orderMDNS, phaseCal, fluxCalStart, fluxCalWhenever, finalCoordsList.size());
			DBManager.addSessionToDB(sessionTO);
			manager.startSMIRFScheduler(finalCoordsList, this.duration*this.durationUnits, this.tobs*this.tobsUnits, "VVK",true,true,true,sessionTO);
			addMessage("started...");
		} catch (EmptyCoordinatesException | CoordinateOverrideException | PointingException | TCCException
				| BackendException | InterruptedException e) {
			e.printStackTrace();
			addMessage(e.getMessage(),ExceptionUtils.getStackTrace(e));
		}


	}

	public void terminateSchedule(ActionEvent event){
		manager.terminate();
		while(manager.getScheduler()!= null && manager.getScheduler().isDone());
		addMessage("Terminated.");
	}

	public void finishSchedule(ActionEvent event){
		manager.finish();
		addMessage("Schedule will finish at the end of this observation.");

	}
	
	
	public void test(){
		ObservationTO observation = new ObservationTO(new Coords(new PointingTO(DBService.getPointingByUniqueName("SMIRF_1906-0119"))),900);
		ObservationManager manager = new ObservationManager();
		List<TBSourceTO> tbs = new ArrayList<>();
		tbs.add(new TBSourceTO("J1859+00"));
		tbs.add(new TBSourceTO("J1900-0051"));
		tbs.add(new TBSourceTO("J1901+00"));
		observation.setTiedBeamSources(tbs);
		observation.setObsType(BackendConstants.tiedArrayFanBeam);
		BackendService service = BackendService.createBackendInstance();
		try {
			service.startBackend(observation);
		} catch (BackendException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	


	private PhaseCalibratorTO getNearestAndBrightestPhaseCalibrator(String utc) throws NoSourceVisibleException{
		List<PhaseCalibratorTO> phaseCalibrators = DBManager.getAllPhaseCalibratorsOrderByFluxDesc();
		List<PhaseCalibratorTO> shortlisted = new ArrayList<>();
		try {
			for(PhaseCalibratorTO p: phaseCalibrators){
				PointingTO pointingTO =  new PointingTO( p);
				ObservationTO observation = new ObservationTO( new Coords( pointingTO ), SMIRFConstants.phaseCalibrationTobs);
				if(ObservationManager.observable(observation,utc)) shortlisted.add(p);
			}
		}
		catch ( TCCException | EmptyCoordinatesException | CoordinateOverrideException e) {
			e.printStackTrace();
		}
		if(shortlisted.size() > 0) return shortlisted.get(0);
		else throw new NoSourceVisibleException("No Phase calibrator is in the visible range at the moment");
	}


	private FluxCalibratorTO getNearestAndHighDMFluxCalibrator(PointingTO firstPointing,String utc) throws NoSourceVisibleException{
		List<FluxCalibratorTO> FluxCalibrators = DBManager.getAllFluxCalibratorsOrderByDMDesc();
		FluxCalibratorTO nearest = null;
		Double minRadEquatorialDistance = null;
		try {
			for(FluxCalibratorTO p: FluxCalibrators){
				ObservationTO observation = new ObservationTO( new Coords( new PointingTO(p)), SMIRFConstants.fluxCalibrationTobs);
				if(ObservationManager.observable(observation,utc)) {
					double radEquatorialDistance = Utilities.distance(firstPointing.getAngleRA(), firstPointing.getAngleDEC(), p.getAngleRA(), p.getAngleDEC());
					if(minRadEquatorialDistance == null || minRadEquatorialDistance > radEquatorialDistance) {
						minRadEquatorialDistance = radEquatorialDistance;
						nearest = p;
					}
				}

			}
		}
		catch ( TCCException | EmptyCoordinatesException | CoordinateOverrideException e) {
			e.printStackTrace();
		}
		if(nearest == null) throw new NoSourceVisibleException("No Flux calibrator is in the visible range at the moment");
		return nearest;
	}


	public Coords getSelectedCoord() {
		return selectedCoord;
	}

	public void setSelectedCoord(Coords selectedCoord) {
		this.selectedCoord = selectedCoord;
	}

	public String getUtc() {
		return utc;
	}
	public void setUtc(String utc) {
		this.utc = utc;
	}
	public Integer getDuration() {
		return duration;
	}
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
	public Integer getTobs() {
		return tobs;
	}
	public void setTobs(Integer tobs) {
		this.tobs = tobs;
	}
	public Boolean getPhaseCal() {
		return phaseCal;
	}
	public void setPhaseCal(Boolean phaseCal) {
		this.phaseCal = phaseCal;
	}
	public Boolean getFluxCalStart() {
		return fluxCalStart;
	}
	public void setFluxCalStart(Boolean fluxCalStart) {
		this.fluxCalStart = fluxCalStart;
	}
	public Boolean getFluxCalWhenever() {
		return fluxCalWhenever;
	}
	public void setFluxCalWhenever(Boolean fluxCalWhenever) {
		this.fluxCalWhenever = fluxCalWhenever;
	}
	public Integer getDurationUnits() {
		return durationUnits;
	}
	public void setDurationUnits(Integer durationUnits) {
		this.durationUnits = durationUnits;
	}
	public Integer getTobsUnits() {
		return tobsUnits;
	}
	public void setTobsUnits(Integer tobsUnits) {
		this.tobsUnits = tobsUnits;
	}
	public Boolean getUtcRendered() {
		return utcRendered;
	}
	public void setUtcRendered(Boolean utcRendered) {
		this.utcRendered = utcRendered;
	}

	public PhaseCalibratorTO getPhaseCalibrator() {
		return phaseCalibrator;
	}

	public void setPhaseCalibrator(PhaseCalibratorTO phaseCalibrator) {
		this.phaseCalibrator = phaseCalibrator;
	}

	public List<PhaseCalibratorTO> getPhaseCalibrators() {
		return phaseCalibrators;
	}

	public void setPhaseCalibrators(List<PhaseCalibratorTO> phaseCalibrators) {
		this.phaseCalibrators = phaseCalibrators;
	}

	public FluxCalibratorTO getFluxCalibrator() {
		return fluxCalibrator;
	}

	public void setFluxCalibrator(FluxCalibratorTO fluxCalibrator) {
		this.fluxCalibrator = fluxCalibrator;
	}

	public List<FluxCalibratorTO> getFluxCalibrators() {
		return fluxCalibrators;
	}

	public void setFluxCalibrators(List<FluxCalibratorTO> fluxCalibrators) {
		this.fluxCalibrators = fluxCalibrators;
	}

	public List<Coords> getCoordsList() {
		return coordsList;
	}

	public void setCoordsList(List<Coords> coordsList) {
		this.coordsList = coordsList;
	}

	public static Integer getCounter() {
		return counter;
	}

	public static void setCounter(Integer counter) {
		SchedulerMB.counter = counter;
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}


	public String getEnteredUTC() {
		return enteredUTC;
	}

	public void setEnteredUTC(String enteredUTC) {
		this.enteredUTC = enteredUTC;
	}

	public void increment() {
		num++;
	}

	public LineChartModel getGlanceGal() {
		return glanceGal;
	}

	public void setGlanceGal(LineChartModel glanceGal) {
		this.glanceGal = glanceGal;
	}

	public LineChartModel getGlanceEq() {
		return glanceEq;
	}

	public void setGlanceEq(LineChartModel glanceEq) {
		this.glanceEq = glanceEq;
	}

	public LineChartModel getGlanceTel() {
		return glanceTel;
	}

	public void setGlanceTel(LineChartModel glanceTel) {
		this.glanceTel = glanceTel;
	}

	public Boolean getOrderMDNS() {
		return orderMDNS;
	}

	public void setOrderMDNS(Boolean orderMDNS) {
		this.orderMDNS = orderMDNS;
	}


}
