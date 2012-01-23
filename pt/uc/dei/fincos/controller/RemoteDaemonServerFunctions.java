package pt.uc.dei.fincos.controller;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote functions of the Daemon Server application
 * 
 * @author Marcelo R.N. Mendes
 * 
 * @see		DaemonServer
 *
 */
public interface RemoteDaemonServerFunctions extends Remote{
	
	/**
	 * Initializes an instance of a Driver with the given Alias
	 * 
	 * @param Alias				An alias for the Driver
	 * @throws RemoteException
	 */
	public void startDriver(String Alias) throws RemoteException;
	
	
	/**
	 * Initializes an instance of a Sink with the given Alias
	 * 
	 * @param Alias				An alias for the Sink
	 * @throws RemoteException
	 */
	public void startSink(String Alias) throws RemoteException;
	
	/**
	 * Closes the Daemon application
	 */
	public void finalizeService() throws RemoteException;
}
