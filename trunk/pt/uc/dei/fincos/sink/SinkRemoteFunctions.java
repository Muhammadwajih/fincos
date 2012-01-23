package pt.uc.dei.fincos.sink;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Properties;

import pt.uc.dei.fincos.basic.InvalidStateException;
import pt.uc.dei.fincos.basic.Status;
import pt.uc.dei.fincos.controller.SinkConfig;




public interface SinkRemoteFunctions extends Remote {
	
	/**
	 * Loads a Sink:
	 * 1) Opens server socket and initializes worker threads to receive events from FINCoS Adapter
	 *     OR
	 *    Subscribes directly to streams at the CEP engine.
	 * 2) Initializes logging, if enabled
	 * 3) Connects to online validator, if enabled
	 * 
	 * 
	 * @param sinkCfg			The configuration to be loaded
	 * @param communicationMode	Either DIRECT_API_COMMUNICATION where events are received from CEP engine directly using API or
	 * 							ADAPTER_CSV_COMMUNICATION where events are received from CEP engine through FINCoS Adapter as CSV messages. 
	 * @param rtMeasurementMode	Either END_TO_END_RT_MILLIS where timestamps are added at Driver and Sinks or
								ADAPTER_RT_NANOS where timestamps are added at Driver and Sinks or
								NO_RT
	 * @param socketBufferSize	The number of events to be buffered during socket communication
	 * @param logFlushInterval	The periodic interval at which log is flushed to disk
	 * @param adapterProperties Properties used to connect directly to CEP engine 
	 * 							(necessary only when 'communicationMode' is set to DIRECT_API_COMMUNICATION) 
	 * @throws IOException
	 */
	public boolean load(SinkConfig sinkCfg, 
						int communicationMode, int rtMeasurementMode,
						int socketBufferSize, int logFlushInterval,
						Properties adapterProperties) 
	throws RemoteException, InvalidStateException, Exception;
	
	
	/**
	 * 
	 * Unloads a Sink:
	 * 1) Closes worker threads and server socket 
	 * 2) Closes logger, if enabled
	 * 3) Disconnects from online validator, if online validation is enabled
	 */
	public void unload() throws RemoteException;
	
	
	
	/**
	 * Gets the status of a remote monitor and its progress
	 * 
	 * @return
	 * @throws RemoteException
	 */	
	public Status getStatus() throws RemoteException;
}
