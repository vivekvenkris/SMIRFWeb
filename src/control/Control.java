package control;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import bean.BackendStatus;
import bean.ObservationTO;
import bean.TCCStatus;
import exceptions.SchedulerException;
import mailer.Mailer;
import manager.Schedulable;
import util.ConfigManager;
import util.SMIRFConstants;

@WebListener
public class Control implements ServletContextListener {
	
	private static boolean finishCall = false;
	private static boolean terminateCall = false;
	private static ObservationTO currentObservation = null;

	private static Schedulable scheduler = null;
	private static Integer schedulerLock = 1;

	private static Integer observationLock = 1;
	
	private static BackendStatus backendStatus;
	private static Integer backendLock = 2;
	
	private static TCCStatus tccStatus;
	private static Integer tccLock = 3;
	
	private static String machineSummary = "";
	private static Integer machineSummaryLock = 4;
	
	private static String allMessages = "";
	private static String messages = "";
	
	private static Boolean polling = false;
	
	private static String emulateForUTC = null;
	
	private static ExecutorService executorService = Executors.newFixedThreadPool(24);
	
	private void shutdownAndAwaitTermination() {
		executorService.shutdown(); // Disable new tasks from being submitted
		executorService.shutdownNow(); // Cancel currently executing tasks
		
		System.err.println("Awaiting termination.");

		try {
			
			/**
			 *  Wait a while for tasks to respond to being cancelled
			 */
			if (!executorService.awaitTermination(30, TimeUnit.SECONDS))
				System.err.println("Pool did not terminate");
			
		} catch (InterruptedException ie) {
			
			/**
			 * (Re-)Cancel if current thread also interrupted
			 */
			executorService.shutdownNow();
			
			/**
			 *  Preserve interrupt status
			 */
			Thread.currentThread().interrupt();
		}
		
		System.err.println("All threads terminated.");
	}

	public static void reset(){
		finishCall = terminateCall = false;
	}
	
	public static void finish(){
		finishCall = true;
	}
	
	public static void terminate(){
		terminateCall = true;
	}
	
	public static boolean isFinishCall() {
		return finishCall;
	}
	public static void setFinishCall(boolean finishCall) {
		Control.finishCall = finishCall;
	}
	public static boolean isTerminateCall() {
		return terminateCall;
	}
	public static void setTerminateCall(boolean terminateCall) {
		Control.terminateCall = terminateCall;
	}
	
	public static synchronized ObservationTO getCurrentObservation() {
		
		synchronized (observationLock) {
			return currentObservation;
		}
	}
	
	public static synchronized void setCurrentObservation(ObservationTO currentObservation) {
		synchronized (observationLock) {
			Control.currentObservation = currentObservation;

		}
	}
	
	public static boolean isThereAnActiveObservation(){
		return (Control.getCurrentObservation() != null);
	}
	
	public static void AddMessage(String message){
		synchronized (messages) {
			messages += message;
			
		}
	}
	
	public static String getAllMessages() {
		return allMessages;
	}
	public static void setAllMessages(String allMessages) {
		Control.allMessages = allMessages;
	}
	public static String getMessages() {
		return messages;
	}
	public static void setMessages(String messages) {
		Control.messages = messages;
	}

	public static Schedulable getScheduler() {
		synchronized (schedulerLock) {
		return scheduler;
		}
	}

	public static void setScheduler(Schedulable scheduler) {
		synchronized (schedulerLock) {
			Control.scheduler = scheduler;
		}
	}

	public static BackendStatus getBackendStatus() {
		synchronized (backendLock) {
			return backendStatus;
		}
	}

	public static void setBackendStatus(BackendStatus backendStatus) {
		synchronized (backendLock) {
			Control.backendStatus = backendStatus;
		}
	}

	public static TCCStatus getTccStatus() {
		synchronized (tccLock) {
			return tccStatus;
		}
	}

	public static void setTccStatus(TCCStatus tccStatus) {
		synchronized (tccLock) {
			Control.tccStatus = tccStatus;
		}
	}

	public static ExecutorService getExecutorService() {
			return executorService;
	}
	
	

	
	public static String getMachineSummary() {
		synchronized (machineSummaryLock) {
			return machineSummary;
		}
	}

	public static void setMachineSummary(String machineSummary) {
		synchronized (machineSummaryLock) {
			Control.machineSummary = machineSummary;
	
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		System.err.println("Context destroy called. Shutting down executors.");
		shutdownAndAwaitTermination();
	}
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		System.err.println( "Shutdown hook initialised from class: " + this.getClass().getName() );
	}

	public static Boolean isPolling() {
		synchronized (polling) {
			return polling;

		}
	}

	public static void setPolling() {
		synchronized (polling) {
			Control.polling = true;
		}
	}

	public static String getEmulateForUTC() {
		return emulateForUTC;
	}

	public static void setEmulateForUTC(String emulateForUTC) {
		Control.emulateForUTC = emulateForUTC;
	}
	
	
	
	
	public static boolean otherSchedulers() throws IOException {
		
		String str = readControlFile();
		
		if(str == null  || str.equals("") ) return false;
		
				
		System.err.println("Another scheduler already running : " + str);
		
		Mailer.sendEmail(new SchedulerException("Another scheduler already running : " + str, SMIRFConstants.levelFatal, null ));
		
		
		return true;
		
	}
	
	public static String readControlFile() throws IOException {
		
		File file = new File(ConfigManager.getSmirfMap().get("CONTROL_FILE"));
		if(!file.exists()) return null;
		
		Path myPath = Paths.get(file.getAbsolutePath());
		List<String> lines =  Files.readAllLines(myPath);
		
		if(lines.isEmpty()) return "";
		
		return lines.get(0);


	}
	
	public static void writeControlFile() throws IOException {
		
		
        Files.write(Paths.get(ConfigManager.getSmirfMap().get("CONTROL_FILE")), "SMIRF".getBytes());
        System.err.println("Control file written");
		
	}
	
	public static void deleteControlFile() throws IOException {
		
		String str = readControlFile();

		if(str.equals("SMIRF")) new File(ConfigManager.getSmirfMap().get("CONTROL_FILE")).delete();	
		
		else Mailer.sendEmail(new SchedulerException("obs.running file changed by "+ str + " while SMIRF was running. Possible bug in " + str + "." , SMIRFConstants.levelFatal, null ));

	}
		
	
	
	
	
}
