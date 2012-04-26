package pt.uc.dei.fincos.validation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;

import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.communication.SocketWorkerThread;
import pt.uc.dei.fincos.controller.ConfigurationParser;
import pt.uc.dei.fincos.controller.DriverConfig;
import pt.uc.dei.fincos.controller.SinkConfig;
import pt.uc.dei.fincos.controller.gui.PopupListener;
import pt.uc.dei.fincos.data.CSVReader;
import pt.uc.dei.fincos.driver.SyntheticWorkloadPhase;
import pt.uc.dei.fincos.driver.WorkloadPhase;
import pt.uc.dei.fincos.validation.realtime.PerfMonValidator;
import pt.uc.dei.fincos.validation.realtime.ThroughputEstimator;


/**
 *
 * FINCoS Performance Monitor main class.
 * Computes performance metrics in real time or
 * from log files produced by Sinks.
 *
 * @author Marcelo R.N. Mendes
 *
 */
public class PerformanceMonitor extends JFrame {
    /** serial id. */
    private static final long serialVersionUID = 7919137484290346256L;

    //======================== Real time performance monitoring variables =======================
    /** List of Server Sockets used to receive data from Drivers and Sinks (one for each port). */
    private ArrayList<ServerSocket> ssList;

    /** Worker threads for receiving events from server Sockets. */
    ArrayList<Receiver> workers;

    /** Loads configuration files containing test setup. */
    private ConfigurationParser config = new ConfigurationParser();

    /** List of drivers configured to send events to this Perfmon instance. */
    private ArrayList<DriverConfig> drivers;

    /** List of sinks configured to send events to this Perfmon instance. */
    private ArrayList<SinkConfig> sinks;

    /** For each combination of query/CEP server, there will be a specific performance validator. */
    private HashMap<String, PerfMonValidator> outputPerfValidators; // one for each

    /**
     *  Defines when throughput is computed.
     */
    public enum WindowEvaluationModel {
        /** At specified intervals (in this case every time GUI is refreshed, 1 per second). */
        TIME_BASED,
        /** Every time a new event arrives. */
        TUPLE_BASED
    };

    /** The evaluation model used in this Perfmon instance. */
    private WindowEvaluationModel throughputWindowModel = WindowEvaluationModel.TUPLE_BASED;

    /**
     * For each combination of input stream/CEP server, there will be a list of throughput estimators (one for each Driver)
     * Throughput of a given input stream will be computed as the sum of the throughputs of all the different Drivers
     * that send events of that type.
     */
    private HashMap<String, ArrayList<ThroughputEstimator>> inputThroughputEstimators;
    //==============================================================================================


    //===================== Offline Performance Measuring (Sink logs) variables ====================
    /** List of log files to be processed by this perfmon instance. */
    ArrayList <String> sinkLogFiles = new ArrayList<String>();

    /** Computes performance stats from Sink log files. */
    OfflinePerformanceValidator offlinePerf;
    //==============================================================================================


    //======================================= GUI ==================================================
    JTextArea infoArea;
    JTabbedPane tabbedPanel;
    JPanel sourcePanel, statsPanel;
    SourcePanel srcPanel;
    GraphPanel graphsPanel; javax.swing.
    JScrollPane statsScroll, graphsScroll;
    JXTable statsTable;
    ArrayList <JTable> statsTables;
    Font statsTableFont = new Font("arial", Font.PLAIN, 14);
    JPanel driversPanel, sinksPanel;						// Real time monitoring
    ArrayList<JLabel> driverLabelList, sinkLabelList;		// Real time monitoring
    JTextField configFilePathField;							// Real time monitoring
    Timer guiRefresher; 									// Real time monitoring
    JSplitPane splitPane;
  //==============================================================================================


    /**
     * Creates and shows a perfmon instance.
     */
    public PerformanceMonitor() {
        super("FINCoS Performance Monitor");
        this.setIconImage(Toolkit.getDefaultToolkit().getImage("imgs/perfmon.png"));
        initGUI();
    }


    private void initGUI() {
        Container c = this.getContentPane();
        c.setLayout(new BorderLayout());

        // Source Panel
        sourcePanel = new JPanel();
        sourcePanel.setLayout(new BorderLayout());
        srcPanel = new SourcePanel();
        infoArea = new JTextArea(8, 15);
        infoArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Info"));
        JScrollPane infoScroll = new JScrollPane(infoArea);
        infoArea.setEditable(false);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,	srcPanel, infoScroll);
        splitPane.setOneTouchExpandable(true);
        sourcePanel.add(splitPane);

        // Stats Panel
        statsPanel = new JPanel();
        statsScroll = new JScrollPane(statsPanel);

        // GraphsPanel
        graphsPanel = new GraphPanel();
        graphsPanel.setLayout(new BorderLayout());
        graphsScroll = new JScrollPane(graphsPanel);

        tabbedPanel = new JTabbedPane();
        tabbedPanel.addTab("Source", sourcePanel);
        tabbedPanel.addTab("Stats", statsScroll);
        tabbedPanel.add("Graph", graphsScroll);
        c.add(tabbedPanel, BorderLayout.CENTER);

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }

        });

        this.setSize(850, 625);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);
    }

    /**
     * Loads the FINCoS Performance Monitor tool for real time monitoring.
     *
     * @param rtMeasurementMode  Either END_TO_END_RT_MILLIS, ADAPTER_RT_NANOS, or NO_RT
     */
    private void loadForRealTime(int rtMeasurementMode) {
        // Initializes communication
        try {
            showInfo("Initializing communication...");
            openServerSockets();
            initializeWorkerThreads(rtMeasurementMode);
            showInfo("Done! Waiting for client connections.");
        } catch (IOException exc) {
            showInfo("Error: " + exc.getClass() + " message: " + exc.getMessage());
        }

        // Initializes Validation mapping stream -> Validator
        initializeValidators();

        // Initializes Throughput estimators for Input Streams
        inputThroughputEstimators = new HashMap<String, ArrayList<ThroughputEstimator>>();

        // Load stats panel
        statsTables = new ArrayList<JTable>();
        loadStatsPanel(getServerList());

        // Load graphs panel
        graphsPanel.loadForRealTimeMonitoring(drivers.toArray(new DriverConfig[0]), sinks.toArray(new SinkConfig[0]));

        // Initializes GUI refresher thread
        if (guiRefresher == null) {
            int delay = 1000 / Globals.DEFAULT_GUI_REFRESH_RATE;
            guiRefresher = new Timer(delay, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    refreshGUI();
                }
            });
        } else {
            guiRefresher.stop();
        }
        guiRefresher.start();
    }

    /**
     * Loads the FINCoS Performance Monitor tool for offline validation
     * (from Sink log files).
     *
     * @param inputSinkLogFiles		A set of log files produced by Sinks
     * @param saveToFile			Indicates if performance stats must be saved to a file
     * @param outputPerfMonLogFile	The path of the file into which performance stats will be saved
     * @param startTime				Starting point of log processing
     * @param endTime				Ending point of log processing
     * @param startProcessingBtn	Button used to start log processing (must be disabled during processing, and enabled afterwards)
     * @param stopProcessingBtn		Button used to interrupt log processing (must be disabled after processing)
     * @param bar					Progress bar used to show progress of log processing
     */
    private void loadForSinkLogFile(String inputSinkLogFiles[], boolean saveToFile,	String outputPerfMonLogFile,
            long startTime, long endTime,
            JButton startProcessingBtn, JButton stopProcessingBtn, JProgressBar bar) {
        if (guiRefresher != null) {
            guiRefresher.stop();
            guiRefresher = null;
        }

        try {
            showInfo("Processing Sink log files...");
            offlinePerf = new OfflinePerformanceValidator(inputSinkLogFiles, saveToFile, outputPerfMonLogFile);
            SinkLogProcessor backgroundThread =
                new SinkLogProcessor(startTime, endTime, startProcessingBtn, stopProcessingBtn, bar);
            backgroundThread.execute();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Could not process log files (" + e.getMessage() + ")", "ERROR", JOptionPane.ERROR_MESSAGE);
            this.srcPanel.stopSinkLogBtn.setEnabled(false);
            this.srcPanel.processSinkLogBtn.setEnabled(true);
            showInfo("Failed");
            return;
        }
    }

    /**
     * Loads the FINCoS Performance Monitor tool for showing
     * performance stats from a previously generated performance
     * log file
     * @param perfLogFile
     * @throws IOException
     */
    private void loadForPerfMonLogFile(String perfLogFile)  {
        if (guiRefresher != null) {
            guiRefresher.stop();
            guiRefresher = null;
        }

        showInfo("Loading FINCoS Performance Monitor log file...");
        CSVReader logReader;
        TreeSet<PerformanceStats> statsSeries = new TreeSet<PerformanceStats>();

        try {
            logReader = new CSVReader(perfLogFile);
            String header = logReader.getNextLine();

            if (header != null && header.equals("FINCoS Performance Log File.")) {
                //ignores next header lines
                do {
                    header = logReader.getNextLine();
                }
                while(header != null && !header.startsWith("Timestamp"));

                String event;
                String[] splitEv;
                String server;
                String streamName, streamType;
                Stream stream;
                PerformanceStats stats = null;

                while ((event = logReader.getNextLine()) != null) {
                    splitEv = CSVReader.split(event, Globals.CSV_SEPARATOR);
                    if (splitEv.length == 9) { // Legacy log files
                        server = splitEv[1];
                        streamName = splitEv[2].substring(0, splitEv[2].indexOf("("));
                        streamType = splitEv[2].substring(splitEv[2].indexOf("(") + 1, splitEv[2].indexOf(")"));
                        if (streamType.equalsIgnoreCase("input")) {
                            stream = new Stream(Stream.INPUT, streamName);
                        } else {
                            stream = new Stream(Stream.OUTPUT, streamName);
                        }
                        stats = new PerformanceStats(server, stream);
                        stats.timestamp = Long.parseLong(splitEv[0]);
                        stats.avg_throughput = Double.parseDouble(splitEv[3]);
                        stats.min_throughput = Double.NaN;
                        stats.max_throughput = Double.NaN;
                        stats.last_throughput = Integer.parseInt(splitEv[4]);
                        stats.avgRT = Double.parseDouble(splitEv[5]);
                        stats.minRT = Double.parseDouble(splitEv[6]);
                        stats.maxRT = Double.parseDouble(splitEv[7]);
                        stats.lastRT = Double.parseDouble(splitEv[8]);
                        stats.stdevRT = Double.NaN;
                        statsSeries.add(stats);
                    } else if (splitEv.length == 12) { // Current log format
                        server = splitEv[1];
                        streamName = splitEv[2].substring(0, splitEv[2].indexOf("("));
                        streamType = splitEv[2].substring(splitEv[2].indexOf("(") + 1, splitEv[2].indexOf(")"));
                        if (streamType.equalsIgnoreCase("input")) {
                            stream = new Stream(Stream.INPUT, streamName);
                        } else {
                            stream = new Stream(Stream.OUTPUT, streamName);
                        }
                        try {
                            stats = new PerformanceStats(server, stream);
                            stats.timestamp = Long.parseLong(splitEv[0]);
                            stats.avg_throughput = Double.parseDouble(splitEv[3]);
                            stats.min_throughput = Double.parseDouble(splitEv[4]);
                            stats.max_throughput = Double.parseDouble(splitEv[5]);
                            stats.last_throughput = Double.parseDouble(splitEv[6]);
                            stats.avgRT = Double.parseDouble(splitEv[7]);
                            stats.minRT = Double.parseDouble(splitEv[8]);
                            stats.maxRT = Double.parseDouble(splitEv[9]);
                            stats.stdevRT = Double.parseDouble(splitEv[10]);
                            stats.lastRT = Double.parseDouble(splitEv[11]);
                            statsSeries.add(stats);
                        } catch (NumberFormatException e) {
                            System.err.println("NumberFormatException " + e.getMessage());
                        }
                    } else {
                        System.err.println("Invalid record format. Expecting 12 fields, got "
                                          + splitEv.length + " fields.");
                    }
                }

                // Load graphs panel
                graphsPanel.loadForLogFiles(statsSeries);

                // Load stats panel
                statsTables = new ArrayList<JTable>();
                loadStatsPanel(graphsPanel.serversStreams);
            } else {
                JOptionPane.showMessageDialog(null, "Invalid log file", "Error", JOptionPane.ERROR_MESSAGE);
                this.srcPanel.stopSinkLogBtn.setEnabled(false);
                this.srcPanel.processSinkLogBtn.setEnabled(true);
                showInfo("Failed");
                return;
            }

            showInfo("Done!");
        } catch (IOException e) {
            showInfo("Failed");
        }
    }

    @SuppressWarnings("serial")
    private JTable addStatsTable(String serverAddress) {
        JPanel serverPanel;

        serverPanel = new JPanel(new BorderLayout());

        serverPanel.setBorder(BorderFactory.createTitledBorder("CEP Engine at " + serverAddress));
        statsTable = new JXTable();
        DefaultTableModel model = new DefaultTableModel(
                new String [] {"Key", "Query",
                        "Avg Throughput", "Min Throughput", "Max Throughput", "Last Throughput",
                        "Avg RT (ms)", "Min RT (ms)", "Max RT (ms)", "Last RT (ms)",
                "Stdev RT (ms)"}, 0)
        {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        statsTable.setModel(model);
        statsTable.setBackground(statsPanel.getBackground());
        statsTable.setColumnControlVisible(true);
        statsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        statsTable.setFont(statsTableFont);
        statsTable.setRowHeight(40);

        //hides column key (it is used only internally)
        statsTable.removeColumn(statsTable.getColumnModel().getColumn(0));
        statsTable.getColumnModel().getColumn(0).setPreferredWidth(150);

        if (srcPanel.sinkLogRadio.isSelected()) {
            JPopupMenu popup = new JPopupMenu();
            JMenuItem rtHistMenu = new JMenuItem("RT Histogram...");
            popup.add(rtHistMenu);
            rtHistMenu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openRTHistogramPanel();
                }
            });
            statsTable.addMouseListener(new PopupListener(popup));
        }


        JScrollPane statsScroll = new JScrollPane();
        statsScroll.setViewportView(statsTable);

        // Hides Min Throughput, Max throughput and Stdev RT columns
        ((TableColumnExt) statsTable.getColumnModel().getColumn(statsTable.getColumnModel().getColumnIndex("Min Throughput"))).setVisible(false);
        ((TableColumnExt) statsTable.getColumnModel().getColumn(statsTable.getColumnModel().getColumnIndex("Max Throughput"))).setVisible(false);
        ((TableColumnExt) statsTable.getColumnModel().getColumn(statsTable.getColumnModel().getColumnIndex("Stdev RT (ms)"))).setVisible(false);

        serverPanel.add(statsScroll, BorderLayout.CENTER);
        serverPanel.setPreferredSize(new Dimension(50, statsTable.getRowCount() * 30));
        statsPanel.add(serverPanel);

        statsTables.add(statsTable);

        return statsTable;
    }

    private void openRTHistogramPanel() {
        DefaultTableModel model = (DefaultTableModel) statsTable.getModel();
        int rowIndex = statsTable.getSelectedRow();

        if (rowIndex >= 0) {

            String streamName = (String) model.getValueAt(rowIndex, 1);
            HistogramPanel histPanel = new HistogramPanel(sinkLogFiles.toArray(new String[0]), streamName,
                    ((Date) srcPanel.sinkLogStartSpinner.getValue()).getTime(),
                    ((Date) srcPanel.sinkLogEndSpinner.getValue()).getTime());
            JFrame frame = new JFrame();
            frame.setTitle("RT distribution for \"" + streamName + "\"");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setBounds(0, 0, 455, 530);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            frame.getContentPane().add(histPanel);
            frame.repaint();
        }

    }

    /**
     * Initializes the stats panel for Log files
     * Adds one table for each server
     * Each table contains one line for every output stream.
     */
    private void loadStatsPanel(TreeMap<String, HashSet<Stream>> serversStreams) {
        statsPanel.removeAll();
        statsTables.clear();
        statsPanel.setLayout(new GridLayout(serversStreams.size(), 1));

        double avgThroughput, minThroughput, maxThroughput, lastThroughput,
        avgRT, minRT, maxRT, lastRT, stdevRT;
        avgThroughput = minThroughput = maxThroughput = lastThroughput
                      = avgRT = minRT = maxRT = lastRT = stdevRT = 0;
        for (Entry<String, HashSet<Stream>> e: serversStreams.entrySet()) {
            String server = e.getKey();
            DefaultTableModel model = (DefaultTableModel) addStatsTable(server).getModel();

            TreeMap<Long, Double> queryStats;
            for (Stream stream : e.getValue()) {
                String keyPrefix = server + Globals.CSV_SEPARATOR + stream.name + Globals.CSV_SEPARATOR;
                if (stream.type == Stream.INPUT) {
                    keyPrefix += "Input" + Globals.CSV_SEPARATOR;
                } else {
                    keyPrefix += "Output" + Globals.CSV_SEPARATOR;
                }

                queryStats = graphsPanel.seriesData.get(keyPrefix + "Avg Throughput");
                if (queryStats != null) {
                    avgThroughput = queryStats.lastEntry().getValue();
                }

                queryStats = graphsPanel.seriesData.get(keyPrefix + "Min Throughput");
                if (queryStats != null) {
                    minThroughput = queryStats.lastEntry().getValue();
                }

                queryStats = graphsPanel.seriesData.get(keyPrefix + "Max Throughput");
                if (queryStats != null) {
                    maxThroughput = queryStats.lastEntry().getValue();
                }

                queryStats = graphsPanel.seriesData.get(keyPrefix + "Last Throughput");
                if (queryStats != null) {
                    lastThroughput = queryStats.lastEntry().getValue();
                }

                queryStats = graphsPanel.seriesData.get(keyPrefix + "Avg Response Time");
                if (queryStats != null) {
                    avgRT = queryStats.lastEntry().getValue();
                }

                queryStats = graphsPanel.seriesData.get(keyPrefix + "Min Response Time");
                if (queryStats != null) {
                    minRT = queryStats.lastEntry().getValue();
                }

                queryStats = graphsPanel.seriesData.get(keyPrefix  + "Max Response Time");
                if (queryStats != null) {
                    maxRT = queryStats.lastEntry().getValue();
                }

                queryStats = graphsPanel.seriesData.get(keyPrefix + "Last Response Time");
                if (queryStats != null) {
                    lastRT = queryStats.lastEntry().getValue();
                }

                queryStats = graphsPanel.seriesData.get(keyPrefix + "Stdev Response Time");
                if (queryStats != null) {
                    stdevRT = queryStats.lastEntry().getValue();
                }

                model.addRow(new Object[] {keyPrefix, stream.name,
                        Globals.FLOAT_FORMAT_2.format(avgThroughput),
                        Globals.FLOAT_FORMAT_2.format(minThroughput),
                        Globals.FLOAT_FORMAT_2.format(maxThroughput),
                        Globals.FLOAT_FORMAT_2.format(lastThroughput),
                        Globals.FLOAT_FORMAT_3.format(avgRT),
                        Globals.FLOAT_FORMAT_3.format(minRT),
                        Globals.FLOAT_FORMAT_3.format(maxRT),
                        Globals.FLOAT_FORMAT_3.format(lastRT),
                        Globals.FLOAT_FORMAT_3.format(stdevRT)});
            }
        }
    }


    /**
     * Initializes the stats panel for Real time monitoring.
     * Adds one table for each server.
     * Each table contains one line for every output stream.
     */
    private void loadStatsPanel(InetAddress[] serverList) {
        statsPanel.removeAll();
        statsTables.clear();
        statsPanel.setLayout(new GridLayout(serverList.length, 1));

        for (InetAddress serverAddress : serverList) {
            String server, outputName;

            DefaultTableModel model = (DefaultTableModel) addStatsTable(serverAddress.getHostAddress()).getModel();

            for (String serverAndOutput: outputPerfValidators.keySet()) {
                server = serverAndOutput.split("/")[0];
                outputName = serverAndOutput.split("/")[1];
                if (server.equals(serverAddress.getHostAddress())) {
                    model.addRow(new Object[] {serverAndOutput, outputName,"0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0"});
                }

            }
        }
        statsPanel.revalidate();
    }

    /**
     * Removes all GUI components from the stats panel.
     */
    private void clearStatsPanel() {
        statsPanel.removeAll();
        statsPanel.repaint();
        statsPanel.revalidate();
    }

    /**
     * Adds a Driver from the test setup into the Perfmon application.
     *
     * @param dr	The configuration of the Driver
     */
    private void addDriver(DriverConfig dr) {
        JLabel drLabel = new JLabel(dr.getAlias() + " at " + dr.getAddress().getHostAddress());
        drLabel.setIcon(Globals.YELLOW_SIGN);
        drLabel.setToolTipText("Disconnected");
        this.drivers.add(dr);
        this.driversPanel.add(drLabel);
        this.driverLabelList.add(drLabel);
    }

    /**
     * Adds a Sink from the test setup into the Perfmon application.
     *
     * @param sink	The configuration of the Sink
     */
    private void addSink(SinkConfig sink) {
        JLabel sinkLabel = new JLabel(sink.getAlias() + " at " + sink.getAddress().getHostAddress());
        sinkLabel.setIcon(Globals.YELLOW_SIGN);
        sinkLabel.setToolTipText("Disconnected");
        this.sinks.add(sink);
        this.sinksPanel.add(sinkLabel);
        this.sinkLabelList.add(sinkLabel);
    }

    private void showInfo(String msg) {
        Date now = new Date();

        infoArea.append(Globals.TIME_FORMAT.format(now) + " - " + msg + "\n");
        infoArea.setCaretPosition(infoArea.getDocument().getLength());
    }

    private void updateStatus(String id, ImageIcon icon, String toolTip) {
        for (JLabel drLabel : driverLabelList) {
            if (drLabel.getText().equals(id)) {
                drLabel.setIcon(icon);
                drLabel.setToolTipText(toolTip);
                drLabel.revalidate();
                return;
            }
        }

        for (JLabel sinkLabel : sinkLabelList) {
            if (sinkLabel.getText().equals(id)) {
                sinkLabel.setIcon(icon);
                sinkLabel.setToolTipText(toolTip);
                sinkLabel.revalidate();
                return;
            }
        }

    }

    private void refreshGUI() {
        if (this.outputPerfValidators != null) {
            PerfMonValidator validator;

            // Updates Query Table(s)
            if (this.statsTables != null) {
                DefaultTableModel model;
                for (JTable table : statsTables) {
                    String outputName;
                    model = (DefaultTableModel) table.getModel();
                    for (int i = 0; i < table.getRowCount(); i++) {
                        outputName = (String) model.getValueAt(i, 0);
                        validator = outputPerfValidators.get(outputName);
                        double minRT, minThroughput;
                        if (validator != null) {
                            model.setValueAt(Globals.FLOAT_FORMAT_2.format(validator.getAvgThroughput()), i, 2);
                            // If window evaluation model is time-based, recompute throughput at clock tick
                            if (throughputWindowModel == WindowEvaluationModel.TIME_BASED) {
                                validator.computeCurrentThroughput();
                            }
                            minThroughput = validator.getMinThroughput();
                            if (minThroughput != Double.MAX_VALUE) {
                                model.setValueAt(Globals.FLOAT_FORMAT_2.format(minThroughput), i, 3);
                            }
                            model.setValueAt(Globals.FLOAT_FORMAT_2.format(validator.getMaxThroughput()), i, 4);
                            model.setValueAt(Globals.FLOAT_FORMAT_2.format(validator.getCurrentThroughput()), i, 5);
                            model.setValueAt(Globals.FLOAT_FORMAT_3.format(validator.getAvgRT()), i, 6);
                            minRT = validator.getMinRT();
                            if (minRT != Double.MAX_VALUE) {
                                model.setValueAt(Globals.FLOAT_FORMAT_3.format(minRT), i, 7);
                            }
                            model.setValueAt(Globals.FLOAT_FORMAT_3.format(validator.getMaxRT()), i, 8);
                            model.setValueAt(Globals.FLOAT_FORMAT_3.format(validator.getCurrentRT()), i, 9);
                            model.setValueAt(Globals.FLOAT_FORMAT_3.format(validator.getStdevRT()), i, 10);
                        } else {
                            model.setValueAt(Globals.FLOAT_FORMAT_2.format(0), i, 2);
                            model.setValueAt(Globals.FLOAT_FORMAT_2.format(0), i, 3);
                            model.setValueAt(Globals.FLOAT_FORMAT_2.format(0), i, 4);
                            model.setValueAt(Globals.FLOAT_FORMAT_2.format(0), i, 5);
                            model.setValueAt(Globals.FLOAT_FORMAT_3.format(0), i, 6);
                            model.setValueAt(Globals.FLOAT_FORMAT_3.format(0), i, 7);
                            model.setValueAt(Globals.FLOAT_FORMAT_3.format(0), i, 8);
                            model.setValueAt(Globals.FLOAT_FORMAT_3.format(0), i, 9);
                            model.setValueAt(Globals.FLOAT_FORMAT_3.format(0), i, 10);
                        }
                    }
                }
            }

            //Update Counters Graph
            graphsPanel.refreshGraphs();
        }
    }


    private void openServerSockets() throws IOException {
        close();

        Set<Integer> portList = new HashSet<Integer>();

        if (this.drivers != null) {
            for (DriverConfig dr : this.drivers) {
                portList.add(dr.getValidatorPort());
            }
        }

        if (this.sinks != null){
            for (SinkConfig sink : this.sinks) {
                portList.add(sink.getValidatorPort());
            }
        }

        this.ssList = new ArrayList<ServerSocket>(portList.size());
        for (Integer port : portList) {
            try {
                showInfo("   Opening server socket at port " + port + "...");
                ServerSocket ss = new ServerSocket(port);
                ssList.add(ss);
                showInfo("   OK!");
            } catch (IOException e) {
                showInfo("Could not open server socket at " + port + "(" + e.getMessage() + ")");
            }

        }
    }

    private void close() {
        try {
            if (workers != null) {
                for (Receiver worker : workers) {
                    if (worker != null) {
                        worker.close();
                        worker = null;
                    }
                }
                workers = null;
            }

            // Close server sockets
            if (ssList != null )
            {
                for (ServerSocket ss : ssList) {
                    ss.close();
                }
            }
        } catch (IOException e1) {
            System.err.println("Could not close socket (" + e1.getMessage() + ")");
        }
    }

    /**
     * Starts worker threads for listening connections from Drivers and Sinks.
     *
     * @param rtMeasurementMode  Either END_TO_END_RT_MILLIS, ADAPTER_RT_NANOS, or NO_RT
     */
    private void initializeWorkerThreads(int rtMeasurementMode) {
        workers = new ArrayList<Receiver>(drivers.size() + sinks.size());
        Receiver r;

        if (this.ssList != null) {
            if (this.drivers != null) {
                for (DriverConfig dr : this.drivers) {
                    for (ServerSocket ss : this.ssList) {
                        if (dr.getValidatorPort() == ss.getLocalPort()) {
                            r = new Receiver("ANONYMOUS", ss, rtMeasurementMode);
                            workers.add(r);
                            r.start();
                            break;
                        }
                    }

                }
            }

            if (this.sinks != null){
                for (SinkConfig sink : this.sinks) {
                    for (ServerSocket ss : this.ssList) {
                        if (sink.getValidatorPort() == ss.getLocalPort()) {
                            r = new Receiver("ANONYMOUS", ss, rtMeasurementMode);
                            workers.add(r);
                            r.start();
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Initializes the list of Validators.
     */
    private void initializeValidators() {
        outputPerfValidators = new HashMap<String, PerfMonValidator>();

        String key;

        if (this.sinks != null) {
            for (SinkConfig sink : this.sinks) {
                for (String outputStream : sink.getOutputStreamList()) {
                    key = sink.getServerAddress().getHostAddress() + "/" + outputStream;
                    outputPerfValidators.put(key, null);
                }

            }
        }

    }


    /**
     * Retrieves the list of all CEP engines (Servers) used in the test from the drivers and sinks lists.
     *
     * @return	An array with the server list
     */
    private InetAddress[] getServerList() {
        InetAddress[] ret;
        Set<InetAddress> list = new HashSet<InetAddress>();

        if (this.drivers != null) {
            for (DriverConfig dr : this.drivers) {
                list.add(dr.getServerAddress());
            }
        }

        if (this.sinks != null) {
            for (SinkConfig sink : this.sinks) {
                list.add(sink.getServerAddress());
            }
        }

        ret = new InetAddress[list.size()];
        list.toArray(ret);

        return ret;
    }

    /**
     * Gives the configuration of the component that just connected to the specified port from the specified address.
     *
     * @param componentAddress		The address of the remote component
     * @param localPort				The port on local machine on which the component connected
     * @return						The configuration of the component (either DriverConfig or SinkConfig)
     */
    private Object getComponentConfig(InetAddress componentAddress, int localPort) {
        if (this.drivers != null) {
            for (DriverConfig dr : this.drivers) {
                if (dr.getAddress().equals(componentAddress) && dr.getValidatorPort() == localPort) {
                    return dr;
                }
            }
        }
        if (this.sinks != null) {
            for (SinkConfig sink : this.sinks) {
                if (sink.getAddress().equals(componentAddress) && sink.getValidatorPort() == localPort) {
                    return sink;
                }
            }
        }
        return null;
    }

    /**
     * Processes Sink log files in background.
     */
    class SinkLogProcessor extends SwingWorker<Set<PerformanceStats>, Void> {
        JButton stopBtn, startBtn;
        JProgressBar bar;
        Timer progressBarRefresher;
        long startTime, endTime;
        public SinkLogProcessor(long startTime, long endTime,
                JButton startBtn, JButton stopBtn, JProgressBar bar) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.startBtn = startBtn;
            this.stopBtn = stopBtn;
            this.bar = bar;
        }

        @Override
        protected Set<PerformanceStats> doInBackground() throws Exception  {
            if (progressBarRefresher != null) {
                progressBarRefresher.stop();
                progressBarRefresher = null;
            }

            int delay = 1000 / Globals.DEFAULT_GUI_REFRESH_RATE;
            progressBarRefresher = new Timer(delay, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    bar.setValue((int) (100 * offlinePerf.getProgress()));
                }
            });
            progressBarRefresher.start();
            return offlinePerf.processLogFiles(startTime, endTime);
        }

        @Override
        protected void done() {
            Set<PerformanceStats> statsSeries;
            try {
                statsSeries = get();

                if (statsSeries != null) {
                    // Load stats panel
                    statsTables = new ArrayList<JTable>();

                    // Load graphs panel
                    graphsPanel.loadForLogFiles(statsSeries);

                    // Load stats panel
                    statsTables = new ArrayList<JTable>();
                    loadStatsPanel(graphsPanel.serversStreams);

                    if (offlinePerf.finished) {
                        showInfo("Finished!"
                                + " (" + offlinePerf.totalProcessedCount + " entries processed. "
                                + " Elapsed time: " + offlinePerf.processingTime / 1000 + " seconds.)");
                        if (progressBarRefresher != null) {
                            progressBarRefresher.stop();
                            progressBarRefresher = null;
                        }
                        bar.setValue(100);
                    }

                } else {
                    showInfo("Failed");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (progressBarRefresher != null) {
                    progressBarRefresher.stop();
                    progressBarRefresher = null;
                }

                offlinePerf = null;
                stopBtn.setEnabled(false);
                startBtn.setEnabled(true);
            }

        }

    }


    /**
     * Receives events from Drivers and Sinks,
     * adds them into a buffer and triggers updates
     * of performance stats.
     *
     * @author Marcelo R.N. Mendes
     *
     */
    class Receiver extends SocketWorkerThread {
        private int function;
        private InetAddress serverAddress;
        private HashMap<String, LinkedList<Long>> streamsTimestampsBuffers;
        private int rtMeasurementMode;

        public Receiver(String threadID, ServerSocket ss, int rtMeasurementMode)  {
            super(threadID, ss);
            streamsTimestampsBuffers = new HashMap<String, LinkedList<Long>>();
            this.rtMeasurementMode = rtMeasurementMode;
        }

        @Override
        protected void listenToConnections() {
            if (this.keepListening) {
                streamsTimestampsBuffers.clear();
                disconnect();
                try {
                    System.out.println("Worker thread " + this.getName() + " ready. Waiting for client connections...");
                    updateStatus(this.getName(), Globals.YELLOW_SIGN, "Disconnected");
                    this.clientSocket = serverSocket.accept();
                    Object config = getComponentConfig(clientSocket.getInetAddress(), serverSocket.getLocalPort());

                    if (config != null) {
                        if (config instanceof DriverConfig) {
                            DriverConfig dr = (DriverConfig) config;
                            this.setName(dr.getAlias() + " at " + dr.getAddress().getHostAddress());
                            this.function = Stream.INPUT;
                            this.serverAddress = dr.getServerAddress();
                            // Creates a buffer for storing timestamps of events for each input stream
                            for (String streamName: dr.getStreamNames()) {
                                LinkedList<Long> tsBuffer = new LinkedList<Long>();
                                streamsTimestampsBuffers.put(streamName, tsBuffer);
                                synchronized (inputThroughputEstimators) {
                                    ArrayList<ThroughputEstimator> txnEstimatorList;
                                    if (inputThroughputEstimators.containsKey(dr.getServerAddress().getHostAddress() + "/" + streamName)) {
                                        txnEstimatorList = inputThroughputEstimators.get(dr.getServerAddress().getHostAddress() + "/" + streamName);
                                        txnEstimatorList.add(
                                                new ThroughputEstimator(tsBuffer, 1,
                                                        dr.getValidationSamplingRate(),
                                                        throughputWindowModel,
                                                        rtMeasurementMode));
                                    } else {
                                        txnEstimatorList = new ArrayList<ThroughputEstimator>();
                                        inputThroughputEstimators.put(dr.getServerAddress().getHostAddress() + "/" + streamName, txnEstimatorList);
                                        txnEstimatorList.add(
                                                new ThroughputEstimator(tsBuffer, 1,
                                                        dr.getValidationSamplingRate(),
                                                        throughputWindowModel,
                                                        rtMeasurementMode));
                                    }
                                }
                            }
                        } else if (config instanceof SinkConfig) {
                            SinkConfig sink = (SinkConfig) config;
                            this.setName(sink.getAlias() + " at " + sink.getAddress().getHostAddress());
                            this.function = Stream.OUTPUT;
                            this.serverAddress = sink.getServerAddress();
                            // Creates a buffer for storing timestamps of events for each output stream
                            for (String streamName: sink.getOutputStreamList()) {
                                LinkedList<Long> buffer = new LinkedList<Long>();
                                streamsTimestampsBuffers.put(streamName, buffer);
                                PerfMonValidator validator =
                                    new PerfMonValidator(sink.getValidationSamplingRate(), buffer, throughputWindowModel, rtMeasurementMode);
                                outputPerfValidators.put(sink.getServerAddress().getHostAddress() + "/" + streamName, validator);
                            }
                        }
                        this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        System.out.println("Worker thread " + this.getName() + " accepted client connection.");
                        showInfo(this.getName() + " is now connected.");
                        updateStatus(this.getName(), Globals.GREEN_SIGN, "Connected");
                    } else {
                        System.out.println("Could not identify component. Connection will now be reset.");
                        listenToConnections();
                    }
                } catch (IOException ioe) {
                    System.err.println("Could not accept client connections. (" + ioe.getMessage() + ")");
                }
            }
        }

        @Override
        public void processIncomingMessage(Object o) throws Exception {
            if (o != null & o instanceof String) {
                String event = (String) o;
                String type = event.substring(event.indexOf(":") + 1, event.indexOf(Globals.CSV_SEPARATOR));
                long timestamp = Long.parseLong(event.substring(event.lastIndexOf(Globals.CSV_SEPARATOR)+1));

                // retrieves the appropriate buffer for this event type
                LinkedList<Long> buffer = streamsTimestampsBuffers.get(type);
                // Stores the event's timestamp on the buffer for posterior processing
                if (buffer != null) {
                    synchronized (buffer) {
                        buffer.offer(timestamp);
                    }
                } else {
                    System.err.println("WARNING: Received event of unknown type.");
                    return;
                }

                // If event comes from an output stream, updates query stats
                if (this.function == Stream.OUTPUT) {
                    PerfMonValidator validator = outputPerfValidators.get(this.serverAddress.getHostAddress()+"/"+type);
                    validator.recomputeStats(event);
                }
            } else {
                System.err.println("WARNING. Invalid or null event.");
            }
        }
    }

    //=================================== END OF Receiver internal class ========================================

    /**
     * Panel of FINCoS Performance Monitor tool that
     * allows users to choose one of three options of
     * sources:
     * 1) Real time;
     * 2) Sink log files;
     * 3) Log file from the FINCoS PerfMon tool
     *
     * @author  Marcelo R.N. Mendes
     */
    class SourcePanel extends javax.swing.JPanel {
        private static final long serialVersionUID = -7531389579938374773L;

        private Date sinkLogStartTime = new Date();
        private Date sinkLogEndTime = new Date(sinkLogStartTime.getTime()+3600000);

        /** Creates new form SourcePanel */
        public SourcePanel() {
            initComponents();
        }

        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        // <editor-fold defaultstate="collapsed" desc="Generated Code">
        private void initComponents() {
            sourceRadioGroup = new javax.swing.ButtonGroup();
            realTimeRadio = new javax.swing.JRadioButton();
            sinkLogRadio = new javax.swing.JRadioButton();
            perfLogRadio = new javax.swing.JRadioButton();
            perfLogField = new javax.swing.JTextField();
            perfLogBrowseBtn = new javax.swing.JButton();
            realTimePanel = new javax.swing.JPanel();
            componentsPanel = new javax.swing.JPanel();
            configFilePathField = new javax.swing.JTextField();
            componentsLabel = new javax.swing.JLabel();
            configFilePathLabel = new javax.swing.JLabel();
            configBrowseBtn = new javax.swing.JButton();
            sinkLogPanel = new javax.swing.JPanel();
            jScrollPane1 = new javax.swing.JScrollPane();
            sinkLogsList = new javax.swing.JList();
            addSinkLogBtn = new javax.swing.JButton();
            removeSinkLogBtn = new javax.swing.JButton();
            sinkLogStartLabel = new javax.swing.JLabel();
            sinkLogStartSpinner = new javax.swing.JSpinner();
            sinkLogEndLabel = new javax.swing.JLabel();
            sinkLogEndSpinner = new javax.swing.JSpinner();
            saveToFileCheck = new javax.swing.JCheckBox();
            saveToFileField = new javax.swing.JTextField();
            sinkLogProgressBar = new javax.swing.JProgressBar();
            stopSinkLogBtn = new javax.swing.JButton();
            processSinkLogBtn = new javax.swing.JButton();

            setBorder(javax.swing.BorderFactory.createEtchedBorder());

            sourceRadioGroup.add(realTimeRadio);
            realTimeRadio.setFont(new java.awt.Font("Tahoma", 1, 11));
            realTimeRadio.setText("Real Time");
            realTimeRadio.setToolTipText("Compute performance stats from a running test.");

            sourceRadioGroup.add(sinkLogRadio);
            sinkLogRadio.setFont(new java.awt.Font("Tahoma", 1, 11));
            sinkLogRadio.setText("Sink Log File(s)");
            sinkLogRadio.setToolTipText("Process one or more log files of Sinks and compute performance stats.");

            sourceRadioGroup.add(perfLogRadio);
            perfLogRadio.setFont(new java.awt.Font("Tahoma", 1, 11));
            perfLogRadio.setText("PerfMon Log File");
            perfLogRadio.setToolTipText("Show stats from  performance log file generated by the FINCoS PerfMon tool.");

            perfLogBrowseBtn.setText("...");

            realTimePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

            javax.swing.GroupLayout componentsPanelLayout = new javax.swing.GroupLayout(componentsPanel);
            componentsPanel.setLayout(componentsPanelLayout);
            componentsPanelLayout.setHorizontalGroup(
                    componentsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGap(0, 459, Short.MAX_VALUE)
            );
            componentsPanelLayout.setVerticalGroup(
                    componentsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGap(0, 193, Short.MAX_VALUE)
            );

            configFilePathField.setToolTipText("Path of file containing test setup");

            componentsLabel.setText("Components:");

            configFilePathLabel.setText("Config. File:");

            configBrowseBtn.setText("...");

            javax.swing.GroupLayout realTimePanelLayout = new javax.swing.GroupLayout(realTimePanel);
            realTimePanel.setLayout(realTimePanelLayout);
            realTimePanelLayout.setHorizontalGroup(
                    realTimePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(realTimePanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(realTimePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(componentsLabel)
                                    .addComponent(configFilePathLabel))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(realTimePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, realTimePanelLayout.createSequentialGroup()
                                                    .addComponent(configFilePathField, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(configBrowseBtn))
                                                    .addComponent(componentsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addGap(26, 26, 26))
            );
            realTimePanelLayout.setVerticalGroup(
                    realTimePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(realTimePanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(realTimePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(configFilePathLabel)
                                    .addComponent(configBrowseBtn)
                                    .addComponent(configFilePathField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(realTimePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(componentsLabel)
                                            .addComponent(componentsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .addContainerGap())
            );

            sinkLogPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

            jScrollPane1.setViewportView(sinkLogsList);

            addSinkLogBtn.setText("Add...");

            removeSinkLogBtn.setText("Remove");
            removeSinkLogBtn.setEnabled(false);

            sinkLogStartLabel.setText("Start");

            sinkLogStartSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE));

            sinkLogEndLabel.setText("End");

            sinkLogEndSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE));

            saveToFileCheck.setSelected(true);
            saveToFileCheck.setText("Save to file:");
            saveToFileCheck.setToolTipText("Save stats to a FINCoS PerfMon log file.");
            saveToFileCheck.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    saveToFileCheckItemStateChanged(evt);
                }
            });

            saveToFileField.setMaximumSize(new java.awt.Dimension(100, 20));

            sinkLogProgressBar.setStringPainted(true);

            stopSinkLogBtn.setToolTipText("Cancel processing of sink log files");
            stopSinkLogBtn.setEnabled(false);
            stopSinkLogBtn.setMaximumSize(new java.awt.Dimension(35, 23));
            stopSinkLogBtn.setMinimumSize(new java.awt.Dimension(35, 23));

            processSinkLogBtn.setToolTipText("Start processing sink log files");

            javax.swing.GroupLayout sinkLogPanelLayout = new javax.swing.GroupLayout(sinkLogPanel);
            sinkLogPanel.setLayout(sinkLogPanelLayout);
            sinkLogPanelLayout.setHorizontalGroup(
                    sinkLogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sinkLogPanelLayout.createSequentialGroup()
                            .addGroup(sinkLogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(sinkLogPanelLayout.createSequentialGroup()
                                            .addGap(10, 10, 10)
                                            .addComponent(saveToFileCheck)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(saveToFileField, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE))
                                            .addGroup(sinkLogPanelLayout.createSequentialGroup()
                                                    .addContainerGap()
                                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addGroup(sinkLogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addGroup(sinkLogPanelLayout.createSequentialGroup()
                                                                    .addComponent(addSinkLogBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                    .addComponent(removeSinkLogBtn))
                                                                    .addGroup(sinkLogPanelLayout.createSequentialGroup()
                                                                            .addComponent(processSinkLogBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                            .addComponent(sinkLogProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                                                                            .addGap(6, 6, 6)
                                                                            .addComponent(stopSinkLogBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                            .addGroup(sinkLogPanelLayout.createSequentialGroup()
                                                                                    .addGap(6, 6, 6)
                                                                                    .addGroup(sinkLogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                            .addComponent(sinkLogEndLabel)
                                                                                            .addComponent(sinkLogStartLabel))
                                                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                            .addGroup(sinkLogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                                                    .addComponent(sinkLogEndSpinner)
                                                                                                    .addComponent(sinkLogStartSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))))))
                                                                                                    .addContainerGap())
            );
            sinkLogPanelLayout.setVerticalGroup(
                    sinkLogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sinkLogPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(sinkLogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(sinkLogPanelLayout.createSequentialGroup()
                                            .addGroup(sinkLogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(removeSinkLogBtn)
                                                    .addComponent(addSinkLogBtn))
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addGroup(sinkLogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                            .addComponent(sinkLogStartLabel)
                                                            .addComponent(sinkLogStartSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addGroup(sinkLogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                    .addComponent(sinkLogEndLabel)
                                                                    .addComponent(sinkLogEndSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                    .addGap(12, 12, 12)
                                                                    .addGroup(sinkLogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                                                            .addComponent(processSinkLogBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                            .addComponent(sinkLogProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                            .addComponent(stopSinkLogBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE))
                                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                            .addGroup(sinkLogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                                                                    .addComponent(saveToFileCheck)
                                                                                    .addComponent(saveToFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                    .addContainerGap())
            );

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(sinkLogRadio)
                                    .addComponent(realTimeRadio)
                                    .addGroup(layout.createSequentialGroup()
                                            .addComponent(perfLogRadio)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(perfLogField, javax.swing.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(perfLogBrowseBtn))
                                            .addComponent(realTimePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(sinkLogPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .addContainerGap())
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(realTimeRadio)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(realTimePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(sinkLogRadio)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(sinkLogPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGap(18, 18, 18)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                            .addGap(3, 3, 3)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(perfLogBrowseBtn)
                                                    .addComponent(perfLogField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                    .addComponent(perfLogRadio))
                                                    .addGap(28, 28, 28))
            );
            // ------------------------- END OF GENERATED CODE ----------------------------

            // ------------------------------ CUSTOM CODE ---------------------------------
            processSinkLogBtn.setText("");
            processSinkLogBtn.setIcon(new ImageIcon("imgs/start_small_green.png"));
            stopSinkLogBtn.setText("");
            stopSinkLogBtn.setIcon(new ImageIcon("imgs/stop_small.png"));
            stopSinkLogBtn.setBorderPainted(false);
            saveToFileCheck.setSelected(false);
            sinkLogStartSpinner.setFont(sinkLogStartSpinner.getFont().deriveFont(Font.PLAIN));
            JSpinner.DateEditor editor1 = new JSpinner.DateEditor(sinkLogStartSpinner, "dd-MM-yyyy HH:mm:ss");
            sinkLogStartSpinner.setValue(sinkLogStartTime);
            sinkLogStartSpinner.setEditor(editor1);
            sinkLogEndSpinner.setFont(sinkLogStartSpinner.getFont());
            JSpinner.DateEditor editor2 = new JSpinner.DateEditor(sinkLogEndSpinner, "dd-MM-yyyy HH:mm:ss");
            sinkLogEndSpinner.setValue(sinkLogEndTime);
            sinkLogEndSpinner.setEditor(editor2);
            sinkLogProgressBar.setMinimum(0);
            sinkLogProgressBar.setMaximum(100);

            driversPanel = new JPanel();
            JScrollPane driversScroll = new JScrollPane(driversPanel);
            driversPanel.setBorder(BorderFactory.createTitledBorder("Drivers"));
            driversPanel.setBackground(Color.WHITE);
            sinksPanel = new JPanel();
            JScrollPane sinksScroll = new JScrollPane(sinksPanel);
            sinksPanel.setBorder(BorderFactory.createTitledBorder("Sinks"));
            sinksPanel.setBackground(Color.WHITE);
            sinksPanel.setLayout(new BorderLayout());
            componentsPanel.setLayout(new GridLayout(1,2));
            componentsPanel.setPreferredSize(new Dimension(450, 185));
            componentsPanel.add(driversScroll);
            componentsPanel.add(sinksScroll);

            // Configuration File Panel
            configChooser = new JFileChooser(Globals.APP_PATH + "config");
            configChooser.addChoosableFileFilter(new FileNameExtensionFilter("XML Configuration file", "xml"));
            configChooser.setAcceptAllFileFilterUsed(false);
            configChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            configBrowseBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int action = configChooser.showOpenDialog(null);
                    if (action == JFileChooser.APPROVE_OPTION && configChooser.getSelectedFile() != null) {
                        configFilePathField.setText(configChooser.getSelectedFile().getPath());
                        try {
                            if (guiRefresher != null) {
                                guiRefresher.stop();
                            }
                            config.open(configChooser.getSelectedFile().getPath());

                            Set<InetAddress> myAddresses = new HashSet<InetAddress>();
                            try {
                                Globals.retrieveMyIPAddresses(myAddresses);
                            } catch (SocketException exc) {
                                System.err.println("Could not retrieve IP addresses of current machine. Setting to localhost address.");
                                myAddresses.add(InetAddress.getLocalHost());
                            }

                            // Retrieve Drivers List
                            DriverConfig[] allDrivers = config.retrieveDriverList();
                            driversPanel.removeAll();
                            driversPanel.setLayout(new GridLayout(allDrivers.length + 5, 1));
                            drivers = new ArrayList<DriverConfig>(allDrivers.length);
                            driverLabelList = new ArrayList<JLabel>(allDrivers.length);
                            for (DriverConfig dr : allDrivers) {
                                if (dr.isValidationEnabled()
                                    /* The IP address of the Validator in Driver's configuration matches one of
                                     * the addresses of the current machine.
                                     */
                                 && (myAddresses.contains(dr.getValidatorAddress())
                                     ||
                                        /* Or the IP address of the Validator in Driver's configuration is set to
                                         * localhost (127.0.0.1) and the Driver itself runs in the same machine than
                                         * the Validator
                                         */
                                        (dr.getValidatorAddress().getHostAddress().equals("127.0.0.1")
                                      && (dr.getAddress().getHostAddress().equals("127.0.0.1")
                                       || myAddresses.contains(dr.getAddress()))))) {
                                    addDriver(dr);
                                }
                            }
                            driversPanel.revalidate();
                            driversPanel.repaint();

                            // Retrieve Sinks List
                            SinkConfig[] allSinks = config.retrieveSinkList();
                            sinksPanel.setLayout(new GridLayout(allSinks.length + 5, 1));
                            sinksPanel.removeAll();
                            sinks = new ArrayList<SinkConfig>(allSinks.length);
                            sinkLabelList = new ArrayList<JLabel>(allSinks.length);
                            for (SinkConfig sink : allSinks) {
                                if (sink.isValidationEnabled()
                                        /* The IP address of the Adapter in Sink's configuration matches one of
                                         * the addresses of the current machine.
                                         */
                                        && (myAddresses.contains(sink.getValidatorAddress())
                                        ||
                                        /* Or the IP address of the Adapter in Sink's configuration is set to
                                         * localhost (127.0.0.1) and the Sink itself runs in the same machine than
                                         * the Adapter
                                         */
                                        (sink.getValidatorAddress().getHostAddress().equals("127.0.0.1")
                                                &&
                                                (sink.getAddress().getHostAddress().equals("127.0.0.1")
                                                        || myAddresses.contains(sink.getAddress()))))) {
                                    addSink(sink);
                                }
                            }
                            sinksPanel.revalidate();
                            sinksPanel.repaint();

                            showInfo("Test setup loaded successfully");

                        } catch (Exception exc) {
                            exc.printStackTrace();
                            JOptionPane.showMessageDialog(null, "Could not open configuration file. File may be corrupted. ", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        if ((drivers == null || drivers.isEmpty())
                         && (sinks == null || sinks.isEmpty())) {
                            JOptionPane.showMessageDialog(null, "No component is configured to send events to this validation tool instance");
                            clearStatsPanel();
                            graphsPanel.clearGraphsPanel();
                            return;
                        }


                        loadForRealTime(config.getResponseTimeMeasurementMode());
                    }
                }
            });

            sinkLogChooser = new JFileChooser(Globals.APP_PATH + "log");
            sinkLogChooser.addChoosableFileFilter(new FileNameExtensionFilter("Sink Log file", "log"));
            sinkLogChooser.setAcceptAllFileFilterUsed(false);
            sinkLogChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            sinkLogChooser.setMultiSelectionEnabled(true);
            addSinkLogBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int action = sinkLogChooser.showOpenDialog(null);

                    File[] selectedFiles = sinkLogChooser.getSelectedFiles();

                    if (action == JFileChooser.APPROVE_OPTION && selectedFiles != null) {
                        for (File file : selectedFiles) {
                            String newFile = file.getPath();
                            if (!sinkLogFiles.contains(newFile)) {
                                sinkLogFiles.add(newFile);
                                sinkLogsList.setListData(sinkLogFiles.toArray(new String[0]));
                                try {
                                    long logStartTime = 1000 * (OfflinePerformanceValidator.getLogStartTimeInMillis(newFile) / 1000);
                                    long logEndTime = 1000 * (OfflinePerformanceValidator.getLogEndTimeInMillis(newFile) / 1000) + 999;
                                    if (sinkLogFiles.size() == 1) { // First element
                                        sinkLogStartTime = new Date(logStartTime);
                                        sinkLogEndTime = new Date(logEndTime);
                                    } else {
                                        sinkLogStartTime.setTime(Math.min(sinkLogStartTime.getTime(), logStartTime));
                                        sinkLogEndTime.setTime(Math.max(sinkLogEndTime.getTime(), logEndTime));
                                    }
                                    sinkLogStartSpinner.setValue(sinkLogStartTime);
                                    sinkLogEndSpinner.setValue(sinkLogEndTime);
                                } catch (Exception exc) {
                                    JOptionPane.showMessageDialog(null, exc.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                                }
                            } else {
                                JOptionPane.showMessageDialog(null, "This file has already been added");
                            }
                        }
                    }
                    removeSinkLogBtn.setEnabled(sinkLogFiles.size() > 0);
                }
            });

            removeSinkLogBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int[] selected  = sinkLogsList.getSelectedIndices();

                    int removedCount = 0;

                    for (int index : selected) {
                        if (index >= 0) {
                            sinkLogFiles.remove(index - removedCount);
                            removedCount++;
                        }
                    }
                    sinkLogsList.setListData(sinkLogFiles.toArray(new String[0]));

                    removeSinkLogBtn.setEnabled(sinkLogFiles.size() > 0);
                }
            });

            perfLogChooser = new JFileChooser(Globals.APP_PATH + "log");
            perfLogChooser.addChoosableFileFilter(new FileNameExtensionFilter("PerfMon Log file", "csv"));
            perfLogChooser.setAcceptAllFileFilterUsed(false);
            perfLogChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            perfLogBrowseBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int action = perfLogChooser.showOpenDialog(null);
                    if (action == JFileChooser.APPROVE_OPTION && perfLogChooser.getSelectedFile() != null) {
                        String perfLogFile = perfLogChooser.getSelectedFile().getPath();
                        perfLogField.setText(perfLogFile);
                        loadForPerfMonLogFile(perfLogFile);
                    }
                }
            });

            ItemListener radioListener1 = new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    // Realtime
                    configFilePathLabel.setEnabled(realTimeRadio.isSelected());
                    configFilePathField.setEnabled(realTimeRadio.isSelected());
                    configBrowseBtn.setEnabled(realTimeRadio.isSelected());
                    componentsLabel.setEnabled(realTimeRadio.isSelected());
                    componentsPanel.setEnabled(realTimeRadio.isSelected());
                    driversPanel.setEnabled(realTimeRadio.isSelected());
                    sinksPanel.setEnabled(realTimeRadio.isSelected());
                    // Sink log file
                    sinkLogsList.setEnabled(sinkLogRadio.isSelected());
                    addSinkLogBtn.setEnabled(sinkLogRadio.isSelected());
                    removeSinkLogBtn.setEnabled(sinkLogRadio.isSelected() && sinkLogsList.getModel().getSize() > 0);
                    sinkLogStartLabel.setEnabled(sinkLogRadio.isSelected());
                    sinkLogStartSpinner.setEnabled(sinkLogRadio.isSelected());
                    sinkLogEndLabel.setEnabled(sinkLogRadio.isSelected());
                    sinkLogEndSpinner.setEnabled(sinkLogRadio.isSelected());
                    sinkLogProgressBar.setEnabled(sinkLogRadio.isSelected());
                    saveToFileCheck.setEnabled(sinkLogRadio.isSelected());
                    saveToFileField.setEnabled(sinkLogRadio.isSelected() && saveToFileCheck.isSelected());
                    processSinkLogBtn.setEnabled(sinkLogRadio.isSelected());
                    // FINCoS perfmon log
                    perfLogField.setEnabled(perfLogRadio.isSelected());
                    perfLogBrowseBtn.setEnabled(perfLogRadio.isSelected());
                }
            };
            realTimeRadio.addItemListener(radioListener1);
            sinkLogRadio.addItemListener(radioListener1);
            perfLogRadio.addItemListener(radioListener1);
            sinkLogRadio.setSelected(true);
            realTimeRadio.setSelected(true);

            processSinkLogBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String[] inputFiles = sinkLogFiles.toArray(new String[0]);
                    String outputFile = saveToFileField.getText();
                    if (!sinkLogFiles.isEmpty()) {
                        if (!saveToFileCheck.isSelected() || (outputFile != null && !outputFile.isEmpty())) {
                            stopSinkLogBtn.setEnabled(true);
                            processSinkLogBtn.setEnabled(false);
                            loadForSinkLogFile(inputFiles, saveToFileCheck.isSelected(), outputFile,
                                    ((Date)sinkLogStartSpinner.getValue()).getTime(),
                                    1000 * (((Date)sinkLogEndSpinner.getValue()).getTime() / 1000) + 999,
                                    processSinkLogBtn, stopSinkLogBtn, sinkLogProgressBar);
                        } else {
                            JOptionPane.showMessageDialog(null, "Inform the path of the perfmon log file.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Inform the path of at least one Sink log file.");
                    }
                }
            });

            stopSinkLogBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (offlinePerf != null) {
                        offlinePerf.stopProcessing();
                        showInfo("Log processing interrupted by the user.");
                        stopSinkLogBtn.setEnabled(false);
                    }

                }
            });

            ChangeListener spinnerChangeListener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    SpinnerDateModel startModel = (SpinnerDateModel) sinkLogStartSpinner.getModel();
                    startModel.setStart(sinkLogStartTime);
                    startModel.setEnd((Date) sinkLogEndSpinner.getValue());

                    SpinnerDateModel endModel = (SpinnerDateModel) sinkLogEndSpinner.getModel();
                    endModel.setStart((Date) sinkLogStartSpinner.getValue());
                    endModel.setEnd(sinkLogEndTime);
                }

            };
            sinkLogStartSpinner.addChangeListener(spinnerChangeListener);
            sinkLogEndSpinner.addChangeListener(spinnerChangeListener);

        }// </editor-fold>

        private void saveToFileCheckItemStateChanged(java.awt.event.ItemEvent evt) {
            saveToFileField.setEnabled(saveToFileCheck.isSelected());
            GregorianCalendar now = new GregorianCalendar();
            if (saveToFileCheck.isSelected()) {
                saveToFileField.setText(Globals.APP_PATH + "log"
                        + File.separator + "["
                        + now.get(Calendar.YEAR) + "-"
                        + Globals.INT_FORMAT_2.format(now.get(Calendar.MONTH) + 1) + "-"
                        + Globals.INT_FORMAT_2.format(now.get(Calendar.DAY_OF_MONTH)) + "_"
                        + Globals.INT_FORMAT_2.format(now.get(Calendar.HOUR_OF_DAY)) + "-"
                        + Globals.INT_FORMAT_2.format(now.get(Calendar.MINUTE)) + "].csv");
            } else {
                saveToFileField.setText("");
            }
        }

        JFileChooser configChooser, sinkLogChooser, perfLogChooser;

        // Variables declaration - do not modify
        private javax.swing.JButton addSinkLogBtn;
        private javax.swing.JLabel componentsLabel;
        private javax.swing.JPanel componentsPanel;
        private javax.swing.JButton configBrowseBtn;
        private javax.swing.JTextField configFilePathField;
        private javax.swing.JLabel configFilePathLabel;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JButton perfLogBrowseBtn;
        private javax.swing.JTextField perfLogField;
        private javax.swing.JRadioButton perfLogRadio;
        private javax.swing.JButton processSinkLogBtn;
        private javax.swing.JPanel realTimePanel;
        private javax.swing.JRadioButton realTimeRadio;
        private javax.swing.JButton removeSinkLogBtn;
        private javax.swing.JCheckBox saveToFileCheck;
        private javax.swing.JTextField saveToFileField;
        private javax.swing.JLabel sinkLogEndLabel;
        private javax.swing.JSpinner sinkLogEndSpinner;
        private javax.swing.JPanel sinkLogPanel;
        private javax.swing.JProgressBar sinkLogProgressBar;
        private javax.swing.JRadioButton sinkLogRadio;
        private javax.swing.JLabel sinkLogStartLabel;
        private javax.swing.JSpinner sinkLogStartSpinner;
        private javax.swing.JList sinkLogsList;
        private javax.swing.ButtonGroup sourceRadioGroup;
        private javax.swing.JButton stopSinkLogBtn;
        // End of variables declaration

    }
    //=================================== END OF SourcePanel internal class ========================================


    /**
     *
     * A panel containing performance graphs and counters
     * Used for showing performance stats at the FINCoS PerfMon application.
     *
     * @author Marcelo R.N. Mendes
     *
     */
    class GraphPanel extends JPanel {
        /** serial id. */
        private static final long serialVersionUID = -9023933264149434389L;

        /** Maps servers to a list of streams.*/
        TreeMap<String, HashSet<Stream>> serversStreams;

        /** Maps servers to a list of series.*/
        private HashMap<String, TimeSeries> graphSeries = new HashMap<String, TimeSeries>();

        /** Performance stats over time. */
        HashMap<String, TreeMap<Long, Double>> seriesData = new HashMap<String, TreeMap<Long, Double>>();

        /** GUI components. */
        JTable countersTable;
        PerfChartPanel chart;
        CounterPanel counterPanel;

        protected void loadForRealTimeMonitoring(DriverConfig[] drivers, SinkConfig[] sinks) {
            // Cleans Panel and subpanels
            this.graphSeries.clear();
            this.seriesData.clear();
            if (chart != null) {
                chart.clear();
            }
            this.clearGraphsPanel();

            // Counters Panel
            serversStreams = new TreeMap<String, HashSet<Stream>>();
            for (DriverConfig dr: drivers) {
                if (!serversStreams.containsKey(dr.getServerAddress().getHostAddress())) {
                    serversStreams.put(dr.getServerAddress().getHostAddress(), new HashSet<Stream>());
                }
                for (WorkloadPhase phase : dr.getWorkload()) {
                    if (phase instanceof SyntheticWorkloadPhase) {
                        SyntheticWorkloadPhase synthPhase = (SyntheticWorkloadPhase) phase;
                        for (EventType type: synthPhase.getSchema().keySet()) {
                            serversStreams.get(dr.getServerAddress().getHostAddress()).add(new Stream(Stream.INPUT, type.getName()));
                        }
                    }
                }
            }
            for (SinkConfig sink: sinks) {
                if (!serversStreams.containsKey(sink.getServerAddress().getHostAddress())) {
                    serversStreams.put(sink.getServerAddress().getHostAddress(), new HashSet<Stream>());
                }
                for (String stream : sink.getOutputStreamList()) {
                    serversStreams.get(sink.getServerAddress().getHostAddress()).add(new Stream(Stream.OUTPUT, stream));
                }

            }

            // Counter panel
            counterPanel = new CounterPanel(serversStreams);

            // Chart
            chart = new PerfChartPanel("", new String[]{}, "", new DecimalFormat("0"), PerfChartPanel.REAL_TIME);

            loadGUIComponents();
        }

        protected void loadForLogFiles(Set<PerformanceStats> statsSeries) {
            // Cleans Panel and subpanels
            this.graphSeries.clear();
            this.seriesData.clear();
            if (chart != null) {
                chart.clear();
            }
            this.clearGraphsPanel();

            serversStreams = new TreeMap<String, HashSet<Stream>>();

            // Chart
            chart = new PerfChartPanel("", new String[]{}, "", new DecimalFormat("0"), PerfChartPanel.LOG_FILE);

            String server;
            Stream stream;
            HashSet<Stream> streamSet;
            for (PerformanceStats stats : statsSeries) {
                server = stats.server;
                stream = stats.stream;
                streamSet = serversStreams.get(server);
                if (streamSet == null) {
                    streamSet = new HashSet<Stream>();
                    streamSet.add(stream);
                    serversStreams.put(server, streamSet);
                } else {
                    streamSet.add(stream);
                }

                String keyPrefix = server + Globals.CSV_SEPARATOR
                                 + stream.name + Globals.CSV_SEPARATOR;
                if (stream.type == Stream.INPUT) {
                    keyPrefix += "Input" + Globals.CSV_SEPARATOR;
                } else {
                    keyPrefix += "Output" + Globals.CSV_SEPARATOR;
                }

                // Average Throughput
                TreeMap<Long, Double> streamAvgThData = seriesData.get(keyPrefix + "Avg Throughput");
                if (streamAvgThData == null) {
                    streamAvgThData = new TreeMap<Long, Double>();
                    seriesData.put(keyPrefix + "Avg Throughput", streamAvgThData);
                }
                streamAvgThData.put(stats.timestamp, stats.avg_throughput);

                // Last Throughput
                TreeMap<Long, Double> streamLastThData = seriesData.get(keyPrefix + "Last Throughput");
                if (streamLastThData == null) {
                    streamLastThData = new TreeMap<Long, Double>();
                    seriesData.put(keyPrefix + "Last Throughput", streamLastThData);
                }
                streamLastThData.put(stats.timestamp, stats.last_throughput);

                // Min Throughput
                TreeMap<Long, Double> streamMinThData = seriesData.get(keyPrefix + "Min Throughput");
                if (streamMinThData == null) {
                    streamMinThData = new TreeMap<Long, Double>();
                    seriesData.put(keyPrefix + "Min Throughput", streamMinThData);
                }
                streamMinThData.put(stats.timestamp, stats.min_throughput);

                // Max Throughput
                TreeMap<Long, Double> streamMaxThData = seriesData.get(keyPrefix + "Max Throughput");
                if (streamMaxThData == null) {
                    streamMaxThData = new TreeMap<Long, Double>();
                    seriesData.put(keyPrefix + "Max Throughput", streamMaxThData);
                }
                streamMaxThData.put(stats.timestamp, stats.max_throughput);

                // Average Response time
                TreeMap<Long, Double> streamAvgRT = seriesData.get(keyPrefix + "Avg Response Time");
                if (streamAvgRT == null) {
                    streamAvgRT = new TreeMap<Long, Double>();
                    seriesData.put(keyPrefix + "Avg Response Time", streamAvgRT);
                }
                streamAvgRT.put(stats.timestamp, stats.avgRT);

                // Last Response time
                TreeMap<Long, Double> streamLastRT = seriesData.get(keyPrefix + "Last Response Time");
                if (streamLastRT == null) {
                    streamLastRT = new TreeMap<Long, Double>();
                    seriesData.put(keyPrefix + "Last Response Time", streamLastRT);
                }
                streamLastRT.put(stats.timestamp, 1.0 * stats.lastRT);

                // Minimum Response time
                TreeMap<Long, Double> streamMinRT = seriesData.get(keyPrefix + "Min Response Time");
                if (streamMinRT == null) {
                    streamMinRT = new TreeMap<Long, Double>();
                    seriesData.put(keyPrefix + "Min Response Time", streamMinRT);
                }
                streamMinRT.put(stats.timestamp, stats.minRT);

                // Maximum Response time
                TreeMap<Long, Double> streamMaxRT = seriesData.get(keyPrefix + "Max Response Time");
                if (streamMaxRT == null) {
                    streamMaxRT = new TreeMap<Long, Double>();
                    seriesData.put(keyPrefix + "Max Response Time", streamMaxRT);
                }
                streamMaxRT.put(stats.timestamp, stats.maxRT);

                // Stdev Response time
                TreeMap<Long, Double> streamStdevRT = seriesData.get(keyPrefix + "Stdev Response Time");
                if (streamStdevRT == null) {
                    streamStdevRT = new TreeMap<Long, Double>();
                    seriesData.put(keyPrefix + "Stdev Response Time", streamStdevRT);
                }
                streamStdevRT.put(stats.timestamp, stats.stdevRT);

            }

            // Counter panel
            counterPanel = new CounterPanel(serversStreams);
            counterPanel.streamsCombo.setSelectedIndex(1);

            loadGUIComponents();

        }

        @SuppressWarnings("serial")
        private void loadGUIComponents() {
            // Counters Table
            JPanel countersTablePanel = new JPanel(new BorderLayout());
            countersTablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Selected Counters"));

            countersTable = new JTable();
            DefaultTableModel model = new DefaultTableModel(new String [] {"Visible", "Color", "Scale", "Server", "Stream", "Counter"}, 0)	{
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 0 || column == 1 || column == 2;
                }

                @Override
                @SuppressWarnings("unchecked")
                public Class getColumnClass(int c) {
                    return getValueAt(0, c).getClass();
                }

            };
            countersTable.setModel(model);
            countersTable.setDefaultRenderer(Color.class, new ColorRenderer(true));
            countersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            countersTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JTable source = (JTable) e.getSource();
                    int row = source.getSelectedRow();
                    int column = source.getSelectedColumn();

                    if (row != -1 && column == 1) {
                        Color c = JColorChooser.showDialog(null, "Counter color", (Color) source.getValueAt(row, column));
                        if (c != null) {
                            source.setValueAt(c, row, column);
                            chart.changeSeriesColor(row, c);
                        }

                    }
                }
            });
            countersTable.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    synchronized (graphSeries) {
                        JTable source = (JTable) e.getSource();
                        int selectedRow = source.getSelectedRow();

                        if (e.getKeyCode() == KeyEvent.VK_DELETE && selectedRow != -1) {
                            String server, stream, streamName, streamType, counter, key;
                            server = (String) source.getValueAt(selectedRow, 3);
                            stream = (String) source.getValueAt(selectedRow, 4);
                            streamName = stream.substring(0, stream.indexOf("(") - 1);
                            streamType = stream.substring(stream.indexOf("(") + 1, stream.indexOf(")"));
                            counter = (String) source.getValueAt(selectedRow, 5);
                            key = server + Globals.CSV_SEPARATOR
                                + streamName + Globals.CSV_SEPARATOR
                                + streamType + Globals.CSV_SEPARATOR
                                + counter;

                            ((DefaultTableModel) source.getModel()).removeRow(selectedRow);
                            chart.removeSeries(key);
                            graphSeries.remove(key);
                        }
                    }
                }
            });

            countersTable.getModel().addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent e) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();
                    DefaultTableModel model = (DefaultTableModel) e.getSource();
                    if (column == 0) {
                        chart.setSeriesVisible(row, (Boolean) model.getValueAt(row, column));
                    } else if (column == 2) {
                        String server, stream, streamName, streamType, counter, key;
                        server = (String) model.getValueAt(row, 3);
                        stream = (String) model.getValueAt(row, 4);
                        streamName = stream.substring(0, stream.indexOf("(") - 1);
                        streamType = stream.substring(stream.indexOf("(") + 1, stream.indexOf(")"));
                        counter = (String) model.getValueAt(row, 5);
                        key = server + Globals.CSV_SEPARATOR
                            + streamName + Globals.CSV_SEPARATOR
                            + streamType + Globals.CSV_SEPARATOR
                            + counter;

                        chart.changeSeriesScale(key, Double.parseDouble((String) model.getValueAt(row, column)));
                    }
                }
            });

            countersTable.getColumnModel().getColumn(0).setPreferredWidth(50);
            countersTable.getColumnModel().getColumn(0).setMaxWidth(50);
            countersTable.getColumnModel().getColumn(1).setPreferredWidth(60);
            countersTable.getColumnModel().getColumn(1).setMaxWidth(60);
            countersTable.getColumnModel().getColumn(2).setPreferredWidth(80);
            countersTable.getColumnModel().getColumn(2).setMaxWidth(80);
            countersTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JComboBox(new String[] { "0.000001", "0.00001", "0.0001", "0.001", "0.01", "0.1", "1", "10", "100", "1000", "10000", "100000", "1000000" })));
            countersTable.getColumnModel().getColumn(3).setPreferredWidth(30);

            JScrollPane countersScroll = new JScrollPane();
            countersScroll.setViewportView(countersTable);
            countersScroll.setPreferredSize(new Dimension(400, 100));
            countersTablePanel.add(countersScroll);

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.add(chart, BorderLayout.CENTER);
            centerPanel.add(countersTablePanel, BorderLayout.PAGE_END);

            this.add(centerPanel, BorderLayout.CENTER);
            this.add(counterPanel, BorderLayout.LINE_END);

        }

        protected void clearGraphsPanel() {
            this.removeAll();
            this.repaint();
            this.revalidate();
        }

        protected void refreshGraphs() {
            String server, streamName, streamType, counter;
            String [] key;
            synchronized (this.graphSeries) {
                if (this.graphSeries != null) {
                    for (Entry<String, TimeSeries> e : graphSeries.entrySet()) {
                        key = e.getKey().split(Globals.CSV_SEPARATOR);
                        server = key[0];
                        streamName = key[1];
                        streamType = key[2];
                        counter = key[3];

                        if (streamType.equalsIgnoreCase("Input")) {
                            ArrayList<ThroughputEstimator> txnEstimators =
                                inputThroughputEstimators.get(server + "/" + streamName);

                            double totalThroughput = 0;

                            if (txnEstimators != null) {
                                for (ThroughputEstimator throughputEstimator : txnEstimators) {
                                    if (throughputEstimator != null) {
                                        totalThroughput += throughputEstimator.computeCurrentThroughput();
                                    }
                                }
                            }

                            chart.updateChart(e.getValue(), System.currentTimeMillis(), totalThroughput);
                        } else if (streamType.equalsIgnoreCase("Output")) {
                            PerfMonValidator validator = outputPerfValidators.get(server + "/" + streamName);
                            if (validator != null) {
                                if (counter.equalsIgnoreCase("Throughput")) {
                                    chart.updateChart(e.getValue(), System.currentTimeMillis(), validator.getCurrentThroughput());
                                } else if (counter.equalsIgnoreCase("Response Time")) {
                                    chart.updateChart(e.getValue(), System.currentTimeMillis(), validator.getCurrentRT());
                                }
                            } else {
                                chart.updateChart(e.getValue(), System.currentTimeMillis(), 0);
                            }
                        }
                    }
                }
            }
        }

        /**
         * A panel containing a JFreechart chart and accessor methods.
         *
         * @author Marcelo R.N. Mendes
         *
         */
        class PerfChartPanel extends JPanel {
            /** serial id .*/
            private static final long serialVersionUID = -8158388572557517686L;

            private HashMap<String, TimeSeries> seriesList;
            private HashMap<String, Double> seriesScales;
            private TimeSeriesCollection dataset;
            private int source;
            static final int REAL_TIME = 0;
            static final int LOG_FILE = 1;

            NumberAxis yAxis;
            XYPlot plot;
            int chartAge;

            /**
             *
             * @param chartTitle	The title of the chart
             * @param seriesList	An initial set of series to be added to chart
             * @param yAxisName		Name of y axis
             * @param yAxisFormat	Number format of y axis
             * @param source		Either real time(0) or log file (1)
             */
            public PerfChartPanel(String chartTitle, String[] seriesList,
                    String yAxisName, DecimalFormat yAxisFormat, int source) {

                this.source = source;
                dataset = new TimeSeriesCollection();

                this.seriesList = new HashMap<String, TimeSeries>();
                this.seriesScales = new HashMap<String, Double>();

                if (seriesList != null) {
                    for (String seriesName : seriesList) {
                        addSeries(seriesName, 1);
                    }
                }

                JFreeChart chart = ChartFactory.createTimeSeriesChart(null, "Time", yAxisName, dataset, false, true, true);
                chart.setTitle(chartTitle);

                plot = chart.getXYPlot();
                plot.setBackgroundPaint(Color.LIGHT_GRAY);
                plot.setDomainGridlinePaint(Color.DARK_GRAY);
                plot.setRangeGridlinePaint(Color.DARK_GRAY);

                yAxis = (NumberAxis) plot.getRangeAxis();
                yAxis.setAutoRange(false);

                yAxis.setLowerBound(0);
                yAxis.setUpperBound(100);
                yAxis.setTickUnit(new NumberTickUnit(10));
                yAxis.setNumberFormatOverride(yAxisFormat);

                if (this.source == REAL_TIME) {
                    DateAxis xAxis = (DateAxis) plot.getDomainAxis();

                    xAxis.setTickUnit(new DateTickUnit(DateTickUnit.SECOND, 10,
                            DateTickUnit.SECOND, 10,
                            DateFormat.getTimeInstance()
                    ));
                }

                ChartPanel chartPanel = new ChartPanel(chart);
                chart.setBackgroundPaint(chartPanel.getBackground());
                chartPanel.setPreferredSize(new Dimension(650, 420));

                add(chartPanel);
            }

            public void clear() {
                seriesList.clear();
                seriesScales.clear();
                dataset.removeAllSeries();
            }

            public TimeSeries addSeries(String seriesName, double seriesScale) {
                TimeSeries timeSeries = new TimeSeries(seriesName, Millisecond.class);
                if (this.source == REAL_TIME) {
                    timeSeries.setMaximumItemAge(60);
                }
                this.seriesList.put(seriesName, timeSeries);
                this.seriesScales.put(seriesName, seriesScale);
                dataset.addSeries(timeSeries);

                return timeSeries;
            }

            public TimeSeries addSeries(String seriesName, Color seriesColor, double seriesScale) {
                TimeSeries timeSeries = addSeries(seriesName, seriesScale);
                for (int i = 0; i < dataset.getSeriesCount(); i++) {
                    if (timeSeries.equals(dataset.getSeries(i))) {
                        changeSeriesColor(i, seriesColor);
                        break;
                    }
                }
                return timeSeries;
            }

            public void removeSeries(Object seriesName) {
                TimeSeries toRemove = this.seriesList.get(seriesName);
                int toRemoveIndex = dataset.indexOf(toRemove);

                for (int i = toRemoveIndex; i < dataset.getSeriesCount() - 1; i++) {
                    changeSeriesColor(i, getSeriesColor(i + 1));
                }

                dataset.removeSeries(toRemoveIndex);
                this.seriesList.remove(seriesName);
                this.seriesScales.remove(seriesName);
            }

            public void setYAxisRange(double min, double max) {
                yAxis.setLowerBound(min);
                yAxis.setUpperBound(max);
            }

            /**
             * Adds a new data point to the series or updates an existing one.
             *
             * @param seriesName	The series to be updated
             * @param timestamp		The point in the X axis
             * @param perfValue		The new value in the Y axis
             */
            public void updateChart(String seriesName, long timestamp, double perfValue) {
                TimeSeries timeSeries = this.seriesList.get(seriesName);
                Double scale = this.seriesScales.get(seriesName);
                if (timeSeries != null && scale != null) {
                    timeSeries.addOrUpdate(new Second(new Date(timestamp)), perfValue * scale);
                }
            }

            public void updateChart(TimeSeries series, long timestamp, double perfValue) {
                Double scale = this.seriesScales.get(series.getKey());
                if (scale != null) {
                    series.addOrUpdate(new Second(new Date(timestamp)), perfValue * scale);
                } else {
                    System.err.println("Could not update " + series.getKey());
                }
            }

            public void changeSeriesScale(String seriesName, double scale) {
                TimeSeries timeSeries = this.seriesList.get(seriesName);
                Double oldScale = this.seriesScales.get(seriesName);
                if (timeSeries != null && oldScale != null) {
                    //updates scale
                    this.seriesScales.put(seriesName, scale);
                    // updates old itens
                    TimeSeriesDataItem item;
                    for (Object objItem : timeSeries.getItems()) {
                        item = (TimeSeriesDataItem) objItem;
                        timeSeries.addOrUpdate(item.getPeriod(), item.getValue().doubleValue() * scale / oldScale);
                    }
                }
            }

            public void changeSeriesColor(int seriesIndex, Color newColor) {
                XYItemRenderer renderer = plot.getRenderer();
                renderer.setSeriesPaint(seriesIndex, newColor);
            }

            private Color getSeriesColor(int seriesIndex) {
                XYItemRenderer renderer = plot.getRenderer();
                return (Color) renderer.getSeriesPaint(seriesIndex);
            }

            /**
             * Defines if a series must be drawn or not.
             *
             * @param seriesIndex	The index of the series
             * @param visible		Visible (true), Hidden (false)
             */
            public void setSeriesVisible(int seriesIndex, boolean visible) {
                plot.getRenderer().setSeriesVisible(seriesIndex, visible);
            }
        }

        class CounterPanel extends javax.swing.JPanel {
            /** serial id. */
            private static final long serialVersionUID = -8102495513812210261L;

            private TreeMap<String, HashSet<Stream>> serversStreams;

            private javax.swing.DefaultComboBoxModel outputModel = new javax.swing.DefaultComboBoxModel(new String[] { "Response Time", "Throughput" });
            private javax.swing.DefaultComboBoxModel inputModel = new javax.swing.DefaultComboBoxModel(new String[] { "Throughput" });
            private javax.swing.DefaultComboBoxModel logModel =
                new javax.swing.DefaultComboBoxModel(new String[] {
                        "Avg Throughput", "Min Throughput",
                        "Max Throughput", "Last Throughput",
                        "Avg Response Time", "Min Response Time",
                        "Max Response Time", "Last Response Time",
                        "Stdev Response Time"
                });
            private final Color[] seriesColors =
                new Color[] {Color.BLUE, Color.RED, new Color(0,200,0),
                    Color.BLACK, Color.YELLOW, Color.ORANGE,
                    Color.CYAN, Color.MAGENTA, Color.WHITE};

            /** Creates new form CounterPanel */
            public CounterPanel(TreeMap<String, HashSet<Stream>> serversStreams) {
                initComponents();
                this.serversStreams = serversStreams;
                setServerList(serversStreams);
            }

            private void setServerList(TreeMap<String, HashSet<Stream>> serversStreams) {
                String[] serverList = new String[serversStreams.size()];
                int i = 0;
                for (String serverAddress : serversStreams.keySet()) {
                    serverList[i] = serverAddress;
                    i++;
                }
                serverCombo.setModel(new javax.swing.DefaultComboBoxModel(serverList));
                if (streamsCombo.getItemCount() > 0) {
                    streamsCombo.setSelectedIndex(-1);
                    streamsCombo.setSelectedIndex(0);
                }
            }

            /** This method is called from within the constructor to
             * initialize the form.
             * WARNING: Do NOT modify this code. The content of this method is
             * always regenerated by the Form Editor.
             */
            // <editor-fold defaultstate="collapsed" desc="Generated Code">
            @SuppressWarnings("serial")
            private void initComponents() {

                streamsLabel = new javax.swing.JLabel();
                streamsCombo = new javax.swing.JComboBox();
                jScrollPane1 = new javax.swing.JScrollPane();
                streamsList = new javax.swing.JList();
                serverLabel = new javax.swing.JLabel();
                serverCombo = new javax.swing.JComboBox();
                counterLabel = new javax.swing.JLabel();
                counterCombo = new javax.swing.JComboBox();
                colorLabel = new javax.swing.JLabel();
                colorPanel = new javax.swing.JPanel();
                scaleLabel = new javax.swing.JLabel();
                scaleCombo = new javax.swing.JComboBox();
                addBtn = new javax.swing.JButton();

                setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Available Counters"));

                streamsLabel.setText("Streams");

                streamsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Input", "Output"}));

                streamsList.setModel(new javax.swing.AbstractListModel() {
                    String[] strings = {};
                    public int getSize() { return strings.length; }
                    public Object getElementAt(int i) { return strings[i]; }
                });
                streamsList.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
                jScrollPane1.setViewportView(streamsList);

                serverLabel.setText("Server");

                counterLabel.setText("Counter");

                colorLabel.setText("Color");

                colorPanel.setBackground(java.awt.Color.blue);
                colorPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        colorPanelMouseClicked(evt);
                    }
                });

                javax.swing.GroupLayout colorPanelLayout = new javax.swing.GroupLayout(colorPanel);
                colorPanel.setLayout(colorPanelLayout);
                colorPanelLayout.setHorizontalGroup(
                        colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 128, Short.MAX_VALUE)
                );
                colorPanelLayout.setVerticalGroup(
                        colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 19, Short.MAX_VALUE)
                );

                scaleLabel.setText("Scale");

                scaleCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0.000001", "0.00001", "0.0001", "0.001", "0.01", "0.1", "1", "10", "100", "1000", "10000", "100000", "1000000" }));
                scaleCombo.setSelectedIndex(6);

                addBtn.setText("<< Add");

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
                this.setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(serverLabel)
                                        .addComponent(serverCombo, 0, 128, Short.MAX_VALUE)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                                        .addComponent(streamsCombo, 0, 128, Short.MAX_VALUE)
                                        .addComponent(streamsLabel)
                                        .addComponent(counterLabel)
                                        .addComponent(counterCombo, 0, 128, Short.MAX_VALUE)
                                        .addComponent(colorLabel)
                                        .addComponent(colorPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(scaleLabel)
                                        .addComponent(scaleCombo, 0, 128, Short.MAX_VALUE)
                                        .addComponent(addBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addContainerGap())
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(serverLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(serverCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(streamsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(streamsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(counterLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(counterCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(colorLabel)
                                .addGap(2, 2, 2)
                                .addComponent(colorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(scaleLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scaleCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(29, 29, 29)
                                .addComponent(addBtn)
                                .addContainerGap(37, Short.MAX_VALUE))
                );


                streamsCombo.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        streamsComboActionPerformed();
                    }
                });

                serverCombo.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        serverComboActionPerformed(evt);
                    }
                });

                addBtn.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        addBtnActionPerformed(evt);
                    }
                });

            }// </editor-fold>

            private void colorPanelMouseClicked(java.awt.event.MouseEvent evt) {
                Color c = JColorChooser.showDialog(null, "Counter Color", colorPanel.getBackground());
                if (c != null) {
                    colorPanel.setBackground(c);
                }
            }

            private void serverComboActionPerformed(java.awt.event.ActionEvent evt) {
                streamsComboActionPerformed();
            }

            private void streamsComboActionPerformed() {
                if (!serversStreams.isEmpty()) {
                    Set<Stream> streams = serversStreams.get(serverCombo.getSelectedItem());
                    ArrayList<String> streamList = new ArrayList<String>();

                    if (streams != null && !streams.isEmpty()) {
                        for (Stream stream : streams) {
                            if (streamsCombo.getSelectedIndex() == stream.type) {
                                streamList.add(stream.name);
                            }
                        }

                        streamsList.setListData(streamList.toArray());

                        if (!streamList.isEmpty()) {
                            streamsList.setSelectedIndex(0);
                        }
                    }

                    if (!seriesData.isEmpty()) {
                        counterCombo.setModel(logModel);
                    } else {
                        if (streamsCombo.getSelectedIndex() == Stream.OUTPUT) {
                            counterCombo.setModel(outputModel);
                        } else {
                            counterCombo.setModel(inputModel);
                        }
                    }
                }

            }

            private void addBtnActionPerformed(java.awt.event.ActionEvent evt) {
                synchronized (graphSeries) {
                    if (streamsList.getSelectedValue() != null) {
                        String key = serverCombo.getSelectedItem() + Globals.CSV_SEPARATOR
                                   + streamsList.getSelectedValue() + Globals.CSV_SEPARATOR
                                   + streamsCombo.getSelectedItem() + Globals.CSV_SEPARATOR
                                   + (String) counterCombo.getSelectedItem();
                        //checks if the counter has already been inserted
                        if (graphSeries.containsKey(key)) {
                            JOptionPane.showMessageDialog(null, "This counter has already been added.");
                            return;
                        } else {
                            TimeSeries addedSeries = chart.addSeries(key, colorPanel.getBackground(), Double.parseDouble((String) scaleCombo.getSelectedItem()));
                            // Add series data
                            if (seriesData != null && !seriesData.isEmpty() && seriesData.get(key) != null) {
                                TreeMap<Long, Double> data = seriesData.get(key);
                                for (Entry<Long, Double> e : data.entrySet()) {
                                    chart.updateChart(addedSeries, e.getKey(), e.getValue());
                                }
                            }

                            graphSeries.put(key, addedSeries);
                            DefaultTableModel model = (DefaultTableModel) countersTable.getModel();
                            model.addRow(new Object[] {new Boolean(true), colorPanel.getBackground(), scaleCombo.getSelectedItem(), serverCombo.getSelectedItem(), streamsList.getSelectedValue()+" (" + streamsCombo.getSelectedItem() + ")", counterCombo.getSelectedItem()}) ;

                            colorPanel.setBackground(seriesColors[model.getRowCount() % seriesColors.length]);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Select a stream from the available list.");
                    }
                }
            }


            // Variables declaration - do not modify
            private javax.swing.JButton addBtn;
            private javax.swing.JLabel colorLabel;
            private javax.swing.JPanel colorPanel;
            private javax.swing.JComboBox counterCombo;
            private javax.swing.JLabel counterLabel;
            private javax.swing.JScrollPane jScrollPane1;
            private javax.swing.JComboBox scaleCombo;
            private javax.swing.JLabel scaleLabel;
            private javax.swing.JComboBox serverCombo;
            private javax.swing.JLabel serverLabel;
            private javax.swing.JComboBox streamsCombo;
            private javax.swing.JLabel streamsLabel;
            private javax.swing.JList streamsList;
            // End of variables declaration

        }

        public class ColorRenderer extends JLabel
        implements TableCellRenderer {
            /** serial id. */
            private static final long serialVersionUID = 3359238534076747478L;

            Border unselectedBorder = null;
            Border selectedBorder = null;
            boolean isBordered = true;

            public ColorRenderer(boolean isBordered) {
                this.isBordered = isBordered;
                setOpaque(true); //MUST do this for background to show up.
            }

            public Component getTableCellRendererComponent(
                    JTable table, Object color,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {
                Color newColor = (Color) color;
                setBackground(newColor);
                if (isBordered) {
                    if (isSelected) {
                        if (selectedBorder == null) {
                            selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
                        }
                        setBorder(selectedBorder);
                    } else {
                        if (unselectedBorder == null) {
                            unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
                        }
                        setBorder(unselectedBorder);
                    }
                }

                return this;
            }
        }
    }
    //=================================== END of GraphPanel internal class ========================================

    public static void main(String[] args) throws UnknownHostException {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PerformanceMonitor();
            }
        });
    }
}

