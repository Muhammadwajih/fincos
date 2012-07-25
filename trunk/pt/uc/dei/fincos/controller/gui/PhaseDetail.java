
package pt.uc.dei.fincos.controller.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
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
@SuppressWarnings("serial")
public class PhaseDetail extends ComponentDetail {

    protected ArrayList<EventType> syntheticTypes = new ArrayList<EventType>();
    private WorkloadPhase oldCfg;
    private SyntheticPhasePanel syntheticPanel;
    private ExternalFilePhasePanel externalFilePanel;
    DriverDetail parent;

    /**
     * Creates new form PhaseDetail.
     *
     * @param parent    parent form
     * @param phase     Workload phase configuration properties, when in UPDATE mode,
     *                  or <tt>null</tt>, in INSERTION mode.
     */
    public PhaseDetail(DriverDetail parent, WorkloadPhase phase) {
        super(parent);
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
            syntheticRadio.setSelected(true);
        }

        this.parent = parent;
    }


    private void initComponents() {
        phaseTypeRadioGroup = new javax.swing.ButtonGroup();
        workloadPanel = new javax.swing.JPanel();
        externalFileRadio = new javax.swing.JRadioButton();
        syntheticRadio = new javax.swing.JRadioButton();
        okBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        phaseDetailPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Phase Detail"); // NOI18N
        setResizable(false);

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

        okBtn.setText("OK"); // NOI18N
        okBtn.setPreferredSize(null);

        cancelBtn.setText("Cancel"); // NOI18N
        cancelBtn.setPreferredSize(null);

        phaseDetailPanel.setLayout(new java.awt.BorderLayout());

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
                .addComponent(phaseDetailPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();

        this.setLocationRelativeTo(null); //screen center
        this.setVisible(true);
    }

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

        okBtn.setIcon(new ImageIcon("imgs/OK.png"));
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
                            LinkedHashMap<EventType, Double> schema = new LinkedHashMap<EventType, Double>();

                            DefaultTableModel model =
                                (DefaultTableModel) syntheticPanel.schemaTable.getModel();

                            Long randomSeed = null;
                            if (syntheticPanel.dataGenSeedCheck.isSelected()) {
                                try {
                                    randomSeed = Long.parseLong(syntheticPanel.dataGenSeedField.getText());
                                } catch (NumberFormatException nfe) {
                                    JOptionPane.showMessageDialog(null, "Invalid seed value.");
                                    return;
                                }
                            }

                            for (int i = 0; i < model.getRowCount() - 1; i++) {
                                for (Attribute att : syntheticTypes.get(i).getAttributes()) {
                                    att.getDomain().setRandomSeed(randomSeed);
                                }
                                schema.put(syntheticTypes.get(i),
                                        Double.parseDouble((String) syntheticPanel.schemaTable.getValueAt(i, 2)));
                            }

                            int dataGenMode =
                                syntheticPanel.dataGenRTRadio.isSelected() ? SyntheticWorkloadPhase.RUNTIME
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
                                    dataGenMode, randomSeed
                            );
                        } else  { // external file
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
                                    externalFilePanel.getDelimiter(),
                                    externalFilePanel.tsCheckBox.isSelected(),
                                    externalFilePanel.useTSRdBtn.isSelected(),
                                    externalFilePanel.tsUnitCombo.getSelectedIndex(),
                                    externalFilePanel.getTSIndex(),
                                    externalFilePanel.typeYesRdBtn.isSelected(),
                                    externalFilePanel.getTypeIndex(),
                                    externalFilePanel.typeField.getText(),
                                    loopCount,
                                    rate
                            );
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
                        JOptionPane.showMessageDialog(null, "One or more required fields were not correctly filled.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Invalid value at Synthetic workload table");
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(null, exc.getMessage());
                }

            }

        });

        cancelBtn.setIcon(new ImageIcon("imgs/Cancel.png"));
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
     * @param type      Data type configuration properties, when in UPDATE mode,
     *                  or <tt>null</tt>, in INSERTION mode.
     * @return          a reference to the just created form
     */
    protected TypeDetail openTypeDetail(EventType type) {
        long randomSeed = 0;
        if (syntheticPanel.dataGenSeedField.getText() != null
                && !syntheticPanel.dataGenSeedField.getText().isEmpty()) {
            try {
                randomSeed = Long.parseLong(syntheticPanel.dataGenSeedField.getText());
            } catch (NumberFormatException e) {
                ;
            }
        }
        return new TypeDetail(this, type, randomSeed);
    }

    /**
     * Fills the UI with the phase properties passed as argument.
     *
     * @param phase     Phase configuration properties to be shown in UI, when in UPDATE mode,
     *                  or <tt>null</tt>, in INSERTION mode.
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
        ((DefaultTableModel) syntheticPanel.schemaTable.getModel()).insertRow(index, new Object[] {newType.getName(), newType.getAttributesNamesList(), "1.0"});
    }

    /**
     * Adds a data type to this (synthetic) phase.
     *
     * @param newType   the new data type
     */
    public void addEventType(EventType newType) {
        this.syntheticTypes.add(newType);
        DefaultTableModel model = ((DefaultTableModel) syntheticPanel.schemaTable.getModel());
        model.insertRow(model.getRowCount() - 1, new Object[] {newType.getName(), newType.getAttributesNamesList(), "1.0"});
    }

    private void removeEventType(int index) {
        syntheticTypes.remove(index);
        ((DefaultTableModel) syntheticPanel.schemaTable.getModel()).removeRow(index);
    }

    private boolean validateFields() {
        if (syntheticRadio.isSelected()) { // synthetic data
            return (syntheticPanel.durationTextField.getText() != null &&
                    !syntheticPanel.durationTextField.getText().isEmpty() &&
                    syntheticPanel.initialRateTextField.getText() != null &&
                    !syntheticPanel.initialRateTextField.getText().isEmpty() &&
                    syntheticPanel.finalRateTextField.getText() != null &&
                    !syntheticPanel.finalRateTextField.getText().isEmpty() &&
                    syntheticPanel.schemaTable.getRowCount() > 1 &&
                    (!syntheticPanel.dataGenSeedCheck.isSelected() ||
                            syntheticPanel.dataGenSeedField.getText() != null &&
                            !syntheticPanel.dataGenSeedField.getText().isEmpty()
                    )
            );
        } else { // external file
            return (externalFilePanel.filePathField.getText() != null &&
                    !externalFilePanel.filePathField.getText().isEmpty() &&
                    (externalFilePanel.tsCheckBox.isSelected() ||
                            (!externalFilePanel.tsCheckBox.isSelected() && externalFilePanel.rateField.getText() != null && !externalFilePanel.rateField.getText().isEmpty())
                    ) &&
                    (externalFilePanel.typeYesRdBtn.isSelected() ||
                            (externalFilePanel.typeNoRdBtn.isSelected() && externalFilePanel.typeField.getText() != null && !externalFilePanel.typeField.getText().isEmpty())
                    )
            );
        }

    }

    private javax.swing.JButton cancelBtn;
    private javax.swing.JRadioButton externalFileRadio;
    private javax.swing.JButton okBtn;
    private javax.swing.JPanel phaseDetailPanel;
    private javax.swing.ButtonGroup phaseTypeRadioGroup;

    private javax.swing.JRadioButton syntheticRadio;
    private javax.swing.JPanel workloadPanel;
}
