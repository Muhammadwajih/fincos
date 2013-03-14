/* FINCoS Framework
 * Copyright (C) 2013 CISUC, University of Coimbra
 *
 * Licensed under the terms of The GNU General Public License, Version 2.
 * A copy of the License has been included with this distribution in the
 * fincos-license.txt file.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 */


package pt.uc.dei.fincos.driver;

import java.rmi.Remote;
import java.rmi.RemoteException;

import pt.uc.dei.fincos.basic.InvalidStateException;
import pt.uc.dei.fincos.basic.Status;
import pt.uc.dei.fincos.controller.DriverConfig;
import pt.uc.dei.fincos.perfmon.DriverPerfStats;


/**
 * RMI functions exposed by a Driver.
 *
 * @author  Marcelo R.N. Mendes
 */
public interface DriverRemoteFunctions extends Remote {

    /**
     *
     * Initializes a Driver.
     *
     * @param cfg                       The configuration parameters of a Driver
     * @param rtMode                    Response time measurement mode
     *                                  (either END-TO-END or ADAPTER)
     * @param rtResolution              Response time measurement resolution
     *                                  (either Milliseconds or Nanoseconds)
     * @param useCreationTime           Indicates if event's creation time must be used
     *                                  instead of their send time
     * @param dataFilesDir              An optional parameter indicating the path to an
     *                                  external dataset file
     *
     * @return                          <tt>true</tt> if the Driver has been successfully
     *                                  initialized, <tt>false</tt> otherwise.
     *
     * @throws Exception               if the Driver has already been loaded,
     *                                 for unsuccessful RMI calls and for unexpected errors.
     */
    boolean load(DriverConfig cfg, int rtMode, int rtResolution, boolean useCreationTime,
            String dataFilesDir) throws Exception;


    /**
     * Start/Resume load submission on Drivers.
     *
     * @throws InvalidStateException    if the Drive is currently in a state that cannot be started.
     * @throws RemoteException          for unsuccessful RMI calls.
     */
    void start() throws InvalidStateException, RemoteException;


    /**
     * Pause load submission on Drivers.
     *
     * @throws InvalidStateException   if the Driver is not running
     * @throws RemoteException         for unsuccessful RMI calls
     */
    void pause() throws InvalidStateException, RemoteException;


    /**
     * Stop load submission on Drivers.
     *
     * @throws InvalidStateException   if the Driver has not been loaded yet
     * @throws RemoteException         for unsuccessful RMI calls
     */
    void stop() throws InvalidStateException, RemoteException;


    /**
     * Interrupts current phase and starts the next one.
     * If there is no more phases, the test is finished.
     *
     * @throws InvalidStateException   if the Driver is not running
     * @throws RemoteException         for unsuccessful RMI calls
     */
    void switchToNextPhase() throws InvalidStateException, RemoteException;


    /**
     * Alter event submission rate.
     *
     * @param factor                    factor by which event rates specified
     *                                  in configuration file must be multiplied
     * @throws InvalidStateException    if the Driver is not running
     * @throws RemoteException          for unsuccessful RMI calls
     */
    void alterRate(double factor) throws InvalidStateException, RemoteException;


    /**
     * @return the status of a remote driver and its progress.
     * @throws RemoteException     for unsuccessful RMI calls
     */
    Status getStatus() throws RemoteException;


    /**
     * Enables or disables online performance tracing at the Driver.
     *
     * @param enabled           <tt>true</tt> for enabling performance tracing,
     *                          <tt>false</tt> for disabling it.
     * @throws RemoteException  for unsuccessful RMI calls
     */
    void setPerfTracing(boolean enabled)throws RemoteException;

    /**
     * Retrieves performance stats from this Driver.
     *
     * @return  a map stream -> number of events received since the
     * @throws RemoteException  for unsuccessful RMI calls
     */
    DriverPerfStats getPerfStats() throws RemoteException;


}
