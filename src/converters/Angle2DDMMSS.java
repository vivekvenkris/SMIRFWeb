package converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import bean.Angle;
@Converter
public class Angle2DDMMSS implements AttributeConverter<Angle, String> {
	@Override
	public String convertToDatabaseColumn(Angle angle) {
		return angle.toString();
	}
	@Override
	public Angle convertToEntityAttribute(String angleStr) {
		// TODO Auto-generated method stub
		return new Angle(angleStr,Angle.DDMMSS);
	}

}
