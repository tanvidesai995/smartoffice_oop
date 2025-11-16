package smartoffice.v1;

/**
 * Abstract Person class.
 * Contains common fields for people in the system.
 *
 * Addresses rubric items: abstract class, package
 */
public abstract class Person {
    protected int id;       // numeric id (primitive used for simplicity)
    protected String name;  // person's name

    // Constructor
    public Person(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Overloaded constructor (demonstrates overloaded constructors across hierarchy)
    public Person(String name) {
        this.id = -1; // default unknown id
        this.name = name;
    }

    // Basic getter methods
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Abstract method to implement in subclasses
    public abstract String getRole();
}
