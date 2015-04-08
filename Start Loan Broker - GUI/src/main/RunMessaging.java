package main;

import bank.Bank;
import client.ClientRequest;
import client.LoanTestClient;
import creditbureau.CreditBureau;
import java.util.logging.Level;
import java.util.logging.Logger;
import loanbroker.LoanBroker;

/**
 * This application tests the LoanBroker system.
 *
 */
public class RunMessaging
{

    public static void main(String[] args)
    {
        try
        {
            // read the queue names from file "MESSAGING.ini"
            JMSSettings.init("MESSAGING_CHANNELS.ini");
            JMSSettings.setRunMode(JMSSettings.RunMode.AUTOMATICALLY); // this means that the Bank will automatically generate an Interest rate and return it!
            final String clientRequestQueue = JMSSettings.get(JMSSettings.LOAN_REQUEST);
            final String clientReplyQueue = JMSSettings.get(JMSSettings.LOAN_REPLY);
            final String clientReplyQueue2 = JMSSettings.get(JMSSettings.LOAN_REPLY_2);
            final String creditRequestQueue = JMSSettings.get(JMSSettings.CREDIT_REQUEST);
            final String creditReplyQueue = JMSSettings.get(JMSSettings.CREDIT_REPLY);
            final String ingRequestQueue = JMSSettings.get(JMSSettings.BANK_1);
            final String rabobankRequestQueue = JMSSettings.get(JMSSettings.BANK_2);
            final String abnambroRequestQueue = JMSSettings.get(JMSSettings.BANK_3);
            final String bankReplyQueue = JMSSettings.get(JMSSettings.BANK_REPLY);

            // create a LoanBroker middleware
            LoanBroker broker = new LoanBroker(clientRequestQueue, creditRequestQueue, creditReplyQueue, bankReplyQueue);
            broker.addBank(ingRequestQueue, "#{amount} > 75000 && #{credit} > 600 && #{history} > 8");
            broker.addBank(rabobankRequestQueue, "#{amount} > 10000 && #{amount} < 75000 && #{credit} > 400 && #{history} > 3");
            broker.addBank(abnambroRequestQueue, "#{amount} > 70000 && #{credit} > 500 && #{history} > 5");

            // create a Client Application
            LoanTestClient client = new LoanTestClient("The Hypotheker", clientRequestQueue, clientReplyQueue);
            LoanTestClient client2 = new LoanTestClient("Fontys", clientRequestQueue, clientReplyQueue2);

            // create the CreditBureau Application
            CreditBureau creditBureau = new CreditBureau(creditRequestQueue);

            // create one Bank application
            Bank ing = new Bank("ING", ingRequestQueue);
            Bank rabo = new Bank("Rabobank", rabobankRequestQueue);
            Bank abnamro = new Bank("ABN AMRO", abnambroRequestQueue);

            // open all connections in the broker, client and credit applications
            broker.start();
            
            creditBureau.start();
            
            ing.start();
            rabo.start();
            abnamro.start();
            
            client.start();
            client2.start();

            // send three requests
            // Wont use these, will break the JMS somehow?
            //client.sendRequest(new ClientRequest(1, 100000, 24));
            //client2.sendRequest(new ClientRequest(1, 1050, 10));
            //client.sendRequest(new ClientRequest(2, 88888, 5));
            //client.sendRequest(new ClientRequest(3, 100, 5));
            //client2.sendRequest(new ClientRequest(2, 600, 20));
            //client2.sendRequest(new ClientRequest(3, 9001, 5));
        }
        catch (Exception ex)
        {
            Logger.getLogger(RunMessaging.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
