package pt.uc.dei.fincos.adapters.jms;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import pt.uc.dei.fincos.basic.Attribute;
import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.basic.Globals;

/**
 * Converts an event, as represented in FINCoS, to a JMS map message ({@link MapMessage}), and vice versa.
 *
 * @author Marcelo R.N. Mendes
 */
public class MapMessageConverter extends Converter {

    /**
     *
     * Creates a Map message converter.
     *
     * @param rtMode        either END-TO-END or ADAPTER
     * @param rtResolution  either Milliseconds or Nanoseconds
     */
    public MapMessageConverter(int rtMode, int rtResolution) {
        super(rtMode, rtResolution);
    }

    @Override
    Message toMessage(Event evt, Session jmsSession)
    throws JMSException {
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

        if (rtMode == Globals.ADAPTER_RT) {
            // Assigns a timestamp to the event just after conversion
            // (i.e., just before sending the event to the target system)
            long timestamp = 0;
            if (rtResolution == Globals.MILLIS_RT) {
                timestamp = System.currentTimeMillis();
            } else if (rtResolution == Globals.NANO_RT) {
                timestamp = System.nanoTime();
            }
            msg.setLong("TS", timestamp);
        } else if (rtMode == Globals.END_TO_END_RT) {
            // The timestamp comes from the Driver
            msg.setLong("TS", evt.getTimestamp());
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
    String toCSV(Message msg, String src) throws JMSException {
        StringBuilder sb = new StringBuilder("type:");
        sb.append(src);
        MapMessage map = (MapMessage) msg;
        Enumeration<String> atts = map.getMapNames();
        while (atts.hasMoreElements()) {
            sb.append(Globals.CSV_SEPARATOR);
            sb.append(msg.getObjectProperty(atts.nextElement()).toString());
        }
        return sb.toString();
    }

    @Override
    Object[] toObjectArray(Message msg, String src, long timestamp) throws JMSException {
        ArrayList<Object> evtData = new ArrayList<Object>();
        evtData.add(src);
        MapMessage map = (MapMessage) msg;
        Enumeration<String> atts = map.getMapNames();
        while (atts.hasMoreElements()) {
            evtData.add(msg.getObjectProperty(atts.nextElement()));
        }
        if (rtMode == Globals.ADAPTER_RT) {
            evtData.add(timestamp);
        } else if (rtMode == Globals.END_TO_END_RT) {
            evtData.add(null);
        }
        return evtData.toArray(new Object[0]);
    }
}
