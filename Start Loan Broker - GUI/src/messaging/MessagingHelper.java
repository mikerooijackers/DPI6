/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import java.util.Properties;
import javax.naming.Context;

/**
 *
 * @author mikerooijackers
 */
public class MessagingHelper
{
    private final static String ACTIVEMQ_CONTEXTFACTORY = "org.apache.activemq.jndi.ActiveMQInitialContextFactory";
    private final static String PROVIDER_URL = "tcp://localhost:61616";
    
    public static Properties createProperties()
    {
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, ACTIVEMQ_CONTEXTFACTORY);
        props.setProperty(Context.PROVIDER_URL, PROVIDER_URL);
        return props;
    }
}
