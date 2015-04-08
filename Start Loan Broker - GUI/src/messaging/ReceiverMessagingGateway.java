/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import java.util.Properties;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author mikerooijackers
 */
public class ReceiverMessagingGateway
{
    private Connection connection;
    private Session session;
    
    private Destination destination;
    private MessageConsumer consumer;
    
    public ReceiverMessagingGateway(String consumerQueue) 
            throws NamingException, JMSException
    {
        if(consumerQueue == null)
            throw new IllegalArgumentException(consumerQueue);
        
        Properties props = MessagingHelper.createProperties();
        props.put("queue."+ consumerQueue, consumerQueue);
        
        Context jdniContext = new InitialContext(props);
        ConnectionFactory connectionFactory = (ConnectionFactory) jdniContext.lookup("ConnectionFactory");
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        
        destination = (Destination)jdniContext.lookup(consumerQueue);
        consumer = session.createConsumer(destination);
    }

    public void setMessageListener(MessageListener messageListener) 
            throws JMSException
    {
        consumer.setMessageListener(messageListener);
    }

    public void start() 
            throws JMSException
    {
        connection.start();
    }

    public Destination getDestination()
    {
        return destination;
    }
}
