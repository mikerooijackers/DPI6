/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import bank.BankQuoteReply;
import bank.BankQuoteRequest;
import bank.BankSerializer;
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
public class BankGateway {
    
    private final MessagingGateway gateway;
    private BankSerializer serializer;
    
    private ArrayList<CallBack<BankQuoteReply>> callbacks;

    public BankGateway(String replyQueue,String requestQueue) {
        this.gateway = new MessagingGateway(replyQueue, requestQueue);
        this.serializer = new BankSerializer();

        this.callbacks = new ArrayList<CallBack<BankQuoteReply>>();

        this.gateway.setListener(new MessageListener() {
            public void onMessage(Message msg) {
                try {
                    BankQuoteReply req = 
                            serializer.replyFromString(
                                    ((TextMessage) msg)
                                            .getText());
                    for (CallBack<BankQuoteReply> callback : callbacks) {
                        callback.call(req);
                    }
                } catch (JMSException ex) {
                    Logger.getLogger(BankGateway.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    public void addListener(CallBack<BankQuoteReply> listener) {
        this.callbacks.add(listener);
    }
    
    public void sendRequest(BankQuoteRequest request) {
        Message msg = this.gateway.createMsg(
                this.serializer.requestToString(request));
        this.gateway.send(msg);
    }
    
    public void start() {
        this.gateway.start();
    }

}
