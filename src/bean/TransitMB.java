package bean;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang3.exception.ExceptionUtils;

import control.Control;
import exceptions.SchedulerException;
import mailer.Mailer;
import manager.DBManager;
import manager.Schedulable;
import manager.TransitScheduler;
import service.EphemService;
import util.SMIRFConstants;
import util.TCCConstants;

@ManagedBean
@RequestScoped
public class TransitMB extends BaseMB {

	Boolean phaseCal;
	Boolean doPulsarSearch;
	Boolean doTiming;
	String observer;
	Boolean enableTCC;
	Boolean enableBackend;
	String nsSpeed;
	List<String> nsSpeeds;
	PhaseCalibratorTO phaseCalibrator;
	List<PhaseCalibratorTO> phaseCalibrators;
	Double nsOffsetDeg;

	Schedulable scheduler = null;


	//TransitScheduleManager manager = new TransitScheduleManager();


	public TransitMB(){
		phaseCal = false;
		doPulsarSearch = true;
		doTiming = true;

		enableBackend = true;
		enableTCC = true;

		phaseCalibrators = DBManager.getAllPhaseCalibrators();

		nsOffsetDeg = 0.0;

		nsSpeeds = new ArrayList<>();
		nsSpeeds.add("Fast");
		nsSpeeds.add("Slow");
		nsSpeed = nsSpeeds.get(0);

		observer = "VVK";


	}



	public void startSMIRFTransit(ActionEvent event){

		if(Control.getScheduler() != null) {
			addMessage("Stop observation before starting another.");
			System.err.println("Stop observation before starting another.");
			return;
		}
		
		try {
			
			if(Control.otherSchedulers()) {
				addMessage("Stop other schedulers. Check email for more.");
				System.err.println("Stop other schedulers. Check email for more.");
				return;
			}
			
			Control.writeControlFile();
			
			
		} catch (IOException e1) {
			addMessage("Could not sanity check obs.running");
			System.err.println("Could not sanity check obs.running");
			e1.printStackTrace();
			return;
		}

		String utc = EphemService.getUtcStringNow();

		ObservationSessionTO observationSessionTO = new ObservationSessionTO(utc, observer, -1 ,-1, null, phaseCal, false, false, 0);
		DBManager.addSessionToDB(observationSessionTO);

		UserInputs inputs = new UserInputs();

		inputs.setSchedulerType(SMIRFConstants.interfacedDynamicScheduler);

		inputs.setUtcStart(utc);

		inputs.setDoPulsarSearching(doPulsarSearch);
		inputs.setDoPulsarTiming(doTiming);
		inputs.setEnableTCC(enableTCC);
		inputs.setEnableBackend(enableBackend);
		inputs.setMdTransit(true);
		inputs.setObserver(observer);
		inputs.setNsOffsetInDeg(nsOffsetDeg);
		inputs.setNsSpeed(nsSpeed.equals("Fast") ? TCCConstants.slewRateNSFast : TCCConstants.slewRateNSSlow );
//		inputs.setPointingTOs(
//				DBManager
//					.getAllPointings()
//						.stream()
//						.filter(f -> !f.getPointingName().startsWith("SMIRF_"))
//						.collect(Collectors.toList())); 
//		System.err.println("Added SMIRF pointing lsit");
		inputs.setPointingTOs(new ArrayList<>());
		
		inputs.setTobsInSecs(null);

		Callable<Boolean> schedulerCallable = new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {


				try {
					scheduler = TransitScheduler.createInstance(SMIRFConstants.interfacedDynamicScheduler);

					scheduler.init(inputs);
					scheduler.startAndOverlook();
					
					

				}catch (SchedulerException e) {
					System.err.println("Exception starting schedluer");
					addMessage("Scheduler could not be started. Reason: " + e.getMessage(), ExceptionUtils.getStackTrace(e));
					e.printStackTrace();
					Mailer.sendEmail(e);

				}

				return true;
			}
		};

		Future<Boolean> schedulerFuture =  Control.getExecutorService().submit(schedulerCallable);
		
		Mailer.sendEmail("SMIRF started at approximate UTC = " + EphemService.getUtcStringNow() + "  and Local = " + LocalDateTime.now() );
				
	}


	public void keepPageAlive(){
				
	}


	public String terminateScheduleTest(){
		terminateSchedule(null);
		return "transit";
	}

	public String finishScheduleTest(){
		finishSchedule(null);
		return "transit";
	}

	public String startSMIRFTransitTest(){
		startSMIRFTransit(null);
		return "transit";
	}


	public void terminateSchedule(ActionEvent event){


		if(Control.getScheduler() !=null){

			Control.getScheduler().terminate();
			addMessage("Terminated.");
			Mailer.sendEmail("SMIRF terminated at approximate UTC = " + EphemService.getUtcStringNow() + "  and Local = " + LocalDateTime.now() );
			return;
		}

		addMessage("No scheduler running to terminate.");
		//	
		//		if(Control.getCurrentObservation() ==null){
		//			addMessage("No observation running at the moment.");
		//			return;
		//		}
		//		
		//		Control.setTerminateCall(true);
		//		
		//		while(manager.getScheduler()!= null && manager.getScheduler().isDone());
		//		
		//		try {
		//			
		//			BackendService.createBackendInstance().stopBackend();
		//			TCCService.createTccInstance().stopTelescope();
		//
		//		} catch (BackendException e) {
		//			
		//			e.printStackTrace();
		//			addMessage(e.getMessage());
		//
		//			
		//		} catch (TCCException e) {
		//			
		//			e.printStackTrace();
		//			addMessage(e.getMessage());
		//
		//		}
		//		
		//		try {
		//			manager.cancelSmirfingObservation(Control.getCurrentObservation());
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//			addMessage(e.getMessage());
		//
		//		}
		//		Control.setTerminateCall(false);
		//
		//		addMessage("Terminated.");
	}

	public void finishSchedule(ActionEvent event){


		if(Control.getScheduler() !=null){

			Control.getScheduler().terminate();
			addMessage("Schedule will finish at the end of this observation.");
			return;
		}

		addMessage("No scheduler running to finish.");

		//		if(Control.getCurrentObservation() ==null){
		//			addMessage("No observation running at the moment.");
		//			return;
		//		}
		//		
		//		Control.setFinishCall(true);
		//		addMessage("Schedule will finish at the end of this observation.");

	}



	public Boolean getPhaseCal() {
		return phaseCal;
	}

	public void setPhaseCal(Boolean phaseCal) {
		this.phaseCal = phaseCal;
	}

	public Boolean getDoPulsarSearch() {
		return doPulsarSearch;
	}

	public void setDoPulsarSearch(Boolean doPulsarSearch) {
		this.doPulsarSearch = doPulsarSearch;
	}

	public Boolean getDoTiming() {
		return doTiming;
	}

	public void setDoTiming(Boolean doTiming) {
		this.doTiming = doTiming;
	}

	public String getObserver() {
		return observer;
	}

	public void setObserver(String observer) {
		this.observer = observer;
	}

	public Boolean getEnableTCC() {
		return enableTCC;
	}

	public void setEnableTCC(Boolean enableTCC) {
		this.enableTCC = enableTCC;
	}

	public Boolean getEnableBackend() {
		return enableBackend;
	}

	public void setEnableBackend(Boolean enableBackend) {
		this.enableBackend = enableBackend;
	}


	public String getNsSpeed() {
		return nsSpeed;
	}

	public void setNsSpeed(String nsSpeed) {
		this.nsSpeed = nsSpeed;
	}

	public List<String> getNsSpeeds() {
		return nsSpeeds;
	}

	public void setNsSpeeds(List<String> nsSpeeds) {
		this.nsSpeeds = nsSpeeds;
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




}


