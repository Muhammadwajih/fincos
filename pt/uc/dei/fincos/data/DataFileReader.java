package pt.uc.dei.fincos.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import pt.uc.dei.fincos.basic.Attribute;
import pt.uc.dei.fincos.basic.CSV_Event;
import pt.uc.dei.fincos.basic.Datatype;
import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.driver.ExternalFileWorkloadPhase;



public class DataFileReader {

    /** Path for the data file. */
	private String path;

	private CSVReader reader;

	/** Sequence of characters used to separate the fields of the records in the data file. */
	private final String delimiter;

	/** Pre-defined types (from configuration file). */
	private Set<EventType> types;

	/** types that are not explicitly mentioned in config file. */
	private HashMap<String, EventType> createdTypes = new HashMap<String, EventType>();

	/** Indicates if the data file contains events' type. */
	private final boolean containsEventsType;

	/** The index of the field containing the type of the records in the data file. */
    private final int typeIndex;

	/** Indicates if the data file contains timestamps. */
	private final boolean containsTimestamp;

	/** The time unit of the timestamps in the data file (if it contains). */
    private final int timestampUnit;

    /** The index of the field containing the timestamp of the records in the data file. */
    private final int timestampIndex;

	/** Event type name to be used when the data file does not contain event types. */
	private String eventTypeName;


	/**
	 * Constructor for workloads using external data files.
	 *
	 * @param path                     The path for the data file
	 * @param delimiter                Sequence of characters used to separate the fields of the records in the data file
	 * @param containsTimestamp        Indicates if the data file contains timestamps
	 * @param timestampUnit            The time unit of the timestamps in the data file (if it contains)
	 * @param timestampIndex           The index of the field containing the timestamp of the records in the data file
	 * @param containsEventsType       Indicates if the data file contains events' type
	 * @param typeIndex                The index of the field containing the type of the records in the data file.
	 * @param eventTypeName            Event type name to be used when the data file does not contain event types
	 * @throws FileNotFoundException   If the data file cannot be opened
	 */
	public DataFileReader(String path, String delimiter,
	        boolean containsTimestamp, int timestampUnit, int timestampIndex,
	        boolean containsEventsType, int typeIndex, String eventTypeName)
	throws FileNotFoundException {
		this.path = path;
		this.delimiter = delimiter;
		if (containsTimestamp) {
		    this.timestampIndex = timestampIndex;
		} else {
		    this.timestampIndex = -1;
		}
		this.containsTimestamp = containsTimestamp;
		this.timestampUnit = timestampUnit;
		this.containsEventsType = containsEventsType;

		if (!this.containsEventsType) {
		    this.setEventTypeName(eventTypeName);
		    this.typeIndex = -1;
		} else {
		    this.typeIndex = typeIndex;
		}
		this.open(path);
	}

	/**
	 * Constructor for Synthetic workloads.
	 *
	 * @param path                     The path for the data file
	 * @param eventTypes               Pre-defined types obtained from configuration file
	 * @throws FileNotFoundException   If the data file cannot be opened
	 */
	public DataFileReader(String path, Set<EventType> eventTypes) throws FileNotFoundException {
		this.path = path;
		this.delimiter = Globals.CSV_DELIMITER;
		this.types = eventTypes;
		this.containsEventsType = true;
		this.typeIndex = 0;
		this.containsTimestamp = false;
		this.timestampIndex = -1;
		this.timestampUnit = ExternalFileWorkloadPhase.MILLISECONDS;
		this.open(path);
	}


	/**
	 * Retrieves the next event from the data file.
	 *
	 * @return		           The event, represented as an array of in CSV format
	 * @throws IOException
	 */
	public CSV_Event getNextCSVEvent() throws Exception {
	    String[] record = reader.getNextRecord();
	    if (record == null) {
	        return null;
	    }
	    String[] payload;
	    String typeName = this.eventTypeName;
	    long timestamp = 0;
	    if (containsTimestamp) {
            if (containsEventsType) {
                typeName = record[getTypeIndex(record)];
                payload = new String[record.length - 2];
            } else {
                payload = new String[record.length - 1];
            }
            if (timestampUnit == ExternalFileWorkloadPhase.DATE_TIME) {
                timestamp = Globals.DATE_TIME_FORMAT.parse(record[getTSIndex(record)]).getTime();
            } else {
                timestamp = Long.parseLong(record[getTSIndex(record)]);
            }
        } else {
            if (containsEventsType) {
                typeName = record[getTypeIndex(record)];
                payload = new String[record.length - 1];
            } else {
                payload = new String[record.length];
            }
        }

	    int payloadIndex = record.length - payload.length;
	    for (int i = 0; i < payload.length; i++) {
	        payload[i] = record[payloadIndex + i];
        }

	    return new CSV_Event(typeName, timestamp, payload);
	}


	/**
	 * Retrieves the next event from the data file.
	 *
	 * @return		The event in the framework internal representation format
	 * @throws IOException
	 */
	public Event getNextEvent() throws Exception {
		String[] record = reader.getNextRecord();
		if (record != null) {
			return fromCSVToEvent(record);
		} else {
			return null;
		}
	}


	/**
	 * Converts an event expressed as CSV textual record into
	 * internal event representation.
	 * (LEGACY METHOD)
	 *
	 * @param csvRecord			An event represented as CSV record
	 * @return					An event represented as an instance of class <tt>Event</tt>
	 * @throws IOException
	 */
	private Event fromCSVToEvent(String[] csvRecord) throws Exception {
		Event e = null;

		EventType type = null;
		Object[] eventRecord;

		String typeName = this.eventTypeName;
		long timestamp = 0;

		if (containsTimestamp) {
		    if (containsEventsType) {
		        typeName = csvRecord[getTypeIndex(csvRecord)];
		        eventRecord = new Object[csvRecord.length - 2];
		    } else {
		        eventRecord = new Object[csvRecord.length - 1];
		    }
		    if (timestampUnit == ExternalFileWorkloadPhase.DATE_TIME) {
                timestamp = Globals.DATE_TIME_FORMAT.parse(csvRecord[getTSIndex(csvRecord)]).getTime();
            } else {
                timestamp = Long.parseLong(csvRecord[getTSIndex(csvRecord)]);
            }
		} else {
		    if (containsEventsType) {
                typeName = csvRecord[getTypeIndex(csvRecord)];
                eventRecord = new Object[csvRecord.length - 1];
            } else {
                eventRecord = new Object[csvRecord.length];
            }
		}

		if (types != null) {
		    type = this.getEventType(typeName, types); // tries to find in the list of types from config file
		}

        if (type == null) {
            type = createdTypes.get(typeName); // tries to find in the list of new types
            if (type == null) {
                // Creates a new type
                Attribute[] atts = new Attribute[eventRecord.length];
                for (int i = 0; i < atts.length; i++) {
                    atts[i] = new Attribute(Datatype.TEXT, "att" + i);
                }
                type = new EventType(typeName, atts);
                createdTypes.put(typeName, type);
            }
        }

        int payloadIndex = csvRecord.length - eventRecord.length;
        for (int i = 0; i < eventRecord.length; i++) {
            eventRecord[i] = csvRecord[payloadIndex + i];
        }
        e = new Event(type, eventRecord);
        e.setTimestamp(timestamp);

		return e;
	}

	/**
	 * Utilitary function used to converts an event expressed as CSV textual
	 * record into internal event representation.
	 * (LEGACY METHOD)
	 *
	 * @param typeName
	 * @param types
	 * @return
	 */
	private EventType getEventType(String typeName, Set<EventType> types) {
		if (types != null) {
			for (EventType type : types) {
				if (type.getName().equalsIgnoreCase(typeName)) {
					return type;
				}
			}
		}

		return null;
	}

	private void setEventTypeName(String eventTypeName) {
		if (eventTypeName != null && !eventTypeName.isEmpty()) {
			this.eventTypeName = eventTypeName;
		} else {
			this.eventTypeName = "unknown";
			System.err.println("WARNING: Null/Blank Event type name.");
		}
	}

	public void closeFile() {
		try {
			this.reader.closeFile();
		} catch (IOException e) {
			System.err.println("WARNING: Could not close data file (" + e.getMessage() + ")");
		}
	}

	private void open(String path) throws FileNotFoundException{
		this.reader = new CSVReader(path, this.delimiter);
	}

	public void reOpen() throws FileNotFoundException {
		this.closeFile();
		this.open(path);
	}

	private int getTSIndex(String[] csvRecord) {
        switch (timestampIndex) {
        case ExternalFileWorkloadPhase.FIRST_FIELD:
            return 0;
        case ExternalFileWorkloadPhase.SECOND_FIELD:
            return 1;
        case ExternalFileWorkloadPhase.LAST_FIELD:
            return csvRecord.length - 1;
        case ExternalFileWorkloadPhase.SECOND_LAST_FIELD:
            return csvRecord.length - 2;
        default:
            return -1;
        }
    }

	private int getTypeIndex(String[] csvRecord) {
	    switch (typeIndex) {
        case ExternalFileWorkloadPhase.FIRST_FIELD:
            return 0;
        case ExternalFileWorkloadPhase.SECOND_FIELD:
            return 1;
        case ExternalFileWorkloadPhase.LAST_FIELD:
            return csvRecord.length - 1;
        case ExternalFileWorkloadPhase.SECOND_LAST_FIELD:
            return csvRecord.length - 2;
        default:
            return -1;
        }
	}
}
