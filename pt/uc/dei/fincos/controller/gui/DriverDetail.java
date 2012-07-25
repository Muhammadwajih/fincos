package pt.uc.dei.fincos.controller.gui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.controller.ConnectionConfig;
import pt.uc.dei.fincos.controller.DriverConfig;
import pt.uc.dei.fincos.driver.ExternalFileWorkloadPhase;
import pt.uc.dei.fincos.driver.SyntheticWorkloadPhase;
import pt.uc.dei.fincos.driver.WorkloadPhase;


/**
 * GUI for configuration of Drivers.
 *
 * @author Marcelo R.N. Mendes
 *
 */
@SuppressWarnings("serial")
public class DriverDetail extends ComponentDetail {
    /** List of workload phases for the Driver being configured. */
    private ArrayList<WorkloadPhase> phases = new ArrayList<WorkloadPhase>();

    /** Previous properties of the Driver (when the form is open for update). */
    private DriverConfig oldCfg;

    /** UI fields. */
    private JTextField addressField;
    private JLabel addressLabel;
    private JTextField aliasField;
    private JLabel aliasLabel;
    private JButton cancelBtn;
    private javax.swing.JComboBox connCombo;
    private JLabel connLbl;
    private JLabel phasesLabel;
    private JScrollPane jScrollPane1;
    private JButton okBtn;
    private JTable phasesTable;
    private JPanel workloadPanel;
    private JPopupMenu phasesPop = new JPopupMenu();
	private JRadioButton threadCountCPUsRadio;
    private JSpinner threadCountField;
    private JRadioButton threadCountFixedRadio;
    private JLabel threadCountLabel;
    private ButtonGroup threadCountRadioGroup;
	private JRadioButton logAllRadio;
	private ButtonGroup logBtnGroup;
	private JCheckBox logCheckBox;
    private javax.swing.JTextField logFlushField;
    private javax.swing.JLabel logFlushLbl;
	private JComboBox logSamplingComboBox;
	private JLabel logSamplingLabel;
	private JRadioButton logTSRadio;
	private JPanel loggingPanel;


    /**
     * Creates a form for editing Driver configuration.
     *
     * @param dr    Driver configuration properties to be shown in UI, when in UPDATE mode,
     *              or <tt>null</tt>, in INSERTION mode.
     */
    public DriverDetail(DriverConfig dr) {
        super(null);
    	initComponents();
    	addListeners();
    	this.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);

        if (dr != null) {
        	this.oldCfg = dr;
        	this.op = UPDATE;
        	setTitle("Editing \"" + dr.getAlias() + "\"");
        	fillProperties(dr);
        } else {
        	this.op = INSERT;
        	setTitle("New Driver");
        }

        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        threadCountRadioGroup = new javax.swing.ButtonGroup();
        logBtnGroup = new javax.swing.ButtonGroup();
        loggingPanel = new javax.swing.JPanel();
        logCheckBox = new javax.swing.JCheckBox();
        logAllRadio = new javax.swing.JRadioButton();
        logTSRadio = new javax.swing.JRadioButton();
        logSamplingLabel = new javax.swing.JLabel();
        logSamplingComboBox = new javax.swing.JComboBox();
        logFlushLbl = new javax.swing.JLabel();
        logFlushField = new javax.swing.JTextField();
        cancelBtn = new javax.swing.JButton();
        okBtn = new javax.swing.JButton();
        aliasLabel = new javax.swing.JLabel();
        aliasField = new javax.swing.JTextField();
        addressLabel = new javax.swing.JLabel();
        addressField = new javax.swing.JTextField();
        connLbl = new javax.swing.JLabel();
        workloadPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        phasesTable = new javax.swing.JTable();
        phasesLabel = new javax.swing.JLabel();
        threadCountLabel = new javax.swing.JLabel();
        threadCountFixedRadio = new javax.swing.JRadioButton();
        threadCountCPUsRadio = new javax.swing.JRadioButton();
        threadCountField = new javax.swing.JSpinner();
        connCombo = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        loggingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Logging"));

        logCheckBox.setText("Log Events to Disk");

        logBtnGroup.add(logAllRadio);
        logAllRadio.setSelected(true);
        logAllRadio.setText("All Fields");

        logBtnGroup.add(logTSRadio);
        logTSRadio.setText("Only Timestamps");

        logSamplingLabel.setText("Sampling Rate");

        logSamplingComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1 ", "0.5", "0.25", "0.2", "0.1", "0.05", "0.025", "0.01", "0.001" }));

        logFlushLbl.setText("Flush Interval");

        logFlushField.setText("10");

        javax.swing.GroupLayout loggingPanelLayout = new javax.swing.GroupLayout(loggingPanel);
        loggingPanel.setLayout(loggingPanelLayout);
        loggingPanelLayout.setHorizontalGroup(
            loggingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loggingPanelLayout.createSequentialGroup()
                .addGroup(loggingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(loggingPanelLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(loggingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(logTSRadio)
                            .addComponent(logAllRadio)))
                    .addComponent(logCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(loggingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(logSamplingLabel)
                    .addComponent(logSamplingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(loggingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(logFlushField)
                    .addComponent(logFlushLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(17, Short.MAX_VALUE))
        );
        loggingPanelLayout.setVerticalGroup(
            loggingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, loggingPanelLayout.createSequentialGroup()
                .addComponent(logCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(logAllRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(logTSRadio))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, loggingPanelLayout.createSequentialGroup()
                .addGroup(loggingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(logSamplingLabel)
                    .addComponent(logFlushLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(loggingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(logSamplingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(logFlushField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        cancelBtn.setText("Cancel");

        okBtn.setText("OK");

        aliasLabel.setText("Alias");

        addressLabel.setText("Address");

        connLbl.setText("Send Events to:");

        workloadPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Workload"));

        phasesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null}
            },
            new String [] {
                "Phase", "Type"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(phasesTable);

        phasesLabel.setText("Phases");

        javax.swing.GroupLayout workloadPanelLayout = new javax.swing.GroupLayout(workloadPanel);
        workloadPanel.setLayout(workloadPanelLayout);
        workloadPanelLayout.setHorizontalGroup(
            workloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(workloadPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(workloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                    .addComponent(phasesLabel))
                .addContainerGap())
        );
        workloadPanelLayout.setVerticalGroup(
            workloadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(workloadPanelLayout.createSequentialGroup()
                .addComponent(phasesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                .addContainerGap())
        );

        threadCountLabel.setText("Thread Count");

        threadCountRadioGroup.add(threadCountFixedRadio);
        threadCountFixedRadio.setText("Fixed:");
        threadCountFixedRadio.setToolTipText("Use a fixed number of threads");

        threadCountRadioGroup.add(threadCountCPUsRadio);
        threadCountCPUsRadio.setText("Available CPU's");
        threadCountCPUsRadio.setToolTipText("Use as many threads as the number of processors/cores in the host machine.");

        threadCountField.setModel(new javax.swing.SpinnerNumberModel(1, 1, 64, 1));

        connCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "New Connection..." }));
        connCombo.setSelectedIndex(-1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(workloadPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addContainerGap())
                        .addGroup(layout.createSequentialGroup()
                            .addGap(10, 10, 10)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(aliasField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
                                .addComponent(aliasLabel, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(connLbl, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(connCombo, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGap(18, 18, 18)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(addressField, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(addressLabel))
                            .addGap(17, 17, 17))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(10, 10, 10)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(threadCountFixedRadio)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(threadCountField, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(threadCountCPUsRadio))
                                .addComponent(threadCountLabel))
                            .addContainerGap(141, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(loggingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addContainerGap()))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(okBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aliasLabel)
                    .addComponent(addressLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aliasField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(connLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(workloadPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(threadCountLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(threadCountFixedRadio)
                    .addComponent(threadCountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(threadCountCPUsRadio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(loggingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(okBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();

        okBtn.setIcon(new ImageIcon("imgs/OK.png"));
        cancelBtn.setIcon(new ImageIcon("imgs/cancel.png"));
        initConnCombo();
        setLoggingEnabled(logCheckBox.isSelected());
    }

    private void initConnCombo() {
        ConnectionConfig[] conns = Controller_GUI.getInstance().getConnections();
        String[] txts = new String[conns.length + 1];
        ImageIcon[] imgs = new ImageIcon[conns.length + 1];
        for (int i = 0; i < conns.length; i++) {
            txts[i] = conns[i].alias;
        }
        txts[txts.length - 1] = "New Connection...";
        imgs[txts.length - 1] = new ImageIcon("imgs/connection_new.png");
        ComboBoxRenderer renderer = new ComboBoxRenderer(txts, imgs);
        //renderer.setPreferredSize(new Dimension(200, 130));
        connCombo.setRenderer(renderer);
        Integer[] intArray = new Integer[txts.length];
        for (int i = 0; i < txts.length; i++) {
            intArray[i] = new Integer(i);
            if (imgs[i] != null) {
                imgs[i].setDescription(txts[i]);
            }
        }
        connCombo.setModel(new javax.swing.DefaultComboBoxModel(intArray));
        connCombo.setSelectedIndex(-1);
        connCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (connCombo.getSelectedIndex() == connCombo.getModel().getSize() - 1) {
                    openNewConnectionForm();
                }
            }
        });
    }

    private void openNewConnectionForm() {
        new ConnectionDetail(this, null);
    }

    public void updateConnectionsList() {
        initConnCombo();
        connCombo.setSelectedIndex(connCombo.getModel().getSize() - 2);
    }

    private void addListeners() {
        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (phasesTable.getCellEditor() != null) {
                        phasesTable.getCellEditor().stopCellEditing();
                    }

                    if (validateFields()) {
                        WorkloadPhase[] workload = new WorkloadPhase[phases.size()];
                        workload = phases.toArray(workload);
                        DriverConfig newCfg;
                        try {
                            int threadCount = 1;
                            try {
                                if (threadCountCPUsRadio.isSelected()) {
                                    threadCount = -1;
                                } else {
                                    threadCount = (Integer) threadCountField.getValue();
                                }
                            } catch (NumberFormatException nfe1) {
                                JOptionPane.showMessageDialog(null, "Invalid value. Port and thread count fields require numeric values.");
                            }

                            int connIndex = connCombo.getSelectedIndex();
                            ConnectionConfig connCfg = Controller_GUI.getInstance().getConnection(connIndex);

                            newCfg =
                                new DriverConfig(aliasField.getText(), InetAddress.getByName(addressField.getText()),
                                        connCfg, workload, threadCount,
                                        logCheckBox.isSelected(),
                                        logAllRadio.isSelected() ?  Globals.LOG_ALL_FIELDS
                                                                 : Globals.LOG_ONLY_TIMESTAMPS,
                                        Double.parseDouble((String) logSamplingComboBox.getSelectedItem()),
                                        Integer.parseInt(logFlushField.getText()));
                            if (Controller_GUI.getInstance().checkDriverUniqueConstraint(oldCfg, newCfg)) {
                                switch (op) {
                                case UPDATE:
                                    Controller_GUI.getInstance().updateDriver(oldCfg, newCfg);
                                    dispose();
                                    break;
                                case INSERT:
                                    Controller_GUI.getInstance().addDriver(newCfg);
                                    dispose();
                                }
                            } else {
                                JOptionPane.showMessageDialog(null, "New configuration violates unique constraint.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (UnknownHostException e2) {
                            JOptionPane.showMessageDialog(null, "Invalid IP address.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "One or more required fields were not correctly filled.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Invalid value at workload table");
                }

            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }

        });

        phasesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable source = (JTable) e.getSource();
                    if (source.isEnabled()) {
                        int selected = source.getSelectedRow();
                        if (selected > -1 && selected < phases.size()) {
                            openPhaseDetail(phases.get(selected));
                        }
                    }
                }
            }
        });
        phasesTable.addMouseListener(new PopupListener(phasesPop));
        JMenuItem addPhaseMenuItem = new JMenuItem("Add...");
        JMenuItem deletePhaseMenuItem = new JMenuItem("Delete");
        JMenuItem copyPhaseMenuItem = new JMenuItem("Copy...");
        phasesPop.add(addPhaseMenuItem);
        phasesPop.add(deletePhaseMenuItem);
        phasesPop.add(copyPhaseMenuItem);

        addPhaseMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openPhaseDetail(null);
            }
        });

        deletePhaseMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = phasesTable.getSelectedRow();

                if (index > -1 && index < phases.size()) {
                    removePhase(index);
                } else {
                    JOptionPane.showMessageDialog(null, "Select a phase to delete");
                }
            }
        });

        copyPhaseMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selected = phasesTable.getSelectedRow();

                if (selected > -1 && selected < phases.size()) {
                    WorkloadPhase copy = phases.get(selected);
                    PhaseDetail detail = openPhaseDetail(null);
                    detail.fillProperties(copy);
                } else {
                    JOptionPane.showMessageDialog(null, "Select a phase to copy");
                }

            }
        });

        threadCountFixedRadio.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                threadCountField.setEnabled(threadCountFixedRadio.isSelected());
            }
        });
        threadCountCPUsRadio.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                threadCountField.setEnabled(threadCountFixedRadio.isSelected());
            }
        });
        threadCountFixedRadio.setSelected(true);

        logCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setLoggingEnabled(logCheckBox.isSelected());
            }
        });
    }


    /**
     * Creates a form for configuring a workload phase.
     *
     * @param workloadPhase     Workload phase configuration properties, when in UPDATE mode,
     *                          or <tt>null</tt>, in INSERTION mode.
     * @return                  a reference to the just created form
     */
	protected PhaseDetail openPhaseDetail(WorkloadPhase workloadPhase) {
    	return new PhaseDetail(this, workloadPhase);
	}


	/**
	 * Fills the UI with the Driver properties passed as argument.
	 *
	 * @param dr   Driver configuration properties to be shown in UI, when in UPDATE mode,
     *             or <tt>null</tt>, in INSERTION mode.
	 */
	public void fillProperties(DriverConfig dr) {
    	this.aliasField.setText(dr.getAlias());
    	this.addressField.setText(dr.getAddress().getHostAddress());

    	WorkloadPhase[] phases = dr.getWorkload();
    	for (int i = 0; i < phases.length; i++) {
    		addPhase(phases[i]);
		}

    	if (dr.getThreadCount() > 0) {
    		this.threadCountFixedRadio.setSelected(true);
    		this.threadCountField.setValue(dr.getThreadCount());
    	} else {
    		this.threadCountCPUsRadio.setSelected(true);
    	}

    	if (dr.getConnection() != null) {
    	    int connIndex = Controller_GUI.getInstance().getConnectionIndex(dr.getConnection().alias);
            this.connCombo.setSelectedIndex(connIndex);
    	} else {
    	    this.connCombo.setSelectedIndex(-1);
    	}


    	logCheckBox.setSelected(dr.isLoggingEnabled());
    	setLoggingEnabled(dr.isLoggingEnabled());
    	if (dr.isLoggingEnabled()) {
    		if (dr.getFieldsToLog() == Globals.LOG_ALL_FIELDS) {
    			logAllRadio.setSelected(true);
    		} else {
    			logTSRadio.setSelected(true);
    		}

    		double logSamplRate = dr.getLoggingSamplingRate();

    		if (logSamplRate == 1) {
    			logSamplingComboBox.setSelectedItem("1");
    		} else if (logSamplRate == 0.001) {
    			logSamplingComboBox.setSelectedItem("0.001");
    		} else {
    			logSamplingComboBox.setSelectedItem("" + logSamplRate);
    		}
    		logFlushField.setText("" + dr.getLogFlushInterval());
    	}
    	for (Component comp : this.getComponents()) {
    	    comp.repaint();
        }
    }

    private boolean validateFields() {

    	return (this.aliasField.getText() != null
    	        && !this.aliasField.getText().isEmpty()
    	        && this.addressField.getText() != null
    	        && !this.addressField.getText().isEmpty()
    	        && this.connCombo.getSelectedIndex() != -1
    	        && this.connCombo.getSelectedIndex() < Controller_GUI.getInstance().getConnections().length
    			&& phasesTable.getRowCount() > 1
    			);
    }

    /**
     * Updates the definition of a workload phase.
     *
     * @param oldCfg    the old phase configuration
     * @param newCfg    the new phase configuration
     */
	public void updatePhase(WorkloadPhase oldCfg, WorkloadPhase newCfg) {
		int index = this.phases.indexOf(oldCfg);

		if (index > -1) {
			this.phases.remove(index);
			((DefaultTableModel) phasesTable.getModel()).removeRow(index);
			addPhase(index, newCfg);
		}
	}

	/**
	 * Adds a phase to the list of workload phases of this Driver.
	 *
	 * @param index    phase index
	 * @param phase    the new phase
	 */
	public void addPhase(int index, WorkloadPhase phase) {
		this.phases.add(index, phase);

		DefaultTableModel model = (DefaultTableModel) this.phasesTable.getModel();
		if (phase instanceof SyntheticWorkloadPhase) {
			model.insertRow(index, new Object[] {"Phase " + (index + 1), "Synthetic"});
		} else if (phase instanceof ExternalFileWorkloadPhase) {
			model.insertRow(index, new Object[] {"Phase " + (index + 1), "External File"});
		}
	}

	/**
     * Adds a phase to the list of workload phases of this Driver.
     *
     * @param phase    the new phase
     */
	public void addPhase(WorkloadPhase phase) {
		this.phases.add(phase);

		DefaultTableModel model = (DefaultTableModel) this.phasesTable.getModel();
		if (phase instanceof SyntheticWorkloadPhase) {
			model.insertRow(model.getRowCount() - 1, new Object[] {"Phase " + (phases.indexOf(phase)+1), "Synthetic"});
		} else if (phase instanceof ExternalFileWorkloadPhase) {
			model.insertRow(model.getRowCount() - 1, new Object[] {"Phase " + (phases.indexOf(phase)+1), "External File"});
		}
	}

    private void removePhase(int index) {
    	if (index < phases.size()) {
    		phases.remove(index);
    		((DefaultTableModel) phasesTable.getModel()).removeRow(index);
    		for (int i = index; i < phasesTable.getRowCount() - 1; i++) {
    			phasesTable.setValueAt("Phase " + (i + 1), index, 0);
    		}
    	}
	}

    private void setLoggingEnabled(boolean enabled) {
    	logAllRadio.setEnabled(enabled);
    	logTSRadio.setEnabled(enabled);
    	logSamplingLabel.setEnabled(enabled);
    	logSamplingComboBox.setEnabled(enabled);
    	logFlushLbl.setEnabled(enabled);
    	logFlushField.setEnabled(enabled);
	}

    /**
     * Disables user input on this form.
     */
	public void disableGUI() {
		this.aliasField.setEnabled(false);
		this.addressField.setEnabled(false);
		this.connLbl.setEnabled(false);
		this.connCombo.setEnabled(false);
		this.phasesTable.setEnabled(false);
		this.threadCountLabel.setEnabled(false);
		this.threadCountFixedRadio.setEnabled(false);
		this.threadCountField.setEnabled(false);
		this.threadCountCPUsRadio.setEnabled(false);
		this.logCheckBox.setEnabled(false);
		this.logAllRadio.setEnabled(false);
		this.logTSRadio.setEnabled(false);
		this.logSamplingLabel.setEnabled(false);
		this.logSamplingComboBox.setEnabled(false);
		this.logFlushLbl.setEnabled(false);
        this.logFlushField.setEnabled(false);
		this.okBtn.setEnabled(false);
	}


}

