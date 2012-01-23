package pt.uc.dei.fincos.validation;

public class Stream {
	protected static final int INPUT = 0;
	protected static final int OUTPUT = 1;
	
	protected int type;
	protected String name;
	
	public Stream (int type, String name) {
		this.type = type;
		this.name = name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Stream) {
			Stream comp = (Stream) obj;		
			return this.type == comp.type && this.name.equals(comp.name);			
		}
		
		return false;
		
	}
	
	@Override
	public int hashCode() {		
		return (this.name+this.type).hashCode();
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
