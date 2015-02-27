/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import javax.jms.*;

/**
 *
 * @author Daan
 */
public class MessagingGateway {
    Destination dest;
    Session ses;
    
    public Message createMsg(String body) throws JMSException{
        TextMessage msg = null;
        msg.setText(body);
        return msg;
    }
    public void send(Message msg){
        
    }
    public void setListener(MessageListener l){
        
    }
}
