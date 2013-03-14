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


package pt.uc.dei.fincos.adapters;

import pt.uc.dei.fincos.sink.Sink;

/**
 *
 * Thread for listening incoming events from the CEP Engine and
 * forwarding them to a given Sink.
 *
 * @author  Marcelo R.N. Mendes
 *
 */
public abstract class OutputListener extends Thread {
    /** An alias for this listener (for logging purposes). */
    protected final String listenerID;

    /** Response time measurement mode (either END-TO-END or ADAPTER). */
    protected final int rtMode;

    /** Response time measurement resolution
     * (either Milliseconds or Nanoseconds). */
    protected final int rtResolution;

    /** Reference to the Sink instance to which results must be forwarded
     * (DIRECT COMMUNICATION). */
    protected final Sink sinkInstance;

    /** Flag used to interrupt event listening. */
    protected boolean keepListening = true;

    /**
     * Constructor for DIRECT COMMUNICATION.
     *
     * @param lsnrID        an alias for this listener
     * @param rtMode        response time measurement mode
     *                      (either END-TO-END, ADAPTER or NO_RT)
     * @param rtResolution  response time measurement resolution
     *                      (either Milliseconds or Nanoseconds)
     * @param sinkInstance  reference to the Sink instance to which results must be forwarded
     */
    public OutputListener(String lsnrID, int rtMode, int rtResolution, Sink sinkInstance) {
        this.listenerID = lsnrID;
        this.rtMode = rtMode;
        this.rtResolution = rtResolution;
        this.sinkInstance = sinkInstance;
    }


    /**
     * Callback method used to receive incoming results from the CEP engine and
     * forward them to an appropriate Sink.
     *
     * @param e     the incoming event, represented as an array of values
     */
    public final void onOutput(Object[] e) {
        this.sinkInstance.processOutputEvent(e);
    }


    /**
     * Performs any vendor-specific initialization on the listener.
     *
     * @throws Exception    if an error occurs
     */
    public abstract void load() throws Exception;

    /**
     * Disconnects from CEP Engine and from Sink.
     */
    public abstract void disconnect();

}
