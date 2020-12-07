package jota.dto.response;

import jota.dto.request.CvtNeighborsRequest;

/**
 * Response of {@link CvtNeighborsRequest}.
 **/
public class RemoveNeighborsResponse extends AbstractResponse {

    private int removedNeighbors;

    /**
     * Gets the number of removed neighbors.
     *
     * @return The number of removed neighbors.
     */
    public int getRemovedNeighbors() {
        return removedNeighbors;
    }
}
