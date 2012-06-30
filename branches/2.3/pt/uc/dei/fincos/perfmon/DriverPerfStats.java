package pt.uc.dei.fincos.perfmon;

import java.io.Serializable;
import java.util.HashMap;

public class DriverPerfStats implements Serializable {

    /** The time this stats have been first updated.*/
    public long start;

    /** The last time this stats have been updated.*/
    public long end;

    /** A map stream -> number of events received. */
    public HashMap<String, Integer> streamStats;
}
