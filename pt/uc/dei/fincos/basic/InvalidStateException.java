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


package pt.uc.dei.fincos.basic;

/**
 * Exception thrown when a remote method of either a Driver or a Sink is called
 * but it is not in the appropriate state for performing the expected action.
 *
 * @author  Marcelo R.N. Mendes
 *
 */
public class InvalidStateException extends Exception {
	/** serial id. */
    private static final long serialVersionUID = -171460482317539819L;

    /**
     * Constructs a new InvalidStateException with the specified detail message.
     *
     * @param message   the detail message.
     */
    public InvalidStateException(String message) {
		super(message);
	}
}
