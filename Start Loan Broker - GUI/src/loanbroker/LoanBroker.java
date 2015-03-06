/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import bank.BankQuoteReply;
import bank.BankQuoteRequest;
import client.ClientReply;
import client.ClientRequest;
import creditbureau.CreditReply;
import creditbureau.CreditRequest;
import loanbroker.gui.LoanBrokerFrame;
import messaging.MessagingGateway.CallBack;

/**
 *
 * @author Maja Pesic
 */
public class LoanBroker {

    private final ClientGateway clientGateway;
    private final CreditGateway creditGateway;
    private final BankGateway bankIngGateway;

    private LoanBrokerFrame frame; // GUI

    /**
     * Initializes attributes, and registers itself (method onClinetRequest) as
     * the listener for new client requests
     * @param clientRequestQueue
     * @param clientReplyQueue
     * @param creditRequestQueue
     * @param creditReplyQueue
     * @param bankRequestQueue
     * @param bankReplyQueue
     * @throws java.lang.Exception
     */
    public LoanBroker(String clientRequestQueue, String clientReplyQueue, String creditRequestQueue, String creditReplyQueue, String bankRequestQueue, String bankReplyQueue) throws Exception {
        super();
        
        this.clientGateway = new ClientGateway(clientReplyQueue, clientRequestQueue);
        this.creditGateway = new CreditGateway(creditReplyQueue, creditRequestQueue);
        this.bankIngGateway = new BankGateway(bankReplyQueue, bankRequestQueue);

        this.clientGateway.addListener(new CallBack<ClientRequest>(){
            public void call(ClientRequest val) {
                onClientRequest(val);
            }
        });

        this.creditGateway.addListener(new CallBack<CreditReply>(){
            public void call(CreditReply val) {
                onCreditReply(val);
            }
        });

        this.bankIngGateway.addListener(new CallBack<BankQuoteReply>(){
            public void call(BankQuoteReply val) {
                onBankReply(val);
            }
        });
        
        /*
         * Make the GUI
         */
        frame = new LoanBrokerFrame();
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                frame.setVisible(true);
            }
        });
    }

    /**
     * This method is called when a new client request arrives.
     * It generates a CreditRequest and sends it to the CreditBureau.
     * @param message the incomming message containng the ClientRequest
     */
    private void onClientRequest(ClientRequest request) {
        frame.addObject(null, request);
        CreditRequest credit = createCreditRequest(request);
        this.creditGateway.sendRequest(credit);
    }
    
    /**
     * This method is called when a new credit reply arrives.
     * It generates a BankQuoteRequest and sends it to the Bank.
     * @param message the incomming message containng the CreditReply
     */
    private void onCreditReply(CreditReply reply) {
        frame.addObject(null, reply); // add the reply to the GUI
        BankQuoteRequest bank = createBankRequest(null, reply); // generate BankQuoteRequest
        this.bankIngGateway.sendRequest(bank);
    }
    
    /**
     * This method is called when a new bank quote reply arrives.
     * It generates a ClientReply and sends it to the LoanTestClient.
     * @param message the incomming message containng the BankQuoteReply
     */
    private void onBankReply(BankQuoteReply reply) {
        frame.addObject(null, reply); // add the reply to the GUI
        ClientReply client = createClientReply(reply); // generate ClientReply
        this.clientGateway.sendReply(client);
    }
    /**
     * Generates a credit request based on the given client request.
     * @param clientRequest
     * @return
     */
    private CreditRequest createCreditRequest(ClientRequest clientRequest) {
        return new CreditRequest(clientRequest.getSSN());
    }
    /**
     * Generates a bank quote request based on the given client request and credit reply.
     * @param creditReply
     * @return
     */
    private BankQuoteRequest createBankRequest(ClientRequest clientRequest, CreditReply creditReply) {
        int ssn = creditReply.getSSN();
        int score = creditReply.getCreditScore();
        int history = creditReply.getHistory();
        int amount = 100; // this must be hard coded because we don't know to which clientRequest this creditReply belongs to!!! 
        int time = 24;   // this must be hard coded because we don't know to which clientRequest this creditReply belongs to!!! 
        if (clientRequest != null){
            amount = clientRequest.getAmount();
            time = clientRequest.getTime();
        }
        return  new BankQuoteRequest(ssn, score, history, amount, time);
    }
    /**
     * Generates a client reply based on the given bank quote reply.
     * @param creditReply
     * @return
     */
    private ClientReply createClientReply(BankQuoteReply reply) {
        return new ClientReply(reply.getInterest(), reply.getQuoteId());
    }

    /**
     * Opens connection to JMS,so that messages can be send and received.
     */
    public void start() {
        this.clientGateway.start();
        this.creditGateway.start();
        this.bankIngGateway.start();
    }
}
