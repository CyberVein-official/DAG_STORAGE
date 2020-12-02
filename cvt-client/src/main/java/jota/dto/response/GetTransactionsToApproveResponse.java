package jota.dto.response;

import jota.dto.request.CvtGetTransactionsToApproveRequest;

/**
 * Response of {@link CvtGetTransactionsToApproveRequest}.
 **/
public class GetTransactionsToApproveResponse extends AbstractResponse {

    private String trunkTransaction;
    private String branchTransaction;

    /**
     * Gets the trunk transaction.
     *
     * @return The trunk transaction.
     */
    public String getTrunkTransaction() {
        return trunkTransaction;
    }

    /**
     * Gets the branch transaction.
     *
     * @return The branch transaction.
     */
    public String getBranchTransaction() {
        return branchTransaction;
    }
}