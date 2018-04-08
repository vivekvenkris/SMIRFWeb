package standalones;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bean.ObservationTO;

import java.util.Set;

import mailer.Mailer;
import manager.DBManager;
import manager.ObservationManager;
import util.BackendConstants;
import util.ConfigManager;

public class SMIRF_getUnprocessedObservations {
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		
//		ObservationManager manager = new ObservationManager();
//		manager.waitForPreviousSMIRFSoups();
//		System.err.println("done.");
//		System.exit(0);
		Map<String, Set<String>> utcMap = new HashMap<>();
		
		Set<String> utcs = new LinkedHashSet<>();

		for(Entry<String,Map<Integer,Integer> > nepenthesServer: BackendConstants.bfNodeNepenthesServers.entrySet()){
			String hostname = nepenthesServer.getKey();
			for(Entry<Integer, Integer> bsEntry : nepenthesServer.getValue().entrySet()){
								
				int size = 0;

				System.err.println("Attempting to connect to " + nepenthesServer.getValue());
				Socket socket = new Socket();
				
				socket.connect(new InetSocketAddress(nepenthesServer.getKey(), bsEntry.getValue()),10000);
				System.err.println("Connected to " + nepenthesServer.getValue());
				
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				
				out.println(ConfigManager.getSmirfMap().get("NEPENTHES_CHECK_DIRS"));
				out.flush();
				
			    final ObjectInputStream mapInputStream = new ObjectInputStream(socket.getInputStream());

				Map<String, Set<String>> utcBpSetMap = (Map<String, Set<String>>) mapInputStream.readObject();
				
				utcBpSetMap.entrySet().stream().forEach(f -> {
					
					if(utcMap.containsKey(f.getKey())) {
						utcMap.get(f.getKey()).addAll(f.getValue());
					}
					else {
						utcMap.put(f.getKey(), f.getValue());
					}
					utcs.addAll(f.getValue());
				});
				
				
				out.flush();
				out.close();
				mapInputStream.close();
				socket.close();
				
			}

		}
		
		utcMap.entrySet().stream().forEach( f -> System.err.println( f.getKey() + ": " + f.getValue()));
		
		Set<String> finalSet = new LinkedHashSet<>();

		for(String utc: utcs) {
			
			boolean all = true;
			
			for(Entry<String, Set<String>> entry : utcMap.entrySet()) {
				if(!entry.getValue().contains(utc)) {
					System.err.println(entry.getKey() + " does not contain " + utc );
					all = false;
				}
			}
			
			if(all) finalSet.add(utc);
			
			
		}
		
		for(String utc : finalSet) {
			ObservationTO to = DBManager.getObservationByUTC(utc);
			if(to == null) System.err.println(utc + " is not SMIRF obs");
			else System.err.println(to.getCoords().getPointingTO().getPointingName() + " " + utc);
		}
		
		System.err.println(String.join("\n", finalSet));

	}

}
