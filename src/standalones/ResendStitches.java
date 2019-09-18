package standalones;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.io.Files;

import bean.ObservationTO;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.InvalidFanBeamNumberException;
import exceptions.ObservationException;
import manager.DBManager;
import manager.ObservationManager;
import util.BackendConstants;
import util.ConfigManager;

public class ResendStitches {
	public static void main(String[] args) throws IOException, EmptyCoordinatesException, CoordinateOverrideException, InvalidFanBeamNumberException, InterruptedException, ObservationException {
		
		//List<String> utcs = Arrays.asList( new String[]{ "2017-05-25-15:39:21", "2017-05-25-15:52:17", "2017-05-25-16:05:41" } );
		//List<String> utcs = Arrays.asList( new String[]{ "2017-05-25-15:52:17", "2017-05-25-16:05:41", "2017-05-25-16:18:13" } );
//		List<String> utcs = Arrays.asList( new String[]{  "2017-05-26-14:55:40", "2017-05-26-15:08:56" } );
		//List<String> utcs = Arrays.asList( new String[]{ "2017-05-26-12:43:32" } );
		//List<String> utcs = Arrays.asList( new String[]{  "2017-05-27-15:02:52", "2017-05-27-15:15:36","2017-05-27-15:28:32" } );
	//	List<String> utcs = Arrays.asList( new String[]{ "2017-05-29-12:46:48", "2017-05-29-12:59:28" , "2017-05-29-13:12:12" } );
		
		File file = new File("/home/vivek/SMIRF/jars/utcs_to_rerun.txt");
		//File file = new File("/home/vivek/failed.utcs");
		List<String> utcs = java.nio.file.Files.readAllLines(file.toPath());
		
		
		//utcs = Arrays.asList( new String[]{ "2018-05-15-22:41:18"} );
		
		System.err.println(utcs);

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file.getAbsolutePath()+".done")));
		
		ObservationManager manager = new ObservationManager();
		
		int i=1;
		
		for(String utc: utcs) {
			System.err.println("Waiting for previous SMIRF soups");
			manager.waitForPreviousSMIRFSoups();
			System.err.println("Adding a new one num:" + i + "utc:" + utc);
			
		
			
			ObservationTO observationTO = DBManager.getObservationByUTC(utc);
			
			
			
			
			if(observationTO == null) {
				continue;
			}
			
			if(!observationTO.getCoords().getPointingTO().isSMIRFPointing()) {
				
				System.err.println("skipping non SMIRF pointing:" + observationTO.getCoords().getPointingTO().getPointingName() );
				continue;
			}

			
			System.err.println("Adding" + observationTO.getCoords().getPointingTO().getPointingName() + " " + observationTO.getUtc());
			manager.sendUniqStitchesForObservation(observationTO);
			System.err.println("sent unique stitches for the observation");
			
			bw.write(utc);
						
			Thread.sleep( 2 * 60 * 1000 );
			i++;
						
		}
		
		bw.close();
		
		System.exit(0);

		
		for(String utc : utcs){
			try { 
				
				restartSmirfingObservation(utc.trim());
				
				break;
			} catch (IOException e) { 
				e.printStackTrace();
			}
		}

	}

	public static void restartSmirfingObservation(String utc) throws IOException {


		for(Entry<String,Map<Integer,Integer> > nepenthesServer: BackendConstants.bfNodeNepenthesServers.entrySet()){

			String hostname = nepenthesServer.getKey();

			for(Entry<Integer, Integer> bsEntry : nepenthesServer.getValue().entrySet()){

				System.err.println("Attempting to connect to " + nepenthesServer.getValue());
				Socket socket = new Socket();

				socket.connect(new InetSocketAddress(nepenthesServer.getKey(), bsEntry.getValue()),10000);
				System.err.println("Connected to " + nepenthesServer.getValue());

				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader( new InputStreamReader(socket.getInputStream()));	

				out.println(ConfigManager.getSmirfMap().get("NEPENTHES_RESTART_UTC_PREFIX") + utc);


				out.flush();
				out.close();
				in.close();
				socket.close();

			}
		}
	}

}
