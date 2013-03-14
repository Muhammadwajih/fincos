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

import java.awt.Color;
import java.awt.Dimension;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;

/**
 * A panel containing a JFreechart chart and accessor methods.
 *
 * @author  Marcelo R.N. Mendes
 *
 */
public class PerfChartPanel extends JPanel {
    /** serial id .*/
    private static final long serialVersionUID = -8158388572557517686L;

    private HashMap<String, TimeSeries> seriesList;
    private HashMap<String, Double> seriesScales;
    private TimeSeriesCollection dataset;
    private int source;
    static final int REAL_TIME = 0;
    static final int LOG_FILE = 1;

    NumberAxis yAxis;
    XYPlot plot;
    int chartAge;

    /**
     *
     * @param chartTitle    The title of the chart
     * @param seriesList    An initial set of series to be added to chart
     * @param yAxisName     Name of y axis
     * @param yAxisFormat   Number format of y axis
     * @param source        Either real time(0) or log file (1)
     */
    public PerfChartPanel(String chartTitle, String[] seriesList,
            String yAxisName, DecimalFormat yAxisFormat, int source) {

        this.source = source;
        dataset = new TimeSeriesCollection();

        this.seriesList = new HashMap<String, TimeSeries>();
        this.seriesScales = new HashMap<String, Double>();

        if (seriesList != null) {
            for (String seriesName : seriesList) {
                addSeries(seriesName, 1);
            }
        }

        JFreeChart chart = ChartFactory.createTimeSeriesChart(null, "Time", yAxisName, dataset, false, true, true);
        chart.setTitle(chartTitle);

        plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.DARK_GRAY);
        plot.setRangeGridlinePaint(Color.DARK_GRAY);

        yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setAutoRange(false);

        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);
        yAxis.setTickUnit(new NumberTickUnit(10));
        yAxis.setNumberFormatOverride(yAxisFormat);

        if (this.source == REAL_TIME) {
            DateAxis xAxis = (DateAxis) plot.getDomainAxis();

            xAxis.setTickUnit(new DateTickUnit(DateTickUnit.SECOND, 10,
                    DateTickUnit.SECOND, 10,
                    DateFormat.getTimeInstance()
            ));
        }

        ChartPanel chartPanel = new ChartPanel(chart);
        chart.setBackgroundPaint(chartPanel.getBackground());
        chartPanel.setPreferredSize(new Dimension(650, 420));

        add(chartPanel);
    }

    public void clear() {
        seriesList.clear();
        seriesScales.clear();
        dataset.removeAllSeries();
    }

    public TimeSeries addSeries(String seriesName, double seriesScale) {
        TimeSeries timeSeries = new TimeSeries(seriesName, Millisecond.class);
        if (this.source == REAL_TIME) {
            timeSeries.setMaximumItemAge(60);
        }
        this.seriesList.put(seriesName, timeSeries);
        this.seriesScales.put(seriesName, seriesScale);
        dataset.addSeries(timeSeries);

        return timeSeries;
    }

    public TimeSeries addSeries(String seriesName, Color seriesColor, double seriesScale) {
        TimeSeries timeSeries = addSeries(seriesName, seriesScale);
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            if (timeSeries.equals(dataset.getSeries(i))) {
                changeSeriesColor(i, seriesColor);
                break;
            }
        }
        return timeSeries;
    }

    public void removeSeries(Object seriesName) {
        TimeSeries toRemove = this.seriesList.get(seriesName);
        int toRemoveIndex = dataset.indexOf(toRemove);

        for (int i = toRemoveIndex; i < dataset.getSeriesCount() - 1; i++) {
            changeSeriesColor(i, getSeriesColor(i + 1));
        }

        dataset.removeSeries(toRemoveIndex);
        this.seriesList.remove(seriesName);
        this.seriesScales.remove(seriesName);
    }

    public void setYAxisRange(double min, double max) {
        yAxis.setLowerBound(min);
        yAxis.setUpperBound(max);
    }

    /**
     * Adds a new data point to the series or updates an existing one.
     *
     * @param seriesName    The series to be updated
     * @param timestamp     The point in the X axis
     * @param perfValue     The new value in the Y axis
     */
    public void updateChart(String seriesName, long timestamp, double perfValue) {
        TimeSeries timeSeries = this.seriesList.get(seriesName);
        Double scale = this.seriesScales.get(seriesName);
        if (timeSeries != null && scale != null) {
            timeSeries.addOrUpdate(new Second(new Date(timestamp)), perfValue * scale);
        }
    }

    public void updateChart(TimeSeries series, long timestamp, double perfValue) {
        Double scale = this.seriesScales.get(series.getKey());
        if (scale != null) {
            series.addOrUpdate(new Second(new Date(timestamp)), perfValue * scale);
        } else {
            System.err.println("Could not update " + series.getKey());
        }
    }

    public void changeSeriesScale(String seriesName, double scale) {
        TimeSeries timeSeries = this.seriesList.get(seriesName);
        Double oldScale = this.seriesScales.get(seriesName);
        if (timeSeries != null && oldScale != null) {
            //updates scale
            this.seriesScales.put(seriesName, scale);
            // updates old itens
            TimeSeriesDataItem item;
            for (Object objItem : timeSeries.getItems()) {
                item = (TimeSeriesDataItem) objItem;
                timeSeries.addOrUpdate(item.getPeriod(), item.getValue().doubleValue() * scale / oldScale);
            }
        }
    }

    public void changeSeriesColor(int seriesIndex, Color newColor) {
        XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesPaint(seriesIndex, newColor);
    }

    private Color getSeriesColor(int seriesIndex) {
        XYItemRenderer renderer = plot.getRenderer();
        return (Color) renderer.getSeriesPaint(seriesIndex);
    }

    /**
     * Defines if a series must be drawn or not.
     *
     * @param seriesIndex   The index of the series
     * @param visible       Visible (true), Hidden (false)
     */
    public void setSeriesVisible(int seriesIndex, boolean visible) {
        plot.getRenderer().setSeriesVisible(seriesIndex, visible);
    }
}
