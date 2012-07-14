package pt.uc.dei.fincos.driver;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;

import pt.uc.dei.fincos.adapters.AdapterType;
import pt.uc.dei.fincos.adapters.cep.CEPEngineFactory;
import pt.uc.dei.fincos.adapters.cep.CEPEngineInterface;
import pt.uc.dei.fincos.adapters.jms.JMS_Writer;
import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.basic.InvalidStateException;
import pt.uc.dei.fincos.basic.Status;
import pt.uc.dei.fincos.basic.Step;
import pt.uc.dei.fincos.controller.ConnectionConfig;
import pt.uc.dei.fincos.controller.DriverConfig;
import pt.uc.dei.fincos.controller.Logger;
import pt.uc.dei.fincos.data.DataFileReader;
import pt.uc.dei.fincos.driver.Scheduler.ArrivalProcess;
import pt.uc.dei.fincos.perfmon.DriverPerfStats;




/**
 *  Class responsible for load generation.
 *
 *  @author Marcelo R.N. Mendes
 */
public class Driver extends JFrame implements DriverRemoteFunctions {

    /** Serial id. */
    private static final long serialVersionUID = -7362660468052650571L;

    // ============================= SETUP ====================================
    /** Folder where generated data is saved on disk (if configured to do so). */
    private static final String DEFAULT_DATA_FILES_DIR = Globals.APP_PATH + "data";

    /** Number of files into which generated data is saved (if configured to do so).*/
    private static final int DATA_FILE_COUNT = 1;

    /** Generates input data (synthetic workloads). */
    private DataGen dg;

    /** Configuration parameters of this driver. */
    private DriverConfig drConfig;

    /** How this Driver sends events to CEP engines (directly through their API or through JMS messages). */
    private AdapterType adapterType;

    /** How latency is computed (Either END-TO-END or ADAPTER). */
    private int rtMode;

    /** Either Milliseconds or Nanoseconds. */
    protected int rtResolution;

    /** Fill events' timestamp with their scheduled time instead of their sending time. */
    private boolean useScheduledTime;

    /** An alias for this Driver. */
    private String alias;
    // ========================================================================


    // =============================== RUN ====================================
    /** Saves the events submitted by this driver into disk. */
    private Logger logger = null;

    /** Set of worker threads the generate load into the SUT. */
    private Sender[] senders;

    /** Number of Sender threads (applies only for synthetic workloads). */
    private int threadCount;
    // ========================================================================


    // =========================== COMMUNICATION ==============================
    /** Interface with JMS Provider. */
    private JMS_Writer jmsInterface;

    /** Direct interface with CEP engine. */
    private CEPEngineInterface cepEngineInterface;
    // ========================================================================


    // ======================= STATUS AND STATISTICS ==========================
    /** Current status of this thread. */
    private Status status;

    /** Total number of events to be submitted. */
    private long totalEventCount = 0;

    /** Number of events sent so far in all phases. */
    private long totalEventsSent = 0;

    /** Number of events sent so far in the current phase only. */
    private long phaseEventsSent = 0;

    /** Indicates if online performance monitoring is enabled. */
    private boolean perfTracingEnabled = false;
    // ========================================================================


    //============================== GUI =====================================
    JTextArea infoArea;
    JLabel statusLabel, progressLabel, eventCountLabel;
    JPanel statusPanel;
    JProgressBar bar;
    //=========================================================================

    /**
     * Creates a Driver with a default alias.
     */
    public Driver() {
        this("Driver");
    }

    /**
     * Creates a Driver, and assigns an alias to it.
     *
     * @param alias    the alias of this Driver
     */
    public Driver(String alias) {
        super(alias);

        this.setIconImage(Toolkit.getDefaultToolkit().getImage("imgs/driver.png"));

        this.alias = alias;
        statusPanel =  new JPanel();
        statusPanel.setLayout(null);
        statusPanel.setPreferredSize(new Dimension(200, 400));
        statusPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Status"));
        statusLabel = new JLabel();
        bar = new JProgressBar(0, 100);
        bar.setStringPainted(true);
        progressLabel = new JLabel("Progress: ");
        eventCountLabel = new JLabel("Sent events: " + Globals.LONG_FORMAT.format(totalEventsSent));

        statusPanel.add(statusLabel);
        statusPanel.add(progressLabel);
        statusPanel.add(bar);
        statusPanel.add(eventCountLabel);

        statusLabel.setBounds(10, 10, 150, 50);
        progressLabel.setBounds(20, 60, 60, 15);
        bar.setBounds(80, 60, 100, 15);
        eventCountLabel.setBounds(20, 75, 175, 50);

        infoArea = new JTextArea(10, 30);
        infoArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Info"));
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

        this.status = new Status();
        this.updateStatus(this.status.getStep(), this.status.getProgress());
        System.out.println("Driver application started. Initializing remote interface...");
        showInfo("Driver application started. Initializing remote interface...");

        try {
            this.initializeRMI();
            showInfo("Done! Waiting for remote commands...");
        } catch (Exception e) {
            this.updateStatus(Step.ERROR, 0);
            System.err.println("ERROR: Could not initialize remote interface: " + e.getMessage());
            showInfo("ERROR: Could not initialize remote interface: " + e.getMessage());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            System.exit(0);
        }

        int delay = 1000 / Globals.DEFAULT_GUI_REFRESH_RATE;
        Timer guiRefresher = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Refresh Status information
                getStatus();
                //Refresh screen with updated Status information
                updateScreen();
            }
        });
        guiRefresher.start();

    }

    private void showInfo(String msg) {
        Date now = new Date();
        infoArea.append(Globals.TIME_FORMAT.format(now) + " - " + msg + "\n");
        infoArea.setCaretPosition(infoArea.getDocument().getLength());
    }

    private void initializeRMI() throws RemoteException {
        DriverRemoteFunctions stub = (DriverRemoteFunctions) UnicastRemoteObject.exportObject(this, 0);
        Registry registry = LocateRegistry.getRegistry(Globals.RMI_PORT);
        registry.rebind(this.alias, stub);
    }


    private void updateStatus(Step s, double progress) {
        synchronized (this.status) {
            this.status.setStep(s);
            this.status.setProgress(progress);
            updateScreen();
        }

    }

    private void updateScreen() {
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

        this.statusLabel.setText(this.status.getStep().toString());
        this.eventCountLabel.setText("Sent events: "
                + Globals.LONG_FORMAT.format(totalEventsSent + phaseEventsSent));
        this.bar.setValue((int) (100 * this.status.getProgress()));

        statusPanel.revalidate();
    }

    @Override
    public Status getStatus() {
        synchronized (this.status) {
            switch (this.status.getStep()) {
            case READY:
            case LOADING:
                if (dg != null) {
                    this.status.setProgress(dg.getProgress());
                } else {
                    this.status.setProgress(1.0);
                }
                break;
            case RUNNING:
            case PAUSED:
            case ERROR:
                phaseEventsSent = 0;
                if (this.senders != null) {
                    for (Sender sender: this.senders) {
                        if (sender != null) {
                            phaseEventsSent += sender.getSentEventCount();
                        }
                    }
                }

                this.status.setProgress((1.0 * (totalEventsSent + phaseEventsSent) / this.totalEventCount));
                break;
            case FINISHED:
                phaseEventsSent = 0;
                this.status.setProgress((1.0 * (totalEventsSent) / this.totalEventCount));
                break;
            case STOPPED:
                totalEventsSent = 0;
                phaseEventsSent = 0;
                this.status.setProgress((1.0 * (totalEventsSent + phaseEventsSent) / this.totalEventCount));
            }

            if (this.senders != null) {
                for (Sender sender: this.senders) {
                    //If there is at least one sender thread with error:
                    if (sender != null && sender.getStatus().getStep() == Step.ERROR) {
                        if (this.status.getStep() != Step.ERROR) {
                            this.status.setStep(Step.ERROR);
                            showInfo("Error during load submission.");
                        }
                        break;
                    }
                    //Else, if there is at least one sender thread running:
                    else if (sender != null && sender.getStatus().getStep() == Step.RUNNING) {
                        this.status.setStep(Step.RUNNING);
                        break;
                    }
                }
            }
        }

        return this.status;
    }

    @Override
    public boolean load(DriverConfig cfg, int rtMode, int rtResolution, boolean useCreationTime,
            String dataFilesDir)
    throws InvalidStateException, Exception {
        if (this.status.getStep() == Step.DISCONNECTED
                || this.status.getStep() == Step.ERROR
                || this.status.getStep() == Step.FINISHED
                || this.status.getStep() == Step.STOPPED
                || this.status.getStep() == Step.CONNECTED) {
            showInfo("Loading Driver...");
            this.totalEventCount = 0;
            this.totalEventsSent = 0;

            this.drConfig = cfg;
            this.rtMode = rtMode;
            this.rtResolution = rtResolution;
            this.useScheduledTime = useCreationTime;

            // Logging
            if (drConfig.isLoggingEnabled()) {
                // Logging
                String logHeader = "FINCoS Driver Log File."
                    + "\n Driver Alias: " + cfg.getAlias()
                    + "\n Driver Address: " + cfg.getAddress().getHostAddress()
                    + "\n Connection: " + cfg.getConnection().alias
                    + "\n Load generation start time: " + new Date();

                try {
                    logger = new Logger(Globals.APP_PATH + "log" + File.separator + cfg.getAlias() + ".log",
                            logHeader, cfg.getLogFlushInterval(), cfg.getLoggingSamplingRate(),
                            cfg.getFieldsToLog());
                } catch (IOException e) {
                    this.showInfo("ERROR: Could not open log file (" + e.getMessage() + ").");
                    this.updateStatus(Step.ERROR, this.status.getProgress());
                    return false;
                }
            }

            ConnectionConfig connCfg = drConfig.getConnection();
            Properties connProps = new Properties();
            for (Entry<String, String> e: connCfg.properties.entrySet()) {
                connProps.put(e.getKey(), e.getValue());
            }
            // Tries to connect to JMS Provider
            if (drConfig.getConnection().type == ConnectionConfig.JMS) {
                this.adapterType = AdapterType.JMS;
                if (jmsInterface != null) {
                    try {
                        jmsInterface.disconnect();
                        jmsInterface = null;
                    } catch (Exception e) {
                        System.err.println("Could not disconnect from JMS provider. (" + e.getMessage() + ")");
                    }
                }

                try {
                    this.showInfo("Trying to establish connection with JMS provider...");
                    String cfName = connCfg.properties.get("cfName");
                    this.jmsInterface = new JMS_Writer(connProps, cfName, drConfig.getStreamNames(),
                                        rtMode, rtResolution);
                    this.showInfo("Done!");
                } catch (Exception e) {
                    this.showInfo("ERROR: Could not connect to JMS provider. (" + e.getMessage() + ").");
                    this.updateStatus(Step.ERROR, this.status.getProgress());
                    return false;
                }
            }
            // Tries to connect directly to the CEP engine
            else if (drConfig.getConnection().type == ConnectionConfig.CEP_ADAPTER) {
                this.adapterType = AdapterType.CEP;
                cepEngineInterface = CEPEngineFactory.getCEPEngineInterface(connProps, rtMode, rtResolution);
                if (cepEngineInterface == null) {
                    throw new Exception("Unsupported CEP engine");
                }
                this.showInfo("Trying to establish connection with CEP engine...");
                cepEngineInterface.connect();
                //Initializes CEP interface with no subscriptions
                cepEngineInterface.load(null, null);
                showInfo("Done!");
            }

            this.updateStatus(Step.CONNECTED, 0);

            int totalTestDuration = 0;
            int synthPhaseCount = 0;
            int extFilePhaseCount = 0;
            long t0, t1;
            long generatedEventCount = 0;

            // Sets the number of threads to be used during load submission
            if (cfg.getThreadCount() >= 1) { // Pre-fixed thread count
                threadCount = cfg.getThreadCount();
            } else {  // Dynamically determined (number of available processors)
                threadCount = Runtime.getRuntime().availableProcessors();
            }

            showInfo("Initializing Workload (" + cfg.getWorkload().length + " phases)");
            t0 = System.currentTimeMillis();
            for (int i = 0; i < cfg.getWorkload().length; i++) {
                if (cfg.getWorkload()[i] instanceof SyntheticWorkloadPhase) {
                    synthPhaseCount++;
                    SyntheticWorkloadPhase syntheticPhase = (SyntheticWorkloadPhase) cfg.getWorkload()[i];
                    showInfo(" Phase " + (i + 1) + ": Synthetic workload"
                            +  "\n\tApproximate phase duration: " + syntheticPhase.getDuration() + " seconds (" + new DecimalFormat("###.##").format(syntheticPhase.getDuration() / 3600.00) + " hour(s))."
                            + "  \n\tEvent submission rates:\n\t\tinitial: " + syntheticPhase.getInitialRate() + " events/second."
                            + "\n\t\tfinal:" + syntheticPhase.getFinalRate() + " events/second."
                            + "\n\t\taverage: " + ((syntheticPhase.getInitialRate() + syntheticPhase.getFinalRate()) / 2) + " events/second."
                            + "\n\t\tPoisson: " + (syntheticPhase.getArrivalProcess() == ArrivalProcess.POISSON ? "Yes" : "No")
                            + "\n\tEvent count: " + syntheticPhase.getTotalEventCount() + " events."
                    );

                    if (syntheticPhase.getDataGenMode() == SyntheticWorkloadPhase.DATASET) {
                        try {
                            showInfo("\tLoading data...");
                            dg = new DataGen(syntheticPhase);
                            this.updateStatus(Step.LOADING, 0);
                            // Cleans data directory
                            File phaseDataDir = new File(DEFAULT_DATA_FILES_DIR + File.separator + cfg.getAlias() + File.separator + "phase_" + (i + 1));
                            if (phaseDataDir.exists()) {
                                for (
                                        File  f : phaseDataDir.listFiles(new FileFilter() {
                                            public boolean accept(File pathname) {
                                                return pathname.getName().contains(".csv");
                                            }
                                        }
                                        )) {
                                    f.delete();
                                }
                            } else {
                                phaseDataDir.mkdirs();
                            }

                            // Generates events
                            dg.generateData(phaseDataDir.getAbsolutePath(), DATA_FILE_COUNT);
                            if (this.status.getStep() == Step.STOPPED) {
                                return false;
                            }
                            showInfo("\tDone! ");
                            generatedEventCount += dg.getGeneratedEventsCount();
                        } catch (Exception ioe) {
                            showInfo("ERROR: Could not load data (" + ioe.getMessage() + ")");
                            this.updateStatus(Step.ERROR, this.status.getProgress());
                            return false;
                        }
                    }
                    totalTestDuration += syntheticPhase.getDuration();
                    totalEventCount += syntheticPhase.getTotalEventCount();
                } else if (cfg.getWorkload()[i] instanceof ExternalFileWorkloadPhase) {
                    extFilePhaseCount++;
                    ExternalFileWorkloadPhase filePhase = (ExternalFileWorkloadPhase) cfg.getWorkload()[i];
                    String detailInfo = " Phase " + (i + 1) + ": External File workload"
                    + "\n\tFile path: " + filePhase.getFilePath()
                    + "\n\tLoop count: " + filePhase.getLoopCount();
                    // timestamp info
                    if (filePhase.containsTimestamps()) {
                        detailInfo += "\n\tContains timestamps: Yes, expressed in ";
                        switch (filePhase.getTimestampUnit()) {
                        case ExternalFileWorkloadPhase.MILLISECONDS:
                            detailInfo += "milliseconds";
                            break;
                        case ExternalFileWorkloadPhase.SECONDS:
                            detailInfo += "seconds";
                            break;
                        case ExternalFileWorkloadPhase.DATE_TIME:
                            detailInfo += "date/time";
                        }
                        if (filePhase.isUsingTimestamps()) {
                            detailInfo += "\n\tUse timestamps for event submission: Yes";
                        } else {
                            detailInfo += "\n\tUse timestamps for event submission: No";
                            detailInfo += "\n\tEvents will be submitted at a rate of " + filePhase.getEventSubmissionRate() + " events/sec.";
                        }
                    } else {
                        detailInfo += "\n\tContains timestamps: No";
                        detailInfo += "\n\tEvents will be submitted at a rate of " + filePhase.getEventSubmissionRate() + " events/sec.";
                    }
                    //Event types info
                    if (filePhase.containsEventTypes()) {
                        detailInfo += "\n\tContains event types: Yes";
                    } else {
                        detailInfo += "\n\tContains event types: No";
                        detailInfo += " (all events in the file are of the same type: \"" + filePhase.getSingleEventTypeName() + "\")";
                    }

                    showInfo(detailInfo);
                }
            }
            t1 = System.currentTimeMillis();

            this.updateStatus(Step.READY, 100);
            showInfo(" Driver loading finished (elapsed Time: " + (t1 - t0) / 1000 + " seconds)" +
                    "\n\t # Synthetic phases: " + synthPhaseCount + " (Total of generated events: " + generatedEventCount + ")" +
                    "\n\t # External File phases: " + extFilePhaseCount + ".");


            return true;
        } else {
            showInfo("Cannot load driver. Driver has already been loaded.");
            throw new InvalidStateException("Cannot load driver. Driver has already been loaded.");
        }

    }

    @Override
    public void start() throws InvalidStateException {
        if (this.status.getStep() == Step.READY) {
            // Initializes a new thread to run load, so RMI thread will return immediately after call.
            new Thread() {
                @Override
                public void run() {
                    try {
                        long testT0, phaseT0;
                        testT0 = System.currentTimeMillis();

                        for (int i = 0; i < drConfig.getWorkload().length; i++) {
                            phaseT0 = System.currentTimeMillis();

                            // Starts phase
                            if (drConfig.getWorkload()[i] instanceof SyntheticWorkloadPhase) {
                                startSyntheticPhase((SyntheticWorkloadPhase) drConfig.getWorkload()[i], i + 1);
                            } else if (drConfig.getWorkload()[i] instanceof ExternalFileWorkloadPhase) {
                                startExternalDatasetPhase((ExternalFileWorkloadPhase) drConfig.getWorkload()[i], i + 1);
                            }

                            // After phase completion, update stats and check if Driver was stopped or paused
                            synchronized (status) {
                                if (status.getStep() != Step.STOPPED) {
                                    for (Sender sender: senders) {
                                        totalEventsSent += sender.getSentEventCount();
                                        phaseEventsSent -= sender.getSentEventCount();
                                    }
                                    // If Driver was paused, wait
                                    while (status.getStep() == Step.PAUSED) {
                                        status.wait();
                                    }
                                }
                                // If Stopped, close connections with FINCoS Adapter and FINCoS Perfmon
                                else {
                                    if (adapterType == AdapterType.JMS) {
                                        if (jmsInterface != null) {
                                            try {
                                                jmsInterface.disconnect();
                                                jmsInterface = null;
                                            } catch (Exception e) {
                                                System.err.println("Could not disconnect from JMS provider. (" + e.getMessage() + ")");
                                            }
                                        }
                                    } else if (adapterType == AdapterType.CEP) {
                                        if (cepEngineInterface != null) {
                                            cepEngineInterface.disconnect();
                                            cepEngineInterface = null;
                                        }
                                    }

                                    return;
                                }
                            }

                            showInfo("Phase "  + (i + 1) + " finished (elapsed time: " + ((System.currentTimeMillis()-phaseT0))/1000 +  " seconds).");
                        }

                        showInfo("Test finished. Total test duration: " + ((System.currentTimeMillis()-testT0))/1000 +  " seconds).");
                        updateStatus(Step.FINISHED, status.getProgress());
                    } catch (IOException ioe) {
                        updateStatus(Step.ERROR, status.getProgress());
                        showInfo("Exception: " + ioe.getMessage());
                    } catch (InterruptedException e) {
                        updateStatus(Step.ERROR, status.getProgress());
                        showInfo("Interrupted Exception");
                    } finally {
                        if (logger != null) {
                            logger.close();
                            logger = null;
                        }

                        try {
                            if (jmsInterface != null) {
                                jmsInterface.disconnect();
                                jmsInterface = null;
                            }
                            if (cepEngineInterface != null) {
                                //cepEngineInterface.disconnect(); C8 gets messed with this
                                cepEngineInterface = null;
                            }

                        } catch (Exception e) {
                            showInfo("Could not disconnect from server. (" + e.getMessage() +")");
                        }
                    }
                }
            }.start();
        } else {
            if (this.status.getStep() == Step.PAUSED) {
                this.resume();
            } else {
                showInfo("Cannot start driver. Driver has not been loaded.");
                throw new InvalidStateException("Driver has not been loaded.");
            }
        }
    }

    /**
     *
     * Starts a phase with synthetic workload.
     *
     * @param syntheticPhase    Synthetic workload parameters
     * @param phaseNumber       The ID of the phase
     * @throws IOException
     * @throws InterruptedException
     */
    private void startSyntheticPhase(SyntheticWorkloadPhase syntheticPhase, int phaseNumber) throws IOException, InterruptedException {
        ThreadGroup senderGroup;
        Scheduler sch;
        DataFileReader reader = null;
        double initialThreadRate, finalThreadRate;
        LinkedHashMap<EventType, Double> schema;
        Set<EventType> types = null;
        senderGroup = new ThreadGroup("Senders");
        showInfo("Phase " + (phaseNumber) + " started. Initializing dispatcher threads (Thread Count: " + threadCount + ")");
        schema = syntheticPhase.getSchema();
        if (schema != null) {
            types = schema.keySet();
        }
        initialThreadRate = syntheticPhase.getInitialRate() / threadCount;
        finalThreadRate  = syntheticPhase.getFinalRate() / threadCount;
        // Creates dispatcher threads
        senders = new Sender[threadCount];

        if (syntheticPhase.getDataGenMode() == SyntheticWorkloadPhase.DATASET) {
            reader = new DataFileReader(DEFAULT_DATA_FILES_DIR + File.separator + drConfig.getAlias() +
                    File.separator + "phase_" + (phaseNumber) + File.separator +
                    DATA_FILE_COUNT + ".csv", types);
        } else if (syntheticPhase.getDataGenMode() == SyntheticWorkloadPhase.RUNTIME) {
            dg = new DataGen(syntheticPhase);
        }

        for (int j = 0; j < threadCount; j++) {
            sch = new Scheduler(initialThreadRate, finalThreadRate,
                    syntheticPhase.getDuration(),
                    syntheticPhase.getArrivalProcess(),
                    syntheticPhase.getRandomSeed());
            if (adapterType == AdapterType.JMS) {
                if (syntheticPhase.getDataGenMode() == SyntheticWorkloadPhase.DATASET) {
                    senders[j] = new Sender(jmsInterface, sch, reader, false,
                                            senderGroup, this.alias + "/sender-" + (j + 1), 1, rtMode, rtResolution,
                                            perfTracingEnabled);
                } else if (syntheticPhase.getDataGenMode() == SyntheticWorkloadPhase.RUNTIME) {
                    senders[j] = new Sender(jmsInterface, sch, dg,
                                            senderGroup, this.alias + "/sender-" + (j + 1), 1, rtMode, rtResolution,
                                            perfTracingEnabled);
                }
            } else if (adapterType == AdapterType.CEP) {
                if (syntheticPhase.getDataGenMode() == SyntheticWorkloadPhase.DATASET) {
                    senders[j] = new Sender(cepEngineInterface, sch, reader, false,
                                            senderGroup, this.alias + "/sender-" + (j + 1), 1, rtMode, rtResolution,
                                            perfTracingEnabled);
                } else if (syntheticPhase.getDataGenMode() == SyntheticWorkloadPhase.RUNTIME) {
                    senders[j] = new Sender(cepEngineInterface, sch, dg,
                                            senderGroup, this.alias + "/sender-" + (j + 1), 1, rtMode, rtResolution,
                                            perfTracingEnabled);
                }
            }

            senders[j].setLogger(logger);
            senders[j].setUseScheduledTime(useScheduledTime);
            senders[j].start();
        }
        updateStatus(Step.RUNNING, 0);
        showInfo("Done. Sending events...");

        // Waits for completion of all sending threads
        for (Sender sender : senders) {
            sender.join();
        }

        dg = null;
    }

    /**
     * Starts a phase with a workload controlled by an external dataset file.
     *
     * @param filePhase         The workload parameters
     * @param phaseNumber       The ID of the phase
     * @throws IOException
     * @throws InterruptedException
     */
    private void startExternalDatasetPhase(ExternalFileWorkloadPhase filePhase, int phaseNumber) throws IOException, InterruptedException {
        Scheduler sch;
        DataFileReader reader = null;
        showInfo("Phase " + phaseNumber + " started. Initializing dispatcher thread...");
        senders = new Sender[1];

        reader = new DataFileReader(filePhase.getFilePath(), filePhase.containsTimestamps(),
                filePhase.getTimestampUnit(), filePhase.containsEventTypes(),
                filePhase.getSingleEventTypeName());
        if (adapterType == AdapterType.JMS) {
            // Event submission is based on timestamps in the data file
            if (filePhase.containsTimestamps() && filePhase.isUsingTimestamps()) {
                senders[0] = new Sender(jmsInterface, reader, filePhase.getTimestampUnit(),
                                        filePhase.getLoopCount(), rtMode, rtResolution,
                                        perfTracingEnabled);
            } else { // Event submission is scheduled based on a fixed rate
                sch = new Scheduler(filePhase.getEventSubmissionRate(),
                        filePhase.getEventSubmissionRate(),
                        1, ArrivalProcess.DETERMINISTIC, 1L);
                senders[0] = new Sender(jmsInterface, sch, reader, filePhase.containsTimestamps(),
                                        null, this.alias + "/sender-1", filePhase.getLoopCount(), rtMode, rtResolution,
                                        perfTracingEnabled);
            }
        } else if (adapterType == AdapterType.CEP) {
            // Event submission is based on timestamps in the data file
            if (filePhase.containsTimestamps() && filePhase.isUsingTimestamps()) {
                senders[0] = new Sender(cepEngineInterface, reader, filePhase.getTimestampUnit(),
                                        filePhase.getLoopCount(), rtMode, rtResolution,
                                        perfTracingEnabled);
            } else { // Event submission is scheduled based on a fixed rate
                sch = new Scheduler(filePhase.getEventSubmissionRate(),
                        filePhase.getEventSubmissionRate(),
                        1, ArrivalProcess.DETERMINISTIC, 1L);
                    senders[0] = new Sender(cepEngineInterface, sch, reader, filePhase.containsTimestamps(),
                                            null, this.alias + "/sender-1", filePhase.getLoopCount(), rtMode, rtResolution,
                                            perfTracingEnabled);
            }
        }
        senders[0].setUseScheduledTime(useScheduledTime);
        senders[0].start();

        updateStatus(Step.RUNNING, 0);
        showInfo("Done! Sending events...");

        // Waits for completion of the sending thread
        senders[0].join();
    }

    private void resume() {
        synchronized (this.status) {
            this.status.setStep(Step.RUNNING);
            showInfo("Driver has been resumed.");
            this.status.notifyAll();
        }
        for (Sender sender : this.senders) {
            sender.resumeLoad();
        }
    }

    @Override
    public void pause() throws InvalidStateException {
        synchronized (this.status) {
            if (this.status.getStep() == Step.RUNNING) {
                for (Sender sender : this.senders) {
                    sender.pauseLoad();
                }
                this.status.setStep(Step.PAUSED);
                showInfo("Driver is paused.");
            } else {
                showInfo("Cannot pause Driver. Driver must be running in order to be paused.");
                throw new InvalidStateException("Driver must be running in order to be paused.");
            }
        }
    }

    @Override
    public void stop() throws InvalidStateException {
        synchronized (this.status) {
            if (this.status.getStep() == Step.RUNNING
                    || this.status.getStep() == Step.PAUSED
                    || this.status.getStep() == Step.ERROR) {
                this.status.setStep(Step.STOPPED);
                if (this.senders != null) {
                    for (Sender sender : this.senders) {
                        sender.stopLoad();
                        sender = null;
                    }
                    this.senders = null;
                }

                if (logger != null) {
                    this.logger.close();
                    logger = null;
                }

                showInfo("Driver has been stopped.");
            } else if (this.status.getStep() == Step.LOADING) {
                this.status.setStep(Step.STOPPED);
                this.dg.stopDataGeneration();
                showInfo("Data generation was stopped.");
            } else if (this.status.getStep() == Step.READY || this.status.getStep() == Step.FINISHED) {
                this.status.setStep(Step.STOPPED);
                totalEventsSent = 0;
                phaseEventsSent = 0;
                showInfo("Driver has been stopped.");
            } else {
                throw new InvalidStateException("Driver must be loading, ready, running or paused in order to be stopped.");
            }
        }
    }

    @Override
    public void switchToNextPhase() throws RemoteException, InvalidStateException {
        synchronized (this.status) {
            if (this.status.getStep() == Step.RUNNING
                    || this.status.getStep() == Step.PAUSED) {
                showInfo("Switching to next phase");
                for (Sender sender : this.senders) {
                    sender.stopLoad();
                }
                showInfo("Done!");
            } else {
                throw new InvalidStateException("Driver must be running or paused in order to switch of phase.");
            }
        }
    }

    @Override
    public void alterRate(double factor) throws InvalidStateException, RemoteException {
        synchronized (this.status) {
            if (this.status.getStep() == Step.RUNNING
                    || this.status.getStep() == Step.PAUSED) {
                showInfo("Changing event submission rate (factor: " + factor + "x)");
                for (Sender sender : this.senders) {
                    sender.setRateFactor(factor);
                }
                showInfo("Done!");
            } else {
                throw new InvalidStateException("Driver must be running or paused in order to alter event submission rate.");
            }

        }
    }

    @Override
    public void setPerfTracing(boolean enabled) throws RemoteException {
        this.perfTracingEnabled = enabled;
        if (this.senders != null) {
            for (Sender sender: this.senders) {
                if (sender != null) {
                    sender.setPerfTracing(enabled);
                }
            }
        }
    }

    @Override
    public DriverPerfStats getPerfStats() throws RemoteException {
        long start = Long.MAX_VALUE;
        long end = 0;
        HashMap<String, Integer> streamStats = new HashMap<String, Integer>();
        if (this.senders != null) {
            for (Sender sender: this.senders) {
                if (sender != null) {
                    DriverPerfStats senderStats = sender.getPerfStats();
                    if (senderStats.getStart() != -1) { // there is some stat
                        start = Math.min(start, senderStats.getStart());
                        end = Math.max(end, senderStats.getEnd());
                        for (Entry<String, Integer> e: senderStats.getStreamStats().entrySet()) {
                            String stream = e.getKey();
                            Integer senderCount = e.getValue();
                            Integer totalCount = streamStats.get(stream);
                            if (totalCount == null) {
                                streamStats.put(stream, senderCount);
                            } else {
                                streamStats.put(stream, totalCount + senderCount);
                            }
                        }
                        senderStats.reset();
                    } else {
                        continue;
                    }
                }
            }
        }
        return new DriverPerfStats(start, end, streamStats);
    }

}
