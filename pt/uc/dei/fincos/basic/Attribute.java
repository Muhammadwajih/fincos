package pt.uc.dei.fincos.basic;

import java.io.Serializable;

/**
 * Basic class that represents an Attribute in an Event Type
 * 
 * @author Marcelo R.N. Mendes
 * 
 * @see EventType
 * @see Datatype
 * @see Domain
 *
 */
public class Attribute implements Serializable, Cloneable{
	private static final long serialVersionUID = -1795395382042692076L;
	
	private Datatype type;
	private String name;
	private Domain domain;
	
	public Attribute(Datatype type, String name) {
		this.setType(type);
		this.setName(name);	
	}
	
	public Attribute(Datatype type, String name, Domain domain) {
		this.setType(type);
		this.setName(name);	
		this.setDomain(domain);
	}

	private void setType(Datatype type) {
		this.type = type;
	}

	public Datatype getType() {
		return type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	private void setDomain(Domain domain) {
		this.domain = domain;
	}

	public Domain getDomain() {
		return domain;
	}
	
	public String toString() {
		return this.name + ":" + this.type;
	}
	
	public boolean equals(Object o) {
		Attribute comp;
		if (o instanceof Attribute) {
			comp = (Attribute)o;
			return this.name.equals(comp.name);
		}
		else return false;
					
	}
	
	public int hashCode() {
		return this.name.hashCode();
	}
	
	@Override
	public Object clone() {
		return new Attribute(this.type, this.name, this.domain);
	}
	
}


