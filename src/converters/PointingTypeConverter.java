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
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		switch((String)arg2){
		case SMIRFConstants.candidatePointingSymbol:
			return "Candidate";
		case SMIRFConstants.smcPointingSymbol:
			return "SMC";
		case SMIRFConstants.lmcPointingSymbol:
			return "LMC";
		case SMIRFConstants.galacticPointingSymbol:
			return "Galactic";
		case SMIRFConstants.phaseCalibratorSymbol:
			return "PhaseCal";
		case SMIRFConstants.fluxCalibratorSymbol:
			return "FluxCal";
		case SMIRFConstants.randomPointingSymbol:
			return "Random";
		case "All":
			return "All";	
		default:
			return "Unknown";

		}	
	}

}
