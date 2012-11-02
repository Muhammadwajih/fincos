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

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import pt.uc.dei.fincos.basic.Attribute;
import pt.uc.dei.fincos.basic.CSV_Event;
import pt.uc.dei.fincos.basic.Datatype;
import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.basic.Status;
import pt.uc.dei.fincos.basic.Step;
import pt.uc.dei.fincos.sink.Sink;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.time.CurrentTimeEvent;


/**
 * Implementation of an adapter for Esper.
 * NOTE: Esper runs embedded into the FINCos Daemon service.
 *
 * @author  Marcelo R.N. Mendes
 *
 */
public final class EsperInterface extends CEP_EngineInterface {
    /** Configuration of the Esper engine. */
    private Configuration esperConfig;

    /** Provider os Esper services. */
    private EPServiceProvider epService;

    /** Esper runtime. */
    private EPRuntime runtime;

    /** Stores schemas for streams whose events are represented as Maps.*/
    private HashMap<String, LinkedHashMap<String, String>> streamsSchemas;

    /** A map query identifier -> query EPL text.*/
    private LinkedHashMap<String, String> queryNamesAndTexts;

    /** List of input streams.*/
    private String[] inputStreamList;

    /** List of output streams.*/
    private String[] outputStreamList;

    /** List of queries for which there is no registered listener. */
    private ArrayList<EPStatement> unlistenedQueries;

    /** Format used to submit event to the Esper engine. */
    private int eventFormat;

    /** Events submitted as Maps. */
    protected static final int MAP_FORMAT = 0;

    /** Events submitted as Plain java Objects. */
    protected static final int POJO_FORMAT = 1;

    /** Indicates if an external clock should be used. */
    private boolean useExternalTimer;

    /** The name of the stream that carries time information. */
    private String extTSEventType;

    /** Index of the external timestamp field. */
    private int extTSIndex;

    /** External timestamp of the last received event. */
    private long lastExtTS;

    /** Single instance of Esper adapter. */
    private static EsperInterface instance;

    /**
     *
     * @param connProps     parameters required for connecting with Esper
     * @param rtMode        response time measurement mode
     *                      (either END-TO-END, ADAPTER or NO_RT)
     * @param rtResolution  response time measurement resolution
     *                      (either Milliseconds or Nanoseconds)
     *
     * @return  the single instance of Esper adapter
     */
    public static synchronized EsperInterface getInstance(Properties connProps,
            int rtMode, int rtResolution) {
        if (instance == null) {
            instance = new EsperInterface(connProps, rtMode, rtResolution);
        }
        return instance;
    }

    private static synchronized void destroyInstance() {
        instance = null;
    }

    /**
     *
     * @param connProps     parameters required for connecting with Esper
     * @param rtMode        response time measurement mode
     *                      (either END-TO-END, ADAPTER or NO_RT)
     * @param rtResolution  response time measurement resolution
     *                      (either Milliseconds or Nanoseconds)
     */
    private EsperInterface(Properties connProps, int rtMode, int rtResolution) {
        super(rtMode, rtResolution);
        this.status = new Status(Step.DISCONNECTED, 0);
        this.setConnProperties(connProps);
        try {
            String useExtClockStr = retrieveConnectionProperty("Use_external_timer");
            boolean useExtTimer = Boolean.parseBoolean(useExtClockStr);
            if (useExtTimer) {
                    this.extTSEventType = retrieveConnectionProperty("External_timer_stream");
                    String extTSFieldIndex = retrieveConnectionProperty("External_timer_index");
                    if (extTSEventType != null && extTSFieldIndex != null) {
                        this.extTSIndex = Integer.parseInt(extTSFieldIndex);
                        this.useExternalTimer = true;
                    } else {
                        this.extTSIndex = -1;
                        this.useExternalTimer = false;
                    }
            } else {
                this.useExternalTimer = false;
                this.extTSEventType = null;
                this.extTSIndex = -1;
            }
        } catch (Exception e) {
            System.err.println("Warning \"Use_External_Timer\" property is missing. "
                    + "Setting to default (false).");
            this.useExternalTimer = false;
            this.extTSEventType = null;
            this.extTSIndex = -1;
        }

        lastExtTS = -1;
    }

    @Override
    public synchronized boolean connect() throws Exception {
        try {
            String eventFormat = retrieveConnectionProperty("Event_format");
            if (eventFormat.equalsIgnoreCase("POJO")) {
                this.eventFormat = POJO_FORMAT;
            } else {
                this.eventFormat = MAP_FORMAT;
            }
        } catch (Exception e) {
            System.err.println("Warning \"Event_format\" property is missing. "
                    + "Setting to default: \"Map\" format.");
            this.eventFormat = MAP_FORMAT;
        }

        String queriesFile = retrieveConnectionProperty("Queries_path");
        String esperConfigurationFile = retrieveConnectionProperty("Configuration_file_path");

        this.esperConfig = new Configuration();
        this.esperConfig.configure(new File(esperConfigurationFile));
        if (useExternalTimer) {
            esperConfig.getEngineDefaults().getThreading().setInternalTimerEnabled(false);
        }

        parseStreamsList(queriesFile, esperConfigurationFile);

        this.epService = EPServiceProviderManager.getDefaultProvider(esperConfig);

        this.runtime = epService.getEPRuntime();
        this.status.setStep(Step.CONNECTED);
        this.unlistenedQueries = new ArrayList<EPStatement>();

        return true;
    }

    /**
     * Retrieves the list of input and output streams defined in Esper's
     * configuration file. For streams whose events are represented as a Map,
     * it's created a Hash structure that stores name and type of its
     * attributes. For streams whose events are represented as POJO, a new Java
     * class is created using schema information from test setup file.
     *
     * @param queriesFile   Path to a file containing queries expressed in
     *                      Esper's EPL query language
     * @param configFile    Path to Esper's configuration file
     * @throws Exception
     */
    private void parseStreamsList(String queriesFile, String configFile) throws Exception {
        ArrayList<String> inputStreams = new ArrayList<String>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();

        // Parsing of Queries file
        Document doc = builder.parse(new File(queriesFile));
        Element queriesList = doc.getDocumentElement();
        NodeList queries = queriesList.getElementsByTagName("Query");
        this.queryNamesAndTexts = new LinkedHashMap<String, String>(queries.getLength());
        Element query;
        String queryName, queryText;
        // Iterates over list of queries/output streams
        for (int i = 0; i < queries.getLength(); i++) {
            query = (Element) queries.item(i);
            queryName = query.getAttribute("name");
            queryText = query.getAttribute("text");
            queryNamesAndTexts.put(queryName, queryText);
        }
        this.outputStreamList = queryNamesAndTexts.keySet().toArray(new String[0]);

        // Parsing of Esper's config file (contains streams list)
        Document confDoc = builder.parse(new File(configFile));
        Element xmlFileRoot = confDoc.getDocumentElement();
        NodeList types = xmlFileRoot.getElementsByTagName("event-type");
        this.streamsSchemas = new HashMap<String, LinkedHashMap<String, String>>();
        Element type;
        String typeName, attName, attType;
        EventType eType;
        LinkedHashMap<String, String> typeAtts = null;
        // Iterates over all streams (input and output) defined in Esper's conf file
        for (int i = 0; i < types.getLength(); i++) {
            type = (Element) types.item(i);
            typeName = type.getAttribute("name");
            inputStreams.add(typeName);

            if (type.getElementsByTagName("java-util-map") != null
                    && type.getElementsByTagName("java-util-map").getLength() > 0) {
                NodeList attributes = ((Element) type.getElementsByTagName("java-util-map").item(0)).getElementsByTagName("map-property");
                // Add streams whose events are represented as Maps
                if (this.eventFormat == MAP_FORMAT) {
                    typeAtts = new LinkedHashMap<String, String>(attributes.getLength());
                    for (int j = 0; j < attributes.getLength(); j++) {
                        Element attribute = (Element) attributes.item(j);
                        attName = attribute.getAttribute("name");
                        attType = attribute.getAttribute("class");
                        typeAtts.put(attName, attType);
                    }
                    streamsSchemas.put(typeName, typeAtts);
                } else if (this.eventFormat == POJO_FORMAT) {
                    Attribute[] atts = new Attribute[attributes.getLength()];
                    for (int j = 0; j < attributes.getLength(); j++) {
                        Element attribute = (Element) attributes.item(j);
                        attName = attribute.getAttribute("name");
                        attType = attribute.getAttribute("class");
                        if (attType.equals("int")) {
                            atts[j] = new Attribute(Datatype.INTEGER, attName);
                        } else if (attType.equals("long")) {
                            atts[j] = new Attribute(Datatype.LONG, attName);
                        } else if (attType.equals("string")) {
                            atts[j] = new Attribute(Datatype.TEXT, attName);
                        } else if (attType.equals("double")) {
                            atts[j] = new Attribute(Datatype.DOUBLE, attName);
                        } else if (attType.equals("float")) {
                            atts[j] = new Attribute(Datatype.FLOAT, attName);
                        } else if (attType.equals("boolean")) {
                            atts[j] = new Attribute(Datatype.BOOLEAN, attName);
                        } else {
                            throw new Exception("Unsupported data type for"
                                    + " attribute \"" + attName
                                    + "\", in event type \"" + typeName
                                    + "\".");
                        }
                    }
                    eType = new EventType(typeName, atts);

                    // Load new class definition into JVM using javassist API
                    createBean(eType);

                    // Replace event definition as a Map by one as POJO
                    esperConfig.removeEventType(typeName, true);
                    esperConfig.addEventType(typeName, Class.forName(typeName).getName());
                }
            }
        }
        // Removes output streams from input streams list
        for (String outputStream: this.outputStreamList) {
            inputStreams.remove(outputStream);
        }
        this.inputStreamList = inputStreams.toArray(new String[0]);
    }

    @Override
    public synchronized void disconnect() {
        this.status.setStep(Step.DISCONNECTED);

        // Stops all queries with a listener attached
        stopAllListeners();

        // Stops all "internal" queries
        for (EPStatement q: unlistenedQueries) {
            if (!q.isStopped() && !q.isDestroyed()) {
                q.stop();
            }
            if (!q.isDestroyed()) {
                q.destroy();
            }
        }
        this.epService.destroy();
        destroyInstance();
    }

    @Override
    public synchronized String[] getInputStreamList() throws Exception {
        return inputStreamList != null ? inputStreamList : new String[0];
    }

    @Override
    public synchronized String[] getOutputStreamList() throws Exception {
        return outputStreamList != null
        ? outputStreamList
                : new String[0];
    }

    @Override
    public synchronized boolean load(String[] outputStreams, Sink sinkInstance)
    throws Exception {
        // This interface instance has already been loaded
        if (this.status.getStep() == Step.READY) {
            return true;
        } else { // If it is not connected yet, try to connect
            if (this.status.getStep() != Step.CONNECTED) {
                this.connect();
            }
        }

        if (this.status.getStep() == Step.CONNECTED) {
            this.status.setStep(Step.LOADING);

            if (outputStreams != null) {
                this.outputListeners = new EsperListener[outputStreams.length];
                int i = 0;
                // TODO: Change this to a mapping Listener->List-of-streams
                for (Entry<String, String> query : this.queryNamesAndTexts.entrySet()) {
                    if (hasListener(query.getKey(), outputStreams)) {
                        outputListeners[i] = new EsperListener("lsnr-0" + (i + 1),
                                rtMode, rtResolution, sinkInstance, this.epService,
                                query.getKey(), query.getValue(),
                                this.streamsSchemas.get(query.getKey()), this.eventFormat);
                        outputListeners[i].load();
                        i++;
                    } else {
                        System.err.println("WARNING: Query \"" + query.getKey() + "\" has no registered listener.");
                        System.out.println("Loading query: \n"  + query.getValue());
                        EPStatement st = epService.getEPAdministrator().createEPL(query.getValue(), query.getKey());
                        unlistenedQueries.add(st);
                    }
                }

                try {
                    this.startAllListeners();
                } catch (Exception e) {
                    throw new Exception("Could not load event listener (" + e.getMessage() + ").");
                }
            }

            this.status.setStep(Step.READY);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public synchronized void send(Event e) throws Exception {
        if (this.status.getStep() == Step.READY || this.status.getStep() == Step.CONNECTED) {
            if (this.useExternalTimer && e.getType().equals(extTSEventType)) {
                advanceClock((Long) e.getAttributeValue(extTSIndex));
            }

            if (this.eventFormat == POJO_FORMAT) {
                sendPOJOEvent(e);
            } else {
                sendMapEvent(e);
            }
        }
    }

    @Override
    public synchronized void send(CSV_Event event) {
        if (this.status.getStep() == Step.READY || this.status.getStep() == Step.CONNECTED) {
            if (this.useExternalTimer && event.getType().equals(extTSEventType)) {
                advanceClock(Long.parseLong(event.getPayload()[extTSIndex]));
            }

            if (this.eventFormat == POJO_FORMAT) {
                sendPOJOEvent(event);
            } else {
                sendMapEvent(event);
            }
        }
    }

    /**
     * Send a Map event to Esper.
     * Event record is initially represented using the FINCoS internal
     * format and it is converted to a Map format before sending to Esper.
     *
     * @param event
     */
    private void sendMapEvent(Event event) {
        String eventTypeName = event.getType().getName();
        LinkedHashMap<String, String> eventSchema = streamsSchemas.get(eventTypeName);

        if (eventSchema != null) {
            int fieldCount = this.rtMode != Globals.NO_RT
                                  ? event.getType().getAttributeCount() + 1
                                  : event.getType().getAttributeCount();

            if (eventSchema.size() != fieldCount) {
                System.err.println("ERROR: Number of fields in event \"" + event + "\" (" + (fieldCount)
                        + ") does not match schema of event type \"" + eventTypeName + "\" ("
                        + eventSchema.size() + ").");
                return;
            }

            Map<String, Object> mapEvent = new HashMap<String, Object>();
            int i = 0;

            for (Entry <String, String> field: eventSchema.entrySet()) {
                if (i == eventSchema.size() - 1) { // Timestamp field (last one, if there is)
                    if (this.rtMode == Globals.ADAPTER_RT) {
                        /* Assigns a timestamp to the event just after conversion
                              (i.e., just before sending the event to the target system) */
                        long timestamp = 0;
                        if (rtResolution == Globals.MILLIS_RT) {
                            timestamp = System.currentTimeMillis();
                        } else if (rtResolution == Globals.NANO_RT) {
                            timestamp = System.nanoTime();
                        }
                        mapEvent.put(field.getKey(), timestamp);
                    } else if (rtMode == Globals.END_TO_END_RT) {
                        // The timestamp comes from the Driver
                        mapEvent.put(field.getKey(), event.getTimestamp());
                    } else if (rtMode == Globals.NO_RT) {
                        mapEvent.put(field.getKey(), event.getAttributeValue(i));
                    }
                } else {
                    mapEvent.put(field.getKey(), event.getAttributeValue(i));
                }
                i++;
            }
            synchronized (runtime) {
                runtime.sendEvent(mapEvent, eventTypeName);
            }
        } else {
            System.err.println("Unknown event type \"" + eventTypeName + "\"."
                    + "It is not possible to send event.");
        }
    }

    /**
     * Send a POJO event to Esper.
     * Event record is initially represented using the FINCoS internal
     * format and it is converted to a Plain Java Object before sending to Esper.
     *
     * @param event
     */
    private void sendPOJOEvent(Event event) {
        String eventTypeName = event.getType().getName();
        try {
            Class<?> eventSchema = Class.forName(eventTypeName);
            Field[] eventFields = eventSchema.getDeclaredFields();
            Object pojoEvent = eventSchema.newInstance();

            int eventFieldCount = this.rtMode != Globals.NO_RT
                                  ? event.getType().getAttributeCount() + 1
                                  : event.getType().getAttributeCount();

            if (eventFields.length != eventFieldCount) {
                System.err.println("ERROR: Number of fields in event \"" + event + "\" (" + (eventFieldCount)
                        + ") does not match schema of event type \"" + eventTypeName + "\" ("
                        + eventFields.length + ").");
                return;
            }

            // Fill object attributes with event data
            Field f;
            for (int i = 0; i < eventFields.length; i++) {
                f = eventFields[i];
                try {
                    // Assigns Timestamp
                    if (i == eventFields.length - 1) { // timestamp field (the last one)
                        if (this.rtMode == Globals.ADAPTER_RT) {
                            /* Assigns a timestamp to the event just after conversion
                              (i.e., just before sending the event to the target system) */
                            long timestamp = 0;
                            if (rtResolution == Globals.MILLIS_RT) {
                                timestamp = System.currentTimeMillis();
                            } else if (rtResolution == Globals.NANO_RT) {
                                timestamp = System.nanoTime();
                            }
                            f.setLong(pojoEvent, timestamp);
                        } else if (rtMode == Globals.END_TO_END_RT) {
                            // The timestamp comes from the Driver
                            f.setLong(pojoEvent, event.getTimestamp());
                        }
                    } else {
                        if (f.getType() == int.class) {
                            f.setInt(pojoEvent, (Integer) event.getAttributeValue(i));
                        } else if (f.getType() == long.class) {
                            f.setLong(pojoEvent, (Long) event.getAttributeValue(i));
                        } else if (f.getType() == String.class) {
                            f.set(pojoEvent, event.getAttributeValue(i));
                        } else if (f.getType() == double.class) {
                            f.setDouble(pojoEvent, (Double) event.getAttributeValue(i));
                        } else if (f.getType() == float.class) {
                            f.setFloat(pojoEvent, (Float) event.getAttributeValue(i));
                        }
                    }
                } catch (ClassCastException cce) {
                    System.err.println("Invalid field value (" + event.getAttributeValue(i)
                                       + ") for field [" + f + "]. It is not possible to send event.");
                    return;
                }
            }

            synchronized (runtime) {
                runtime.sendEvent(pojoEvent);
            }
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Unknown event type \"" + eventTypeName + "\"." + "It is not possible to send event.");
            return;
        } catch (Exception e) {
            System.err.println("Unexpected exception: " + e.getMessage()
                    + ". It is not possible to send event.");
            e.printStackTrace();
            return;
        }
    }

    /**
     * Send a Map event to Esper.
     * Event record is initially represented as a FINCoS CSV record
     * and it is converted to a Map format before sending to Esper.
     *
     * @param event
     */
    private void sendMapEvent(CSV_Event event) {
        String eventTypeName = event.getType();
        LinkedHashMap<String, String> eventSchema = streamsSchemas.get(eventTypeName);

        if (eventSchema != null) {
            int fieldCount = this.rtMode != Globals.NO_RT
                                  ? event.getPayload().length + 1
                                  : event.getPayload().length;

            if (eventSchema.size() != fieldCount) {
                System.err.println("ERROR: Number of fields in event \"" + event + "\" (" + (fieldCount)
                        + ") does not match schema of event type \"" + eventTypeName + "\" ("
                        + eventSchema.size() + ").");
                return;
            }

            Map<String, Object> mapEvent = new HashMap<String, Object>();
            int i = 0;

            for (Entry <String, String> field: eventSchema.entrySet()) {
                if (i == eventSchema.size() - 1 && rtMode != Globals.NO_RT) { // Timestamp field (last one, if there is)
                    if (this.rtMode == Globals.ADAPTER_RT) {
                        /* Assigns a timestamp to the event just after conversion
                              (i.e., just before sending the event to the target system) */
                        long timestamp = 0;
                        if (rtResolution == Globals.MILLIS_RT) {
                            timestamp = System.currentTimeMillis();
                        } else if (rtResolution == Globals.NANO_RT) {
                            timestamp = System.nanoTime();
                        }
                        mapEvent.put(field.getKey(), timestamp);
                    } else if (rtMode == Globals.END_TO_END_RT) {
                        // The timestamp comes from the Driver
                        mapEvent.put(field.getKey(), event.getTimestamp());
                    }
                } else {
                    if (field.getValue().equals("int")) {
                        mapEvent.put(field.getKey(), Integer.parseInt(event.getPayload()[i]));
                    } else if (field.getValue().equals("long")) {
                        mapEvent.put(field.getKey(), Long.parseLong(event.getPayload()[i]));
                    } else if (field.getValue().equals("string")) {
                        mapEvent.put(field.getKey(), event.getPayload()[i]);
                    } else if (field.getValue().equals("double")) {
                        mapEvent.put(field.getKey(), Double.parseDouble(event.getPayload()[i]));
                    } else if (field.getValue().equals("float")) {
                        mapEvent.put(field.getKey(), Float.parseFloat(event.getPayload()[i]));
                    }
                }
                i++;
            }
            synchronized (runtime) {
                runtime.sendEvent(mapEvent, eventTypeName);
            }
        } else {
            System.err.println("Unknown event type \"" + eventTypeName + "\"."
                    + "It is not possible to send event.");
        }
    }

    /**
     * Send a POJO event to Esper.
     * Event record is initially represented using the FINCoS internal
     * format and it is converted to a Plain Java Object before sending to Esper.
     *
     * @param event
     */
    private void sendPOJOEvent(CSV_Event event) {
        String eventTypeName = event.getType();
        try {
            Class<?> eventSchema = Class.forName(eventTypeName);
            Field[] eventFields = eventSchema.getDeclaredFields();
            Object pojoEvent = eventSchema.newInstance();

            int eventFieldCount = this.rtMode != Globals.NO_RT
                                  ? event.getPayload().length + 1
                                  : event.getPayload().length;

            if (eventFields.length != eventFieldCount) {
                System.err.println("ERROR: Number of fields in event \"" + event + "\" (" + (eventFieldCount)
                        + ") does not match schema of event type \"" + eventTypeName + "\" ("
                        + eventFields.length + ").");
                return;
            }

            // Fill object attributes with event data
            Field f;
            for (int i = 0; i < eventFields.length; i++) {
                f = eventFields[i];

                // Assigns Timestamp
                if (i == eventFields.length - 1) { // timestamp field (the last one)
                    if (this.rtMode == Globals.ADAPTER_RT) {
                        /* Assigns a timestamp to the event just after conversion
                          (i.e., just before sending the event to the target system) */
                        long timestamp = 0;
                        if (rtResolution == Globals.MILLIS_RT) {
                            timestamp = System.currentTimeMillis();
                        } else if (rtResolution == Globals.NANO_RT) {
                            timestamp = System.nanoTime();
                        }
                        f.setLong(pojoEvent, timestamp);
                    } else if (rtMode == Globals.END_TO_END_RT) {
                        // The timestamp comes from the Driver
                        f.setLong(pojoEvent, event.getTimestamp());
                    }
                } else {
                    if (f.getType() == int.class) {
                        f.setInt(pojoEvent, Integer.parseInt(event.getPayload()[i]));
                    } else if (f.getType() == long.class) {
                        f.setLong(pojoEvent, Long.parseLong(event.getPayload()[i]));
                    } else if (f.getType() == String.class) {
                        f.set(pojoEvent, event.getPayload()[i]);
                    } else if (f.getType() == double.class) {
                        f.setDouble(pojoEvent, Double.parseDouble(event.getPayload()[i]));
                    } else if (f.getType() == float.class) {
                        f.setFloat(pojoEvent, Float.parseFloat(event.getPayload()[i]));
                    }
                }
            }

            synchronized (runtime) {
                runtime.sendEvent(pojoEvent);
            }
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Unknown event type \"" + eventTypeName + "\"." + "It is not possible to send event.");
            return;
        } catch (ClassCastException cce) {
            System.err.println("Invalid field value. It is not possible to send event.");
            return;
        } catch (Exception e) {
            System.err.println("Unexpected exception: " + e.getMessage()
                    + ". It is not possible to send event.");
            e.printStackTrace();
            return;
        }
    }

    /**
     * Creates and loads into JVM a class with the same schema as the event type
     * passed as argument.
     *
     * @param eType			Schema of new class
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    private void createBean(EventType eType) throws CannotCompileException, NotFoundException {
        try {
            Class.forName(eType.getName());
        } catch (ClassNotFoundException e) {
            ClassPool pool = ClassPool.getDefault();
            CtClass schemaClass = pool.makeClass(eType.getName());

            for (Attribute att : eType.getAttributes()) {
                CtField cfield = null;
                switch (att.getType()) {
                case INTEGER:
                    cfield = new CtField(CtClass.intType, att.getName(), schemaClass);
                    break;
                case FLOAT:
                    cfield = new CtField(CtClass.floatType, att.getName(), schemaClass);
                    break;
                case DOUBLE:
                    cfield = new CtField(CtClass.doubleType, att.getName(), schemaClass);
                    break;
                case TEXT:
                    cfield = new CtField(pool.get("java.lang.String"), att.getName(), schemaClass);
                    break;
                case BOOLEAN:
                    cfield = new CtField(CtClass.booleanType, att.getName(), schemaClass);
                    break;
                case LONG:
                    cfield = new CtField(CtClass.longType, att.getName(), schemaClass);
                    break;
                case TIMESTAMP:
                    cfield = new CtField(CtClass.longType, att.getName(), schemaClass);
                    break;
                }
                if (cfield != null) {
                    cfield.setModifiers(Modifier.PUBLIC);
                    schemaClass.addField(cfield);
                    schemaClass.addMethod(CtNewMethod.getter("get" + att.getName().substring(0, 1).toUpperCase() + att.getName().substring(1), cfield));
                    schemaClass.addMethod(CtNewMethod.setter("set" + att.getName().substring(0, 1).toUpperCase() + att.getName().substring(1), cfield));
                }
            }

            schemaClass.toClass();
        }
    }

    private boolean hasListener(String queryName, String[] listenedQueries) {
        for (int i = 0; i < listenedQueries.length; i++) {
            if (queryName.equals(listenedQueries[i])) {
                return true;
            }
        }
        return false;
    }

    private void advanceClock(Long extTimestamp) {
        if (lastExtTS == -1) {
            lastExtTS = extTimestamp;
        }

        if (extTimestamp != lastExtTS) { // Time advanced
            this.runtime.sendEvent(new CurrentTimeEvent(extTimestamp));
        }

    }
}
