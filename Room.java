package smartoffice.v1;

import java.util.ArrayList;
import java.util.List;

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
    private List<Booking> bookings;

    // Constructor
    public Room(int roomId, String name) {
        this.roomId = roomId;
        this.name = name;
        this.bookings = new ArrayList<Booking>();
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
        for (Booking b : bookings) {
            if (b.getTimeSlot().equals(timeSlot)) {
                throw new BookingException("Time slot already booked: " + timeSlot);
            }
        }
        Booking b = new Booking(bookedBy, timeSlot);
        bookings.add(b);
        return b;
    }

    // Simple method to cancel a booking
    public boolean cancelBooking(String bookedBy, String timeSlot) {
        Booking toRemove = null;
        for (Booking b : bookings) {
            if (b.getBookedBy().equals(bookedBy) && b.getTimeSlot().equals(timeSlot)) {
                toRemove = b;
                break;
            }
        }
        if (toRemove != null) {
            bookings.remove(toRemove);
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
