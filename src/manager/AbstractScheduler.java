package manager;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import bean.ObservationTO;
import bean.UserInputs;
import control.Control;
import exceptions.SchedulerException;
import service.TCCService;
import util.SMIRFConstants;

public abstract class AbstractScheduler implements Schedulable, SMIRFConstants {
	
	protected UserInputs userInputs;
	protected ObservationManager observationManager = new ObservationManager();
	
	protected Future<Boolean> previousObservation = null;
	protected Future<Boolean> sendStitches = null;
	
	protected TCCService tccService = TCCService.createTccInstance();

	protected Callable<Boolean> sendStitchesThread = new Callable<Boolean>() {

		@Override
		public Boolean call() throws Exception {
			try { 
				System.err.println("Sending stitches for observation - " + Control.getCurrentObservation());
				return observationManager.sendUniqStitchesForObservation( Control.getCurrentObservation());
			}catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}

	};
	
	protected Callable<Boolean> manageObservation = new Callable<Boolean>() {

		@Override
		public Boolean call() throws Exception {
			
			ObservationTO observation = Control.getCurrentObservation();
			System.err.println("Observation started.");
			try {
				observationManager.waitForObservationCompletion(observation);
			}catch(InterruptedException e){
				System.err.println("Observation interrupted before completion");	
				observationManager.stopObserving();
				return false;
			}
			
			observationManager.stopObserving();
			
			if(Control.isTerminateCall()) {
				return true;
			}

			if( observation.getDoPulsarSearch() ) sendStitches.get();
			PostObservationManager.doPostObservationStuff(observation);

			DBManager.makeObservationComplete(observation);
			DBManager.incrementCompletedObservation(observation.getObservingSession());

			return true;
		}
	};


	@Override
	public void finish() {
		Control.finish();
	}

	@Override
	public void terminate() {
		System.err.println("scheduler is terminating");
		Control.terminate();
		
	}

	@Override
	public boolean isRunning() {
		return Control.getCurrentObservation()!=null;
	}

}
