package pt.uc.dei.fincos.perfmon.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.CategoryTableXYDataset;

import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.data.CSVReader;
import pt.uc.dei.fincos.perfmon.Histogram;

/**
 *
 * @author  Marcelo R. N. Mendes
 */
public class HistogramPanel extends JPanel {
    /** serial id. */
    private static final long serialVersionUID = -2702545159393767976L;

    protected double binWidth;
    private String[] logFilePath;
    private String stream;
    private long startTimestamp;
    private long endTimestamp;


    /**
     * Creates new form HistogramForm
     *
     * @param logFilesPath
     * @param streamName
     * @param startTimestamp
     * @param endTimestamp
     */
    public HistogramPanel(String logFilesPath[], String streamName,
            long startTimestamp, long endTimestamp) {
        this.logFilePath = logFilesPath;
        this.stream = streamName;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;

        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        bucketsRadio = new javax.swing.JRadioButton();
        binsRadio = new javax.swing.JRadioButton();
        binField = new javax.swing.JTextField();
        minField = new javax.swing.JTextField();
        maxField = new javax.swing.JTextField();
        bucketsField = new javax.swing.JTextField();
        histPanel = new javax.swing.JTabbedPane();
        tabularPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        chartPanel = new javax.swing.JPanel();
        cumulativeCheck = new javax.swing.JCheckBox();
        processBtn = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Histogram"));

        jLabel1.setText("Min");

        jLabel2.setText("Max");

        buttonGroup1.add(bucketsRadio);
        bucketsRadio.setText("# Buckets");

        buttonGroup1.add(binsRadio);
        binsRadio.setText("Bin Width");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout tabularPanelLayout = new javax.swing.GroupLayout(tabularPanel);
        tabularPanel.setLayout(tabularPanelLayout);
        tabularPanelLayout.setHorizontalGroup(
                tabularPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(tabularPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                        .addContainerGap())
        );
        tabularPanelLayout.setVerticalGroup(
                tabularPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(tabularPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                        .addContainerGap())
        );

        histPanel.addTab("Tabular", tabularPanel);

        histPanel.addTab("Chart", chartPanel);

        cumulativeCheck.setText("Cumulative");

        processBtn.setText("Process Log");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(histPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                                        .addContainerGap())
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel1)
                                                        .addComponent(jLabel2))
                                                        .addGap(7, 7, 7)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addComponent(maxField, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(minField, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addGap(40, 40, 40)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(bucketsRadio)
                                                                        .addComponent(binsRadio))
                                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(layout.createSequentialGroup()
                                                                                        .addComponent(bucketsField, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                        .addGap(18, 18, 18)
                                                                                        .addComponent(cumulativeCheck))
                                                                                        .addGroup(layout.createSequentialGroup()
                                                                                                .addComponent(binField, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addGap(18, 18, 18)
                                                                                                .addComponent(processBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                                                                .addGap(27, 27, 27))))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel1)
                                .addComponent(minField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(bucketsRadio)
                                .addComponent(bucketsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cumulativeCheck))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(maxField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(binsRadio)
                                        .addComponent(binField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(processBtn))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(histPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                                        .addContainerGap())
        );


        //-------------------------------- Custom Code --------------------------------
        processBtn.addActionListener(new ActionListener() {
            private DecimalFormat yAxisPercentFormat = new DecimalFormat("0.0%");
            @Override
            public void actionPerformed(ActionEvent e) {
                double min = Double.parseDouble(minField.getText());
                double max = Double.parseDouble(maxField.getText());
                boolean cumulative = cumulativeCheck.isSelected();
                Histogram hist;
                if (bucketsRadio.isSelected()) {
                    try {
                        int numBuckets = Integer.parseInt(bucketsField.getText());
                        hist = new Histogram(numBuckets, min, max, cumulative);
                        binWidth = (max - min) / numBuckets;
                    } catch (Exception e2) {
                        JOptionPane.showMessageDialog(null, "Invalid bucket size.");
                        return;
                    }
                } else {
                    try {
                        binWidth = Double.parseDouble(binField.getText());
                        hist = new Histogram(binWidth, min, max, cumulative);
                    } catch (Exception e2) {
                        JOptionPane.showMessageDialog(null, "Invalid bin width");
                        return;
                    }
                }

                try {
                    for (int i = 0; i < logFilePath.length; i++) {
                        CSVReader logReader = new CSVReader(logFilePath[i]);
                        // parses log header
                        String server;

                        String header = logReader.getNextLine();
                        if (header == null || !header.contains("FINCoS")
                                || (!header.contains("Driver")) && !(header.contains("Sink"))) {
                            System.err.println("Invalid log file.");
                            return;
                        }

                        // ignores next two lines of log header
                        logReader.getNextLine(); // Driver/Sink alias
                        logReader.getNextLine(); // Driver/Sink address

                        // Server address
                        server = logReader.getNextLine();

                        if (server != null) {
                            server = server.substring(server.indexOf(":") + 2);
                        }

                        // ignores next line of log header
                        logReader.getNextLine().length(); // Log Start time

                        // Determines Response time measurement mode of the test
                        String rtMode = logReader.getNextLine().substring(server.indexOf(":") + 2);
                        int rtMeasurementMode;
                        if (rtMode.contains("ADAPTER")) {
                            rtMeasurementMode = Globals.ADAPTER_RT;
                        } else if (rtMode.contains("END_TO_END")) {
                            rtMeasurementMode = Globals.END_TO_END_RT;
                        } else {
                            rtMeasurementMode = Globals.NO_RT;
                        }

                        String rtResStr = logReader.getNextLine().substring(server.indexOf(":") + 2);
                        double rtFactor = 1.0;
                        if (rtResStr.contains("milliseconds")) {
                            rtFactor = 1.0;
                        } else {
                            rtFactor = 1E6;
                        }

                        // ignores next line of log header (log sampling rate; not relevant here)
                        logReader.getNextLine();

                        String record = logReader.getNextLine();
                        String streamName;
                        String[] splitEv;
                        long outputArrivalTime = 0, causerEmissionTime = 0, timestamp;
                        while (record != null) {
                            splitEv = CSVReader.split(record, Globals.CSV_DELIMITER);
                            streamName = splitEv[1];
                            timestamp = Long.parseLong(splitEv[0]);
                            if (timestamp < startTimestamp) {
                                record = logReader.getNextLine();
                                continue;
                            }
                            if (timestamp > endTimestamp) {
                                break;
                            }
                            if (rtMeasurementMode != Globals.NO_RT && streamName.equals(stream)) {
                                    outputArrivalTime = Long.parseLong(splitEv[splitEv.length - 1]);
                                    causerEmissionTime = Long.parseLong(splitEv[splitEv.length - 2]);
                                hist.addItem((outputArrivalTime - causerEmissionTime) / rtFactor);
                            }
                            record = logReader.getNextLine();
                        }
                    }

                    jTextArea1.setText("Bin\tFrequency\n-------------------------------\n");

                    TreeMap<Double, Double> histResult =  hist.getHistogram();
                    for (Entry<Double, Double> ent :histResult.entrySet()) {
                        jTextArea1.append(Globals.FLOAT_FORMAT_3.format(ent.getKey())
                                + "\t" + Globals.FLOAT_FORMAT_3.format(100 * ent.getValue()) + "%\n");
                    }
                    jTextArea1.setCaretPosition(0);
                    PerfChartPanel chart = new PerfChartPanel("", "", yAxisPercentFormat);
                    chart.addSeries("Series 1", histResult);
                    chartPanel.removeAll();
                    chartPanel.add(chart);
                    chartPanel.repaint();
                    chartPanel.revalidate();
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(null, "Could not open source file. ", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        ItemListener itemListener = new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                bucketsField.setEnabled(bucketsRadio.isSelected());
                binField.setEnabled(!bucketsRadio.isSelected());
            }
        };
        bucketsRadio.addItemListener(itemListener);
        binsRadio.addItemListener(itemListener);

        bucketsRadio.setSelected(true);

        bucketsField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) { }

            @Override
            public void focusLost(FocusEvent e) {
                if (bucketsField.getText() != null && !bucketsField.getText().isEmpty())
                {
                    try {
                        int numOfBuckets = Integer.parseInt(bucketsField.getText());
                        double max = Double.parseDouble(maxField.getText());
                        double min = Double.parseDouble(minField.getText());
                        double binWidth = (max - min) / numOfBuckets;
                        binField.setText(Globals.FLOAT_FORMAT_3.format(binWidth));
                    } catch (Exception e2) {

                    }
                }
            }
        });

        binField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) { }

            @Override
            public void focusLost(FocusEvent e) {
                if (binField.getText() != null && !binField.getText().isEmpty()) {
                    try {
                        double binWidth  = Double.parseDouble(binField.getText());
                        double max = Double.parseDouble(maxField.getText());
                        double min = Double.parseDouble(minField.getText());
                        int numOfBuckets = (int) Math.round((max - min) / binWidth) + 1;
                        bucketsField.setText("" + numOfBuckets);
                    } catch (Exception e2) {
                        bucketsField.setText("ERR");
                    }
                }
            }
        });

        FocusListener lsnr = new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) { }

            @Override
            public void focusLost(FocusEvent e) {
                if (binField.isEnabled()) {
                    if (binField.getText() != null && !binField.getText().isEmpty()) {
                        try {
                            double binWidth  = Double.parseDouble(binField.getText());
                            double max = Double.parseDouble(maxField.getText());
                            double min = Double.parseDouble(minField.getText());
                            int numOfBuckets = (int) Math.round((max - min) / binWidth) + 1;
                            bucketsField.setText("" + numOfBuckets);
                        } catch (Exception e2) {
                            bucketsField.setText("ERR");
                        }
                    }
                }
                if(bucketsField.isEnabled()) {
                    if (bucketsField.getText() != null && !bucketsField.getText().isEmpty())
                    {
                        try {
                            int numOfBuckets = Integer.parseInt(bucketsField.getText());
                            double max = Double.parseDouble(maxField.getText());
                            double min = Double.parseDouble(minField.getText());
                            double binWidth = (max - min) / numOfBuckets;
                            binField.setText(Globals.FLOAT_FORMAT_3.format(binWidth));
                        } catch (Exception e2) {

                        }
                    }
                }
            }
        };
        maxField.addFocusListener(lsnr);
        minField.addFocusListener(lsnr);

        bucketsField.setText("100");
        minField.setText("0");
        maxField.setText("100.0");
    }// </editor-fold>


    // Variables declaration - do not modify
    private javax.swing.JTextField binField;
    private javax.swing.JRadioButton binsRadio;
    private javax.swing.JTextField bucketsField;
    private javax.swing.JRadioButton bucketsRadio;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel chartPanel;
    private javax.swing.JCheckBox cumulativeCheck;
    private javax.swing.JTabbedPane histPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField maxField;
    private javax.swing.JTextField minField;
    private javax.swing.JButton processBtn;
    private javax.swing.JPanel tabularPanel;
    // End of variables declaration


    class PerfChartPanel extends JPanel {
        /** serial id. */
        private static final long serialVersionUID = -5909998805654480256L;

        private CategoryTableXYDataset dataset;
        XYPlot plot;
        int chartAge;

        /**
         *
         * @param chartTitle	The title of the chart
         * @param yAxisName		Name of y axis
         * @param yAxisFormat	Number format of y axis
         */
        public PerfChartPanel(String chartTitle, String yAxisName,
                DecimalFormat yAxisFormat) {
            dataset = new  CategoryTableXYDataset();
            JFreeChart chart = ChartFactory.createHistogram(null, null, yAxisName, dataset, PlotOrientation.VERTICAL, false, true, true);
            chart.setTitle(chartTitle);
            plot = chart.getXYPlot();
            XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
            renderer.setShadowVisible(false);

            plot.setDomainGridlinePaint(Color.DARK_GRAY);
            plot.setRangeGridlinePaint(Color.DARK_GRAY);

            ((NumberAxis) plot.getRangeAxis()).setNumberFormatOverride(yAxisFormat);

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(400, 300));

            add(chartPanel);
        }

        public void addSeries(String seriesName, TreeMap<Double, Double> histogram) {
            int counter = 0;
            double maxFreq = 0;
            double maxValue = 0;
            Entry<Double, Double> prev = null;
            for (Entry<Double, Double> e : histogram.entrySet()) {
                if (counter < histogram.size() - 1) {
                    dataset.add(e.getKey(), e.getValue(), seriesName);
                    prev = e;
                } else {
                    maxValue = e.getKey();
                    maxFreq = e.getValue();
                    dataset.add(prev.getKey() + binWidth, maxFreq, seriesName);

                    if (maxValue > prev.getKey() + binWidth) {
                        XYPointerAnnotation maxAnnotation = new XYPointerAnnotation(
                                "\u2265" + Globals.FLOAT_FORMAT_2.format(prev.getKey()) + "\n",
                                prev.getKey() + binWidth,
                                Math.min(0.85 * plot.getRangeAxis().getUpperBound(), 1.01 * maxFreq),
                                -1.571);
                        plot.addAnnotation(maxAnnotation);
                    }
                }
                counter++;
                //dataset.addValue(e.getValue(), seriesName, e.getKey());
            }
            plot.getRangeAxis().setAutoRange(false);
            plot.getRangeAxis().setAutoRange(true);
        }
    }
}
