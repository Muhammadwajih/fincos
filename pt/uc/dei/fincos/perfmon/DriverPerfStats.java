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

/**
 * Performance stats collected by a Driver when performance is being measured
 * by FINCoS Perfom in realtime.
 *
 * @author  Marcelo R.N. Mendes
 *
 * @see SinkPerfStats
 */
public final class DriverPerfStats implements Serializable {
    /** serial id. */
    private static final long serialVersionUID = 6940744959618534404L;

    /** The time this stats have been first updated.*/
    private long start;

    /** The last time this stats have been updated.*/
    private long end;

    /** A map stream -> number of events received. */
    private HashMap<String, Integer> streamStats;

    /**
     * Initializes an empty set of performance stats.
     */
    public DriverPerfStats() {
        this.reset();
    }

    /**
     * Initializes an new set of performance stats.
     *
     * @param start         the start time of the interval which this
     *                      set of performance stats refers to
     * @param end           the end time of the interval which this
     *                      set of performance stats refers to
     * @param streamStats   the performance stats (a map Stream -> Event Count)
     */
    public DriverPerfStats(long start, long end, HashMap<String, Integer> streamStats) {
        super();
        this.start = start;
        this.end = end;
        this.streamStats = streamStats;
    }

    /**
     * Increments the number of received events from the
     * stream passed as argument by one.
     *
     * @param stream    the event stream
     */
    public void incrementCount(String stream) {
        this.incrementCount(stream, 1, System.currentTimeMillis());
    }

    /**
     * Increments the number of received events from the
     * stream passed as argument by one.
     *
     * @param stream        the event stream
     * @param timestamp     the timestamp of the last received event
     */
    public void incrementCount(String stream, long timestamp) {
        this.incrementCount(stream, 1, timestamp);
    }

    /**
     * Increments the number of received events from the
     * stream passed as argument.
     *
     * @param stream        the event stream
     * @param increment     the increment
     * @param timestamp     the timestamp of the last received event
     */
    public void incrementCount(String stream, int increment, long timestamp) {
        if (start == -1) {
            start = timestamp;
        }
        end = timestamp;
        Integer count = this.streamStats.get(stream);
        if (count == null) {
            this.streamStats.put(stream, increment);
        } else {
            this.streamStats.put(stream, count + increment);
        }
    }

    /**
     * Clears all accumulated results.
     */
    public void reset() {
        this.streamStats = new HashMap<String, Integer>();
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
     * @return  a map stream -> number of events received.
     */
    public HashMap<String, Integer> getStreamStats() {
        return streamStats;
    }
}
