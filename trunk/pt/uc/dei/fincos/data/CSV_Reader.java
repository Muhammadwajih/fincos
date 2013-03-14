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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import pt.uc.dei.fincos.basic.Globals;


/**
 * Class used to read CSV files.
 *
 * @author  Marcelo R.N. Mendes
 *
 */
public final class CSV_Reader {
    /** Path for a CSV file to be read. */
    private String filePath;

    /** reads the CSV file. */
    private BufferedReader reader;

    /** character used to separate fields of the CSV records stored in the file. */
    private String delimiter;


    /**
     *
     * @param path                     path for a CSV file to be read.
     * @throws FileNotFoundException   if an attempt to open the file denoted by the specified
     *                                 path has failed
     */
    public CSV_Reader(String path) throws FileNotFoundException {
        this(path, Globals.CSV_DELIMITER);
    }

    /**
     *
     * @param path                     path for a CSV file to be read.
     * @param delimiter                character used to separate fields of the CSV
     *                                 records stored in the file
     * @throws FileNotFoundException   if an attempt to open the file denoted by the
     *                                 specified path has failed
     */
    public CSV_Reader(String path, String delimiter) throws FileNotFoundException {
        this.filePath = path;
        reader = new BufferedReader(new FileReader(path));
        this.delimiter = delimiter;
    }

    /**
     * Reads a line of text fromt the CSV file, and parses it
     * into a record, represented as an array of strings.
     *
     * @return                 An array of strings, each representing an attribute of the record.
     * @throws IOException     If an I/O error occurs
     */
    public String[] getNextRecord() throws IOException {
        String [] cols = null;
        String line = reader.readLine();

        if (line != null) {
            cols = split(line, this.delimiter);
        }

        return cols;
    }

    /**
     * Reads a line of text fromt the CSV file.  A line is considered to be terminated by any one
     * of a line feed ('\n'), a carriage return ('\r'), or a carriage return
     * followed immediately by a linefeed.
     *
     * @return     A String containing the contents of the line, not including
     *             any line-termination characters, or null if the end of the
     *             stream has been reached
     *
     * @exception  IOException  If an I/O error occurs
     */
    public String getNextLine() throws IOException {
        return reader.readLine();
    }

    /**
     * More efficient implementation of split method for string.
     *
     * @param line          The record to be split
     * @param separator     The character used to split record's fields
     * @return              The array of strings computed by splitting this string
     *                      around the separator character
     */
    public static String[] split(String line, String separator) {
        if (line == null) {
            return null;
        }
        String[] ret = null;
        ArrayList<String> cols = new ArrayList<String>();
        int commaIndex;
        while (true) {
            commaIndex = line.lastIndexOf(separator);
            if (commaIndex != -1) {
                cols.add(line.substring(commaIndex + 1));
                line = line.substring(0, commaIndex);
            } else {
                cols.add(line);
                break;
            }
        }
        ret = new String[cols.size()];

        int i = ret.length - 1;
        for (String c : cols) {
            ret[i] = c;
            i--;
        }
        return ret;
    }


    /**
     * Closes the CSV file and releases any system resources associated with it.
     *
     * @throws IOException     if an I/O error occurs
     */
    public void closeFile() throws IOException {
        reader.close();
    }

    /**
     * Closes the CSV file and reopens it.
     *
     * @throws IOException     if an I/O error occurs
     */
    public void reopen() throws IOException {
        this.closeFile();
        reader = new BufferedReader(new FileReader(this.filePath));
    }
}
