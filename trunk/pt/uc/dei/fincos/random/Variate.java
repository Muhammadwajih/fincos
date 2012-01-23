package pt.uc.dei.fincos.random;

import java.io.Serializable;
import java.util.Random;

/**
 * Abstract class used for generation of Random numbers
 * 
 * @author 	Marcelo R.N. Mendes
 * 
 * @see		ConstantVariate
 * @see		RandomExponentialVariate
 * @see		RandomNormalVariate
 * @see		RandomUniformVariate
 *
 */
public abstract class Variate implements Serializable{
	private static final long serialVersionUID = 7132000136731568068L;
	
	protected  Random rnd;
	
	/**
	 * Generates a number based on its specific Distribution
	 * 
	 * @return	the generated number
	 */
	public abstract double generate();
	
	/**
	 * Sets the seed for random number generation
	 * 
	 * @param seed
	 */
	public void setRandomSeed(Long seed){
		if(this.rnd != null) {
			if (seed != null)
				this.rnd.setSeed(seed);
			else 
				this.rnd = new Random();
		}			
	}
}
