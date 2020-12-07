package jota.dto.response;

import jota.dto.request.CvtStoreTransactionsRequest;

/**
 * Response of {@link CvtStoreTransactionsRequest}.
 **/
public class StoreTransactionsResponse extends AbstractResponse {

    /**
     * Initializes a new instance of the StoreTransactionsResponse class.
     */
    public StoreTransactionsResponse(long duration) {
        setDuration(duration);
    }
}

