package util;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public interface BackendConstants {

	String backendIP = (SMIRFConstants.simulate)? "127.0.0.1":"172.17.228.204";
	int   backendPort = 38010;
	String query = "query";
	String prepare = "prepare";
	String start = "start";
	String stop = "stop";
	String bootUp = "startMopsr();";
	String shutDown = "stopMopsr();";
	int maxIter = 5;
	Integer maximumNumberOfTB = 4;

	
	String backendResponseSuccess = "ok";
	String backendIdle = "Idle";
	String backendPrepared = "parsed correctly";
	
	String loadBackendScript="/home/dada/scripts/load_config.csh";
	String psrBackend= "live_320chan_8ant_44pfb_352_beams";//"live_bfp_40chan_16ant_22pfb_352_beams";
	String corrBackend= "live_320chan_8ant_44pfb_352_beams";//"live_corr_40chan_16ant_22pfb"; 
	String otherBackends = "other";
	String dadaAtLocalhost = "dada@localhost";
	
	String tiedArrayFanBeam = "TIED_ARRAY_FAN_BEAM";
	String fanBeam = "FAN_BEAM";
	String correlation = "CORRELATION";
	
	String resultsDir = "/data/mopsr/results/";
	String calibrateScript = "python /home/observer/bin/calib_wrk_superbays.py -b 1280 -n 352 -c cc.sum -antf obs.antenna -plot no -tsamp 0.032 -nchan 40";
	String updateDelays = "/home/dada/linux_64/share/update_delays_snr.csh calib.delays";
	
	
	
	
	/* for simple date format */
	String backendUTCFormat = "yyyy-MM-dd-HH:mm:ss.SSS";
	String backendUTCFormatOfPattern="yyyy-MM-dd-kk:mm:ss.SSS";
	
	
	
	
	
	Map<String, Integer> bfNodeNepenthesServers = Utilities.populateNepenthesServers();
	
	
	
	/* xml message constants*/
	
	String invalidInstance = "Invalid instance. This instance can only check the status."
			+ "It cannot start or stop backend. Please use createBackendInstance() to manipulate the backend. ";
	
	
	
	String command = "<command>${command}</command>";
	
	String westArmParams = 
			"<west_arm_parameters>" + 
					"<tracking>${west_tracking}</tracking>" +
					"<ns_tilt units='${west_ns_tilt_units}'> ${west_ns_tilt}</ns_tilt>" +
					"<md_angle units='${west_md_angle_units}'>${west_md_angle}</md_angle>" +
			"</west_arm_parameters>";
	
	String eastArmParams =
			"<east_arm_parameters>" + 
					"<tracking>${east_tracking}</tracking>" +
					"<ns_tilt units='${east_ns_tilt_units}'> ${east_ns_tilt}</ns_tilt>" +
					"<md_angle units='${east_md_angle_units}'>${east_md_angle}</md_angle>" +
			"</east_arm_parameters>";
	
	String pfbParams =
			"<pfb_parameters>" +
					"<oversampling_ratio>${oversampling_ratio}</oversampling_ratio>" +
					"<sampling_time units='${tsamp_units}'>${tsamp}</sampling_time>" +
					"<channel_bandwidth units='${foff_units}'>${foff}</channel_bandwidth>" +
					"<dual_sideband>${dual_sideband}</dual_sideband>" +
					"<resolution units='${resolution_units}'>${resolution}</resolution>" +
			"</pfb_parameters>";
	
	String signalParams = 
			"<signal_parameters>" +
					"<nchan>${nchan}</nchan>" +
					"<nbit>${nbits}</nbit>" +
					"<ndim>${ndim}</ndim>" +
					"<npol>${npol}</npol>" +
					"<nant>${nant}</nant>" +
					"<bandwidth units='${bw_units}'>${bw}</bandwidth>" +
					"<centre_frequency units='${cfreq_units}'>${cfreq}</centre_frequency>" +
			"</signal_parameters>";
	
	String obsParams = 
			"<observation_parameters>" +
					"<observer>${observer}</observer>" +
					"<tobs>${tobs}</tobs>" +
			"</observation_parameters>";
	
		
	String boresightParams = 
			"<boresight_parameters>" +
				"<name epoch='${boresight_source_epoch}'>${boresight_source_name}</name>" +
				"<project_id>${boresight_project_id}</project_id>"+
				"<ra units='${boresight_ra_units}'>${boresight_ra}</ra>" +
				"<dec units='${boresight_dec_units}'>${boresight_dec}</dec>" +
				"<rfi_mitigation>${rfi_mitigation}</rfi_mitigation>" +
				"<antenna_weights>${antenna_weights}</antenna_weights>" +
				"<delay_tracking>${delay_tracking}</delay_tracking>" +
				"<processing_file>${boresight_proc_file}</processing_file>" +
			"</boresight_parameters>" ;
	
	
	String tb0Params =
			"<tied_beam_0_parameters>" +
				"<project_id>${tb0_project_id}</project_id>" +
				"<mode>${tb0_mode}</mode>" +
				"<processing_file>${tb0_proc_file}</processing_file>" +
				"<name epoch='${tb0_source_epoch}'>${tb0_source_name}</name>" +
				"<ra units='${tb0_ra_units}'>${tb0_ra}</ra>" +
				"<dec units='${tb0_dec_units}'>${tb0_dec}</dec>" +
				"${tb0DspsrParams}" +
			"</tied_beam_0_parameters>";
	
	String tb0DspsrParams = 
			"<optional_dspsr_parameters>" +
					"<dm units='${tb0_dm_units}'>${tb0_dm}</dm>" +
					"<period units='${tb0_period_units}'>${tb0_period}</period>" +
					"<acceleration units='${tb0_acc_units}'>${tb0_acc}</acceleration>" +
				"</optional_dspsr_parameters>";
	
	String tb1Params =
			"<tied_beam_1_parameters>" +
				"<project_id>${tb1_project_id}</project_id>" +
				"<mode>${tb1_mode}</mode>" +
				"<processing_file>${tb1_proc_file}</processing_file>" +
				"<name epoch='${tb1_source_epoch}'>${tb1_source_name}</name>" +
				"<ra units='${tb1_ra_units}'>${tb1_ra}</ra>" +
				"<dec units='${tb1_dec_units}'>${tb1_dec}</dec>" +
				"${tb1DspsrParams}" +
			"</tied_beam_1_parameters>";
	
	String tb1DspsrParams = 
			"<optional_dspsr_parameters>" +
				"<dm units='${tb1_dm_units}'>${tb1_dm}</dm>" +
				"<period units='${tb1_period_units}'>${tb1_period}</period>" +
				"<acceleration units='${tb1_acc_units}'>${tb1_acc}</acceleration>" +
			"</optional_dspsr_parameters>";
	
	String tb2Params =
			"<tied_beam_2_parameters>" +
				"<project_id>${tb2_project_id}</project_id>" +
				"<mode>${tb2_mode}</mode>" +
				"<processing_file>${tb2_proc_file}</processing_file>" +
				"<name epoch='${tb2_source_epoch}'>${tb2_source_name}</name>" +
				"<ra units='${tb2_ra_units}'>${tb2_ra}</ra>" +
				"<dec units='${tb2_dec_units}'>${tb2_dec}</dec>" +
				"${tb2DspsrParams}" +
			"</tied_beam_2_parameters>";
	
	String tb2DspsrParams = 
			"<optional_dspsr_parameters>" +
				"<dm units='${tb2_dm_units}'>${tb2_dm}</dm>" +
				"<period units='${tb2_period_units}'>${tb2_period}</period>" +
				"<acceleration units='${tb2_acc_units}'>${tb2_acc}</acceleration>" +
			"</optional_dspsr_parameters>";
	
	String tb3Params =
			"<tied_beam_3_parameters>" +
				"<project_id>${tb3_project_id}</project_id>" +
				"<mode>${tb3_mode}</mode>" +
				"<processing_file>${tb3_proc_file}</processing_file>" +
				"<name epoch='${tb3_source_epoch}'>${tb3_source_name}</name>" +
				"<ra units='${tb3_ra_units}'>${tb3_ra}</ra>" +
				"<dec units='${tb3_dec_units}'>${tb3_dec}</dec>" +
				"${tb3DspsrParams}" +
			"</tied_beam_3_parameters>";
	
	String tb3DspsrParams = 
			"<optional_dspsr_parameters>" +
				"<dm units='${tb3_dm_units}'>${tb3_dm}</dm>" +
				"<period units='${tb3_period_units}'>${tb3_period}</period>" +
				"<acceleration units='${tb3_acc_units}'>${tb3_acc}</acceleration>" +
			"</optional_dspsr_parameters>";
	
	String[] tbParams = {tb0Params,tb1Params,tb2Params,tb3Params};
	String[] dspsrParams = {tb0DspsrParams,tb1DspsrParams,tb2DspsrParams,tb3DspsrParams};

	
	String corrParams = 
			"<correlation_parameters>" +
				"<project_id>${corr_project_id}</project_id>" +
				"<type>${corr_type}</type>" +
				"<processing_file>${corr_proc_file}</processing_file>" +
				"<dump_time units='${corr_dump_time_units}'>${corr_dump_time}</dump_time>" +
			"</correlation_parameters>";

	String fabBeamParams = "<fan_beams_parameters>" +
			"<project_id>${fb_project_id}</project_id>" +
			"<mode>${fb_mode}</mode>" +
			"<nbeams>${fb_nbeams}</nbeams>" +
			"<beam_spacing units='${fb_spacing_units}'>${fb_spacing}</beam_spacing>" +
			"</fan_beams_parameters>";
	
	
	
	String messageWrapper ="<?xml version='1.0' encoding='ISO-8859-1'?>" +
			"<mpsr_tmc_message>"                                          +                
				"<command>${command}</command>${parameters}"				  +
			"</mpsr_tmc_message>";
	Map<String, String> defaultParams = new HashMap<String, String>();

	@Deprecated
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
	
	
}
