package manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import bean.Angle;
import bean.ObservationTO;
import bean.PointingTO;
import bean.TCCStatus;
import bean.UserInputs;
import control.Control;
import exceptions.BackendException;
import exceptions.CoordinateOverrideException;
import exceptions.DriveBrokenException;
import exceptions.EmptyCoordinatesException;
import exceptions.EphemException;
import exceptions.InvalidFanBeamNumberException;
import exceptions.NoSourceVisibleException;
import exceptions.ObservationException;
import exceptions.SchedulerException;
import exceptions.TCCException;
import mailer.Mailer;
import service.DBService;
import service.TCCService;
import service.TCCStatusService;
import util.Constants;
import util.SMIRFConstants;
import util.TCCConstants;
import util.Utilities;

public abstract class AbstractScheduler implements Schedulable, SMIRFConstants {
	
	protected UserInputs userInputs;
	protected ObservationManager observationManager = new ObservationManager();
	
	protected Future<Boolean> previousObservation = null;
	protected Future<Boolean> sendStitches = null;
	
	protected TCCService tccService = TCCService.createTccInstance();
	
	@Override
	public void startAndOverlook() {
		
		while(!Control.isTerminateCall() && ! Control.isFinishCall() ) {
			Exception exception = null;
			try {
				/**
				 * Start the scheduler
				 */
				start();
				
				
			}catch (Exception e) {
				System.err.println("Overlooker caught " + Utilities.putSpacesBetweenWords(e.getClass().getSimpleName()));
				exception = e;
			}
			
			previousObservation = null; 
			
			if(exception instanceof ExecutionException) {
				Throwable t = ((ExecutionException)exception).getCause();
				
				if(t instanceof Exception) {
					System.err.println("overriding exception with its root cause.");
					exception = (Exception)t;
				}
				
			}
			
			if(Control.isFinishCall() || Control.isTerminateCall()) break;

			if(exception instanceof DriveBrokenException) {
				
				
				TCCStatusService tccStatusService = new TCCStatusService();
				
				DriveBrokenException dbe = (DriveBrokenException)exception;
				
				System.err.println("Drive broken exception has reached the overlooker.");
				System.err.println("Checking if this is really true");
				
				String extra = "";
				
				TCCStatus status = (dbe.getStatusObject() instanceof TCCStatus)? (TCCStatus) dbe.getStatusObject() : Control.getTccStatus();

				if(! status.getNs().getError().equals("None")) {
					extra += "Anansi currently has the following initial error string:" + status.getNs().getError() + "<br/> <br/>";
				}
				
				extra += "Checking the credibility of this error. <br/> <br/>"
						+ "Driving " + dbe.getArm() + " arm alone to test if it is broken. <br/> <br/>"
						+"Telescope's current position:";

				
				extra += status.getNSMDPositionString().replaceAll("\n", "<br/>");
				
				extra +="<br/>";
				
				
				
				Angle initialPositionNS = status.getNSPosition(dbe.getArm());
				double position = initialPositionNS.getRadianValue();
				double increment = 1 * Constants.deg2Rad;
				double newPosition = (position + increment) > SMIRFConstants.maxRadNS ? position - increment : position + increment;
				
				System.err.println("Trying to move " + dbe.getArm()  + " arm to " + newPosition * Constants.rad2Deg + "degrees");
				
				extra += "Trying to move " + dbe.getArm()  + " arm to " + newPosition * Constants.rad2Deg + "degrees <br/>";
				
				try {
					System.err.println("Pointing NS");
					tccService.pointNS(new Angle(newPosition, Angle.DEG), dbe.getArm());
					Thread.sleep(2000);
					System.err.println("Checking if it is driving.");
					while(tccStatusService.isTelescopeDriving()) {
						System.err.println("Test Driving......");
						Thread.sleep(5000);
						if(Control.isTerminateCall()) {
							System.err.println("Terminated while testing drive.");
							tccService.stopTelescope();
							break;
						}
					}
					
					Double finalPosition = Control.getTccStatus().getNSPosition(dbe.getArm()).getDegreeValue();
					
					extra += "Done. Final position of arm: " + finalPosition + "<br/>";

					
					if( Math.abs(newPosition - Control.getTccStatus().getNSPosition(dbe.getArm()).getDegreeValue()) <TCCConstants.OnSourceThresholdRadNS ) {
						extra += "Okay that seem to have worked. Hmmm, weird. Anyway, I am going to count this"
								+ " as a false positive and move on.<br/>";
					}
					else {
						extra += "Hmm, that did not go all the way to the new position but did not give an error either.  "
								+ "I am going to give the observations another try.<br/>";
					}
					
					extra += "decreasing severity of exception from FATAL to WARN. Continuing observations. <br/>";
					dbe.setLevel(SMIRFConstants.levelWarn);
					System.err.println("Success, restarting observations and sending warning mail.");
					dbe.setExtra(extra);

					Mailer.sendEmail(dbe);
					
				}catch (DriveBrokenException e) {
					
					extra += "Got the same error again:" + e.getMessage() + "<br/> ";
					extra += "Anansi currently has the following final error string:" + Control.getTccStatus().getNs().getError() + "<br/> <br/>";
					extra += "I think the telescope is actually broken. Level = FATAL. <br/> <br/>";
					extra += "I am stopping telescope operations.";
					
					dbe.setExtra(extra);
					System.err.println("Failure, Drive is actually broken. Aborting all operations.");
					dbe.setLevel(SMIRFConstants.levelFatal);

					Mailer.sendEmail(dbe);
					break;
					
				} 
				catch (TCCException | InterruptedException e) {
					
					extra += "Some other error now:" + e.getMessage() + "<br/>";
					extra += "I need a human to look at this. <br/>";
					extra += "I am stopping telescope operations. <br/>";
					
					System.err.println("Failure, Other errors. Aborting.");
					e.printStackTrace();
					break;
				}
			}
			
			else if (exception instanceof TCCException) {
				
				exception.printStackTrace();
				Mailer.sendEmail(exception);
				break;
			}
			
			else if ( exception instanceof BackendException) {
				
				exception.printStackTrace();
				Mailer.sendEmail(exception);
				try {
					tccService.stopTelescope();
				} catch (TCCException e) {
					e.printStackTrace();
					Mailer.sendEmail(exception);
				}
				break;
			} 
			
			else {
				exception.printStackTrace();
				Mailer.sendEmail(exception);
				break;
			}
			
//			catch (DriveBrokenException e) {
//				e.printStackTrace();
//			} catch (TCCException | CoordinateOverrideException | EmptyCoordinatesException e) {
//				e.printStackTrace();
//				Mailer.sendEmail(e);
//			} catch (InterruptedException | ExecutionException | ObservationException | EphemException | IOException e) {
//				e.printStackTrace();
//				Mailer.sendEmail(e);
//			} catch (BackendException e) {
//				e.printStackTrace();
//				Mailer.sendEmail(e);
//			} catch (NoSourceVisibleException e) {
//				e.printStackTrace();
//				Mailer.sendEmail(e);
//			} catch (SchedulerException e) {
//				e.printStackTrace();
//				Mailer.sendEmail(e);
//			} 
		}
		
		System.err.println("Exiting overlooker. Resetting all controls");
		
		Control.reset();
		Control.setScheduler(null);
		
		System.err.println("Controls reset. Deleting control file");
		
		try {
			shutdown();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.err.println("Done. THE END.");
		
		
	}

	protected Callable<Boolean> sendStitchesThread = new Callable<Boolean>() {

		@Override
		public Boolean call() throws Exception{
				try {
					System.err.println("Sending stitches for observation - " + Control.getCurrentObservation());
					return observationManager.sendUniqStitchesForObservation( Control.getCurrentObservation());
				} catch (EmptyCoordinatesException | CoordinateOverrideException | InvalidFanBeamNumberException
						| IOException e) {
					e.printStackTrace();
					Mailer.sendEmail(e);
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
				throw e;
			}catch(DriveBrokenException e){
				System.err.println("Observation Manager thinks one of the drives broke during tracking " + observation.getCoords().getPointingTO().getPointingName());	
				System.err.println("Stopping the current observation.");
				observationManager.stopObserving();
				throw e;
			}
			
			
			observationManager.stopObserving();
			
			if(Control.isTerminateCall()) {
				System.err.println("Terminate call initated. Exiting Observation Manager ");

				return true;
			}

			if( observation.getDoPulsarSearch() ) sendStitches.get();
			PostObservationManager.doPostObservationStuff(observation);

			DBManager.makeObservationComplete(observation);
			DBManager.incrementCompletedObservation(observation.getObservingSession());
			DBManager.incrementPointingObservations(observation.getCoords().getPointingTO().getPointingID());


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
	
	@Override
	public void addToSession(PointingTO to) {
		
		if(thisSession != null) {
			
			if(thisSession.size() > 9) {
				thisSession.removeFirst();
			}
			
			thisSession.add(to);
			
		}
		
	}
	
	@Override
	public void shutdown() throws IOException {
		
		Control.deleteControlFile();
		
	}

}
