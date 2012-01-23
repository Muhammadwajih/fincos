package pt.uc.dei.fincos.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.basic.InvalidStateException;
import pt.uc.dei.fincos.basic.Status;
import pt.uc.dei.fincos.basic.Step;
import pt.uc.dei.fincos.driver.DriverRemoteFunctions;
import pt.uc.dei.fincos.driver.SyntheticWorkloadPhase;
import pt.uc.dei.fincos.driver.WorkloadPhase;
import pt.uc.dei.fincos.sink.SinkRemoteFunctions;

/**
 * 
 * Low-level class that implements controller functions:
 * 1) Loading of test setup from configuration file
 * 2) Keeping the list of configured Drivers and Sinks
 * 3) RMI communication with drivers and sinks:
 * 		3.1) Keeping the list of active Drivers and Sinks
 * 		3.2) Initialize Drivers and Sinks
 * 		3.3) Start, pause, stop load submission
 * 		3.4) Alter event submission rates
 * 
 * @author Marcelo R.N. Mendes
 *
 */
public class ControllerFacade {
	private static ControllerFacade instance;
	
	// Test Options, initialized with default values 
	private int socketBufferSize=1; //No buffering
	private int logFlushInterval=0; // Every event is logged immediately to disk
	private int communicationMode=Globals.ADAPTER_CSV_COMMUNICATION; // Communications uses FINCoS adapter
	private int rtMeasurementMode=Globals.END_TO_END_RT_MILLIS;	// Response time resolution is in milliseconds
	private boolean useEventsCreationTime=false; //Indicates if event's creation time must be used instead of their send time
	private String CEPInterfaceConnPropertiesFilePath="";
	private Properties CEPInterfaceConnProperties=null;
	
	// List of drivers and sinks
	private ArrayList<DriverConfig> drivers = new ArrayList<DriverConfig>(1);
	private ArrayList<SinkConfig> sinks = new ArrayList<SinkConfig>(1);	
	
	// RMI interfaces with drivers and sinks
	private HashMap<DriverConfig, DriverRemoteFunctions> remoteDrivers = new HashMap<DriverConfig, DriverRemoteFunctions>(1);
	private HashMap<SinkConfig, SinkRemoteFunctions> remoteSinks = new HashMap<SinkConfig, SinkRemoteFunctions>(1);
	
	// Parser of xml configuration file which contains test setup  
	private ConfigurationParser config = new ConfigurationParser();
	
	private static final Status DISCONNECTED = new Status(Step.DISCONNECTED, 0.0);
	
	public static ControllerFacade getInstance() {
		if(instance == null)
			instance = new ControllerFacade();
	
		return instance;
	}
	
	public void openTestSetup(File f) throws IllegalArgumentException, Exception
	{
		try {			
			config.closeFile();
			config.open(f.getPath());					
			// Retrieve Drivers List
			DriverConfig driverList[] = config.retrieveDriverList();
			this.drivers = new ArrayList<DriverConfig>(driverList.length);
			for (int i = 0; i < driverList.length; i++) {
				this.drivers.add(driverList[i]);			
			}					
			// Initializes remote Driver map
			remoteDrivers = new HashMap<DriverConfig, DriverRemoteFunctions>(this.drivers.size());
			for (DriverConfig dr : this.drivers) {									
				remoteDrivers.put(dr, null);																
			}	
			// Retrieve Sinks List
			SinkConfig sinkList[] = config.retrieveSinkList();
			this.sinks = new ArrayList<SinkConfig>(sinkList.length);
			for (int i = 0; i < sinkList.length; i++) {
				this.sinks.add(sinkList[i]);			
			}			
			// Initializes remote Sink map
			remoteSinks = new HashMap<SinkConfig, SinkRemoteFunctions>(this.sinks.size());
			for (SinkConfig sink : this.sinks) {									
				remoteSinks.put(sink, null);																
			}
		}
		catch (Exception e) {
			cleanup();
			// forwards exception		
			throw new Exception ("Could not open configuration file. " +
					"File may be corrupted."
			);						
		} 

		try {
			this.updateTestOptions(config.getSocketBufferSize(),
					config.getLogFlushInterval(), 
					config.getCommunicationMode(), 
					config.getResponseTimeMeasurementMode(),
					config.isUsingCreationTime(),
					config.getCEPInterfaceConnPropertiesFile());
		}
		catch (FileNotFoundException fnfe) {
			throw fnfe;
		}
		catch (Exception e) {				
			throw new IllegalArgumentException(e.getMessage());				
		}


	}
	
	private void cleanup(){
		config.closeFile();
		if(this.drivers!=null)
			this.drivers.clear();
		if(this.sinks!=null)
			this.sinks.clear();
	}
	
	public boolean isTestSetupLoaded() {
		return config.isFileOpen();
	}
	
	public void saveTestSetupFile() 
	throws  FileNotFoundException, ParserConfigurationException, 
			IOException, TransformerException, Exception 
	{
		DriverConfig[] driverList = new DriverConfig[drivers.size()];
		SinkConfig[] sinkList = new SinkConfig[sinks.size()];
		
		if(config.isFileOpen()) {
			config.save(drivers.toArray(driverList), 
					sinks.toArray(sinkList), 
					socketBufferSize,
					logFlushInterval,
					communicationMode, 
					rtMeasurementMode, 
					useEventsCreationTime, 
					CEPInterfaceConnPropertiesFilePath
			);	
		}
		else {
			throw new Exception("Could not save test setup. File path has not been informed.");
		}		
	}
	
	public void saveTestSetupFileAs(File f) 
	throws  FileNotFoundException, ParserConfigurationException, 
			IOException, TransformerException 
	{
		DriverConfig[] driverList = new DriverConfig[drivers.size()];
		SinkConfig[] sinkList = new SinkConfig[sinks.size()];
		
		config.saveAs(drivers.toArray(driverList), 
				  sinks.toArray(sinkList), 
				  socketBufferSize,
				  logFlushInterval,
				  communicationMode, 
				  rtMeasurementMode, 
				  useEventsCreationTime, 
				  CEPInterfaceConnPropertiesFilePath, 
				  f.getPath());

	}
	
	public synchronized ArrayList<DriverConfig> getDriverList() {
		return this.drivers;
	}
	
	public synchronized ArrayList<SinkConfig> getSinkList() {
		return this.sinks;
	}
	
	/**
	 * Returns the duration of the test in seconds 
	 * (maximum duration amongst all configured drivers).
	 * Applies only if all Drivers have synthetic workloads
	 * (returns -1 if any Driver has a phase based on an external dataset file).
	 */
	public long getTestDuration() {
		long duration = 0;
		long driverDuration;
		WorkloadPhase drPhases[];
		
		for (DriverConfig dr : this.drivers) {
			drPhases = dr.getWorkload();
			driverDuration = 0;
			for (WorkloadPhase phase: drPhases) {
				if(phase instanceof SyntheticWorkloadPhase) 
					driverDuration += ((SyntheticWorkloadPhase) phase).getDuration();
				else
					return -1;
			}
			duration = Math.max(duration, driverDuration);
		}
		
		return duration;

	}
	
	public synchronized void addDriver(DriverConfig dr) {
		this.drivers.add(dr);
		this.remoteDrivers.put(dr, null);
	}
	
	public synchronized void addSink(SinkConfig sink) {
		this.sinks.add(sink);
		this.remoteSinks.put(sink, null);
	}
	
	public synchronized boolean updateDriver(DriverConfig oldCfg, DriverConfig newCfg) {					
		int index = this.drivers.indexOf(oldCfg);
		return updateDriver(index, newCfg);
	}
	
	public synchronized boolean updateSink(SinkConfig oldCfg, SinkConfig newCfg) {
		int index = this.sinks.indexOf(oldCfg);
		return updateSink(index, newCfg);
	}
	
	public synchronized boolean updateDriver(int index, DriverConfig newCfg) {					
		if(index > -1) {			
			DriverConfig oldCfg = this.drivers.get(index);
			this.drivers.remove(index);				
			this.drivers.add(index, newCfg);
			this.remoteDrivers.put(newCfg, remoteDrivers.get(oldCfg));
			this.remoteDrivers.remove(oldCfg);			
			return true;
		}		
		else {
			return false;
		}
	}
	
	public synchronized boolean updateSink(int index, SinkConfig newCfg) {					
		if(index > -1) {
			SinkConfig oldCfg = this.sinks.get(index);
			this.sinks.remove(index);
			this.sinks.add(index, newCfg);				
			this.remoteSinks.put(newCfg, remoteSinks.get(oldCfg));
			this.remoteSinks.remove(oldCfg);
			return true;
		}
		else
			return false;
	}
	
	public synchronized void deleteDriver(DriverConfig dr) {
		this.remoteDrivers.remove(dr);
		this.drivers.remove(dr);
	}
	
	public synchronized void deleteSink(SinkConfig sink) {
		this.remoteSinks.remove(sink);
		this.sinks.remove(sink);
	}
	
	/**
	 * 
	 * Sets values for test parameters 
	 * 
	 * @param socketBufferSize_			Size of buffer in events for socket communication
	 * @param logFlushInterval_			The periodic interval at which log is flushed to disk
	 * @param communicationMode_			Either DIRECT_API_COMMUNICATION where events are sent/received to/from CEP engine directly using API or
	 * 									ADAPTER_CSV_COMMUNICATION where events are sent/received to/from CEP engine through Adapters as CSV messages. 
	 * @param rtMeasurementMode_			Either END_TO_END_RT_MILLIS where timestamps are added at Driver and Sinks or
										ADAPTER_RT_NANOS where timestamps are added at Driver and Sinks or
										NO_RT									
	 * @param useCreationTime_			Indicates if event's creation time must be used instead of their send time
	 * @param cepEngConnPropertiesFile_  Path to file containing Vendor-specific connection properties
	 * 									necessary to connect directly to a CEP engine
	 * @throws Exception 
	 */
	public void updateTestOptions(int socketBufferSize_, int logFlushInterval_, 
								int communicationMode_, int rtMeasurementMode_,	
								boolean useCreationTime_,
								String cepEngConnPropertiesFile_) throws Exception 
	{
		this.socketBufferSize = socketBufferSize_;
		this.logFlushInterval = logFlushInterval_;
		this.communicationMode = communicationMode_;
		this.rtMeasurementMode = rtMeasurementMode_;		
		this.useEventsCreationTime = useCreationTime_;
				
		if(
			this.communicationMode == Globals.DIRECT_API_COMMUNICATION &&
			this.rtMeasurementMode == Globals.ADAPTER_RT_NANOS
		   ) 
		{
			this.rtMeasurementMode = Globals.END_TO_END_RT_MILLIS;
			throw new Exception("Incompatible communication (DIRECT_API_COMMUNICATION)" +
			" and RT measurement (ADAPTER_RT_NANOS) modes. " +
			"RT mode was set to default (END_TO_END_RT_MILLIS)");
		}
		
		if(communicationMode_ == Globals.DIRECT_API_COMMUNICATION) {
			this.CEPInterfaceConnPropertiesFilePath = cepEngConnPropertiesFile_;
			this.CEPInterfaceConnProperties = new Properties();
			FileInputStream fis=null;					
			try {
				fis = new FileInputStream(cepEngConnPropertiesFile_);
				this.CEPInterfaceConnProperties.load(fis);	
			} catch (FileNotFoundException e1) {
				this.communicationMode = Globals.ADAPTER_CSV_COMMUNICATION;
				throw new FileNotFoundException("Could not open CEP Interface properties file (File not Found)." +
						" Communication mode will be set to default (ADAPTER_CSV_COMMUNICATION).");			
			} catch (IOException ioe) {
				this.communicationMode = Globals.ADAPTER_CSV_COMMUNICATION;
				throw new Exception("Could not open CEP Interface properties file ("+
									ioe.getMessage()+")." +
									" Communication mode will be set to default (ADAPTER_CSV_COMMUNICATION).");			
			}	
			finally {
				if(fis != null)
					fis.close();
			}
		}
	}

	public void setSocketBufferSize(int socketBufferSize) {
		this.socketBufferSize = socketBufferSize;
	}

	public int getSocketBufferSize() {
		return socketBufferSize;
	}

	public int getLogFlushInterval() {
		return logFlushInterval;
	}

	public int getCommunicationMode() {
		return communicationMode;
	}

	public int getRtMeasurementMode() {
		return rtMeasurementMode;
	}

	public boolean getUseEventsCreationTime() {
		return useEventsCreationTime;
	}

	public String getCEPInterfaceConnPropertiesFilePath() {
		return CEPInterfaceConnPropertiesFilePath;
	}
	
	public Boolean loadRemoteDriver(DriverConfig dr) 
	throws ConnectException, NotBoundException, AccessException, RemoteException, Exception 
	{
		Boolean ret = null;
		
		DriverRemoteFunctions remoteDr;
		Registry daemonRegistry = LocateRegistry.getRegistry(dr.getAddress().getHostAddress(), Globals.DEFAULT_RMI_PORT);					
		RemoteDaemonServerFunctions remoteDaemon = (RemoteDaemonServerFunctions) daemonRegistry.lookup("FINCoS");
		remoteDaemon.startDriver(dr.getAlias());

		Registry driverRegistry = LocateRegistry.getRegistry(dr.getAddress().getHostAddress(), Globals.DEFAULT_RMI_PORT);
		remoteDr = (DriverRemoteFunctions) driverRegistry.lookup(dr.getAlias());
		synchronized (remoteDrivers) {
			remoteDrivers.put(dr, remoteDr);	
		}

		if(remoteDr != null) 
			ret = remoteDr.load(dr, null, communicationMode, rtMeasurementMode, useEventsCreationTime, 
								socketBufferSize, logFlushInterval, CEPInterfaceConnProperties);
		
		return ret;
	}
	
	public Boolean loadRemotSink(SinkConfig sink) 
	throws ConnectException, NotBoundException, AccessException, RemoteException, Exception 
	{
		Boolean ret = null;

		SinkRemoteFunctions remoteSink;		
		Registry daemonRegistry = LocateRegistry.getRegistry(sink.getAddress().getHostAddress(), Globals.DEFAULT_RMI_PORT);
		RemoteDaemonServerFunctions remoteDaemon = (RemoteDaemonServerFunctions) daemonRegistry.lookup("FINCoS");
		remoteDaemon.startSink(sink.getAlias());

		Registry registry = LocateRegistry.getRegistry(sink.getAddress().getHostAddress(), Globals.DEFAULT_RMI_PORT);
		remoteSink = (SinkRemoteFunctions) registry.lookup(sink.getAlias());
		synchronized (remoteSinks) {
			remoteSinks.put(sink, remoteSink);	
		}

		if(remoteSink != null) 
			ret = remoteSink.load(sink, communicationMode, rtMeasurementMode, socketBufferSize, logFlushInterval, CEPInterfaceConnProperties);
		
		return ret;
	}
	
	public synchronized boolean isDriverConnected(DriverConfig dr) {
		if(this.remoteDrivers == null || this.remoteDrivers.isEmpty()) {
			return false;
		}
		else {
			DriverRemoteFunctions remoteDr = remoteDrivers.get(dr);
			return remoteDr != null;	
		}		
	}
	
	public synchronized boolean isSinkConnected(SinkConfig sink) {
		SinkRemoteFunctions remoteSink = remoteSinks.get(sink);
		return remoteSink != null;
	}
	
	private DriverRemoteFunctions lookupRemoteDriver(DriverConfig dr) 
	throws Exception {
		if(remoteDrivers == null || remoteDrivers.isEmpty()) {
			throw new Exception("Remote connection with Drivers has not been established or" +
								" no Driver has been configured.");
		}
		DriverRemoteFunctions ret = remoteDrivers.get(dr);
		
		if(ret == null)
			throw new Exception("Driver is not connected.");
		else
			return ret;
	}
	
	private SinkRemoteFunctions lookupRemoteSink(SinkConfig sink) 
	throws Exception {
		if(remoteSinks == null || remoteSinks.isEmpty()) {
			throw new Exception("Remote connection with Sinks has not been established or" +
								" no Sink has been configured.");
		}
		SinkRemoteFunctions ret = remoteSinks.get(sink);
		
		if(ret == null)
			throw new Exception("Sink is not connected.");
		else
			return ret;
	}
	
	public synchronized void startRemoteDriver(DriverConfig dr) 
	throws RemoteException, InvalidStateException, Exception 
	{
		DriverRemoteFunctions remoteDr = this.lookupRemoteDriver(dr); 						
		remoteDr.start();
	}
	
	public synchronized void pauseRemoteDriver(DriverConfig dr) 
	throws RemoteException, InvalidStateException, Exception  
	{
		DriverRemoteFunctions remoteDr = this.lookupRemoteDriver(dr);
		remoteDr.pause();
	}
	
	public synchronized void stopRemoteDriver(DriverConfig dr) 
	throws RemoteException, InvalidStateException, Exception 
	{
		DriverRemoteFunctions remoteDr = this.lookupRemoteDriver(dr);
		remoteDr.stop();
	}
	
	public synchronized void stopRemoteSink(SinkConfig sink) 
	throws RemoteException, Exception
	{
		SinkRemoteFunctions remoteSink = this.lookupRemoteSink(sink);
		remoteSink.unload();	
	}
	
	public synchronized void switchRemoteDriverToNextPhase(DriverConfig dr) 
	throws RemoteException, InvalidStateException, Exception
	{
		DriverRemoteFunctions remoteDr = this.lookupRemoteDriver(dr);
		remoteDr.switchToNextPhase();
	}
	
	public synchronized void alterRemoteDriverSubmissionRate(DriverConfig dr, double eventRateFactor) 
	throws RemoteException, InvalidStateException, Exception
	{
		DriverRemoteFunctions remoteDr = this.lookupRemoteDriver(dr);
		remoteDr.alterRate(eventRateFactor);
	}
	
	public synchronized Status getDriverStatus(DriverConfig dr) 
	throws RemoteException 
	{		
		try {
			DriverRemoteFunctions remoteDr = this.lookupRemoteDriver(dr);
			return remoteDr.getStatus();
		} 
		catch (RemoteException re) { // Driver has just disconnected: forward this information with an exception
			this.remoteDrivers.put(dr, null);
			throw re;
		} 
		catch (Exception e) { // Driver disconnection has been detected before.
			return DISCONNECTED; 
		}
		
	}
	
	public synchronized Status getSinkStatus(SinkConfig sink)
	throws RemoteException
	{				
		try {
			SinkRemoteFunctions remoteSink = this.lookupRemoteSink(sink);
			return remoteSink.getStatus();			 
		}
		catch (RemoteException re) { // Sink has just disconnected: forward this information with an exception
			this.remoteSinks.put(sink, null);
			throw re;
		}	
		catch (Exception e) { // Sink disconnection has been detected before.
			return DISCONNECTED; 
		}
	}
	
	/**
	 * Stops the FINCoS Daemon service in a given host
	 * 
	 * @param ipAddress			The host where the Daemon service must be stopped 
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public void terminateFINCoSDaemonService(String ipAddress) 
	throws RemoteException, NotBoundException 
	{
		Registry daemonRegistry = LocateRegistry.getRegistry(ipAddress, Globals.DEFAULT_RMI_PORT);					
		RemoteDaemonServerFunctions remoteDaemon = (RemoteDaemonServerFunctions) daemonRegistry.lookup("FINCoS");
		remoteDaemon.finalizeService();
	}
	
	/**
	 * Stops the FINCoS Daemon service in all hosts where a 
	 * FINCoS Component (Driver or Sink) was configured to run.
	 */
	public void terminateAllFINCOSDaemonServiceInstances() {
		Set<InetAddress> hosts = new HashSet<InetAddress>();				
		
		for (DriverConfig dr : getDriverList()) {			
			hosts.add(dr.getAddress());
		}		
		for (SinkConfig sink : getSinkList()) {
			hosts.add(sink.getAddress());
		}		
		
		for (InetAddress inetAddress : hosts) {
			try {		
				terminateFINCoSDaemonService(inetAddress.getHostAddress());
			} catch (Exception e) {
				// Do nothing. An expected exception will be thrown when application closes.
			}
		}
	}
}
