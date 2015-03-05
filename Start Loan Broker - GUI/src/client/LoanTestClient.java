package client;

import messaging.MessagingGateway.CallBack;
import client.gui.ClientFrame;

/**
 * This class represents one Client Application.
 * It:
 *  1. Creates a ClientRequest for a loan.
 *  2. Sends it to the LoanBroker Messaging-Oriented Middleware (MOM).
 *  3. Receives the reply from the LoanBroker MOM.
 * 
 */
public class LoanTestClient {
    private final LoanBrokerGateway gateway;
    private ClientFrame frame; // GUI

    /**
     * Create a loan test client.
     * @param name The name of the window.
     * @param requestQueue The queueu to get requests from.
     * @param replyQueue The queue to send replies over.
     * @throws Exception Possible jms exceptions or ui exceptions.
     */
    public LoanTestClient(String name, String requestQueue, String replyQueue) throws Exception {
        super();
        
        this.gateway = new LoanBrokerGateway(requestQueue, replyQueue);
        
        this.gateway.addListener(new CallBack<ClientReply>() {
            public void call(ClientReply val) {
                processLoanOffer(val);
            }
        });
        
         // create the GUI
        frame = new ClientFrame(name) {

            @Override
            public void send(ClientRequest request) {
                sendRequest(request);
            }
        };

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                frame.setVisible(true);
            }
        });
    }
    
    /**
     * Sends new loan request to the LoanBroker.
     * @param request
     */
    public void sendRequest(ClientRequest request) {
        this.gateway.sendRequest(request);
        this.frame.addRequest(request);
    }
    /**
     * This message is called whenever a new client reply message arrives.
     *  The message is de-serialized into a ClientReply, and the reply is shown in the GUI.
     * @param message
     */
    private void processLoanOffer(ClientReply reply) {
        frame.addReply(null, reply);
    }
    /**
     * Opens connection to JMS,so that messages can be send and received.
     */
    public void start() {
        this.gateway.start();
    }
}