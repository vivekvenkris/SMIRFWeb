package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import bean.FluxCalibrator;
import bean.Observation;
import bean.ObservationSessionTO;
import bean.ObservationTO;
import bean.ObservingSession;
import bean.PhaseCalibrator;
import bean.Pointing;
import bean.PointingTO;
import bean.TBSourceTO;
import manager.DBManager;
import manager.PSRCATManager;
import util.SMIRFConstants;
import util.Utilities;

public class DBService implements SMIRFConstants {
	private static EntityManagerFactory emFactory = Persistence.createEntityManagerFactory("SMIRFWeb");

	public static void addPointingsToDB(List<Pointing> gridPoints){

		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction( ).begin( );
		for(Pointing pointing: gridPoints){
			entityManager.persist(pointing);
		}
		entityManager.getTransaction().commit();
		entityManager.close();
	}
	
	public static void deletePointingsFromDB(List<Pointing> gridPoints){

		for(Pointing pointing: gridPoints){
			deletePointingFromDB(pointing);
		}
	}
	
	public static void deletePointingFromDB(Pointing p){

		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction( ).begin( );
		if(!entityManager.contains(p)) {
			p = entityManager.merge(p);
		}
		entityManager.remove(p);
		entityManager.getTransaction().commit();
		entityManager.close();
	}




	public static void updatePointingsToDB(List<PointingTO> gridPoints){

		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction( ).begin( );
		for(PointingTO pointingTO: gridPoints){
			System.err.println(pointingTO +  " " + pointingTO.getPointingID());
			Pointing pointing = entityManager.find(Pointing.class, pointingTO.getPointingID());
			PointingTO.updatePointing(pointing, pointingTO);
			entityManager.persist(pointing);
		}
		entityManager.getTransaction().commit();
		entityManager.close();
	}



	public static Pointing getPointingByID(Integer pointingID){
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		Pointing pointing = entityManager.find(Pointing.class, pointingID);
		entityManager.getTransaction().commit();
		entityManager.close();
		return pointing;
	}


	public static Pointing getPointingByUniqueName(String pointingName){
		try {
			EntityManager entityManager = emFactory.createEntityManager( );
			TypedQuery<Pointing> query = entityManager.createQuery("FROM Pointing t where t.pointingName =?1",Pointing.class);
			Pointing result = query.setParameter(1, pointingName).getSingleResult();
			return result;
		}catch(NoResultException e) {
			e.printStackTrace();
			return null;
		}
	}


	public static FluxCalibrator getFluxCalByUniqueName(String name){
		EntityManager entityManager = emFactory.createEntityManager( );
		TypedQuery<FluxCalibrator> query = entityManager.createQuery("FROM FluxCalibrator t where t.sourceName =?1",FluxCalibrator.class);
		FluxCalibrator result = query.setParameter(1, name).getSingleResult();
		return result;
	}

	public static PhaseCalibrator getPhaseCalByUniqueName(String name){
		EntityManager entityManager = emFactory.createEntityManager( );
		TypedQuery<PhaseCalibrator> query = entityManager.createQuery("FROM PhaseCalibrator t where t.sourceName =?1",PhaseCalibrator.class);
		PhaseCalibrator result = query.setParameter(1, name).getSingleResult();
		return result;
	}

	public static List<Pointing> getAllPointings(){
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		@SuppressWarnings("unchecked")
		List<Pointing> pointings = entityManager.createQuery("SELECT p FROM Pointing p").getResultList();
		entityManager.getTransaction().commit();
		entityManager.close();
		return pointings;

	}


	public static List<Observation> getAllObservations(){
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		@SuppressWarnings("unchecked")
		List<Observation> Observations = entityManager.createQuery("SELECT p FROM Observation p").getResultList();
		entityManager.getTransaction().commit();
		entityManager.close();
		return Observations;

	}

	public static List<Observation> getShortlistedSMIRFObservations(Boolean complete, Integer managementStatus){
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		@SuppressWarnings("unchecked")
		TypedQuery<Observation> query = entityManager
		.createQuery(
				"SELECT p FROM Observation p where p.complete = ?1 "
						+ "and p.managementStatus = ?2 "
						+ "and p.sourceName LIKE '" + SMIRFConstants.SMIRFPointingPrefix + "%'",
						Observation.class);
		query.setParameter(1, complete);
		query.setParameter(2, managementStatus);
		List<Observation> observations = query.getResultList();

		entityManager.getTransaction().commit();
		entityManager.close();
		return observations;

	}


	public static void updateObservations(List<ObservationTO> observationTOs){

		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction( ).begin( );
		for(ObservationTO observation: observationTOs){
			Observation observationFromDB = entityManager.find(Observation.class, observation.getObservationID());
			Observation.updateObservtion(observation, observationFromDB);
			entityManager.persist(observation);
		}
		entityManager.getTransaction().commit();
		entityManager.close();
	}


	public static Double getDaysSinceObserved(String source_name) {

		EntityManager entityManager = emFactory.createEntityManager( );
		TypedQuery<Observation> query = entityManager.createQuery("FROM Observation t where t.sourceName =?1 ORDER BY t.utc DESC ",Observation.class);
		Observation t =  null;
		List<Observation> list  = query.setParameter(1, source_name).setMaxResults(1).getResultList();
		if(list != null && ! list.isEmpty()) t = list.get(0);
		else return null;

		Double difference = Utilities.getTimeDifferenceInDays(
				Utilities.getUTCLocalDateTime(new ObservationTO(t).getUtc()),
				Utilities.getUTCLocalDateTime(EphemService.getUtcStringNow())); 


		return difference; 
	}

	public static List<Pointing> getAllUnobservedPointingsOrderByPriority(){
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		@SuppressWarnings("unchecked")
		List<Object> minObs = entityManager.createQuery("select min(q.numObs) from Pointing q  ").getResultList();
		@SuppressWarnings("unchecked")
		List<Pointing> pointings = entityManager.createQuery("SELECT p FROM Pointing p where p.numObs= ( select min(q.numObs) from Pointing q ) order by p.priority DESC ").getResultList();
		entityManager.getTransaction().commit();
		entityManager.close();
		return pointings;

	}

	public static List<String> getAllPointingTypes(){
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		@SuppressWarnings("unchecked")
		List<String> uniqPointingTYpes = entityManager.createQuery("select DISTINCT(q.type) from Pointing q  ").getResultList();
		entityManager.getTransaction().commit();
		entityManager.close();
		return uniqPointingTYpes;
	}

	public static List<Pointing> getAllPointingsForPointingType(String pointingType){
		EntityManager entityManager = emFactory.createEntityManager( );
		TypedQuery<Pointing> query = entityManager.createQuery("FROM Pointing t where t.type =?1",Pointing.class);
		List<Pointing> result = query.setParameter(1, pointingType).getResultList();
		return result;
	}




	public static Pointing incrementPointingObservations(Integer pointingID){
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		Pointing pointing = entityManager.find(Pointing.class, pointingID);
		pointing.setNumObs(pointing.getNumObs() +1);
		entityManager.getTransaction().commit();
		entityManager.close();
		return pointing;
	}




	public static PhaseCalibrator getCalibratorByID(Integer calibratorID){
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		PhaseCalibrator calibrator = entityManager.find(PhaseCalibrator.class, calibratorID);
		entityManager.getTransaction().commit();
		entityManager.close();
		return calibrator;
	}



	public static PhaseCalibrator getPhaseCalibratorByName(String name){
		EntityManager entityManager = emFactory.createEntityManager( );
		TypedQuery<PhaseCalibrator> query = entityManager.createQuery("select t from PhaseCalibrator t where t.sourceName =?1",PhaseCalibrator.class);
		PhaseCalibrator result = query.setParameter(1, name).getSingleResult();
		return result;
	}

	public static FluxCalibrator getFluxCalibratorByName(String name){
		EntityManager entityManager = emFactory.createEntityManager( );
		TypedQuery<FluxCalibrator> query = entityManager.createQuery("select t from FluxCalibrator t where t.sourceName =?1",FluxCalibrator.class);
		FluxCalibrator result = query.setParameter(1, name).getSingleResult();
		return result;
	}


	public static List<PhaseCalibrator> getAllPhaseCalibrators(){
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		@SuppressWarnings("unchecked")
		List<PhaseCalibrator> phaseCalibrators = entityManager.createQuery("SELECT p FROM PhaseCalibrator p").getResultList();
		entityManager.getTransaction().commit();
		entityManager.close();
		return phaseCalibrators;

	}

	public static List<PhaseCalibrator> getAllPhaseCalibratorsOrderByFluxDesc(){
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		@SuppressWarnings("unchecked")
		List<PhaseCalibrator> phaseCalibrators = entityManager.createQuery("SELECT p FROM PhaseCalibrator p order by p.fluxJY DESC ").getResultList();
		entityManager.getTransaction().commit();
		entityManager.close();
		return phaseCalibrators;

	}


	public static List<FluxCalibrator> getAllFluxCalibrators(){
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		@SuppressWarnings("unchecked")
		List<FluxCalibrator> fluxCalibrators = entityManager.createQuery("SELECT p FROM FluxCalibrator p").getResultList();
		entityManager.getTransaction().commit();
		entityManager.close();
		return fluxCalibrators;

	}

	public static List<FluxCalibrator> getAllFluxCalibratorsOrderByDMDesc(){
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		@SuppressWarnings("unchecked")
		List<FluxCalibrator> fluxCalibrators = entityManager.createQuery("SELECT p FROM FluxCalibrator p order by p.dm desc").getResultList();
		entityManager.getTransaction().commit();
		entityManager.close();
		return fluxCalibrators;

	}



	public static void addObservationToDB(ObservationTO observationTO){

		Observation observation = new Observation(observationTO);
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		entityManager.persist(observation);
		//entityManager.persist(observation.getObservingSession());
		entityManager.getTransaction().commit();
		entityManager.close();
		observationTO.setObservationID(observation.getObservationID());

	}




	public static void addSessionToDB(ObservationSessionTO observationSessionTO){
		ObservingSession observationSession = new ObservingSession(observationSessionTO);
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		entityManager.persist(observationSession);
		entityManager.getTransaction().commit();
		entityManager.close();
		observationSessionTO.setSessionID(observationSession.getSessionID()); 
	}

	public static ObservationSessionTO getObservationSessionByID(Integer observationSessionTO){
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		ObservingSession observingSession = entityManager.find(ObservingSession.class, observationSessionTO);
		entityManager.getTransaction().commit();
		entityManager.close();
		return new ObservationSessionTO(observingSession);
	}



	public static void makeObservationComplete(ObservationTO observationTO){
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		Observation observation = entityManager.find(Observation.class, observationTO.getObservationID());
		observation.setComplete(true);
		entityManager.persist(observation);
		entityManager.getTransaction().commit();
		entityManager.close();
	}

	public static void incrementCompletedObservation(ObservationSessionTO observationSessionTO){
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		ObservingSession observingSession = entityManager.find(ObservingSession.class, observationSessionTO.getSessionID());
		observingSession.setNumPointingsDone(observingSession.getNumPointingsDone() + 1);
		entityManager.persist(observingSession);
		entityManager.getTransaction().commit();
		entityManager.close();
	}



	public static Observation getObservationByUTC(String utc){
		try {
			EntityManager entityManager = emFactory.createEntityManager( );
			TypedQuery<Observation> query = entityManager.createQuery("select t from Observation t where t.utc =?1",Observation.class);
			Observation result = query.setParameter(1, utc).getSingleResult();
			return result;
		}catch(NoResultException e) {
			e.printStackTrace();
			return null;
		}

	}



	public static void updatePulsarLastObservedTimes() {

		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS); 
			stmt = conn.createStatement();

			//String sql = "SELECT Pulsars.name, MAX(UTCs.utc) FROM TB_Obs LEFT JOIN UTCs ON (TB_Obs.utc_id = UTCs.id) LEFT JOIN Pulsars ON TB_Obs.psr_id = Pulsars.id  WHERE Pulsars.observe = 1 GROUP BY Pulsars.name";

			String sql = "SELECT Pulsars.name, MAX(UTCs.utc) FROM  Pulsars LEFT JOIN TB_Obs ON TB_Obs.psr_id = Pulsars.id LEFT JOIN UTCs ON (TB_Obs.utc_id = UTCs.id)   WHERE Pulsars.observe = 1 GROUP BY Pulsars.name";

			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {

				String name = rs.getString("name");
				String maxUTC = rs.getString("MAX(UTCs.utc)");

				TBSourceTO to = PSRCATManager.getTimingProgrammeSouceByName(name);

				if(to == null) {
					System.err.println("Last obs updater: Cannot find " + name  + " in observable list");
					continue;
				}

				if(maxUTC == null) {
					to.setLastObserved(null);
					to.setDaysSinceLastObserved(null);
				}
				else {
					LocalDateTime utcLastObserved = Utilities.getUTCLocalDateTime(maxUTC);
					double daysSinceLastObserved = Utilities.getTimeDifferenceInDays(utcLastObserved, EphemService.getUTCTimestamp());
					to.setLastObserved(utcLastObserved);

					to.setDaysSinceLastObserved(daysSinceLastObserved); 
				}

			}

			//System.err.println(" ** Last observed values updated  ** ");


		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		finally{
			try{
				if(stmt!=null) conn.close();
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}


	}

	public static Double getPulsarSNRForObs(String utc, String source_name) {

		if(utc.contains(".")) utc = utc.replaceAll(".000", "");

		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS); 
			stmt = conn.createStatement();

			String sql = "select t.snr from TB_Obs t "
					+ "inner join UTCs u on  u.id = t.utc_id "
					+ "inner join Pulsars p on p.id = t.psr_id "
					+ "where u.utc='" + utc + "' and p.name='"+source_name+"'";


			ResultSet rs = stmt.executeQuery(sql);


			while (rs.next()) {

				String snr = rs.getString("snr");

				System.err.println("snr=" + snr);

				return Double.parseDouble(snr);


			}

			return -1.0;

		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			return -1.0;
		}
		finally{
			try{
				if(stmt!=null) conn.close();
			}catch (SQLException e) {
				e.printStackTrace();
				return -1.0;
			}


		}


	}

	public static Integer getDesiredCadence(String name) {
		Connection conn = null;
		Statement stmt = null;

		List<String> psrNames = new ArrayList<String>();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS); 
			stmt = conn.createStatement();

			String sql = "select p.desired_cadence from Pulsars p "
					+ "where  p.name='"+ name+"'";


			ResultSet rs = stmt.executeQuery(sql);


			rs.next();
			return rs.getInt(1);

		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		finally{
			try{
				if(stmt!=null) conn.close();
			}catch (SQLException e) {
				e.printStackTrace();
				return null;
			}


		}

		
	}


	public static List<String> getTimingProgrammeSources() {

		Connection conn = null;
		Statement stmt = null;

		List<String> psrNames = new ArrayList<String>();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS); 
			stmt = conn.createStatement();

			String sql = "select p.name from Pulsars p "
					+ "where  p.observe=1";


			ResultSet rs = stmt.executeQuery(sql);


			while (rs.next()) {

				String name = rs.getString("name");

				psrNames.add(name);



			}

			return psrNames;

		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		finally{
			try{
				if(stmt!=null) conn.close();
			}catch (SQLException e) {
				e.printStackTrace();
				return null;
			}


		}


	}





	public static void main(String[] args) {



		//System.err.println(getPulsarSNRForObs("2018-04-15-04:11:50.000", "J0401-7608"));

//		for(TBSourceTO to: PSRCATManager.getTimingProgrammeSources()) {
//			System.err.println(getDesiredCadence(to.getPsrName()) + " " + to.getPsrName());
//		}
		
		for(PointingTO pto: DBManager.getAllPointings()) {
			System.err.println(pto.getPointingName() + " "+ pto.getLeastCadanceInDays());
		}

	}










	@Deprecated
	public static void addPointingsToDB_JDBC(List<Pointing> gridPoints){
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASS); 
			stmt = conn.createStatement();
			for(Pointing grid: gridPoints){
				String ra[] = grid.getAngleRA().toHHMMSS().split(":");
				String raStr = ra[0]+ra[1];

				String dec[] = grid.getAngleDEC().toDDMMSS().split(":");
				String decStr = dec[0] + dec[1];
				String name = "SMIRF_"+raStr + decStr;
				String comma = ",";
				String sql = "INSERT INTO mpsr_ksp_survey.pointings (pointing_name,ra_rad,dec_rad,ra_hms,dec_dms,gal_lat_dms,gal_long_dms,priority, type)"
						+ "VALUES("+ Utilities.asq(name) + comma + grid.getAngleRA().getRadianValue() + comma + grid.getAngleDEC().getRadianValue() + comma + Utilities.asq(grid.getAngleRA().toString()) + comma + Utilities.asq(grid.getAngleDEC().toString()) +
						comma + Utilities.asq(grid.getAngleLAT().toString()) + comma + Utilities.asq(grid.getAngleLON().toString()) + comma + grid.getPriority() + comma + Utilities.asq( grid.getType()) +")";
				stmt.executeUpdate(sql);

			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		finally{
			try{
				if(stmt!=null) conn.close();
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}



}
