package converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import util.SMIRFConstants;
@FacesConverter("converters.PointingTypeConverter")
public class PointingTypeConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {

		switch(arg2){
		case  "Candidate":
			return SMIRFConstants.candidatePointingSymbol;
		case "SMC":
			return SMIRFConstants.smcPointingSymbol;
		case "LMC":
			return SMIRFConstants.lmcPointingSymbol;
		case "Galactic":
			return SMIRFConstants.galacticPointingSymbol;
		case "PhaseCal":
			return SMIRFConstants.phaseCalibratorSymbol;
		case "FluxCal":
			return SMIRFConstants.fluxCalibratorSymbol;
		case "Random":
			return SMIRFConstants.randomPointingSymbol;
		case "All":
			return "All";	
		default:
			return "Unknown";

		}
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object obj) {


		String arg2 = (String)obj;
		if (SMIRFConstants.candidatePointingSymbol.equals(arg2)) {
			return "Candidate";
		} else if (SMIRFConstants.smcPointingSymbol.equals(arg2)) {
			return "SMC";
		} else if (SMIRFConstants.lmcPointingSymbol.equals(arg2)) {
			return "LMC";
		} else if (SMIRFConstants.galacticPointingSymbol.equals(arg2)) {
			return "Galactic";
		} else if (SMIRFConstants.phaseCalibratorSymbol.equals(arg2)) {
			return "PhaseCal";
		} else if (SMIRFConstants.fluxCalibratorSymbol.equals(arg2)) {
			return "FluxCal";
		} else if (SMIRFConstants.randomPointingSymbol.equals(arg2)) {
			return "Random";
		} else if (arg2.equals("All")) {
			return "All";
		} else {
			return "Unknown";
		}	
	}

}
