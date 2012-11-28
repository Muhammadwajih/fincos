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
package pt.uc.dei.fincos.controller.gui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import pt.uc.dei.fincos.controller.ConnectionConfig;

/**
 *
 * @author  Marcelo R.N. Mendes
 */
@SuppressWarnings({ "rawtypes", "serial" })
public final class ConnectionsDialog extends ComponentDetail {

    /** serial id. */
    private static final long serialVersionUID = -3833728491407520899L;

    /** List of available connections. */
    private ArrayList<ConnectionConfig> connections;

    /** Flag indicating if one or more configurations have been added,
     * updated or removed. */
    private boolean dirty = false;

    /** Popup menu for the connections table. */
    private JPopupMenu popupMenu = new JPopupMenu();


    /**
     * Creates new form ConnectionsDialog.
     *
     * @param conns     the list of available connections
     */
    public ConnectionsDialog(ArrayList<ConnectionConfig> conns) {
        super(null);
        this.connections = new ArrayList<ConnectionConfig>();
        this.connections.addAll(conns);
        initComponents();

        JMenuItem copyMenuItem = new JMenuItem("Copy...");
        JMenuItem delMenuItem = new JMenuItem("Delete");
        popupMenu.add(copyMenuItem);
        popupMenu.add(delMenuItem);

        copyMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int selected =
                        connectionsTable.convertRowIndexToModel(connectionsTable.getSelectedRow());
                if (selected > -1) {
                    ConnectionConfig copy = connections.get(selected).clone();
                    ConnectionDetail cDetail = openConnectionDetail(null);
                    cDetail.fillProperties(copy);
                    cDetail.setVisible(true);

                } else {
                    JOptionPane.showMessageDialog(null, "Select a Connection to copy");
                }
            }
        });
        delMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                deleteConnection();
            }
        });
        connectionsTable.addMouseListener(new PopupListener(popupMenu));

        fillGUI(conns);
        this.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
        addListeners();
        this.setLocationRelativeTo(null);
    }

    private void fillGUI(List<ConnectionConfig> connections) {
        DefaultTableModel model = (DefaultTableModel) this.connectionsTable.getModel();
        for (ConnectionConfig c : connections) {
            model.addRow(new Object[]{c.alias, c.type == ConnectionConfig.CEP_ADAPTER
                                      ? "CEP Adapter" : "JMS"});
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings({ "unchecked" })
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        connLbl = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        connectionsTable = new javax.swing.JTable();
        addBtn = new javax.swing.JButton();
        deleteBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        okBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Connection Configuration");

        connLbl.setText("Connections:");

        connectionsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Alias", "Type"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(connectionsTable);

        addBtn.setText("Add...");
        addBtn.setPreferredSize(new java.awt.Dimension(70, 23));

        deleteBtn.setText("Delete");
        deleteBtn.setPreferredSize(new java.awt.Dimension(70, 23));

        cancelBtn.setText("Cancel");
        cancelBtn.setPreferredSize(new java.awt.Dimension(90, 30));

        okBtn.setText("OK");
        okBtn.setPreferredSize(new java.awt.Dimension(90, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(connLbl)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(7, 7, 7)
                                .addComponent(okBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                            .addComponent(deleteBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(connLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(okBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addListeners() {
        okBtn.setIcon(new ImageIcon("imgs/ok.png"));
        cancelBtn.setIcon(new ImageIcon("imgs/cancel.png"));

        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dirty) {
                    if (JOptionPane.showConfirmDialog(null,
                            "Discard changes?", "",
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        dispose();
                    }
                } else {
                    dispose();
                }
            }
        });

        okBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (dirty) {
                    Controller_GUI.getInstance().setConnections(connections);
                    try {
                        Controller_GUI.getInstance().saveConnections();
                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(null,
                                "Error while saving connections file. Message: "
                                + e1.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
                }
                dispose();
            }
        });

        addBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ConnectionDetail connDetail = openConnectionDetail(null);
                connDetail.setVisible(true);
            }
        });

        deleteBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                deleteConnection();
            }
        });

        connectionsTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable source = (JTable) e.getSource();
                    if (source.isEnabled()) {
                        int selected = connectionsTable.convertRowIndexToModel(connectionsTable.getSelectedRow());
                        if (selected > -1) {
                            ConnectionDetail cDetail =
                                    openConnectionDetail(connections.get(selected));
                            cDetail.setVisible(true);
                        }
                    }
                }
            }
        });
    }

    private void deleteConnection() {
        int tableIndex = connectionsTable.getSelectedRow();
        if (tableIndex != -1) {
            int index = connectionsTable.convertRowIndexToModel(tableIndex);
            DefaultTableModel model = (DefaultTableModel) connectionsTable.getModel();
            int confirmDelete = JOptionPane.showConfirmDialog(null,
                    "Do you want to permanently remove this connection from the repository? "
                    + "(WARN: The connection will be unavailable for this and other test setups)",
                    "Confirm deletion", JOptionPane.YES_NO_OPTION);
            if (confirmDelete == JOptionPane.YES_OPTION) {
                connections.remove(index);
                model.removeRow(index);
                dirty = true;
            }
        }
    }

    /**
     * Creates a form for configuring a connection.
     *
     * @param connCfg   Workload phase configuration properties, when in UPDATE mode,
     *                  or <tt>null</tt>, in INSERTION mode.
     * @return          a reference to the just created form
     */
    protected ConnectionDetail openConnectionDetail(ConnectionConfig connCfg) {
        return new ConnectionDetail(this, connCfg);
    }

    /**
     * Add a new connection to the list of available connections.
     *
     * @param connCfg   the new connection
     */
    protected void addConnection(ConnectionConfig connCfg) {
        this.connections.add(connCfg);
        ((DefaultTableModel) this.connectionsTable.getModel()).addRow(
                new Object[]{connCfg.alias, connCfg.type == ConnectionConfig.CEP_ADAPTER
                             ? "CEP Adapter" : "JMS"});
        dirty = true;
    }

    /**
     * Updates an existing connection.
     *
     * @param oldCfg    old configuration
     * @param newCfg    new configuration
     */
    protected void updateConnection(ConnectionConfig oldCfg, ConnectionConfig newCfg) {
        int index = this.connections.indexOf(oldCfg);
        this.connections.remove(index);
        this.connections.add(index, newCfg);
        ((DefaultTableModel) connectionsTable.getModel()).removeRow(index);
        ((DefaultTableModel) connectionsTable.getModel()).insertRow(index,
                new Object[]{newCfg.alias, newCfg.type == ConnectionConfig.CEP_ADAPTER
                             ? "CEP Adapter" : "JMS"});
        dirty = true;
    }

    public void disableGUI() {
        this.connectionsTable.setEnabled(false);
        this.addBtn.setEnabled(false);
        this.deleteBtn.setEnabled(false);
        this.okBtn.setEnabled(false);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBtn;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JLabel connLbl;
    private javax.swing.JTable connectionsTable;
    private javax.swing.JButton deleteBtn;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton okBtn;
    // End of variables declaration//GEN-END:variables
}
