package pt.uc.dei.fincos.driver;

/**
 * A phase in the workload of a Driver that is based on an external dataset file.
 *
 * @author Marcelo R.N. Mendes
 *
 * @see SyntheticWorkloadPhase
 */
public class ExternalFileWorkloadPhase extends WorkloadPhase {
    /** serial id. */
    private static final long serialVersionUID = -8771391854252211855L;

    /** The path of the external file. */
    private String filePath;

    /** The number of times the external file will be submitted. */
    private int loopCount;

    /** A flag indicating if the external file contains timestamps.  */
    private boolean containsTimestamps;

    /** Use the timestamps in the file. */
    private boolean useTimestamps;

    /** The time unit of the timestamps in the external data file. */
    private int timestampUnit;

    /** Injection rate, when not using the timestamps in the data file. */
    private double eventSubmissionRate;

    /** A flag indicating if the external file contains event types. */
    private boolean containsEventTypes;

    /** The external file does not contain event types. All events are of the same type*/
    private String singleEventTypeName;

    /** Timestamp of the data file is expressed in milliseconds. */
    public static final int MILLISECONDS = 0;

    /** Timestamp of the data file is expressed in seconds. */
    public static final int SECONDS = 1;

    /** Timestamp of the data file is expressed as a date and time record. */
    public static final int DATE_TIME = 2;



    public ExternalFileWorkloadPhase(String filePath, int loopCount,
            boolean containsTimestamps, boolean useTimestamps,
            int timestampUnit, double eventSubmissionRate, boolean containsEventTypes, String singleEventTypeName) throws Exception
            {
        setFilePath(filePath);
        setLoopCount(loopCount);
        setContainsTimestamps(containsTimestamps);
        setUseTimestamps(useTimestamps);
        setTimestampUnit(timestampUnit);
        setEventSubmissionRate(eventSubmissionRate);
        setContainsEventTypes(containsEventTypes);
        setSingleEventTypeName(singleEventTypeName);
            }

    public void setTimestampUnit(int timestampUnit) throws Exception {
        if(timestampUnit == MILLISECONDS || timestampUnit == SECONDS || timestampUnit == DATE_TIME)
            this.timestampUnit = timestampUnit;
        else
            throw new Exception ("Invalid timestamp unit.");
    }

    public int getTimestampUnit() {
        return timestampUnit;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setContainsTimestamps(boolean containsTimestamps) {
        this.containsTimestamps = containsTimestamps;
    }

    public boolean containsTimestamps() {
        return containsTimestamps;
    }

    public void setUseTimestamps(boolean useTimestamps) {
        this.useTimestamps = useTimestamps;
    }

    public boolean isUsingTimestamps() {
        return useTimestamps;
    }

    public void setContainsEventTypes(boolean containsEventTypes) {
        this.containsEventTypes = containsEventTypes;
    }

    public boolean containsEventTypes() {
        return containsEventTypes;
    }

    public void setSingleEventTypeName(String singleEventTypeName) {
        this.singleEventTypeName = singleEventTypeName;
    }

    public String getSingleEventTypeName() {
        return singleEventTypeName;
    }

    public void setEventSubmissionRate(double eventSubmissionRate) throws Exception {
        if(eventSubmissionRate > 0)
            this.eventSubmissionRate = eventSubmissionRate;
        else
            throw new Exception ("Invalid event submission rate.");
    }

    public double getEventSubmissionRate() {
        return eventSubmissionRate;
    }

    public void setLoopCount(int loopCount) {
        if (loopCount > 0) {
            this.loopCount = loopCount;
        } else {
            System.err.println("WARNING: Invalid loop count (" + loopCount + "). Setting to default: 1.");
            this.loopCount = 1;
        }
    }

    public int getLoopCount() {
        return loopCount;
    }
}
