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

import java.io.Serializable;

/**
 *
 * Abstract class that encapsulates the characteristics of a given data item.
 * Used by the data generation mechanism to generate appropriate values for
 * each attribute of an event type.
 *
 * @author  Marcelo R.N. Mendes
 *
 * @see PredefinedListDomain
 * @see RandomDomain
 * @see SequentialDomain
 *
 */
public abstract class Domain implements Serializable {
    /** Serial id. */
    private static final long serialVersionUID = -3537819512665229025L;

    /**
     * Polymorphic method used to generate data. Each subclass of
     * <tt>Domain</tt> implements this method according to its
     * expected behavior
     *
     * @return      A data value.
     */
    public abstract Object generateValue();

    /**
     * Sets seed for random number generation.
     *
     * @param seed     the random number generation seed
     */
    public abstract void setRandomSeed(Long seed);

}
