package bean;

import java.util.Comparator;

import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import manager.MolongloCoordinateTransforms;
import service.EphemService;

public class Coords {
 PointingTO pointingTO;
 Angle angleLST;
 Angle angleHA;
 Angle angleNS;
 Angle angleMD;
 
 public Coords(PointingTO pointingTO, Angle lst) throws EmptyCoordinatesException, CoordinateOverrideException {
	 this.angleLST = lst; 
	 this.pointingTO = pointingTO;
	 this.angleHA = EphemService.getHA(lst, pointingTO.getAngleRA());
	 CoordinateTO cto = new CoordinateTO(angleHA,pointingTO.getAngleDEC(), null,null);
	 MolongloCoordinateTransforms.skyToTel(cto);
	 this.angleNS = cto.getAngleNS();
	 this.angleMD = cto.getAngleMD();
	 
	 
}
 
 
 public void recompute(Angle lst) throws EmptyCoordinatesException, CoordinateOverrideException{
	 this.angleLST =lst;
	 this.angleHA = EphemService.getHA(lst, pointingTO.getAngleRA());
	 CoordinateTO cto = new CoordinateTO(angleHA,pointingTO.getAngleDEC(), null,null);
	 MolongloCoordinateTransforms.skyToTel(cto);
	 this.angleNS = cto.getAngleNS();
	 this.angleMD = cto.getAngleMD();
	 
 }
 @Override
	public String toString() {
		//return  "NS= " +this.angleNS + " MD= "+ this.angleMD + " " +this.pointingTO.getPointingID() + " " + this.pointingTO.getPointingName() +"\n";
		return  " " +this.angleNS + "  "+ this.angleMD + " " + " " +this.pointingTO.getAngleRA().getDegreeValue() + " " + this.pointingTO.getAngleDEC().getDegreeValue() + " " +this.pointingTO.getPointingID() + " " + this.pointingTO.getPointingName() +"\n";
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
