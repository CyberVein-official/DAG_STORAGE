package jota.dto.response;

import jota.dto.request.CvtNeighborsRequest;

/**
 * Response of {@link CvtNeighborsRequest}.
 **/
public class AddNeighborsResponse extends AbstractResponse {

    private int addedNeighbors;

    /**
     * Gets the number of added neighbors.
     *
     * @return The number of added neighbors.
     */
    public int getAddedNeighbors() {
        return addedNeighbors;
    }
}
