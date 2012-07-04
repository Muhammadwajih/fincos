package pt.uc.dei.fincos.perfmon;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Performance stats collected by a Driver when performance is being measured
 * by FINCoS Perfom in realtime.
 *
 * @author Marcelo R.N. Mendes
 *
 * @see SinkPerfStats
 */
public class DriverPerfStats implements Serializable {
    /** serial id. */
    private static final long serialVersionUID = 6940744959618534404L;

    /** The time this stats have been first updated.*/
    private long start;

    /** The last time this stats have been updated.*/
    private long end;

    /** A map stream -> number of events received. */
    private HashMap<String, Integer> streamStats;

    public DriverPerfStats() {
        this.reset();
    }

    public DriverPerfStats(long start, long end, HashMap<String, Integer> streamStats) {
        super();
        this.start = start;
        this.end = end;
        this.streamStats = streamStats;
    }

    public void incrementCount(String stream) {
        this.incrementCount(stream, 1, System.currentTimeMillis());
    }

    public void incrementCount(String stream, long timestamp) {
        this.incrementCount(stream, 1, timestamp);
    }

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
