/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker.bank;

import bank.BankQuoteRequest;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import messaging.GatewayException;
import messaging.SenderMessagingGateway;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

/**
 * This class contains a collection of SenderGateways for the banks. Given a
 * secific request, it determmines to which banks the request has to be sent.
 *
 * @author Maja Pesic
 */
public class BanksSenderRouter
{

    /**
     * The list of senders of all participating banks.
     */
    private ArrayList<BankSender> banks;

    /**
     * The only constructor.
     */
    public BanksSenderRouter()
    {
        banks = new ArrayList<>();
    }

    void addBank(String factory, String destination, String expression)
    {
        try
        {
            banks.add(new BankSender(factory, destination, expression));
        }
        catch (Exception ex)
        {
            Logger.getLogger(BanksSenderRouter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Iterable<SenderMessagingGateway> getEligibleBanks(BankQuoteRequest request)
    {
        ArrayList<SenderMessagingGateway> result = new ArrayList<>();
        for (BankSender bank : banks)
        {
            if (bank.canHandleRequest(request))
            {
                result.add(bank.getSender());
            }
        }
        return result;
    }

    public void start()
            throws GatewayException
    {
        try
        {
            for (BankSender bank : banks)
            {
                bank.getSender().start();
            }
        }
        catch (JMSException ex)
        {
            throw new GatewayException("An error has occured in starting a gateway.", ex);
        }
    }

    /**
     * This class represents the sender to ONE bank.
     */
    private class BankSender
    {
        /*
         one sender for one bank, you can either:
         - use existing class MessagingGateway and use it only for sending, or
         - create a new class SenderMessagingGateway that only can send, and can not receive 
         */

        private SenderMessagingGateway sender;

        private final String expression;
        private final Evaluator evaluator;

        BankSender(String factoryName, String destinationName, String expression)
                throws Exception
        {
            this.sender = new SenderMessagingGateway(destinationName);
            this.expression = expression;
            this.evaluator = new Evaluator();
        }

        /**
         * returns whether this bank is willing to handle this loan request.
         *
         * @param request
         * @return
         */
        public boolean canHandleRequest(BankQuoteRequest request)
        {
            if (expression != null)
            {
                try
                {
                    evaluator.putVariable("amount", Integer.toString(request.getAmount()));
                    evaluator.putVariable("credit", Integer.toString(request.getCredit()));
                    evaluator.putVariable("history", Integer.toString(request.getHistory()));

                    String result = evaluator.evaluate(expression);
                    return result.equals("1.0"); //1.0 means: result of evaluation is positive (true)
                }
                catch (EvaluationException ex)
                {
                    Logger.getLogger(BankSender.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
            else
            {
                return true;
            }
        }

        public SenderMessagingGateway getSender()
        {
            return sender;
        }
    }
}
