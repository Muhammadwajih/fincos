package pt.uc.dei.fincos.controller;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import pt.uc.dei.fincos.basic.Attribute;
import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.driver.ExternalFileWorkloadPhase;
import pt.uc.dei.fincos.driver.SyntheticWorkloadPhase;
import pt.uc.dei.fincos.driver.WorkloadPhase;



/**
 * Class that encapsulates the configuration of a Driver 
 * 
 * @author Marcelo R.N. Mendes
 * 
 * @see ComponentConfig
 *
 */
public class DriverConfig extends ComponentConfig implements Cloneable{			
	private static final long serialVersionUID = -3142718728972724155L;
	
	private WorkloadPhase[] workload;			
	private int serverPort;
	private int threadCount;	
		
	/**
	 * 
	 * @param alias						An unique identifier for the Driver
	 * @param address					The address of the machine where the Driver should run
	 * @param workload					The workload specification of the Driver
	 * @param serverAddress				The address of the server to where events must be sent
	 * @param serverPort				The port in the server on which the events must be sent
	 * @param threadCount				The number of threads to be used during load generation 
	 * 									(-1 for creating as many threads as the number of processors
	 * 									in the host machine)
	 * @param loggingEnabled			Indicates if sent events must be logged to disk
	 * @param fieldsToLog				Either all fields (LOG_ALL_FIELDS) or only timestamps (LOG_ONLY_TIMESTAMPS)
	 * @param loggingSamplingRate		The fraction of events that will be logged
	 * @param validate					Indicates if events must be sent to an instance of the FINCoS PerfMon
	 * @param validatorAddress			The address of the FINCoS PerfMon application
	 * @param validatorPort				The port on which the FINCoS PerfMon listens for incoming events
	 * @param validationSamplingRate	The fraction of events that will be sent for validation
	 */
	public DriverConfig(String alias, InetAddress address, WorkloadPhase[] workload, 
						InetAddress serverAddress, int serverPort, int threadCount,
						boolean loggingEnabled, int fieldsToLog, 
						double loggingSamplingRate,	boolean validate, 
						InetAddress validatorAddress, int validatorPort, 
						double validationSamplingRate) {
		this.setAlias(alias);
		this.setAddress(address);
		this.setWorkload(workload);		
		this.setThreadCount(threadCount);
		this.setServerAddress(serverAddress);
		this.setServerPort(serverPort);
		this.setLoggingEnabled(loggingEnabled);
		this.setFieldsToLog(fieldsToLog);
		this.setLoggingSamplingRate(loggingSamplingRate);
		this.setValidationEnabled(validate);
		this.setValidatorAddress(validatorAddress);
		this.setValidatorPort(validatorPort);
		this.setValidationSamplingRate(validationSamplingRate);				
	}
	

	public void setWorkload(WorkloadPhase[] workload) {
		this.workload = workload;
	}


	/**
	 * 
	 * @return	The workload of this Driver
	 */
	public WorkloadPhase[] getWorkload() {
		return workload;
	}
		
	
	/**
	 *  
	 * @return  The port on the server into which this Driver must send events
	 */
	public int getServerPort() {
		return serverPort;
	}
	
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	
	@Override
	protected Object clone() {
		return new DriverConfig(this.getAlias(), this.getAddress(), this.workload, 
				this.getServerAddress(), this.serverPort, this.threadCount,
				this.isLoggingEnabled(), this.getFieldsToLog(), this.getLoggingSamplingRate(),
				this.isValidationEnabled(), this.getValidatorAddress(), 
				this.getValidatorPort(), this.getValidationSamplingRate());
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DriverConfig) {
			DriverConfig comp = (DriverConfig) obj;
			
			return  super.equals(comp) && 						
					//this.workload.equals(comp.workload) &&									
					this.threadCount == comp.threadCount &&
					this.serverPort == comp.serverPort;
		}
		
		return false;
	}	
	
	@Override
	public int hashCode() {
		return super.hashCode()+/*this.workload.hashCode()+*/+this.threadCount+this.serverPort;
	}
	

	/**
	 * Retrieves the names of all input streams configured for this Driver 
	 * 
	 * @return	The names of the input streams
	 */
	public String[] getStreamNames() {
		EventType types[] = this.getStreamList();
		String ret[] = new String[types.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = types[i].getName();
		}
		
		return ret;
	}
	
	/**
	 * Retrieves the list of all input streams configured for this Driver 
	 * 
	 * @return	The Schemas of the input streams
	 */
	public EventType[] getStreamList() {
		EventType ret[] = new EventType[0];
		
		SyntheticWorkloadPhase syntheticPhase;
		ExternalFileWorkloadPhase externalFilePhase;
		
		Set<EventType> streams = new HashSet<EventType>();
		
		for (WorkloadPhase phase: this.getWorkload()) {
			if(phase instanceof SyntheticWorkloadPhase) {
				syntheticPhase = (SyntheticWorkloadPhase) phase;
				for (EventType inputStream: syntheticPhase.getSchema().keySet()) {
					streams.add(inputStream);
				}						
			} else if(phase instanceof ExternalFileWorkloadPhase) {
				externalFilePhase = (ExternalFileWorkloadPhase) phase;						
				if(externalFilePhase.containsEventTypes()) {
					streams.add(new EventType("Other", new Attribute[0]));					
				}
				else {
					streams.add(new EventType(externalFilePhase.getSingleEventTypeName(), new Attribute[0]));	
				}
			}
		}
		
		ret = streams.toArray(ret);
		
		return ret;
	}
	
	/**
	 * Sets the number of threads to be used during load submission 
	 * (-1 for setting it to the number of processors of the 
	 * host machine)
	 * 
	 * @param threadCount	the number of threads used to load submission (limited to 64)
	 */
	public void setThreadCount(int threadCount) {
		// Limits thread count to a maximum of 64
		if(threadCount <= 64)
			this.threadCount = threadCount;
		else {			
			this.threadCount = 64;
		}	
	}

	public int getThreadCount() {
		return threadCount;
	}
}
