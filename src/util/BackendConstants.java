package util;

import java.util.HashMap;
import java.util.Map;

public interface BackendConstants {
	
	String backendIP = "172.17.228.204";
	int   backendPort = 38010;
	String query = "query";
	String prepare = "prepare";
	String start = "start";
	String stop = "stop";
	String bootUp = "startMopsr();";
	String shutDown = "stopMopsr();";
	int maxIter = 5;
	
	String backendResponseSuccess = "ok";
	String backendIdle = "Idle";
	String backendPrepared = "parsed correctly";
	
	String loadBackendScript="/home/dada/scripts/load_config.csh";
	String psrBackend= "live_bfp_40chan_16ant_22pfb_352_beams";
	String corrBackend= "live_corr_40chan_16ant_22pfb"; 
	String otherBackends = "other";
	String dadaAtLocalhost = "dada@localhost";
	
	String tiedArrayFanBeam = "TIED_ARRAY_FAN_BEAM";
	String fanBeam = "FAN_BEAM";
	String correlation = "CORRELATION";
	
	String resultsDir = "/data/mopsr/results/";
	String calibrateScript = "python /home/observer/bin/calib_wrk_superbays.py -b 1280 -n 352 -c cc.sum -antf obs.antenna -plot no -tsamp 0.032 -nchan 40";
	String updateDelays = "/home/dada/linux_64/share/update_delays_snr.csh calib.delays";
	
	
	String invalidInstance = "Invalid instance. This instance can only check the status."
			+ "It cannot start or stop backend. Please use createBackendInstance() to manipulate the backend. ";
	
	String messageWrapper = "<?xml version='1.0' encoding='ISO-8859-1'?>" +
			"<mpsr_tmc_message>"                                          +                
				"<command>${command}</command>${parameters}"				  +
			"</mpsr_tmc_message>";
	String paramTemplate = "<source_parameters>" +
								"<name epoch='${epoch}'>${source_name}</name>" +
								"<ra units='${ra_units}'>${ra}</ra>" +
								"<dec units='${dec_units}'>${dec}</dec>" +
								"<ns_tilt units='${ns_tilt_units}'>${ns_tilt}</ns_tilt>" +
								"<md_angle units='${md_angle_units}'>${md_angle}</md_angle>" +
							"</source_parameters>" +
							"<signal_parameters>" +
								"<nchan>${nchan}</nchan>" +
								"<nbit>${nbits}</nbit>" +
								"<ndim>${ndim}</ndim>" +
								"<npol>${npol}</npol>" +
								"<nant>${nant}</nant>" +
								"<bandwidth units='${bw_units}'>${bw}</bandwidth>" +
								"<centre_frequency units='${cfreq_units}'>${cfreq}</centre_frequency>" +
							"</signal_parameters>" +
							"<pfb_parameters>" +
								"<oversampling_ratio>${oversampling_ratio}</oversampling_ratio>" +
								"<sampling_time units='${tsamp_units}'>${tsamp}</sampling_time>" +
								"<channel_bandwidth units='${foff_units}'>${foff}</channel_bandwidth>" +
								"<dual_sideband>${dual_sideband}</dual_sideband>" +
								"<resolution units='${resolution_units}'>${resolution}</resolution>" +
							"</pfb_parameters>" +
							"<observation_parameters>" +
								"<observer>${observer}</observer>" +
								"<aq_processing_file>${aq_proc_file}</aq_processing_file>" +
								"<bf_processing_file>${bf_proc_file}</bf_processing_file>" +
								"<bp_processing_file>${bp_proc_file}</bp_processing_file>" +
								"<mode>${mode}</mode>" +
								"<project_id>${project_id}</project_id>" +
								"<tobs>${tobs}</tobs>" +
								"<type>${obs_type}</type>" +
								"<config>${config}</config>" +
							"</observation_parameters>" ;
	
	Map<String, String> defaultParams = new HashMap<String, String>();
}
