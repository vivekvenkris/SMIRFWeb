package test;

public class TestNS{}
//
//import bean.Angle;
//import bean.CoordinateTO;
//import bean.Coords;
//import bean.ObservationTO;
//import bean.PointingTO;
//import bean.TBSourceTO;
//import exceptions.BackendException;
//import exceptions.CoordinateOverrideException;
//import exceptions.EmptyCoordinatesException;
//import exceptions.EphemException;
//import exceptions.ObservationException;
//import exceptions.TCCException;
//import manager.MolongloCoordinateTransforms;
//import manager.ObservationManager;
//import manager.PSRCATManager;
//import service.EphemService;
//import service.TCCService;
//import util.BackendConstants;
//import util.Constants;
//import util.SMIRFConstants;
//
//public class TestNS {
//	public static void main(String[] args) 
//			throws ObservationException, EmptyCoordinatesException, 
//			CoordinateOverrideException, TCCException, BackendException, InterruptedException, EphemException {
//		
//		ObservationManager manager = new ObservationManager();
//		TBSourceTO sourceTO = PSRCATManager.getTBSouceByName("J1453-6413");
//		PointingTO to = new PointingTO(sourceTO);
//		
//		
//		TCCService tccService = TCCService.createTccInstance();
//		
//		tccService.pointNS(new Angle(-3.0, Angle.DEG));
//		
//		
//		System.exit(0);
//		
//		for(;;){
//			
//			Coords temp = new Coords(to, EphemService.getAngleLMSTForMolongloNow());
//
//			if( Math.abs(temp.getAngleMD().getRadianValue()) <  Constants.RadMolongloMDBeamWidth/2.0) break;
//			
//			else Thread.sleep(60 * 1000);
//			
//			System.err.println( " Waiting for source. Source MD for LST =  " + temp.getAngleLST().toHHMMSS() + " = " + 
//			temp.getAngleMD().getDegreeValue() + " degrees");
//			
//			
//		}
//		
//		ObservationTO observation = new ObservationTO(new Coords(to, EphemService.getAngleLMSTForMolongloNow()), null, 300, "NS tester", BackendConstants.smirfBackend,
//				BackendConstants.tiedArrayFanBeam, "P000", -0.5, true);
//
//		for( double offset: new double[]{-0.5,0,0.5} ){
//			
//			System.err.println("TestNS: Considering offset = " + offset + " degrees");
//			
//			observation.setDegNSOffset(offset);
//			
//			System.err.println("TestNS: Starting observation..");
//			manager.startObserving(observation);
//			
//			System.err.println("TestNS: Waiting for observation to complete..");
//			manager.waitForObservationCompletion(observation);
//			
//			System.err.println("TestNS: stopping observation..");
//			manager.stopObserving();
//			
//		}
//		
//		
//		
//	
//	}
//}
