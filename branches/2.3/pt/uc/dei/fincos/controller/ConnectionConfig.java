package pt.uc.dei.fincos.controller;

import java.security.InvalidParameterException;
import java.util.LinkedHashMap;

/**
 * Encapsulates the configuration of a connection with a CEP engine or a JMS provider.
 */
public class ConnectionConfig {
    /** A unique identifier for this connection. */
    public String alias;

    /** Either CEP_ADAPTER or JMS. */
    public int type;

    /** Vendor-specific parameters used to establish the connection .*/
    public LinkedHashMap<String, String> properties;

    public static final int CEP_ADAPTER = 0;
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
        return "ConnectionConfig [alias=" + alias + ", type=" + type + ", properties=" + properties + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConnectionConfig other = (ConnectionConfig) obj;
        if (alias == null) {
            if (other.alias != null)
                return false;
        } else if (!alias.equals(other.alias))
            return false;
        return true;
    }

}
