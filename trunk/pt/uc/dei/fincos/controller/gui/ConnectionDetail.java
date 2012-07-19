package pt.uc.dei.fincos.controller.gui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.table.DefaultTableModel;

import pt.uc.dei.fincos.controller.ConnectionConfig;

/**
 *
 * @author Marcelo
 */
public class ConnectionDetail extends ComponentDetail {

    ConnectionConfig oldCfg;
    Dialog parent;

    private JPopupMenu propsPop = new JPopupMenu();
    private JMenuItem addMenuItem = new JMenuItem("Add");
    private JMenuItem delMenuItem = new JMenuItem("Delete");

    /** Creates new form ConnectionDialog */
    public ConnectionDetail(Dialog parent, ConnectionConfig connCfg) {
        super(null);
        this.parent = parent;
        initComponents();
        addListeners();
        cepAdapterRadioBtn.setSelected(true);

        if (connCfg != null) {
            this.oldCfg = connCfg;
            this.op = UPDATE;
            setTitle("Editing \"" + connCfg.alias + "\"");
            fillProperties(connCfg);
            aliasField.setEditable(false);
        } else {
            this.op = INSERT;
            setTitle("Create new Connection");
            ((DefaultTableModel) this.propertiesTable.getModel()).addRow(new Object[] {null, null});
        }

        this.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void fillProperties(ConnectionConfig connCfg) {
        aliasField.setText(connCfg.alias);
        aliasField.setCaretPosition(0);
        if (connCfg.type == ConnectionConfig.CEP_ADAPTER) {
            cepAdapterRadioBtn.setSelected(true);
            String engine = connCfg.properties.get("engine");
            customPropertyField.setText(engine);
        } else {
            jmsRadioBtn.setSelected(true);
            String cfName = connCfg.properties.get("cfName");
            customPropertyField.setText(cfName);
        }
        customPropertyField.setCaretPosition(0);
        DefaultTableModel model = (DefaultTableModel) propertiesTable.getModel();
        for (Entry e : connCfg.properties.entrySet()) {
            if (e.getKey().equals("engine") || e.getKey().equals("cfName")) {
                continue;
            }
            model.addRow(new Object[] {e.getKey(), e.getValue()});
        }
        if (model.getRowCount() == 0) {
            ((DefaultTableModel) this.propertiesTable.getModel()).addRow(new Object[] {null, null});
        }
        propertiesTable.setAutoCreateRowSorter(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        connTypeRadioGroup = new javax.swing.ButtonGroup();
        propertiesPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        propertiesTable = new javax.swing.JTable();
        cepAdapterRadioBtn = new javax.swing.JRadioButton();
        jmsRadioBtn = new javax.swing.JRadioButton();
        customPropertyLbl = new javax.swing.JLabel();
        typeLbl = new javax.swing.JLabel();
        customPropertyField = new javax.swing.JTextField();
        aliasLbl = new javax.swing.JLabel();
        aliasField = new javax.swing.JTextField();
        okBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        propertiesLbl = new javax.swing.JLabel();


        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        propertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        propertiesPanel.setLayout(new java.awt.BorderLayout());

        propertiesTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {
                },
                new String [] {
                        "Name", "Value"
                }
        ));
        jScrollPane1.setViewportView(propertiesTable);

        propertiesPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        connTypeRadioGroup.add(cepAdapterRadioBtn);
        cepAdapterRadioBtn.setText("CEP Adapter");
        cepAdapterRadioBtn.setToolTipText("Direct connection with the CEP engine, using a custom-code adapter");

        connTypeRadioGroup.add(jmsRadioBtn);
        jmsRadioBtn.setText("JMS");
        jmsRadioBtn.setToolTipText("Communication through JMS provider");

        customPropertyLbl.setToolTipText("");
        customPropertyLbl.setPreferredSize(new java.awt.Dimension(55, 14));
        customPropertyLbl.setMinimumSize(new java.awt.Dimension(55, 14));
        customPropertyLbl.setMaximumSize(new java.awt.Dimension(55, 14));

        typeLbl.setText("Type:");

        customPropertyField.setToolTipText("name of the connection factory at the JNDI server");

        aliasLbl.setText("Alias:");
        aliasField.setToolTipText("unique identifier for this connection");

        okBtn.setIcon(new javax.swing.ImageIcon("imgs/ok.png")); // NOI18N
        okBtn.setText("OK");
        okBtn.setPreferredSize(new java.awt.Dimension(90, 30));

        cancelBtn.setIcon(new javax.swing.ImageIcon("imgs/cancel.png"));
        cancelBtn.setText("Cancel");
        cancelBtn.setPreferredSize(new java.awt.Dimension(95, 30));

        propertiesLbl.setText("Properties:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(propertiesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                                        .addContainerGap())
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(typeLbl)
                                                        .addComponent(customPropertyLbl, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
                                                        .addComponent(aliasLbl))
                                                        .addGap(18, 18, 18)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                                        .addComponent(cepAdapterRadioBtn)
                                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(jmsRadioBtn))
                                                                        .addComponent(customPropertyField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                                                                        .addComponent(aliasField, javax.swing.GroupLayout.Alignment.LEADING))
                                                                        .addGap(234, 234, 234))
                                                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                                                .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(okBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addContainerGap())
                                                                                .addGroup(layout.createSequentialGroup()
                                                                                        .addComponent(propertiesLbl)
                                                                                        .addContainerGap(404, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(aliasLbl)
                                .addComponent(aliasField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(typeLbl)
                                        .addComponent(cepAdapterRadioBtn)
                                        .addComponent(jmsRadioBtn))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(customPropertyLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(customPropertyField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(propertiesLbl)
                                                .addGap(2, 2, 2)
                                                .addComponent(propertiesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(okBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>

    private void addListeners() {
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((op == INSERT && !isUnique(aliasField.getName()))
                        ||
                    (op == UPDATE && !oldCfg.alias.equals(aliasField.getText()) && !isUnique(aliasField.getName()))) {
                    JOptionPane.showMessageDialog(null, "There is already a connection named \""
                            + aliasField.getText() + "\".", "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    String alias = aliasField.getText();
                    int type = cepAdapterRadioBtn.isSelected() ? ConnectionConfig.CEP_ADAPTER : ConnectionConfig.JMS;
                    LinkedHashMap<String, String> props = new LinkedHashMap<String, String>();
                    if (type == ConnectionConfig.CEP_ADAPTER) {
                        props.put("engine", customPropertyField.getText());
                    } else {
                        props.put("cfName", customPropertyField.getText());
                    }
                    for (int i = 0; i < propertiesTable.getRowCount(); i++) {
                        String name = (String) propertiesTable.getValueAt(i, 0);
                        String value = (String) propertiesTable.getValueAt(i, 1);
                        if (name == null || name.isEmpty()) {
                            continue;
                        }
                        props.put(name, value);
                    }
                    ConnectionConfig newCfg = new ConnectionConfig(alias, type, props);
                    if (parent instanceof ConnectionsDialog) {
                        switch (op) {
                        case UPDATE:
                            ((ConnectionsDialog) parent).updateConnection(oldCfg, newCfg);
                            break;
                        case INSERT:
                            ((ConnectionsDialog) parent).addConnection(newCfg);
                        }
                    } else if (parent instanceof DriverDetail) {
                        try {
                            Controller_GUI.getInstance().addConnection(newCfg);
                            ((DriverDetail) parent).updateConnectionsList();
                        } catch (Exception e1) {
                            JOptionPane.showMessageDialog(null, "Could not create connection ("
                                    + e1.getMessage() + ").", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else if (parent instanceof SinkDetail) {
                        try {
                            Controller_GUI.getInstance().addConnection(newCfg);
                            ((SinkDetail) parent).updateConnectionsList();
                        } catch (Exception e1) {
                            JOptionPane.showMessageDialog(null, "Could not create connection ("
                                    + e1.getMessage() + ").", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    dispose();
                }
            }
        });

        java.awt.event.ItemListener radioLsnr = new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (cepAdapterRadioBtn.isSelected()) {
                    customPropertyLbl.setText("Engine:");
                    customPropertyLbl.setToolTipText("name of the CEP engine");
                    customPropertyField.setToolTipText("name of the CEP engine");
                } else {
                    customPropertyLbl.setText("CF name:");
                    customPropertyLbl.setToolTipText("name of the connection factory at the JNDI server");
                    customPropertyField.setToolTipText("name of the connection factory at the JNDI server");
                }

            }
        };
        cepAdapterRadioBtn.addItemListener(radioLsnr);
        jmsRadioBtn.addItemListener(radioLsnr);

        propsPop.add(addMenuItem);
        propsPop.add(delMenuItem);
        propertiesTable.addMouseListener(new PopupListener(propsPop));

        addMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel model = ((DefaultTableModel) propertiesTable.getModel());
                model.addRow(new Object[]{null, null});
                propertiesTable.requestFocusInWindow();
             //   propertiesTable.setRowSelectionInterval(model.getRowCount() - 1, model.getRowCount() - 1);
                propertiesTable.editCellAt(model.getRowCount() - 1, 0);
            }
        });

        delMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel model = ((DefaultTableModel) propertiesTable.getModel());
                int selected = propertiesTable.convertRowIndexToModel(propertiesTable.getSelectedRow());
                if (selected != -1) {
                    if (model.getRowCount() == 1) {
                        propertiesTable.setValueAt(null, 0, 0);
                        propertiesTable.setValueAt(null, 0, 1);
                    } else {
                        model.removeRow(selected);
                    }

                }
            }
        });
    }

    /**
     * Checks unique constraint for a given connection name.
     *
     * @param alias     connection alias
     * @return          <tt>true</tt> if there is no other connection in the set with the alias
     *                  passed as argument, <tt>false</tt> otherwise.
     */
    protected boolean isUnique(String alias) {
        for (ConnectionConfig c : Controller_GUI.getInstance().getConnections()) {
            if (c.alias.equals(alias)) {
                return false;
            }
        }
        return true;
    }

    // Variables declaration - do not modify
    private javax.swing.JTextField aliasField;
    private javax.swing.JLabel aliasLbl;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JRadioButton cepAdapterRadioBtn;
    private javax.swing.ButtonGroup connTypeRadioGroup;
    private javax.swing.JTextField customPropertyField;
    private javax.swing.JLabel customPropertyLbl;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton jmsRadioBtn;
    private javax.swing.JButton okBtn;
    private javax.swing.JLabel propertiesLbl;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JTable propertiesTable;
    private javax.swing.JLabel typeLbl;
    // End of variables declaration

}
