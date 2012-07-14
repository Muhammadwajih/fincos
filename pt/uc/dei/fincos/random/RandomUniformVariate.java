package pt.uc.dei.fincos.random;
import java.util.Random;

/**
 * Class used for data generation.
 * Generates numbers uniformly distributed over a given range.
 *
 * @author Marcelo R.N. Mendes
 *
 */
public class RandomUniformVariate extends Variate {
	private static final long serialVersionUID = 7525133407325119395L;

	private double lower;
	private double upper;

	/**
	 * Class Constructor.
	 *
	 * @param seed		seed for random number generation
	 * @param lower		the lower bound for the uniform distribution range (inclusive)
	 * @param upper		the lower bound for the uniform distribution range (inclusive)
	 */
	public RandomUniformVariate(Long seed, double lower, double upper) {
		if(seed != null)
			rnd = new Random(seed);
		else
			rnd = new Random();
		this.lower = lower;
		this.upper = upper;
	}

	@Override
	public double generate() {
		return  (lower + rnd.nextDouble()*(upper-lower));
	}

	public double getLower() {
		return lower;
	}

	public double getUpper() {
		return upper;
	}
}
