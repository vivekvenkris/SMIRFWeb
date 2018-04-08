package converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import bean.PhaseCalibratorTO;
import manager.DBManager;
@FacesConverter("converters.PhaseCalibratorConverter")
public class PhaseCalibratorConverter implements Converter{
	@Override
	public Object getAsObject(FacesContext fc, UIComponent uic, String str) {
		if(str.equals("auto")){
			return null;
		}
		else{
			return (Object)DBManager.getPhaseCalibratorByName(str);
		}
	}

	@Override
	public String getAsString(FacesContext fc, UIComponent uic, Object obj) {
		if(obj instanceof PhaseCalibratorTO)
		return ((PhaseCalibratorTO)obj).getSourceName();
		else return obj.toString();
	}
}