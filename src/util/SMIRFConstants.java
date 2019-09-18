package util;

public interface SMIRFConstants {

	//Utilities.simulate();
	
	String smirfConfigRoot = "/home/vivek/SMIRF/config/";
	String smirfConfig = smirfConfigRoot + "smirf.cfg";
	String smirfConventionsConfig = smirfConfigRoot+ "smirf_conventions.cfg";

	double minRadMD = Double.parseDouble(ConfigManager.getSmirfMap().get("MIN_MD")) * Constants.deg2Rad;
	double maxRadMD = Double.parseDouble(ConfigManager.getSmirfMap().get("MAX_MD")) * Constants.deg2Rad;

	double minRadHA = Double.parseDouble(ConfigManager.getSmirfMap().get("MIN_HA")) * Constants.hrs2Rad;
	double maxRadHA = Double.parseDouble(ConfigManager.getSmirfMap().get("MAX_HA")) * Constants.hrs2Rad;

	double minRadNS = Double.parseDouble(ConfigManager.getSmirfMap().get("MIN_NS")) * Constants.deg2Rad;
	double maxRadNS = Double.parseDouble(ConfigManager.getSmirfMap().get("MAX_NS")) * Constants.deg2Rad;

	double minGalacticLongitude = Double.parseDouble(ConfigManager.getSmirfMap().get("MIN_GL")) * Constants.deg2Rad;
	double maxGalacticLongitude = Double.parseDouble(ConfigManager.getSmirfMap().get("MAX_GL")) * Constants.deg2Rad;

	double minGalacticLatitude = Double.parseDouble(ConfigManager.getSmirfMap().get("MIN_GB")) * Constants.deg2Rad;
	double maxGalacticLatitude = Double.parseDouble(ConfigManager.getSmirfMap().get("MAX_GB")) * Constants.deg2Rad;

	double tilingDiameter = Double.parseDouble(ConfigManager.getSmirfMap().get("TILING_DIAMETER")) * Constants.deg2Rad;
	double tilingRadius = tilingDiameter/2.0;
	
	Integer maxFRBTrtansitTOBS = Integer.parseInt(ConfigManager.getSmirfMap().get("MAX_FRB_TRANSIT_TOBS"));


	/* unique stitching constants */

	int BF08 = ConfigManager.getServerNumberForServerName( ConfigManager.getEdgeNode() );
	double thresholdPercent = Double.parseDouble(ConfigManager.getSmirfMap().get("STITCH_SPILLOVER_THRESHOLD"));

	String PID = ConfigManager.getSmirfMap().get("SMIRF_PID");
	String interimFRBTransitPID = ConfigManager.getSmirfMap().get("INTERIM_FRB_TRANSIT_PID");
	String nonSMIRFPointingPID = ConfigManager.getSmirfMap().get("NON_SMIRF_POINTING_PID");
	String frbFollowUpPID = ConfigManager.getSmirfMap().get("FRB_FOLLOW_UP_PID");
	String frbTransitPID = ConfigManager.getSmirfMap().get("FRB_TRANSIT_PID");


	int tobs = Integer.parseInt(ConfigManager.getSmirfMap().get("SURVEY_TOBS"));
	int phaseCalibrationTobs = Integer.parseInt(ConfigManager.getSmirfMap().get("PHASE_CAL_TOBS"));
	int fluxCalibrationTobs = Integer.parseInt(ConfigManager.getSmirfMap().get("FLUX_CAL_TOBS"));
	


	Integer highestPriority=1;
	Integer lowestPriority=100;

	String phaseCalibratorSymbol   = ConfigManager.getSmirfConventionsMap().get("PHASE_CAL_SYMBOL");
	String fluxCalibratorSymbol    = ConfigManager.getSmirfConventionsMap().get("FLUX_CAL_SYMBOL");
	String smcPointingSymbol       = ConfigManager.getSmirfConventionsMap().get("SMC_POINTING_SYMBOL");
	String lmcPointingSymbol       = ConfigManager.getSmirfConventionsMap().get("LMC_POINTING_SYMBOL");
	String galacticPointingSymbol  = ConfigManager.getSmirfConventionsMap().get("GALACTIC_POINTING_SYMBOL");
	String candidatePointingSymbol = ConfigManager.getSmirfConventionsMap().get("CANDIDATE_POINTING_SYMBOL");
	String randomPointingSymbol    = ConfigManager.getSmirfConventionsMap().get("USER_POINTING_SYMBOL");
	String transitPointingSymbol    = ConfigManager.getSmirfConventionsMap().get("INTERIM_TRANSIT_SYMBOL");
	String pulsarPointingSymbol  = ConfigManager.getSmirfConventionsMap().get("PSR_POINTING_SYMBOL");
	String frbFieldPointingSymbol  = ConfigManager.getSmirfConventionsMap().get("FRB_FIELD_POINTING_SYMBOL");
	String SMIRFPointingSymbols = smcPointingSymbol + lmcPointingSymbol + galacticPointingSymbol + candidatePointingSymbol;

	String SMIRFPointingPrefix = ConfigManager.getSmirfConventionsMap().get("SMIRF_POINTING_PREEFIX");
	String fluxCalPointingPrefix = ConfigManager.getSmirfConventionsMap().get("FLUX_CAL_POINTING_PREFIX");
	String phaseCalPointingPrefix = ConfigManager.getSmirfConventionsMap().get("PHASE_CAL_POINTING_PREFIX");
	String pulsarPointingPrefix = ConfigManager.getSmirfConventionsMap().get("PSR_POINTING_PREEFIX");




	/* JDBC & JPA constants */

	String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	String DB_URL = "jdbc:mysql://172.17.228.204/observations";

	String USER = "vivek";
	String PASS = "4&.S1kz5";
	
	String dynamicTransitScheduler = "DT";
	String staticTransitScheduler = "StT";
	String candidateConfirmationTransitScheduler = "CcT";
	String pulsarDynamicTransitScheduler = "PDT";
	String singleTransitScheduler = "SiT";
	String interfacedDynamicScheduler = "IDT";
	
	
	String levelInfo = "I";
	String levelFatal = "F";
	String levelDebug = "D";
	String levelWarn = "W";
	String levelCandidate = "C";




}
