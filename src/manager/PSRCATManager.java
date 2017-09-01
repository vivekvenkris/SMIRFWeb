package manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import bean.Angle;
import bean.ObservationTO;
import bean.TBSourceTO;
import util.ConfigManager;
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
					else if(name.equals(F0)) tbSourceTO.setF0(Double.parseDouble(value));
					else if(name.equals(P0)) tbSourceTO.setP0(Double.parseDouble(value));
					else if(name.equals(DM)) tbSourceTO.setDM(Double.parseDouble(value));
				
					
					
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
		
		String file = null;
		if(( file = ConfigManager.getSmirfMap().get("HIGH_PRIORITY_PULSAR_LIST")) !=null){
			
			BufferedReader br = null;
			try {
				br = new BufferedReader( new FileReader(new File(file)));
				String line = "";
				
				while( ( line = br.readLine())  !=null) {
					
					if(PSRCATManager.getTbSources().contains(new TBSourceTO(line))){
						//System.err.println("increasing priority of TB source: " + line);
						PSRCATManager.getTbSources().get(PSRCATManager.getTbSources().indexOf(new TBSourceTO(line))).setPriority(2);
					}
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				try {
					if(br!=null)
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
			
		}
		
		if(( file = ConfigManager.getSmirfMap().get("PULSAR_FLUX_LIST")) !=null){
			
			BufferedReader br = null;
			try {
				br = new BufferedReader( new FileReader(new File(file)));
				String line = "";
				
				while( ( line = br.readLine())  !=null) {
					
					String[] chunks = line.split(" ");
					
					if(chunks.length !=2) continue;
					
					String psrName = chunks[0].trim();
					
					
					Double flux = Double.parseDouble(chunks[1].trim());
					
					if(PSRCATManager.getTbSources().contains(new TBSourceTO(psrName))){
						if(PSRCATManager.getTbSources().contains(new TBSourceTO(psrName))) {
							PSRCATManager.getTbSources().get(PSRCATManager.getTbSources().indexOf(new TBSourceTO(psrName))).setFluxAt843MHz(flux);
						}
					}
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				try {
					if(br!=null)
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
		
		tbSources.stream().map(t-> (t.getPsrName() + " " + t.getFluxAt843MHz())).collect(Collectors.toList());
		
	}
	
}
