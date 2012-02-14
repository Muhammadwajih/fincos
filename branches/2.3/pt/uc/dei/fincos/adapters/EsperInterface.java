package pt.uc.dei.fincos.adapters;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
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
import pt.uc.dei.fincos.basic.Datatype;
import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.basic.Status;
import pt.uc.dei.fincos.basic.Step;
import pt.uc.dei.fincos.data.CSVReader;
import pt.uc.dei.fincos.sink.Sink;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;


/**
 * Implementation of an adapter for Esper.
 * Notice that Esper runs in the same process as
 * the Adapter itself.
 *
 * @author Marcelo R.N. Mendes
 *
 */
public class EsperInterface extends CEPEngineInterface {
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
    protected static final int MAP_FORMAT = 0;
    protected static final int POJO_FORMAT = 1;

    /**
     *
     * @param connectionProperties      Parameters required for connecting with Esper
     */
    public EsperInterface(Properties connectionProperties) {
        this.status = new Status(Step.DISCONNECTED, 0);
        this.setConnProperties(connectionProperties);
    }

    @Override
    public boolean connect() throws Exception {
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

        parseStreamsList(queriesFile, esperConfigurationFile);

        this.epService = EPServiceProviderManager.getDefaultProvider(esperConfig);

        this.runtime = epService.getEPRuntime();
        this.status.setStep(Step.CONNECTED);
        this.unlistenedQueries = new ArrayList<EPStatement>();

        return true;
    }

    /**
     * Retrieves the list of input and output stream defined in Esper's configuration file
     * For streams whose events are represented as a Map, it's created a
     * Hash structure that stores name and type of its attributes.
     * For streams whose events are represented as POJO, a new Java class is created using
     * schema information from test setup file.
     *
     * @param queriesFile   Path to a file containing queries expressed in Esper's EPL query language
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
                        } else {
                            throw new Exception("Unsupported data type for attribute \""
                                    + attName + "\", in event type \"" + typeName + "\".");
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
    public void disconnect() {
        this.runtime.resetStats();
        this.status.setStep(Step.DISCONNECTED);

        // Stops all queries with a listener attached
        stopAllEventListeners();

        // Stops all "internal" queries
        for (EPStatement q: unlistenedQueries) {
            q.stop();
        }
    }

    @Override
    public String[] getInputStreamList() throws Exception {
        return inputStreamList != null ? inputStreamList : new String[0];
    }

    @Override
    public String[] getOutputStreamList() throws Exception {
        return outputStreamList != null
        ? outputStreamList
                : new String[0];
    }

    @Override
    public boolean load(HashMap<String, ArrayList<InetSocketAddress>> outputToSink)
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

            this.outputListeners = new EsperListener[outputToSink.size()];

            int i = 0;
            ArrayList<InetSocketAddress> sinksList;
            for (Entry<String, String> query : this.queryNamesAndTexts.entrySet()) {
                sinksList = outputToSink.get(query.getKey());
                if (sinksList != null && !sinksList.isEmpty()) {
                    outputListeners[i] =
                        new EsperListener(this.epService, query.getKey(),
                                query.getValue(),
                                this.streamsSchemas.get(query.getKey()),
                                sinksList, this.eventFormat);
                    outputListeners[i].rtMeasurementMode = this.getRtMeasurementMode();
                    outputListeners[i].communicationMode = Globals.ADAPTER_CSV_COMMUNICATION;
                    outputListeners[i].socketBufferSize = this.getSocketBufferSize();
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
                this.startAllEventListeners();
                this.status.setStep(Step.READY);
            } catch (Exception e) {
                throw new Exception("Could not load event listener (" + e.getMessage() + ").");
            }


            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean load(String[] outputStreams, Sink sinkInstance)
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

                for (int i = 0; i < outputStreams.length; i++) {
                    System.out.println("Listening to "  + outputStreams[i]);
                    outputListeners[i] = new EsperListener(this.epService, outputStreams[i],
                            queryNamesAndTexts.get(outputStreams[i]), this.streamsSchemas.get(outputStreams[i]),
                            sinkInstance, this.eventFormat);
                    outputListeners[i].rtMeasurementMode = this.getRtMeasurementMode();
                    outputListeners[i].communicationMode = Globals.DIRECT_API_COMMUNICATION;
                    outputListeners[i].socketBufferSize = this.getSocketBufferSize();
                    outputListeners[i].load();
                }

                try {
                    this.startAllEventListeners();
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
    public void sendEvent(String e) throws Exception {
        if (this.status.getStep() == Step.READY) {
            if (this.eventFormat == POJO_FORMAT) {
                sendPOJOEvent(e);
            } else {
                sendMapEvent(e);
            }
        }
    }

    @Override
    public void sendEvent(Event e) throws Exception {
        if (this.status.getStep() == Step.READY || this.status.getStep() == Step.CONNECTED) {
            if (this.eventFormat == POJO_FORMAT) {
                sendPOJOEvent(e);
            } else {
                sendMapEvent(e);
            }
        }
    }

    /**
     * Send a Map event to Esper.
     * Event record is initially represented a CSV String
     * and it is converted to a Map format before sending to Esper.
     *
     * @param event
     */
    private void sendMapEvent(String event) {
        Map<String, Object> mapEvent = new HashMap<String, Object>();
        int i = 1;
        String timestampFieldName = "";
        String[] eventPayload = CSVReader.split(event, Globals.CSV_SEPARATOR);
        String eventTypeName = eventPayload[0].substring(5);
        LinkedHashMap<String, String> eventSchema = streamsSchemas.get(eventTypeName);
        try {
            if (eventSchema != null) {
                for (Entry <String, String> field: eventSchema.entrySet()) {
                    if (field.getValue().equals("int")) {
                        mapEvent.put(field.getKey(), Integer.parseInt(eventPayload[i]));
                    } else if (field.getValue().equals("long")) {
                        mapEvent.put(field.getKey(), Long.parseLong(eventPayload[i]));
                    } else if (field.getValue().equals("string")) {
                        mapEvent.put(field.getKey(), eventPayload[i]);
                    } else if (field.getValue().equals("double")) {
                        mapEvent.put(field.getKey(), Double.parseDouble(eventPayload[i]));
                    } else if (field.getValue().equals("float")) {
                        mapEvent.put(field.getKey(), Float.parseFloat(eventPayload[i]));
                    }

                    i++;
                    timestampFieldName = field.getKey();
                }

                // If response time is measured at Adapter, add timestamp immediately before sending event to Esper
                if (this.getRtMeasurementMode() == Globals.ADAPTER_RT_NANOS) {
                    /* "event" already contains a timestamp at its last field (timestampFieldName)
					   added before calling sendEvent. Replaced here for removing the time spent
					   spent in format conversion from response time .
                     */
                    long timestamp = System.nanoTime();
                    mapEvent.put(timestampFieldName, timestamp);
                }
                /*System.out.println("Received event: [" + e + "] from Driver. Sending" +
						event.toString()+ " to Esper" + " at " +  new Date(System.currentTimeMillis()));*/
                synchronized (runtime) {
                    runtime.sendEvent(mapEvent, eventTypeName);
                }
            } else {
                System.err.println("Unknown event type \"" + eventTypeName + "\"." + "It is not possible to send event.");
            }
        } catch (ArrayIndexOutOfBoundsException arrExc) {
            System.err.println("ERROR: Number of fields in event \"" + event + "\" ("
                    + (eventPayload.length - 1) + ") does not match schema of event type \""
                    + eventTypeName + "\" (" + eventSchema.size() + ").");
        }
    }

    /**
     * Send a POJO event to Esper.
     * Event record is initially represented as a CSV String
     * and it is converted to a Plain Java Object before sending to Esper.
     *
     * @param event
     */
    private void sendPOJOEvent(String event) {
        String[] eventPayload = CSVReader.split(event, Globals.CSV_SEPARATOR);
        String eventTypeName = eventPayload[0].substring(5);
        try {
            Class<?> eventSchema = Class.forName(eventTypeName);
            Field[] eventFields = eventSchema.getDeclaredFields();
            Object pojoEvent = eventSchema.newInstance();

            if (eventFields.length != eventPayload.length - 1) {
                System.err.println("ERROR: Number of fields in event \"" + event + "\" ("
                        + (eventPayload.length - 1) + ") does not match schema of event type \""
                        + eventTypeName + "\" (" + eventFields.length + ").");
                return;
            }

            // Fill object attributes with event data
            Field f;
            for (int j = 0; j < eventFields.length; j++) {
                f = eventFields[j];
                if (f.getType() == int.class) {
                    f.setInt(pojoEvent, Integer.parseInt(eventPayload[j + 1]));
                } else if (f.getType() == long.class) {
                    f.setLong(pojoEvent, Long.parseLong(eventPayload[j + 1]));
                } else if (f.getType() == String.class) {
                    f.set(pojoEvent, eventPayload[j + 1]);
                } else if (f.getType() == double.class) {
                    f.setDouble(pojoEvent, Double.parseDouble(eventPayload[j + 1]));
                } else if (f.getType() == float.class) {
                    f.setFloat(pojoEvent, Float.parseFloat(eventPayload[j + 1]));
                }
            }

            // If response time is measured at Adapter, add timestamp immediately before sending event to Esper
            if (this.getRtMeasurementMode() == Globals.ADAPTER_RT_NANOS) {
                /* "event" already contains a timestamp at its last field (eventFields[eventFields.length-1])
				   added before calling sendEvent. Replaced here for removing the time
				   spent in format conversion from response time.*/

                long timestamp = System.nanoTime();
                eventFields[eventFields.length - 1].setLong(pojoEvent, timestamp);
            }

            synchronized (runtime) {
                runtime.sendEvent(pojoEvent);
            }

        } catch (ClassNotFoundException cnfe) {
            System.err.println("Unknown event type \"" + eventTypeName + "\"."
                    + "It is not possible to send event.");
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
     * Event record is initially represented using the FINCoS internal
     * format and it is converted to a Map format before sending to Esper.
     *
     * @param event
     */
    private void sendMapEvent(Event event) {
        Map<String, Object> mapEvent = new HashMap<String, Object>();
        int i = 0;
        String eventTypeName = event.getType().getName();
        LinkedHashMap<String, String> eventSchema = streamsSchemas.get(eventTypeName);
        try {
            if (eventSchema != null) {
                for (Entry <String, String> field: eventSchema.entrySet()) {
                    // If response time is measured and this is the last field, assign timestamp
                    if (this.getRtMeasurementMode() == Globals.END_TO_END_RT_MILLIS
                            && i == eventSchema.size() - 1) {
                        mapEvent.put(field.getKey(), event.getTimestamp());
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
        } catch (ArrayIndexOutOfBoundsException arrExc) {
            System.err.println("ERROR: Number of fields in event \"" + event + "\" (" + (event.getType().getAttributeCount())
                    + ") does not match schema of event type \"" + eventTypeName + "\" (" + eventSchema.size() + ").");
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

            int eventFieldCount = this.getRtMeasurementMode() == Globals.END_TO_END_RT_MILLIS
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

                // Assigns Timestamp
                if (this.getRtMeasurementMode() == Globals.END_TO_END_RT_MILLIS
                        && i == eventFields.length - 1) {
                    f.setLong(pojoEvent, event.getTimestamp());
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
                    schemaClass.addMethod(CtNewMethod.getter("get" + att.getName(), cfield));
                    schemaClass.addMethod(CtNewMethod.setter("set" + att.getName(), cfield));
                }
            }

            schemaClass.toClass();
        }
    }
}
