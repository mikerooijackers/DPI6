/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author mikerooijackers
 */
public class MessagingGateway {
    private Connection connection;
    private Session session;
    private HashMap<String, Destination> destinations;
    private MessageConsumer consumer;
    private MessageProducer producer;
    
    
    public MessagingGateway(String producerDest, String consumerDest) {
        try {
            
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY,"org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL,"tcp://localhost:61616");
            props.put( ( "queue." + consumerDest ) ,consumerDest);
            props.put( ( "queue." + producerDest ) ,producerDest);        
            Context jndiContext = new InitialContext(props);
            
            ConnectionFactory connectionFactory = 
                    (ConnectionFactory) jndiContext.lookup(
                            "ConnectionFactory");
            
            this.connection = connectionFactory.createConnection();
            this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            this.setDestinations(consumerDest, producerDest, jndiContext);
            this.setConsumer(consumerDest);
            this.setProducer(producerDest);
            
        } catch (NamingException ex) {
            Logger.getLogger(MessagingGateway.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (JMSException ex) {
            Logger.getLogger(MessagingGateway.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    public Message createMsg(String body) {
        try {
            return this.session.createTextMessage(body);
        } catch (JMSException ex) {
            Logger.getLogger(MessagingGateway.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public void send(Message msg) {
        try {
            this.producer.send(msg);
        } catch (JMSException ex) {
            Logger.getLogger(MessagingGateway.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    public void setListener(MessageListener listener) {
        try {
            this.consumer.setMessageListener(listener);
        } catch (JMSException ex) {
            Logger.getLogger(MessagingGateway.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    public void start() {
        try {
            this.connection.start();
        } catch (JMSException ex) {
            Logger.getLogger(MessagingGateway.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    private void setDestinations(String requestQueue, String replyQueue, Context jndiContext) 
            throws NamingException {
        this.destinations = new HashMap<String, Destination>();
        this.destinations.put(
                requestQueue,
                (Destination)
                        jndiContext.lookup(
                                requestQueue));
        this.destinations.put(
                replyQueue,
                (Destination)
                        jndiContext.lookup(
                                replyQueue));
    }
    
    private void setConsumer(String requestQueue) throws JMSException {
        this.consumer = this.session.createConsumer(this.destinations.get(requestQueue));
    }
    
    private void setProducer(String replyQueue) throws JMSException {
        this.producer = this.session.createProducer(this.destinations.get(replyQueue));
    }
    
    public interface CallBack<TArg> {
        public abstract void call(TArg val);
    }
}
