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


package pt.uc.dei.fincos.perfmon.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import pt.uc.dei.fincos.perfmon.OfflinePerformanceValidator;
import pt.uc.dei.fincos.perfmon.PerformanceStats;

/**
 * Processes Sink log files in background.
 *
 * @author  Marcelo R.N. Mendes
 */
public class SinkLogProcessor extends SwingWorker<Set<PerformanceStats>, Void> {

    /** Computes performance stats from one or more Sink log files. */
    OfflinePerformanceValidator offlinePerf;

    /** Start of measurement interval. */
    long startTime;

    /** End of measurement interval. */
    long endTime;

    /** A dialog to show the progress of log processing. */
    LogProcessProgressDialog progressDialog;

    /** Updates the progress of log processing on the GUI. */
    Timer progressUpdater;

    /** Perfmon app. */
    PerformanceMonitor parentApp;

    /**
     *
     * Creates a thread to process sink log files.
     *
     * @param offlinePerf       computes performance stats from Sink log files
     * @param startTime         start point from which log files must be processed (in milliseconds)
     * @param endTime           ending point until which log files must be processed (in milliseconds)
     * @param progressDialog    a dialog to show the progress of log processing
     * @param parent            the parent Perfmon application of this background thread
     */
    public SinkLogProcessor(OfflinePerformanceValidator offlinePerf,
            long startTime, long endTime,
            LogProcessProgressDialog progressDialog,
            PerformanceMonitor parent) {
        this.offlinePerf = offlinePerf;
        this.startTime = startTime;
        this.endTime = endTime;
        this.progressDialog = progressDialog;
        this.parentApp = parent;
    }

    @Override
    protected Set<PerformanceStats> doInBackground() throws Exception  {
        progressDialog.setVisible(true);
        if (progressUpdater != null) {
            progressUpdater.stop();
            progressUpdater = null;
        }
        int delay = 250;
        progressUpdater = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                progressDialog.updateProgress((int) (100 * offlinePerf.getProgress()));
            }
        });
        progressUpdater.start();
        return offlinePerf.processLogFiles(startTime, endTime);
    }

    @Override
    protected void done() {
        Set<PerformanceStats> statsSeries;
        try {
            statsSeries = get();
            if (statsSeries != null) {
                if (offlinePerf.isFinished()) {
                    progressDialog.updateProgress(100);
                    progressDialog.dispose();
                    parentApp.showInfo("Finished!"
                            + " (" + offlinePerf.totalProcessedCount + " entries processed. "
                            + " Elapsed time: " + offlinePerf.processingTime / 1000 + " seconds.)");
                    parentApp.loadForSinkLogFile(statsSeries, offlinePerf.inputLogFilesPaths, startTime, endTime);
                    if (progressUpdater != null) {
                        progressUpdater.stop();
                        progressUpdater = null;
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (progressUpdater != null) {
                progressUpdater.stop();
                progressUpdater = null;
            }

            offlinePerf = null;
        }
    }
}
