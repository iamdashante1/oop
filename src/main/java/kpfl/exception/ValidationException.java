package kpfl.exception;

/** Thrown when user input or a business rule is violated. Shown to the user as a friendly message. */
public class ValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
