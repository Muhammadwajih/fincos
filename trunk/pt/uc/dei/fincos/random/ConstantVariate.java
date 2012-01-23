package pt.uc.dei.fincos.random;

/**
 * Class used for data generation. Generates always the same value
 * 
 * @author Marcelo R.N. Mendes
 *
 */
public class ConstantVariate extends Variate {
	private static final long serialVersionUID = 5717720149376374436L;
	
	private double value;
	
	/**
	 * Class Constructor.
	 *  
	 * @param value		the constant value to be returned by the {@link #generate()} method
	 */
	public ConstantVariate(double value) {
		this.value = value;
	}
	
	
	@Override
	public double generate() {
		return this.value;
	}
	
	public double getValue() {
		return value;
	}

}
