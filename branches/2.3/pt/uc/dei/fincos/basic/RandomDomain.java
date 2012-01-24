package pt.uc.dei.fincos.basic;

import pt.uc.dei.fincos.random.Variate;

/**
 * A domain that generates purely random data following a given
 * Random Variate distribution
 * 
 * @author Marcelo R.N. Mendes
 *
 * @see Domain
 * @see PredefinedListDomain
 * @see SequentialDomain
 * @see Variate
 * 
 */
public class RandomDomain extends Domain {
	private static final long serialVersionUID = 7105802538322323500L;
	
	Variate variate;
	
	public RandomDomain(Variate variate) {
		this.variate = variate;
	}
	
	@Override
	public Object generateValue() {
		// No need for synchronization here: 
		//   generate() methods of all subclasses of Variate are thread-safe
		return this.variate.generate();
	}

	public Variate getVariate() {
		return variate;
	}
	
	@Override
	public String toString() {	
		return "Random";
	}

	@Override
	public void setRandomSeed(Long seed) {		
		this.variate.setRandomSeed(seed);		
	}
}
