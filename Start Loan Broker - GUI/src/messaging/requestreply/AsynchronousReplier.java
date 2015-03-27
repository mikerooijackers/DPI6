package messaging.requestreply;

import java.util.HashMap;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import messaging.MessagingGateway;

/**
 *
 * @param <REQUEST>
 * @param <REPLY>
 * @author Maja Pesic
 */
public class AsynchronousReplier<REQUEST, REPLY> {

    /**
     * For sending and receiving messages
     */
    private MessagingGateway gateway;
    /**
     * For each request, we register the message that brought request. We need
     * the message later, to get the RetournAddress to which we will send the
     * reply.
     */
    private Map<REQUEST, Message> activeRequests = null;
    /**
     * The serializer for domain classes REQUEST and REPLY
     */
    private IRequestReplySerializer<REQUEST, REPLY> serializer = null;
    /**
     * The listener that will be informed when each request arrives.
     */
    private IRequestListener<REQUEST> requestListener = null;

    /**
     * This constructor: 1. intitiates the serializer, receiver and
     * activeRequests 2. registeres a message listener for the MessagingGateway
     * (method onMessage)
     *
     * @param requestReceiverQueue is the name of teh JMS queue from which the
     * requests will be received.
     * @param serializer used to de-serialize REQUESTs and serialize REPLIES.
     * @throws java.lang.Exception
     */
    public AsynchronousReplier(String requestReceiverQueue, IRequestReplySerializer<REQUEST, REPLY> serializer) throws Exception {
        this.serializer = serializer;
        this.gateway = new MessagingGateway(null, requestReceiverQueue);
        this.gateway.setListener(new MessageListener() {

            @Override
            public void onMessage(Message message) {
                onRequest((TextMessage) message);
            }
        });
        this.activeRequests = new HashMap<>();
    }

    /**
     * sets the listener that will be notified when each request arriives
     *
     * @param requestListener
     */
    public void setRequestListener(IRequestListener<REQUEST> requestListener) {
        this.requestListener = requestListener;
    }

    /**
     * Opens the jms connection of the Messaging Gateway in order to start
     * sending/receiving requests.
     */
    public void start() throws JMSException {
        gateway.start();
    }

    /**
     * This method is invoked every time a new request arrives
     *
     * @todo Implement this method. It should: 1. de-serialize the message into
     * a REQUEST 2. register the message to belong to the REQUEST 3. notify the
     * listener about the REQUEST arrival
     * @param message the incomming message containing the request
     */
    private synchronized void onRequest(TextMessage message) {
        try {
            REQUEST request = this.serializer.requestFromString(message.getText());
            this.activeRequests.put(request, message);
            requestListener.receivedRequest(request);
        } catch (JMSException ex) {
            Logger.getLogger(AsynchronousReplier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sends the reply for a specific request.
     *
     * @throws javax.jms.JMSException
     * @todo implement the following: 1. get the requestMessage registered for
     * the request from activeRequests 2. serialize the reply and create the
     * replyMessage for it 3. set the JMSCorrelationID of the replyMessage to be
     * equal to the JMSMessageID of the requestMessage 4. get the getJMSReplyTo
     * destination of the requestMessage 5. send the replyMessage to this
     * Destination; use method send(Message m, Destination d) in
     * MessagingGateway
     *
     * @param request to which this reply belongs
     * @param reply to the request
     * @return true if the reply is sent succefully; false if sending reply
     * fails
     */
    public synchronized boolean sendReply(REQUEST request, REPLY reply) throws JMSException {
        Message requestMessage = activeRequests.get(request);
        String replyBody = serializer.replyToString(reply);
        Message replyMessage = gateway.createMessage(replyBody);
        replyMessage.setJMSCorrelationID(requestMessage.getJMSMessageID());
        Destination destination = requestMessage.getJMSReplyTo();
        gateway.sendMessage(destination, replyMessage);
        activeRequests.remove(request);
        return true;
    }
}
