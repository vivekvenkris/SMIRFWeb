package exceptions;

public class InvalidFanBeamNumberException extends CustomException {
	public InvalidFanBeamNumberException(String message, String trace) {
		super(message,trace);
	}
	public InvalidFanBeamNumberException(String message){
		super(message);
	}
	public InvalidFanBeamNumberException(String message, String trace, Integer fanbeam) {
		super(message,trace);
		this.statusObject = "FB number: " + fanbeam + " is invalid";
	}	
}
