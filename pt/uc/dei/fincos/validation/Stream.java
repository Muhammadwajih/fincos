package pt.uc.dei.fincos.validation;

public class Stream {
	public static final int INPUT = 0;
	public static final int OUTPUT = 1;

	/** Either INPUT or OUTPUT. */
	public final int type;

	/** The identifier of the stream. */
	public final String name;

	/**
	 *
	 * @param type the type of the stream (either INPUT or OUTPUT)
	 * @param name the identifier of the stream
	 */
	public Stream(int type, String name) {
		this.type = type;
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Stream) {
			Stream comp = (Stream) obj;
			return this.type == comp.type && this.name.equals(comp.name);
		}

		return false;

	}

	@Override
	public int hashCode() {
		return (this.name + this.type).hashCode();
	}

	@Override
	public String toString() {
		switch (this.type) {
		case INPUT:
			return this.name + "(input)";
		case OUTPUT:
			return this.name + "(output)";
		default:
			return this.name;
		}

	}

}
