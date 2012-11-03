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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.controller.ControllerFacade;


/**
 *
 * @author  Marcelo R.N. Mendes
 */
public final class TestOptions extends javax.swing.JFrame {

    /** serial id */
    private static final long serialVersionUID = 1679474313988723749L;

    /** Creates new form TestOptions */
    private JFileChooser connPropertiesChooser;

    public TestOptions() {
        super("Test Options");
        connPropertiesChooser = new JFileChooser(Globals.APP_PATH + "config");
        connPropertiesChooser.setFileFilter(new FileNameExtensionFilter("Properties file (.properties)", "properties"));
        connPropertiesChooser.setAcceptAllFileFilterUsed(false);
        connPropertiesChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        initComponents();
        rtCheckBox.setSelected(true);
        addListeners();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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

        rtModeBtnGroup = new javax.swing.ButtonGroup();
        rtResolutionBtnGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        okBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        rtPanel = new javax.swing.JPanel();
        rtEndToEndRadio = new javax.swing.JRadioButton();
        rtAdapterRadio = new javax.swing.JRadioButton();
        rtModeLbl = new javax.swing.JLabel();
        rtCheckBox = new javax.swing.JCheckBox();
        rtResolutionLbl = new javax.swing.JLabel();
        rtCreationTimeCheckBox = new javax.swing.JCheckBox();
        rtMillisRdBtn = new javax.swing.JRadioButton();
        rtNanosRdBtn = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        okBtn.setText("OK");
        okBtn.setMaximumSize(new java.awt.Dimension(65, 23));
        okBtn.setMinimumSize(new java.awt.Dimension(65, 23));
        okBtn.setPreferredSize(new java.awt.Dimension(65, 23));

        cancelBtn.setText("Cancel");

        rtPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Response Time Measurement"));

        rtModeBtnGroup.add(rtEndToEndRadio);
        rtEndToEndRadio.setText("End-to-End (from Driver to Sink)");
        rtEndToEndRadio.setToolTipText("Event conversion time is accounted");

        rtModeBtnGroup.add(rtAdapterRadio);
        rtAdapterRadio.setText("Process Time (inside Adapters)");
        rtAdapterRadio.setToolTipText("Event conversion time is not accounted");

        rtModeLbl.setText("Mode:");

        rtCheckBox.setText("RT measurement enabled");

        rtResolutionLbl.setText("Resolution:");

        rtCreationTimeCheckBox.setText("Use Creation Time");
        rtCreationTimeCheckBox.setToolTipText("Use event's original timestamp instead of their send time");

        rtResolutionBtnGroup.add(rtMillisRdBtn);
        rtMillisRdBtn.setText("Milliseconds");

        rtResolutionBtnGroup.add(rtNanosRdBtn);
        rtNanosRdBtn.setText("Nanoseconds");

        javax.swing.GroupLayout rtPanelLayout = new javax.swing.GroupLayout(rtPanel);
        rtPanel.setLayout(rtPanelLayout);
        rtPanelLayout.setHorizontalGroup(
            rtPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rtPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rtPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rtAdapterRadio)
                    .addGroup(rtPanelLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(rtCreationTimeCheckBox))
                    .addComponent(rtCheckBox)
                    .addComponent(rtModeLbl)
                    .addComponent(rtEndToEndRadio)
                    .addComponent(rtResolutionLbl)
                    .addGroup(rtPanelLayout.createSequentialGroup()
                        .addComponent(rtMillisRdBtn)
                        .addGap(18, 18, 18)
                        .addComponent(rtNanosRdBtn)))
                .addContainerGap(99, Short.MAX_VALUE))
        );
        rtPanelLayout.setVerticalGroup(
            rtPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rtPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rtCheckBox)
                .addGap(11, 11, 11)
                .addComponent(rtModeLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rtEndToEndRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rtCreationTimeCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rtAdapterRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addComponent(rtResolutionLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rtPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rtMillisRdBtn)
                    .addComponent(rtNanosRdBtn))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rtPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(okBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rtPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(okBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 333, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 273, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addListeners() {
        rtCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                rtModeLbl.setEnabled(rtCheckBox.isSelected());
                rtEndToEndRadio.setEnabled(rtCheckBox.isSelected());
                rtCreationTimeCheckBox.setEnabled(rtCheckBox.isSelected());
                rtAdapterRadio.setEnabled(rtCheckBox.isSelected());
                rtResolutionLbl.setEnabled(rtCheckBox.isSelected());
                rtMillisRdBtn.setEnabled(rtCheckBox.isSelected());
                rtNanosRdBtn.setEnabled(rtCheckBox.isSelected());
            }
        });

        rtCreationTimeCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (rtCreationTimeCheckBox.isSelected()) {
                    rtMillisRdBtn.setSelected(true);
                }
                rtNanosRdBtn.setEnabled(!rtCreationTimeCheckBox.isSelected());
            }
        });

        rtEndToEndRadio.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                rtCreationTimeCheckBox.setEnabled(rtEndToEndRadio.isSelected());
            }
        });

        rtEndToEndRadio.setSelected(true);

        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rtMode = 0, rtResolution = 0;
                if (rtCheckBox.isSelected()) {
                    if (rtEndToEndRadio.isSelected()) {
                        rtMode = Globals.END_TO_END_RT;
                    } else if (rtAdapterRadio.isSelected()) {
                        rtMode = Globals.ADAPTER_RT;
                    }
                    if (rtMillisRdBtn.isSelected()) {
                        rtResolution = Globals.MILLIS_RT;
                    } else if (rtNanosRdBtn.isSelected()) {
                        rtResolution = Globals.NANO_RT;
                    }
                } else {
                    rtMode = Globals.NO_RT;
                }

                try {
                    ControllerFacade.getInstance().updateTestOptions(rtMode, rtResolution, rtCreationTimeCheckBox.isSelected());
                    if (!Controller_GUI.getInstance().configModified) {
                        Controller_GUI.getInstance().setTitle(Controller_GUI.getInstance().getTitle() + "*");
                    }
                    Controller_GUI.getInstance().configModified = true;
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(null, e1.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                dispose();
            }
        });

        cancelBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

    }

    public void fillProperties(int rtMode, int rtResolution, boolean useCreationTime) {
        switch (rtMode) {
            case Globals.END_TO_END_RT:
                rtEndToEndRadio.setSelected(true);
                break;
            case Globals.ADAPTER_RT:
                rtAdapterRadio.setSelected(true);
                break;
            case Globals.NO_RT:
                rtCheckBox.setSelected(false);
                break;
        }

        switch (rtResolution) {
            case Globals.MILLIS_RT:
                rtMillisRdBtn.setSelected(true);
                break;
            case Globals.NANO_RT:
                rtNanosRdBtn.setSelected(true);
                break;
        }

        rtCreationTimeCheckBox.setSelected(useCreationTime);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelBtn;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton okBtn;
    private javax.swing.JRadioButton rtAdapterRadio;
    private javax.swing.JCheckBox rtCheckBox;
    private javax.swing.JCheckBox rtCreationTimeCheckBox;
    private javax.swing.JRadioButton rtEndToEndRadio;
    private javax.swing.JRadioButton rtMillisRdBtn;
    private javax.swing.ButtonGroup rtModeBtnGroup;
    private javax.swing.JLabel rtModeLbl;
    private javax.swing.JRadioButton rtNanosRdBtn;
    private javax.swing.JPanel rtPanel;
    private javax.swing.ButtonGroup rtResolutionBtnGroup;
    private javax.swing.JLabel rtResolutionLbl;
    // End of variables declaration//GEN-END:variables

}
