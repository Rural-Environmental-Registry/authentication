package hammerConsult.cardpg.keycloak.exception;

public class ErrorGenericException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7845599683931920887L;

	public ErrorGenericException() {
        super("User not found.");
    }

    public ErrorGenericException(String message) {
        super(message);
    }

    public ErrorGenericException(String message, Throwable cause) {
        super(message, cause);
    }
}
