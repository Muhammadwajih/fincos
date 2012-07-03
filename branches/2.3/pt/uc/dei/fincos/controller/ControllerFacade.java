package pt.uc.dei.fincos.controller;

import java.io.File;
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
 * Facade class that implementing controller functions:
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

    /** Singleton instance of the facade. */
    private static ControllerFacade instance;

    /** Response time measurement mode (either END-TO-END or ADAPTER). */
    private int rtMode;

    /** Response time measurement resolution (either Milliseconds or Nanoseconds). */
    private int rtResolution;

    /** Indicates if event's creation time must be used instead of their send time. */
    private boolean useEventsCreationTime = false;

    /** List of configured Drivers. */
    private ArrayList<DriverConfig> drivers = new ArrayList<DriverConfig>(1);

    /** List of configured Sinks. */
    private ArrayList<SinkConfig> sinks = new ArrayList<SinkConfig>(1);

    /** RMI interfaces with Drivers. */
    private HashMap<DriverConfig, DriverRemoteFunctions> remoteDrivers = new HashMap<DriverConfig, DriverRemoteFunctions>(1);

    /** RMI interfaces with Sinks. */
    private HashMap<SinkConfig, SinkRemoteFunctions> remoteSinks = new HashMap<SinkConfig, SinkRemoteFunctions>(1);

    /** Parser of xml configuration file which contains test setup. */
    private ConfigurationParser config = new ConfigurationParser();

    private static final Status DISCONNECTED = new Status(Step.DISCONNECTED, 0.0);

    /**
     *
     * @return the unique instance of the Facade
     */
    public static ControllerFacade getInstance() {
        if (instance == null) {
            instance = new ControllerFacade();
        }

        return instance;
    }

    /**
     * Loads a test setup file.
     *
     * @param f                             the setup file to be loaded
     * @throws Exception                    if the setup file is corrupted
     */
    public void openTestSetup(File f) throws Exception {
        try {
            config.closeFile();
            config.open(f.getPath());
            // Retrieve Drivers List
            DriverConfig[] driverList = config.retrieveDriverList();
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
            SinkConfig[] sinkList = config.retrieveSinkList();
            this.sinks = new ArrayList<SinkConfig>(sinkList.length);
            for (int i = 0; i < sinkList.length; i++) {
                this.sinks.add(sinkList[i]);
            }
            // Initializes remote Sink map
            remoteSinks = new HashMap<SinkConfig, SinkRemoteFunctions>(this.sinks.size());
            for (SinkConfig sink : this.sinks) {
                remoteSinks.put(sink, null);
            }
        } catch (Exception e) {
            cleanup();
            e.printStackTrace();
            throw new Exception("Could not open configuration file. File may be corrupted.");
        }

        try {
            this.updateTestOptions(config.getResponseTimeMode(), config.getResponseTimeResolution(),
                    config.isUsingCreationTime());
        } catch (FileNotFoundException fnfe) {
            throw fnfe;
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private void cleanup() {
        config.closeFile();
        if (this.drivers != null) {
            this.drivers.clear();
        }
        if (this.sinks != null) {
            this.sinks.clear();
        }
    }

    /**
     * Indicates if the Facade has a setup file loaded.
     *
     * @return  <tt>true</tt> if there is currently a setup loaded, <tt>false</tt> otherwise.
     */
    public boolean isTestSetupLoaded() {
        return config.isFileOpen();
    }

    /**
     * Saves an already open test setup file to disk.
     *
     * @throws FileNotFoundException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TransformerException
     * @throws Exception
     */
    public void saveTestSetupFile() throws  FileNotFoundException, ParserConfigurationException,
    IOException, TransformerException, Exception {
        DriverConfig[] driverList = new DriverConfig[drivers.size()];
        SinkConfig[] sinkList = new SinkConfig[sinks.size()];
        if (config.isFileOpen()) {
            config.save(drivers.toArray(driverList), sinks.toArray(sinkList),
                        rtMode, rtResolution, useEventsCreationTime);
        } else {
            throw new Exception("Could not save test setup. File path has not been informed.");
        }
    }

    /**
     * Saves an already open test setup into a new file on disk.
     *
     * @param f                                 the new file, where the setup must be saved
     * @throws FileNotFoundException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws TransformerException
     */
    public void saveTestSetupFileAs(File f) throws  FileNotFoundException, ParserConfigurationException,
    IOException, TransformerException {
        DriverConfig[] driverList = new DriverConfig[drivers.size()];
        SinkConfig[] sinkList = new SinkConfig[sinks.size()];
        config.saveAs(drivers.toArray(driverList),
                sinks.toArray(sinkList),
                rtMode, rtResolution,
                useEventsCreationTime,
                f.getPath());
    }

    /**
     *
     * @return  the list of Drivers in a test setup file (if already loaded)
     */
    public synchronized ArrayList<DriverConfig> getDriverList() {
        return this.drivers;
    }

    /**
     *
     * @return  the list of Sinks in a test setup file (if already loaded)
     */
    public synchronized ArrayList<SinkConfig> getSinkList() {
        return this.sinks;
    }

    /**
    *
    * @return  the list of Drivers in a test setup file (if already loaded)
    */
   public synchronized HashMap<DriverConfig, DriverRemoteFunctions> getRemoteDrivers() {
       return this.remoteDrivers;
   }

   /**
    *
    * @return  the list of Sinks in a test setup file (if already loaded)
    */
   public synchronized HashMap<SinkConfig, SinkRemoteFunctions> getRemoteSinks() {
       return this.remoteSinks;
   }


    /**
     * Returns the total duration of the test in seconds
     * (applies only if all Drivers have synthetic workloads).
     *
     * @return the maximum duration amongst all configured drivers, or -1 if any Driver has a phase based on an external dataset file
     */
    public long getTestDuration() {
        long duration = 0;
        long driverDuration;
        WorkloadPhase[] drPhases;

        for (DriverConfig dr : this.drivers) {
            drPhases = dr.getWorkload();
            driverDuration = 0;
            for (WorkloadPhase phase: drPhases) {
                if (phase instanceof SyntheticWorkloadPhase) {
                    driverDuration += ((SyntheticWorkloadPhase) phase).getDuration();
                } else {
                    return -1;
                }
            }
            duration = Math.max(duration, driverDuration);
        }

        return duration;

    }

    /**
     * Adds a Driver to a test setup.
     *
     * @param dr    The Driver to be added
     */
    public synchronized void addDriver(DriverConfig dr) {
        this.drivers.add(dr);
        this.remoteDrivers.put(dr, null);
    }

    /**
     * Adds a Sink to a test setup.
     *
     * @param sink    The Sink to be added
     */
    public synchronized void addSink(SinkConfig sink) {
        this.sinks.add(sink);
        this.remoteSinks.put(sink, null);
    }

    /**
     * Updates a Driver in the test setup.
     *
     * @param oldCfg    the old configuration of the Driver
     * @param newCfg    the new configuration of the Driver
     * @return          <tt>true</tt> if the Driver has been successfully updated,
     *                  <tt>false</tt> otherwise
     */
    public synchronized boolean updateDriver(DriverConfig oldCfg, DriverConfig newCfg) {
        int index = this.drivers.indexOf(oldCfg);
        return updateDriver(index, newCfg);
    }

    /**
     * Updates a Sink in the test setup.
     *
     * @param oldCfg    the old configuration of the Sink
     * @param newCfg    the new configuration of the Sink
     * @return          <tt>true</tt> if the Sink has been successfully updated,
     *                  <tt>false</tt> otherwise
     */
    public synchronized boolean updateSink(SinkConfig oldCfg, SinkConfig newCfg) {
        int index = this.sinks.indexOf(oldCfg);
        return updateSink(index, newCfg);
    }

    /**
     * Updates a Driver in the test setup.
     *
     * @param index     the index of the Driver to be updated
     * @param newCfg    the new configuration of the Driver
     * @return          <tt>true</tt> if the Driver has been successfully updated,
     *                  <tt>false</tt> otherwise
     */
    public synchronized boolean updateDriver(int index, DriverConfig newCfg) {
        if (index > -1) {
            DriverConfig oldCfg = this.drivers.get(index);
            this.drivers.remove(index);
            this.drivers.add(index, newCfg);
            this.remoteDrivers.put(newCfg, remoteDrivers.get(oldCfg));
            this.remoteDrivers.remove(oldCfg);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Updates a Sink in the test setup.
     *
     * @param index     the index of the Sink to be updated
     * @param newCfg    the new configuration of the Sink
     * @return          <tt>true</tt> if the Sink has been successfully updated,
     *                  <tt>false</tt> otherwise
     */
    public synchronized boolean updateSink(int index, SinkConfig newCfg) {
        if (index > -1) {
            SinkConfig oldCfg = this.sinks.get(index);
            this.sinks.remove(index);
            this.sinks.add(index, newCfg);
            this.remoteSinks.put(newCfg, remoteSinks.get(oldCfg));
            this.remoteSinks.remove(oldCfg);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Deletes a Driver from the test setup.
     *
     * @param dr    the Driver to be deleted
     */
    public synchronized void deleteDriver(DriverConfig dr) {
        this.remoteDrivers.remove(dr);
        this.drivers.remove(dr);
    }

    /**
     * Deletes a Sink from the test setup.
     *
     * @param sink    the Sink to be deleted
     */
    public synchronized void deleteSink(SinkConfig sink) {
        this.remoteSinks.remove(sink);
        this.sinks.remove(sink);
    }

    /**
     *
     * Sets values for test parameters.
     *
     * @param rtMode     Either END_TO_END_RT_MILLIS, ADAPTER_RT_NANOS, or NO_RT
     * @param useCreationTime       Indicates if event's creation time must be used instead of their send time
     *
     * @throws Exception
     */
    public void updateTestOptions(int rtMode, int rtResolution, boolean useCreationTime) throws Exception
    {
        this.rtMode = rtMode;
        this.rtResolution = rtResolution;
        this.useEventsCreationTime = useCreationTime;
    }

    /**
     *
     * @return  the response time measurement mode used in the test setup
     *          (either END_TO_END_RT_MILLIS, ADAPTER_RT_NANOS, or NO_RT)
     */
    public int getRtMode() {
        return rtMode;
    }

    /**
    *
    * @return  the response time resolution used in the test setup
    *          (either milliseconds or nanoseconds)
    */
   public int getRtResolution() {
       return rtResolution;
   }

    /**
     *
     * @return  <tt>true</tt> if the test setup uses events' creation time instead of
     *          their send time for RT measurement, <tt>false</tt> otherwise
     */
    public boolean getUseEventsCreationTime() {
        return useEventsCreationTime;
    }

    /**
     * Loads a remote Driver through RMI.
     *
     * @param dr                    the remote Driver to be loaded
     * @return                      <tt>true</tt> if the Driver has been successfully loaded,
     *                              <tt>false</tt> otherwise
     * @throws ConnectException     if an error occurs while connecting with the remote Driver
     * @throws NotBoundException    if an error occurs while connecting with the remote Driver
     * @throws AccessException      if an error occurs while connecting with the remote Driver
     * @throws RemoteException      if an error occurs while connecting with the remote Driver
     * @throws Exception            for unexpected errors
     */
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

        if (remoteDr != null) {
            ret = remoteDr.load(dr, rtMode, rtResolution, useEventsCreationTime, null);
        }

        return ret;
    }

    /**
     * Loads a remote Sink through RMI.
     *
     * @param sink                  the remote Sink to be loaded
     * @return                      <tt>true</tt> if the Sink has been successfully loaded,
     *                              <tt>false</tt> otherwise
     * @throws ConnectException     if an error occurs while connecting with the remote Sink
     * @throws NotBoundException    if an error occurs while connecting with the remote Sink
     * @throws AccessException      if an error occurs while connecting with the remote Sink
     * @throws RemoteException      if an error occurs while connecting with the remote Sink
     * @throws Exception            for unexpected errors
     */
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

        if (remoteSink != null) {
            ret = remoteSink.load(sink, rtMode, rtResolution);
        }

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
