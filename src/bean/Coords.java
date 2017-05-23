package bean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import exceptions.TCCException;
import manager.MolongloCoordinateTransforms;
import service.EphemService;
import service.TCCStatusService;
import util.BackendConstants;
import util.Utilities;

public class Coords {
 PointingTO pointingTO;
 Angle angleLST;
 Angle angleHA;
 Angle angleNS;
 Angle angleMD;
 String utc;
 
 public Coords(PointingTO pointingTO, Angle lst) throws EmptyCoordinatesException, CoordinateOverrideException {
	 this.angleLST = lst; 
	 this.pointingTO = pointingTO;
	 this.angleHA = EphemService.getHA(lst, pointingTO.getAngleRA());
	 CoordinateTO cto = new CoordinateTO(angleHA,pointingTO.getAngleDEC(), null,null);
	 MolongloCoordinateTransforms.skyToTel(cto);
	 this.angleNS = cto.getAngleNS();
	 this.angleMD = cto.getAngleMD();
	 
	 
}

public Coords(PointingTO pointingTO){
	this.pointingTO = pointingTO;	 
}
public Coords(PointingTO pointingTO, LocalDateTime utcTime) throws EmptyCoordinatesException, CoordinateOverrideException{
	this(pointingTO, new Angle(EphemService.getRadLMSTforMolonglo(utcTime), Angle.HHMMSS));
	utc = utcTime.format(DateTimeFormatter.ofPattern(BackendConstants.backendUTCFormatOfPattern));
}

public Coords(TCCStatus status) throws CoordinateOverrideException, EmptyCoordinatesException{
	
	angleNS = new Angle((status.getTiltNS().getValue0() + status.getTiltNS().getValue1())/2.0, Angle.DEG) ;
	angleMD = new Angle((status.getTiltMD().getValue0() + status.getTiltMD().getValue1())/2.0, Angle.DEG) ;
	CoordinateTO cto = new CoordinateTO(null, null, angleNS, angleMD);
	MolongloCoordinateTransforms.telToSky(cto);
	
	this.angleHA  = cto.getAngleHA();
	this.angleLST = EphemService.getAngleLMSTForMolongloNow();
	
	
	pointingTO = new PointingTO(EphemService.getRA(angleLST, angleHA), cto.getAngleDEC());
	
	
}

public static void main(String[] args) throws TCCException, CoordinateOverrideException, EmptyCoordinatesException {
	new Coords(new TCCStatusService().getTelescopeStatus());
}
 
 public void recompute(Angle lst) throws EmptyCoordinatesException, CoordinateOverrideException{
	 this.angleLST =lst;
	 this.angleHA = EphemService.getHA(lst, pointingTO.getAngleRA());
	 CoordinateTO cto = new CoordinateTO(angleHA,pointingTO.getAngleDEC(), null,null);
	 MolongloCoordinateTransforms.skyToTel(cto);
	 this.angleNS = cto.getAngleNS();
	 this.angleMD = cto.getAngleMD();
	 
 }
 
 public void recomputeForNow() throws EmptyCoordinatesException, CoordinateOverrideException{
	 this.angleLST = new Angle(EphemService.getRadLMSTForMolongloNow(),Angle.RAD);
	 this.angleHA = EphemService.getHA(angleLST, pointingTO.getAngleRA());
	 CoordinateTO cto = new CoordinateTO(angleHA,pointingTO.getAngleDEC(), null,null);
	 MolongloCoordinateTransforms.skyToTel(cto);
	 this.angleNS = cto.getAngleNS();
	 this.angleMD = cto.getAngleMD();
 }
 @Override
	public String toString() {
		//return  "NS= " +this.angleNS + " MD= "+ this.angleMD + " " +this.pointingTO.getPointingID() + " " + this.pointingTO.getPointingName() +"\n";
		return  " " +this.angleNS + "  "+ this.angleMD + " " + " " +this.pointingTO.getAngleRA().getDegreeValue() + " " + this.pointingTO.getAngleDEC().getDegreeValue() + " " 
				+this.pointingTO.getPointingID() + " " + this.pointingTO.getPointingName() + " " + this.pointingTO.getType();
	} 
 
 @Override
	public boolean equals(Object obj) {
		if(! (obj instanceof Coords)) return false;
		Coords coords = (Coords) obj;
		return this.pointingTO.getPointingID().equals(coords.getPointingTO().getPointingID());
	}
 
 public static Comparator<Coords> compareMD = new Comparator<Coords>() {

	@Override
	public int compare(Coords c1, Coords c2) {
		return (int)Math.round(c1.getAngleMD().getRadianValue() - c2.getAngleMD().getRadianValue());
	}
};

public static Comparator<Coords> compareNS = new Comparator<Coords>() {

	@Override
	public int compare(Coords c1, Coords c2) {
		return (int)Math.round(c1.getAngleNS().getRadianValue() - c2.getAngleNS().getRadianValue());
	}
};

public static Comparator<Coords> compareNSMD = new Comparator<Coords>() {

	@Override
	public int compare(Coords c1, Coords c2) {
		int comp1 = c1.getAngleNS().getRadianValue().compareTo(c2.getAngleNS().getRadianValue());
		if(comp1!=0) return comp1;
		int comp2 = c1.getAngleMD().getRadianValue().compareTo(c2.getAngleMD().getRadianValue());
		return comp2;
	}
};

public static Comparator<Coords> compareMDNS = new Comparator<Coords>() {

	@Override
	public int compare(Coords c1, Coords c2) {
		int comp1 = c1.getAngleMD().getRadianValue().compareTo(c2.getAngleMD().getRadianValue());
		if(comp1!=0) return comp1;
		int comp2 = c1.getAngleNS().getRadianValue().compareTo(c2.getAngleNS().getRadianValue()); 
		return comp2;
	}
};


public static List<Coords> sortCoordsByNearestDistanceTo(List<Coords> inputList, Coords nearTo){
	
	List<Double> distanceList = apply(inputList, a -> Utilities.distance(a.getAngleNS(), a.getAngleMD(), nearTo.getAngleNS(), nearTo.getAngleMD()));
	List<Double> unsortedList = new ArrayList<>(distanceList);
	
	Collections.sort(distanceList);
	List<Double> sortedList = distanceList;
	
	List<Coords> outputList = new ArrayList<>();
	
	for( int i=0; i < inputList.size(); i++ ){
		int index = unsortedList.indexOf(sortedList.get(i));
		outputList.add(inputList.get(index));
	}
	return outputList;
	
}

public static <T, R> List<R> apply(Collection<T> coll, Function<? super T, ? extends R> mapper) {
    return coll.stream().map(mapper).collect(Collectors.toList());
}


public String getUtc() {
	return utc;
}

public void setUtc(String utc) {
	this.utc = utc;
}

public PointingTO getPointingTO() {
	return pointingTO;
}


public void setPointingTO(PointingTO pointingTO) {
	this.pointingTO = pointingTO;
}


public Angle getAngleLST() {
	return angleLST;
}

public void setAngleLST(Angle angleLST) {
	this.angleLST = angleLST;
}

public Angle getAngleHA() {
	return angleHA;
}

public void setAngleHA(Angle angleHA) {
	this.angleHA = angleHA;
}

public Angle getAngleNS() {
	return angleNS;
}
public void setAngleNS(Angle angleNS) {
	this.angleNS = angleNS;
}
public Angle getAngleMD() {
	return angleMD;
}
public void setAngleMD(Angle angleMD) {
	this.angleMD = angleMD;
}
 
 
 
}
