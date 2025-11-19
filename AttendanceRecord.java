package smartoffice.v1;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AttendanceRecord: simple POJO for one check-in or check-out event.
 *
 * CSV columns used for persistence:
 * employeeId,employeeName,method,isCheckIn,timestampIso
 *
 * - Uses java.time.LocalDateTime for timestamp.
 *
 * Addresses: attendance data model, timestamps, CSV persistence
 */
public class AttendanceRecord {
    private int employeeId;
    private String employeeName;
    private String method;    // "RFID"
    private boolean isCheckIn;
    private LocalDateTime timestamp;

    // DateTime format for CSV and display
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public AttendanceRecord(int employeeId, String employeeName, String method, boolean isCheckIn, LocalDateTime timestamp) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.method = method;
        this.isCheckIn = isCheckIn;
        this.timestamp = timestamp;
    }

    // Create record with current time
    public AttendanceRecord(int employeeId, String employeeName, String method, boolean isCheckIn) {
        this(employeeId, employeeName, method, isCheckIn, LocalDateTime.now());
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getMethod() {
        return method;
    }

    public boolean isCheckIn() {
        return isCheckIn;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // CSV row string
    public String toCsvRow() {
        return employeeId + "," + escapeCsv(employeeName) + "," + method + "," + (isCheckIn ? "1" : "0") + "," + timestamp.format(FORMATTER);
    }

    // Parse CSV row into AttendanceRecord. Returns null if parse error.
    public static AttendanceRecord fromCsvRow(String row) {
        // Expect 5 columns separated by commas: id,name,method,isCheckIn,timestamp
        String[] parts = row.split(",", 5);
        if (parts.length < 5) return null;
        try {
            int id = Integer.parseInt(parts[0].trim());
            String name = unescapeCsv(parts[1].trim());
            String method = parts[2].trim();
            boolean isCheckIn = parts[3].trim().equals("1");
            java.time.LocalDateTime ts = java.time.LocalDateTime.parse(parts[4].trim(), FORMATTER);
            return new AttendanceRecord(id, name, method, isCheckIn, ts);
        } catch (Exception e) {
            throw new ParsingException(
            return null;
        }
    }

    // Basic escaping for CSV field (escape quotes)
    private static String escapeCsv(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    private static String unescapeCsv(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
            s = s.replace("\"\"", "\"");
        }
        return s;
    }

    public String describe() {
        return "AttendanceRecord[empId=" + employeeId + ", name=" + employeeName + ", method=" + method +
                ", type=" + (isCheckIn ? "IN" : "OUT") + ", time=" + timestamp.format(FORMATTER) + "]";
    }
}
