package jota.dto.request;

import jota.CvtAPICommands;

/**
 * This class represents the core api request 'getNodeInfo', 'getNeighbors' and 'interruptAttachToTangle'.
 **/
public class CvtCommandRequest {

    final String command;

    /**
     * Initializes a new instance of the CvtCommandRequest class.
     */
    protected CvtCommandRequest(CvtAPICommands command) {
        this.command = command.command();
    }

    /**
     * Get information about the node.
     *
     * @return The Node info.
     */
    public static CvtCommandRequest createNodeInfoRequest() {
        return new CvtCommandRequest(CvtAPICommands.GET_NODE_INFO);
    }

    /**
     * Gets the tips of the node.
     *
     * @return The tips of the node.
     */
    public static CvtCommandRequest createGetTipsRequest() {
        return new CvtCommandRequest(CvtAPICommands.GET_TIPS);
    }

    /**
     * Gets the neighbours of the node.
     *
     * @return The list of neighbors.
     */
    public static CvtCommandRequest createGetNeighborsRequest() {
        return new CvtCommandRequest(CvtAPICommands.GET_NEIGHBORS);
    }

    /**
     * Interrupt attaching to the tangle
     *
     * @return
     */
    public static CvtCommandRequest createInterruptAttachToTangleRequest() {
        return new CvtCommandRequest(CvtAPICommands.INTERRUPT_ATTACHING_TO_TANGLE);
    }
}
