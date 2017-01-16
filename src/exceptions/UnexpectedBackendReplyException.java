package exceptions;

public class UnexpectedBackendReplyException  extends BackendException{

	public UnexpectedBackendReplyException(String command,String response) {
		message = "Backend returned unexpected reply to "+command+" command. Expected = OK got "+response;
	}
	
	public UnexpectedBackendReplyException(String command,String expected, String response) {
		message = "Backend returned unexpected reply to "+command +" command. Expected = "+ expected +" got "+response;
	}

}
