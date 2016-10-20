package manager;

import bean.CoordinateTO;
import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import util.Constants;

//this is Pablo's transformations;
public class MolongloCoordinateTransforms implements Constants{
	
	public static void telToSky(CoordinateTO coordinateTO) throws CoordinateOverrideException, EmptyCoordinatesException{
		
		if(coordinateTO.hasNullTel())  throw new EmptyCoordinatesException("Telescope coordinates given to teltoSky transform were null.",coordinateTO);		
		if(!coordinateTO.hasNullSky()) throw new CoordinateOverrideException("telToSky transform got non null sky coordinates.",coordinateTO);
		
		
		double ns = coordinateTO.getRadNS();
		double md = coordinateTO.getRadMD();
		double ha_y =   Math.sin(md)*cosSkew*cosSlope 
				      + Math.sin(ns)*Math.cos(md)*sinSkew 
				      - Math.cos(ns)*Math.cos(md)*cosSkew*sinSlope;

		double ha_x =  - Math.sin(ns)*Math.cos(md)*sinLat*cosSkew 
					   + Math.cos(ns)*Math.cos(md)*(cosLat*cosSlope - sinLat*sinSkew*sinSlope)
					   + Math.sin(md)*(cosLat*sinSlope + sinLat*sinSkew*cosSlope);
		
		coordinateTO.setRadHA(Math.atan2(ha_y,ha_x));

		double dec =     Math.asin(Math.sin(ns)*Math.cos(md)*cosLat*cosSkew 
				       + Math.cos(ns)*Math.cos(md)*(sinLat*cosSlope + cosLat*sinSkew*sinSlope) 
				       + Math.sin(md)*(sinLat*sinSlope - cosLat*sinSkew*cosSlope));
		
		coordinateTO.setRadDec(dec);
	}

	public static void skyToTel(CoordinateTO coordinateTO) throws EmptyCoordinatesException, CoordinateOverrideException{
		
		if(coordinateTO.hasNullSky())  throw new EmptyCoordinatesException("Sky coordinates given to skyToTel transform were null.",coordinateTO);
		if(!coordinateTO.hasNullTel()) throw new CoordinateOverrideException("skyToTel transform got non null telescope coordinates",coordinateTO);		
		double ha = coordinateTO.getRadHA();
		double dec = coordinateTO.getRadDec();
		
		double ns_y =    Math.sin(ha)*Math.cos(dec)*sinSkew 
				       - Math.cos(ha)*Math.cos(dec)*sinLat*cosSkew 
				       + Math.sin(dec)*cosLat*cosSkew;

		double ns_x =  - Math.sin(ha)*Math.cos(dec)*cosSkew*sinSlope 
				       + Math.cos(ha)*Math.cos(dec)*( cosLat*cosSlope - sinLat*sinSkew*sinSlope) 
				       + Math.sin(dec)*(sinLat*cosSlope+cosLat*sinSkew*sinSlope);

		coordinateTO.setRadNS(Math.atan2(ns_y,ns_x));

		double md = 	 Math.asin(Math.sin(ha)*Math.cos(dec)*cosSkew*cosSlope 
					   + Math.cos(ha)*Math.cos(dec)*(cosLat*sinSlope + sinLat*sinSkew*cosSlope)
					   + Math.sin(dec)*(sinLat*sinSlope - cosLat*sinSkew*cosSlope));
		coordinateTO.setRadMD(md);
	}

	

	
	
}
