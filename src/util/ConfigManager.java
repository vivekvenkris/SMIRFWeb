package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.javatuples.Pair;


public class ConfigManager {
	
	public static void main(String[] args) throws IOException {
		loadConfigs();
	}
	
	private static Map<String, Integer> nepenthesServers;
	private static Map<String, String> smirfMap;
	private static Map<String, String> smirfConventionsMap;
	private static Map<String, String> mopsrMap;
	private static Map<String, String> mopsrBpMap;
	private static Map<String, String> mopsrBpCornerturnMap;
	
	
	private static Map<String, Pair<Integer, Integer > > beamBoundariesMap = new HashMap<>();
	
	private static Integer numFanBeams;
	private static String  edgeNode;
	private static List<String> active_nodes = new ArrayList<>();

	static void loadConfigs() throws IOException{
		
		smirfMap = readConfig(SMIRFConstants.smirfConfig);
		smirfConventionsMap = readConfig(SMIRFConstants.smirfConventionsConfig);
		
		mopsrMap = readConfig(smirfMap.get("MOPSR_CFG"));
		mopsrBpMap = readConfig(smirfMap.get("MOPSR_BP_CFG"));
		mopsrBpCornerturnMap = readConfig(smirfMap.get("MOPSR_BP_CORNERTURN_CFG"));
		
		edgeNode = smirfMap.get("EDGE_NODE");
		
		
		Integer nepenthesBasePort = Integer.parseInt(mopsrMap.get("SMIRF_NEPENTHES_SERVER"));
		Integer numNodes = Integer.parseInt(smirfMap.get("NUM_NODES"));
		
		 Map<String, Integer> nepenthesServers = new HashMap<>();
		
		for(int i=0; i< numNodes; i++){
			
			String nodeName = smirfMap.get( String.format("NODE_%02d", i) );
			
			Integer server = Integer.parseInt(nodeName.replaceAll("\\D+", ""));
			nepenthesServers.put(nodeName, nepenthesBasePort + server);
			
			if(smirfMap.get(String.format("NODE_STATE_%02d", i)).equals("active")) active_nodes.add(nodeName);
			
		}
		
		ConfigManager.nepenthesServers =  Collections.unmodifiableMap(nepenthesServers);
		
		
		numFanBeams = Integer.parseInt(mopsrBpCornerturnMap.get("NBEAM"));
		
		Integer numBP = Integer.parseInt(mopsrBpMap.get("NUM_BP"));
		
		for(String node: active_nodes){
			
			List<Integer> beamProcessors = new ArrayList<>();
			
			for( int bp=0 ; bp< numBP; bp++ ) if( mopsrBpMap.get("BP_" + bp).equals(node) ) beamProcessors.add(bp);
			
			Integer minFB = null, maxFB = null;
			for(Integer bp: beamProcessors) {
				
				Integer min = Integer.parseInt(mopsrBpCornerturnMap.get("BEAM_FIRST_RECV_" + bp));
				Integer max = Integer.parseInt(mopsrBpCornerturnMap.get("BEAM_LAST_RECV_" + bp));
				
				
				if(minFB == null || minFB > min)  minFB = min;
				if(maxFB == null || maxFB < max)  maxFB = max;
				
			}
			beamBoundariesMap.put(node, new Pair<Integer, Integer>(minFB + 1 , maxFB + 1));
			
		}
		

		
	}

	private static Map<String, String> readConfig(String file) throws IOException{

		Map<String, String> map = new HashMap<>();

		try {
			BufferedReader br = new BufferedReader( new FileReader( new File(file)));
			String line;
			while((line = br.readLine()) !=null){
				
				if(line.trim().isEmpty()) continue;
				
				if(line.trim().startsWith("#")) continue;
				
				if(line.contains("#")) line = line.substring(0, line.indexOf("#"));
				
				String[] chunks = line.trim().split("\\s+",2);
				map.put(chunks[0], chunks[1]);
			}
			br.close();
			return Collections.unmodifiableMap(map);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}

	}
	
	
	public static String getServerForFB(int fb){
		
		for(Entry<String, Pair<Integer, Integer>> entry : beamBoundariesMap.entrySet()) if( (fb >= entry.getValue().getValue0()) && ( fb <= entry.getValue().getValue1() ) ) return entry.getKey();
		return null;
		
	}
	
	public static Integer getServerNumberForFB(int fb){
		return  getServerNumberForServerName(getServerForFB(fb));
	}
	
	public static  Integer getServerNumberForServerName(String name){
		return Integer.parseInt(name.replaceAll("\\D+", ""));
	}
	
	

	public static Map<String, Integer> getNepenthesServers() {
		return nepenthesServers;
	}

	public static Map<String, String> getSmirfMap() {
		return smirfMap;
	}

	public static Map<String, String> getMopsrMap() {
		return mopsrMap;
	}

	public static Map<String, String> getMopsrBpMap() {
		return mopsrBpMap;
	}

	public static Map<String, String> getMopsrBpCornerturnMap() {
		return mopsrBpCornerturnMap;
	}

	public static Map<String, Pair<Integer, Integer>> getBeamBoundariesMap() {
		return beamBoundariesMap;
	}

	public static Integer getNumFanBeams() {
		return numFanBeams;
	}

	public static String getEdgeNode() {
		return edgeNode;
	}

	public static List<String> getActive_nodes() {
		return active_nodes;
	}

	public static Map<String, String> getSmirfConventionsMap() {
		return smirfConventionsMap;
	}
	

}
