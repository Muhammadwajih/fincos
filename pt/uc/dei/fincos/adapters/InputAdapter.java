package pt.uc.dei.fincos.adapters;

import pt.uc.dei.fincos.basic.Event;

/**
 * Interface to send events to a CEP engine or a JMS Provider.
 *
 * @author Marcelo R.N. Mendes
 */
public interface InputAdapter {
    /**
     *
     * 1) Converts an event from the internal framework representation to a format supported by the
     * target system.
     * 2) Sends the event to the target system.
     *
     * (IMPLEMENTATIONS OF THIS METHOD MUST BE THREAD-SAFE!)
     *
     * @param e             the event to be converted and sent
     * @throws Exception    if an error occurs during event submission
     */
    void send(Event e) throws Exception;
}
