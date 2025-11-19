
//Implementation without String Builder- check if should implement this


   package smartoffice.v1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;


 * AttendanceManager (array-based): manages attendance records in memory and persists to CSV.
 *
 * - Uses arrays with manual resizing instead of Lists
 * - No Collections, Maps, or StringBuilder
 * - Reports implemented by brute-force scanning (no grouping structures)
 
public class AttendanceManager {
    private String attendanceCsvPath;

    // dynamic array for records
    private AttendanceRecord[] records;
    private int recordCount;

    private static final int INITIAL_CAPACITY = 16;

    public AttendanceManager(String attendanceCsvPath) {
        this.attendanceCsvPath = attendanceCsvPath;
        this.records = new AttendanceRecord[INITIAL_CAPACITY];
        this.recordCount = 0;
        loadFromCsv();
    }

    // add record to internal array (dynamic grow)
    private void addRecord(AttendanceRecord rec) {
        if (recordCount == records.length) {
            AttendanceRecord[] bigger = new AttendanceRecord[records.length * 2];
            System.arraycopy(records, 0, bigger, 0, records.length);
            records = bigger;
        }
        records[recordCount++] = rec;
    }

    // Record a new attendance event and persist to CSV
    public void recordAttendance(AttendanceRecord rec) {
        addRecord(rec);
        appendToCsv(rec);
    }

    // Append a CSV row (simple, append mode)
    private void appendToCsv(AttendanceRecord rec) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(attendanceCsvPath, true)); // append
            writer.write(rec.toCsvRow());
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Failed to append attendance CSV: " + e.getMessage());
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (IOException ex) { /* ignore */ }
            }
        }
    }

    // Load all records from CSV into memory (simple)
    private void loadFromCsv() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(attendanceCsvPath));
            String line;
            while ((line = reader.readLine()) != null) {
                AttendanceRecord r = AttendanceRecord.fromCsvRow(line);
                if (r != null) addRecord(r);
            }
        } catch (IOException e) {
            // file might not exist; that's fine
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException ex) { /* ignore */ }
            }
        }
    }

    // Simulated RFID scan by employeeId (stub)
    // If isCheckIn true -> record check-in, else check-out
    public AttendanceRecord simulateRFIDScan(int employeeId, boolean isCheckIn) {
        String name = lookupEmployeeNameById(employeeId);
        if (name == null) {
            name = "Unknown-" + employeeId; // allow unknown IDs to be recorded
        }
        AttendanceRecord rec = new AttendanceRecord(employeeId, name, "RFID", isCheckIn);
        recordAttendance(rec);
        return rec;
    }

    // Helper: find employee name by id using OfficeSystem (simple public helper)
    private String lookupEmployeeNameById(int id) {
        Employee e = OfficeSystem.findEmployeeByIdStatic(id);
        if (e != null) return e.getName();
        return null;
    }

    // Return a shallow copy array of all records (caller gets array sized exactly to count)
    public AttendanceRecord[] getAllRecords() {
        AttendanceRecord[] out = new AttendanceRecord[recordCount];
        System.arraycopy(records, 0, out, 0, recordCount);
        return out;
    }

    // helper: collect records for a specific date into a temp array, returns array sized to count
    private AttendanceRecord[] collectRecordsForDate(LocalDate date) {
        AttendanceRecord[] tmp = new AttendanceRecord[recordCount];
        int c = 0;
        for (int i = 0; i < recordCount; i++) {
            AttendanceRecord r = records[i];
            if (r != null && r.getTimestamp().toLocalDate().equals(date)) {
                tmp[c++] = r;
            }
        }
        AttendanceRecord[] out = new AttendanceRecord[c];
        System.arraycopy(tmp, 0, out, 0, c);
        return out;
    }

    // helper: collect records between inclusive start and end
    private AttendanceRecord[] collectRecordsBetween(LocalDate start, LocalDate end) {
        AttendanceRecord[] tmp = new AttendanceRecord[recordCount];
        int c = 0;
        for (int i = 0; i < recordCount; i++) {
            AttendanceRecord r = records[i];
            LocalDate d = r.getTimestamp().toLocalDate();
            if ((d.isEqual(start) || d.isAfter(start)) && (d.isEqual(end) || d.isBefore(end))) {
                tmp[c++] = r;
            }
        }
        AttendanceRecord[] out = new AttendanceRecord[c];
        System.arraycopy(tmp, 0, out, 0, c);
        return out;
    }

    // insertion sort by timestamp (in-place) for small arrays
    private void sortRecordsByTimestamp(AttendanceRecord[] arr) {
        if (arr == null) return;
        for (int i = 1; i < arr.length; i++) {
            AttendanceRecord key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j].getTimestamp().isAfter(key.getTimestamp())) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    // ========================= DAILY REPORT =============================
    // Returns human-readable report and writes CSV file
    public String generateReportDaily(LocalDate date) {
        // collect records for date
        AttendanceRecord[] dayRecords = collectRecordsForDate(date);

        // find unique employee IDs present that day (brute force)
        int[] empIdsTmp = new int[dayRecords.length];
        int empIdCount = 0;
        for (int i = 0; i < dayRecords.length; i++) {
            int id = dayRecords[i].getEmployeeId();
            boolean found = false;
            for (int j = 0; j < empIdCount; j++) {
                if (empIdsTmp[j] == id) { found = true; break; }
            }
            if (!found) empIdsTmp[empIdCount++] = id;
        }

        String sb = "";
        String header = "Daily Attendance Report for " + date.toString();
        sb += header + System.lineSeparator();
        sb += "empId,empName,firstCheckIn,lastCheckOut,totalHours (approx),notes" + System.lineSeparator();

        String outCsv = "attendance-report-" + date.format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outCsv));
            writer.write("empId,empName,firstCheckIn,lastCheckOut,totalHours,notes");
            writer.newLine();

            // For each employee id, scan records again to collect that employee's records for the day
            for (int eidx = 0; eidx < empIdCount; eidx++) {
                int empId = empIdsTmp[eidx];

                // collect this employee's records for the date
                AttendanceRecord[] tmp = new AttendanceRecord[dayRecords.length];
                int tc = 0;
                for (int k = 0; k < dayRecords.length; k++) {
                    if (dayRecords[k].getEmployeeId() == empId) tmp[tc++] = dayRecords[k];
                }
                AttendanceRecord[] empRecords = new AttendanceRecord[tc];
                System.arraycopy(tmp, 0, empRecords, 0, tc);

                // sort by timestamp
                sortRecordsByTimestamp(empRecords);

                // compute firstIn and lastOut
                LocalDateTime firstIn = null;
                LocalDateTime lastOut = null;
                for (int r = 0; r < empRecords.length; r++) {
                    AttendanceRecord ar = empRecords[r];
                    if (ar.isCheckIn() && firstIn == null) firstIn = ar.getTimestamp();
                    if (!ar.isCheckIn()) lastOut = ar.getTimestamp();
                }

                String notes = "";
                double hours = 0.0;
                if (firstIn != null && lastOut != null && lastOut.isAfter(firstIn)) {
                    Duration dur = Duration.between(firstIn, lastOut);
                    hours = dur.toMinutes() / 60.0;
                } else {
                    notes = "missing check-in or check-out";
                }

                String empName = (empRecords.length > 0) ? empRecords[0].getEmployeeName() : ("Emp-" + empId);
                String firstStr = (firstIn == null) ? "" : firstIn.format(AttendanceRecord.FORMATTER);
                String lastStr = (lastOut == null) ? "" : lastOut.format(AttendanceRecord.FORMATTER);

                sb += empId + "," + empName + "," + firstStr + "," + lastStr + "," +
                      String.format("%.2f", hours) + "," + notes + System.lineSeparator();

                writer.write(empId + "," + escapeCsv(empName) + "," + firstStr + "," + lastStr + "," +
                             String.format("%.2f", hours) + "," + notes);
                writer.newLine();
            }

            writer.flush();
        } catch (IOException e) {
            sb += "Failed to write daily report CSV: " + e.getMessage() + System.lineSeparator();
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (IOException ex) { /* ignore */ }
            }
        }

        return sb;
    }

    // ========================= WEEKLY REPORT =============================
    public String generateReportWeekly(LocalDate anyDateInWeek) {
        LocalDate monday = anyDateInWeek.with(java.time.DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);

        // collect records between dates
        AttendanceRecord[] weekRecords = collectRecordsBetween(monday, sunday);

        // find unique employee IDs in weekRecords
        int[] empIdsTmp = new int[weekRecords.length];
        int empIdCount = 0;
        for (int i = 0; i < weekRecords.length; i++) {
            int id = weekRecords[i].getEmployeeId();
            boolean found = false;
            for (int j = 0; j < empIdCount; j++) {
                if (empIdsTmp[j] == id) { found = true; break; }
            }
            if (!found) empIdsTmp[empIdCount++] = id;
        }

        String sb = "";
        sb += "Weekly Attendance Report for " + monday.toString() + " to " + sunday.toString() + System.lineSeparator();
        sb += "empId,empName,daysPresent,totalHours(approx),notes" + System.lineSeparator();

        String outCsv = "attendance-report-week-" + monday.format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outCsv));
            writer.write("empId,empName,daysPresent,totalHours,notes");
            writer.newLine();

            for (int ei = 0; ei < empIdCount; ei++) {
                int empId = empIdsTmp[ei];

                double totalHours = 0.0;
                int daysPresent = 0;
                String notes = "";

                // for each day in week, check presence
                for (int d = 0; d < 7; d++) {
                    LocalDate cur = monday.plusDays(d);

                    // collect this employee's records for this date (scan weekRecords)
                    AttendanceRecord[] tmp = new AttendanceRecord[weekRecords.length];
                    int tc = 0;
                    for (int k = 0; k < weekRecords.length; k++) {
                        if (weekRecords[k].getEmployeeId() == empId &&
                            weekRecords[k].getTimestamp().toLocalDate().equals(cur)) {
                            tmp[tc++] = weekRecords[k];
                        }
                    }
                    if (tc == 0) continue;

                    // got records for this date
                    daysPresent++;

                    AttendanceRecord[] dayList = new AttendanceRecord[tc];
                    System.arraycopy(tmp, 0, dayList, 0, tc);
                    sortRecordsByTimestamp(dayList);

                    LocalDateTime firstIn = null;
                    LocalDateTime lastOut = null;
                    for (int r = 0; r < dayList.length; r++) {
                        AttendanceRecord ar = dayList[r];
                        if (ar.isCheckIn() && firstIn == null) firstIn = ar.getTimestamp();
                        if (!ar.isCheckIn()) lastOut = ar.getTimestamp();
                    }

                    if (firstIn != null && lastOut != null && lastOut.isAfter(firstIn)) {
                        totalHours += Duration.between(firstIn, lastOut).toMinutes() / 60.0;
                    } else {
                        notes = "some days missing check-in/out";
                    }
                } // each day

                // pick a name from any matching record in weekRecords
                String empName = "Emp-" + empId;
                for (int k = 0; k < weekRecords.length; k++) {
                    if (weekRecords[k].getEmployeeId() == empId) { empName = weekRecords[k].getEmployeeName(); break; }
                }

                sb += empId + "," + empName + "," + daysPresent + "," +
                      String.format("%.2f", totalHours) + "," + notes + System.lineSeparator();

                writer.write(empId + "," + escapeCsv(empName) + "," + daysPresent + "," +
                             String.format("%.2f", totalHours) + "," + notes);
                writer.newLine();
            }

            writer.flush();
        } catch (IOException e) {
            sb += "Failed to write weekly report CSV: " + e.getMessage() + System.lineSeparator();
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (IOException ex) { /* ignore */ }
            }
        }

        return sb;
    }

    // ========================= MONTHLY REPORT =============================
    public String generateReportMonthly(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        AttendanceRecord[] monthRecords = collectRecordsBetween(start, end);

        // find unique employee IDs in monthRecords
        int[] empIdsTmp = new int[monthRecords.length];
        int empIdCount = 0;
        for (int i = 0; i < monthRecords.length; i++) {
            int id = monthRecords[i].getEmployeeId();
            boolean found = false;
            for (int j = 0; j < empIdCount; j++) {
                if (empIdsTmp[j] == id) { found = true; break; }
            }
            if (!found) empIdsTmp[empIdCount++] = id;
        }

        String sb = "";
        sb += "Monthly Attendance Report for " + year + "-" + String.format("%02d", month) + System.lineSeparator();
        sb += "empId,empName,daysPresent,totalHours(approx),notes" + System.lineSeparator();

        String outCsv = "attendance-report-month-" + year + String.format("%02d", month) + ".csv";
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outCsv));
            writer.write("empId,empName,daysPresent,totalHours,notes");
            writer.newLine();

            for (int ei = 0; ei < empIdCount; ei++) {
                int empId = empIdsTmp[ei];

                int daysPresent = 0;
                double totalHours = 0.0;
                String notes = "";

                // iterate each date in month
                int daysInMonth = start.lengthOfMonth();
                for (int d = 1; d <= daysInMonth; d++) {
                    LocalDate cur = LocalDate.of(year, month, d);

                    // collect this employee's records for this date (scan monthRecords)
                    AttendanceRecord[] tmp = new AttendanceRecord[monthRecords.length];
                    int tc = 0;
                    for (int k = 0; k < monthRecords.length; k++) {
                        if (monthRecords[k].getEmployeeId() == empId &&
                            monthRecords[k].getTimestamp().toLocalDate().equals(cur)) {
                            tmp[tc++] = monthRecords[k];
                        }
                    }
                    if (tc == 0) continue;

                    daysPresent++;
                    AttendanceRecord[] dayList = new AttendanceRecord[tc];
                    System.arraycopy(tmp, 0, dayList, 0, tc);
                    sortRecordsByTimestamp(dayList);

                    LocalDateTime firstIn = null;
                    LocalDateTime lastOut = null;
                    for (int r = 0; r < dayList.length; r++) {
                        AttendanceRecord ar = dayList[r];
                        if (ar.isCheckIn() && firstIn == null) firstIn = ar.getTimestamp();
                        if (!ar.isCheckIn()) lastOut = ar.getTimestamp();
                    }

                    if (firstIn != null && lastOut != null && lastOut.isAfter(firstIn)) {
                        totalHours += Duration.between(firstIn, lastOut).toMinutes() / 60.0;
                    } else {
                        notes = "some days missing check-in/out";
                    }
                } // each day

                // pick name from any matching record
                String empName = "Emp-" + empId;
                for (int k = 0; k < monthRecords.length; k++) {
                    if (monthRecords[k].getEmployeeId() == empId) { empName = monthRecords[k].getEmployeeName(); break; }
                }

                sb += empId + "," + empName + "," + daysPresent + "," +
                      String.format("%.2f", totalHours) + "," + notes + System.lineSeparator();

                writer.write(empId + "," + escapeCsv(empName) + "," + daysPresent + "," +
                             String.format("%.2f", totalHours) + "," + notes);
                writer.newLine();
            }

            writer.flush();
        } catch (IOException e) {
            sb += "Failed to write monthly report CSV: " + e.getMessage() + System.lineSeparator();
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (IOException ex) { /* ignore */ }
            }
        }

        return sb;
    }

    // Basic CSV escaping helper for names in output
    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }
}

