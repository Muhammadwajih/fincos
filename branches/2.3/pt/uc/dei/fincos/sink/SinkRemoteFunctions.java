package pt.uc.dei.fincos.sink;

import java.rmi.Remote;
import java.rmi.RemoteException;

import pt.uc.dei.fincos.basic.InvalidStateException;
import pt.uc.dei.fincos.basic.Status;
import pt.uc.dei.fincos.controller.SinkConfig;


/**
 * RMI functions exposed by a Sink.
 *
 * @author Marcelo R.N. Mendes
 */
public interface SinkRemoteFunctions extends Remote {

    /**
     * Initializes a Sink.
     *
     * @param sinkCfg                   the configuration to be loaded
     * @param rtMode                    response time measurement mode (either END-TO-END or ADAPTER)
     * @param rtResolution              response time measurement resolution (either Milliseconds or Nanoseconds)
     *
     * @return                          <tt>true</tt> if the Driver has been successfully initialized, <tt>false</tt> otherwise.
     *
     * @throws InvalidStateException    if the Sink has already been loaded.
     * @throws RemoteException          for unsuccessful RMI calls.
     * @throws Exception                for unexpected errors.
     */
    boolean load(SinkConfig sinkCfg, int rtMode, int rtResolution)
    throws RemoteException, InvalidStateException, Exception;


    /**
     *
     * Unloads a Sink.
     *
     * @throws RemoteException  for unsuccessful RMI calls
     */
    void unload() throws RemoteException;



    /**
     *
     * @return the status of a remote Sink
     * @throws RemoteException  for unsuccessful RMI calls
     */
    Status getStatus() throws RemoteException;
}
