package pt.uc.dei.fincos.adapters.jms;

import java.util.HashMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;

import pt.uc.dei.fincos.adapters.OutputListener;
import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.sink.Sink;

public class JMS_Listener extends OutputListener implements MessageListener {

    /** Converts JMS messages to events, as represented in FINCoS. */
    private final Converter msgConverter;

    /** Maps {@link Destination} names into event types. */
    private final HashMap<String, EventType> evtTypes;

    /**
     *
     * @param lsnrID                an alias for this listener
     * @param rtMeasurementMode     response time measurement (either in milliseconds or nanoseconds)
     * @param logFlushInterval      frequency of log flushes to disk, in milliseconds
     * @param sinkInstance          reference to the Sink instance to which events must be forwarded
     * @param msgConverter          converts JMS messages to events, as represented in FINCoS
     * @param listenedTypes         a list of event types this listener listens to
     */
    public JMS_Listener(String lsnrID, int rtMeasurementMode, int logFlushInterval, Sink sinkInstance,
            Converter msgConverter, EventType[] listenedTypes) {
        super(lsnrID, rtMeasurementMode, logFlushInterval, sinkInstance);
        this.msgConverter = msgConverter;
        this.evtTypes = new HashMap<String, EventType>();
        for (EventType eventType : listenedTypes) {
            this.evtTypes.put(eventType.getName(), eventType);
        }
    }

    @Override
    public void onMessage(Message msg) {
        try {
            String src = ((Queue) msg.getJMSDestination()).getQueueName();
            EventType type = this.evtTypes.get(src);
            if (type == null) {
                throw new RuntimeException("Received a message of unrecognized type: \"" + src + "\"");
            }
            // Converts and forwards the event message to a Sink
         //   super.onOutput(msgConverter.toEvent(msg, type).getValues());
        //    System.out.println("[" + this.listenerID + "] Received: " + msgConverter.toEvent(msg, type).toCSV());
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
