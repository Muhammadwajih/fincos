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

import java.io.Serializable;
import java.net.InetAddress;

import pt.uc.dei.fincos.basic.Globals;

/**
 *
 * Class that encapsulates the configuration of a component
 * involved in load generation (Driver or Sink).
 *
 * @author  Marcelo R.N. Mendes
 *
 * @see DriverConfig
 * @see SinkConfig
 *
 */
public abstract class ComponentConfig implements Serializable {
    /** Serial id. */
    private static final long serialVersionUID = -2242158660695499721L;

    /** Unique identifier for this component. */
    private String alias;

    /** The IP address of the host where this component must run. */
    private InetAddress address;

    /** Connection through which events must be sent/received. */
    private ConnectionConfig connection;

    /** Flag indicating if events must be logged to disk. */
    private boolean loggingEnabled;

    /** Indicates which fields must be logged (all or timestamp only).*/
    private int fieldsToLog;

    /** Logging sampling rate. */
    private double loggingSamplingRate;

    /** The periodic interval at which log is flushed to disk. */
    private int logFlushInterval;


    /**
     *
     *
     * @param alias                 unique identifier of the component
     * @param address               IP address where this component runs
     * @param connection            a connection with the target system
     * @param loggingEnabled        Indicates if sent/received events must be logged to disk
     * @param fieldsToLog           Either all fields (LOG_ALL_FIELDS)
     *                              or only timestamps (LOG_ONLY_TIMESTAMPS)
     * @param loggingSamplingRate   The fraction of events that will be logged
     * @param logFlushInterval      The periodic interval at which log is flushed to disk
     */
    public ComponentConfig(String alias, InetAddress address, ConnectionConfig connection,
                           boolean loggingEnabled, int fieldsToLog, double loggingSamplingRate,
                           int logFlushInterval) {
        this.alias = alias;
        this.address = address;
        this.connection = connection;
        this.loggingEnabled = loggingEnabled;
        this.fieldsToLog = fieldsToLog;
        this.loggingSamplingRate = loggingSamplingRate;
        this.logFlushInterval = logFlushInterval;
    }

    /**
     *
     * @return  The unique identifier of this component.
     */
    public final String getAlias() {
        return alias;
    }

    /**
     *  Sets the alias of this component.
     *
     * @param alias a unique identifier.
     */
    public final void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     *
     * @return  the address where this component must run.
     */
    public final InetAddress getAddress() {
        return address;
    }

    /**
     * Sets the address where this component must run.
     *
     * @param address   component's address
     */
    public final void setAddress(InetAddress address) {
        this.address = address;
    }

    /**
     * Sets the connection through which this component will send/receive events.
     *
     * @param connection    the connection configuration object
     */
    public final void setConnection(ConnectionConfig connection) {
        this.connection = connection;
    }

    /**
     *
     * @return  the connection through which this component will send/receive events.
     */
    public final ConnectionConfig getConnection() {
        return connection;
    }


    /**
     *
     * Sets a flag indicating if events must be logged to disk.
     *
     * @param loggingEnabled    <tt>true</tt> to enable log, <tt>false</tt> do disable it
     */
    public final void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    /**
     *
     * @return  <tt>true</tt> if this component is configured to log events to disk,
     *          <tt>false</tt> otherwise.
     */
    public final boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    /**
     * Sets which fields must be logged.
     *
     * @param fieldsToLog   Either {@link Globals}.LOG_ALL_FIELDS or
     *                      {@link Globals}.LOG_ONLY_TIMESTAMPS
     */
    public final void setFieldsToLog(int fieldsToLog) {
        this.fieldsToLog = fieldsToLog;
    }

    /**
     *
     * @return  fields must be logged.
     */
    public final int getFieldsToLog() {
        return fieldsToLog;
    }

    /**
     * Sets the logging sampling rate.
     *
     * @param loggingSamplingRate   the fraction of events that will be logged
     */
    public final void setLoggingSamplingRate(double loggingSamplingRate) {
        this.loggingSamplingRate = loggingSamplingRate;
    }

    /**
     *
     * @return  the fraction of events that will be logged to disk.
     */
    public final double getLoggingSamplingRate() {
        return loggingSamplingRate;
    }

    /**
     * Sets the frequency interval (in milliseconds) at which this component's
     * log must be flushed to disk.
     *
     * @param logFlushInterval  the log flush interval
     */
    public final void setLogFlushInterval(int logFlushInterval) {
        this.logFlushInterval = logFlushInterval;
    }

    /**
     *
     * @return  the frequency interval (in milliseconds) at which this component's
     *          log must be flushed to disk.
     */
    public final int getLogFlushInterval() {
        return logFlushInterval;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ComponentConfig other = (ComponentConfig) obj;
        if (alias == null) {
            if (other.alias != null) {
                return false;
            }
        } else if (!alias.equals(other.alias)) {
            return false;
        }
        return true;
    }


}
