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


package pt.uc.dei.fincos.driver;

import java.io.IOException;

import pt.uc.dei.fincos.adapters.InputAdapter;
import pt.uc.dei.fincos.basic.CSV_Event;
import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.basic.Status;
import pt.uc.dei.fincos.basic.Step;
import pt.uc.dei.fincos.controller.Logger;
import pt.uc.dei.fincos.data.DataFileReader;
import pt.uc.dei.fincos.perfmon.DriverPerfStats;


/**
 * Sends events to the system under test.
 *
 * @author  Marcelo R.N. Mendes
 * @see Driver
 *
 */
public final class Sender extends Thread {

    /** Interface with the system to where events must be sent
     * (i.e., a CEP engine or a JMS provider). */
    private InputAdapter adapter;

    /** How latency is computed: end-to-end (from Drivers to Sink) or inside adapters. */
    private final int rtMode;

    /** Resolution of the timestamps: milliseconds or nanoseconds. */
    private final int rtResolution;

    /** Use events' *scheduled time* instead of *sending time* for response time measurement. */
    private final boolean useScheduledTime;

    /** The time unit of the timestamps in the external data file (if used). */
    private int timestampUnit;

    /** Number of times the external data file must be read/submitted. */
    private final int fileRepeatCount;

    /** Schedules event creation and submission on synthetic workloads. */
    private final Scheduler scheduler;

    /** Generates input data on synthetic workloads. */
    private DataGen datagen;

    /** Reads input data from a external file (if used). */
    private DataFileReader dataFileReader;

    /** Saves submitted events to disk. */
    private Logger logger;

    /** Highest resolution for Thread.sleep() method, in nanoseconds. Configured value: 1ms. */
    private static final long SLEEP_TIME_RESOLUTION = (long) 1E6;

    /** Current status of this thread. */
    private Status status;

    /** Number of events sent so far. */
    private long sentEventCount = 0;

    /** Time that the Thread was paused since last pause, in nanoseconds. */
    private long timeInPause = 0;

    /** A multiplication factor used to increase or decrease the rate
     *  at which events are submitted. */
    private double factor = 1.0;

    /** Indicates if online performance monitoring is enabled. */
    private boolean perfTracingEnabled;

    /** Collected performance metrics. */
    private DriverPerfStats perfStats;

    /**
     * Constructor #1: event submission is controlled by the Scheduler passed as argument;
     *                 events' data is generated in runtime.
     *
     * @param adapter               Interface with the system to where events must be sent
     *                              (i.e., a CEP engine or a JMS provider)
     * @param scheduler             Schedules event submission according to a given rate
     * @param dataGen               Events' data generator
     * @param group                 A thread group
     * @param id                    A thread ID
     * @param loopCount             Number of repetitions of the workload
     * @param rtMode                response time measurement mode
     *                              (either END-TO-END or ADAPTER)
     * @param rtResolution          response time measurement resolution
     *                              (either Milliseconds or Nanoseconds)
     * @param useScheduleTime       use events' *scheduled time* instead of
     *                              *sending time* for response time measurement.
     * @param perfTracingEnabled    indicates if online performance monitoring is enabled
     */
    public Sender(InputAdapter adapter, Scheduler scheduler, DataGen dataGen,
            ThreadGroup group, String id, int loopCount, int rtMode, int rtResolution,
            boolean useScheduleTime, boolean perfTracingEnabled) {
        super(group, id);
        this.adapter = adapter;
        this.scheduler = scheduler;
        this.datagen = dataGen;
        this.status = new Status(Step.STOPPED, 0);
        this.fileRepeatCount = loopCount;
        this.useScheduledTime = useScheduleTime;
        if (useScheduleTime) {
            this.rtMode = Globals.END_TO_END_RT;
            this.rtResolution = Globals.MILLIS_RT;
        } else {
            this.rtMode = rtMode;
            this.rtResolution = rtResolution;
        }

        this.perfTracingEnabled = perfTracingEnabled;
        this.perfStats = new DriverPerfStats();
    }

    /**
     * Constructor #2: event submission is controlled by the Scheduler passed as argument;
     *                 events' data is loaded from data file.
     *
     * @param adapter               Interface with the system to where events must be sent
     *                              (i.e., a CEP engine or a JMS provider)
     * @param scheduler             Schedules event submission according to a given rate
     * @param dataReader            Reads data sets from disk
     * @param containsTimestamps    Indicates if the events in the data file are
     *                              associated to timestamps
     * @param group                 A thread group
     * @param id                    A thread ID
     * @param loopCount             Number of repetitions of the workload
     * @param rtMode                response time measurement mode
     *                              (either END-TO-END or ADAPTER)
     * @param rtResolution          response time measurement resolution
     *                              (either Milliseconds or Nanoseconds)
     * @param useScheduleTime       use events' *scheduled time* instead of
     *                              *sending time* for response time measurement.
     * @param perfTracingEnabled    indicates if online performance monitoring is enabled
     */
    public Sender(InputAdapter adapter, Scheduler scheduler, DataFileReader dataReader,
            boolean containsTimestamps, ThreadGroup group, String id,
            int loopCount, int rtMode, int rtResolution, boolean useScheduleTime,
            boolean perfTracingEnabled) {
        super(group, id);
        this.adapter = adapter;
        this.scheduler = scheduler;
        this.dataFileReader = dataReader;
        this.status = new Status(Step.STOPPED, 0);
        this.fileRepeatCount = loopCount;
        this.useScheduledTime = useScheduleTime;
        if (useScheduleTime) {
            this.rtMode = Globals.END_TO_END_RT;
            this.rtResolution = Globals.MILLIS_RT;
        } else {
            this.rtMode = rtMode;
            this.rtResolution = rtResolution;
        }
        this.perfTracingEnabled = perfTracingEnabled;
        this.perfStats = new DriverPerfStats();
    }

    /**
     * Constructor #3: event submission is controlled by timestamps in a datafile.
     *
     * @param adapter               Interface with the system to where events must be sent
     *                              (i.e., a CEP engine or a JMS provider)
     * @param dataReader            Reads events from a data file at disk
     * @param timestampUnit         Time Unit of timestamps in data file
     * @param loopCount             Number of repetitions of the workload
     * @param rtMode                response time measurement mode
     *                              (either END-TO-END or ADAPTER)
     * @param rtResolution          response time measurement resolution
     *                              (either Milliseconds or Nanoseconds)
     * @param useScheduleTime       use events' *scheduled time* instead of
     *                              *sending time* for response time measurement.
     * @param perfTracingEnabled    indicates if online performance monitoring is enabled
     */
    public Sender(InputAdapter adapter, DataFileReader dataReader,
            int timestampUnit, int loopCount, int rtMode, int rtResolution,
            boolean useScheduleTime, boolean perfTracingEnabled) {
        this.adapter = adapter;
        this.scheduler = null;
        this.dataFileReader = dataReader;
        this.timestampUnit = timestampUnit;
        this.status = new Status(Step.STOPPED, 0);
        this.fileRepeatCount = loopCount;
        this.useScheduledTime = useScheduleTime;
        if (useScheduleTime) {
            this.rtMode = Globals.END_TO_END_RT;
            this.rtResolution = Globals.MILLIS_RT;
        } else {
            this.rtMode = rtMode;
            this.rtResolution = rtResolution;
        }
        this.perfTracingEnabled = perfTracingEnabled;
        this.perfStats = new DriverPerfStats();
    }

    /**
     * @return  the current status of this sender thread
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @return  the number of events sent so far by this sender thread
     */
    public long getSentEventCount() {
        return sentEventCount;
    }

    /**
     * Stops sending events until the resume() method is called.
     */
    public void pauseLoad() {
        this.status.setStep(Step.PAUSED);
    }

    /**
     * Restarts event submission, if previously paused.
     */
    public void resumeLoad() {
        synchronized (this) {
            this.status.setStep(Step.RUNNING);
            notifyAll();
        }
    }

    /**
     * Stops permanently event submission.
     */
    public void stopLoad() {
        synchronized (this) {
            this.status.setStep(Step.STOPPED);
            // If it is paused
            notifyAll();
        }

    }

    /**
     * Method used to increase or decrease the rate at which events are submitted.
     *
     * @param factor		The multiplication factor (e.g. 2 = 2x faster than original rate)
     */
    public void setRateFactor(double factor) {
        if (this.scheduler != null) {  // scheduled run
            this.scheduler.setRateFactor(factor);
        } else {  // timestamped run
            this.factor = factor;
        }

    }

    @Override
    public void run() {
        if (this.scheduler != null) {
            scheduledRun();
        } else {
            timestampedRun();
        }
    }

    /**
     * Workload with a predetermined event submission rate.
     */
    private void scheduledRun() {
        long interTime;                 // inter-arrival time, in nanoseconds
        long sleepTime;                 // in milliseconds
        long pauseT0;                   // in nanoseconds
        Event event = null;
        CSV_Event csvEvent = null;

        try {
            this.status.setStep(Step.RUNNING);

            if (dataFileReader != null) {
                csvEvent = dataFileReader.getNextCSVEvent();
            } else if (datagen != null) {
                synchronized (datagen) {
                    event = datagen.getNextEvent();
                }
            }

            for (int i = 0; i < fileRepeatCount; i++) {
                long expectedElapsedTime = 0; // in nanoseconds
                long now;
                long t0 = System.currentTimeMillis();
                long firstTimestamp = t0;
                long scheduledTime = 0;

                while (event != null || csvEvent != null) {
                    try {
                        // Checks if driver has been paused and waits if so
                        synchronized (this) {
                            pauseT0 = 0;
                            while (this.status.getStep() == Step.PAUSED) {
                                if (pauseT0 == 0) {
                                    pauseT0 = System.nanoTime();
                                }
                                this.wait();
                            }
                            if (pauseT0 != 0) {
                                timeInPause += (System.nanoTime() - pauseT0);
                            }
                        }
                        // Checks if driver was stopped
                        if (this.status.getStep() == Step.STOPPED) {
                            return;
                        }

                        interTime = this.scheduler.getInterArrivalTime();
                        expectedElapsedTime += interTime;
                        scheduledTime = firstTimestamp + (expectedElapsedTime + timeInPause) / SLEEP_TIME_RESOLUTION;
                        now = System.currentTimeMillis();
                        sleepTime = scheduledTime - now;
                        if (sleepTime > 0) {
                            Thread.sleep(sleepTime);
                        }

                        if (useScheduledTime) {
                            if (event != null) {
                                event.setTimestamp(scheduledTime);
                            } else {
                                csvEvent.setTimestamp(scheduledTime);
                            }
                        }
                        // Sends the event
                        if (event != null) {
                            this.sendEvent(event);
                        } else {
                            this.sendEvent(csvEvent);
                        }
                    } catch (Exception exc) {
                        System.err.println("Cannot send event (" + exc.getMessage() + ")");
                        exc.printStackTrace();
                        if (this.status.getStep() == Step.RUNNING) {
                            this.status.setStep(Step.ERROR);
                        }
                    }

                    if (dataFileReader != null) { // read event from data file
                        csvEvent = dataFileReader.getNextCSVEvent();
                    } else if (datagen != null)  { // generate event
                        event = datagen.getNextEvent();
                    }
                }
                if (dataFileReader != null) {
                    dataFileReader.reOpen();
                    event = dataFileReader.getNextEvent();
                }
            }

            this.status.setStep(Step.FINISHED);
        } catch (IOException ioe) {
            System.err.println("Cannot read datafile (" + ioe.getMessage() + ")");
            this.status.setStep(Step.ERROR);
        } catch (Exception exc) {
            System.err.println("Unexpected exception. (" + exc.getClass() + "-" + exc.getMessage() + ")\n load submisson will abort.");
            this.status.setStep(Step.ERROR);
            exc.printStackTrace();
            return;
        } finally {
            if (dataFileReader != null) {
                dataFileReader.closeFile();
            }
        }
    }

    /**
     * Workload where event submission rate is determined by the timestamps of
     * events in the data file.
     */
    private void timestampedRun() {
        long interTime;                 // interarrival time in nanoseconds
        long sleepTime;                 // in milliseconds
        long pauseT0;                   // in milliseconds
        long currentTS, lastTS;         // in milliseconds
        long timeResolution;

        if (this.timestampUnit == ExternalFileWorkloadPhase.MILLISECONDS) {
            timeResolution = 1;
        } else { // seconds and date/time
            timeResolution = 1000;
        }

        try {
            this.status.setStep(Step.RUNNING);
            for (int i = 0; i < fileRepeatCount; i++) {
                CSV_Event event = dataFileReader.getNextCSVEvent();

                // initializes lastTS variable
                if (event != null) {
                    lastTS = event.getTimestamp();

                } else {
                    return;
                }


                // Used when timestamping mode is based on scheduled time -----------------
                long scheduledTime;
                long expectedElapsedTime = 0;
                long now;
                long t0 = System.currentTimeMillis();
                long firstTimestamp = t0;
                //------------------------------------------------------------------------

                while (event != null) {
                    try {
                        // Checks if driver was paused and waits if so
                        synchronized (this) {
                            pauseT0 = 0;
                            while (this.status.getStep() == Step.PAUSED) {
                                if (pauseT0 == 0) {
                                    pauseT0 = System.nanoTime();
                                }
                                this.wait();
                            }
                            if (pauseT0 != 0) {
                                timeInPause += (System.nanoTime() - pauseT0);
                            }

                        }
                        // Checks if driver was stopped
                        if (this.status.getStep() == Step.STOPPED) {
                            return;
                        }

                        currentTS = event.getTimestamp();
                        interTime = Math.round(1E6 * timeResolution * (currentTS - lastTS)
                                                                    / factor);
                        expectedElapsedTime += interTime;
                        scheduledTime = firstTimestamp + (expectedElapsedTime + timeInPause)
                                                         / SLEEP_TIME_RESOLUTION;
                        now = System.currentTimeMillis();
                        sleepTime = scheduledTime - now;
                        if (sleepTime > 0) {
                            Thread.sleep(sleepTime);
                        }
                        lastTS = currentTS;

                        // Sends the event
                        event.setTimestamp(scheduledTime);
                        this.sendEvent(event);

                    } catch (Exception e2) {
                        System.err.println("Cannot send event (" + e2.getMessage() + ")");
                        if (this.status.getStep() == Step.RUNNING) {
                            this.status.setStep(Step.ERROR);
                        }
                    }

                    event = dataFileReader.getNextCSVEvent();
                }

                dataFileReader.reOpen();
            }

            this.status.setStep(Step.FINISHED);
        } catch (IOException ioe) {
            System.err.println("Cannot read datafile (" + ioe.getMessage() + ")");
            this.status.setStep(Step.ERROR);
        } catch (Exception exc) {
            System.err.println("Unexpected exception. (" + exc.getClass()
                             + "-" + exc.getMessage() + ")\n load submisson will abort.");
            exc.printStackTrace();
            this.status.setStep(Step.ERROR);
            return;
        } finally {
            if (dataFileReader != null) {
                dataFileReader.closeFile();
            }
        }
    }

    /**
     * Sends an event to the system under test.
     *
     * @param event         the event to be sent
     * @throws Exception    if an error occurs during event submission
     */
    private void sendEvent(Event event) throws Exception {
        // Response time measurement:
        if (rtMode == Globals.END_TO_END_RT && !useScheduledTime) {  // Do not use scheduled time...
            // ...use measured send time
            long sendTime = 0;
            if (rtResolution == Globals.MILLIS_RT) {
                sendTime = System.currentTimeMillis();
            } else if (rtResolution == Globals.NANO_RT) {
                sendTime = System.nanoTime();
            }
            event.setTimestamp(sendTime);
        }

        // Send the event to server;
        adapter.send(event);

        // Logs the sent event
        if (logger != null) {
            logger.log(event);
        }

        // Updates perf stats
        if (perfTracingEnabled) {
            this.perfStats.incrementCount(event.getType().getName());
        }

        sentEventCount++;
    }

    /**
     * Sends an event to the system under test.
     *
     * @param event         the event to be sent
     * @throws Exception    if an error occurs during event submission
     */
    private void sendEvent(CSV_Event event) throws Exception {
        // Response time measurement:
        if (rtMode == Globals.END_TO_END_RT && !useScheduledTime) {  // Do not use scheduled time...
            // ...use measured send time
            long sendTime = 0;
            if (rtResolution == Globals.MILLIS_RT) {
                sendTime = System.currentTimeMillis();
            } else if (rtResolution == Globals.NANO_RT) {
                sendTime = System.nanoTime();
            }
            event.setTimestamp(sendTime);
        }

        // Send the event to server;
        adapter.send(event);

        // Logs the sent event
        if (logger != null) {
            logger.log(event);
        }

        // Updates perf stats
        if (perfTracingEnabled) {
            this.perfStats.incrementCount(event.getType());
        }

        sentEventCount++;
    }

    /**
     * Sets the logger for this sender thread.
     *
     * @param logger    A logger to save submitted events to disk.
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Indicates if this sender thread is using event's scheduled time as their timestamp.
     *
     * @return  <tt>true</tt> if events' scheduled time is being used as their timestamp,
     *          <tt>false</tt> otherwise
     */
    public boolean isUsingScheduledTime() {
        return useScheduledTime;
    }

    /**
     * Enables or disables online performance tracing at the Sender.
     *
     * @param enabled           <tt>true</tt> for enabling performance tracing,
     *                          <tt>false</tt> for disabling it.
     * @throws RemoteException
     */
    protected void setPerfTracing(boolean enabled) {
        this.perfTracingEnabled = true;
    }

    /**
     *
     * @return  the performance stats for this sender
     */
    protected DriverPerfStats getPerfStats() {
        return this.perfStats;
    }

    /**
     * Resets the performance stats of this sender.
     */
    protected void resetPerfStats() {
        this.perfStats.reset();
    }
}
