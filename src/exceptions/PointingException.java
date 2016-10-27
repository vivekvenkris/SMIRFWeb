package exceptions;

public class PointingException extends CustomException {

	public PointingException(String message) {
		message="Pointing exception. Cause:" +message;
	}
}
