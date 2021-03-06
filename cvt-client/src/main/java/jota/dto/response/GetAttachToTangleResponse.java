package jota.dto.response;

import jota.dto.request.CvtAttachToTangleRequest;

/**
 * Response of {@link CvtAttachToTangleRequest}.
 **/
public class GetAttachToTangleResponse extends AbstractResponse {

    private String[] trytes;

    /**
     * Initializes a new instance of the GetAttachToTangleResponse class.
     */
    public GetAttachToTangleResponse(long duration) {
        setDuration(duration);
    }

    /**
     * Initializes a new instance of the GetAttachToTangleResponse class with the given trytes.
     */
    public GetAttachToTangleResponse(String[] trytes) {
        setDuration(0L);
        this.trytes = trytes;
    }

    /**
     * Gets the rytes.
     *
     * @return The trytes.
     */
    public String[] getTrytes() {
        return trytes;
    }
}
