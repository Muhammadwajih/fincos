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

import java.io.Serializable;
import java.util.Random;

/**
 * Abstract class used for generation of Random numbers.
 *
 * @author  Marcelo R.N. Mendes
 *
 * @see     ConstantVariate
 * @see     RandomExponentialVariate
 * @see     RandomNormalVariate
 * @see     RandomUniformVariate
 *
 */
public abstract class Variate implements Serializable {
    /** serial id. */
    private static final long serialVersionUID = 7132000136731568068L;

    /** Random number generator. */
    protected final Random rnd;


    /**
     * Initializes the random number generator of this Variate.
     *
     * @param seed  the initial seed
     */
    public Variate(Long seed) {
        if (seed != null) {
            this.rnd = new Random(seed);
        } else {
            this.rnd = new Random();
        }
    }

    /**
     * Generates a number based on its specific Distribution.
     *
     * @return  the generated number
     */
    public abstract double generate();

    /**
     * Sets the seed for random number generation.
     *
     * @param seed  the new seed
     */
    public final void setRandomSeed(Long seed) {
        if (this.rnd != null) {
            if (seed != null) {
                this.rnd.setSeed(seed);
            } else {
                this.rnd.setSeed(System.nanoTime());
            }
        }
    }
}
