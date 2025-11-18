package smartoffice.v1;

/**
 * Device represents a simple office device (e.g., projector, AC).
 * Implements DeviceControllable and Loggable (multiple interfaces).
 *
 * Demonstrates:
 * - overloaded constructors
 * - wrapper usage in analytics (Integer)
 * - exception throwing (DeviceOperationException)
 * - implements interfaces (DeviceControllable, Loggable)
 *
 * Addresses rubric items: multiple interfaces, overloaded constructors, exception handling, wrappers
 */
public class Device implements DeviceControllable, Loggable {
    private String name;
    private String type;
    private boolean isOn;
    private Integer toggles; // wrapper class used to count toggles

    // Default constructor (overloaded)
    public Device() {
        this.name = "Unnamed";
        this.type = "Generic";
        this.isOn = false;
        this.toggles = Integer.valueOf(0);
    }

    // Overloaded constructor
    public Device(String name, String type) {
        this.name = name;
        this.type = type;
        this.isOn = false;
        this.toggles = Integer.valueOf(0);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isOn() {
        return isOn;
    }

    public Integer getToggles() {
        return toggles;
    }

    // Turn the device on
    public void turnOn() throws DeviceOperationException {
        if (isOn) {
            throw new DeviceOperationException("Device already ON: " + name);
        }
        isOn = true;
        toggles = Integer.valueOf(toggles.intValue() + 1);
    }

    // Turn the device off
    public void turnOff() throws DeviceOperationException {
        if (!isOn) {
            throw new DeviceOperationException("Device already OFF: " + name);
        }
        isOn = false;
        toggles = Integer.valueOf(toggles.intValue() + 1);
    }

    // Toggle the device
    public void toggle() throws DeviceOperationException {
        if (isOn) {
            turnOff();
        } else {
            turnOn();
        }
    }

    // Loggable interface implementation
    public String getLogEntry() {
        return "Device[" + name + "," + type + "] state=" + (isOn ? "ON" : "OFF") + " toggles=" + toggles;
    }

    // Overloaded vararg method: toggle multiple times (varargs)
    // Example: device.multiToggle(1,1,1) toggles three times.
    public void multiToggle(int... times) throws DeviceOperationException {
        for (int t : times) {
            if (t <= 0) continue;
            // t times toggle
            for (int i = 0; i < t; i++) {
                toggle();
            }
        }
    }

    // Overloaded vararg method: toggle with boolean flags (different signature)
    public void multiToggle(boolean... flags) throws DeviceOperationException {
        for (boolean f : flags) {
            if (f && !isOn) turnOn();
            if (!f && isOn) turnOff();
        }
    }

    public String toString() {
        return getLogEntry();
    }
}
