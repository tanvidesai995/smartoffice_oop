package smartoffice.v1;

/**
 * Employee extends Person.
 * Simple concrete class representing a staff member.
 *
 * Addresses rubric items: hierarchical inheritance
 */
public class Employee extends Person {
    protected String department;

    public Employee(int id, String name, String department) {
        super(id, name); // calls Person(int, String)
        this.department = department;
    }

    // Overloaded constructor
    public Employee(String name, String department) {
        super(name); // calls Person(String)
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public String getRole() {
        return "Employee";
    }

    // Simple behavior
    public String checkIn() {
        return name + " checked in to department " + department;
    }
}
