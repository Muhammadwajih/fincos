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


package pt.uc.dei.fincos.controller;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.LinkedHashMap;

/**
 * Encapsulates the configuration of a connection with a CEP engine or a JMS provider.
 *
 * @author  Marcelo R.N. Mendes
 */
public final class ConnectionConfig implements Serializable, Cloneable {
    /** Serial id. */
    private static final long serialVersionUID = -1527657198530605646L;

    /** A unique identifier for this connection. */
    private final String alias;

    /** Either CEP_ADAPTER or JMS. */
    private final int type;

    /** Vendor-specific parameters used to establish the connection .*/
    private final LinkedHashMap<String, String> properties;

    /** Direct connection with CEP engine via custom-code Adapter.  */
    public static final int CEP_ADAPTER = 0;

    /** Communication via JMS. */
    public static final int JMS = 1;

    /**
     *
     * @param alias         a unique identifier for this connection
     * @param type          either CEP_ADAPTER or JMS
     * @param properties    vendor-specific parameters used to establish the connection
     */
    public ConnectionConfig(String alias, int type, LinkedHashMap<String, String> properties) {
        super();
        this.alias = alias;
        if (type != CEP_ADAPTER && type != JMS) {
            throw new InvalidParameterException("Invalid value for connection type.");
        }
        this.type = type;
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "ConnectionConfig [alias=" + getAlias() + ", "
                               + "type=" + getType() + ", "
                               + "properties=" + getProperties() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getAlias() == null) ? 0 : getAlias().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ConnectionConfig other = (ConnectionConfig) obj;
        if (getAlias() == null) {
            if (other.getAlias() != null) {
                return false;
            }
        } else if (!getAlias().equals(other.getAlias())) {
            return false;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ConnectionConfig clone() {
        return new ConnectionConfig(this.getAlias(), this.getType(),
                    (LinkedHashMap<String, String>) this.getProperties().clone());
    }

    /**
     *
     * @return  the unique identifier for this connection
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @return the type of this connection
     *         (either CEP_ADAPTER or JMS)
     */
    public int getType() {
        return type;
    }

    /**
     * @return the parameters used to establish this connection
     */
    public LinkedHashMap<String, String> getProperties() {
        return properties;
    }
}
