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
 * Basic class that represents an Attribute in an Event Type.
 *
 * @author  Marcelo R.N. Mendes
 *
 * @see EventType
 * @see Datatype
 * @see Domain
 *
 */
public final class Attribute implements Serializable, Cloneable {
    /** serial id. */
    private static final long serialVersionUID = -1795395382042692076L;

    /** The datatype of this attribute. */
    private final Datatype type;

    /** A unique identifier for this attribute. */
    private String name;

    /** The domain of values assumed by this attribute. */
    private final Domain domain;

    /** Cached hash code. */
    private int hashCode;

    /**
     * Creates a new attribute.
     *
     * @param type  The datatype of this attribute.
     * @param name  A unique identifier for this attribute.
     */
    public Attribute(Datatype type, String name) {
        this(type, name, null);
    }

    /**
     * Creates a new attribute.
     *
     * @param type      The datatype of this attribute.
     * @param name      A unique identifier for this attribute.
     * @param domain    The domain of values assumed by this attribute.
     */
    public Attribute(Datatype type, String name, Domain domain) {
        this.type = type;
        this.name = name;
        this.domain = domain;
        this.hashCode = this.name.hashCode();
    }

    /**
     *
     * @return  the datatype of this attribute.
     */
    public Datatype getType() {
        return type;
    }

    /**
     *
     * @return the name of this attribute
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this attribute.
     *
     * @param name  the new name
     */
    public void setName(String name) {
        this.name = name;
        this.hashCode = this.name.hashCode();
    }

    /**
     *
     * @return  the domain of values assumed by this attribute
     */
    public Domain getDomain() {
        return domain;
    }

    @Override
    public String toString() {
        return this.name + ":" + this.type;
    }

    @Override
    public boolean equals(Object o) {
        Attribute comp;
        if (o instanceof Attribute) {
            comp = (Attribute) o;
            return this.name.equals(comp.name);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public Attribute clone() {
        return new Attribute(this.type, this.name, this.domain);
    }
}


