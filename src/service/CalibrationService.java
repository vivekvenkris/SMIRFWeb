package service;

import java.io.IOException;

import util.BackendConstants;
import util.Utilities;

public class CalibrationService {
	
	public boolean Calibrate(String utc, String calibratorName) throws IOException, InterruptedException{
		
			System.err.println("Starting calibration for UTC:" + utc);
			
			String process = "cd "+BackendConstants.resultsDir+"/"+utc+ "/"+calibratorName+ "; "+ BackendConstants.calibrateScript;
			
			System.err.println("running: "+ process + " as " + BackendConstants.dadaAtLocalhost);
			
			String response =Utilities.runSSHProcess(BackendConstants.dadaAtLocalhost, process, null, true);
			
			//System.err.println(response);
			process = "cd "+BackendConstants.resultsDir+"/"+utc+"/"+calibratorName+ "; "+BackendConstants.updateDelays;
			System.err.println("running: "+ process);
			response = Utilities.runSSHProcess(BackendConstants.dadaAtLocalhost, process, null, true);
			System.err.println(response);
			
			System.err.println("calibrated.");
			return true;
			

	}

}
