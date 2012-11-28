/* FINCoS Framework
 * Copyright (C) 2012 CISUC, University of Coimbra
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


package pt.uc.dei.fincos.adapters.cep;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

import pt.uc.dei.fincos.adapters.OutputListener;
import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.sink.Sink;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;


/**
 * Implementation of an event listener for Esper.
 *
 * @author  Marcelo R.N. Mendes
 * @see OutputListener
 *
 */
public final class EsperListener extends OutputListener implements UpdateListener {

    /** Reference to the Esper service provider. */
    private EPServiceProvider epService;

    /** The query this listener listens to. */
    private EPStatement query;

    /** The name of the output stream of the query this listener listens to. */
    private String queryOutputName;

    /** The EPL code of the query this listener listens to. */
    private String queryText;

    /** The schema of the output produced by the query. */
    LinkedHashMap<String, String> querySchema;

    /** The format used to exchange events with the Esper engine. */
    private int eventFormat;

    /**
     * Constructor for direct communication between Esper and Sink.
     * Events received from Esper are forwarded to Sink through method call.
     *
     * @param lsnrID                an alias for this listener
     * @param rtMode                response time measurement mode
     *                              (either END-TO-END or ADAPTER)
     * @param rtResolution          response time measurement resolution
     *                              (either Milliseconds or Nanoseconds)
     * @param sinkInstance          reference to the Sink instance to which
     *                              results must be forwarded
     * @param epService             Esper service instance
     * @param queryOutputName       name of output stream which this listener
     *                              subscribes to
     * @param queryText             Query text, expressed in Esper's EPL
     * @param querySchema           Schema of output stream
     * @param eventFormat           Either MAP or POJO
     */
    public EsperListener(String lsnrID, int rtMode, int rtResolution,
            Sink sinkInstance, EPServiceProvider epService,
            String queryOutputName, String queryText,
            LinkedHashMap<String, String> querySchema, int eventFormat) {
        super(lsnrID, rtMode, rtResolution, sinkInstance);
        this.epService = epService;
        this.queryText = queryText;
        this.querySchema = querySchema;
        this.queryOutputName = queryOutputName;
        this.setEventFormat(eventFormat);
    }

    @Override
    public void load() throws Exception {
        try {
            System.out.println("Loading query: \n" + queryText);
            query = epService.getEPAdministrator().createEPL(queryText, queryOutputName);
        } catch (Exception e) {
            throw new Exception("Could not create EPL statement ("
                                + e.getMessage() + ").");
        }
    }

    @Override
    public void run() {
        query.addListener(this);
    }

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        long timestamp = -1;
        if (this.rtMode == Globals.ADAPTER_RT) {
            if (this.rtResolution == Globals.MILLIS_RT) {
                timestamp = System.currentTimeMillis();
            } else if (this.rtResolution == Globals.NANO_RT) {
                timestamp = System.nanoTime();
            }
        }

        for (int i = 0; i < newEvents.length; i++) {
            processIncomingEvent(newEvents[i], timestamp);
        }
    }

    /**
     * Processes an event coming from Esper.
     *
     * @param event         the incoming event
     * @param timestamp     the timestamp associated with the event
     */
    private void processIncomingEvent(EventBean event, long timestamp) {
        onOutput(toFieldArray(event, timestamp)); //TODO: Remove this or optimize code
    }

    /**
     * Translates the event from the Esper native format to Array of Objects.
     *
     * @param event         The event in Esper's native representation
     * @param timestamp     The timestamp associated with the incoming event
     * @return      The event as an array of objects
     *
     */
    private Object[] toFieldArray(EventBean event, long timestamp) {
        Object[] eventObj = null;
        int fieldCount = 0;

        if (querySchema != null) { ////Input events are MAPs
            int i = 1;
            /* If response time is being measured, leave a slot for the arrival
             *  time of the event (filled here or at the Sink). */
            fieldCount = rtMode != Globals.NO_RT ? querySchema.size() + 2
                    : querySchema.size() + 1;
            eventObj = new Object[fieldCount];
            for (String att: querySchema.keySet()) {
                eventObj[i] = event.get(att);
                i++;
            }
        } else { //Input events are POJO
            try {
                Field[] fields = Class.forName(queryOutputName).getFields();
                /* If response time is being measured, leave a slot for the arrival time of the
                event (filled here or at the Sink). */
                fieldCount = rtMode != Globals.NO_RT ? fields.length + 2
                                                     : fields.length + 1;
                eventObj = new Object[fieldCount];
                int i = 1;
                for (Field f : fields) {
                    eventObj[i] = event.get(f.getName());
                    i++;
                }
            } catch (ClassNotFoundException cne) {
                System.err.println("The type \"" + queryOutputName + "\" has not been defined. ");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // First element is the stream name
        eventObj[0] = queryOutputName;
        // Last element is the arrival time
        if (rtMode == Globals.ADAPTER_RT) {
            eventObj[fieldCount - 1] = timestamp;
        }

        return eventObj;
    }

    /**
     *
     * Translates the event from the Esper native format to the framework's CSV format.
     *
     * @param event The event in Esper's native representation
     * @return      The event in CSV representation
     */
    public String toCSV(EventBean event) {
        StringBuilder sb = new StringBuilder();
        sb.append(queryOutputName);
        if (eventFormat == EsperInterface.MAP_FORMAT) { // Input events are MAPs
            if (querySchema != null) {
                for (String att: querySchema.keySet()) {
                    sb.append(Globals.CSV_DELIMITER);
                    sb.append(event.get(att));
                }
            }
        } else { //Input events are POJO
            try {
                for (Field f : Class.forName(queryOutputName).getFields()) {
                    sb.append(Globals.CSV_DELIMITER);
                    sb.append(event.get(f.getName()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    @Override
    public void disconnect() {
        if (query != null) {
            query.removeListener(this);
            if (!query.isStopped() && !query.isDestroyed()) {
                query.stop();
            }
            if (!query.isDestroyed()) {
                query.destroy();
            }
        }
    }

    /**
     * Sets the format used to exchange events with the Esper engine.
     *
     * @param eventFormat  the event format (either POJO or Map)
     */
    public void setEventFormat(int eventFormat) {
        if (eventFormat == EsperInterface.POJO_FORMAT) {
            this.eventFormat = eventFormat;
        } else {
            this.eventFormat = EsperInterface.MAP_FORMAT;
        }
    }

    /**
     *
     * @return     the format used to exchange events with the Esper engine.
     */
    public int getEventFormat() {
        return eventFormat;
    }

}
