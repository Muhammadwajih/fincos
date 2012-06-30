package pt.uc.dei.fincos.adapters.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;

import pt.uc.dei.fincos.adapters.OutputListener;
import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.sink.Sink;

/**
 * Listens to messages coming from one or more JMS queues.
 *
 * @author Marcelo R.N. Mendes
 */
public class JMS_Listener extends OutputListener implements MessageListener {

    /** Converts JMS messages to events, as represented in FINCoS. */
    private final Converter msgConverter;


    /**
     *
     * @param lsnrID        an alias for this listener
     * @param rtMode        response time measurement mode (either END-TO-END or ADAPTER)
     * @param rtResolution  response time measurement resolution (either Milliseconds or Nanoseconds)
     * @param sinkInstance  reference to the Sink instance to which events must be forwarded
     * @param msgConverter  converts JMS messages to events, as represented in FINCoS
     */
    public JMS_Listener(String lsnrID, int rtMode, int rtResolution, Sink sinkInstance,
            Converter msgConverter) {
        super(lsnrID, rtMode, rtResolution, sinkInstance);
        this.msgConverter = msgConverter;
    }

    @Override
    public void onMessage(Message msg) {
        long timestamp = -1;
        if (this.rtMode == Globals.ADAPTER_RT) {
            if (this.rtResolution == Globals.MILLIS_RT) {
                timestamp = System.currentTimeMillis();
            } else if (this.rtResolution == Globals.NANO_RT) {
                timestamp = System.nanoTime();
            }
        }

        try {
            String src = ((Queue) msg.getJMSDestination()).getQueueName();
            // Converts and forwards the event message to a Sink
            super.onOutput(msgConverter.toObjectArray(msg, src, timestamp));
            //System.out.println("[" + this.listenerID + "] Received: " + msgConverter.toObjectArray(msg, src));
        } catch (JMSException jmsExc) {
            throw new RuntimeException(jmsExc.getMessage());
        }
    }

    @Override
    public void load() throws Exception {
        // No operation required
    }

    @Override
    public void disconnect() {
        // No operation required
    }

}
