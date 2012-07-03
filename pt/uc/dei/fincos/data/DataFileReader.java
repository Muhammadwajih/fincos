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

	private String path;

	private CSVReader reader;

	/** Pre-defined types (from configuration file). */
	private Set<EventType> types;

	/** types that are not explicitly mentioned in config file. */
	private HashMap<String, EventType> createdTypes = new HashMap<String, EventType>();

	/** Indicates if the data file contains events' type. */
	private final boolean containsEventsType;

	/** Indicates if the data file contains timestamps. */
	private final boolean containsTimestamp;

	/** The time unit of the timestamps in the data file (if it contains). */
    private final int timestampUnit;

	/** Event type name to be used when the data file does not contain event types. */
	private String eventTypeName;

	/**
	 * Constructor for workloads using external data files.
	 *
	 * @param path                     The path for the data file
	 * @param containsTimestamp        Indicates if the data file contains timestamps
	 * @param containsEventsType       Indicates if the data file contains events' type
	 * @param timestampUnit            The time unit of the timestamps in the data file (if it contains)
	 * @param eventTypeName            Event type name to be used when the data file does not contain event types
	 * @throws FileNotFoundException
	 */
	public DataFileReader(String path, boolean containsTimestamp, int timestampUnit,
	        boolean containsEventsType, String eventTypeName)
	throws FileNotFoundException {
		this.path = path;
		this.open(path);
		this.containsTimestamp = containsTimestamp;
		this.timestampUnit = timestampUnit;
		this.containsEventsType = containsEventsType;
		if (!this.containsEventsType) {
		    this.setEventTypeName(eventTypeName);
		}
	}

	/**
	 * Constructor for Synthetic workloads.
	 *
	 * @param path			The path for the data file
	 * @param eventTypes	Pre-defined types obtained from configuration file
	 * @throws FileNotFoundException
	 */
	public DataFileReader(String path, Set<EventType> eventTypes) throws FileNotFoundException {
		this.path = path;
		this.open(path);
		this.types = eventTypes;
		this.containsEventsType = true;
		this.containsTimestamp = false;
		this.timestampUnit = ExternalFileWorkloadPhase.MILLISECONDS;
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
                // TODO: remove the substring due to the "type:"
                typeName = record[1].substring(5);
                payload = new String[record.length - 2];
            } else {
                payload = new String[record.length - 1];
            }
            if (timestampUnit == ExternalFileWorkloadPhase.DATE_TIME) {
                timestamp = Globals.DATE_TIME_FORMAT.parse(record[0]).getTime();
            } else {
                timestamp = Long.parseLong(record[0]);
            }
        } else {
            if (containsEventsType) {
                // TODO: remove the substring due to the "type:"
                typeName = record[0].substring(5);
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
		        // TODO: remove the substring due to the "type:"
		        typeName = csvRecord[1].substring(5);
		        eventRecord = new Object[csvRecord.length - 2];
		    } else {
		        eventRecord = new Object[csvRecord.length - 1];
		    }
		    if (timestampUnit == ExternalFileWorkloadPhase.DATE_TIME) {
                timestamp = Globals.DATE_TIME_FORMAT.parse(csvRecord[0]).getTime();
            } else {
                timestamp = Long.parseLong(csvRecord[0]);
            }
		} else {
		    if (containsEventsType) {
                // TODO: remove the substring due to the "type:"
                typeName = csvRecord[0].substring(5);
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
		this.reader = new CSVReader(path, Globals.CSV_SEPARATOR);
	}

	public void reOpen() throws FileNotFoundException {
		this.closeFile();
		this.open(path);
	}
}
