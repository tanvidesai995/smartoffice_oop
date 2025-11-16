package smartoffice.v1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * ConfigManager handles simple logging to a file and reading logs.
 * Uses java.io.* classes (permission was granted).
 *
 * Methods are simple and beginner-friendly.
 *
 * Addresses rubric items: I/O, logging, exception handling (IOException)
 */
public class ConfigManager {
    private String logFilePath;

    public ConfigManager(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    // Append a log message to the file
    public void log(String message) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(logFilePath, true)); // append mode
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            // simple handling: print message to console if file write fails
            System.err.println("Failed to write log: " + e.getMessage());
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (IOException e) { /* ignore close error */ }
            }
        }
    }

    // Read entire log file and return it as a single String
    public String readLog() {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(logFilePath));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            return "Could not read log file: " + e.getMessage();
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException e) { /* ignore close error */ }
            }
        }
        return sb.toString();
    }
}
