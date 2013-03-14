/* FINCoS Framework
 * Copyright (C) 2013 CISUC, University of Coimbra
 *
 * Licensed under the terms of The GNU General Public License, Version 2.
 * A copy of the License has been included with this distribution in the
 * fincos-license.txt file.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 */


package pt.uc.dei.fincos.adapters.jms;

import java.util.ArrayList;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.Session;
import javax.jms.StreamMessage;

import pt.uc.dei.fincos.basic.Attribute;
import pt.uc.dei.fincos.basic.CSV_Event;
import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.basic.Globals;

/**
 * Converts an event, as represented in FINCoS, to a JMS stream message
 * ({@link StreamMessage}), and vice versa.
 *
 * @author  Marcelo R.N. Mendes
 */
public final class StreamMessageConverter extends Converter {

    /**
     * Creates a stream message converter.
     *
     * @param rtMode        either END-TO-END or ADAPTER
     * @param rtResolution  either Milliseconds or Nanoseconds
     */
    public StreamMessageConverter(int rtMode, int rtResolution) {
        super(rtMode, rtResolution);
    }

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

        if (rtMode == Globals.ADAPTER_RT) {
            /* Assigns a timestamp to the event just after conversion
              (i.e., just before sending the event to the target system) */
            long timestamp = 0;
            if (rtResolution == Globals.MILLIS_RT) {
                timestamp = System.currentTimeMillis();
            } else if (rtResolution == Globals.NANO_RT) {
                timestamp = System.nanoTime();
            }
            msg.writeLong(timestamp);
        } else if (rtMode == Globals.END_TO_END_RT) {
            // The timestamp comes from the Driver
            msg.writeLong(evt.getTimestamp());
        }

        return msg;
    }

    @Override
    Message toMessage(CSV_Event event, Session jmsSession) throws JMSException {
        StreamMessage msg = jmsSession.createStreamMessage();
        for (int i = 0; i < event.getPayload().length; i++) {
            msg.writeString(event.getPayload()[i]);
        }

        if (rtMode == Globals.ADAPTER_RT) {
            /* Assigns a timestamp to the event just after conversion
              (i.e., just before sending the event to the target system) */
            long timestamp = 0;
            if (rtResolution == Globals.MILLIS_RT) {
                timestamp = System.currentTimeMillis();
            } else if (rtResolution == Globals.NANO_RT) {
                timestamp = System.nanoTime();
            }
            msg.writeLong(timestamp);
        } else if (rtMode == Globals.END_TO_END_RT) {
            // The timestamp comes from the Driver
            msg.writeLong(event.getTimestamp());
        }

        return msg;
    }

    @Override
    Event toEvent(Message msg, EventType type) throws JMSException {
        long timestamp = System.currentTimeMillis();
        StreamMessage str = (StreamMessage) msg;
        Event evt = new Event(type);
        evt.setTimestamp(timestamp);
        String[] atts = type.getAttributesNames();
        for (int i = 0; i < atts.length; i++) {
            evt.setAttributeValue(i, str.readObject());
        }
        return evt;
    }

    @Override
    String toCSV(Message msg, String src) throws JMSException {
        StringBuilder sb = new StringBuilder();
        sb.append(src);
        StreamMessage str = (StreamMessage) msg;
        while (true) {
            try {
                String att = str.readObject().toString();
                sb.append(Globals.CSV_DELIMITER);
                sb.append(att);
            } catch (MessageEOFException e) {
                break;
            }
        }

        return sb.toString();
    }

    @Override
    Object[] toObjectArray(Message msg, String src, long timestamp) throws JMSException {
        ArrayList<Object> evtData = new ArrayList<Object>();
        evtData.add(src);
        StreamMessage str = (StreamMessage) msg;
        while (true) {
            try {
                Object att = str.readObject();
                evtData.add(att);
            } catch (MessageEOFException e) {
                break;
            }
        }
        if (rtMode == Globals.ADAPTER_RT) {
            evtData.add(timestamp);
        } else if (rtMode == Globals.END_TO_END_RT) {
            evtData.add(null);
        }
        return evtData.toArray(new Object[0]);
    }
}
