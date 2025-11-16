package smartoffice.v1;

/**
 * Manager extends Employee.
 * Shows hierarchical inheritance.
 *
 * Addresses rubric items: hierarchical inheritance
 */
public class Manager extends Employee {
    protected int teamSize;

    public Manager(int id, String name, String department, int teamSize) {
        super(id, name, department);
        this.teamSize = teamSize;
    }

    // Overloaded constructor
    public Manager(String name, String department, int teamSize) {
        super(name, department);
        this.teamSize = teamSize;
    }

    public int getTeamSize() {
        return teamSize;
    }

    @Override
    public String getRole() {
        return "Manager";
    }

    // Manager-specific method
    public String approveBooking(int roomId) {
        return "Manager " + name + " approved booking for room " + roomId;
    }
}
