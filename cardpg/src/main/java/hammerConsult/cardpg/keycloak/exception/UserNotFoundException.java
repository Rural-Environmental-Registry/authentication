package hammerConsult.cardpg.keycloak.exception;

public class UserNotFoundException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7845599683931920887L;

	public UserNotFoundException() {
        super("User not found.");
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
