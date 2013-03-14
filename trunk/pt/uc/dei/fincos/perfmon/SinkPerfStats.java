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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Performance stats collected by a Sink when performance is being measured
 * by FINCoS Perfom in realtime.
 *
 * @author  Marcelo R.N. Mendes
 *
 * @see DriverPerfStats
 */
public final class SinkPerfStats implements Serializable, Cloneable {
    /** serial id. */
    private static final long serialVersionUID = 8191336871710437713L;

    /** The time this stats have been first updated.*/
    private long start;

    /** The last time this stats have been updated.*/
    private long end;

    /** A map stream -> perf counters. */
    private HashMap<String, OutStreamCounters> streamStats;

    /**
     * Initializes an empty set of performance stats.
     */
    public SinkPerfStats() {
        this.start = -1;
        this.end = -1;
        this.streamStats = new HashMap<String, OutStreamCounters>();
    }

    /**
     * Initializes an new set of performance stats.
     *
     * @param start         the start time of the interval which this
     *                      set of performance stats refers to
     * @param end           the end time of the interval which this
     *                      set of performance stats refers to
     * @param streamStats   the performance stats (a map Stream -> Counters)
     */
    public SinkPerfStats(long start, long end,
            HashMap<String, OutStreamCounters> streamStats) {
        super();
        this.start = start;
        this.end = end;
        this.streamStats = streamStats;
    }

    /**
     * Processes an event received at the Sink.
     *
     * @param stream        the output stream from where the event came from
     * @param inputTS       the timestamp of the input event that caused the result event
     * @param outputTS      the timestamp of the result event
     * @param tsResolution  resolution of the timestamps (milliseconds or nanoseconds)
     */
    public void offer(String stream, long inputTS, long outputTS, int tsResolution) {
        this.offer(stream, inputTS, outputTS, System.currentTimeMillis(), tsResolution);
    }

    /**
     * Processes an event received at the Sink.
     *
     * @param stream        the output stream from where the event came from
     * @param inputTS       the timestamp of the input event that caused the result event
     * @param outputTS      the timestamp of the result event
     * @param timestamp     the timestamp to be assigned to this update
     * @param tsResolution  resolution of the timestamps (milliseconds or nanoseconds)
     */
    public void offer(String stream, long inputTS, long outputTS, long timestamp,
            int tsResolution) {
        if (start == -1) {
            start = timestamp;
        }
        end = timestamp;
        OutStreamCounters stats = this.streamStats.get(stream);
        if (stats == null) {
            stats = new OutStreamCounters();
            this.streamStats.put(stream, stats);
        }
        stats.offer(inputTS, outputTS, tsResolution);
    }

    /**
     * Clears all accumulated results.
     */
    public void reset() {
        for (OutStreamCounters c : streamStats.values()) {
            c.startNewPeriod();
        }
        this.start = -1;
        this.end = -1;
    }

    /**
     *
     * @return  the time this stats have been first updated.
     */
    public long getStart() {
        return start;
    }

    /**
     *
     * @return  the last time this stats have been updated
     */
    public long getEnd() {
        return end;
    }

    /**
     *
     * @return   a map stream -> perf counters
     */
    public HashMap<String, OutStreamCounters> getStreamStats() {
        return streamStats;
    }

    @Override
    public SinkPerfStats clone() {
        HashMap<String, OutStreamCounters> streamStats =
                new HashMap<String, OutStreamCounters>();
        for (Entry<String, OutStreamCounters> e: this.streamStats.entrySet()) {
            streamStats.put(e.getKey(), e.getValue().clone());
        }
        return new SinkPerfStats(this.start, this.end, streamStats);
    }
}
