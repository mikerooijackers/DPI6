package client;

import client.gui.ClientFrame;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import messaging.requestreply.IReplyListener;

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
        
         // create the GUI
        frame = new ClientFrame(name) {

            @Override
            public void send(ClientRequest request) {
                sendRequest(request);
            }
        };

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
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
        try
        {
            gateway.sendRequest(request, new IReplyListener<ClientRequest, ClientReply>()
            {

                @Override
                public void onReply(ClientRequest request, ClientReply reply)
                {
                    frame.addReply(request, reply);
                }
            });
            frame.addRequest(request);
        }
        catch (Exception ex)
        {
            Logger.getLogger(LoanTestClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Opens connection to JMS,so that messages can be send and received.
     */
    public void start() throws JMSException {
        this.gateway.start();
    }
}