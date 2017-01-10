package bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang3.exception.ExceptionUtils;

import exceptions.BackendException;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.NoSourceVisibleException;
import exceptions.PointingException;
import exceptions.TCCException;
import manager.DBManager;
import manager.ObservationManager;
import manager.ScheduleManager;
import service.DBService;
import service.EphemService;
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
	
	Boolean utcRendered = false;
	static Integer counter = 0;
	private Integer num = 0;
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
		addMessage("working...");
		System.err.println("counter " + counter++);
		ScheduleManager manager = new ScheduleManager();
		
		try {
			System.err.println(utc + " " + duration* durationUnits + " " + tobs * tobsUnits);
			
			if(this.utc.equals("now")) utc = EphemService.getUtcStringNow();
			else utc = enteredUTC + ".000";
			
			List<Coords> surveyCoordsList = manager.getPointingsForSession(utc, duration* durationUnits ,tobs * tobsUnits);
			if(surveyCoordsList.isEmpty() && SMIRFConstants.simulate) surveyCoordsList.add(new Coords(new PointingTO(DBService.getPointingByID(1)))) ; 
			coordsList.clear();
			if(this.phaseCal) {
				if(this.phaseCalibrator==null) phaseCalibrator = getNearestAndBrightestPhaseCalibrator(utc);		
				coordsList.add( new Coords(new PointingTO(phaseCalibrator)));
			}
			
			if(fluxCalStart){
				if(this.fluxCalibrator == null)  fluxCalibrator = getNearestAndHighDMFluxCalibrator(surveyCoordsList.get(0).getPointingTO(),utc);
				coordsList.add( new Coords(new PointingTO(fluxCalibrator)));
			}
				
			
			coordsList.addAll(surveyCoordsList);
			System.err.println(coordsList);
			System.err.println(coordsList.size());
		} catch (EmptyCoordinatesException | CoordinateOverrideException | PointingException | NoSourceVisibleException | TCCException e) {
			e.printStackTrace();
		}

	}
	
	public void toggleUTCInput(){
		addMessage("toggling UTC");
		if(utc.equals("now")){
			utcRendered = false;
		}
		else {
			utcRendered = true;

		}
		System.err.println("here ....");
	}
	
	public void deleteRow(){
		coordsList.remove(selectedCoord);
	}
	
	public void startSchedule(ActionEvent event){
		List<Coords> finalCoordsList = coordsList;
		
		ScheduleManager manager = new ScheduleManager();
		try {
			manager.startSMIRFScheduler(finalCoordsList, this.duration*this.durationUnits, this.tobs*this.tobsUnits, "VVK");
			addMessage("started...");
		} catch (EmptyCoordinatesException | CoordinateOverrideException | PointingException | TCCException
				| BackendException | InterruptedException e) {
			e.printStackTrace();
			addMessage(e.getMessage(),ExceptionUtils.getStackTrace(e));
		}
		
		
	}
	
	public void stopSchedule(ActionEvent event){
		
	}

	
	private PhaseCalibratorTO getNearestAndBrightestPhaseCalibrator(String utc) throws NoSourceVisibleException{
		List<PhaseCalibratorTO> phaseCalibrators = DBManager.getAllPhaseCalibratorsOrderByFluxDesc();
		List<PhaseCalibratorTO> shortlisted = new ArrayList<>();
		try {
		for(PhaseCalibratorTO p: phaseCalibrators){
			PointingTO pointingTO =  new PointingTO( p);
			Observation observation = new Observation( new Coords( pointingTO ), SMIRFConstants.phaseCalibrationTobs);
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
			Observation observation = new Observation( new Coords( new PointingTO(p)), SMIRFConstants.fluxCalibrationTobs);
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
	
	
	
}
