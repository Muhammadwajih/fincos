package pt.uc.dei.fincos.adapters.jms;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import pt.uc.dei.fincos.basic.Attribute;
import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.EventType;

/**
 * Converts an event, as represented in FINCoS, to a JMS map message ({@link MapMessage}), and vice versa.
 */
public class MapMessageConverter extends Converter {

    @Override
    Message toMessage(Event evt, Session jmsSession)  throws JMSException {
        MapMessage msg = jmsSession.createMapMessage();
        Attribute[] atts = evt.getType().getAttributes();
        Object[] payload = evt.getValues();
        for (int i = 0; i < atts.length; i++) {
            switch (atts[i].getType()) {
            case INTEGER:
                msg.setInt(atts[i].getName(), (Integer) payload[i]);
                break;
            case LONG:
            case TIMESTAMP:
                msg.setLong(atts[i].getName(), (Long) payload[i]);
                break;
            case FLOAT:
                msg.setFloat(atts[i].getName(), (Float) payload[i]);
                break;
            case DOUBLE:
                msg.setDouble(atts[i].getName(), (Double) payload[i]);
                break;
            case TEXT:
                msg.setString(atts[i].getName(), (String) payload[i]);
                break;
            case BOOLEAN:
                msg.setBoolean(atts[i].getName(), (Boolean) payload[i]);
                break;
             default:
                 msg.setObject(atts[i].getName(), payload[i]);
            }
        }
        return msg;
    }

    @Override
    Event toEvent(Message msg, EventType type) throws JMSException {
        long timestamp = System.currentTimeMillis();
        MapMessage map = (MapMessage) msg;
        Event evt = new Event(type);
        evt.setTimestamp(timestamp);
        String[] atts = type.getAttributesNames();
        for (int i = 0; i < atts.length; i++) {
            evt.setAttributeValue(i, map.getObject(atts[i]));
        }
        return evt;
    }

    @Override
    String toCSV(Message msg) {
       throw new RuntimeException("Not implemented.");
    }

}
