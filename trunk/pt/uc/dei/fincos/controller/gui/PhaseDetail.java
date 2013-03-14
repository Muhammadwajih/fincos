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


package pt.uc.dei.fincos.controller.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import pt.uc.dei.fincos.basic.Attribute;
import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.driver.ExternalFileWorkloadPhase;
import pt.uc.dei.fincos.driver.Scheduler.ArrivalProcess;
import pt.uc.dei.fincos.driver.SyntheticWorkloadPhase;
import pt.uc.dei.fincos.driver.WorkloadPhase;


/**
 * Form for configuring the properties of a workload phase of a Driver.
 *
 * @author  Marcelo R.N. Mendes
 */
public final class PhaseDetail extends ComponentDetail {

    /** serial id. */
    private static final long serialVersionUID = 4634783547971155251L;

    /** List of configured types for this phase (if it is synthetic). */
    protected ArrayList<EventType> syntheticTypes;

    /** Old phase configuration (used when in EDIT mode). */
    private WorkloadPhase oldCfg;

    /** UI for synthetic phases. */
    private SyntheticPhasePanel syntheticPanel;

    /** UI for phases based on data files. */
    private ExternalFilePhasePanel externalFilePanel;

    /** The parent component of this form. */
    private final DriverDetail parent;

    /**
     * Creates new form PhaseDetail.
     *
     * @param parent    parent form
     * @param phase     Workload phase configuration properties, when in UPDATE mode,
     *                  or <tt>null</tt>, in INSERTION mode.
     */
    public PhaseDetail(DriverDetail parent, WorkloadPhase phase) {
        super(parent);
        syntheticTypes = new ArrayList<EventType>();
        syntheticPanel = new SyntheticPhasePanel(this);
        externalFilePanel = new ExternalFilePhasePanel();

        initComponents();
        addListeners();

        if (phase != null) {
            this.oldCfg = phase;
            this.op = UPDATE;
            fillProperties(phase);
        } else {
            this.op = INSERT;
            setTitle("New Phase");
            externalFileRadio.setSelected(true);
            syntheticRadio.setSelected(true);
        }

        this.parent = parent;

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

        phaseTypeRadioGroup = new javax.swing.ButtonGroup();
        workloadPanel = new javax.swing.JPanel();
        externalFileRadio = new javax.swing.JRadioButton();
        syntheticRadio = new javax.swing.JRadioButton();
        phaseDetailPanel = new javax.swing.JPanel();
        cancelBtn = new javax.swing.JButton();
        okBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Phase Detail");
        setMinimumSize(new java.awt.Dimension(497, 526));

        workloadPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Workload"));

        phaseTypeRadioGroup.add(externalFileRadio);
        externalFileRadio.setText("External File"); // NOI18N

        phaseTypeRadioGroup.add(syntheticRadio);
        syntheticRadio.setSelected(true);
        syntheticRadio.setText("Synthetic"); // NOI18N

        javax.swing.GroupLayout workloadPanelLayout = new javax.swing.GroupLayout(workloadPanel);
        workloadPanel.setLayout(workloadPanelLayout);
        workloadPanelLayout.setHorizontalGroup(
            workloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(workloadPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(syntheticRadio)
                .addGap(18, 18, 18)
                .addComponent(externalFileRadio)
                .addContainerGap(285, Short.MAX_VALUE))
        );
        workloadPanelLayout.setVerticalGroup(
            workloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(workloadPanelLayout.createSequentialGroup()
                .addGroup(workloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(syntheticRadio)
                    .addComponent(externalFileRadio))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        phaseDetailPanel.setMaximumSize(new java.awt.Dimension(480, 397));
        phaseDetailPanel.setMinimumSize(new java.awt.Dimension(450, 397));
        phaseDetailPanel.setPreferredSize(new java.awt.Dimension(477, 400));
        phaseDetailPanel.setLayout(new java.awt.BorderLayout());

        cancelBtn.setText("Cancel"); // NOI18N
        cancelBtn.setPreferredSize(null);

        okBtn.setText("OK"); // NOI18N
        okBtn.setPreferredSize(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(phaseDetailPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
                    .addComponent(workloadPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addComponent(workloadPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(phaseDetailPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addListeners() {
        phaseDetailPanel.setLayout(new BorderLayout());
        phaseDetailPanel.setPreferredSize(new Dimension(300, 275));

        ItemListener radioListener1 = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (syntheticRadio.isSelected()) {
                    phaseDetailPanel.removeAll();
                    phaseDetailPanel.add(syntheticPanel, BorderLayout.CENTER);
                    phaseDetailPanel.revalidate();
                    phaseDetailPanel.repaint();
                } else if (externalFileRadio.isSelected()) {
                    phaseDetailPanel.removeAll();
                    phaseDetailPanel.add(externalFilePanel, BorderLayout.CENTER);
                    phaseDetailPanel.revalidate();
                    phaseDetailPanel.repaint();
                }
            }
        };
        syntheticRadio.addItemListener(radioListener1);
        externalFileRadio.addItemListener(radioListener1);

        syntheticRadio.setSelected(true);

        okBtn.setIcon(new ImageIcon("imgs/ok.png"));
        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (syntheticPanel.schemaTable.getCellEditor() != null) {
                        syntheticPanel.schemaTable.getCellEditor().stopCellEditing();
                    }
                    if (validateFields()) {
                        WorkloadPhase newCfg;

                        if (syntheticRadio.isSelected()) { // synthetic data
                            LinkedHashMap<EventType, Double> schema =
                                    new LinkedHashMap<EventType, Double>();

                            DefaultTableModel model =
                                    (DefaultTableModel) syntheticPanel.schemaTable.getModel();

                            Long randomSeed = null;
                            if (syntheticPanel.dataGenSeedCheck.isSelected()) {
                                String rndSeedStr = syntheticPanel.dataGenSeedField.getText();
                                randomSeed = Long.parseLong(rndSeedStr);
                            }

                            for (int i = 0; i < model.getRowCount() - 1; i++) {
                                for (Attribute att : syntheticTypes.get(i).getAttributes()) {
                                    att.getDomain().setRandomSeed(randomSeed);
                                }
                                String mixStr =
                                        (String) syntheticPanel.schemaTable.getValueAt(i, 2);
                                double mix = Double.parseDouble(mixStr);
                                schema.put(syntheticTypes.get(i), mix);
                            }

                            int dataGenMode = syntheticPanel.dataGenRTRadio.isSelected()
                                              ? SyntheticWorkloadPhase.RUNTIME
                                              : SyntheticWorkloadPhase.DATASET;

                            ArrivalProcess arrivalProcess = ArrivalProcess.DETERMINISTIC;
                            if (syntheticPanel.poissonCheckBox.isSelected()) {
                                arrivalProcess = ArrivalProcess.POISSON;
                            }

                            newCfg = new SyntheticWorkloadPhase(
                                    Integer.parseInt(syntheticPanel.durationTextField.getText()),
                                    Double.parseDouble(syntheticPanel.initialRateTextField.getText()),
                                    Double.parseDouble(syntheticPanel.finalRateTextField.getText()),
                                    arrivalProcess, schema, syntheticPanel.deterministicMixCheckBox.isSelected(),
                                    dataGenMode, randomSeed);
                        } else { // external file
                            double rate = 1;

                            if (externalFilePanel.useFixedRateRdBtn.isSelected()) {
                                rate = Double.parseDouble(externalFilePanel.rateField.getText());
                            }

                            int loopCount;
                            try {
                                loopCount = (Integer) externalFilePanel.loopSpinner.getValue();
                                if (loopCount < 1) {
                                    throw new NumberFormatException();
                                }
                            } catch (NumberFormatException nfe) {
                                JOptionPane.showMessageDialog(null, "Invalid loop count.");
                                return;
                            }

                            newCfg = new ExternalFileWorkloadPhase(
                                    externalFilePanel.filePathField.getText(),
                                    externalFilePanel.getDelimiter(false),
                                    externalFilePanel.tsCheckBox.isSelected(),
                                    externalFilePanel.useTSRdBtn.isSelected(),
                                    externalFilePanel.tsUnitCombo.getSelectedIndex(),
                                    externalFilePanel.getTSIndex(),
                                    externalFilePanel.sendTSCheck.isSelected(),
                                    externalFilePanel.typeYesRdBtn.isSelected(),
                                    externalFilePanel.getTypeIndex(),
                                    externalFilePanel.typeField.getText(),
                                    loopCount,
                                    rate);
                        }

                        switch (op) {
                            case UPDATE:
                                parent.updatePhase(oldCfg, newCfg);
                                dispose();
                                break;
                            case INSERT:
                                parent.addPhase(newCfg);
                                dispose();
                        }


                    } else {
                        JOptionPane.showMessageDialog(null,
                                                      "One or more required fields "
                                                    + "were not correctly filled.",
                                                      "Invalid Input",
                                                      JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null,
                                                  "Invalid value at Synthetic workload table");
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(null, exc.getMessage());
                }

            }
        });

        cancelBtn.setIcon(new ImageIcon("imgs/cancel.png"));
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    /**
     * Creates a form for configuring a data type.
     *
     * @param type Data type configuration properties, when in UPDATE mode, or
     * <tt>null</tt>, in INSERTION mode.
     * @return a reference to the just created form
     */
    protected TypeDetail openTypeDetail(EventType type) {
        long randomSeed = 0;
        if (syntheticPanel.dataGenSeedField.getText() != null
                && !syntheticPanel.dataGenSeedField.getText().isEmpty()) {
            try {
                randomSeed = Long.parseLong(syntheticPanel.dataGenSeedField.getText());
            } catch (NumberFormatException e) {
                System.err.println("WARN: Invalid random number generation seed.");
            }
        }
        return new TypeDetail(this, type, randomSeed);
    }

    /**
     * Fills the UI with the phase properties passed as argument.
     *
     * @param phase Phase configuration properties to be shown in UI, when in
     * UPDATE mode, or <tt>null</tt>, in INSERTION mode.
     */
    public void fillProperties(WorkloadPhase phase) {
        if (phase instanceof SyntheticWorkloadPhase) {
            SyntheticWorkloadPhase synthPhase = (SyntheticWorkloadPhase) phase;
            externalFileRadio.setSelected(true);
            syntheticRadio.setSelected(true);
            syntheticPanel.fillProperties(synthPhase);
        } else if (phase instanceof ExternalFileWorkloadPhase) {
            ExternalFileWorkloadPhase filePhase = (ExternalFileWorkloadPhase) phase;
            externalFileRadio.setSelected(true);
            externalFilePanel.fillProperties(filePhase);
        }

        this.validate();
        this.repaint();
    }

    /**
     * Updates a data type from this (synthetic) phase.
     *
     * @param oldType   the old configuration of the type
     * @param newType   the new configuration of the type
     */
    public void updateEventType(EventType oldType, EventType newType) {
        int index = this.syntheticTypes.indexOf(oldType);

        if (index > -1) {
            String mix = (String) syntheticPanel.schemaTable.getValueAt(index, 2);
            removeEventType(index);
            addEventType(index, newType);
            syntheticPanel.schemaTable.setValueAt(mix, index, 2);
        }
    }

    /**
     * Adds a data type to this (synthetic) phase.
     *
     * @param index     data type index
     * @param newType   the new data type
     */
    public void addEventType(int index, EventType newType) {
        this.syntheticTypes.add(index, newType);
        Object[] row = new Object[] {newType.getName(),
                                     newType.getAttributesNamesList(),
                                     "1.0"};
        ((DefaultTableModel) syntheticPanel.schemaTable.getModel()).insertRow(index, row);
    }

    /**
     * Adds a data type to this (synthetic) phase.
     *
     * @param newType   the new data type
     */
    public void addEventType(EventType newType) {
        this.syntheticTypes.add(newType);
        DefaultTableModel model = ((DefaultTableModel) syntheticPanel.schemaTable.getModel());
        Object[] row = new Object[] {newType.getName(),
                                     newType.getAttributesNamesList(),
                                     "1.0"};
        model.insertRow(model.getRowCount() - 1, row);
    }

    /**
     * Removes a data type from this (synthetic) phase.
     *
     * @param index     the index of the type to be removed
     */
    private void removeEventType(int index) {
        syntheticTypes.remove(index);
        ((DefaultTableModel) syntheticPanel.schemaTable.getModel()).removeRow(index);
    }

    @Override
    protected boolean validateFields() {
        boolean ret = true;
        if (syntheticRadio.isSelected()) { // synthetic data
            if (syntheticPanel.durationTextField.getText() == null
             || syntheticPanel.durationTextField.getText().isEmpty()) {
                syntheticPanel.durationTextField.setBackground(INVALID_INPUT_COLOR);
                ret = false;
            } else {
                try {
                    String duration = syntheticPanel.durationTextField.getText();
                    Integer.parseInt(duration);
                    Color defaultColor = UIManager.getColor("TextField.background");
                    syntheticPanel.durationTextField.setBackground(defaultColor);
                } catch (NumberFormatException nfe) {
                    syntheticPanel.durationTextField.setBackground(INVALID_INPUT_COLOR);
                    ret = false;
                }
            }

            if (syntheticPanel.initialRateTextField.getText() == null
             || syntheticPanel.initialRateTextField.getText().isEmpty()) {
                syntheticPanel.initialRateTextField.setBackground(INVALID_INPUT_COLOR);
                ret = false;
            } else {
                try {
                    String initRate = syntheticPanel.initialRateTextField.getText();
                    Double.parseDouble(initRate);
                    Color defaultColor = UIManager.getColor("TextField.background");
                    syntheticPanel.initialRateTextField.setBackground(defaultColor);
                } catch (NumberFormatException nfe) {
                    syntheticPanel.initialRateTextField.setBackground(INVALID_INPUT_COLOR);
                    ret = false;
                }
            }

            if (syntheticPanel.finalRateTextField.getText() == null
             || syntheticPanel.finalRateTextField.getText().isEmpty()) {
                syntheticPanel.finalRateTextField.setBackground(INVALID_INPUT_COLOR);
                ret = false;
            } else {
                try {
                    String finalRate = syntheticPanel.finalRateTextField.getText();
                    Double.parseDouble(finalRate);
                    Color defaultColor = UIManager.getColor("TextField.background");
                    syntheticPanel.finalRateTextField.setBackground(defaultColor);
                } catch (NumberFormatException nfe) {
                    syntheticPanel.finalRateTextField.setBackground(INVALID_INPUT_COLOR);
                    ret = false;
                }
            }

            if (syntheticPanel.schemaTable.getRowCount() <= 1) {
                syntheticPanel.schemaTable.setBackground(INVALID_INPUT_COLOR);
                ret = false;
            } else {
                Color defaultColor = UIManager.getColor("Table.background");
                syntheticPanel.schemaTable.setBackground(defaultColor);
            }

            if (syntheticPanel.dataGenSeedCheck.isSelected()) {
                if (syntheticPanel.dataGenSeedField.getText() == null
                 || syntheticPanel.dataGenSeedField.getText().isEmpty()) {
                    syntheticPanel.dataGenSeedField.setBackground(INVALID_INPUT_COLOR);
                    ret = false;
                } else {
                    try {
                        String rndSeed = syntheticPanel.dataGenSeedField.getText();
                        Long.parseLong(rndSeed);
                        Color defaultColor = UIManager.getColor("TextField.background");
                        syntheticPanel.dataGenSeedField.setBackground(defaultColor);
                    } catch (NumberFormatException nfe) {
                        syntheticPanel.dataGenSeedField.setBackground(INVALID_INPUT_COLOR);
                        ret = false;
                    }
                }
            }

        } else { // external file
            if (externalFilePanel.filePathField.getText() == null
             || externalFilePanel.filePathField.getText().isEmpty()) {
                externalFilePanel.filePathField.setBackground(INVALID_INPUT_COLOR);
                ret = false;
            } else {
                Color defaultColor = UIManager.getColor("TextField.background");
                externalFilePanel.filePathField.setBackground(defaultColor);
            }

            if (externalFilePanel.otherRdBtn.isSelected()) {
                if (externalFilePanel.otherCharField.getText() == null
                 || externalFilePanel.otherCharField.getText().isEmpty()) {
                    externalFilePanel.otherCharField.setBackground(INVALID_INPUT_COLOR);
                    ret = false;
                } else {
                    Color defaultColor = UIManager.getColor("TextField.background");
                    externalFilePanel.otherCharField.setBackground(defaultColor);
                }
            }

            if (externalFilePanel.useFixedRateRdBtn.isSelected()) {
                if (externalFilePanel.rateField.getText() == null
                 || externalFilePanel.rateField.getText().isEmpty()) {
                    externalFilePanel.rateField.setBackground(INVALID_INPUT_COLOR);
                    ret = false;
                } else {
                    try {
                        String rate = externalFilePanel.rateField.getText();
                        Double.parseDouble(rate);
                        Color defaultColor = UIManager.getColor("TextField.background");
                        externalFilePanel.rateField.setBackground(defaultColor);
                    } catch (NumberFormatException nfe) {
                        externalFilePanel.rateField.setBackground(INVALID_INPUT_COLOR);
                        ret = false;
                    }
                }
            }

            if (externalFilePanel.typeNoRdBtn.isSelected()) {
                if (externalFilePanel.typeField.getText() == null
                 || externalFilePanel.typeField.getText().isEmpty()) {
                    externalFilePanel.typeField.setBackground(INVALID_INPUT_COLOR);
                    ret = false;
                } else {
                    Color defaultColor = UIManager.getColor("TextField.background");
                    externalFilePanel.typeField.setBackground(defaultColor);
                }
            }

           /* return (externalFilePanel.filePathField.getText() != null
                    && !externalFilePanel.filePathField.getText().isEmpty()
                    && (externalFilePanel.tsCheckBox.isSelected()
                        || (!externalFilePanel.tsCheckBox.isSelected()
                            && externalFilePanel.rateField.getText() != null
                            && !externalFilePanel.rateField.getText().isEmpty()))
                    && (externalFilePanel.typeYesRdBtn.isSelected()
                       || (externalFilePanel.typeNoRdBtn.isSelected()
                           && externalFilePanel.typeField.getText() != null
                           && !externalFilePanel.typeField.getText().isEmpty())
                    )
            );*/
        }
        return ret;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelBtn;
    private javax.swing.JRadioButton externalFileRadio;
    private javax.swing.JButton okBtn;
    private javax.swing.JPanel phaseDetailPanel;
    private javax.swing.ButtonGroup phaseTypeRadioGroup;
    private javax.swing.JRadioButton syntheticRadio;
    private javax.swing.JPanel workloadPanel;
    // End of variables declaration//GEN-END:variables

}
