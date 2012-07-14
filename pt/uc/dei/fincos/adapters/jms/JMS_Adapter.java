package pt.uc.dei.fincos.adapters.jms;

import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Provides connectivity with JMS messaging systems.
 *
 * @author Marcelo R.N. Mendes
 */
public abstract class JMS_Adapter {

    /** Naming context.  */
    Context ctxt;

    /** Connection with a JMS provider. */
    QueueConnection conn;

    /** JMS Session. */
    QueueSession session;

    /** Converts events, as represented in FINCoS, to JMS messages. */
    final Converter msgConverter;

    /**
     * Connects to a JMS provider.
     *
     * @param connProps             connection properties
     * @param connFactoryName       name of the connection factory at the JNDI server
     * @param msgConverter          converts events, as represented in FINCoS, to JMS messages and vice-versa
     *
     * @throws NamingException  if a naming exception is encountered
     * @throws JMSException     if an error occurs during connection with JMS provider
     */
    public JMS_Adapter(Properties connProps, String connFactoryName, Converter msgConverter)
    throws NamingException, JMSException {
        // Set the JNDI properties; specific to the naming service vendor
        this.ctxt = new InitialContext(connProps);
        // Retrieve the queue connection factory.
        QueueConnectionFactory cf = (QueueConnectionFactory) ctxt.lookup(connFactoryName);
        // Create the JMS connection.
        this.conn  = cf.createQueueConnection();
        // Create the JMS session over the JMS connection.
        session = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        // Message converter
        this.msgConverter = msgConverter;
    }

    /**
     * Releases any JMS resources (e.g., senders, session and connection).
     *
     * @throws JMSException     if an error occurs during disconnection
     */
    public abstract void disconnect() throws JMSException;
}
