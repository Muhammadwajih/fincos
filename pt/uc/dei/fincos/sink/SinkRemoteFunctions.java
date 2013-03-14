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


package pt.uc.dei.fincos.sink;

import java.rmi.Remote;
import java.rmi.RemoteException;

import pt.uc.dei.fincos.basic.Status;
import pt.uc.dei.fincos.controller.SinkConfig;
import pt.uc.dei.fincos.perfmon.SinkPerfStats;


/**
 * RMI functions exposed by a Sink.
 *
 * @author  Marcelo R.N. Mendes
 */
public interface SinkRemoteFunctions extends Remote {

    /**
     * Initializes a Sink.
     *
     * @param sinkCfg                   the configuration to be loaded
     * @param rtMode                    response time measurement mode
     *                                  (either END-TO-END or ADAPTER)
     * @param rtResolution              response time measurement resolution
     *                                  (either Milliseconds or Nanoseconds)
     *
     * @return                          <tt>true</tt> if the Sink has been successfully
     *                                  initialized, <tt>false</tt> otherwise.
     *
     * @throws Exception                if the Sink has already been loaded,
     *                                  for unsuccessful RMI calls, and for unexpected errors.
     */
    boolean load(SinkConfig sinkCfg, int rtMode, int rtResolution)
    throws Exception;


    /**
     *
     * Unloads a Sink.
     *
     * @throws RemoteException  for unsuccessful RMI calls
     */
    void unload() throws RemoteException;


    /**
     *
     * @return the status of a remote Sink
     * @throws RemoteException  for unsuccessful RMI calls
     */
    Status getStatus() throws RemoteException;


    /**
     * Enables or disables online performance tracing at the Sink.
     *
     * @param enabled           <tt>true</tt> for enabling performance tracing,
     *                          <tt>false</tt> for disabling it.
     * @throws RemoteException  for unsuccessful RMI calls
     */
    void setPerfTracing(boolean enabled)throws RemoteException;


    /**
     * Retrieves performance stats from this Sink.
     *
     * @return  a map stream -> number of events received since the
     * @throws RemoteException  for unsuccessful RMI calls
     */
    SinkPerfStats getPerfStats() throws RemoteException;
}
