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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import pt.uc.dei.fincos.basic.Attribute;
import pt.uc.dei.fincos.basic.EventType;



/**
 * Form for configuring the properties of a data type.
 *
 * @author  Marcelo R.N. Mendes
 */
@SuppressWarnings({ "serial", "rawtypes" })
public final class TypeDetail extends ComponentDetail {

    /** serial id. */
    private static final long serialVersionUID = 4308405938778241398L;

    /** Parent form. */
    private PhaseDetail parent;

    /** Previous properties of the data type (when the form is open for update). */
    private EventType oldType;

    /** Seed for random number generation. */
    private Long dataGenSeed;

    /** List of attributes of this data type. */
    private ArrayList<Attribute> columns;

    /**
     * Creates a form for configuring a data type.
     *
     * @param parent        parent form
     * @param type          Data type configuration properties, when in UPDATE
     *                      mode, or <tt>null</tt>, in INSERTION mode.
     * @param dataGenSeed   seed for random number generation
     *
     */
    public TypeDetail(PhaseDetail parent, EventType type, Long dataGenSeed) {
        super(parent);
        this.columns = new ArrayList<Attribute>();
        initComponents();
        addListeners();

        if (type != null) {
            this.oldType = type;
            this.op = UPDATE;
            fillProperties(type);
        } else {
            this.op = INSERT;
            setTitle("New Event Type");
        }

        this.parent = parent;

        this.setResizable(false);
        this.setLocationRelativeTo(null); //screen center
        this.setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        columnsTablePopup = new javax.swing.JPopupMenu();
        jLabel1 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        cancelBtn = new javax.swing.JButton();
        okBtn = new javax.swing.JButton();
        schemaScroll = new javax.swing.JScrollPane();
        columnsTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Event Type Detail");

        jLabel1.setText("Name");

        cancelBtn.setText("Cancel");
        cancelBtn.setPreferredSize(null);

        okBtn.setText("OK");
        okBtn.setPreferredSize(null);

        columnsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null}
            },
            new String [] {
                "Name", "Type", "Distribution"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, false
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
        columnsTable.setMaximumSize(new java.awt.Dimension(150, 16));
        schemaScroll.setViewportView(columnsTable);

        jLabel2.setText("Columns");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(160, 160, 160))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(schemaScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 79, Short.MAX_VALUE)
                            .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(okBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(19, 19, 19))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(schemaScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addListeners() {
        cancelBtn.setIcon(new ImageIcon("imgs/cancel.png"));
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }

        });
        okBtn.setIcon(new ImageIcon("imgs/ok.png"));
        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (validateFields()) {
                        String typeName = nameField.getText();
                        Attribute[] atts = new Attribute[columns.size()];
                        atts = columns.toArray(atts);
                        EventType newType = new EventType(typeName, atts);

                        switch (op) {
                        case UPDATE:
                            parent.updateEventType(oldType, newType);
                            dispose();
                            break;
                        case INSERT:
                            parent.addEventType(newType);
                            dispose();
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "One or more required fields were not correctly filled.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(null, exc.getMessage());
                }
            }

        });

        columnsTable.addMouseListener(new PopupListener(columnsTablePopup));
        JMenuItem addColMenuItem = new JMenuItem("Add...");
        JMenuItem deleteColMenuItem = new JMenuItem("Delete");
        JMenuItem copyOneColMenuItem = new JMenuItem("Make one copy");
        JMenuItem copyManyColsMenuItem = new JMenuItem("Make multiple copies...");

        addColMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openColumnDetail(null);
            }
        });

        columnsTable.getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
               int row = e.getFirstRow();
               int col = e.getColumn();
               if (row < columnsTable.getRowCount() && col != -1
                   && columnsTable.isEditing()) {
                   String newAttName = (String) columnsTable.getValueAt(row, col);
                   Attribute oldAtt = columns.get(row);
                   Attribute newAtt = oldAtt.clone();
                   newAtt.setName(newAttName);
                   updateColumn(oldAtt, newAtt);
               }
            }
          });


        deleteColMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] indexes = columnsTable.getSelectedRows();

                if (indexes.length > 0) {
                    int count = 0;
                    for (int i : indexes) {
                        columns.remove(i - count);
                        ((DefaultTableModel) columnsTable.getModel()).removeRow(i - count);
                        count++;
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Select a column to delete");
                }
            }
        });

        copyOneColMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] indexes = columnsTable.getSelectedRows();
                if (indexes.length > 0) {
                    Attribute original, copy;
                    ArrayList<Attribute> toAdd = new ArrayList<Attribute>(indexes.length);
                    for (int i : indexes) {
                        original = columns.get(i);
                        copy = original.clone();
                        copy.setName(copy.getName() + "_copy");
                        toAdd.add(copy);
                    }

                    for (Attribute attribute : toAdd) {
                        addColumn(attribute);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Select at least one column to copy");
                }
            }
        });

        copyManyColsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] indexes = columnsTable.getSelectedRows();
                if (indexes.length > 0) {
                    int numCopies = 0;
                    String input = JOptionPane.showInputDialog(null, "Number of Copies", "2");
                    try {
                        if (input != null) {
                            numCopies = Integer.parseInt(input);
                        } else {
                            return;
                        }

                    } catch (Exception e2) {
                        JOptionPane.showMessageDialog(null, "Invalid number");
                        return;
                    }

                    Attribute original, copy;
                    ArrayList<Attribute> toAdd = new ArrayList<Attribute>(indexes.length);
                    for (int i = 1; i <= numCopies; i++) {
                        for (int index : indexes) {
                            if (index < columns.size()) {
                                original = columns.get(index);
                                copy = original.clone();
                                copy.setName(copy.getName() + "_copy_" + i);
                                toAdd.add(copy);
                            }
                        }
                    }


                    for (Attribute attribute : toAdd) {
                        addColumn(attribute);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Select at least one column to copy");
                }
            }
        });

        columnsTablePopup.add(addColMenuItem);
        columnsTablePopup.add(deleteColMenuItem);
        columnsTablePopup.add(copyOneColMenuItem);
        columnsTablePopup.add(copyManyColsMenuItem);

        columnsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable source = (JTable) e.getSource();
                    if (source.isEnabled()) {
                        int selected = source.getSelectedRow();
                        if (selected > -1 && selected < columns.size()) {
                            openColumnDetail(columns.get(selected));
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean validateFields() {
        boolean ret = true;
        if (nameField.getText() == null || nameField.getText().isEmpty()) {
            nameField.setBackground(INVALID_INPUT_COLOR);
            ret = false;
        } else {
            Color defaultColor = UIManager.getColor("TextField.background");
            nameField.setBackground(defaultColor);
        }

        if (columnsTable.getRowCount() <= 1) {
            columnsTable.setBackground(INVALID_INPUT_COLOR);
            ret = false;
        } else {
            Color defaultColor = UIManager.getColor("Table.background");
            columnsTable.setBackground(defaultColor);
        }

        return ret;
    }


    /**
     * Fills the UI with the data type properties passed as argument.
     *
     * @param type   Driver configuration properties to be shown in UI, when in UPDATE mode,
     *               or <tt>null</tt>, in INSERTION mode.
     */
    public void fillProperties(EventType type) {
        this.nameField.setText(type.getName());

        Attribute[] atts = type.getAttributes();
        DefaultTableModel model = (DefaultTableModel) columnsTable.getModel();

        int rowCount = model.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            model.removeRow(0);
        }

        for (int i = 0; i < atts.length; i++) {
            this.columns .add(atts[i]);
            model.addRow(new Object[] {atts[i].getName(), atts[i].getType(), atts[i].getDomain()});
        }

        model.addRow(new Object[] {null, null, null});
    }


    /**
     * Updates the definition of a attribute of this data type.
     *
     * @param oldColumn     the old attribute configuration
     * @param newColumn     the new attribute configuration
     */
    public void updateColumn(Attribute oldColumn, Attribute newColumn) {
        int index = this.columns.indexOf(oldColumn);

        if (index > -1) {
            removeColumn(index);
            addColumn(index, newColumn);
        }
    }

    /**
     * Adds an attribute to this data type.
     *
     * @param index         attribute index
     * @param newColumn     the new attribute
     */
    public void addColumn(int index, Attribute newColumn) {
        this.columns.add(index, newColumn);
        ((DefaultTableModel) this.columnsTable.getModel()).insertRow(index, new Object[] {newColumn.getName(), newColumn.getType(), newColumn.getDomain()});
    }


    /**
     * Adds an attribute to this data type.
     *
     * @param newColumn     the new attribute
     */
    public void addColumn(Attribute newColumn) {
        this.columns.add(newColumn);
        DefaultTableModel model = ((DefaultTableModel) this.columnsTable.getModel());
        model.insertRow(model.getRowCount() - 1, new Object[] {newColumn.getName(), newColumn.getType(), newColumn.getDomain()});
    }

    private void removeColumn(int index) {
        this.columns.remove(index);
        ((DefaultTableModel) columnsTable.getModel()).removeRow(index);
    }


    /**
     * Creates a form for configuring an attribute.
     *
     * @param att       Attribute configuration properties, when in UPDATE mode,
     *                  or <tt>null</tt>, in INSERTION mode.
     * @return          a reference to the just created form
     */
    protected ColumnDetail openColumnDetail(Attribute att) {
        return new ColumnDetail(this, att, this.dataGenSeed);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelBtn;
    private javax.swing.JTable columnsTable;
    private javax.swing.JPopupMenu columnsTablePopup;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField nameField;
    private javax.swing.JButton okBtn;
    private javax.swing.JScrollPane schemaScroll;
    // End of variables declaration//GEN-END:variables

}
