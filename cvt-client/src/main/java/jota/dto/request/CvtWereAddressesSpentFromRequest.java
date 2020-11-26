package jota.dto.request;

import jota.CvtAPICommands;

/**
 * This class represents the core API request 'wereAddressesSpentFrom'.
 *
 * Check if a list of addresses was ever spent from, in the current epoch, or in previous epochs.
 **/
public class CvtWereAddressesSpentFromRequest extends CvtCommandRequest {

    private String[] addresses;

    /**
     * Initializes a new instance of the CvtWereAddressesSpentFromRequest class.
     */
    private CvtWereAddressesSpentFromRequest(String... addresses) {
        super(CvtAPICommands.WERE_ADDRESSES_SPENT_FROM);
        this.addresses = addresses;
    }

    /**
     * Create a new instance of the CvtWereAddressesSpentFromRequest class.
     */
    public static CvtWereAddressesSpentFromRequest create(String... addresses) {
        return new CvtWereAddressesSpentFromRequest(addresses);
    }

    /**
     * Gets the addresses.
     *
     * @return The addresses.
     */
    public String[] getAddresses() {
        return addresses;
    }

    /**
     * Sets the addresses.
     *
     * @param trytes The addresses.
     */
    public void setAddresses(String[] addresses) {
        this.addresses = addresses;
    }
}
