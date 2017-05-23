package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import bean.Angle;
import bean.FluxCalibrator;
import bean.Observation;
import bean.ObservationSessionTO;
import bean.ObservationTO;
import bean.ObservingSession;
import bean.PhaseCalibrator;
import bean.Pointing;
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


	public static Pointing getPointingByID(Integer pointingID){
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		Pointing pointing = entityManager.find(Pointing.class, pointingID);
		entityManager.getTransaction().commit();
		entityManager.close();
		return pointing;
	}


	public static Pointing getPointingByUniqueName(String pointingName){
		EntityManager entityManager = emFactory.createEntityManager( );
		TypedQuery<Pointing> query = entityManager.createQuery("FROM Pointing t where t.pointingName =?1",Pointing.class);
		Pointing result = query.setParameter(1, pointingName).getSingleResult();
		return result;
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

	public static void main(String[] args) {

		ObservationTO to = new ObservationTO();
		to.setUtc("2017-01-07-11:11:11.000");
		to.setName("SMIRF_1610-5703");
		to.setTobs(720);
		to.setObservingSession(null);
		to.setObsType(smcPointingSymbol);
		to.setTiedBeamSources(Arrays.asList(new TBSourceTO[]{PSRCATManager.getTBSouceByName("J1141-6545"),PSRCATManager.getTBSouceByName("J1745-3040")}));
		to.setObservingSession(DBService.getObservationSessionByID(701));
		DBService.addObservationToDB(to);
		
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
		
		EntityManager entityManager = emFactory.createEntityManager( );
		TypedQuery<Observation> query = entityManager.createQuery("select t from Observation t where t.utc =?1",Observation.class);
		Observation result = query.setParameter(1, utc).getSingleResult();
		return result;
		
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
