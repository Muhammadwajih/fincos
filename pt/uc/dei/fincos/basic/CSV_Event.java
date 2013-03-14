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

/**
 *  A representation for events read from data files.
 *
 *  @author  Marcelo R.N. Mendes
 *
 *  @see    Event
 *
 */
public final class CSV_Event {

    /** The name of the type of this record. */
    private String type;

    /** The timestamp of this record. */
    private long timestamp;

    /** The payload of this record. */
    private String[] payload;


    /**
     *
     * @param type      The name of the type of this record
     * @param timestamp The timestamp of this record
     * @param payload   The payload of this record
     */
    public CSV_Event(String type, long timestamp, String[] payload) {
        this.setType(type);
        this.setTimestamp(timestamp);
        this.setPayload(payload);
    }


    /**
     * Sets this event's type.
     *
     * @param type  the event type
     */
    public void setType(String type) {
        this.type = type;
    }


    /**
     *
     * @return  this event's type
     */
    public String getType() {
        return type;
    }


    /**
     * Sets the timestamp of this event.
     *
     * @param timestamp     the timestamp associated with this event
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    /**
     *
     * @return  the timestamp associated with this event
     */
    public long getTimestamp() {
        return timestamp;
    }


    /**
     * Sets the payload of this event.
     *
     * @param payload   the event's payload
     */
    public void setPayload(String[] payload) {
        this.payload = payload;
    }


    /**
     *
     * @return  this event's payload
     */
    public String[] getPayload() {
        return payload;
    }


    /**
    *
    * @return  the event's data as a CSV record
    */
   public String toCSV() {
       StringBuilder sb = new StringBuilder();
       sb.append(this.getType());

       for (String attributeValue : payload) {
           sb.append(Globals.CSV_DELIMITER);
           sb.append(attributeValue.toString());
       }

       return sb.toString();
   }
}
