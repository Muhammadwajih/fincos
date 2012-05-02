package pt.uc.dei.fincos.sink;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;

import pt.uc.dei.fincos.adapters.cep.CEPEngineFactory;
import pt.uc.dei.fincos.adapters.cep.CEPEngineInterface;
import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.basic.InvalidStateException;
import pt.uc.dei.fincos.basic.Status;
import pt.uc.dei.fincos.basic.Step;
import pt.uc.dei.fincos.communication.ClientSocketInterface;
import pt.uc.dei.fincos.communication.SocketWorkerThread;
import pt.uc.dei.fincos.controller.Logger;
import pt.uc.dei.fincos.controller.SinkConfig;
import pt.uc.dei.fincos.data.CSVReader;


public class Sink extends JFrame implements SinkRemoteFunctions{
	private static final long serialVersionUID = 5882702336468705470L;
	
	//========================= GUI ===================================
	JLabel statusLabel, eventCountLabel; 	
	JPanel statusPanel;
	JTextArea infoArea;	
	//=================================================================
	
	private ServerSocket incomingEventsSocket; //Used to receive events from FINCoS Adapter
	private CEPEngineInterface cepEngineInterface; //Used to receive events directly from CEP engine
	private AdapterListener workerThreads[];
	private Logger logger;
	
	private String alias;	
	private int rtMeasurementMode;
	
	private int logSamplMod, validSamplMod, fieldsToLog;
	
	private ClientSocketInterface validator;
	
	private Status status;	
	
	private long receivedEvents = 0;			
	
	public Sink() {
		this("Sink");
	}
	
	public Sink(String alias) {
		super(alias);		
		
		this.setIconImage(Toolkit.getDefaultToolkit().getImage("imgs/sink.png"));
		
		this.alias = alias;
		this.status = new Status();
			
		statusPanel =  new JPanel();
		statusPanel.setLayout(null);
        statusPanel.setPreferredSize(new Dimension(200,400));
        statusPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Status"));
        statusLabel = new JLabel();                       
        eventCountLabel = new JLabel("Rcvd events: 0");
        
		statusPanel.add(statusLabel);		
		statusPanel.add(eventCountLabel);
		
		statusLabel.setBounds(10, 10, 150, 50);
		eventCountLabel.setBounds(20, 45, 175, 50);
        
		infoArea = new JTextArea(10, 30);   
        infoArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Info"));
        JScrollPane infoScroll = new JScrollPane(infoArea);
        infoArea.setEditable(false);        
        
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(statusPanel, BorderLayout.LINE_START);
		this.getContentPane().add(infoScroll, BorderLayout.CENTER);
		
		this.setSize(700, 400);        
		this.setLocationRelativeTo(null); //screen center
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setVisible(true);
		
		refreshGUI();						
		
		int delay = 1000/Globals.DEFAULT_GUI_REFRESH_RATE;
		Timer guiRefresher = new Timer(delay, new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {				
				refreshGUI();					
			}			
		});
		guiRefresher.start();
		
		try {
			System.out.println("Sink application started. Initializing remote interface...");
			showInfo("Sink application started. Initializing remote interface...");
			this.initializeRMI();
			System.out.println("Done! Waiting for remote commands...");
			showInfo("Done! Waiting for remote commands...");
		} catch (Exception e) {
			this.status.setStep(Step.ERROR);			
			System.err.println("ERROR: Could not initialize remote interface: " + e.getMessage());
			showInfo("ERROR: Could not initialize remote interface: " + e.getMessage());
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
						
			guiRefresher.stop();
			guiRefresher = null;
			this.dispose();
			
		} 
		
	}
	
	private void initializeRMI() throws RemoteException {
		SinkRemoteFunctions stub = (SinkRemoteFunctions) UnicastRemoteObject.exportObject(this, 0);
		Registry registry = LocateRegistry.getRegistry(Globals.DEFAULT_RMI_PORT);
		registry.rebind(alias, stub);	
	}

	public void refreshGUI() {
		switch (this.status.getStep()) {
		case DISCONNECTED:
			this.statusLabel.setIcon(Globals.YELLOW_SIGN);
			break;
		case CONNECTED:
		case READY:
		case PAUSED:
		case STOPPED:		
			this.statusLabel.setIcon(Globals.BLUE_SIGN);
			break;		
		case LOADING:
		case RUNNING: 
			this.statusLabel.setIcon(Globals.GREEN_SIGN);
			break;
		case ERROR:
			this.statusLabel.setIcon(Globals.RED_SIGN);
			break;
		}		
						
		eventCountLabel.setText("Rcvd events: " + 
				Globals.LONG_FORMAT.format(receivedEvents));		

		if(this.status.getStep() == Step.READY && receivedEvents > 0)
			this.status.setStep(Step.RUNNING);
		
		this.statusLabel.setText(this.status.getStep().toString());		
		
		statusPanel.revalidate();
	}
	

	@Override
	public boolean load(SinkConfig sinkCfg, 
			int communicationMode,	int rtMeasurementMode,
			int socketBufferSize, int logFlushInterval,
			Properties adapterProperties) 
	throws RemoteException, InvalidStateException, Exception {						
		if(this.status.getStep() == Step.DISCONNECTED ||
		   this.status.getStep() == Step.ERROR ||
		   this.status.getStep() == Step.STOPPED) {
			if(communicationMode == Globals.ADAPTER_CSV_COMMUNICATION) {
				// Socket communication
				try {
					unload();
					this.status.setStep(Step.CONNECTED);
					incomingEventsSocket = new ServerSocket(sinkCfg.getPort());				
					String outputStreams[] = sinkCfg.getOutputStreamList();
					workerThreads = new AdapterListener[outputStreams.length];		
					for (int i = 0; i < workerThreads.length; i++) {
						workerThreads[i] = new AdapterListener("Listener for " + outputStreams[i], incomingEventsSocket);
						workerThreads[i].start();	
					}	
				} catch (IOException ioe) {	
					showInfo("Could not open server socket (" + ioe.getMessage() + ").");
					System.err.println("Could not open server socket (" + ioe.getMessage() + ").");
					this.status.setStep(Step.ERROR);
					return false;
				}
			}
			else if (communicationMode == Globals.DIRECT_API_COMMUNICATION) {
				cepEngineInterface = CEPEngineFactory.getCEPEngineInterface(adapterProperties);		
				if(cepEngineInterface == null)		
					throw new Exception("Unsupported CEP engine");		
				cepEngineInterface.setRtMeasurementMode(rtMeasurementMode);
				cepEngineInterface.setSocketBufferSize(socketBufferSize);
				showInfo("Connecting to CEP engine...");
				cepEngineInterface.connect();				
				cepEngineInterface.load(sinkCfg.getOutputStreamList(), this);
			}
			
			this.rtMeasurementMode = rtMeasurementMode;
					
			// Logging
			if(sinkCfg.isLoggingEnabled()) {											
				String rtResolution;
				switch (rtMeasurementMode) {
				case Globals.ADAPTER_RT_NANOS:
					rtResolution = "ADAPTER_RT_NANOS";
					break;
				case Globals.END_TO_END_RT_MILLIS:
					rtResolution = "END_TO_END_RT_MILLIS";
					break;
				default:
					rtResolution = "NO_RT";				
				}
				
				try {
					String logHeader = "FINCoS Sink Log File."+
					 "\n Sink Alias: " + sinkCfg.getAlias()+
					 "\n Sink Address: " + sinkCfg.getAddress().getHostAddress()+
					 "\n Server Address: " + sinkCfg.getServerAddress().getHostAddress()+
					 "\n Start time: " + new Date()+
					 "\n Response Time Resolution: " + rtResolution;
					logger = new Logger(Globals.APP_PATH+"log"+File.separator+sinkCfg.getAlias()+".log", 
										logHeader, logFlushInterval, sinkCfg.getLoggingSamplingRate(),
										sinkCfg.getFieldsToLog());	
					logSamplMod = (int) (1/sinkCfg.getLoggingSamplingRate());
					fieldsToLog = sinkCfg.getFieldsToLog();
				} catch (IOException ioe2) {
					System.err.println("Could not open logger (" + ioe2.getMessage() + ").");
					showInfo("Could not open logger (" + ioe2.getMessage() + ").");
					this.status.setStep(Step.ERROR);
					return false;
				}
			}
			
			// Validation
			if(sinkCfg.isValidationEnabled()) {
				this.validSamplMod = (int) (1/sinkCfg.getValidationSamplingRate());				
				
				// Tries to connect to validator
				if(validator != null) {
					try {
						validator.disconnect();
						validator = null;
					} catch (IOException ioe3) {					
						System.err.println("Could not disconnect from validator. (" +ioe3.getMessage() +")");
						showInfo("Could not disconnect from validator. (" +ioe3.getMessage() +")");
						this.status.setStep(Step.ERROR);					
					}
				}
				try {				
					showInfo("Trying to establish connection to validator at " + sinkCfg.getValidatorAddress().getHostAddress() + ":" + sinkCfg.getValidatorPort() + "...");					
					this.validator = // * communication with FINCoS Perfmon has no buffering
						new ClientSocketInterface(sinkCfg.getValidatorAddress(), sinkCfg.getValidatorPort(),1);					
					showInfo("Done!");
				} catch (UnknownHostException e) {					
					showInfo("ERROR: Cannot connect to specified validator: Unknown host.");
					this.status.setStep(Step.ERROR);
					return false;
				} catch (IOException e) {					
					showInfo("ERROR: Cannot connect to specified validator. (" + e.getMessage() + ").");
					this.status.setStep(Step.ERROR);
					return false;
				}
			}								
			
			this.status.setStep(Step.READY);
			showInfo(alias+" has been successfully loaded.");
			return true;
		}
		else {
			throw new InvalidStateException("Could not load sink. Sink has already been loaded.");
		}			
	}
	

	public void unload() {
		if(workerThreads != null) {
			for (AdapterListener worker : workerThreads) {
				worker.close();
				worker = null;
			}
			workerThreads = null;	
		}
		
		if(this.incomingEventsSocket != null) {
			try {
				this.incomingEventsSocket.close();
			} catch (IOException e) {
				System.err.println("Exception while closing server socket (" + e.getMessage() + ").");
			}
			this.incomingEventsSocket = null;
		}
		
		if(this.cepEngineInterface != null) {
			cepEngineInterface.disconnect();
			this.cepEngineInterface = null;
		}
		
		if(this.logger != null) {
			this.logger.close();
			this.logger = null;				
		}	
		
		if(this.validator != null) {
			try {
				validator.disconnect();
				validator = null;
			} catch (IOException e) {
				System.err.println("Exception while disconnecting from Validator.");
			}
		}
				
		this.status.setStep(Step.STOPPED);
		showInfo("Sink has been stopped.");
		receivedEvents = 0;
	}

	
	public void showInfo(String msg) {
		Date now = new Date();		
		infoArea.append(Globals.TIME_FORMAT.format(now) + " - " + msg + "\n" );
		infoArea.setCaretPosition(infoArea.getDocument().getLength());
	}
	
	private String toCSV(Object event[]) {
		StringBuilder sb = new StringBuilder("type:");		
		sb.append(event[0]);
		
		for (int i = 1; i < event.length; i++) {
			sb.append(Globals.CSV_SEPARATOR);
			sb.append(event[i]);	
		}
		
		return sb.toString();
	}
	
	public void processOutputEvent(Object event[]) {
		long timestampMillis = System.currentTimeMillis();
		long receivedCount=0;
		synchronized (this) {
			receivedEvents++;
			receivedCount = receivedEvents;
		}
				
		
		try {			
			// Log event, if configured to
			if(logger != null && receivedCount%logSamplMod == 0) {	
				if(fieldsToLog == Globals.LOG_ALL_FIELDS) {
					switch (rtMeasurementMode) {
					// event already contains times in nanos; only adds timestamp in millis
					case Globals.ADAPTER_RT_NANOS:						
						logger.writeRecord(toCSV(event), timestampMillis);  
						break;
					// event already contains time of causer event in millis (adds timestamp in millis)
					case Globals.END_TO_END_RT_MILLIS: 
						logger.writeRecord(toCSV(event), timestampMillis);
						break;
					// event does not contain any times; only adds timestamp in millis
					case Globals.NO_RT:  
						logger.writeRecord(toCSV(event), timestampMillis);
					}				
				}					
				else if(fieldsToLog == Globals.LOG_ONLY_TIMESTAMPS) {					
					String eventType = "type:"+event[0];					
					switch (rtMeasurementMode) {
					// event already contains time of causer event in millis (adds timestamp and arrival time of event in millis)
					case Globals.END_TO_END_RT_MILLIS: 								
						Long causerEmissionTimeMillis = (Long) event[event.length-1];									
						logger.writeRecord(eventType+Globals.CSV_SEPARATOR+causerEmissionTimeMillis, 
										   timestampMillis);						
					break;
					// event does not contain any times; only adds timestamp in millis
					case Globals.NO_RT: 
						logger.writeRecord(eventType, timestampMillis);
					}														
				}									
			}			
			
			// Send the event to validator
			if(validator != null) {
				if(receivedCount%validSamplMod == 0) {
					if(rtMeasurementMode == Globals.ADAPTER_RT_NANOS)
						System.err.println("incompatibility");
					else if(rtMeasurementMode == Globals.END_TO_END_RT_MILLIS ||
							rtMeasurementMode == Globals.NO_RT)
						validator.sendTimestampedCSVEvent(toCSV(event), timestampMillis);
				}					
			}
		} catch (IOException ioe) {
			System.err.println("Error while processing event \"" + event + 
					"\" (" + ioe.getMessage() +").");
		}		
	}
	
	
	
	/**
	 * Processes an event received from the CEP engine (or FINCoS Adapter)
	 * 
	 * @param event
	 * @throws Exception
	 */
	public void processOutputEvent(String event) {		
		long timestampMillis = System.currentTimeMillis();
		long receivedCount=0;
		synchronized (this) {
			receivedEvents++;
			receivedCount = receivedEvents;
		}
				
		
		try {
			// Log event, if configured to
			if(logger != null && receivedCount%logSamplMod == 0) {	
				if(fieldsToLog == Globals.LOG_ALL_FIELDS) {
					switch (rtMeasurementMode) {
					// event already contains times in nanos; only adds timestamp in millis
					case Globals.ADAPTER_RT_NANOS:						
						logger.writeRecord(event, timestampMillis);  
						break;
					// event already contains time of causer event in millis (adds timestamp in millis)
					case Globals.END_TO_END_RT_MILLIS: 
						logger.writeRecord(event, timestampMillis);
						break;
					// event does not contain any times; only adds timestamp in millis
					case Globals.NO_RT:  
						logger.writeRecord(event, timestampMillis);
					}				
				}					
				else if(fieldsToLog == Globals.LOG_ONLY_TIMESTAMPS) {
					String eventSplit[] = CSVReader.split(event, Globals.CSV_SEPARATOR);
					String eventType = eventSplit[0];
					switch (rtMeasurementMode) {
					// event already contains times in nanos; only adds timestamp in millis
					case Globals.ADAPTER_RT_NANOS:												
						String causerEmissionTimeNano = eventSplit[eventSplit.length-2];
						String arrivalTimeNanos = eventSplit[eventSplit.length-1];						
						logger.writeRecord( eventType+Globals.CSV_SEPARATOR+
											causerEmissionTimeNano+Globals.CSV_SEPARATOR+
											arrivalTimeNanos, timestampMillis);  
						break;
					// event already contains time of causer event in millis (adds timestamp and arrival time of event in millis)
					case Globals.END_TO_END_RT_MILLIS: 						
						String causerEmissionTimeMillis = eventSplit[eventSplit.length-1];									
						logger.writeRecord( eventType+Globals.CSV_SEPARATOR+
											causerEmissionTimeMillis,
											timestampMillis);						
						break;
					// event does not contain any times; only adds timestamp in millis
					case Globals.NO_RT: 
						logger.writeRecord(eventType, timestampMillis);
					}														
				}									
			}			
			
			// Send the event to validator
			if(validator != null) {
				if(receivedCount%validSamplMod == 0) {
					if(rtMeasurementMode == Globals.ADAPTER_RT_NANOS)
						validator.sendCSVEvent(event);
					else if(rtMeasurementMode == Globals.END_TO_END_RT_MILLIS ||
							rtMeasurementMode == Globals.NO_RT)
						validator.sendTimestampedCSVEvent(event, timestampMillis);
				}					
			}
		} catch (IOException ioe) {
			System.err.println("Error while processing event \"" + event + 
					"\" (" + ioe.getMessage() +").");
		}		
	}
	
	class AdapterListener extends SocketWorkerThread{
		
		public AdapterListener(String threadID, ServerSocket ss) {
			super(threadID, ss);
		}

		@Override
		public void processIncomingMessage(Object o) throws Exception {
			if(o instanceof String)
				processOutputEvent((String)o);
			else if (o instanceof Event) {
				
			}					
		}		
	}
	

	@Override
	public Status getStatus() throws RemoteException {
		return this.status;
	}

}
