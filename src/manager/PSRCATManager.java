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
	
	public static  List<TBSourceTO> loadDB(){
		for(String psrcatDB: psrcatDBs){
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(psrcatDB));
				String line = "";
				TBSourceTO tbSourceTO = null;
				while((line=br.readLine())!=null){

					String name = line.substring(0, 8).trim();
					String value = line.substring(9, 34).trim();
					if(name.equals(PSRJ)) {
						if(tbSourceTO != null) tbSources.add(tbSourceTO);
						tbSourceTO = new TBSourceTO();
						tbSourceTO.setPsrName(value);
					}
					else if(name.contains(RAJ))  tbSourceTO.setAngleRA(new Angle(value, Angle.HHMMSS));
					else if(name.contains(DECJ))  tbSourceTO.setAngleRA(new Angle(value, Angle.DDMMSS));
					
					

					tbSourceTO.addToEphemerides(line);


				}
			} catch ( IOException e) {
				e.printStackTrace();
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
	
}
