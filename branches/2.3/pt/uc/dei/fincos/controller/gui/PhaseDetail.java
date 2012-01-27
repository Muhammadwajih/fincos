
package pt.uc.dei.fincos.controller.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import pt.uc.dei.fincos.basic.Attribute;
import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.basic.Globals;
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

    private JFileChooser fileChooser;
    private ArrayList<EventType> syntheticTypes = new ArrayList<EventType>();
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
        syntheticPanel = new SyntheticPhasePanel();
        externalFilePanel = new ExternalFilePhasePanel();

        initComponents();

        if (phase != null) {
            this.oldCfg = phase;
            this.op = UPDATE;
            fillProperties(phase);
        } else {
            this.op = INSERT;
            setTitle("New Phase");
            syntheticRadio.setSelected(true);
        }

        fileChooser = new JFileChooser(Globals.APP_PATH);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("CSV data file", "csv"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        this.parent = parent;
    }


    private void initComponents() {
        //================================= GENERATED CODE ==========================================
        phaseTypeRadioGroup = new javax.swing.ButtonGroup();
        schemaTablePopup = new javax.swing.JPopupMenu();
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
                        .addContainerGap(283, Short.MAX_VALUE))
        );
        workloadPanelLayout.setVerticalGroup(
                workloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(workloadPanelLayout.createSequentialGroup()
                        .addGroup(workloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                .addComponent(externalFileRadio)
                                .addComponent(syntheticRadio))
                                .addContainerGap(3, Short.MAX_VALUE))
        );

        okBtn.setText("OK"); // NOI18N
        okBtn.setPreferredSize(null);

        cancelBtn.setText("Cancel"); // NOI18N
        cancelBtn.setPreferredSize(null);

        javax.swing.GroupLayout phaseDetailPanelLayout = new javax.swing.GroupLayout(phaseDetailPanel);
        phaseDetailPanel.setLayout(phaseDetailPanelLayout);
        phaseDetailPanelLayout.setHorizontalGroup(
                phaseDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 475, Short.MAX_VALUE)
        );
        phaseDetailPanelLayout.setVerticalGroup(
                phaseDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 349, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(phaseDetailPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addContainerGap())
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(workloadPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addContainerGap())
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                        .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(okBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addContainerGap())))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(workloadPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(phaseDetailPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(okBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        pack();
        //--------------------------------- End of Generated Code ---------------------------------



        //=====================================  CUSTOM CODE =========================================
        java.awt.Font f = new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11);
        syntheticPanel.durationLabel2.setFont(f);
        syntheticPanel.rateLabel.setFont(f);

        syntheticPanel.dataGenSeedCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syntheticPanel.dataGenSeedField.setEnabled(syntheticPanel.dataGenSeedCheck.isSelected());
            }
        });

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

                            if (!externalFilePanel.timestampRadioYES.isSelected()) {
                                rate = Double.parseDouble(externalFilePanel.rateField.getText());
                            }

                            int loopCount;
                            try {
                                loopCount = Integer.parseInt(externalFilePanel.loopCountField.getText());
                                if (loopCount < 1) {
                                    throw new NumberFormatException();
                                }
                            } catch (NumberFormatException nfe) {
                                JOptionPane.showMessageDialog(null, "Invalid loop count.");
                                return;
                            }

                            newCfg = new ExternalFileWorkloadPhase(
                                    externalFilePanel.externalFilePathField.getText(),
                                    loopCount,
                                    !externalFilePanel.timestampRadioNO.isSelected(),
                                    externalFilePanel.timestampRadioYES.isSelected(),
                                    externalFilePanel.timestampUnitCombo.getSelectedIndex(),
                                    rate,
                                    externalFilePanel.eventTypeRadioYES.isSelected(),
                                    externalFilePanel.eventTypeField.getText()
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

        this.setLocationRelativeTo(null); //screen center
        this.setVisible(true);
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
            syntheticRadio.setSelected(true);

            syntheticPanel.durationTextField.setText(synthPhase.getDuration() + "");
            syntheticPanel.initialRateTextField.setText(synthPhase.getInitialRate() + "");
            syntheticPanel.finalRateTextField.setText(synthPhase.getFinalRate() + "");

            syntheticPanel.poissonCheckBox.setSelected(synthPhase.getArrivalProcess() == ArrivalProcess.POISSON);

            DefaultTableModel model = (DefaultTableModel) syntheticPanel.schemaTable.getModel();
            int rowCount = model.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                model.removeRow(0);
            }
            if (synthPhase.getSchema() != null) {
                for (Entry<EventType, Double> e : synthPhase.getSchema().entrySet()) {
                    this.syntheticTypes.add(e.getKey());
                    ((DefaultTableModel) syntheticPanel.schemaTable.getModel()).addRow(
                            new Object[] {e.getKey().getName(), e.getKey().getAttributesNamesList(), ""+e.getValue()});
                }

                ((DefaultTableModel) syntheticPanel.schemaTable.getModel()).addRow(new Object[] {null, null});
            }

            syntheticPanel.deterministicMixCheckBox.setSelected(synthPhase.isDeterministicEventMix());

            if (synthPhase.getDataGenMode() == SyntheticWorkloadPhase.RUNTIME) {
                syntheticPanel.dataGenRTRadio.setSelected(true);
            } else if (synthPhase.getDataGenMode() == SyntheticWorkloadPhase.DATASET) {
                syntheticPanel.dataGenDSRadio.setSelected(true);
            }
            if (synthPhase.getRandomSeed() != null) {
                syntheticPanel.dataGenSeedCheck.setSelected(true);
                syntheticPanel.dataGenSeedField.setText("" + synthPhase.getRandomSeed());
            }

        } else if (phase instanceof ExternalFileWorkloadPhase) {
            ExternalFileWorkloadPhase filePhase = (ExternalFileWorkloadPhase) phase;
            externalFileRadio.setSelected(true);

            externalFilePanel.externalFilePathField.setText(filePhase.getFilePath());
            externalFilePanel.loopCountField.setText("" + filePhase.getLoopCount());

            if (filePhase.containsTimestamps()) {
                if (filePhase.isUsingTimestamps()) {
                    externalFilePanel.timestampRadioYES.setSelected(true);
                } else {
                    externalFilePanel.timestampRadioYESNO.setSelected(true);
                }
            } else {
                externalFilePanel.timestampRadioNO.setSelected(true);
            }
            externalFilePanel.timestampUnitCombo.setSelectedIndex(filePhase.getTimestampUnit());
            externalFilePanel.rateField.setText(filePhase.getEventSubmissionRate() + "");
            if (filePhase.containsEventTypes()) {
                externalFilePanel.eventTypeRadioYES.setSelected(true);
            } else {
                externalFilePanel.eventTypeRadioNO.setSelected(true);
            }
            externalFilePanel.eventTypeField.setText(filePhase.getSingleEventTypeName());
        }

        this.validate();
        this.repaint();
    }

    public void updateEventType(EventType oldType, EventType newType) {
        int index = this.syntheticTypes.indexOf(oldType);

        if(index > -1) {
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
            return (externalFilePanel.externalFilePathField.getText()!= null &&
                    !externalFilePanel.externalFilePathField.getText().isEmpty() &&
                    (externalFilePanel.timestampRadioYES.isSelected() ||
                            (!externalFilePanel.timestampRadioYES.isSelected() && externalFilePanel.rateField.getText()!=null && !externalFilePanel.rateField.getText().isEmpty())
                    ) &&
                    (externalFilePanel.eventTypeRadioYES.isSelected() ||
                            (externalFilePanel.eventTypeRadioNO.isSelected() && externalFilePanel.eventTypeField.getText()!=null && !externalFilePanel.eventTypeField.getText().isEmpty())
                    )
            );
        }

    }

    private javax.swing.JButton cancelBtn;
    private javax.swing.JRadioButton externalFileRadio;
    private javax.swing.JButton okBtn;
    private javax.swing.JPanel phaseDetailPanel;
    private javax.swing.ButtonGroup phaseTypeRadioGroup;
    private javax.swing.JPopupMenu schemaTablePopup;
    private javax.swing.JRadioButton syntheticRadio;
    private javax.swing.JPanel workloadPanel;

    // Internal Class ----------------------------------------------------------
    public class SyntheticPhasePanel extends javax.swing.JPanel {

        /** Creates new form SyntheticPhasePanel. */
        public SyntheticPhasePanel() {
            initComponents();
        }

        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">
        private void initComponents() {
            dataGenRadioGroup = new javax.swing.ButtonGroup();
            durationLabel2 = new javax.swing.JLabel();
            dataGenPanel = new javax.swing.JPanel();
            dataGenDSRadio = new javax.swing.JRadioButton();
            dataGenRTRadio = new javax.swing.JRadioButton();
            dataGenLabel = new javax.swing.JLabel();
            dataGenSeedCheck = new javax.swing.JCheckBox();
            dataGenSeedField = new javax.swing.JTextField();
            schemaLabel1 = new javax.swing.JLabel();
            finalRateTextField = new javax.swing.JTextField();
            durationLabel = new javax.swing.JLabel();
            rateLabel = new javax.swing.JLabel();
            durationTextField = new javax.swing.JTextField();
            initialRateTextField = new javax.swing.JTextField();
            initialRateLabel = new javax.swing.JLabel();
            finalRateLabel = new javax.swing.JLabel();
            poissonCheckBox = new javax.swing.JCheckBox();
            schemaScroll1 = new javax.swing.JScrollPane();
            schemaTable = new javax.swing.JTable();
            deterministicMixCheckBox = new javax.swing.JCheckBox();

            setBorder(javax.swing.BorderFactory.createTitledBorder(""));

            durationLabel2.setText("sec"); // NOI18N

            dataGenPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Data Generation Options"));

            dataGenRadioGroup.add(dataGenDSRadio);
            dataGenDSRadio.setText("Before test and save to data file");
            dataGenDSRadio.setToolTipText("Generate events to a data file before test starts. Events will be read from it during test.");

            dataGenRadioGroup.add(dataGenRTRadio);
            dataGenRTRadio.setSelected(true);
            dataGenRTRadio.setText("In runtime");
            dataGenRTRadio.setToolTipText("Generate events during load submission.");

            dataGenLabel.setText("Generate Events' Data:");

            dataGenSeedCheck.setText("Use a fixed seed:");
            dataGenSeedCheck.setToolTipText("Random number generation");

            dataGenSeedField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

            javax.swing.GroupLayout dataGenPanelLayout = new javax.swing.GroupLayout(dataGenPanel);
            dataGenPanel.setLayout(dataGenPanelLayout);
            dataGenPanelLayout.setHorizontalGroup(
                    dataGenPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dataGenPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(dataGenPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(dataGenLabel)
                                    .addGroup(dataGenPanelLayout.createSequentialGroup()
                                            .addComponent(dataGenRTRadio)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(dataGenDSRadio)))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(dataGenPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(dataGenSeedCheck)
                                                    .addGroup(dataGenPanelLayout.createSequentialGroup()
                                                            .addGap(21, 21, 21)
                                                            .addComponent(dataGenSeedField, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                            .addContainerGap())
            );
            dataGenPanelLayout.setVerticalGroup(
                    dataGenPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dataGenPanelLayout.createSequentialGroup()
                            .addGroup(dataGenPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(dataGenLabel)
                                    .addComponent(dataGenSeedCheck))
                                    .addGroup(dataGenPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(dataGenPanelLayout.createSequentialGroup()
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addGroup(dataGenPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                                            .addComponent(dataGenRTRadio)
                                                            .addComponent(dataGenDSRadio)))
                                                            .addComponent(dataGenSeedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                            .addGap(24, 24, 24))
            );

            schemaLabel1.setText("Schema"); // NOI18N

            finalRateTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
            finalRateTextField.setToolTipText("Final event submission rate, in events per second"); // NOI18N
            finalRateTextField.setPreferredSize(new java.awt.Dimension(75, 20));

            durationLabel.setText("Duration"); // NOI18N

            rateLabel.setText("events/sec"); // NOI18N

            durationTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
            durationTextField.setToolTipText("Phase duration in seconds"); // NOI18N
            durationTextField.setPreferredSize(new java.awt.Dimension(75, 20));

            initialRateTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
            initialRateTextField.setToolTipText("Initial event submission rate, in events per second"); // NOI18N
            initialRateTextField.setPreferredSize(new java.awt.Dimension(75, 20));

            initialRateLabel.setText("Initial Rate"); // NOI18N

            finalRateLabel.setText("Final Rate"); // NOI18N

            poissonCheckBox.setText("Poisson process");
            poissonCheckBox.setToolTipText("Indicates if event arrivals follow a Poisson process");

            schemaTable.setModel(new javax.swing.table.DefaultTableModel(
                    new Object [][] {
                            {null, null, null}
                    },
                    new String [] {
                            "Type", "Columns", "Mix"
                    }
            ) {
                Class[] types = new Class [] {
                        java.lang.String.class, java.lang.String.class, java.lang.String.class
                };
                boolean[] canEdit = new boolean [] {
                        false, false, true
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
            schemaTable.setMaximumSize(new java.awt.Dimension(150, 16));
            schemaScroll1.setViewportView(schemaTable);

            deterministicMixCheckBox.setText("Deterministic Mix (all types have the same frequency)");
            deterministicMixCheckBox.setToolTipText("Event types are generated in a predictable and repeatable order.");

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(deterministicMixCheckBox)
                                    .addComponent(schemaScroll1, javax.swing.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(durationTextField, 0, 0, Short.MAX_VALUE)
                                                    .addComponent(durationLabel))
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(durationLabel2)
                                                    .addGap(18, 18, 18)
                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addComponent(initialRateTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(initialRateLabel))
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                    .addGroup(layout.createSequentialGroup()
                                                                            .addComponent(finalRateTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                            .addComponent(rateLabel)
                                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                            .addComponent(poissonCheckBox))
                                                                            .addComponent(finalRateLabel)))
                                                                            .addComponent(schemaLabel1)
                                                                            .addComponent(dataGenPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                            .addContainerGap())
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(durationLabel)
                                    .addComponent(initialRateLabel)
                                    .addComponent(finalRateLabel))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(durationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(durationLabel2)
                                            .addComponent(initialRateTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(finalRateTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(rateLabel)
                                            .addComponent(poissonCheckBox))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(schemaLabel1)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(schemaScroll1, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(deterministicMixCheckBox)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                                            .addComponent(dataGenPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(11, 11, 11))
            );
            //----------------------------------- End of Generated code ----------------------------------

            //-------------------------------------- Custom code -----------------------------------------
            schemaTablePopup = new javax.swing.JPopupMenu();

            schemaTable.getColumnModel().getColumn(0).setPreferredWidth(50);
            schemaTable.getColumnModel().getColumn(1).setPreferredWidth(200);
            schemaTable.getColumnModel().getColumn(2).setPreferredWidth(30);

            schemaTable.addMouseListener(new PopupListener(schemaTablePopup));
            JMenuItem addPhaseMenuItem = new JMenuItem("Add...");
            JMenuItem deletePhaseMenuItem = new JMenuItem("Delete");

            addPhaseMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openTypeDetail(null);
                }
            });

            deletePhaseMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = schemaTable.getSelectedRow();

                    if (index > -1 && index < syntheticTypes.size()) {
                        syntheticTypes.remove(index);
                        ((DefaultTableModel) schemaTable.getModel()).removeRow(index);
                    } else {
                        JOptionPane.showMessageDialog(null, "Select a type to delete");
                    }
                }
            });

            schemaTablePopup.add(addPhaseMenuItem);
            schemaTablePopup.add(deletePhaseMenuItem);

            schemaTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        JTable source = (JTable) e.getSource();
                        if (source.isEnabled()) {
                            int selected = source.getSelectedRow();
                            if (selected > -1 && selected < syntheticTypes.size()) {
                                openTypeDetail(syntheticTypes.get(selected));
                            }
                        }
                    }
                }
            });


        }// </editor-fold>


        // Variables declaration - do not modify
        private javax.swing.JRadioButton dataGenDSRadio;
        private javax.swing.JLabel dataGenLabel;
        private javax.swing.JPanel dataGenPanel;
        private javax.swing.ButtonGroup dataGenRadioGroup;
        private javax.swing.JRadioButton dataGenRTRadio;
        private javax.swing.JCheckBox dataGenSeedCheck;
        private javax.swing.JTextField dataGenSeedField;
        private javax.swing.JCheckBox deterministicMixCheckBox;
        private javax.swing.JLabel durationLabel;
        private javax.swing.JLabel durationLabel2;
        private javax.swing.JTextField durationTextField;
        private javax.swing.JLabel finalRateLabel;
        private javax.swing.JTextField finalRateTextField;
        private javax.swing.JLabel initialRateLabel;
        private javax.swing.JTextField initialRateTextField;
        private javax.swing.JCheckBox poissonCheckBox;
        private javax.swing.JLabel rateLabel;
        private javax.swing.JLabel schemaLabel1;
        private javax.swing.JScrollPane schemaScroll1;
        private javax.swing.JTable schemaTable;
        // End of variables declaration
    }
    // End of SyntheticPhasePanel internal class --------------------------------

    // Internal class -----------------------------------------------------------
    public class ExternalFilePhasePanel extends javax.swing.JPanel {

        /** Creates new form ExternalFilePhasePanel. */
        public ExternalFilePhasePanel() {
            initComponents();
        }

        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        // <editor-fold defaultstate="collapsed" desc="Generated Code">
        private void initComponents() {
            timestampRadioGroup = new javax.swing.ButtonGroup();
            eventTypeRadioGroup = new javax.swing.ButtonGroup();
            timestampsPanel = new javax.swing.JPanel();
            timestampRadioYES = new javax.swing.JRadioButton();
            timestampUnitCombo = new javax.swing.JComboBox();
            timestampRadioNO = new javax.swing.JRadioButton();
            timestampLabel1 = new javax.swing.JLabel();
            rateField = new javax.swing.JTextField();
            timestampLabel2 = new javax.swing.JLabel();
            timestampRadioYESNO = new javax.swing.JRadioButton();
            timestampLabelYESNO = new javax.swing.JLabel();
            timestampLabelYES = new javax.swing.JLabel();
            externalFilePathField = new javax.swing.JTextField();
            eventTypesPanel = new javax.swing.JPanel();
            eventTypeField = new javax.swing.JTextField();
            eventTypeLabel = new javax.swing.JLabel();
            eventTypeRadioNO = new javax.swing.JRadioButton();
            eventTypeRadioYES = new javax.swing.JRadioButton();
            fileBrowseBtn = new javax.swing.JButton();
            pathLabel = new javax.swing.JLabel();
            loopCountLabel = new javax.swing.JLabel();
            loopCountField = new javax.swing.JTextField();

            setBorder(javax.swing.BorderFactory.createTitledBorder(""));

            timestampsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Timestamps"));

            timestampRadioGroup.add(timestampRadioYES);
            timestampRadioYES.setSelected(true);
            timestampRadioYES.setText("Data file contains timestamps, specified in"); // NOI18N

            timestampUnitCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Milliseconds", "Seconds", "Date/Time" }));

            timestampRadioGroup.add(timestampRadioNO);
            timestampRadioNO.setText("Data file DOES NOT contain timestamps."); // NOI18N

            timestampLabel1.setText("Events will be submitted at:"); // NOI18N

            rateField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

            timestampLabel2.setText("events/sec"); // NOI18N

            timestampRadioGroup.add(timestampRadioYESNO);
            timestampRadioYESNO.setText("Data file contains timestamps, but"); // NOI18N

            timestampLabelYESNO.setText("DO NOT use them for event submisssion!"); // NOI18N

            timestampLabelYES.setText("Use them for event submission!"); // NOI18N

            javax.swing.GroupLayout timestampsPanelLayout = new javax.swing.GroupLayout(timestampsPanel);
            timestampsPanel.setLayout(timestampsPanelLayout);
            timestampsPanelLayout.setHorizontalGroup(
                    timestampsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(timestampsPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(timestampsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(timestampsPanelLayout.createSequentialGroup()
                                            .addGap(21, 21, 21)
                                            .addComponent(timestampLabelYES))
                                            .addGroup(timestampsPanelLayout.createSequentialGroup()
                                                    .addComponent(timestampRadioYES)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(timestampUnitCombo, 0, 138, Short.MAX_VALUE))
                                                    .addGroup(timestampsPanelLayout.createSequentialGroup()
                                                            .addGroup(timestampsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                    .addGroup(timestampsPanelLayout.createSequentialGroup()
                                                                            .addGap(21, 21, 21)
                                                                            .addComponent(timestampLabelYESNO))
                                                                            .addComponent(timestampRadioYESNO)
                                                                            .addComponent(timestampRadioNO))
                                                                            .addGroup(timestampsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                    .addGroup(timestampsPanelLayout.createSequentialGroup()
                                                                                            .addGap(17, 17, 17)
                                                                                            .addComponent(rateField, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                            .addGap(7, 7, 7)
                                                                                            .addComponent(timestampLabel2))
                                                                                            .addGroup(timestampsPanelLayout.createSequentialGroup()
                                                                                                    .addGap(18, 18, 18)
                                                                                                    .addComponent(timestampLabel1)))))
                                                                                                    .addContainerGap())
            );
            timestampsPanelLayout.setVerticalGroup(
                    timestampsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(timestampsPanelLayout.createSequentialGroup()
                            .addGroup(timestampsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(timestampRadioYES)
                                    .addComponent(timestampUnitCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(timestampsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, timestampsPanelLayout.createSequentialGroup()
                                                    .addGap(2, 2, 2)
                                                    .addComponent(timestampLabelYES)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                                                    .addComponent(timestampRadioNO)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(timestampRadioYESNO)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(timestampLabelYESNO)
                                                    .addContainerGap())
                                                    .addGroup(timestampsPanelLayout.createSequentialGroup()
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(timestampLabel1)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addGroup(timestampsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                    .addComponent(timestampLabel2)
                                                                    .addComponent(rateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                    .addGap(23, 23, 23))))
            );

            externalFilePathField.setToolTipText("Path for a CSV datafile.");

            eventTypesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Event Types"));

            eventTypeLabel.setText("All events are of the same type:"); // NOI18N

            eventTypeRadioGroup.add(eventTypeRadioNO);
            eventTypeRadioNO.setText("Data File DOES NOT contain event types."); // NOI18N

            eventTypeRadioGroup.add(eventTypeRadioYES);
            eventTypeRadioYES.setSelected(true);
            eventTypeRadioYES.setText("Data File contains event types"); // NOI18N

            javax.swing.GroupLayout eventTypesPanelLayout = new javax.swing.GroupLayout(eventTypesPanel);
            eventTypesPanel.setLayout(eventTypesPanelLayout);
            eventTypesPanelLayout.setHorizontalGroup(
                    eventTypesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(eventTypesPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(eventTypesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(eventTypeRadioYES)
                                    .addComponent(eventTypeRadioNO)
                                    .addGroup(eventTypesPanelLayout.createSequentialGroup()
                                            .addGap(21, 21, 21)
                                            .addComponent(eventTypeLabel)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(eventTypeField, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)))
                                            .addContainerGap())
            );
            eventTypesPanelLayout.setVerticalGroup(
                    eventTypesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(eventTypesPanelLayout.createSequentialGroup()
                            .addComponent(eventTypeRadioYES)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(eventTypeRadioNO)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(eventTypesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(eventTypeLabel)
                                    .addComponent(eventTypeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            fileBrowseBtn.setText("..."); // NOI18N

            pathLabel.setText("Path:"); // NOI18N

            loopCountLabel.setText("loop count:");

            loopCountField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
            loopCountField.setText("1");
            loopCountField.setToolTipText("The number of times that the data file must be read/submitted.");

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(eventTypesPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(timestampsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                            .addComponent(pathLabel)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(externalFilePathField, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(fileBrowseBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(loopCountLabel)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(loopCountField, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addContainerGap())
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addGap(11, 11, 11)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                    .addComponent(pathLabel)
                                    .addComponent(fileBrowseBtn)
                                    .addComponent(loopCountLabel)
                                    .addComponent(externalFilePathField, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(loopCountField, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(timestampsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(eventTypesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap(16, Short.MAX_VALUE))
            );
            // ------------------------------ End of generated code -------------------------------------

            // ----------------------------------- Custom code ------------------------------------------
            ItemListener radioListener2 = new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    timestampUnitCombo.setEnabled(externalFileRadio.isSelected() && timestampRadioYES.isSelected());
                    timestampLabel1.setEnabled(externalFileRadio.isSelected() && !timestampRadioYES.isSelected());
                    timestampLabel2.setEnabled(externalFileRadio.isSelected() && !timestampRadioYES.isSelected());
                    rateField.setEnabled(externalFileRadio.isSelected() && !timestampRadioYES.isSelected());
                }
            };
            timestampRadioYES.addItemListener(radioListener2);
            timestampRadioNO.addItemListener(radioListener2);
            timestampRadioYESNO.addItemListener(radioListener2);

            ItemListener radioListener3 = new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    eventTypeField.setEnabled(eventTypeRadioNO.isSelected());
                }
            };
            eventTypeRadioYES.addItemListener(radioListener3);
            eventTypeRadioNO.addItemListener(radioListener3);

            timestampRadioYES.setSelected(true);
            eventTypeRadioYES.setSelected(true);

            fileBrowseBtn.addActionListener(new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    fileChooser.showOpenDialog(null);

                    if (fileChooser.getSelectedFile() != null) {
                        externalFilePathField.setText(fileChooser.getSelectedFile().getPath());
                    }
                }
            });

        }// </editor-fold>


        // Variables declaration - do not modify
        private javax.swing.JTextField eventTypeField;
        private javax.swing.JLabel eventTypeLabel;
        private javax.swing.JRadioButton eventTypeRadioNO;
        private javax.swing.JRadioButton eventTypeRadioYES;
        private javax.swing.JPanel eventTypesPanel;
        private javax.swing.JTextField externalFilePathField;
        private javax.swing.JButton fileBrowseBtn;
        private javax.swing.JTextField loopCountField;
        private javax.swing.JLabel loopCountLabel;
        private javax.swing.JLabel pathLabel;
        private javax.swing.JTextField rateField;
        private javax.swing.JLabel timestampLabel1;
        private javax.swing.JLabel timestampLabel2;
        private javax.swing.JLabel timestampLabelYES;
        private javax.swing.JLabel timestampLabelYESNO;
        private javax.swing.JRadioButton timestampRadioNO;
        private javax.swing.JRadioButton timestampRadioYES;
        private javax.swing.JRadioButton timestampRadioYESNO;
        private javax.swing.JComboBox timestampUnitCombo;
        private javax.swing.JPanel timestampsPanel;
        private javax.swing.ButtonGroup eventTypeRadioGroup;
        private javax.swing.ButtonGroup timestampRadioGroup;
        // End of variables declaration
    }
    // End of ExternalFilePhasePanel internal class --------------------------------
}
