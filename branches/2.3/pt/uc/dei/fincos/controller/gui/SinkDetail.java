package pt.uc.dei.fincos.controller.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.controller.SinkConfig;


/**
 * GUI for configuration of Sinks
 *
 * @author Marcelo R.N. Mendes
 *
 */
@SuppressWarnings("serial")
public class SinkDetail extends ComponentDetail {

	private ArrayList<String> streams;
	private SinkConfig oldCfg;

    private javax.swing.JTextField addressField;
    private javax.swing.JLabel addressLabel;
    private javax.swing.JTextField aliasField;
    private javax.swing.JLabel aliasLabel;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JList streamsList;
    private javax.swing.JScrollPane streamsScrollPane1;
    private javax.swing.JButton okBtn;
    private javax.swing.JTextField portField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField serverAddressField;
    private javax.swing.JLabel serverAddressLabel;
    private javax.swing.JPanel streamsPanel;

    private javax.swing.JRadioButton logAllRadio;
    private javax.swing.JCheckBox logCheckBox;
    private javax.swing.ButtonGroup logRadioGroup;
    private javax.swing.JComboBox logSamplingComboBox;
    private javax.swing.JLabel logSamplingLabel;
    private javax.swing.JRadioButton logTSRadio;
    private javax.swing.JPanel loggingPanel;

    private JComboBox validationSamplingComboBox;
    private JLabel validationSamplingLabel;
    private JCheckBox validateCheckBox;
    private JPanel validationPanel;
    private JTextField validatorAddressField;
    private JLabel validatorAddressLabel;
    private JTextField validatorPortField;
    private JLabel validatorPortLabel;

    private JPopupMenu streamsPopup = new JPopupMenu();

    /** Creates new form Sink */
    public SinkDetail(SinkConfig sink) {
        super(null);
        initComponents();

        this.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);

        this.streams = new ArrayList<String>();

        if(sink != null) {
        	this.oldCfg = sink;
        	this.op = UPDATE;
        	setTitle("Editing \"" + sink.getAlias() + "\"");
        	fillProperties(sink);
        }
        else {
        	this.streams = new ArrayList<String>(1);
        	this.op = INSERT;
        	setTitle("New Sink");
        }

        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    public void fillProperties(SinkConfig sink) {
    	this.aliasField.setText(sink.getAlias());
    	this.addressField.setText(sink.getAddress().getHostAddress());
    	this.portField.setText(sink.getPort()+"");
    	this.serverAddressField.setText(sink.getServerAddress().getHostAddress());

    	DefaultListModel model = (DefaultListModel) this.streamsList.getModel();
    	String streams [] = sink.getOutputStreamList();

    	for (String stream : streams) {
    		model.addElement(stream);
    		this.streams.add(stream);
		}

    	logCheckBox.setSelected(sink.isLoggingEnabled());
    	setLoggingEnabled(sink.isLoggingEnabled());
    	if(sink.isLoggingEnabled()) {
    		if(sink.getFieldsToLog() == Globals.LOG_ALL_FIELDS)
    			logAllRadio.setSelected(true);
    		else
    			logTSRadio.setSelected(true);
    		double logSamplRate = sink.getLoggingSamplingRate();
    		if(logSamplRate == 1)
    			logSamplingComboBox.setSelectedItem("1");
    		else if(logSamplRate == 0.001)
    			logSamplingComboBox.setSelectedItem("0.001");
    		else
    			logSamplingComboBox.setSelectedItem(""+logSamplRate);
    	}

    	validateCheckBox.setSelected(sink.isValidationEnabled());
    	setValidationEnabled(sink.isValidationEnabled());
    	if(sink.isValidationEnabled()) {
    		validatorAddressField.setText(sink.getValidatorAddress().getHostAddress());
    		validatorPortField.setText(""+sink.getValidatorPort());
    		double validSamplRate = sink.getValidationSamplingRate();
    		if(validSamplRate == 1)
    			validationSamplingComboBox.setSelectedItem("1");
    		else if(validSamplRate == 0.001)
    			validationSamplingComboBox.setSelectedItem("0.001");
    		else
    			validationSamplingComboBox.setSelectedItem(""+validSamplRate);
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

	private void initComponents() {

        aliasLabel = new javax.swing.JLabel("Alias");
        aliasField = new javax.swing.JTextField();
        addressField = new javax.swing.JTextField();
        addressLabel = new javax.swing.JLabel("Address");
        streamsPanel = new javax.swing.JPanel();
        streamsScrollPane1 = new javax.swing.JScrollPane();
        streamsList = new javax.swing.JList();
        okBtn = new javax.swing.JButton("OK", new ImageIcon("imgs/OK.png"));
        okBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(validateFields()) {
					if(streams.isEmpty()) {
						JOptionPane.showMessageDialog(null, "There must be at least one stream.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
						return;
					}
					String streamsArr[] = new String[streams.size()];
					streams.toArray(streamsArr);
					SinkConfig newCfg;
					try {
						int validatorPort = 0;
						double validatorSamplingRate = 0;
						if(validateCheckBox.isSelected()) {
							try {
								validatorPort = Integer.parseInt(validatorPortField.getText());
								validatorSamplingRate = Double.parseDouble((String)validationSamplingComboBox.getSelectedItem());
							}
							catch (NumberFormatException nfe1) {
								JOptionPane.showMessageDialog(null, "Invalid value for PerfMon port");
								return;
							}

						}

						newCfg = new SinkConfig(aliasField.getText(), InetAddress.getByName(addressField.getText()),
												Integer.parseInt(portField.getText()), streamsArr,
												InetAddress.getByName(serverAddressField.getText()),
												logCheckBox.isSelected(),
												logAllRadio.isSelected()? Globals.LOG_ALL_FIELDS
																		: Globals.LOG_ONLY_TIMESTAMPS,
												Double.parseDouble((String)logSamplingComboBox.getSelectedItem()),
												validateCheckBox.isSelected(),
												InetAddress.getByName(validatorAddressField.getText()),
												validatorPort, validatorSamplingRate);
						if (Controller_GUI.getInstance().checkSinkUniqueConstraint(oldCfg, newCfg)) {
							switch (op) {
							case UPDATE:
								Controller_GUI.getInstance().updateSink(oldCfg, newCfg);
								dispose();
								break;
							case INSERT:
								Controller_GUI.getInstance().addSink(newCfg);
								dispose();
							}
						}
						else
							JOptionPane.showMessageDialog(null, "New configuration violates unique constraint.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
					} catch (UnknownHostException e2) {
						JOptionPane.showMessageDialog(null, "Invalid IP address.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
					}
				}
				else {
					JOptionPane.showMessageDialog(null, "One or more required fields were not correctly filled.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
				}

			}

        });
        cancelBtn = new javax.swing.JButton("Cancel", new ImageIcon("imgs/cancel.png"));
        cancelBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}

        });
        portField = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel("Port");


        serverAddressField = new javax.swing.JTextField();
        serverAddressLabel = new javax.swing.JLabel("Server Address");

        streamsList.setModel(new DefaultListModel());
        streamsList.addMouseListener(new PopupListener(streamsPopup));
        JMenuItem addStream = new JMenuItem("Add...");
        addStream.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String streamName =	JOptionPane.showInputDialog("Stream name:");
				if(streamName.isEmpty())
					JOptionPane.showMessageDialog(null, "Invalid stream name.", "Invalid input", JOptionPane.ERROR_MESSAGE);
				else if (streams.contains(streamName))
					JOptionPane.showMessageDialog(null, "Duplicate stream name.", "Invalid input", JOptionPane.ERROR_MESSAGE);
				else {
					streams.add(streamName);
					((DefaultListModel)streamsList.getModel()).addElement(streamName);
				}

			}
        });
        JMenuItem deleteStream = new JMenuItem("Delete");
        deleteStream.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		int indices[] = streamsList.getSelectedIndices();

        		int removedCount = 0;
        		for (int index : indices) {
        			streams.remove(index-removedCount);
        			((DefaultListModel)streamsList.getModel()).remove(index-removedCount);
        			removedCount++;
        		}
        	}
        });
        streamsPopup.add(addStream);
        streamsPopup.add(deleteStream);

        streamsScrollPane1.setViewportView(streamsList);


        streamsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Streams"));
        streamsPanel.setLayout(new BorderLayout());
        streamsPanel.setPreferredSize(new Dimension(25, 50));
       // streamsPanel.setMaximumSize(new Dimension(30, 50));
        streamsPanel.add(streamsScrollPane1, BorderLayout.CENTER);

        java.awt.Font f = new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11);
        logRadioGroup = new javax.swing.ButtonGroup();
        logAllRadio = new javax.swing.JRadioButton();
        logAllRadio.setSelected(true);
        logAllRadio.setFont(f);
        logTSRadio = new javax.swing.JRadioButton();
        logTSRadio.setFont(f);

        logSamplingComboBox = new javax.swing.JComboBox();
        logSamplingComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "0.5", "0.25", "0.2", "0.1", "0.05", "0.025", "0.01", "0.001" }));
        logSamplingComboBox.setSelectedItem("1");
        logSamplingLabel = new javax.swing.JLabel();
        logSamplingLabel.setFont(f);

        loggingPanel = new javax.swing.JPanel();
        logCheckBox = new javax.swing.JCheckBox();
        logCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	setLoggingEnabled(logCheckBox.isSelected());
            }
        });
        logCheckBox.setSelected(true);

        validationPanel = new JPanel();
        validationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Runtime Monitoring"));

        validateCheckBox = new JCheckBox("Send Events to FINCoS PerfMon tool");
        validateCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	setValidationEnabled(validateCheckBox.isSelected());
            }
        });

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
        validationSamplingComboBox.setPreferredSize(new Dimension(25,20));
        validationSamplingComboBox.setSelectedItem("0.1");
        validationSamplingComboBox.setEnabled(false);

        validationSamplingLabel = new JLabel("Sampling Rate");
        validationSamplingLabel.setFont(f);
        validationSamplingLabel.setEnabled(false);

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

        okBtn.setText("OK");

        cancelBtn.setText("Cancel");

        serverAddressLabel.setText("Server Address");

        loggingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Logging"));

        logCheckBox.setText("Log Events to Disk");

        logRadioGroup.add(logAllRadio);
        logAllRadio.setText("All Fields");

        logRadioGroup.add(logTSRadio);
        logTSRadio.setText("Only Timestamps");

        logSamplingLabel.setText("Sampling Rate");

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
                .addContainerGap(99, Short.MAX_VALUE))
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(loggingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(138, 138, 138)
                                .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(okBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(aliasLabel)
                                    .addComponent(aliasField, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(addressLabel)
                                    .addComponent(addressField, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(portLabel)
                                    .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(serverAddressLabel)
                                    .addComponent(serverAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(14, 14, 14)
                                .addComponent(streamsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(10, 10, 10))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(validationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(aliasLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(aliasField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(addressLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(portLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(serverAddressLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(serverAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(streamsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(loggingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(validationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }

    private boolean validateFields() {
    	int port = -1;
    	try {
    		port = Integer.parseInt(this.portField.getText());
    	} catch(NumberFormatException nfe) {
    		return false;
    	}
    	return (port > 0 && this.aliasField.getText() != null &&
    			!this.aliasField.getText().isEmpty() &&
    			this.addressField.getText() != null &&
    			!this.addressField.getText().isEmpty() &&
    			this.serverAddressField.getText() != null &&
    			!this.serverAddressField.getText().isEmpty() &&
    			(!validateCheckBox.isSelected() ||
    			 (validatorAddressField.getText() != null &&
    			  !validatorAddressField.getText().isEmpty() &&
    			  validatorPortField.getText() != null &&
    			  !validatorPortField.getText().isEmpty()
    			 )
    			)
    			);
    }

	class PopupListener extends MouseAdapter {
	    JPopupMenu popup;

	    PopupListener(JPopupMenu popupMenu) {
	        popup = popupMenu;
	    }

	    @Override
        public void mousePressed(MouseEvent e) {
	    	if (e.isPopupTrigger()) {
	            popup.show(e.getComponent(),
	                       e.getX(), e.getY());
	        }
	    }

	    @Override
        public void mouseReleased(MouseEvent e) {
	    	if (e.isPopupTrigger()) {
	            popup.show(e.getComponent(),
	                       e.getX(), e.getY());
	        }
	    }
	}

	public void disableGUI() {
		this.aliasField.setEnabled(false);
		this.addressField.setEnabled(false);
		this.portField.setEnabled(false);
		this.serverAddressField.setEnabled(false);
		this.streamsList.setEnabled(false);
		this.logCheckBox.setEnabled(false);
		this.logAllRadio.setEnabled(false);
		this.logTSRadio.setEnabled(false);
		this.logSamplingComboBox.setEnabled(false);
		this.validateCheckBox.setEnabled(false);
		this.validatorAddressField.setEnabled(false);
		this.validatorPortField.setEnabled(false);
		this.validationSamplingComboBox.setEnabled(false);
		this.okBtn.setEnabled(false);	}

}

