package pt.uc.dei.fincos.driver;

public class ExternalFileWorkloadPhase extends WorkloadPhase {
	private static final long serialVersionUID = -596574796116035691L;
	
	private String filePath;
	private int loopCount;
	private boolean containsTimestamps;	// The external file contains timestamps
	private boolean useTimestamps; 		// Use the timestamps in the file
	private int timestampUnit;			// The time unit of the timestamps in the external data file
	private double eventSubmissionRate; 
	private boolean containsEventTypes; // The external file contains event types
	private String singleEventTypeName; // The external file does not contain event types. All events are of the same type
	
	public static final int MILLISECONDS = 0;
	public static final int SECONDS = 1;
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
		if(loopCount > 0)
			this.loopCount = loopCount;
		else {
			System.err.println("WARNING: Invalid loop count (" + loopCount +"). Setting to default: 1.");
			this.loopCount = 1;
		}
	}

	public int getLoopCount() {
		return loopCount;
	}
}
