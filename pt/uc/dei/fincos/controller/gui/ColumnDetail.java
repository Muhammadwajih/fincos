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
import java.util.Map.Entry;

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
@SuppressWarnings("serial")
public class ColumnDetail extends ComponentDetail {
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
        }


        if (a.getDomain() instanceof PredefinedListDomain) {
            domainTypeCombo.setSelectedItem("Predefined List");
            predefinedPanel.fillProperties((PredefinedListDomain) a.getDomain());
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        //================================= GENERATED CODE ==========================================
        nameField = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        dataTypeCombo = new javax.swing.JComboBox();
        dataTypeLabel = new javax.swing.JLabel();
        domainPanel = new javax.swing.JPanel();
        domainTypeLabel = new javax.swing.JLabel();
        domainTypeCombo = new javax.swing.JComboBox();
        domainParamsPanel = new javax.swing.JPanel();
        okBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Column Detail");
        setResizable(false);

        nameLabel.setText("Name");

        dataTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "BOOLEAN", "DOUBLE", "FLOAT", "INTEGER", "LONG", "TEXT" }));

        dataTypeLabel.setText("Data Type");

        domainPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Domain Options"));

        domainTypeLabel.setText("Type:");

        domainTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Predefined List", "Random", "Sequential" }));

        domainParamsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Parameters"));

        javax.swing.GroupLayout domainParamsPanelLayout = new javax.swing.GroupLayout(domainParamsPanel);
        domainParamsPanel.setLayout(domainParamsPanelLayout);
        domainParamsPanelLayout.setHorizontalGroup(
                domainParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 269, Short.MAX_VALUE)
                );
        domainParamsPanelLayout.setVerticalGroup(
                domainParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 200, Short.MAX_VALUE)
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

        okBtn.setText("OK");
        okBtn.setPreferredSize(null);

        cancelBtn.setText("Cancel");
        cancelBtn.setPreferredSize(null);

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
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 88, Short.MAX_VALUE)
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
        //==============================  END OF GENERATED CODE =======================================



        //==============================  EVENT HANDLING CODE =========================================
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

        okBtn.setPreferredSize(null);
        okBtn.setIcon(new ImageIcon("imgs/ok.png"));
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
                                    itemsMix.put(model.getValueAt(i, 0), Double.parseDouble((String)model.getValueAt(i, 1)));
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
                        JOptionPane.showMessageDialog(null, "One or more required fields were not correctly filled.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Invalid number format " + nfe.getMessage());
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(null, exc.getMessage());
                }
            }

        });

        cancelBtn.setPreferredSize(null);
        cancelBtn.setIcon(new ImageIcon("imgs/cancel.png"));
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }

        });

        domainParamsPanel.setLayout(new BorderLayout());
        domainPanel.setPreferredSize(new Dimension(300, 275));

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
        this.setTitle("Column Detail");
        this.setLocationRelativeTo(null); //screen center
        this.setResizable(false);
        this.setVisible(true);
    }


    // Variables declaration - do not modify
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
    // End of variables declaration


    // ======================= Internal class: SequentialDomainPanel===========================

    class SequentialDomainPanel extends javax.swing.JPanel {

        /** Creates new form SequentialDomainPanel */
        public SequentialDomainPanel() {
            initComponents();
        }


        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        // <editor-fold defaultstate="collapsed" desc="Generated Code">
        private void initComponents() {
            initialRadioGroup = new javax.swing.ButtonGroup();
            incrementRadioGroup = new javax.swing.ButtonGroup();
            jLabel1 = new javax.swing.JLabel();
            initialConstantField = new javax.swing.JTextField();
            initialRandomVariateCombo = new javax.swing.JComboBox();
            initialConstantRadio = new javax.swing.JRadioButton();
            initialRandomRadio = new javax.swing.JRadioButton();
            jLabel2 = new javax.swing.JLabel();
            incrementConstantRadio = new javax.swing.JRadioButton();
            incrementRandomRadio = new javax.swing.JRadioButton();
            incrementRandomVariateCombo = new javax.swing.JComboBox();
            incrementConstantField = new javax.swing.JTextField();
            initialParam2Field = new javax.swing.JTextField();
            initialParam1Field = new javax.swing.JTextField();
            initialParam1Label = new javax.swing.JLabel();
            initialParam2Label = new javax.swing.JLabel();
            jLabel5 = new javax.swing.JLabel();
            initialConstantField3 = new javax.swing.JTextField();
            incrParam1Label = new javax.swing.JLabel();
            incrParam1Field = new javax.swing.JTextField();
            incrParam2Label = new javax.swing.JLabel();
            incrParam2Field = new javax.swing.JTextField();

            jLabel1.setText("Initial Value:");

            java.awt.Font f = new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 10);
            initialConstantField.setFont(f);

            initialRandomVariateCombo.setFont(f);
            initialRandomVariateCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Uniform", "Normal", "Exponential" }));

            initialRadioGroup.add(initialConstantRadio);
            initialConstantRadio.setFont(f);
            initialConstantRadio.setText("Constant:");

            initialRadioGroup.add(initialRandomRadio);
            initialRandomRadio.setFont(f);
            initialRandomRadio.setText("Random Variate:");

            jLabel2.setText("Increment:");

            incrementRadioGroup.add(incrementConstantRadio);
            incrementConstantRadio.setFont(f);
            incrementConstantRadio.setText("Constant:");

            incrementRadioGroup.add(incrementRandomRadio);
            incrementRandomRadio.setFont(f);
            incrementRandomRadio.setText("Random Variate:");

            incrementRandomVariateCombo.setFont(f);
            incrementRandomVariateCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Uniform", "Normal", "Exponential" }));

            incrementConstantField.setFont(f);

            initialParam2Field.setFont(f);

            initialParam1Field.setFont(f);

            initialParam1Label.setFont(f);
            initialParam1Label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            initialParam1Label.setText("lower");

            initialParam2Label.setFont(f);
            initialParam2Label.setText("upper");

            jLabel5.setFont(f);
            jLabel5.setText("param1");

            initialConstantField3.setFont(f);

            incrParam1Label.setFont(f);
            incrParam1Label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            incrParam1Label.setText("lower");

            incrParam1Field.setFont(f);

            incrParam2Label.setFont(f);
            incrParam2Label.setText("upper");

            incrParam2Field.setFont(f);

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                            .addContainerGap()
                                            .addComponent(jLabel2))
                                            .addGroup(layout.createSequentialGroup()
                                                    .addContainerGap()
                                                    .addComponent(jLabel1))
                                                    .addGroup(layout.createSequentialGroup()
                                                            .addGap(10, 10, 10)
                                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                    .addComponent(initialRandomRadio)
                                                                    .addComponent(initialConstantRadio)))
                                                                    .addGroup(layout.createSequentialGroup()
                                                                            .addGap(10, 10, 10)
                                                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                    .addComponent(incrementRandomRadio)
                                                                                    .addComponent(incrementConstantRadio)))
                                                                                    .addGroup(layout.createSequentialGroup()
                                                                                            .addGap(62, 62, 62)
                                                                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                                    .addComponent(initialParam1Label, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                    .addComponent(incrParam1Label, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                                                            .addGroup(layout.createSequentialGroup()
                                                                                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                            .addComponent(initialParam1Field, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                            .addComponent(incrementConstantField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                            .addComponent(incrParam1Field, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                            .addComponent(initialConstantField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                                    .addComponent(initialParam2Label)
                                                                                                                                    .addComponent(incrParam2Label))
                                                                                                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                                            .addComponent(initialParam2Field, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                            .addComponent(incrParam2Field, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                                                                                                            .addComponent(initialRandomVariateCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                                                                                            .addComponent(incrementRandomVariateCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                                                                                                                            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    );
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(initialConstantRadio)
                                    .addComponent(initialConstantField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(initialRandomRadio)
                                            .addComponent(initialRandomVariateCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(initialParam2Label)
                                                    .addComponent(initialParam2Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(initialParam1Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(initialParam1Label))
                                                    .addGap(5, 5, 5)
                                                    .addComponent(jLabel2)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                            .addComponent(incrementConstantRadio)
                                                            .addComponent(incrementConstantField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                    .addComponent(incrementRandomRadio)
                                                                    .addComponent(incrementRandomVariateCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                            .addGroup(layout.createSequentialGroup()
                                                                                    .addGap(6, 6, 6)
                                                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                            .addComponent(incrParam1Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                            .addComponent(incrParam1Label)))
                                                                                            .addGroup(layout.createSequentialGroup()
                                                                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                            .addComponent(incrParam2Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                            .addComponent(incrParam2Label))))
                                                                                                            .addGap(14, 14, 14))
                    );
            //==============================  END OF GENERATED CODE =======================================


            //==============================  EVENT HANDLING CODE =========================================
            ItemListener l1 = new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    initialConstantField.setEnabled(initialConstantRadio.isSelected());
                    initialRandomVariateCombo.setEnabled(initialRandomRadio.isSelected());
                    initialParam1Label.setEnabled(initialRandomRadio.isSelected());
                    initialParam1Field.setEnabled(initialRandomRadio.isSelected());
                    initialParam2Label.setEnabled(initialRandomRadio.isSelected());
                    initialParam2Field.setEnabled(initialRandomRadio.isSelected());

                }
            };
            initialConstantRadio.addItemListener(l1);
            initialRandomRadio.addItemListener(l1);

            ItemListener l2 = new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    incrementConstantField.setEnabled(incrementConstantRadio.isSelected());
                    incrementRandomVariateCombo.setEnabled(incrementRandomRadio.isSelected());
                    incrParam1Label.setEnabled(incrementRandomRadio.isSelected());
                    incrParam1Field.setEnabled(incrementRandomRadio.isSelected());
                    incrParam2Label.setEnabled(incrementRandomRadio.isSelected());
                    incrParam2Field.setEnabled(incrementRandomRadio.isSelected());
                }
            };
            incrementConstantRadio.addItemListener(l2);
            incrementRandomRadio.addItemListener(l2);

            initialConstantRadio.setSelected(true);
            incrementConstantRadio.setSelected(true);

            initialRandomVariateCombo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (initialRandomVariateCombo.getSelectedItem().equals("Uniform")) {
                        initialParam1Label.setText("lower");
                        initialParam2Label.setText("upper");
                        initialParam2Label.setVisible(true);
                        initialParam2Field.setVisible(true);
                    } else if (initialRandomVariateCombo.getSelectedItem().equals("Normal")) {
                        initialParam1Label.setText("mean");
                        initialParam2Label.setText("stdev");
                        initialParam2Label.setVisible(true);
                        initialParam2Field.setVisible(true);
                    } else if (initialRandomVariateCombo.getSelectedItem().equals("Exponential")) {
                        initialParam1Label.setText("lambda");
                        initialParam2Label.setVisible(false);
                        initialParam2Field.setVisible(false);
                    }
                }
            });

            incrementRandomVariateCombo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (incrementRandomVariateCombo.getSelectedItem().equals("Uniform")) {
                        incrParam1Label.setText("lower");
                        incrParam2Label.setText("upper");
                        incrParam2Label.setVisible(true);
                        incrParam2Field.setVisible(true);
                    } else if (incrementRandomVariateCombo.getSelectedItem().equals("Normal")) {
                        incrParam1Label.setText("mean");
                        incrParam2Label.setText("stdev");
                        incrParam2Label.setVisible(true);
                        incrParam2Field.setVisible(true);
                    } else if (incrementRandomVariateCombo.getSelectedItem().equals("Exponential")) {
                        incrParam1Label.setText("lambda");
                        incrParam2Label.setVisible(false);
                        incrParam2Field.setVisible(false);
                    }
                }
            });
        }// </editor-fold>


        // Variables declaration - do not modify
        private javax.swing.JLabel incrParam1Label;
        private javax.swing.JTextField incrParam2Field;
        private javax.swing.JLabel incrParam2Label;
        private javax.swing.JTextField incrParam1Field;
        private javax.swing.JTextField incrementConstantField;
        private javax.swing.JRadioButton incrementConstantRadio;
        private javax.swing.ButtonGroup incrementRadioGroup;
        private javax.swing.JRadioButton incrementRandomRadio;
        private javax.swing.JComboBox incrementRandomVariateCombo;
        private javax.swing.JTextField initialConstantField;
        private javax.swing.JTextField initialConstantField3;
        private javax.swing.JRadioButton initialConstantRadio;
        private javax.swing.JTextField initialParam1Field;
        private javax.swing.JLabel initialParam1Label;
        private javax.swing.JTextField initialParam2Field;
        private javax.swing.JLabel initialParam2Label;
        private javax.swing.ButtonGroup initialRadioGroup;
        private javax.swing.JRadioButton initialRandomRadio;
        private javax.swing.JComboBox initialRandomVariateCombo;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel5;
        // End of variables declaration

        public void fillProperties(SequentialDomain domain) {
            Variate initialVariate = domain.getInitialVariate();
            Variate incrementVariate = domain.getIncrementVariate();

            if (initialVariate instanceof ConstantVariate) {
                initialConstantRadio.setSelected(true);
                initialConstantField.setText(((ConstantVariate) initialVariate).getValue() + "");
            } else {
                initialRandomRadio.setSelected(true);
                if (initialVariate instanceof RandomExponentialVariate) {
                    initialRandomVariateCombo.setSelectedItem("Exponential");
                    initialParam1Field.setText(((RandomExponentialVariate) initialVariate).getLambda() + "");
                } else if (initialVariate instanceof RandomNormalVariate) {
                    initialRandomVariateCombo.setSelectedItem("Normal");
                    initialParam1Field.setText(((RandomNormalVariate) initialVariate).getMean() + "");
                    initialParam2Field.setText(((RandomNormalVariate) initialVariate).getStdev() + "");
                } else if (initialVariate instanceof RandomUniformVariate) {
                    initialRandomVariateCombo.setSelectedItem("Uniform");
                    initialParam1Field.setText(((RandomUniformVariate) initialVariate).getLower() + "");
                    initialParam2Field.setText(((RandomUniformVariate) initialVariate).getUpper() + "");
                }
            }

            if (incrementVariate instanceof ConstantVariate) {
                incrementConstantRadio.setSelected(true);
                incrementConstantField.setText(((ConstantVariate) incrementVariate).getValue() + "");
            } else {
                incrementRandomRadio.setSelected(true);
                if (incrementVariate instanceof RandomExponentialVariate) {
                    incrementRandomVariateCombo.setSelectedItem("Exponential");
                } else if (incrementVariate instanceof RandomNormalVariate) {
                    incrementRandomVariateCombo.setSelectedItem("Normal");
                    incrParam1Field.setText(((RandomNormalVariate) incrementVariate).getMean() + "");
                    incrParam2Field.setText(((RandomNormalVariate) incrementVariate).getStdev() + "");
                } else if (incrementVariate instanceof RandomUniformVariate) {
                    incrementRandomVariateCombo.setSelectedItem("Uniform");
                    incrParam1Field.setText(((RandomUniformVariate) incrementVariate).getLower() + "");
                    incrParam2Field.setText(((RandomUniformVariate) incrementVariate).getUpper() + "");
                }
            }
        }

        public boolean validateFields() {
            return (
                    // checks initial value
                    (
                            (initialConstantRadio.isSelected() &&
                                    initialConstantField.getText() != null &&
                                    !initialConstantField.getText().isEmpty()
                                    )
                                    ||
                                    (
                                            initialRandomRadio.isSelected() &&
                                            initialParam1Field.getText() != null &&
                                            !initialParam1Field.getText().isEmpty() &&
                                            (
                                                    initialRandomVariateCombo.getSelectedItem().equals("Exponential") ||
                                                    initialParam2Field.getText() != null &&
                                                    !initialParam2Field.getText().isEmpty()
                                                    )
                                            )
                            )
                            &&
                            // checks increment
                            (
                                    (incrementConstantRadio.isSelected() &&
                                            incrementConstantField.getText()!= null &&
                                            !incrementConstantField.getText().isEmpty()
                                            )
                                            ||
                                            (
                                                    incrementRandomRadio.isSelected() &&
                                                    incrParam1Field.getText() != null &&
                                                    !incrParam1Field.getText().isEmpty() &&
                                                    (
                                                            incrementRandomVariateCombo.getSelectedItem().equals("Exponential") ||
                                                            incrParam2Field.getText() != null &&
                                                            !incrParam2Field.getText().isEmpty()
                                                            )
                                                    )
                                    )
                    );
        }
    }
    // ======================= End of SequentialDomainPanel class ============================



    // ===================== Internal class: PredefinedListDomainPanel =======================
    class PredefinedListDomainPanel extends javax.swing.JPanel {

        /** Creates new form SequentialDomainPanel */
        public PredefinedListDomainPanel() {
            initComponents();
        }

        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        // <editor-fold defaultstate="collapsed" desc="Generated Code">
        private void initComponents() {
            jScrollPane1 = new javax.swing.JScrollPane();
            itemsTable = new javax.swing.JTable();
            sameFreqCheck = new javax.swing.JCheckBox();
            addBtn = new javax.swing.JButton();
            deleteBtn = new javax.swing.JButton();

            itemsTable.setModel(new javax.swing.table.DefaultTableModel(
                    new Object [][] {

                    },
                    new String [] {
                            "Item", "Frequency"
                    }
                    ));
            jScrollPane1.setViewportView(itemsTable);

            sameFreqCheck.setText("Deterministic (items have same frequency)");

            addBtn.setFont(new java.awt.Font("Tahoma", 1, 11));
            addBtn.setText("+");
            addBtn.setToolTipText("Add item");

            deleteBtn.setFont(new java.awt.Font("Tahoma", 1, 11));
            deleteBtn.setText("-");
            deleteBtn.setToolTipText("Delete item");

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(sameFreqCheck)
                                    .addGroup(layout.createSequentialGroup()
                                            .addContainerGap()
                                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(deleteBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(addBtn))))
                                                    .addGap(4, 4, 4))
                    );
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                            .addComponent(addBtn)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(deleteBtn))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
                                                    .addGap(7, 7, 7)
                                                    .addComponent(sameFreqCheck)))
                                                    .addContainerGap())
                    );
            //==============================  END OF GENERATED CODE =======================================



            //===================================  CUSTOM CODE ============================================
            sameFreqCheck.setToolTipText("Items are generated in a predictable and repeatable order.)");

            addBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();
                    model.addRow(new Object[]{null, null});
                    itemsTable.changeSelection(model.getRowCount() - 1, 0, false, false);
                    itemsTable.editCellAt(model.getRowCount() - 1, 0);
                }

            });

            deleteBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int selected = itemsTable.getSelectedRow();
                    if (selected > -1) {
                        ((DefaultTableModel) itemsTable.getModel()).removeRow(selected);
                    } else {
                        JOptionPane.showMessageDialog(null, "Select an item to delete");
                    }
                }

            });


        }// </editor-fold>


        // Variables declaration - do not modify
        private javax.swing.JButton addBtn;
        private javax.swing.JButton deleteBtn;
        private javax.swing.JTable itemsTable;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JCheckBox sameFreqCheck;
        // End of variables declaration


        public void fillProperties(PredefinedListDomain domain) {
            sameFreqCheck.setSelected(domain.isDeterministic());

            DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();

            // Clears the table
            int rowCount = model.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                model.removeRow(0);
            }


            if (domain.isDeterministic()) {
                for (Object item : domain.getItems()) {
                    model.addRow(new Object[]{item, "1.0"});
                }
            } else {
                for (Entry<Object, Double> e : domain.getItemMix().entrySet()) {
                    model.addRow(new Object[]{e.getKey(), Double.toString(e.getValue())});
                }

            }

            if (dataTypeCombo.getSelectedItem().equals("BOOLEAN")) {
                domainTypeCombo.setSelectedItem("Predefined List");
                predefinedPanel.addBtn.setEnabled(false);
                predefinedPanel.deleteBtn.setEnabled(false);
                domainTypeCombo.setEnabled(false);
            }
        }

        public boolean validateFields(String dataType) throws Exception {
            DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();
            if (model.getRowCount() < 1) {
                return false;
            } else {
                String item;
                for (int i = 0; i < model.getRowCount(); i++) {
                    item = (String) model.getValueAt(i, 0);
                    if (item == null || item.isEmpty()
                            || (!sameFreqCheck.isSelected() && (model.getValueAt(i, 1) == null || model.getValueAt(i, 1).equals("")))) {
                        return false;
                    }

                    String type = "datatype";
                    try {
                        if (dataType.equals("BOOLEAN")) {
                            if (item.equalsIgnoreCase("True")
                                    || item.equalsIgnoreCase("Yes")
                                    || item.equalsIgnoreCase("1")) {
                                model.setValueAt("true", i, 0);
                            } else if (item.equalsIgnoreCase("False")
                                    || item.equalsIgnoreCase("No")
                                    || item.equalsIgnoreCase("0")) {
                                model.setValueAt("false", i, 0);
                            } else {
                                throw new Exception("\"" + item + "\" is not a valid BOOLEAN. (row " + (i+1)+").");
                            }
                        }
                        if (dataType.equals("DOUBLE")) {
                            type = "DOUBLE";
                            Double.parseDouble(item);
                        } else if (dataType.equals("FLOAT")) {
                            type = "FLOAT";
                            Float.parseFloat(item);
                        } else if (dataType.equals("INTEGER")) {
                            type = "INTEGER";
                            Integer.parseInt(item);
                        } else if (dataType.equals("LONG")) {
                            type = "LONG";
                            Long.parseLong(item);
                        } else if (dataType.equals("TIMESTAMP")) {
                            type = "TIMESTAMP";
                            Long.parseLong(item);
                        }
                    } catch (NumberFormatException nfe) {
                        throw new Exception("\"" + item + "\" is not a valid " + type +
                                ". (row " + (i + 1) + ").");
                    }
                }
                return true;
            }
        }
    }
    // ======================= End of PredefinedListDomainPanel class ========================


    // ===================== Internal class: PredefinedListDomainPanel =======================
    class RandomlDomainPanel extends javax.swing.JPanel {

        /** Creates new form SequentialDomainPanel */
        public RandomlDomainPanel() {
            initComponents();
        }

        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        // <editor-fold defaultstate="collapsed" desc="Generated Code">
        private void initComponents() {
            distrCombo = new javax.swing.JComboBox();
            param1Field = new javax.swing.JTextField();
            param2Label = new javax.swing.JLabel();
            param1Label = new javax.swing.JLabel();
            param2Field = new javax.swing.JTextField();
            distrLabel = new javax.swing.JLabel();

            distrCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Uniform", "Normal", "Exponential" }));

            param2Label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            param2Label.setText("upper");

            param1Label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            param1Label.setText("lower");

            distrLabel.setText("Distribution");

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(distrLabel)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(distrCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(layout.createSequentialGroup()
                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                            .addComponent(param1Label, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(param2Label, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                                    .addComponent(param2Field, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                                                                    .addComponent(param1Field, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)))))
                                                                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    );
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(distrLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(distrCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(8, 8, 8)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(param1Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(param1Label))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(param2Label)
                                            .addComponent(param2Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addContainerGap(21, Short.MAX_VALUE))
                    );
            //==============================  END OF GENERATED CODE =======================================



            //==============================  EVENT HANDLING CODE =========================================
            distrCombo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (distrCombo.getSelectedItem().equals("Uniform")) {
                        param1Label.setText("lower");
                        param2Label.setText("upper");
                        param2Label.setVisible(true);
                        param2Field.setVisible(true);
                    } else if (distrCombo.getSelectedItem().equals("Normal")) {
                        param1Label.setText("mean");
                        param2Label.setText("stdev");
                        param2Label.setVisible(true);
                        param2Field.setVisible(true);
                    } else if (distrCombo.getSelectedItem().equals("Exponential")) {
                        param1Label.setText("lambda");
                        param2Label.setVisible(false);
                        param2Field.setVisible(false);
                    }
                }
            });

        }// </editor-fold>


        private void fillProperties(RandomDomain domain) {
            Variate v = domain.getVariate();

            if (v instanceof RandomExponentialVariate) {
                distrCombo.setSelectedItem("Exponential");
                param1Field.setText(((RandomExponentialVariate) v).getLambda() + "");
            } else if (v instanceof RandomNormalVariate) {
                distrCombo.setSelectedItem("Normal");
                param1Field.setText(((RandomNormalVariate) v).getMean() + "");
                param2Field.setText(((RandomNormalVariate) v).getStdev() + "");
            } else if (v instanceof RandomUniformVariate) {
                distrCombo.setSelectedItem("Uniform");
                param1Field.setText(((RandomUniformVariate) v).getLower() + "");
                param2Field.setText(((RandomUniformVariate) v).getUpper() + "");
            }
        }


        public boolean validateFields() {
            return (param1Field.getText() != null &&
                    !param1Field.getText().isEmpty() &&
                    (
                            distrCombo.getSelectedItem().equals("Exponential") ||
                            param2Field.getText() != null &&
                            !param2Field.getText().isEmpty()
                            )
                    );
        }

        // Variables declaration - do not modify
        private javax.swing.JComboBox distrCombo;
        private javax.swing.JLabel distrLabel;
        private javax.swing.JTextField param1Field;
        private javax.swing.JLabel param1Label;
        private javax.swing.JTextField param2Field;
        private javax.swing.JLabel param2Label;
        // End of variables declaration

    }
    // ======================= End of RandomDomainPanel class ========================
}


