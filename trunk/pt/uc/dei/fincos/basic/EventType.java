package pt.uc.dei.fincos.basic;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * A class that represents the schema for event instances. 
 * Consists in a name and a set of attributes
 * 
 * @author Marcelo R.N. Mendes
 * 
 * @see		Attribute
 *
 */
public class EventType implements Serializable{
	private static final long serialVersionUID = 2785420172709613384L;
	
	private String name;
	private Attribute[] attributes;
	
	private int hashCode;
	
	public EventType(String name, Attribute[] attributes){ 
		this.setName(name);
		this.setAttributes(attributes);		
		this.hashCode = this.toString().hashCode();
	}
	
	
	private void setName(String name) {
		this.name = name;		
	}

	public String getName() {
		return this.name;
	}

	private void setAttributes(Attribute[] attributes) {
		LinkedHashSet<Attribute>  attSet = new LinkedHashSet<Attribute>(attributes.length);
		boolean duplicateAtt;
				
		for (int i = 0; i < attributes.length; i++) {
			duplicateAtt = !attSet.add(attributes[i]);
			if (duplicateAtt)
				System.err.println("Duplicate attribute: "+attributes[i].getName());		
		}
		
		this.attributes = new Attribute[attSet.size()];
		
		Iterator<Attribute> iter  = attSet.iterator();		
		for (int i = 0; iter.hasNext(); i++) {
			this.attributes[i] = iter.next();
		}	
	}

	public Attribute[] getAttributes() {
		return this.attributes;
	}
	
	
	public int getAttributeCount(){
		if(this.attributes != null)
			return attributes.length;
		else
			return 0;
	}
	
	public String[] getAttributesNames() {
		String ret[] = new String[this.attributes.length];
		
		for (int i = 0; i < this.attributes.length; i++) {
			ret[i] = attributes[i].getName();
		}
		
		return ret;
	}
	
	public String getAttributesNamesList() {
		String ret = "";
		
		for (int i = 0; i < this.attributes.length; i++) {
			ret += attributes[i].getName()+",";
		}
		
		return ret.substring(0, ret.length()-1);
	}
	
	public boolean equals(Object o) {
		EventType comp;		
		
		if (o instanceof EventType){
			comp = (EventType)o;
			if(this.name.equals(comp.name)) {
				if ( (this.attributes == null || comp.attributes == null) ||
						 (this.attributes.length != comp.attributes.length)
					   )
						return false;
					else {
						for (int i = 0; i < this.attributes.length; i++) {
							if(!this.attributes[i].equals(comp.attributes[i]))
								return false;
						}	
						return true;
					}
			}
			else {
				return false;
			}
		}
		else
			return false;
	}
	
	
	public int hashCode() {
		return this.hashCode;
	}
	
	@Override
	public String toString() {
		String ret = ":" + this.name + "[ ";
		
		for (int i = 0; i < this.attributes.length; i++) {
			ret+=this.attributes[i].getName()+":" + attributes[i].getType() + " ";
		}
		
		ret+="]";

		return ret;
		
	}
}
