package bean;

import exceptions.CoordinateOverrideException;
import exceptions.EmptyCoordinatesException;
import util.SMIRFConstants;
import util.Utilities;

public class TBSourceTO {
	
	public static class DSPSRParameters{
		Double periodSecs;
		Double DM;
		Double acceleration;
		public DSPSRParameters(){
			
		}
		
		public DSPSRParameters(Double dm, Double periodSecs, Double acceleration) {
			this.periodSecs = periodSecs;
			this.DM = dm;
			this.acceleration = acceleration;
		}
		
		public DSPSRParameters(DSPSRParameters dspsrParameters) {
			this.periodSecs = dspsrParameters.getPeriodSecs();
			this.DM = dspsrParameters.getDM();
			this.acceleration = dspsrParameters.getAcceleration();
		}
		
		public Double getPeriodSecs() {
			return periodSecs;
		}
		public void setPeriodSecs(Double periodSecs) {
			this.periodSecs = periodSecs;
		}
		public Double getDM() {
			return DM;
		}
		public void setDM(Double dM) {
			DM = dM;
		}
		public Double getAcceleration() {
			return acceleration;
		}
		public void setAcceleration(Double acceleration) {
			this.acceleration = acceleration;
		}
		
		
	}
	
	Angle angleRA;
	Angle angleDEC;
	DSPSRParameters dspsrParams;
	String psrName;
	String ephemerides;
	Integer priority;
	Double fluxAt843MHz;
	boolean precessed;
	
	Double DM;
	Double F0;
	Double P0;
	
	public double getAbsoluteDistanceFromBoresight(Coords boresight) throws EmptyCoordinatesException, CoordinateOverrideException{
		
		Coords tb = new Coords(new PointingTO(this.getAngleRA(), this.getAngleDEC()), boresight.getAngleLST());
		return Math.abs(Utilities.distance(boresight.getAngleNS().getRadianValue(), boresight.getAngleMD().getRadianValue(), 
				tb.getAngleNS().getRadianValue(), tb.getAngleMD().getRadianValue()));
	}
	
	@Override
	public boolean equals(Object obj) {
		if( obj instanceof TBSourceTO) return ((TBSourceTO)obj).psrName.equals(psrName);
		return false;
	}
	
	
	@Override
	public String toString() {
		return psrName;
	}
	
	
	public TBSourceTO() {
		priority = 1;
		ephemerides = "";
	}
	
	

	public TBSourceTO(TBSourceTO to){
		
		this.angleRA = to.getAngleRA();
		this.angleDEC = to.getAngleDEC();
		if(to.getDspsrParams() != null) 
				this.dspsrParams = new DSPSRParameters(to.getDspsrParams());
		this.psrName = to.getPsrName();
		this.ephemerides = to.getEphemerides();
		this.priority = to.getPriority();
		this.fluxAt843MHz = to.getFluxAt843MHz();
		this.precessed = to.isPrecessed();
		DM = to.getDM();
		F0 = to.getF0();
		P0 = to.getP0();
	}
	
	public TBSourceTO(String jname){
		this();
		psrName = jname;
		priority = 1;
		ephemerides = "";
	}
	
	public void addToEphemerides( String str){
		this.ephemerides+=(str+"\n");
	}
	
	public Angle getAngleRA() {
		return angleRA;
	}
	public void setAngleRA(Angle angleRA) {
		this.angleRA = angleRA;
	}
	public Angle getAngleDEC() {
		return angleDEC;
	}
	public void setAngleDEC(Angle angleDEC) {
		this.angleDEC = angleDEC;
	}
	public DSPSRParameters getDspsrParams() {
		return dspsrParams;
	}
	public void setDspsrParams(DSPSRParameters dspsrParams) {
		this.dspsrParams = dspsrParams;
	}
	
	public String getPsrName() {
		return psrName;
	}
	public void setPsrName(String psrName) {
		this.psrName = psrName;
	}

	public String getEphemerides() {
		return ephemerides;
	}

	public void setEphemerides(String ephemerides) {
		this.ephemerides = ephemerides;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}


	public Double getFluxAt843MHz() {
		return fluxAt843MHz;
	}


	public void setFluxAt843MHz(Double fluxAt843MHz) {
		this.fluxAt843MHz = fluxAt843MHz;
	}


	public Double getDM() {
		return DM;
	}


	public void setDM(Double dM) {
		DM = dM;
	}


	public Double getF0() {
		return F0;
	}


	public void setF0(Double f0) {
		F0 = f0;
	}


	public Double getP0() {
		return P0;
	}


	public void setP0(Double p0) {
		P0 = p0;
	}

	public boolean isPrecessed() {
		return precessed;
	}

	public void setPrecessed(boolean precessed) {
		this.precessed = precessed;
	}

	
	
	
}
