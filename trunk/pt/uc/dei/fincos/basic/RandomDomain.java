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


package pt.uc.dei.fincos.basic;

import pt.uc.dei.fincos.random.Variate;

/**
 * A domain that generates purely random data following
 * a given Random Variate distribution.
 *
 * @author  Marcelo R.N. Mendes
 *
 * @see Domain
 * @see PredefinedListDomain
 * @see SequentialDomain
 * @see Variate
 *
 */
public final class RandomDomain extends Domain {
    /** serial id. */
    private static final long serialVersionUID = 7105802538322323500L;

    /** Random variate used for generating the domain's values. */
    private Variate variate;

    /**
     * Creates a new Random domain.
     *
     * @param variate   random variate used for generating the domain's values
     */
    public RandomDomain(Variate variate) {
        this.variate = variate;
    }

    @Override
    public Object generateValue() {
        // No need for synchronization here:
        //   generate() methods of all subclasses of Variate are thread-safe
        return this.variate.generate();
    }

    /**
     *
     * @return  the random variate used for generating this domain's values
     */
    public Variate getVariate() {
        return variate;
    }

    @Override
    public String toString() {
        return "Random";
    }

    @Override
    public void setRandomSeed(Long seed) {
        this.variate.setRandomSeed(seed);
    }
}
