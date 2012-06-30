package pt.uc.dei.fincos.basic;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Set;

import javax.swing.ImageIcon;

/**
 * Static constants and methods used in different places of the framework.
 *
 * @author Marcelo R.N. Mendes
 *
 */
public class Globals {
    /** Directory where the application is executed. */
    public static final String APP_PATH = System.getProperty("user.dir") + File.separator;

    /** Character used to separate fields in events represented in CSV format. */
    public static final String CSV_SEPARATOR = ",";

    /** Port used by the components of the framework to communicate through Remote Method Invocation. */
    public static final int DEFAULT_RMI_PORT = 1099;

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


    /**
     * Retrieves all IP addresses assigned to all network cards of the current machine.
     *
     * @param addressList       A pointer to a list to be filled with the IP addresses
     * @throws SocketException  if an I/O error occurs
     */
    public static void retrieveMyIPAddresses(Set<InetAddress> addressList) throws SocketException {
        Enumeration<NetworkInterface> nics = java.net.NetworkInterface.getNetworkInterfaces();
        NetworkInterface nic;
        Enumeration<InetAddress> nicAddresses;

        // Iterates over NICs list
        while (nics.hasMoreElements()) {
            nic = nics.nextElement();
            if (nic.isLoopback()) {
                continue;
            }
            nicAddresses = nic.getInetAddresses();
            // Iterates over addresses of a NIC
            while (nicAddresses.hasMoreElements()) {
                addressList.add(nicAddresses.nextElement());
            }
        }
    }
}
