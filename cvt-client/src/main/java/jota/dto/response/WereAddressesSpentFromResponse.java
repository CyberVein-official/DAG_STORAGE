package jota.dto.response;

import jota.dto.request.CvtWereAddressesSpentFromRequest;

/**
 * Response of {@link CvtWereAddressesSpentFromRequest}.
 **/
public class WereAddressesSpentFromResponse extends AbstractResponse {

    private boolean[] states;

    /**
     * Gets the states.
     *
     * @return The states.
     */
    public boolean[] getStates() {
        return states;
    }
}
