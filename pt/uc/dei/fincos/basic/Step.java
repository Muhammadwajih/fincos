/* FINCoS Framework
 * Copyright (C) 2012 CISUC, University of Coimbra
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
 * The possible states that a component of the framework can be in
 * a given moment in time.
 *
 * @author  Marcelo R.N. Mendes
 *
 * @see		Status
 *
 */
public enum Step {DISCONNECTED, CONNECTED, LOADING, READY, RUNNING, PAUSED, STOPPED, FINISHED, ERROR}
