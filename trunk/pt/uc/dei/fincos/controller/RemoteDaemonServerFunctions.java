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


package pt.uc.dei.fincos.controller;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote functions of the Daemon Server application.
 *
 * @author  Marcelo R.N. Mendes
 *
 * @see		DaemonServer
 *
 */
public interface RemoteDaemonServerFunctions extends Remote {

	/**
	 * Initializes an instance of a Driver with the given alias.
	 *
	 * @param alias				   an alias for the Driver
	 * @throws RemoteException     if the RMI call fails
	 */
	void startDriver(String alias) throws RemoteException;


	/**
	 * Initializes an instance of a Sink with the given alias.
	 *
	 * @param alias				   an alias for the Sink
	 * @throws RemoteException     if the RMI call fails
	 */
	void startSink(String alias) throws RemoteException;

}
