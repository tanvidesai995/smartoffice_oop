package smartoffice.v1;

/**
 * Admin extends Manager - further down the hierarchy.
 *
 * Addresses rubric items: hierarchical inheritance
 */
public class Admin extends Manager {
    private boolean superUser;

    public Admin(int id, String name, String department, int teamSize, boolean superUser) {
        super(id, name, department, teamSize);
        this.superUser = superUser;
    }

    // Overloaded constructor
    public Admin(String name, String department, int teamSize) {
        super(name, department, teamSize);
        this.superUser = false;
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    public boolean isSuperUser() {
        return superUser;
    }

    // Admin-specific action
    public String resetDevice(String deviceName) {
        return "Admin " + name + " reset device " + deviceName;
    }
}
