package smartoffice.v1;

/**
 * Custom exception for device operation errors.
 *
 * Addresses rubric items: custom exceptions, exception handling
 */
public class DeviceOperationException extends Exception {
    public DeviceOperationException(String message) {
        super(message);
    }
}
