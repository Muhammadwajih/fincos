package pt.uc.dei.fincos.validation;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;

import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.controller.DriverConfig;
import pt.uc.dei.fincos.controller.SinkConfig;
import pt.uc.dei.fincos.controller.gui.PopupListener;
import pt.uc.dei.fincos.driver.DriverRemoteFunctions;
import pt.uc.dei.fincos.perfmon.DriverPerfStats;
import pt.uc.dei.fincos.perfmon.gui.GraphPanel;
import pt.uc.dei.fincos.perfmon.gui.SourceDialog;
import pt.uc.dei.fincos.sink.SinkRemoteFunctions;
import pt.uc.dei.fincos.validation.realtime.PerfMonValidator;
import pt.uc.dei.fincos.validation.realtime.ThroughputEstimator;


/**
 *
 * FINCoS Performance Monitor main class. Computes performance metrics in real time
 * or from log files produced by Sinks.
 *
 * @author Marcelo R.N. Mendes
 *
 */
public class PerformanceMonitor extends JFrame {

    /** serial id. */
    private static final long serialVersionUID = 7919137484290346256L;

    /** The source of performance counters used by the Pefmon app. */
    private int dataSource = -1;

    /** Performance data is gathered online, from running Drivers and Sinks. */
    private static final int REALTIME = 0;

    /** Performance data is obtained from log files produced by Sinks, after test completion.*/
    private static final int SINK_LOG_FILES = 1;

    /** Performance data was previously collected by the Perfmon itself. */
    private static final int PERFMON_LOG_FILES = 2;


    //----------------------------------------- Realtime ------------------------------------------
    /** List of configured Drivers . */
    private DriverConfig[] drivers;

    /** List of configured Sinks. */
    private SinkConfig[] sinks;

    /** RMI interfaces with Drivers. */
    private HashMap<DriverConfig, DriverRemoteFunctions> remoteDrivers;

    /** RMI interfaces with Sinks. */
    private HashMap<SinkConfig, SinkRemoteFunctions> remoteSinks;

    /** For each combination of Connection/input stream, there is a performance counter. */
    private HashMap<String, Double> inputStreamsStats;

    /** For each combination of query/CEP server, there will be a specific performance validator. */
    private HashMap<String, PerfMonValidator> outputPerfValidators;

    /** Background thread that periodically refreshes GUI when the Pefmon is in reatime mode. */
    Timer guiRefresher;

    /** Defines when throughput is computed. */
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
    //---------------------------------------------------------------------------------------------


    //------------------------------------- Sink log file(s) --------------------------------------
    /** Path(s) of the log file(s). */
    String[] sinklogFilesPaths;

    /** Start of measurement interval. */
    long miStart;

    /** End of measurement interval. */
    long miEnd;
    //---------------------------------------------------------------------------------------------


    //-------------------------------------------- GUI --------------------------------------------
    JTabbedPane tabbedPanel;
    JPanel statsPanel;
    GraphPanel graphsPanel;
    JScrollPane statsScroll, graphsScroll;
    JXTable statsTable;
    ArrayList <JTable> statsTables;
    Font statsTableFont = new Font("arial", Font.PLAIN, 14);
    //---------------------------------------------------------------------------------------------


    /**
     * Perfmon is opened as a stand-alone application for offline processing of log files.
     */
    public PerformanceMonitor() {
        super("FINCoS Performance Monitor (Offline mode).");
        this.setIconImage(Toolkit.getDefaultToolkit().getImage("imgs/perfmon.png"));
        initGUI();
        addListerners();
        new SourceDialog(this);
    }


    /**
     * Perfmon is created inside the Controller application, for real-time monitoring.
     *
     * @param drivers       List of configured Drivers
     * @param sinks         List of configured Sinks
     * @param remoteDrivers RMI interfaces with Drivers
     * @param remoteSinks   RMI interfaces with Sinks
     */
    public PerformanceMonitor(DriverConfig[] drivers, SinkConfig[] sinks,
            HashMap<DriverConfig, DriverRemoteFunctions> remoteDrivers,
            HashMap<SinkConfig, SinkRemoteFunctions> remoteSinks) {
        super("FINCoS Performance Monitor");
        this.drivers = drivers;
        this.sinks = sinks;
        this.remoteDrivers = remoteDrivers;
        this.remoteSinks = remoteSinks;
        this.setIconImage(Toolkit.getDefaultToolkit().getImage("imgs/perfmon.png"));
        initGUI();
    }

    private void initGUI() {
        Container c = this.getContentPane();
        c.setLayout(new BorderLayout());

        // Stats Panel
        statsPanel = new JPanel();
        statsScroll = new JScrollPane(statsPanel);

        // GraphsPanel
        graphsPanel = new GraphPanel(this);
        graphsPanel.setLayout(new BorderLayout());
        graphsScroll = new JScrollPane(graphsPanel);

        tabbedPanel = new JTabbedPane();
        tabbedPanel.addTab("Stats", statsScroll);
        tabbedPanel.add("Graph", graphsScroll);
        c.add(tabbedPanel, BorderLayout.CENTER);

        if (this.dataSource == REALTIME) { // Realtime processing
            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            // Disables performance tracing on Drivers and Sinks
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    for (DriverRemoteFunctions dr : remoteDrivers.values()) {
                        try {
                            dr.setPerfTracing(false);
                        } catch (RemoteException e1) {
                            System.err.println(e1.getMessage());
                        }
                    }
                }
            });
        } else { // Offline processing
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        }

        this.setSize(850, 625);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);
    }

    private void addListerners() {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem dataSrcMenu = new JMenuItem("Source...");
        popup.add(dataSrcMenu);
        dataSrcMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSourceDialog();
            }
        });
        statsPanel.addMouseListener(new PopupListener(popup));
        graphsPanel.addMouseListener(new PopupListener(popup));
        tabbedPanel.addMouseListener(new PopupListener(popup));
    }

    private void showSourceDialog() {
        new SourceDialog(this);
    }

    /**
     * Shows a message on the status bar of the FINCoS Perfmon application.
     *
     * @param msg   the message to be exhibited
     */
    public void showInfo(String msg) {
        System.err.println("Not implemented yet.");
    }

    /**
     * Loads the FINCoS Performance Monitor tool for real time monitoring.
     *
     * @param rtMeasurementMode  Either END_TO_END_RT_MILLIS, ADAPTER_RT_NANOS, or NO_RT
     */
    private void loadForRealTime(int rtMeasurementMode) {
        // Initializes Validation mapping stream -> Validator
        initializeValidators();

        // Initializes Throughput estimators for Input Streams
        inputThroughputEstimators = new HashMap<String, ArrayList<ThroughputEstimator>>();

        // Load stats panel
        statsTables = new ArrayList<JTable>();
        loadStatsPanelOnline(getServerList());

        // Load graphs panel
        graphsPanel.loadForRealTimeMonitoring(drivers, sinks);

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
     *
     * Loads the FINCoS Performance Monitor tool for offline validation
     * (from Sink log files).
     *
     * @param statsSeries   performance stats
     * @param logFiles
     * @param miStart
     * @param miEnd
     */
    public void loadForSinkLogFile(Set<PerformanceStats> statsSeries, String[] logFiles, long miStart, long miEnd) {
        if (guiRefresher != null) {
            guiRefresher.stop();
            guiRefresher = null;
        }

        this.dataSource = SINK_LOG_FILES;

        this.sinklogFilesPaths = logFiles;
        this.miStart = miStart;
        this.miEnd = miEnd;

        // Load stats panel
        statsTables = new ArrayList<JTable>();

        // Load graphs panel
        graphsPanel.loadForLogFiles(statsSeries);

        // Load stats panel
        statsTables = new ArrayList<JTable>();
        loadStatsPanelOffline(graphsPanel.getServersStreams());
    }

    /**
     * Loads the FINCoS Performance Monitor tool for showing
     * performance stats from a previously generated performance
     * log file.
     *
     * @param statsSeries   performance stats
     *
     */
    public void loadForPerfMonLogFile(Set<PerformanceStats> statsSeries)  {
        if (guiRefresher != null) {
            guiRefresher.stop();
            guiRefresher = null;
        }

        this.dataSource = PERFMON_LOG_FILES;

        // Load graphs panel
        graphsPanel.loadForLogFiles(statsSeries);

        // Load stats panel
        statsTables = new ArrayList<JTable>();
        loadStatsPanelOffline(graphsPanel.getServersStreams());
    }

    @SuppressWarnings("serial")
    private JTable addStatsTable(String connectionAlias) {
        JPanel serverPanel;

        serverPanel = new JPanel(new BorderLayout());

        serverPanel.setBorder(BorderFactory.createTitledBorder(connectionAlias));
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

        if (this.dataSource == SINK_LOG_FILES) {
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
            HistogramPanel histPanel = new HistogramPanel(sinklogFilesPaths, streamName, miStart, miEnd);
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
    private void loadStatsPanelOffline(TreeMap<String, HashSet<Stream>> serversStreams) {
        statsPanel.removeAll();
        //statsPanel.repaint();
        //statsPanel.revalidate();
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

                queryStats = graphsPanel.getSeriesData(keyPrefix + "Avg Throughput");
                if (queryStats != null) {
                    avgThroughput = queryStats.lastEntry().getValue();
                }

                queryStats = graphsPanel.getSeriesData(keyPrefix + "Min Throughput");
                if (queryStats != null) {
                    minThroughput = queryStats.lastEntry().getValue();
                }

                queryStats = graphsPanel.getSeriesData(keyPrefix + "Max Throughput");
                if (queryStats != null) {
                    maxThroughput = queryStats.lastEntry().getValue();
                }

                queryStats = graphsPanel.getSeriesData(keyPrefix + "Last Throughput");
                if (queryStats != null) {
                    lastThroughput = queryStats.lastEntry().getValue();
                }

                queryStats = graphsPanel.getSeriesData(keyPrefix + "Avg Response Time");
                if (queryStats != null) {
                    avgRT = queryStats.lastEntry().getValue();
                }

                queryStats = graphsPanel.getSeriesData(keyPrefix + "Min Response Time");
                if (queryStats != null) {
                    minRT = queryStats.lastEntry().getValue();
                }

                queryStats = graphsPanel.getSeriesData(keyPrefix  + "Max Response Time");
                if (queryStats != null) {
                    maxRT = queryStats.lastEntry().getValue();
                }

                queryStats = graphsPanel.getSeriesData(keyPrefix + "Last Response Time");
                if (queryStats != null) {
                    lastRT = queryStats.lastEntry().getValue();
                }

                queryStats = graphsPanel.getSeriesData(keyPrefix + "Stdev Response Time");
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
     * Initializes the stats panel for real-time monitoring.
     * Adds one table for each server (i.e. a connection on the test setup file).
     * Each table contains one line for every output stream.
     *
     * @param serverList    the list of connections used in the test setup
     */
    private void loadStatsPanelOnline(String[] serverList) {
        statsPanel.removeAll();
        statsTables.clear();
        statsPanel.setLayout(new GridLayout(serverList.length, 1));

        for (String connAlias : serverList) {
            String server, outputName;

            DefaultTableModel model = (DefaultTableModel) addStatsTable(connAlias).getModel();

            for (String serverAndOutput: outputPerfValidators.keySet()) {
                server = serverAndOutput.split("/")[0];
                outputName = serverAndOutput.split("/")[1];
                if (server.equals(connAlias)) {
                    model.addRow(new Object[] {serverAndOutput, outputName,
                            "0.0", "0.0", "0.0", "0.0",
                            "0.0", "0.0", "0.0", "0.0",
                    "0.0"});
                }
            }
        }
        statsPanel.revalidate();
    }

    private void refreshGUI2() {
        // For each Driver
        for (DriverConfig dr: drivers) {
            DriverRemoteFunctions remoteDr = remoteDrivers.get(dr);
            try {
                DriverPerfStats stats = remoteDr.getPerfStats();
                long interval = stats.end - stats.start;
                // Computes throughput per stream
                for (Entry<String, Integer> e : stats.streamStats.entrySet()) {
                    // The throughput of this stream on this Driver
                    String streamName = e.getKey();
                    Integer sentCount = e.getValue();
                    double throughput = 1.0 * sentCount / interval;
                    String streamID = dr.getConnection().alias + "/" + streamName;
                    // The throughput of this stream for *all* Drivers
                    Double totalThroughput = inputStreamsStats.get(streamID);
                    if (totalThroughput == null) {
                        inputStreamsStats.put(streamID, throughput);
                    } else {
                        inputStreamsStats.put(streamID, totalThroughput + throughput);
                    }
                }
            } catch (RemoteException e) {
                System.err.println(e.getMessage());
            }
        }
        // Updates UI
        graphsPanel.refreshGraphs();

        // Resets stats
        inputStreamsStats.clear();
    }

    /**
     *
     * @param connection    the connection alias
     * @param inputStream   the stream name
     * @return              the throughput of a given input stream on a given connection
     */
    public Double getThroughputFor(String connection, String inputStream) {
        return inputStreamsStats.get(connection + "/" + inputStream);
    }

    /**
     *
     * @param connection     the connection alias
     * @param outputstream   the stream name
     * @return               the throughput and response time for a given output stream on a given connection
     */
    public Double[] getStatsFor(String connection, String outputstream) {
        throw new RuntimeException("Not implemented.");
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

    /**
     * Initializes the list of Validators.
     */
    private void initializeValidators() {
        outputPerfValidators = new HashMap<String, PerfMonValidator>();

        String key;

        if (this.sinks != null) {
            for (SinkConfig sink : this.sinks) {
                for (String outputStream : sink.getOutputStreamList()) {
                    key = sink.getConnection().alias + "/" + outputStream;
                    outputPerfValidators.put(key, null);
                }

            }
        }
    }


    /**
     * Retrieves the list of all servers (i.e. unique connections) used in the test
     * from the drivers and sinks lists.
     *
     * @return	An array with the server list
     */
    private String[] getServerList() {
        String[] ret;
        Set<String> list = new HashSet<String>();

        if (this.drivers != null) {
            for (DriverConfig dr : this.drivers) {
                list.add(dr.getConnection().alias);
            }
        }

        if (this.sinks != null) {
            for (SinkConfig sink : this.sinks) {
                list.add(sink.getConnection().alias);
            }
        }

        ret = new String[list.size()];
        list.toArray(ret);

        return ret;
    }

    public static void main(String[] args) throws UnknownHostException {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PerformanceMonitor();
            }
        });
    }
}

