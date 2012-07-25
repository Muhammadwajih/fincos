package pt.uc.dei.fincos.controller.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import pt.uc.dei.fincos.driver.ExternalFileWorkloadPhase;

/**
 * Form for configuring a workload based on a third-party dataset file.
 *
 * @author Marcelo R.N. Mendes
 *
 */
public class ExternalFilePhasePanel extends javax.swing.JPanel {

    /** serial id. */
    private static final long serialVersionUID = -3941829337826804375L;

    /** sample record */
    private HashMap<String, String> recordExample;

    private JFileChooser fileChooser;

    private static final Font plain = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    private static final Font bold = new Font(Font.SANS_SERIF, Font.BOLD, 11);

    /** Creates new form ExternalFilePhasePanel */
    public ExternalFilePhasePanel() {
        recordExample = new HashMap<String, String>();
        recordExample.put("timestamp", "08:15:00");
        recordExample.put("type", "SensorReading");
        recordExample.put("payload1", "25");
        recordExample.put("payload2", "0.7");
        fileChooser = new JFileChooser("./data/");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        initComponents();
        addListeners();
    }

    private void initComponents() {
        delimiterBtnGroup = new javax.swing.ButtonGroup();
        typeBtnGroup = new javax.swing.ButtonGroup();
        loadSubmissionBtnGroup = new javax.swing.ButtonGroup();
        filePathLbl = new javax.swing.JLabel();
        filePathField = new javax.swing.JTextField();
        browseBtn = new javax.swing.JButton();
        optionsPanel = new javax.swing.JPanel();
        useTSRdBtn = new javax.swing.JRadioButton();
        useFixedRateRdBtn = new javax.swing.JRadioButton();
        rateField = new javax.swing.JTextField();
        loopLbl = new javax.swing.JLabel();
        loopSpinner = new javax.swing.JSpinner();
        delimiterPanel = new javax.swing.JPanel();
        commaRdBtn = new javax.swing.JRadioButton();
        semicolonRdBtn = new javax.swing.JRadioButton();
        spaceRdBtn = new javax.swing.JRadioButton();
        tabRdBtn = new javax.swing.JRadioButton();
        otherRdBtn = new javax.swing.JRadioButton();
        otherCharField = new javax.swing.JTextField();
        tsCheckBox = new javax.swing.JCheckBox();
        tsUnitCombo = new javax.swing.JComboBox();
        sendTSCheck = new javax.swing.JCheckBox();
        typePanel = new javax.swing.JPanel();
        typeYesRdBtn = new javax.swing.JRadioButton();
        typeNoRdBtn = new javax.swing.JRadioButton();
        typeField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        schemaPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        schemaTable = new javax.swing.JTable();
        exampleLbl2 = new javax.swing.JLabel();
        exampleLbl1 = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        filePathLbl.setText("Path:");

        filePathField.setToolTipText("Path for the datafile.");

        browseBtn.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        browseBtn.setText("...");
        browseBtn.setPreferredSize(new java.awt.Dimension(20, 20));

        optionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Load Submission Options"));

        loadSubmissionBtnGroup.add(useTSRdBtn);
        useTSRdBtn.setSelected(true);
        useTSRdBtn.setText("Use the timestamps in the data file");

        loadSubmissionBtnGroup.add(useFixedRateRdBtn);
        useFixedRateRdBtn.setText("Fixed submission rate (evts/sec):");

        loopLbl.setText("Loop count:");
        loopLbl.setToolTipText("Number of times the data file must be read/submitted.");

        loopSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        loopSpinner.setToolTipText("Number of times the data file must be read/submitted.");

        javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, optionsPanelLayout.createSequentialGroup()
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(useTSRdBtn)
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addComponent(useFixedRateRdBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rateField, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 57, Short.MAX_VALUE)
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(loopSpinner, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(loopLbl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(54, 54, 54))
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useTSRdBtn)
                    .addComponent(loopLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useFixedRateRdBtn)
                    .addComponent(rateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(loopSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(9, Short.MAX_VALUE))
        );

        delimiterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Delimiter"));
        delimiterPanel.setToolTipText("Character(s) used to separate the fields of the records in the datafile.");

        delimiterBtnGroup.add(commaRdBtn);
        commaRdBtn.setSelected(true);
        commaRdBtn.setText("Comma");

        delimiterBtnGroup.add(semicolonRdBtn);
        semicolonRdBtn.setText("Semicolon");

        delimiterBtnGroup.add(spaceRdBtn);
        spaceRdBtn.setText("Space");

        delimiterBtnGroup.add(tabRdBtn);
        tabRdBtn.setText("Tab");

        delimiterBtnGroup.add(otherRdBtn);
        otherRdBtn.setText("Other:");

        javax.swing.GroupLayout delimiterPanelLayout = new javax.swing.GroupLayout(delimiterPanel);
        delimiterPanel.setLayout(delimiterPanelLayout);
        delimiterPanelLayout.setHorizontalGroup(
            delimiterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(delimiterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(commaRdBtn)
                .addGap(15, 15, 15)
                .addComponent(semicolonRdBtn)
                .addGap(16, 16, 16)
                .addComponent(spaceRdBtn)
                .addGap(18, 18, 18)
                .addComponent(tabRdBtn)
                .addGap(18, 18, 18)
                .addComponent(otherRdBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(otherCharField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );
        delimiterPanelLayout.setVerticalGroup(
            delimiterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(delimiterPanelLayout.createSequentialGroup()
                .addGroup(delimiterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(commaRdBtn)
                    .addComponent(semicolonRdBtn)
                    .addComponent(spaceRdBtn)
                    .addComponent(tabRdBtn)
                    .addComponent(otherRdBtn)
                    .addComponent(otherCharField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tsCheckBox.setSelected(true);
        tsCheckBox.setText("Data file contains timestamps, specified in");

        tsUnitCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Milliseconds", "Seconds", "Date/Time" }));

        sendTSCheck.setText("Send timestamps to the engine.");
        sendTSCheck.setToolTipText("Include the timestamp as a field of the events sent to the system under test.");

        typePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Type"));

        typeBtnGroup.add(typeYesRdBtn);
        typeYesRdBtn.setSelected(true);
        typeYesRdBtn.setText("Data file contains types");

        typeBtnGroup.add(typeNoRdBtn);
        typeNoRdBtn.setText("Data file DOES NOT contain types (all records are of the type:");

        jLabel2.setText(")");

        javax.swing.GroupLayout typePanelLayout = new javax.swing.GroupLayout(typePanel);
        typePanel.setLayout(typePanelLayout);
        typePanelLayout.setHorizontalGroup(
            typePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(typePanelLayout.createSequentialGroup()
                .addGroup(typePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(typeYesRdBtn)
                    .addGroup(typePanelLayout.createSequentialGroup()
                        .addComponent(typeNoRdBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(typeField, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)))
                .addContainerGap())
        );
        typePanelLayout.setVerticalGroup(
            typePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(typePanelLayout.createSequentialGroup()
                .addComponent(typeYesRdBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(typePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(typeNoRdBtn)
                    .addComponent(typeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)))
        );

        schemaPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Schema"));
        schemaPanel.setToolTipText("Drag and drop to edit the order of the fields in the datafile.");

        schemaTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "timestamp", "type", "payload"
            }
        ));
        schemaTable.setToolTipText("Drag and drop to edit the order of the fields in the datafile.");
        jScrollPane1.setViewportView(schemaTable);

        exampleLbl2.setFont(new java.awt.Font("Courier New", 0, 11)); // NOI18N
        exampleLbl2.setText("[08:15:00,SensorReading,25,0.7]");

        exampleLbl1.setText("Example:");

        javax.swing.GroupLayout schemaPanelLayout = new javax.swing.GroupLayout(schemaPanel);
        schemaPanel.setLayout(schemaPanelLayout);
        schemaPanelLayout.setHorizontalGroup(
            schemaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(schemaPanelLayout.createSequentialGroup()
                .addGroup(schemaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(schemaPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE))
                    .addGroup(schemaPanelLayout.createSequentialGroup()
                        .addGap(72, 72, 72)
                        .addComponent(exampleLbl1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exampleLbl2)))
                .addContainerGap())
        );
        schemaPanelLayout.setVerticalGroup(
            schemaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(schemaPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(schemaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exampleLbl2)
                    .addComponent(exampleLbl1)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(schemaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(sendTSCheck))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(filePathLbl)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(filePathField, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browseBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(delimiterPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(tsCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tsUnitCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(typePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(optionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(filePathField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(browseBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(delimiterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tsCheckBox)
                            .addComponent(tsUnitCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sendTSCheck))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(filePathLbl)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(typePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(schemaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(optionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>

    private void addListeners() {
        browseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                fileChooser.showOpenDialog(null);
                if (fileChooser.getSelectedFile() != null) {
                    filePathField.setText(fileChooser.getSelectedFile().getPath());
                    filePathField.setCaretPosition(0);
                }
            }
        });

        ItemListener delimiterRadioListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    revalidateSchema();
                }
            }
        };
        commaRdBtn.addItemListener(delimiterRadioListener);
        semicolonRdBtn.addItemListener(delimiterRadioListener);
        spaceRdBtn.addItemListener(delimiterRadioListener);
        tabRdBtn.addItemListener(delimiterRadioListener);
        otherRdBtn.addItemListener(delimiterRadioListener);

        otherCharField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                revalidateSchema();
            }
        });
        otherCharField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                otherRdBtn.setSelected(true);
            }
        });

        tsCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                useTSRdBtn.setEnabled(tsCheckBox.isSelected());
                TableColumnModel tcm = schemaTable.getColumnModel();
                if (!tsCheckBox.isSelected()) {
                    TableColumn column = null;
                    for (int i = 0; i < tcm.getColumnCount(); i++) {
                        column = tcm.getColumn(i);
                        if (column.getHeaderValue().equals("timestamp")) {
                            break;
                        } else {
                            column = null;
                        }
                    }
                    if (column != null) {
                        tcm.removeColumn(column);
                    }
                    useFixedRateRdBtn.setSelected(true);
                } else {
                    TableColumn column = new TableColumn();
                    column.setHeaderValue("timestamp");
                    tcm.addColumn(column);
                }
                tsUnitCombo.setEnabled(tsCheckBox.isSelected());
                sendTSCheck.setEnabled(tsCheckBox.isSelected());
            }
        });

        schemaTable.getTableHeader().setPreferredSize(new Dimension(10, 23));
        schemaTable.getTableHeader().setDefaultRenderer(new TableHeaderRenderer(schemaTable.getTableHeader()));

        schemaTable.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {}
            @Override
            public void columnRemoved(TableColumnModelEvent e) {revalidateSchema();}
            @Override
            public void columnMoved(TableColumnModelEvent e) {revalidateSchema();}
            @Override
            public void columnMarginChanged(ChangeEvent e) {}
            @Override
            public void columnAdded(TableColumnModelEvent e) {revalidateSchema();}
        });

        typeField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                typeNoRdBtn.setSelected(true);
            }
        });

        ItemListener ls = new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    TableColumnModel tcm = schemaTable.getColumnModel();
                    if (typeNoRdBtn.isSelected()) {
                        TableColumn column = null;
                        for (int i = 0; i < tcm.getColumnCount(); i++) {
                            column = tcm.getColumn(i);
                            if (column.getHeaderValue().equals("type")) {
                                break;
                            } else {
                                column = null;
                            }
                        }
                        if (column != null) {
                            tcm.removeColumn(column);
                        }
                    } else {
                        TableColumn column = new TableColumn();
                        column.setHeaderValue("type");
                        tcm.addColumn(column);
                    }
                }
            }
        };
        typeNoRdBtn.addItemListener(ls);
        typeYesRdBtn.addItemListener(ls);
    }

    /**
     * Fills the UI with the parameters of the workload phase.
     *
     * @param phase workload parameters
     */
    public void fillProperties(ExternalFileWorkloadPhase phase) {
        filePathField.setText(phase.getFilePath());
        filePathField.setCaretPosition(0);

        if (phase.getDelimiter().equals(",")) {
            commaRdBtn.setSelected(true);
        } else if (phase.getDelimiter().equals(";")) {
            semicolonRdBtn.setSelected(true);
        } else if (phase.getDelimiter().equals(" ")) {
            spaceRdBtn.setSelected(true);
        } else if (phase.getDelimiter().equals("\t")) {
            tabRdBtn.setSelected(true);
        } else {
            otherCharField.setText(phase.getDelimiter());
            otherRdBtn.setSelected(true);
        }

        tsCheckBox.setSelected(phase.containsTimestamps());
        if (phase.containsTimestamps()) {
            tsUnitCombo.setSelectedIndex(phase.getTimestampUnit());
            sendTSCheck.setSelected(phase.isIncludingTS());
        }

        if (phase.containsEventTypes()) {
            typeYesRdBtn.setSelected(true);
        } else {
            typeNoRdBtn.setSelected(true);
            typeField.setText(phase.getSingleEventTypeName());
        }

        TableColumnModel tcm = schemaTable.getColumnModel();
        // Clears the schema table
        int colCount = tcm.getColumnCount();
        for (int i = 0; i < colCount; i++) {
            TableColumn col = tcm.getColumn(0);
            tcm.removeColumn(col);
        }
        TableColumn tsCol = new TableColumn();
        tsCol.setHeaderValue("timestamp");
        TableColumn typeCol = new TableColumn();
        typeCol.setHeaderValue("type");
        TableColumn payloadCol = new TableColumn();
        payloadCol.setHeaderValue("payload");
        switch (phase.getTimestampIndex()) {
        case ExternalFileWorkloadPhase.FIRST_FIELD:
            tcm.addColumn(tsCol);
            switch (phase.getTypeIndex()) {
            case ExternalFileWorkloadPhase.SECOND_FIELD:
                tcm.addColumn(typeCol);
                tcm.addColumn(payloadCol);
                break;
            case ExternalFileWorkloadPhase.LAST_FIELD:
                tcm.addColumn(payloadCol);
                tcm.addColumn(typeCol);
                break;
            case -1: // No type
                tcm.addColumn(payloadCol);
                break;
            default:
                throw new RuntimeException("Incompatible indexes for timestamp and type fields.");
            }
            break;
        case ExternalFileWorkloadPhase.SECOND_FIELD:
            switch (phase.getTypeIndex()) {
            case ExternalFileWorkloadPhase.FIRST_FIELD:
                tcm.addColumn(typeCol);
                tcm.addColumn(tsCol);
                tcm.addColumn(payloadCol);
                break;
            default:
                throw new RuntimeException("Incompatible indexes for timestamp and type fields.");
            }
            break;
        case ExternalFileWorkloadPhase.LAST_FIELD:
            switch (phase.getTypeIndex()) {
            case ExternalFileWorkloadPhase.FIRST_FIELD:
                tcm.addColumn(typeCol);
                tcm.addColumn(payloadCol);
                tcm.addColumn(tsCol);
                break;
            case ExternalFileWorkloadPhase.SECOND_LAST_FIELD:
                tcm.addColumn(payloadCol);
                tcm.addColumn(typeCol);
                tcm.addColumn(tsCol);
                break;
            case -1: // No type
                tcm.addColumn(payloadCol);
                tcm.addColumn(tsCol);
                break;
            default:
                throw new RuntimeException("Incompatible indexes for timestamp and type fields.");
            }
            break;
        case ExternalFileWorkloadPhase.SECOND_LAST_FIELD:
            switch (phase.getTypeIndex()) {
            case ExternalFileWorkloadPhase.LAST_FIELD:
                tcm.addColumn(payloadCol);
                tcm.addColumn(tsCol);
                tcm.addColumn(typeCol);
                break;
            default:
                throw new RuntimeException("Incompatible indexes for timestamp and type fields.");
            }
            break;
        case -1: // No timestamp
            switch (phase.getTypeIndex()) {
            case ExternalFileWorkloadPhase.FIRST_FIELD:
                tcm.addColumn(typeCol);
                tcm.addColumn(payloadCol);
                break;
            case ExternalFileWorkloadPhase.LAST_FIELD:
                tcm.addColumn(payloadCol);
                tcm.addColumn(typeCol);
                break;
            case -1: // No type
                tcm.addColumn(payloadCol);
                break;
            default:
                throw new RuntimeException("Incompatible indexes for timestamp and type fields.");
            }
            break;
        }

        if (phase.isUsingTimestamps()) {
            useTSRdBtn.setSelected(true);
        } else {
            useFixedRateRdBtn.setSelected(true);
            rateField.setText(phase.getEventSubmissionRate() + "");
        }


        loopSpinner.setValue(phase.getLoopCount());
    }

    private void revalidateSchema() {
        StringBuilder sb = new StringBuilder();
        TableColumnModel tcm = schemaTable.getColumnModel();
        sb.append("[");
        for (int i = 0; i < tcm.getColumnCount(); i++) {
            String col = (String) tcm.getColumn(i).getHeaderValue();
            if (col.equals("timestamp")) {
                sb.append(recordExample.get("timestamp"));
                sb.append(getDelimiter(true));
            } else if (col.equals("type")) {
                sb.append(recordExample.get("type"));
                sb.append(getDelimiter(true));
            } else if (col.equals("payload")) {
                sb.append(recordExample.get("payload1"));
                sb.append(getDelimiter(true));
                sb.append(recordExample.get("payload2"));
                sb.append(getDelimiter(true));
            }
        }
        String s = sb.toString();
        if (tcm.getColumnCount() > 0) {
            exampleLbl2.setText(s.substring(0, s.length() - getDelimiter(true).length()) + "]");
        }
    }

    /**
     *
     * @param UI    the delimiter is to be shown on UI?
     * @return      the character used to delimit the fields of records in the datafile
     */
    public String getDelimiter(boolean UI) {
        if (commaRdBtn.isSelected()) {
            return ",";
        } else if (semicolonRdBtn.isSelected()) {
            return ";";
        } else if (spaceRdBtn.isSelected()) {
            return " ";
        } else if (tabRdBtn.isSelected()) {
            if (UI) {
                return "   ";
            }  else {
                return "\t";
            }
        } else if (otherRdBtn.isSelected()) {
            return otherCharField.getText();
        }

        return null;
    }

    /**
     *
     * @return  the index of the "timestamp" field in the data file
     */
    public int getTSIndex() {
        TableColumnModel tcm = schemaTable.getColumnModel();
        int tsIndex = -1;
        int typeIndex = -1;
        for (int i = 0; i < tcm.getColumnCount(); i++) {
            String col = (String) tcm.getColumn(i).getHeaderValue();
            if (col.equals("timestamp")) {
                tsIndex = i;
            }
            if (col.equals("type")) {
                typeIndex = i;
            }
        }

        if (tsIndex == 0) {
            tsIndex = ExternalFileWorkloadPhase.FIRST_FIELD;
        } else if (tsIndex == 2) {
            tsIndex = ExternalFileWorkloadPhase.LAST_FIELD;
        } else if (tsIndex == 1) {
            if (typeIndex == 0) {
                tsIndex = ExternalFileWorkloadPhase.SECOND_FIELD;
            } else if (typeIndex == 2) {
                tsIndex = ExternalFileWorkloadPhase.SECOND_LAST_FIELD;
            } else {
                tsIndex = ExternalFileWorkloadPhase.LAST_FIELD;
            }

        }

        return tsIndex;
    }

    /**
     *
     * @return  the index of the "type" field in the data file
     */
    public int getTypeIndex() {
        TableColumnModel tcm = schemaTable.getColumnModel();
        int typeIndex = -1;
        int tsIndex = -1;
        for (int i = 0; i < tcm.getColumnCount(); i++) {
            String col = (String) tcm.getColumn(i).getHeaderValue();
            if (col.equals("type")) {
                typeIndex = i;
            }
            if (col.equals("timestamp")) {
                tsIndex = i;
            }
        }

        if (typeIndex == 0) {
            typeIndex = ExternalFileWorkloadPhase.FIRST_FIELD;
        } else if (typeIndex == 2) {
            typeIndex = ExternalFileWorkloadPhase.LAST_FIELD;
        } else if (typeIndex == 1) {
            if (tsIndex == 0) {
                typeIndex = ExternalFileWorkloadPhase.SECOND_FIELD;
            } else if (tsIndex == 2) {
                typeIndex = ExternalFileWorkloadPhase.SECOND_LAST_FIELD;
            } else {
                typeIndex = ExternalFileWorkloadPhase.LAST_FIELD;
            }

        }

        return typeIndex;
    }

    // Variables declaration - do not modify
    private javax.swing.JButton browseBtn;
    private javax.swing.JRadioButton commaRdBtn;
    private javax.swing.ButtonGroup delimiterBtnGroup;
    private javax.swing.JPanel delimiterPanel;
    private javax.swing.JLabel exampleLbl1;
    private javax.swing.JLabel exampleLbl2;
    protected javax.swing.JTextField filePathField;
    private javax.swing.JLabel filePathLbl;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.ButtonGroup loadSubmissionBtnGroup;
    private javax.swing.JLabel loopLbl;
    protected javax.swing.JSpinner loopSpinner;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JTextField otherCharField;
    private javax.swing.JRadioButton otherRdBtn;
    protected javax.swing.JTextField rateField;
    private javax.swing.JPanel schemaPanel;
    private javax.swing.JTable schemaTable;
    private javax.swing.JRadioButton semicolonRdBtn;
    protected javax.swing.JCheckBox sendTSCheck;
    private javax.swing.JRadioButton spaceRdBtn;
    private javax.swing.JRadioButton tabRdBtn;
    protected javax.swing.JCheckBox tsCheckBox;
    protected javax.swing.JComboBox tsUnitCombo;
    private javax.swing.ButtonGroup typeBtnGroup;
    protected javax.swing.JTextField typeField;
    protected javax.swing.JRadioButton typeNoRdBtn;
    private javax.swing.JPanel typePanel;
    protected javax.swing.JRadioButton typeYesRdBtn;
    protected javax.swing.JRadioButton useFixedRateRdBtn;
    protected javax.swing.JRadioButton useTSRdBtn;
    // End of variables declaration
}


class TableHeaderRenderer extends DefaultTableCellRenderer
implements MouseListener, MouseMotionListener {
    private JTableHeader header;
    private DefaultTableCellRenderer oldRenderer;
    private int rolloverColumn = -1;

    public TableHeaderRenderer(JTableHeader header) {
        this.header = header;
        this.oldRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();

        header.addMouseListener(this);
        header.addMouseMotionListener(this);
    }

    private void updateRolloverColumn(MouseEvent e) {
        int col = header.columnAtPoint(e.getPoint());
        if (col != rolloverColumn) {
            rolloverColumn = col;
            header.repaint();
        }
    }

    public void mouseMoved(MouseEvent e) {
        updateRolloverColumn(e);
    }

    public void mouseEntered(MouseEvent e) {
        updateRolloverColumn(e);
    }

    public void mouseExited(MouseEvent e) {
        rolloverColumn = -1;
        header.repaint();
    }

    public void mousePressed(MouseEvent e) {
        rolloverColumn = -1;
        header.repaint();
    }

    public void mouseReleased(MouseEvent e) {
        updateRolloverColumn(e);
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {
        JComponent comp =
            (JComponent) oldRenderer.getTableCellRendererComponent(table,
                    value, isSelected,
                    hasFocus || (column == rolloverColumn),
                    row, column);
        //   comp.setBorder(new EmptyBorder(3, 8, 4, 8));
        if (isSelected) {
            comp.setBorder(new LineBorder(Color.CYAN));
        }

        return comp;
    }
}

