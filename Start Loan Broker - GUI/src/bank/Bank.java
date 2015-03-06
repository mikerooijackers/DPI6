package bank;

import bank.gui.BankFrame;
import java.util.Random;
import main.JMSSettings;
import main.JMSSettings.RunMode;
import messaging.MessagingGateway.CallBack;

/**
 * This class represents one Bank Application.
 * It should eventually:
 *  1. Receive BankQuoteRequest-s for a loan from the LoanBroker Messaging-Oriented Middleware (MOM).
 *  2. Randomly create BankQuoteReply for each request (use method "computeBankReply").
 *  3. Send the BankQuoteReply from the LoanBroker MOM.
 */
public class Bank {

    private static final int ERROR_CODE = 1;
    private static final int NO_ERROR_CODE = 0;
    private final double primeRate = 3.5;
    private final String name;
    private final double ratePremium = 0.5;
    private final int maxLoanTerm = 10000;
    private int quoteCounter = 0;
    protected Random random = new Random();
    private BankFrame frame; // GUI

    /**
     * attributes for connection to JMS
     */
    private final LoanBrokerGateway gateway;
    
    public Bank(String bankName, String bankRequestQueue, String bankReplyQueue) throws Exception {
        super();
        this.name = bankName;
        
        this.gateway = new LoanBrokerGateway(bankReplyQueue, bankRequestQueue);

        this.gateway.addListener(new CallBack<BankQuoteRequest>() {
            public void call(BankQuoteRequest val) {
                onBankQuoteRequest(val);
            }
        });

        // create GUI
        frame = new BankFrame(this, name);
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                frame.setVisible(true);
            }
        });
    }
    
    public boolean onSendBankReplyClicked(BankQuoteRequest request, double interest, int error)
    {
         BankQuoteReply reply = createReply(interest,error);
         return sendReply(request, reply);
    }
    
    public BankQuoteReply createReply(double interest, int error){
       String quoteID = name + "-" + String.valueOf(++quoteCounter);
       return new BankQuoteReply(interest, quoteID, error);
    }

    /**
     * Processes a new request message. Only if the debug_mode is true, this method
     * randomly generates a reply and sends it back.
     * @param message
     */
    private void onBankQuoteRequest(BankQuoteRequest request) {
        frame.addRequest(request);
        if (JMSSettings.getRunMode() == RunMode.AUTOMATICALLY) { // only in automatic mode send immediately random reply
            BankQuoteReply reply = computeReplyRandomly(request);
            Bank.this.sendReply(request, reply);
        }
    }

    /**
     * Sends the reply for one request.
     * @param request for which the reply is sent
     * @param reply
     * @return true if the reply is successfully sent, false if sending fails
     */
    private boolean sendReply(BankQuoteRequest request, BankQuoteReply reply) {
        this.gateway.sendReply(reply);
        frame.addReply(request, reply);
        return true;
    }

    /**
     * Randomly generates a reply for the given request.
     * @param request for which the reply should be generated.
     * @return randomly generated reply
     */
    private BankQuoteReply computeReplyRandomly(BankQuoteRequest request) {

        double interest = 0.0;
        int error = ERROR_CODE;
        if (request.getTime() <= maxLoanTerm) {

            double temp = primeRate + ratePremium + (double) (request.getTime() / 12) / 10 + (double) random.nextInt(10) / 10;
            interest = round(temp, 1); // round to one decimal

            error = NO_ERROR_CODE;
        }
        return createReply(interest,error);

    }

    /**
     * Rounds a decimal number.
     * @param value is the decimal value to be rounded.
     * @param decimals the number of decimal places after rounding.
     * @return
     */
    private double round(double value, int decimals) {
        final int temp = (int) Math.pow(10, decimals);// temp will be 1 or 10 or 100 or 1000 or , ...
        double result = Math.round(value * temp);
        result = result / temp;
        return result;
    }

    /**
     * Opens connection to JMS,so that messages can be send and received.
     */
    public void start() {
        this.gateway.start();
    }
}
