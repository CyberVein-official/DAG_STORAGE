package jota.dto.request;

import jota.CvtAPICommands;

/**
 * This class represents the core api request 'getBalances'.
 **/
public class CvtGetBalancesRequest extends CvtCommandRequest {

    private String[] addresses;
    private Integer threshold;
    private String[] tips;

    /**
     * Initializes a new instance of the CvtGetBalancesRequest class.
     */
    private CvtGetBalancesRequest(final Integer threshold, final String[] addresses, final String... tips) {
        super(CvtAPICommands.GET_BALANCES);
        this.addresses = addresses;
        this.threshold = threshold;
        this.tips = tips;
    }

    /**
     * Create a new instance of the CvtGetBalancesRequest class.
     */
    public static CvtGetBalancesRequest createCvtGetBalancesRequest(final Integer threshold, final String[] addresses, final String... tips) {
        return new CvtGetBalancesRequest(threshold, addresses, tips);
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
     * @param addresses The addresses.
     */
    public void setAddresses(String[] addresses) {
        this.addresses = addresses;
    }

    /**
     * Gets the threshold.
     *
     * @return The threshold.
     */
    public Integer getThreshold() {
        return threshold;
    }

    /**
     * Sets the threshold.
     *
     * @param threshold The threshold.
     */
    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    /**
     * Sets the tips to serve as reference for the balance
     *
     * @param tips The starting points we walk back from to find the balance of the addresses
     */
    public void setTips(final String... tips) { 
        this.tips = tips;
    }

    /**
     * Gets the tips
     * 
     * @return the tips
     */
    public String[] getTips() { 
        return tips; 
    }
}

