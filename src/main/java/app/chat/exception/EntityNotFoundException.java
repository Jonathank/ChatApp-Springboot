package app.chat.exception;

public class EntityNotFoundException extends Exception {
    
    private static final long serialVersionUID = 1L;

    public EntityNotFoundException(String message) {
        super(message);
   }
}
