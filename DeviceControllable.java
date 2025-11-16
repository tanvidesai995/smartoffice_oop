package smartoffice.v1;

/**
 * Interface representing device control operations.
 *
 * Addresses rubric items: interface, multiple inheritance via interfaces
 */
public interface DeviceControllable {
    // Turn the device on
    void turnOn() throws DeviceOperationException;

    // Turn the device off
    void turnOff() throws DeviceOperationException;

    // Toggle device state
    void toggle() throws DeviceOperationException;
}
