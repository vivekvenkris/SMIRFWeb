package standalones;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


import com.github.drapostolos.rdp4j.DirectoryPoller;
import com.github.drapostolos.rdp4j.DirectoryPollerBuilder;
import com.github.drapostolos.rdp4j.RegexFileFilter;
import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

import listeners.StatusLIstener;
import service.ProcessMonitorService;


public class SFtpMonitorExample {


    private String host = "mpsr-bf00";
    
    private String root = "/data/mopsr/smirf/BS" + host.replaceAll("\\D+","");


    public static void main(String[] args) throws Exception 
    {
        new SFtpMonitorExample().doMain(args);
    }

	private void doMain(String[] args) throws InterruptedException {
        
        DirectoryPollerBuilder dpb =  DirectoryPoller.newBuilder();


        PolledDirectory polledDirectory = new ProcessMonitorService( host, root );
        
        try {
			for(FileElement fileElement : new ProcessMonitorService( host, root ).listFiles()){
				
				dpb.addPolledDirectory(new ProcessMonitorService(host,root+"/"+fileElement.getName()));
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        DirectoryPoller dp = dpb
				            .addListener(new StatusLIstener())
				            .enableFileAddedEventsForInitialContent()
				            .enableParallelPollingOfDirectories()
				//            .setPollingInterval(10, TimeUnit.MINUTES)
				            .setPollingInterval(2, TimeUnit.SECONDS)
				            .start();

        TimeUnit.HOURS.sleep(2);

        dp.stop();	}
}
