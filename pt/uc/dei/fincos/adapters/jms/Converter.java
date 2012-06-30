package pt.uc.dei.fincos.adapters.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.EventType;

/**
 * Converts an event, as represented in FINCoS, to a JMS message, and vice versa.
 *
 * @author Marcelo R.N. Mendes
 */
public abstract class Converter {

    /** Either END-TO-END or ADAPTER. */
    protected final int rtMode;

    /** Either Milliseconds or Nanoseconds. */
    protected final int rtResolution;

    /**
     * Creates a message converter.
     *
     * @param rtMode        either END-TO-END or ADAPTER
     * @param rtResolution  either Milliseconds or Nanoseconds
     */
    public Converter(int rtMode, int rtResolution) {
        super();
        this.rtMode = rtMode;
        this.rtResolution = rtResolution;
    }


    /**
     * Converts an event, as represented in FINCoS, to a JMS message.
     *
     * @param evt               the event to be converted
     * @param jmsSession        the JMS session used to create messages
     * @param rtMode either END-TO-END or ADAPTER
     * @param rtResolution      either Milliseconds or Nanoseconds
     *
     * @return                  an instance of a subclass of {@link Message}
     * @throws JMSException     if an error occur while creating the message
     */
    abstract Message toMessage(Event evt, Session jmsSession) throws JMSException;

    /**
     * Converts a JMS message to an event, as represented in FINCoS.
     *
     * @param msg           the JMS message
     * @param type          the type of the to-be-created event
     * @return              the message converted to FINCoS internal format
     * @throws JMSException if an error occur while reading the message
     */
    abstract Event toEvent(Message msg, EventType type) throws JMSException;

    /**
     * Converts a JMS message to a comma-separated record.
     *
     * @param msg   the JMS message
     * @param src  the source of the JMS message
     * @return      a CSV record containing the message's payload
     */
    abstract String toCSV(Message msg, String src) throws JMSException;

    /**
     * Converts a JMS message to an array of Objects.
     *
     * @param msg           the JMS message
     * @param src           the source of the JMS message
     * @param timestamp     the arrival time of the message
     * @return              an array of Objects containing the event type (first element)
     *                      and the event payload inside the message.
     * @throws JMSException if an error occur while reading the message
     */
    abstract Object[] toObjectArray(Message msg, String src, long timestamp) throws JMSException;

}
