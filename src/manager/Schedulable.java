package manager;

import java.util.List;

import bean.PointingTO;
import bean.UserInputs;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.NoSourceVisibleException;
import exceptions.SchedulerException;
import exceptions.TCCException;

public interface Schedulable {
	
	void init(UserInputs userInputs)  throws SchedulerException;
	void start();
	PointingTO next() 
			throws CoordinateOverrideException, EmptyCoordinatesException, 
			TCCException, NoSourceVisibleException, SchedulerException;
	
	double getRadStartMDPosition();
	
	List<PointingTO>  getDefaultPointings();
		
	void finish();
	void terminate();
	
	boolean isRunning();
	String getType();

}
