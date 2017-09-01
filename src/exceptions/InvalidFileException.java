package exceptions;

public class InvalidFileException extends CustomException{
	public InvalidFileException(String message, String trace) {
		super(message, trace);
	}
	public InvalidFileException(String message) {
		super(message);
	}
}
