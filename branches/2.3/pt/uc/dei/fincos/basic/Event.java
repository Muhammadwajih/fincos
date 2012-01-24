package pt.uc.dei.fincos.basic;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * Class that represents an event instance. Unlike <tt>EventType</tt>, which is only 
 * a schema information holder, an <tt>Event</tt> effectively contains data.
 * 
 * @author Marcelo R.N. Mendes
 *
 * @see		EventType
 * @see		Attribute
 */
public class Event implements Serializable{
	private static final long serialVersionUID = 2370123373567592329L;
	
	private EventType type;
	private LinkedHashMap<Attribute, Object> attributes;	
	private long timestamp;
	
	public Event (EventType type){
		this.setType(type);
		this.attributes = new LinkedHashMap<Attribute, Object>(type.getAttributes().length);
		for (int i = 0; i < type.getAttributes().length; i++) {
			this.attributes.put(type.getAttributes()[i], null);
		}		
	}
	
	/**
	 * Constructor
	 * 
	 * Creates an Event instance and fills its attributes with values passed as argument
	 * 
	 * @param type			Event's schema
	 * @param attValues		Values for the event's attributes
	 * @throws				Exception, if the values passed as argument do not match event type's schema
	 */
	public Event (EventType type, Object[] attValues) throws Exception{
		this(type);
		
		if(attValues.length != type.getAttributes().length)
			throw new Exception("Number of values doesn't match number of attributes.");
		else {
			for (int i = 0; i < type.getAttributes().length; i++) {
				this.setAttributeValue(type.getAttributes()[i], attValues[i]);  
			}			
		}
		
	}
	
	private void setType(EventType type) {
		this.type = type;
	}
	
	public EventType getType() {
		return type;
	}
	
	
	/**
	 * Gets the list of attributes and their respective values of this Event instance
	 * 
	 * @return		A list of pairs in the form attribute-value
	 */
	public LinkedHashMap<Attribute, Object> getAttributes(){
		return this.attributes;
	}
	
	
	/**
	 * Sets the value of an Event's attribute. (Checks if they are compatible)
	 * 
	 * @param att		The attribute whose value must be set
	 * @param value		The intended value
	 */
	public void setAttributeValue(Attribute att, Object value) {
		if (value == null || value.equals("null")) {
			return;
		}
					
		try {			
			switch (att.getType()){
			case INTEGER:
				if (value instanceof String)
					this.attributes.put(att, Integer.parseInt((String)value));
				else {
					if (value instanceof Double)
						this.attributes.put(att, Math.round((Double)value));
					else 
						this.attributes.put(att, (Integer)value);
				}
				break;
			case LONG:
			case TIMESTAMP:
			{
				if (value instanceof String)
					this.attributes.put(att, Long.parseLong((String)value));
				else {
					if (value instanceof Double)
						this.attributes.put(att, Math.round((Double)value));
					else {
						if (value instanceof Integer)
							this.attributes.put(att, new Long((Integer)value));
						else
							this.attributes.put(att, (Long)value);	
					}					
				}
				break;
			}			
			case FLOAT:
				if (value instanceof String)
					this.attributes.put(att, Float.parseFloat((String)value));
				else {
					if (value instanceof Integer)
						this.attributes.put(att, new Float((Integer)value));
					else {
						if (value instanceof Double)
							this.attributes.put(att, new Float((Double)value));
						else
							this.attributes.put(att, (Float)value);
					}			
				}						
				break;
			case DOUBLE:
				if (value instanceof String)
					this.attributes.put(att, Double.parseDouble((String)value));
				else
					this.attributes.put(att, (Double)value);
				break;
			case TEXT:
				this.attributes.put(att, (String)value);
				break;
			case BOOLEAN:
				if (value instanceof String)
					this.attributes.put(att, Boolean.parseBoolean((String)value));
				else
					this.attributes.put(att, (Float)value);				
			}	
		}catch (ClassCastException e){
			this.attributes.put(att, null);
			throw new ClassCastException("Configured value is incompatible with field's type.");			
		}		
	}
	

	/**
	 * Sets the value of an Event's attribute.
	 * 
	 * @param attName	The name of the attribute whose value must be set
	 * @param value		The intended value
	 */
	public void setAttributeValue(String attName, Object value) {
		Attribute busc = new Attribute(null, attName);
		if (this.attributes.containsKey(busc)) {
			Entry<Attribute, Object> entry;
			Attribute a;
						
			Iterator<Entry<Attribute, Object>> i = this.attributes.entrySet().iterator();
			while (i.hasNext()) {
				entry = i.next();
				a = entry.getKey();	
				if (a.equals(busc)) {
					this.setAttributeValue(a, value);
					break;
				}
				
			}
		}
	}
	
	
	/**
	 * Returns only the value of a given attribute passed as argument
	 * 
	 * @param att		The attribute whose value must be returned
	 * @return			The Attribute's value
	 */
	public Object getAttributeValue (Attribute att) {
		return this.attributes.get(att);
	}
	
	
	/**
	 * Returns  the value of a the i-th attribute
	 * 
	 * @param i			The index of the attribute whose value must be returned
	 * @return			The Attribute's value
	 */
	public Object getAttributeValue (int i) {
		return this.attributes.get(this.type.getAttributes()[i]);
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Returns the event's data as a CSV record
	 * 
	 * @return
	 */
	public String toCSV() {
		StringBuilder sb = new StringBuilder("type:");
		sb.append(this.getType().getName());
		
		for (Object attributeValue : this.getAttributes().values()) {
			sb.append(Globals.CSV_SEPARATOR);
			sb.append(attributeValue.toString());
		}
		
		return sb.toString();
	}
	
	public String toString() {
		String ret = "<"+this.type.getName()+">[ ";
		Entry<Attribute, Object> entry;
		Attribute a;
		Object value;
		
		Iterator<Entry<Attribute, Object>> i = this.attributes.entrySet().iterator();
		while (i.hasNext()) {
			entry = i.next();
			a = entry.getKey();
			value = entry.getValue();
			ret += a.getName()+"="+value+" ";
		}
		ret+="]";
		
		return ret;
	}	
}
