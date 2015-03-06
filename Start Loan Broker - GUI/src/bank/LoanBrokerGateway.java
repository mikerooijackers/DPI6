/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bank;

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
 * @author Daan
 */
public class LoanBrokerGateway {

    private final MessagingGateway gateway;
    private BankSerializer serializer;
    
    private ArrayList<CallBack<BankQuoteRequest>> callbacks;

    public LoanBrokerGateway(String replyQueue,String requestQueue) {
        this.gateway = new MessagingGateway(requestQueue, replyQueue);
        this.serializer = new BankSerializer();

        this.callbacks = new ArrayList<CallBack<BankQuoteRequest>>();

        this.gateway.setListener(new MessageListener() {
            public void onMessage(Message msg) {
                try {
                    BankQuoteRequest req = 
                            serializer.requestFromString(
                                    ((TextMessage) msg)
                                            .getText());
                    for (CallBack<BankQuoteRequest> callback : callbacks) {
                        callback.call(req);
                    }
                } catch (JMSException ex) {
                    Logger.getLogger(LoanBrokerGateway.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    public void addListener(CallBack<BankQuoteRequest> listener) {
        this.callbacks.add(listener);
    }
    
    public void sendReply(BankQuoteReply reply) {
        Message msg = this.gateway.createMsg(
                this.serializer.replyToString(reply));
        this.gateway.send(msg);
    }
    
    public void start() {
        this.gateway.start();
    }
}
