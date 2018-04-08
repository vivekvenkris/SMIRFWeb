package listeners;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import bean.BackendStatus;
import bean.TCCStatus;
import control.Control;
import exceptions.BackendException;
import exceptions.TCCException;
import service.BackendStatusService;
import service.DBService;
import service.TCCStatusService;

@WebListener
public class StatusPooler implements ServletContextListener {

	boolean terminate = false;
	Future<Boolean> poller = null;
	Future<Boolean> tbSourceUpdater = null;
	Future<Boolean> machineSummaryPooler = null;
	@Override
	public void contextInitialized(ServletContextEvent event) {
		startPollingThreads();
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {

		System.err.println("Waiting for thread pool to stop");
		
		terminate = true;
		
		try {
			System.err.println("waiting to terminate status poller");
			poller.get();
			System.err.println("waiting to terminate TB source updater");
			tbSourceUpdater.get();
			System.err.println("waiting to terminate Machine Summary updater");
			machineSummaryPooler.get();
			System.err.println("All threads killed");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

	}

	public void startPollingThreads(){

		Callable<Boolean> pollingThread = new Callable<Boolean>() {

			@Override
			public Boolean call() {

				BackendStatusService backendStatusService = new BackendStatusService();
				TCCStatusService  tccStatusService = new TCCStatusService();

				while( true ){

					if(Thread.currentThread().isInterrupted()){
						System.err.println("status pooler received interrupt. exiting thread");
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
					
					if(terminate) return true;

				}

				return true;
			}
		};


		Callable<Boolean> tbSourceUpdateThread = new Callable<Boolean>() {
			@Override
			public Boolean call() {

				while(true) {

					if(Thread.currentThread().isInterrupted()){
						System.err.println("TB source updater received interrupt. exiting thread");
						break;
					}	

					DBService.updatePulsarLastObservedTimes();

					try {
												
						for(int i=0; i< 60; i++) {
							
							Thread.sleep(10 * 1000);
							
							if(terminate) break;
							
						}
						
					} catch (InterruptedException e) {
						System.err.println(this.getClass().getName() + "received interrupt. exiting thread");
						break;
					}

					if(terminate) return true;

				}

				return true;
			}
		};

		
		Callable<Boolean> machineSummaryThread = new Callable<Boolean>() {
			@Override
			public Boolean call() {
				
				BackendStatusService service = new BackendStatusService();
				
				while(true) {

					if(Thread.currentThread().isInterrupted()){
						System.err.println("TB source updater received interrupt. exiting thread");
						break;
					}	
					
					Control.setMachineSummary(service.getBuffersXML());
					
					try {
						service.getDataBlocks("aq", 0);
					} catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException e1) {
						System.err.println("Problem parsing machine summary");
						e1.printStackTrace();
					}


					try {
						
						for(int i=0; i< 6; i++) {
							
							Thread.sleep(10* 1000);
							if(terminate) break;
							
						}
						
					} catch (InterruptedException e) {
						System.err.println(this.getClass().getName() + "received interrupt. exiting thread");
						break;
					}
					
					if(terminate) return true;


				}

				return true;
			}
		};



		poller = Control.getExecutorService().submit(pollingThread);
		tbSourceUpdater = Control.getExecutorService().submit(tbSourceUpdateThread); 
		machineSummaryPooler = Control.getExecutorService().submit(machineSummaryThread);
		
		Control.setPolling();

		System.err.println("*******Status pooler started*******");

	}



}
