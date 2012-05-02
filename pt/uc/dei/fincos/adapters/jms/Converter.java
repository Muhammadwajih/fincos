package pt.uc.dei.fincos.adapters.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.EventType;

/**
 * Converts an event, as represented in FINCoS, to a JMS message, and vice versa.
 */
public abstract class Converter {

    /**
     * Converts an event, as represented in FINCoS, to a JMS message.
     *
     * @param evt           the event to be converted
     * @param jmsSession    the JMS session used to create messages
     * @return              an instance of a subclass of {@link Message}
     * @throws JMSException if an error occur while creating the message
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
     * @return      a CSV record containing the message's payload
     */
    abstract String toCSV(Message msg);

}
