package jota.dto.request;

import jota.CvtAPICommands;

/**
 * This class represents the core API request 'getTransactionsToApprove'.
 **/
public class CvtGetTransactionsToApproveRequest extends CvtCommandRequest {

    private Integer depth;
    private String reference;

    /**
     * Initializes a new instance of the CvtGetTransactionsToApproveRequest class.
     */
    private CvtGetTransactionsToApproveRequest(final Integer depth, final String reference) {
        super(CvtAPICommands.GET_TRANSACTIONS_TO_APPROVE);
        this.depth = depth;
        this.reference = reference;
    }

    /**
     * Create a new instance of the CvtGetTransactionsToApproveRequest class.
     */
    public static CvtGetTransactionsToApproveRequest createCvtGetTransactionsToApproveRequest(Integer depth, final String reference) {
        return new CvtGetTransactionsToApproveRequest(depth, reference);
    }
}

