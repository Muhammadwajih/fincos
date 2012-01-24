package pt.uc.dei.fincos.controller;

import java.io.IOException;
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
 * Remote application that runs in background to receive RMI calls in order to start Driver and Sink applications
 * It is intended to run in each machine at which a Driver or Sink will run.
 *
 * @author Marcelo R.N. Mendes
 *
 */
public class DaemonServer implements RemoteDaemonServerFunctions{
	private HashMap<String, Driver> driverList;
	private HashMap<String, Sink> monitorList;

	public DaemonServer() {
		driverList = new HashMap<String, Driver>();
		monitorList = new HashMap<String, Sink>();
	}

	public DaemonServer(String ID) {
		driverList = new HashMap<String, Driver>();
		monitorList = new HashMap<String, Sink>();
	}

	/**
	 * Prepares the daemon server's to accept RMI calls
	 *
	 */
	public void start() {
		try {
			System.out.println("Trying to start rmi regitry application...");
			Runtime.getRuntime().exec("rmiregistry");
			System.out.println("Done!");

			System.out.println("Trying to initialize RMI interface...");
			RemoteDaemonServerFunctions stub = (RemoteDaemonServerFunctions) UnicastRemoteObject.exportObject(this, 0);
			Registry registry = LocateRegistry.getRegistry(Globals.DEFAULT_RMI_PORT);
			registry.rebind("FINCoS", stub);
			System.out.println("Done!");
		} catch (IOException e1) {
			e1.printStackTrace();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				;
			}

			System.exit(-1);
		}
	}

	@Override
	public void finalizeService(){
		System.exit(0);
	}

	@Override
	public void startDriver(String alias) throws RemoteException {

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
	public void startSink(String alias) throws RemoteException {
		System.out.println("Initializing new Sink application.");
		Sink monitor = this.monitorList.get(alias);
		if(monitor == null)
			this.monitorList.put(alias, new Sink(alias));
		else
			monitor.setVisible(true);
	}


	public static void main(String[] args) {
		DaemonServer daemon = new DaemonServer();
		daemon.start();
	}
}
