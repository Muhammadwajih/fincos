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


package pt.uc.dei.fincos.basic;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import javax.swing.ImageIcon;

/**
 * Static constants and methods used in different places of the framework.
 *
 * @author  Marcelo R.N. Mendes
 *
 */
public class Globals {
    /** Directory where the application is executed. */
    public static final String APP_PATH = System.getProperty("user.dir") + File.separator;

    /** Default character used to separate fields of records in a data file. */
    public static final String CSV_DELIMITER = ",";

    /** Port used by the components of the framework to communicate via RMI. */
    public static final int RMI_PORT = 1212;

    /** Response time is measured from Drivers to Sinks. */
    public static final int END_TO_END_RT = 0;

    /** Response time is measured inside the adapter. */
    public static final int ADAPTER_RT = 1;

    /** Response time is not measured. */
    public static final int NO_RT = 2;

    /** Response time measurement resolution is in milliseconds. */
    public static final int MILLIS_RT = 0;

    /** Response time measurement resolution is in milliseconds. */
    public static final int NANO_RT = 1;

    /** All fields of events are logged to disk. */
    public static final int LOG_ALL_FIELDS = 0;

    /** Only the timestamps of events are logged to disk. */
    public static final int LOG_ONLY_TIMESTAMPS = 1;

    /** Default sampling rate used when monitoring performance online. */
    public static final double PERFMON_ONLINE_SAMPLING_RATE = 0.1;

    /** Used for formatting dates, times and numbers on GUI.  */
    public static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    public static final DecimalFormat FLOAT_FORMAT_2 = new DecimalFormat("0.00");
    public static final DecimalFormat FLOAT_FORMAT_3 = new DecimalFormat("0.000");
    public static final DecimalFormat INT_FORMAT_1 = new DecimalFormat("0");
    public static final DecimalFormat INT_FORMAT_2 = new DecimalFormat("00");
    public static final DecimalFormat LONG_FORMAT = new DecimalFormat("###,###");

    /** Number of times, per second, the GUI is refreshed. */
    public static final int DEFAULT_GUI_REFRESH_RATE = 1;

    /** Icons used in several places of the GUI. **/
    public static final ImageIcon BLUE_SIGN = new ImageIcon("imgs/blue.png");
    public static final ImageIcon GREEN_SIGN = new ImageIcon("imgs/green.png");
    public static final ImageIcon RED_SIGN = new ImageIcon("imgs/red.png");
    public static final ImageIcon YELLOW_SIGN = new ImageIcon("imgs/yellow.png");
}
