package pt.uc.dei.fincos.random;

import java.util.Random;

/**
 * Class used for data generation.
 * Generates numbers following a Normal (Gaussian) distribution.
 * 
 * @author Marcelo R.N. Mendes
 *
 */
public class RandomNormalVariate extends Variate{
	private static final long serialVersionUID = -2212145157310217993L;
	
	private double mean;
	private double stdev;
	
	
	/**
	 * Class Constructor.
	 * Standard Normal Distribution (0 mean, 1 stdev)
	 * 
	 * @param seed	seed for random number generation
	 */
	public RandomNormalVariate(long seed){
		this(seed, 0, 1);
	}
	
	/**
	 * Class Constructor.
	 * 
	 * @param seed		seed for random number generation
	 * @param mean		Mean of Normal Distribution
	 * @param stdev		Standard Deviation of Normal Distribution
	 */
	public RandomNormalVariate(Long seed, double mean, double stdev){
		if(seed != null)
			this.rnd = new Random(seed);
		else
			this.rnd = new Random();
		this.mean = mean;
		this.setStdev(stdev);
	}	

	
	@Override
	public double generate() {
		return (mean+stdev*rnd.nextGaussian());
	}

	private void setStdev(double stdev) {
		if(stdev >= 0)
			this.stdev = stdev;
		else {
			System.err.println("Invalid value for standard deviation.");
		}
	}
	
	public double getMean() {
		return mean;
	}
	
	public double getStdev() {
		return stdev;
	}
}
