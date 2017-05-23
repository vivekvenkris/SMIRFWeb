package standalones;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bean.ObservationTO;
import util.BackendConstants;
import util.ConfigManager;

public class ResendStitches {
	public static void main(String[] args) {
		
		List<String> utcs = Arrays.asList( new String[]{"2017-05-23-07:20:42","2017-05-23-07:34:10","2017-05-23-07:46:46" } );
		
		for(String utc : utcs){
			try {
				restartSmirfingObservation(utc);
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
