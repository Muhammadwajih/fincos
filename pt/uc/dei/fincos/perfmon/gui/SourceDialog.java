package pt.uc.dei.fincos.perfmon.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TreeSet;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.data.CSVReader;
import pt.uc.dei.fincos.perfmon.OfflinePerformanceValidator;
import pt.uc.dei.fincos.perfmon.PerformanceStats;
import pt.uc.dei.fincos.perfmon.Stream;

public class SourceDialog extends javax.swing.JDialog {

    private final Font f = new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11);

    /** List of log files to be processed by this perfmon instance. */
    ArrayList <String> sinkLogFiles;

    /** Start of measurement interval for Sink log files. */
    private Date sinkLogStartTime = new Date();

    /** End of measurement interval for Sink log files. */
    private Date sinkLogEndTime = new Date(sinkLogStartTime.getTime() + 3600000);

    private PerformanceMonitor parent;

    /** Creates new form SourceDialog */
    public SourceDialog(PerformanceMonitor parent) {
        super(parent, true);
        this.parent = parent;
        this.setTitle("Select data source");
        sinkLogFiles = new ArrayList<String>();
        initComponents();
        addListeners();
        setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        srcBtnGroup = new javax.swing.ButtonGroup();
        srcPanel = new javax.swing.JPanel();
        sinkLogRadio = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        sinkLogsList = new javax.swing.JList();
        sinkAddLogBtn = new javax.swing.JButton();
        sinkDelLogBtn = new javax.swing.JButton();
        miPanel = new javax.swing.JPanel();
        sinkLogStartLbl = new javax.swing.JLabel();
        sinkLogStartSpinner = new javax.swing.JSpinner();
        sinkLogEndSpinner = new javax.swing.JSpinner();
        sinkLogEndLbl = new javax.swing.JLabel();
        saveToFileCheck = new javax.swing.JCheckBox();
        saveToFileField = new javax.swing.JTextField();
        perfLogBrowseBtn = new javax.swing.JButton();
        perfLogRadio = new javax.swing.JRadioButton();
        perfLogField = new javax.swing.JTextField();
        okBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);

        srcPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Data Source"));

        srcBtnGroup.add(sinkLogRadio);
        sinkLogRadio.setText("Sink log file(s)");

        jScrollPane1.setViewportView(sinkLogsList);

        sinkAddLogBtn.setText("Add...");
        sinkAddLogBtn.setPreferredSize(new java.awt.Dimension(71, 23));

        sinkDelLogBtn.setText("Remove");

        miPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Measurement Interval:"));
        miPanel.setPreferredSize(new java.awt.Dimension(370, 50));

        sinkLogStartLbl.setText("Start:");

        sinkLogStartSpinner.setModel(new javax.swing.SpinnerDateModel());

        sinkLogEndSpinner.setModel(new javax.swing.SpinnerDateModel());

        sinkLogEndLbl.setText("End:");

        javax.swing.GroupLayout miPanelLayout = new javax.swing.GroupLayout(miPanel);
        miPanel.setLayout(miPanelLayout);
        miPanelLayout.setHorizontalGroup(
                miPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(miPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(sinkLogStartLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sinkLogStartSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                        .addComponent(sinkLogEndLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sinkLogEndSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
        );
        miPanelLayout.setVerticalGroup(
                miPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(miPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(sinkLogStartLbl)
                        .addComponent(sinkLogStartSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(sinkLogEndLbl)
                        .addComponent(sinkLogEndSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        saveToFileCheck.setText("Save to file:");

        perfLogBrowseBtn.setFont(new java.awt.Font("SansSerif", 0, 10)); // NOI18N
        perfLogBrowseBtn.setText("...");
        perfLogBrowseBtn.setMaximumSize(new java.awt.Dimension(30, 20));
        perfLogBrowseBtn.setMinimumSize(new java.awt.Dimension(30, 20));
        perfLogBrowseBtn.setPreferredSize(new java.awt.Dimension(30, 20));

        srcBtnGroup.add(perfLogRadio);
        perfLogRadio.setText("Perfmon log file");

        javax.swing.GroupLayout srcPanelLayout = new javax.swing.GroupLayout(srcPanel);
        srcPanel.setLayout(srcPanelLayout);
        srcPanelLayout.setHorizontalGroup(
                srcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(srcPanelLayout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addGroup(srcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                                .addGroup(srcPanelLayout.createSequentialGroup()
                                        .addComponent(sinkAddLogBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(sinkDelLogBtn)))
                                        .addContainerGap(10, Short.MAX_VALUE))
                                        .addGroup(srcPanelLayout.createSequentialGroup()
                                                .addGap(48, 48, 48)
                                                .addComponent(saveToFileField, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                                                .addContainerGap(10, Short.MAX_VALUE))
                                                .addGroup(srcPanelLayout.createSequentialGroup()
                                                        .addGap(27, 27, 27)
                                                        .addGroup(srcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addComponent(saveToFileCheck)
                                                                .addComponent(miPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                .addContainerGap(10, Short.MAX_VALUE))
                                                                .addGroup(srcPanelLayout.createSequentialGroup()
                                                                        .addContainerGap()
                                                                        .addGroup(srcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, srcPanelLayout.createSequentialGroup()
                                                                                        .addGap(21, 21, 21)
                                                                                        .addComponent(perfLogField, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                                                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                        .addComponent(perfLogBrowseBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                        .addComponent(sinkLogRadio)
                                                                                        .addComponent(perfLogRadio))
                                                                                        .addContainerGap())
        );
        srcPanelLayout.setVerticalGroup(
                srcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(srcPanelLayout.createSequentialGroup()
                        .addComponent(sinkLogRadio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(srcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(sinkAddLogBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(sinkDelLogBtn))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(miPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(saveToFileCheck)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(saveToFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(perfLogRadio)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(srcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(perfLogField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(perfLogBrowseBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addContainerGap())
        );

        okBtn.setText("OK");
        okBtn.setPreferredSize(new java.awt.Dimension(75, 30));

        cancelBtn.setText("Cancel");
        cancelBtn.setPreferredSize(new java.awt.Dimension(75, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(srcPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(okBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(srcPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(okBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();

        sinkLogsList.setFont(f);
        sinkAddLogBtn.setFont(f);
        sinkDelLogBtn.setFont(f);
        ((TitledBorder) miPanel.getBorder()).setTitleFont(f);
        sinkLogStartLbl.setFont(f);
        sinkLogStartSpinner.setFont(f);
        sinkLogEndLbl.setFont(f);
        sinkLogEndSpinner.setFont(f);
        saveToFileCheck.setFont(f);
        saveToFileField.setFont(f);

        saveToFileCheck.setSelected(false);
        sinkLogStartSpinner.setFont(sinkLogStartSpinner.getFont().deriveFont(Font.PLAIN));
        JSpinner.DateEditor editor1 = new JSpinner.DateEditor(sinkLogStartSpinner, "dd-MM-yyyy HH:mm:ss");
        sinkLogStartSpinner.setValue(sinkLogStartTime);
        sinkLogStartSpinner.setEditor(editor1);
        sinkLogEndSpinner.setFont(sinkLogStartSpinner.getFont());
        JSpinner.DateEditor editor2 = new JSpinner.DateEditor(sinkLogEndSpinner, "dd-MM-yyyy HH:mm:ss");
        sinkLogEndSpinner.setValue(sinkLogEndTime);
        sinkLogEndSpinner.setEditor(editor2);

        sinkLogChooser = new JFileChooser(Globals.APP_PATH + "log");
        sinkLogChooser.addChoosableFileFilter(new FileNameExtensionFilter("Sink Log file", "log"));
        sinkLogChooser.setAcceptAllFileFilterUsed(false);
        sinkLogChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        sinkLogChooser.setMultiSelectionEnabled(true);

        perfLogChooser = new JFileChooser(Globals.APP_PATH + "log");
        perfLogChooser.addChoosableFileFilter(new FileNameExtensionFilter("PerfMon Log file", "csv"));
        perfLogChooser.setAcceptAllFileFilterUsed(false);
        perfLogChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }

    private void addListeners() {
        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sinkLogRadio.isSelected()) { // Sink log file(s)
                    if (!sinkLogFiles.isEmpty()) {
                        processSinkLogFiles();
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, "Inform the path of at least one Sink log file.");
                    }
                } else { // Perfmon log file
                    processPerfmonLogFile();
                    dispose();
                }
            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        sinkAddLogBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int action = sinkLogChooser.showOpenDialog(null);

                File[] selectedFiles = sinkLogChooser.getSelectedFiles();

                if (action == JFileChooser.APPROVE_OPTION && selectedFiles != null) {
                    for (File file : selectedFiles) {
                        String newFile = file.getPath();
                        if (!sinkLogFiles.contains(newFile)) {
                            sinkLogFiles.add(newFile);
                            sinkLogsList.setListData(sinkLogFiles.toArray(new String[0]));
                            try {
                                long logStartTime = 1000 * (OfflinePerformanceValidator.getLogStartTimeInMillis(newFile) / 1000);
                                long logEndTime = 1000 * (OfflinePerformanceValidator.getLogEndTimeInMillis(newFile) / 1000) + 999;
                                if (sinkLogFiles.size() == 1) { // First element
                                    sinkLogStartTime = new Date(logStartTime);
                                    sinkLogEndTime = new Date(logEndTime);
                                } else {
                                    sinkLogStartTime.setTime(Math.min(sinkLogStartTime.getTime(), logStartTime));
                                    sinkLogEndTime.setTime(Math.max(sinkLogEndTime.getTime(), logEndTime));
                                }
                                sinkLogStartSpinner.setValue(sinkLogStartTime);
                                sinkLogEndSpinner.setValue(sinkLogEndTime);
                            } catch (Exception exc) {
                                JOptionPane.showMessageDialog(null, exc.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "This file has already been added");
                        }
                    }
                }
                sinkDelLogBtn.setEnabled(sinkLogFiles.size() > 0);
            }
        });

        sinkDelLogBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selected  = sinkLogsList.getSelectedIndices();

                int removedCount = 0;

                for (int index : selected) {
                    if (index >= 0) {
                        sinkLogFiles.remove(index - removedCount);
                        removedCount++;
                    }
                }
                sinkLogsList.setListData(sinkLogFiles.toArray(new String[0]));

                sinkDelLogBtn.setEnabled(sinkLogFiles.size() > 0);
            }
        });

        perfLogBrowseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int action = perfLogChooser.showOpenDialog(null);
                if (action == JFileChooser.APPROVE_OPTION && perfLogChooser.getSelectedFile() != null) {
                    String perfLogFile = perfLogChooser.getSelectedFile().getPath();
                    perfLogField.setText(perfLogFile);
                }
            }
        });

        ItemListener radioListener1 = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // Sink log file
                sinkLogsList.setEnabled(sinkLogRadio.isSelected());
                sinkAddLogBtn.setEnabled(sinkLogRadio.isSelected());
                sinkDelLogBtn.setEnabled(sinkLogRadio.isSelected() && sinkLogsList.getModel().getSize() > 0);
                miPanel.setEnabled(sinkLogRadio.isSelected());
                sinkLogStartSpinner.setEnabled(sinkLogRadio.isSelected());
                sinkLogEndLbl.setEnabled(sinkLogRadio.isSelected());
                sinkLogEndSpinner.setEnabled(sinkLogRadio.isSelected());
                saveToFileCheck.setEnabled(sinkLogRadio.isSelected());
                saveToFileField.setEnabled(sinkLogRadio.isSelected() && saveToFileCheck.isSelected());
                // FINCoS perfmon log
                perfLogField.setEnabled(perfLogRadio.isSelected());
                perfLogBrowseBtn.setEnabled(perfLogRadio.isSelected());
            }
        };
        sinkLogRadio.addItemListener(radioListener1);
        perfLogRadio.addItemListener(radioListener1);
        sinkLogRadio.setSelected(true);

        ChangeListener spinnerChangeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                SpinnerDateModel startModel = (SpinnerDateModel) sinkLogStartSpinner.getModel();
                startModel.setStart(sinkLogStartTime);
                startModel.setEnd((Date) sinkLogEndSpinner.getValue());

                SpinnerDateModel endModel = (SpinnerDateModel) sinkLogEndSpinner.getModel();
                endModel.setStart((Date) sinkLogStartSpinner.getValue());
                endModel.setEnd(sinkLogEndTime);
            }

        };
        sinkLogStartSpinner.addChangeListener(spinnerChangeListener);
        sinkLogEndSpinner.addChangeListener(spinnerChangeListener);

        saveToFileCheck.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                saveToFileField.setEnabled(saveToFileCheck.isSelected());
                GregorianCalendar now = new GregorianCalendar();
                if (saveToFileCheck.isSelected()) {
                    saveToFileField.setText(Globals.APP_PATH + "log"
                            + File.separator + "["
                            + now.get(Calendar.YEAR) + "-"
                            + Globals.INT_FORMAT_2.format(now.get(Calendar.MONTH) + 1) + "-"
                            + Globals.INT_FORMAT_2.format(now.get(Calendar.DAY_OF_MONTH)) + "_"
                            + Globals.INT_FORMAT_2.format(now.get(Calendar.HOUR_OF_DAY)) + "-"
                            + Globals.INT_FORMAT_2.format(now.get(Calendar.MINUTE)) + "].csv");
                } else {
                    saveToFileField.setText("");
                }
            }
        });
    }

    private void processSinkLogFiles() {
        String[] inputSinkLogFiles = sinkLogFiles.toArray(new String[0]);
        boolean saveToFile = saveToFileCheck.isSelected();
        String outputPerfMonLogFile = saveToFileField.getText();
        long startTime = ((Date) sinkLogStartSpinner.getValue()).getTime();
        long endTime = ((Date) sinkLogEndSpinner.getValue()).getTime();

        try {
            parent.showInfo("Processing Sink log files...");
            OfflinePerformanceValidator offlinePerf = new OfflinePerformanceValidator(inputSinkLogFiles,
                    saveToFile, outputPerfMonLogFile);
            LogProcessProgressDialog progDialog = new LogProcessProgressDialog(parent, offlinePerf);
            SinkLogProcessor logProcessor = new SinkLogProcessor(offlinePerf, startTime,
                    endTime, progDialog, parent);
            logProcessor.execute();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Could not process log files (" + e.getMessage() + ")", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processPerfmonLogFile() {
        parent.showInfo("Loading FINCoS Performance Monitor log file...");
        CSVReader logReader;
        TreeSet<PerformanceStats> statsSeries = new TreeSet<PerformanceStats>();

        try {
            logReader = new CSVReader(perfLogField.getText());
            String header = logReader.getNextLine();

            if (header != null && header.equals("FINCoS Performance Log File.")) {
                //ignores next header lines
                do {
                    header = logReader.getNextLine();
                }
                while(header != null && !header.startsWith("Timestamp"));

                String event;
                String[] splitEv;
                String server;
                String streamName, streamType;
                Stream stream;
                PerformanceStats stats = null;

                while ((event = logReader.getNextLine()) != null) {
                    splitEv = CSVReader.split(event, Globals.CSV_SEPARATOR);
                    if (splitEv.length == 9) { // Legacy log files
                        server = splitEv[1];
                        streamName = splitEv[2].substring(0, splitEv[2].indexOf("("));
                        streamType = splitEv[2].substring(splitEv[2].indexOf("(") + 1, splitEv[2].indexOf(")"));
                        if (streamType.equalsIgnoreCase("input")) {
                            stream = new Stream(Stream.INPUT, streamName);
                        } else {
                            stream = new Stream(Stream.OUTPUT, streamName);
                        }
                        stats = new PerformanceStats(server, stream);
                        stats.timestamp = Long.parseLong(splitEv[0]);
                        stats.avgThroughput = Double.parseDouble(splitEv[3]);
                        stats.minThroughput = Double.NaN;
                        stats.maxThroughput = Double.NaN;
                        stats.lastThroughput = Integer.parseInt(splitEv[4]);
                        stats.avgRT = Double.parseDouble(splitEv[5]);
                        stats.minRT = Double.parseDouble(splitEv[6]);
                        stats.maxRT = Double.parseDouble(splitEv[7]);
                        stats.lastRT = Double.parseDouble(splitEv[8]);
                        stats.stdevRT = Double.NaN;
                        statsSeries.add(stats);
                    } else if (splitEv.length == 12) { // Current log format
                        server = splitEv[1];
                        streamName = splitEv[2].substring(0, splitEv[2].indexOf("("));
                        streamType = splitEv[2].substring(splitEv[2].indexOf("(") + 1, splitEv[2].indexOf(")"));
                        if (streamType.equalsIgnoreCase("input")) {
                            stream = new Stream(Stream.INPUT, streamName);
                        } else {
                            stream = new Stream(Stream.OUTPUT, streamName);
                        }
                        try {
                            stats = new PerformanceStats(server, stream);
                            stats.timestamp = Long.parseLong(splitEv[0]);
                            stats.avgThroughput = Double.parseDouble(splitEv[3]);
                            stats.minThroughput = Double.parseDouble(splitEv[4]);
                            stats.maxThroughput = Double.parseDouble(splitEv[5]);
                            stats.lastThroughput = Double.parseDouble(splitEv[6]);
                            stats.avgRT = Double.parseDouble(splitEv[7]);
                            stats.minRT = Double.parseDouble(splitEv[8]);
                            stats.maxRT = Double.parseDouble(splitEv[9]);
                            stats.stdevRT = Double.parseDouble(splitEv[10]);
                            stats.lastRT = Double.parseDouble(splitEv[11]);
                            statsSeries.add(stats);
                        } catch (NumberFormatException e) {
                            System.err.println("NumberFormatException " + e.getMessage());
                        }
                    } else {
                        System.err.println("Invalid record format. Expecting 12 fields, got "
                                          + splitEv.length + " fields.");
                    }
                }
                parent.loadForPerfMonLogFile(statsSeries);
            } else {
                JOptionPane.showMessageDialog(null, "Invalid log file", "Error", JOptionPane.ERROR_MESSAGE);
                parent.showInfo("Failed");
                return;
            }
            parent.showInfo("Done!");
        } catch (IOException e) {
            parent.showInfo("Failed");
        }
    }

    JFileChooser sinkLogChooser, perfLogChooser;

    // Variables declaration - do not modify
    private javax.swing.JButton cancelBtn;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel miPanel;
    private javax.swing.JButton okBtn;
    private javax.swing.JButton perfLogBrowseBtn;
    private javax.swing.JTextField perfLogField;
    private javax.swing.JRadioButton perfLogRadio;
    private javax.swing.JCheckBox saveToFileCheck;
    private javax.swing.JTextField saveToFileField;
    private javax.swing.JButton sinkAddLogBtn;
    private javax.swing.JButton sinkDelLogBtn;
    private javax.swing.JLabel sinkLogEndLbl;
    private javax.swing.JSpinner sinkLogEndSpinner;
    private javax.swing.JRadioButton sinkLogRadio;
    private javax.swing.JLabel sinkLogStartLbl;
    private javax.swing.JSpinner sinkLogStartSpinner;
    private javax.swing.JList sinkLogsList;
    private javax.swing.ButtonGroup srcBtnGroup;
    private javax.swing.JPanel srcPanel;
    // End of variables declaration

}
