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

import exceptions.InvalidFanBeamNumberException;


public class ConfigManager {
	
	public static void main(String[] args) throws IOException {
		loadConfigs();
		
		System.err.println(nodeBsBpFbMap);
		
	}
	
	private static  Map<String, Map<Integer, Integer>>  nepenthesServers;
	private static Map<String, String> smirfMap;
	private static Map<String, String> smirfConventionsMap;
	private static Map<String, String> mopsrMap;
	private static Map<String, String> mopsrBpMap;
	private static Map<String, String> mopsrBpCornerturnMap;
	private static Map<String, String> mopsrBsMap;
	
	//node, bs, start FB, end FB
	private static Map<String, Map<Integer, Pair<Integer, Integer > > > beamBoundariesMap = new HashMap<>();
	// node bs bp start FB end FB
	private static Map<String, Map<Integer, Map<Integer, Pair<Integer, Integer > > > > nodeBsBpFbMap = new HashMap<>();
	
	
	private static Integer numFanBeams;
	private static String  edgeNode;
	private static Integer  edgeBS;


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
		edgeBS = Integer.parseInt(smirfMap.get("EDGE_BS"));
		
		
		
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
		
		for(Entry<String, List<Integer>> entry: activeBSForNodes.entrySet()){
			String node = entry.getKey();
			for(Integer bs: entry.getValue()){
				
				List<Integer> beamProcessors = new ArrayList<>();
				
				if(node.equals(edgeNode)) 	{
					
					Map<Integer, Pair<Integer, Integer>> map = beamBoundariesMap.getOrDefault(node, new HashMap<>());
					map.put(bs, new Pair<Integer, Integer>( 1 , numFanBeams));
					beamBoundariesMap.put(node, map);	
					
					Map<Integer, Map<Integer, Pair<Integer, Integer>>> bsBpFbMap = nodeBsBpFbMap.getOrDefault(node, new HashMap<>() );
					Map<Integer, Pair<Integer, Integer>> bpFbMap = bsBpFbMap.getOrDefault(bs, new HashMap<>());
					for(int bp=0; bp< numBP; bp++)	bpFbMap.put(bp, new Pair<Integer, Integer>( 1 , numFanBeams));
					bsBpFbMap.put(bs, bpFbMap);
					nodeBsBpFbMap.put(node, bsBpFbMap);

					
					continue;
					
				}
				
				for( int bp=0 ; bp< numBP; bp++ ) if( mopsrBpMap.get("BP_" + bp).equals(node) ) beamProcessors.add(bp);
				Integer minFB = null, maxFB = null;
				
				Map<Integer, Map<Integer, Pair<Integer, Integer>>> bsBpFbMap = nodeBsBpFbMap.getOrDefault(node, new HashMap<>() );
				Map<Integer, Pair<Integer, Integer>> bpFbMap = bsBpFbMap.getOrDefault(bs, new HashMap<>());
				
				for(Integer bp: beamProcessors) {
					
					Integer min = Integer.parseInt(mopsrBpCornerturnMap.get("BEAM_FIRST_RECV_" + bp));
					Integer max = Integer.parseInt(mopsrBpCornerturnMap.get("BEAM_LAST_RECV_" + bp));
					
					
					if(minFB == null || minFB > min)  minFB = min;
					if(maxFB == null || maxFB < max)  maxFB = max;
					
					bpFbMap.put(bp, new Pair<Integer, Integer>( min+1, max + 1));
				}
				Map<Integer, Pair<Integer, Integer>> map = beamBoundariesMap.getOrDefault(node, new HashMap<>());
				map.put(bs, new Pair<Integer, Integer>( minFB + 1 ,  maxFB + 1));
				beamBoundariesMap.put(node, map);	
				
			
				bsBpFbMap.put(bs, bpFbMap);
				nodeBsBpFbMap.put(node, bsBpFbMap);

			}

		}
		
				
	}
	
	public static String getBeamSubDir(String utc, Integer fanbeam){
		String root = "";
		
		for(Entry<String, Map <Integer, Map < Integer , Pair<Integer, Integer>>>> e: nodeBsBpFbMap.entrySet()){
			
			for(Entry<Integer, Map < Integer , Pair<Integer, Integer>>> e2: e.getValue().entrySet()){
				
				if(e2.getKey().equals(edgeBS)) continue;
				
				for(Entry<Integer , Pair<Integer, Integer>> e3: e2.getValue().entrySet()){
					
					if(fanbeam >= e3.getValue().getValue0() && fanbeam <= e3.getValue().getValue1() ){
						
						 
						root =	ConfigManager.getSmirfMap().get("BEAM_PROCESSOR_PREFIX")
									 + String.format("%02d", e3.getKey()) + "/"
									 + utc + "/"
									 + ConfigManager.getSmirfMap().get("FB_DIR") + "/"
									 + ConfigManager.getSmirfMap().get("BEAM_DIR_PREFIX") 
									 + String.format("%03d", fanbeam);
						return root;
					}
				}
				
			}
		}
		
		
		return root;
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
				
				if(chunks.length != 2){
					System.err.println("Bad line in " + file +" =>'" + line+"' . Ignoring line");
					continue;
				}
				
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
	
	public static Integer getBeamSearcherForFB(int fb) throws InvalidFanBeamNumberException{

		for(Entry<String,Map<Integer, Pair<Integer, Integer>>> entry : beamBoundariesMap.entrySet()){
			
			if(entry.getKey().equals(edgeNode)) continue;

			for(Entry <Integer, Pair<Integer, Integer>> entry2: entry.getValue().entrySet()) {
				
				if(entry2.getKey().equals(edgeBS)) continue;


				if( (fb >= entry2.getValue().getValue0()) && ( fb <= entry2.getValue().getValue1() ) ) return entry2.getKey();

			}


		}
		throw new InvalidFanBeamNumberException(fb + "");

	}
	
	
//	public static String getServerForFB(int fb){
//		for(Entry<String, Pair<Integer, Integer>> entry : beamBoundariesMap.entrySet()) if( (fb >= entry.getValue().getValue0()) && ( fb <= entry.getValue().getValue1() ) ) return entry.getKey();
//		return null;
//		
//	}
//	
	
	
//	public static Integer getServerNumberForFB(int fb){
//		return  getServerNumberForServerName(getServerForFB(fb));
//	}
//	
	
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

	public static Map<String, Map<Integer, Pair<Integer, Integer>>> getBeamBoundariesMap() {
		return beamBoundariesMap;
	}

	public static Integer getEdgeBS() {
		return edgeBS;
	}

	
}
