<<<<<<< HEAD
package jota.dto.request;

import jota.CvtAPICommands;

/**
 * This class represents the core API request 'broadcastTransaction'.
 *
 * Broadcast a list of transactions to all neighbors. The input trytes for this call are provided by attachToTangle
 **/
public class CvtBroadcastTransactionRequest extends CvtCommandRequest {

    private String[] trytes;

    /**
     * Initializes a new instance of the CvtBroadcastTransactionRequest class.
     */
    private CvtBroadcastTransactionRequest(final String... trytes) {
        super(CvtAPICommands.BROADCAST_TRANSACTIONS);
        this.trytes = trytes;
    }

    /**
     * Initializes a new instance of the CvtBroadcastTransactionRequest class.
     */
    public static CvtBroadcastTransactionRequest createBroadcastTransactionsRequest(final String... trytes) {
        return new CvtBroadcastTransactionRequest(trytes);
    }

    /**
     * Gets the trytes.
     *
     * @return The trytes.
     */
    public String[] getTrytes() {
        return trytes;
    }

    /**
     * Sets the trytes.
     *
     * @param trytes The trytes.
     */
    public void setTrytes(String[] trytes) {
        this.trytes = trytes;
    }

=======
package jota.dto.request;

import jota.CvtAPICommands;

/**
 * This class represents the core API request 'broadcastTransaction'.
 *
 * Broadcast a list of transactions to all neighbors. The input trytes for this call are provided by attachToTangle
 **/
public class CvtBroadcastTransactionRequest extends CvtCommandRequest {

    private String[] trytes;

    /**
     * Initializes a new instance of the CvtBroadcastTransactionRequest class.
     */
    private CvtBroadcastTransactionRequest(final String... trytes) {
        super(CvtAPICommands.BROADCAST_TRANSACTIONS);
        this.trytes = trytes;
    }

    /**
     * Initializes a new instance of the CvtBroadcastTransactionRequest class.
     */
    public static CvtBroadcastTransactionRequest createBroadcastTransactionsRequest(final String... trytes) {
        return new CvtBroadcastTransactionRequest(trytes);
    }

>>>>>>> 05f4f06df55719f449d00a6466e1ae45de137138
}