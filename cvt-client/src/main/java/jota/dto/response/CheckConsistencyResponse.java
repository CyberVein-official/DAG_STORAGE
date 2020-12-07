package jota.dto.response;

import jota.dto.request.CvtCheckConsistencyRequest;

/**
 * Response of {@link CvtCheckConsistencyRequest}.
 **/
public class CheckConsistencyResponse extends AbstractResponse {

    private boolean state;
    private String info;

    /**
     * Gets the state.
     *
     * @return The state.
     */
    public boolean getState() {
        return state;
    }

    /**
     * If state is false, this provides information on the cause of the inconsistency.
     * @return
     */
    public String getInfo() {
        return info;
    }
}
