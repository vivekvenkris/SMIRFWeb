package manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import bean.Furby;
import util.ConfigManager;

public class FurbyManager {
	
	
	public static List<Furby> getFurbies() {
		
		String file = ConfigManager.getSmirfMap().get("FURBY_FILE");
		
		
		File furbyCatalogFile = new File(file);
		
		if(!furbyCatalogFile.exists()) {
			
			System.err.println("No FURBY catalog found at: " + furbyCatalogFile.getAbsolutePath());
			return null;
			
		}
		
		List<String> lines;
		try {
			lines = Files.readAllLines(furbyCatalogFile.toPath());
			
			System.err.println(lines.size());
			List<Furby> furbies = new ArrayList<>();
			
			for(String line : lines){
				
				if(line.contains("#")) continue;
				
				String[] chunks = line.trim().split("\\s+");
				
				if(chunks.length == 0 ) continue;
			
				
				String furbyID = chunks[0];
				
				File furbyFile = new File(furbyCatalogFile.getParentFile(),"furby_"+furbyID);
				
				if(!furbyFile.exists())	continue;
				
				furbies.add(new Furby(furbyID));
			
				
				
			}
			return furbies;

		} catch (IOException e) {
			System.err.println("VVK is an idiot.");
			e.printStackTrace();
			return null;
		}
		
		
		
	}

}
