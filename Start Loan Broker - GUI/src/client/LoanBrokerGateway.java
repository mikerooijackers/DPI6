/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 *
 * @author mikerooijackers
 */
abstract class LoanBrokerGateway {

    MessagingGateway msgGtw;
    ClientSerializer clientSerializer;
    
    public LoanBrokerGateway() {
        msgGtw.setListener(new MessageListener() {
            public void onMessage(Message msg) {
                try {
                    String body = ((TextMessage) msg).getText();
                    ClientReply reply = clientSerializer.replyFromString(body);
                    onClientReply(reply);
                } catch (JMSException ex) {
                    Logger.getLogger(LoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    abstract void onClientReply(ClientReply clientReply);
    
    void requestQuote(ClientRequest clientRequest) {
        String serR = clientSerializer.requestToString(clientRequest);
        Message msg = msgGtw.createMsg(serR);
        msgGtw.send(msg);
    }
    
}
