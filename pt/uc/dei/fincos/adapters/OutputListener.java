package pt.uc.dei.fincos.adapters;

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

    /** Response time measurement mode (either END-TO-END or ADAPTER). */
    protected final int rtMode;

    /** Response time measurement resolution (either Milliseconds or Nanoseconds). */
    protected final int rtResolution;

    /** Reference to the Sink instance to which results must be forwarded (DIRECT COMMUNICATION). */
    protected final Sink sinkInstance;

    /** Flag used to interrupt event listening. */
    protected boolean keepListening = true;

    /**
     * Constructor for DIRECT COMMUNICATION.
     *
     * @param lsnrID        an alias for this listener
     * @param rtMode        response time measurement mode (either END-TO-END, ADAPTER or NO_RT)
     * @param rtResolution  response time measurement resolution (either Milliseconds or Nanoseconds)
     * @param sinkInstance  reference to the Sink instance to which results must be forwarded
     */
    public OutputListener(String lsnrID, int rtMode, int rtResolution, Sink sinkInstance) {
        this.listenerID = lsnrID;
        this.rtMode = rtMode;
        this.rtResolution = rtResolution;
        this.sinkInstance = sinkInstance;
    }


    /**
     * Callback method used to receive incoming results from the CEP engine and
     * forward them to an appropriate Sink.
     *
     * @param e     the incoming event, represented as an array of values
     */
    public void onOutput(Object[] e) {
        this.sinkInstance.processOutputEvent(e);
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
