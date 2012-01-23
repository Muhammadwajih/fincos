package pt.uc.dei.fincos.adapters;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.Status;
import pt.uc.dei.fincos.sink.Sink;



/**
 * Abstract class for interfacing with CEP engines. 
 * Vendor-specific adapters must inherit from this class. 
 * 
 * @author Marcelo R.N. Mendes
 *
 */
public abstract class CEPEngineInterface {
	protected Status status;
	private Properties connProperties;
	private int rtMeasurementMode;
	private int communicationMode;
	private int socketBufferSize;
	protected Sink sinkInstance;
	
	// Threads to listen for incoming events
	protected OutputListener outputListeners[];
		
	/**
	 * Connects to CEP engine
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract boolean connect() throws Exception;
	
	
	/**
	 * 
	 * Performs any vendor-specific initialization at client side (e.g. initialize listeners for output streams).
	 * [ADAPTER CSV COMMUNICATION]
	 * 
	 * @param outputToSink	Maps output streams at CEP engine to corresponding address(es) and port(s) of Sink(s)
	 * @return
	 * @throws Exception
	 */
	public abstract boolean load(HashMap<String, ArrayList<InetSocketAddress>> outputToSink) throws Exception;
	
	
	/**
	 * 
	 * Performs any vendor-specific initialization at client side (e.g. initialize listeners for output streams).
	 * [DIRECT API COMMUNICATION]
	 * 
	 * @param outputStreams		List of output streams to subscribe
	 * @param sinkInstance		An instance of FINCoS Sink to which output events must
	 * 							be forwarded
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract boolean load(String [] outputStreams, Sink sinkInstance) throws Exception;
	
	/**
	 * Disconnects from CEP engine and performs any finalization procedures needed (e.g. close listeners for output streams)
	 * 
	 */
	public abstract void disconnect();
	
	
	/**
	 * 
	 * Sends an event to an input stream at CEP engine.
	 * Converts from the framework CSV-based representation to a format supported by the CEP engine
	 * 
	 * @param e
	 * @throws Exception
	 */
	public abstract void sendEvent(String e) throws Exception;
	
	/**
	 * 
	 * Sends an event to an input stream at CEP engine.
	 * Converts from the internal framework representation to a format
	 * supported by the CEP engine. 
	 * (IMPLEMENTATIONS OF THIS METHOD MUST BE THREAD-SAFE!)
	 * 
	 * @param e
	 * @throws Exception
	 */
	public abstract void sendEvent(Event e) throws Exception;
	
	
	/**
	 * Optional function. Retrieves the list of input streams of a continuous query running on CEP engine
	 * 
	 * @return
	 * @throws Exception
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
	 * Sets the CEP engine's connection properties
	 * 
	 * @param connProperties
	 */
	public void setConnProperties(Properties connProperties) {
		this.connProperties = connProperties;
	}
	
	public void setRtMeasurementMode(int rtMeasurementMode) {
		this.rtMeasurementMode = rtMeasurementMode;
	}


	public void setCommunicationMode(int communicationMode) {
		this.communicationMode = communicationMode;
	}


	public int getCommunicationMode() {
		return communicationMode;
	}


	public int getRtMeasurementMode() {
		return rtMeasurementMode;
	}


	public void setSocketBufferSize(int socketBufferSize) {
		this.socketBufferSize = socketBufferSize;
	}


	public int getSocketBufferSize() {
		return socketBufferSize;
	}


	/**
	 * Retrieves a connection property 
	 * 
	 * @param propertyName	The name of the connection property to be retrieved
	 * @return				The value of the connection property to be retrieved
	 * @throws Exception	If the property is missing
	 */
	protected String retrieveConnectionProperty(String propertyName) throws Exception {
		String retrievedProperty = this.connProperties.getProperty(propertyName);
		
		if(retrievedProperty == null || retrievedProperty.isEmpty()) {
			throw new Exception("Required connection property \"" + 
								propertyName + "\" is missing.");
		}
		
		return retrievedProperty;
	}
	
	protected void startAllEventListeners(){
		if(this.outputListeners != null) {
			for (int i = 0; i < this.outputListeners.length; i++) {
				if(outputListeners[i] != null)
					outputListeners[i].start();
			}	
		}
	}
	
	protected void stopAllEventListeners(){
		if(this.outputListeners != null) {
			for (int i = 0; i < this.outputListeners.length; i++) {
				if(outputListeners[i] != null)
					outputListeners[i].disconnect();
			}	
		}
	}
}
