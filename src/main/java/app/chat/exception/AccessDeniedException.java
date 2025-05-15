package app.chat.exception;

public class AccessDeniedException extends Exception {
	    
	    private static final long serialVersionUID = 1L;

	    public AccessDeniedException(String message) {
	        super(message);
	   }
}
