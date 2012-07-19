package pt.uc.dei.fincos.adapters.jms;

import java.util.HashMap;
import java.util.Properties;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.naming.NamingException;

import pt.uc.dei.fincos.adapters.InputAdapter;
import pt.uc.dei.fincos.basic.CSV_Event;
import pt.uc.dei.fincos.basic.Event;

/**
 * Adapter used to publish messages into one or more JMS queues.
 * By default, NON-PERSISTENT messages are used, as to minimize
 * the impact on the JMS server.
 *
 * @author Marcelo R.N. Mendes
 */
public class JMS_Writer extends JMS_Adapter implements InputAdapter {

    /** Maps input channel names into JMS senders/destinations. */
    private HashMap<String, MessageProducer> senders;

    /**
     * Creates an adapter to publish messages into a JMS message bus.
     *
     * @param connProps         connection properties
     * @param connFactoryName   name of the connection factory at the JNDI server
     * @param inputChannels     a list of JMS destinations into which this adapter will insert messages
     * @param rtMode            either END-TO-END or ADAPTER
     * @param rtResolution      either Milliseconds or Nanoseconds
     *
     * @throws NamingException  if a naming exception is encountered
     * @throws JMSException     if an error occurs during connection with JMS provider
     */
    public JMS_Writer(Properties connProps, String connFactoryName, String[] inputChannels,
            int rtMode, int rtResolution) throws NamingException, JMSException {
        this(connProps, connFactoryName, inputChannels, new MapMessageConverter(rtMode, rtResolution));
    }

    /**
     * Creates an adapter to publish messages into a JMS message bus.
     *
     * @param connProps         connection properties
     * @param connFactoryName   name of the connection factory at the JNDI server
     * @param inputChannels     a list of JMS destinations into which this adapter will insert messages
     * @param msgConverter      converts events, as represented in FINCoS, to JMS messages and vice-versa
     *
     * @throws NamingException  if a naming exception is encountered
     * @throws JMSException     if an error occurs during connection with JMS provider
     */
    public JMS_Writer(Properties connProps, String connFactoryName, String[] inputChannels, Converter msgConverter)
    throws NamingException, JMSException {
        super(connProps, connFactoryName, msgConverter);
        // Start the queue connection.
        conn.start();
        // Creates a sender for each input channel
        senders = new HashMap<String, MessageProducer>();
        for (int i = 0; i < inputChannels.length; i++) {
            createMessageProducerFor(inputChannels[i]);
        }
    }


    /**
     * Converts the event passed as argument into a JMS message and sends it
     * to the messaging system.
     *
     * @param ev            the event to be sent
     * @throws Exception    if an error occurs during message publishing
     */
    public void send(Event ev) throws Exception {
        // Retrieves the appropriate sender
        String dest = ev.getType().getName();
        MessageProducer sender = senders.get(dest);
        if (sender == null) {
           sender = createMessageProducerFor(dest);
        }
        //Converts the event into a JMS message
        Message msg = this.msgConverter.toMessage(ev, this.session);
        // Sends the message
        sender.send(msg);
    }


    @Override
    public void disconnect() throws JMSException {
        // Closes the senders, the JMS session and the JMS connection.
        for (MessageProducer sender : senders.values()) {
            sender.close();
        }
        senders.clear();
        session.close();
        conn.close();
    }

    @Override
    public void send(CSV_Event event) throws Exception {
     // Retrieves the appropriate sender
        String dest = event.getType();
        MessageProducer sender = senders.get(dest);
        if (sender == null) {
            sender = createMessageProducerFor(dest);
        }
        //Converts the event into a JMS message
        Message msg = this.msgConverter.toMessage(event, this.session);
        // Sends the message
        sender.send(msg);
    }

    private MessageProducer createMessageProducerFor(String dest) throws NamingException, JMSException {
        Queue q = (Queue) ctxt.lookup(dest);
        MessageProducer producer = session.createProducer(q);
        senders.put(dest, producer);
        // Performance Tuning Settings
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        producer.setDisableMessageID(true);
        producer.setDisableMessageTimestamp(true);
        return producer;
    }
}
