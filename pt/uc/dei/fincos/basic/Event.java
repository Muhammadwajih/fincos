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


package pt.uc.dei.fincos.basic;

import java.io.Serializable;
import java.security.InvalidParameterException;

/**
 * Class that represents an event instance. Unlike <tt>EventType</tt>, which is only
 * a schema information holder, an <tt>Event</tt> effectively contains data.
 *
 * @author  Marcelo R.N. Mendes
 *
 * @see EventType
 * @see Attribute
 */
public final class Event implements Serializable {
    /** serial id. */
    private static final long serialVersionUID = 2370123373567592329L;

    /** The type of this event. */
    private final EventType type;

    /** The timestamp of this event. */
    private long timestamp;

    /** The data carried by this event. */
    private final Object[] payload;

    /**
     * Creates an empty Event instance.
     *
     * @param type  Event's schema
     * @throws Exception
     */
    public Event(EventType type) {
        this(type, new Object[type.getAttributeCount()]);
    }

    /**
     *
     * Creates an Event instance and fills its attributes with the values passed as argument.
     *
     * @param type                          Event's schema
     * @param attValues                     Values for the event's attributes
     */
    public Event(EventType type, Object[] attValues) {
        // Sets event's type
        this.type = type;
        // Sets event's value
        if (attValues.length != type.getAttributeCount()) {
            throw new InvalidParameterException("Number of values doesn't match "
                                             + "number of attributes.");
        }
        this.payload = attValues;
    }

    /**
     *
     * @return  the type of this event
     */
    public EventType getType() {
        return type;
    }

    /**
     *
     * @return  the list of attributes of this event
     */
    public Attribute[] getAttributes() {
        return this.type.getAttributes();
    }

    /**
     *
     * @return  the list of values assigned to the attributes of this event
     */
    public Object[] getValues() {
        return this.payload;
    }

    /**
     * Sets the value of an Event's attribute. (Checks if they are compatible)
     *
     * @param att       The name of the attribute whose value must be set
     * @param value     The intended value
     */
    public void setAttributeValue(String att, Object value) {
        int fieldIndex = this.type.indexOf(att);
        this.setAttributeValue(fieldIndex, value);
    }

    /**
     * Sets the value of an Event's attribute. (Checks if they are compatible)
     *
     * @param attIndex  The index of the attribute whose value must be set
     * @param value     The intended value
     */
    public void setAttributeValue(int attIndex, Object value) {
        if (value == null || value.equals("null")) {
            return;
        }
        try {
            switch (this.type.getAttribute(attIndex).getType()) {
            case INTEGER:
                if (value instanceof String) {
                    this.payload[attIndex] = Integer.parseInt((String) value);
                } else {
                    if (value instanceof Double) {
                        this.payload[attIndex] = (int) Math.round((Double) value);
                    } else {
                        this.payload[attIndex] = value;
                    }
                }
                break;
            case LONG:
                if (value instanceof String) {
                    this.payload[attIndex] = Long.parseLong((String) value);
                } else {
                    if (value instanceof Double) {
                        this.payload[attIndex] = Math.round((Double) value);
                    } else {
                        if (value instanceof Integer) {
                            this.payload[attIndex] = new Long((Integer) value);
                        } else {
                            this.payload[attIndex] = value;
                        }
                    }
                }
                break;
            case FLOAT:
                if (value instanceof String) {
                    this.payload[attIndex] = Float.parseFloat((String) value);
                } else {
                    if (value instanceof Integer) {
                        this.payload[attIndex] = new Float((Integer) value);
                    } else {
                        if (value instanceof Double) {
                            this.payload[attIndex] = new Float((Double) value);
                        } else {
                            this.payload[attIndex] = value;
                        }
                    }
                }
                break;
            case DOUBLE:
                if (value instanceof String) {
                    this.payload[attIndex] = Double.parseDouble((String) value);
                } else {
                    this.payload[attIndex] = value;
                }
                break;
            case TEXT:
                this.payload[attIndex] = value;
                break;
            case BOOLEAN:
                if (value instanceof String) {
                    this.payload[attIndex] = Boolean.parseBoolean((String) value);
                } else {
                    this.payload[attIndex] = value;
                }
                break;
             default:
                 this.payload[attIndex] = null;
            }
        } catch (ClassCastException e) {
            this.payload[attIndex] = null;
            throw new ClassCastException("Configured value is incompatible with field's type.");
        }
    }


    /**
     * Returns only the value of a given attribute passed as argument.
     *
     * @param att		The attribute whose value must be returned
     * @return			The Attribute's value
     */
    public Object getAttributeValue(String att) {
        int fieldIndex = this.type.indexOf(att);
        return this.payload[fieldIndex];
    }


    /**
     * Returns the value of a the i-th attribute.
     *
     * @param i			The index of the attribute whose value must be returned
     * @return			The Attribute's value
     */
    public Object getAttributeValue(int i) {
        return this.payload[i];
    }

    /**
     * Sets the timestamp of this event.
     *
     * @param timestamp     event's timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     *
     * @return  the timestamp of this event
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     *
     * @return  the event's data as a CSV record
     */
    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getType().getName());

        for (Object attributeValue : payload) {
            sb.append(Globals.CSV_DELIMITER);
            sb.append(attributeValue.toString());
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        String ret = "<" + this.type.getName() + ">[ ";

        Attribute[] atts = type.getAttributes();
        for (int i = 0; i < type.getAttributeCount(); i++) {
            ret +=  atts[i].getName() + "=" + payload[i] + " ";
        }
        ret += "]";

        return ret;
    }
}
