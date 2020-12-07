package jota.dto.request;

import jota.CvtAPICommands;

/**
 * This class represents the core api request 'checkConsistency'.
 **/
public class CvtCheckConsistencyRequest extends CvtCommandRequest {

    private String[] tails;

    /**
     * Initializes a new instance of the CvtCheckConsistencyRequest class.
     */
    private CvtCheckConsistencyRequest(final String... tails) {
        super(CvtAPICommands.CHECK_CONSISTENCY);
        this.tails = tails;
    }

    /**
     * Create a new instance of the CvtGetBalancesRequest class.
     */
    public static CvtCheckConsistencyRequest create(final String... tails) {
        return new CvtCheckConsistencyRequest(tails);
    }

    /**
     * Gets the tails.
     *
     * @return The tails.
     */
    public String[] getTails() {
        return tails;
    }

    /**
     * Sets the tails.
     *
     * @param tails The tails.
     */
    public void setTails(String[] tails) {
        this.tails = tails;
    }
}

