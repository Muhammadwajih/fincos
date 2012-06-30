package pt.uc.dei.fincos.perfmon.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import pt.uc.dei.fincos.validation.OfflinePerformanceValidator;
import pt.uc.dei.fincos.validation.PerformanceMonitor;
import pt.uc.dei.fincos.validation.PerformanceStats;

/**
 * Processes Sink log files in background.
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
                if (offlinePerf.finished) {
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
            e.printStackTrace();
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
