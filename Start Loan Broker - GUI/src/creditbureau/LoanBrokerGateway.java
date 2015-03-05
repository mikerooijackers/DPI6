/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package creditbureau;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import messaging.MessagingGateway;
import messaging.MessagingGateway.CallBack;

/**
 *
 * @author mikerooijackers
 */
public class LoanBrokerGateway {

    private final MessagingGateway gateway;
    private CreditSerializer serializer;
    
    private ArrayList<CallBack<CreditRequest>> callbacks;

    public LoanBrokerGateway(String requestQueue, String replyQueue) {
        this.gateway = new MessagingGateway(replyQueue, requestQueue);
        this.serializer = new CreditSerializer();

        this.callbacks = new ArrayList<CallBack<CreditRequest>>();

        this.gateway.setListener(new MessageListener() {
            public void onMessage(Message msg) {
                try {
                    CreditRequest req = 
                            serializer.requestFromString(
                                    ((TextMessage) msg)
                                            .getText());
                    for (CallBack<CreditRequest> callback : callbacks) {
                        callback.call(req);
                    }
                } catch (JMSException ex) {
                    Logger.getLogger(LoanBrokerGateway.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    public void addListener(CallBack<CreditRequest> listener) {
        this.callbacks.add(listener);
    }
    
    public void sendReply(CreditReply reply) {
        Message msg = this.gateway.createMsg(
                this.serializer.replyToString(reply));
        this.gateway.send(msg);
    }
    
    public void start() {
        this.gateway.start();
    }
}
