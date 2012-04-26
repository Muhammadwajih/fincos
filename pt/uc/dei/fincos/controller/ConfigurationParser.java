package pt.uc.dei.fincos.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pt.uc.dei.fincos.basic.Attribute;
import pt.uc.dei.fincos.basic.Datatype;
import pt.uc.dei.fincos.basic.Domain;
import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.basic.PredefinedListDomain;
import pt.uc.dei.fincos.basic.RandomDomain;
import pt.uc.dei.fincos.basic.SequentialDomain;
import pt.uc.dei.fincos.driver.ExternalFileWorkloadPhase;
import pt.uc.dei.fincos.driver.Scheduler.ArrivalProcess;
import pt.uc.dei.fincos.driver.SyntheticWorkloadPhase;
import pt.uc.dei.fincos.driver.WorkloadPhase;
import pt.uc.dei.fincos.random.ConstantVariate;
import pt.uc.dei.fincos.random.RandomExponentialVariate;
import pt.uc.dei.fincos.random.RandomNormalVariate;
import pt.uc.dei.fincos.random.RandomUniformVariate;
import pt.uc.dei.fincos.random.Variate;


/**
 * Class used to load and save configuration files containing tests setup
 *
 * @author Marcelo R.N. Mendes
 *
 */
public class ConfigurationParser {
    /** Root of the xml setup file. */
    private Element xmlFileRoot;

    /** Setup file. */
    private File configFile;

    /**
     * Opens a XML file containing test setup.
     *
     * @param path			Path to configuration file
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void open(String path) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        configFile = new File(path);
        Document doc = builder.parse(configFile);
        this.xmlFileRoot = doc.getDocumentElement();
    }

    /**
     * Saves a test setup into a new configuration file
     *
     * @param drivers						List of configured Drivers
     * @param sinks							List of configured Sinks
     * @param socketBufferSize				Test option
     * @param logFlushInterval  			Test option
     * @param communicationMode				Test option
     * @param rtMeasurementMode				Test option
     * @param cepInterfacePropertiesFile	Test option
     * @param path				Path of the new configuration file to be saved
     * @throws ParserConfigurationException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws TransformerException
     */
    public void saveAs(DriverConfig[] drivers, SinkConfig[] sinks,
            int socketBufferSize, int logFlushInterval,
            int communicationMode, int rtMeasurementMode,
            boolean useEventsCreationTime,
            String cepInterfacePropertiesFile,
            String path) throws ParserConfigurationException, FileNotFoundException, IOException, TransformerException {
        this.configFile = new File(path);
        this.save(drivers, sinks, socketBufferSize, logFlushInterval,
                communicationMode, rtMeasurementMode,useEventsCreationTime,
                cepInterfacePropertiesFile);
    }

    /**
     * Saves a test setup into the current configuration file
     *
     * @param drivers                       List of configured Drivers
     * @param sinks                         List of configured Sinks
     * @param socketBufferSize		        Test option
     * @param logFlushInterval              Test option
     * @param communicationMode             Test option
     * @param rtMeasurementMode             Test option
     * @param useEventsCreationTime         Test option
     * @param cepInterfacePropertiesFile    Test option
     *
     * @throws ParserConfigurationException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws TransformerException
     */
    public void save(DriverConfig[] drivers, SinkConfig[] sinks,
            int socketBufferSize, int logFlushInterval,
            int communicationMode, int rtMeasurementMode,
            boolean useEventsCreationTime,
            String cepInterfacePropertiesFile)
    throws ParserConfigurationException, FileNotFoundException, IOException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("FINCoS");
        doc.appendChild(root);

        Element testOptions = doc.createElement("TestOptions");
        Element driverList = doc.createElement("Drivers");
        Element sinkList = doc.createElement("Sinks");

        root.appendChild(testOptions);
        root.appendChild(driverList);
        root.appendChild(sinkList);

        String timestampingMode = "SEND_TIME";
        if(useEventsCreationTime)
            timestampingMode = "CREATION_TIME";

        testOptions.setAttribute("LOG_FLUSH_INTERVAL", Integer.toString(logFlushInterval));
        testOptions.setAttribute("SOCKET_BUFFER_SIZE", Integer.toString(socketBufferSize));
        testOptions.setAttribute("COMMUNICATION_MODE", Integer.toString(communicationMode));
        testOptions.setAttribute("RESPONSE_TIME_MEASUREMENT_MODE", Integer.toString(rtMeasurementMode));
        testOptions.setAttribute("TIMESTAMPING_MODE", timestampingMode);
        testOptions.setAttribute("CEP_ENGINE_INTERFACE_CONN_PROPERTIES", cepInterfacePropertiesFile);

        Element driver;
        for (DriverConfig dr : drivers) {
            driver = doc.createElement("Driver");
            driver.setAttribute("name", dr.getAlias());
            driver.setAttribute("address", dr.getAddress().getHostAddress());
            driver.setAttribute("threadCount", dr.getThreadCount()+"");

            Element server = doc.createElement("Server");
            server.setAttribute("address", dr.getServerAddress().getHostAddress());
            server.setAttribute("port", ""+dr.getServerPort());
            driver.appendChild(server);

            Element workload, phase, schema;
            workload = doc.createElement("Workload");
            SyntheticWorkloadPhase syntheticPhase;
            ExternalFileWorkloadPhase externalFilePhase;
            for (WorkloadPhase w : dr.getWorkload()) {
                if(w instanceof SyntheticWorkloadPhase) {
                    syntheticPhase = (SyntheticWorkloadPhase) w;
                    phase = doc.createElement("Phase");
                    phase.setAttribute("type", "Synthetic");
                    Element duration = doc.createElement("duration");
                    duration.appendChild(doc.createTextNode(""+syntheticPhase.getDuration()));
                    Element initialRate = doc.createElement("initialRate");
                    initialRate.appendChild(doc.createTextNode(""+syntheticPhase.getInitialRate()));
                    Element finalRate = doc.createElement("finalRate");
                    finalRate.appendChild(doc.createTextNode(""+syntheticPhase.getFinalRate()));
                    Element arrivalProcess = doc.createElement("arrivalProcess");
                    arrivalProcess.appendChild(doc.createTextNode(""+syntheticPhase.getArrivalProcess()));
                    phase.appendChild(duration);
                    phase.appendChild(initialRate);
                    phase.appendChild(finalRate);
                    phase.appendChild(arrivalProcess);
                    schema = this.saveSchema(doc, syntheticPhase.getSchema());
                    schema.setAttribute("deterministicMix", ""+syntheticPhase.isDeterministicEventMix());
                    phase.appendChild(schema);
                    Element dataGen = doc.createElement("DataGeneration");
                    if(syntheticPhase.getDataGenMode() == SyntheticWorkloadPhase.RUNTIME)
                        dataGen.setAttribute("mode", "Runtime");
                    else
                        dataGen.setAttribute("mode", "Dataset");
                    Long randomSeed = syntheticPhase.getRandomSeed();
                    if(randomSeed != null)
                        dataGen.setAttribute("randomSeed", ""+randomSeed);
                    phase.appendChild(dataGen);

                    workload.appendChild(phase);
                }
                else if(w instanceof ExternalFileWorkloadPhase) {
                    externalFilePhase = (ExternalFileWorkloadPhase) w;
                    phase = doc.createElement("Phase");
                    phase.setAttribute("type", "External File");
                    Element path = doc.createElement("path");
                    path.appendChild(doc.createTextNode(externalFilePhase.getFilePath()));
                    Element loopCount = doc.createElement("loopCount");
                    loopCount.appendChild(doc.createTextNode(""+externalFilePhase.getLoopCount()));
                    Element timestamps = doc.createElement("timestamps");
                    boolean containsTS = externalFilePhase.containsTimestamps();
                    boolean useTS = externalFilePhase.isUsingTimestamps();
                    int timeUnit = externalFilePhase.getTimestampUnit();
                    double eventRate = externalFilePhase.getEventSubmissionRate();
                    timestamps.setAttribute("contains", ""+containsTS);
                    if(containsTS) {
                        timestamps.setAttribute("use", ""+useTS);
                        if(useTS) {
                            timestamps.setAttribute("timeUnit", ""+timeUnit);
                        }
                        else {
                            timestamps.setAttribute("eventRate", ""+eventRate);
                        }
                    }
                    else {
                        timestamps.setAttribute("eventRate", ""+eventRate);
                    }
                    Element eventTypes = doc.createElement("eventTypes");
                    boolean containsEventTypes = externalFilePhase.containsEventTypes();
                    String singleTypeName = externalFilePhase.getSingleEventTypeName();
                    eventTypes.setAttribute("contains", ""+containsEventTypes);
                    if(!containsEventTypes)
                        eventTypes.setAttribute("singleTypeName", singleTypeName);

                    phase.appendChild(path);
                    phase.appendChild(loopCount);
                    phase.appendChild(timestamps);
                    phase.appendChild(eventTypes);
                    workload.appendChild(phase);
                }

            }
            driver.appendChild(workload);

            Element logging = doc.createElement("Logging");
            logging.setAttribute("log", ""+dr.isLoggingEnabled());
            if(dr.isLoggingEnabled()) {
                logging.setAttribute("fieldsToLog", dr.getFieldsToLog()==Globals.LOG_ALL_FIELDS
                        ?"all":"timestamps");
                logging.setAttribute("samplingRate", ""+dr.getLoggingSamplingRate());
            }
            driver.appendChild(logging);

            Element validation = doc.createElement("Validation");
            validation.setAttribute("validate", ""+dr.isValidationEnabled());
            if(dr.isValidationEnabled()) {
                validation.setAttribute("validatorAddress", dr.getValidatorAddress().getHostAddress());
                validation.setAttribute("validatorPort", ""+dr.getValidatorPort());
                validation.setAttribute("samplingRate", ""+dr.getValidationSamplingRate());
            }
            driver.appendChild(validation);

            driverList.appendChild(driver);
        }

        Element sink;

        for (SinkConfig sinkCfg : sinks) {
            sink = doc.createElement("Sink");
            sink.setAttribute("name", sinkCfg.getAlias());
            sink.setAttribute("address", sinkCfg.getAddress().getHostAddress());
            sink.setAttribute("port", sinkCfg.getPort()+"");
            Element server = doc.createElement("Server");
            server.setAttribute("address", sinkCfg.getServerAddress().getHostAddress());
            sink.appendChild(server);

            Element stream;
            for(String streamName : sinkCfg.getOutputStreamList()) {
                stream = doc.createElement("Stream");
                stream.appendChild(doc.createTextNode(streamName));
                sink.appendChild(stream);
            }

            Element logging = doc.createElement("Logging");
            logging.setAttribute("log", ""+sinkCfg.isLoggingEnabled());
            if(sinkCfg.isLoggingEnabled()) {
                logging.setAttribute("fieldsToLog", sinkCfg.getFieldsToLog()==Globals.LOG_ALL_FIELDS
                        ?"all":"timestamps");
                logging.setAttribute("samplingRate", ""+sinkCfg.getLoggingSamplingRate());
            }
            sink.appendChild(logging);

            Element validation = doc.createElement("Validation");
            validation.setAttribute("validate", ""+sinkCfg.isValidationEnabled());
            if(sinkCfg.isValidationEnabled()) {
                validation.setAttribute("validatorAddress", sinkCfg.getValidatorAddress().getHostAddress());
                validation.setAttribute("validatorPort", ""+sinkCfg.getValidatorPort());
                validation.setAttribute("samplingRate", ""+sinkCfg.getValidationSamplingRate());
            }
            sink.appendChild(validation);

            sinkList.appendChild(sink);
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(this.configFile, false));
        bw.write(fromXMLDocToString(doc));
        bw.flush();
        bw.close();
        this.xmlFileRoot = doc.getDocumentElement();
    }

    /**
     * Indicates if the parser already has a configuration file open

     * @return
     */
    public boolean isFileOpen() {
        return (this.xmlFileRoot != null);
    }

    /**
     * Unloads parser's configuration file
     *
     */
    public void closeFile() {
        this.xmlFileRoot = null;
    }

    /**
     * Retrieves the list of Drivers in this parser's configuration file
     *
     * @return			A list of configurations of Drivers
     * @throws Exception
     * @throws DOMException
     * @throws NumberFormatException
     */
    public DriverConfig[] retrieveDriverList() throws NumberFormatException, DOMException, Exception  {
        DriverConfig ret[] = null;

        if(isFileOpen()) {
            Element driversList =(Element)this.xmlFileRoot.getElementsByTagName("Drivers").item(0);

            NodeList drivers = driversList.getElementsByTagName("Driver");
            ret = new DriverConfig[drivers.getLength()];

            Element driver;
            String driverName;
            InetAddress driverAddress, serverAddress, validatorAddress=null;
            int serverPort=0, validatorPort=0, threadCount=1;
            int fieldsToLog = 0;
            boolean validate, log;
            double validSamplingRate=0;
            double logSamplingRate=0;

            // Iterates over driver list
            for (int i = 0; i < drivers.getLength(); i++) {
                driver = (Element)drivers.item(i);
                driverName = driver.getAttribute("name");
                driverAddress = InetAddress.getByName(driver.getAttribute("address"));
                threadCount = Integer.parseInt(driver.getAttribute("threadCount"));
                Element workload = (Element) driver.getElementsByTagName("Workload").item(0);
                NodeList phases = workload.getElementsByTagName("Phase");
                WorkloadPhase wps []= new WorkloadPhase[phases.getLength()];
                Element phase;
                // Iterates over phases of a driver
                for (int j = 0; j < phases.getLength(); j++) {
                    phase = (Element)phases.item(j);
                    wps[j] = processPhase(phase);
                }
                Element server = (Element) driver.getElementsByTagName("Server").item(0);
                serverAddress = InetAddress.getByName(server.getAttribute("address"));
                serverPort = Integer.parseInt(server.getAttribute("port"));

                Element logging = (Element) driver.getElementsByTagName("Logging").item(0);
                log = Boolean.parseBoolean(logging.getAttribute("log"));
                if(log) {
                    if(logging.getAttribute("fieldsToLog").equalsIgnoreCase("all"))
                        fieldsToLog = Globals.LOG_ALL_FIELDS;
                    else
                        fieldsToLog = Globals.LOG_ONLY_TIMESTAMPS;
                    logSamplingRate = Double.parseDouble(logging.getAttribute("samplingRate"));
                }

                Element validation = (Element) driver.getElementsByTagName("Validation").item(0);
                validate = Boolean.parseBoolean(validation.getAttribute("validate"));
                if(validate) {
                    validatorAddress = InetAddress.getByName(validation.getAttribute("validatorAddress"));
                    validatorPort = Integer.parseInt(validation.getAttribute("validatorPort"));
                    validSamplingRate = Double.parseDouble(validation.getAttribute("samplingRate"));
                }

                ret[i] = new DriverConfig(driverName, driverAddress, wps,
                        serverAddress, serverPort, threadCount,
                        log, fieldsToLog, logSamplingRate,
                        validate, validatorAddress, validatorPort,
                        validSamplingRate);
            }
        }

        return ret;
    }

    /**
     * Retrieves the list of all input streams configured for all Drivers
     *
     * @return	The schemas of the input streams
     */
    public EventType[] getInputStreamList() throws NumberFormatException, DOMException, Exception {
        Set<EventType> inputStreamList = new HashSet<EventType>();
        for (DriverConfig dr : retrieveDriverList()) {
            for (EventType streamSchema : dr.getStreamList()) {
                inputStreamList.add(streamSchema);
            }
        }

        return inputStreamList.toArray(new EventType[0]);
    }

    /**
     * Retrieves the names of all input streams configured for all Drivers
     *
     * @return	The names of the input streams
     */
    public String[] getInputStreamNames() throws NumberFormatException, DOMException, Exception {
        EventType[] types = this.getInputStreamList();
        String [] ret = new String [types.length];

        for (int i = 0; i < ret.length; i++) {
            ret[i] = types[i].getName();
        }

        return ret;
    }

    /**
     * Retrieves the names of all output streams configured for all Sinks
     *
     * @return	The names of the output streams
     */
    public String[] getOutputStreamNames() throws Exception{
        Set<String> outputStreamList = new HashSet<String>();
        for (SinkConfig sink : retrieveSinkList()) {
            for (String streamSchema : sink.getOutputStreamList()) {
                outputStreamList.add(streamSchema);
            }
        }

        return outputStreamList.toArray(new String[0]);
    }

    /**
     * Retrieves the test option "communication mode"
     *
     * @return Either DIRECT_API_COMMUNICATION or ADAPTER_CSV_COMMUNICATION
     */
    public int getCommunicationMode() {
        if(isFileOpen()) {
            Element testOptions =(Element)this.xmlFileRoot.getElementsByTagName("TestOptions").item(0);
            if(testOptions != null) {
                int communicationMode =Integer.parseInt(testOptions.getAttribute("COMMUNICATION_MODE"));
                if(communicationMode != Globals.ADAPTER_CSV_COMMUNICATION &&
                        communicationMode != Globals.DIRECT_API_COMMUNICATION) {
                    System.err.println("Invalid RT communication mode. Setting to default.");
                    communicationMode = Globals.ADAPTER_CSV_COMMUNICATION; // Default mode
                }
                return communicationMode;
            }
            else {
                System.err.println("WARNING: Could not retrieve communication mode. Setting to default.");
                return Globals.ADAPTER_CSV_COMMUNICATION; // Default mode
            }
        }
        else
            return Globals.ADAPTER_CSV_COMMUNICATION;
    }

    /**
     * Retrieves the test option "response time measurement mode"
     *
     * @return Either END_TO_END_RT_MILLIS, ADAPTER_RT_NANOS or NO_RT
     */
    public int getResponseTimeMeasurementMode() {
        if(isFileOpen()) {
            Element testOptions =(Element)this.xmlFileRoot.getElementsByTagName("TestOptions").item(0);
            if(testOptions != null) {
                int rtMeasurementMode = Integer.parseInt(testOptions.getAttribute("RESPONSE_TIME_MEASUREMENT_MODE"));
                if(rtMeasurementMode != Globals.ADAPTER_RT_NANOS &&
                        rtMeasurementMode != Globals.END_TO_END_RT_MILLIS &&
                        rtMeasurementMode != Globals.NO_RT) {
                    System.err.println("Invalid RT measurement mode. Setting to default.");
                    rtMeasurementMode = Globals.END_TO_END_RT_MILLIS; // Default mode
                }

                if(rtMeasurementMode == Globals.ADAPTER_RT_NANOS &&
                        this.getCommunicationMode() == Globals.DIRECT_API_COMMUNICATION) {
                    System.err.println("WARNING: It is not allowed to mix \"ADAPTER_RT_NANOS\" RT measurement mode with "+
                            "\"DIRECT_API_COMMUNICATION\" communication mode." +
                    " Setting RT mode to \"END_TO_END_RT_MILLIS\"" );
                    rtMeasurementMode = Globals.END_TO_END_RT_MILLIS;
                }


                return rtMeasurementMode;
            }
            else {
                System.err.println("WARNING: Could not retrieve RT measurement mode. Setting to default.");
                return Globals.END_TO_END_RT_MILLIS; // Default mode
            }

        }
        else
            return Globals.END_TO_END_RT_MILLIS;
    }

    public boolean isUsingCreationTime() {
        if(isFileOpen()) {
            Element testOptions =(Element)this.xmlFileRoot.getElementsByTagName("TestOptions").item(0);
            if(testOptions != null) {
                String timestampingMode = (testOptions.getAttribute("TIMESTAMPING_MODE"));
                if(timestampingMode != null && !timestampingMode.isEmpty()) {
                    if(timestampingMode.equalsIgnoreCase("CREATION_TIME"))
                        return true;
                    else if(timestampingMode.equalsIgnoreCase("SEND_TIME"))
                        return false;
                    else {
                        System.err.println("WARNING: Invalid timestamping mode. Setting to default (SEND_TIME).");
                        return false;
                    }
                }
                else
                    return false; // Default mode
            }
            else {
                System.err.println("WARNING: Could not retrieve timestamping mode. Setting to default (SEND_TIME).");
                return false; // Default mode
            }
        }
        else
            return false;

    }

    /**
     * Returns the the interval at which log entries are flushed to disk
     *
     * @return
     */
    public int getLogFlushInterval(){
        if(isFileOpen()) {
            Element testOptions =(Element)this.xmlFileRoot.getElementsByTagName("TestOptions").item(0);
            if(testOptions != null) {
                int logFlushInterval =Integer.parseInt(testOptions.getAttribute("LOG_FLUSH_INTERVAL"));
                if(logFlushInterval < 0) {
                    System.err.println("Invalid log flush interval. Setting to default (10ms).");
                    logFlushInterval = 10;
                }

                return logFlushInterval;
            }
            else {
                System.err.println("WARNING: Could not retrieve log flush interval. Setting to default (10ms).");
                return 10; // Default value
            }

        }
        else
            return 10; // Default value
    }

    /**
     * Returns the buffer size used in the communication between Drivers/Sinks and Adapter
     *
     * @return
     */
    public int getSocketBufferSize() {
        if(isFileOpen()) {
            Element testOptions =(Element)this.xmlFileRoot.getElementsByTagName("TestOptions").item(0);
            if(testOptions != null) {
                int bufferSize =Integer.parseInt(testOptions.getAttribute("SOCKET_BUFFER_SIZE"));
                if(bufferSize < 1) {
                    System.err.println("Invalid buffer size. Setting to default.");
                    bufferSize = 1;
                }
                return bufferSize;
            }
            else {
                System.err.println("WARNING: Could not retrieve socket buffer size. Setting to default.");
                return 1; // Default value (no buffering)
            }

        }
        else
            return 1; // Default value (no buffering)
    }

    /**
     * Returns the path to a file containing connection properties in case of direct
     * connection between Drivers/Sinks and a CEP engine.
     *
     * @return
     */
    public String getCEPInterfaceConnPropertiesFile() {
        String connPropertiesFilePath = "";
        if(isFileOpen()) {
            Element testOptions =(Element)this.xmlFileRoot.getElementsByTagName("TestOptions").item(0);
            if(testOptions != null) {
                connPropertiesFilePath = testOptions.getAttribute("CEP_ENGINE_INTERFACE_CONN_PROPERTIES");
            }
        }
        return connPropertiesFilePath;
    }

    /**
     * Parses the configuration of a Driver's phase
     *
     * @param phase			A XML element containing the configuration of a phase
     * @return				Either an instance of <tt>SyntheticWorkloadPhase</tt> or of <tt>ExternalFileWorkloadPhase</tt>
     * @throws NumberFormatException
     * @throws DOMException
     * @throws Exception
     */
    private WorkloadPhase processPhase(Element phase) throws NumberFormatException, DOMException, Exception {
        String type = phase.getAttribute("type");
        WorkloadPhase ret = null;

        if(type.equalsIgnoreCase("Synthetic")) {
            int phaseDuration;
            double phaseInitialRate, phaseFinalRate;

            phaseDuration = Integer.parseInt(
                    phase.getElementsByTagName("duration").item(0).getFirstChild().getNodeValue());
            phaseInitialRate = Double.parseDouble(
                    phase.getElementsByTagName("initialRate").item(0).getFirstChild().getNodeValue());
            phaseFinalRate = Double.parseDouble(
                    phase.getElementsByTagName("finalRate").item(0).getFirstChild().getNodeValue());
            ArrivalProcess arrivalProcess;

            if(phase.getElementsByTagName("arrivalProcess").item(0)!=null) {
                String  arrivaProcessStr = phase.getElementsByTagName("arrivalProcess").item(0).getFirstChild().getNodeValue();
                if(arrivaProcessStr != null && !arrivaProcessStr.isEmpty()) {
                    if(arrivaProcessStr.equalsIgnoreCase("DETERMINISTIC"))
                        arrivalProcess = ArrivalProcess.DETERMINISTIC;
                    else if (arrivaProcessStr.equalsIgnoreCase("POISSON"))
                        arrivalProcess = ArrivalProcess.POISSON;
                    else {
                        System.err.println("WARNING: Invalid arrival process. Setting to default: DETERMINISTIC");
                        arrivalProcess = ArrivalProcess.DETERMINISTIC;
                    }

                }
                else {
                    arrivalProcess = ArrivalProcess.DETERMINISTIC;
                }
            }
            else { // older versions of configuration file
                arrivalProcess = ArrivalProcess.DETERMINISTIC;
            }

            Element schema = (Element)phase.getElementsByTagName("Schema").item(0);
            Element dataGen = (Element)phase.getElementsByTagName("DataGeneration").item(0);
            String dataGenModeStr  = dataGen.getAttribute("mode");
            int dataGenMode;
            if(dataGenModeStr.equalsIgnoreCase("Runtime"))
                dataGenMode = SyntheticWorkloadPhase.RUNTIME;
            else
                dataGenMode = SyntheticWorkloadPhase.DATASET;

            Long randomSeed = null;
            String randomSeedStr = dataGen.getAttribute("randomSeed");
            if(randomSeedStr != null && !randomSeedStr.isEmpty())
                randomSeed = Long.parseLong(randomSeedStr);

            boolean deterministicEventMix;
            String deterministicEventMixStr = schema.getAttribute("deterministicMix");
            if(deterministicEventMixStr != null && !deterministicEventMixStr.isEmpty()) {
                deterministicEventMix = Boolean.parseBoolean(deterministicEventMixStr);
            }
            else { // older versions of configuration file
                deterministicEventMix = false;
            }

            ret = new SyntheticWorkloadPhase(phaseDuration, phaseInitialRate,  phaseFinalRate,
                    arrivalProcess, this.getSchema(schema, randomSeed),
                    deterministicEventMix, dataGenMode, randomSeed);
        }
        else if(type.equalsIgnoreCase("External File")) {
            String path = phase.getElementsByTagName("path").item(0).getFirstChild().getNodeValue();

            int loopCount;
            if(phase.getElementsByTagName("loopCount").item(0) != null) {
                loopCount = Integer.parseInt(phase.getElementsByTagName("loopCount").item(0).getFirstChild().getNodeValue());
            }
            else { // older versions of configuration file
                loopCount = 1;
            }

            Element timestamps = (Element) phase.getElementsByTagName("timestamps").item(0);
            boolean containsTS = Boolean.parseBoolean(timestamps.getAttribute("contains"));
            boolean useTS = Boolean.parseBoolean(timestamps.getAttribute("use"));
            int timeUnit = 0;
            double eventRate = 1;
            if(containsTS && useTS)
                timeUnit = Integer.parseInt(timestamps.getAttribute("timeUnit"));
            else {
                eventRate = Double.parseDouble(timestamps.getAttribute("eventRate"));
            }
            Element eventTypes = (Element) phase.getElementsByTagName("eventTypes").item(0);
            boolean containsTypes = Boolean.parseBoolean(eventTypes.getAttribute("contains"));
            String singleTypeName = eventTypes.getAttribute("singleTypeName");
            ret = new ExternalFileWorkloadPhase(path, loopCount,containsTS, useTS, timeUnit,
                    eventRate, containsTypes, singleTypeName);
        }
        else throw new Exception();

        return ret;
    }

    /**
     * Retrieves the list of Sinks in this parser's configuration file
     *
     * @return			A list of configurations of Sinks
     * @throws Exception
     */
    public SinkConfig[] retrieveSinkList() throws Exception {
        SinkConfig ret[] = null;

        if(isFileOpen()) {
            Element sinksList =(Element)this.xmlFileRoot.getElementsByTagName("Sinks").item(0);

            NodeList sinks = sinksList.getElementsByTagName("Sink");
            ret = new SinkConfig[sinks.getLength()];

            Element sink;
            String sinkName;
            InetAddress sinkAddress, serverAddress, validatorAddress=null;
            int sinkPort, validatorPort=0;
            int fieldsToLog=0;
            boolean log, validate;
            double validSamplingRate=0;
            double logSamplingRate=0;

            // Iterates over driver list
            for (int i = 0; i < sinks.getLength(); i++) {
                sink = (Element)sinks.item(i);
                sinkName = sink.getAttribute("name");
                sinkPort = Integer.parseInt(sink.getAttribute("port"));
                sinkAddress = InetAddress.getByName(sink.getAttribute("address"));
                NodeList streams = sink.getElementsByTagName("Stream");
                String streamNames []= new String[streams.getLength()];

                // Iterates over streams of a Sink
                for (int j = 0; j < streams.getLength(); j++) {
                    streamNames[j] = streams.item(j).getFirstChild().getNodeValue();
                }

                Element server = (Element) sink.getElementsByTagName("Server").item(0);
                serverAddress = InetAddress.getByName(server.getAttribute("address"));

                Element logging = (Element) sink.getElementsByTagName("Logging").item(0);
                log = Boolean.parseBoolean(logging.getAttribute("log"));
                if(log) {
                    if(logging.getAttribute("fieldsToLog").equalsIgnoreCase("all"))
                        fieldsToLog = Globals.LOG_ALL_FIELDS;
                    else
                        fieldsToLog = Globals.LOG_ONLY_TIMESTAMPS;
                    logSamplingRate = Double.parseDouble(logging.getAttribute("samplingRate"));
                }

                Element validation = (Element) sink.getElementsByTagName("Validation").item(0);
                validate = Boolean.parseBoolean(validation.getAttribute("validate"));
                if(validate) {
                    validatorAddress = InetAddress.getByName(validation.getAttribute("validatorAddress"));
                    validatorPort = Integer.parseInt(validation.getAttribute("validatorPort"));
                    validSamplingRate = Double.parseDouble(validation.getAttribute("samplingRate"));
                }

                ret[i] = new SinkConfig(sinkName, sinkAddress, sinkPort, streamNames,
                        serverAddress, log, fieldsToLog, logSamplingRate,
                        validate, validatorAddress, validatorPort,
                        validSamplingRate);
            }


        }
        return ret;
    }


    /**
     *
     * Used to retrieve event types and their mixes of an execution phase of a given Driver
     *
     * @param phaseSchema		An XML element containing the schema of a phase
     * @param randomSeed		The Driver's seed used in random number generation
     *
     * @return					A map <<tt>EventType</tt>, Double>, containing event mix
     * @throws Exception
     * @throws DOMException
     * @throws NumberFormatException
     */
    private LinkedHashMap<EventType, Double> getSchema(Element phaseSchema, Long randomSeed) throws NumberFormatException, DOMException, Exception{
        LinkedHashMap<EventType, Double> ret=null;

        String typeName;
        Double typeMix;

        NodeList typeList = phaseSchema.getElementsByTagName("EventType");
        ret = new LinkedHashMap<EventType, Double>(typeList.getLength());

        Element type;
        for (int i = 0; i < typeList.getLength(); i++) {
            type = (Element)typeList.item(i);
            typeName = type.getAttribute("name");
            typeMix = Double.parseDouble(type.getAttribute("mix"));
            NodeList atts = type.getElementsByTagName("Attribute");
            Attribute attributes[] = new Attribute[atts.getLength()];

            Element att;
            for (int j = 0; j < atts.getLength(); j++) {
                att = (Element)atts.item(j);
                String attName = att.getAttribute("name");
                Datatype attDataType = this.parseDataType(att.getAttribute("type"));
                Domain attDomain =
                    this.parseDomain((Element)att.getElementsByTagName("Domain").item(0), randomSeed+j);
                attributes[j] = new Attribute(attDataType, attName, attDomain);
            }
            ret.put(new EventType(typeName, attributes), typeMix) ;

        }
        return ret;
    }

    private Datatype parseDataType(String datatype) {
        if (datatype.equalsIgnoreCase("INTEGER"))
            return Datatype.INTEGER;
        else {
            if (datatype.equalsIgnoreCase("LONG"))
                return Datatype.LONG;
            else {
                if (datatype.equalsIgnoreCase("FLOAT"))
                    return Datatype.FLOAT;
                else {
                    if (datatype.equalsIgnoreCase("DOUBLE"))
                        return Datatype.DOUBLE;
                    else {
                        if (datatype.equalsIgnoreCase("TEXT"))
                            return Datatype.TEXT;
                        else {
                            if (datatype.equalsIgnoreCase("BOOLEAN"))
                                return Datatype.BOOLEAN;
                            else
                                return null;
                        }
                    }

                }
            }
        }
    }

    /**
     * Parses a Domain. Calls specific parsing methods for each kind of domain.
     *
     * @param domain		A XML element containing domain's parameters
     * @param randomSeed	The seed used in random number generation
     * @return				An instance of <tt>Domain</tt>
     * @throws Exception
     * @throws DOMException
     * @throws NumberFormatException
     * @see 				<tt>Domain</tt> class
     */
    private Domain parseDomain(Element domain, Long randomSeed) throws NumberFormatException, DOMException, Exception {
        Domain ret=null;

        String type = domain.getAttribute("type");

        if(type.equalsIgnoreCase("RANDOM"))
            ret =
                new RandomDomain(
                        this.parseRandomVariate((Element)domain.getElementsByTagName("RandomVariable").item(0),
                                randomSeed));
        else {
            if(type.equalsIgnoreCase("SEQUENTIAL")) {
                ret = this.parseSequentialDomain(domain, randomSeed);
            }
            else {
                if (type.equalsIgnoreCase("PREDEFINED_LIST"))
                    ret = this.parsePredefinedList(domain, randomSeed);
            }
        }

        return ret;
    }


    /**
     * Parses a sequential Domain
     *
     * @param domain		A XML element containing domain's parameters
     * @param randomSeed	The seed used in random number generation
     *
     * @return				An instance of <tt>SequentialDomain</tt>
     * @throws Exception
     * @throws DOMException
     * @throws NumberFormatException
     * @see 				<tt>SequentialDomain</tt> class
     */
    private Domain parseSequentialDomain(Element domain, Long randomSeed) throws NumberFormatException, DOMException, Exception{
        Domain ret;

        Variate initialValueVariate = null;
        Variate incrementVariate = null;

        // Parses Initial Value
        Element initialValue = (Element)domain.getElementsByTagName("InitialValue").item(0);
        if (initialValue.getAttribute("type").equalsIgnoreCase("Constant")) {
            initialValueVariate = new ConstantVariate(Double.parseDouble(initialValue.getFirstChild().getNodeValue()));
        }
        else {
            if (initialValue.getAttribute("type").equalsIgnoreCase("Random"))
                initialValueVariate =
                    this.parseRandomVariate((Element)initialValue.getElementsByTagName("RandomVariable").item(0),
                            randomSeed);
        }

        // Parses Increment
        Element increment = (Element)domain.getElementsByTagName("Increment").item(0);
        if (increment.getAttribute("type").equalsIgnoreCase("Constant")) {
            incrementVariate = new ConstantVariate(Double.parseDouble(increment.getFirstChild().getNodeValue()));
        }
        else {
            if (increment.getAttribute("type").equalsIgnoreCase("Random"))
                incrementVariate =
                    this.parseRandomVariate((Element)increment.getElementsByTagName("RandomVariable").item(0),
                            randomSeed);
        }

        ret = new SequentialDomain(initialValueVariate, incrementVariate);

        return ret;
    }


    /**
     * Parses a domain of the type predefined list
     *
     * @param domain		A XML element containing domain's parameters
     * @param randomSeed	The seed used in random number generation
     *
     * @return				An instance of <tt>PredefinedListDomain</tt>
     * @see 				<tt>PredefinedListDomain</tt> class
     */
    private Domain parsePredefinedList(Element domain,  Long randomSeed) {
        NodeList list = domain.getChildNodes();
        Element item;

        String mode = domain.getAttribute("behavior");

        if (mode.equalsIgnoreCase("Deterministic")) {
            ArrayList<Object> itemList = new ArrayList<Object>();
            Object itemValue;
            for (int i = 0; i < list.getLength(); i++) {
                if (list.item(i).getNodeType() == Node.ELEMENT_NODE){
                    item = (Element)list.item(i);
                    itemValue = item.getAttribute("value");
                    itemList.add(itemValue);
                }
            }
            String items[] = new String[itemList.size()];
            items = itemList.toArray(items);

            return new PredefinedListDomain(items);
        }
        else {
            if (mode.equalsIgnoreCase("Stochastic")) {
                LinkedHashMap<Object, Double> items = new LinkedHashMap<Object, Double>();
                String key;
                Double frequency;

                for (int i = 0; i < list.getLength(); i++) {
                    if (list.item(i).getNodeType() == Node.ELEMENT_NODE){
                        item = (Element)list.item(i);
                        key = item.getAttribute("value");
                        try {
                            frequency = Double.parseDouble(item.getAttribute("frequency"));
                            items.put(key, frequency);
                        }
                        catch (ClassCastException ce) {
                            System.err.println("Invalid frequency for item.");
                            frequency = null;
                        }
                    }
                }

                return new PredefinedListDomain(items, randomSeed);
            }
        }
        return null;
    }

    /**
     * Parses a Variate from XML configuration file
     *
     * @param randomVariate			a Variate object represented as XML element
     * @param seed					seeed for random number generation
     * @return						an instance of <tt>Variate</tt> class
     * @throws Exception
     * @throws DOMException
     * @throws NumberFormatException
     * @see							<tt>Variate</tt> class
     */
    private Variate parseRandomVariate(Element randomVariate, Long seed) throws NumberFormatException, DOMException, Exception {
        Variate rv = null;

        if(randomVariate.getAttribute("type").equalsIgnoreCase("UNIFORM")){
            Element lowerBound = (Element)randomVariate.getElementsByTagName("lower").item(0);
            Element upperBound = (Element)randomVariate.getElementsByTagName("upper").item(0);
            rv = new RandomUniformVariate(seed,
                    Double.parseDouble(lowerBound.getFirstChild().getNodeValue()),
                    Double.parseDouble(upperBound.getFirstChild().getNodeValue()));
        }
        else {
            if(randomVariate.getAttribute("type").equalsIgnoreCase("NORMAL")){
                Element mean = (Element)randomVariate.getElementsByTagName("mean").item(0);
                Element stdev = (Element)randomVariate.getElementsByTagName("stdev").item(0);
                rv = new RandomNormalVariate(seed,
                        Double.parseDouble(mean.getFirstChild().getNodeValue()),
                        Double.parseDouble(stdev.getFirstChild().getNodeValue()));
            }
            else {
                if(randomVariate.getAttribute("type").equalsIgnoreCase("EXPONENTIAL")){
                    Element lambda = (Element)randomVariate.getElementsByTagName("lambda").item(0);
                    rv = new RandomExponentialVariate(seed,
                            Double.parseDouble(lambda.getFirstChild().getNodeValue()));
                }
            }
        }

        return rv;
    }

    /**
     * Converts the schema of a phase to XML
     *
     * @param doc			used to create new elements
     * @param eventMix		event types and frequencies on dataset
     * @return				an XML Element representing the schema
     */
    private Element saveSchema(Document doc, Map<EventType, Double> eventMix) {
        Element schema = doc.createElement("Schema");
        Element type;
        EventType t;
        for (Entry<EventType, Double> e : eventMix.entrySet()) {
            type = doc.createElement("EventType");
            t = e.getKey();
            type.setAttribute("name", t.getName());
            type.setAttribute("mix", e.getValue()+"");
            Element attribute, domain;
            for (Attribute att : t.getAttributes()) {
                attribute = doc.createElement("Attribute");
                attribute.setAttribute("name", att.getName());
                attribute.setAttribute("type", att.getType().toString());
                domain = this.saveDomain(doc, att.getDomain());
                attribute.appendChild(domain);
                type.appendChild(attribute);
            }
            schema.appendChild(type);
        }

        return schema;
    }

    /**
     * Converts an instance of the <tt>Domain</tt> class into XML textual representation
     * and writes it into a XML document
     *
     * @param doc		The XML document where the <tt>Domain</tt> instance must be written to
     * @param d			The element where the <tt>Domain</tt> instance must be written to
     * @return			The <tt>Domain</tt> instance
     * @see				<tt>Domain</tt> class
     */
    private Element saveDomain(Document doc, Domain d) {
        Element domain = doc.createElement("Domain");

        if (d instanceof SequentialDomain) {
            SequentialDomain seq = (SequentialDomain) d;
            domain.setAttribute("type", "SEQUENTIAL");
            Element initialValue = doc.createElement("InitialValue");
            this.saveVariate(doc, initialValue, seq.getInitialVariate());
            Element increment = doc.createElement("Increment");;
            this.saveVariate(doc, increment, seq.getIncrementVariate());
            domain.appendChild(initialValue);
            domain.appendChild(increment);
        }
        else if(d instanceof PredefinedListDomain){
            PredefinedListDomain  def = (PredefinedListDomain) d;
            domain.setAttribute("type", "PREDEFINED_LIST");
            if(def.isDeterministic()) {
                domain.setAttribute("behavior", "Deterministic");
                Element item;
                for (Object itemName : def.getItems()) {
                    item = doc.createElement("Item");
                    item.setAttribute("value", ""+itemName);
                    item.setAttribute("frequency", "1");
                    domain.appendChild(item);
                }
            }
            else {
                domain.setAttribute("behavior", "Stochastic");
                Element item;
                for (Entry<Object, Double> e : def.getItemMix().entrySet()) {
                    item = doc.createElement("Item");
                    item.setAttribute("value", ""+e.getKey());
                    item.setAttribute("frequency", ""+e.getValue());
                    domain.appendChild(item);
                }
            }

        }
        else if (d instanceof RandomDomain) {
            RandomDomain rand = (RandomDomain)d;
            //domain.setAttribute("type", "RANDOM");
            this.saveVariate(doc, domain, rand.getVariate());
        }

        return domain;
    }

    /**
     * Converts an instance of the <tt>Variate</tt> class into XML textual representation
     * and writes it into a XML document
     *
     * @param doc		The XML document where the Variate instance must be written to
     * @param e			The element where the Variate instance must be written to
     * @param v			The Variate instance
     * @see				<tt>Variate</tt> class
     */
    private void saveVariate(Document doc, Element e, Variate v) {
        if(v instanceof ConstantVariate) {
            e.setAttribute("type", "Constant");
            String value = ""+((ConstantVariate)v).getValue();
            e.appendChild(doc.createTextNode(value));
        }
        else
        {
            Element randomVar = doc.createElement("RandomVariable");
            e.setAttribute("type", "Random");
            e.appendChild(randomVar);
            if(v instanceof RandomExponentialVariate){
                randomVar.setAttribute("type", "EXPONENTIAL");
                String lambdaValue = "" + ((RandomExponentialVariate)v).getLambda();
                Element lambda = doc.createElement("lambda");
                lambda.appendChild(doc.createTextNode(lambdaValue));
                randomVar.appendChild(lambda);
            }
            else if(v instanceof RandomNormalVariate){
                randomVar.setAttribute("type", "NORMAL");
                String meanValue = "" + ((RandomNormalVariate)v).getMean();
                Element mean = doc.createElement("mean");
                mean.appendChild(doc.createTextNode(meanValue));
                String stdevValue = "" + ((RandomNormalVariate)v).getStdev();
                Element stdev = doc.createElement("stdev");
                stdev.appendChild(doc.createTextNode(stdevValue));
                randomVar.appendChild(mean);
                randomVar.appendChild(stdev);
            }
            else if(v instanceof RandomUniformVariate){
                randomVar.setAttribute("type", "UNIFORM");
                String lwrValue = "" + ((RandomUniformVariate)v).getLower();
                Element lower = doc.createElement("lower");
                lower.appendChild(doc.createTextNode(lwrValue));
                String uprValue = "" + ((RandomUniformVariate)v).getUpper();
                Element upper = doc.createElement("upper");
                upper.appendChild(doc.createTextNode(uprValue));
                randomVar.appendChild(lower);
                randomVar.appendChild(upper);
            }
        }
    }

    /**
     * Reads a XML document and puts it into a String object
     *
     * @param doc			The XML document
     * @return				A textual representation of the XML document
     * @throws TransformerException
     */
    private String fromXMLDocToString(Document doc) throws TransformerException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        DOMSource source = new DOMSource(doc);
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        transformer.transform(source, result);
        return sw.toString();

    }

    /**
     *
     * @return
     */
    public String getFilePath() {
        return this.configFile.getPath();
    }
}
