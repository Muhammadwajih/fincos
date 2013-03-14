/* FINCoS Framework
 * Copyright (C) 2013 CISUC, University of Coimbra
 *
 * Licensed under the terms of The GNU General Public License, Version 2.
 * A copy of the License has been included with this distribution in the
 * fincos-license.txt file.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 */


package pt.uc.dei.fincos.perfmon;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.data.CSV_Reader;
import pt.uc.dei.fincos.data.CSV_Writer;


/**
 * Computes performance stats from Sink log files.
 *
 * @author  Marcelo R.N. Mendes
 *
 */
public final class OfflinePerformanceValidator {
    /** A flag to indicate if the computed performance stats must be saved
     *  to a FINCoS Perfmon log file.*/
    private boolean saveToFile;

    /** Used to write the computed stats to a FINCoS Perfmon log file. */
    private CSV_Writer perfLogWriter;

    /** A flag to allow the user to interrupt the computation of performance stats. */
    private boolean keepProcessing = true;

    /** A flag that indicates that the Sink log file has been processed. */
    private boolean finished = true;

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
    public OfflinePerformanceValidator(String[] inputLogFilesPaths, boolean saveToFile,
            String outputLogFilePath) throws Exception {
        this.inputLogFilesPaths = inputLogFilesPaths;
        statsOverTime = new TreeSet<PerformanceStats>();
        this.saveToFile = saveToFile;

        // Writes the header of output log file
        if (saveToFile) {
            perfLogWriter = new CSV_Writer(outputLogFilePath, 10);
            perfLogWriter.writeRecord("FINCoS Performance Log File.\nGenerated from:");

            for (String logFilePath : inputLogFilesPaths) {
                try {
                    long t0 = OfflinePerformanceValidator.getLogStartTimeInMillis(logFilePath);
                    long t1 = OfflinePerformanceValidator.getLogEndTimeInMillis(logFilePath);
                    perfLogWriter.writeRecord(" \"" + logFilePath + "\" [" + new Date(t0)
                            + "] to [" + new Date(t1) + "]");
                } catch (NumberFormatException e) {
                    throw new Exception("Invalid log file: \"" + logFilePath
                                      + "\". Log processing halted.");
                }
            }
            perfLogWriter.writeRecord("log file(s).\n"
                    + "Timestamp" + Globals.CSV_DELIMITER
                    + "Server" + Globals.CSV_DELIMITER
                    + "Stream" + Globals.CSV_DELIMITER
                    + "Avg_Throughput" + Globals.CSV_DELIMITER
                    + "Min_Throughput" + Globals.CSV_DELIMITER
                    + "Max_Throughput" + Globals.CSV_DELIMITER
                    + "Last_Throughput" + Globals.CSV_DELIMITER
                    + "Avg_RT" + Globals.CSV_DELIMITER
                    + "Min_RT" + Globals.CSV_DELIMITER
                    + "Max_RT" + Globals.CSV_DELIMITER
                    + "Stdev_RT" + Globals.CSV_DELIMITER
                    + "Last_RT");
        }
    }

    /**
     * Returns the list of stats in a per-second basis, sorted by timestamp.
     *
     * @param startTime     Starting point from which log files must be processed
     *                      (in milliseconds)
     * @param endTime       Ending point until which log files must be processed
     *                      (in milliseconds)
     *
     * @return              Historical performance stats
     *
     * @throws Exception    If an I/O error occurs
     */
    public TreeSet<PerformanceStats> processLogFiles(long startTime, long endTime)
    throws Exception {
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
        this.finished = false;
    }

    /**
     * Retrieves the start time of a given log file.
     *
     * @param inputLogFile  The path of the log file
     * @return              The start time of inputLogFile
     * @throws Exception    If an I/O error occurs
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
            startTime = Long.parseLong(CSV_Reader.split(firstLine,
                            Globals.CSV_DELIMITER)[TIMESTAMP_FIELD]);
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
     * Retrieves the end time of a given log file.
     *
     * @param inputLogFile The path of the log file
     * @return the end time of inputLogFile
     * @throws Exception If an I/O error occurs
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
            endTime = Long.parseLong(CSV_Reader.split(lastLine,
                    Globals.CSV_DELIMITER)[TIMESTAMP_FIELD]);
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
     * @return the progress of the log processing task (i.e.
     * amount_of_work_done/total_amount_of_work)
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
     * Add stats to the historical series. If there is already a stats equal to
     * that being added (i.e. with the same combination of the attributes
     * "server", "stream", and "timestamp"), it is updated appropriately.
     *
     * @param newStats The new performance stats to be added.
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
     *
     * @return  <tt>true</tt> if the log processing has finished,
     *          <tt>false</tt> otherwise
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Log processing task (executes as a separate thread).
     */
    class LogProcessor extends Thread {

        /**
         * Reads a Sink log file.
         */
        CSV_Reader logReader;
        /**
         * Keeps a snapshot of the performance stats.
         */
        HashMap<String, PerformanceStats> streamsStats;
        /**
         * Starting point from which the log file must be processed
         * (in milliseconds).
         */
        long startTime;
        /**
         * Ending point until which the log file must be processed
         * (in milliseconds).
         */
        long endTime;
        /**
         * Latency measurement mode used in the test during which the log file
         * was generated.
         */
        int rtMeasurementMode;
        /**
         * Resolution of latency measurement (either milliseconds or
         * nanoseconds).
         */
        int rtResolution;
        /**
         * Variables to keep track of the progress of this log processing task.
         */
        long processedCount = 0;
        long totalReadBytes = 0;
        long logFileSizeInBytes;

        /**
         *
         *
         * @param inputLogFilePath  path for the log file to be processed
         * @param startTime         start of measurement interval
         * @param endTime           end of measurement interval
         * @throws IOException      if an error occurs while opening the log file
         */
        public LogProcessor(String inputLogFilePath, long startTime, long endTime)
        throws IOException {
            this.startTime = startTime;
            this.endTime = endTime;
            logReader = new CSV_Reader(inputLogFilePath);
            logFileSizeInBytes = new java.io.File(inputLogFilePath).length();
            streamsStats = new HashMap<String, PerformanceStats>();
        }

        @Override
        public void run() {
            String event, streamName;
            long outputArrivalTime = 0, causerEmissionTime = 0, timestamp = 0;
            String[] splitEv;
            long lowerTS = startTime;
            PerformanceStats streamStats, historicStats;
            try {
                long charsPerByte = (long) java.nio.charset.Charset.defaultCharset().
                                           newDecoder().averageCharsPerByte();

                // parses log header
                String connection;
                int streamType;
                String header = logReader.getNextLine();
                totalReadBytes += (header.length() / charsPerByte + 2);
                if (header.contains("FINCoS")) {
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

                // Ignores log header (Driver/Sink alias)
                totalReadBytes += (logReader.getNextLine().length() / charsPerByte + 2);

                // Ignores log header (Driver/Sink address)
                totalReadBytes += (logReader.getNextLine().length() / charsPerByte + 2);

                // Connection alias
                connection = logReader.getNextLine();
                totalReadBytes += (connection.length() / charsPerByte + 2);
                connection = connection.substring(connection.indexOf(":") + 2);

                // ignores next line of log header (Log Start time)
                totalReadBytes += (logReader.getNextLine().length() / charsPerByte + 2);

                // Determines Response time measurement mode of the test
                String rtModeStr = logReader.getNextLine();
                totalReadBytes += (rtModeStr.length() / charsPerByte + 2);
                if (rtModeStr.contains("ADAPTER")) {
                    this.rtMeasurementMode = Globals.ADAPTER_RT;
                } else if (rtModeStr.contains("END_TO_END")) {
                    this.rtMeasurementMode = Globals.END_TO_END_RT;
                } else {
                    this.rtMeasurementMode = Globals.NO_RT;
                }

                // Determines Response time resolution of the test
                String rtResolutionStr = logReader.getNextLine();
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
                String logSamplRateStr = logReader.getNextLine();
                totalReadBytes += (logSamplRateStr.length() / charsPerByte + 2);
                logSamplRateStr = logSamplRateStr.substring(logSamplRateStr.indexOf(":") + 2);
                int logSamplingFactor =
                        (int) Math.round(1 / Double.parseDouble(logSamplRateStr));

                double rt;
                while (keepProcessing && (event = logReader.getNextLine()) != null) {
                    try {
                        totalReadBytes += (event.length() / charsPerByte + 2);
                        splitEv = CSV_Reader.split(event, Globals.CSV_DELIMITER);
                        streamName = splitEv[STREAM_NAME_FIELD];
                        timestamp = Long.parseLong(splitEv[TIMESTAMP_FIELD]);

                        if (timestamp < startTime) {
                            continue;
                        }

                        if (timestamp > endTime) {
                            break;
                        }

                        // For every new event, update stats of corresponding stream
                        streamStats = streamsStats.get(streamName);
                        if (streamStats == null) {
                            Stream s = new Stream(streamType, streamName);
                            streamStats = new PerformanceStats(connection, s);
                            streamsStats.put(streamName, streamStats);
                        }
                        streamStats.lastEventCount += logSamplingFactor;
                        streamStats.totalEventCount += logSamplingFactor;
                        if (rtMeasurementMode != Globals.NO_RT) {
                            outputArrivalTime = Long.parseLong(splitEv[splitEv.length - 1]);
                            causerEmissionTime = Long.parseLong(splitEv[splitEv.length - 2]);

                            rt = (outputArrivalTime - causerEmissionTime) / rtFactor;
                            if (rt >= 0) {
                                streamStats.lastRT = rt;
                                streamStats.minRT = Math.min(streamStats.lastRT, streamStats.minRT);
                                streamStats.maxRT = Math.max(streamStats.lastRT, streamStats.maxRT);
                                streamStats.totalRT += streamStats.lastRT;
                                streamStats.sumSqrRT += streamStats.lastRT * streamStats.lastRT;
                            } else {
                                System.err.println("Warning: Negative response time (" + rt + "). "
                                        + "This may indicate problems with system(s) clock(s).");
                            }

                        } else {
                            streamStats.lastRT = Double.NaN;
                            streamStats.minRT = Double.NaN;
                            streamStats.maxRT = Double.NaN;
                            streamStats.totalRT = Double.NaN;
                            streamStats.sumSqrRT = Double.NaN;
                        }


                        // Periodically updates 1-second-basis stats of ALL streams
                        if (timestamp >= (lowerTS + PerformanceStats.DEFAULT_TIME_BUCKET_IN_MILLIS)) {
                            for (Map.Entry<String, PerformanceStats> e : streamsStats.entrySet()) {
                                streamStats = e.getValue();
                                // Advances time of the stats
                                streamStats.elapsedTime +=
                                        PerformanceStats.DEFAULT_TIME_BUCKET_IN_MILLIS;
                                // Recompute periodic stats
                                streamStats.refreshPeriodicStats();
                                historicStats = (PerformanceStats) streamStats.clone();
                                historicStats.timestamp = lowerTS
                                        + PerformanceStats.DEFAULT_TIME_BUCKET_IN_MILLIS;
                                addHistoricStats(historicStats);
                                streamStats.lastEventCount = 0;
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

        /**
         *
         * @return  the progress of the performance validation task
         */
        public double getProgress() {
            return 1.0 * this.totalReadBytes / this.logFileSizeInBytes;
        }
    }
}
