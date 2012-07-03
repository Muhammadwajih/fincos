package pt.uc.dei.fincos.perfmon.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

import pt.uc.dei.fincos.perfmon.OfflinePerformanceValidator;

/**
 * Dialog showing progress of log processing.
 */
public class LogProcessProgressDialog extends JDialog {

    OfflinePerformanceValidator perfMeasurer;

    /**
     *
     * @param parent
     */
    public LogProcessProgressDialog(java.awt.Frame parent, OfflinePerformanceValidator perfMeasurer) {
        super(parent, false);
        this.perfMeasurer = perfMeasurer;
        initComponents();
        setLocationRelativeTo(null);
        addListeners();
    }

    private void initComponents() {

        cancelBtn = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Processing log file(s)...");

        cancelBtn.setText("Cancel");
        cancelBtn.setPreferredSize(new java.awt.Dimension(75, 25));

        progressBar.setPreferredSize(new java.awt.Dimension(146, 18));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        pack();
    }

    private void addListeners() {
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                perfMeasurer.stopProcessing();
                dispose();
            }
        });
    }

    /**
     * Sets the progress of the log processing.
     *
     * @param value a value between 0 and 100
     */
    public void updateProgress(int value) {
        this.progressBar.setValue(value);
    }

    // Variables declaration - do not modify
    private javax.swing.JButton cancelBtn;
    private javax.swing.JProgressBar progressBar;
    // End of variables declaration
}
