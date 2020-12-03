package jota.dto.request;

import jota.CvtAPICommands;

/**
 * This class represents the core API request 'wereAddressesSpentFrom'.
 * <p>
 * Check if a list of addresses was ever spent from, in the current epoch, or in previous epochs.
 **/
public class CvtAttachNewAddressesRequest extends CvtCommandRequest {

    /**
     * Initializes a new instance of the CvtWereAddressesSpentFromRequest class.
     */
    private CvtAttachNewAddressesRequest(String... addresses) {
        super(CvtAPICommands.ATTACH_NEW_ADDRESS);
    }
}

