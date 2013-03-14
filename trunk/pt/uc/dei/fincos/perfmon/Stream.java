/* FINCoS Framework
 * Copyright (C) 2013 CISUC, University of Coimbra
 *
 * Licensed under the terms of The GNU General Public License, Version 2.
 * A copy of the License has been included with this distribution in the
 * fincos-license.txt file.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 */


package pt.uc.dei.fincos.perfmon;

/**
 * A class representing an input/output stream exercised during performance run.
 *
 * @author  Marcelo R.N. Mendes
 */
public final class Stream {

    /** Input stream. */
	public static final int INPUT = 0;

	/** Output stream. */
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
