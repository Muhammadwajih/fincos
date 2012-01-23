package pt.uc.dei.fincos.adapters;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.communication.ClientSocketInterface;
import pt.uc.dei.fincos.sink.Sink;



/**
 *  
 * Thread for listening incoming events from an output stream at a CEP Engine and
 * forwarding them to a given Sink. 
 * 
 * @author Marcelo R.N. Mendes
 *
 */
public abstract class OutputListener extends Thread{
	protected String listenerID; // for logging purposes	
	protected int logFlushInterval=10;
	protected int socketBufferSize;
	protected int rtMeasurementMode;
	protected int communicationMode;
	
	protected ArrayList<InetSocketAddress> sinksList; // Communication CEP-Engine -> Sink is through FINCoS Adapter
	private ArrayList<ClientSocketInterface> sinkInterfaces;
	
	protected Sink sinkInstance; // Direct communication CEP-Engine -> Sink	
	
	protected boolean keepListening = true;		
	
	public void connectWithAllSinks() {				
		disconnectFromAllSinks();
				
		this.sinkInterfaces = new ArrayList<ClientSocketInterface>();		
		for (InetSocketAddress sinkAddressPort: sinksList) {
			try {
				sinkInterfaces.add(connectWithSink(sinkAddressPort.getAddress().getHostAddress(), 
						sinkAddressPort.getPort()));	
			} catch (IOException ioe) {
				System.err.println("Could not connect to Sink at " + 
						sinkAddressPort.getAddress()+ ":" + sinkAddressPort.getPort()+
						"(" + ioe.getMessage() + ").");
			}			
		}				
	}
	
	private ClientSocketInterface connectWithSink(String address, int port) throws IOException{				
		return new ClientSocketInterface(address, port, socketBufferSize);
	}
	
	public void disconnectFromAllSinks() {		
		if(this.sinkInterfaces != null) {
			System.out.println("disconnecting from sink(s)...");
			for (ClientSocketInterface sinkInterface : sinkInterfaces) {
				try {
					sinkInterface.disconnect();
				} catch (IOException e) {
					System.err.println("Error while closing connection with Sink (" + e.getMessage() + ")");
				}
				finally {
					sinkInterface = null;
				}
			}
				
			sinkInterfaces.clear();
			sinkInterfaces = null;						
		}
	}

	public void forwardToSink(Object[] e) {
		if(this.communicationMode == Globals.DIRECT_API_COMMUNICATION) {
			this.sinkInstance.processOutputEvent(e);
		}
	}
	
	public void forwardToSinks(String e) {			
		if(this.communicationMode == Globals.ADAPTER_CSV_COMMUNICATION) {
			// If it is the first event, open connection to Sink(s)
			if(this.sinkInterfaces == null || sinkInterfaces.isEmpty()) {				
				connectWithAllSinks();
			}

			// Send event to all Sinks that subscribe to this stream
			for (ClientSocketInterface sinkInterface : sinkInterfaces) {
				try {			
					sinkInterface.sendCSVEvent(e);
				} catch (IOException ioe) {
					System.err.println("Error while forwarding event to Sink. ("+ ioe.getMessage() + "). Trying retransmission...");			
					try {
						sinkInterface.disconnect();
						sinkInterface = connectWithSink(sinkInterface.getDestinationAddress(), 
														sinkInterface.getDestinationPort());
						forwardToSinks(e);
					} catch (IOException ioe1) {
						System.err.println("Could not reconnect to Sink (it seems to be offline). Message will be discarded.");					
					}		
				}				
			}					
		}
		else if(this.communicationMode == Globals.DIRECT_API_COMMUNICATION) {
			this.sinkInstance.processOutputEvent(e);
		}

	}

	/**
	 * Performs any vendor-specific initialization on the listener
	 */
	public abstract void load() throws Exception;

	/**
	 * Disconnects from CEP Engine and from Sink
	 */
	protected abstract void disconnect();

}
