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


package pt.uc.dei.fincos.perfmon.gui;

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
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
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
import pt.uc.dei.fincos.controller.gui.Controller_GUI;
import pt.uc.dei.fincos.controller.gui.PopupListener;
import pt.uc.dei.fincos.driver.DriverRemoteFunctions;
import pt.uc.dei.fincos.perfmon.DriverPerfStats;
import pt.uc.dei.fincos.perfmon.OutStreamCounters;
import pt.uc.dei.fincos.perfmon.PerformanceStats;
import pt.uc.dei.fincos.perfmon.SinkPerfStats;
import pt.uc.dei.fincos.perfmon.Stream;
import pt.uc.dei.fincos.sink.SinkRemoteFunctions;


/**
 *
 * FINCoS Performance Monitor main class.
 * Computes performance metrics in real
 * time or from log files produced by Sinks.
 *
 * @author  Marcelo R.N. Mendes
 *
 */
public final class PerformanceMonitor extends JFrame {

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

    /** For each combination of Connection/output stream, there is a set of performance counters. */
    private HashMap<String, OutStreamCounters> outputStreamStats;

    /** Background thread that periodically refreshes GUI when the Pefmon is in reatime mode. */
    private Timer guiRefresher;

    /** The last time the GUI has been refreshed. */
    private long guiLastRefresh;

    //---------------------------------------------------------------------------------------------


    //------------------------------------- Sink log file(s) --------------------------------------
    /** Path(s) of the log file(s). */
    private String[] sinklogFilesPaths;

    /** Start of measurement interval. */
    private long miStart;

    /** End of measurement interval. */
    private long miEnd;
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
        addListeners();
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
        this.dataSource = REALTIME;
        initGUI();
        loadForRealTime();
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
                    guiRefresher.stop();
                    guiRefresher = null;
                    for (DriverRemoteFunctions dr : remoteDrivers.values()) {
                        try {
                            if (dr != null) {
                                dr.setPerfTracing(false);
                            }
                        } catch (RemoteException e1) {
                            System.err.println(e1.getMessage());
                        }
                    }
                    Controller_GUI.getInstance().closePerfmon();
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

    private void addListeners() {
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
        System.out.println(msg);
    }

    /**
     * Loads the FINCoS Performance Monitor tool for real time monitoring.
     *
     * @param rtMode  Either END_TO_END, ADAPTER, or NO_RT
     */
    private void loadForRealTime() {
        // Initializes input stream stats
        inputStreamsStats = new HashMap<String, Double>();

        // Initializes output stream stats
        outputStreamStats = new HashMap<String, OutStreamCounters>();
        if (this.sinks != null) {
            for (SinkConfig sink : this.sinks) {
                for (String outputStream : sink.getOutputStreamList()) {
                    String key = sink.getConnection().getAlias() + "/" + outputStream;
                    outputStreamStats.put(key, null);
                }
            }
        }

        // Load stats panel
        statsTables = new ArrayList<JTable>();
        loadStatsPanelOnline(getServerList());

        // Load graphs panel
        graphsPanel.loadForRealTimeMonitoring(drivers, sinks);

        // Enables performance monitoring at the Drivers
        for (Entry<DriverConfig, DriverRemoteFunctions> entry: remoteDrivers.entrySet()) {
            DriverRemoteFunctions remoteDr = entry.getValue();
            if (remoteDr != null) {
                try {
                    remoteDr.setPerfTracing(true);
                } catch (RemoteException e) {
                    System.err.println("Could not enable performance monitoring at \""
                            + entry.getKey().getAlias() + "\" (" + e.getMessage() + ").");
                }
            }
        }

        // Enables performance monitoring at the Sinks
        for (Entry<SinkConfig, SinkRemoteFunctions> entry: remoteSinks.entrySet()) {
            SinkRemoteFunctions remoteSink = entry.getValue();
            if (remoteSink != null) {
                try {
                    remoteSink.setPerfTracing(true);
                } catch (RemoteException e) {
                    System.err.println("Could not enable performance monitoring at \""
                            + entry.getKey().getAlias() + "\" (" + e.getMessage() + ").");
                }
            }
        }

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
     * @param logFiles      log files to be preocessed
     * @param miStart       start of measurement interval
     * @param miEnd         end of measurement interval
     */
    public void loadForSinkLogFile(Set<PerformanceStats> statsSeries,
            String[] logFiles, long miStart, long miEnd) {
        if (guiRefresher != null) {
            guiRefresher.stop();
            guiRefresher = null;
        }

        this.dataSource = SINK_LOG_FILES;

        this.sinklogFilesPaths = logFiles;
        this.miStart = miStart;
        this.miEnd = miEnd;

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
        String[] columns;
        if (dataSource != REALTIME) {
            columns = new String [] {"Key", "Query",
                    "Avg Throughput", "Min Throughput", "Max Throughput", "Last Throughput",
                    "Avg RT (ms)", "Min RT (ms)", "Max RT (ms)", "Last RT (ms)",
                    "Stdev RT (ms)"};
        } else {
            columns = new String [] {"Key", "Query",
                    "Throughput", "Avg RT (ms)", "Min RT (ms)", "Max RT (ms)",
                    "Last RT (ms)", "Stdev RT (ms)"};
        }
        DefaultTableModel model = new DefaultTableModel(columns, 0)
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

        if (dataSource != REALTIME) {
            // Hides Min Throughput, Max throughput and Stdev RT columns
            ((TableColumnExt) statsTable.getColumnModel().getColumn(statsTable.getColumnModel().getColumnIndex("Min Throughput"))).setVisible(false);
            ((TableColumnExt) statsTable.getColumnModel().getColumn(statsTable.getColumnModel().getColumnIndex("Max Throughput"))).setVisible(false);
            ((TableColumnExt) statsTable.getColumnModel().getColumn(statsTable.getColumnModel().getColumnIndex("Stdev RT (ms)"))).setVisible(false);
        }

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
     * Initializes the stats panel for Log files.
     * Adds one table for each server.
     * Each table contains one line for every output stream.
     *
     * @param serversStreams    a mapping server -> list of streams
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
                String keyPrefix = server + Globals.CSV_DELIMITER + stream.name + Globals.CSV_DELIMITER;
                if (stream.type == Stream.INPUT) {
                    keyPrefix += "Input" + Globals.CSV_DELIMITER;
                } else {
                    keyPrefix += "Output" + Globals.CSV_DELIMITER;
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

            for (String serverAndOutput: outputStreamStats.keySet()) {
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

    private void refreshGUI() {
        long now = System.currentTimeMillis();
        if (guiLastRefresh == 0) {
            guiLastRefresh = now - Globals.DEFAULT_GUI_REFRESH_RATE * 1000;
        }
        long interval = now - guiLastRefresh; // in milliseconds
        // For each Driver
        for (DriverConfig dr: drivers) {
            DriverRemoteFunctions remoteDr = remoteDrivers.get(dr);
            if (remoteDr != null) {
                try {
                    DriverPerfStats stats = remoteDr.getPerfStats();
                    // Computes throughput per stream
                    for (Entry<String, Integer> e : stats.getStreamStats().entrySet()) {
                        // The throughput of this stream on this Driver
                        String streamName = e.getKey();
                        Integer sentCount = e.getValue();
                        double throughput = interval != 0 ? 1000.0 * sentCount / interval : 0;
                        String streamID = dr.getConnection().getAlias() + "/" + streamName;
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
        }

        // For each Sink
        for (SinkConfig sink: sinks) {
            SinkRemoteFunctions remoteSink = remoteSinks.get(sink);
            if (remoteSink != null) {
                try {
                    SinkPerfStats stats = remoteSink.getPerfStats();
                    // Computes throughput per stream
                    for (Entry<String, OutStreamCounters> e : stats.getStreamStats().entrySet()) {
                        String streamName = e.getKey();
                        OutStreamCounters counters = e.getValue();
                        counters.setThroughput(interval != 0 ? 1E3 * counters.getLastCount() / interval : 0);
                        String streamID = sink.getConnection().getAlias() + "/" + streamName;
                        outputStreamStats.put(streamID, counters);
                    }
                } catch (RemoteException e) {
                    System.err.println(e.getMessage());
                }
            }
        }

        // Updates UI
        graphsPanel.refreshGraphs();
        refreshStatsTable();

        // Resets stats
        inputStreamsStats.clear();

        guiLastRefresh = now;
    }

    private void refreshStatsTable() {
        if (this.statsTables != null) {
            DefaultTableModel model;
            OutStreamCounters streamStats;

            for (JTable table : statsTables) {
                String outputName;
                model = (DefaultTableModel) table.getModel();
                for (int i = 0; i < table.getRowCount(); i++) {
                    outputName = (String) model.getValueAt(i, 0);
                    streamStats = outputStreamStats.get(outputName);
                    double minRT;
                    if (streamStats != null) {
                        model.setValueAt(Globals.FLOAT_FORMAT_2.format(streamStats.getThroughput()), i, 2);
                        model.setValueAt(Globals.FLOAT_FORMAT_3.format(streamStats.getAvgRT()), i, 3);
                        minRT = streamStats.getMinRT();
                        if (minRT != Double.MAX_VALUE) {
                            model.setValueAt(Globals.FLOAT_FORMAT_3.format(minRT), i, 4);
                        }
                        model.setValueAt(Globals.FLOAT_FORMAT_3.format(streamStats.getMaxRT()), i, 5);
                        model.setValueAt(Globals.FLOAT_FORMAT_3.format(streamStats.getLastRT()), i, 6);
                        model.setValueAt(Globals.FLOAT_FORMAT_3.format(streamStats.getStdevRT()), i, 7);
                    } else {
                        model.setValueAt(Globals.FLOAT_FORMAT_2.format(0), i, 2);
                        model.setValueAt(Globals.FLOAT_FORMAT_3.format(0), i, 3);
                        model.setValueAt(Globals.FLOAT_FORMAT_3.format(0), i, 4);
                        model.setValueAt(Globals.FLOAT_FORMAT_3.format(0), i, 5);
                        model.setValueAt(Globals.FLOAT_FORMAT_3.format(0), i, 6);
                        model.setValueAt(Globals.FLOAT_FORMAT_3.format(0), i, 7);
                    }
                }
            }
        }

    }

    /**
     *
     * @param connection    the connection alias
     * @param inputStream   the stream name
     * @return              the throughput of a given input stream on a given connection
     */
    public Double getThroughputFor(String connection, String inputStream) {
        Double throughput = inputStreamsStats.get(connection + "/" + inputStream);
        if (throughput != null) {
            return throughput;
        }
        return 0.0;
    }

    /**
     *
     * @param connection     the connection alias
     * @param outputstream   the stream name
     * @return               the throughput for a given output stream on a given connection
     */
    public Double getOutThroughputFor(String connection, String outputstream) {
        OutStreamCounters counters = outputStreamStats.get(connection + "/" + outputstream);
        if (counters != null) {
           return counters.getThroughput();
        }

        return 0.0;
    }

    /**
    *
    * @param connection     the connection alias
    * @param outputstream   the stream name
    * @return               the response time for a given output stream on a given connection
    */
   public Double getResponseTimeFor(String connection, String outputstream) {
       OutStreamCounters counters = outputStreamStats.get(connection + "/" + outputstream);
       if (counters != null) {
          return counters.getLastRT();
       }

       return 0.0;
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
                list.add(dr.getConnection().getAlias());
            }
        }

        if (this.sinks != null) {
            for (SinkConfig sink : this.sinks) {
                list.add(sink.getConnection().getAlias());
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

