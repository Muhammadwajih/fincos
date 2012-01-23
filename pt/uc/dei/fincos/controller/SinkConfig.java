package pt.uc.dei.fincos.controller;

import java.net.InetAddress;

/**
 * Class that encapsulates the configuration of a Sink 
 * 
 * @author Marcelo R.N. Mendes
 *
 * @see ComponentConfig
 * 
 */
public class SinkConfig extends ComponentConfig implements Cloneable{	
	private static final long serialVersionUID = -7247958453310369444L;
	
	private int port;
	private String outputStreamList[];
	
	/**
	 * 
	 * @param alias						An unique identifier for the Sink
	 * @param address					The address of the machine where the Sink must run
	 * @param port						The port to which the Sink will listen in order to process incoming events
	 * @param outputStreamsList			A list of streams to which the Sink is associated
	 * @param serverAddress				The address of the server from where output events come
	 * @param loggingEnabled			Indicates if received events must be logged to disk
	 * @param fieldsToLog				Either all fields (LOG_ALL_FIELDS) or only timestamps (LOG_ONLY_TIMESTAMPS)
	 * @param loggingSamplingRate		The fraction of all events that will be logged
	 * @param validationEnabled			Indicates if events must be sent to an instance of the FINCoS PerfMon
	 * @param validatorAddress			The address of the FINCoS PerfMon application
	 * @param validatorPort				The port on which the FINCoS PerfMon listens for incoming events
	 * @param validationSamplingRate	The fraction of all events that will be sent for validation
	 */
	public SinkConfig(String alias, InetAddress address, int port, String[] outputStreamsList,
			InetAddress serverAddress, boolean loggingEnabled, int fieldsToLog, 
			double loggingSamplingRate,	boolean validationEnabled, 
			InetAddress validatorAddress, int validatorPort, 
			double validationSamplingRate) {
		this.setAlias(alias);
		this.setAddress(address);
		this.setPort(port);
		this.setOutputStreamList(outputStreamsList);
		this.setServerAddress(serverAddress);
		this.setLoggingEnabled(loggingEnabled);
		this.setFieldsToLog(fieldsToLog);
		this.setLoggingSamplingRate(loggingSamplingRate);
		this.setValidationEnabled(validationEnabled);
		this.setValidatorAddress(validatorAddress);
		this.setValidatorPort(validatorPort);
		this.setValidationSamplingRate(validationSamplingRate);		
	}
	
	/**
	 * The port at which this Sink will receive events
	 * 
	 * @return
	 */
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	/**
	 * 
	 * @return	The list of streams that this Sink will listen to
	 */
	public String[] getOutputStreamList() {
		return outputStreamList;
	}
	
	public void setOutputStreamList(String[] inputStreamList) {
		if(inputStreamList != null)
			this.outputStreamList = inputStreamList;
		else
			this.outputStreamList = new String[0];
	}
	
	/**
	 * 
	 * @return	The number of streams this Sink will listen to
	 */
	public int getStreamCount() {
		return this.outputStreamList.length;
	}
		
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new SinkConfig(this.getAlias(), this.getAddress(), 
				this.getPort(), this.outputStreamList, this.getServerAddress(),
				this.isLoggingEnabled(), this.getFieldsToLog(), this.getLoggingSamplingRate(),				
				this.isValidationEnabled(), this.getValidatorAddress(), 
				this.getValidatorPort(), this.getValidationSamplingRate());
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof SinkConfig) {
			boolean ret = super.equals(obj);
			SinkConfig comp = (SinkConfig) obj;
			
			ret &= this.port == comp.port;
			
			if(this.getStreamCount() == comp.getStreamCount()) {
				for (int i = 0; i < this.outputStreamList.length; i++) {
					if(!this.outputStreamList[i].equals(comp.outputStreamList[i]))
						return false;
				}				
			}
			else
				ret = false;
						
			return ret;
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {		
		int hash = super.hashCode()+this.port;
		
		for (String stream : this.outputStreamList) {
			hash += stream.hashCode();
		}
		
		return hash;
	}
}
