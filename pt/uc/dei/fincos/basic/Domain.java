package pt.uc.dei.fincos.basic;

import java.io.Serializable;

/**
 * 
 * Abstract class that encapsulates the characteristics of a given data item.
 * Used by the data generation mechanism to generate appropriate values for 
 * each attribute of an event type.
 * 
 * @author Marcelo R.N. Mendes
 *
 * @see PredefinedListDomain
 * @see RandomDomain
 * @see SequentialDomain
 * 
 */
public abstract class Domain implements Serializable{
	private static final long serialVersionUID = -3537819512665229025L;
	
	/**
	 * Polymorphic method used to generate data. Each subclass of
	 * <tt>Domain</tt> implements this method according to its
	 * expected behavior
	 * 
	 * @return		A data value.
	 */	
	public abstract Object generateValue();
	
	/**
	 * Sets seed for random number generation
	 * 
	 * @param seed
	 */
	public abstract void setRandomSeed(Long seed);

}
