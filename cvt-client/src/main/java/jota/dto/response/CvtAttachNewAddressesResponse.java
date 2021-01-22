package jota.dto.response;

import jota.dto.request.CvtWereAddressesSpentFromRequest;

/**
 * Response of {@link CvtWereAddressesSpentFromRequest}.
 **/
public class CvtAttachNewAddressesResponse extends AbstractResponse {

    private String address;

    /**
     * Gets the states.
     *
     * @return The states.
     */
    public String getAddress() {
        return address;
    }
}
