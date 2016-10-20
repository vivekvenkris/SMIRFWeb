package util;

import java.util.HashMap;
import java.util.Map;

public interface TCCConstants {
	String tccControllerIP = "127.0.0.1";
	Integer   tccControllerPort = 38012;
	
	String tccStatusIP = "127.0.0.1";
	Integer   tccStatusPort = 38013;
	
	String EAST = "east";
	String WEST = "west";
	String NS = "ns";
	String MD = "md";
	
	String track = "point";
	String stop = "stop";
	
	Double slewRateMD = 2.5/60;
	Double slewRateNSFast = 4.8/60;
	Double slewRateNSSlow = 1.0/60.0;
	
	Double OnSourceThresholdRadNS = 0.005;
	Double OnSourceThresholdRadMD = 0.005;
	
	String messageWrapper = "<?xml version='1.0' encoding='ISO-8859-1'?>"
							+ "<tcc_request>"
							+ "<user_info>"
								+ "<name>SMIRFWeb</name>"
								+ "<comment/>"
							+ "</user_info>"
							+ "<tcc_command>"
								+ "<command>${command}</command>"
								+ "${parameters}"
						+ "</tcc_command>"
					+ "</tcc_request>";
	
	String pointParameters = "<pointing  units='${units}' tracking='${tracking}' system='${system}' epoch='${epoch}'>"
			+ "<xcoord>${xcoord}</xcoord>"
			+ "<ycoord>${ycoord}</ycoord>"
		+ "</pointing>"
		+ "<ns>"
			+ "<east>"
				+ "<state>${ns_east_state}</state>"
				+ "<offset units='${ns_east_offset_units}'>${ns_east_offset}</offset>"
			+ "</east>"
			+ "<west>"
				+ "<state>${ns_west_state}</state>"
				+ "<offset units='${ns_west_offset_units}'>${ns_west_offset}</offset>"
			+ "</west>"
		+ "</ns>"
		+ "<md>"
		+ "<east>"
			+ "<state>${md_east_state}</state>"
			+ "<offset units='${md_east_offset_units}'>${md_east_offset}</offset>"
		+ "</east>"
		+ "<west>"
			+ "<state>${md_west_state}</state>"
			+ "<offset units='${md_west_offset_units}'>${md_west_offset}</offset>"
		+ "</west>"
	+ "</md>";	
	
	String pingCommand = "<?xml version=1.0 encoding=ISO-8859-1?>"
			+ "<tcc_request>"
			+ "<user_info>"
				+ "<name>Automatic_Mode</name>"
				+ "<comment></comment>"
			+ "</user_info>"
			+ "<server_command>"
				+ "<command>ping</command>"
		+ "</server_command>"
	+ "</tcc_request>";
	
	String statusTemplate = "<?xml version='1.0' encoding='ISO-8859-1'?> "
			+" <tcc_status> "
			+"   <overview> "
			+"     <error_string>${error}</error_string> "
			+"   </overview> "
			+"   <coordinates> "
			+"     <RA>${ra}</RA> "
			+"     <Dec>${dec}</Dec> "
			+"     <HA>${ha}/HA> "
			+"     <Glat>${glat}</Glat> "
			+"     <Glon>${glon}</Glon> "
			+"     <Alt>${alt}</Alt> "
			+"     <Az>${az}</Az> "
			+"     <NS>${ns}</NS> "
			+"     <EW>${ew}</EW> "
			+"     <LMST>${lmst}</LMST> "
			+"   </coordinates> "
			+"   <ns> "
			+"     <error>${ns_error}</error> "
			+"     <east> "
			+"       <tilt>${ns_east_tilt}</tilt> "
			+"       <count>${ns_east_count}</count> "
			+"       <driving>${ns_east_driving}</driving> "
			+"       <state>${ns_east_state}</state> "
			+"       <on_target>${ns_east_ontarget}</on_target> "
			+"       <system_status>${ns_east_status}</system_status> "
			+"     </east> "
			+"     <west> "
			+"       <tilt>${ns_west_tilt}</tilt> "
			+"       <count>${ns_west_count}</count> "
			+"       <driving>${ns_west_driving}</driving> "
			+"       <state>${ns_west_state}</state> "
			+"       <on_target>${ns_west_ontarget}</on_target> "
			+"       <system_status>${ns_west_status}</system_status> "
			+"     </west> "
			+"   </ns> "
			+"   <md> "
			+"      <error>${md_error}</error> "
			+"     <east> "
			+"       <tilt>${md_east_tilt}</tilt> "
			+"       <count>${md_east_count}</count> "
			+"       <driving>${md_east_driving}</driving> "
			+"       <state>${md_east_state}</state> "
			+"       <on_target>${md_east_ontarget}</on_target> "
			+"       <system_status>${md_east_status}</system_status> "
			+"     </east> "
			+"     <west> "
			+"       <tilt>${md_west_tilt}</tilt> "
			+"       <count>${md_west_count}</count> "
			+"       <driving>${md_west_driving}</driving> "
			+"       <state>${md_west_state}</state> "
			+"       <on_target>${md_west_ontarget}</on_target> "
			+"       <system_status>${md_west_status}</system_status> "
			+"     </west> "
			+"   </md> "
			+" </tcc_status> ";
	
	String responseTemplate = "<tcc_reply>"
  +"<success>TCC command passed</success>"
+"</tcc_reply>";
	
	Map<String, String> defaultParams = new HashMap<String, String>();


}

