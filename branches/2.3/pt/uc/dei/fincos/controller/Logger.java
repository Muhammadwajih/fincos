package pt.uc.dei.fincos.controller;

import java.io.IOException;

import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.data.CSVReader;
import pt.uc.dei.fincos.data.CSVWriter;

/**
 * Logs to disk the events sent and received by Drivers and Sinks.
 *
 * @author Marcelo R.N. Mendes
 */
public class Logger extends CSVWriter {
    /** Log sampling fraction. */
    private int logSamplMod;

    /** Which fields are to be logged by this logger (either all fields or just timestamps). */
    private int fieldsToLog;

    /** Number of log requests. */
    private int entriesCount = 0;

    /**
     * Default constructor. Logs all events and all fields.
     *
     * @param path				Path of log file
     * @param logFileHeader		A header for the log file
     * @param flushInterval		Interval between log flushes (writes to disk)
     * @throws IOException
     */
    public Logger(String path, String logFileHeader, int flushInterval) throws IOException {
        super(path, flushInterval);
        writeRecord(logFileHeader);
        this.logSamplMod = 1; // Logs all entries
        this.setFieldsToLog(Globals.LOG_ALL_FIELDS);
    }

    /**
     * Constructor.
     *
     * @param path				Path of log file
     * @param logFileHeader		A header for the log file
     * @param flushInterval		Interval between log flushes (writes to disk)
     * @param logSamplingRate	The fraction of events that must be logged to disk
     * @param fieldsToLog		Either LOG_ALL_FIELDS or LOG_ONLY_TIMESTAMPS
     * @throws IOException
     */
    public Logger(String path, String logFileHeader, int flushInterval,
            double logSamplingRate, int fieldsToLog) throws IOException {
        super(path, flushInterval);
        writeRecord(logFileHeader);
        this.setLogSamplingRate(logSamplingRate);
        this.setFieldsToLog(fieldsToLog);
    }

    /**
     *
     * @param fieldsToLog	Either LOG_ALL_FIELDS or LOG_ONLY_TIMESTAMPS. If receives a different
     * 						value, sets to default LOG_ALL_FIELDS.
     */
    public void setFieldsToLog(int fieldsToLog) {
        if (fieldsToLog == Globals.LOG_ALL_FIELDS || fieldsToLog == Globals.LOG_ONLY_TIMESTAMPS) {
            this.fieldsToLog = fieldsToLog;
        } else {
            System.err.println("WARNING: Invalid logging mode. Setting to default LOG_ALL_FIELDS");
            this.fieldsToLog = Globals.LOG_ALL_FIELDS;
        }
    }

    /**
     *
     * @return  which fields are to be logged by this logger (either all fields or just timestamps)
     */
    public int getFieldsToLog() {
        return fieldsToLog;
    }

    /**
     *
     * @param logSamplingRate 	The fraction of events that must be logged to disk.
     * 							It must be less than or equal to one. If it is greater	than one,
     * 							disables sampling (sets to one).
     */
    public void setLogSamplingRate(double logSamplingRate) {
        if (logSamplingRate <= 1.0) {
            this.logSamplMod = (int) (1 / logSamplingRate);
        } else {
            System.err.println("WARNING: Invalid logging sampling rate. Setting to default (no sampling)");
            this.logSamplMod = 1; // log all entries
        }
    }

    /**
     *
     * @return  the inverse of the configured log sampling ratio (1 / logSamplingRate)
     */
    public int getLogSamplMod() {
        return logSamplMod;
    }

    /**
     * Logs an event.
     *
     * @param evt           The event to be logged
     * @param timestamp     The timestamp of the entry (in milliseconds, using a valid clock time)
     * @throws IOException
     */
    public void log(Event evt, long timestamp) throws IOException {
        synchronized (this) {
            this.entriesCount++;
        }

        if (entriesCount % logSamplMod == 0) {
            String entry = evt.toCSV();
            if (fieldsToLog == Globals.LOG_ALL_FIELDS) {
                this.writeRecord(timestamp + Globals.CSV_SEPARATOR + entry);
            } else if (fieldsToLog == Globals.LOG_ONLY_TIMESTAMPS) {
                this.writeRecord(timestamp + Globals.CSV_SEPARATOR + CSVReader.split(entry, Globals.CSV_SEPARATOR)[0]);
            }
        }
    }

    /**
     * Logs an event.
     *
     * @param evt           The event to be logged
     * @throws IOException
     */
    public void log(Event evt) throws IOException {
        synchronized (this) {
            this.entriesCount++;
        }

        if (entriesCount % logSamplMod == 0) {
            String entry = evt.toCSV();
            if (fieldsToLog == Globals.LOG_ALL_FIELDS) {
                this.writeRecord(System.currentTimeMillis() + Globals.CSV_SEPARATOR + entry);
            } else if (fieldsToLog == Globals.LOG_ONLY_TIMESTAMPS) {
                this.writeRecord(System.currentTimeMillis() + Globals.CSV_SEPARATOR + CSVReader.split(entry, Globals.CSV_SEPARATOR)[0]);
            }
        }
    }

    /**
     * Flushes log entries to disk and closes log file.
     *
     * @throws IOException
     */
    public void close()  {
        try {
            super.closeFile();
        } catch (IOException e) {
            System.err.println("Could not close log file (" +e.getMessage()+")");
            e.printStackTrace();
        }
    }
}
