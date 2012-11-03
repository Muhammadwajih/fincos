/* FINCoS Framework
 * Copyright (C) 2012 CISUC, University of Coimbra
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

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;

import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.perfmon.Stream;

/**
 * A panel that allows to choose the counters to be exhibited on the GUI
 * of the FINCoS Perfmon application.
 *
 * @author  Marcelo R.N. Mendes
 */
public class CounterPanel extends javax.swing.JPanel {
    /** serial id. */
    private static final long serialVersionUID = -8102495513812210261L;

    /** Maps connections to a list of streams.*/
    private TreeMap<String, HashSet<Stream>> serversStreams;

    /** Performance stats over time. */
    private HashMap<String, TreeMap<Long, Double>> seriesData;

    /** The parent component of this panel. */
    GraphPanel parent;

    /** CounterCombo model for output streams. */
    private final DefaultComboBoxModel outputModel;

    /** CounterCombo model for input streams. */
    private final DefaultComboBoxModel inputModel;

    /** CounterCombo model for offline performance measurement. */
    private final DefaultComboBoxModel logModel;

    /** Available series colors. */
    private final Color[] seriesColors;


    /**
     * Creates new form CounterPanel.
     *
     * @param serversStreams    maps connections to a list of streams
     * @param seriesData        performance stats over time
     * @param parent            the parent component of this panel
     */
    public CounterPanel(TreeMap<String, HashSet<Stream>> serversStreams, HashMap<String,
            TreeMap<Long, Double>> seriesData, GraphPanel parent) {
        this.serversStreams = serversStreams;
        this.seriesData = seriesData;
        this.parent = parent;

        outputModel = new DefaultComboBoxModel(new String[] { "Response Time", "Throughput" });
        inputModel = new javax.swing.DefaultComboBoxModel(new String[] { "Throughput" });
        logModel = new DefaultComboBoxModel(new String[] {
                "Avg Throughput", "Min Throughput",
                "Max Throughput", "Last Throughput",
                "Avg Response Time", "Min Response Time",
                "Max Response Time", "Last Response Time",
                "Stdev Response Time"
        });
        seriesColors = new Color[] {
                Color.BLUE, Color.RED, new Color(0, 200, 0),
                Color.BLACK, Color.YELLOW, Color.ORANGE,
                Color.CYAN, Color.MAGENTA, Color.WHITE};
        initComponents();
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
        if (streamsList.getSelectedValue() != null) {
            String key = serverCombo.getSelectedItem() + Globals.CSV_DELIMITER
            + streamsList.getSelectedValue() + Globals.CSV_DELIMITER
            + streamsCombo.getSelectedItem() + Globals.CSV_DELIMITER
            + (String) counterCombo.getSelectedItem();

            int seriesCount = parent.addSeries(key, colorPanel.getBackground(),
                    Double.parseDouble((String) scaleCombo.getSelectedItem()),
                    (String) scaleCombo.getSelectedItem(), (String) serverCombo.getSelectedItem(),
                    (String) streamsList.getSelectedValue(), (String) streamsCombo.getSelectedItem(),
                    (String) counterCombo.getSelectedItem());
            if (seriesCount != -1) {
                colorPanel.setBackground(seriesColors[seriesCount % seriesColors.length]);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Select a stream from the available list.");
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
    protected javax.swing.JComboBox streamsCombo;
    private javax.swing.JLabel streamsLabel;
    private javax.swing.JList streamsList;
    // End of variables declaration

}