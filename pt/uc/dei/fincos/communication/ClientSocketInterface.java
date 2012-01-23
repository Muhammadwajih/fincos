package pt.uc.dei.fincos.communication;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import pt.uc.dei.fincos.basic.Globals;



/**
 * Class used to encapsulate client-side socket communication between 
 * event-sending applications (Drivers, Sinks, and output listeners at Adapter) and 
 * event-receiving applications (Adapter and PerfMon)
 * 
 * 
 * @author Marcelo R.N. Mendes
 *
 */
public class ClientSocketInterface {
	private Socket s;	
	private PrintWriter output;
	private int  eventSentCount=0;	
	private int socketBufferSize; // In number of events
	
	private String destinationAddress;
	private int destinationPort;
	
	/**
	 * 
	 * @param destinationAddress		The address where the receiving application is running
	 * @param destinationPort			The port at which the receiving application is listening
	 * @param socketBufferSize			The number of events to be buffered before flushing socket	 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public ClientSocketInterface(InetAddress destinationAddress, int destinationPort, int socketBufferSize) 
	throws UnknownHostException, IOException {
		this(destinationAddress.getHostAddress(), destinationPort, socketBufferSize);
	}	
	
	/**
	 * 
	 * @param destinationAddress		The address where the receiving application is running
	 * @param destinationPort			The port at which the receiving application is listening
	 * @param socketBufferSize			The number of events to be buffered before flushing socket
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public ClientSocketInterface(String destinationAddress, int destinationPort, int socketBufferSize) 
	throws UnknownHostException, IOException {
		this.destinationAddress = destinationAddress;
		this.destinationPort = destinationPort;
		// Socket communication
		s = new Socket(destinationAddress, destinationPort);	
		s.setTcpNoDelay(true);
		s.setSoLinger(true, 5);
		output = new PrintWriter(s.getOutputStream());
		this.setSocketBufferSize(socketBufferSize);		
	}		
	

	/**
	 * Sends an event in String CSV format to another application. 
	 * (It is a blocking operation)
	 *  
	 * @param event			The event to be sent
	 * @param timestamp		The timestamp to be associated to the event (in milliseconds)
	 * @throws IOException 
	 * 
	 */	
	public void sendTimestampedCSVEvent(String event, long timestamp) 
	throws IOException {					
		output.print(event+Globals.CSV_SEPARATOR+timestamp+"\n");		
		
		boolean mustFlush;
		
		synchronized (this) {
			this.eventSentCount++;
			mustFlush = this.eventSentCount%getSocketBufferSize() == 0;
		}

		// Periodically Flushes Socket buffer
		if(mustFlush)
			output.flush(); // Forces a flush every "DEFAULT_SOCKET_BUFFER_SIZE" events						
	}
	
	/**
	 * Sends an event in String CSV format to another application.
	 * No timestamp is sent.
	 * (It is a blocking operation)
	 *  
	 * @param event			The event to be sent
	 * @throws IOException 
	 * 
	 */	
	public void sendCSVEvent(String event) 
	throws IOException {				
		output.print(event+"\n");
		boolean mustFlush;
				
		synchronized (this) {
			this.eventSentCount++;
			mustFlush = this.eventSentCount%getSocketBufferSize() == 0;
		}		

		// Periodically Flushes Socket buffer
		if(mustFlush)
			output.flush(); // Forces a flush every "DEFAULT_SOCKET_BUFFER_SIZE" events	
	}
	
	/**
	 * Closes the connection with the destination and the log file
	 * 
	 * @throws IOException
	 */
	public synchronized void disconnect() throws IOException  {		
		if(output != null) {
			output.flush();
			output.close();
			output = null;
		}
		
		if(s != null) {
			s.close();
			s = null;
		}
	}
	
	public synchronized boolean isConnected () {
		return s!=null && s.isConnected() && !s.isClosed() && output != null;
	}

	public void setSocketBufferSize(int socketBufferSize) {
		if(socketBufferSize > 0)
			this.socketBufferSize = socketBufferSize;
		else {
			System.err.println("Invalid buffer size: " + socketBufferSize + ". Setting to default (1).");
			this.socketBufferSize = 1; // No buffering
		}
			
	}

	public int getSocketBufferSize() {
		return socketBufferSize;
	}

	public void setDestinationAddress(String destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public String getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationPort(int destinationPort) {
		this.destinationPort = destinationPort;
	}

	public int getDestinationPort() {
		return destinationPort;
	}
}