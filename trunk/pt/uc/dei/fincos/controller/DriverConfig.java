/* FINCoS Framework
 * Copyright (C) 2012 CISUC, University of Coimbra
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
import java.util.HashSet;
import java.util.Set;

import pt.uc.dei.fincos.basic.Attribute;
import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.driver.ExternalFileWorkloadPhase;
import pt.uc.dei.fincos.driver.SyntheticWorkloadPhase;
import pt.uc.dei.fincos.driver.WorkloadPhase;



/**
 * Class that encapsulates the configuration of a Driver.
 *
 * @author  Marcelo R.N. Mendes
 *
 * @see ComponentConfig
 *
 */
public class DriverConfig extends ComponentConfig implements Cloneable {
    /** Serial id. */
    private static final long serialVersionUID = -3142718728972724155L;

    /** The workload generated by this driver. */
    private WorkloadPhase[] workload;

    /** The number of threads to be used during load generation. */
    private int threadCount;

    /**
     * Creates a Driver configuration.
     *
     * @param alias                     An unique identifier for the Driver
     * @param address                   The address of the machine where the Driver should run
     * @param connection                Connection through which events must be sent
     * @param workload                  The workload specification of the Driver
     * @param threadCount               The number of threads to be used during load generation
     *                                  (-1 for creating as many threads as the number of processors
     *                                  in the host machine)
     * @param loggingEnabled            Indicates if sent events must be logged to disk
     * @param fieldsToLog               Either all fields (LOG_ALL_FIELDS) or only timestamps (LOG_ONLY_TIMESTAMPS)
     * @param loggingSamplingRate       The fraction of events that will be logged
     * @param logFlushInterval          The periodic interval at which log is flushed to disk
     *
     */
    public DriverConfig(String alias, InetAddress address, ConnectionConfig connection,
            WorkloadPhase[] workload, int threadCount, boolean loggingEnabled, int fieldsToLog,
            double loggingSamplingRate, int logFlushInterval) {
        super(alias, address, connection, loggingEnabled, fieldsToLog, loggingSamplingRate, logFlushInterval);
        this.setWorkload(workload);
        this.setThreadCount(threadCount);
    }


    public void setWorkload(WorkloadPhase[] workload) {
        this.workload = workload;
    }


    /**
     *
     * @return  The workload of this Driver
     */
    public WorkloadPhase[] getWorkload() {
        return workload;
    }


    @Override
    protected Object clone() {
        return new DriverConfig(this.getAlias(), this.getAddress(), this.getConnection(),
                this.workload, this.threadCount, this.isLoggingEnabled(),
                this.getFieldsToLog(), this.getLoggingSamplingRate(),
                this.getLogFlushInterval());
    }

    /**
     * Retrieves the names of all input streams configured for this Driver.
     *
     * @return  The names of the input streams
     */
    public String[] getStreamNames() {
        EventType[] types = this.getStreamList();
        String[] ret = new String[types.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = types[i].getName();
        }

        return ret;
    }

    /**
     * Retrieves the list of all input streams configured for this Driver.
     *
     * @return	The Schemas of the input streams
     */
    public EventType[] getStreamList() {
        EventType[] ret = new EventType[0];

        SyntheticWorkloadPhase syntheticPhase;
        ExternalFileWorkloadPhase externalFilePhase;

        Set<EventType> streams = new HashSet<EventType>();

        for (WorkloadPhase phase: this.getWorkload()) {
            if (phase instanceof SyntheticWorkloadPhase) {
                syntheticPhase = (SyntheticWorkloadPhase) phase;
                for (EventType inputStream: syntheticPhase.getSchema().keySet()) {
                    streams.add(inputStream);
                }
            } else if (phase instanceof ExternalFileWorkloadPhase) {
                externalFilePhase = (ExternalFileWorkloadPhase) phase;
                if (externalFilePhase.containsEventTypes()) {
                    //streams.add(new EventType("Unknown", new Attribute[0]));
                } else {
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
     * host machine).
     *
     * @param threadCount	the number of threads used to load submission (limited to 64)
     */
    public void setThreadCount(int threadCount) {
        // Limits thread count to a maximum of 64
        if (threadCount <= 64) {
            this.threadCount = threadCount;
        } else {
            this.threadCount = 64;
        }
    }

    /**
     *
     * @return  the number of threads used to load submission
     */
    public int getThreadCount() {
        return threadCount;
    }
}