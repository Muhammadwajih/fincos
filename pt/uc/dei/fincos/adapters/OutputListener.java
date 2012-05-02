package pt.uc.dei.fincos.adapters;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.communication.ClientSocketInterface;
import pt.uc.dei.fincos.sink.Sink;



/**
 *
 * Thread for listening incoming events from the CEP Engine and
 * forwarding them to a given Sink.
 *
 * @author Marcelo R.N. Mendes
 *
 */
public abstract class OutputListener extends Thread {
    /** An alias for this listener (for logging purposes). */
    protected final String listenerID;

    /** Response time measurement (either in milliseconds or nanoseconds). */
    protected final int rtMeasurementMode;

    /** Either DIRECT or through FINCoS_ADAPTER. */
    protected final int communicationMode;

    /** The number of events to be buffered before flushing socket with a sink (when communicating through FINCoS Adapter. */
    protected final int socketBufferSize;

    /** Frequency of log flushes to disk, in milliseconds. */
    protected final int logFlushInterval;

    /** List of IP addresses of Sinks to which results must be forwarded (COMMUNICATION THROUGH FINCoS ADAPTER). */
    protected final ArrayList<InetSocketAddress> sinksList;

    /** Sockets to Sinks to which results must be forwarded (COMMUNICATION THROUGH FINCoS ADAPTER). */
    private ArrayList<ClientSocketInterface> sinkInterfaces;

    /** Reference to the Sink instance to which results must be forwarded (DIRECT COMMUNICATION). */
    protected final Sink sinkInstance;

    /** Flag used to interrupt event listening. */
    protected boolean keepListening = true;

    /**
     * Constructor for DIRECT COMMUNICATION.
     *
     * @param lsnrID                an alias for this listener
     * @param rtMeasurementMode     response time measurement (either in milliseconds or nanoseconds)
     * @param logFlushInterval      frequency of log flushes to disk, in milliseconds
     * @param sinkInstance          reference to the Sink instance to which results must be forwarded
     */
    public OutputListener(String lsnrID, int rtMeasurementMode, int logFlushInterval, Sink sinkInstance) {
        this(lsnrID, rtMeasurementMode, Globals.DIRECT_API_COMMUNICATION, -1, logFlushInterval, sinkInstance, null);
    }

    /**
     * Constructor for communication through FINCOS ADAPTER.
     *
     * @param lsnrID                an alias for this listener
     * @param rtMeasurementMode     response time measurement (either in milliseconds or nanoseconds)
     * @param socketBufferSize      the number of events to be buffered before flushing socket with a sink
     * @param logFlushInterval      frequency of log flushes to disk, in milliseconds
     * @param sinksList             address(es) and port(s) of Sink(s) to which received events must be forwarded
     */
    public OutputListener(String lsnrID, int rtMeasurementMode, int socketBufferSize,
            int logFlushInterval, ArrayList<InetSocketAddress> sinksList) {
        this(lsnrID, rtMeasurementMode, Globals.ADAPTER_CSV_COMMUNICATION, socketBufferSize, logFlushInterval, null, sinksList);
    }

    private OutputListener(String lsnrID, int rtMeasurementMode, int communicationMode, int socketBufferSize,
            int logFlushInterval, Sink sinkInstance, ArrayList<InetSocketAddress> sinksList) {
        this.listenerID = lsnrID;
        this.rtMeasurementMode = rtMeasurementMode;
        this.communicationMode = communicationMode;
        this.socketBufferSize = socketBufferSize;
        this.logFlushInterval = logFlushInterval;
        this.sinkInstance = sinkInstance;
        this.sinksList = sinksList;
    }

    /**
     * Connects with all the sinks this listener sends results to.
     */
    protected void connectWithAllSinks() {
        disconnectFromAllSinks();

        this.sinkInterfaces = new ArrayList<ClientSocketInterface>();
        for (InetSocketAddress sinkAddressPort: sinksList) {
            try {
                sinkInterfaces.add(connectWithSink(sinkAddressPort.getAddress().getHostAddress(),
                        sinkAddressPort.getPort()));
            } catch (IOException ioe) {
                System.err.println("Could not connect to Sink at "
                        + sinkAddressPort.getAddress() + ":" + sinkAddressPort.getPort()
                        + "(" + ioe.getMessage() + ").");
            }
        }
    }

    private ClientSocketInterface connectWithSink(String address, int port) throws IOException {
        return new ClientSocketInterface(address, port, socketBufferSize);
    }

    /**
     * Disconnects from all the sinks this listener sends results to.
     */
    public void disconnectFromAllSinks() {
        if (this.sinkInterfaces != null) {
            System.out.println("disconnecting from sink(s)...");
            for (ClientSocketInterface sinkInterface : sinkInterfaces) {
                try {
                    sinkInterface.disconnect();
                } catch (IOException e) {
                    System.err.println("Error while closing connection with Sink (" + e.getMessage() + ")");
                } finally {
                    sinkInterface = null;
                }
            }

            sinkInterfaces.clear();
            sinkInterfaces = null;
        }
    }

    /**
     * Callback method used to receive incoming results from the CEP engine and
     * forward them to an appropriate Sink.
     *
     * @param e     the incoming event, represented as an array of values
     */
    public void onOutput(Object[] e) {
        if (this.communicationMode == Globals.DIRECT_API_COMMUNICATION) {
            this.sinkInstance.processOutputEvent(e);
        }
    }

    /**
     * Callback method used to receive incoming results from the CEP engine and
     * forward them to an appropriate Sink.
     *
     * @param e     the incoming event, represented as a CSV record.
     */
    public void onOutput(String e) {
        if (this.communicationMode == Globals.ADAPTER_CSV_COMMUNICATION) {
            // If it is the first event, open connection to Sink(s)
            if (this.sinkInterfaces == null || sinkInterfaces.isEmpty()) {
                connectWithAllSinks();
            }

            // Send event to all Sinks that subscribe to this stream
            for (ClientSocketInterface sinkInterface : sinkInterfaces) {
                try {
                    sinkInterface.sendCSVEvent(e);
                } catch (IOException ioe) {
                    System.err.println("Error while forwarding event to Sink. (" + ioe.getMessage() + "). Trying retransmission...");
                    try {
                        sinkInterface.disconnect();
                        sinkInterface = connectWithSink(sinkInterface.getDestinationAddress(),
                                sinkInterface.getDestinationPort());
                        onOutput(e);
                    } catch (IOException ioe1) {
                        System.err.println("Could not reconnect to Sink (it seems to be offline). Message will be discarded.");
                    }
                }
            }
        } else if (this.communicationMode == Globals.DIRECT_API_COMMUNICATION) {
            this.sinkInstance.processOutputEvent(e);
        }

    }

    /**
     * Performs any vendor-specific initialization on the listener.
     *
     * @throws Exception    if an error occurs
     */
    public abstract void load() throws Exception;

    /**
     * Disconnects from CEP Engine and from Sink.
     */
    public abstract void disconnect();

}
