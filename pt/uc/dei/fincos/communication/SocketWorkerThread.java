package pt.uc.dei.fincos.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 
 * Abstract class that encapsulates a thread to listen client
 * connections to a server socket.
 * The processing of incoming messages is an abstract function
 * 
 * @author Marcelo R.N. Mendes
 */
public abstract class SocketWorkerThread extends Thread{
	protected ServerSocket serverSocket;
	protected Socket clientSocket;
	protected BufferedReader input;
	protected boolean keepListening=true;
	
	public abstract void processIncomingMessage(Object o) throws Exception;
	
	public SocketWorkerThread(String threadID, ServerSocket ss) {
		super(threadID);		
		this.serverSocket = ss;
	}
	
	public SocketWorkerThread(String threadID, ThreadGroup workersGroup, ServerSocket ss) {
		super(workersGroup, threadID);		
		this.serverSocket = ss;
	}
	
	@Override
	public void run() {
		// Setup connection
		listenToConnections();
		
		String incomingEvent;
		while (keepListening) {				
			try {
				if(input != null) {								
					incomingEvent = (String) input.readLine();
					if(incomingEvent != null) {						
						this.processIncomingMessage(incomingEvent);		
					}		
					else
						break;
				}
				else
					break;
			} catch (IOException ioe) {
				System.out.println("Worker thread " + this.getName() + ". Cannot read from socket (" + ioe.getMessage() + "). Restarting socket...");							
				this.listenToConnections();
			} catch (Exception exc) {					
				System.err.println("Worker thread " + this.getName() + "." + exc.getClass() + "-" + exc.getMessage());
				exc.printStackTrace();
			}						
		}
		
		disconnect();	
		System.out.println("Worker thread terminated.");
	}
	
	/**
	 * Makes the worker thread to halt
	 */
	public void close() {
		System.out.println("Closing worker thread...");
		this.keepListening = false;
	}
	
	protected void listenToConnections() {		
		if(this.keepListening) {
			disconnect();
			try {	
			//	System.out.println("Worker thread " + this.getName() + " ready. Waiting for client connections...");
				this.clientSocket = serverSocket.accept();				
				this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));				
				System.out.println("Worker thread " + this.getName() + " accepted client connection.");				
			} catch (IOException ioe) {
				System.err.println("Could not accept client connections. (" + ioe.getMessage() + ")");				
			}
		}
			
	}
	
	protected void disconnect() {				
		try {
			if(this.input != null) {
				this.input.close();
				this.input = null;
			}			
			if(this.clientSocket != null && !this.clientSocket.isClosed()) {
				this.clientSocket.close();
				this.clientSocket = null;
			}
				
		} catch (IOException ioe) {
			System.err.println("Could not close connection(" + ioe.getMessage() + ")");
		}
	}	

}
