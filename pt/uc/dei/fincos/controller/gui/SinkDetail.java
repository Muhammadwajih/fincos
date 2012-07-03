package pt.uc.dei.fincos.controller.gui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.controller.ConnectionConfig;
import pt.uc.dei.fincos.controller.SinkConfig;


/**
 * GUI for configuration of Sinks
 *
 * @author Marcelo R.N. Mendes
 *
 */
@SuppressWarnings("serial")
public class SinkDetail extends ComponentDetail {

    //private ArrayList<String> streams;
    private SinkConfig oldCfg;

    private javax.swing.JTextField addressField;
    private javax.swing.JLabel addressLabel;
    private javax.swing.JTextField aliasField;
    private javax.swing.JLabel aliasLabel;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JComboBox connCombo;
    private javax.swing.JLabel connLbl;
    private javax.swing.JList streamsList;
    private javax.swing.JScrollPane streamsScrollPane1;
    private javax.swing.JButton okBtn;
    private javax.swing.JPanel streamsPanel;

    private javax.swing.JRadioButton logAllRadio;
    private javax.swing.ButtonGroup logBtnGroup;
    private javax.swing.JCheckBox logCheckBox;
    private javax.swing.JTextField logFlushField;
    private javax.swing.JLabel logFlushLbl;
    private javax.swing.JComboBox logSamplingComboBox;
    private javax.swing.JLabel logSamplingLabel;
    private javax.swing.JRadioButton logTSRadio;
    private javax.swing.JPanel loggingPanel;

    private JPopupMenu streamsPopup = new JPopupMenu();

    /** Creates new form Sink */
    public SinkDetail(SinkConfig sink) {
        super(null);
        initComponents();
        addListeners();

        this.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);

        if (sink != null) {
            this.oldCfg = sink;
            this.op = UPDATE;
            setTitle("Editing \"" + sink.getAlias() + "\"");
            fillProperties(sink);
        } else {
            this.op = INSERT;
            setTitle("New Sink");
        }

        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public void fillProperties(SinkConfig sink) {
        this.aliasField.setText(sink.getAlias());
        this.addressField.setText(sink.getAddress().getHostAddress());
        if (sink.getConnection() != null) {
            int connIndex = Controller_GUI.getInstance().getConnectionIndex(sink.getConnection().alias);
            this.connCombo.setSelectedIndex(connIndex);
        } else {
            this.connCombo.setSelectedIndex(-1);
        }

        DefaultListModel model = (DefaultListModel) this.streamsList.getModel();
        String[] streams  = sink.getOutputStreamList();

        for (String stream : streams) {
            model.addElement(stream);
        }

        logCheckBox.setSelected(sink.isLoggingEnabled());
        setLoggingEnabled(sink.isLoggingEnabled());
        if (sink.isLoggingEnabled()) {
            if (sink.getFieldsToLog() == Globals.LOG_ALL_FIELDS) {
                logAllRadio.setSelected(true);
            } else {
                logTSRadio.setSelected(true);
            }
            double logSamplRate = sink.getLoggingSamplingRate();
            if (logSamplRate == 1) {
                logSamplingComboBox.setSelectedItem("1");
            } else if (logSamplRate == 0.001) {
                logSamplingComboBox.setSelectedItem("0.001");
            } else {
                logSamplingComboBox.setSelectedItem("" + logSamplRate);
            }
            logFlushField.setText("" + sink.getLogFlushInterval());
        }
    }

    private void setLoggingEnabled(boolean enabled) {
        logAllRadio.setEnabled(enabled);
        logTSRadio.setEnabled(enabled);
        logSamplingLabel.setEnabled(enabled);
        logSamplingComboBox.setEnabled(enabled);
    }

    private void initComponents() {
        logBtnGroup = new javax.swing.ButtonGroup();
        aliasLabel = new javax.swing.JLabel();
        aliasField = new javax.swing.JTextField();
        streamsPanel = new javax.swing.JPanel();
        streamsScrollPane1 = new javax.swing.JScrollPane();
        streamsList = new javax.swing.JList();
        addressLabel = new javax.swing.JLabel();
        addressField = new javax.swing.JTextField();
        okBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        connLbl = new javax.swing.JLabel();
        loggingPanel = new javax.swing.JPanel();
        logCheckBox = new javax.swing.JCheckBox();
        logAllRadio = new javax.swing.JRadioButton();
        logTSRadio = new javax.swing.JRadioButton();
        logSamplingLabel = new javax.swing.JLabel();
        logSamplingComboBox = new javax.swing.JComboBox();
        logFlushLbl = new javax.swing.JLabel();
        logFlushField = new javax.swing.JTextField();
        connCombo = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        aliasLabel.setText("Alias");

        streamsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Streams"));

        streamsList.setModel(new DefaultListModel());
        streamsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        streamsScrollPane1.setViewportView(streamsList);

        javax.swing.GroupLayout streamsPanelLayout = new javax.swing.GroupLayout(streamsPanel);
        streamsPanel.setLayout(streamsPanelLayout);
        streamsPanelLayout.setHorizontalGroup(
                streamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(streamsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(streamsScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                        .addContainerGap())
        );
        streamsPanelLayout.setVerticalGroup(
                streamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(streamsPanelLayout.createSequentialGroup()
                        .addComponent(streamsScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                        .addContainerGap())
        );

        addressLabel.setText("Address");

        okBtn.setText("OK");

        cancelBtn.setText("Cancel");

        connLbl.setText("Receive Events from:");

        loggingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Logging"));

        logCheckBox.setSelected(true);
        logCheckBox.setText("Log Events to Disk");

        logBtnGroup.add(logAllRadio);
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
                                                        .addComponent(logSamplingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(logSamplingLabel))
                                                        .addGap(18, 18, 18)
                                                        .addGroup(loggingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                .addComponent(logFlushField)
                                                                .addComponent(logFlushLbl))
                                                                .addContainerGap(15, Short.MAX_VALUE))
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
                                .addGap(14, 14, 14)
                                .addGroup(loggingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(loggingPanelLayout.createSequentialGroup()
                                                .addComponent(logFlushLbl)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(logFlushField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(loggingPanelLayout.createSequentialGroup()
                                                        .addComponent(logSamplingLabel)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(logSamplingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(loggingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                .addComponent(aliasLabel)
                                                                .addComponent(aliasField, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
                                                                .addComponent(addressLabel)
                                                                .addComponent(addressField, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
                                                                .addComponent(connLbl)
                                                                .addComponent(connCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                .addGap(14, 14, 14)
                                                                .addComponent(streamsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                                                .addContainerGap())
                                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                                        .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(okBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addContainerGap())))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(aliasLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(aliasField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(addressLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(addressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(connLbl)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(connCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(streamsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(loggingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(okBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addContainerGap(16, Short.MAX_VALUE))
        );

        pack();

        okBtn.setIcon(new ImageIcon("imgs/OK.png"));
        cancelBtn.setIcon(new ImageIcon("imgs/cancel.png"));
        initConnCombo();
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
                if (validateFields()) {
                    DefaultListModel model = (DefaultListModel) streamsList.getModel();
                    if (model.size() == 0) {
                        JOptionPane.showMessageDialog(null, "There must be at least one stream.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String[] streamsArr = new String[model.size()];
                    for (int i = 0; i < model.size(); i++) {
                        streamsArr[i] = (String) model.elementAt(i);
                    }
                    SinkConfig newCfg;
                    try {
                        int connIndex = connCombo.getSelectedIndex();
                        ConnectionConfig connCfg = Controller_GUI.getInstance().getConnection(connIndex);

                        newCfg = new SinkConfig(aliasField.getText(), InetAddress.getByName(addressField.getText()),
                                connCfg, streamsArr, logCheckBox.isSelected(),
                                logAllRadio.isSelected() ? Globals.LOG_ALL_FIELDS : Globals.LOG_ONLY_TIMESTAMPS,
                                        Double.parseDouble((String) logSamplingComboBox.getSelectedItem()),
                                        Integer.parseInt(logFlushField.getText()));
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
                        } else {
                            JOptionPane.showMessageDialog(null, "New configuration violates unique constraint.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (UnknownHostException e2) {
                        JOptionPane.showMessageDialog(null, "Invalid IP address.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "One or more required fields were not correctly filled.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        streamsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    String streamName = JOptionPane.showInputDialog("New stream name:");
                    ((DefaultListModel) streamsList.getModel()).setElementAt(streamName, index);
                }
            }
        });

        streamsList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    deleteStream();
                }
            }
        });

        streamsList.addMouseListener(new PopupListener(streamsPopup));
        JMenuItem addStream = new JMenuItem("Add...");
        JMenuItem deleteStream = new JMenuItem("Delete");
        deleteStream.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteStream();
            }
        });
        streamsPopup.add(addStream);
        streamsPopup.add(deleteStream);

        addStream.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String streamName = JOptionPane.showInputDialog("Stream name:");
                if (streamName.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Invalid stream name.", "Invalid input", JOptionPane.ERROR_MESSAGE);
                } else if (!checkUniqueStreamName(streamName)) {
                    JOptionPane.showMessageDialog(null, "Duplicate stream name.", "Invalid input", JOptionPane.ERROR_MESSAGE);
                } else {
                    ((DefaultListModel)streamsList.getModel()).addElement(streamName);
                }

            }
        });

        logCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setLoggingEnabled(logCheckBox.isSelected());
            }
        });
    }

    /**
     * Deletes the selected stream(s) on the Jlist.
     */
    protected void deleteStream() {
        int[] indices = streamsList.getSelectedIndices();

        int removedCount = 0;
        for (int index : indices) {
            ((DefaultListModel) streamsList.getModel()).remove(index - removedCount);
            removedCount++;
        }
    }

    private boolean validateFields() {
        return (this.aliasField.getText() != null
                && !this.aliasField.getText().isEmpty()
                && this.addressField.getText() != null
                && !this.addressField.getText().isEmpty()
                && this.connCombo.getSelectedIndex() != -1
                && this.connCombo.getSelectedIndex() < Controller_GUI.getInstance().getConnections().length);
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

    /**
     * Checks if a given stream name is unique.
     *
     * @param newStream    the stream whose uniqueness must be checked
     * @return             <tt>true</tt> if there is no stream in this Sink with the specified name,
     *                     <tt>true</tt> otherwise.
     */
    private boolean checkUniqueStreamName(String newStream) {
        DefaultListModel model = (DefaultListModel) this.streamsList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (newStream.equals(model.elementAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Disables user input on this form.
     */
    public void disableGUI() {
        this.aliasField.setEnabled(false);
        this.addressField.setEnabled(false);
        this.connLbl.setEnabled(false);
        this.connCombo.setEnabled(false);
        this.streamsList.setEnabled(false);
        this.logCheckBox.setEnabled(false);
        this.logAllRadio.setEnabled(false);
        this.logTSRadio.setEnabled(false);
        this.logSamplingComboBox.setEnabled(false);
        this.logSamplingLabel.setEnabled(false);
        this.logFlushLbl.setEnabled(false);
        this.logFlushField.setEnabled(false);
        this.okBtn.setEnabled(false);

    }

}

