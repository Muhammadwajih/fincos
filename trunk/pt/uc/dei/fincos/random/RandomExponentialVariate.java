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
 * Generates numbers exponentially distributed (using inverse transformation).
 * Useful for representing the interarrival times of a Poisson Process.
 *
 * @author  Marcelo R.N. Mendes
 *
 */
public final class RandomExponentialVariate extends Variate {
    /** serial id. */
    private static final long serialVersionUID = 4845237589459748273L;

    /** The lambda parameter of this exponential variate. */
    private final double lambda;

    /**
     * Creates a new exponential variate.
     *
     * @param seed          seed for random number generation
     * @param lambda        the rate of requisitions of a Poisson process
     *                      (inverse of mean)
     * @throws Exception    if the value passed for lambda is negative
     */
    public RandomExponentialVariate(Long seed, double lambda)
    throws Exception {
        super(seed);
        if (lambda > 0) {
            this.lambda = lambda;
        } else {
            throw new Exception("Invalid parameter for exponential distribution"
                              + " (non-positive lambda).");
        }
    }

    @Override
    public double generate() {
        return (-(1 / lambda) * (Math.log(rnd.nextDouble())));
    }

    /**
     *
     * @return the lambda parameter of this exponential variate
     */
    public double getLambda() {
        return lambda;
    }

}
