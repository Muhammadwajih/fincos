package pt.uc.dei.fincos.basic;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * A class that represents the schema for event instances, consisting
 * in a name and a set of attributes.
 *
 * @author Marcelo R.N. Mendes
 *
 * @see Attribute
 *
 */
public class EventType implements Serializable {

    /** serial id. */
    private static final long serialVersionUID = 2785420172709613384L;

    /** A unique name for this type. */
    private final String name;

    /** The list of attributes of this type. */
    private final Attribute[] attributes;

    /** Cached hash code. */
    private final int hashCode;

    /** Indexes of attributes. */
    private final LinkedHashMap<Attribute, Integer> attIndex;

    /**
     * Creates a new type with the name and attributes passed as argument.
     *
     * @param name         Type's unique name
     * @param attributes   Type's attribute
     */
    public EventType(String name, Attribute[] attributes) {
        // Sets type's name
        this.name = name;

        // Sets type's attributes
        LinkedHashSet<Attribute>  attSet = new LinkedHashSet<Attribute>(attributes.length);
        boolean duplicateAtt;
        for (int i = 0; i < attributes.length; i++) {
            duplicateAtt = !attSet.add(attributes[i]);
            if (duplicateAtt) {
                System.err.println("WARN: Duplicate attribute \""
                        + attributes[i].getName() + "\" will be ignored.");
            }
        }
        this.attributes = new Attribute[attSet.size()];
        this.attIndex = new LinkedHashMap<Attribute, Integer>();
        Iterator<Attribute> iter  = attSet.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            this.attributes[i] = iter.next();
            this.attIndex.put(attributes[i], i);
        }

        // Computes and caches the hash code for this type
        this.hashCode = computeHashCode();
    }

    /**
     *
     * @return the name of this type.
     */
    public String getName() {
        return this.name;
    }

    /**
     *
     * @return an array containing the attributes of this type.
     */
    public Attribute[] getAttributes() {
        return this.attributes;
    }

    /**
    *
    * @return the i-th attribute of this type.
    */
   public Attribute getAttribute(int i) {
       return this.attributes[i];
   }

    /**
     *
     * @return  the number of attributes of this type.
     */
    public int getAttributeCount() {
            return attributes.length;
    }

    /**
     *
     * @return  an array containing the names of the attributes of this type.
     */
    public String[] getAttributesNames() {
        String[] ret = new String[this.attributes.length];
        for (int i = 0; i < this.attributes.length; i++) {
            ret[i] = attributes[i].getName();
        }
        return ret;
    }

    /**
     * Retrieves the index of a given attribute in this type.
     *
     * @param att   the attribute
     * @return      the index of the attribute
     */
    public int indexOf(Attribute att) {
        return this.attIndex.get(att);
    }

    /**
     *
     * @return a comma-separated list with the names of the attributes of this type.
     */
    public String getAttributesNamesList() {
        String ret = "";

        for (int i = 0; i < this.attributes.length; i++) {
            ret += attributes[i].getName() + ",";
        }

        return ret.substring(0, ret.length() - 1);
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
        EventType other = (EventType) obj;
        if (!Arrays.equals(attributes, other.attributes)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    private int computeHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(attributes);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(":");
        sb.append(this.name);
        sb.append("[ ");

        for (int i = 0; i < this.attributes.length; i++) {
            sb.append(this.attributes[i].getName());
            sb.append(":");
            sb.append(attributes[i].getType());
            sb.append(" ");
        }

        sb.append("]");

        return sb.toString();
    }
}
