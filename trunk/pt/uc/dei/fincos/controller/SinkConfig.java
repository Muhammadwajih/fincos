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


package pt.uc.dei.fincos.controller;

import java.net.InetAddress;

/**
 * Class that encapsulates the configuration of a Sink.
 *
 * @author  Marcelo R.N. Mendes
 *
 * @see ComponentConfig
 *
 */
public final class SinkConfig extends ComponentConfig implements Cloneable {
    /** Serial id. */
    private static final long serialVersionUID = -7247958453310369444L;

    /** List of streams this Sink must listen to. */
    private String[] outputStreamList;

    /**
     * Creates a Sink configuration.
     *
     * @param alias                     An unique identifier for the Sink
     * @param address                   The address of the machine where the Sink must run
     * @param connection                Connection through which events must be received
     * @param outputStreamsList         A list of streams to which the Sink is associated
     * @param loggingEnabled            Indicates if received events must be logged to disk
     * @param fieldsToLog               Either all fields (LOG_ALL_FIELDS)
     *                                  or only timestamps (LOG_ONLY_TIMESTAMPS)
     * @param loggingSamplingRate       The fraction of all events that will be logged
     * @param logFlushInterval          The periodic interval at which log is flushed to disk
     */
    public SinkConfig(String alias, InetAddress address, ConnectionConfig connection,
            String[] outputStreamsList, boolean loggingEnabled, int fieldsToLog,
            double loggingSamplingRate, int logFlushInterval) {
        super(alias, address, connection, loggingEnabled, fieldsToLog,
              loggingSamplingRate, logFlushInterval);
        this.setOutputStreamList(outputStreamsList);
    }

    /**
     *
     * @return  The list of streams that this Sink will listen to.
     */
    public String[] getOutputStreamList() {
        return outputStreamList;
    }

    /**
     * Sets the list of streams that this Sink will listen to.
     *
     * @param outputStreamList   The list of streams
     */
    public void setOutputStreamList(String[] outputStreamList) {
        if (outputStreamList != null) {
            this.outputStreamList = outputStreamList;
        } else {
            this.outputStreamList = new String[0];
        }
    }

    /**
     *
     * @return  The number of streams this Sink will listen to
     */
    public int getStreamCount() {
        return this.outputStreamList.length;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new SinkConfig(this.getAlias(), this.getAddress(),
                this.getConnection(), this.outputStreamList,
                this.isLoggingEnabled(), this.getFieldsToLog(),
                this.getLoggingSamplingRate(),
                this.getLogFlushInterval());
    }
}
