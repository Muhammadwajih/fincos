package pt.uc.dei.fincos.controller.gui;

import java.awt.Dialog;
import java.awt.Dimension;
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
	private ButtonGroup logRadioGroup;
	private JRadioButton logAllRadio;
	private JCheckBox logCheckBox;
	private JComboBox logSamplingComboBox;
	private JLabel logSamplingLabel;
	private JRadioButton logTSRadio;
	private JPanel loggingPanel;
    private JComboBox validationSamplingComboBox;
    private JLabel validationSamplingLabel;
    private JTextField serverAddressField;
    private JLabel serverAddressLabel;
    private JTextField serverPortField;
    private JLabel serverPortLabel;
    private JCheckBox validateCheckBox;
    private JPanel validationPanel;
    private JTextField validatorAddressField;
    private JLabel validatorAddressLabel;
    private JTextField validatorPortField;
    private JLabel validatorPortLabel;

    /**
     * Creates a form for editing Driver configuration.
     *
     * @param dr    Driver configuration properties to be shown in UI, when in UPDATE mode,
     *              or <tt>null</tt>, in INSERTION mode.
     */
    public DriverDetail(DriverConfig dr) {
        super(null);
    	initComponents();
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
        this.setVisible(true);
    }

    private void initComponents() {
        aliasField = new javax.swing.JTextField();
        aliasLabel = new javax.swing.JLabel("Alias");
        addressLabel = new javax.swing.JLabel("Address");
        addressField = new javax.swing.JTextField();
        workloadPanel = new javax.swing.JPanel();
        phasesLabel = new javax.swing.JLabel("Phases");
        jScrollPane1 = new javax.swing.JScrollPane();
        phasesTable = new javax.swing.JTable();
        okBtn = new javax.swing.JButton("OK", new ImageIcon("imgs/OK.png"));

        cancelBtn = new javax.swing.JButton("Cancel", new ImageIcon("imgs/cancel.png"));

        workloadPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Workload"));

        phasesTable.setModel(new DefaultTableModel(
            new String [] {
                "Phase", "Type"
            	}, 1)  {
	        	@Override
	        	public boolean isCellEditable(int row, int column) {
	        			return false;
	        	}
        	});
        jScrollPane1.setViewportView(phasesTable);

        serverAddressLabel = new JLabel("Server Address");
        serverAddressField = new JTextField();
        serverPortLabel = new JLabel("Server Port");
        serverPortField = new JTextField();

        java.awt.Font f = new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11);
        threadCountLabel = new javax.swing.JLabel();
        threadCountRadioGroup = new javax.swing.ButtonGroup();
        threadCountFixedRadio = new javax.swing.JRadioButton();
        threadCountCPUsRadio = new javax.swing.JRadioButton();
        threadCountField = new javax.swing.JSpinner();
        threadCountLabel.setText("Thread Count");
        threadCountRadioGroup.add(threadCountFixedRadio);
        threadCountFixedRadio.setText("Fixed:");
        threadCountFixedRadio.setFont(f);
        threadCountFixedRadio.setToolTipText("Use a fixed number of threads");
        threadCountRadioGroup.add(threadCountCPUsRadio);
        threadCountCPUsRadio.setText("Available CPU's");
        threadCountCPUsRadio.setFont(f);
        threadCountCPUsRadio.setToolTipText("Use as many threads as the number of processors/cores in the host machine.");
        threadCountField.setModel(new javax.swing.SpinnerNumberModel(1, 1, 64, 1));

        loggingPanel = new javax.swing.JPanel();
        loggingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Logging"));

        logCheckBox = new JCheckBox();
        logCheckBox.setText("Log Events to Disk");
        logRadioGroup = new ButtonGroup();
        logAllRadio = new JRadioButton();
        logAllRadio.setText("All Fields");
        logAllRadio.setSelected(true);
        logAllRadio.setFont(f);
        logRadioGroup.add(logAllRadio);
        logTSRadio = new JRadioButton();
        logTSRadio.setText("Only Timestamps");
        logTSRadio.setFont(f);
        logRadioGroup.add(logTSRadio);
        logSamplingLabel = new javax.swing.JLabel();
        logSamplingLabel.setText("Sampling Rate");
        logSamplingLabel.setFont(f);
        logSamplingComboBox = new javax.swing.JComboBox();
        logSamplingComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "0.5", "0.25", "0.2", "0.1", "0.05", "0.025", "0.01", "0.001" }));
        logSamplingComboBox.setSelectedItem("1");

        validationPanel = new JPanel();
        validationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Runtime Monitoring"));
        validateCheckBox = new JCheckBox("Send Events to FINCoS PerfMon tool");
        validatorAddressLabel = new JLabel("Address");
        validatorAddressLabel.setEnabled(false);
        validatorAddressLabel.setFont(f);
        validatorAddressField = new JTextField();
        validatorAddressField.setEnabled(false);

        validatorPortLabel = new JLabel("Port");
        validatorPortLabel.setEnabled(false);
        validatorPortLabel.setFont(f);
        validatorPortField = new JTextField();
        validatorPortField.setEnabled(false);

        validationSamplingComboBox = new JComboBox();
        validationSamplingComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "0.5", "0.25", "0.2", "0.1", "0.05", "0.025", "0.01", "0.001" }));
        validationSamplingComboBox.setPreferredSize(new Dimension(25, 20));
        validationSamplingComboBox.setSelectedItem("0.1");
        validationSamplingComboBox.setEnabled(false);

        validationSamplingLabel = new JLabel("Sampling Rate");
        validationSamplingLabel.setFont(f);
        validationSamplingLabel.setEnabled(false);

      //-------------------------------------- Generated Code ------------------------------------
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
                    .addComponent(logSamplingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(logSamplingLabel))
                .addContainerGap(112, Short.MAX_VALUE))
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
                .addComponent(logSamplingLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(logSamplingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout validationPanelLayout = new javax.swing.GroupLayout(validationPanel);
        validationPanel.setLayout(validationPanelLayout);
        validationPanelLayout.setHorizontalGroup(
            validationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(validationPanelLayout.createSequentialGroup()
                .addGroup(validationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(validationPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(validationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(validatorAddressLabel)
                            .addComponent(validatorAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(validationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(validatorPortLabel)
                            .addComponent(validatorPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(validationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(validationSamplingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(validationSamplingLabel)))
                    .addComponent(validateCheckBox))
                .addContainerGap())
        );
        validationPanelLayout.setVerticalGroup(
            validationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(validationPanelLayout.createSequentialGroup()
                .addGroup(validationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(validationPanelLayout.createSequentialGroup()
                        .addComponent(validationSamplingLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(validationSamplingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(validationPanelLayout.createSequentialGroup()
                        .addComponent(validateCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(validationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(validationPanelLayout.createSequentialGroup()
                                .addComponent(validatorAddressLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(validatorAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(validationPanelLayout.createSequentialGroup()
                                .addComponent(validatorPortLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(validatorPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(threadCountFixedRadio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(threadCountField, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(threadCountCPUsRadio))
                    .addComponent(threadCountLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(serverAddressLabel, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(serverAddressField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(35, 35, 35)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(serverPortLabel, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(serverPortField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(74, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(workloadPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(validationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(aliasField, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(aliasLabel))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addressField, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(addressLabel))
                        .addGap(17, 17, 17))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(okBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(loggingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addComponent(workloadPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(threadCountLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(threadCountFixedRadio)
                    .addComponent(threadCountCPUsRadio)
                    .addComponent(threadCountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(serverAddressLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(serverAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(serverPortLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(serverPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loggingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(validationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
        //--------------------------------- End of Generated Code ---------------------------------

        //-------------------------------------- Custom Code ------------------------------------

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
        					int validatorPort = 0, serverPort = 0, threadCount = 1;
        					double validatorSamplingRate = 0;
        					serverPort = Integer.parseInt(serverPortField.getText());

        					try {
        						if (threadCountCPUsRadio.isSelected()) {
        							threadCount = -1;
        						} else {
        							threadCount = (Integer) threadCountField.getValue();
        						}

        						if (validateCheckBox.isSelected()) {
        							validatorPort = Integer.parseInt(validatorPortField.getText());
        							validatorSamplingRate = Double.parseDouble((String) validationSamplingComboBox.getSelectedItem());
        						}

        					} catch (NumberFormatException nfe1) {
        						JOptionPane.showMessageDialog(null, "Invalid value. Port and thread count fields require numeric values.");
        					}

        					newCfg =
        						new DriverConfig(aliasField.getText(), InetAddress.getByName(addressField.getText()),
        								workload, InetAddress.getByName(serverAddressField.getText()),
        								serverPort, threadCount,
        								logCheckBox.isSelected(),
        								logAllRadio.isSelected() ?  Globals.LOG_ALL_FIELDS
        														 : Globals.LOG_ONLY_TIMESTAMPS,
        								Double.parseDouble((String) logSamplingComboBox.getSelectedItem()),
        								validateCheckBox.isSelected(),
        								InetAddress.getByName(validatorAddressField.getText()),
        								validatorPort , validatorSamplingRate
        						);
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

        phasesPop.add(addPhaseMenuItem);
        phasesPop.add(deletePhaseMenuItem);
        phasesPop.add(copyPhaseMenuItem);

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

        validateCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	setValidationEnabled(validateCheckBox.isSelected());
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

    	this.serverAddressField.setText(dr.getServerAddress().getHostAddress());
    	this.serverPortField.setText("" + dr.getServerPort());

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
    	}

    	validateCheckBox.setSelected(dr.isValidationEnabled());
    	setValidationEnabled(dr.isValidationEnabled());
    	if (dr.isValidationEnabled()) {
    		validatorAddressField.setText(dr.getValidatorAddress().getHostAddress());
    		validatorPortField.setText("" + dr.getValidatorPort());
    		double validSamplRate = dr.getValidationSamplingRate();
    		if (validSamplRate == 1) {
    			validationSamplingComboBox.setSelectedItem("1");
    		} else if (validSamplRate == 0.001) {
    			validationSamplingComboBox.setSelectedItem("0.001");
    		} else {
    			validationSamplingComboBox.setSelectedItem("" + validSamplRate);
    		}
    	}
    }


    private boolean validateFields() {
    	try {
    		Integer.parseInt(this.serverPortField.getText());

    		if (validateCheckBox.isSelected()) {
    			Integer.parseInt(this.validatorPortField.getText());
    			if (this.validatorAddressField == null || this.validatorAddressField.getText().isEmpty()) {
    				return false;
    			}
    		}
    	} catch (NumberFormatException nfe) {
    		return false;
    	}

    	return (this.aliasField.getText() != null &&
    			!this.aliasField.getText().isEmpty() &&
    			this.addressField.getText() != null &&
    			!this.addressField.getText().isEmpty() &&
    			this.serverAddressField.getText() != null &&
    			!this.serverAddressField.getText().isEmpty() &&
    			phasesTable.getRowCount() > 1
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
	}

    private void setValidationEnabled(boolean enabled) {
    	validatorAddressLabel.setEnabled(enabled);
    	validatorPortLabel.setEnabled(enabled);
    	validatorAddressField.setEnabled(enabled);
        validatorPortField.setEnabled(enabled);
        validationSamplingLabel.setEnabled(enabled);
        validationSamplingComboBox.setEnabled(enabled);
    }

    /**
     * Disables user input on this form.
     */
	public void disableGUI() {
		this.aliasField.setEnabled(false);
		this.addressField.setEnabled(false);
		this.phasesTable.setEnabled(false);
		this.serverAddressField.setEnabled(false);
		this.serverPortField.setEnabled(false);
		this.threadCountLabel.setEnabled(false);
		this.threadCountFixedRadio.setEnabled(false);
		this.threadCountField.setEnabled(false);
		this.threadCountCPUsRadio.setEnabled(false);
		this.logCheckBox.setEnabled(false);
		this.logAllRadio.setEnabled(false);
		this.logTSRadio.setEnabled(false);
		this.logSamplingLabel.setEnabled(false);
		this.logSamplingComboBox.setEnabled(false);
		this.validateCheckBox.setEnabled(false);
		this.validatorAddressField.setEnabled(false);
		this.validatorPortField.setEnabled(false);
		this.validationSamplingComboBox.setEnabled(false);
		this.okBtn.setEnabled(false);
	}


}

