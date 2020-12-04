package jota.dto.response;

import jota.dto.request.CvtFindTransactionsRequest;

/**
 * Response of {@link CvtFindTransactionsRequest}.
 **/
public class FindTransactionResponse extends AbstractResponse {

    String[] hashes;


    /**
     * Gets the hashes.
     *
     * @return The hashes.
     */
    public String[] getHashes() {
        return hashes;
    }
}
