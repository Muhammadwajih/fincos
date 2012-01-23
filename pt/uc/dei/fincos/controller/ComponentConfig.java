package pt.uc.dei.fincos.controller;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * 
 * Class that encapsulates the configuration of a component
 * involved in load generation (Driver or Sink)
 * 
 * @author Marcelo R.N. Mendes
 * 
 * @see DriverConfig
 * @see SinkConfig
 *
 */
public abstract class ComponentConfig implements Serializable{
	private static final long serialVersionUID = -2242158660695499721L;
	
	private String alias;
	private InetAddress address;
	
	private InetAddress serverAddress;
	
	private boolean loggingEnabled;
	private int fieldsToLog;
	private double loggingSamplingRate;		
	
	private boolean validationEnabled;
	private InetAddress validatorAddress;
	private int validatorPort;
	private double validationSamplingRate;
	
	
	/**
	 * 
	 * @return 	The unique identifier of this component
	 */
	public String getAlias() {
		return alias;
	}
	
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	
	/**
	 * The address where this component must run
	 * 
	 * @return
	 */
	public InetAddress getAddress() {
		return address;
	}
	
	public void setAddress(InetAddress address) {
		this.address = address;
	}
	
	/**
	 * The address to/from which this component must send/receive events
	 * 
	 * @return
	 */
	public InetAddress getServerAddress() {
		return serverAddress;
	}
	
	public void setServerAddress(InetAddress serverAddress) {
		this.serverAddress = serverAddress;
	}
	
	
	public void setLoggingEnabled(boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}

	public boolean isLoggingEnabled() {
		return loggingEnabled;
	}

	public void setFieldsToLog(int fieldsToLog) {
		this.fieldsToLog = fieldsToLog;
	}

	public int getFieldsToLog() {
		return fieldsToLog;
	}

	public void setLoggingSamplingRate(double loggingSamplingRate) {
		this.loggingSamplingRate = loggingSamplingRate;
	}

	public double getLoggingSamplingRate() {
		return loggingSamplingRate;
	}

	/**
	 * 
	 * @return	The Address of a FINCoS Performance Monitor instance to 
	 * 			where this component must send events
	 */
	public InetAddress getValidatorAddress() {
		return validatorAddress;
	}
	
	public void setValidatorAddress(InetAddress validatorAddress) {
		this.validatorAddress = validatorAddress;
	}
	
	
	/**
	 * 
	 * @return	The Port of a FINCoS Performance Monitor instance at 
	 * 			which this component must send events
	 */
	public int getValidatorPort() {
		return validatorPort;
	}
	
	public void setValidatorPort(int validatorPort) {
		this.validatorPort = validatorPort;
	}
	
	public boolean isValidationEnabled() {
		return validationEnabled;
	}
	
	public void setValidationEnabled(boolean validationEnabled) {
		this.validationEnabled = validationEnabled;
	}
	
	public double getValidationSamplingRate() {
		return validationSamplingRate;
	}
	
	public void setValidationSamplingRate(double validationSamplingRate) {
		this.validationSamplingRate = validationSamplingRate;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ComponentConfig) {
			ComponentConfig comp = (ComponentConfig) obj;
			
			boolean ret = (this.alias.equals(comp.alias)) &&
					this.address.equals(comp.address) &&
					this.serverAddress.equals(comp.serverAddress) &&
					this.loggingEnabled == comp.loggingEnabled &&
					this.validationEnabled == comp.validationEnabled;
			
					if(this.loggingEnabled && comp.loggingEnabled) {
						ret = ret && this.fieldsToLog == comp.fieldsToLog &&
								this.loggingSamplingRate == comp.loggingSamplingRate;
					}
			
					if(this.validationEnabled && comp.validationEnabled) {
						ret = ret && this.validatorAddress.equals(comp.validatorAddress) &&
										this.validatorPort == comp.validatorPort;	
					}

					return ret;
		}
		
		return false;
	}	
	
	@Override
	public int hashCode() {
		return 
			this.address.getHostAddress().hashCode() +			
			this.serverAddress.getHostAddress().hashCode()+	
			(this.isLoggingEnabled()
					?1231+this.fieldsToLog+((int)this.loggingSamplingRate*1000)
					:1237)+
			(this.isValidationEnabled() 
					?1231+this.validatorAddress.getHostAddress().hashCode()
						 +this.validatorPort+((int)this.validationSamplingRate*1000)
					:1237);
	}
}
