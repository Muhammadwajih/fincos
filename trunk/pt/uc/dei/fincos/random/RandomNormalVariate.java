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


package pt.uc.dei.fincos.random;



/**
 * Class used for data generation.
 * Generates numbers following a Normal (Gaussian) distribution.
 *
 * @author  Marcelo R.N. Mendes
 *
 */
public final class RandomNormalVariate extends Variate {
    /** serial id. */
    private static final long serialVersionUID = -2212145157310217993L;

    /** The mean of this normal variate. */
    private final double mean;

    /** The standard deviation of this normal variate. */
    private final double stdev;


    /**
     * Class Constructor.
     * Standard Normal Distribution (0 mean, 1 stdev)
     *
     * @param seed  seed for random number generation
     */
    public RandomNormalVariate(long seed) {
        this(seed, 0, 1);
    }

    /**
     * Class Constructor.
     *
     * @param seed         seed for random number generation
     * @param mean         mean of Normal distribution
     * @param stdev        standard deviation of Normal distribution
     *
     */
    public RandomNormalVariate(Long seed, double mean, double stdev) {
        super(seed);
        this.mean = mean;
        if (stdev >= 0) {
            this.stdev = stdev;
        } else {
            throw new IllegalArgumentException("Invalid value for standard deviation.");
        }
    }


    @Override
    public double generate() {
        return (mean + stdev * rnd.nextGaussian());
    }

    /**
     *
     * @return  the mean of this normal variate
     */
    public double getMean() {
        return mean;
    }

    /**
     *
     * @return  the standard deviation of this normal variate
     */
    public double getStdev() {
        return stdev;
    }
}
