package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import bean.PhaseCalibrator;
import bean.Pointing;
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
	
	public static PhaseCalibrator getCalibratorByID(Integer calibratorID){
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		PhaseCalibrator calibrator = entityManager.find(PhaseCalibrator.class, calibratorID);
		entityManager.getTransaction().commit();
		entityManager.close();
		return calibrator;
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

	// still under development
	public static Pointing getPointingByUniqueName(String pointingName){
		EntityManager entityManager = emFactory.createEntityManager( );
		TypedQuery<Pointing> query = entityManager.createQuery("FROM mpsr_ksp_survey.pointings t where t.pointing_name =?",Pointing.class);
		Pointing result = query.setParameter(1, pointingName).getSingleResult();
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
	
	public static List<Pointing> getAllPointingsOrderByNumObs(){
		EntityManager entityManager = emFactory.createEntityManager( );
		entityManager.getTransaction().begin();
		@SuppressWarnings("unchecked")
		List<Object> minObs = entityManager.createQuery("select min(q.numObs) from Pointing q  ").getResultList();
		System.err.println("survey number:" + ((Integer)minObs.get(0)+1));
		List<Pointing> pointings = entityManager.createQuery("SELECT p FROM Pointing p where p.numObs= ( select min(q.numObs) from Pointing q ) order by p.priority DESC ").getResultList();
		entityManager.getTransaction().commit();
		entityManager.close();
		return pointings;
	
	}
	
	

	public static void main(String[] args) {
		
		System.err.println(getAllPointingsOrderByNumObs());
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
