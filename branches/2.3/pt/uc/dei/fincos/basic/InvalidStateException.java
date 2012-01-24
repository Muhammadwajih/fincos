package pt.uc.dei.fincos.basic;

/**
 * Exception thrown when a remote method of either a Driver or a Sink is called
 * but it is not in the appropriate state for performing the expected action.
 * 
 * @author Marcelo R.N. Mendes
 *
 */
@SuppressWarnings("serial")
public class InvalidStateException extends Exception {
	public InvalidStateException(String message) {
		super(message);
	}
}
