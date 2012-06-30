package pt.uc.dei.fincos.adapters.jms;

import java.util.HashMap;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import pt.uc.dei.fincos.basic.Attribute;
import pt.uc.dei.fincos.basic.Datatype;
import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.basic.Globals;

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


    /**
     * Test method.
     */
    public static void main(String[] args) throws Exception {
        // Parameters for connection with JMS provider
        Properties connProps = new Properties();
        connProps.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
        connProps.setProperty("java.naming.provider.url", "jnp://localhost:1099");
        connProps.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
      //  connProps.setProperty(Context.INITIAL_CONTEXT_FACTORY,"org.apache.activemq.jndi.ActiveMQInitialContextFactory");
       // connProps.setProperty(Context.PROVIDER_URL,"tcp://127.0.0.1:61616");
        // Name of destinations
        String[] channels = {"Stock"};
        // FINCoS internal representation of the Stock message
        EventType stockType = new EventType("Stock", new Attribute[] {
                new Attribute(Datatype.LONG, "TS"),
                new Attribute(Datatype.TEXT, "Symbol"),
                new Attribute(Datatype.DOUBLE, "Price"),
                new Attribute(Datatype.INTEGER, "Volume")
        });
        Converter converter = new StreamMessageConverter(Globals.END_TO_END_RT, Globals.NANO_RT);
        // Sets up a JMS Writer
        JMS_Writer writer = new JMS_Writer(connProps, "ThroughputConnectionFactory", channels, converter);
        // Sets up a JMS Reader
        HashMap<String, String[]> outputListeners = new HashMap<String, String[]>();
        outputListeners.put("lsnr-01", new String[] {"Stock"});
        JMS_Reader reader = new JMS_Reader(connProps, "ThroughputConnectionFactory", converter, outputListeners,
                Globals.END_TO_END_RT, Globals.NANO_RT, null);
        // Sends data
        long t0 = System.currentTimeMillis();
        long ts;
        int i;
        System.out.println("Warmup started.");
        for (i = 0; (ts = System.currentTimeMillis()) - t0 < 10000; i++) {
            writer.send(new Event(stockType, new Object[] {System.currentTimeMillis(), "MSFT", 32.01, 100+i}));
          //  Thread.sleep(500);
        }
        System.out.println("Throughput: "  + (1000.0 * i / (ts - t0)) + " msgs/sec.");

        System.out.println("MI started.");
        t0 = System.currentTimeMillis();
        for (i = 0; (ts = System.currentTimeMillis()) - t0 < 10000; i++) {
            writer.send(new Event(stockType, new Object[] {System.currentTimeMillis(), "MSFT", 32.01, 100+i}));
      //      Thread.sleep(500);
        }
        System.out.println("Throughput: "  + (1000.0 * i / (ts - t0)) + " msgs/sec.");

        Thread.sleep(2000);

        reader.disconnect();
        writer.disconnect();
    }


}
