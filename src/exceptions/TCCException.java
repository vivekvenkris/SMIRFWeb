package exceptions;



import bean.TCCStatus;

public class TCCException extends CustomException{
	public TCCException(String message, String trace) {
		super(message,trace);
	}
	public TCCException(String message){
		super(message);
	}
	public TCCException(String message, String trace, TCCStatus status) {
		super(message,trace);
		this.statusObject = status;
	}	
}
