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
	
	private static  Map<String, Map<Integer, Integer>>  nepenthesServers;
	private static Map<String, String> smirfMap;
	private static Map<String, String> smirfConventionsMap;
	private static Map<String, String> mopsrMap;
	private static Map<String, String> mopsrBpMap;
	private static Map<String, String> mopsrBpCornerturnMap;
	private static Map<String, String> mopsrBsMap;
	
	
	private static Map<String, Pair<Integer, Integer > > beamBoundariesMap = new HashMap<>();
	
	private static Integer numFanBeams;
	private static String  edgeNode;
	private static String  edgeBS;


	private static Map<String, List<Integer>> activeBSForNodes = new HashMap<>();
	
	static{
		try {
			loadConfigs();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void loadConfigs() throws IOException{
		
		smirfMap = readConfig(SMIRFConstants.smirfConfig);
		smirfConventionsMap = readConfig(SMIRFConstants.smirfConventionsConfig);
		
		mopsrMap = readConfig( Utilities.createDirectoryStructure(smirfMap.get("CONFIG_ROOT"),smirfMap.get("MOPSR_CONFIG")));
		mopsrBpMap = readConfig(Utilities.createDirectoryStructure(smirfMap.get("CONFIG_ROOT"),smirfMap.get("MOPSR_BP_CONFIG")));
		mopsrBpCornerturnMap = readConfig(Utilities.createDirectoryStructure(smirfMap.get("CONFIG_ROOT"),smirfMap.get("MOPSR_BP_CORNERTURN_CONFIG")));
		mopsrBsMap = readConfig(Utilities.createDirectoryStructure(smirfMap.get("CONFIG_ROOT"),smirfMap.get("MOPSR_BS_CONFIG")));
				
		edgeNode = smirfMap.get("EDGE_NODE");
		edgeBS = smirfMap.get("EDGE_BS");
		
		
		
		Integer nepenthesBasePort = Integer.parseInt(mopsrMap.get("SMIRF_NEPENTHES_SERVER"));
		
		Integer numBS = Integer.parseInt(mopsrBsMap.get("NUM_BS"));
		
		// node, bs, port
		 Map<String, Map<Integer, Integer>> nepenthesServers = new HashMap<>();
		
		for(int bs=0; bs< numBS; bs++){
			
			String nodeName = mopsrBsMap.get( String.format("BS_%d", bs) );
			
			if(mopsrBsMap.get(String.format("BS_STATE_%d", bs)).equals("active")){
								
				Map<Integer, Integer> map = nepenthesServers.getOrDefault(nodeName, new HashMap<>());
				map.put(bs,nepenthesBasePort + bs );
				
				nepenthesServers.put(nodeName, map);
				
				List<Integer> bsList = activeBSForNodes.getOrDefault(nodeName, new ArrayList<>());
				bsList.add(bs);
				activeBSForNodes.put(nodeName, bsList);

			}
			
		}
						
		ConfigManager.nepenthesServers =  Collections.unmodifiableMap(nepenthesServers);
		
		
		numFanBeams = Integer.parseInt(mopsrBpCornerturnMap.get("NBEAM"));
		
		Integer numBP = Integer.parseInt(mopsrBpMap.get("NUM_BP"));
		
		for(String node: activeBSForNodes.keySet()){
			
			if(node.equals(edgeNode)) 	{
				beamBoundariesMap.put(node, new Pair<Integer, Integer>( 1 , numFanBeams));
				continue;
			}
			
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
		return (name == null) ? null : Integer.parseInt(name.replaceAll("\\D+", ""));
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

	

	public static Map<String, Map<Integer, Integer>> getNepenthesServers() {
		return nepenthesServers;
	}

	public static Map<String, String> getMopsrBsMap() {
		return mopsrBsMap;
	}

	public static Map<String, List<Integer>> getActiveBSForNodes() {
		return activeBSForNodes;
	}

	public static Map<String, String> getSmirfConventionsMap() {
		return smirfConventionsMap;
	}
	

}
