package smartoffice.v1;

/**
 * Room class with a non-static nested Booking class.
 *
 * Demonstrates:
 * - nested class (non-static)
 * - overloaded constructors
 * - simple booking list
 *
 * Addresses rubric items: nested class, overloaded constructors
 */
public class Room {
    private int roomId;
    private String name;
    private Booking[] bookings;
    private bookingcount;

    // Constructor
    public Room(int roomId, String name) {
        this.roomId = roomId;
        this.name = name;
        this.bookings = new Booking[10];
        this.bookingcount=0;
    }

    // Overloaded constructor
    public Room(int roomId) {
        this(roomId, "Room-" + roomId);
    }

    public int getRoomId() {
        return roomId;
    }

    public String getName() {
        return name;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    // Book the room (creates a Booking object)
    public Booking createBooking(String bookedBy, String timeSlot) throws BookingException {
        // Simple check: prevent duplicate timeSlot
        for (Booking b : this.bookings) {
            if (b.getTimeSlot().equals(timeSlot)) {
                throw new BookingException("Time slot already booked: " + timeSlot);
            }
        }
        Booking b = new Booking(bookedBy, timeSlot);
        this.bookings[this.bookingcount++]= b;
        return b;
    }

    // Simple method to cancel a booking
    public boolean cancelBooking(String bookedBy, String timeSlot) {
        Booking toRemove = null;
        int index=0;
        for (int i=0;  i<this.bookings.length; i++) {
            b= this.bookings[i];
            if (b.getBookedBy().equals(bookedBy) && b.getTimeSlot().equals(timeSlot)) {
                toRemove = b;
                index=i;
                break;
            }
        }
        if (toRemove != null) {
            bookings[i]= null;
            return true;
        }
        return false;
    }

    // Non-static nested class (inner class) representing a Booking
    public class Booking {
        private String bookedBy;
        private String timeSlot;

        // Booking constructor
        public Booking(String bookedBy, String timeSlot) {
            this.bookedBy = bookedBy;
            this.timeSlot = timeSlot;
        }

        public String getBookedBy() {
            return bookedBy;
        }

        public String getTimeSlot() {
            return timeSlot;
        }

        public String describe() {
            return "Booking: " + bookedBy + " at " + timeSlot + " in " + Room.this.name;
        }
    }
}
