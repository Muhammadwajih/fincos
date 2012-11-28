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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedHashMap;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import pt.uc.dei.fincos.basic.Attribute;
import pt.uc.dei.fincos.basic.Datatype;
import pt.uc.dei.fincos.basic.Domain;
import pt.uc.dei.fincos.basic.PredefinedListDomain;
import pt.uc.dei.fincos.basic.RandomDomain;
import pt.uc.dei.fincos.basic.SequentialDomain;
import pt.uc.dei.fincos.random.ConstantVariate;
import pt.uc.dei.fincos.random.RandomExponentialVariate;
import pt.uc.dei.fincos.random.RandomNormalVariate;
import pt.uc.dei.fincos.random.RandomUniformVariate;
import pt.uc.dei.fincos.random.Variate;

/**
 * GUI for configuring attributes of Event types of Synthetic workloads.
 *
 * @author  Marcelo R.N. Mendes
 *
 */
@SuppressWarnings("rawtypes")
public final class ColumnDetail extends ComponentDetail {

    /** Serial id.  */
    private static final long serialVersionUID = -1349017355443745498L;

    /** The three types of panels displayed on this form. */
    private SequentialDomainPanel sequentialPanel = new SequentialDomainPanel();
    private PredefinedListDomainPanel predefinedPanel = new PredefinedListDomainPanel();
    private RandomlDomainPanel randomPanel = new RandomlDomainPanel();
    private TypeDetail parent;

    /** Previous properties of the Attribute (when form is open for update). */
    private Attribute oldAtt;

    /** Seed for random number generation. */
    private Long randomSeed;

    /**
     * Creates new form ColumnDetail.
     *
     * @param parent        parent form
     * @param att           properties of an Attribute in an Event Type
     * @param randomSeed    seed for random number generation
     */
    public ColumnDetail(TypeDetail parent, Attribute att, Long randomSeed) {
        super(parent);
        this.randomSeed = randomSeed;

        initComponents();

        okBtn.setIcon(new ImageIcon("imgs/ok.png"));
        cancelBtn.setIcon(new ImageIcon("imgs/cancel.png"));

        domainParamsPanel.setLayout(new BorderLayout());
        domainPanel.setPreferredSize(new Dimension(300, 275));

        this.setTitle("Column Detail");
        this.setLocationRelativeTo(null); //screen center
        this.setResizable(false);
        this.setVisible(true);

        this.addListeners();

        this.dataTypeCombo.getItemListeners()[0].itemStateChanged(null);

        if (att != null) {
            this.oldAtt = att;
            this.op = UPDATE;
            fillProperties(att);
        } else {
            this.op = INSERT;
            setTitle("New Column");
        }

        this.parent = parent;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings({ "unchecked" })
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cancelBtn = new javax.swing.JButton();
        okBtn = new javax.swing.JButton();
        domainPanel = new javax.swing.JPanel();
        domainTypeLabel = new javax.swing.JLabel();
        domainTypeCombo = new javax.swing.JComboBox();
        domainParamsPanel = new javax.swing.JPanel();
        nameField = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        dataTypeLabel = new javax.swing.JLabel();
        dataTypeCombo = new javax.swing.JComboBox();

        cancelBtn.setText("Cancel");
        cancelBtn.setPreferredSize(null);

        okBtn.setText("OK");
        okBtn.setPreferredSize(null);

        domainPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Domain Options"));

        domainTypeLabel.setText("Type:");

        domainTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Predefined List", "Random", "Sequential" }));

        domainParamsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Parameters"));

        javax.swing.GroupLayout domainParamsPanelLayout = new javax.swing.GroupLayout(domainParamsPanel);
        domainParamsPanel.setLayout(domainParamsPanelLayout);
        domainParamsPanelLayout.setHorizontalGroup(
            domainParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 272, Short.MAX_VALUE)
        );
        domainParamsPanelLayout.setVerticalGroup(
            domainParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 203, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout domainPanelLayout = new javax.swing.GroupLayout(domainPanel);
        domainPanel.setLayout(domainPanelLayout);
        domainPanelLayout.setHorizontalGroup(
            domainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(domainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(domainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(domainPanelLayout.createSequentialGroup()
                        .addComponent(domainTypeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(domainTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(domainParamsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        domainPanelLayout.setVerticalGroup(
            domainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(domainPanelLayout.createSequentialGroup()
                .addGroup(domainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(domainTypeLabel)
                    .addComponent(domainTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(domainParamsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        nameLabel.setText("Name");

        dataTypeLabel.setText("Data Type");

        dataTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "BOOLEAN", "DOUBLE", "FLOAT", "INTEGER", "LONG", "TEXT" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(domainPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nameLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 83, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(dataTypeLabel)
                                .addGap(58, 58, 58))
                            .addComponent(dataTypeCombo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(okBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(dataTypeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dataTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(domainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addListeners() {
        dataTypeCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (dataTypeCombo.getSelectedItem().equals("BOOLEAN")) {
                    DefaultTableModel model = (DefaultTableModel) predefinedPanel.itemsTable.getModel();
                    int rowCount = model.getRowCount();
                    for (int i = 0; i < rowCount; i++) {
                        model.removeRow(0);
                    }
                    domainTypeCombo.setSelectedItem("Predefined List");
                    predefinedPanel.addBtn.setEnabled(false);
                    predefinedPanel.deleteBtn.setEnabled(false);
                    model.addRow(new Object[] {"true", "1.0"});
                    model.addRow(new Object[] {"false", "1.0"});
                    domainTypeCombo.setEnabled(false);
                } else {
                    DefaultTableModel model = (DefaultTableModel) predefinedPanel.itemsTable.getModel();
                    int rowCount = model.getRowCount();
                    for (int i = 0; i < rowCount; i++) {
                        model.removeRow(0);
                    }
                    predefinedPanel.addBtn.setEnabled(true);
                    predefinedPanel.deleteBtn.setEnabled(true);
                    domainTypeCombo.setEnabled(true);
                }
            }
        });

        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (predefinedPanel.itemsTable.getCellEditor() != null) {
                        predefinedPanel.itemsTable.getCellEditor().stopCellEditing();
                    }

                    if (validateFields()) {
                        String attName = nameField.getText();
                        Domain domain = null;
                        if (domainTypeCombo.getSelectedItem().equals("Predefined List")) {
                            DefaultTableModel model = (DefaultTableModel) predefinedPanel.itemsTable.getModel();
                            boolean deterministic = predefinedPanel.sameFreqCheck.isSelected();

                            if (deterministic) {
                                Object[] items = new Object[model.getRowCount()];
                                for (int i = 0; i < model.getRowCount(); i++) {
                                    items[i] = model.getValueAt(i, 0);
                                }
                                domain = new PredefinedListDomain(items);
                            } else {
                                LinkedHashMap<Object, Double> itemsMix = new LinkedHashMap<Object, Double>(model.getRowCount());
                                for (int i = 0; i < model.getRowCount(); i++) {
                                    double val =
                                            Double.parseDouble((String) model.getValueAt(i, 1));
                                    itemsMix.put(model.getValueAt(i, 0), val);
                                }
                                domain = new PredefinedListDomain(itemsMix, randomSeed);
                            }
                        } else if (domainTypeCombo.getSelectedItem().equals("Random")) {
                            if (randomPanel.distrCombo.getSelectedItem().equals("Uniform")) {
                                double lower = Double.parseDouble(randomPanel.param1Field.getText());
                                double upper = Double.parseDouble(randomPanel.param2Field.getText());
                                domain = new RandomDomain(new RandomUniformVariate(randomSeed, lower, upper));
                            } else if (randomPanel.distrCombo.getSelectedItem().equals("Normal")) {
                                double mean = Double.parseDouble(randomPanel.param1Field.getText());
                                double stdev = Double.parseDouble(randomPanel.param2Field.getText());
                                domain = new RandomDomain(new RandomNormalVariate(randomSeed, mean, stdev));
                            } else if (randomPanel.distrCombo.getSelectedItem().equals("Exponential")) {
                                double lambda = Double.parseDouble(randomPanel.param1Field.getText());
                                domain = new RandomDomain(new RandomExponentialVariate(randomSeed, lambda));
                            }
                        } else if (domainTypeCombo.getSelectedItem().equals("Sequential")) {
                            Variate initialVariate = null;
                            Variate incrementVariate = null;

                            if (sequentialPanel.initialConstantRadio.isSelected()) {
                                double value = Double.parseDouble(sequentialPanel.initialConstantField.getText());
                                initialVariate = new ConstantVariate(value);
                            } else {
                                if (sequentialPanel.initialRandomVariateCombo.getSelectedItem().equals("Uniform")) {
                                    double lower = Double.parseDouble(sequentialPanel.initialParam1Field.getText());
                                    double upper = Double.parseDouble(sequentialPanel.initialParam2Field.getText());
                                    initialVariate = new RandomUniformVariate(randomSeed, lower, upper);
                                } else if (sequentialPanel.initialRandomVariateCombo.getSelectedItem().equals("Normal")) {
                                    double mean = Double.parseDouble(sequentialPanel.initialParam1Field.getText());
                                    double stdev = Double.parseDouble(sequentialPanel.initialParam2Field.getText());
                                    initialVariate = new RandomNormalVariate(randomSeed, mean, stdev);
                                } else if (sequentialPanel.initialRandomVariateCombo.getSelectedItem().equals("Exponential")) {
                                    double lambda = Double.parseDouble(sequentialPanel.initialParam1Field.getText());
                                    initialVariate = new RandomExponentialVariate(randomSeed, lambda);
                                }
                            }
                            if (sequentialPanel.incrementConstantRadio.isSelected()) {
                                double value = Double.parseDouble(sequentialPanel.incrementConstantField.getText());
                                incrementVariate = new ConstantVariate(value);
                            } else {
                                if (sequentialPanel.incrementRandomVariateCombo.getSelectedItem().equals("Uniform")) {
                                    double lower = Double.parseDouble(sequentialPanel.incrParam1Field.getText());
                                    double upper = Double.parseDouble(sequentialPanel.incrParam2Field.getText());
                                    incrementVariate = new RandomUniformVariate(randomSeed, lower, upper);
                                } else if (sequentialPanel.incrementRandomVariateCombo.getSelectedItem().equals("Normal")) {
                                    double mean = Double.parseDouble(sequentialPanel.incrParam1Field.getText());
                                    double stdev = Double.parseDouble(sequentialPanel.incrParam2Field.getText());
                                    incrementVariate = new RandomNormalVariate(randomSeed, mean, stdev);
                                } else if (sequentialPanel.incrementRandomVariateCombo.getSelectedItem().equals("Exponential")) {
                                    double lambda = Double.parseDouble(sequentialPanel.incrParam1Field.getText());
                                    incrementVariate = new RandomExponentialVariate(randomSeed, lambda);
                                }
                            }
                            domain = new SequentialDomain(initialVariate, incrementVariate);
                        }

                        String type = (String) dataTypeCombo.getSelectedItem();
                        Datatype dataType = null;
                        if (type.equals("INTEGER")) {
                            dataType = Datatype.INTEGER;
                        } else if (type.equals("LONG")) {
                            dataType = Datatype.LONG;
                        } else if (type.equals("FLOAT")) {
                            dataType = Datatype.FLOAT;
                        } else if (type.equals("DOUBLE")) {
                            dataType = Datatype.DOUBLE;
                        } else if (type.equals("TEXT")) {
                            dataType = Datatype.TEXT;
                        } else if (type.equals("BOOLEAN")) {
                            dataType = Datatype.BOOLEAN;
                        }

                        Attribute newAtt = new Attribute(dataType, attName, domain);

                        switch (op) {
                        case UPDATE:
                            parent.updateColumn(oldAtt, newAtt);
                            dispose();
                            break;
                        case INSERT:
                            parent.addColumn(newAtt);
                            dispose();
                        }
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "One or more required fields were not correctly filled.",
                                "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null,
                            "Invalid number format " + nfe.getMessage());
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(null, exc.getMessage());
                }
            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }

        });

        domainTypeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = domainTypeCombo.getSelectedIndex();
                switch (selectedIndex) {
                case 0:
                    domainParamsPanel.removeAll();
                    domainParamsPanel.add(predefinedPanel);
                    domainParamsPanel.revalidate();
                    domainParamsPanel.repaint();
                    break;
                case 1:
                    domainParamsPanel.removeAll();
                    domainParamsPanel.add(randomPanel);
                    domainParamsPanel.revalidate();
                    domainParamsPanel.repaint();
                    break;
                case 2:
                    domainParamsPanel.removeAll();
                    domainParamsPanel.add(sequentialPanel);
                    domainParamsPanel.revalidate();
                    domainParamsPanel.repaint();
                    break;
                default:
                    break;
                }
            }
        });

        domainTypeCombo.setSelectedIndex(0);
    }

    /**
     * Fills the GUI with the properties of an Attribute passed as argument.
     *
     * @param a     the Attribute whose properties must be shown in GUI
     */
    public void fillProperties(Attribute a) {
        this.nameField.setText(a.getName());

        switch (a.getType()) {
        case INTEGER:
            dataTypeCombo.setSelectedItem("INTEGER");
            break;
        case LONG:
            dataTypeCombo.setSelectedItem("LONG");
            break;
        case FLOAT:
            dataTypeCombo.setSelectedItem("FLOAT");
            break;
        case DOUBLE:
            dataTypeCombo.setSelectedItem("DOUBLE");
            break;
        case TEXT:
            dataTypeCombo.setSelectedItem("TEXT");
            break;
        case BOOLEAN:
            dataTypeCombo.setSelectedItem("BOOLEAN");
            break;
        }

        if (a.getType() == Datatype.BOOLEAN) {
                domainTypeCombo.setSelectedItem("Predefined List");
                domainTypeCombo.setEnabled(false);
        }

        if (a.getDomain() instanceof PredefinedListDomain) {
            domainTypeCombo.setSelectedItem("Predefined List");
            predefinedPanel.fillProperties(a.getType(), (PredefinedListDomain) a.getDomain());
        } else if (a.getDomain() instanceof RandomDomain) {
            domainTypeCombo.setSelectedItem("Random");
            randomPanel.fillProperties((RandomDomain) a.getDomain());
        } else if (a.getDomain() instanceof SequentialDomain) {
            domainTypeCombo.setSelectedItem("Sequential");
            sequentialPanel.fillProperties((SequentialDomain) a.getDomain());
        }
    }

    /**
     * Checks if the input provided by the user in the GUI is valid.
     *
     * @return              <tt>true</tt> if the input is valid, <tt>false</tt> otherwise.
     * @throws Exception    for invalid inputs with a specific error message
     */
    private boolean validateFields() throws Exception {
        if (nameField.getText() == null || nameField.getText().isEmpty()) {
            return false;
        }

        if (domainTypeCombo.getSelectedItem().equals("Predefined List")) {
            return predefinedPanel.validateFields((String) dataTypeCombo.getSelectedItem());
        } else if (domainTypeCombo.getSelectedItem().equals("Random")) {
            return randomPanel.validateFields();
        } else if (domainTypeCombo.getSelectedItem().equals("Sequential")) {
            return sequentialPanel.validateFields();
        } else {
            return false;
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelBtn;
    private javax.swing.JComboBox dataTypeCombo;
    private javax.swing.JLabel dataTypeLabel;
    private javax.swing.JPanel domainPanel;
    private javax.swing.JPanel domainParamsPanel;
    private javax.swing.JComboBox domainTypeCombo;
    private javax.swing.JLabel domainTypeLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JButton okBtn;
    // End of variables declaration//GEN-END:variables

}
