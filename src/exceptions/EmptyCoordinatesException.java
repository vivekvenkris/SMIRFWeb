package exceptions;

import bean.CoordinateTO;

public class EmptyCoordinatesException extends CustomException{
	
	public EmptyCoordinatesException(String message, String trace, CoordinateTO coords) {
		super(message,trace,coords);
	}
	public EmptyCoordinatesException(String message, CoordinateTO coords) {
		super(message,coords);
	}
	public EmptyCoordinatesException(String message, String trace) {
		super(message,trace);
	}
	
	public EmptyCoordinatesException(String message) {
		super(message);
	}
	
	protected EmptyCoordinatesException(){
		super();
	}
	

}
