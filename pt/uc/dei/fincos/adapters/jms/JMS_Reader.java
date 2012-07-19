package pt.uc.dei.fincos.adapters.jms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.naming.NamingException;

import pt.uc.dei.fincos.sink.Sink;

/**
 * Adapter used to read messages from one or more JMS queues.
 *
 * @author Marcelo R.N. Mendes
 */
public class JMS_Reader extends JMS_Adapter {

    /** Message consumers. */
    private final ArrayList<MessageConsumer> consumers;

    /**
    *
    * @param connProps         connection properties
    * @param connFactoryName   name of the connection factory at the JNDI server
    * @param outputListeners   a mapping [listener] -> [list-of-destinations-to-be-listened]
    * @param rtMode            either END-TO-END or ADAPTER
    * @param rtResolution      either Milliseconds or Nanoseconds
    * @param sinkInstance      reference to the Sink instance to which messages must be forwarded    *
    *
    * @throws NamingException  if a naming exception is encountered
    * @throws JMSException     if an error occurs during connection with JMS provider
    */
   public JMS_Reader(Properties connProps, String connFactoryName, HashMap<String, String[]> outputListeners,
           int rtMode, int rtResolution, Sink sinkInstance)
   throws NamingException, JMSException {
       this(connProps, connFactoryName, new MapMessageConverter(rtMode, rtResolution),
               outputListeners, rtMode, rtResolution, sinkInstance);
   }

    /**
     *
     * @param connProps         connection properties
     * @param connFactoryName   name of the connection factory at the JNDI server
     * @param msgConverter      converts events, as represented in FINCoS, to JMS messages and vice-versa
     * @param outputListeners   a mapping [listener] -> [list-of-destinations-to-be-listened]
     * @param rtMode            response time measurement mode (either END-TO-END or ADAPTER)
     * @param rtResolution      response time measurement resolution (either Milliseconds or Nanoseconds)
     * @param sinkInstance      reference to the Sink instance to which messages must be forwarded
     *
     * @throws NamingException  if a naming exception is encountered
     * @throws JMSException     if an error occurs during connection with JMS provider
     */
    public JMS_Reader(Properties connProps, String connFactoryName, Converter msgConverter,
            HashMap<String, String[]> outputListeners, int rtMode, int rtResolution,
            Sink sinkInstance)
    throws NamingException, JMSException {
        super(connProps, connFactoryName, msgConverter);
        consumers = new ArrayList<MessageConsumer>();
        int i = 1;
        // Creates a listener for every entry in outputListeners
        for (Entry<String, String[]> entry: outputListeners.entrySet()) {
            String[] outputChannels = entry.getValue();
            JMS_Listener lsnr = new JMS_Listener(entry.getKey(), rtMode,
                    rtResolution, sinkInstance, msgConverter);
            // Associate the listener with all the queues in outputChannels
            for (String channel: outputChannels) {
                Queue q = (Queue) ctxt.lookup(channel);
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
