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
 * A domain that generates sequential data. An initial value and an increment
 * are specified. Both the initial value and the increment are instances of the
 * <tt>Variate</tt> class.
 *
 * @author  Marcelo R.N. Mendes
 *
 * @see Domain
 * @see PredefinedListDomain
 * @see RandomDomain
 * @see Variate
 *
 */
public final class SequentialDomain extends Domain {
    /** serial id. */
    private static final long serialVersionUID = -6565555446467539524L;

    /** Keeps track of the previously generated value. */
    private double previousValue;

    /** A random variate used for generating the initial value for this domain. */
    private Variate initialVariate;

    /** A random variate used for generating the increment for this domain. */
    private Variate incrementVariate;

    /**
     * Creates a new Sequential domain.
     *
     * @param initialValueVariate   A random variate used for generating the
     *                              initial value for this domain.
     * @param incrementVariate      A random variate used for generating the
     *                              increment for this domain.
     */
    public SequentialDomain(Variate initialValueVariate,
            Variate incrementVariate) {
        this.previousValue = initialValueVariate.generate();
        this.initialVariate = initialValueVariate;
        this.incrementVariate = incrementVariate;
    }


    @Override
    public Object generateValue() {
        double incrementValue = this.incrementVariate.generate();
        synchronized (this) {
            double value = this.previousValue + incrementValue;
            this.previousValue = value;
            return value;
        }
    }

    /**
     *
     * @return  the random variate used for generating the
     *          initial value for this domain
     */
    public Variate getInitialVariate() {
        return initialVariate;
    }

    /**
     *
     * @return  the random variate used for generating the
     *          increment for this domain
     */
    public Variate getIncrementVariate() {
        return incrementVariate;
    }

    @Override
    public String toString() {
        return "Sequential";
    }

    @Override
    public void setRandomSeed(final Long seed) {
        this.initialVariate.setRandomSeed(seed);
        this.incrementVariate.setRandomSeed(seed);
    }
}
