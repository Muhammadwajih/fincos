package pt.uc.dei.fincos.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import pt.uc.dei.fincos.basic.Attribute;
import pt.uc.dei.fincos.basic.Datatype;
import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.basic.Globals;



public class DataFileReader {
	
	private String path;
	
	private CSVReader reader;
	
	// pre-defined types (from configuration file)
	private Set<EventType> types;	
	
	// types that are not explicitly mentioned in config file
	private HashMap<String, EventType> createdTypes = new HashMap<String, EventType>();
	
	// Indicates if the data file contains events' type
	private boolean containsEventsType;
	
	// Indicates if the data file contains timestamps
	private boolean containsTimestamp;
	
	// Event type name to be used when the data file does not contain event types
	private String eventTypeName;
	
	/**
	 * Constructor for workloads using external data files
	 * 
	 * @param path					The path for the data file
	 * @param containsTimestamp		Indicates if the data file contains timestamps
	 * @param containsEventsType	Indicates if the data file contains events' type
	 * @param eventTypeName			Event type name to be used when the data file does not contain event types
	 * @throws FileNotFoundException
	 */
	public DataFileReader(String path, boolean containsTimestamp, boolean containsEventsType, String eventTypeName) throws FileNotFoundException {
		this.path = path;
		this.open(path);
		this.containsTimestamp = containsTimestamp;
		this.containsEventsType = containsEventsType;	
		if(!this.containsEventsType)
			this.setEventTypeName(eventTypeName);
	}
	
	/**
	 * Constructor for Synthetic workloads
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
	}
	
	
	/**
	 * Retrieves the next event from the data file
	 * 
	 * @return		The event in CSV format
	 * @throws IOException
	 */
	public String getNextCSVEvent() throws IOException {		
		if(containsEventsType)
			return reader.getNextLine();
		else {
			if(!this.containsTimestamp) {
				String line =  reader.getNextLine();
				if(line!= null)
					return "type:" + eventTypeName + Globals.CSV_SEPARATOR + line;
				else
					return null;
			}			
			else {
				String e = reader.getNextLine();
				if(e != null) {
					int tsIndex = e.indexOf(Globals.CSV_SEPARATOR);
					return e.substring(0, tsIndex+1)+
							"type:" + eventTypeName + Globals.CSV_SEPARATOR +
							e.substring(tsIndex+1);	
				}
				else
					return null;
			}
		}
			 
	}
	
	
	/**
	 * Retrieves the next event from the data file.
	 * (LEGACY METHOD)
	 * 
	 * @return		The event in the framework internal representation format
	 * @throws IOException
	 */
	public Event getNextEvent() throws IOException {
		String[] record = reader.getNextRecord();
		if(record != null)
			return fromCSVToEvent(record);
		else 
			return null;
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
	private Event fromCSVToEvent(String[] csvRecord) throws IOException {		
		Event e=null;
				
		EventType type= null;
		String eventRecord[]= new String[csvRecord.length-1];;
		
		if (types != null && csvRecord[0].startsWith("type:")) { // CSV contains events' type
			type = this.getEventType(csvRecord[0].substring(5), types); // tries to find in the list of types from config file
			if (type == null) {
				type = createdTypes.get("Type"+(csvRecord.length-1)); // tries to find in the list of new types
				if (type == null) {
					// Creates a new type
					Attribute atts[] = new Attribute[csvRecord.length-1];
					for (int i = 0; i < atts.length; i++) {
						atts[i] = new Attribute(Datatype.TEXT, "att"+i);
					}
					type = new EventType("Type"+atts.length, atts);
					createdTypes.put("Type"+atts.length, type);
				}
			}
			
			for (int i = 0; i < eventRecord.length; i++) {
				eventRecord[i] = csvRecord[i+1];
			}	
			try {
				e = new Event(type, eventRecord);
			} catch (Exception exc) {
				System.err.println("Could not create event instance. " + exc.getMessage());
			}
		}
		else { // CSV does not specify events' type
			type = createdTypes.get("Type"+(eventRecord.length)); // tries to find in the list of new types
			if (type == null) {
				//	Creates a new type
				Attribute atts[] = new Attribute[eventRecord.length];
				for (int i = 0; i < atts.length; i++) {
					atts[i] = new Attribute(Datatype.TEXT, "att"+i);
				}
				type = new EventType("Type"+atts.length, atts);
				createdTypes.put("Type"+atts.length, type);
			}
		
			try {
				e = new Event(type, eventRecord);
			} catch (Exception exc) {
				System.err.println("Could not create event instance. " + exc.getMessage());
			}
		}
		
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
				if (type.getName().equalsIgnoreCase(typeName))
					return type;
			}
		}
		
		return null;
	}
	
	public void setEventTypeName(String eventTypeName) {
		if(eventTypeName != null && !eventTypeName.isEmpty())
			this.eventTypeName = eventTypeName;
		else {
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
