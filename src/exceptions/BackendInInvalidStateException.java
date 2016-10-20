package exceptions;

public class BackendInInvalidStateException extends BackendException {
	
public BackendInInvalidStateException(String action, String expected, String got) {
	message = "backend not in expected state to "+action + "backend. Expected: "+ expected + " got:"+got;
}
}
