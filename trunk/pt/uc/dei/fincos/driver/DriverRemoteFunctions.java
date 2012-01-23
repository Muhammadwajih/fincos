package pt.uc.dei.fincos.driver;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Properties;

import pt.uc.dei.fincos.basic.InvalidStateException;
import pt.uc.dei.fincos.basic.Status;
import pt.uc.dei.fincos.controller.DriverConfig;




public interface DriverRemoteFunctions extends Remote {
	
	/**
	 * 
	 * Initializes a driver
	 * 
	 * @param dr				The configuration parameters of a Driver
	 * @param dataFilesDir		An optional parameter indicating the path to an external dataset file
	 * @param communicationMode	Either DIRECT_API_COMMUNICATION where events are sent to CEP engine directly using API or
	 * 							ADAPTER_CSV_COMMUNICATION where events are sent to CEP engine through FINCoS Adapter, as CSV messages. 
	 * @param rtMeasurementMode	Either END_TO_END_RT_MILLIS where timestamps are added at Driver and Sinks or
								ADAPTER_RT_NANOS where timestamps are added at Driver and Sinks or
								NO_RT
	 * @param useCreationTime	Indicates if event's creation time must be used instead of their send time
	 * @param socketBufferSize	The number of events to be buffered during socket communication
	 * @param logFlushInterval	The periodic interval at which log is flushed to disk
	 * @param adapterProperties Properties used to connect directly to CEP engine 
	 * 							(necessary only when 'communicationMode' is set to DIRECT_API_COMMUNICATION)
	 * @return
	 * @throws InvalidStateException
	 * @throws RemoteException
	 */
	public boolean load (DriverConfig dr, String dataFilesDir, 
						int communicationMode, int rtMeasurementMode,
						boolean useCreationTime,
						int socketBufferSize, int logFlushInterval,
						Properties adapterProperties) throws InvalidStateException, RemoteException, Exception;
	
	
	/**
	 * Start/Resume load submission on Drivers
	 * 
	 * @throws InvalidStateException
	 * @throws RemoteException
	 */
	public void start() throws InvalidStateException, RemoteException;
	
	
	/**
	 * Pause load submission on Drivers
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public void pause() throws InvalidStateException, RemoteException;
	
	
	/**
	 * Stop load submission on Drivers
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public void stop() throws InvalidStateException, RemoteException;
	
	
	/**
	 * Interrupts current phase and starts the next one. 
	 * If there is no more phases, the test is finished.
	 * 
	 * @throws RemoteException
	 */
	public void switchToNextPhase() throws InvalidStateException, RemoteException;
	
	
	/**
	 * Alter event submission rate
	 * 
	 * @param factor					factor by which event rates specified in configuration file must be multiplied
	 * @throws InvalidStateException
	 * @throws RemoteException
	 */
	public void alterRate(double factor) throws InvalidStateException, RemoteException;
	
	/**
	 * Gets the status of a remote driver and its progress
	 * 
	 * @return
	 * @throws RemoteException
	 */	
	public Status getStatus() throws RemoteException;
}
