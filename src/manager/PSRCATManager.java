package manager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bean.Angle;
import bean.Observation;
import bean.TBSourceTO;
import util.PSRCATConstants;

public class PSRCATManager implements PSRCATConstants{
	private static List<TBSourceTO> tbSources = new ArrayList<>();
	static{
		loadDB();
	}
	
	public static TBSourceTO getTBSouceByName(String jname){
		int index = tbSources.indexOf(new TBSourceTO(jname));
		return index == -1 ? null : tbSources.get(index);
	}
	
	public static  List<TBSourceTO> loadDB(){
		for(String psrcatDB: psrcatDBs){
			BufferedReader br = null;
			int count = 0;
			try {
				br = new BufferedReader(new FileReader(psrcatDB));
				String line = "";
				TBSourceTO tbSourceTO = null;
				count++;
				while((line=br.readLine())!=null){
					if(line.contains("#") || line.contains("@") || line.equals("")) continue;
					String name = line.substring(0, endOfName).trim();
					String value = line.substring(endOfName + 1, endofValue > line.length()? line.length() : endofValue ).trim();
					if(name.equals(PSRJ)) {
						if(tbSourceTO != null 
								&& tbSourceTO.getAngleRA() !=null && tbSourceTO.getAngleDEC() !=null){
								tbSources.add(tbSourceTO);
						}
						tbSourceTO = new TBSourceTO();
						tbSourceTO.setPsrName(value);
					}
					else if(name.equals(RAJ))  tbSourceTO.setAngleRA(new Angle(value, Angle.HHMMSS));
					else if(name.equals(DECJ))  tbSourceTO.setAngleDEC(new Angle(value, Angle.DDMMSS));
					
					
					tbSourceTO.addToEphemerides(line);


				}
			} catch ( IOException e) {
				e.printStackTrace();
				System.err.println("line:" + count);
			}
			finally {
				if(br!=null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return tbSources;
	}
	
	public static List<TBSourceTO> refresh(){
		loadDB();
		return tbSources;
	}

	public static List<TBSourceTO> getTbSources() {
		return tbSources;
	}

	public static void setTbSources(List<TBSourceTO> tbSources) {
		PSRCATManager.tbSources = tbSources;
	}
	
	public static void main(String[] args) {
		System.err.println(tbSources.size());
	}
	
}
