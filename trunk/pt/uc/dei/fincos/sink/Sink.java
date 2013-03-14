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


package pt.uc.dei.fincos.sink;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import javax.jms.JMSException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;

import pt.uc.dei.fincos.adapters.AdapterType;
import pt.uc.dei.fincos.adapters.cep.CEP_EngineFactory;
import pt.uc.dei.fincos.adapters.cep.CEP_EngineInterface;
import pt.uc.dei.fincos.adapters.jms.JMS_Reader;
import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.basic.InvalidStateException;
import pt.uc.dei.fincos.basic.Status;
import pt.uc.dei.fincos.basic.Step;
import pt.uc.dei.fincos.controller.ConnectionConfig;
import pt.uc.dei.fincos.controller.Logger;
import pt.uc.dei.fincos.controller.SinkConfig;
import pt.uc.dei.fincos.driver.Driver;
import pt.uc.dei.fincos.perfmon.SinkPerfStats;

/**
 * Sink application. Receives and processes results from the system under test.
 *
 * @author  Marcelo R.N. Mendes
 *
 * @see Driver
 */
public final class Sink extends JFrame implements SinkRemoteFunctions {
    /** Serial id. */
    private static final long serialVersionUID = -8020918447595615618L;

    /** The unique identifier of this Sink. */
    private String alias;

    /** Used to receive events directly from CEP engine. */
    private CEP_EngineInterface cepEngineInterface;

    /** Used to receive events from a JMS provider. */
    private JMS_Reader jmsInterface;

    /** How this Sink receives events from CEP engines
     * (directly through their API or through JMS messages). */
    private AdapterType adapterType;

    /** Logs received events to disk. */
    private Logger logger;

    /** Which fields will be logged to disk
     * (all or timestamps only). */
    private int fieldsToLog;

    /** Logging sampling. */
    private int logSamplMod;

    /** Response time measurement mode
     * (either END-TO-END or ADAPTER). */
    protected int rtMode;

    /** Response time measurement resolution
     * (either Milliseconds or Nanoseconds). */
    protected int rtResolution;

    /** Current state of this Sink. */
    private Status status;

    /** Total number of events received so far. */
    private long receivedEvents = 0;

    /** Indicates if online performance monitoring is enabled. */
    private boolean perfTracingEnabled = false;

    /** Collected performance metrics. */
    private SinkPerfStats perfStats;

    //========================= GUI ===================================
    JLabel statusLabel, eventCountLabel;
    JPanel statusPanel;
    JTextArea infoArea;
    //=================================================================

    /**
     * Creates an anonymous Sink.
     */
    public Sink() {
        this("Sink");
    }

    /**
     * Creates a named Sink.
     *
     * @param alias     the identifier of the Sink
     */
    public Sink(String alias) {
        super(alias);

        this.setIconImage(Toolkit.getDefaultToolkit().getImage("imgs/sink.png"));

        this.alias = alias;
        this.status = new Status();
        this.perfStats = new SinkPerfStats();

        statusPanel =  new JPanel();
        statusPanel.setLayout(null);
        statusPanel.setPreferredSize(new Dimension(200, 400));
        statusPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Status"));
        statusLabel = new JLabel();
        eventCountLabel = new JLabel("Rcvd events: 0");

        statusPanel.add(statusLabel);
        statusPanel.add(eventCountLabel);

        statusLabel.setBounds(10, 10, 150, 50);
        eventCountLabel.setBounds(20, 45, 175, 50);

        infoArea = new JTextArea(10, 30);
        infoArea.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Info"));
        JScrollPane infoScroll = new JScrollPane(infoArea);
        infoArea.setEditable(false);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(statusPanel, BorderLayout.LINE_START);
        this.getContentPane().add(infoScroll, BorderLayout.CENTER);

        this.setSize(700, 400);
        this.setLocationRelativeTo(null); //screen center
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.setVisible(true);

        refreshGUI();

        int delay = 1000 / Globals.DEFAULT_GUI_REFRESH_RATE;
        Timer guiRefresher = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshGUI();
            }
        });
        guiRefresher.start();

        try {
            String logMsg = "Sink application started. Initializing remote interface...";
            System.out.println(logMsg);
            showInfo(logMsg);
            this.initializeRMI();
            System.out.println("Done! Waiting for remote commands...");
            showInfo("Done! Waiting for remote commands...");
        } catch (Exception e) {
            this.status.setStep(Step.ERROR);
            String logMsg = "ERROR: Could not initialize remote interface: "
                          + e.getMessage();
            System.err.println(logMsg);
            showInfo(logMsg);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            guiRefresher.stop();
            guiRefresher = null;
            this.dispose();
        }
    }

    /**
     * Locates the RMI registry and binds this Sink.
     *
     * @throws RemoteException  if an error occurs when contacting the registry.
     */
    private void initializeRMI() throws RemoteException {
        SinkRemoteFunctions stub = (SinkRemoteFunctions) UnicastRemoteObject.exportObject(this, 0);
        Registry registry = LocateRegistry.getRegistry(Globals.RMI_PORT);
        registry.rebind(alias, stub);
    }

    /**
     * Updates the UI of this Sink.
     */
    private void refreshGUI() {
        switch (this.status.getStep()) {
        case DISCONNECTED:
            this.statusLabel.setIcon(Globals.YELLOW_SIGN);
            break;
        case CONNECTED:
        case READY:
        case PAUSED:
        case STOPPED:
        case FINISHED:
            this.statusLabel.setIcon(Globals.BLUE_SIGN);
            break;
        case LOADING:
        case RUNNING:
            this.statusLabel.setIcon(Globals.GREEN_SIGN);
            break;
        case ERROR:
            this.statusLabel.setIcon(Globals.RED_SIGN);
            break;
        }

        eventCountLabel.setText("Rcvd events: "
                + Globals.LONG_FORMAT.format(receivedEvents));

        if (this.status.getStep() == Step.READY && receivedEvents > 0) {
            this.status.setStep(Step.RUNNING);
        }

        this.statusLabel.setText(this.status.getStep().toString());

        statusPanel.revalidate();
    }


    @Override
    public boolean load(SinkConfig sinkCfg, int rtMode, int rtResolution)
    throws Exception {
        if (this.status.getStep() == Step.DISCONNECTED
            || this.status.getStep() == Step.ERROR
            || this.status.getStep() == Step.STOPPED) {
            Properties connProps = new Properties();
            for (Entry<String, String> e: sinkCfg.getConnection().getProperties().entrySet()) {
                connProps.put(e.getKey(), e.getValue());
            }
            if (sinkCfg.getConnection().getType() == ConnectionConfig.JMS) {
                this.adapterType = AdapterType.JMS;
                String cfName = (String) connProps.get("cfName");
                showInfo("Connecting to JMS Provider...");
                HashMap<String, String[]> lsnrs = new HashMap<String, String[]>();
                lsnrs.put("Lsnr-1", sinkCfg.getOutputStreamList());
                try {
                    this.jmsInterface = new JMS_Reader(connProps, cfName, lsnrs,
                                                       rtMode, rtResolution, this);
                    showInfo("Done!");
                } catch (Exception e) {
                    this.showInfo("ERROR: Could not connect to JMS provider. ("
                                + e.getMessage() + ").");
                    this.status.setStep(Step.ERROR);
                    return false;
                } catch (Error err) {
                    this.showInfo("ERROR: Could not connect to JMS provider. ("
                                + err.getMessage() + ").");
                    this.status.setStep(Step.ERROR);
                    return false;
                }
            } else if (sinkCfg.getConnection().getType() == ConnectionConfig.CEP_ADAPTER) {
                this.adapterType = AdapterType.CEP;
                cepEngineInterface = CEP_EngineFactory.getCEPEngineInterface(connProps,
                                        rtMode, rtResolution);
                if (cepEngineInterface == null) {
                    throw new Exception("Unsupported CEP engine");
                }
                showInfo("Connecting to CEP engine...");
                try {
                    cepEngineInterface.connect();
                    cepEngineInterface.load(sinkCfg.getOutputStreamList(), this);
                    showInfo("Done!");
                } catch (Exception e) {
                    e.printStackTrace();
                    this.showInfo("ERROR: Could not connect to CEP engine. ("
                                + e.getMessage() + ").");
                    this.status.setStep(Step.ERROR);
                    return false;
                } catch (Error err) {
                    this.showInfo("ERROR: Could not connect to CEP engine. ("
                                + err.getMessage() + ").");
                    this.status.setStep(Step.ERROR);
                    return false;
                }
            }

            this.rtMode = rtMode;
            this.rtResolution = rtResolution;

            // Logging
            if (sinkCfg.isLoggingEnabled()) {
                String rtModeStr;
                switch (rtMode) {
                case Globals.END_TO_END_RT:
                    rtModeStr = "END_TO_END";
                    break;
                case Globals.ADAPTER_RT:
                    rtModeStr = "ADAPTER";
                    break;
                default:
                    rtModeStr = "NO_RT";
                }
                String rtResolutionStr = "";
                switch (rtResolution) {
                case Globals.MILLIS_RT:
                    rtResolutionStr = "milliseconds";
                    break;
                case Globals.NANO_RT:
                    rtResolutionStr = "nanoseconds";
                    break;
                }

                try {
                    String logHeader = "FINCoS Sink Log File."
                        + "\n Sink Alias: " + sinkCfg.getAlias()
                        + "\n Sink Address: " + sinkCfg.getAddress().getHostAddress()
                        + "\n Connection: " + sinkCfg.getConnection().getAlias()
                        + "\n Start time: " + new Date()
                        + "\n Response Time Mode: " + rtModeStr
                        + "\n Response Time Resolution: " + rtResolutionStr
                        + "\n Log Sampling rate: " + sinkCfg.getLoggingSamplingRate();
                    String logFile = Globals.APP_PATH + "log" + File.separator
                                    + sinkCfg.getAlias() + ".log";
                    logger = new Logger(logFile, logHeader,
                                        sinkCfg.getLogFlushInterval(),
                                        sinkCfg.getLoggingSamplingRate(),
                                        sinkCfg.getFieldsToLog());
                    logSamplMod = (int) (1 / sinkCfg.getLoggingSamplingRate());
                    fieldsToLog = sinkCfg.getFieldsToLog();
                } catch (IOException ioe2) {
                    System.err.println("Could not open logger (" + ioe2.getMessage() + ").");
                    showInfo("Could not open logger (" + ioe2.getMessage() + ").");
                    this.status.setStep(Step.ERROR);
                    return false;
                }
            }
            this.status.setStep(Step.READY);
            showInfo(alias + " has been successfully loaded.");
            return true;
        } else {
            throw new InvalidStateException("Could not load sink. Sink has already been loaded.");
        }
    }

    @Override
    public void unload() {
        perfStats = new SinkPerfStats();

        if (cepEngineInterface != null) {
            cepEngineInterface.disconnect();
            cepEngineInterface = null;
        }
        if (jmsInterface != null) {
            try {
                jmsInterface.disconnect();
            } catch (JMSException e) {
                showInfo("WARN: Error while disconnecting from JMS provider.");
            }
            jmsInterface = null;
        }
        if (this.logger != null) {
            this.logger.close();
            this.logger = null;
        }

        this.status.setStep(Step.STOPPED);
        showInfo("Sink has been stopped.");
        receivedEvents = 0;
    }


    /**
     * Displays a message on Sink's UI.
     *
     * @param msg   the message to be displayed
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

    /**
     * Converts an event represented as array of objects
     * into a textual CSV representation.
     *
     * @param event     the event, as an array of Objects
     * @return          the event represented as a CSV record
     */
    private String toCSV(Object[] event) {
        StringBuilder sb = new StringBuilder();
        sb.append(event[0]);

        for (int i = 1; i < event.length; i++) {
            sb.append(Globals.CSV_DELIMITER);
            sb.append(event[i]);
        }

        return sb.toString();
    }

    /**
     * Processes an event coming from the CEP engine.
     *
     * @param event         the event, represented as an array of Objects
     */
    public void processOutputEvent(Object[] event) {
        if (rtMode == Globals.END_TO_END_RT) {
            if (rtResolution == Globals.MILLIS_RT) {
                event[event.length - 1] = System.currentTimeMillis();
            } else if (rtResolution == Globals.NANO_RT) {
                event[event.length - 1] = System.nanoTime();
            }
        }
        long receivedCount = 0;
        synchronized (this) {
            receivedEvents++;
            receivedCount = receivedEvents;
        }

        // Updates perf stats, if realtime performance monitoring is enabled
        if (perfTracingEnabled) {
            Long inputTS = 0L;
            Long outputTS = 0L;
            if (rtMode != Globals.NO_RT) {
                inputTS = (Long) event[event.length - 2];
                outputTS = (Long) event[event.length - 1];
            }
            this.perfStats.offer((String) event[0], inputTS, outputTS, rtResolution);
        }

        try {
            // Log event, if configured to
            if (logger != null && receivedCount % logSamplMod == 0) {
                long logEntryTS = System.currentTimeMillis();
                if (fieldsToLog == Globals.LOG_ALL_FIELDS) {
                    logger.writeRecord(toCSV(event), logEntryTS);
                } else if (fieldsToLog == Globals.LOG_ONLY_TIMESTAMPS) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(event[0]);
                    if (rtMode != Globals.NO_RT) {
                        sb.append(Globals.CSV_DELIMITER);
                        sb.append(event[event.length - 2]); // input event timestamp
                        sb.append(Globals.CSV_DELIMITER);
                        sb.append(event[event.length - 1]); // answer event timestamp
                    }
                    logger.writeRecord(sb.toString(), logEntryTS);
                }
            }
        } catch (IOException ioe) {
            System.err.println("Error while processing event \""
                             + Arrays.toString(event) + "\" (" + ioe.getMessage() + ").");
        }
    }

    /**
     *
     * @return  the type of the adapter used by this sink
     *          (either CEP or JMS)
     */
    public AdapterType getAdapterType() {
        return adapterType;
    }

    @Override
    public Status getStatus() throws RemoteException {
        return this.status;
    }

    @Override
    public void setPerfTracing(boolean enabled) throws RemoteException {
        this.perfTracingEnabled = enabled;
    }

    @Override
    public SinkPerfStats getPerfStats() throws RemoteException {
        SinkPerfStats ret = this.perfStats.clone();
        this.perfStats.reset();
        return ret;
    }
}
