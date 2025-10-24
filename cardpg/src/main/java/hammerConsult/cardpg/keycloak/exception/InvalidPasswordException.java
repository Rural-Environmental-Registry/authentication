package hammerConsult.cardpg.keycloak.exception;

public class InvalidPasswordException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -415583217990537523L;

	public InvalidPasswordException() {
        super("User not found.");
    }

    public InvalidPasswordException(String message) {
        super(message);
    }

    public InvalidPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
