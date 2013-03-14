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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import pt.uc.dei.fincos.basic.Globals;

/**
 * A set of performance metrics collected from log files.
 *
 * @author  Marcelo R.N. Mendes
 */
public final class PerformanceStats implements Cloneable, Comparable<PerformanceStats> {

    /** Controls the frequency at which stats are stored over time. Default: 1-second. */
    public static final int DEFAULT_TIME_BUCKET_IN_MILLIS = 1000;

    /** The identifier of the target system. */
    public String server;

    /** The stream on the target system these stats refer to. */
    public Stream stream;

    /** The point in time these stats correspond to. */
    public long timestamp;

    // Performance Stats --------------------------------------------------------------------------
    /** Number of events processed since last stats refresh. */
    public int lastEventCount = 0;

    /** Total number of events processed. */
    public int totalEventCount = 0;

    /** The interval since the first processed event until the last processed event. */
    public double elapsedTime;

    /** Average number of processed events per second. */
    public double avgThroughput;

    /** Average number of processed events per second since last stats refresh. */
    public double lastThroughput;

    /** Maximum throughput. */
    public double maxThroughput = 0;

    /** Minimum throughput. */
    public double minThroughput = Integer.MAX_VALUE;

    /** The last response time. */
    public double lastRT;

    /** Average response time. */
    public double avgRT;

    /** Sum of all response times. */
    double totalRT = 0;

    /** Maximum response time. */
    public double maxRT = 0;

    /** Minimum response time. */
    public double minRT = Long.MAX_VALUE;

    /** Standard deviation of response time. */
    public double stdevRT;

    /** The sum of squares of the response times (used to compute stdev). */
    double sumSqrRT = 0;
    // --------------------------------------------------------------------------------------------

    /** The number format used when converting these stats to a textual representation. */
    public final DecimalFormat statsFormat;


    /**
     *
     * @param server    The identifier of the target system these stats corresponds to
     * @param stream    The stream on the target system these stats refer to
     */
    public PerformanceStats(String server, Stream stream) {
        this.server = server;
        this.stream = stream;
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');
        statsFormat = new DecimalFormat("0.000", symbols);
    }

    /**
     *  Computes periodic stats (average RT, average, last, min, and max throughput).
     */
    void refreshPeriodicStats() {
        avgThroughput = (1.0E3 * totalEventCount) / elapsedTime;
        lastThroughput = (1.0E3 * lastEventCount) / DEFAULT_TIME_BUCKET_IN_MILLIS;
        minThroughput = Math.min(minThroughput, lastThroughput);
        maxThroughput = Math.max(maxThroughput, lastThroughput);
        avgRT = totalRT / totalEventCount;
        stdevRT = Math.sqrt((sumSqrRT - totalRT * totalRT / totalEventCount)
                            / (totalEventCount - 1));
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(timestamp); sb.append(Globals.CSV_DELIMITER);
        sb.append(server); sb.append(Globals.CSV_DELIMITER);
        sb.append(stream); sb.append(Globals.CSV_DELIMITER);
        sb.append(statsFormat.format(avgThroughput)); sb.append(Globals.CSV_DELIMITER);
        sb.append(statsFormat.format(minThroughput)); sb.append(Globals.CSV_DELIMITER);
        sb.append(statsFormat.format(maxThroughput)); sb.append(Globals.CSV_DELIMITER);
        sb.append(statsFormat.format(lastThroughput)); sb.append(Globals.CSV_DELIMITER);
        sb.append(statsFormat.format(avgRT)); sb.append(Globals.CSV_DELIMITER);
        sb.append(statsFormat.format(minRT)); sb.append(Globals.CSV_DELIMITER);
        sb.append(statsFormat.format(maxRT)); sb.append(Globals.CSV_DELIMITER);
        sb.append(statsFormat.format(stdevRT)); sb.append(Globals.CSV_DELIMITER);
        sb.append(statsFormat.format(lastRT));
        return sb.toString();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        PerformanceStats ret = new PerformanceStats(this.server, this.stream);

        ret.lastEventCount = this.lastEventCount;
        ret.totalEventCount = this.totalEventCount;
        ret.elapsedTime = this.elapsedTime;

        ret.avgThroughput = this.avgThroughput;
        ret.lastThroughput = this.lastThroughput;
        ret.minThroughput = this.minThroughput;
        ret.maxThroughput = this.maxThroughput;

        ret.avgRT = this.avgRT;
        ret.lastRT = this.lastRT;
        ret.minRT = this.minRT;
        ret.maxRT = this.maxRT;
        ret.totalRT = this.totalRT;
        ret.stdevRT = this.stdevRT;
        ret.sumSqrRT = this.sumSqrRT;

        return ret;
    }

    @Override
    public int compareTo(PerformanceStats o) {
        return (this.timestamp < o.timestamp
                ? -1
                : (this.timestamp > o.timestamp
                        ? 1
                        : stream.name.compareTo(o.stream.name)));
    }

    /**
     * If two stats refer to the same timestamp, to the same server address and
     * to the same stream, they are considered the same.
     *
     * @param o the reference stats with which to compare.
     *
     * @return <code>true</code> if this object is the same as the o
     *          argument; <code>false</code> otherwise.
     *
     */
    @Override
    public boolean equals(Object o) {
        PerformanceStats comp;
        if (o instanceof PerformanceStats) {
            comp = (PerformanceStats) o;
            return this.timestamp == comp.timestamp
            && this.server.equals(comp.server)
            && this.stream.equals(comp.stream);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) timestamp + this.server.hashCode() + this.stream.hashCode();
    }
}
