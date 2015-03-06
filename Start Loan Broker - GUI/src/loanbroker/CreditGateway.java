/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import creditbureau.CreditReply;
import creditbureau.CreditRequest;
import creditbureau.CreditSerializer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import messaging.MessagingGateway.CallBack;
import messaging.MessagingGateway;

/**
 *
 * @author mikerooijackers
 */
public class CreditGateway {
    
    private final MessagingGateway gateway;
    private CreditSerializer serializer;
    
    private ArrayList<CallBack<CreditReply>> callbacks;

    public CreditGateway(String replyQueue, String requestQueue) {
        this.gateway = new MessagingGateway(replyQueue, requestQueue);
        this.serializer = new CreditSerializer();

        this.callbacks = new ArrayList<CallBack<CreditReply>>();

        this.gateway.setListener(new MessageListener() {
            public void onMessage(Message msg) {
                try {
                    CreditReply req = 
                            serializer.replyFromString(
                                    ((TextMessage) msg)
                                            .getText());
                    for (CallBack<CreditReply> callback : callbacks) {
                        callback.call(req);
                    }
                } catch (JMSException ex) {
                    Logger.getLogger(CreditGateway.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    public void addListener(CallBack<CreditReply> listener) {
        this.callbacks.add(listener);
    }
    
    public void sendRequest(CreditRequest request) {
        Message msg = this.gateway.createMsg(
                this.serializer.requestToString(request));
        this.gateway.send(msg);
    }
    
    public void start() {
        this.gateway.start();
    }
}
