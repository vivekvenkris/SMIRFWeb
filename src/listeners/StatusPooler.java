package listeners;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;


import bean.BackendStatus;
import bean.TCCStatus;
import control.Control;
import exceptions.BackendException;
import exceptions.TCCException;
import service.BackendStatusService;
import service.TCCStatusService;

@WebListener
public class StatusPooler implements ServletContextListener {

	boolean terminate = false;
	Future<Boolean> poller = null;
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		startPollingThread();
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		
		System.err.println("Waiting for thread pool to stop");
		try {
			poller.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
	}
	
	public void startPollingThread(){
		
		Callable<Boolean> pollingThread = new Callable<Boolean>() {

			@Override
			public Boolean call() {
				
				BackendStatusService backendStatusService = new BackendStatusService();
				TCCStatusService  tccStatusService = new TCCStatusService();
				
				while( true ){
					
						if(Thread.currentThread().isInterrupted()){
							System.err.println(this.getClass().getName() + "received interrupt. exiting thread");
							break;
						}
					
						try {
							Control.setBackendStatus(backendStatusService.getBackendStatus());
						} catch (BackendException e) {
							BackendStatus status = new BackendStatus(false, "Error:", e.getMessage());
							Control.setBackendStatus(status);
						}
						
						try {
							Control.setTccStatus(tccStatusService.getTelescopeStatus());
						} catch (TCCException e) {
							TCCStatus status = new TCCStatus();
							status.setOverview("Error:" + e.getMessage());
						}
						
						try {
							Thread.sleep( 2 * 1000 );
						} catch (InterruptedException e) {
							System.err.println(this.getClass().getName() + "received interrupt. exiting thread");
							break;
						}

				}
				
				return true;
			}
		};
		
		 poller = Control.getExecutorService().submit(pollingThread);
		 Control.setPolling();
		 
		 System.err.println("*******Status pooler started*******");
		
	}
	
	

}
