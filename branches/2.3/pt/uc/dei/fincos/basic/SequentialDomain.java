package pt.uc.dei.fincos.basic;

import pt.uc.dei.fincos.random.Variate;

/**
 * A domain that generates sequential data. 
 * An initial value and an increment are specified.
 * Both the initial value and the increment are instances of the
 * <tt>Variate</tt> class.
 * 
 * @author Marcelo R.N. Mendes
 *
 * @see Domain
 * @see PredefinedListDomain
 * @see RandomDomain
 * @see Variate
 * 
 */
public class SequentialDomain extends Domain {
	private static final long serialVersionUID = -6565555446467539524L;
	
	private double previousValue;
	private Variate initialVariate;
	private Variate incrementVariate;
	
	public SequentialDomain(Variate initialValueVariate, Variate incrementVariate)
	{
		this.previousValue = initialValueVariate.generate();
		this.initialVariate = initialValueVariate;
		this.incrementVariate = incrementVariate;
	}
	
	public SequentialDomain(String name, Variate initialValueVariate, Variate incrementVariate)
	{
		this.previousValue = initialValueVariate.generate();
		this.initialVariate = initialValueVariate;
		this.incrementVariate = incrementVariate;
	}
	

	public Object generateValue() {
		double incrementValue = this.incrementVariate.generate();
		synchronized (this) { // Protects state variable "previousValue" from concurrent access
			double value = this.previousValue+incrementValue;
			this.previousValue = value;
			return value;	
		}		
	}
	
	public Variate getInitialVariate() {
		return initialVariate;
	}
	
	public Variate getIncrementVariate() {
		return incrementVariate;
	}
	
	@Override
	public String toString() {	
		return "Sequential";
	}

	@Override
	public void setRandomSeed(Long seed) {		
		this.initialVariate.setRandomSeed(seed);
		this.incrementVariate.setRandomSeed(seed);
	}
}
