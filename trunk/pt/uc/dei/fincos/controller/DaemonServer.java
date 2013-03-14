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

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.driver.Driver;
import pt.uc.dei.fincos.sink.Sink;


/**
 *
 * Remote application that runs in background to receive RMI calls in order to
 * start Driver and Sink applications. This application should be started on
 * every machine where a Driver or Sink is supposed to run.
 *
 * @author  Marcelo R.N. Mendes
 *
 */
public class DaemonServer implements RemoteDaemonServerFunctions {

    /** The list of Drivers running at this service instance. */
    private HashMap<String, Driver> driverList;

    /** The list of Sink running at this service instance. */
	private HashMap<String, Sink> sinkList;

	/**
	 * Default constructor.
	 */
	public DaemonServer() {
	    super();
		driverList = new HashMap<String, Driver>();
		sinkList = new HashMap<String, Sink>();
	}

	/**
	 * Prepares the daemon server's to accept RMI calls.
	 *
	 * @throws Exception   if an error occurs while starting the daemon server
	 *
	 */
	private void start() throws Exception {
			System.out.println("Trying to start rmi regitry application...");
			LocateRegistry.createRegistry(Globals.RMI_PORT);
			System.out.println("Done!");
			System.out.println("Trying to initialize RMI interface...");
			UnicastRemoteObject.exportObject(this, 0);
			Registry registry = LocateRegistry.getRegistry(Globals.RMI_PORT);
			registry.rebind("FINCoS", this);
			System.out.println("Done!");
	}

	@Override
	public final void startDriver(String alias) throws RemoteException {
		Driver driver = this.driverList.get(alias);
		if (driver == null) {
		    System.out.println("Initializing new Driver application.");
            this.driverList.put(alias, new Driver(alias));
            System.out.println("Initializing new Driver application.");
		} else {
		    System.out.println("Loading Driver \"" + alias + "\".");
		    driver.setVisible(true);
		}

	}

	@Override
	public final void startSink(String alias) throws RemoteException {
		Sink sink = this.sinkList.get(alias);
		if (sink == null) {
		    System.out.println("Initializing new Sink application.");
			this.sinkList.put(alias, new Sink(alias));
		} else {
		    System.out.println("Loading Sink \"" + alias + "\".");
			sink.setVisible(true);
		}
	}


	public static void main(String[] args) {
        try {
            System.setSecurityManager(new SecurityManager());
            DaemonServer daemon = new DaemonServer();
            daemon.start();
        } catch (Exception e) {
            System.err.println("ERROR: Could not start FINCoS daemon service.");
            e.printStackTrace();
            System.exit(1);
        }
	}
}
