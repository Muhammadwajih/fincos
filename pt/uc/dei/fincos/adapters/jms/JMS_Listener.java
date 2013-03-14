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
 * @author  Marcelo R.N. Mendes
 */
public final class JMS_Listener extends OutputListener implements MessageListener {

    /** Converts JMS messages to events, as represented in FINCoS. */
    private final Converter msgConverter;


    /**
     *
     * @param lsnrID        an alias for this listener
     * @param rtMode        response time measurement mode
     *                      (either END-TO-END or ADAPTER)
     * @param rtResolution  response time measurement resolution
     *                      (either Milliseconds or Nanoseconds)
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
        } catch (JMSException jmsExc) {
            throw new RuntimeException(jmsExc.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
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
