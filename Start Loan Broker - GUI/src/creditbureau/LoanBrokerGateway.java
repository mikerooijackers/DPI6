/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package creditbureau;

import java.util.logging.Level;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import messaging.MessagingGateway;

/**
 *
 * @author mikerooijackers
 */
abstract class LoanBrokerGateway {

    MessagingGateway msgGtw;
    CreditSerializer creditSerializer;

    public LoanBrokerGateway() {
        msgGtw.setListener(new MessageListener() {
            public void onMessage(Message msg) {
                try {
                    String body = ((TextMessage) msg).getText();
                    CreditReply reply = creditSerializer.replyFromString(body);
                    onCredit(reply);
                } catch (JMSException ex) {
                    java.util.logging.Logger.getLogger(LoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    abstract void onCreditReply(CreditReply creditReply);

    void onCredit(CreditReply reply) throws JMSException {
        String serR = creditSerializer.replyToString(reply);
        Message msg = msgGtw.createMsg(serR);
        msgGtw.send(msg);
    }
}
