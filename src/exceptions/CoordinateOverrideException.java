package exceptions;

import org.apache.commons.lang3.exception.ExceptionUtils;

import bean.CoordinateTO;

public class CoordinateOverrideException  extends CustomException{
	
	public CoordinateOverrideException(String message, CoordinateTO coordinateTO) {
		super(message,coordinateTO);
	}
	
	public CoordinateOverrideException(String message, String trace, CoordinateTO coords) {
		super(message,trace,coords);
	}
	
	public CoordinateOverrideException(String message) {
		super(message);
	}
	
	protected CoordinateOverrideException(){
		super();
	}
}
