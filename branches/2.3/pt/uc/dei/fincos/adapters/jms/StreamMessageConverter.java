package pt.uc.dei.fincos.adapters.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.StreamMessage;

import pt.uc.dei.fincos.basic.Attribute;
import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.EventType;

/**
 * Converts an event, as represented in FINCoS, to a JMS stream message ({@link StreamMessage}), and vice versa.
 */
public class StreamMessageConverter extends Converter {

    @Override
    Message toMessage(Event evt, Session jmsSession)  throws JMSException {
        StreamMessage msg = jmsSession.createStreamMessage();
        Attribute[] atts = evt.getType().getAttributes();
        Object[] payload = evt.getValues();
        for (int i = 0; i < atts.length; i++) {
            switch (atts[i].getType()) {
            case INTEGER:
                msg.writeInt((Integer) payload[i]);
                break;
            case LONG:
            case TIMESTAMP:
                msg.writeLong((Long) payload[i]);
                break;
            case FLOAT:
                msg.writeFloat((Float) payload[i]);
                break;
            case DOUBLE:
                msg.writeDouble((Double) payload[i]);
                break;
            case TEXT:
                msg.writeString((String) payload[i]);
                break;
            case BOOLEAN:
                msg.writeBoolean((Boolean) payload[i]);
                break;
             default:
                 msg.writeObject(payload[i]);
            }
        }
        return msg;
    }

    @Override
    Event toEvent(Message msg, EventType type) throws JMSException {
        long timestamp = System.currentTimeMillis();
        StreamMessage map = (StreamMessage) msg;
        Event evt = new Event(type);
        evt.setTimestamp(timestamp);
        String[] atts = type.getAttributesNames();
        for (int i = 0; i < atts.length; i++) {
            evt.setAttributeValue(i, map.readObject());
        }
        return evt;
    }

    @Override
    String toCSV(Message msg) {
       throw new RuntimeException("Not implemented.");
    }

}
