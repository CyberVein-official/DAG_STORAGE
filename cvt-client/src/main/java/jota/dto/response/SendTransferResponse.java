package jota.dto.response;

import jota.model.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of api request 'sendTransfer'.
 **/
public class SendTransferResponse extends AbstractResponse {

    private List<Transaction> transactions = new ArrayList<>();
    private Boolean[] successfully;

    /**
     * Initializes a new instance of the SendTransferResponse class.
     */
    public static SendTransferResponse create(List<Transaction> transactions, Boolean[] successfully, long duration) {
        SendTransferResponse res = new SendTransferResponse();
        res.transactions = transactions;
        res.successfully = successfully;
        res.setDuration(duration);
        return res;
    }

    /**
     * Gets the transactions.
     *
     * @return The transactions.
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * Sets the transactions.
     *
     * @param transactions The transactions.
     */

}
