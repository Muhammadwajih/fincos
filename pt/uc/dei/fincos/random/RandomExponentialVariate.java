package pt.uc.dei.fincos.random;

import java.util.Random;

/**
 * Class used for data generation.
 * Generates numbers exponentially distributed (using inverse transformation). 
 * Useful for representing the interarrival times of a Poisson Process.
 * 
 * @author Marcelo R.N. Mendes
 *
 */
public class RandomExponentialVariate extends Variate {
	private static final long serialVersionUID = 4845237589459748273L;
	
	private double lambda;
	
	/**
	 * Class Constructor.
	 * 
	 * @param seed		seed for random number generation
	 * @param lambda	The rate of requisitions of a Poisson process (inverse of mean)
	 * @throws Exception 
	 */
	public RandomExponentialVariate(Long seed, double lambda) throws Exception {
		if(seed != null)
			this.rnd = new Random(seed);
		else
			this.rnd = new Random();
		this.setLambda(lambda);
	}
	
	@Override
	public double generate() {
		return (-(1/lambda)*(Math.log(rnd.nextDouble())));
	}

	private void setLambda(double lambda) throws Exception {
		if(lambda > 0)
			this.lambda = lambda;
		else {
			throw new Exception("Invalid parameter for exponential distribution (non-positive lambda).");			
		}
			
	}
	
	public double getLambda() {
		return lambda;
	}

}
