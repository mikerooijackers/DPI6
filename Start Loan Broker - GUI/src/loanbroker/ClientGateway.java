/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import client.ClientReply;
import client.ClientRequest;
import client.ClientSerializer;
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
public class ClientGateway {
    
    private final MessagingGateway gateway;
    private ClientSerializer serializer;
    
    private ArrayList<CallBack<ClientRequest>> callbacks;

    public ClientGateway(String requestQueue, String replyQueue) {
        this.gateway = new MessagingGateway(replyQueue, requestQueue);
        this.serializer = new ClientSerializer();

        this.callbacks = new ArrayList<CallBack<ClientRequest>>();

        this.gateway.setListener(new MessageListener() {
            public void onMessage(Message msg) {
                try {
                    ClientRequest req = 
                            serializer.requestFromString(
                                    ((TextMessage) msg)
                                            .getText());
                    for (CallBack<ClientRequest> callback : callbacks) {
                        callback.call(req);
                    }
                } catch (JMSException ex) {
                    Logger.getLogger(ClientGateway.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    public void addListener(CallBack<ClientRequest> listener) {
        this.callbacks.add(listener);
    }
    
    public void sendReply(ClientReply reply) {
        Message msg = this.gateway.createMsg(
                this.serializer.replyToString(reply));
        this.gateway.send(msg);
    }
    
    public void start() {
        this.gateway.start();
    }
}
