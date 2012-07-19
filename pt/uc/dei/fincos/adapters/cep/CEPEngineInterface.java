package pt.uc.dei.fincos.adapters.cep;

import java.util.Properties;

import pt.uc.dei.fincos.adapters.InputAdapter;
import pt.uc.dei.fincos.adapters.OutputListener;
import pt.uc.dei.fincos.basic.Status;
import pt.uc.dei.fincos.sink.Sink;



/**
 * Abstract class for interfacing with CEP engines.
 * Vendor-specific adapters must inherit from this class.
 *
 * @author Marcelo R.N. Mendes
 *
 */
public abstract class CEPEngineInterface implements InputAdapter {

    /** The connection status of this interface. */
    public Status status;

    /** Parameters used to connect with the CEP engine. */
    private Properties connProperties;

    /** Either END-TO-END or ADAPTER. */
    protected final int rtMode;

    /** Either Milliseconds or Nanoseconds. */
    protected final int rtResolution;

    /** The owner application where this adapter runs. */
    protected Sink sinkInstance;

    /** Threads to listen for incoming events. */
    protected OutputListener[] outputListeners;


    /**
     *
     * @param rtMode        response time measurement mode (either END-TO-END, ADAPTER or NO_RT)
     * @param rtResolution  response time measurement resolution (either Milliseconds or Nanoseconds)
     */
    public CEPEngineInterface(int rtMode, int rtResolution) {
        this.rtMode = rtMode;
        this.rtResolution = rtResolution;
    }

    /**
     * Connects to CEP engine.
     *
     * @return              <tt>true</tt> if a connection with the CEP engine has been successfully established,
     *                      <tt>false</tt> otherwise.
     * @throws Exception    if an error occurs during connection
     */
    public abstract boolean connect() throws Exception;

    /**
     *
     * Performs any vendor-specific initialization at client side (e.g. initialize listeners for output streams).
     *
     * @param outputStreams     List of output streams to subscribe
     * @param sinkInstance      An instance of FINCoS Sink to which output events must be forwarded
     *
     * @return                  <tt>true</tt> if the adapter was loaded successfully, <tt>false</tt> otherwise.
     * @throws Exception        if an error occurs
     */
    public abstract boolean load(String [] outputStreams, Sink sinkInstance) throws Exception;

    /**
     * Disconnects from CEP engine and performs any finalization procedures needed (e.g. close listeners for output streams)
     */
    public abstract void disconnect();


    /**
     * Retrieves the list of input streams on the CEP engine
     * (optional function).
     *
     * @return              the list of input streams on the CEP engine
     * @throws Exception    if the list cannot be retrieved.
     */
    public abstract String[] getInputStreamList() throws Exception;


    /**
     * Optional function. Retrieves the list of output streams of a continuous query running on CEP engine
     *
     * @return
     * @throws Exception
     */
    public abstract String[] getOutputStreamList() throws Exception;

    /**
     * Sets the CEP engine's connection properties.
     *
     * @param connProperties
     */
    public void setConnProperties(Properties connProperties) {
        this.connProperties = connProperties;
    }


    /**
     * Retrieves a connection property.
     *
     * @param propertyName  The name of the connection property to be retrieved
     * @return              The value of the connection property to be retrieved
     * @throws Exception    If the property is missing
     */
    protected String retrieveConnectionProperty(String propertyName) throws Exception {
        String retrievedProperty = this.connProperties.getProperty(propertyName);

        if (retrievedProperty == null || retrievedProperty.isEmpty()) {
            throw new Exception("Required connection property \"" + propertyName + "\" is missing.");
        }

        return retrievedProperty;
    }

    /**
     * Starts all listeners.
     */
    protected void startAllListeners() {
        if (this.outputListeners != null) {
            for (int i = 0; i < this.outputListeners.length; i++) {
                if (outputListeners[i] != null) {
                    outputListeners[i].start();
                }
            }
        }
    }

    /**
     * Stops all listeners.
     */
    protected void stopAllListeners() {
        if (this.outputListeners != null) {
            for (int i = 0; i < this.outputListeners.length; i++) {
                if (outputListeners[i] != null) {
                    outputListeners[i].disconnect();
                }
            }
        }
    }
}
