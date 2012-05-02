package pt.uc.dei.fincos.adapters;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.w3c.dom.DOMException;

import pt.uc.dei.fincos.adapters.cep.CEPEngineFactory;
import pt.uc.dei.fincos.adapters.cep.CEPEngineInterface;
import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.basic.Step;
import pt.uc.dei.fincos.communication.SocketWorkerThread;
import pt.uc.dei.fincos.controller.ConfigurationParser;
import pt.uc.dei.fincos.controller.DriverConfig;
import pt.uc.dei.fincos.controller.SinkConfig;
import pt.uc.dei.fincos.driver.ExternalFileWorkloadPhase;
import pt.uc.dei.fincos.driver.SyntheticWorkloadPhase;
import pt.uc.dei.fincos.driver.WorkloadPhase;




/**
 * A graphical application that serves as an interface between the Framework and CEP engines.
 * Includes:
 * 1) Input Adapter - Receives events from drivers and forwards to CEP engine;
 * 2) Output Adapter - Receives events from CEP engine and forwards to Sinks.
 * 
 *
 * @author Marcelo R.N. Mendes
 * 
 * @see CEPEngineFactory
 * @see CEPEngineInterface
 *
 */
@SuppressWarnings("serial")
public class Adapter extends JFrame{
	private ServerSocket incomingEventsSockets[]; //Used to receive events from drivers
	private DriverListener workerThreads[];	
	
	private CEPEngineInterface cepEngine;	
	
	private ConfigurationParser config;
	
	private int rtMeasurementMode, socketBufferSize;
	
//=========================== GUI ===================================
	JTextArea infoArea;
	JTextField connPropertiesFileField, testConfigFileField;
	JButton loadBtn, connectBtn;
	JFileChooser testConfigChooser, connPropertiesChooser;
	
	JTable  inputTable, outputTable;
	JButton connBrowseBtn;
	
	ImageIcon connectIcon = new ImageIcon("imgs/connect.png");
	ImageIcon loadIcon = new ImageIcon("imgs/load_small.png");
	ImageIcon unknownIcon = new ImageIcon("imgs/unknown.png");	
	ImageIcon passedIcon = new ImageIcon("imgs/passed.png");
	ImageIcon errorIcon = new ImageIcon("imgs/error.png");
//===================================================================
	
	public Adapter() {
		super("FINCoS - Adapter");	
		
		this.setIconImage(Toolkit.getDefaultToolkit().getImage("imgs/adapter.png"));
		
		testConfigChooser = new JFileChooser(Globals.APP_PATH+"config");
		testConfigChooser.addChoosableFileFilter(new FileNameExtensionFilter("XML Configuration file", "xml"));
		testConfigChooser.setAcceptAllFileFilterUsed(false);
		testConfigChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		connPropertiesChooser = new JFileChooser(Globals.APP_PATH+"config");
		connPropertiesChooser.addChoosableFileFilter(new FileNameExtensionFilter("Properties file (.properties)", "properties"));
		connPropertiesChooser.setAcceptAllFileFilterUsed(false);
    	connPropertiesChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		// Setup Panel
		JPanel setupPanel = new JPanel();
		setupPanel.setBorder(BorderFactory.createTitledBorder("Setup"));
		setupPanel.setPreferredSize(new Dimension(200, 130));
		JLabel testConfigFileLabel = new JLabel("Test Configuration File: ");		
		testConfigFileField = new JTextField(40);
		testConfigFileField.setToolTipText("Path for a test configuration file.");
		JButton configBrowseBtn = new JButton("...");		
		configBrowseBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int action = testConfigChooser.showOpenDialog(null);
				if(action == JFileChooser.APPROVE_OPTION && testConfigChooser.getSelectedFile() != null) {
					String path = testConfigChooser.getSelectedFile().getPath();
					testConfigFileField.setText(path);
					try {
						if(loadProfile(path)) {
							connPropertiesFileField.setEnabled(true);
							connBrowseBtn.setEnabled(true);
							connectBtn.setEnabled(true);
							loadBtn.setEnabled(false);	
						}						
					} catch (Exception e1) {						
						JOptionPane.showMessageDialog(null, "Could not open configuration file. File may be corrupted. ", "Error", JOptionPane.ERROR_MESSAGE);
					}
					
				}
									
			}
			
		});
		JLabel connPropertiesFileLabel = new JLabel("Connection Properties: ");		
		connPropertiesFileField = new JTextField(40);
		connPropertiesFileField.setToolTipText("Path of file containing connection's properties to a CEP engine.");
		connBrowseBtn = new JButton("...");		
		connBrowseBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int action = connPropertiesChooser.showOpenDialog(null);
				if(action == JFileChooser.APPROVE_OPTION && connPropertiesChooser.getSelectedFile() != null) {
					connPropertiesFileField.setText(connPropertiesChooser.getSelectedFile().getPath());
					connectBtn.setEnabled(true);
					loadBtn.setEnabled(false);
				}
									
			}			
		});
		connPropertiesFileField.setEnabled(false);
		connBrowseBtn.setEnabled(false);
		
		connectBtn = new JButton("Connect", connectIcon);
		connectBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {	
				Properties prop = new Properties();
				FileInputStream fis=null;
				try {
					fis = new FileInputStream(connPropertiesFileField.getText());
					prop.load(fis);	
					boolean passedStreamCompatibilityTest = connectToCEPEngine(prop);
					if(passedStreamCompatibilityTest) {
						showInfo("Done!");
						loadBtn.setEnabled(true);
						connectBtn.setEnabled(false);
						inputTable.setEnabled(false);
						outputTable.setEnabled(false);
					}
					else {
						showInfo("Done (with warnings)");
						JOptionPane.showMessageDialog(null, "One or more configured streams are not available at CEP engine. Please deselect them and try again.", "Stream Incompatibility", JOptionPane.WARNING_MESSAGE);
					}
					
				}
				catch (FileNotFoundException fnfe) {
					JOptionPane.showMessageDialog(null, "Could not open properties file.\n(file not found)", "Error", JOptionPane.ERROR_MESSAGE);
				}				
				catch (Exception exc) {					
					JOptionPane.showMessageDialog(null, "Cannot connect to CEP engine.\n(" + exc.getMessage() + ")", "Error", JOptionPane.ERROR_MESSAGE);					
					showInfo("Error while connecting to CEP engine.");
				} 
				finally {
					try {
						if(fis != null)
							fis.close();
					} catch (IOException ioe) {
						System.err.println("Could not close properties file ("+ioe.getMessage()+").");						
					}
				}
			}
			
		});
		connectBtn.setEnabled(false);		
		connectBtn.setToolTipText("Connects to CEP engine and checks stream compatibility.");
		
		loadBtn = new JButton("Load", loadIcon);
		loadBtn.setEnabled(false);
		loadBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {	
				if(inputTable.getCellEditor() != null)
					inputTable.getCellEditor().stopCellEditing();
				if (outputTable.getCellEditor() != null)
					outputTable.getCellEditor().stopCellEditing();
				try {					
					HashMap<Integer, Integer> inputPortsWorkerThreadCounts = getInputMap();
					incomingEventsSockets = new ServerSocket[inputPortsWorkerThreadCounts .size()];					
					HashMap<String, ArrayList<InetSocketAddress>> outputStreamsToSinks = getOutputMap();
					showInfo("Loading Adapter...");
					load(inputPortsWorkerThreadCounts, outputStreamsToSinks);					
					showInfo("Done! Loading complete. Adapter is now ready.");
				}	
				catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(null, "Invalid address/port value.");					
				}
				catch (Exception exc) {							
					JOptionPane.showMessageDialog(null, "Could not load adapter.\n(" + exc.getMessage() + ")", "Error", JOptionPane.ERROR_MESSAGE);					
					showInfo("Error while loading adapter.");
				}	
				finally {
					loadBtn.setEnabled(false);
					connectBtn.setEnabled(true);
					inputTable.setEnabled(true);
					outputTable.setEnabled(true);
				}
			}			
		});	
		loadBtn.setToolTipText("Loads the Adapter (Opens Server Socket for Drivers and subscribes to outputs streams at CEP engine).");
		
		setupPanel.add(testConfigFileLabel);
		setupPanel.add(testConfigFileField);
		setupPanel.add(configBrowseBtn);
		setupPanel.add(connPropertiesFileLabel);
		setupPanel.add(connPropertiesFileField);
		setupPanel.add(connBrowseBtn);
		setupPanel.add(connectBtn);
		setupPanel.add(loadBtn);
		
		// Streams Panel
		JPanel streamsPanel = new JPanel(new BorderLayout());
		streamsPanel.setBorder(BorderFactory.createTitledBorder("Streams"));
		JPanel inputPanel = new JPanel(new BorderLayout());
		inputPanel.setPreferredSize(new Dimension(330, 200));
		inputPanel.setBorder(BorderFactory.createTitledBorder("Input"));
		inputTable = new JTable();
		inputTable.setToolTipText("Bind input streams to port(s) on this machine.");
		inputTable.setModel(new javax.swing.table.DefaultTableModel(
	           new String [] {
	                "", "Name", "Port", "Driver Count", "check"
	            }, 0
	        ){
			@Override
			public boolean isCellEditable(int row, int column) {				
					return column == 0;				
			}
			@SuppressWarnings("unchecked")
			@Override
	        public Class getColumnClass(int c) {
				if (getValueAt(0, c)!= null)
					return getValueAt(0, c).getClass();
				else
					return Object.class;
	        }
		});
		inputTable.setColumnSelectionAllowed(true);	
		inputTable.setEnabled(false);
		inputTable.setRowHeight(20);
		inputTable.getTableHeader().setReorderingAllowed(false);	        
		inputTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);		
		inputTable.getColumnModel().getColumn(0).setPreferredWidth(20);
		inputTable.getColumnModel().getColumn(0).setMaxWidth(20);		
		inputTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		inputTable.getColumnModel().getColumn(2).setPreferredWidth(55);
		inputTable.getColumnModel().getColumn(3).setPreferredWidth(55);
		inputTable.getColumnModel().getColumn(4).setPreferredWidth(35);
		inputTable.getColumnModel().getColumn(4).setMaxWidth(35);					
		JScrollPane inputScroll = new JScrollPane(); 
		inputScroll.setViewportView(inputTable);
		inputPanel.add(inputScroll);
		JPanel outputPanel = new JPanel(new BorderLayout());
		outputPanel.setBorder(BorderFactory.createTitledBorder("Output"));
		outputPanel.setPreferredSize(new Dimension(355, 200));
		outputTable = new javax.swing.JTable();
		outputTable.setToolTipText("Bind output streams to Sinks, specifying their addresses and ports.");
		outputTable.setModel(new javax.swing.table.DefaultTableModel(			  
	           new String [] { "", "Name", "Address", "Port", "check"}
	           ,0
	        ){
			@Override
			public boolean isCellEditable(int row, int column) {				
				return column == 0;
			}	
			
			@SuppressWarnings("unchecked")
			@Override
	        public Class getColumnClass(int c) {
				if (getValueAt(0, c)!= null)
					return getValueAt(0, c).getClass();
				else
					return Object.class;
	        }
			
		});
		outputTable.setColumnSelectionAllowed(true);
		outputTable.setEnabled(false);
		outputTable.setRowHeight(20);
		outputTable.setEditingColumn(1);
		outputTable.setEditingRow(1);	        
		outputTable.getTableHeader().setReorderingAllowed(false);	        
		outputTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		outputTable.getColumnModel().getColumn(0).setPreferredWidth(20);
		outputTable.getColumnModel().getColumn(0).setMaxWidth(20);
		outputTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		outputTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		outputTable.getColumnModel().getColumn(3).setPreferredWidth(40);
		outputTable.getColumnModel().getColumn(4).setPreferredWidth(35);
		outputTable.getColumnModel().getColumn(4).setMaxWidth(35);		
		JScrollPane outputScroll = new JScrollPane(); 
		outputScroll.setViewportView(outputTable);
	    outputPanel.add(outputScroll);   
		streamsPanel.add(inputPanel, BorderLayout.LINE_START);
		streamsPanel.add(outputPanel, BorderLayout.LINE_END);		
		
		// Info log area
		infoArea = new JTextArea(8, 20);               
        infoArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Info"));
        JScrollPane infoScroll = new JScrollPane(infoArea);
        infoArea.setEditable(false);
		
        
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(setupPanel, BorderLayout.PAGE_START);
		this.getContentPane().add(infoScroll, BorderLayout.PAGE_END);
		this.getContentPane().add(streamsPanel, BorderLayout.CENTER);
		
		this.setSize(700, 600);        
        this.setLocationRelativeTo(null);
		this.setResizable(false);		
		this.setVisible(true);
		
		this.addWindowListener(new WindowAdapter() {			
			public void windowClosing(WindowEvent e) {				
				System.out.println("Disconnecting from CEP engine...");							
				unload();
				
				// Gives some time to settle
				try {					
					Thread.sleep(500);
				} catch (InterruptedException iexc) {			
					iexc.printStackTrace();
				}
				System.out.println("Done");				
				System.exit(0);
			}
			
		});
	}
		
	private boolean loadProfile(String path) throws NumberFormatException, DOMException, Exception {
		config = new ConfigurationParser();		
		config.open(path);
		
		Set<InetAddress> myAddresses = new HashSet<InetAddress>();
		try {
			 Globals.retrieveMyIPAddresses(myAddresses);			
		} catch (SocketException e) {
			System.err.println("Could not retrieve IP addresses of current machine. Setting to localhost address.");
			myAddresses.add(InetAddress.getLocalHost());
		}
				
		rtMeasurementMode = config.getResponseTimeMeasurementMode();		
		
		if(config.getCommunicationMode() == Globals.DIRECT_API_COMMUNICATION) {
			JOptionPane.showMessageDialog(null, "Communication in test configuration file does not use an instance of FINCoS Adapter.", 
					"Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		socketBufferSize = config.getSocketBufferSize();		
		
		// INPUT		
		DriverConfig drivers[] = config.retrieveDriverList();
		DefaultTableModel inputModel = (DefaultTableModel) this.inputTable.getModel();
		// Clear Table
		int inputRowCount = inputModel.getRowCount();		
		for (int i = 0; i < inputRowCount; i++) {			
			inputModel.removeRow(0);
		}
		HashMap<String, Set<DriverConfig>> inputStreamNamesPortsDriverCount = new HashMap<String, Set<DriverConfig>>();		
		SyntheticWorkloadPhase syntheticPhase;
		ExternalFileWorkloadPhase externalFilePhase;
		
		for (DriverConfig dr : drivers) {				
			if( 
				/* The IP address of the Adapter in Driver's configuration matches one of 
				 * the addresses of the current machine.
				 */
				myAddresses.contains(dr.getServerAddress()) 
			      ||
			     /* Or the IP address of the Adapter in Driver's configuration is set to 
			      * localhost (127.0.0.1) and the Driver itself runs in the same machine than 
			      * the Adapter
			      */
			   (	dr.getServerAddress().getHostAddress().equals("127.0.0.1") 
			    		&& 
			    	(dr.getAddress().getHostAddress().equals("127.0.0.1")||myAddresses.contains(dr.getAddress()))
			   )
			)					  
			{					
				for (WorkloadPhase phase: dr.getWorkload()) {
					if(phase instanceof SyntheticWorkloadPhase) {
						syntheticPhase = (SyntheticWorkloadPhase) phase;
						for (EventType inputStream: syntheticPhase.getSchema().keySet()) {
							addRowToInputTable(inputStream.getName(), dr, inputModel, inputStreamNamesPortsDriverCount);
						}						
					} else if(phase instanceof ExternalFileWorkloadPhase) {
						externalFilePhase = (ExternalFileWorkloadPhase) phase;						
						if(externalFilePhase.containsEventTypes()) {
							addRowToInputTable("Unknown", dr, inputModel, inputStreamNamesPortsDriverCount);
						}
						else {
							addRowToInputTable(externalFilePhase.getSingleEventTypeName(), dr, inputModel, inputStreamNamesPortsDriverCount);	
						}
					}
				}
			}
		}		
		inputTable.setEnabled(inputModel.getRowCount() != 0);
		
		// OUTPUT
		SinkConfig sinks[] = config.retrieveSinkList();		
		DefaultTableModel outputModel = (DefaultTableModel) this.outputTable.getModel();
		// Clear Table
		int outputRowCount = outputModel.getRowCount();		
		for (int i = 0; i < outputRowCount; i++) {			
			outputModel.removeRow(0);
		}		
		for (SinkConfig sink : sinks) {
			for (String outputStream: sink.getOutputStreamList()) {
				if( 
						/* The IP address of the Adapter in Sink's configuration matches one of 
						 * the addresses of the current machine.
						 */
						myAddresses.contains(sink.getServerAddress()) 
					      ||
					     /* Or the IP address of the Adapter in Sink's configuration is set to 
					      * localhost (127.0.0.1) and the Sink itself runs in the same machine than 
					      * the Adapter
					      */
					   (	sink.getServerAddress().getHostAddress().equals("127.0.0.1") 
					    		&& 
					    	(sink.getAddress().getHostAddress().equals("127.0.0.1")||myAddresses.contains(sink.getAddress()))
					   )
					)
					outputModel.addRow(new Object[]{true, outputStream, sink.getAddress().getHostAddress(), ""+sink.getPort(), unknownIcon});				
			}
		}	
		outputTable.setEnabled(outputModel.getRowCount() != 0);
		
		return true;
	}


	// Utilitary method
	private void addRowToInputTable(String inputStream, DriverConfig dr, DefaultTableModel inputModel, HashMap<String, Set<DriverConfig>> inputStreamNamesPortsDriverCount) {
		Set<DriverConfig> driverSet;
		if(!inputStreamNamesPortsDriverCount.containsKey(inputStream+"/"+dr.getServerPort()))
		{
			inputModel.addRow(new Object[]{true, inputStream, ""+dr.getServerPort(), ""+1, unknownIcon});
			driverSet = new HashSet<DriverConfig>();
			driverSet.add(dr);
			inputStreamNamesPortsDriverCount.put(inputStream+"/"+dr.getServerPort(), driverSet);
		}
		else {
			driverSet = inputStreamNamesPortsDriverCount.get(inputStream+"/"+dr.getServerPort());
			driverSet.add(dr);

			int driverCount = driverSet.size();

			String streamName;
			String port;
			for (int i = 0; i < inputModel.getRowCount(); i++) {
				streamName = (String) inputModel.getValueAt(i, 1);
				port = (String) inputModel.getValueAt(i, 2);

				if(streamName.equals(inputStream) && port.equals(""+dr.getServerPort())) 									
					inputModel.setValueAt(""+driverCount, i, 3);
			}
		}

	}
	
	/**
	 * Connects to CEP engine and retrieves streams list.
	 * This distributable version comes with support only to the open-source engine Esper
	 * Change this method to add support for other products.
	 * 
	 * @param connProp			Connection properties
	 * @throws Exception
	 */
	public boolean connectToCEPEngine(Properties connProp) throws Exception {				
		// Unloads Adapter
		unload();	
		
		boolean ret = true;
		
		this.cepEngine = CEPEngineFactory.getCEPEngineInterface(connProp);		
		if(cepEngine == null)		
			throw new Exception("Unsupported CEP engine");		
		cepEngine.setRtMeasurementMode(this.rtMeasurementMode);
		cepEngine.setSocketBufferSize(this.socketBufferSize);
		//cepEngine.setCommunicationMode(this)
		showInfo("Connecting to CEP engine...");
		cepEngine.connect();
		
		// Checks streams compatibility
		boolean selected;
		String streamName;
		boolean contains;
		boolean engineProvidesStreamsList=true;
		//INPUT
		String inputStreams [] = cepEngine.getInputStreamList();
			
		if(inputStreams != null) {
			DefaultTableModel inputModel = (DefaultTableModel) this.inputTable.getModel();		
			for (int i = 0; i < inputModel.getRowCount(); i++) {	
				selected = 	(Boolean) inputModel.getValueAt(i, 0);
				streamName = (String) inputModel.getValueAt(i, 1);
				contains = false;
				if(!streamName.equals("Unknown")) {
					for (int j = 0; j < inputStreams.length; j++) {
						if(streamName.equals(inputStreams[j])) {
							contains = true;
							inputModel.setValueAt(passedIcon, i, 4);
							break;
						}
					}			
					if(!contains) {
						inputModel.setValueAt(errorIcon, i, 4);
						
						if(selected) {
							ret = false;
						}
					}	
				}			
			}
		}
		else {
			engineProvidesStreamsList = false;
		}

		//OUTPUT	
		String outputStreams [] = cepEngine.getOutputStreamList();
		if(outputStreams != null) {
			DefaultTableModel outputModel = (DefaultTableModel) this.outputTable.getModel();		
			for (int i = 0; i < outputModel.getRowCount(); i++) {	
				selected = 	(Boolean) outputModel.getValueAt(i, 0);
				streamName = (String) outputModel.getValueAt(i, 1);
				contains = false;
				for (int j = 0; j < outputStreams.length; j++) {
					if(streamName.equals(outputStreams[j])) {
						contains = true;
						outputModel.setValueAt(passedIcon, i, 4);
						break;
					}
				}			
				if(!contains) {
					outputModel.setValueAt(errorIcon, i, 4);
					
					if(selected) {
						ret = false;
					}
				}
			}
		}
		else {
			engineProvidesStreamsList = false;
		}
		
		if(!engineProvidesStreamsList) {
			JOptionPane.showMessageDialog(null, "Could not check stream compatibility at CEP engine.", "Stream Compatibility Test", JOptionPane.WARNING_MESSAGE);
		}
					
		return ret;
	}
	
	/**
	 * Loads the adapter:
	 * 1) Initializes Server socket for drivers
	 * 2) Initializes worker threads to listening to Drivers connections
	 * 3) Initializes listeners for output streams
	 * 
	 * @param driversPorts				A Mapping local port -> number of drivers
	 * @param outputToSink				A mapping between output streams and Sinks 
	 * @throws IOException
	 */
	public void load(HashMap<Integer,Integer> driversPorts , HashMap<String, ArrayList<InetSocketAddress>> outputToSink) throws Exception {	
		int workerThreadCount = 0;
		for ( int count: driversPorts.values()) {
			workerThreadCount+=count;
		}
		workerThreads = new DriverListener[workerThreadCount];
		
		int socketIndex = 0;
		int workerThreadIndex = 0;
		for (Entry<Integer, Integer> e: driversPorts.entrySet()) {
			if(incomingEventsSockets[socketIndex] == null || incomingEventsSockets[socketIndex].isClosed()) {				
				showInfo("\tInitiliazing server socket at port " + e.getKey() + "...");
				incomingEventsSockets[socketIndex] = new ServerSocket(e.getKey());				

				// Initializes worker threads to listen for drivers' events
				ThreadGroup workersGroup = new ThreadGroup("Driver Listeners");
				for (int i = workerThreadIndex; i < workerThreadIndex+e.getValue(); i++) {					
					workerThreads[i] = new DriverListener(incomingEventsSockets[socketIndex], cepEngine, workersGroup, ""+i, rtMeasurementMode);
					workerThreads[i].start();
				}
				workerThreadIndex+=e.getValue();
			}			
			socketIndex++;
		}
		showInfo("\tDone!");

		// Loads specific adapters (Initializes listeners for output streams)
		showInfo("\tInitiliazing vendor-specific adapter...");
		this.cepEngine.load(outputToSink);
		showInfo("\tDone!");
	}
	
	private HashMap<Integer, Integer> getInputMap() {
		HashMap<Integer, Integer> ret = new HashMap<Integer, Integer>();
		boolean selected;
		DefaultTableModel inputModel = (DefaultTableModel) this.inputTable.getModel();
		Integer port, count, totalCount;
		for (int i = 0; i < inputModel.getRowCount(); i++) {
			selected = (Boolean)inputModel.getValueAt(i, 0);
			if(selected) {
				port = Integer.parseInt((String) inputModel.getValueAt(i, 2));
				count = Integer.parseInt((String) inputModel.getValueAt(i, 3));
				totalCount = ret.get(port);
				
				if(totalCount == null)
					totalCount = count;								
				else
					totalCount += count;
				
				ret.put(port, totalCount);
			}			
		}				
		
		return ret;
	}
	
	/**
	 * Reads the output table and creates a mapping output stream -> list of subscribers (sinks)
	 * 
	 * @return
	 */
	private HashMap<String, ArrayList<InetSocketAddress>> getOutputMap() {
		DefaultTableModel outputModel = (DefaultTableModel) this.outputTable.getModel();
		
		HashMap<String, ArrayList<InetSocketAddress>> ret = new HashMap<String, ArrayList<InetSocketAddress>>();
		ArrayList<InetSocketAddress> listOfSubscribers;
		InetSocketAddress sinkAddrPort;
		String address, outputStreamName;
		boolean selected;
		int port;
		
		for (int i = 0; i < outputModel.getRowCount(); i++) {
			selected = (Boolean)outputModel.getValueAt(i, 0);
			if(selected) {
				address = (String)outputModel.getValueAt(i, 2);
				
				if(address == null || address.isEmpty())
					throw new NumberFormatException("Invalid address value.");
				port = Integer.parseInt((String)outputModel.getValueAt(i, 3));
				sinkAddrPort = new InetSocketAddress(address, port);
				outputStreamName = (String)outputModel.getValueAt(i, 1);
				if(ret.containsKey(outputStreamName)) {
					listOfSubscribers = ret.get(outputStreamName);
				}
				else {
					listOfSubscribers = new ArrayList<InetSocketAddress>();					
					ret.put(outputStreamName, listOfSubscribers);
				}
				listOfSubscribers.add(sinkAddrPort);
			}			
		}		
		return ret;
	}
	
	/**
	 * Unloads the adapter: 
	 * Closes the server socket and worker threads used to receive events from Drivers
	 * Unloads CEP engine
	 */
	public void unload() {		
		// Disconnects worker threads
		if(this.workerThreads != null) {
			for (int i = 0; i < this.workerThreads.length; i++) {	
				if(this.workerThreads[i] != null)
					this.workerThreads[i].close();
					this.workerThreads[i] = null;
			}	
		}
			
		// Closes server sockets
		try {
			if(this.incomingEventsSockets != null) {
				for (int i = 0; i < this.incomingEventsSockets.length; i++) {	
					if(incomingEventsSockets[i] != null)
						this.incomingEventsSockets[i].close();
				}	
			}			
		} catch (IOException ioe) {
			System.err.println("Could not close connection(" + ioe.getMessage() + ")");
		}
		
		// Unloads CEP adapter (disconnects from CEP engine and destroys output listeners)
		if(cepEngine != null && cepEngine.status.getStep() != Step.DISCONNECTED) {			
			showInfo("Disconnecting from CEP engine...");
			cepEngine.disconnect();
			showInfo("Done!");
		}		
	}
	
	public void showInfo(String msg) {
		Date now = new Date();
		
		infoArea.append(Globals.TIME_FORMAT.format(now) + " - " + msg + "\n" );
		infoArea.setCaretPosition(infoArea.getDocument().getLength());
	}

	
	public static void main(String[] args) {
		java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Adapter();
            }
        });
	}
}


/**
 * Listens connections from Drivers and forwards events to CEP engine
 * 
 */
class DriverListener extends SocketWorkerThread {
	private CEPEngineInterface cepInterface;		
	private int rtMeasurementMode;
	private String event;
	private long timestamp;
	
	public DriverListener(ServerSocket ss, CEPEngineInterface cep, ThreadGroup group, 
			String threadID,int rtMeasurementMode) throws IOException {
		super(threadID, group, ss);
		this.cepInterface = cep;				
		this.rtMeasurementMode = rtMeasurementMode;
	}

	@Override
	public void processIncomingMessage(Object o) {		
		try {			
			event = (String) o;
			
			if(this.rtMeasurementMode == Globals.ADAPTER_RT_NANOS) {
				timestamp = System.nanoTime();
				event +=Globals.CSV_SEPARATOR+timestamp;
			}
			
			this.cepInterface.sendEvent(event);
			
		} catch (Exception e) {				
			System.err.println("Error while processing event \"" + o + "\"");			
		}		
	}
		

}
