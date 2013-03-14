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
 * Generates numbers uniformly distributed over a given range.
 *
 * @author  Marcelo R.N. Mendes
 *
 */
public final class RandomUniformVariate extends Variate {

    /** serial id. */
    private static final long serialVersionUID = 7894780994769365256L;

    /** The lower boundary of this random uniform variate. */
    private double lower;

    /** The upper boundary of this random uniform variate. */
    private double upper;

    /**
     * Class Constructor.
     *
     * @param seed    seed for random number generation
     * @param lower   the lower bound for this uniform distribution (inclusive)
     * @param upper   the lower bound for this uniform distribution (inclusive)
     */
    public RandomUniformVariate(Long seed, double lower, double upper) {
        super(seed);
        this.lower = lower;
        this.upper = upper;
    }

    @Override
    public double generate() {
        return  (lower + rnd.nextDouble() * (upper - lower));
    }

    /**
     *
     * @return  the lower boundary of this random uniform variate
     */
    public double getLower() {
        return lower;
    }

    /**
     *
     * @return  the upper boundary of this random uniform variate
     */
    public double getUpper() {
        return upper;
    }
}
