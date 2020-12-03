package jota.dto.response;

import jota.dto.request.CvtCommandRequest;

/**
 * Response of {@link CvtCommandRequest}.
 **/
public class GetTipsResponse extends AbstractResponse {

    private String[] hashes;

    /**
     * Gets the hashes.
     *
     * @return The hashes.
     */
    public String[] getHashes() {
        return hashes;
    }
}
