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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.jfree.data.time.TimeSeries;

import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.controller.DriverConfig;
import pt.uc.dei.fincos.controller.SinkConfig;
import pt.uc.dei.fincos.driver.ExternalFileWorkloadPhase;
import pt.uc.dei.fincos.driver.SyntheticWorkloadPhase;
import pt.uc.dei.fincos.driver.WorkloadPhase;
import pt.uc.dei.fincos.perfmon.PerformanceStats;
import pt.uc.dei.fincos.perfmon.Stream;

/**
 *
 * A panel containing performance graphs and counters
 * Used for showing performance stats at the FINCoS PerfMon application.
 *
 * @author  Marcelo R.N. Mendes
 *
 */
@SuppressWarnings({"serial", "rawtypes", "unchecked" })
public final class GraphPanel extends JPanel {
    /** serial id. */
    private static final long serialVersionUID = -9023933264149434389L;

    /** Maps connections to a list of streams.*/
    private TreeMap<String, HashSet<Stream>> serversStreams;

    /** Maps connections to a list of series.*/
    private final HashMap<String, TimeSeries> graphSeries;

    /** Performance stats over time. */
    private final HashMap<String, TreeMap<Long, Double>> seriesData;

    /** A panel containing a JFreechart chart. */
    private PerfChartPanel chart;

    /** A panel that allows to choose which counters to show on GUI. */
    private CounterPanel counterPanel;

    /** A table indicating which counters are currently being shown on GUI.. */
    private JTable countersTable;

    /** The parent component of this panel. */
    private final PerformanceMonitor parent;

    public GraphPanel(PerformanceMonitor parent) {
        this.parent = parent;
        this.graphSeries = new HashMap<String, TimeSeries>();
        this.seriesData = new HashMap<String, TreeMap<Long, Double>>();
    }

    public void loadForRealTimeMonitoring(DriverConfig[] drivers, SinkConfig[] sinks) {
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
            if (!serversStreams.containsKey(dr.getConnection().getAlias())) {
                serversStreams.put(dr.getConnection().getAlias(), new HashSet<Stream>());
            }
            for (WorkloadPhase phase : dr.getWorkload()) {
                if (phase instanceof SyntheticWorkloadPhase) {
                    SyntheticWorkloadPhase synthPhase = (SyntheticWorkloadPhase) phase;
                    for (EventType type: synthPhase.getSchema().keySet()) {
                        serversStreams.get(dr.getConnection().getAlias()).add(new Stream(Stream.INPUT, type.getName()));
                    }
                } else if (phase instanceof ExternalFileWorkloadPhase) {
                        ExternalFileWorkloadPhase filePhase = (ExternalFileWorkloadPhase) phase;
                        if (!filePhase.containsEventTypes()) {
                            serversStreams.get(dr.getConnection().getAlias()).add(new Stream(Stream.INPUT, filePhase.getSingleEventTypeName()));
                        }

                }
            }
        }
        for (SinkConfig sink: sinks) {
            if (!serversStreams.containsKey(sink.getConnection().getAlias())) {
                serversStreams.put(sink.getConnection().getAlias(), new HashSet<Stream>());
            }
            for (String stream : sink.getOutputStreamList()) {
                serversStreams.get(sink.getConnection().getAlias()).add(new Stream(Stream.OUTPUT, stream));
            }

        }

        // Counter panel
        counterPanel = new CounterPanel(serversStreams, seriesData, this);

        // Chart
        chart = new PerfChartPanel("", new String[]{}, "",
                new DecimalFormat("0"), PerfChartPanel.REAL_TIME);

        loadGUIComponents();
    }

    /**
     *
     * @return  a list of streams per connection.
     */
    public TreeMap<String, HashSet<Stream>> getServersStreams() {
        return serversStreams;
    }

    /**
     * Loads this graph panel for offline processing of log files.
     *
     * @param statsSeries    performance stats
     */
    public void loadForLogFiles(Set<PerformanceStats> statsSeries) {
        // Cleans Panel and subpanels
        this.graphSeries.clear();
        this.seriesData.clear();
        if (chart != null) {
            chart.clear();
        }
        this.clearGraphsPanel();

        serversStreams = new TreeMap<String, HashSet<Stream>>();

        // Chart
        chart = new PerfChartPanel("", new String[]{}, "",
                new DecimalFormat("0"), PerfChartPanel.LOG_FILE);

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

            String keyPrefix = server + Globals.CSV_DELIMITER
            + stream.name + Globals.CSV_DELIMITER;
            if (stream.type == Stream.INPUT) {
                keyPrefix += "Input" + Globals.CSV_DELIMITER;
            } else {
                keyPrefix += "Output" + Globals.CSV_DELIMITER;
            }

            // Average Throughput
            TreeMap<Long, Double> streamAvgThData = seriesData.get(keyPrefix + "Avg Throughput");
            if (streamAvgThData == null) {
                streamAvgThData = new TreeMap<Long, Double>();
                seriesData.put(keyPrefix + "Avg Throughput", streamAvgThData);
            }
            streamAvgThData.put(stats.timestamp, stats.avgThroughput);

            // Last Throughput
            TreeMap<Long, Double> streamLastThData = seriesData.get(keyPrefix + "Last Throughput");
            if (streamLastThData == null) {
                streamLastThData = new TreeMap<Long, Double>();
                seriesData.put(keyPrefix + "Last Throughput", streamLastThData);
            }
            streamLastThData.put(stats.timestamp, stats.lastThroughput);

            // Min Throughput
            TreeMap<Long, Double> streamMinThData = seriesData.get(keyPrefix + "Min Throughput");
            if (streamMinThData == null) {
                streamMinThData = new TreeMap<Long, Double>();
                seriesData.put(keyPrefix + "Min Throughput", streamMinThData);
            }
            streamMinThData.put(stats.timestamp, stats.minThroughput);

            // Max Throughput
            TreeMap<Long, Double> streamMaxThData = seriesData.get(keyPrefix + "Max Throughput");
            if (streamMaxThData == null) {
                streamMaxThData = new TreeMap<Long, Double>();
                seriesData.put(keyPrefix + "Max Throughput", streamMaxThData);
            }
            streamMaxThData.put(stats.timestamp, stats.maxThroughput);

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
        counterPanel = new CounterPanel(serversStreams, seriesData, this);
        counterPanel.streamsCombo.setSelectedIndex(1);

        loadGUIComponents();

    }

    private void loadGUIComponents() {
        // Counters Table
        JPanel countersTablePanel = new JPanel(new BorderLayout());
        countersTablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Selected Counters"));

        countersTable = new JTable();
        DefaultTableModel model = new DefaultTableModel(new String [] {"Visible", "Color", "Scale", "Server", "Stream", "Counter"}, 0)  {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 1 || column == 2;
            }

            @Override
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
                        key = server + Globals.CSV_DELIMITER
                        + streamName + Globals.CSV_DELIMITER
                        + streamType + Globals.CSV_DELIMITER
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
                    key = server + Globals.CSV_DELIMITER
                    + streamName + Globals.CSV_DELIMITER
                    + streamType + Globals.CSV_DELIMITER
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

    public void refreshGraphs() {
        String connection, streamName, streamType, counter;
        String [] key;
        synchronized (this.graphSeries) {
            for (Entry<String, TimeSeries> e : graphSeries.entrySet()) {
                key = e.getKey().split(Globals.CSV_DELIMITER);
                connection = key[0];
                streamName = key[1];
                streamType = key[2];
                counter = key[3];
                if (streamType.equalsIgnoreCase("Input")) {
                    Double throughput = parent.getThroughputFor(connection, streamName);
                    if (throughput != null) {
                        chart.updateChart(e.getValue(), System.currentTimeMillis(), throughput);
                    }
                } else if (streamType.equalsIgnoreCase("Output")) {
                    Double throughput = parent.getOutThroughputFor(connection, streamName);
                    if (throughput != null && !throughput.isNaN()) {
                        if (counter.equalsIgnoreCase("Throughput")) {
                            chart.updateChart(e.getValue(), System.currentTimeMillis(), throughput);
                        } else if (counter.equalsIgnoreCase("Response Time")) {
                            chart.updateChart(e.getValue(),
                                              System.currentTimeMillis(),
                                              parent.getResponseTimeFor(connection, streamName));
                        }
                    }
                }
            }
        }
    }


    /**
     * Adds a series to the GUI.
     *
     * @param seriesId       the unique identifier of the series
     * @param color          the color of the series on the chart
     * @param scale          the scale of the series on the chart
     * @param scaleStr       the textual representation of the scale of the series on the chart
     * @param connection     to which connection the data of this series is related to
     * @param streamName     the name of the stream
     * @param streamType     the type of the stream (either INPUT or OUTPUT)
     * @param counter        the name of the performance counter
     *
     * @return               the number of series currently shown on GUI,
     *                       or -1 if the series could not be added
     */
    public int addSeries(String seriesId, Color color, double scale, String scaleStr,
            String connection, String streamName, String streamType, String counter) {
        synchronized (graphSeries) {
            if (graphSeries.containsKey(seriesId)) { //Checks if the counter has already been inserted
                JOptionPane.showMessageDialog(null, "This counter has already been added.");
                return -1;
            } else {
                TimeSeries addedSeries = chart.addSeries(seriesId, color, scale);
                // Add series data
                if (seriesData != null && !seriesData.isEmpty() && seriesData.get(seriesId) != null) {
                    TreeMap<Long, Double> data = seriesData.get(seriesId);
                    for (Entry<Long, Double> e : data.entrySet()) {
                        chart.updateChart(addedSeries, e.getKey(), e.getValue());
                    }
                }
                graphSeries.put(seriesId, addedSeries);
                DefaultTableModel model = (DefaultTableModel) countersTable.getModel();
                model.addRow(new Object[] {new Boolean(true), color, scaleStr, connection,
                        streamName + " (" + streamType + ")", counter});
            }
        }
        return ((DefaultTableModel) countersTable.getModel()).getRowCount();
    }

    /**
     *
     * @param seriesID  the identifier of the series
     * @return          the performance stats for a given series
     */
    public TreeMap<Long, Double> getSeriesData(String seriesID) {
        return this.seriesData.get(seriesID);
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
