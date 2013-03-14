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


package pt.uc.dei.fincos.perfmon;

import java.util.TreeMap;

/**
 * Class representing a equi-width histogram.
 *
 * @author  Marcelo R.N. Mendes
 *
 */
public final class Histogram {
    /** Minimum value for the x-axis of the histogram. */
    private double min;

    /** Maximum value for the x-axis of the histogram. */
    private double max;

    /** Width of the histogram's bins. */
    private double binWidth;

    /** Number of bins in the histogram. */
    private int binCount;

    /** Histogram data. */
    private long[] histogram;

    /** Flag indicating if the histogram is cumulative. */
    private boolean cumulative;

    /** Number of entries in the histogram. */
    private long itemCount;

    /**
     *
     * @param binWidth		Size of the buckets
     * @param min			Min value of the histogram
     *                      (smaller values will be part of the first bucket)
     * @param max			Max value of the histogram
     *                      (greater values will be part of the last bucket)
     * @param cumulative	Indicates if frequencies of the histogram are cumulative or not
     */
    public Histogram(double binWidth, double min, double max, boolean cumulative) {
        this.binWidth = binWidth;
        this.binCount = (int) Math.round((max - min) / binWidth) + 1;
        this.min = min;
        this.max = max;
        this.cumulative = cumulative;
        this.clear();
    }

    /**
     *
     * @param bucketCount	Number of bins
     * @param min			Min value of the histogram
     *                      (smaller values will be part of the first bucket)
     * @param max			Max value of the histogram
     *                      (greater values will be part of the last bucket)
     * @param cumulative	Indicates if frequencies of the histogram are cumulative or not
     */
    public Histogram(int bucketCount, double min, double max, boolean cumulative) {
        this.binCount = bucketCount;
        this.binWidth = (max - min + 1) / bucketCount;
        this.min = min;
        this.max = max;
        this.cumulative = cumulative;
        this.clear();
    }

    /**
     * Resets the histogram.
     */
    private void clear() {
        histogram = new long[binCount];
        itemCount = 0;
    }

    /**
     * Places the item passed as argument in the appropriate bin in the histogram.
     *
     * @param item     the item to be added
     */
    public void addItem(double item) {
        itemCount++;

        int index;
        index = (int) Math.floor((item - min) / binWidth);

        if (index < 0) {
            return;
        } else if (index >= histogram.length) { // Add items that fall beyond the specified max to the last bucket
            index = histogram.length - 1;
        }

        histogram[index]++;
    }

    /**
     * Builds a histogram represented as an ordered Map structure.
     * Keys are the lower boundary of the bins and the values are
     * the corresponding frequencies.
     *
     * @return  the histogram
     */
    public TreeMap<Double, Double> getHistogram() {
        TreeMap<Double, Double> ret = new TreeMap<Double, Double>();

        for (int i = 0; i < histogram.length; i++) {
            if (!cumulative || i == 0) {
                ret.put(min + (i) * binWidth, 1.0 * histogram[i] / itemCount);
            } else {
                ret.put(min + (i) * binWidth,
                        ret.get(min + (i - 1) * binWidth) + 1.0 * histogram[i] / itemCount);
            }
        }

        return ret;
    }

    /**
     *
     * @return the minimum value for the x-axis of the histogram
     */
    public double getMin() {
        return min;
    }

    /**
     *
     * @return  the maximum value for the x-axis of the histogram
     */
    public double getMax() {
        return max;
    }

    /**
     *
     * @return  the width of the histogram's bins
     */
    public double getBinWidth() {
        return binWidth;
    }

    /**
     *
     * @return  the number of bins in the histogram
     */
    public int getBucketCount() {
        return binCount;
    }

}
