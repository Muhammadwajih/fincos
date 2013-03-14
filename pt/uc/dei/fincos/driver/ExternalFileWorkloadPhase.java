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

import java.security.InvalidParameterException;

/**
 * Class representing a phase in the workload of a Driver that is based on an
 * external dataset file.
 *
 * @author  Marcelo R.N. Mendes
 *
 * @see SyntheticWorkloadPhase
 */
public final class ExternalFileWorkloadPhase extends WorkloadPhase {
    /** serial id. */
    private static final long serialVersionUID = -8771391854252211855L;

    /** The path of the external file. */
    private String filePath;

    /** Sequence of characters used to separate the fields of the records in the data file. */
    private String delimiter;

    /** A flag indicating if the external file contains timestamps.  */
    private boolean containsTimestamps;

    /** Use the timestamps in the file. */
    private boolean useTimestamps;

    /** The time unit of the timestamps in the external data file. */
    private int timestampUnit;

    /** The index of the field containing the timestamp of the records in the data file. */
    private int timestampIndex;

    /** A flag indicating if the timestamp field (if present)
     * must be included in the payload of the events. */
    private boolean includeTS;

    /** A flag indicating if the external file contains event types. */
    private boolean containsEventTypes;

    /** The index of the field containing the type of the records in the data file. */
    private int typeIndex;

    /** The external file does not contain event types. All events are of the same type. */
    private String singleEventTypeName;

    /** Injection rate, when not using the timestamps in the data file. */
    private double eventSubmissionRate;

    /** The number of times the external file will be submitted. */
    private int loopCount;

    /** Timestamp of the data file is expressed in milliseconds. */
    public static final int MILLISECONDS = 0;

    /** Timestamp of the data file is expressed in seconds. */
    public static final int SECONDS = 1;

    /** Timestamp of the data file is expressed as a date and time record. */
    public static final int DATE_TIME = 2;

    /** First field in the event. */
    public static final int FIRST_FIELD = 0;

    /** Second field in the event. */
    public static final int SECOND_FIELD = 1;

    /** Last field in the event. */
    public static final int LAST_FIELD = 2;

    /** Second last field in the event. */
    public static final int SECOND_LAST_FIELD = 3;



    /**
     *
     * @param filePath              the path of the data file
     * @param delimiter             sequence of characters used to separate the
     *                              fields of the records in the data file
     * @param containsTimestamps    a flag indicating if the data file contains timestamps
     * @param useTimestamps         a flag indicating if the timestamps in the
     *                              file should be used for event submission
     * @param timestampUnit         the time unit of the timestamps in the external data file
     * @param timestampIndex        the index of the field containing the timestamp
     *                              of the records in the data file
     * @param includeTimestamp      A flag indicating if the timestamp field (if present)
     *                              must be included in the payload of the events
     * @param containsEventTypes    a flag indicating if the data file contains event types
     * @param typeIndex             the index of the field containing the type
     *                              of the records in the data file
     * @param singleEventTypeName   type of the events in the data file, if it is not typed.
     * @param loopCount             the number of times the external file will be submitted
     * @param eventSubmissionRate   injection rate, when not using the timestamps in the data file
     */
    public ExternalFileWorkloadPhase(String filePath, String delimiter,
            boolean containsTimestamps, boolean useTimestamps, int timestampUnit,
            int timestampIndex, boolean includeTimestamp,
            boolean containsEventTypes, int typeIndex, String singleEventTypeName,
            int loopCount, double eventSubmissionRate) {
        setFilePath(filePath);
        setDelimiter(delimiter);
        setLoopCount(loopCount);
        setContainsTimestamps(containsTimestamps);
        setUseTimestamps(useTimestamps);
        setTimestampUnit(timestampUnit);
        setTimestampIndex(timestampIndex);
        setIncludeTS(includeTimestamp);
        setEventSubmissionRate(eventSubmissionRate);
        setContainsEventTypes(containsEventTypes);
        setTypeIndex(typeIndex);
        setSingleEventTypeName(singleEventTypeName);
    }

    /**
     * Sets the the time unit of the timestamps in the external data file.
     *
     * @param timestampUnit     the time unit
     *                          (either MILLISECONDS, SECONDS, or DATE_TIME)
     *
     */
    private void setTimestampUnit(int timestampUnit) {
        if (timestampUnit == MILLISECONDS
         || timestampUnit == SECONDS
         || timestampUnit == DATE_TIME) {
            this.timestampUnit = timestampUnit;
        } else {
            throw new InvalidParameterException("Invalid timestamp unit.");
        }
    }

    /**
     *
     * @return  the time unit of the timestamps in the external data file
     */
    public int getTimestampUnit() {
        return timestampUnit;
    }

    /**
     * Sets The index of the field containing the timestamp of the records in the data file.
     *
     * @param timestampIndex    either FIRST_FIELD, SECOND_FIELD,
     *                          LAST_FIELD or SECOND_LAST_FIELD
     */
    private void setTimestampIndex(int timestampIndex) {
        if (timestampIndex == FIRST_FIELD
            || timestampIndex == SECOND_FIELD
            || timestampIndex == LAST_FIELD
            || timestampIndex == SECOND_LAST_FIELD) {
            this.timestampIndex = timestampIndex;
        } else {
            this.timestampIndex = -1;
        }
    }

    /**
     *
     * @return  the index of the field containing the timestamp of the records in the data file
     */
    public int getTimestampIndex() {
        return timestampIndex;
    }

    /**
     * Sets the value of the flag indicating whether the timestamp field (if present)
     * must be included in the payload of the events or not.
     *
     * @param includeTS     <tt>true</tt> for including the timestamp on the event's payload,
     *                      <tt>false</tt> otherwise
     */
    private void setIncludeTS(boolean includeTS) {
        this.includeTS = includeTS;
    }

    /**
     * Indicates whether the timestamp field (if present) is included
     * in the payload of the events or not.
     *
     * @return  <tt>true</tt> when the timestamp is included on the event's payload,
     *          <tt>false</tt> otherwise
     */
    public boolean isIncludingTS() {
        return includeTS;
    }

    /**
     * Sets the path of the external file.
     *
     * @param filePath  the path for the external file
     */
    private void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     *
     * @return  the path of the data file
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Sets the sequence of characters used to separate the
     * fields of the records in the data file.
     *
     * @param delimiter     the sequence of characters
     */
    private void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     *
     * @return  sequence of characters used to separate the fields
     *          of the records in the data file
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Sets the value of the flag that indicates whether
     * the external file contains timestamps or not.
     *
     * @param containsTimestamps    <tt>true</tt> if the data file
     *                              contains timestamps,
     *                              <tt>false</tt> otherwise
     */
    private void setContainsTimestamps(boolean containsTimestamps) {
        this.containsTimestamps = containsTimestamps;
    }

    /**
     *
     * @return  <tt>true</tt> if the data file contains timestamps,
     *          <tt>false</tt> otherwise
     */
    public boolean containsTimestamps() {
        return containsTimestamps;
    }

    /**
     * Sets the value of the flag that indicates whether
     * the timestamps in the data file will be used
     * for load submission or not.
     *
     * @param useTimestamps <tt>true</tt> if the timestamps in the data file
     *                      will be used for load submission,
     *                      <tt>false</tt> otherwise
     */
    private void setUseTimestamps(boolean useTimestamps) {
        this.useTimestamps = useTimestamps;
    }

    /**
     *
     * @return  <tt>true</tt> if the timestamps in the data file should be used
     *          for load submission, <tt>false</tt> otherwise
     */
    public boolean isUsingTimestamps() {
        return useTimestamps;
    }

    /**
     * Sets the value of the flag that indicates whether
     * the external file contains event types or not.
     *
     * @param containsEventTypes    <tt>true</tt> if the file is typed,
     *                              <tt>false</tt> otherwise
     */
    private void setContainsEventTypes(boolean containsEventTypes) {
        this.containsEventTypes = containsEventTypes;
    }

    /**
     *
     * @return  <tt>true</tt> if the data file contains event types,
     *          <tt>false</tt> otherwise
     */
    public boolean containsEventTypes() {
        return containsEventTypes;
    }

    /**
     * Sets the index of the field containing the type
     * of the records in the data file.
     *
     * @param typeIndex     either FIRST_FIELD, SECOND_FIELD,
     *                             LAST_FIELD or SECOND_LAST_FIELD
     */
    private void setTypeIndex(int typeIndex) {
        if (typeIndex == FIRST_FIELD
                || typeIndex == SECOND_FIELD
                || typeIndex == LAST_FIELD
                || typeIndex == SECOND_LAST_FIELD) {
            this.typeIndex = typeIndex;
        } else {
            this.typeIndex = -1;
        }
    }

    /**
     *
     * @return  the index of the field containing the type of the records in the data file
     */
    public int getTypeIndex() {
        return typeIndex;
    }

    /**
     * Sets the type of the events in the data file, if it is not typed.
     *
     * @param singleEventTypeName   the name of the type for all events
     *                              in the data file
     */
    private void setSingleEventTypeName(String singleEventTypeName) {
        this.singleEventTypeName = singleEventTypeName;
    }

    /**
     *
     * @return  type of the events in the data file, if it is not typed.
     */
    public String getSingleEventTypeName() {
        return singleEventTypeName;
    }

    /**
     * Sets the event submission rate
     * (when not using the timestamps in the data file).
     *
     * @param eventSubmissionRate   the input rate
     */
    private void setEventSubmissionRate(double eventSubmissionRate) {
        if (eventSubmissionRate > 0) {
            this.eventSubmissionRate = eventSubmissionRate;
        } else {
            throw new InvalidParameterException("Invalid event submission rate.");
        }
    }

    /**
     *
     * @return  injection rate, when not using the timestamps in the data file
     */
    public double getEventSubmissionRate() {
        return eventSubmissionRate;
    }

    /**
     * Sets the number of times the external file will be submitted.
     *
     * @param loopCount     number of repetitions
     */
    private void setLoopCount(int loopCount) {
        if (loopCount > 0) {
            this.loopCount = loopCount;
        } else {
            System.err.println("WARNING: Invalid loop count (" + loopCount
                             + "). Setting to default: 1.");
            this.loopCount = 1;
        }
    }

    /**
     *
     * @return  the number of times the data file will be submitted
     */
    public int getLoopCount() {
        return loopCount;
    }
}
