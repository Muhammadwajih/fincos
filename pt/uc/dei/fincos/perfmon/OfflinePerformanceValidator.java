package pt.uc.dei.fincos.perfmon;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.data.CSVReader;
import pt.uc.dei.fincos.data.CSVWriter;


/**
 * Computes performance stats from Sink log files.
 *
 * @author Marcelo R.N. Mendes
 *
 */
public class OfflinePerformanceValidator {
    /** A flag to indicate if the computed performance stats must be saved to a FINCoS Perfmon log file.*/
    private boolean saveToFile;

    /** Used to write the computed stats to a FINCoS Perfmon log file. */
    private CSVWriter perfLogWriter;

    /** A flag to allow the user to interrupt the computation of performance stats. */
    private boolean keepProcessing = true;

    /** A flag that indicates that the Sink log file has been processed. */
    public boolean finished = true;

    /** Stores query stats over time. */
    private TreeSet<PerformanceStats> statsOverTime;

    /** The paths of the Sink files to be processed. */
    public String[] inputLogFilesPaths;

    /** One processor for each log file. */
    private LogProcessor[] processors;

    /** Total number of records processed (for all files). */
    public long totalProcessedCount = 0;

    /** Total processing time (in milliseconds). */
    public long processingTime;

    /** Current progress of log processing. */
    public double progress = 0;

    /** Index of the column in the log file which stores the timestamp of the record. */
    private static final int TIMESTAMP_FIELD = 0;

    /** Index of the column in the log file which stores the stream name of the record .*/
    private static final int STREAM_NAME_FIELD = 1;

    /** Number of lines of the header of the log file .*/
    private static final int SINK_LOG_HEADER_SIZE = 8;

    /**
     *
     * @param inputLogFilesPaths    Log files to be processed
     * @param saveToFile            Indicates if a Perfmon log file must be generated
     * @param outputLogFilePath     Path of the Perfmon log file to be generated
     * @throws Exception            If an I/O error occurs.
     */
    public OfflinePerformanceValidator(String[] inputLogFilesPaths, boolean saveToFile, String outputLogFilePath)
    throws Exception {
        this.inputLogFilesPaths = inputLogFilesPaths;
        statsOverTime = new TreeSet<PerformanceStats>();
        this.saveToFile = saveToFile;

        // Writes the header of output log file
        if (saveToFile) {
            perfLogWriter = new CSVWriter(outputLogFilePath, 10);
            perfLogWriter.writeRecord("FINCoS Performance Log File.\nGenerated from:");

            for (String logFilePath : inputLogFilesPaths) {
                try {
                    long t0 = OfflinePerformanceValidator.getLogStartTimeInMillis(logFilePath);
                    long t1 = OfflinePerformanceValidator.getLogEndTimeInMillis(logFilePath);
                    perfLogWriter.writeRecord(" \"" + logFilePath + "\" [" + new Date(t0)
                                            + "] to [" +  new Date(t1) + "]");
                } catch (NumberFormatException e) {
                    throw new Exception("Invalid log file: \"" + logFilePath + "\". Log processing halted.");
                }
            }
            perfLogWriter.writeRecord("log file(s).\n"
                                    + "Timestamp" + Globals.CSV_SEPARATOR
                                    + "Server" + Globals.CSV_SEPARATOR
                                    + "Stream" + Globals.CSV_SEPARATOR
                                    + "Avg_Throughput" + Globals.CSV_SEPARATOR
                                    + "Min_Throughput" + Globals.CSV_SEPARATOR
                                    + "Max_Throughput" + Globals.CSV_SEPARATOR
                                    + "Last_Throughput" + Globals.CSV_SEPARATOR
                                    + "Avg_RT" + Globals.CSV_SEPARATOR
                                    + "Min_RT" + Globals.CSV_SEPARATOR
                                    + "Max_RT" + Globals.CSV_SEPARATOR
                                    + "Stdev_RT" + Globals.CSV_SEPARATOR
                                    + "Last_RT");
        }
    }

    /**
     * Returns the list of stats in a per-second basis, sorted by timestamp.
     *
     * @param startTime     Starting point from which log files must be processed(in milliseconds)
     * @param endTime       Ending point until which log files must be processed(in milliseconds)
     * @return              Historical performance stats
     * @throws Exception    If an I/O error occurs
     */
    public TreeSet<PerformanceStats> processLogFiles(long startTime, long endTime) throws Exception {
        processors = new LogProcessor[inputLogFilesPaths.length];

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < inputLogFilesPaths.length; i++) {
            processors[i] = new LogProcessor(inputLogFilesPaths[i], startTime, endTime);
            processors[i].start();
        }

        // Waits for completion of all log-processor threads
        for (LogProcessor processor : processors) {
            processor.join();
        }

        // Prints processing stats
        for (LogProcessor processor : processors) {
            totalProcessedCount += processor.processedCount;
            processor = null;
        }
        processingTime = System.currentTimeMillis() - t0;

        if (saveToFile && perfLogWriter != null) {
            for (PerformanceStats stats : statsOverTime) {
                perfLogWriter.writeRecord(stats.toString());
            }

            perfLogWriter.closeFile();
        }

        processors = null;

        return this.statsOverTime;
    }

    /**
     * Interrups log file(s) processing.
     */
    public synchronized void stopProcessing() {
        this.keepProcessing = false;
        finished = false;
    }

    /**
     *  Retrieves the start time of a given log file.
     *
     * @param inputLogFile      The path of the log file
     * @return                  The start time of inputLogFile
     * @throws Exception        If an I/O error occurs
     */
    public static long getLogStartTimeInMillis(String inputLogFile) throws Exception {
        long startTime;
        RandomAccessFile randomFile = null;
        try {
            randomFile = new RandomAccessFile(inputLogFile, "r");
            // Ignores log header
            for (int i = 0; i < SINK_LOG_HEADER_SIZE; i++) {
                randomFile.readLine();
            }
            String firstLine = randomFile.readLine();
            if (firstLine == null) {
                throw new Exception("The log file is empty.");
            }
            startTime = Long.parseLong(CSVReader.split(firstLine, Globals.CSV_SEPARATOR)[TIMESTAMP_FIELD]);
        } catch (NumberFormatException e) {
            throw new Exception("Invalid log file: \"" + inputLogFile + "\".");
        } finally {
            try {
                randomFile.close();
            } catch (Exception e2) {
                System.err.println("Could not close log file \"" + inputLogFile + "\".");
            }
        }

        return startTime;
    }

    /**
     *  Retrieves the end time of a given log file.
     *
     * @param inputLogFile      The path of the log file
     * @return                  the end time of inputLogFile
     * @throws Exception        If an I/O error occurs
     */
    public static long getLogEndTimeInMillis(String inputLogFile) throws Exception {
        RandomAccessFile randomFile = null;
        long endTime;
        try {
            randomFile = new RandomAccessFile(inputLogFile, "r");
            // Looks for the last line at the end of log file
            long fileSize = randomFile.length();
            int c;
            int count = 0;
            do {
                randomFile.seek(fileSize - 2 - count);
                c = randomFile.read();
                count++;
            } while (c != '\n');

            String lastLine = randomFile.readLine();
            endTime = Long.parseLong(CSVReader.split(lastLine, Globals.CSV_SEPARATOR)[TIMESTAMP_FIELD]);
        } catch (NumberFormatException e) {
            throw new Exception("Invalid log file: \"" + inputLogFile + "\".");
        } finally {
            try {
                randomFile.close();
            } catch (Exception e2) {
                System.err.println("Could not close log file \"" + inputLogFile + "\".");
            }

        }
        return endTime;
    }

    /**
     *
     * @return  the progress of the log processing task (i.e. amount_of_work_done/total_amount_of_work)
     */
    public synchronized double getProgress() {
        if (processors != null) {
            progress = 0;
            for (int i = 0; i < processors.length; i++) {
                progress += processors[i].getProgress() / processors.length;
            }
        }

        return progress;
    }

    /**
     * Add stats to the historical series. If there is already a stats equal to that being added
     * (i.e. with the same combination of the attributes "server", "stream", and "timestamp"),
     * it is updated appropriately.
     *
     * @param newStats  The new performance stats to be added.
     */
    private void addHistoricStats(PerformanceStats newStats) {
        synchronized (statsOverTime) {
            // If already contain stats for this server/stream/timestamp, update it
            //(it might happen when processing multiple log files)
            if (statsOverTime.contains(newStats)) {
                PerformanceStats currentStats = statsOverTime.floor(newStats);
                statsOverTime.remove(currentStats);
                newStats.maxRT = Math.max(currentStats.maxRT, newStats.maxRT);
                newStats.minRT = Math.min(currentStats.minRT, newStats.minRT);
                newStats.lastEventCount += currentStats.lastEventCount;
                newStats.totalEventCount += currentStats.totalEventCount;
                newStats.totalRT += currentStats.totalRT;
                newStats.sumSqrRT += currentStats.sumSqrRT;
                newStats.refreshPeriodicStats();
            }

            statsOverTime.add(newStats);
        }
    }

    /**
     * Log processing task (executes as a separate thread).
     */
    class LogProcessor extends Thread {
        /** Reads a Sink log file. */
        CSVReader logReader;

        /** Keeps a snapshot of the performance stats. */
        HashMap<String, PerformanceStats> streamsStats;

        /** Starting point from which the log file must be processed(in milliseconds). */
        long startTime;

        /** Ending point until which the log file must be processed(in milliseconds).*/
        long endTime;

        /** Latency measurement mode used in the test during which the log file was generated. */
        int rtMeasurementMode;

        /** Resolution of latency measurement (either milliseconds or nanoseconds). */
        int rtResolution;

        /** Variables to keep track of the progress of this log processing task. */
        long processedCount = 0;
        long totalReadBytes = 0;
        long logFileSizeInBytes;

        public LogProcessor(String inputLogFilePath, long startTime, long endTime) throws IOException {
            this.startTime = startTime;
            this.endTime = endTime;
            logReader = new CSVReader(inputLogFilePath);
            logFileSizeInBytes = new java.io.File(inputLogFilePath).length();
            streamsStats = new HashMap<String, PerformanceStats>();
        }

        @Override
        public void run() {
            String event, streamName;
            long outputArrivalTime = 0, causerEmissionTime = 0, timestamp = 0;
            String[] splitEv;
            long lowerTS = startTime;
            PerformanceStats currentStreamStats, historicStats;
            try {
                long charsPerByte = (long) java.nio.charset.Charset.defaultCharset().newDecoder().averageCharsPerByte();

                // parses log header
                String connection;
                int streamType;
                String header = logReader.getNextLine();
                totalReadBytes += (header.length() / charsPerByte + 2);
                if (header != null && header.contains("FINCoS")) {
                    if (header.contains("Driver")) {
                        streamType = Stream.INPUT;
                    } else if (header.contains("Sink")) {
                        streamType = Stream.OUTPUT;
                    } else {
                        System.err.println("Invalid log file.");
                        return;
                    }
                } else {
                    System.err.println("Invalid log file.");
                    return;
                }
                // ignores next two lines of log header
                totalReadBytes += (logReader.getNextLine().length() / charsPerByte + 2); // Driver/Sink alias
                totalReadBytes += (logReader.getNextLine().length() / charsPerByte + 2); // Driver/Sink address

                // Connection alias
                connection = logReader.getNextLine();
                totalReadBytes += (connection.length() / charsPerByte + 2);
                if (connection != null) {
                    connection = connection.substring(connection.indexOf(":") + 2);
                }

                // ignores next line of log header
                totalReadBytes += (logReader.getNextLine().length() / charsPerByte + 2); // Log Start time

                // Determines Response time measurement mode of the test
                String rtModeStr = logReader.getNextLine().substring(connection.indexOf(":") + 2);
                totalReadBytes += (rtModeStr.length() / charsPerByte + 2);
                if (rtModeStr.contains("ADAPTER")) {
                    this.rtMeasurementMode = Globals.ADAPTER_RT;
                } else if (rtModeStr.contains("END_TO_END")) {
                    this.rtMeasurementMode = Globals.END_TO_END_RT;
                } else {
                    this.rtMeasurementMode = Globals.NO_RT;
                }

                // Determines Response time resolution of the test
                String rtResolutionStr = logReader.getNextLine().substring(connection.indexOf(":") + 2);
                totalReadBytes += (rtModeStr.length() / charsPerByte + 2);
                double rtFactor = 1.0;
                if (rtResolutionStr.contains("milliseconds")) {
                    this.rtResolution = Globals.MILLIS_RT;
                    rtFactor = 1.0;
                } else {
                    this.rtResolution = Globals.NANO_RT;
                    rtFactor = 1E6;
                }

                // Log sampling rate
                String logSamplingRateStr = logReader.getNextLine();
                totalReadBytes += (logSamplingRateStr.length() / charsPerByte + 2);
                if (logSamplingRateStr != null) {
                    logSamplingRateStr = logSamplingRateStr.substring(logSamplingRateStr.indexOf(":") + 2);
                }
                int logSamplingFactor = (int) Math.round(1 / Double.parseDouble(logSamplingRateStr));

                double rt;
                while (keepProcessing && (event = logReader.getNextLine()) != null) {
                    try {
                        totalReadBytes += (event.length() / charsPerByte + 2);
                        splitEv = CSVReader.split(event, Globals.CSV_SEPARATOR);
                        streamName = splitEv[STREAM_NAME_FIELD].substring(5);
                        timestamp = Long.parseLong(splitEv[TIMESTAMP_FIELD]);

                        if (timestamp < startTime) {
                            continue;
                        }

                        if (timestamp > endTime) {
                            break;
                        }

                        // For every new event, update stats of corresponding stream
                        currentStreamStats = streamsStats.get(streamName);
                        if (currentStreamStats == null) {
                            currentStreamStats = new PerformanceStats(connection, new Stream(streamType, streamName));
                            streamsStats.put(streamName, currentStreamStats);
                        }
                        currentStreamStats.lastEventCount += logSamplingFactor;
                        currentStreamStats.totalEventCount += logSamplingFactor;
                        if (rtMeasurementMode != Globals.NO_RT) {
                            outputArrivalTime = Long.parseLong(splitEv[splitEv.length - 1]);
                            causerEmissionTime = Long.parseLong(splitEv[splitEv.length - 2]);

                            rt = (outputArrivalTime - causerEmissionTime) / rtFactor;
                            if (rt >= 0) {
                                currentStreamStats.lastRT = rt;
                                currentStreamStats.minRT = Math.min(currentStreamStats.lastRT, currentStreamStats.minRT);
                                currentStreamStats.maxRT = Math.max(currentStreamStats.lastRT, currentStreamStats.maxRT);
                                currentStreamStats.totalRT += currentStreamStats.lastRT;
                                currentStreamStats.sumSqrRT += currentStreamStats.lastRT * currentStreamStats.lastRT;
                            } else {
                                System.err.println("Warning: Negative response time (" + rt + "). " +
                                "This may indicate problems with system(s) clock(s).");
                            }

                        } else {
                            currentStreamStats.lastRT = Double.NaN;
                            currentStreamStats.minRT = Double.NaN;
                            currentStreamStats.maxRT = Double.NaN;
                            currentStreamStats.totalRT = Double.NaN;
                            currentStreamStats.sumSqrRT = Double.NaN;
                        }


                        // Periodically updates 1-second-basis stats of ALL streams
                        if (timestamp >= (lowerTS + PerformanceStats.DEFAULT_TIME_BUCKET_IN_MILLIS)) {
                            for (Map.Entry<String, PerformanceStats> e : streamsStats.entrySet()) {
                                currentStreamStats = e.getValue();
                                // Advances time of the stats
                                currentStreamStats.elapsedTime += PerformanceStats.DEFAULT_TIME_BUCKET_IN_MILLIS;
                                // Recompute periodic stats
                                currentStreamStats.refreshPeriodicStats();
                                historicStats = (PerformanceStats) currentStreamStats.clone();
                                historicStats.timestamp = lowerTS + PerformanceStats.DEFAULT_TIME_BUCKET_IN_MILLIS;
                                addHistoricStats(historicStats);
                                currentStreamStats.lastEventCount = 0;
                            }
                            lowerTS += PerformanceStats.DEFAULT_TIME_BUCKET_IN_MILLIS;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid event format - " + event);
                    } catch (CloneNotSupportedException e) {
                        System.err.println("Error: " + e.getMessage());
                    }
                }

                for (PerformanceStats s : streamsStats.values()) {
                    processedCount += s.totalEventCount;
                }

                totalReadBytes -= 4;

                logReader.closeFile();
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }

        }
        public double getProgress() {
            return 1.0 * this.totalReadBytes / this.logFileSizeInBytes;
        }
    }
}
