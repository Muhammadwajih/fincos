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

import pt.uc.dei.fincos.basic.Globals;

/**
 * Performance stats for an output stream.
 *
 * @author  Marcelo R.N. Mendes
 */
public final class OutStreamCounters implements Serializable, Cloneable {

    /** serial id. */
    private static final long serialVersionUID = 7977546503970271299L;

    /** Total number of received events. */
    private long totalCount;

    /** Number of events received since last reset. */
    private long lastCount;

    /** Sum of all response times. */
    private double sumRT;

    /** Last response time. */
    private double lastRT;

    /** Maximum response time. */
    private double  maxRT;

    /** Minimum response time. */
    private double minRT;

    /** Sum of squares of response time (used to compute stdev).*/
    private double sumSqrRT = 0;

    /** Number of events processed per second. */
    private double throughput;


    /**
     * Initializes an empty set of performance stats.
     */
    public OutStreamCounters() {
        this.totalCount = 0;
        this.lastCount = 0;
        this.sumRT = 0;
        this.lastRT = 0;
        this.maxRT = 0;
        this.minRT = Long.MAX_VALUE;
        this.sumSqrRT = 0;
    }

    /**
     * Initializes a new set of performance stats.
     *
     * @param totalCount    total number of events
     * @param lastCount     last number of events
     * @param sumRT         total sum of response times
     * @param lastRT        last response time
     * @param maxRT         maximum response time
     * @param minRT         minimum response time
     * @param sumSqrRT      Sum of the squares of the response times
     */
    private OutStreamCounters(long totalCount, long lastCount, double sumRT,
                              double lastRT, double maxRT, double minRT,
                              double sumSqrRT) {
        this.totalCount = totalCount;
        this.lastCount = lastCount;
        this.sumRT = sumRT;
        this.lastRT = lastRT;
        this.maxRT = maxRT;
        this.minRT = minRT;
        this.sumSqrRT = sumSqrRT;
    }


    /**
     * Adds a new data point to this performance stats.
     *
     * @param inputTS       the timestamp of the input event that caused the result event
     * @param outputTS      the timestamp of the result event
     * @param resolution    resolution of the timestamps (milliseconds or nanoseconds)
     */
    public void offer(long inputTS, long outputTS, int resolution) {
        double rtFactor = 1.0;
        if (resolution == Globals.NANO_RT) {
            rtFactor = 1E6;
        }
        totalCount++;
        lastCount++;
        lastRT = (outputTS - inputTS) / rtFactor;
        sumRT += lastRT;
        maxRT = Math.max(maxRT, lastRT);
        minRT = Math.min(minRT, lastRT);
        sumSqrRT += lastRT * lastRT;
    }

    /**
     *
     * @return  the average response time
     */
    public double getAvgRT() {
        return sumRT / totalCount;
    }

    /**
     *
     * @return  the last response time
     */
    public double getLastRT() {
        return lastRT;
    }

    /**
     *
     * @return  Minimum response time
     */
    public double getMinRT() {
        return minRT;
    }

    /**
     *
     * @return  Maximum response time
     */
    public double getMaxRT() {
        return maxRT;
    }

    /**
     *
     * @return  the response time standard deviation
     */
    public double getStdevRT() {
        return Math.sqrt((sumSqrRT - sumRT * sumRT / totalCount) / (totalCount - 1));
    }

    /**
     *
     * @return  number of events received since last reset
     */
    public long getLastCount() {
        return lastCount;
    }

    /**
     *
     * @return  total number of received events
     */
    public long getTotalCount() {
        return totalCount;
    }

    /**
     * Resets the number of events received since last period.
     */
    public void startNewPeriod() {
        lastCount = 0;
    }


    /**
     * Sets the value of the throughput.
     *
     * @param throughput    the throughput value, in events per second
     */
    public void setThroughput(double throughput) {
        this.throughput = throughput;
    }


    /**
     *
     * @return  the throughput, in events per second
     */
    public double getThroughput() {
        return throughput;
    }

    @Override
    protected OutStreamCounters clone() {
        return new OutStreamCounters(totalCount, lastCount, sumRT,
                                    lastRT, maxRT, minRT, sumSqrRT);
    }
}
