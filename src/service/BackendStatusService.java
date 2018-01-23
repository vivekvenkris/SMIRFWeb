package service;

import java.net.ConnectException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;

import bean.BackendStatus;
import exceptions.BackendException;
import util.BackendConstants;
import util.Utilities;

public class BackendStatusService implements BackendConstants{
	
	private boolean isON() throws BackendException{
		try {
			this.sendCommand(state);
			return true;
		} catch (BackendException e) {
			return false;
		}
	}
	
	public BackendStatus getBackendStatus() throws  BackendException{
		return new BackendStatus(isON(), sendCommand(state), sendCommand(startUTC));
	}
		
	
	public synchronized String sendCommand(String message) throws BackendException{
		try{
			String response = Utilities.talkToServer(message, backendIP, backendStatusPort);
			return response;		
		}
		catch (Exception e) {
			throw new BackendException(" Backend failed: Cause: "+ e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}
	
	public static void main(String[] args) throws BackendException, InterruptedException {
		
		BackendStatusService service = new BackendStatusService();
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		while(true){
			System.err.println(stopWatch.getTime() + " " + service.getBackendStatus() );
			Thread.sleep(2000);
		}
		
	}
	

}
