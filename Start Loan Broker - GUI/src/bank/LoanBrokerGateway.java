/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bank;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import loanbroker.bank.BankGateway;
import messaging.requestreply.AsynchronousReplier;
import messaging.requestreply.IRequestListener;

/**
 *
 * @author mikerooijackers
 */
public abstract class LoanBrokerGateway extends AsynchronousReplier<BankQuoteRequest, BankQuoteReply>
{
    
    public LoanBrokerGateway(String requestQueue)
            throws Exception
    {
        super(requestQueue, new BankSerializer());
        super.setRequestListener(new IRequestListener<BankQuoteRequest>()
        {
            
            @Override
            public void receivedRequest(BankQuoteRequest request)
            {
                onBankQuoteRequestReceived(request);
            }
        });
    }
    
    public abstract void onBankQuoteRequestReceived(BankQuoteRequest request);

    @Override
    public void beforeReply(Message request, Message reply)
    {
        try
        {
            super.beforeReply(request, reply);
            int aggregationId = request.getIntProperty(BankGateway.AGGREGATION_CORRELATION);
            reply.setIntProperty(BankGateway.AGGREGATION_CORRELATION, aggregationId);
        }
        catch (JMSException ex)
        {
            Logger.getLogger(LoanBrokerGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
