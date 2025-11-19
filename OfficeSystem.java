package smartoffice.v1;

import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * OfficeSystem (array-based) - CLI coordinator rewritten to use arrays.
 *
 * - rooms, devices, employees use manually-resizing arrays
 * - no List, ArrayList, Collections, Map, or StringBuilder
 * - uses AttendanceManager and ConfigManager (both assumed present)
 */
public class OfficeSystem {
    private static Room[] rooms = new Room[8];
    private static int roomCount = 0;

    private static Device[] devices = new Device[8];
    private static int deviceCount = 0;

    private static Employee[] employees = new Employee[8];
    private static int employeeCount = 0;

    private static ConfigManager configManager = new ConfigManager("activity.log");
    private static AttendanceManager attendanceManager = new AttendanceManager("attendance.csv");

    // ---------- dynamic add helpers ----------
    private static void ensureRoomCapacity() {
        if (roomCount == rooms.length) {
            Room[] bigger = new Room[rooms.length * 2];
            System.arraycopy(rooms, 0, bigger, 0, rooms.length);
            rooms = bigger;
        }
    }

    private static void addRoom(Room r) {
        ensureRoomCapacity();
        rooms[roomCount++] = r;
    }

    private static void ensureDeviceCapacity() {
        if (deviceCount == devices.length) {
            Device[] bigger = new Device[devices.length * 2];
            System.arraycopy(devices, 0, bigger, 0, devices.length);
            devices = bigger;
        }
    }

    private static void addDevice(Device d) {
        ensureDeviceCapacity();
        devices[deviceCount++] = d;
    }

    private static void ensureEmployeeCapacity() {
        if (employeeCount == employees.length) {
            Employee[] bigger = new Employee[employees.length * 2];
            System.arraycopy(employees, 0, bigger, 0, employees.length);
            employees = bigger;
        }
    }

    private static void addEmployee(Employee e) {
        ensureEmployeeCapacity();
        employees[employeeCount++] = e;
    }

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
        int n = Math.min(flags.length, deviceCount);
        for (int i = 0; i < n; i++) {
            Device d = devices[i];
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
        for (int i = 0; i < employeeCount; i++) {
            Employee e = employees[i];
            if (e != null && e.getId() == id) return e;
        }
        return null;
    }

    public static Employee findEmployeeByNameStatic(String name) {
        if (name == null) return null;
        for (int i = 0; i < employeeCount; i++) {
            Employee e = employees[i];
            if (e != null && e.getName().equalsIgnoreCase(name)) return e;
        }
        return null;
    }

    private static Room findRoomById(int id) {
        for (int i = 0; i < roomCount; i++) {
            Room r = rooms[i];
            if (r != null && r.getRoomId() == id) return r;
        }
        return null;
    }

    private static Device findDeviceByName(String name) {
        if (name == null) return null;
        for (int i = 0; i < deviceCount; i++) {
            Device d = devices[i];
            if (d != null && d.getName().equalsIgnoreCase(name)) return d;
        }
        return null;
    }

    // simple analytics replacing Collections usage
    public static double averageDeviceToggles() {
        if (deviceCount == 0) return 0.0;
        double sum = 0.0;
        for (int i = 0; i < deviceCount; i++) {
            Device d = devices[i];
            if (d != null) sum += d.getToggles().doubleValue();
        }
        Double avg = Double.valueOf(sum / deviceCount);
        return avg.doubleValue();
    }

    // Seed initial demo data (uses addRoom/addDevice/addEmployee)
    private static void seedData() {
        addRoom(new Room(101, "Conference A"));
        addRoom(new Room(102, "Conference B"));
        addRoom(new Room(201, "Huddle Room"));

        addDevice(new Device("Projector-1", "Projector"));
        addDevice(new Device("AC-1", "AC"));
        addDevice(new Device("Light-1", "Light"));

        addEmployee(new Employee(1, "Alice", "Engineering"));
        addEmployee(new Employee(2, "Bob", "Design"));
        addEmployee(new Employee(3, "Carol", "QA"));
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
