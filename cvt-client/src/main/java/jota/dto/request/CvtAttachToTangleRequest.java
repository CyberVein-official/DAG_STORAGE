package jota.dto.request;

import jota.CvtAPICommands;

/**
 * This class represents the core API request 'attachToTangle'.
 *
 * It is used to attach trytes to the tangle.
 **/
public class CvtAttachToTangleRequest extends CvtCommandRequest {

    private String trunkTransaction;
    private String branchTransaction;
    private Integer minWeightMagnitude;
    private String[] trytes;

    /**
     * Initializes a new instance of the CvtAttachedToTangleRequest class.
     */
    private CvtAttachToTangleRequest(final String trunkTransaction, final String branchTransaction, final Integer minWeightMagnitude, final String... trytes) {
        super(CvtAPICommands.ATTACH_TO_TANGLE);
        this.trunkTransaction = trunkTransaction;
        this.branchTransaction = branchTransaction;
        this.minWeightMagnitude = minWeightMagnitude;
        this.trytes = trytes;
    }

    /**
     * Create a new instance of the CvtAttachedToTangleRequest class.
     */
    public static CvtAttachToTangleRequest createAttachToTangleRequest(final String trunkTransaction, final String branchTransaction, final Integer minWeightMagnitude, final String... trytes) {
        return new CvtAttachToTangleRequest(trunkTransaction, branchTransaction, minWeightMagnitude, trytes);
    }

    /**
     * Gets the trunk transaction.
     *
     * @return The trunk transaction.
     */
    public String getTrunkTransaction() {
        return trunkTransaction;
    }

    /**
     * Sets the trunk transaction.
     *
     * @param trunkTransaction The trunk transaction.
     */
    public void setTrunkTransaction(String trunkTransaction) {
        this.trunkTransaction = trunkTransaction;
    }

    /**
     * Gets the branch transaction.
     *
     * @return The branch transaction.
     */
    public String getBranchTransaction() {
        return branchTransaction;
    }

}
