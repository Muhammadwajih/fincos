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
 * Static constants and methods used in different places of the framework
 * 
 * @author Marcelo R.N. Mendes
 *
 */
public class Globals {
	public static final String APP_PATH = System.getProperty("user.dir")+ File.separator;
	public static final String CSV_SEPARATOR = ",";
	
	public static final int END_TO_END_RT_MILLIS=0; //Response time is measured from Drivers to Sinks, in milliseconds
	public static final int ADAPTER_RT_NANOS=1; //Response time is measured inside Adapter, in nanoseconds
	public static final int NO_RT=2; //Response time is not measured
	
	public static final int ADAPTER_CSV_COMMUNICATION=0; //Events are sent/received to/from CEP engine through Adapters as CSV messages.
	public static final int DIRECT_API_COMMUNICATION=1; //Events are sent/received to/from CEP engine directly using API. 
	
	public static final int LOG_ALL_FIELDS = 0;
	public static final int LOG_ONLY_TIMESTAMPS = 1;
	
	// Used in the GUI of applications for formatting times in the panel with detailed info
	public static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
	
	public static final DecimalFormat FLOAT_FORMAT_2 = new DecimalFormat("0.00");
	public static final DecimalFormat FLOAT_FORMAT_3 = new DecimalFormat("0.000");	
	public static final DecimalFormat INT_FORMAT = new DecimalFormat("0");
	public static final DecimalFormat LONG_FORMAT = new DecimalFormat("###,###");
	
	// Used for by Driver for sending events when parsing timestamps of data files expressed as date/time  
	public static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");	
	
	public static final int DEFAULT_GUI_REFRESH_RATE = 1; // per second		
	
	public static final int DEFAULT_RMI_PORT = 1099; // per second	
	
	public static final ImageIcon BLUE_SIGN = new ImageIcon("imgs/blue.png");
	public static final ImageIcon GREEN_SIGN = new ImageIcon("imgs/green.png");
	public static final ImageIcon RED_SIGN = new ImageIcon("imgs/red.png");
	public static final ImageIcon YELLOW_SIGN = new ImageIcon("imgs/yellow.png");

	
	/**
	 * Retrieves all IP addresses assigned to all network cards of the current machine
	 * 
	 * @param addressList		A pointer to a list to be filled with the IP addresses
	 * @throws SocketException
	 */
	public static void retrieveMyIPAddresses(Set<InetAddress> addressList) throws SocketException {			
		Enumeration<NetworkInterface> nics = java.net.NetworkInterface.getNetworkInterfaces();
		NetworkInterface nic;
		Enumeration<InetAddress> nicAddresses;		
		
		// Iterates over NICs list
		while (nics.hasMoreElements()) {
			nic = (NetworkInterface) nics.nextElement();
			
			if(nic.isLoopback())
				continue;
			
			nicAddresses = nic.getInetAddresses();
			// Iterates over addresses of a NIC
			while (nicAddresses.hasMoreElements()) {				
				addressList.add(nicAddresses.nextElement());
			}			
		}
				
	}
}
