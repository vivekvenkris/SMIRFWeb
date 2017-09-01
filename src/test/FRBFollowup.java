package test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.event.ActionEvent;

import bean.Pointing;
import bean.PointingTO;
import bean.UserInputs;
import listeners.StatusPooler;
import manager.DBManager;
import manager.Schedulable;
import manager.TransitScheduler;
import service.EphemService;
import util.SMIRFConstants;
import util.TCCConstants;

public class FRBFollowup {
	
public static void main(String[] args) {
	
	StatusPooler poll = new StatusPooler();
	poll.startPollingThread();
	

	try{
		
		UserInputs inputs = new UserInputs();
				
		inputs.setSchedulerType(SMIRFConstants.dynamicTransitScheduler);
		
		inputs.setUtcStart(EphemService.getUtcStringNow());
		inputs.setTobsInSecs(360);
		inputs.setSessionTimeInSecs(null);
		
		inputs.setDoPulsarSearching(true);
		inputs.setDoPulsarTiming(true);
		inputs.setEnableTCC(true);
		inputs.setEnableBackend(true);
		inputs.setMdTransit(true);
		inputs.setObserver("VVK");
		inputs.setNsOffsetInDeg(0.0);
		inputs.setNsSpeed( TCCConstants.slewRateNSFast );
		
		List<PointingTO> pointingTOs = DBManager.getAllPointingsForPointingType("C").stream().sorted(
				Comparator.comparing(f -> ((PointingTO)f).getAngleRA().getDecimalHourValue())).collect(Collectors.toList());
		
		
		inputs.setPointingTOs(pointingTOs);
		
		Schedulable scheduler = TransitScheduler.createInstance(SMIRFConstants.dynamicTransitScheduler);
		
		scheduler.init(inputs);
		scheduler.start();
		
		
	}
	catch (Exception e) {
		e.printStackTrace();
	}
}




}
