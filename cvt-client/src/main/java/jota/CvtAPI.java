package jota;

import jota.error.BaseException;
import jota.pow.ICurl;
import jota.pow.SpongeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * CvtAPI Builder. Usage:
 * <p>
 * {@code CvtApiProxy api = CvtApiProxy.Builder}
 * {@code .protocol("http")}
 * {@code .nodeAddress("localhost")}
 * {@code .port(12345)}
 * {@code .build();}
 *
 * {@code GetNodeInfoResponse response = api.getNodeInfo();}
 *
 */
public class CvtAPI extends CvtAPICore {

    private static final Logger log = LoggerFactory.getLogger(CvtAPI.class);
    private ICurl customCurl;

    protected CvtAPI(Builder builder) {
        super(builder);
        customCurl = builder.customCurl;
    }

    /**
     * Generates a new address from a seed and returns the remainderAddress.
     * This is either done deterministically, or by providing the index of the new remainderAddress.
     * <br/><br/>
     * Deprecated -> Use the new functions {@link #getNextAvailableAddress}, {@link #getAddressesUnchecked} and {@link #generateNewAddresses}
     * @param seed      Tryte-encoded seed. It should be noted that this seed is not transferred.
     * @param security  Security level to be used for the private key / address. Can be 1, 2 or 3.
     * @param index     Key index to start search from. If the index is provided, the generation of the address is not deterministic.
     * @param checksum  Adds 9-tryte address checksum.
     * @param total     Total number of addresses to generate.
     * @param returnAll If <code>true</code>, it returns all addresses which were deterministically generated (until findTransactions returns null).
     * @return GetNewAddressResponse containing an array of strings with the specified number of addresses.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    @Deprecated
    public GetNewAddressResponse getNewAddress(final String seed, int security, final int index, final boolean checksum, final int total, final boolean returnAll) throws ArgumentException {

        // If total number of addresses to generate is supplied, simply generate
        // and return the list of all addresses
        if (total != 0) {
            return getAddressesUnchecked(seed, security, checksum, index, total);
        }

        // If !returnAll return only the last address that was generated
        if (returnAll) {
            return generateNewAddresses(seed, security, checksum, 0, 1, true);
        } else {
            return generateNewAddresses(seed, security, checksum, 0, 1, false);
        }
    }

    /**
     * Checks all addresses until the first unspent address is found. Starts at index 0.
     * @param seed      Tryte-encoded seed. It should be noted that this seed is not transferred.
     * @param security  Security level to be used for the private key / address. Can be 1, 2 or 3.
     * @param checksum  Adds 9-tryte address checksum.
     * @return GetNewAddressResponse containing an array of strings with the specified number of addresses.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public GetNewAddressResponse getNextAvailableAddress(String seed, int security, boolean checksum) throws ArgumentException {
        return generateNewAddresses(seed, security, checksum, 0, 1, false);
    }

    /**
     * Checks all addresses until the first unspent address is found.
     * @param seed      Tryte-encoded seed. It should be noted that this seed is not transferred.
     * @param security  Security level to be used for the private key / address. Can be 1, 2 or 3.
     * @param checksum  Adds 9-tryte address checksum.
     * @param index     Key index to start search from.
     * @return GetNewAddressResponse containing an array of strings with the specified number of addresses.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public GetNewAddressResponse getNextAvailableAddress(String seed, int security, boolean checksum, int index) throws ArgumentException {
        return generateNewAddresses(seed, security, checksum, index, 1, false);
    }

    /**
     * Generates new addresses, meaning addresses which were not spend from, according to the connected node.
     * Starts at index 0, untill <code>amount</code> of unspent addresses are found.
     * @param seed      Tryte-encoded seed. It should be noted that this seed is not transferred.
     * @param security  Security level to be used for the private key / address. Can be 1, 2 or 3.
     * @param checksum  Adds 9-tryte address checksum.
     * @param amount    Total number of addresses to generate.
     * @return GetNewAddressResponse containing an array of strings with the specified number of addresses.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public GetNewAddressResponse generateNewAddresses(String seed, int security, boolean checksum, int amount) throws ArgumentException {
        return generateNewAddresses(seed, security, checksum, 0, amount, false);
    }

    /**
     * Generates new addresses, meaning addresses which were not spend from, according to the connected node.
     * Stops when <code>amount</code> of unspent addresses are found,starting from <code>index</code>
     * @param seed      Tryte-encoded seed. It should be noted that this seed is not transferred.
     * @param security  Security level to be used for the private key / address. Can be 1, 2 or 3.
     * @param checksum  Adds 9-tryte address checksum.
     * @param index     Key index to start search from.
     * @param amount    Total number of addresses to generate.
     * @return GetNewAddressResponse containing an array of strings with the specified number of addresses.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public GetNewAddressResponse generateNewAddresses(String seed, int security, boolean checksum, int index, int amount) throws ArgumentException {
        return generateNewAddresses(seed, security, checksum, 0, amount, false);
    }

    /**
     * Generates new addresses, meaning addresses which were not spend from, according to the connected node.
     * Stops when <code>amount</code> of unspent addresses are found,starting from <code>index</code>
     * @param seed      Tryte-encoded seed. It should be noted that this seed is not transferred.
     * @param security  Security level to be used for the private key / address. Can be 1, 2 or 3.
     * @param checksum  Adds 9-tryte address checksum.
     * @param index     Key index to start search from.
     * @param amount    Total number of addresses to generate.
     * @param addSpendAddresses If <code>true</code>, it returns all addresses, even those who were determined to be spent from
     * @return GetNewAddressResponse containing an array of strings with the specified number of addresses.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public GetNewAddressResponse generateNewAddresses(String seed, int security, boolean checksum, int index, int amount, boolean addSpendAddresses) throws ArgumentException {
        if ((!InputValidator.isValidSeed(seed))) {
            throw new IllegalStateException(Constants.INVALID_SEED_INPUT_ERROR);
        }

        StopWatch stopWatch = new StopWatch();
        List<String> allAddresses = new ArrayList<>();

        for (int i = index, numUnspentFound=0; numUnspentFound < amount; i++) {

            final String newAddress = CvtAPIUtils.newAddress(seed, security, i, checksum, customCurl.clone());
            final FindTransactionResponse response = findTransactionsByAddresses(newAddress);


            if (response.getHashes().length == 0) {
                //Unspent address
                allAddresses.add(newAddress);
                numUnspentFound++;
            } else if (addSpendAddresses) {
                //Spend address, were interested anyways
                allAddresses.add(newAddress);
            }
        }

        return GetNewAddressResponse.create(allAddresses, stopWatch.getElapsedTimeMili());
    }

    /**
     * Generates <code>amount</code> of addresses, starting from <code>index</code>
     * This does not mean that these addresses are safe to use (unspent)
     * @param seed      Tryte-encoded seed. It should be noted that this seed is not transferred.
     * @param security  Security level to be used for the private key / address. Can be 1, 2 or 3.
     * @param checksum  Adds 9-tryte address checksum.
     * @param index     Key index to start search from. The generation of the address is not deterministic.
     * @param amount    Total number of addresses to generate.
     * @return GetNewAddressResponse containing an array of strings with the specified number of addresses.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public GetNewAddressResponse getAddressesUnchecked(String seed, int security, boolean checksum, int index, int amount) throws ArgumentException {
        if ((!InputValidator.isValidSeed(seed))) {
            throw new IllegalStateException(Constants.INVALID_SEED_INPUT_ERROR);
        }

        StopWatch stopWatch = new StopWatch();

        List<String> allAddresses = new ArrayList<>();
        for (int i = index; i < index + amount; i++) {
            allAddresses.add(CvtAPIUtils.newAddress(seed, security, i, checksum, customCurl.clone()));
        }
        return GetNewAddressResponse.create(allAddresses, stopWatch.getElapsedTimeMili());
    }

    /**
     * @param seed            Tryte-encoded seed. It should be noted that this seed is not transferred.
     * @param security        The security level of private key / seed.
     * @param start           Starting key index.
     * @param end             Ending key index.
     * @param inclusionStates If <code>true</code>, it gets the inclusion states of the transfers.
     * @return Bundle of transfers.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public GetTransferResponse getTransfers(String seed, int security, Integer start, Integer end, Boolean inclusionStates) throws ArgumentException {

        // validate seed
        if ((!InputValidator.isValidSeed(seed))) {
            throw new IllegalStateException(Constants.INVALID_SEED_INPUT_ERROR);
        }

        if (start > end || end > (start + 500)) {
            throw new ArgumentException(Constants.INVALID_INPUT_ERROR);
        }

        StopWatch stopWatch = new StopWatch();

        GetNewAddressResponse gnr = getNewAddress(seed, security, start, false, end, true);
        if (gnr != null && gnr.getAddresses() != null) {
            Bundle[] bundles = bundlesFromAddresses(gnr.getAddresses().toArray(new String[gnr.getAddresses().size()]), inclusionStates);
            return GetTransferResponse.create(bundles, stopWatch.getElapsedTimeMili());
        }
        return GetTransferResponse.create(new Bundle[]{}, stopWatch.getElapsedTimeMili());
    }
    /**
     * Internal function to get the formatted bundles of a list of addresses.
     *
     * @param addresses       List of addresses.
     * @param inclusionStates If <code>true</code>, it gets the inclusion states of the transfers.
     * @return A Transaction objects.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public Bundle[] bundlesFromAddresses(String[] addresses, final Boolean inclusionStates) throws ArgumentException {

        List<Transaction> trxs = findTransactionObjectsByAddresses(addresses);
        // set of tail transactions
        List<String> tailTransactions = new ArrayList<>();
        List<String> nonTailBundleHashes = new ArrayList<>();

        for (Transaction trx : trxs) {
            // Sort tail and nonTails
            if (trx.getCurrentIndex() == 0) {
                tailTransactions.add(trx.getHash());
            } else {
                if (nonTailBundleHashes.indexOf(trx.getBundle()) == -1) {
                    nonTailBundleHashes.add(trx.getBundle());
                }
            }
        }

        List<Transaction> bundleObjects = findTransactionObjectsByBundle(nonTailBundleHashes.toArray(new String[nonTailBundleHashes.size()]));
        for (Transaction trx : bundleObjects) {
            // Sort tail and nonTails
            if (trx.getCurrentIndex() == 0) {
                if (tailTransactions.indexOf(trx.getHash()) == -1) {
                    tailTransactions.add(trx.getHash());
                }
            }
        }

        final List<Bundle> finalBundles = new ArrayList<>();
        final String[] tailTxArray = tailTransactions.toArray(new String[tailTransactions.size()]);

        // If inclusionStates, get the confirmation status
        // of the tail transactions, and thus the bundles
        GetInclusionStateResponse gisr = null;
        if (tailTxArray.length != 0 && inclusionStates) {
            gisr = getLatestInclusion(tailTxArray);
            if (gisr == null || gisr.getStates() == null || gisr.getStates().length == 0) {
                throw new IllegalStateException(Constants.GET_INCLUSION_STATE_RESPONSE_ERROR);
            }
        }
        final GetInclusionStateResponse finalInclusionStates = gisr;
        Parallel.For(Arrays.asList(tailTxArray),
                new Parallel.Operation<String>() {
                    public void perform(String param) {

                        try {
                            GetBundleResponse bundleResponse = getBundle(param);
                            Bundle gbr = new Bundle(bundleResponse.getTransactions(), bundleResponse.getTransactions().size());
                            if (gbr.getTransactions() != null) {
                                if (inclusionStates) {
                                    boolean thisInclusion = false;
                                    if (finalInclusionStates != null) {
                                        thisInclusion = finalInclusionStates.getStates()[Arrays.asList(tailTxArray).indexOf(param)];
                                    }
                                    for (Transaction t : gbr.getTransactions()) {
                                        t.setPersistence(thisInclusion);
                                    }
                                }
                                finalBundles.add(gbr);
                            }
                            // If error returned from getBundle, simply ignore it because the bundle was most likely incorrect
                        } catch (ArgumentException e) {
                            log.warn(Constants.GET_BUNDLE_RESPONSE_ERROR);
                        }
                    }
                });

        Collections.sort(finalBundles);
        Bundle[] returnValue = new Bundle[finalBundles.size()];
        for (int i = 0; i < finalBundles.size(); i++) {
            returnValue[i] = new Bundle(finalBundles.get(i).getTransactions(), finalBundles.get(i).getTransactions().size());
        }
        return returnValue;
    }

    /**
     * Wrapper function that stores and broadcasts the specified trytes.
     *
     * @param trytes The trytes.
     * @return A BroadcastTransactionsResponse.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public BroadcastTransactionsResponse storeAndBroadcast(final String... trytes) throws ArgumentException {

        if (!InputValidator.isArrayOfAttachedTrytes(trytes)) {
            throw new ArgumentException(Constants.INVALID_TRYTES_INPUT_ERROR);
        }

        try {
            storeTransactions(trytes);
        } catch (Exception e) {
            throw new ArgumentException(e.toString());
        }
        return broadcastTransactions(trytes);
    }

    /**
     * Facade method: Gets transactions to approve, attaches to Tangle, broadcasts and stores.
     *
     * @param trytes             The trytes.
     * @param depth              The depth.
     * @param minWeightMagnitude The minimum weight magnitude.
     * @param reference          Hash of transaction to start random-walk from, used to make sure the tips returned reference a given transaction in their past.
     * @return Transactions objects.
     * @throws ArgumentException is thrown when invalid trytes is provided.
     */
    public List<Transaction> sendTrytes(final String[] trytes, final int depth, final int minWeightMagnitude, final String reference) throws ArgumentException {
        final GetTransactionsToApproveResponse txs = getTransactionsToApprove(depth, reference);

        // attach to tangle - do pow
        final GetAttachToTangleResponse res = attachToTangle(txs.getTrunkTransaction(), txs.getBranchTransaction(), minWeightMagnitude, trytes);

        try {
            storeAndBroadcast(res.getTrytes());
        } catch (ArgumentException e) {
            return new ArrayList<>();
        }

        final List<Transaction> trx = new ArrayList<>();

        for (final String tryte : Arrays.asList(res.getTrytes())) {
            trx.add(new Transaction(tryte, customCurl.clone()));
        }
        return trx;
    }

    /**
     * Wrapper function for getTrytes and transactionObjects.
     * Gets the trytes and transaction object from a list of transaction hashes.
     *
     * @param hashes The hashes
     * @return Transaction objects.
     **/
    public List<Transaction> findTransactionsObjectsByHashes(String[] hashes) throws ArgumentException {

        if (!InputValidator.isArrayOfHashes(hashes)) {
            throw new IllegalStateException(Constants.INVALID_HASHES_INPUT_ERROR);
        }

        final GetTrytesResponse trytesResponse = getTrytes(hashes);

        final List<Transaction> trxs = new ArrayList<>();

        for (final String tryte : trytesResponse.getTrytes()) {
            trxs.add(new Transaction(tryte, customCurl.clone()));
        }
        return trxs;
    }


}
