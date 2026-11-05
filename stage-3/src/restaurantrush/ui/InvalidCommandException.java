package restaurantrush.ui;

/** Thrown when typed input is not a well-formed command. */
public class InvalidCommandException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidCommandException(String message) {
        super(message);
    }
}
