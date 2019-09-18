package test;

import bean.Angle;
import bean.PointingTO;

public class EffelsbergFollowup {
	public static void main(String[] args) {
		//PointingTO pto = new PointingTO(new Angle("22:05:55", Angle.HHMMSS), new Angle("-08:50:30", Angle.DDMMSS));
		
		double bw = 0.16;
		double spacing = 0.091;
		double extent = 1.4;
		PointingTO pto = new PointingTO(new Angle("22:05:55", Angle.HHMMSS), new Angle("-08:50:30", Angle.DDMMSS));
		System.err.println(pto.getAngleRA().toHHMMSS() + " " +  pto.getAngleDEC().toDDMMSS() + " " + pto.getAngleLAT().getDegreeValue() + " "+ pto.getAngleLON().getDegreeValue() );
		for(double i=0; i <= extent; i =i + 2 * bw + spacing ) {
			double dec_deg = pto.getAngleDEC().getDegreeValue();
			double ra_deg = (22.101947 +2.654839e-4*(dec_deg -8.943028) + 4.195246e-06*(dec_deg -8.9430278)*(dec_deg -8.9430278))*15;
			PointingTO npto = new PointingTO(new Angle(ra_deg + "", Angle.DEG), new Angle(pto.getAngleDEC().getDegreeValue() + i + "", Angle.DEG ));
			System.err.println(npto.getAngleRA().toHHMMSS() + " " +  npto.getAngleDEC().toDDMMSS() + " " + npto.getAngleLAT().getDegreeValue() + " "+ npto.getAngleLON().getDegreeValue() );
		}
				
		for(double i=-1 * extent; i <= 0; i =i + 2 * bw + spacing ) {
			double dec_deg = pto.getAngleDEC().getDegreeValue();
			double ra_deg = (22.101947 +2.654839e-4*(dec_deg -8.943028) + 4.195246e-06*(dec_deg -8.9430278)*(dec_deg -8.9430278))*15;
			PointingTO npto = new PointingTO(new Angle(ra_deg + "", Angle.DEG), new Angle(pto.getAngleDEC().getDegreeValue() + i + "", Angle.DEG ));
			System.err.println(npto.getAngleRA().toHHMMSS() + " " +  npto.getAngleDEC().toDDMMSS() + " " + npto.getAngleLAT().getDegreeValue() + " "+ npto.getAngleLON().getDegreeValue() );
		}
	}

}
