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
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.basic.InvalidStateException;
import pt.uc.dei.fincos.basic.Step;
import pt.uc.dei.fincos.controller.ConnectionConfig;
import pt.uc.dei.fincos.controller.ConnectionsFileParser;
import pt.uc.dei.fincos.controller.ControllerFacade;
import pt.uc.dei.fincos.controller.DriverConfig;
import pt.uc.dei.fincos.controller.SinkConfig;
import pt.uc.dei.fincos.perfmon.gui.PerformanceMonitor;
import pt.uc.dei.fincos.sink.SinkRemoteFunctions;

/**
 * Controller Application (GUI version).
 *
 * @author  Marcelo R.N. Mendes
 *
 */
public final class Controller_GUI extends JFrame {

    /** Serial ID. */
    private static final long serialVersionUID = -1013940232010985491L;

    /** Facade class implementing the functions of the Controller application. */
    ControllerFacade facade;

    /** Flag indicating if the setup file has been modified since last save. */
    public boolean configModified;

    /** Multiplier factor for input rate. */
    private double eventRateFactor = 1.0;

    /** Instance of FINCoS Perfmon, used for realtime performance monitoring. */
    private PerformanceMonitor perfmon;

    /** Path for the file containing connections. */
    public static final String CONNECTIONS_FILE = Globals.APP_PATH + "config" + File.separator + "Connections.fcf";


    //	=========================== GUI ===================================
    private Timer guiRefresher;

    // Menu
    private JMenuBar menuBar = new JMenuBar();
    private JMenu fileMenu, driverMenu, sinkMenu, testMenu, alterLoadFactorMenuItem, viewMenu;
    private JMenuItem profileLoadMenuItem, saveMenuItem, saveAsMenuItem, exitMenuItem,
            newDriverMenuItem, editDriverMenuItem, deleteDriverMenuItem,
            newSinkMenuItem, editSinkMenuItem, deleteSinkMenuItem,
            loadMenuItem, startMenuItem, pauseMenuItem, stopMenuItem, switchMenuItem, optionsMenuItem,
            connectionsMenuItem, perfmonMenuItem;
    private ButtonGroup rateFactorGroup;

    // ToolBar
    private JToolBar toolBar = new JToolBar();
    private JButton openBtn = new JButton(new ImageIcon("imgs/open.png"));
    private JButton saveBtn = new JButton(new ImageIcon("imgs/save.png"));
    private JButton saveAsBtn = new JButton(new ImageIcon("imgs/save_as.png"));
    private JButton loadBtn = new JButton(new ImageIcon("imgs/load.png"));
    private JButton startBtn = new JButton(new ImageIcon("imgs/start.png"));
    private JButton pauseBtn = new JButton(new ImageIcon("imgs/pause.png"));
    private JButton stopBtn = new JButton(new ImageIcon("imgs/stop.png"));
    private JButton switchPhaseBtn = new JButton(new ImageIcon("imgs/switchPhase.png"));
    private JButton perfmonBtn = new JButton(new ImageIcon("imgs/perfmon.png"));

    // Components
    JPanel driversPanel, sinksPanel, componentsPanel;
    JTable driversTable, sinksTable;
    JPopupMenu driversPop = new JPopupMenu();
    JMenuItem driverCopyMenu = new JMenuItem("Copy...");
    JPopupMenu sinksPop = new JPopupMenu();
    JMenuItem sinkCopyMenu = new JMenuItem("Copy...");

    // Connections
    private ArrayList<ConnectionConfig> connections;

    //Misc
    private JPanel commandPanel;
    private JFileChooser fileChooser;

    //Info
    private JTextArea infoArea;
    //===================================================================

    /** Singleton instance of the Controller. */
    static Controller_GUI instance;

    /**
     *
     * @return     an unique instance of the Controller app
     */
    public static Controller_GUI getInstance() {
        if (instance == null) {
            instance = new Controller_GUI();
        }
        return instance;
    }

    private Controller_GUI() {
        super("FINCoS Controller");
        this.setIconImage(Toolkit.getDefaultToolkit().getImage("imgs/ctrl.png"));

        this.facade = ControllerFacade.getInstance();

        commandPanel = new JPanel();
        commandPanel.setLayout(new BorderLayout());
        initializeMenuBar();
        initializeToolBar();
        commandPanel.add(menuBar, BorderLayout.NORTH);
        commandPanel.add(toolBar, BorderLayout.CENTER);

        infoArea = new JTextArea(10, 30);
        infoArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Info"));
        JScrollPane infoScroll = new JScrollPane(infoArea);
        infoArea.setEditable(false);

        initializeComponentsPanel();

        fileChooser = new JFileChooser(Globals.APP_PATH + "config");
        fileChooser.setFileFilter(new FileNameExtensionFilter("XML Configuration file", "xml"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(commandPanel, BorderLayout.NORTH);
        this.getContentPane().add(componentsPanel, BorderLayout.CENTER);
        this.getContentPane().add(infoScroll, BorderLayout.SOUTH);

        this.setSize(800, 600);
        this.setLocationRelativeTo(null); //screen center
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setVisible(true);

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if (configModified) {
                    int userChoice = JOptionPane.showConfirmDialog(null, "Test setup has been modified. Save changes?", "FINCoS Controller", JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (userChoice) {
                        case JOptionPane.YES_OPTION:
                            boolean saved = saveProfile();
                            if (saved) {
                                System.exit(0);
                            }
                            break;
                        case JOptionPane.NO_OPTION:
                            System.exit(0);
                            break;
                        case JOptionPane.CANCEL_OPTION:
                            break;
                    }
                } else {
                    System.exit(0);
                }
            }
        });

        try {
            openConnectionsFile();
        } catch (Exception e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(null, "Could not open connections file. Application will abort execution.", "ERROR", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void openConnectionsFile() throws Exception {
        File f = new File(CONNECTIONS_FILE);
        if (!f.exists()) {
            ConnectionsFileParser.createEmptyFile(CONNECTIONS_FILE);
        }
        this.connections = new ArrayList<ConnectionConfig>();
        this.connections.addAll(Arrays.asList(ConnectionsFileParser.getConnections(CONNECTIONS_FILE)));
    }

    /**
     * Updates the list of available connections.
     *
     * @param connections   list of connections
     */
    public void setConnections(ArrayList<ConnectionConfig> connections) {
        this.connections = connections;
        // Reopens the profile to reflect changes in connections
        if (facade.isTestSetupLoaded()) {
            this.loadProfile(new File(facade.getCurrentSetup()));
        }
    }

    /**
     *
     * @return  an array containing all configured connections
     */
    public ConnectionConfig[] getConnections() {
        return this.connections.toArray(new ConnectionConfig[0]);
    }

    /**
     * Gets the configuration of a given connection.
     *
     * @param alias    the connection to be retrieved
     * @return         the connection configuration, or <tt>null</tt> if there is no such connection
     */
    public ConnectionConfig getConnection(String alias) {
        for (ConnectionConfig conn : this.connections) {
            if (conn.getAlias().equals(alias)) {
                return conn;
            }
        }
        return null;
    }

    /**
     * Gets the configuration of a given connection.
     *
     * @param index    the index of the connection to be retrieved
     * @return         the connection configuration, or <tt>null</tt> if there is no such connection
     *
     * @throws IndexOutOfBoundsException    if the index is out of range (index < 0 || index >= size())
     */
    public ConnectionConfig getConnection(int index) throws IndexOutOfBoundsException {
        return this.connections.get(index);
    }

    /**
     * Gets the configuration of a given connection.
     *
     * @param alias    the connection to be retrieved
     * @return         the connection index, or <tt>-1</tt> if there is no such connection
     */
    public int getConnectionIndex(String alias) {
        for (int i = 0; i < this.connections.size(); i++) {
            if (connections.get(i).getAlias().equals(alias)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Adds a connection to the list of configured connections.
     * @param conn  the new connection
     * @throws Exception    if the new connection cannot be saved on the file.
     */
    public void addConnection(ConnectionConfig conn) throws Exception {
        this.connections.add(conn);
        this.saveConnections();
    }

    /**
     * Saves the list of connections to disk.
     *
     * @throws Exception   if an error occurs when writing data to the file
     */
    public void saveConnections() throws Exception {
        ConnectionsFileParser.saveToFile(this.connections.toArray(new ConnectionConfig[connections.size()]),
                CONNECTIONS_FILE);
    }

    private void initializeMenuBar() {
        // Menu File
        fileMenu = new JMenu("File");
        profileLoadMenuItem = new JMenuItem("Open");
        profileLoadMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (configModified) {
                    int userChoice = JOptionPane.showConfirmDialog(null,
                            "Test setup has been modified. Save changes?",
                            "FINCoS Controller",
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (userChoice) {
                        case JOptionPane.YES_OPTION:
                            boolean saved = saveProfile();
                            if (saved) {
                                openProfileAction();
                            }
                            break;
                        case JOptionPane.NO_OPTION:
                            openProfileAction();
                            break;
                        case JOptionPane.CANCEL_OPTION:
                            break;
                        default:
                            break;
                    }
                } else {
                    openProfileAction();
                }
            }
        });
        saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                saveProfile();
            }
        });
        saveAsMenuItem = new JMenuItem("Save As...");
        saveAsMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                saveProfileAs();
            }
        });
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        fileMenu.add(profileLoadMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(exitMenuItem);

        // Menu Driver
        driverMenu = new JMenu("Drivers");
        newDriverMenuItem = new JMenuItem("New...");
        newDriverMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new DriverDetail(null).setVisible(true);
            }
        });
        editDriverMenuItem = new JMenuItem("Edit...");
        editDriverMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int selected = driversTable.getSelectedRow();
                if (selected > -1) {
                    new DriverDetail(facade.getDriverList().get(selected)).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Select a driver to edit");
                }
            }
        });
        deleteDriverMenuItem = new JMenuItem("Delete");
        deleteDriverMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(null, "Delete Driver(s)?", "Confirm Delete", JOptionPane.YES_NO_OPTION)
                        == JOptionPane.YES_OPTION) {
                    deleteDrivers();
                }
            }
        });
        driverMenu.add(newDriverMenuItem);
        driverMenu.add(editDriverMenuItem);
        driverMenu.add(deleteDriverMenuItem);

        // Menu Sink
        sinkMenu = new JMenu("Sinks");
        newSinkMenuItem = new JMenuItem("New...");
        newSinkMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new SinkDetail(null).setVisible(true);
            }
        });
        editSinkMenuItem = new JMenuItem("Edit...");
        editSinkMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int selected = sinksTable.getSelectedRow();
                if (selected > -1) {
                    new SinkDetail(facade.getSinkList().get(selected)).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Select a sink to edit");
                }
            }
        });
        deleteSinkMenuItem = new JMenuItem("Delete");
        deleteSinkMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(null, "Delete Sink(s)?", "Confirm Delete", JOptionPane.YES_NO_OPTION)
                        == JOptionPane.YES_OPTION) {
                    deleteSinks();
                }
            }
        });

        sinkMenu.add(newSinkMenuItem);
        sinkMenu.add(editSinkMenuItem);
        sinkMenu.add(deleteSinkMenuItem);


        // Menu Test
        testMenu = new JMenu("Test");
        loadMenuItem = new JMenuItem("Load");
        loadMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                loadAllComponents();
            }
        });
        startMenuItem = new JMenuItem("Start");
        startMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                startLoadSubmission();
            }
        });
        pauseMenuItem = new JMenuItem("Pause");
        pauseMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                pauseLoadSubmission();
            }
        });
        stopMenuItem = new JMenuItem("Stop");
        stopMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stopLoadSubmission();
            }
        });
        switchMenuItem = new JMenuItem("Switch to next phase");
        switchMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                switchToNextPhase();
            }
        });
        alterLoadFactorMenuItem = new JMenu("Alter Event Rate");
        alterLoadFactorMenuItem.setEnabled(false);
        rateFactorGroup = new ButtonGroup();
        JCheckBoxMenuItem rateFactorItem;
        for (int i = 15; i >= 5; i--) {
            rateFactorItem = new JCheckBoxMenuItem(i / 10.0 + "x");
            rateFactorItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String text = ((JCheckBoxMenuItem) e.getSource()).getText();
                    eventRateFactor = Double.parseDouble(text.substring(0, text.length() - 1));
                    alterRate();
                }
            });
            if (i == 10) {
                alterLoadFactorMenuItem.addSeparator();
                alterLoadFactorMenuItem.add(rateFactorItem);
                alterLoadFactorMenuItem.addSeparator();
                rateFactorItem.setSelected(true);
            } else {
                alterLoadFactorMenuItem.add(rateFactorItem);
            }
            rateFactorGroup.add(rateFactorItem);

        }
        rateFactorItem = new JCheckBoxMenuItem("Custom...");
        rateFactorGroup.add(rateFactorItem);
        rateFactorItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog("Inform the factor to multiply event rate (e.g. 2.5)");
                try {
                    if (input != null) {
                        eventRateFactor = Double.parseDouble(input);
                        alterRate();
                        ((JCheckBoxMenuItem) e.getSource()).setText("Custom (" + eventRateFactor + "x)...");
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Invalid value");
                    actionPerformed(e);
                }

            }
        });
        alterLoadFactorMenuItem.add(rateFactorItem);

        optionsMenuItem = new JMenuItem("Options...");
        optionsMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                TestOptions tOptions = new TestOptions();
                tOptions.fillProperties(facade.getRtMode(),
                        facade.getRtResolution(),
                        facade.getUseEventsCreationTime());
            }
        });

        testMenu.add(loadMenuItem);
        testMenu.add(startMenuItem);
        testMenu.add(pauseMenuItem);
        testMenu.add(stopMenuItem);
        testMenu.add(switchMenuItem);
        testMenu.add(alterLoadFactorMenuItem);
        testMenu.addSeparator();
        testMenu.add(optionsMenuItem);

        // View Menu
        viewMenu = new JMenu("View");
        connectionsMenuItem = new JMenuItem("Connections");
        connectionsMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showConnectionsDialog();
            }
        });
        perfmonMenuItem = new JMenuItem("Performance Monitor");
        perfmonMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showPerfmon();
            }
        });
        perfmonMenuItem.setEnabled(false);
        viewMenu.add(connectionsMenuItem);
        viewMenu.add(perfmonMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(driverMenu);
        menuBar.add(sinkMenu);
        menuBar.add(testMenu);
        menuBar.add(viewMenu);

    }

    private void showConnectionsDialog() {
        ConnectionsDialog cd = new ConnectionsDialog(connections);
        DefaultTableModel m = (DefaultTableModel) driversTable.getModel();
        for (int i = 0; i < m.getRowCount(); i++) {
            if (m.getValueAt(i, 0).equals("RUNNING")
                    || m.getValueAt(i, 0).equals("READY")) {
                cd.disableGUI();
                break;
            }
        }
        m = (DefaultTableModel) sinksTable.getModel();
        for (int i = 0; i < m.getRowCount(); i++) {
            if (m.getValueAt(i, 0).equals("RUNNING")
                    || m.getValueAt(i, 0).equals("READY")) {
                cd.disableGUI();
                break;
            }
        }
        cd.setVisible(true);
    }

    private void initializeToolBar() {
        toolBar.add(openBtn);
        toolBar.add(saveBtn);
        toolBar.add(saveAsBtn);
        toolBar.add(loadBtn);
        toolBar.add(startBtn);
        toolBar.add(pauseBtn);
        toolBar.add(stopBtn);
        toolBar.add(switchPhaseBtn);
        toolBar.add(perfmonBtn);

        openBtn.setToolTipText("Open configuration file.");
        saveBtn.setToolTipText("Save configuration file.");
        saveAsBtn.setToolTipText("Save configuration as...");
        loadBtn.setToolTipText("Load (initialize all components)");
        startBtn.setToolTipText("Start/resume load submission.");
        pauseBtn.setToolTipText("Pause load submission.");
        stopBtn.setToolTipText("Stop load submission/data generation.");
        switchPhaseBtn.setToolTipText("Switch Drivers to next phase.");
        perfmonBtn.setToolTipText("View online performance stats.");
        perfmonBtn.setEnabled(false);

        openBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (configModified) {
                    int userChoice = JOptionPane.showConfirmDialog(null,
                            "Test setup has been modified. Save changes?",
                            "FINCoS Controller",
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (userChoice) {
                        case JOptionPane.YES_OPTION:
                            boolean saved = saveProfile();
                            if (saved) {
                                openProfileAction();
                            }
                            break;
                        case JOptionPane.NO_OPTION:
                            openProfileAction();
                            break;
                        case JOptionPane.CANCEL_OPTION:
                            break;
                        default:
                            break;
                    }
                } else {
                    openProfileAction();
                }
            }
        });

        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveProfile();
            }
        });

        saveAsBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                saveProfileAs();
            }
        });

        loadBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadAllComponents();
            }
        });

        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startLoadSubmission();
            }
        });

        pauseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseLoadSubmission();
            }
        });

        stopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopLoadSubmission();
            }
        });

        switchPhaseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchToNextPhase();
            }
        });

        perfmonBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPerfmon();
            }
        });
    }

    @SuppressWarnings("serial")
    private void initializeComponentsPanel() {
        componentsPanel = new JPanel(new GridLayout(1, 2));

        //Drivers Panel
        driversPanel = new JPanel(new BorderLayout());
        driversPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Drivers"));
        driversTable = new JTable();
        driversTable.setModel(
                new DefaultTableModel(new String[]{"Status", "Alias", "Address"},
                0) {

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                });

        driversTable.getTableHeader().setReorderingAllowed(false);
        driversTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        driversTable.getColumnModel().getColumn(0).setPreferredWidth(5);
        driversTable.getColumnModel().getColumn(1).setPreferredWidth(20);
        driversTable.getColumnModel().getColumn(2).setPreferredWidth(20);
        driversTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable source = (JTable) e.getSource();
                    if (source.isEnabled()) {
                        int selected = source.getSelectedRow();
                        if (selected > -1) {
                            DriverDetail dd = new DriverDetail(facade.getDriverList().get(selected));
                            if (!source.getModel().getValueAt(selected, 0).equals("DISCONNECTED")
                                    && !source.getModel().getValueAt(selected, 0).equals("STOPPED")
                                    && !source.getModel().getValueAt(selected, 0).equals("FINISHED")) {
                                dd.disableGUI();
                            }
                            dd.setVisible(true);
                        }
                    }
                }
            }
        });
        JScrollPane driverScroll = new JScrollPane();
        driverScroll.setViewportView(driversTable);
        driversPanel.add(driverScroll);
        JMenuItem loadDriverMenuItem = new JMenuItem("Load");
        JMenuItem startDriverMenuItem = new JMenuItem("Start");
        JMenuItem pauseDriverMenuItem = new JMenuItem("Pause");
        JMenuItem stopDriverMenuItem = new JMenuItem("Stop");
        driversPop.add(driverCopyMenu);
        driversPop.add(loadDriverMenuItem);
        driversPop.add(startDriverMenuItem);
        driversPop.add(pauseDriverMenuItem);
        driversPop.add(stopDriverMenuItem);
        driverCopyMenu.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int selected = driversTable.getSelectedRow();
                if (selected > -1) {
                    DriverConfig copy = facade.getDriverList().get(selected);
                    DriverDetail dscreen = new DriverDetail(null);
                    dscreen.fillProperties(copy);
                    dscreen.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Select a Driver to copy");
                }
            }
        });
        loadDriverMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (driversTable) {
                    int selected = driversTable.getSelectedRow();
                    if (selected > -1) {
                        DriverConfig dr = facade.getDriverList().get(selected);
                        showInfo("Loading " + dr.getAlias() + "...");
                        loadDriver(dr);
                    } else {
                        JOptionPane.showMessageDialog(null, "Select a Driver to load");
                    }
                }
            }
        });

        startDriverMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (driversTable) {
                    int selected = driversTable.getSelectedRow();

                    if (selected > -1) {
                        DriverConfig dr = facade.getDriverList().get(selected);
                        startDriver(dr);
                    } else {
                        JOptionPane.showMessageDialog(null, "Select a Driver to start");
                    }
                }
            }
        });

        pauseDriverMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (driversTable) {
                    int selected = driversTable.getSelectedRow();

                    if (selected > -1) {
                        DriverConfig dr = facade.getDriverList().get(selected);

                        if (facade.isDriverConnected(dr)) {
                            showInfo("Pausing " + dr.getAlias() + "...");
                            pauseDriver(dr);
                        } else {
                            showInfo("Could not pause " + dr.getAlias() + ". Driver is not connected.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Select a Driver to pause");
                    }
                }
            }
        });
        stopDriverMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (driversTable) {
                    int selected = driversTable.getSelectedRow();

                    if (selected > -1) {
                        DriverConfig dr = facade.getDriverList().get(selected);
                        if (facade.isDriverConnected(dr)) {
                            showInfo("Stopping " + dr.getAlias() + "...");
                            stopDriver(dr);
                        } else {
                            showInfo("Could not stop " + dr.getAlias() + ". Driver is not connected.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Select a Driver to stop");
                    }
                }
            }
        });

        driversTable.addMouseListener(new PopupListener(driversPop));

        //Sinks Panel
        sinksPanel = new JPanel(new BorderLayout());
        sinksPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Sinks"));
        sinksTable = new JTable();
        sinksTable.setModel(
                new DefaultTableModel(new String[]{"Status", "Alias", "Address"},
                0) {

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                });
        sinksTable.getTableHeader().setReorderingAllowed(false);
        sinksTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        sinksTable.getColumnModel().getColumn(0).setPreferredWidth(5);
        sinksTable.getColumnModel().getColumn(1).setPreferredWidth(20);
        sinksTable.getColumnModel().getColumn(2).setPreferredWidth(20);
        sinksTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable source = (JTable) e.getSource();
                    if (source.isEnabled()) {
                        int selected = source.getSelectedRow();
                        if (selected > -1) {
                            SinkDetail sd = new SinkDetail(facade.getSinkList().get(selected));
                            if (source.getModel().getValueAt(selected, 0).equals("RUNNING")
                                    || source.getModel().getValueAt(selected, 0).equals("READY")) {
                                sd.disableGUI();
                            }
                            sd.setVisible(true);
                        }
                    }
                }
            }
        });
        JScrollPane sinkScroll = new JScrollPane();
        sinkScroll.setViewportView(sinksTable);
        sinksPanel.add(sinkScroll);
        JMenuItem loadSinkMenuItem = new JMenuItem("Load");
        JMenuItem stopSinkMenuItem = new JMenuItem("Stop");
        sinksPop.add(sinkCopyMenu);
        sinksPop.add(loadSinkMenuItem);
        sinksPop.add(stopSinkMenuItem);
        sinkCopyMenu.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int selected = sinksTable.getSelectedRow();

                if (selected > -1) {
                    SinkConfig copy = facade.getSinkList().get(selected);
                    SinkDetail sinkScreen = new SinkDetail(null);
                    sinkScreen.fillProperties(copy);
                    sinkScreen.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Select a Sink to copy");
                }

            }
        });
        loadSinkMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (sinksTable) {
                    int selected = sinksTable.getSelectedRow();

                    if (selected > -1) {
                        SinkConfig sink = facade.getSinkList().get(selected);
                        showInfo("Loading " + sink.getAlias() + "...");
                        loadSink(sink);
                    } else {
                        JOptionPane.showMessageDialog(null, "Select a Sink to load");
                    }
                }
            }
        });
        stopSinkMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (sinksTable) {
                    int selected = sinksTable.getSelectedRow();

                    if (selected > -1) {
                        SinkConfig sink = facade.getSinkList().get(selected);

                        if (facade.isSinkConnected(sink)) {
                            showInfo("Stopping " + sink.getAlias() + "...");
                            stopSink(sink);
                        } else {
                            showInfo("Could not stop " + sink.getAlias() + ". Sink is not connected.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Select a Sink to stop");
                    }
                }
            }
        });

        sinksTable.addMouseListener(new PopupListener(sinksPop));

        componentsPanel.add(driversPanel);
        componentsPanel.add(sinksPanel);

    }

    private void exit() {
        System.exit(0);
    }

    /**
     * Displays a message on the Controller UI.
     *
     * @param msg   the message
     */
    private void showInfo(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Date now = new Date();
                infoArea.append(Globals.TIME_FORMAT.format(now) + " - " + msg + "\n");
                infoArea.setCaretPosition(infoArea.getDocument().getLength());
            }
        });

    }

    private void openProfileAction() {
        configModified = false;
        int action = fileChooser.showOpenDialog(null);

        if (action == JFileChooser.APPROVE_OPTION
            && fileChooser.getSelectedFile() != null) {
            loadProfile(fileChooser.getSelectedFile());
        }
    }

    private void loadProfile(File f) {
        try {
            if (guiRefresher != null) {
                guiRefresher.stop();
            }
            try {
                facade.openTestSetup(f);
            } catch (IllegalArgumentException ie) { // Inconsistent test options
                JOptionPane.showMessageDialog(null, ie.getMessage(),
                        "Warning", JOptionPane.WARNING_MESSAGE);
            }
            this.reloadDriversTable();
            this.reloadSinksTable();
            this.setTitle("FINCoS Controller (" + f.getPath() + ")");
            this.perfmonBtn.setEnabled(true);
            this.perfmonMenuItem.setEnabled(true);
        } catch (Exception e) { // Error parsing configuration file
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            this.reloadDriversTable();
            this.reloadSinksTable();
            this.setTitle("FINCoS Controller");
        }
    }

    private boolean saveProfile() {
        if (this.facade.getDriverList() == null
                || this.facade.getDriverList().isEmpty()
                || this.facade.getSinkList() == null
                || this.facade.getSinkList().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Cannot save configuration file. "
                    + "It is necessary to configure at least one Driver and one Sink.");
            return false;
        }

        if (facade.isTestSetupLoaded()) {
            try {
                facade.saveTestSetupFile();
                configModified = false;
                String title = this.getTitle();
                if (title.endsWith("*")) {
                    this.setTitle(title.substring(0, title.length() - 1));
                }
                return true;
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Configuration file was deleted.",
                        "Warning", JOptionPane.WARNING_MESSAGE);
                return this.saveProfileAs();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Could not save configuration file.\n(" + e.getClass() + " - "
                        + e.getMessage() + ")", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            return this.saveProfileAs();
        }
    }

    private boolean saveProfileAs() {
        if (this.facade.getDriverList() == null
                || this.facade.getDriverList().isEmpty()
                || this.facade.getSinkList() == null
                || this.facade.getSinkList().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Cannot save configuration file. "
                    + "It is necessary to configure at least one Driver and one Sink.");
            return false;
        }

        fileChooser.showSaveDialog(null);

        File f = fileChooser.getSelectedFile();
        if (f != null) {
            try {
                if (!f.getName().endsWith(".xml")) {
                    f = new File(f.getAbsolutePath() + ".xml");
                }

                facade.saveTestSetupFileAs(f);
                this.setTitle("FINCoS Controller (" + f.getPath() + ")");
                configModified = false;
                return true;
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(null, "File not found.",
                                              "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Could not save configuration file.\n("
                                              + e.getClass() + " - " + e.getMessage() + ")",
                                              "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            return false;
        }
    }

    private void reloadDriversTable() {
        synchronized (driversTable) {
            DefaultTableModel model = (DefaultTableModel) this.driversTable.getModel();
            int rowCount = model.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                model.removeRow(0);
            }

            if (this.facade.getDriverList() != null && !this.facade.getDriverList().isEmpty()) {
                for (DriverConfig dr : facade.getDriverList()) {
                    Object[] row = new Object[]{"DISCONNECTED",
                                                dr.getAlias(),
                                                dr.getAddress().getHostAddress()};
                    model.addRow(row);
                }
            }
        }
    }

    private void reloadSinksTable() {
        synchronized (sinksTable) {
            DefaultTableModel model = (DefaultTableModel) this.sinksTable.getModel();
            int rowCount = model.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                model.removeRow(0);
            }

            if (this.facade.getSinkList() != null && !this.facade.getSinkList().isEmpty()) {
                for (SinkConfig sink : this.facade.getSinkList()) {
                    Object[] row = new Object[]{"DISCONNECTED",
                                                sink.getAlias(),
                                                sink.getAddress().getHostAddress()};
                    model.addRow(row);
                }
            }
        }
    }

    /**
     * Enforce unique constraints for Drivers in the test setup
     * (a Driver must have a unique Alias).
     *
     *
     * @param oldCfg		The old configuration of the Driver (null, if it is a new one)
     * @param newCfg		The new configuration of the Driver being inserted or updated
     * @return				True if there is no uniqueness violation, false otherwise
     */
    public boolean checkDriverUniqueConstraint(DriverConfig oldCfg, DriverConfig newCfg) {
        if (facade.getDriverList().contains(newCfg)) {
            return facade.getDriverList().contains(oldCfg);
        } else {
            // if the new driver has the same alias as an existing one or
            // runs in the same machine than another Driver or Sink
            // Uniqueness Violation
            for (DriverConfig element : this.facade.getDriverList()) {
                if ((newCfg.getAlias().equalsIgnoreCase(element.getAlias())
                        && (oldCfg == null || !oldCfg.getAlias().equals(element.getAlias())))) {
                    return false;
                }
            }
            return true;
        }

    }

    /**
     * Adds the Driver configuration passed as argument to the test setup.
     *
     * @param dr		The configuration of the Driver to be added
     */
    public void addDriver(DriverConfig dr) {
        synchronized (driversTable) {
            facade.addDriver(dr);
            Object[] row = new Object[]{"DISCONNECTED",
                                        dr.getAlias(),
                                        dr.getAddress().getHostAddress()};
            ((DefaultTableModel) driversTable.getModel()).addRow(row);
        }
        if (!configModified) {
            this.setTitle(this.getTitle() + "*");
        }

        configModified = true;
    }

    /**
     * Updates the configuration of an existing Driver.
     *
     * @param oldCfg		The old configuration of the Driver
     * @param newCfg		The new configuration of the Driver
     */
    public void updateDriver(DriverConfig oldCfg, DriverConfig newCfg) {
        synchronized (driversTable) {
            int index = this.facade.getDriverList().indexOf(oldCfg);

            if (index > -1) {
                facade.updateDriver(index, newCfg);

                String status = (String) driversTable.getValueAt(index, 0);
                ((DefaultTableModel) driversTable.getModel()).removeRow(index);
                Object[] newRow = new Object[] {status,
                                                newCfg.getAlias(),
                                                newCfg.getAddress().getHostAddress()};
                ((DefaultTableModel) driversTable.getModel()).insertRow(index, newRow);

                if (!configModified) {
                    this.setTitle(this.getTitle() + "*");
                }

                configModified = true;
            }
        }
    }

    /**
     * Removes one or more Drivers from the test setup.
     *
     */
    public void deleteDrivers() {
        synchronized (driversTable) {
            int[] indexes = this.driversTable.getSelectedRows();

            if (indexes.length > 0) {
                ArrayList<DriverConfig> toRemove = new ArrayList<DriverConfig>(indexes.length);
                int i = 0;
                for (int index : indexes) {
                    toRemove.add(facade.getDriverList().get(index));
                    ((DefaultTableModel) driversTable.getModel()).removeRow(index - i);
                    i++;
                }

                for (DriverConfig dr : toRemove) {
                    facade.deleteDriver(dr);
                }

                if (!configModified) {
                    this.setTitle(this.getTitle() + "*");
                }
                configModified = true;
            } else {
                JOptionPane.showMessageDialog(null, "Select a driver to delete");
            }
        }
    }

    /**
     * Enforce unique constraints for Sinks in the test setup
     * Unique Constraints:
     *  1) A Sink must have a unique Alias
     *  2) If a Sink runs in the same machine (address) as another Component (Driver or Sink),
     *     it cannot be configured to send events to the same Validator at the same port than the latter.
     *
     *
     * @param oldCfg		The old configuration of the Sink (null, if it is a new one)
     * @param newCfg		The new configuration of the Sink being inserted or updated
     * @return				<tt>true</tt> if there is no uniqueness violation, <tt>false</tt> otherwise
     */
    public boolean checkSinkUniqueConstraint(SinkConfig oldCfg, SinkConfig newCfg) {
        if (facade.getSinkList().contains(newCfg)) {
            return facade.getSinkList().contains(oldCfg);
        } else {
            // if the new sink has the same alias as an existing one: Uniqueness Violation
            for (SinkConfig element : this.facade.getSinkList()) {
                if ((newCfg.getAlias().equalsIgnoreCase(element.getAlias())
                        && (oldCfg == null || !oldCfg.getAlias().equals(element.getAlias())))) {
                    System.out.println("2");
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Adds the Sink configuration passed as argument to the test setup.
     *
     * @param sink sink configuration
     */
    public void addSink(SinkConfig sink) {
        synchronized (sinksTable) {
            facade.addSink(sink);
            ((DefaultTableModel) sinksTable.getModel()).addRow(new Object[]{"DISCONNECTED", sink.getAlias(), sink.getAddress().getHostAddress()});
        }
        if (!configModified) {
            this.setTitle(this.getTitle() + "*");
        }

        configModified = true;
    }

    /**
     * Updates the configuration of an existing Sink.
     *
     * @param oldCfg		The old configuration of the Driver
     * @param newCfg		The new configuration of the Driver
     */
    public void updateSink(SinkConfig oldCfg, SinkConfig newCfg) {
        synchronized (sinksTable) {
            int index = facade.getSinkList().indexOf(oldCfg);
            if (index > -1) {
                facade.updateSink(index, newCfg);
                String status = (String) sinksTable.getValueAt(index, 0);
                ((DefaultTableModel) sinksTable.getModel()).removeRow(index);
                ((DefaultTableModel) sinksTable.getModel()).insertRow(index, new Object[]{status, newCfg.getAlias(), newCfg.getAddress().getHostAddress()});

                if (!configModified) {
                    this.setTitle(this.getTitle() + "*");
                }
                configModified = true;
            }
        }
    }

    /**
     * Removes one or more Sinks from the test setup.
     *
     */
    public void deleteSinks() {
        synchronized (sinksTable) {
            int[] indexes = this.sinksTable.getSelectedRows();

            if (indexes.length > 0) {
                ArrayList<SinkConfig> toRemove = new ArrayList<SinkConfig>(indexes.length);
                int i = 0;
                for (int index : indexes) {
                    toRemove.add(facade.getSinkList().get(index));
                    ((DefaultTableModel) sinksTable.getModel()).removeRow(index - i);
                    i++;
                }

                for (SinkConfig sink : toRemove) {
                    facade.deleteSink(sink);
                }

                if (!configModified) {
                    this.setTitle(this.getTitle() + "*");
                }
                configModified = true;
            } else {
                JOptionPane.showMessageDialog(null, "Select a Sink to delete.");
            }
        }
    }

    /**
     * Updates the GUI with information about the Status of the components (Drivers and Sinks).
     */
    private void refreshGUI() {
        Step s;

        synchronized (this.driversTable) {
            DefaultTableModel driverModel = (DefaultTableModel) this.driversTable.getModel();
            synchronized (facade) {
                DriverConfig dr;
                for (int i = 0; i < facade.getDriverList().size(); i++) {
                    dr = facade.getDriverList().get(i);
                    if (facade.isDriverConnected(dr)) {
                        try {
                            s = facade.getDriverStatus(dr).getStep();
                        } catch (RemoteException e1) {
                            s = Step.DISCONNECTED;
                            showInfo(dr.getAlias() + " has disconnected.");
                        }
                    } else {
                        s = Step.DISCONNECTED;
                    }

                    driverModel.setValueAt(s.toString(), i, 0);
                }
            }
        }

        synchronized (sinksTable) {
            DefaultTableModel sinkModel = (DefaultTableModel) this.sinksTable.getModel();
            synchronized (facade) {
                SinkConfig sink;

                for (int j = 0; j < facade.getSinkList().size(); j++) {
                    sink = facade.getSinkList().get(j);

                    if (facade.isSinkConnected(sink)) {
                        try {
                            s = facade.getSinkStatus(sink).getStep();
                        } catch (RemoteException e1) {
                            s = Step.DISCONNECTED;
                            showInfo(sink.getAlias() + " has disconnected.");
                        }
                    } else {
                        s = Step.DISCONNECTED;
                    }

                    sinkModel.setValueAt(s.toString(), j, 0);
                }
            }
        }


    }

    private void startGUIRefresh() {
        if (guiRefresher == null) {
            int delay = 1000 / Globals.DEFAULT_GUI_REFRESH_RATE;
            guiRefresher = new Timer(delay, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    //Refresh screen with information obtained from other components via RMI
                    refreshGUI();
                }
            });

        }
        guiRefresher.start();
    }

    private void stopGUIRefresh() {
        if (guiRefresher != null) {
            guiRefresher.stop();
            guiRefresher = null;
        }
    }

    // ============================ Control Functions ==============================
    private void showPerfmon() {
        if (this.perfmon == null) {
            this.perfmon = new PerformanceMonitor(facade.getDriverList().toArray(new DriverConfig[0]),
                    facade.getSinkList().toArray(new SinkConfig[0]),
                    facade.getRemoteDrivers(), facade.getRemoteSinks());
        } else {
            this.perfmon.requestFocus();
        }
    }

    public void closePerfmon() {
        this.perfmon = null;
    }

    /**
     * Initializes a given Driver.
     *
     * @param	dr		The Driver to be loaded
     */
    private void loadDriver(DriverConfig dr) {
        stopGUIRefresh();
        DriverLoader drLoader = new DriverLoader(dr);
        drLoader.execute();
        startGUIRefresh();
    }

    /**
     * Initializes a given Sink.
     *
     * @param sink		The Sink to be loaded
     */
    private void loadSink(SinkConfig sink) {
        SinkLoader sinkLoader = new SinkLoader(sink);
        sinkLoader.execute();
    }

    /**
     * Initializes all Drivers and all Sinks by means of RMI calls to them.
     */
    private void loadAllComponents() {
        if (facade.getDriverList() == null
                || facade.getDriverList().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Cannot load test. It is necessary to configure at least one Driver and one Sink.");
        } else {
            // Stops GUI refreshing thread
            stopGUIRefresh();

            showInfo("Loading components...");
            try {
                // Call the remote function "load" for each Driver
                for (DriverConfig dr : facade.getDriverList()) {
                    loadDriver(dr);
                    Thread.sleep(250);
                }

                // Call the remote function "load" for each Sink
                for (SinkConfig sink : facade.getSinkList()) {
                    loadSink(sink);
                    Thread.sleep(250);
                }
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }

            // Restarts GUI refreshing thread
            startGUIRefresh();
        }
    }

    /**
     * Starts event submission at a given Driver.
     *
     * @param dr	The Driver to be started
     */
    private void startDriver(DriverConfig dr) {
        if (facade.isDriverConnected(dr)) {
            showInfo("Starting " + dr.getAlias() + "...");
            RemoteDriverCaller starter = new RemoteDriverCaller(dr, "start");
            starter.execute();
        } else {
            showInfo("Could not start " + dr.getAlias() + ". Driver is not connected.");
        }
    }

    /**
     * Starts all Drivers.
     */
    private synchronized void startLoadSubmission() {
        if (facade.getDriverList() == null || facade.getDriverList().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Cannot start test. It is necessary to configure at least one Driver and one Sink.");
        } else {
            showInfo("Load submission started.");
            for (DriverConfig dr : facade.getDriverList()) {
                startDriver(dr);
            }
        }
    }

    /**
     * Pauses event submission at a given Driver.
     *
     * @param dr	The Driver to be paused
     *
     */
    private void pauseDriver(DriverConfig dr) {
        if (facade.isDriverConnected(dr)) {
            RemoteDriverCaller caller = new RemoteDriverCaller(dr, "pause");
            caller.execute();
        } else {
            showInfo("Could not pause " + dr.getAlias() + ". Driver is not connected.");
        }
    }

    /**
     * Pauses all Drivers.
     */
    private synchronized void pauseLoadSubmission() {
        showInfo("Pausing load submission...");
        for (DriverConfig dr : facade.getDriverList()) {
            pauseDriver(dr);
        }
    }

    /**
     * Stops event submission at a given Driver.
     *
     * @param dr	The Driver to be stopped
     */
    private void stopDriver(DriverConfig dr) {
        if (facade.isDriverConnected(dr)) {
            RemoteDriverCaller caller = new RemoteDriverCaller(dr, "stop");
            caller.execute();
        } else {
            showInfo("Could not stop " + dr.getAlias() + ". Driver is not connected.");
        }
    }

    /**
     * Unloads a given Sink.
     * @param sink		The sink to be stopped
     */
    private void stopSink(SinkConfig sink) {
        if (facade.isSinkConnected(sink)) {
            RemoteSinkCaller caller = new RemoteSinkCaller(sink, "stop");
            caller.execute();
        } else {
            showInfo("Could not stop " + sink.getAlias() + ". Sink is not connected.");
        }
    }

    /**
     * Stops all Drivers and Sinks.
     */
    private synchronized void stopLoadSubmission() {
        showInfo("Stopping Drivers...");
        for (DriverConfig dr : facade.getDriverList()) {
            stopDriver(dr);
        }
        showInfo("Stopping Sinks...");
        for (SinkConfig sink : facade.getSinkList()) {
            stopSink(sink);
        }
    }

    /**
     * Makes a Driver to jump to the next phase
     * (or finish execution if it has no more phases).
     *
     * @param dr	The Driver that must switch to next phase
     */
    private void switchDriverToNextPhase(DriverConfig dr) {
        if (facade.isDriverConnected(dr)) {
            RemoteDriverCaller caller = new RemoteDriverCaller(dr, "switch");
            caller.execute();
        } else {
            showInfo("Could not switch phase of " + dr.getAlias() + ". Driver is not connected.");
        }
    }

    /**
     *
     * Forces all Drivers to jump to the next phase
     * (or finish execution if it has no more phases).
     *
     */
    private synchronized void switchToNextPhase() {
        showInfo("Switching Drivers to next phase...");
        for (DriverConfig dr : facade.getDriverList()) {
            switchDriverToNextPhase(dr);
        }
    }

    /**
     * Alters a submission rate of a given Driver.
     *
     * @param dr	The Driver whose submission rate must be changed
     */
    private void alterDriverRate(DriverConfig dr) {
        if (facade.isDriverConnected(dr)) {
            RemoteDriverCaller caller = new RemoteDriverCaller(dr, "alter");
            caller.execute();
        } else {
            showInfo("Could not alter submission rate of " + dr.getAlias() + ". Driver is not connected.");
        }
    }

    /**
     * Alter event submission rate.
     *
     */
    private synchronized void alterRate() {
        showInfo("Altering rates on Drivers...");
        for (DriverConfig dr : facade.getDriverList()) {
            alterDriverRate(dr);
        }
    }

    /*
     *=========================== RMI Worker Threads ==============================
     * Used to call remote methods while keeping GUI responsive to the user
     *=============================================================================
     */
    class DriverLoader extends SwingWorker<Boolean, Void> {

        DriverConfig dr;

        public DriverLoader(DriverConfig driverConfig) {
            this.dr = driverConfig;
        }

        @Override
        protected Boolean doInBackground() {
            Boolean ret = null;
            try {
                ret = facade.loadRemoteDriver(dr);
            } catch (ConnectException ce) {
                showInfo("Could not connect to remote driver " + dr.getAlias() + ". (" + ce.getMessage() + ")");
            } catch (NotBoundException nbe) {
                showInfo("Could not connect to remote driver " + dr.getAlias() + ". (" + nbe.getClass() + "-" + nbe.getMessage() + ")");
            } catch (AccessException ae) {
                showInfo("Could not connect to remote driver " + dr.getAlias() + ". (" + ae.getClass() + "-" + ae.getMessage() + ")");
            } catch (RemoteException re) {
                showInfo("Could not connect to remote driver " + dr.getAlias() + ". (" + re.getClass() + "-" + re.getMessage() + ")");
            } catch (Exception e) {
                showInfo("Error while loading " + dr.getAlias() + "(" + e.getMessage() + ")");
            }

            return ret;

        }

        @Override
        protected void done() {
            try {
                if (get() != null) {
                    if (get()) {
                        showInfo("Driver " + dr.getAlias() + " has been successfully loaded.");
                    } else {
                        showInfo("Remote Driver " + dr.getAlias() + " reported that was not successfully loaded.");
                    }
                }
            } catch (InterruptedException e) {
                showInfo(dr.getAlias() + " could not be loaded. (" + e.getMessage() + ")");
            } catch (ExecutionException e) {
                showInfo(dr.getAlias() + " could not be loaded. (" + e.getMessage() + ")");
            }
        }
    }

    class SinkLoader extends SwingWorker<Boolean, Void> {

        SinkRemoteFunctions remoteSink;
        SinkConfig sink;

        public SinkLoader(SinkConfig sinkConfig) {
            this.sink = sinkConfig;
        }

        @Override
        protected Boolean doInBackground() throws RemoteException, InvalidStateException {
            Boolean ret = null;
            try {
                ret = facade.loadRemoteSink(sink);
            } catch (ConnectException ce) {
                showInfo("Could not connect to remote sink " + sink.getAlias()
                       + ". (" + ce.getMessage() + ")");
            } catch (NotBoundException nbe) {
                showInfo("Could not connect to remote sink " + sink.getAlias()
                       + ". (" + nbe.getClass() + "-" + nbe.getMessage() + ")");
            } catch (AccessException ae) {
                showInfo("Could not connect to remote sink " + sink.getAlias()
                       + ". (" + ae.getClass() + "-" + ae.getMessage() + ")");
            } catch (RemoteException re) {
                showInfo("Could not connect to remote sink " + sink.getAlias()
                       + ". (" + re.getClass() + "-" + re.getMessage() + ")");
            } catch (Exception e) {
                showInfo("Error while loading " + sink.getAlias() + "(" + e.getMessage() + ")");
            }

            return ret;

        }

        @Override
        protected void done() {
            try {
                if (get() != null) {
                    if (get()) {
                        showInfo("Sink " + sink.getAlias() + " has been successfully loaded.");
                    } else {
                        showInfo("Remote Sink " + sink.getAlias()
                                + " reported that was not successfully loaded.");
                    }
                }
            } catch (InterruptedException e) {
                showInfo(sink.getAlias() + " could not be loaded. (" + e.getMessage() + ")");
            } catch (ExecutionException e) {
                showInfo(sink.getAlias() + " could not be loaded. (" + e.getMessage() + ")");
            }
        }
    }

    class RemoteDriverCaller extends SwingWorker<Void, Void> {

        DriverConfig dr;
        String op;

        public RemoteDriverCaller(DriverConfig driverConfig, String operation) {
            this.dr = driverConfig;
            this.op = operation;
        }

        @Override
        protected Void doInBackground() {
            try {
                if (op.equals("start")) {
                    facade.startRemoteDriver(dr);
                    showInfo("  " + dr.getAlias() + " started.");
                    alterLoadFactorMenuItem.setEnabled(true);
                    alterLoadFactorMenuItem.getItem(6).setSelected(true);
                } else if (op.equals("pause")) {
                    facade.pauseRemoteDriver(dr);
                    showInfo("  " + dr.getAlias() + " paused.");
                } else if (op.equals("stop")) {
                    facade.stopRemoteDriver(dr);
                    showInfo("  " + dr.getAlias() + " stopped.");
                } else if (op.equals("switch")) {
                    facade.switchRemoteDriverToNextPhase(dr);
                    showInfo("  " + dr.getAlias() + " switched to next phase.");
                } else if (op.equals("alter")) {
                    facade.alterRemoteDriverSubmissionRate(dr, eventRateFactor);
                    showInfo("Event rate on  " + dr.getAlias()
                            + " altered (x " + eventRateFactor + ").");
                }
            } catch (InvalidStateException ise) {
                showInfo("Cannot " + op + " " + dr.getAlias() + "(" + ise.getMessage() + ")");
            } catch (Exception e) {
                e.printStackTrace();
                showInfo("Could not " + op + " " + dr.getAlias()
                        + "(" + e.getClass() + "-" + e.getMessage() + ")");
            }

            return null;
        }
    }

    class RemoteSinkCaller extends SwingWorker<Void, Void> {

        SinkConfig sink;
        String op;

        public RemoteSinkCaller(SinkConfig sinkConfig, String operation) {
            this.sink = sinkConfig;
            this.op = operation;
        }

        @Override
        protected Void doInBackground() {
            try {
                if (op.equals("stop")) {
                    facade.stopRemoteSink(sink);
                    showInfo("  " + sink.getAlias() + " stopped.");
                }
            } catch (Exception e) {
                showInfo("Could not " + op + " " + sink.getAlias()
                        + "(" + e.getClass() + "-" + e.getMessage() + ")");
            }

            return null;
        }
    }
    // ========================== End of RMI Worker Threads ==============================

    private static void setUI() {
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
        Font bold = new Font(Font.SANS_SERIF, Font.BOLD, 11);
        // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        UIManager.put("Label.font", font);
        UIManager.put("Label.foreground", Color.BLACK);
        UIManager.put("TextField.font", font);
        UIManager.put("TextField.foreground", Color.BLACK);
        UIManager.put("List.font", font);
        UIManager.put("List.foreground", Color.BLACK);
        UIManager.put("Table.font", font);
        UIManager.put("Table.foreground", Color.BLACK);
        UIManager.put("Button.font", bold);
        UIManager.put("Button.foreground", Color.BLACK);
        UIManager.put("RadioButton.font", font);
        UIManager.put("RadioButton.foreground", Color.BLACK);
        UIManager.put("CheckBox.font", font);
        UIManager.put("CheckBox.foreground", Color.BLACK);
        UIManager.put("ComboBox.font", font);
        UIManager.put("ComboBox.foreground", Color.BLACK);
        UIManager.put("TitledBorder.font", bold);
        UIManager.put("TitledBorder.foreground", Color.BLACK);
    }

    public static void main(String[] args)
    throws UnknownHostException, ClassNotFoundException, InstantiationException,
    IllegalAccessException, UnsupportedLookAndFeelException {
        System.setSecurityManager(new SecurityManager());
        setUI();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                Controller_GUI.getInstance();
            }
        });
    }
}
