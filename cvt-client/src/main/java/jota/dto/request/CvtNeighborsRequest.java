package jota.dto.request;

import jota.CvtAPICommands;

/**
 * This class represents the core API request 'addNeighbors' and 'removeNeighbors'.
 **/
public class CvtNeighborsRequest extends CvtCommandRequest {

    private String[] uris;

    /**
     * Initializes a new instance of the CvtNeighborsRequest class.
     */
    private CvtNeighborsRequest(CvtAPICommands type, final String... uris) {
        super(type);
        this.uris = uris;
    }

    /**
     * Create a new instance of the CvtNeighborsRequest class.
     */
    public static CvtNeighborsRequest createAddNeighborsRequest(String... uris) {
        return new CvtNeighborsRequest(CvtAPICommands.ADD_NEIGHBORS, uris);
    }

    /**
     * Create a new instance of the CvtNeighborsRequest class.
     */
    public static CvtNeighborsRequest createRemoveNeighborsRequest(String... uris) {
        return new CvtNeighborsRequest(CvtAPICommands.REMOVE_NEIGHBORS, uris);
    }

    /**
     * Gets the uris.
     *
     * @return The uris.
     */
    public String[] getUris() {
        return uris;
    }

    /**
     * Sets the uris.
     *
     * @param uris The uris.
     */
    public void setUris(String[] uris) {
        this.uris = uris;
    }
}

