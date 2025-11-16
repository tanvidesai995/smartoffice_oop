package smartoffice.v1;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * OfficeSystem - corrected CLI coordinator.
 *
 * Fixes:
 * - Removed an invalid catch for DeviceOperationException that the compiler flagged.
 * - Keeps handling for BookingException, NumberFormatException, DateTimeParseException, and a generic Exception.
 *
 * Responsibilities:
 * - Rooms, Devices, Employees seeding
 * - Room booking (overloaded methods / varargs)
 * - Device toggling (varargs)
 * - Attendance via RFID (simulateRFIDScan)
 * - Generate daily/weekly/monthly attendance reports
 * - Logging via ConfigManager
 *
 * Short, beginner-friendly methods; no advanced Java constructs.
 *
 * Save as: smartoffice/v1/OfficeSystem.java
 * Addresses rubric items: CLI, overloaded methods, varargs, wrappers, I/O, exception handling, attendance integration
 */
public class OfficeSystem {
    private static List<Room> rooms = new ArrayList<Room>();
    private static List<Device> devices = new ArrayList<Device>();
    private static List<Employee> employees = new ArrayList<Employee>();
    private static ConfigManager configManager = new ConfigManager("activity.log");
    private static AttendanceManager attendanceManager = new AttendanceManager("attendance.csv");

    // ---------- Booking methods (overloaded / vararg) ----------
    public static void bookRoom(int roomId, String user, String timeSlot) throws BookingException {
        Room room = findRoomById(roomId);
        if (room == null) {
            throw new BookingException("Room not found: " + roomId);
        }
        Room.Booking booking = room.createBooking(user, timeSlot);
        String log = "BOOKED: " + booking.describe();
        configManager.log(log);
        System.out.println("Success: " + booking.describe());
    }

    public static void bookRoom(int roomId, String user) throws BookingException {
        bookRoom(roomId, user, "09:00-10:00"); // default slot
    }

    // vararg: book multiple rooms for same user
    public static void bookRoom(String user, int... roomIds) {
        for (int id : roomIds) {
            try { bookRoom(id, user); }
            catch (BookingException e) { System.err.println("Could not book room " + id + ": " + e.getMessage()); }
        }
    }

    // ---------- Device toggle helpers (varargs) ----------
    public static void toggleDevices(String... deviceNames) {
        for (String name : deviceNames) {
            Device d = findDeviceByName(name);
            if (d == null) {
                String msg = "Device not found: " + name;
                System.err.println(msg);
                configManager.log("ERROR: " + msg);
                continue;
            }
            try {
                d.toggle();
                String msg = "TOGGLE: " + d.getLogEntry();
                configManager.log(msg);
                System.out.println(msg);
            } catch (DeviceOperationException e) {
                // Device-level errors handled here per-device
                System.err.println("Device error: " + e.getMessage());
            }
        }
    }

    // vararg boolean flags to set first N devices
    public static void toggleDevices(boolean... flags) {
        int n = Math.min(flags.length, devices.size());
        for (int i = 0; i < n; i++) {
            Device d = devices.get(i);
            try {
                if (flags[i]) d.turnOn();
                else d.turnOff();
                String msg = "SET: " + d.getLogEntry();
                configManager.log(msg);
                System.out.println(msg);
            } catch (DeviceOperationException e) {
                System.err.println("Device error for " + d.getName() + ": " + e.getMessage());
            }
        }
    }

    // ---------- Helpers for lookups ----------
    public static Employee findEmployeeByIdStatic(int id) {
        for (Employee e : employees) if (e.getId() == id) return e;
        return null;
    }

    public static Employee findEmployeeByNameStatic(String name) {
        for (Employee e : employees) if (e.getName().equalsIgnoreCase(name)) return e;
        return null;
    }

    private static Room findRoomById(int id) {
        for (Room r : rooms) if (r.getRoomId() == id) return r;
        return null;
    }

    private static Device findDeviceByName(String name) {
        for (Device d : devices) if (d.getName().equalsIgnoreCase(name)) return d;
        return null;
    }

    // simple analytics using wrappers
    public static double averageDeviceToggles() {
        if (devices.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Device d : devices) sum += d.getToggles().doubleValue();
        Double avg = Double.valueOf(sum / devices.size());
        return avg.doubleValue();
    }

    // Seed initial demo data
    private static void seedData() {
        rooms.add(new Room(101, "Conference A"));
        rooms.add(new Room(102, "Conference B"));
        rooms.add(new Room(201, "Huddle Room"));

        devices.add(new Device("Projector-1", "Projector"));
        devices.add(new Device("AC-1", "AC"));
        devices.add(new Device("Light-1", "Light"));

        employees.add(new Employee(1, "Alice", "Engineering"));
        employees.add(new Employee(2, "Bob", "Design"));
        employees.add(new Employee(3, "Carol", "QA"));
    }

    // ---------- Main CLI loop ----------
    public static void main(String[] args) {
        seedData();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Smart Office CLI (with Attendance via RFID)");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1": // Book room
                        System.out.println("Enter room id (e.g., 101):");
                        String rIdStr = scanner.nextLine().trim();
                        Integer roomId = Integer.valueOf(Integer.parseInt(rIdStr)); // wrapper used
                        System.out.println("Enter your name:");
                        String user = scanner.nextLine().trim();
                        System.out.println("Enter timeslot (e.g., 10:00-11:00) or press Enter for default:");
                        String timeslot = scanner.nextLine().trim();
                        if (timeslot.isEmpty()) bookRoom(roomId.intValue(), user);
                        else bookRoom(roomId.intValue(), user, timeslot);
                        break;

                    case "2": // Toggle devices by name
                        System.out.println("Enter device names separated by commas (e.g., Projector-1,AC-1):");
                        String line = scanner.nextLine().trim();
                        String[] names = line.split(",");
                        for (int i = 0; i < names.length; i++) names[i] = names[i].trim();
                        toggleDevices(names);
                        break;

                    case "3": // Device boolean flags
                        System.out.println("Enter device state flags (true,false,...) to set first devices accordingly:");
                        String flagsLine = scanner.nextLine().trim();
                        String[] flagParts = flagsLine.split(",");
                        boolean[] flags = new boolean[flagParts.length];
                        for (int i = 0; i < flagParts.length; i++) flags[i] = Boolean.parseBoolean(flagParts[i].trim());
                        toggleDevices(flags);
                        break;

                    case "4": // Average toggles
                        System.out.println("Average device toggles: " + averageDeviceToggles());
                        break;

                    // ===== Attendance options (RFID only) =====
                    case "10":
                        // Simulate RFID check-in/out (this code does not throw DeviceOperationException)
                        System.out.println("Enter employee ID (integer):");
                        String idStr = scanner.nextLine().trim();
                        int empId = Integer.parseInt(idStr);
                        System.out.println("Check-in or Check-out? (in/out):");
                        String inout = scanner.nextLine().trim().toLowerCase();
                        boolean isIn = inout.startsWith("i");
                        AttendanceRecord rec1 = attendanceManager.simulateRFIDScan(empId, isIn);
                        configManager.log("ATTEND: " + rec1.describe());
                        System.out.println("Recorded: " + rec1.describe());
                        break;

                    case "12":
                        // Generate daily report
                        System.out.println("Enter date for daily report (YYYY-MM-DD):");
                        String dateStr = scanner.nextLine().trim();
                        LocalDate d = LocalDate.parse(dateStr);
                        String daily = attendanceManager.generateReportDaily(d);
                        System.out.println(daily);
                        break;

                    case "13":
                        // Generate weekly report: enter any date in that week
                        System.out.println("Enter a date in the week (YYYY-MM-DD):");
                        String wstr = scanner.nextLine().trim();
                        LocalDate wd = LocalDate.parse(wstr);
                        String weekly = attendanceManager.generateReportWeekly(wd);
                        System.out.println(weekly);
                        break;

                    case "14":
                        // Generate monthly report
                        System.out.println("Enter year (e.g., 2025):");
                        int year = Integer.parseInt(scanner.nextLine().trim());
                        System.out.println("Enter month (1-12):");
                        int month = Integer.parseInt(scanner.nextLine().trim());
                        String monthly = attendanceManager.generateReportMonthly(year, month);
                        System.out.println(monthly);
                        break;

                    case "5": // Read activity log
                        System.out.println("Logs:");
                        System.out.println(configManager.readLog());
                        break;

                    case "6": // Demo bulk booking
                        System.out.println("Sample booking via bulk vararg: booking user 'Alice' for rooms 101 and 201.");
                        bookRoom("Alice", 101, 201);
                        break;

                    case "0":
                        running = false;
                        break;

                    default:
                        System.out.println("Unknown option. Try again.");
                }
            } catch (BookingException be) {
                System.err.println("Booking error: " + be.getMessage());
            } catch (NumberFormatException nfe) {
                System.err.println("Invalid number: " + nfe.getMessage());
            } catch (DateTimeParseException dtpe) {
                System.err.println("Invalid date format: " + dtpe.getMessage());
            } catch (Exception e) {
                // generic catch-all for any unexpected runtime exceptions
                System.err.println("Unexpected error: " + e.getMessage());
            }
        }

        scanner.close();
        System.out.println("Exiting Smart Office CLI. Goodbye!");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("Menu:");
        System.out.println("1 - Book room");
        System.out.println("2 - Toggle devices by name (comma separated)");
        System.out.println("3 - Set first devices states by boolean flags (vararg)");
        System.out.println("4 - Show device toggles average");
        System.out.println("5 - Read activity log");
        System.out.println("6 - Demo: bulk book rooms for 'Alice' (vararg)");
        System.out.println("--- Attendance (RFID only) ---");
        System.out.println("10 - Simulate RFID check-in/check-out (by employee ID)");
        System.out.println("12 - Generate daily attendance report (YYYY-MM-DD)");
        System.out.println("13 - Generate weekly attendance report (enter a date in week)");
        System.out.println("14 - Generate monthly attendance report (enter year and month)");
        System.out.println("0 - Exit");
        System.out.println("Enter choice:");
    }
}
