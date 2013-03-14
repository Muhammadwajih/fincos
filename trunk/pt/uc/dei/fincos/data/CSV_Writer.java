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


package pt.uc.dei.fincos.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.Globals;

/**
 *
 * Class used to write CSV files.
 *
 * @author  Marcelo R.N. Mendes
 *
 */
public class CSV_Writer {

    /** Data writer. */
	private BufferedWriter writer;

	/** The interval, in milliseconds, at which data is flushed to disk. */
	private final int flushInterval;

	/** Keeps track of disk flushes. */
    private long lastFlushTimestamp = 0;


    /**
     *
     * @param path              path for the CSV file
     * @param flushInterval     the interval, in milliseconds, at which data must
     *                          be flushed to disk
     * @throws IOException      if the file cannot be open
     */
	public CSV_Writer(String path, int flushInterval) throws IOException {
	    if (flushInterval < 0) {
            flushInterval = 10; //default value: 10ms
        }
        this.flushInterval = flushInterval;

		File f = new File(path);

		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}

		writer = new BufferedWriter(new FileWriter(path));
	}


	/**
	 * Writes an event along with its associated timestamp into a CSV file.
	 *
	 * @param e                an event, already in CSV representation
	 * @param timestamp        the timestamp of the event
	 * @throws IOException     if an error occurs while writing to the CSV file
	 */
	public final synchronized void writeRecord(String e, long timestamp)
    throws IOException {
		writer.write(timestamp + Globals.CSV_DELIMITER + e);
		writer.newLine();

		if (System.currentTimeMillis() - lastFlushTimestamp >= flushInterval) {
			lastFlushTimestamp = System.currentTimeMillis();
			writer.flush();
		}
	}

	/**
	 * Writes an event into a CSV file.
	 *
	 * @param e                an event, already in CSV representation
	 * @throws IOException     if an error occurs while writing to the CSV file
	 */
	public final synchronized void writeRecord(String e) throws IOException {
		writer.write(e);
		writer.newLine();

		if (System.currentTimeMillis() - lastFlushTimestamp >= flushInterval) {
			lastFlushTimestamp = System.currentTimeMillis();
			writer.flush();
		}
	}

	/**
	 * Writes an event along with an associated timestamp into the CSV file.
	 *
	 * @param e                the event, in the FINCoS representation format
	 * @param timestamp        a timestamp, associated with the event
	 *
	 * @throws IOException     if an error occurs while writing to the CSV file
	 */
	public final synchronized void writeRecord(Event e, long timestamp)
	throws IOException {
		if (timestamp != 0) {
			writeRecord(e.toCSV(), timestamp);
		} else {
			writeRecord(e.toCSV());
		}
	}

	/**
	 * Writes an event into a CSV file.
	 *
	 * @param e                An event, in an internal representation format
	 * @throws IOException     if an error occurs while writing to the CSV file
	 */
	public final synchronized void writeRecord(Event e) throws IOException {
		writeRecord(e, 0);
	}

	/**
     * Flushes any entries to disk and closes the file.
     *
     * @throws IOException  if an error occurs while closing/writing to the CSV file
     */
	public final void closeFile() throws IOException {
		writer.flush();
		writer.close();
	}
}
