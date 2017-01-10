package exceptions;

public class ObservationException extends CustomException{
	public ObservationException(String message, String trace) {
		super(message, trace);
	}
	public ObservationException(String message) {
		super(message);
	}
	

}
