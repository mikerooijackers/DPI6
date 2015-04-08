/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker.bank;

import bank.BankQuoteReply;
import bank.BankQuoteRequest;
import bank.BankSerializer;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import messaging.GatewayException;
import messaging.ReceiverMessagingGateway;
import messaging.SenderMessagingGateway;
import messaging.requestreply.IReplyListener;

/**
 *
 * @author Maja Pesic
 */
public class BankGateway
{

    public static final String AGGREGATION_CORRELATION = "aggregation";
    private int aggregateCounter = 0; // counting bank requests

    private BanksSenderRouter sender; // separate sender for each bank
    /*
     one receiver for all banks, you can either:
     - use existing class MessagingGateway and use it only for receiving, or
     - create a new class ReceiverMessagingGateway that only can receive, and can not send
     */
    private ReceiverMessagingGateway receiver;

    private BankSerializer serializer; // serializing bank requests and replies

    private HashMap<Integer, BankQuoteAggregate> replyAggregate; // storing one aggregate (of replies) for each BankQuoteRequests

    /**
     * initialize attributes,register as receiver listener.
     *
     * @param factoryName
     * @param receiveDestination
     * @throws messaging.GatewayException
     */
    public BankGateway(String factoryName, String receiveDestination)
            throws GatewayException
    {
        try
        {
            sender = new BanksSenderRouter();
            receiver = new ReceiverMessagingGateway(receiveDestination);

            receiver.setMessageListener(new MessageListener()
            {

                @Override
                public void onMessage(Message message)
                {
                    messageReceived((TextMessage) message);
                }
            });
            serializer = new BankSerializer();
            replyAggregate = new HashMap<>();
        }
        catch (Exception ex)
        {
            throw new GatewayException("An error has occured in creating the gateway.", ex);
        }
    }

    public void addBank(String factory, String destination, String expression)
    {
        sender.addBank(factory, destination, expression);
    }

    /**
     * @todo Implement the following method: 1. get the AGGREGATION_CORRELATION
     * of the received message 2. get the BankQuoteAggregate that is registered
     * for this AGGREGATION_CORRELATION 3. de-serialize the message into a
     * BankQuoteReply 4. add the BankQuoteReply to the BankQuoteAggregate 5. if
     * this is the last expected reply, 5.a. notify the BankQuoteAggregate
     * listener and u 5.b. unregister the BankQuoteAggregate
     * @param msg the message that has just been received
     */
    private synchronized void messageReceived(TextMessage msg)
    {
        try
        {
            int aggregationId = msg.getIntProperty(BankGateway.AGGREGATION_CORRELATION);
            BankQuoteAggregate aggregate = replyAggregate.get(aggregationId);
            BankQuoteReply bankReply = serializer.replyFromString(msg.getText());
            if (aggregate.addReply(bankReply))
            {
                aggregate.notifyListener();
                replyAggregate.remove(aggregationId);
            }
        }
        catch (JMSException ex)
        {
            Logger.getLogger(BankGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @todo Implement the following method: 1. serialize the request into a
     * string 2. get all eligible banks (SenderMessagingGateway) for this
     * request and count them 3. for each eligible bank: 3a. create a new
     * message for the request 3b. set the JMSReplyTo to the receiver's
     * destination 3c. set the AGGREGATION_CORRELATION to the current
     * aggregateCounter 3d. let the bank send the message 4. if the message was
     * sent to at least one bank: 4a. create and register a new
     * BankQuoteAggregate for the current value of the aggregateCounter 4b.*
     * increase the aggregateCounter 5. if there was no eligible banks (no
     * message was sent), create a new BankQuoteReply(0, "There are no eligible
     * banks for this loan.", 10) and notify the listener about its 'arrival'.
     * @param request
     * @param listener
     */
    public synchronized void sendRequest(BankQuoteRequest request, IReplyListener<BankQuoteRequest, BankQuoteReply> listener)
    {
        String body = serializer.requestToString(request);
        Iterable<SenderMessagingGateway> banks = sender.getEligibleBanks(request);
        int banksSend = 0;
        for (SenderMessagingGateway bank : banks)
        {
            try
            {
                Message message = bank.createMessage(body);
                message.setJMSReplyTo(receiver.getDestination());
                message.setIntProperty(BankGateway.AGGREGATION_CORRELATION, this.aggregateCounter);
                bank.send(message);
                banksSend++;
            }
            catch (JMSException ex)
            {
                Logger.getLogger(BankGateway.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (banksSend == 0)
        {
            BankQuoteReply reply = new BankQuoteReply(0, "There are no eligible banks for this loan.", 10);
            listener.onReply(request, reply);
        }
        else
        {
            BankQuoteAggregate aggregate = new BankQuoteAggregate(request, banksSend, listener);
            this.replyAggregate.put(this.aggregateCounter, aggregate);
            this.aggregateCounter++;
        }
    }

    public void start()
            throws GatewayException
    {
        try
        {
            receiver.start();
            sender.start();
        }
        catch (JMSException ex)
        {
            Logger.getLogger(BankGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
