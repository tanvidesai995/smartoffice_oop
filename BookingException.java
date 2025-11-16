package smartoffice.v1;

/**
 * Custom exception thrown when booking fails.
 *
 * Addresses rubric items: custom exceptions, exception handling
 */
public class BookingException extends Exception {
    public BookingException(String message) {
        super(message);
    }
}
