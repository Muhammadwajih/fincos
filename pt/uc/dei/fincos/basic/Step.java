package pt.uc.dei.fincos.basic;

/**
 * The possible states that a component of the framework can be in
 * a given moment in time.
 * 
 * @author	Marcelo R.N. Mendes
 * @see		Status
 *
 */
public enum Step {DISCONNECTED, CONNECTED, LOADING, READY, RUNNING, PAUSED, STOPPED, FINISHED, ERROR}
