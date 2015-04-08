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
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author mikerooijackers
 */
public class SenderMessagingGateway
{
    private Connection connection;
    private Session session;
    
    private Destination destination;
    private MessageProducer producer;
    
    public SenderMessagingGateway(String producerQueue) 
            throws NamingException, JMSException
    {
        if(producerQueue == null)
            throw new IllegalArgumentException(producerQueue);
        
        Properties props = MessagingHelper.createProperties();
        props.put("queue." + producerQueue, producerQueue);
        
        Context jdniContext = new InitialContext(props);
        ConnectionFactory connectionFactory = (ConnectionFactory) jdniContext.lookup("ConnectionFactory");
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        
        destination = (Destination)jdniContext.lookup(producerQueue);
        producer = session.createProducer(destination);
    }

    public Destination getDestination()
    {
        return destination;
    }
    
    public void start() 
            throws JMSException
    {
        connection.start();
    }
    
    public Message createMessage(String body)
            throws JMSException
    {
        return session.createTextMessage(body);
    }

    public void send(Message message) 
            throws JMSException
    {
        producer.send(message);
    }
}
