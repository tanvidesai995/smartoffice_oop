package smartoffice.v1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AttendanceManager: manages attendance records in memory and persists to CSV.
 *
 * Responsibilities:
 * - simulate RFID scan (stub) to record check-in/check-out
 * - append attendance records to attendance.csv
 * - load records from attendance.csv
 * - generate daily, weekly, and monthly reports (console + CSV)
 *
 * Design is intentionally simple and beginner-friendly.
 *
 * Facial recognition removed as requested.
 */
public class AttendanceManager {
    private String attendanceCsvPath;
    private List<AttendanceRecord> records;

    public AttendanceManager(String attendanceCsvPath) {
        this.attendanceCsvPath = attendanceCsvPath;
        this.records = new ArrayList<AttendanceRecord>();
        loadFromCsv(); // load existing records if file present
    }

    // Record a new attendance event and persist to CSV
    public void recordAttendance(AttendanceRecord rec) {
        records.add(rec);
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
                if (r != null) records.add(r);
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

    // Get records for a specific LocalDate (based on timestamp's date)
    public List<AttendanceRecord> getRecordsForDate(LocalDate date) {
        List<AttendanceRecord> res = new ArrayList<AttendanceRecord>();
        for (AttendanceRecord r : records) {
            if (r.getTimestamp().toLocalDate().equals(date)) {
                res.add(r);
            }
        }
        return res;
    }

    // Get records between dates inclusive
    public List<AttendanceRecord> getRecordsBetween(LocalDate start, LocalDate end) {
        List<AttendanceRecord> res = new ArrayList<AttendanceRecord>();
        for (AttendanceRecord r : records) {
            LocalDate d = r.getTimestamp().toLocalDate();
            if ((d.isEqual(start) || d.isAfter(start)) && (d.isEqual(end) || d.isBefore(end))) {
                res.add(r);
            }
        }
        return res;
    }

    // Generate daily report: for each employee present on that day, show first check-in and last check-out and duration if available
    // Returns formatted string and writes CSV to "attendance-report-YYYYMMDD.csv"
    public String generateReportDaily(java.time.LocalDate date) {
        List<AttendanceRecord> dayRecords = getRecordsForDate(date);
        // Group by employee id
        Map<Integer, List<AttendanceRecord>> byEmp = new HashMap<Integer, List<AttendanceRecord>>();
        for (AttendanceRecord r : dayRecords) {
            int id = r.getEmployeeId();
            if (!byEmp.containsKey(id)) byEmp.put(id, new ArrayList<AttendanceRecord>());
            byEmp.get(id).add(r);
        }

        StringBuilder sb = new StringBuilder();
        String header = "Daily Attendance Report for " + date.toString();
        sb.append(header).append(System.lineSeparator());
        sb.append("empId,empName,firstCheckIn,lastCheckOut,totalHours (approx),notes").append(System.lineSeparator());

        String outCsv = "attendance-report-" + date.format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outCsv));
            writer.write("empId,empName,firstCheckIn,lastCheckOut,totalHours,notes");
            writer.newLine();

            for (Integer empId : byEmp.keySet()) {
                List<AttendanceRecord> list = byEmp.get(empId);
                // sort by timestamp
                Collections.sort(list, new Comparator<AttendanceRecord>() {
                    public int compare(AttendanceRecord a, AttendanceRecord b) {
                        return a.getTimestamp().compareTo(b.getTimestamp());
                    }
                });

                java.time.LocalDateTime firstIn = null;
                java.time.LocalDateTime lastOut = null;
                for (AttendanceRecord ar : list) {
                    if (ar.isCheckIn() && firstIn == null) firstIn = ar.getTimestamp();
                    if (!ar.isCheckIn()) lastOut = ar.getTimestamp();
                }
                String notes = "";
                double hours = 0.0;
                if (firstIn != null && lastOut != null && lastOut.isAfter(firstIn)) {
                    java.time.Duration dur = java.time.Duration.between(firstIn, lastOut);
                    hours = dur.toMinutes() / 60.0;
                } else {
                    notes = "missing check-in or check-out";
                }
                String empName = list.get(0).getEmployeeName();
                String firstStr = (firstIn == null) ? "" : firstIn.format(AttendanceRecord.FORMATTER);
                String lastStr = (lastOut == null) ? "" : lastOut.format(AttendanceRecord.FORMATTER);

                sb.append(empId).append(",").append(empName).append(",").append(firstStr).append(",").append(lastStr)
                        .append(",").append(String.format("%.2f", hours)).append(",").append(notes).append(System.lineSeparator());

                // write to CSV
                writer.write(empId + "," + escapeCsv(empName) + "," + firstStr + "," + lastStr + "," + String.format("%.2f", hours) + "," + notes);
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            sb.append("Failed to write daily report CSV: ").append(e.getMessage()).append(System.lineSeparator());
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (IOException ex) { /* ignore */ }
            }
        }

        return sb.toString();
    }

    // Generate weekly report for the week that contains the given date (week starts on Monday)
    public String generateReportWeekly(java.time.LocalDate anyDateInWeek) {
        java.time.LocalDate monday = anyDateInWeek.with(java.time.DayOfWeek.MONDAY);
        java.time.LocalDate sunday = monday.plusDays(6);
        List<AttendanceRecord> weekRecords = getRecordsBetween(monday, sunday);
        // Group by employee id
        Map<Integer, List<AttendanceRecord>> byEmp = new HashMap<Integer, List<AttendanceRecord>>();
        for (AttendanceRecord r : weekRecords) {
            int id = r.getEmployeeId();
            if (!byEmp.containsKey(id)) byEmp.put(id, new ArrayList<AttendanceRecord>());
            byEmp.get(id).add(r);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Weekly Attendance Report for ").append(monday.toString()).append(" to ").append(sunday.toString()).append(System.lineSeparator());
        sb.append("empId,empName,daysPresent,totalHours(approx),notes").append(System.lineSeparator());

        String outCsv = "attendance-report-week-" + monday.format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outCsv));
            writer.write("empId,empName,daysPresent,totalHours,notes");
            writer.newLine();

            for (Integer empId : byEmp.keySet()) {
                List<AttendanceRecord> list = byEmp.get(empId);
                // Group by date for this employee
                Map<java.time.LocalDate, List<AttendanceRecord>> byDate = new HashMap<java.time.LocalDate, List<AttendanceRecord>>();
                for (AttendanceRecord ar : list) {
                    java.time.LocalDate d = ar.getTimestamp().toLocalDate();
                    if (!byDate.containsKey(d)) byDate.put(d, new ArrayList<AttendanceRecord>());
                    byDate.get(d).add(ar);
                }
                int daysPresent = byDate.size();
                double totalHours = 0.0;
                String notes = "";
                for (java.time.LocalDate d : byDate.keySet()) {
                    List<AttendanceRecord> dayList = byDate.get(d);
                    Collections.sort(dayList, new Comparator<AttendanceRecord>() {
                        public int compare(AttendanceRecord a, AttendanceRecord b) {
                            return a.getTimestamp().compareTo(b.getTimestamp());
                        }
                    });
                    java.time.LocalDateTime firstIn = null;
                    java.time.LocalDateTime lastOut = null;
                    for (AttendanceRecord ar : dayList) {
                        if (ar.isCheckIn() && firstIn == null) firstIn = ar.getTimestamp();
                        if (!ar.isCheckIn()) lastOut = ar.getTimestamp();
                    }
                    if (firstIn != null && lastOut != null && lastOut.isAfter(firstIn)) {
                        totalHours += java.time.Duration.between(firstIn, lastOut).toMinutes() / 60.0;
                    } else {
                        notes = "some days missing check-in/out";
                    }
                }
                String empName = list.get(0).getEmployeeName();
                sb.append(empId).append(",").append(empName).append(",").append(daysPresent).append(",").append(String.format("%.2f", totalHours)).append(",").append(notes).append(System.lineSeparator());
                writer.write(empId + "," + escapeCsv(empName) + "," + daysPresent + "," + String.format("%.2f", totalHours) + "," + notes);
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            sb.append("Failed to write weekly report CSV: ").append(e.getMessage()).append(System.lineSeparator());
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (IOException ex) { /* ignore */ }
            }
        }

        return sb.toString();
    }

    // Generate monthly report given year and month (1-12)
    public String generateReportMonthly(int year, int month) {
        java.time.LocalDate start = java.time.LocalDate.of(year, month, 1);
        java.time.LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<AttendanceRecord> monthRecords = getRecordsBetween(start, end);
        Map<Integer, List<AttendanceRecord>> byEmp = new HashMap<Integer, List<AttendanceRecord>>();
        for (AttendanceRecord r : monthRecords) {
            int id = r.getEmployeeId();
            if (!byEmp.containsKey(id)) byEmp.put(id, new ArrayList<AttendanceRecord>());
            byEmp.get(id).add(r);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Monthly Attendance Report for ").append(year).append("-").append(String.format("%02d", month)).append(System.lineSeparator());
        sb.append("empId,empName,daysPresent,totalHours(approx),notes").append(System.lineSeparator());

        String outCsv = "attendance-report-month-" + year + String.format("%02d", month) + ".csv";
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outCsv));
            writer.write("empId,empName,daysPresent,totalHours,notes");
            writer.newLine();

            for (Integer empId : byEmp.keySet()) {
                List<AttendanceRecord> list = byEmp.get(empId);
                // Group by date for this employee
                Map<java.time.LocalDate, List<AttendanceRecord>> byDate = new HashMap<java.time.LocalDate, List<AttendanceRecord>>();
                for (AttendanceRecord ar : list) {
                    java.time.LocalDate d = ar.getTimestamp().toLocalDate();
                    if (!byDate.containsKey(d)) byDate.put(d, new ArrayList<AttendanceRecord>());
                    byDate.get(d).add(ar);
                }
                int daysPresent = byDate.size();
                double totalHours = 0.0;
                String notes = "";
                for (java.time.LocalDate d : byDate.keySet()) {
                    List<AttendanceRecord> dayList = byDate.get(d);
                    Collections.sort(dayList, new Comparator<AttendanceRecord>() {
                        public int compare(AttendanceRecord a, AttendanceRecord b) {
                            return a.getTimestamp().compareTo(b.getTimestamp());
                        }
                    });
                    java.time.LocalDateTime firstIn = null;
                    java.time.LocalDateTime lastOut = null;
                    for (AttendanceRecord ar : dayList) {
                        if (ar.isCheckIn() && firstIn == null) firstIn = ar.getTimestamp();
                        if (!ar.isCheckIn()) lastOut = ar.getTimestamp();
                    }
                    if (firstIn != null && lastOut != null && lastOut.isAfter(firstIn)) {
                        totalHours += java.time.Duration.between(firstIn, lastOut).toMinutes() / 60.0;
                    } else {
                        notes = "some days missing check-in/out";
                    }
                }
                String empName = list.get(0).getEmployeeName();
                sb.append(empId).append(",").append(empName).append(",").append(daysPresent).append(",").append(String.format("%.2f", totalHours)).append(",").append(notes).append(System.lineSeparator());
                writer.write(empId + "," + escapeCsv(empName) + "," + daysPresent + "," + String.format("%.2f", totalHours) + "," + notes);
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            sb.append("Failed to write monthly report CSV: ").append(e.getMessage()).append(System.lineSeparator());
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (IOException ex) { /* ignore */ }
            }
        }

        return sb.toString();
    }

    // Basic CSV escaping helper for names in output
    private String escapeCsv(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    // Simple in-memory access for other modules
    public List<AttendanceRecord> getAllRecords() {
        return new ArrayList<AttendanceRecord>(records);
    }
}
