package pt.uc.dei.fincos.perfmon;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Performance stats collected by a Sink when performance is being measured
 * by FINCoS Perfom in realtime.
 *
 * @author Marcelo R.N. Mendes
 *
 * @see DriverPerfStats
 */
public class SinkPerfStats implements Serializable, Cloneable {
    /** serial id. */
    private static final long serialVersionUID = 8191336871710437713L;

    /** The time this stats have been first updated.*/
    private long start;

    /** The last time this stats have been updated.*/
    private long end;

    /** A map stream -> perf counters. */
    private HashMap<String, OutStreamCounters> streamStats;

    public SinkPerfStats() {
        this.start = -1;
        this.end = -1;
        this.streamStats = new HashMap<String, OutStreamCounters>();
    }

    public SinkPerfStats(long start, long end, HashMap<String, OutStreamCounters> streamStats) {
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
        HashMap<String, OutStreamCounters> streamStats = new HashMap<String, OutStreamCounters>();
        for (Entry<String, OutStreamCounters> e: this.streamStats.entrySet()) {
            streamStats.put(e.getKey(), e.getValue().clone());
        }
        return new SinkPerfStats(this.start, this.end, streamStats);
    }
}
