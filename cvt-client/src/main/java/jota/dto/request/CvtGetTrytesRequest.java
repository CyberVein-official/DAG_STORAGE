package jota.dto.request;

import jota.CvtAPICommands;

/**
 * This class represents the core API request 'getTrytes'.
 **/
public class CvtGetTrytesRequest extends CvtCommandRequest {

    private String[] hashes;

    /**
     * Initializes a new instance of the CvtGetTrytesRequest class.
     */
    private CvtGetTrytesRequest(final String... hashes) {
        super(CvtAPICommands.GET_TRYTES);
        this.hashes = hashes;
    }

    /**
     * Create a new instance of the CvtGetTrytesRequest class.
     */
    public static CvtGetTrytesRequest createGetTrytesRequest(String... hashes) {
        return new CvtGetTrytesRequest(hashes);
    }

    /**
     * Gets the hashes.
     *
     * @return The hashes.
     */
    public String[] getHashes() {
        return hashes;
    }

    /**
     * Sets the hashes.
     *
     * @param hashes The hashes.
     */
    public void setHashes(String[] hashes) {
        this.hashes = hashes;
    }
}
