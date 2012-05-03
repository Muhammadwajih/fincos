package pt.uc.dei.fincos.adapters.jms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.naming.NamingException;

import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.sink.Sink;

public class JMS_Reader extends JMS_Adapter {

    /** Message consumers. */
    private final ArrayList<MessageConsumer> consumers;

    /**
    *
    * @param connProps         connection properties
    * @param connFactoryName   name of the connection factory at the JNDI server
    * @param outputListeners   a mapping [listener] -> [list-of-destinations-to-be-listened]
    * @param sinkInstance      reference to the Sink instance to which messages must be forwarded
    *
    * @throws NamingException  if a naming exception is encountered
    * @throws JMSException     if an error occurs during connection with JMS provider
    */
   public JMS_Reader(Properties connProps, String connFactoryName, HashMap<String, EventType[]> outputListeners, Sink sinkInstance)
   throws NamingException, JMSException {
       this(connProps, connFactoryName, new MapMessageConverter(), outputListeners, sinkInstance);
   }

    /**
     *
     * @param connProps         connection properties
     * @param connFactoryName   name of the connection factory at the JNDI server
     * @param msgConverter      converts events, as represented in FINCoS, to JMS messages and vice-versa
     * @param outputListeners   a mapping [listener] -> [list-of-destinations-to-be-listened]
     * @param sinkInstance      reference to the Sink instance to which messages must be forwarded
     *
     * @throws NamingException  if a naming exception is encountered
     * @throws JMSException     if an error occurs during connection with JMS provider
     */
    public JMS_Reader(Properties connProps, String connFactoryName, Converter msgConverter, HashMap<String, EventType[]> outputListeners,
            Sink sinkInstance)
    throws NamingException, JMSException {
        super(connProps, connFactoryName, msgConverter);
        consumers = new ArrayList<MessageConsumer>();
        int i = 1;
        // Creates a listener for every entry in outputListeners
        for (Entry<String, EventType[]> entry: outputListeners.entrySet()) {
            EventType[] outputChannels = entry.getValue();
            JMS_Listener lsnr = new JMS_Listener(entry.getKey(), Globals.END_TO_END_RT_MILLIS,
                    Globals.DEFAULT_LOG_FLUSH_INTERVAL, sinkInstance, msgConverter, outputChannels);
            // Associate the listener with all the queues in outputChannels
            for (EventType type: outputChannels) {
                Queue q = (Queue) ctxt.lookup(type.getName());
                MessageConsumer consumer = session.createConsumer(q);
                consumers.add(consumer);
                consumer.setMessageListener(lsnr);
            }

            i++;
        }
        // Start the queue connection.
        conn.start();
    }

    @Override
    public void disconnect() throws JMSException {
        for (MessageConsumer consumer : consumers) {
            consumer.setMessageListener(null);
            consumer.close();
        }
        consumers.clear();
        session.close();
        conn.close();
    }

}
