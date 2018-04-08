package manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import bean.PointingTO;
import bean.UserInputs;
import exceptions.BackendException;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.EphemException;
import exceptions.NoSourceVisibleException;
import exceptions.ObservationException;
import exceptions.SchedulerException;
import exceptions.TCCException;
/**
 * Any Scheduler that you want, needs to have implementations of some basic functionalities.
 * This interface gives the minimalistic functions that a Scheduler needs to run.
 * Any Scheduler implementation should be derived form this interface
 * 
 * @author Vivek Venkatraman Krishnan
 *
 */
public interface Schedulable {
	
	LinkedList<PointingTO> thisSession = new LinkedList<PointingTO>();

	/**
	 * Give the list of pointings and other inputs from user to initialise the Scheduler.
	 * @param userInputs
	 * @throws SchedulerException
	 */
	void init(UserInputs userInputs)  throws SchedulerException;
	
	/**
	 * This is the highest level thread that will start the scheduler and over look exceptions.
	 * It will attempt to fix exceptions and rerun the scheduler, if possible.
	 * If not, it will send a FATAL email and bail out
	 */
	void startAndOverlook();
	
	/**
	 * This will be the core operation of the scheduler that manages the observation.
	 * it goes through an infinite loop and asks for a next() pointing every time and 
	 * observes it. The only ways this can bail out is if there is some error or the session end time is reached
	 * @throws EmptyCoordinatesException 
	 * @throws CoordinateOverrideException 
	 * @throws TCCException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws ObservationException 
	 * @throws IOException 
	 * @throws SchedulerException 
	 * @throws NoSourceVisibleException 
	 * @throws EphemException 
	 * @throws BackendException 
	 */
	void start() throws TCCException, CoordinateOverrideException, EmptyCoordinatesException, InterruptedException, ExecutionException, ObservationException, IOException, NoSourceVisibleException, SchedulerException, BackendException, EphemException;
	
	/**
	 * This is where the whole logic of which source to go to next and how long to observe, is implemented.
	 * start() will call this method after every observation to get the next source to go to.
	 * @return PointingTO
	 * @throws CoordinateOverrideException
	 * @throws EmptyCoordinatesException
	 * @throws TCCException
	 * @throws NoSourceVisibleException
	 * @throws SchedulerException
	 */
	PointingTO next() 
			throws CoordinateOverrideException, EmptyCoordinatesException, 
			TCCException, NoSourceVisibleException, SchedulerException;
	
	/**
	 * This specifies the starting MD angle for the observation. The scheduler will wait until this angle is reached
	 * before it starts to observe the source
	 * @return
	 */
	double getRadStartMDPosition();
	
	/**
	 * Gives a list of pointings to use if the user does not give any.
	 * @return List<PointingTO>
	 */
	List<PointingTO>  getDefaultPointings();
	
	/**
	 * How to "finish" a schedule?
	 */
	void finish();
	
	/**
	 * How to terminate a schedule?
	 */
	void terminate();
	
	/**
	 * A status checker to see if the scheduler is currently running.
	 * @return
	 */
	boolean isRunning();
	String getType();
	
	void addToSession(PointingTO to);
	
	void shutdown() throws IOException;

}
