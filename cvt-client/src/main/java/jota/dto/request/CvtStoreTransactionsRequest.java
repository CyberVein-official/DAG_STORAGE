package jota.dto.request;

import jota.CvtAPICommands;

/**
 * This class represents the core API request 'getTransactionsToApprove'.
 *
 * It stores transactions into the local storage. The trytes to be used for this call are returned by attachToTangle.
 **/
public class CvtStoreTransactionsRequest extends CvtCommandRequest {

    private String[] trytes;

    /**
     * Initializes a new instance of the CvtStoreTransactionsRequest class.
     */
    private CvtStoreTransactionsRequest(final String... trytes) {
        super(CvtAPICommands.STORE_TRANSACTIONS);
        this.trytes = trytes;
    }

    /**
     * Create a new instance of the CvtStoreTransactionsRequest class.
     */
    public static CvtStoreTransactionsRequest createStoreTransactionsRequest(final String... trytes) {
        return new CvtStoreTransactionsRequest(trytes);
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
}
