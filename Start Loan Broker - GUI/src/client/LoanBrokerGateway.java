/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

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
    private final ClientSerializer serializer;
    
    private ArrayList<CallBack<ClientReply>> callbacks;

    public LoanBrokerGateway(String requestQueue, String replyQueue) {
        this.gateway = new MessagingGateway(requestQueue, replyQueue);
        this.serializer = new ClientSerializer();
        
        this.callbacks = new ArrayList<CallBack<ClientReply>>();
        this.gateway.setListener(new MessageListener() {
            public void onMessage(Message msg) {
                try {
                    ClientReply req = 
                            serializer.replyFromString(
                                    ((TextMessage) msg)
                                            .getText());
                    for (CallBack<ClientReply> callback : callbacks) {
                        callback.call(req);
                    }
                } catch (JMSException ex) {
                    Logger.getLogger(LoanBrokerGateway.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    public void addListener(CallBack<ClientReply> listener) {
        this.callbacks.add(listener);
    }
    
    public void sendRequest(ClientRequest request) {
        Message msg = this.gateway.createMsg(
                this.serializer.requestToString(request));
        this.gateway.send(msg);
    }
    
    public void start() {
        this.gateway.start();
    }
    
}
