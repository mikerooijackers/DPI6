/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bank;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import messaging.MessagingGateway;

/**
 *
 * @author Daan
 */
abstract class loanBrokerGateway {

    MessagingGateway msgGtw;
    BankSerializer sr;

    loanBrokerGateway() {
        msgGtw.setListener(new MessageListener() {
            public void onMessage(Message msg) {
                try {
                    String body = ((TextMessage) msg).getText();
                    BankQuoteReply reply = sr.replyFromString(body);
                    onBankReply(reply);
                } catch (JMSException ex) {
                    Logger.getLogger(loanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    abstract void onBankReply(BankQuoteReply r);
    
    void requestQuote(BankQuoteRequest r) throws JMSException{
        String serR = sr.requestToString(r);
        Message msg = msgGtw.createMsg(serR);
        msgGtw.send(msg);        
    }
    
}
